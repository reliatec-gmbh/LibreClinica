/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://www.libreclinica.org/download.html#headLicense
 *
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 * copyright (C) 2026 UMIN (University Hospital Medical Information Network)
 *
 * Author:  Yoshiteru Chiba
 * Notice:  Developed under a service-agreement contract with UMIN;
 *          copyright in this port has been assigned to UMIN. The
 *          author retains moral rights inalienable under Article 59
 *          of the Japanese Copyright Act. See README for full
 *          attribution.
 *
 * Modifications:
 *   - Ported from OpenClinica v3.x; date: 2026-03-11 - 2026-03-12.
 *   - CommonsClientHttpRequestFactory replaced with
 *     HttpComponentsClientHttpRequestFactory
 *     (commons-httpclient 3.x -> httpclient 4.5.7, required by Spring 5).
 *   - org.json.JSONObject replaced with
 *     com.fasterxml.jackson.databind.JsonNode / ObjectMapper
 *     (spring-security-oauth2 1.x JSON removed in Spring 5 environment).
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 */
package org.akaza.openclinica.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.StratificationFactorBean;
import org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// Modified: use HttpComponentsClientHttpRequestFactory (Spring 5 compatible)
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
// Modified: use Jackson instead of org.json.JSONObject (not available in Spring 5 environment)
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
// Modified: SSL certificate verification skip (self-signed cert support)
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;

public class RandomizeService extends RandomizationRegistrar {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;
    private EventCRFDAO eventCRFDAO;
    private ItemDataDAO itemDataDAO;
    private ItemDAO itemDAO;
    private ItemGroupDAO itemGroupDAO;
    private SectionDAO sectionDAO;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private ItemGroupMetadataDAO itemGroupMetadataDAO;
    private StudyEventDAO studyEventDAO;
    private EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private ExpressionService expressionService;
    // Modified: SSL verification skip for self-signed certificate environments
    HttpComponentsClientHttpRequestFactory requestFactory = createSslSkipRequestFactory();
    public static final int RANDOMIZATION_READ_TIMEOUT = 10000;
    StudyDAO sdao = null;

