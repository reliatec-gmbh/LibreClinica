/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.web.restful;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.view.Viewable;

/***
 * Rest service for ODM clinical data usage
 * ROOT_CONTEXT/rest/clinicaldata/{format}/{mode}/{STUDYOID} format:xml/ json
 * mode:view
 *
 * @author jnyayapathi
 */
@Path("/clinicaldata")
@Component
@Scope("prototype")
public class ODMClinicaDataResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ODMClinicaDataResource.class);
	private static final int INDENT_LEVEL = 2;

	private ClinicalDataCollectorResource clinicalDataCollectorResource;
	private MetadataCollectorResource metadataCollectorResource;
	private DataSource dataSource;

	public MetadataCollectorResource getMetadataCollectorResource() {
		return metadataCollectorResource;
	}

	public void setMetadataCollectorResource(MetadataCollectorResource metadataCollectorResource) {
		this.metadataCollectorResource = metadataCollectorResource;
	}

	public ClinicalDataCollectorResource getClinicalDataCollectorResource() {
		return clinicalDataCollectorResource;
	}

	public void setClinicalDataCollectorResource(ClinicalDataCollectorResource clinicalDataCollectorResource) {
		this.clinicalDataCollectorResource = clinicalDataCollectorResource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @api {get} /rest/clinicaldata/html/print/:study/:subject/:event/:form Retrieve case report forms - HTML
	 * @apiVersion 3.8.0
	 * @apiName getPrintCRFController
	 * @apiGroup Subject
	 * @apiPermission user
	 *
	 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
	 *
	 * @apiParam {String} study Study or Site OID.
	 * @apiParam {String} subject Subject Key or ID.
	 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
	 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
	 *
	 * @apiError NoAccessRight Only authenticated users can access the data.
	 * @apiError NotFound   The resource was not found.
	 *
	 * @apiErrorExample Response (example):
	 *     HTTP/1.1 401 Not Authenticated
	 *     {
	 *       "error": "NoAccessRight"
	 *     }
	 */
	@GET
	@Path("/html/print/{studyOID}/{studySubjectIdentifier}/{eventOID}/{formVersionOID}")
	public Viewable getPrintCRFController(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
	  		@PathParam("studyOID") String studyOID,
		  	@PathParam("studySubjectIdentifier") String studySubjectIdentifier,
		  	@PathParam("eventOID") String eventOID,
		  	@PathParam("formVersionOID") String formVersionOID,
		  	@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
		  	@DefaultValue("n") @QueryParam("includeAudits") String includeAudits) throws Exception {

		request.setCharacterEncoding("UTF-8");
		request.setAttribute("studyOID", studyOID);
		request.setAttribute("studySubjectOID", getStudySubjectOID(studySubjectIdentifier, studyOID));
		request.setAttribute("eventOID", eventOID);
		request.setAttribute("formVersionOID", formVersionOID);
		request.setAttribute("includeAudits", includeAudits);
		request.setAttribute("includeDNs", includeDns);

		return new Viewable("/WEB-INF/jsp/printcrf.jsp", null);
	}

	/**
	 * @api {get} /rest/clinicaldata/json/view/:study/:subject/:event/:form Retrieve case report forms - JSON
	 * @apiVersion 3.8.0
	 * @apiName getODMClinicaldataJSON
	 * @apiGroup Subject
	 * @apiPermission user
	 *
	 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
	 *
	 * @apiParam {String} study Study or Site OID.
	 * @apiParam {String} subject Subject Key or ID.
	 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
	 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
	 *
	 * @apiError NoAccessRight Only authenticated users can access the data.
	 * @apiError NotFound   The resource was not found.
	 */
	@GET
	@Path("/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getODMClinicaldata(@Context HttpServletRequest request,
		    @PathParam("studyOID") String studyOID,
 			@PathParam("studySubjectIdentifier") String studySubjectIdentifier,
		 	@PathParam("studyEventOID") String studyEventOID,
		    @PathParam("formVersionOID") String formVersionOID,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits) {

		LOGGER.debug("Requesting JSON clinical data resource");

		FullReportBean report = getFullReport(request, studyOID, studySubjectIdentifier, studyEventOID, formVersionOID, includeDns, includeAudits);
		if (report.getClinicalDataMap() == null) { return null; }
		report.createOdmXml(true);

		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setTypeHintsEnabled(true);

		JSON json = xmlSerializer.read(report.getXmlOutput().toString().trim());
		JSONClinicalDataPostProcessor processor = new JSONClinicalDataPostProcessor(request.getLocale());
        processor.process(json);

		return json.toString(INDENT_LEVEL);
	}

	/**
	 * @api {get} /rest/clinicaldata/xml/view/:study/:subject/:event/:form Retrieve case report forms - XML
	 * @apiVersion 3.8.0
	 * @apiName getODMMetadata
	 * @apiGroup Subject
	 * @apiPermission user
	 *
	 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
	 *
	 * @apiParam {String} study Study or Site OID.
	 * @apiParam {String} subject Subject Key or ID.
	 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
	 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
	 *
	 * @apiError NoAccessRight Only authenticated users can access the data.
	 * @apiError NotFound   The resource was not found.
	 *
	 * @apiErrorExample Response (example):
	 *     HTTP/1.1 401 Not Authenticated
	 *     {
	 *       "error": "NoAccessRight"
	 *     }
	 */
	@GET
	@Path("/xml/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadata(@Context HttpServletRequest request,
			@PathParam("studyOID") String studyOID,
			@PathParam("studySubjectIdentifier") String studySubjectIdentifier,
			@PathParam("studyEventOID") String studyEventOID,
			@PathParam("formVersionOID") String formVersionOID,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits) {

		LOGGER.debug("Requesting XML clinical data resource");

		FullReportBean report = getFullReport(request, studyOID, studySubjectIdentifier, studyEventOID, formVersionOID, includeDns, includeAudits);
		report.createOdmXml(true);

		LOGGER.debug(report.getXmlOutput().toString().trim());

		return report.getXmlOutput().toString().trim();
	}

	private FullReportBean getFullReport(HttpServletRequest request,
			String studyOID,
			String studySubjectIdentifier,
		 	String studyEventOID,
			String formVersionOID,
		 	String includeDns,
			String includeAudits) {

		boolean includeDN = false;
		boolean includeAudit = false;
		if (includeDns.equalsIgnoreCase("yes") || includeDns.equalsIgnoreCase("y")) { includeDN = true; }
		if (includeAudits.equalsIgnoreCase("yes") || includeAudits.equalsIgnoreCase("y")) { includeAudit = true; }

		int userId = ((UserAccountBean) request.getSession().getAttribute("userBean")).getId();

        return getMetadataCollectorResource().collectODMMetadataForClinicalData(
            studyOID,
            formVersionOID,
            getClinicalDataCollectorResource().generateClinicalData(
                studyOID,
                getStudySubjectOID(studySubjectIdentifier, studyOID),
                studyEventOID,
                formVersionOID,
                includeDN,
                includeAudit,
                request.getLocale(),
                userId
            )
        );
	}

	private boolean isStudySubjectAccessible(StudyBean requestedStudy, StudyBean assignedStudy) {

		boolean isAssignedToRequestedStudy =
				requestedStudy != null &&
				requestedStudy.getId() == assignedStudy.getId();

		boolean isAssignedToSiteOfRequestedParentStudy =
				requestedStudy != null &&
				requestedStudy.getParentStudyId() == 0 &&
				requestedStudy.getId() == assignedStudy.getParentStudyId();

		return isAssignedToRequestedStudy || isAssignedToSiteOfRequestedParentStudy;
	}

	/**
	 *  This function checks to see whether the supplied subject identifier is a Study Subject OID or a Study Subject ID.
	 *  Additionally, it verifies subject assignment to requested study or site
	 *  If a valid Study Subject OID or wildcard (*) is supplied, just return it.
	 *  If a Study Subject ID is supplied, a lookup is done to get the Study Subject OID and return that.
	 */
	private String getStudySubjectOID(String subjectIdentifier, String studyOID) {

		// Wildcard to retrieve all study subjects
		if (subjectIdentifier.equals("*")) { return subjectIdentifier; }

		// Database access objects
		StudyDAO studyDAO = new StudyDAO(getDataSource());
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(getDataSource());

		// Find requested study (parent study or site)
		StudyBean requestedStudy = studyDAO.findByOid(studyOID);
		// Find study subject by OID
		StudySubjectBean studySubject = studySubjectDAO.findByOid(subjectIdentifier);

		if (studySubject != null && studySubject.getOid() != null) {
			// When matching study subject was found verify the assignment to requested study or site

			// Find study or site where study subject is assigned currently
			StudyBean assignedStudy = studyDAO.findByPK(studySubject.getStudyId());

			if (!isStudySubjectAccessible(requestedStudy, assignedStudy)) {
				LOGGER.warn("Subject with study subject OID {} is not in study {}; request refused", subjectIdentifier, studyOID);
				throw new WebApplicationException(Response.Status.FORBIDDEN);
			}

			return studySubject.getOid();
		} else {
			// Otherwise try to find study subject by study subject ID (label)
			studySubject = studySubjectDAO.findByLabelAndStudy(subjectIdentifier, requestedStudy);

			if (studySubject != null && studySubject.getOid() != null) {
				// When matching study subject was found verify the assignment to requested study or site

				// Find study or site where study subject is assigned currently
				StudyBean assignedStudy = studyDAO.findByPK(studySubject.getStudyId());

				if (!isStudySubjectAccessible(requestedStudy, assignedStudy)) {
					LOGGER.warn("Subject with study subject ID {} is not in study {}; request refused", subjectIdentifier, studyOID);
					throw new WebApplicationException(Response.Status.FORBIDDEN);
				}

				return studySubject.getOid();
			}

			// When study subject was  not found return the requested identifier string
			return subjectIdentifier;
		}
	}

}
