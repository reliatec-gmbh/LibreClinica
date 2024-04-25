package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;

public class ResponseSetDAO extends EntityDAO<ResponseSetBean> {
    public ResponseSetDAO(DataSource ds) {
        super(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RESPONSESET;
    }

    @Override
    public ResponseSetBean emptyBean() {
        return new ResponseSetBean();
    }

    @Override
    public ResponseSetBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ResponseSetBean rsb = new ResponseSetBean();
        rsb.setId((Integer) hm.get("response_set_id"));
        rsb.setResponseTypeId((Integer) hm.get("response_type_id"));
        rsb.setLabel((String) hm.get("label"));
        rsb.setOptions((String) hm.get("options_text"), (String) hm.get("options_values"));

        return rsb;
    }

    @Override
    public ArrayList<ResponseSetBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    public ArrayList<ResponseSetBean> findAllByItemId(int itemId) throws OpenClinicaException {
        String queryName = "findAllResponseSetByItemId";

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, itemId);

        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<ResponseSetBean> findAllByItemIdAndCrfVersionId(int itemId, int crfVersionId) throws OpenClinicaException {
        String queryName = "findAllResponseSetByItemIdAndCrfVersionId";

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, itemId);
        variables.put(2, crfVersionId);

        return executeFindAllQuery(queryName, variables);
    }

    @Override
    public ArrayList<ResponseSetBean> findAll() throws OpenClinicaException {
        return null;
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        return null;
    }

    @Override
    public ResponseSetBean create(ResponseSetBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ResponseSetBean update(ResponseSetBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<ResponseSetBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<ResponseSetBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        int ind = 1; //o_sec_id
        this.setTypeExpected(ind++, TypeNames.INT); //response_set_id
        this.setTypeExpected(ind++, TypeNames.INT); //response_type_id
        this.setTypeExpected(ind++, TypeNames.STRING); //string label
        this.setTypeExpected(ind++, TypeNames.STRING); //string options_text
        this.setTypeExpected(ind++, TypeNames.STRING); //string options_values
        this.setTypeExpected(ind, TypeNames.INT); //int version
    }
}
