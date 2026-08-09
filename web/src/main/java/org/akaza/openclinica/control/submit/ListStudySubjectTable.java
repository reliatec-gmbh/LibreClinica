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
import org.akaza.openclinica.lctable.*;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.Td;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableFilterDef.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.SafeUrl.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final Map<Integer, String> EVENT_STATUS_ICON_PATHS = new HashMap<>();
    static {
        EVENT_STATUS_ICON_PATHS.put(1, "images/icon_Scheduled.gif");
        EVENT_STATUS_ICON_PATHS.put(2, "images/icon_NotStarted.gif");
        EVENT_STATUS_ICON_PATHS.put(3, "images/icon_InitialDE.gif");
        EVENT_STATUS_ICON_PATHS.put(4, "images/icon_DEcomplete.gif");
        EVENT_STATUS_ICON_PATHS.put(5, "images/icon_Stopped.gif");
        EVENT_STATUS_ICON_PATHS.put(6, "images/icon_Skipped.gif");
        EVENT_STATUS_ICON_PATHS.put(7, "images/icon_Locked.gif");
        EVENT_STATUS_ICON_PATHS.put(8, "images/icon_Signed.gif");
    }

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

        this.table = new LCTable<>("findSubjects", buildColumns(), this::fetchData);
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

    /** ID for definition-level event actions (e.g. "Schedule" or "Add another occurrence"). */
    private static String eventActionId(String action, int studySubjectId, int sedId) {
        return "findSubjects-" + action + "-event-" + studySubjectId + "-" + sedId;
    }

    /** Id for an action tied to one specific event occurrence (view/edit/remove an occurrence). */
    private static String occurrenceActionId(String action, int occurrenceId) {
        return "findSubjects-" + action + "-occurrence-" + occurrenceId;
    }

    /** Toolbar dropdown to navigate to {@code ListEventsForSubjects} for a selected event definition. */
    private void renderSelectEventControl(Div<?> container, LCTableContext<FindSubjectsRow> ctx) {
        container.div().attrClass("dropdown-list")
            .of(div -> {
                div.label().text("").__();
                div.select()
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
            .text(resword.getString("add_new_subject"))
            .__();
    }

    // -- Column definitions ----------------------------------------------------------------------

    private List<LCTableColumnDef<FindSubjectsRow>> buildColumns() {
        List<LCTableColumnDef<FindSubjectsRow>> columns = new ArrayList<>();

        columns.add(textCol("studySubject.label", resword.getString("study_subject_ID"), 6, row -> row.studySubject.getLabel()));
        columns.add(enumColHidden("studySubject.status", resword.getString("subject_status"), 6,
            row -> row.studySubject.getStatus(), Status.toDropDownArrayList(), Status::getName, Status::getName
        ));
        columns.add(textColHidden("enrolledAt", resword.getString("site_id"), 5, row -> row.enrolledAt));
        columns.add(textColHidden("studySubject.oid", resword.getString("rule_oid"), 6, row -> row.studySubject.getOid()));
        columns.add(new LCTableColumnDef<>("subject.charGender", resword.getString("gender"), 3, HIDDEN, NOT_SORTABLE, textFilter(),
            LCTableColumnDef.<FindSubjectsRow>nullSafeColText(row -> String.valueOf(row.subject.getGender()))
        ));
        columns.add(textColHidden("studySubject.secondaryLabel", resword.getString("secondary_ID"), 5, row -> row.studySubject.getSecondaryLabel()));
        columns.add(textColHidden("subject.uniqueIdentifier", resword.getString("subject_unique_ID"), 6, row -> row.subject.getUniqueIdentifier()));

        // one column per active study-group-class
        for (StudyGroupClassBean sgc : studyGroupClasses) {
            List<StudyGroupBean> groupOptions = studyGroupDAO.findAllByGroupClass(sgc);
            columns.add(enumColNotSortable("sgc_" + sgc.getId(), sgc.getName(), 6, groupOptions, StudyGroupBean::getName, StudyGroupBean::getName,
                row -> {
                    GroupAssignment ga = row.groupAssignmentsByClassId.get(sgc.getId());
                    return ga == null ? "" : ga.groupName;
                }
            ));
        }

        // one column per active study-event-definition
        for (StudyEventDefinitionBean sed : studyEventDefinitions) {
            columns.add(customTdCol("sed_" + sed.getId(), sed.getName(), 6, NOT_SORTABLE,
                // SubjectEventStatus.getName() (Term.getName()) already resolves the translated display name via
                // the terms resource bundle -- do NOT re-translate it here (that would look up the translated text
                // itself as if it were a resource key, throwing MissingResourceException).
                new LCTableFilterDef.Select<>(SubjectEventStatus.toArrayList(), SubjectEventStatus::getName, SubjectEventStatus::getName),
                (td, row) -> renderEventCell(td, row, row.eventDataByDefinitionId.get(sed.getId()))
            ));
        }

        columns.add(customTdCol("actions", resword.getString("rule_actions"), 10, NOT_SORTABLE, clearFilter(),
            this::renderActionsCell
        ));

        return columns;
    }

    /** Renders an event definition cell with status icon(s) and a hover/focus popup for event occurrence details (see {@link #renderEventPopup}). */
    private void renderEventCell(Td<?> td, FindSubjectsRow row, EventColumnData data) {
        if (data == null) {
            td.text("");
            return;
        }
        List<StudyEventBean> occurrences = data.occurrences == null ? Collections.emptyList() : data.occurrences;

        td.div().attrClass("event-trigger lc-popup-trigger").addAttr("tabindex", "0")
            .of(trigger -> {
                renderStatusIcons(trigger, data.aggregateStatus, occurrences);
                renderEventPopup(trigger, row.studySubject, data.definition, data.aggregateStatus, occurrences);
            })
            .__();
    }

    /** Renders an icon and count badge for each distinct status present across occurrences. */
    private <T extends Element<?, ?>> void renderStatusIcons(Div<T> trigger, SubjectEventStatus aggregateStatus, List<StudyEventBean> occurrences) {
        if (occurrences.isEmpty()) {
            renderStatusBadge(trigger, aggregateStatus, 0, false);
            return;
        }
        boolean showCounts = occurrences.size() > 1;
        Map<Integer, Long> countByStatusId = occurrences.stream()
            .collect(Collectors.groupingBy(o -> o.getSubjectEventStatus().getId(), Collectors.counting()));
        // iterate in the same fixed, deterministic order used for the column's filter dropdown
        for (SubjectEventStatus status : SubjectEventStatus.toArrayList()) {
            Long count = countByStatusId.get(status.getId());
            if (count != null) {
                renderStatusBadge(trigger, status, count.intValue(), showCounts);
            }
        }
    }

    private <T extends Element<?, ?>> void renderStatusBadge(Div<T> trigger, SubjectEventStatus status, int count, boolean showCount) {
        String iconPath = EVENT_STATUS_ICON_PATHS.get(status.getId());
        trigger.span().attrClass("event-status-badge")
            .of(badge -> {
                if (iconPath != null) {
                    badge.img().attrSrc(iconPath).attrAlt(status.getName()).__();
                }
                if (showCount) {
                    badge.text(" x" + count);
                }
            })
            .__();
    }

    /** Renders the hover/focus popup showing subject/event header and scrolling occurrence list. */
    private <T extends Element<?, ?>> void renderEventPopup(Div<T> trigger, StudySubjectBean studySubject,
                                                            StudyEventDefinitionBean sed, SubjectEventStatus aggregateStatus, List<StudyEventBean> occurrences) {
        Status subjectStatus = studySubject.getStatus();
        boolean canAddOccurrence = sed.isRepeating() && !occurrences.isEmpty()
            && subjectStatus != Status.DELETED && subjectStatus != Status.AUTO_DELETED
            && studyBean.getStatus() == Status.AVAILABLE;
        String addOccurrenceHref = "CreateNewStudyEvent?studySubjectId=" + studySubject.getId() + "&studyEventDefinition=" + sed.getId();

        trigger.div().attrClass("event-popup lc-popup ViewSubjectsPopup")
            .of(popup -> {
                popup.div().attrClass("event-popup-header table_header_row_left")
                    .of(header -> {
                        header.div().attrClass("event-popup-header-text")
                            .text(resword.getString("subject") + ": " + studySubject.getLabel())
                            .br().__()
                            .text(resword.getString("event") + ": " + sed.getName())
                            .__();
                        if (canAddOccurrence) {
                            header.a().attrClass("text-btn").attrHref(addOccurrenceHref)
                                .attrId(eventActionId("add-occurrence", studySubject.getId(), sed.getId()))
                                .text(resword.getString("add_another_occurrence"))
                                .__();
                        }
                    })
                    .__();
                popup.div().attrClass("event-popup-body")
                    .of(body -> renderPopupBody(body, studySubject, sed, aggregateStatus, occurrences))
                    .__();
            })
            .__();
    }

    private <T extends Element<?, ?>> void renderPopupBody(Div<T> body, StudySubjectBean studySubject, StudyEventDefinitionBean sed,
                                                           SubjectEventStatus aggregateStatus, List<StudyEventBean> occurrences) {
        if (occurrences.isEmpty()) {
            body.div().attrClass("event-occurrence-row")
                .of(row -> {
                    row.div().attrClass("table_cell_left")
                        .of(line -> renderStatusLine(line, aggregateStatus))
                        .__();
                    if (currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                        row.div().attrClass("table_cell_left occurrence-actions occurrence-actions-vertical")
                            .of(actions -> actions.of(actionLink(eventActionId("schedule", studySubject.getId(), sed.getId()), resword.getString("schedule"),
                                url("CreateNewStudyEvent").param("studySubjectId", studySubject.getId()).param("studyEventDefinition", sed.getId()),
                                "bt_Schedule.gif", true)))
                            .__();
                    }
                })
                .__();
            return;
        }
        int total = occurrences.size();
        for (int i = 0; i < total; i++) {
            StudyEventBean occurrence = occurrences.get(i);
            if (sed.isRepeating()) {
                renderRepeatingOccurrenceRow(body, studySubject, occurrence, i + 1, total);
            } else {
                renderSimpleOccurrenceRow(body, studySubject, occurrence);
            }
        }
    }

    /** Vertical layout for non-repeating events and unpopulated occurrences (icon + text label). */
    private <T extends Element<?, ?>> void renderSimpleOccurrenceRow(Div<T> body, StudySubjectBean studySubject, StudyEventBean occurrence) {
        body.div().attrClass("event-occurrence-row")
            .of(row -> {
                String date = formatDate(occurrence.getDateStarted());
                if (!date.isEmpty()) {
                    row.div().attrClass("table_cell_left").text(date).__();
                }
                row.div().attrClass("table_cell_left")
                    .of(line -> renderStatusLine(line, occurrence.getSubjectEventStatus()))
                    .__();
                row.div().attrClass("table_cell_left occurrence-actions occurrence-actions-vertical")
                    .of(actions -> renderOccurrenceActions(actions, studySubject, occurrence, true))
                    .__();
            })
            .__();
    }

    /** Compact two-line layout for repeating event occurrences. */
    private <T extends Element<?, ?>> void renderRepeatingOccurrenceRow(Div<T> body, StudySubjectBean studySubject, StudyEventBean occurrence,
                                                                        int occNumber, int total) {
        body.div().attrClass("event-occurrence-row event-occurrence-row-compact")
            .of(row -> {
                row.div().attrClass("table_cell_left occurrence-summary-line")
                    .of(line -> {
                        String date = formatDate(occurrence.getDateStarted());
                        line.text(resword.getString("ocurrence") + " #" + occNumber + " of " + total + " \u00B7 ");
                        if (!date.isEmpty()) {
                            line.text(date + " \u00B7 ");
                        }
                        line.text(resword.getString("status") + ": ");
                        renderStatusIconAndText(line, occurrence.getSubjectEventStatus());
                    })
                    .__();
                row.div().attrClass("table_cell_left occurrence-actions occurrence-actions-compact")
                    .of(actions -> {
                        actions.text(resword.getString("rule_actions") + ": ");
                        renderOccurrenceActions(actions, studySubject, occurrence, false);
                    })
                    .__();
            })
            .__();
    }

    /** Renders "Status: [icon] text", used by both the simple and compact occurrence-row layouts. */
    private <T extends Element<?, ?>> void renderStatusLine(Div<T> line, SubjectEventStatus status) {
        line.text(resword.getString("status") + ": ");
        renderStatusIconAndText(line, status);
    }

    /** Renders the status icon (if any) immediately followed by the status's display text. */
    private <T extends Element<?, ?>> void renderStatusIconAndText(Div<T> container, SubjectEventStatus status) {
        String iconPath = EVENT_STATUS_ICON_PATHS.get(status.getId());
        if (iconPath != null) {
            container.img().attrClass("event-status-icon-inline").attrSrc(iconPath).attrAlt(status.getName()).__();
        }
        container.text(" " + status.getName());
    }

    /**
     * Renders view/edit/remove action links for an event occurrence.
     *
     * @param includeText if true, renders icon + text label; if false, renders icon-only links.
     */
    private <T extends Element<?, ?>> void renderOccurrenceActions(Div<T> actions, StudySubjectBean studySubject, StudyEventBean occurrence,
                                                                   boolean includeText) {
        Status eventSysStatus = studySubject.getStatus();
        SubjectEventStatus eventStatus = occurrence.getSubjectEventStatus();
        String studyEventId = String.valueOf(occurrence.getId());
        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        boolean studyDirectorOrSysAdmin = currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin();
        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;

        if (eventSysStatus == Status.DELETED || eventSysStatus == Status.AUTO_DELETED) {
            actions.of(actionLink(occurrenceActionId("view", occurrence.getId()), view, url("EnterDataForStudyEvent").param("eventId", studyEventId), "bt_View.gif", includeText));
            return;
        }
        if (eventSysStatus.getId() != Status.AVAILABLE.getId() && eventSysStatus != Status.SIGNED) {
            return;
        }
        if (eventStatus == SubjectEventStatus.LOCKED) {
            if (studyDirectorOrSysAdmin) {
                actions.of(actionLink(occurrenceActionId("view", occurrence.getId()), view, url("EnterDataForStudyEvent").param("eventId", studyEventId), "bt_View.gif", includeText));
                if (studyAvailable) {
                    actions.of(actionLink(occurrenceActionId("remove", occurrence.getId()), resword.getString("remove"),
                        url("RemoveStudyEvent").param("action", "confirm").param("id", studyEventId).param("studySubId", studySubject.getId()),
                        "bt_Remove.gif", includeText));
                }
            }
            return;
        }
        actions.of(actionLink(occurrenceActionId("view", occurrence.getId()), view, url("EnterDataForStudyEvent").param("eventId", studyEventId), "bt_View.gif", includeText));
        if (studyDirectorOrSysAdmin && studyAvailable) {
            actions.of(actionLink(occurrenceActionId("edit", occurrence.getId()), resword.getString("edit"),
                url("UpdateStudyEvent").param("event_id", studyEventId).param("ss_id", studySubject.getId()), "bt_Edit.gif", includeText));
            actions.of(actionLink(occurrenceActionId("remove", occurrence.getId()), resword.getString("remove"),
                url("RemoveStudyEvent").param("action", "confirm").param("id", studyEventId).param("studySubId", studySubject.getId()), "bt_Remove.gif", includeText));
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(resformat.getString("date_format_string")).format(date);
    }

    private void renderActionsCell(Td<?> td, FindSubjectsRow row) {
        StudySubjectBean studySubject = row.studySubject;
        if (studySubject.getId() == 0) {
            return;
        }
        td.of(actionLink(actionId("view", studySubject.getId()), resword.getString("view"), url("ViewStudySubject").param("id", studySubject.getId()), "bt_View.gif"));

        if (currentRole.getRole() == Role.MONITOR) {
            return;
        }

        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;
        boolean subjectDeleted = studySubject.getStatus() == Status.DELETED || studySubject.getStatus() == Status.AUTO_DELETED;

        if (studyAvailable && !subjectDeleted && currentRole.getRole() != Role.RESEARCHASSISTANT && currentRole.getRole() != Role.RESEARCHASSISTANT2) {
            td.of(actionLink(actionId("remove", studySubject.getId()), resword.getString("remove"),
                url("RemoveStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId()).param("studyId", studySubject.getStudyId()),
                "bt_Remove.gif"));
        }
        if (studyAvailable && subjectDeleted) {
            td.of(actionLink(actionId("restore", studySubject.getId()), resword.getString("restore"),
                url("RestoreStudySubject").param("action", "confirm").param("id", studySubject.getId()).param("subjectId", studySubject.getSubjectId()).param("studyId", studySubject.getStudyId()),
                "bt_Restore.gif"));
        }
        if (studyAvailable && currentRole.getRole() != Role.RESEARCHASSISTANT && currentRole.getRole() != Role.RESEARCHASSISTANT2
            && currentRole.getRole() != Role.INVESTIGATOR && studySubject.getStatus() == Status.AVAILABLE) {
            td.of(actionLink(actionId("reassign", studySubject.getId()), resword.getString("reassign"), url("ReassignStudySubject").param("id", studySubject.getId()), "bt_Reassign.gif"));
        }
        if (currentRole.getRole() == Role.INVESTIGATOR && studyAvailable && studySubject.getStatus() != Status.DELETED && row.isSignable) {
            td.of(actionLink(actionId("sign", studySubject.getId()), resword.getString("sign"), url("SignStudySubject").param("id", studySubject.getId()), "icon_Signed.gif"));
        }
        try {
            if (studyAvailable && (currentRole.getRole() == Role.RESEARCHASSISTANT || currentRole.getRole() == Role.RESEARCHASSISTANT2)
                && studySubject.getStatus() == Status.AVAILABLE && "ACTIVE".equalsIgnoreCase(pManageStatus(studySubject))
                && "enabled".equalsIgnoreCase(participateStatus(studySubject))) {
                td.of(actionLink(actionId("connect-participant", studySubject.getId()), resword.getString("connect_participant"), participateUrl(studySubject), "bt_Ocui.gif"));
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
        return this.table.render(request.getRequestURI(), params, request.getContextPath());
    }

}
