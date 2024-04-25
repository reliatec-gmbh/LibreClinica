/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.managestudy.LabsForSiteBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class LabsForSiteDAO extends AuditableEntityDAO<LabsForSiteBean> {

    public LabsForSiteDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public LabsForSiteDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public LabsForSiteDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    private void setQueryNames() {
        getNextPKName = "findNextKey";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_LABS_FOR_SITE;
    }

    @Override
    public void setTypesExpected() {
        // 1 id serial NOT NULL,
        // 2 site_id INT NOT NULL,
        // 3 laboratory_id INT NOT NULL,

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// id
        this.setTypeExpected(2, TypeNames.INT);// site_id
        this.setTypeExpected(3, TypeNames.INT);// laboratory_id
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
    public LabsForSiteBean getEntityFromHashMap(HashMap<String, Object> hm) {
        LabsForSiteBean cb = new LabsForSiteBean();

        // first set all the strings
        cb.setId((Integer) hm.get("id"));
        cb.setSite_id((Integer) hm.get("site_id"));
        cb.setLaboratory_id((Integer) hm.get("laboratory_id"));




        return cb;
    }

    @Override
    public ArrayList<LabsForSiteBean> findAll() {
        return findAllByLimit(false);
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<LabsForSiteBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }
    public ArrayList<LabsForSiteBean> findAllByLimit(boolean isLimited) {
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
    public LabsForSiteBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<LabsForSiteBean> findBySiteId(Integer siteId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(siteId);
        ArrayList<HashMap<String, Object>> alist = select(digester.getQuery("findBySiteId"), variables);
        return alist.stream().map(this::getEntityFromHashMap).collect(toCollection(ArrayList::new));
    }


    public ArrayList<LabsForSiteBean> findBySiteIdAndLabId(Integer siteId, Integer labId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(siteId, labId);
        ArrayList<HashMap<String, Object>> alist = select(digester.getQuery("findBySiteIdAndLabId"), variables);
        return alist.stream().map(this::getEntityFromHashMap).collect(toCollection(ArrayList::new));
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
    public ArrayList<LabsForSiteBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<LabsForSiteBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public LabsForSiteBean update(LabsForSiteBean sb) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public LabsForSiteBean create(LabsForSiteBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        sb.setId(this.findNextKey());

        variables.put(1, sb.getId());
        variables.put(2, sb.getSite_id());
        variables.put(3, sb.getLaboratory_id());

        this.executeUpdate(digester.getQuery("create"), variables, nullVars);

        return sb;
    }

    public LabsForSiteBean emptyBean() {
        return new LabsForSiteBean();
    }
    public boolean delete(LabsForSiteBean item){
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, item.getId());
        return this.executeUpdate(digester.getQuery("deleteById"), variables) > 0;
    }


}
