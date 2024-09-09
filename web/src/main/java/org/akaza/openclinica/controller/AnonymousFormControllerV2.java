/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/api/v2/anonymousform")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AnonymousFormControllerV2 {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;


    /**
     * @api {post} /pages/api/v2/anonymousform/form Retrieve anonymous form URL
     * @apiName getEnketoForm
     * @apiPermission Module participate - enabled
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid
     * @apiParam {String} submissionUri Submission Url
     * @apiGroup Form
     * @apiDescription Retrieve anonymous form url.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "submissionUri": "abcde"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "url": "http://localhost:8006/::YYYi?iframe=true&ecid=abb764d026830e98b895ece6d9dcaf3c5e817983cc00a4ebfaabcb6c3700b4d5",
     *                    "offline": "false"
     *                    }
     */

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public ResponseEntity<AnonymousUrlResponse> getEnketoForm(@RequestBody HashMap<String, String> map) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        EventDefinitionCrfTagService tagService = (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");
        String formUrl = null;
        String studyOid = map.get("studyOid");

        if (!mayProceed(studyOid))
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        String submissionUri = map.get("submissionUri");
        if (submissionUri != "" && submissionUri != null) {


            StudyBean study = getStudy(studyOid);

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
            ArrayList<EventDefinitionCRFBean> edcBeans = edcdao.findAllSubmissionUriAndStudyId(submissionUri, study.getId());
            if (edcBeans.size() != 0) {
                EventDefinitionCRFBean edcBean = edcBeans.get(0);
                CRFDAO crfdao = new CRFDAO(dataSource);
                CRFVersionDAO cvdao = new CRFVersionDAO(dataSource);
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);

                CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                CRFBean crf = (CRFBean) crfdao.findByPK(crfVersionBean.getCrfId());
                StudyBean sBean = (StudyBean) sdao.findByPK(edcBean.getStudyId());
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(edcBean.getStudyEventDefinitionId());

                String tagPath = sedBean.getOid() + "." + crf.getOid();

                boolean isOffline = tagService.getEventDefnCrfOfflineStatus(2,tagPath,true);
                String offline = null;
                if (isOffline) offline = "true";
                else offline = "false";

                formUrl = createAnonymousEnketoUrl(sBean.getOid(), crfVersionBean, edcBean, isOffline);
                AnonymousUrlResponse anonResponse = new AnonymousUrlResponse(formUrl, offline, crf.getName(), crfVersionBean.getDescription());

                return new ResponseEntity<AnonymousUrlResponse>(anonResponse, org.springframework.http.HttpStatus.OK);
            } else {
                return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
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

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private String createAnonymousEnketoUrl(String studyOID, CRFVersionBean crfVersion, EventDefinitionCRFBean edcBean, boolean isOffline) throws Exception {
        StudyBean parentStudyBean = getParentStudy(studyOID);
        PFormCache cache = PFormCache.getInstance(context);
        String enketoURL = cache.getPFormURL(parentStudyBean.getOid(), crfVersion.getOid(), isOffline);
        String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(),edcBean.getStudyEventDefinitionId());
        String url = null;
        if (isOffline) url = enketoURL.split("#",2)[0] + "?" + FORM_CONTEXT + "=" + contextHash + "#" + enketoURL.split("#",2)[1];
        else url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
        return url;

    }
    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE , PENDING , INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus  + "   siteStatus: " + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((dataSource == null) ? 0 : dataSource.hashCode());
		result = prime * result + ((participantPortalRegistrar == null) ? 0 : participantPortalRegistrar.hashCode());
		result = prime * result + ((sdao == null) ? 0 : sdao.hashCode());
		result = prime * result + ((udao == null) ? 0 : udao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnonymousFormControllerV2 other = (AnonymousFormControllerV2) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (dataSource == null) {
			if (other.dataSource != null)
				return false;
		} else if (!dataSource.equals(other.dataSource))
			return false;
		if (participantPortalRegistrar == null) {
			if (other.participantPortalRegistrar != null)
				return false;
		} else if (!participantPortalRegistrar.equals(other.participantPortalRegistrar))
			return false;
		if (sdao == null) {
			if (other.sdao != null)
				return false;
		} else if (!sdao.equals(other.sdao))
			return false;
		if (udao == null) {
			if (other.udao != null)
				return false;
		} else if (!udao.equals(other.udao))
			return false;
		return true;
	}

	private class AnonymousUrlResponse {
        private String url = null;
        private String offline = null;
        private String name = null;
        private String description = null;

        public AnonymousUrlResponse(String url, String offline, String name, String description) {
            this.url = url;
            this.offline = offline;
            this.name = name;
            this.description = description;
        }

		@Override
		public String toString() {
			return "AnonymousUrlResponse [url=" + url + ", offline=" + offline + ", name=" + name + ", description="
					+ description + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((offline == null) ? 0 : offline.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnonymousUrlResponse other = (AnonymousUrlResponse) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (offline == null) {
				if (other.offline != null)
					return false;
			} else if (!offline.equals(other.offline))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}

		private AnonymousFormControllerV2 getEnclosingInstance() {
			return AnonymousFormControllerV2.this;
		}
    }

}
