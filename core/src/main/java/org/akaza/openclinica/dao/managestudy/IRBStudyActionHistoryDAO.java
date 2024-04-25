package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBStudyActionHistoryBean;
import org.akaza.openclinica.bean.managestudy.IRBStudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IRBStudyActionHistoryDAO extends AuditableEntityDAO<IRBStudyActionHistoryBean> {
    public IRBStudyActionHistoryDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
        getCurrentPKName = "getCurrentIRBStudyActionPrimaryKey";
    }

    @Override
    public IRBStudyActionHistoryBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBStudyActionHistoryBean retval = new IRBStudyActionHistoryBean();
        retval.setIrbStudyActionHistoryId((Integer) hm.get("irb_study_action_history_id"));
        retval.setStudyId((Integer) hm.get("study_id"));
        retval.setIrbProtocolActionTypeId((Integer) hm.get("irb_protocol_action_type_id"));
        retval.setActionLabel((String) hm.get("action"));
        retval.setEffectiveDate((Date) hm.get("effective_date"));
        retval.setHrpoAction((int) hm.get("hrpo_action"));
        retval.setVersionNumber((int) hm.get("version_number"));
        retval.setVersionDate((Date) hm.get("version_date"));
        retval.setSubmissionToCdcIrb((Date) hm.get("submission_to_cdc_irb"));
        retval.setCdcIrbApproval((Date) hm.get("cdc_irb_approval"));
        retval.setNotificationSentToSites((Date) hm.get("notification_sent_to_sites"));
        retval.setEnrollmentPauseDate((Date) hm.get("enrollment_pause_date"));
        retval.setEnrollmentReStartedDate((Date) hm.get("enrollment_re_started_date"));
        retval.setReasonForEnrollmentPause((String) hm.get("reason_for_enrollment_pause"));

        return retval;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryBean> findAll() throws OpenClinicaException {
        return null;
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, id);
        return this.executeFindByPKQuery("findIRBStudyActionHistoryById", parameters);
    }

    private int populateVariablesAndNullVars(IRBStudyActionHistoryBean eb,
                                             HashMap<Integer, Object> variables,
                                             HashMap<Integer, Integer> nullVars,
                                             int startIndex) {
        int retval = startIndex;
        variables.put(retval++, eb.getIrbProtocolActionTypeId());
        if(eb.getEffectiveDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getEffectiveDate());
        variables.put(retval++, eb.getHrpoAction());
        variables.put(retval++, eb.getVersionNumber());
        if(eb.getVersionDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getVersionDate());
        if(eb.getSubmissionToCdcIrb()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getSubmissionToCdcIrb());
        if(eb.getCdcIrbApproval()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getCdcIrbApproval());
        if(eb.getNotificationSentToSites()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getNotificationSentToSites());
        if(eb.getEnrollmentPauseDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getEnrollmentPauseDate());
        if(eb.getEnrollmentReStartedDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getEnrollmentReStartedDate());
        if(eb.getReasonForEnrollmentPause()==null) nullVars.put(retval, TypeNames.STRING);
        variables.put(retval, eb.getReasonForEnrollmentPause());

        return retval;
    }

    @Override
    public IRBStudyActionHistoryBean create(IRBStudyActionHistoryBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();


        variables.put(1, eb.getStudyId());
        int position = populateVariablesAndNullVars(eb, variables, nullVars, 2);

        executeUpdateWithPK(digester.getQuery("createIRBStudyActionHistory"), variables, nullVars);
        if (isQuerySuccessful()) {
            eb.setIrbStudyActionHistoryId(getLatestPK());
        }
        return eb;
    }

    @Override
    public IRBStudyActionHistoryBean update(IRBStudyActionHistoryBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        int position = populateVariablesAndNullVars(eb, variables, nullVars, 1);
        variables.put(position+1, eb.getIrbStudyActionHistoryId());

        executeUpdateWithPK(digester.getQuery("updateIRBStudyActionHistory"), variables, nullVars);

        return eb;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyActionHistoryBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.INT);
        setTypeExpected(3, TypeNames.INT);
        setTypeExpected(4, TypeNames.STRING);
        setTypeExpected(5, TypeNames.DATE);
        setTypeExpected(6, TypeNames.INT);
        setTypeExpected(7, TypeNames.INT);
        setTypeExpected(8, TypeNames.DATE);
        setTypeExpected(9, TypeNames.DATE);
        setTypeExpected(10, TypeNames.DATE);
        setTypeExpected(11, TypeNames.DATE);
        setTypeExpected(12, TypeNames.DATE);
        setTypeExpected(13, TypeNames.DATE);
        setTypeExpected(14, TypeNames.STRING);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    @Override
    public IRBStudyActionHistoryBean emptyBean() {
        return new IRBStudyActionHistoryBean();
    }

    public ArrayList<IRBStudyActionHistoryBean> findByStudyId(int studyId) {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, studyId);
        return this.executeFindAllQuery("findIRBStudyActionHistoryByStudyId", parameters);
    }
}
