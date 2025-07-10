/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.service;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameter;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

public class StudyParameterValueDAO extends AuditableEntityDAO<StudyParameterValueBean> {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDY_PARAMETER;

    }

    public StudyParameterValueDAO(DataSource ds) {
        super(ds);
    }

    public StudyParameterValueDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyParameterValueBean> findAll() {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyParameterValueBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public StudyParameterValueBean create(StudyParameterValueBean spvb) {
        HashMap<Integer, Object> variables = variables(spvb.getStudyId(), spvb.getValue(), spvb.getParameter());

        this.executeUpdate(digester.getQuery("create"), variables);
        return spvb;

    }

    public StudyParameterValueBean update(StudyParameterValueBean spvb) {
        HashMap<Integer, Object> variables = variables(spvb.getValue(), spvb.getStudyId(), spvb.getParameter());

        this.executeUpdate(digester.getQuery("update"), variables);
        return spvb;
    }

    public StudyParameterValueBean getEntityFromHashMap(HashMap<String, Object> hm) {
        // study_id numeric,
        // value varchar(50),
        // study_parameter_id int4,
        StudyParameterValueBean spvb = new StudyParameterValueBean();
        // super.setEntityAuditInformation(spvb, hm);
        spvb.setValue((String) hm.get("value"));
        spvb.setStudyId(((Integer) hm.get("study_id")).intValue());
        spvb.setId(((Integer) hm.get("study_parameter_value_id")).intValue());
        spvb.setParameter((String) hm.get("parameter"));

        return spvb;
    }

    public StudyParameter getParameterEntityFromHashMap(HashMap<String, Object> hm) {
        // study_parameter_id serial NOT NULL,
        // handle varchar(50),
        // name varchar(50),
        // description varchar(255),
        // default_value varchar(50),
        // inheritable bool DEFAULT true,
        // overridable bool,
        StudyParameter sp = new StudyParameter();
        // super.setEntityAuditInformation(spvb, hm);
        sp.setId(((Integer) hm.get("study_parameter_id")).intValue());
        sp.setHandle((String) hm.get("handle"));
        sp.setName((String) hm.get("name"));
        sp.setDescription((String) hm.get("description"));
        sp.setDefaultValue((String) hm.get("default_value"));
        sp.setInheritable(((Boolean) hm.get("inheritable")).booleanValue());
        sp.setOverridable(((Boolean) hm.get("overridable")).booleanValue());
        return sp;
    }

    @Override
    public void setTypesExpected() {
        // study_parameter_value_id serial NOT NULL,
        // study_id int4,
        // value varchar(50),
        // parameter varchar(50),

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);

    }

    public void setTypesExpectedForParameter() {
        // study_parameter_id serial NOT NULL,
        // handle varchar(50),
        // name varchar(50),
        // description varchar(255),
        // default_value varchar(50),
        // inheritable bool DEFAULT true,
        // overridable bool,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.BOOL);
        this.setTypeExpected(7, TypeNames.BOOL);

    }

    public StudyParameterValueBean findByHandleAndStudy(int studyId, String handle) {
    	String queryName = "findByStudyAndHandle";
        HashMap<Integer, Object> variables = variables(studyId, handle);
        return executeFindByPKQuery(queryName, variables);
    }

    public boolean setParameterValue(int studyId, String parameterHandle, String value) {
        return false;
    }

    public ArrayList<StudyParameter> findAllParameters() {
        this.setTypesExpectedForParameter();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllParameters"));
        ArrayList<StudyParameter> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            StudyParameter eb = (StudyParameter) this.getParameterEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<StudyParameterValueBean> findAllParameterValuesByStudy(StudyBean study) {
    	String queryName = "findAllParameterValuesByStudy";
        HashMap<Integer, Object> variables = variables(study.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyParamsConfig> findParamConfigByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
        this.setTypeExpected(8, TypeNames.STRING);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.BOOL);
        this.setTypeExpected(11, TypeNames.BOOL);
        HashMap<Integer, Object> variables = variables(study.getId());

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findParamConfigByStudy"), variables);
        ArrayList<StudyParamsConfig> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            StudyParameterValueBean spvb = new StudyParameterValueBean();
            spvb.setValue((String) hm.get("value"));
            spvb.setStudyId(((Integer) hm.get("study_id")).intValue());
            spvb.setId(((Integer) hm.get("study_parameter_value_id")).intValue());

            StudyParameter sp = new StudyParameter();
            sp.setId(((Integer) hm.get("study_parameter_id")).intValue());
            sp.setHandle((String) hm.get("handle"));
            sp.setName((String) hm.get("name"));
            sp.setDescription((String) hm.get("description"));
            sp.setDefaultValue((String) hm.get("default_value"));
            sp.setInheritable(((Boolean) hm.get("inheritable")).booleanValue());
            sp.setOverridable(((Boolean) hm.get("overridable")).booleanValue());

            StudyParamsConfig config = new StudyParamsConfig();
            config.setParameter(sp);
            config.setValue(spvb);
            al.add(config);
        }
        return al;

    }

    public EntityBean findByPK(int ID) {
        EntityBean eb = new StudyParameterValueBean();
        return eb;

    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyParameterValueBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyParameterValueBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public StudyParameterValueBean emptyBean() {
		return new StudyParameterValueBean();
	}

}
