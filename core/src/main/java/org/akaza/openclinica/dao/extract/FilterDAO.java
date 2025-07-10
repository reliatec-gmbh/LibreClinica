/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.extract;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.bean.extract.FilterObjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * The data access object for filters.
 *
 * @author thickerson
 *
 */
public class FilterDAO extends AuditableEntityDAO<FilterBean> {
    private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_FILTER;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
        // TODO figure out the error with current primary keys?
    }

    public FilterDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    /**
     * creator object to be used during testing, tbh
     *
     * @param ds
     * @param digester
     */
    public FilterDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// filter id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.STRING);// description
        this.setTypeExpected(4, TypeNames.STRING);// sql statement?
        this.setTypeExpected(5, TypeNames.INT);// status id
        this.setTypeExpected(6, TypeNames.DATE);// created
        this.setTypeExpected(7, TypeNames.DATE);// updated
        this.setTypeExpected(8, TypeNames.INT);// owner id
        this.setTypeExpected(9, TypeNames.INT);// update id
    }

    public FilterBean update(FilterBean fb) {
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), fb.getName());
        variables.put(new Integer(2), fb.getDescription());
        variables.put(new Integer(3), new Integer(fb.getStatus().getId()));
        variables.put(new Integer(4), fb.getSQLStatement());// string, updateid,
        // filterid
        variables.put(new Integer(5), new Integer(fb.getUpdaterId()));
        variables.put(new Integer(6), new Integer(fb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables, nullVars);
        return fb;
    }

    public FilterBean create(FilterBean fb) {
        logger.info("logged following owner id: " + fb.getOwnerId() + " vs. " + fb.getOwner().getId());
        int id = getNextPK();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), fb.getId());
        variables.put(new Integer(2), fb.getName());
        // name desc sql, status id owner id
        variables.put(new Integer(3), fb.getDescription());
        variables.put(new Integer(4), fb.getSQLStatement());
        variables.put(new Integer(5), new Integer(fb.getStatus().getId()));
        variables.put(new Integer(6), new Integer(fb.getOwner().getId()));
        // changed from get owner id, tbh

        this.executeUpdate(digester.getQuery("create"), variables);

        fb.setId(id);
        return fb;
    }

    public FilterBean getEntityFromHashMap(HashMap<String, Object> hm) {
        FilterBean fb = new FilterBean();
        this.setEntityAuditInformation(fb, hm);
        fb.setDescription((String) hm.get("description"));
        fb.setName((String) hm.get("name"));
        fb.setId(((Integer) hm.get("filter_id")).intValue());
        fb.setSQLStatement((String) hm.get("sql_statement"));
        return fb;
    }

    public ArrayList<FilterBean> findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAll"));
        ArrayList<FilterBean> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            FilterBean fb = (FilterBean) this.getEntityFromHashMap(hm);
            al.add(fb);
        }
        return al;
    }

    public ArrayList<FilterBean> findAllAdmin() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllAdmin"));
        ArrayList<FilterBean> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            FilterBean fb = (FilterBean) this.getEntityFromHashMap(hm);
            al.add(fb);
        }
        return al;
    }

     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<FilterBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public EntityBean findByPK(int ID) {
        FilterBean fb = new FilterBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        
        if (alist != null && alist.size() > 0) {
            fb = (FilterBean) this.getEntityFromHashMap(alist.get(0));
        }

        return fb;
    }

     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<FilterBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<FilterBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * To be created with the header string and an array list of
     * FilterObjectBeans, which contain the number, type and value of criteria
     * to apply to the existing filter.
     *
     * fundamental change 06-03: adding the new query formation will change the
     * SQL so that
     *
     * @param oldSQLStatement
     * @param connector
     * @param filterObjs
     */
    public String genSQLStatement(String oldSQLStatement, String connector, ArrayList<FilterObjectBean> filterObjs) {
        StringBuffer sb = new StringBuffer();
        // sb.append(" and subject_id in "+
        // "(select subject_id from extract_data_table where ");
        if (oldSQLStatement != null) {
            sb.append(oldSQLStatement);
        } else {
            sb.append(" and subject_id in " + "(select subject_id from extract_data_table where ");
        }
        String tailEnd = "";
        int count = 0;
        for (FilterObjectBean fob : filterObjs) {
            tailEnd = "(" + tailEnd;
            if (count != 0) {
                tailEnd = tailEnd + " " + connector + " ";// fob.getOperand();
            }
            count++;
            // TODO add this to create like operators, maybe move this to else
            // where?
            if (fob.getOperand().equals(" like ") || fob.getOperand().equals(" not like ")) {
                fob.setValue("%" + fob.getValue() + "%");
            }
            tailEnd = tailEnd + "(item_id = " + fob.getItemId() + " and value " + fob.getOperand() + " '" + fob.getValue() + "'))";
        }
        if (oldSQLStatement != null) {
            sb.append(" and ");
            // rearrange sql here, and above, so that
            // filter can be changed
        }
        sb.append(tailEnd);
        // sb.append(")");
        // and a parens at the very end!
        return sb.toString();
    }

    /**
     * Will generate an explanation stating that this filter will look for value
     * x at question y AND value like z at question a OR value not like a at
     * question c...
     *
     * @param oldExplanation
     * @param connector
     * @param filterObjs
     */
    public ArrayList<String> genExplanation(ArrayList<String> oldExplanation, String connector, ArrayList<FilterObjectBean> filterObjs) {
        ArrayList<String> sb = new ArrayList<>();
        if (oldExplanation != null) {
            sb.addAll(oldExplanation);
        } else {
            sb.add("This Filter will look for:");
        }
        int count = 0;
        for (FilterObjectBean fob : filterObjs) {
            String answerLine = "A value " + fob.getOperand() + " " + fob.getValue() + " " + "for question " + fob.getItemName();

            sb.add(answerLine);
            count++;
            if (count < filterObjs.size()) {
                sb.add(connector + " ");
            }
        }
        return sb;
    }

	@Override
	public FilterBean emptyBean() {
		return new FilterBean();
	}
}
