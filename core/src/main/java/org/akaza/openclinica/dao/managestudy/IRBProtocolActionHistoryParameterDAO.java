package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBProtocolActionHistoryParameterBean;
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

public class IRBProtocolActionHistoryParameterDAO extends AuditableEntityDAO<IRBProtocolActionHistoryParameterBean> {
    public IRBProtocolActionHistoryParameterDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
    }

    @Override
    public IRBProtocolActionHistoryParameterBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBProtocolActionHistoryParameterBean retval = new IRBProtocolActionHistoryParameterBean();
        retval.setIrbProtocolActionHistoryParameterId((Integer)hm.get("irb_protocol_action_history_parameter_id"));
        retval.setAction((String)hm.get("action"));
        retval.setCdcIrbProtocolVersionDate((Boolean)hm.get("cdc_irb_protocol_version_date"));
        retval.setVersion((Boolean)hm.get("version"));
        retval.setSiteSubmittedToLocalIrb((Boolean)hm.get("site_submitted_to_local_irb"));
        retval.setLocalIrbApproval((Boolean)hm.get("local_irb_approval"));
        retval.setSiteSendsDocsToCrb((Boolean)hm.get("site_sends_docs_to_crb"));
        retval.setPackageSentToCdcIrb((Boolean)hm.get("package_sent_to_cdc_irb"));
        retval.setCdcApprovalAcknowledgment((Boolean)hm.get("cdc_approval_acknowledgment"));
        retval.setEnrollmentPauseDate((Boolean)hm.get("enrollment_pause_date"));
        retval.setEnrollmentReStartedDate((Boolean)hm.get("enrollment_re_started_date"));
        retval.setReasonForEnrollmentPause((Boolean)hm.get("reason_for_enrollment_pause"));

        return retval;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryParameterBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryParameterBean> findAll() throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();

        return this.executeFindAllQuery("findAllProtocolActionHistoryParameter", parameters);
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        throw new RuntimeException();
    }

    @Override
    public IRBProtocolActionHistoryParameterBean findByPKAndStudy(int id, StudyBean study) {
        return super.findByPKAndStudy(id, study);
    }

    @Override
    public IRBProtocolActionHistoryParameterBean create(IRBProtocolActionHistoryParameterBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public IRBProtocolActionHistoryParameterBean update(IRBProtocolActionHistoryParameterBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryParameterBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryParameterBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
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
    public IRBProtocolActionHistoryParameterBean emptyBean() {
        return new IRBProtocolActionHistoryParameterBean();
    }
}