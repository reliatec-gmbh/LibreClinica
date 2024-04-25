package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.IRBProtocolActionTypeBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;

public class IRBProtocolActionTypeDAO extends AuditableEntityDAO<IRBProtocolActionTypeBean> {
    public IRBProtocolActionTypeDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    private void setQueryNames() {
    }

    @Override
    public IRBProtocolActionTypeBean getEntityFromHashMap(HashMap<String, Object> hm) {
        IRBProtocolActionTypeBean retval = new IRBProtocolActionTypeBean();
        retval.setProtocolActionTypeId((Integer)hm.get("irb_protocol_action_type_id"));
        retval.setLabel((String)hm.get("label"));

        return retval;
    }

    @Override
    public ArrayList<IRBProtocolActionTypeBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionTypeBean> findAll() throws OpenClinicaException {
        HashMap<Integer, Object> parameters = new HashMap<>();

        return this.executeFindAllQuery("findAllProtocolActionTypes", parameters);
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        throw new RuntimeException();
    }

    @Override
    public IRBProtocolActionTypeBean findByPKAndStudy(int id, StudyBean study) {
        return super.findByPKAndStudy(id, study);
    }

    @Override
    public IRBProtocolActionTypeBean create(IRBProtocolActionTypeBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public IRBProtocolActionTypeBean update(IRBProtocolActionTypeBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionTypeBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<IRBProtocolActionTypeBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);
        setTypeExpected(2, TypeNames.STRING);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_IRB;
    }

    @Override
    public IRBProtocolActionTypeBean emptyBean() {
        return new IRBProtocolActionTypeBean();
    }
}
