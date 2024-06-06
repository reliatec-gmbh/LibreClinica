/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.pform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.servlet.ServletContext;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;

public class PFormCache {
    
    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String,HashMap<String, String>> urlCache = null;
    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String,HashMap<String, String>> offlineUrlCache = null;
    // HashMap of context hash, HashMap of properties such as study subject oid, crf version oid, etc...
    HashMap<String, HashMap<String, String>> subjectContextCache = null;

    @SuppressWarnings("unused")
    private PFormCache() {
        // NOOP
    }

    @SuppressWarnings("unchecked")
	private PFormCache(ServletContext context) {

        // Retrieve cache HashMaps from servlet context
        urlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformURLCache");
        offlineUrlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformOfflineURLCache");
        subjectContextCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("subjectContextCache");

        // When not available in servlet context, instantiate fresh ones
        if (urlCache == null) {
            urlCache = new HashMap<>();
            context.setAttribute("pformURLCache", urlCache);
        }
        if (offlineUrlCache == null) {
            offlineUrlCache = new HashMap<>();
            context.setAttribute("pformOfflineURLCache", offlineUrlCache);
        }
        if (subjectContextCache == null)  {
            subjectContextCache = new HashMap<>();
            context.setAttribute("subjectContextCache", subjectContextCache);
        }
    }

    public static PFormCache getInstance(ServletContext context) throws Exception {
        return new PFormCache(context);        
    }

    public String getPFormURL(String studyOID, String crfVersionOID) throws Exception  {
        return getPFormURL(studyOID, crfVersionOID, false);
    }

    public String getPFormURL(String studyOID, String crfVersionOID, boolean isOffline) throws Exception {

        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));

        // Retrieve study form URLs from cache
        HashMap<String, String> studyURLs = isOffline ? offlineUrlCache.get(studyOID) : urlCache.get(studyOID);

        // When study URLs not available in cache, retrieve them then first from Enketo
        if (studyURLs == null) {

            // Retrieve map of CRF Version OID to Enketo Form URLs for caching
            studyURLs = new HashMap<>();
            String url = isOffline ? enketo.getOfflineFormURL(crfVersionOID) : enketo.getFormURL(crfVersionOID);
            if (url.isEmpty()) {
                throw new Exception("Unable to get enketo form url.");
            }
            studyURLs.put(crfVersionOID, url);

            // Create a cache
            if (isOffline) {
                offlineUrlCache.put(studyOID, studyURLs);
            } else {
                urlCache.put(studyOID, studyURLs);
            }
            
            return url;

        } else if (studyURLs.get(crfVersionOID) == null) { // When specific CRF Version does not exist in cache

            String url = isOffline ? enketo.getOfflineFormURL(crfVersionOID) : enketo.getFormURL(crfVersionOID);
            studyURLs.put(crfVersionOID, url);

            return url;
        }
        else { // Otherwise retrieve the Enketo form URL from cache
            return studyURLs.get(crfVersionOID);
        }
    }

    public HashMap<String,String> getSubjectContext(String key) {
        return subjectContextCache.get(key);
    }

    public String putSubjectContext(String studySubjectOID,
                                    String studyEventDefinitionID,
                                    String studyEventOrdinal,
                                    String crfVersionOID) throws NoSuchAlgorithmException {
        
        // Build parameter map
        HashMap<String,String> contextMap = new HashMap<>();
        contextMap.put("studySubjectOID", studySubjectOID);
        contextMap.put("studyEventDefinitionID", studyEventDefinitionID);
        contextMap.put("studyEventOrdinal", studyEventOrdinal);
        contextMap.put("crfVersionOID", crfVersionOID);

        // Information to hash
        String hashString = studySubjectOID + "." + studyEventDefinitionID + "." + studyEventOrdinal + "." + crfVersionOID;

        // Hashing and hex encoding
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Utf8.encode(hashString));
        String hashOutput = new String(Hex.encode(digest.digest()));

        // Use hash as key for the parameter map (will be needed for data import)
        subjectContextCache.put(hashOutput, contextMap);

        return hashOutput;
    }
    
    public String putAnonymousFormContext(String studyOID,
                                          String crfVersionOID ,
                                          int studyEventDefinitionId) throws NoSuchAlgorithmException  {

        // Build parameter map
        HashMap<String,String> contextMap = new HashMap<>();
        contextMap.put("studySubjectOID", null); // for every submit new study subject will be created
        contextMap.put("studyOID", studyOID);
        contextMap.put("crfVersionOID", crfVersionOID);
        contextMap.put("studyEventDefinitionID", String.valueOf(studyEventDefinitionId));
        contextMap.put("studyEventOrdinal", "1");

        // Information to hash
        String hashString = studyOID + "." + crfVersionOID;

        // Hashing and hex encoding
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Utf8.encode(hashString));
        String hashOutput = new String(Hex.encode(digest.digest()));

        // Use hash as key for the parameter map (will be needed for data import)
        subjectContextCache.put(hashOutput, contextMap);

        return hashOutput;
    }

}
