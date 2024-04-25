package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBStudyActionHistoryParameterBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IRBStudyActionHistoryParameterDAO extends AuditableEntityDAO<IRBStudyActionHistoryParameterBean> {
    public IRBStudyActionHistoryParameterDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
    }

    @Override
    public IRBStudyActionHistoryParameterBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBStudyActionHistoryParameterBean retval = new IRBStudyActionHistoryParameterBean();

        retval.setIrbStudyActionHistoryParameterId((Integer)hm.get("irb_study_action_history_parameter_id"));
        retval.setAction((String)hm.get("action"));
        retval.setEffectiveDate((Boolean)hm.get("effective_date"));
        retval.setHrpoAction((Boolean)hm.get("hrpo_action"));
        retval.setVersionNumber((Boolean)hm.get("version_number"));
        retval.setVersionDate((Boolean)hm.get("version_date"));
        retval.setSubmissionToCdcIrb((Boolean)hm.get("submission_to_cdc_irb"));
        retval.setCdcIrbApproval((Boolean)hm.get("cdc_irb_approval"));
        retval.setNotificationSentToSites((Boolean)hm.get("notification_sent_to_sites"));
        retval.setEnrollmentPauseDate((Boolean)hm.get("enrollment_pause_date"));
        retval.setEnrollmentReStartedDate((Boolean)hm.get("enrollment_re_started_date"));
        retval.setReasonForEnrollmentPause((Boolean)hm.get("reason_for_enrollment_pause"));

        return retval;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryParameterBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryParameterBean> findAll() throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();

        return this.executeFindAllQuery("findAllIrbStudyActionHistoryParameter", parameters);
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        throw new RuntimeException();
    }

    @Override
    public IRBStudyActionHistoryParameterBean findByPKAndStudy(int id, StudyBean study) {
        return super.findByPKAndStudy(id, study);
    }

    @Override
    public IRBStudyActionHistoryParameterBean create(IRBStudyActionHistoryParameterBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public IRBStudyActionHistoryParameterBean update(IRBStudyActionHistoryParameterBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryParameterBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryParameterBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.STRING);
        setTypeExpected(3, TypeNames.BOOL);
        setTypeExpected(4, TypeNames.BOOL);
        setTypeExpected(5, TypeNames.BOOL);
        setTypeExpected(6, TypeNames.BOOL);
        setTypeExpected(7, TypeNames.BOOL);
        setTypeExpected(8, TypeNames.BOOL);
        setTypeExpected(9, TypeNames.BOOL);
        setTypeExpected(10, TypeNames.BOOL);
        setTypeExpected(11, TypeNames.BOOL);
        setTypeExpected(12, TypeNames.BOOL);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    @Override
    public IRBStudyActionHistoryParameterBean emptyBean() {
        return new IRBStudyActionHistoryParameterBean();
    }
}