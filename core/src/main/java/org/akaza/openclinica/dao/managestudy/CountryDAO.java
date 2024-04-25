/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.managestudy.CountryBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class CountryDAO extends AuditableEntityDAO<CountryBean> {

    public CountryDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public CountryDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public CountryDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    private void setQueryNames() {
        getNextPKName = "findNextKey";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_COUNTRY;
    }

    @Override
    public void setTypesExpected() {
        // 1 sysid serial NOT NULL,
        // 2 code varchar(3),
        // 3 displayname varchar(50),
        // 4 sortorder INT,

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sysid
        this.setTypeExpected(2, TypeNames.STRING);// code
        this.setTypeExpected(3, TypeNames.STRING);// displayname
        this.setTypeExpected(4, TypeNames.INT);// sortorder
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



    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public CountryBean getEntityFromHashMap(HashMap<String, Object> hm) {
        CountryBean cb = new CountryBean();

        // first set all the strings
        cb.setSysid((Integer) hm.get("sysid"));
        cb.setDisplayname((String) hm.get("displayname"));
        cb.setCode((String) hm.get("code"));
        cb.setSortorder((Integer) hm.get("sortorder"));



        return cb;
    }

    @Override
    public ArrayList<CountryBean> findAll() {
        return findAllByLimit(false);
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<CountryBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }
    public ArrayList<CountryBean> findAllByLimit(boolean isLimited) {
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

    @Override
    public CountryBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public CountryBean findByName(String name) {
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

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<CountryBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<CountryBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public CountryBean update(CountryBean sb) {
        throw new RuntimeException("Not implemented");
    }

    public CountryBean create(CountryBean sb) {
        throw new RuntimeException("Not implemented");
    }


    public CountryBean emptyBean() {
        return new CountryBean();
    }

}
