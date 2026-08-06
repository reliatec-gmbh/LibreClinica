/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
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
 *   - sendEmail() and randomizeStudy() were not ported: a study is registered
 *     in the randomization module's own administration, not from LibreClinica,
 *     so neither had a caller.
 *   - added a connect timeout next to the existing read timeout, and turned off
 *     the automatic retries of the underlying HttpClient, so that the Build
 *     Study page still renders when the module is unreachable. Without the
 *     second part the default retry handler repeats the request three times
 *     and the page waits for four connect timeouts instead of one.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 */
package org.akaza.openclinica.service.pmanage;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Modified: CommonsClientHttpRequestFactory (Spring 3 / commons-httpclient 3.x) replaced with
//           HttpComponentsClientHttpRequestFactory (Spring 5 / httpclient 4.5.7)
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RandomizationRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int RANDOMIZATION_READ_TIMEOUT = 5000;
    public static final int RANDOMIZATION_CONNECT_TIMEOUT = 5000;
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
        // Modified: use HttpComponentsClientHttpRequestFactory instead of CommonsClientHttpRequestFactory.
        // The client is built here rather than taken from the factory default because the default one
        // retries a failed request three times, so an unreachable module would cost four connect
        // timeouts. useSystemProperties() keeps the rest of the default client's behaviour.
        CloseableHttpClient httpClient = HttpClients.custom().useSystemProperties().disableAutomaticRetries().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        // Modified: without a connect timeout an unreachable module blocks the Build Study page
        // until the operating system gives up on the TCP connection.
        requestFactory.setConnectTimeout(RANDOMIZATION_CONNECT_TIMEOUT);
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
}
