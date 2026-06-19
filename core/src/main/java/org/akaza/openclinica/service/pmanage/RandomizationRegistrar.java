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
 *   - LibreClinica names these properties "sysURL"/"moduleManager"; added a
 *     fallback that strips "MainMenu" from sysURL when sysURL.base is absent,
 *     plus a null-guard for moduleManager to prevent NPE.
 *   - getCachedRandomizationDTOObject: added null-guard for
 *     seRandomizationDTO to prevent NPE.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 */
package org.akaza.openclinica.service.pmanage;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Modified: CommonsClientHttpRequestFactory (Spring 3 / commons-httpclient 3.x) replaced with
//           HttpComponentsClientHttpRequestFactory (Spring 5 / httpclient 4.5.7)
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

public class RandomizationRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int RANDOMIZATION_READ_TIMEOUT = 5000;
    private static final String CACHE_KEY = "randomizeObject";
    private CacheManager cacheManager;
    private net.sf.ehcache.Cache cache;

    public RandomizationRegistrar() {
        cacheManager = CacheManager.getInstance();
        this.cache = cacheManager.getCache(CACHE_KEY);
    }

    public SeRandomizationDTO getRandomizationDTOObject(String studyOid) {
        // LibreClinica compatibility: derive the base URL from sysURL when sysURL.base is unset.
        String sysUrlBase = CoreResources.getField("sysURL.base");
        if (sysUrlBase == null || sysUrlBase.trim().isEmpty()) {
            String sysUrl = CoreResources.getField("sysURL");
            if (sysUrl != null && !sysUrl.trim().isEmpty()) {
                int idx = sysUrl.indexOf("MainMenu");
                sysUrlBase = (idx > 0) ? sysUrl.substring(0, idx) : sysUrl;
                if (!sysUrlBase.endsWith("/")) sysUrlBase += "/";
            } else {
                sysUrlBase = "";
            }
            logger.info("sysURL.base not set, derived from sysURL: {}", sysUrlBase);
        }
        String moduleManager = CoreResources.getField("moduleManager");
        if (moduleManager == null || moduleManager.trim().isEmpty()) {
            logger.error("moduleManager property is not set in datainfo.properties. Cannot retrieve randomization config.");
            return null;
        }
        String ocUrl = sysUrlBase + "rest2/openrosa/" + studyOid;
        String randomizationUrl = moduleManager + "/app/rest/oc/se_randomizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        // Modified: use HttpComponentsClientHttpRequestFactory instead of CommonsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.getForObject(randomizationUrl, SeRandomizationDTO.class);
            if (response.getStudyOid() != null) {
                return response;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public SeRandomizationDTO getCachedRandomizationDTOObject(String studyOid, Boolean resetCache) throws Exception {
        SeRandomizationDTO seRandomizationDTO = null;
        // LibreClinica compatibility: derive the base URL from sysURL when sysURL.base is unset.
        String sysUrlBase = CoreResources.getField("sysURL.base");
        if (sysUrlBase == null || sysUrlBase.trim().isEmpty()) {
            String sysUrl = CoreResources.getField("sysURL");
            if (sysUrl != null && !sysUrl.trim().isEmpty()) {
                int idx = sysUrl.indexOf("MainMenu");
                sysUrlBase = (idx > 0) ? sysUrl.substring(0, idx) : sysUrl;
            } else {
                sysUrlBase = "";
            }
        }
        String mapKey = sysUrlBase + studyOid;
        Element element = cache.get(mapKey);
        if (element != null && element.getObjectValue() != null && !resetCache) {
            seRandomizationDTO = (SeRandomizationDTO) element.getObjectValue();
        }
        if (seRandomizationDTO == null) {
            seRandomizationDTO = getRandomizationDTOObject(studyOid);
        }
        if (seRandomizationDTO != null) {
            cache.put(new Element(mapKey, seRandomizationDTO));
        }
        return seRandomizationDTO;
    }

    public void sendEmail(JavaMailSenderImpl mailSender, UserAccountBean user, String emailSubject, String message) throws OpenClinicaSystemException {
        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo(user.getEmail());
            helper.setSubject(emailSubject);
            helper.setText(message);
            mailSender.send(mimeMessage);
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.error("Email could not be sent");
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
        }
    }

    public String randomizeStudy(String studyOid, String studyName, UserAccountBean userAccount) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String randomizationUrl = CoreResources.getField("moduleManager") + "/app/rest/oc/se_randomizations";
        SeRandomizationDTO seRandomizationDTO = new SeRandomizationDTO();
        seRandomizationDTO.setStudyOid(studyOid);
        seRandomizationDTO.setInstanceUrl(ocUrl);
        seRandomizationDTO.setOcUser_username(userAccount.getName());
        seRandomizationDTO.setOcUser_name(userAccount.getFirstName());
        seRandomizationDTO.setOcUser_lastname(userAccount.getLastName());
        seRandomizationDTO.setOcUser_emailAddress(userAccount.getEmail());
        seRandomizationDTO.setStudyName(studyName);
        seRandomizationDTO.setOpenClinicaVersion(CoreResources.getField("OpenClinica.version"));

        // Modified: use HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.postForObject(randomizationUrl, seRandomizationDTO, SeRandomizationDTO.class);
            if (response != null && response.getStatus() != null)
                return response.getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }
}
