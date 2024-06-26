/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.logic;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.masking.MaskingBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 *
 *
 */
public class MaskingDAO extends AuditableEntityDAO<MaskingBean> {
    private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_MASKING;
        // TODO work on new instance
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        // TODO figure out the error with current primary keys?
    }

    public MaskingDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    public MaskingDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// mask id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.STRING);// desc
        this.setTypeExpected(4, TypeNames.INT);// status id
        this.setTypeExpected(5, TypeNames.INT);// owner id
        this.setTypeExpected(6, TypeNames.TIMESTAMP);// created
        this.setTypeExpected(7, TypeNames.TIMESTAMP);// updated
        this.setTypeExpected(8, TypeNames.INT);// update id
        this.setTypeExpected(9, TypeNames.INT);// study id
        this.setTypeExpected(10, TypeNames.INT);// role id
        this.setTypeExpected(11, TypeNames.STRING);// entity name
        this.setTypeExpected(12, TypeNames.INT);// entity id

    }

    public MaskingBean getEntityFromHashMap(HashMap<String, Object> hm) {
        MaskingBean mb = new MaskingBean();
        this.setEntityAuditInformation(mb, hm);
        mb.setName((String) hm.get("name"));
        mb.setId(((Integer) hm.get("mask_id")).intValue());
        // TODO set other variables here, tbh
        return mb;
    }

    public ArrayList<MaskingBean>findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> aList = this.select(digester.getQuery("findAll"));
        ArrayList<MaskingBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : aList) {
            MaskingBean mb = this.getEntityFromHashMap(hm);
            al.add(mb);
        }
        return al;
    }

    @Override
    public MaskingBean update(MaskingBean eb) {
        return eb;
    }

    @Override
    public MaskingBean create(MaskingBean eb) {
        return eb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<MaskingBean>findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public EntityBean findByPK(int ID) {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<MaskingBean>findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<MaskingBean>findAllByPermission(Object objCurrentUser, int intActionType) {
      throw new RuntimeException("Not implemented");
    }

	@Override
	public MaskingBean emptyBean() {
		return new MaskingBean();
	}
}
