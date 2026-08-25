/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.managestudy.ListEventsForSubjectRow.CrfOccurrenceData;
import org.akaza.openclinica.control.managestudy.ListEventsForSubjectRow.GroupAssignment;
import org.akaza.openclinica.control.managestudy.ListEventsForSubjectRow.OccurrenceData;
import org.akaza.openclinica.control.popup.CrfStatusPopup;
import org.akaza.openclinica.control.popup.EventStatusPopup;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListEventsForSubjectFilter;
import org.akaza.openclinica.dao.managestudy.ListEventsForSubjectSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.*;
import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.Td;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.SafeUrl.url;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * LCTable-based rendering of the "listEventsForSubject" table (event-definition-specific drill-down from
 * the "findSubjects" Subject Matrix grid, reached via {@code ListEventsForSubjects?defId=...}).
 *
 * <p>Unlike {@code ListStudySubjectTable} ("findSubjects"), this table is scoped to a single, already-selected
 * {@link StudyEventDefinitionBean}: instead of one aggregate cell per event-definition column, it renders one
 * icon/popup <em>per occurrence</em> of that single definition, stacked vertically inside the Event Status,
 * Event Date, and each dynamic CRF column, mirroring the legacy {@code ListEventsForSubjectTableFactory}.
 *
 * <p>Kept unfixed on purpose, for behavioural parity with the legacy jmesa table (see the migration notes):
 * <ul>
 * <li>the "Event Date" column is {@code NOT_SORTABLE} and has {@code NO_FILTER} (the legacy sort/filter for
 * this column were already broken -- they sort/filter by subject creation date, not the displayed event date);</li>
 * <li>the raw-string SQL filter/sort building in {@link ListEventsForSubjectFilter}/{@link ListEventsForSubjectSort}
 * is reused as-is;</li>
 * <li>the N+1 per-subject/per-occurrence ancillary lookups in {@link #buildRow} are reused as-is.</li>
 * </ul>
 * Improved/changed on purpose (explicit product decisions for this migration):
 * <ul>
 * <li>deterministic default sort: by subject label (study subject ID) ascending when no sort is requested;</li>
 * <li>within each subject's row, occurrences of the selected event definition are always ordered by event date
 * ascending (the legacy table's occurrence order was effectively random).</li>
 * </ul>
 */
public class ListEventsForSubjectTable {

    private final StudySubjectDAO studySubjectDAO;
    private final SubjectDAO subjectDAO;
    private final StudyEventDAO studyEventDAO;
    private final StudyGroupClassDAO studyGroupClassDAO;
    private final SubjectGroupMapDAO subjectGroupMapDAO;
    private final StudyGroupDAO studyGroupDAO;
    private final StudyDAO studyDAO;
    private final EventCRFDAO eventCRFDAO;
    private final EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private final CRFDAO crfDAO;
    private final CRFVersionDAO crfVersionDAO;

    private final StudyBean studyBean;
    private final StudyUserRoleBean currentRole;
    private final UserAccountBean currentUser;
    private final StudyEventDefinitionBean selectedStudyEventDefinition;
    private final String contextPath;   // needed for building REST "print" links (see printExistingCrfHref/printBlankCrfHref)

    private final ResourceBundle resword;
    private final ResourceBundle resformat;
    private final ResourceBundle resnotes;

    private final List<StudyGroupClassBean> studyGroupClasses;
    private final List<StudyEventDefinitionBean> studyEventDefinitions;   // all event definitions of the study (for the toolbar dropdown)
    private final List<CRFBean> crfs;                                    // active top-level CRFs of the selected event definition

    private final LCPopup<EventStatusPopup.Context, EventStatusPopup.EventOccurrence> eventPopup;
    private final LCPopup<CrfStatusPopup.Context, CrfOccurrenceData> crfPopup;

    private final LCTable<ListEventsForSubjectRow> table;

    public ListEventsForSubjectTable(StudySubjectDAO studySubjectDAO, SubjectDAO subjectDAO, StudyEventDAO studyEventDAO,
            StudyEventDefinitionDAO studyEventDefinitionDAO, StudyGroupClassDAO studyGroupClassDAO, SubjectGroupMapDAO subjectGroupMapDAO,
            StudyGroupDAO studyGroupDAO, StudyDAO studyDAO, EventCRFDAO eventCRFDAO, EventDefinitionCRFDAO eventDefinitionCRFDAO, CRFDAO crfDAO,
            CRFVersionDAO crfVersionDAO, StudyBean studyBean, StudyUserRoleBean currentRole, UserAccountBean currentUser,
            StudyEventDefinitionBean selectedStudyEventDefinition, Locale locale, String contextPath) {
        this.studySubjectDAO = studySubjectDAO;
        this.subjectDAO = subjectDAO;
        this.studyEventDAO = studyEventDAO;
        this.studyGroupClassDAO = studyGroupClassDAO;
        this.subjectGroupMapDAO = subjectGroupMapDAO;
        this.studyGroupDAO = studyGroupDAO;
        this.studyDAO = studyDAO;
        this.eventCRFDAO = eventCRFDAO;
        this.eventDefinitionCRFDAO = eventDefinitionCRFDAO;
        this.crfDAO = crfDAO;
        this.crfVersionDAO = crfVersionDAO;
        this.studyBean = studyBean;
        this.currentRole = currentRole;
        this.currentUser = currentUser;
        this.selectedStudyEventDefinition = selectedStudyEventDefinition;
        this.contextPath = contextPath;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.resformat = ResourceBundleProvider.getFormatBundle(locale);
        this.resnotes = ResourceBundleProvider.getTextsBundle(locale);

        // sites inherit their parent study's group-classes/event-definitions, exactly as the legacy factory does
        int parentStudyId = studyBean.getParentStudyId();
        StudyBean groupClassAndEventDefSourceStudy = parentStudyId > 0 ? studyDAO.findByPK(parentStudyId) : studyBean;
        this.studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(groupClassAndEventDefSourceStudy);
        this.studyEventDefinitions = studyEventDefinitionDAO.findAllByStudy(groupClassAndEventDefSourceStudy);
        this.crfs = loadCrfs(selectedStudyEventDefinition);

        this.eventPopup = EventStatusPopup.perOccurrence(selectedStudyEventDefinition, studyBean, currentRole, currentUser, resword, resformat);
        this.crfPopup = CrfStatusPopup.perOccurrence(selectedStudyEventDefinition, studyBean, currentRole, currentUser, crfVersionDAO, resword, resformat, contextPath);

        this.table = new LCTable<>("listEventsForSubject", buildColumns(), this::fetchData, List.of("defId"))
            .setRowTestAttributes(row -> Map.of("subject", row.studySubject.getLabel()));
        this.table.addCustomToolbarControl(this::renderSelectEventControl);
        if (isAddSubjectLinkShown()) {
            this.table.addCustomToolbarControl(this::renderAddNewSubjectControl);
        }
    }

    private boolean isAddSubjectLinkShown() {
        return studyBean.getStatus().isAvailable() && currentRole.getRole() != Role.MONITOR;
    }

    /** Active, top-level (non-child) CRFs attached to {@code eventDefinition}, in the same order as the legacy factory's {@code getCrfs(...)}. */
    private List<CRFBean> loadCrfs(StudyEventDefinitionBean eventDefinition) {
        List<CRFBean> result = new ArrayList<>();
        for (EventDefinitionCRFBean edc : eventDefinitionCRFDAO.findAllActiveByEventDefinitionId(eventDefinition.getId())) {
            if (edc.getParentId() == 0) {
                CRFBean crf = crfDAO.findByPK(edc.getCrfId());
                crf.setVersions(crfVersionDAO.findAllByCRFId(edc.getCrfId()));
                result.add(crf);
            }
        }
        return result;
    }

    // -- Element ID helpers (uses database PKs for stable IDs) -------------------------------------

    private static String actionId(String action, int studySubjectId) {
        return "listEventsForSubject-" + action + "-" + studySubjectId;
    }

    // -- Toolbar controls --------------------------------------------------------------------------

    /** Toolbar dropdown reciprocal to {@code ListStudySubjectTable}'s "Select an Event": navigates among event definitions, or back to "findSubjects". */
    private void renderSelectEventControl(Div<?> container, LCTableContext<ListEventsForSubjectRow> ctx) {
        container.div().attrClass("dropdown-list")
            .of(div -> {
                div.label().text(resword.getString("events") + ": ").__();
                div.select()
                    .addAttr("data-testid", "select-event-definition")
                    .addAttr("onchange",
                        "var v=this.value; if (v == '0') { window.location='" + ctx.resourcePath + "/ListStudySubjects'; } "
                            + "else if (v) { window.location='" + ctx.resourcePath + "/ListEventsForSubjects?module=submit&defId=' + v; }")
                    .of(select -> {
                        select.option().attrValue("0").text(resnotes.getString("all_events")).__();
                        for (StudyEventDefinitionBean sed : studyEventDefinitions) {
                            boolean isSelected = sed.getId() == selectedStudyEventDefinition.getId();
                            select.option().attrValue(String.valueOf(sed.getId())).attrSelected(isSelected).text(sed.getName()).__();
                        }
                    })
                    .__();
            })
            .__();
    }

    /** Toolbar button opening the "Add New Subject" modal (JSP-side BlockUI overlay; unrelated to LCTable/HTMX). */
    private void renderAddNewSubjectControl(Div<?> container, LCTableContext<ListEventsForSubjectRow> ctx) {
        container.a().attrClass("text-btn").attrHref("javascript:;").attrId("addSubject")
            .addAttr("data-testid", "add-subject-button")
            .text(resword.getString("add_new_subject"))
            .__();
    }

    // -- Column definitions --------------------------------------------------------------------------

    private List<LCTableColumnDef<ListEventsForSubjectRow>> buildColumns() {
        List<LCTableColumnDef<ListEventsForSubjectRow>> columns = new ArrayList<>();

        columns.add(textCol("studySubject.label", resword.getString("study_subject_ID"), 0, row -> row.studySubject.getLabel()));
        columns.add(enumColHidden("studySubject.status", resword.getString("subject_status"), 0,
            row -> row.studySubject.getStatus(), Status.toDropDownArrayList(), Status::getName, Status::getName
        ));
        columns.add(textColHidden("enrolledAt", resword.getString("site_id"), 0, row -> row.enrolledAt));
        columns.add(textColHidden("subject.charGender", resword.getString("gender"), 0,row -> String.valueOf(row.subject.getGender())));

        // one hidden column per active study-group-class (legacy default: hide Subject Status, Site, Gender and all group-class columns)
        for (StudyGroupClassBean sgc : studyGroupClasses) {
            List<StudyGroupBean> groupOptions = studyGroupDAO.findAllByGroupClass(sgc);
            columns.add(enumColHiddenNotSortable("sgc_" + sgc.getId(), sgc.getName(), 0, groupOptions, StudyGroupBean::getName, StudyGroupBean::getName,
                row -> {
                    GroupAssignment ga = row.groupAssignmentsByClassId.get(sgc.getId());
                    return ga == null ? "" : ga.groupName;
                }
            ));
        }

        columns.add(customTdCol("event.status", resword.getString("event_status"), 0, NOT_SORTABLE,
            new LCTableFilterDef.Select<>(SubjectEventStatus.toArrayList(), SubjectEventStatus::getName, SubjectEventStatus::getName),
            LCTablePopupColumn.perOccurrence(eventPopup, this::toEventStatusPopupContexts)
        ));

        // "Event Date": NOT_SORTABLE / NO_FILTER on purpose -- the legacy sort/filter for this column were already
        // broken (they act on the subject's creation date, not the displayed event date); see class javadoc.
        columns.add(customTdCol("studySubject.createdDate", resword.getString("event_date"), 0, NOT_SORTABLE, NO_FILTER,
            this::renderEventDateCell
        ));

        // one column per active top-level CRF of the selected event definition
        for (CRFBean crf : crfs) {
            LCTableColumnDef<ListEventsForSubjectRow> crfColumn = LCTableColumnDef.<ListEventsForSubjectRow>customTdCol(
                "crf_" + crf.getId(), crf.getName(), 0, NOT_SORTABLE,
                new LCTableFilterDef.Select<>(DataEntryStage.toArrayList(), DataEntryStage::getName, DataEntryStage::getName),
                (td, row) -> renderCrfCell(td, row, crf)
            );
            crfColumn.setTestAttributes(row -> Map.of("crf", crf.getName()));
            columns.add(crfColumn);
        }

        columns.add(customTdCol("actions", resword.getString("rule_actions"), 0, NOT_SORTABLE, LCTableFilterDef.clearFilter(), this::renderActionsCell));

        return columns;
    }

    // -- Event-status column: one status icon + popup per occurrence, via EventStatusPopup#perOccurrence ---------

    /** Builds one {@link EventStatusPopup.Context} (singleton items list) per occurrence, for {@code LCTablePopupColumn.perOccurrence}. */
    private List<EventStatusPopup.Context> toEventStatusPopupContexts(ListEventsForSubjectRow row) {
        List<EventStatusPopup.Context> contexts = new ArrayList<>();
        for (OccurrenceData occ : row.occurrences) {
            EventStatusPopup.EventOccurrence item = occ.studyEvent == null
                ? EventStatusPopup.EventOccurrence.notScheduled(occ.eventStatus)
                : EventStatusPopup.EventOccurrence.of(occ.studyEvent);
            contexts.add(new EventStatusPopup.Context(row.studySubject, List.of(item)));
        }
        return contexts;
    }

    // -- Event-date column: one date line per occurrence, aligned with the event-status/CRF columns -----

    private void renderEventDateCell(Td<?> td, ListEventsForSubjectRow row) {
        for (OccurrenceData occ : row.occurrences) {
            td.div().attrClass("event-occurrence-date-line").text(formatDate(occ.eventDate)).__();
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(resformat.getString("date_format_string")).format(date);
    }

    // -- CRF columns: one status icon + popup per occurrence, via CrfStatusPopup#perOccurrence -------------

    private void renderCrfCell(Td<?> td, ListEventsForSubjectRow row, CRFBean crf) {
        List<CrfStatusPopup.Context> contexts = new ArrayList<>();
        for (OccurrenceData occ : row.occurrences) {
            CrfOccurrenceData data = occ.crfByCrfId.get(crf.getId());
            if (data == null) {
                td.div().__();
                continue;
            }
            contexts.add(new CrfStatusPopup.Context(row.studySubject, occ.studyEvent, data));
        }
        contexts.forEach(ctx -> crfPopup.render(td, ctx));
    }

    // -- Actions column: view/remove/restore/reassign, ported from the legacy ActionsCellEditor -------------

    private void renderActionsCell(Td<?> td, ListEventsForSubjectRow row) {
        StudySubjectBean studySubject = row.studySubject;
        if (studySubject.getId() == 0) {
            return;
        }
        td.of(actionLink(actionId("view", studySubject.getId()), resword.getString("view"), url("ViewStudySubject").param("id", studySubject.getId()),
            "bt_View.gif", "view"));

        if (currentRole.getRole() == Role.MONITOR) {
            return;
        }

        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;
        boolean subjectDeleted = studySubject.getStatus() == Status.DELETED;

        if (studyAvailable && !subjectDeleted) {
            td.of(actionLink(actionId("remove", studySubject.getId()), resword.getString("remove"),
                url("RemoveStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId())
                    .param("studyId", studySubject.getStudyId()),
                "bt_Remove.gif", "remove"));
        }
        if (studyAvailable && subjectDeleted) {
            td.of(actionLink(actionId("restore", studySubject.getId()), resword.getString("restore"),
                url("RestoreStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId())
                    .param("studyId", studySubject.getStudyId()),
                "bt_Restore.gif", "restore"));
        }
        if (studyAvailable && studySubject.getStatus() == Status.AVAILABLE
            && currentRole.getRole() != Role.INVESTIGATOR && currentRole.getRole() != Role.RESEARCHASSISTANT
            && currentRole.getRole() != Role.RESEARCHASSISTANT2) {
            td.of(actionLink(actionId("reassign", studySubject.getId()), resword.getString("reassign"), url("ReassignStudySubject").param("id",
                studySubject.getId()), "bt_Reassign.gif", "reassign"));
        }
    }

    // -- Data fetching -----------------------------------------------------------------------------

    private LCTableData<ListEventsForSubjectRow> fetchData(LCTableParams p) {
        ListEventsForSubjectFilter filter = buildFilter(p);
        ListEventsForSubjectSort sort = buildSort(p);

        int rowStart = p.page * p.maxRows;
        int rowEnd = rowStart + p.maxRows;
        List<StudySubjectBean> studySubjects = studySubjectDAO.getWithFilterAndSort(studyBean, filter, sort, rowStart, rowEnd);
        int total = studySubjectDAO.getCountWithFilter(filter, studyBean);

        List<ListEventsForSubjectRow> rows = new ArrayList<>();
        for (StudySubjectBean studySubject : studySubjects) {
            rows.add(buildRow(studySubject));
        }
        return new LCTableData<>(rows, total);
    }

    private ListEventsForSubjectFilter buildFilter(LCTableParams p) {
        ListEventsForSubjectFilter filter = new ListEventsForSubjectFilter(selectedStudyEventDefinition.getId());
        p.filters.forEach((property, value) -> {
            if ("studySubject.status".equals(property)) {
                filter.addFilter(property, Status.getByName(value).getId() + "");
            } else if ("event.status".equals(property)) {
                filter.addFilter(property, SubjectEventStatus.getByName(value).getId() + "");
            } else if (property.startsWith("sgc_")) {
                int studyGroupClassId = Integer.parseInt(property.substring(4));
                StudyGroupBean group = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId);
                filter.addFilter(property, group.getId() + "");
            } else if (property.startsWith("crf_")) {
                filter.addFilter(property, DataEntryStage.getByName(value).getId() + "");
            } else {
                // free-text columns: studySubject.label, enrolledAt, subject.charGender
                filter.addFilter(property, value);
            }
        });
        return filter;
    }

    private ListEventsForSubjectSort buildSort(LCTableParams p) {
        ListEventsForSubjectSort sort = new ListEventsForSubjectSort();
        if (p.sortProp != null && !p.sortProp.isEmpty()) {
            sort.addSort(p.sortProp, p.sortDir == null ? "asc" : p.sortDir);
        } else {
            // deterministic default sort (explicit product decision): the legacy DAO overload has no default
            // ORDER BY at all when no sort is requested
            sort.addSort("studySubject.label", "asc");
        }
        return sort;
    }

    private ListEventsForSubjectRow buildRow(StudySubjectBean studySubject) {
        String enrolledAt = studyDAO.findByPK(studySubject.getStudyId()).getIdentifier();
        SubjectBean subject = subjectDAO.findByPK(studySubject.getSubjectId());

        Map<Integer, GroupAssignment> groupAssignmentsByClassId = new HashMap<>();
        for (StudyGroupClassBean sgc : studyGroupClasses) {
            SubjectGroupMapBean sgm = subjectGroupMapDAO.findByStudySubjectAndStudyGroupClass(studySubject.getId(), sgc.getId());
            if (sgm != null) {
                groupAssignmentsByClassId.put(sgc.getId(), new GroupAssignment(sgm.getStudyGroupId(), sgm.getStudyGroupName()));
            }
        }

        List<EventCRFBean> eventCrfs = eventCRFDAO.findAllByStudySubject(studySubject.getId());
        Map<String, EventCRFBean> eventCrfByKey = new HashMap<>();
        for (EventCRFBean ec : eventCrfs) {
            CRFBean crf = crfDAO.findByVersionId(ec.getCRFVersionId());
            eventCrfByKey.put(crf.getId() + "_" + ec.getStudyEventId(), ec);
        }

        List<StudyEventBean> studyEvents = new ArrayList<>(studyEventDAO.findAllByDefinitionAndSubject(selectedStudyEventDefinition, studySubject));
        // explicit product decision: order occurrences by event date ascending (the legacy table's order was effectively random)
        studyEvents.sort(Comparator.comparing(StudyEventBean::getDateStarted, Comparator.nullsLast(Comparator.naturalOrder())));

        List<OccurrenceData> occurrences = new ArrayList<>();
        if (studyEvents.isEmpty()) {
            occurrences.add(buildOccurrence(null, studySubject, eventCrfByKey));
        } else {
            for (StudyEventBean studyEvent : studyEvents) {
                occurrences.add(buildOccurrence(studyEvent, studySubject, eventCrfByKey));
            }
        }

        return new ListEventsForSubjectRow(studySubject, subject, enrolledAt, groupAssignmentsByClassId, occurrences);
    }

    private OccurrenceData buildOccurrence(StudyEventBean studyEvent, StudySubjectBean studySubject, Map<String, EventCRFBean> eventCrfByKey) {
        SubjectEventStatus status = studyEvent == null ? SubjectEventStatus.NOT_SCHEDULED : studyEvent.getSubjectEventStatus();
        java.util.Date eventDate = studyEvent == null ? null : studyEvent.getDateStarted();

        Map<Integer, CrfOccurrenceData> crfByCrfId = new LinkedHashMap<>();
        for (CRFBean crf : crfs) {
            EventCRFBean eventCrf = studyEvent == null ? null : eventCrfByKey.get(crf.getId() + "_" + studyEvent.getId());
            DataEntryStage stage = eventCrf != null ? eventCrf.getStage() : DataEntryStage.UNCOMPLETED;
            EventDefinitionCRFBean edc = getEventDefinitionCRFBean(crf, studySubject);
            crfByCrfId.put(crf.getId(), new CrfOccurrenceData(crf, edc, eventCrf, stage));
        }
        return new OccurrenceData(studyEvent, status, eventDate, crfByCrfId);
    }

    private EventDefinitionCRFBean getEventDefinitionCRFBean(CRFBean crf, StudySubjectBean studySubject) {
        EventDefinitionCRFBean edc = eventDefinitionCRFDAO.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
            selectedStudyEventDefinition.getId(), crf.getId(), studySubject.getStudyId());
        if (edc.getId() == 0) {
            edc = eventDefinitionCRFDAO.findForStudyByStudyEventDefinitionIdAndCRFId(selectedStudyEventDefinition.getId(), crf.getId());
        }
        CRFVersionBean defaultVersion = crfVersionDAO.findByPK(edc.getDefaultVersionId());
        edc.setDefaultCRF(defaultVersion);
        return edc;
    }

    // -- Rendering entry point ---------------------------------------------------------------------

    public String render(HttpServletRequest request) {
        final LCTableParams params = new LCTableParams(request.getQueryString(), this.table);
        return this.table.render(request.getRequestURI(), params, request.getContextPath());
    }

}

