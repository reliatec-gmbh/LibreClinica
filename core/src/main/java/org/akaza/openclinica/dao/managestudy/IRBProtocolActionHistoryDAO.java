package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBProtocolActionHistoryBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IRBProtocolActionHistoryDAO extends AuditableEntityDAO<IRBProtocolActionHistoryBean> {
    public IRBProtocolActionHistoryDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() { getCurrentPKName = "getCurrentIRBProtocolActionPrimaryKey"; }

    @Override
    public IRBProtocolActionHistoryBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBProtocolActionHistoryBean protocolActionHistoryBean =
                new IRBProtocolActionHistoryBean();
        protocolActionHistoryBean.setIrbProtocolActionHistoryId(
                (Integer) hm.get("irb_protocol_action_history_id") );
        protocolActionHistoryBean.setIrbProtocolActionTypeId(
                (Integer) hm.get("irb_protocol_action_type_id") );
        protocolActionHistoryBean.setAction((String) hm.get("action"));
        protocolActionHistoryBean.setVersionDate( (Date) hm.get("version_date") );
        protocolActionHistoryBean.setVersionNumber((Integer) hm.get("version_number"));
        protocolActionHistoryBean.setSiteSubmittedToLocalIrb(
                (Date) hm.get("site_submitted_to_local_irb"));
        protocolActionHistoryBean.setLocalIrbApproval( (Date) hm.get("local_irb_approval"));
        protocolActionHistoryBean.setReceivedDocsFromSites( (Date) hm.get("received_docs_from_sites"));
        protocolActionHistoryBean.setPackageSentToCdcIrb( (Date) hm.get("package_sent_to_cdc_irb"));
        protocolActionHistoryBean.setCdcApproval( (Date) hm.get("cdc_approval"));
        protocolActionHistoryBean.setEnrollmentPauseDate( (Date) hm.get("enrollment_pause_date"));
        protocolActionHistoryBean.setEnrollmentRestartedDate( (Date) hm.get("enrollment_restarted_date"));
        protocolActionHistoryBean.setReasonForEnrollmentPaused( (String) hm.get("reason_for_enrollment_paused"));

        return protocolActionHistoryBean;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryBean> findAll() throws OpenClinicaException {
        return null;
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, id);
        return this.executeFindByPKQuery("findProtocolActionHistoryById", parameters);
    }

    private int populateVariablesAndNullVars(IRBProtocolActionHistoryBean eb,
                                             HashMap<Integer, Object> variables,
                                             HashMap<Integer, Integer> nullVars,
                                             int startIndex) {
        int retval = startIndex;

        variables.put(retval++, eb.getIrbProtocolActionTypeId());
        if(eb.getVersionDate()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getVersionDate());
        variables.put(retval++, eb.getVersionNumber());
        if(eb.getSiteSubmittedToLocalIrb()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getSiteSubmittedToLocalIrb());
        if(eb.getLocalIrbApproval()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getLocalIrbApproval());
        if(eb.getReceivedDocsFromSites()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getReceivedDocsFromSites());
        if(eb.getPackageSentToCdcIrb()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getPackageSentToCdcIrb());
        if(eb.getCdcApproval()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval, eb.getCdcApproval());

        return retval;
    }

    @Override
    public IRBProtocolActionHistoryBean create(IRBProtocolActionHistoryBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        //variables.put(1, eb.getSiteId());
        variables.put(1, eb.getIrbSiteId());
        int position = populateVariablesAndNullVars(eb, variables, nullVars, 2);

        executeUpdateWithPK(digester.getQuery("createProtocolActionHistory"), variables, nullVars);
        if (isQuerySuccessful()) {
            eb.setIrbProtocolActionHistoryId(getLatestPK());
        }
        return eb;
    }

    @Override
    public IRBProtocolActionHistoryBean update(IRBProtocolActionHistoryBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        //variables.put(1, eb.getSiteId());
        int position = populateVariablesAndNullVars(eb, variables, nullVars, 1);
        variables.put(position+1, eb.getIrbProtocolActionHistoryId());

        executeUpdateWithPK(digester.getQuery("updateProtocolActionHistory"), variables, nullVars);

        return eb;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionHistoryBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.INT);
        setTypeExpected(3, TypeNames.STRING);
        setTypeExpected(4, TypeNames.DATE);
        setTypeExpected(5, TypeNames.INT);
        setTypeExpected(6, TypeNames.DATE);
        setTypeExpected(7, TypeNames.DATE);
        setTypeExpected(8, TypeNames.DATE);
        setTypeExpected(9, TypeNames.DATE);
        setTypeExpected(10, TypeNames.DATE);
        setTypeExpected(11, TypeNames.DATE);
        setTypeExpected(12, TypeNames.DATE);
        setTypeExpected(13, TypeNames.STRING);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    @Override
    public IRBProtocolActionHistoryBean emptyBean() {
        return new IRBProtocolActionHistoryBean();
    }

    public ArrayList<IRBProtocolActionHistoryBean> findBySiteId(int siteId) {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, siteId);
        return this.executeFindAllQuery("findAllProtocolActionHistoryBySite", parameters);
    }
}
