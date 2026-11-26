/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.popup.EventStatusPopup;
import org.akaza.openclinica.control.submit.FindSubjectsRow.EventColumnData;
import org.akaza.openclinica.control.submit.FindSubjectsRow.GroupAssignment;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.FindSubjectsFilter;
import org.akaza.openclinica.dao.managestudy.FindSubjectsSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.*;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.Td;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.SafeUrl.url;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.LCTableText.literal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * LCTable-based rendering of the "findSubjects" table (Subject Matrix grid).
 * Dynamically builds columns for study groups and event definitions, handles subject status,
 * and renders event occurrence details in popup menus.
 */
public class ListStudySubjectTable {

    private final StudySubjectDAO studySubjectDAO;
    private final SubjectDAO subjectDAO;
    private final StudyEventDAO studyEventDAO;
    private final StudyGroupClassDAO studyGroupClassDAO;
    private final SubjectGroupMapDAO subjectGroupMapDAO;
    private final StudyGroupDAO studyGroupDAO;
    private final StudyDAO studyDAO;
    private final EventCRFDAO eventCRFDAO;
    private final EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private final StudyParameterValueDAO studyParameterValueDAO;

    private final StudyBean studyBean;
    private final StudyUserRoleBean currentRole;
    private final UserAccountBean currentUser;
    private final HttpSession session;         // for participant portal status caching

    private final ResourceBundle resword;
    private final ResourceBundle resformat;    // for date formatting in event popups

    private final List<StudyGroupClassBean> studyGroupClasses;
    private final List<StudyEventDefinitionBean> studyEventDefinitions;

    private final LCTable<FindSubjectsRow> table;

    public ListStudySubjectTable(StudySubjectDAO studySubjectDAO, SubjectDAO subjectDAO, StudyEventDAO studyEventDAO,
            StudyEventDefinitionDAO studyEventDefinitionDAO, StudyGroupClassDAO studyGroupClassDAO, SubjectGroupMapDAO subjectGroupMapDAO,
            StudyGroupDAO studyGroupDAO, StudyDAO studyDAO, EventCRFDAO eventCRFDAO, EventDefinitionCRFDAO eventDefinitionCRFDAO,
            StudyParameterValueDAO studyParameterValueDAO, StudyBean studyBean, StudyUserRoleBean currentRole, UserAccountBean currentUser,
            Locale locale, HttpSession session) {
        this.studySubjectDAO = studySubjectDAO;
        this.subjectDAO = subjectDAO;
        this.studyEventDAO = studyEventDAO;
        this.studyGroupClassDAO = studyGroupClassDAO;
        this.subjectGroupMapDAO = subjectGroupMapDAO;
        this.studyGroupDAO = studyGroupDAO;
        this.studyDAO = studyDAO;
        this.eventCRFDAO = eventCRFDAO;
        this.eventDefinitionCRFDAO = eventDefinitionCRFDAO;
        this.studyParameterValueDAO = studyParameterValueDAO;
        this.studyBean = studyBean;
        this.currentRole = currentRole;
        this.currentUser = currentUser;
        this.session = session;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.resformat = ResourceBundleProvider.getFormatBundle(locale);

        // sites inherit their parent study's group-classes/event-definitions
        int parentStudyId = studyBean.getParentStudyId();
        if (parentStudyId > 0) {
            StudyBean parentStudy = studyDAO.findByPK(parentStudyId);
            this.studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(parentStudy);
            this.studyEventDefinitions = studyEventDefinitionDAO.findAllActiveByParentStudyId(parentStudy.getId());
        } else {
            this.studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(studyBean);
            this.studyEventDefinitions = studyEventDefinitionDAO.findAllActiveByParentStudyId(studyBean.getId());
        }

        this.table = new LCTable<>("findSubjects", buildColumns(), this::fetchData)
            .setRowTestAttributes(row -> Map.of("subject", row.studySubject.getLabel()));
        this.table.addCustomToolbarControl(this::renderSelectEventControl);
        if (isAddSubjectLinkShown()) {
            this.table.addCustomToolbarControl(this::renderAddNewSubjectControl);
        }
    }

