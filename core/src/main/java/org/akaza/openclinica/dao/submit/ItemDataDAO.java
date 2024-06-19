/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.submit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

/**
 * <P>
 * ItemDataDAO.java, the equivalent to AnswerDAO in the original code base. If item is date, item data value has to be
 * saved into database as specified in ISO 8601.
 * 
 * @author thickerson
 * 
 * 
 */
public class ItemDataDAO extends AuditableEntityDAO<ItemDataBean> {

    boolean formatDates = true;

    // YW 12-06-2007 <<!!! Be careful when there is item with data-type as
    // "Date".
    // You have to make sure that string pattern conversion has been done before
    // you insert/update items into database
    // or once you fetched items from database.
    // The correct patterns are:
    // in database, it should be oc_date_format_string
    // in application, it should be local date_format_string
    // If your method makes use of "getEntityFromHashMap", conversion has been
    // handled.
    // And as at this point, "getEntityFromHashMap" is used for fetched data
    // from database,
    // conversion is from oc_date_format pattern to local date_format pattern.
    // YW >>

    public boolean isFormatDates() {
        return formatDates;
    }

    public void setFormatDates(boolean formatDates) {
        this.formatDates = formatDates;
    }

    private void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
    }

    public ItemDataDAO(DataSource ds) {
        super(ds);
        setQueryNames();
        if (this.locale == null) {
            this.locale = ResourceBundleProvider.getLocale(); // locale still might be null.
        }
    }

    public ItemDataDAO(DataSource ds, Locale locale) {
        super(ds);
        setQueryNames();
        if (locale != null) {
            this.locale = locale;
        } else {
            this.locale = ResourceBundleProvider.getLocale();
        }
        if (this.locale != null) {
            local_df_string = ResourceBundleProvider.getFormatBundle(this.locale).getString("date_format_string");
        }
    }

    public ItemDataDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemDataDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEMDATA;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.DATE);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.INT);// owner id
        this.setTypeExpected(9, TypeNames.INT);// update id
        this.setTypeExpected(10, TypeNames.INT);// ordinal
        this.setTypeExpected(11, TypeNames.INT);// old_status_id
        this.setTypeExpected(12, TypeNames.BOOL);// ocform_deleted
    }

    @Override
    public ItemDataBean update(ItemDataBean idb) {
        // YW 12-06-2007 << convert to oc_date_format_string pattern before
        // inserting into database
        ItemDataType dataType = getDataType(idb.getItemId());
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), local_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }
        idb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getEventCRFId()));
        variables.put(new Integer(2), new Integer(idb.getItemId()));
        variables.put(new Integer(3), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(4), idb.getValue());
        variables.put(new Integer(5), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(6), new Integer(idb.getOrdinal()));
        variables.put(new Integer(7), new Integer(idb.getOldStatus().getId()));
        variables.put(new Integer(8), new Boolean(idb.isDeleted()));
        variables.put(new Integer(9), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    /**
     * This will update item data value
     * 
     * @param eb
     * @return
     */
    public EntityBean updateValue(EntityBean eb) {
        ItemDataBean idb = (ItemDataBean) eb;

        // YW 12-06-2007 << convert to oc_date_format_string pattern before
        // inserting into database
        ItemDataType dataType = getDataType(idb.getItemId());
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), local_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }
        idb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(2), idb.getValue());
        variables.put(new Integer(3), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(4), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("updateValue"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    public EntityBean updateValueForRemoved(EntityBean eb) {
        ItemDataBean idb = (ItemDataBean) eb;

        // YW 12-06-2007 << convert to oc_date_format_string pattern before
        // inserting into database
        ItemDataType dataType = getDataType(idb.getItemId());
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), local_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }
        idb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(2), idb.getValue());
        variables.put(new Integer(3), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(4), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("updateValueForRemoved"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    /**
     * this will update item data status
     */
    public EntityBean updateStatus(EntityBean eb) {
        ItemDataBean idb = (ItemDataBean) eb;
        idb.setActive(false);
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(2), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("updateStatus"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    /*
     * current_df_string= yyyy-MM-dd oc_df_string = yyyy-mm-dd local_df_string = dd-MMM-yyyy
     */
    public ItemDataBean setItemDataBeanIfDateOrPdate(ItemDataBean idb, String current_df_string, ItemDataType dataType) {
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), current_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }
        return idb;
    }

    /**
     * This will update item data value
     * 
     * @param eb
     * @return
     */
    public EntityBean updateValue(EntityBean eb, String current_df_string) {
        ItemDataBean idb = (ItemDataBean) eb;

        ItemDataType dataType = getDataType(idb.getItemId());
        setItemDataBeanIfDateOrPdate(idb, current_df_string, dataType);

        idb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(2), idb.getValue());
        variables.put(new Integer(3), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(4), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("updateValue"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    public EntityBean updateUser(EntityBean eb) {
        ItemDataBean idb = (ItemDataBean) eb;

        idb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(2), new Integer(idb.getId()));
        this.executeUpdate(digester.getQuery("updateUser"), variables);

        if (isQuerySuccessful()) {
            idb.setActive(true);
        }

        return idb;
    }

    @Override
    public ItemDataBean create(ItemDataBean idb) {
        // YW 12-06-2007 << convert to oc_date_format_string pattern before
        // inserting into database
        ItemDataType dataType = getDataType(idb.getItemId());
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), local_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }

        HashMap<Integer, Object> variables = new HashMap<>();
        int id = getNextPK();
        variables.put(new Integer(1), new Integer(id));
        variables.put(new Integer(2), new Integer(idb.getEventCRFId()));
        variables.put(new Integer(3), new Integer(idb.getItemId()));
        variables.put(new Integer(4), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(5), idb.getValue());
        variables.put(new Integer(6), new Integer(idb.getOwnerId()));
        variables.put(new Integer(7), new Integer(idb.getOrdinal()));
        variables.put(new Integer(8), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(9), new Boolean(idb.isDeleted()));
        this.executeUpdate(digester.getQuery("create"), variables);

        if (isQuerySuccessful()) {
            idb.setId(id);
        }

        return idb;
    }

    public EntityBean upsert(EntityBean eb) {
        ItemDataBean idb = (ItemDataBean) eb;
        // YW 12-06-2007 << convert to oc_date_format_string pattern before
        // inserting into database
        ItemDataType dataType = getDataType(idb.getItemId());
        if (dataType.equals(ItemDataType.DATE)) {
            idb.setValue(Utils.convertedItemDateValue(idb.getValue(), local_df_string, oc_df_string, locale));
        } else if (dataType.equals(ItemDataType.PDATE)) {
            idb.setValue(formatPDate(idb.getValue()));
        }

        HashMap<Integer, Object> variables = new HashMap<>();
        int id = getNextPK();
        variables.put(new Integer(1), new Integer(id));
        variables.put(new Integer(2), new Integer(idb.getEventCRFId()));
        variables.put(new Integer(3), new Integer(idb.getItemId()));
        variables.put(new Integer(4), new Integer(idb.getStatus().getId()));
        variables.put(new Integer(5), idb.getValue());
        variables.put(new Integer(6), new Integer(idb.getOwnerId()));
        variables.put(new Integer(7), new Integer(idb.getOrdinal()));
        variables.put(new Integer(8), new Integer(idb.getUpdaterId()));
        variables.put(new Integer(9), new Boolean(idb.isDeleted()));
        this.executeUpdate(digester.getQuery("upsert"), variables);

        if (isQuerySuccessful()) {
            idb.setId(id);
        }

        return idb;
    }

    /*
     * Small check to make sure the type is a date, tbh
     */
    public ItemDataType getDataType(int itemId) {
        ItemDAO itemDAO = new ItemDAO(this.getDs());
        ItemBean itemBean = (ItemBean) itemDAO.findByPK(itemId);
        return itemBean.getDataType();
    }

    // public boolean isPDateType(int itemId) {
    // ItemDAO itemDAO = new ItemDAO(this.getDs());
    // ItemBean itemBean = (ItemBean)itemDAO.findByPK(itemId);
    // if (itemBean.getDataType().equals(ItemDataType.PDATE)) {
    // return true;
    // }
    // return false;
    //
    // }

    public String formatPDate(String pDate) {
        String temp = "";
        if (pDate != null && pDate.length() > 0) {
            String yearMonthFormat = I18nFormatUtil.yearMonthFormatString(this.locale);
            String yearFormat = I18nFormatUtil.yearFormatString();
            String dateFormat = I18nFormatUtil.dateFormatString(this.locale);
            try {
                if (StringUtil.isFormatDate(pDate, dateFormat, this.locale)) {
                    temp = new SimpleDateFormat(oc_df_string, this.locale).format(new SimpleDateFormat(dateFormat, this.locale).parse(pDate));
                } else if (StringUtil.isPartialYear(pDate, yearFormat, this.locale)) {
                    temp = pDate;
                } else if (StringUtil.isPartialYearMonth(pDate, yearMonthFormat, this.locale)) {
                    temp = new SimpleDateFormat(ApplicationConstants.getPDateFormatInSavedData(), this.locale).format(new SimpleDateFormat(yearMonthFormat,
                            this.locale).parse(pDate));
                }
            } catch (Exception ex) {
                logger.warn("Parsial Date Parsing Exception........");
            }
        }
        return temp;
    }

    public String reFormatPDate(String pDate) {
        String temp = "";
        if (pDate != null && pDate.length() > 0) {
            String yearMonthFormat = I18nFormatUtil.yearMonthFormatString(this.locale);
            String dateFormat = I18nFormatUtil.dateFormatString(this.locale);
            try {
                if (StringUtil.isFormatDate(pDate, oc_df_string, this.locale)) {
                    temp = new SimpleDateFormat(dateFormat, this.locale).format(new SimpleDateFormat(oc_df_string, this.locale).parse(pDate));
                } else if (StringUtil.isPartialYear(pDate, "yyyy", this.locale)) {
                    temp = pDate;
                } else if (StringUtil.isPartialYearMonth(pDate, ApplicationConstants.getPDateFormatInSavedData(), this.locale)) {
                    temp = new SimpleDateFormat(yearMonthFormat, this.locale).format(new SimpleDateFormat(ApplicationConstants.getPDateFormatInSavedData(),
                            this.locale).parse(pDate));
                }
            } catch (Exception ex) {
                logger.warn("Parsial Date Parsing Exception........");
            }
        }
        return temp;
    }

    public ItemDataBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ItemDataBean eb = new ItemDataBean();
        this.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("item_data_id")).intValue());
        eb.setEventCRFId(((Integer) hm.get("event_crf_id")).intValue());
        eb.setItemId(((Integer) hm.get("item_id")).intValue());
        eb.setValue((String) hm.get("value"));
        // YW 12-06-2007 << since "getEntityFromHashMap" only be used for find
        // right now,
        // convert item date value to local_date_format_string pattern once
        // fetching out from database
        if (formatDates) {
            ItemDataType dataType = getDataType(eb.getItemId());
            if (dataType.equals(ItemDataType.DATE)) {
                eb.setValue(Utils.convertedItemDateValue(eb.getValue(), oc_df_string, local_df_string, locale));
            } else if (dataType.equals(ItemDataType.PDATE)) {
                eb.setValue(reFormatPDate(eb.getValue()));
            }
        }
        eb.setStatus(Status.get(((Integer) hm.get("status_id")).intValue()));
        eb.setOrdinal(((Integer) hm.get("ordinal")).intValue());
        eb.setDeleted(((Boolean) hm.get("deleted")).booleanValue());
        eb.setOldStatus(Status.get(hm.get("old_status_id") == null ? 1 : ((Integer) hm.get("old_status_id")).intValue()));
        return eb;
    }

    public List<ItemDataBean> findByStudyEventAndOids(Integer studyEventId, String itemOid, String itemGroupOid) {
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), studyEventId);
        variables.put(new Integer(2), itemOid);
        variables.put(new Integer(3), itemGroupOid);
        variables.put(new Integer(4), Status.DELETED.getId());
        variables.put(new Integer(5), Status.AUTO_DELETED.getId());

        ArrayList<ItemDataBean> dataItems = this.executeFindAllQuery("findByStudyEventAndOIDs", variables);
        return dataItems;
    }

    @Override
    public ArrayList<ItemDataBean> findAll() {
        setTypesExpected();

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAll"));
        ArrayList<ItemDataBean> al = new ArrayList<ItemDataBean>();
        for(HashMap<String, Object> hm : alist) {
            ItemDataBean eb = (ItemDataBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<ItemDataBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    public EntityBean findByPK(int ID) {
        ItemDataBean eb = new ItemDataBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            eb = (ItemDataBean) this.getEntityFromHashMap(alist.get(0));
        }
        return eb;
    }

    public void delete(int itemDataId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(itemDataId));

        this.executeUpdate(digester.getQuery("delete"), variables);
        return;

    }

    public void deleteDnMap(int itemDataId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(itemDataId));

        this.executeUpdate(digester.getQuery("deleteDn"), variables);
        return;

    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemDataBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemDataBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

    public ArrayList<ItemDataBean> findAllBySectionIdAndEventCRFId(int sectionId, int eventCRFId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(sectionId));
        variables.put(new Integer(2), new Integer(eventCRFId));

        return this.executeFindAllQuery("findAllBySectionIdAndEventCRFId", variables);
    }

    public ArrayList<ItemDataBean> findByCRFVersion(CRFVersionBean crfVersionBean) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionBean.getId()));

        return this.executeFindAllQuery("findByCRFVersion", variables);
    }
    
    
        
    public ArrayList<ItemDataBean> findAllActiveBySectionIdAndEventCRFId(int sectionId, int eventCRFId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(sectionId));
        variables.put(new Integer(2), new Integer(eventCRFId));

        return this.executeFindAllQuery("findAllActiveBySectionIdAndEventCRFId", variables);
    }

    public ArrayList<ItemDataBean> findAllByEventCRFId(int eventCRFId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCRFId));

        return this.executeFindAllQuery("findAllByEventCRFId", variables);
    }

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemId(int eventCRFId, int itemId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCRFId));
        variables.put(new Integer(2), new Integer(itemId));

        return this.executeFindAllQuery("findAllByEventCRFIdAndItemId", variables);
    }

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemIdNoStatus(int eventCRFId, int itemId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCRFId));
        variables.put(new Integer(2), new Integer(itemId));

        return this.executeFindAllQuery("findAllByEventCRFIdAndItemIdNoStatus", variables);
    }

    public ArrayList<ItemDataBean> findAllBlankRequiredByEventCRFId(int eventCRFId, int crfVersionId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCRFId));
        variables.put(new Integer(2), new Integer(crfVersionId));

        return this.executeFindAllQuery("findAllBlankRequiredByEventCRFId", variables);
    }

    public ItemDataBean findByEventCRFIdAndItemName(EventCRFBean eventCrfBean, String itemName) {

        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCrfBean.getId()));
        variables.put(new Integer(2), itemName);

        ArrayList<ItemDataBean> itemDataBeans = this.executeFindAllQuery("findAllByEventCRFIdAndItemName", variables);
        return !itemDataBeans.isEmpty() && itemDataBeans.size() == 1 ? itemDataBeans.get(0) : null;
    }

    public void updateStatusByEventCRF(EventCRFBean eventCRF, Status s) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(s.getId()));
        variables.put(new Integer(2), new Integer(eventCRF.getId()));

        String sql = digester.getQuery("updateStatusByEventCRF");
        executeUpdate(sql, variables);

        return;
    }

    public ItemDataBean findByItemIdAndEventCRFId(int itemId, int eventCRFId) {
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(eventCRFId));

        EntityBean eb = this.executeFindByPKQuery("findByItemIdAndEventCRFId", variables);

        if (!eb.isActive()) {
            return new ItemDataBean();
        } else {
            return (ItemDataBean) eb;
        }
    }

    // YW, 1-25-2008, for repeating item
    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal) {
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(eventCRFId));
        variables.put(new Integer(3), new Integer(ordinal));

        EntityBean eb = this.executeFindByPKQuery("findByItemIdAndEventCRFIdAndOrdinal", variables);

        if (!eb.isActive()) {
            return new ItemDataBean();// hmm, return null instead?
        } else {
            return (ItemDataBean) eb;
        }
    }

    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinalRaw(int itemId, int eventCRFId, int ordinal) {
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(eventCRFId));
        variables.put(new Integer(3), new Integer(ordinal));

        EntityBean eb = this.executeFindByPKQuery("findByItemIdAndEventCRFIdAndOrdinal", variables);

        if (!eb.isActive()) {
            return new ItemDataBean();// hmm, return null instead?
        } else {
            return (ItemDataBean) eb;
        }
    }

    public int findAllRequiredByEventCRFId(EventCRFBean ecb) {
        setTypesExpected();
        int answer = 0;
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("findAllRequiredByEventCRFId");
        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);

        if (rows.size() > 0) {
            answer = rows.size();
        }

        return answer;
    }

    /**
     * Gets the maximum ordinal for item data in a given item group in a given section and event crf
     * 
     * @param ecb
     * @param sb
     * @param igb
     * @return
     */
    public int getMaxOrdinalForGroup(EventCRFBean ecb, SectionBean sb, ItemGroupBean igb) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        variables.put(new Integer(2), new Integer(sb.getId()));
        variables.put(new Integer(3), new Integer(igb.getId()));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getMaxOrdinalForGroup"), variables);
        if (alist != null && alist.size() > 0) {
            try {
                HashMap<String, Object> hm = alist.get(0);
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    /**
     * Gets the maximum ordinal for item data in a given item group in a given section and event crf
     * 
     * @param item_group_oid
     * 
     * @return
     */
    public int getMaxOrdinalForGroupByGroupOID(String item_group_oid, int event_crf_id) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(event_crf_id, item_group_oid);

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getMaxOrdinalForGroupByGroupOID"), variables);
        if (alist != null && alist.size() > 0) {
            try {
                HashMap<String, Object> hm = alist.get(0);
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    public int getMaxOrdinalForGroupByItemAndEventCrf(Integer itemId, EventCRFBean ec) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), itemId);
        variables.put(new Integer(2), new Integer(ec.getId()));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getMaxOrdinalForGroupByItemAndEventCrf"), variables);
        if (alist != null && alist.size() > 0) {
            try {
                HashMap<String, Object> hm = alist.get(0);
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    public boolean isItemExists(int item_id, int ordinal_for_repeating_group_field, int event_crf_id) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(item_id));
        variables.put(new Integer(2), new Integer(ordinal_for_repeating_group_field));
        variables.put(new Integer(3), new Integer(event_crf_id));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("isItemExists"), variables);
        if (alist != null && alist.size() > 0) {
            return true;
        }

        return false;
    }

    public int getGroupSize(int itemId, int eventcrfId) {
        HashMap<Integer, Object> variables = variables(itemId, eventcrfId);

        String query = digester.getQuery("getGroupSize");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public List<String> findValuesByItemOID(String itoid) {
        List<String> vals = new ArrayList<String>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, itoid);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findValuesByItemOID"), variables);
        for(HashMap<String, Object> hm : alist) {
            vals.add((String) hm.get("value"));
        }
        return vals;
    }

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemGroupId(int eventCRFId, int itemGroupId) {
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCRFId));
        variables.put(new Integer(2), new Integer(itemGroupId));

        return this.executeFindAllQuery("findAllByEventCRFIdAndItemGroupId", variables);
    }

	public void undelete(int itemDataId, int updaterId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(updaterId));
        variables.put(new Integer(2), new Integer(itemDataId));

        this.executeUpdate(digester.getQuery("undelete"), variables);
        return;

	}

	@Override
	public ItemDataBean emptyBean() {
		return new ItemDataBean();
	}
}