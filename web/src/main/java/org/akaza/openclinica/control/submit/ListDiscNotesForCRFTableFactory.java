/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesForCRFFilter;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesForCRFSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.lang.StringUtils;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

public class ListDiscNotesForCRFTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private StudyGroupDAO studyGroupDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private CRFDAO crfDAO;
    private StudyBean studyBean;
    private String[] columnNames = new String[] {};
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<CRFBean> crfBeans;
    private ArrayList<EventDefinitionCRFBean> eventDefinitionCrfs;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private ResourceBundle resword = ResourceBundleProvider.getWordsBundle();
    private ResourceBundle resformat = ResourceBundleProvider.getFormatBundle();
    private StudyEventDefinitionBean selectedStudyEventDefinition;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes;
    private Set<Integer> resolutionStatusIds;

    final HashMap<Integer, String> imageIconPaths = new HashMap<Integer, String>(8);
    final HashMap<Integer, String> crfColumnImageIconPaths = new HashMap<Integer, String>(8);
    final HashMap<Integer, String> discNoteIconPaths = new HashMap<Integer, String>(8);

    public ListDiscNotesForCRFTableFactory() {
        imageIconPaths.put(1, "images/icon_Scheduled.gif");
        imageIconPaths.put(2, "images/icon_NotStarted.gif");
        imageIconPaths.put(3, "images/icon_InitialDE.gif");
        imageIconPaths.put(4, "images/icon_DEcomplete.gif");
        imageIconPaths.put(5, "images/icon_Stopped.gif");
        imageIconPaths.put(6, "images/icon_Skipped.gif");
        imageIconPaths.put(7, "images/icon_Locked.gif");
        imageIconPaths.put(8, "images/icon_Signed.gif");

        crfColumnImageIconPaths.put(0, "images/icon_Invalid.gif");
        crfColumnImageIconPaths.put(1, "images/icon_NotStarted.gif");
        crfColumnImageIconPaths.put(2, "images/icon_InitialDE.gif");
        crfColumnImageIconPaths.put(3, "images/icon_DEcomplete.gif");
        crfColumnImageIconPaths.put(4, "images/icon_DDE.gif");
        crfColumnImageIconPaths.put(5, "images/icon_DEcomplete.gif");
        crfColumnImageIconPaths.put(6, "images/icon_DEcomplete.gif");
        crfColumnImageIconPaths.put(7, "images/icon_Locked.gif");

        discNoteIconPaths.put(1,
                "<img name='icon_Note' src='images/icon_Note.gif' border='0' alt='" + resword.getString("open") + "' title='" + resword.getString("open")
                    + "'/>");
        discNoteIconPaths.put(2, "<img name='icon_flagYellow' src='images/icon_flagYellow.gif' border='0' alt='" + resword.getString("updated") + "' title='"
            + resword.getString("updated") + "' />");
        discNoteIconPaths.put(3, "<img name='icon_flagGreen' src='images/icon_flagGreen.gif' border='0' alt='" + resword.getString("resolved") + "' title='"
            + resword.getString("resolved") + "'/>");
        discNoteIconPaths.put(4, "<img name='icon_flagBlack' src='images/icon_flagBlack.gif' border='0' alt='" + resword.getString("closed") + "' title='"
            + resword.getString("closed") + "'/>");
        discNoteIconPaths.put(5, "<img name='icon_flagWhite' src='images/icon_flagWhite.gif' border='0' alt='" + resword.getString("not_applicable")
            + "' title='" + resword.getString("not_applicable") + "'/>");

    }

    @Override
    protected String getTableName() {
        return "listDiscNotesForCRF";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn(columnNames[0]), resword.getString("study_subject_ID"), null, null);
        configureColumn(row.getColumn(columnNames[1]), resword.getString("event_status"), new EventStatusCellEditor(),
                new SubjectEventStatusDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[2]), resword.getString("event_date"), new EventStartDateCellEditor(), null);

        // crf columns
        for (int i = 3; i < columnNames.length - 1; i++) {
            CRFBean crfBean = crfBeans.get(i - 3);
            configureColumn(row.getColumn(columnNames[i]), crfBean.getName(), new EventCrfCellEditor(), new SubjectEventCRFStatusDroplistFilterEditor(), false,
                    false);
        }

        String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn(columnNames[columnNames.length - 1]), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true,
                false);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        getColumnNamesMap();
        tableFacade.addFilterMatcher(new MatcherKey(Character.class), new CharFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "event.status"), new SubjectEventStatusFilterMatcher());

        // crf columns filtering
        for (int i = 3; i < columnNames.length - 1; i++) {
            tableFacade.addFilterMatcher(new MatcherKey(String.class, columnNames[i]), new SubjectEventCRFStatusFilterMatcher());
        }
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListDiscNotesForCRFTableToolbar toolbar = new ListDiscNotesForCRFTableToolbar(getStudyEventDefinitions(), selectedStudyEventDefinition);
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);
        toolbar.setDiscNoteType(discNoteType);
        toolbar.setResolutionStatus(resolutionStatus);
        toolbar.setResword(resword);
        toolbar.setModule(module);
        tableFacade.setToolbar(toolbar);
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        Limit limit = tableFacade.getLimit();

        ListDiscNotesForCRFFilter listDiscNotesForCRF = getListEventsForSubjectFilter(limit);

        listDiscNotesForCRF.addFilter("dn.discrepancy_note_type_id", this.discNoteType);
        StringBuffer constraints = new StringBuffer();
        if (this.discNoteType > 0 && this.discNoteType < 10) {
            constraints.append(" and dn.discrepancy_note_type_id=" + this.discNoteType);
        }
        if (this.resolutionStatusIds != null && this.resolutionStatusIds.size() > 0) {
            String s = " and (";
            for (Integer resolutionStatusId : this.resolutionStatusIds) {
                s += "dn.resolution_status_id = " + resolutionStatusId + " or ";
            }
            s = s.substring(0, s.length() - 3) + " )";
            listDiscNotesForCRF.addFilter("dn.resolution_status_id", s);
            constraints.append(s);
        }

        if (!limit.isComplete()) {
            int totalRows = getStudySubjectDAO().getCountWithFilter(listDiscNotesForCRF, getStudyBean());
            tableFacade.setTotalRows(totalRows);
        }

        ListDiscNotesForCRFSort eventsForSubjectSort = getListEventsForSubjectSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<StudySubjectBean> items =
            getStudySubjectDAO().getWithFilterAndSort(getStudyBean(), listDiscNotesForCRF, eventsForSubjectSort, rowStart, rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        boolean hasDN = false;
        for (StudySubjectBean studySubjectBean : items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            theItem.put("studySubject.label", studySubjectBean.getLabel());
            theItem.put("studySubject.status", studySubjectBean.getStatus());

            SubjectBean subjectBean = (SubjectBean) getSubjectDAO().findByPK(studySubjectBean.getSubjectId());
            theItem.put("subject", subjectBean);
            theItem.put("subject.charGender", subjectBean.getGender());

            // Get EventCrfs for study Subject
            List<EventCRFBean> eventCrfs = getEventCRFDAO().findAllByStudySubject(studySubjectBean.getId());
            HashMap<String, EventCRFBean> crfAsKeyEventCrfAsValue = new HashMap<String, EventCRFBean>();
            for (EventCRFBean eventCRFBean : eventCrfs) {
                CRFBean crf = getCrfDAO().findByVersionId(eventCRFBean.getCRFVersionId());
                crfAsKeyEventCrfAsValue.put(crf.getId() + "_" + eventCRFBean.getStudyEventId(), eventCRFBean);
            }

            // Get the event Status
            List<StudyEventBean> eventsForStudySubjectAndEventDefinitions =
                getStudyEventDAO().findAllByDefinitionAndSubject(selectedStudyEventDefinition, studySubjectBean);
            List<DisplayBean> events = new ArrayList<DisplayBean>();

            // study event size < 1
            if (eventsForStudySubjectAndEventDefinitions.size() < 1) {
                DisplayBean d = new DisplayBean();
                d.getProps().put("event", null);
                d.getProps().put("event.status", SubjectEventStatus.NOT_SCHEDULED);
                d.getProps().put("event.startDate", null);
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
                    d.getProps().put("crf_" + crf.getId(), DataEntryStage.UNCOMPLETED);
                    d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));
                    d.getProps().put("crf_" + crf.getId() + "_discCounts", discCounts);
                    theItem.put("crf_" + crf.getId(), "");
                }
                events.add(d);
            }
            // study event size >0
            for (StudyEventBean studyEventBean : eventsForStudySubjectAndEventDefinitions) {
                DisplayBean d = new DisplayBean();
                d.getProps().put("event", studyEventBean);
                d.getProps().put("event.status", studyEventBean.getSubjectEventStatus());
                d.getProps().put("event.startDate", studyEventBean.getCreatedDate());
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    EventCRFBean eventCRFBean = crfAsKeyEventCrfAsValue.get(crf.getId() + "_" + studyEventBean.getId());
                    HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
                    if (eventCRFBean != null) {
                        d.getProps().put("crf_" + crf.getId(), eventCRFBean.getStage());
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", eventCRFBean);
                        // List<DiscrepancyNoteBean> discs =
                        // getDiscrepancyNoteDAO().findAllByStudyEvent(studyEventBean);
                        List<DiscrepancyNoteBean> discs =
                            getDiscrepancyNoteDAO().findAllParentItemNotesByEventCRFWithConstraints(eventCRFBean.getId(), constraints);
                        hasDN = hasDN == false ? discs != null && discs.size() > 0 : hasDN;
                        for (DiscrepancyNoteBean discrepancyNoteBean : discs) {
                            Integer value = discCounts.get(discrepancyNoteBean.getResStatus());
                            if (value != null) {
                                discCounts.put(discrepancyNoteBean.getResStatus(), ++value);
                            } else {
                                discCounts.put(discrepancyNoteBean.getResStatus(), 1);
                            }
                        }
                        d.getProps().put("crf_" + crf.getId() + "_discCounts", discCounts);

                    } else {
                        d.getProps().put("crf_" + crf.getId(), DataEntryStage.UNCOMPLETED);
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                        d.getProps().put("crf_" + crf.getId() + "_discCounts", discCounts);
                    }
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));

                    theItem.put("crf_" + crf.getId(), "");
                }
                events.add(d);
            }
            theItem.put("events", events);
            theItem.put("event.status", "");
            theItem.put("event.startDate", "");
            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);
        this.setStudyHasDiscNotes(hasDN);
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("studySubject.label");

        columnNamesList.add("event.status");
        columnNamesList.add("event.startDate");

        for (CRFBean crfBean : getCrfs(selectedStudyEventDefinition)) {
            columnNamesList.add("crf_" + crfBean.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    protected ListDiscNotesForCRFFilter getListEventsForSubjectFilter(Limit limit) {
        ListDiscNotesForCRFFilter listDiscNotesForCRFFilter = new ListDiscNotesForCRFFilter(selectedStudyEventDefinition.getId());
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            listDiscNotesForCRFFilter.addFilter(property, value);
        }

        return listDiscNotesForCRFFilter;
    }

    protected ListDiscNotesForCRFSort getListEventsForSubjectSort(Limit limit) {
        ListDiscNotesForCRFSort listDiscNotesForCRFSort = new ListDiscNotesForCRFSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listDiscNotesForCRFSort.addSort(property, order);
        }

        return listDiscNotesForCRFSort;
    }

    private ArrayList<StudyEventDefinitionBean> getStudyEventDefinitions() {
        if (this.studyEventDefinitions == null) {
            if (studyBean.getParentStudyId() > 0) {
                StudyBean parentStudy = (StudyBean) getStudyDAO().findByPK(studyBean.getParentStudyId());
                studyEventDefinitions = getStudyEventDefinitionDao().findAllByStudy(parentStudy);
            } else {
                studyEventDefinitions = getStudyEventDefinitionDao().findAllByStudy(studyBean);
            }
        }
        return this.studyEventDefinitions;
    }

    private ArrayList<CRFBean> getCrfs(StudyEventDefinitionBean eventDefinition) {
        if (this.crfBeans == null) {
            crfBeans = new ArrayList<CRFBean>();
            eventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();
            ArrayList<Integer> ids = new ArrayList<Integer>();
            List<EventDefinitionCRFBean> edcs = new ArrayList<EventDefinitionCRFBean>();
            if (this.getStudyBean().getParentStudyId() > 0) {
                edcs =
                    (List<EventDefinitionCRFBean>) getEventDefintionCRFDAO().findAllActiveNonHiddenByEventDefinitionIdAndStudy(eventDefinition.getId(),
                            this.getStudyBean());
            } else {
                edcs = getEventDefintionCRFDAO().findAllActiveByEventDefinitionId(eventDefinition.getId());
            }
            if (edcs.size() > 0) {
                for (EventDefinitionCRFBean eventDefinitionCrf : edcs) {
                    CRFBean crf = (CRFBean) getCrfDAO().findByPK(eventDefinitionCrf.getCrfId());
                    if (!ids.contains(crf.getId())) {
                        crfBeans.add(crf);
                        ids.add(crf.getId());
                        eventDefinitionCrfs.add(eventDefinitionCrf);
                    }

                }
            }
            return crfBeans;
        }
        return crfBeans;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    public void setStudyEventDefinitionDao(StudyEventDefinitionDAO studyEventDefinitionDao) {
        this.studyEventDefinitionDao = studyEventDefinitionDao;
    }

    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        return studySubjectDAO;
    }

    public void setStudySubjectDAO(StudySubjectDAO studySubjectDAO) {
        this.studySubjectDAO = studySubjectDAO;
    }

    public SubjectDAO getSubjectDAO() {
        return subjectDAO;
    }

    public void setSubjectDAO(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        return studyEventDAO;
    }

    public void setStudyEventDAO(StudyEventDAO studyEventDAO) {
        this.studyEventDAO = studyEventDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        return studyGroupClassDAO;
    }

    public void setStudyGroupClassDAO(StudyGroupClassDAO studyGroupClassDAO) {
        this.studyGroupClassDAO = studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        return subjectGroupMapDAO;
    }

    public void setSubjectGroupMapDAO(SubjectGroupMapDAO subjectGroupMapDAO) {
        this.subjectGroupMapDAO = subjectGroupMapDAO;
    }

    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

    public StudyGroupDAO getStudyGroupDAO() {
        return studyGroupDAO;
    }

    public void setStudyGroupDAO(StudyGroupDAO studyGroupDAO) {
        this.studyGroupDAO = studyGroupDAO;
    }

    public StudyUserRoleBean getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(StudyUserRoleBean currentRole) {
        this.currentRole = currentRole;
    }

    public EventCRFDAO getEventCRFDAO() {
        return eventCRFDAO;
    }

    public void setEventCRFDAO(EventCRFDAO eventCRFDAO) {
        this.eventCRFDAO = eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefintionCRFDAO() {
        return eventDefintionCRFDAO;
    }

    public void setEventDefintionCRFDAO(EventDefinitionCRFDAO eventDefintionCRFDAO) {
        this.eventDefintionCRFDAO = eventDefintionCRFDAO;
    }

    public CRFDAO getCrfDAO() {
        return crfDAO;
    }

    public void setCrfDAO(CRFDAO crfDAO) {
        this.crfDAO = crfDAO;
    }

    public UserAccountBean getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccountBean currentUser) {
        this.currentUser = currentUser;
    }

    public StudyEventDefinitionBean getSelectedStudyEventDefinition() {
        return selectedStudyEventDefinition;
    }

    public void setSelectedStudyEventDefinition(StudyEventDefinitionBean selectedStudyEventDefinition) {
        this.selectedStudyEventDefinition = selectedStudyEventDefinition;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        return discrepancyNoteDAO;
    }

    public void setDiscrepancyNoteDAO(DiscrepancyNoteDAO discrepancyNoteDAO) {
        this.discrepancyNoteDAO = discrepancyNoteDAO;
    }

    private class CharFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            String item = StringUtils.lowerCase(String.valueOf(itemValue));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));
            if (StringUtils.contains(item, filter)) {
                return true;
            }

            return false;
        }
    }

    public class StatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {

            String item = StringUtils.lowerCase(String.valueOf(((Status) itemValue).getId()));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));

            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    public class SubjectEventStatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            // No need to evaluate itemValue and filterValue.
            return true;
        }
    }

    public class SubjectGroupFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {

            String item = StringUtils.lowerCase(String.valueOf(itemValue).trim());
            String filter = StringUtils.lowerCase(String.valueOf(filterValue.trim()));
            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    public class SubjectEventCRFStatusFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {
            // No need to evaluate itemValue and filterValue.
            return true;
        }
    }

    private class SubjectEventStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object subjectEventStatus : SubjectEventStatus.toArrayList()) {
                ((SubjectEventStatus) subjectEventStatus).getName();
                options.add(new Option(String.valueOf(((SubjectEventStatus) subjectEventStatus).getId()), ((SubjectEventStatus) subjectEventStatus).getName()));
            }
            return options;
        }
    }

    private class SubjectEventCRFStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object eventCRFStatus : DataEntryStage.toArrayList()) {
                if (((DataEntryStage) eventCRFStatus).getId() != 0) {
                    options.add(new Option(String.valueOf(((DataEntryStage) eventCRFStatus).getId()), ((DataEntryStage) eventCRFStatus).getName()));
                }
            }
            return options;
        }
    }

    private class EventStatusCellEditor implements CellEditor {

        SubjectEventStatus subjectEventStatus;
        StudyEventBean studyEvent;
        List<DisplayBean> events;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            List<StudyEventBean> studyEvents;

            StringBuilder url = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                DisplayBean display = events.get(i);
                subjectEventStatus = (SubjectEventStatus) display.getProps().get("event.status");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                studyEvents = new ArrayList<StudyEventBean>();
                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                }
                // url.append(eventDivBuilder(subject, Integer.valueOf(rowcount
                // + String.valueOf(i)), studyEvents, studyEventDefinition,
                // studySubjectBean));
                url.append("<table><tr><td>");
                url.append("<img src='" + imageIconPaths.get(subjectEventStatus.getId()) + "' border='0' style='position: relative; left: 7px;'>");
                url.append("</td></tr></table>");
            }

            return url.toString();
        }
    }

    private class EventStartDateCellEditor implements CellEditor {
        Date eventStartDate;
        List<DisplayBean> events;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            StringBuilder url = new StringBuilder();

            for (DisplayBean display : events) {
                eventStartDate = (Date) display.getProps().get("event.startDate");
                url.append("<table border='0'  cellpadding='0'  cellspacing='0' ><tr valign='top' ><td>");
                url.append(eventStartDate == null ? "" : formatDate(eventStartDate));
                url.append("</td></tr></table>");
            }
            return url.toString();
        }
    }

    private class EventCrfCellEditor implements CellEditor {

        DataEntryStage dataEntryStage;
        StudyEventBean studyEvent;
        List<DisplayBean> events;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            List<StudyEventBean> studyEvents;
            HashMap<ResolutionStatus, Integer> discCounts;

            StringBuilder url = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {

                DisplayBean display = events.get(i);
                dataEntryStage = (DataEntryStage) display.getProps().get(property);
                discCounts = (HashMap<ResolutionStatus, Integer>) display.getProps().get(property + "_discCounts");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                studyEvents = new ArrayList<StudyEventBean>();
                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                }

                /*
                 * EventCrfLayerBuilder eventCrfLayerBuilder = new EventCrfLayerBuilder(subject, Integer.valueOf(rowcount + String.valueOf(i)), studyEvents,
                 * dataEntryStage, eventCrf, studySubjectBean, studyBean, currentRole, currentUser, eventDefintionCrf, crf);
                 */

                // url.append(eventCrfLayerBuilder.buid());
                url.append("<table><tr><td>");
                url.append("<img src='" + crfColumnImageIconPaths.get(dataEntryStage.getId()) + "' border='0'>");
                for (ResolutionStatus key : discCounts.keySet()) {
                    url.append(discNoteIconPaths.get(key.getId()) + "(" + discCounts.get(key) + ") ");
                }
                url.append("</td></tr></table>");

            }

            return url.toString();
        }
    }

    private class ActionsCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();

                url.append(viewStudySubjectLinkBuilder(studySubjectBean));
                url.append(downloadNotesLinkBuilder(studySubjectBean));
                value = url.toString();
            }

            return value;
        }

    }

    class DisplayBean {

        HashMap<String, Object> props = new HashMap<String, Object>();

        public HashMap<String, Object> getProps() {
            return props;
        }

        public void setProps(HashMap<String, Object> props) {
            this.props = props;
        }
    }

    private String viewStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&module=" + module + "&id=" + studySubject.getId() + "&resolutionStatus=" + this.resolutionStatus
                        + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view"))
                    .append("hspace=\"2\" style=\"float:left\" align=\"left\"").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
        } else {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&id=" + studySubject.getId() + "&module=" + module + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view"))
                    .append("hspace=\"2\" style=\"float:left\" align=\"left\"").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
        }
        return actionLink.toString();
    }

    private String downloadNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.isStudyHasDiscNotes()) {
            if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType
                            + "&resolutionStatus=" + resolutionStatus + "')");
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes"))
                        .title(resword.getString("download_discrepancy_notes")).append("hspace=\"2\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            } else {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?discNoteType=" + discNoteType + "&module=" + module + "&subjectId="
                            + studySubject.getId() + "')");
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes"))
                        .title(resword.getString("download_discrepancy_notes")).append("hspace=\"2\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            }
        }
        return actionLink.toString();
    }

    private String formatDate(Date date) {
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Integer getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(Integer resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public Integer getDiscNoteType() {
        return discNoteType;
    }

    public void setDiscNoteType(Integer discNoteType) {
        this.discNoteType = discNoteType;
    }

    public Boolean getStudyHasDiscNotes() {
        return studyHasDiscNotes;
    }

    public void setStudyHasDiscNotes(Boolean studyHasDiscNotes) {
        this.studyHasDiscNotes = studyHasDiscNotes;
    }

    public Boolean isStudyHasDiscNotes() {
        return this.studyHasDiscNotes;
    }

    public Set<Integer> getResolutionStatusIds() {
        return resolutionStatusIds;
    }

    public void setResolutionStatusIds(Set<Integer> resolutionStatusIds) {
        this.resolutionStatusIds = resolutionStatusIds;
    }

}