    private boolean isAddSubjectLinkShown() {
        return studyBean.getStatus() == Status.AVAILABLE && currentRole.getRole() != Role.MONITOR;
    }

    // -- Element ID helpers (uses database PKs for stable IDs) -------------------------------------

    /** Id for a row-level action link in the "Actions" column (view/remove/restore/reassign/sign/...). */
    private static String actionId(String action, int studySubjectId) {
        return "findSubjects-" + action + "-" + studySubjectId;
    }

    /** Toolbar dropdown to navigate to {@code ListEventsForSubjects} for a selected event definition. */
    private void renderSelectEventControl(Div<?> container, LCTableContext<FindSubjectsRow> ctx) {
        container.div().attrClass("dropdown-list")
            .of(div -> {
                div.label().text("").__();
                div.select()
                    .addAttr("data-testid", "select-event-definition")
                    .addAttr("onchange",
                        "var v=this.value; if (v) { window.location='" + ctx.resourcePath
                            + "/ListEventsForSubjects?module=submit&defId=' + v; }")
                    .of(select -> {
                        select.option().attrValue("").text(resword.getString("select_an_event")).__();
                        for (StudyEventDefinitionBean sed : studyEventDefinitions) {
                            select.option().attrValue(String.valueOf(sed.getId())).text(sed.getName()).__();
                        }
                    })
                    .__();
            })
            .__();
    }

    /** Toolbar button opening the "Add New Subject" modal. */
    private void renderAddNewSubjectControl(Div<?> container, LCTableContext<FindSubjectsRow> ctx) {
        container.a().attrClass("text-btn").attrHref("javascript:;").attrId("addSubject")
            .addAttr("data-testid", "add-subject-button")
            .text(resword.getString("add_new_subject"))
            .__();
    }

    // -- Column definitions ----------------------------------------------------------------------

    private List<LCTableColumnDef<FindSubjectsRow>> buildColumns() {
        List<LCTableColumnDef<FindSubjectsRow>> columns = new ArrayList<>();

        columns.add(textCol      ("studySubject.label",          key("study_subject_ID"),  "study-subject-id", 0, row -> row.studySubject.getLabel()));
        columns.add(enumColHidden("studySubject.status",         key("subject_status"),    "subject-status", 0,
            row -> row.studySubject.getStatus(), Status.toDropDownArrayList(), Status::getName, Status::getName
        ));
        columns.add(textColHidden("enrolledAt",                  key("site_id"),           "site-id", 0, row -> row.enrolledAt));
        columns.add(textColHidden("studySubject.oid",            key("rule_oid"),          "oid", 0, row -> row.studySubject.getOid()));
        columns.add(textColHidden("subject.charGender",          key("gender"),            "sex",0, row -> String.valueOf(row.subject.getGender())));
        columns.add(textColHidden("studySubject.secondaryLabel", key("secondary_ID"),      "secondary-id", 0, row -> row.studySubject.getSecondaryLabel()));
        columns.add(textColHidden("subject.uniqueIdentifier",    key("subject_unique_ID"), "person-id", 0, row -> row.subject.getUniqueIdentifier()));

        // one column per active study-group-class
        for (StudyGroupClassBean sgc : studyGroupClasses) {
            List<StudyGroupBean> groupOptions = studyGroupDAO.findAllByGroupClass(sgc);
            columns.add(enumColNotSortable("sgc_" + sgc.getId(), literal(sgc.getName()), null, 0,
                groupOptions, StudyGroupBean::getName, StudyGroupBean::getName,
                row -> {
                    GroupAssignment ga = row.groupAssignmentsByClassId.get(sgc.getId());
                    return ga == null ? "" : ga.groupName;
                }
            ));
        }

        // one column per active study-event-definition -- LCPopup-based aggregate event-status popup (see
        // EventStatusPopup#aggregate), one instance per column (captures 'sed' in its closures)
        for (StudyEventDefinitionBean sed : studyEventDefinitions) {
            LCPopup<EventStatusPopup.Context, EventStatusPopup.EventOccurrence> eventPopup =
                EventStatusPopup.aggregate(sed, studyBean, currentRole, currentUser, resword, resformat);
            LCTableColumnDef<FindSubjectsRow> eventColumn = LCTableColumnDef.<FindSubjectsRow>customTdCol(
                "sed_" + sed.getId(), literal(sed.getName()), null, 5, NOT_SORTABLE,
                // SubjectEventStatus.getName() (Term.getName()) already resolves the translated display name via
                // the terms resource bundle -- do NOT re-translate it here (that would look up the translated text
                // itself as if it were a resource key, throwing MissingResourceException).
                new LCTableFilterDef.Select<>(SubjectEventStatus.toArrayList(), SubjectEventStatus::getName, SubjectEventStatus::getName),
                LCTablePopupColumn.single(eventPopup, row -> toEventStatusPopupContext(row, sed))
            );
            eventColumn.setTestAttributes(row -> Map.of("event", sed.getName()));
            columns.add(eventColumn);
        }

        columns.add(customTdCol("actions", key("rule_actions"), "actions", 0, NOT_SORTABLE, LCTableFilterDef.clearFilter(), this::renderActionsCell));

        return columns;
    }