    private static HttpComponentsClientHttpRequestFactory createSslSkipRequestFactory() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(builder.build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        } catch (Exception e) {
            return new HttpComponentsClientHttpRequestFactory();
        }
    }

    public RandomizeService(DataSource ds) {
        this.ds = ds;
        this.expressionService = new ExpressionService(ds);
    }

    public String getRandomizationCode(EventCRFBean eventCrfBean, List<StratificationFactorBean> stratificationFactorBeans, RuleSetBean ruleSet) {
        // Modified: LibreClinica DAOs are not generic (type parameters removed from LC fork)
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        String identifier = ssBean.getOid();
        StudyDAO sdao = new StudyDAO(ds);
        StudyBean sBean = (StudyBean) sdao.findByPK(ssBean.getStudyId());
        String siteIdentifier = sBean.getOid();
        String name = sBean.getName();
        UserAccountDAO udao = new UserAccountDAO(ds);
        int userId = 0;
        if (eventCrfBean.getUpdaterId() == 0) {
            userId = eventCrfBean.getOwnerId();
        } else {
            userId = eventCrfBean.getUpdaterId();
        }
        UserAccountBean uBean = (UserAccountBean) udao.findByPK(userId);
        String user = uBean.getName();

        StudyBean study = getParentStudy(sBean.getOid());
        SeRandomizationDTO randomization = null;

        try {
            randomization = getCachedRandomizationDTOObject(study.getOid(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String randomiseUrl = randomization.getUrl();
        String username = randomization.getUsername();
        String password = randomization.getPassword();
        String timezone = "America/New_York";

        HttpHeaders headers = createHeaders(username, password);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Modified: use JsonNode instead of JSONObject
        JsonNode jsonRandObject = retrieveARandomisation(randomiseUrl, ssBean, headers);
        if (jsonRandObject != null) {
            return jsonRandObject.path("code").asText();
        } else {
            addOrUpdateASite(randomiseUrl, sBean, headers, timezone);
            JsonNode jsonRandomisedObject = randomiseSubject(randomiseUrl, ssBean, sBean, headers, user, stratificationFactorBeans, eventCrfBean, ruleSet);
            if (jsonRandomisedObject != null)
                return jsonRandomisedObject.path("code").asText();
            else
                return "";
        }
    }

    private String getExpressionValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String expression = getExpressionService().constructFullExpressionIfPartialProvided(expr, ruleSet.getTarget().getValue());
        ItemDataBean itemData = null;
        if (expression != null && !expression.isEmpty()) {
            ItemBean itemBean = getExpressionService().getItemBeanFromExpression(expression);
            String itemGroupBOrdinal = getExpressionService().getGroupOrdninalCurated(expression);
            itemData = getItemDataDAO().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(), eventCrfBean.getId(),
                    itemGroupBOrdinal == "" ? 1 : Integer.valueOf(itemGroupBOrdinal));
        }
        return itemData.getValue();
    }

    private String getStudySubjectAttrValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String value = "";
        // Modified: LibreClinica DAOs are not generic
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        SubjectDAO subdao = new SubjectDAO(ds);
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(ds);
        StudyGroupDAO sgdao = new StudyGroupDAO(ds);
        StudyDAO sdao = new StudyDAO(ds);
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        SubjectBean subjectBean = (SubjectBean) subdao.findByPK(ssBean.getSubjectId());

        String prefix = "STUDYGROUPCLASSLIST";
        String param = expr.split("\\.", -1)[1].trim();

        if (param.equalsIgnoreCase("BIRTHDATE")) {
            value = subjectBean.getDateOfBirth().toString();
        } else if (param.equalsIgnoreCase("SEX")) {
            if (String.valueOf(subjectBean.getGender()).equals("m"))
                value = "Male";
            else
                value = "Female";
        } else if (param.startsWith(prefix)) {
            String gcName = param.substring(21, param.indexOf("\"]"));
            StudyGroupBean sgBean = sgdao.findSubjectStudyGroup(ssBean.getId(), gcName);
            if (sgBean != null)
                value = sgBean.getName();
        }
        return value;
    }

    // Modified: return JsonNode instead of JSONObject
    private JsonNode retrieveARandomisation(String randomiseUrl, StudySubjectBean studySubject, HttpHeaders headers) {
        randomiseUrl = randomiseUrl + "/api/randomisation?identifier=" + studySubject.getOid();
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        String body = null;
        JsonNode jsonNode = null;
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.GET, request, String.class);
            body = response.getBody();
            // Modified: use Jackson ObjectMapper instead of new JSONObject(body)
            jsonNode = new ObjectMapper().readTree(body);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return jsonNode;
    }

    private void addOrUpdateASite(String randomiseUrl, StudyBean studyBean, HttpHeaders headers, String timezone) {
        randomiseUrl = randomiseUrl + "/api/sites";
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        MultiValueMap<String, String> siteMap = new LinkedMultiValueMap<>();
        siteMap.add("siteIdentifier", studyBean.getOid());
        siteMap.add("name", studyBean.getName());
        siteMap.add("timezone", timezone);
        HttpEntity<MultiValueMap<String, String>> siteRequest = new HttpEntity<>(siteMap, headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.POST, siteRequest, String.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    // Modified: return JsonNode instead of JSONObject
    private JsonNode randomiseSubject(String randomiseUrl, StudySubjectBean studySubject, StudyBean studyBean, HttpHeaders headers, String user,
            List<StratificationFactorBean> stratificationFactorBeans, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        int i = 1;
        String exp = "";

        randomiseUrl = randomiseUrl + "/api/randomise";
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        MultiValueMap<String, String> subjectMap = new LinkedMultiValueMap<>();
        subjectMap.add("identifier", String.valueOf(studySubject.getOid()));
        subjectMap.add("siteIdentifier", studyBean.getOid());
        subjectMap.add("user", user);
        for (StratificationFactorBean stratificationFactorBean : stratificationFactorBeans) {
            exp = stratificationFactorBean.getStratificationFactor().getValue();
            if (exp.startsWith("SS.")) {
                subjectMap.add("question" + i, getStudySubjectAttrValue(exp, eventCrfBean, ruleSet));
            } else {
                String output = getExpressionValue(exp, eventCrfBean, ruleSet);
                subjectMap.add("question" + i, output);
            }
            i++;
        }

        String body = null;
        JsonNode jsonNode = null;
        HttpEntity<MultiValueMap<String, String>> subjectRequest = new HttpEntity<>(subjectMap, headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.POST, subjectRequest, String.class);
            body = response.getBody();
            // Modified: use Jackson ObjectMapper instead of new JSONObject(body)
            jsonNode = new ObjectMapper().readTree(body);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return jsonNode;
    }

    HttpHeaders createHeaders(final String username, final String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
                String authHeader = "Basic " + new String(encodedAuth);
                set("Authorization", authHeader);
            }
        };
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public ItemDataDAO getItemDataDAO() {
        return new ItemDataDAO(ds);
    }

    public void setItemDataDAO(ItemDataDAO itemDataDAO) {
        this.itemDataDAO = itemDataDAO;
    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }
    }
}
