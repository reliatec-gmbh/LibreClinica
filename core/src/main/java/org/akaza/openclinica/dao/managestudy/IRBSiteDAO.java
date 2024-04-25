package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBSiteBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IRBSiteDAO extends AuditableEntityDAO<IRBSiteBean> {
    public IRBSiteDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
        getCurrentPKName = "getCurrentIRBSitePrimaryKey";
    }

    @Override
    public IRBSiteBean emptyBean() {
        return new IRBSiteBean();
    }



    @Override
    public IRBSiteBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBSiteBean retval = new IRBSiteBean();
        retval.setIrbSiteId((Integer) hm.get("irb_site_id"));
        retval.setSiteId((Integer) hm.get("site_id"));
        retval.setVersionNumber((Integer) hm.get("version_number"));
        retval.setSiteReliesOnCdcIrb((Boolean) hm.get("site_relies_on_cdc_irb"));
        retval.setIs1572((Boolean) hm.get("is_1572"));
        retval.setCdcIrbProtocolVersionDate((Date) hm.get("cdc_irb_protocol_version_date"));
        retval.setLocalIrbApprovedProtocol((Date) hm.get("local_irb_approved_protocol"));
        retval.setCdcReceivedLocalDocuments((Date) hm.get("cdc_received_local_documents"));
        retval.setSiteConsentPackageSendToCdcIrb((Date) hm.get("site_consent_package_send_to_cdc_irb"));
        retval.setInitialCdcIrbApproval((Date) hm.get("initial_cdc_irb_approval"));
        retval.setCrbApprovalToEnroll((Date) hm.get("crb_approval_to_enroll"));
        retval.setIrbApproval((Date) hm.get("irb_approval"));
        retval.setExpirationDate((Date) hm.get("expiration_date"));
        retval.setActive((Boolean) hm.get("active"));
        retval.setComments((String) hm.get("comments"));

        return retval;
    }

    @Override
    public ArrayList<IRBSiteBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBSiteBean> findAll() throws OpenClinicaException {
        return null;
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();
        return this.executeFindByPKQuery("findIRBSiteById", parameters);
    }

    private int populateVariablesAndNullVars(IRBSiteBean eb,
                                             HashMap<Integer, Object> variables,
                                             HashMap<Integer, Integer> nullVars,
                                             int startIndex) {
        int retval = startIndex;

        variables.put(retval++, eb.getVersionNumber());
        variables.put(retval++, eb.isSiteReliesOnCdcIrb());
        variables.put(retval++, eb.isIs1572());
        if(eb.getCdcIrbProtocolVersionDate()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getCdcIrbProtocolVersionDate());
        if(eb.getLocalIrbApprovedProtocol()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getLocalIrbApprovedProtocol());
        if(eb.getCdcReceivedLocalDocuments()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getCdcReceivedLocalDocuments());
        if(eb.getSiteConsentPackageSendToCdcIrb()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getSiteConsentPackageSendToCdcIrb());
        if(eb.getInitialCdcIrbApproval()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getInitialCdcIrbApproval());
        if(eb.getCrbApprovalToEnroll()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getCrbApprovalToEnroll());
        if(eb.getIrbApproval()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getIrbApproval());
        if(eb.getExpirationDate()==null) nullVars.put(retval, Types.DATE);
        variables.put(retval++, eb.getExpirationDate());
        variables.put(retval++, eb.isActive());
        if(eb.getComments()==null) nullVars.put(retval, TypeNames.STRING);
        variables.put(retval, eb.getComments());

        return retval;
    }
    @Override
    public IRBSiteBean create(IRBSiteBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        variables.put(1, eb.getSiteId());
        int position = populateVariablesAndNullVars(eb, variables, nullVars, 2);

        executeUpdateWithPK(digester.getQuery("createIRBSite"), variables, nullVars);
        if (isQuerySuccessful()) {
            eb.setIrbSiteId(getLatestPK());
        }
        return eb;
    }

    @Override
    public IRBSiteBean update(IRBSiteBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        int position = populateVariablesAndNullVars(eb, variables, nullVars, 1);
        variables.put(position+1, eb.getIrbSiteId());

        executeUpdateWithPK(digester.getQuery("updateIRBSite"), variables, nullVars);

        return eb;
    }

    @Override
    public ArrayList<IRBSiteBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBSiteBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }


    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.INT);
        setTypeExpected(3, TypeNames.INT);
        setTypeExpected(4, TypeNames.BOOL);
        setTypeExpected(5, TypeNames.BOOL);
        setTypeExpected(6, TypeNames.DATE);
        setTypeExpected(7, TypeNames.DATE);
        setTypeExpected(8, TypeNames.DATE);
        setTypeExpected(9, TypeNames.DATE);
        setTypeExpected(10, TypeNames.DATE);
        setTypeExpected(11, TypeNames.DATE);
        setTypeExpected(12, TypeNames.DATE);
        setTypeExpected(13, TypeNames.DATE);
        setTypeExpected(14, TypeNames.BOOL);
        setTypeExpected(15, TypeNames.STRING);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    public IRBSiteBean findBySiteId(int siteId) {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, siteId);
        return this.executeFindByPKQuery("findIRBSiteBySiteId", parameters);
    }
}