    /** Builds the {@link EventStatusPopup.Context} for one (row, event-definition-column) pair. */
    private EventStatusPopup.Context toEventStatusPopupContext(FindSubjectsRow row, StudyEventDefinitionBean sed) {
        EventColumnData data = row.eventDataByDefinitionId.get(sed.getId());
        List<StudyEventBean> occurrences = data == null || data.occurrences == null ? List.of() : data.occurrences;
        List<EventStatusPopup.EventOccurrence> items = occurrences.isEmpty()
            ? List.of(EventStatusPopup.EventOccurrence.notScheduled(data == null ? SubjectEventStatus.NOT_SCHEDULED : data.aggregateStatus))
            : occurrences.stream().map(EventStatusPopup.EventOccurrence::of).collect(Collectors.toList());
        return new EventStatusPopup.Context(row.studySubject, items);
    }

    private void renderActionsCell(Td<?> td, FindSubjectsRow row) {
        StudySubjectBean studySubject = row.studySubject;
        if (studySubject.getId() == 0) {
            return;
        }
        td.of(actionLink(actionId("view", studySubject.getId()), resword.getString("view"), url("ViewStudySubject").param("id", studySubject.getId()), "bt_View.gif", "view"));

        if (currentRole.getRole() == Role.MONITOR) {
            return;
        }

        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;
        boolean subjectDeleted = studySubject.getStatus() == Status.DELETED || studySubject.getStatus() == Status.AUTO_DELETED;

        if (studyAvailable && !subjectDeleted && currentRole.getRole() != Role.RESEARCHASSISTANT && currentRole.getRole() != Role.RESEARCHASSISTANT2) {
            td.of(actionLink(actionId("remove", studySubject.getId()), resword.getString("remove"),
                url("RemoveStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId()).param("studyId", studySubject.getStudyId()),
                "bt_Remove.gif", "remove"));
        }
        if (studyAvailable && subjectDeleted) {
            td.of(actionLink(actionId("restore", studySubject.getId()), resword.getString("restore"),
                url("RestoreStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId()).param("studyId", studySubject.getStudyId()),
                "bt_Restore.gif", "restore"));
        }
        if (studyAvailable && currentRole.getRole() != Role.RESEARCHASSISTANT && currentRole.getRole() != Role.RESEARCHASSISTANT2
            && currentRole.getRole() != Role.INVESTIGATOR && studySubject.getStatus() == Status.AVAILABLE) {
            td.of(actionLink(actionId("reassign", studySubject.getId()), resword.getString("reassign"), url("ReassignStudySubject").param("id", studySubject.getId()), "bt_Reassign.gif", "reassign"));
        }
        if (currentRole.getRole() == Role.INVESTIGATOR && studyAvailable && studySubject.getStatus() != Status.DELETED && row.isSignable) {
            td.of(actionLink(actionId("sign", studySubject.getId()), resword.getString("sign"), url("SignStudySubject").param("id", studySubject.getId()), "icon_Signed.gif", "sign"));
        }
        try {
            if (studyAvailable && (currentRole.getRole() == Role.RESEARCHASSISTANT || currentRole.getRole() == Role.RESEARCHASSISTANT2)
                && studySubject.getStatus() == Status.AVAILABLE && "ACTIVE".equalsIgnoreCase(pManageStatus(studySubject))
                && "enabled".equalsIgnoreCase(participateStatus(studySubject))) {
                td.of(actionLink(actionId("connect-participant", studySubject.getId()), resword.getString("connect_participant"), participateUrl(studySubject), "bt_Ocui.gif", "connect-participant"));
            }
        } catch (Exception e) {
            // matches legacy behaviour: silently omit the "connect participant" link on any failure
        }
    }

    // -- Participant-portal helpers (ported as-is from ListStudySubjectTableFactory) ------------------

    private String participateStatus(StudySubjectBean studySubject) {
        StudyBean study = studyDAO.findByPK(studySubject.getStudyId());
        StudyBean pStudy = getParentStudy(study.getOid());
        return studyParameterValueDAO.findByHandleAndStudy(pStudy.getId(), "participantPortal").getValue();
    }

    private String pManageStatus(StudySubjectBean studySubject) throws Exception {
        StudyBean study = studyDAO.findByPK(studySubject.getStudyId());
        StudyBean pStudy = getParentStudy(study.getOid());
        return new ParticipantPortalRegistrar().getCachedRegistrationStatus(pStudy.getOid(), session);
    }

    private SafeUrl participateUrl(StudySubjectBean studySubject) throws Exception {
        StudyBean study = studyDAO.findByPK(studySubject.getStudyId());
        StudyBean pStudy = getParentStudy(study.getOid());
        String hostUrl = new ParticipantPortalRegistrar().getStudyHost(pStudy.getOid());
        return url(hostUrl).param("ssid", studySubject.getLabel());
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = studyDAO.findByOid(studyOid);
        return study.getParentStudyId() == 0 ? study : studyDAO.findByPK(study.getParentStudyId());
    }

    // -- Data fetching -----------------------------------------------------------------------------

    private LCTableData<FindSubjectsRow> fetchData(LCTableParams p) {
        FindSubjectsFilter filter = buildFilter(p);
        FindSubjectsSort sort = buildSort(p);

        int rowStart = p.page * p.maxRows;
        int rowEnd = rowStart + p.maxRows;
        List<StudySubjectBean> studySubjects = studySubjectDAO.getWithFilterAndSort(studyBean, filter, sort, rowStart, rowEnd);
        int total = studySubjectDAO.getCountWithFilter(filter, studyBean);

        List<FindSubjectsRow> rows = new ArrayList<>();
        for (StudySubjectBean studySubject : studySubjects) {
            rows.add(buildRow(studySubject));
        }
        return new LCTableData<>(rows, total);
    }

    private FindSubjectsFilter buildFilter(LCTableParams p) {
        FindSubjectsFilter filter = new FindSubjectsFilter();
        p.filters.forEach((property, value) -> {
            if ("studySubject.status".equals(property)) {
                filter.addFilter(property, Status.getByName(value).getId() + "");
            } else if (property.startsWith("sgc_")) {
                int studyGroupClassId = Integer.parseInt(property.substring(4));
                StudyGroupBean group = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId);
                filter.addFilter(property, group.getId() + "");
            } else {
                // includes sed_* (FindSubjectsFilter itself resolves the translated status name to its id)
                // and the plain free-text columns (studySubject.label, enrolledAt, studySubject.oid,
                // subject.charGender, studySubject.secondaryLabel, subject.uniqueIdentifier)
                filter.addFilter(property, value);
            }
        });
        return filter;
    }

    private FindSubjectsSort buildSort(LCTableParams p) {
        FindSubjectsSort sort = new FindSubjectsSort();
        if (p.sortProp != null && !p.sortProp.isEmpty()) {
            sort.addSort(p.sortProp, p.sortDir == null ? "asc" : p.sortDir);
        }
        return sort;
    }

    private FindSubjectsRow buildRow(StudySubjectBean studySubject) {
        String enrolledAt = (studyDAO.findByPK(studySubject.getStudyId())).getIdentifier();
        SubjectBean subject = subjectDAO.findByPK(studySubject.getSubjectId());

        List<StudyEventBean> allStudyEvents = studyEventDAO.findAllByStudySubject(studySubject);
        Map<Integer, List<StudyEventBean>> studyEventsBySedId = new HashMap<>();
        for (StudyEventBean studyEventBean : allStudyEvents) {
            studyEventsBySedId.computeIfAbsent(studyEventBean.getStudyEventDefinitionId(), k -> new ArrayList<>()).add(studyEventBean);
        }

        Map<Integer, GroupAssignment> groupAssignmentsByClassId = new HashMap<>();
        for (StudyGroupClassBean sgc : studyGroupClasses) {
            SubjectGroupMapBean sgm = subjectGroupMapDAO.findByStudySubjectAndStudyGroupClass(studySubject.getId(), sgc.getId());
            if (sgm != null) {
                groupAssignmentsByClassId.put(sgc.getId(), new GroupAssignment(sgm.getStudyGroupId(), sgm.getStudyGroupName()));
            }
        }

        Map<Integer, EventColumnData> eventDataByDefinitionId = new HashMap<>();
        for (StudyEventDefinitionBean sed : studyEventDefinitions) {
            List<StudyEventBean> studyEvents = studyEventsBySedId.getOrDefault(sed.getId(), new ArrayList<>());
            SubjectEventStatus aggregateStatus = SubjectEventStatus.NOT_SCHEDULED;
            for (StudyEventBean studyEventBean : studyEvents) {
                if (studyEventBean.getSampleOrdinal() == 1) {
                    aggregateStatus = studyEventBean.getSubjectEventStatus();
                    break;
                }
            }
            eventDataByDefinitionId.put(sed.getId(), new EventColumnData(aggregateStatus, studyEvents, sed));
        }

        boolean isSignable = isSignable(allStudyEvents, studySubject);

        return new FindSubjectsRow(studySubject, subject, enrolledAt, isSignable, groupAssignmentsByClassId, eventDataByDefinitionId);
    }

    private boolean isSignable(List<StudyEventBean> allStudyEvents, StudySubjectBean studySubject) {
        if (studySubject.getStatus().isSigned()) {
            return false;
        }
        for (StudyEventBean studyEventBean : allStudyEvents) {
            if (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED
                || studyEventBean.getSubjectEventStatus() == SubjectEventStatus.SCHEDULED) {
                return false;
            }
            if (eventHasRequiredUncompleteCRFs(studyEventBean)) {
                return false;
            }
        }
        return true;
    }

    private boolean eventHasRequiredUncompleteCRFs(StudyEventBean studyEventBean) {
        List<EventCRFBean> eventCRFs = eventCRFDAO.findAllByStudyEvent(studyEventBean);
        for (EventCRFBean crfBean : eventCRFs) {
            if (crfBean != null && crfBean.getCompletionStatusId() == 0 && eventDefinitionCRFDAO.isRequiredInDefinition(crfBean.getCRFVersionId(), studyEventBean)) {
                return true;
            }
        }
        return false;
    }

    // -- Rendering entry point ---------------------------------------------------------------------

    public String render(HttpServletRequest request) {
        final LCTableParams params = new LCTableParams(request.getQueryString(), this.table);
        return this.table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }

}
