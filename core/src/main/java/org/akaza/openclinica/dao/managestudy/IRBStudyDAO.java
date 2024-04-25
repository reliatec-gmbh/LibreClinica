package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBStudyBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IRBStudyDAO extends AuditableEntityDAO<IRBStudyBean> {
    public IRBStudyDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
        getCurrentPKName = "getCurrentIRBStudyPrimaryKey";
    }

    @Override
    public IRBStudyBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBStudyBean retval = new IRBStudyBean();
        retval.setIrbStudyId((Integer) hm.get("irb_study_id"));
        retval.setStudyId((Integer) hm.get("study_id"));
        retval.setVersion1ProtocolDate((Date) hm.get("version1_protocol_date"));
        retval.setCdcIrbProtocolNumber((String) hm.get("cdc_irb_protocol_number"));
        retval.setProtocolOfficer((String) hm.get("protocol_officer"));
        retval.setSubmittedCdcIrb((Date) hm.get("submitted_cdc_irb"));
        retval.setApprovalByCdcIrb((Date) hm.get("approval_by_cdc_irb"));
        retval.setCdcIrbExpirationDate((Date) hm.get("cdc_irb_expiration_date"));

        return retval;
    }

    @Override
    public ArrayList<IRBStudyBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyBean> findAll() throws OpenClinicaException {
        return null;
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();
        return this.executeFindByPKQuery("findIRBStudyById", parameters);
    }

    private int populateVariablesAndNullVars(IRBStudyBean eb,
                                             HashMap<Integer, Object> variables,
                                             HashMap<Integer, Integer> nullVars,
                                             int startIndex) {
        int retval = startIndex;
        variables.put(retval++, eb.getCdcIrbProtocolNumber());
        if(eb.getVersion1ProtocolDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getVersion1ProtocolDate());
        if(eb.getProtocolOfficer()==null) nullVars.put(retval, TypeNames.STRING);
        variables.put(retval++, eb.getProtocolOfficer());
        if(eb.getSubmittedCdcIrb()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getSubmittedCdcIrb());
        if(eb.getApprovalByCdcIrb()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval++, eb.getApprovalByCdcIrb());
        if(eb.getCdcIrbExpirationDate()==null) nullVars.put(retval, TypeNames.DATE);
        variables.put(retval, eb.getCdcIrbExpirationDate());

        return retval;
    }

    @Override
    public IRBStudyBean create(IRBStudyBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();


        variables.put(1, eb.getStudyId());
        int position = populateVariablesAndNullVars(eb, variables, nullVars, 2);

        executeUpdateWithPK(digester.getQuery("createIRBStudy"), variables, nullVars);
        if (isQuerySuccessful()) {
            eb.setIrbStudyId(getLatestPK());
        }
        return eb;
    }

    @Override
    public IRBStudyBean update(IRBStudyBean eb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        int position = populateVariablesAndNullVars(eb, variables, nullVars, 1);
        variables.put(position+1, eb.getIrbStudyId());

        executeUpdateWithPK(digester.getQuery("updateIRBStudy"), variables, nullVars);

        return eb;
    }

    @Override
    public ArrayList<IRBStudyBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBStudyBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.INT);
        setTypeExpected(3, TypeNames.STRING);
        setTypeExpected(4, TypeNames.DATE);
        setTypeExpected(5, TypeNames.STRING);
        setTypeExpected(6, TypeNames.DATE);
        setTypeExpected(7, TypeNames.DATE);
        setTypeExpected(8, TypeNames.DATE);
    }

    @Override
    public IRBStudyBean emptyBean() {
        return new IRBStudyBean();
    }


    public IRBStudyBean findByStudy(StudyBean currentStudy) {
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, currentStudy.getId());
        return this.executeFindByPKQuery("findIRBStudyByStudyId", parameters);
        }
}
