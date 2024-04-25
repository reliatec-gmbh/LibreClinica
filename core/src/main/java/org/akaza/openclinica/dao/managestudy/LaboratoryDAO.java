/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.LaboratoryBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class LaboratoryDAO extends AuditableEntityDAO<LaboratoryBean> {

    public LaboratoryDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public LaboratoryDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public LaboratoryDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    private void setQueryNames() {
        getNextPKName = "findNextKey";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_LABORATORY;
    }

    @Override
    public void setTypesExpected() {
        // 1 labId integer NOT NULL,
        // 2 labName varchar(200) NOT NULL,
        // 3 address1 varchar(200),
        // 4 address2 varchar(200),
        // 5 stateProvinceRegion varchar(100),
        // 6 city varchar(100),
        // 7 country varchar(100),
        // 8 zipPostal varchar(50),
        // 9 activeLab boolean,
        // 10 labTypes varchar(80),


        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// labId
        this.setTypeExpected(2, TypeNames.STRING);// labName
        this.setTypeExpected(3, TypeNames.STRING);// address1
        this.setTypeExpected(4, TypeNames.STRING);// address2
        this.setTypeExpected(5, TypeNames.STRING);// stateProvinceRegion
        this.setTypeExpected(6, TypeNames.STRING);// city
        this.setTypeExpected(7, TypeNames.STRING);// country
        this.setTypeExpected(8, TypeNames.STRING);// zipPostal
        this.setTypeExpected(9, TypeNames.STRING);// activeLab
        this.setTypeExpected(10, TypeNames.STRING);// labTypes
   }

    /**
     * <P>
     * findNextKey, a method to return a simple int from the database.
     * 
     * @return int, which is the next primary key for creating a study.
     */
    public int findNextKey() {
        return getNextPK();
    }



    public LaboratoryBean findByUniqueIdentifier(String oid) {
        String queryName = "findByUniqueIdentifier";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    public LaboratoryBean findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        String queryName = "findSiteByUniqueIdentifier";
        HashMap<Integer, Object> variables = variables(parentUniqueIdentifier, siteUniqueIdentifier);
        return executeFindByPKQuery(queryName, variables);
    }


    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public LaboratoryBean getEntityFromHashMap(HashMap<String, Object> hm) {
        LaboratoryBean lb = new LaboratoryBean();
        lb.setLabId((Integer) hm.get("labid"));
        lb.setLabName((String) hm.get("labname"));
        lb.setAddress1((String) hm.get("address1"));
        lb.setAddress2((String) hm.get("address2"));
        lb.setCity((String) hm.get("city"));
        lb.setCountry((String) hm.get("country"));
        lb.setZipPostal((String) hm.get("zippostal"));
        lb.setActiveLab(hm.get("activelab").equals("t"));
        lb.setLabTypes(Arrays.asList(((String) hm.get("labtypes")).split(",")));

        return lb;
    }

    @Override
    public ArrayList<LaboratoryBean> findAll() {
        return findAllByLimit(false);
    }

    public ArrayList<LaboratoryBean> findAllByLimit(boolean isLimited) {
        this.setTypesExpected();
        String sql;
        if (isLimited) {
            sql = digester.getQuery("findAll") + " limit 5";
        } else {
            sql = digester.getQuery("findAll");
        }
        ArrayList<HashMap<String, Object>> alist = this.select(sql);
        return alist.stream().map(hm -> this.getEntityFromHashMap(hm)).collect(toCollection(ArrayList::new));
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<LaboratoryBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public LaboratoryBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public LaboratoryBean findByName(String name) {
        String queryName = "findByName";
        HashMap<Integer, Object> variables = variables(name);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * deleteTestOnly, used only to clean up after unit testing
     * 
     * @param name name
     */
    public void deleteTestOnly(String name) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, name);
        this.executeUpdate(digester.getQuery("deleteTestOnly"), variables);
    }


    @Override
    public LaboratoryBean emptyBean() {
        return new LaboratoryBean();
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<LaboratoryBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<LaboratoryBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public LaboratoryBean update(LaboratoryBean sb) {
        throw new RuntimeException("Not implemented");
    }

    public LaboratoryBean create(LaboratoryBean sb) {
        throw new RuntimeException("Not implemented");
    }
}
