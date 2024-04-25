package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.ProtocolDeviationBean;
import org.akaza.openclinica.bean.managestudy.ProtocolDeviationSeverityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ProtocolDeviationSeverityDAO extends AuditableEntityDAO<ProtocolDeviationSeverityBean> {
    public ProtocolDeviationSeverityDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        setQueryNames();
    }

    protected void setQueryNames() {
    }

    public Integer getCountWithFilter(ProtocolDeviationFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithStudy");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }
    @Override
    public ProtocolDeviationSeverityBean findByPKAndStudy(int id, StudyBean study) {
        return super.findByPKAndStudy(id, study);
    }

    @Override
    public ProtocolDeviationSeverityBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ProtocolDeviationSeverityBean eb = new ProtocolDeviationSeverityBean();
        //super.setEntityAuditInformation(eb, hm);
        eb.setProtocolDeviationSeverityId((Integer) hm.get("protocol_deviation_severity_id"));
        eb.setLabel((String) hm.get("label"));

        return eb;
    }
    @Override
    public ArrayList<ProtocolDeviationSeverityBean> findAll(String strOrderByColumn, boolean blnAscendingSort,
                                                    String strSearchPhrase) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<ProtocolDeviationSeverityBean> findAll() throws OpenClinicaException {
        return executeFindAllQuery("findAllProtocolDeviationSeverity");
    }

    @Override
    public EntityBean findByPK(int id) throws OpenClinicaException {
        return null;
    }

    @Override
    public ProtocolDeviationSeverityBean create(ProtocolDeviationSeverityBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ProtocolDeviationSeverityBean update(ProtocolDeviationSeverityBean eb) throws OpenClinicaException {
        return null;
    }

    @Override
    public ArrayList<ProtocolDeviationSeverityBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return null;
    }


    @Override
    public ArrayList<ProtocolDeviationSeverityBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return null;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
    }


    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECT;
    }


    @Override
    public ProtocolDeviationSeverityBean emptyBean() {
        return new ProtocolDeviationSeverityBean();
    }

}
