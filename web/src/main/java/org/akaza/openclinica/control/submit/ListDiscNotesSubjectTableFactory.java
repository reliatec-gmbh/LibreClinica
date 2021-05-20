/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesSubjectFilter;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesSubjectSort;
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
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

public class ListDiscNotesSubjectTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private StudyDAO studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyBean studyBean;
    private String[] columnNames = new String[] {};
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private ResourceBundle resword;
    private ResourceBundle resterm;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes;
    private Set<Integer> resolutionStatusIds;

    final HashMap<Integer, String> imageIconPaths = new HashMap<Integer, String>(8);
    final HashMap<Integer, String> discNoteIconPaths = new HashMap<Integer, String>(8);

    public ListDiscNotesSubjectTableFactory(ResourceBundle resterm) {
        this.resterm = resterm;
        imageIconPaths.put(1, "images/icon_Scheduled.gif");
        imageIconPaths.put(2, "images/icon_NotStarted.gif");
        imageIconPaths.put(3, "images/icon_InitialDE.gif");
        imageIconPaths.put(4, "images/icon_DEcomplete.gif");
        imageIconPaths.put(5, "images/icon_Stopped.gif");
        imageIconPaths.put(6, "images/icon_Skipped.gif");
        imageIconPaths.put(7, "images/icon_Locked.gif");
        imageIconPaths.put(8, "images/icon_Signed.gif");

        discNoteIconPaths.put(1, "<img name='icon_Note' src='images/icon_Note.gif' border='0' alt='" + resterm.getString("Open") + "' title='"
            + resterm.getString("Open") + "' align='left'/>");
        discNoteIconPaths.put(2, "<img name='icon_flagYellow' src='images/icon_flagYellow.gif' border='0' alt='" + resterm.getString("Updated") + "' title='"
            + resterm.getString("Updated") + "' align='left'/>");
        discNoteIconPaths.put(3, "<img name='icon_flagGreen' src='images/icon_flagGreen.gif' border='0' alt='" + resterm.getString("Resolved") + "' title='"
            + resterm.getString("Resolved") + "' align='left'/>");
        discNoteIconPaths.put(4, "<img name='icon_flagBlack' src='images/icon_flagBlack.gif' border='0' alt='" + resterm.getString("Closed") + "' title='"
            + resterm.getString("Closed") + "' align='left'/>");
        discNoteIconPaths.put(5, "<img name='icon_flagWhite' src='images/icon_flagWhite.gif' border='0' alt='" + resterm.getString("Not_Applicable")
            + "' title='" + resterm.getString("Not_Applicable") + "' align='left'/>");

    }

    @Override
    protected String getTableName() {
        return "listDiscNotes";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn(columnNames[0]), resword.getString("study_subject_ID"), null, null);
        configureColumn(row.getColumn(columnNames[1]), resword.getString("subject_status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[2]), "Site ID", null, null);

        // study event definition columns
        for (int i = 3; i < columnNames.length - 1; i++) {
            StudyEventDefinitionBean studyEventDefinition = studyEventDefinitions.get(i - 3);
            configureColumn(row.getColumn(columnNames[i]), studyEventDefinition.getName(), new StudyEventDefinitionMapCellEditor(),
                    new SubjectEventStatusDroplistFilterEditor(), true, false);
        }
        String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn(columnNames[columnNames.length - 1]), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true,
                false);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        // getColumnNames();
        getColumnNamesMap();
        tableFacade.addFilterMatcher(new MatcherKey(Character.class), new CharFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());
        // tableFacade.addFilterMatcher(new MatcherKey(Integer.class), new
        // SubjectEventStatusFilterMatcher());

        for (int i = 3; i < columnNames.length - 1; i++) {
            tableFacade.addFilterMatcher(new MatcherKey(Integer.class, columnNames[i]), new SubjectEventStatusFilterMatcher());
        }

    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListDiscNotesSubjectTableToolbar toolbar = new ListDiscNotesSubjectTableToolbar(getStudyEventDefinitions());
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);
        toolbar.setDiscNoteType(discNoteType);
        toolbar.setResolutionStatus(resolutionStatus);
        toolbar.setResword(resword);
        toolbar.setModule(module);
        tableFacade.setToolbar(toolbar);
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        StudyBean study = this.getStudyBean();
        Limit limit = tableFacade.getLimit();

        ListDiscNotesSubjectFilter subjectFilter = getSubjectFilter(limit);
        subjectFilter.addFilter("dn.discrepancy_note_type_id", this.discNoteType);
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
            subjectFilter.addFilter("dn.resolution_status_id", s);
            constraints.append(s);
        }

        if (!limit.isComplete()) {
            int totalRows = getStudySubjectDAO().getCountWithFilter(subjectFilter, study);
            tableFacade.setTotalRows(totalRows);
        }

        ListDiscNotesSubjectSort subjectSort = getSubjectSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(study, subjectFilter, subjectSort, rowStart, rowEnd);

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        boolean hasDN = false;
        for (StudySubjectBean studySubjectBean : items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            theItem.put("studySubject.label", studySubjectBean.getLabel());
            theItem.put("studySubject.status", studySubjectBean.getStatus());
            theItem.put("enrolledAt", ((StudyBean) getStudyDAO().findByPK(studySubjectBean.getStudyId())).getIdentifier());

            // Get All study events for this study subject and then put list in
            // HashMap with study event definition id as
            // key and a list of study events as the value.
            List<StudyEventBean> allStudyEventsForStudySubject = getStudyEventDAO().findAllByStudySubject(studySubjectBean);
            HashMap<Integer, List<StudyEventBean>> allStudyEventsForStudySubjectBySedId = new HashMap<Integer, List<StudyEventBean>>();
            theItem.put("isSignable", isSignable(allStudyEventsForStudySubject));

            for (StudyEventBean studyEventBean : allStudyEventsForStudySubject) {
                if (allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()) == null) {
                    ArrayList<StudyEventBean> a = new ArrayList<StudyEventBean>();
                    a.add(studyEventBean);
                    allStudyEventsForStudySubjectBySedId.put(studyEventBean.getStudyEventDefinitionId(), a);
                } else {
                    allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()).add(studyEventBean);
                }

            }

            for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {

                List<StudyEventBean> studyEvents = allStudyEventsForStudySubjectBySedId.get(studyEventDefinition.getId());
                SubjectEventStatus subjectEventStatus = null;
                HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();

                studyEvents = studyEvents == null ? new ArrayList<StudyEventBean>() : studyEvents;
                if (studyEvents.size() < 1) {
                    subjectEventStatus = SubjectEventStatus.NOT_SCHEDULED;
                } else {
                    for (StudyEventBean studyEventBean : studyEvents) {
                        discCounts = countAll(discCounts, studyEventBean, constraints, study.isSite(study.getParentStudyId()));
                        hasDN = hasDN == false ? discCounts.size() > 0 : hasDN;
                        if (studyEventBean.getSampleOrdinal() == 1) {
                            subjectEventStatus = studyEventBean.getSubjectEventStatus();
                            // break;
                        }
                    }

                }
                theItem.put("sed_" + studyEventDefinition.getId() + "_discCounts", discCounts);
                theItem.put("sed_" + studyEventDefinition.getId(), subjectEventStatus.getId());
                theItem.put("sed_" + studyEventDefinition.getId() + "_studyEvents", studyEvents);
                theItem.put("sed_" + studyEventDefinition.getId() + "_object", studyEventDefinition);

            }

            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);

        setStudyHasDiscNotes(hasDN);
    }

    private Boolean isSignable(List<StudyEventBean> allStudyEventsForStudySubject) {
        boolean isSignable = true;
        boolean isRequiredUncomplete;
        for (StudyEventBean studyEventBean : allStudyEventsForStudySubject) {
            if (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED) {
                isSignable = false;
                break;
            } else {
                isRequiredUncomplete = eventHasRequiredUncompleteCRFs(studyEventBean);
                if (isRequiredUncomplete) {
                    isSignable = false;
                    break;
                }
            }
        }
        return isSignable;
    }

    private boolean eventHasRequiredUncompleteCRFs(StudyEventBean studyEventBean) {

        List<EventCRFBean> eventCrfBeans = new ArrayList<EventCRFBean>();
        eventCrfBeans.addAll(getEventCRFDAO().findAllByStudyEvent(studyEventBean));
        // If the EventCRFBean has a completionStatusId of 0 (indicating that it
        // is not complete),
        // then find out whether it's required. If so, then return from the
        // method false.
        for (EventCRFBean crfBean : eventCrfBeans) {
            if (crfBean != null && crfBean.getCompletionStatusId() == 0) {
                if (getEventDefintionCRFDAO().isRequiredInDefinition(crfBean.getCRFVersionId(), studyEventBean)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("studySubject.label");
        columnNamesList.add("studySubject.status");
        columnNamesList.add("enrolledAt");

        for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {
            columnNamesList.add("sed_" + studyEventDefinition.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    protected ListDiscNotesSubjectFilter getSubjectFilter(Limit limit) {
        ListDiscNotesSubjectFilter listDiscNotesSubjectFilter = new ListDiscNotesSubjectFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            listDiscNotesSubjectFilter.addFilter(property, value);
        }

        return listDiscNotesSubjectFilter;
    }

    /**
     * A very custom way to sort the items. The PresidentSort acts as a command
     * for the Hibernate criteria object. There are probably many ways to do
     * this, but this is the most flexible way I have found. The point is you
     * need to somehow take the Limit information and sort the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected ListDiscNotesSubjectSort getSubjectSort(Limit limit) {
        ListDiscNotesSubjectSort listDiscNotesSubjectSort = new ListDiscNotesSubjectSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listDiscNotesSubjectSort.addSort(property, order);
        }

        return listDiscNotesSubjectSort;
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

    public StudyGroupDAO getStudyGroupDAO() {
        return studyGroupDAO;
    }

    public void setStudyGroupDAO(StudyGroupDAO studyGroupDAO) {
        this.studyGroupDAO = studyGroupDAO;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        return discrepancyNoteDAO;
    }

    public void setDiscrepancyNoteDAO(DiscrepancyNoteDAO discrepancyNoteDAO) {
        this.discrepancyNoteDAO = discrepancyNoteDAO;
    }

    public ResourceBundle getResword() {
        return resword;
    }

    public void setResword(ResourceBundle resword) {
        this.resword = resword;
    }

    public ResourceBundle getResterm() {
        return resterm;
    }

    public void setResterm(ResourceBundle resterm) {
        this.resterm = resterm;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public UserAccountBean getCurrentUser() {
        return currentUser;
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

    public Boolean isStudyHasDiscNotes() {
        return studyHasDiscNotes;
    }

    public void setStudyHasDiscNotes(Boolean studyHasDiscNotes) {
        this.studyHasDiscNotes = studyHasDiscNotes;
    }

    public void setCurrentUser(UserAccountBean currentUser) {
        this.currentUser = currentUser;
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
            String item = StringUtils.lowerCase(String.valueOf(itemValue));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));
            if (filter.equals(item)) {
                return true;
            }
            return false;
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

    private class StatusCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            StudySubjectBean studySubject = (StudySubjectBean) new BasicCellEditor().getValue(item, "studySubject", rowcount);
            return studySubject.getStatus().getName();
        }
    }

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : Status.toActiveArrayList()) {
                ((Status) status).getName();
                options.add(new Option(String.valueOf(((Status) status).getId()), ((Status) status).getName()));
            }
            return options;
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

    private class StudyEventDefinitionMapCellEditor implements CellEditor {

        StudyEventDefinitionBean studyEventDefinition;
        SubjectEventStatus subjectEventStatus;
        List<StudyEventBean> studyEvents;
        HashMap<ResolutionStatus, Integer> discCounts;

        private String getCount() {
            return studyEvents.size() < 2 ? "" : "&nbsp;x" + String.valueOf(studyEvents.size() + "");
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            studyEvents = (List<StudyEventBean>) ((HashMap<Object, Object>) item).get(property + "_studyEvents");
            studyEventDefinition = (StudyEventDefinitionBean) ((HashMap<Object, Object>) item).get(property + "_object");
            subjectEventStatus = SubjectEventStatus.get((Integer) ((HashMap<Object, Object>) item).get(property));
            discCounts = (HashMap<ResolutionStatus, Integer>) ((HashMap<Object, Object>) item).get(property + "_discCounts");

            StringBuilder url = new StringBuilder();
            url.append("<table><tr><td><img src='" + imageIconPaths.get(subjectEventStatus.getId()) + "' border='0'>");
            url.append(getCount() + "</td></tr>");
            url.append("<tr>");
            for (ResolutionStatus key : discCounts.keySet()) {
                url.append("<td>");
                url.append(discNoteIconPaths.get(key.getId()) + "(" + discCounts.get(key) + ")");
                url.append("</td>");
            }
            if (discCounts.keySet().size() <= 1) {
                url.append("<td>");
                url.append("<img border=\"0\" src=\"images/icon_transparent.gif\"/>&nbsp;&nbsp;");
                url.append("</td>");
                url.append("<td>");
                url.append("<img border=\"0\" src=\"images/icon_transparent.gif\"/>&nbsp;&nbsp;");
                if (studyEventDefinition.getName().length() > 9) {
                    int totNumOfSpaces = studyEventDefinition.getName().length() - 9;
                    for (int i = 0; i < totNumOfSpaces; i++) {
                        url.append("&nbsp;");
                    }
                }
                url.append("</td>");
            }
            url.append("</tr></table>");

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
                url.append(viewNotesLinkBuilder(studySubjectBean));
                url.append(downloadNotesLinkBuilder(studySubjectBean));
                value = url.toString();
            }

            return value;
        }
    }

    private String viewNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&id=" + studySubject.getId() + "&resolutionStatus=" + resolutionStatus + "discNoteType=" + discNoteType + "&module="
                        + module + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).append(
                    "hspace=\"4\" style=\"float:left\" width=\"24 \" height=\"15\" align=\"left\"").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
        } else {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&id=" + studySubject.getId() + "&module=" + module + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).append(
                    "hspace=\"2\" style=\"float:left\" width=\"24 \" height=\"15\" align=\"left\"").end().aEnd();
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
                            + "&resolutionStatus=" + resolutionStatus + "')").close();
                actionLink.img().name("bt_Download").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"4\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            } else {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType + "&module="
                            + module + "')").close();
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"2\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            }
        }
        return actionLink.toString();
    }

    public Set<Integer> getResolutionStatusIds() {
        return resolutionStatusIds;
    }

    public void setResolutionStatusIds(Set<Integer> resolutionStatusIds) {
        this.resolutionStatusIds = resolutionStatusIds;
    }
    
    public HashMap<ResolutionStatus, Integer> countAll(HashMap<ResolutionStatus, Integer> discCounts, StudyEventBean studyEvent, StringBuffer constraints, boolean isSite) {
        HashMap<ResolutionStatus, Integer> temp = new HashMap<ResolutionStatus, Integer>();
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("itemData", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("subject", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("eventCrf", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("StudySub", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("studyEvent", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        
        return discCounts;
    }
    
    public HashMap<ResolutionStatus, Integer> getTotal(HashMap<ResolutionStatus, Integer> discCounts, HashMap<ResolutionStatus, Integer> discCountsTemp) {
        if(discCountsTemp.size()>0) {
            for(int i=1; i<6; ++i) {
                Integer c = 0;
                if(discCounts.get(ResolutionStatus.get(i))!=null) {
                    c = discCounts.get(ResolutionStatus.get(i));
                }
                if(discCountsTemp.get(ResolutionStatus.get(i))!=null) {
                    discCounts.put(ResolutionStatus.get(i), c+discCountsTemp.get(ResolutionStatus.get(i)));
                }
            }
        }
        return discCounts;
    }

}
