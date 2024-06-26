/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <P>
 * SectionDAO.java, the data access object for creation and access to the
 * sections of a CRF. CRFs will have more than one version, which in turn will
 * have one or more sections, which will have one or more items with metadata
 * for presentation.
 *
 * @author thickerson
 *
 *
 */
public class SectionDAO extends AuditableEntityDAO<SectionBean> {

    // private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SECTION;
    }

    public SectionDAO(DataSource ds) {
        super(ds);
    }

    public SectionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SectionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);// crf version id
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);// label
        this.setTypeExpected(5, TypeNames.STRING);// title
        this.setTypeExpected(6, TypeNames.STRING);// subtitle
        this.setTypeExpected(7, TypeNames.STRING);// instructions
        this.setTypeExpected(8, TypeNames.STRING);// page num label
        this.setTypeExpected(9, TypeNames.INT);// order by
        this.setTypeExpected(10, TypeNames.INT);// parent id
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.DATE);
        this.setTypeExpected(13, TypeNames.INT);// owner id
        this.setTypeExpected(14, TypeNames.INT);// update id
        this.setTypeExpected(15, TypeNames.INT);// borders

    }

    @Override
    public SectionBean update(SectionBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(sb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(3), sb.getLabel());
        variables.put(new Integer(4), sb.getTitle());
        variables.put(new Integer(5), sb.getInstructions());
        variables.put(new Integer(6), sb.getSubtitle());
        variables.put(new Integer(7), sb.getPageNumberLabel());
        variables.put(new Integer(8), new Integer(sb.getOrdinal()));
        variables.put(new Integer(9), new Integer(sb.getUpdaterId()));
        variables.put(new Integer(10), new Integer(sb.getBorders()));
        variables.put(new Integer(11), new Integer(sb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);
        return sb;
    }

    @Override
    public SectionBean create(SectionBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), new Integer(sb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(3), sb.getLabel());
        variables.put(new Integer(4), sb.getTitle());
        variables.put(new Integer(5), sb.getInstructions());
        variables.put(new Integer(6), sb.getSubtitle());
        variables.put(new Integer(7), sb.getPageNumberLabel());
        variables.put(new Integer(8), new Integer(sb.getOrdinal()));
        variables.put(new Integer(9), new Integer(sb.getParentId()));
        variables.put(new Integer(10), new Integer(sb.getOwnerId()));
        variables.put(new Integer(11), new Integer(sb.getBorders()));
        this.executeUpdate(digester.getQuery("create"), variables);
        return sb;
    }

    public SectionBean getEntityFromHashMap(HashMap<String, Object> hm) {
        SectionBean eb = new SectionBean();
        this.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("section_id")).intValue());
        eb.setCRFVersionId(((Integer) hm.get("crf_version_id")).intValue());
        eb.setLabel((String) hm.get("label"));
        eb.setTitle((String) hm.get("title"));
        eb.setInstructions((String) hm.get("instructions"));
        eb.setSubtitle((String) hm.get("subtitle"));
        eb.setPageNumberLabel((String) hm.get("page_number_label"));
        eb.setOrdinal(((Integer) hm.get("ordinal")).intValue());
        eb.setParentId(((Integer) hm.get("parent_id")).intValue());
        eb.setBorders(((Integer) hm.get("borders")).intValue());
        return eb;
    }

    public ArrayList<SectionBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SectionBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<SectionBean> findByVersionId(int ID) {
        boolean useCache = true;
    	String queryName = "findByVersionId";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindAllQuery(queryName, variables, useCache);
    }

    public SectionBean findByPK(int ID) {
    	boolean useCache = true;
    	String queryName = "findByPK";
    	HashMap<Integer, Object> variables = variables(ID);
    	return executeFindByPKQuery(queryName, variables, useCache);
    }

	/**
	 * NOT IMPLEMENTED
	 */
    public ArrayList<SectionBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

	/**
	 * NOT IMPLEMENTED
	 */
    public ArrayList<SectionBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

    public ArrayList<SectionBean> findAllByCRFVersionId(int crfVersionId) {
    	String queryName = "findAllByCRFVersion";
        HashMap<Integer, Object> variables = variables(crfVersionId);
        return executeFindAllQuery(queryName, variables);
    }

    private HashMap<Integer, Integer> getNumItemsBySectionIdFromRows(ArrayList<HashMap<String, Object>> rows) {
        HashMap<Integer, Integer> answer = new HashMap<>();
        for(HashMap<String, Object> hm : rows) {
            Integer sectionIdInt = (Integer) hm.get("section_id");
            Long numItemsInt = (Long) hm.get("num_items");

            if (numItemsInt != null && sectionIdInt != null) {
                answer.put(sectionIdInt, numItemsInt.intValue());
            }
        }

        return answer;
    }
    
    //JN : bySectionID indicates group by section id
    public HashMap<Integer, Integer> getNumItemsBySectionId() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        String sql = digester.getQuery("getNumItemsBySectionId");
        ArrayList<HashMap<String, Object>> rows = this.select(sql);
        return getNumItemsBySectionIdFromRows(rows);
    }
