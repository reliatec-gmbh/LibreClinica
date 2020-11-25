/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.service;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameter;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import javax.sql.DataSource;

public class StudyConfigService {

    private DataSource ds;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public StudyConfigService(DataSource ds) {
        this.ds = ds;
    }

    /**
     * @return Returns the ds.
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * @param ds
     *            The ds to set.
     */
    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    /**
     * This method construct an object which has all the study parameter values
     *
     * @param study
     * @return
     */
    public StudyBean setParametersForStudy(StudyBean study) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        ArrayList parameters = spvdao.findAllParameters();
        StudyParameterConfig spc = new StudyParameterConfig();

        for (int i = 0; i < parameters.size(); i++) {
            StudyParameter sp = (StudyParameter) parameters.get(i);
            String handle = sp.getHandle();
            // logger.info("handle:" + handle);
            StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getId(), handle);
            // TO DO: will change to use java reflection later
            if (spv.getId() > 0) {
                if (handle.equalsIgnoreCase("collectDob")) {
                    spc.setCollectDob(spv.getValue());
                } else if (handle.equalsIgnoreCase("genderRequired")) {
                    spc.setGenderRequired(spv.getValue());
                } else if (handle.equalsIgnoreCase("discrepancyManagement")) {
                    spc.setDiscrepancyManagement(spv.getValue());
                } else if (handle.equalsIgnoreCase("subjectPersonIdRequired")) {
                    spc.setSubjectPersonIdRequired(spv.getValue());
                    // logger.info("subjectPersonIdRequired" +
                    // spc.getSubjectPersonIdRequired());
                } else if (handle.equalsIgnoreCase("interviewerNameRequired")) {
                    spc.setInterviewerNameRequired(spv.getValue());
                } else if (handle.equalsIgnoreCase("interviewerNameDefault")) {
                    spc.setInterviewerNameDefault(spv.getValue());
                } else if (handle.equalsIgnoreCase("interviewerNameEditable")) {
                    spc.setInterviewerNameEditable(spv.getValue());
                } else if (handle.equalsIgnoreCase("interviewDateRequired")) {
                    spc.setInterviewDateRequired(spv.getValue());
                } else if (handle.equalsIgnoreCase("interviewDateDefault")) {
                    spc.setInterviewDateDefault(spv.getValue());
                } else if (handle.equalsIgnoreCase("interviewDateEditable")) {
                    spc.setInterviewDateEditable(spv.getValue());
                } else if (handle.equalsIgnoreCase("subjectIdGeneration")) {
                    spc.setSubjectIdGeneration(spv.getValue());
                    logger.debug("subjectIdGeneration" + spc.getSubjectIdGeneration());
                } else if (handle.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                    spc.setSubjectIdPrefixSuffix(spv.getValue());
                } else if (handle.equalsIgnoreCase("personIdShownOnCRF")) {
                    spc.setPersonIdShownOnCRF(spv.getValue());
                } else if (handle.equalsIgnoreCase("secondaryLabelViewable")) {
                    spc.setSecondaryLabelViewable(spv.getValue());
                } else if (handle.equalsIgnoreCase("adminForcedReasonForChange")) {
                    spc.setAdminForcedReasonForChange(spv.getValue());
                } else if (handle.equalsIgnoreCase("eventLocationRequired")) {
                    spc.setEventLocationRequired(spv.getValue());
                } else if (handle.equalsIgnoreCase("participantPortal")) {
                    spc.setParticipantPortal(spv.getValue());
                } else if (handle.equalsIgnoreCase("randomization")) {
                    spc.setRandomization(spv.getValue());
                }
            }
        }
        study.setStudyParameterConfig(spc);
        return study;

    }

    public StudyBean setParameterValuesForStudy(StudyBean study) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        ArrayList<StudyParamsConfig> theParameters = spvdao.findParamConfigByStudy(study);
        study.setStudyParameters(theParameters);

        ArrayList parameters = spvdao.findAllParameterValuesByStudy(study);

        for (int i = 0; i < parameters.size(); i++) {
            StudyParameterValueBean spvb = (StudyParameterValueBean) parameters.get(i);
            String parameter = spvb.getParameter();
            if (parameter.equalsIgnoreCase("collectDob")) {
                study.getStudyParameterConfig().setCollectDob(spvb.getValue());
            } else if (parameter.equalsIgnoreCase("genderRequired")) {
                study.getStudyParameterConfig().setGenderRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                study.getStudyParameterConfig().setSubjectPersonIdRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                study.getStudyParameterConfig().setDiscrepancyManagement(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                study.getStudyParameterConfig().setSubjectIdGeneration(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                study.getStudyParameterConfig().setSubjectIdPrefixSuffix(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                study.getStudyParameterConfig().setInterviewerNameRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                study.getStudyParameterConfig().setInterviewerNameDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                study.getStudyParameterConfig().setInterviewerNameEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                study.getStudyParameterConfig().setInterviewDateRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                study.getStudyParameterConfig().setInterviewDateDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                study.getStudyParameterConfig().setInterviewDateEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                study.getStudyParameterConfig().setPersonIdShownOnCRF(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                study.getStudyParameterConfig().setAdminForcedReasonForChange(spvb.getValue());
            }
        }
        return study;

    }

    public StudyBean setParametersForSite(StudyBean site) {
        StudyDAO sdao = new StudyDAO(ds);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        StudyBean parent = (StudyBean) sdao.findByPK(site.getParentStudyId());
        parent = this.setParameterValuesForStudy(parent);
        site.setStudyParameterConfig(parent.getStudyParameterConfig());
        ArrayList siteParameters = spvdao.findAllParameterValuesByStudy(site);

        for (int i = 0; i < siteParameters.size(); i++) {
            StudyParameterValueBean spvb = (StudyParameterValueBean) siteParameters.get(i);
            String parameter = spvb.getParameter();
            if (parameter.equalsIgnoreCase("collectDob")) {
                site.getStudyParameterConfig().setCollectDob(spvb.getValue());
            } else if (parameter.equalsIgnoreCase("genderRequired")) {
                site.getStudyParameterConfig().setGenderRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                site.getStudyParameterConfig().setSubjectPersonIdRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                site.getStudyParameterConfig().setDiscrepancyManagement(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                site.getStudyParameterConfig().setSubjectIdGeneration(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                site.getStudyParameterConfig().setSubjectIdPrefixSuffix(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                site.getStudyParameterConfig().setInterviewerNameRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                site.getStudyParameterConfig().setInterviewerNameDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                site.getStudyParameterConfig().setInterviewerNameEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                site.getStudyParameterConfig().setInterviewDateRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                site.getStudyParameterConfig().setInterviewDateDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                site.getStudyParameterConfig().setInterviewDateEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                site.getStudyParameterConfig().setPersonIdShownOnCRF(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                site.getStudyParameterConfig().setAdminForcedReasonForChange(spvb.getValue());
            }

            // will add interview name/date features later
        }
        return site;

    }

}
