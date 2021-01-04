/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/* 
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research 
 */
package org.akaza.openclinica.ws.ccts;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.akaza.openclinica.ws.logic.CctsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Krikor Krumlian
 *
 */
@Endpoint
public class CctsSubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/ccts/subject/v1";
    private final String SUCCESS_MESSAGE = "success";
    private String dateFormat;

    /**
     * Constructor
     * @param subjectService
     * @param cctsService
     */
    public CctsSubjectEndpoint(SubjectServiceInterface subjectService, CctsService cctsService) {
    	// TODO arguments not used
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "commitRequest", namespace = NAMESPACE_URI_V1)
    public Source createSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        // TODO: Add Logic
        logger.debug("In CreateSubject");
        return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "rollbackRequest", namespace = NAMESPACE_URI_V1)
    public Source rollBackSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        // TODO: Add Logic 
        return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
    }

    /**
     * Create Response 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "commitResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        return responseElement;

    }

    /**
     * @return
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

}