/**
 * Groups by sectionId and takes sectionID
 * @param sb
 * @return
 */
    public HashMap<Integer, Integer> getNumItemsBySection(SectionBean sb){
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        String sql = digester.getQuery("getNumItemsBySection");
        ArrayList<HashMap<String, Object>> rows = this.select(sql);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public HashMap<Integer, Integer> getNumItemsPlusRepeatBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId());
        String sql = digester.getQuery("getNumItemsPlusRepeatBySectionId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap<Integer, Integer> getNumItemsCompletedBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId());
        String sql = digester.getQuery("getNumItemsCompletedBySectionId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public HashMap<Integer, Integer> getNumItemsCompletedBySection(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId());
        String sql = digester.getQuery("getNumItemsCompletedBySection");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap<Integer, Integer> getNumItemsPendingBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId());
        String sql = digester.getQuery("getNumItemsPendingBySectionId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public HashMap<Integer, Integer> getNumItemsPendingBySection(EventCRFBean ecb,SectionBean sb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId(), sb.getId());
        String sql = digester.getQuery("getNumItemsPendingBySection");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public HashMap<Integer, Integer> getNumItemsBlankBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId());
        String sql = digester.getQuery("getNumItemsBlankBySectionId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public HashMap<Integer, Integer> getNumItemsBlankBySection(EventCRFBean ecb,SectionBean sb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.LONG); // count

        HashMap<Integer, Object> variables = variables(ecb.getId(), sb.getId());
        String sql = digester.getQuery("getNumItemsBlankBySectionId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }
    
    public SectionBean findNext(EventCRFBean ecb, SectionBean current) {
        String queryName = "findNext";
        HashMap<Integer, Object> variables = variables(ecb.getCRFVersionId(), current.getOrdinal());
        return executeFindByPKQuery(queryName, variables);
    }

    public SectionBean findPrevious(EventCRFBean ecb, SectionBean current) {
        String queryName = "findPrevious";
        HashMap<Integer, Object> variables = variables(ecb.getCRFVersionId(), current.getOrdinal());
        return executeFindByPKQuery(queryName, variables);
    }

    public void deleteTestSection(String label) {
    	String query = digester.getQuery("deleteTestSection");
        HashMap<Integer, Object> variables = variables(label);
        this.executeUpdate(query, variables);
    }

    public boolean hasSCDItem(Integer sectionId) {
        return countSCDItemBySectionId(sectionId) > 0;
    }
    
    public int countSCDItemBySectionId(Integer sectionId) {
        HashMap<Integer, Object> variables = variables(sectionId);
        String query = digester.getQuery("countSCDItemBySectionId");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
           result = 0;
        }
        return result;
    }
    
    public boolean containNormalItem(Integer crfVersionId, Integer sectionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); //item_id
        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), sectionId);
        variables.put(new Integer(2), crfVersionId);
        variables.put(new Integer(3), crfVersionId);
        variables.put(new Integer(4), sectionId);
        variables.put(new Integer(5), crfVersionId);
        
        ArrayList<HashMap<String, Object>> rows = this.select(digester.getQuery("containNormalItem"), variables);
        if(rows.size()>0) {
            return (Integer) (rows.get(0)).get("item_id") > 0;
        } else {
            return false;
        }
    }

	@Override
	public SectionBean emptyBean() {
		return new SectionBean();
	}
}