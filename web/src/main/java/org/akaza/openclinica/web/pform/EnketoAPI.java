/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform;

import java.net.URL;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("deprecation")
public class EnketoAPI {

    private String enketoURL = null;
    private String token = null;
    private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EnketoAPI(EnketoCredentials credentials) {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
    }

    public String getOfflineFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/offline");
        EnketoURLResponse response = getURL(eURL, crfOID);
        if (response != null) {
            String myUrl = response.getOffline_url();
            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return myUrl;
        } else
            return "";
    }

    public String getFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/iframe");
        EnketoURLResponse response = getURL(eURL, crfOID);
        if (response != null) {
            String myUrl = response.getIframe_url();
            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return myUrl;
        } else
            return "";
    }

    public String getFormPreviewURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v1/survey/preview");
        EnketoURLResponse response = getURL(eURL, crfOID);
        if (response != null)
            return response.getPreview_url();
        else
            return "";
    }

    private EnketoURLResponse getURL(URL url, String crfOID) {
        try {
            String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Basic " + userPasswdCombo);
            headers.add("Accept-Charset", "UTF-8");
            EnketoURLRequest body = new EnketoURLRequest(ocURL, crfOID);
            HttpEntity<EnketoURLRequest> request = new HttpEntity<EnketoURLRequest>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoURLResponse> response = rest.postForEntity(url.toString(), request, EnketoURLResponse.class);
            if (response != null)
                return response.getBody();
            else
                return null;

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public EnketoURLResponse getEditURL(String crfOid, String instance, String ecid, String redirect) {
        if (enketoURL == null)
            return null;

        try {
            // Build instanceId to cache populated instance at Enketo with
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String hashString = ecid + "." + String.valueOf(cal.getTimeInMillis());
            MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA-256");
            String instanceId = encoder.encode(hashString);

            URL eURL = new URL(enketoURL + "/api/v1/instance/iframe");
            String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Basic " + userPasswdCombo);
            headers.add("Accept-Charset", "UTF-8");
            EnketoEditURLRequest body = new EnketoEditURLRequest(ocURL, crfOid, instanceId, redirect, instance);
            HttpEntity<EnketoEditURLRequest> request = new HttpEntity<EnketoEditURLRequest>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoURLResponse> response = rest.postForEntity(eURL.toString(), request, EnketoURLResponse.class);
            if (response != null)
                return response.getBody();
            else
                return null;

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

}