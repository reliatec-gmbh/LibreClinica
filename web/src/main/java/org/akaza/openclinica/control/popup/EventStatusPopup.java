/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.control.popup;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.lctable.LCPopup;
import org.akaza.openclinica.lctable.LCPopup.PopupAction;
import org.akaza.openclinica.lctable.LCPopup.PopupItemLayout;
import org.akaza.openclinica.lctable.LCPopup.PopupItemRenderer;
import org.xmlet.htmlapifaster.Div;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.akaza.openclinica.lctable.LCPopup.PopupAction.of;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/**
 * {@link LCPopup} specialization generalizing both current event-status popups:
 * <ul>
 * <li>{@link #aggregate}: {@code findSubjects}'s ("Subject Matrix") one-aggregate-trigger-per-cell popup, showing
 * every occurrence of one event definition for one subject (ported from {@code ListStudySubjectTable}'s
 * {@code renderEventCell}/{@code renderStatusIcons}/{@code renderEventPopup}/{@code renderPopupBody}).</li>
 * <li>{@link #perOccurrence}: {@code listEventsForSubjects}'s one-popup-per-occurrence popup, scoped to a single,
 * pre-selected event definition (ported from {@code ListEventsForSubjectTable}'s
 * {@code renderEventStatus*}/{@code renderEventOccurrence*} methods).</li>
 * </ul>
 * Both factories return an {@code LCPopup<Context, EventOccurrence>}: the same {@code EventOccurrence} item type
 * (and hence the same {@link PopupItemLayout}/{@link PopupAction} rendering machinery) serves either mode; only the
 * number of items resolved per popup instance (many vs. one) and each mode's own gating/layout closures differ.
 *
 * <p>Application-specific (depends on the LibreClinica domain model) -- deliberately kept out of the generic
 * {@code org.akaza.openclinica.lctable} library; see that package's {@link LCPopup} for the mechanical, reusable
 * part this class builds on.
 */
public final class EventStatusPopup {
    private EventStatusPopup() {}

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

    /** One occurrence (real or synthetic "not scheduled" placeholder) of the popup's event definition. */
    public static final class EventOccurrence {
        public final SubjectEventStatus status;
        public final Date eventDate;                     // null for the synthetic "not scheduled" placeholder
        public final Optional<StudyEventBean> studyEvent; // empty for the synthetic "not scheduled" placeholder

        private EventOccurrence(SubjectEventStatus status, Date eventDate, Optional<StudyEventBean> studyEvent) {
            this.status = status;
            this.eventDate = eventDate;
            this.studyEvent = studyEvent;
        }

        public static EventOccurrence notScheduled(SubjectEventStatus status) {
            return new EventOccurrence(status, null, Optional.empty());
        }

        public static EventOccurrence of(StudyEventBean studyEvent) {
            return new EventOccurrence(studyEvent.getSubjectEventStatus(), studyEvent.getDateStarted(), Optional.of(studyEvent));
        }
    }

    /** The popup's own subject data: the study subject, plus the (never empty) list of occurrences to list. */
    public static final class Context {
        public final StudySubjectBean studySubject;
        public final List<EventOccurrence> occurrences;

        public Context(StudySubjectBean studySubject, List<EventOccurrence> occurrences) {
            this.studySubject = studySubject;
            this.occurrences = occurrences;
        }
    }

    // -- shared trigger-icon rendering (same badge-per-distinct-status logic serves both aggregate and
    //    per-occurrence modes: with a singleton items list, "showCount" naturally evaluates to false) ------------

    private static void renderTriggerIcons(Div<?> trigger, List<EventOccurrence> items) {
        if (items.size() == 1 && items.get(0).studyEvent.isEmpty()) {
            renderBadge(trigger, items.get(0).status, 0, false);
            return;
        }
        boolean showCounts = items.size() > 1;
        Map<Integer, Long> countByStatusId = items.stream()
            .collect(Collectors.groupingBy(o -> o.status.getId(), Collectors.counting()));
        // iterate in the same fixed, deterministic order used for the column's filter dropdown
        for (SubjectEventStatus status : SubjectEventStatus.toArrayList()) {
            Long count = countByStatusId.get(status.getId());
            if (count != null) {
                renderBadge(trigger, status, count.intValue(), showCounts);
            }
        }
    }

    private static void renderBadge(Div<?> trigger, SubjectEventStatus status, int count, boolean showCount) {
        String iconPath = EVENT_STATUS_ICON_PATHS.get(status.getId());
        trigger.span().attrClass("lc-popup-status-badge")
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

    // -- shared status-line rendering -----------------------------------------------------------------------

    private static String formatDate(Date date, ResourceBundle resformat) {
        if (date == null) return "";
        return new SimpleDateFormat(resformat.getString("date_format_string")).format(date);
    }

    private static void renderStatusIconAndText(Div<?> container, SubjectEventStatus status) {
        String iconPath = EVENT_STATUS_ICON_PATHS.get(status.getId());
        if (iconPath != null) {
            container.img().attrClass("lc-popup-status-icon-inline").attrSrc(iconPath).attrAlt(status.getName()).__();
        }
        container.text(" " + status.getName());
    }

    private static void renderStatusLine(Div<?> line, SubjectEventStatus status, ResourceBundle resword) {
        line.text(resword.getString("status") + ": ");
        renderStatusIconAndText(line, status);
    }

    /** {@link PopupItemLayout#SIMPLE} status content: an optional date line, a separator, then a "Status: [icon] text" line. */
    private static void renderSimpleStatus(Div<?> row, EventOccurrence occ, ResourceBundle resword, ResourceBundle resformat) {
        String date = formatDate(occ.eventDate, resformat);
        if (!date.isEmpty()) {
            row.div().attrClass("table_cell_left").text(date).__();
            row.div().attrClass("lc-popup-row-separator").__();
        }
        row.div().attrClass("table_cell_left").of(line -> renderStatusLine(line, occ.status, resword)).__();
    }

    /** {@link PopupItemLayout#COMPACT} status content: one "Occurrence #i of N &middot; date &middot; status" line. */
    private static void renderCompactStatus(Div<?> row, EventOccurrence occ, int index, int total, ResourceBundle resword, ResourceBundle resformat) {
        row.div().attrClass("lc-popup-summary-line")
            .of(line -> {
                String date = formatDate(occ.eventDate, resformat);
                line.text(resword.getString("ocurrence") + " #" + (index + 1) + " of " + total + " \u00B7 ");
                if (!date.isEmpty()) {
                    line.text(date + " \u00B7 ");
                }
                renderStatusLine(line, occ.status, resword);
            })
            .__();
    }

    // -- shared occurrence-action gating (ported from ListStudySubjectTable#renderOccurrenceActions /
    //    ListEventsForSubjectTable#renderEventOccurrenceActions, unchanged branching, only the two tables' own
    //    subtly-different existing gating rules are preserved verbatim -- see aggregate()/perOccurrence() below) ---

    private static List<PopupAction> viewEditRemoveActions(StudySubjectBean studySubject, StudyEventBean studyEvent, boolean showLabel,
            StudyUserRoleBean currentRole, UserAccountBean currentUser, StudyBean studyBean, ResourceBundle resword, String actionIdPrefix) {
        List<PopupAction> actions = new ArrayList<>();
        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        boolean studyDirectorOrSysAdmin = currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin();
        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;
        SubjectEventStatus eventStatus = studyEvent.getSubjectEventStatus();

        if (eventStatus == SubjectEventStatus.LOCKED) {
            if (studyDirectorOrSysAdmin) {
                actions.add(of(actionIdPrefix + "view-occurrence-" + studyEvent.getId(), view,
                    url("EnterDataForStudyEvent").param("eventId", studyEvent.getId()), "bt_View.gif", showLabel));
                if (studyAvailable) {
                    actions.add(of(actionIdPrefix + "remove-occurrence-" + studyEvent.getId(), resword.getString("remove"),
                        url("RemoveStudyEvent").param("action", "confirm").param("id", studyEvent.getId()).param("studySubId", studySubject.getId()),
                        "bt_Remove.gif", showLabel));
                }
            }
            return actions;
        }
        actions.add(of(actionIdPrefix + "view-occurrence-" + studyEvent.getId(), view,
            url("EnterDataForStudyEvent").param("eventId", studyEvent.getId()), "bt_View.gif", showLabel));
        if (studyDirectorOrSysAdmin && studyAvailable) {
            actions.add(of(actionIdPrefix + "edit-occurrence-" + studyEvent.getId(), resword.getString("edit"),
                url("UpdateStudyEvent").param("event_id", studyEvent.getId()).param("ss_id", studySubject.getId()), "bt_Edit.gif", showLabel));
            actions.add(of(actionIdPrefix + "remove-occurrence-" + studyEvent.getId(), resword.getString("remove"),
                url("RemoveStudyEvent").param("action", "confirm").param("id", studyEvent.getId()).param("studySubId", studySubject.getId()),
                "bt_Remove.gif", showLabel));
        }
        return actions;
    }

    // -- aggregate (findSubjects) ---------------------------------------------------------------------------

    /**
     * Builds the aggregate ({@code findSubjects}-style) popup for one event-definition column: one trigger per
     * cell, listing every occurrence of {@code sed} for the subject (or a single "not scheduled" placeholder).
     */
    public static LCPopup<Context, EventOccurrence> aggregate(StudyEventDefinitionBean sed, StudyBean studyBean, StudyUserRoleBean currentRole,
            UserAccountBean currentUser, ResourceBundle resword, ResourceBundle resformat) {

        BiConsumer<Div<?>, Context> headerRenderer = (header, ctx) ->
            header.div().attrClass("lc-popup-header-text")
                .text(resword.getString("subject") + ": " + ctx.studySubject.getLabel()).br().__()
                .text(resword.getString("event") + ": " + sed.getName())
                .__();

        Optional<BiConsumer<Div<?>, Context>> extraHeaderControl = Optional.of((header, ctx) -> {
            boolean hasOccurrence = ctx.occurrences.stream().anyMatch(o -> o.studyEvent.isPresent());
            Status subjectStatus = ctx.studySubject.getStatus();
            boolean canAddOccurrence = sed.isRepeating() && hasOccurrence
                && subjectStatus != Status.DELETED && subjectStatus != Status.AUTO_DELETED
                && studyBean.getStatus() == Status.AVAILABLE;
            if (canAddOccurrence) {
                header.a().attrClass("text-btn")
                    .attrHref("CreateNewStudyEvent?studySubjectId=" + ctx.studySubject.getId() + "&studyEventDefinition=" + sed.getId())
                    .attrId("findSubjects-add-occurrence-event-" + ctx.studySubject.getId() + "-" + sed.getId())
                    .text(resword.getString("add_another_occurrence"))
                    .__();
            }
        });

        PopupItemRenderer<Context, EventOccurrence> itemRenderer = new PopupItemRenderer<>() {
            @Override
            public void renderStatus(Div<?> row, Context ctx, EventOccurrence item, int index, int total) {
                if (layoutFor(ctx, item) == PopupItemLayout.COMPACT) {
                    renderCompactStatus(row, item, index, total, resword, resformat);
                } else {
                    renderSimpleStatus(row, item, resword, resformat);
                }
            }

            @Override
            public List<PopupAction> actionsFor(Context ctx, EventOccurrence item) {
                StudySubjectBean studySubject = ctx.studySubject;
                if (item.studyEvent.isEmpty()) {
                    // no occurrence yet: same gating as the legacy findSubjects "empty occurrences" branch
                    // (deliberately NOT conditioned on the subject's own status, matching current behaviour)
                    List<PopupAction> actions = new ArrayList<>();
                    if (currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                        actions.add(of("findSubjects-schedule-event-" + studySubject.getId() + "-" + sed.getId(), resword.getString("schedule"),
                            url("CreateNewStudyEvent").param("studySubjectId", studySubject.getId()).param("studyEventDefinition", sed.getId()),
                            "bt_Schedule.gif", true));
                    }
                    return actions;
                }
                StudyEventBean studyEvent = item.studyEvent.get();
                Status eventSysStatus = studySubject.getStatus();
                boolean showLabel = layoutFor(ctx, item) != PopupItemLayout.COMPACT;
                if (eventSysStatus == Status.DELETED || eventSysStatus == Status.AUTO_DELETED) {
                    List<PopupAction> actions = new ArrayList<>();
                    actions.add(of("findSubjects-view-occurrence-" + studyEvent.getId(), resword.getString("view") + "/" + resword.getString("enter_data"),
                        url("EnterDataForStudyEvent").param("eventId", studyEvent.getId()), "bt_View.gif", showLabel));
                    return actions;
                }
                if (eventSysStatus.getId() != Status.AVAILABLE.getId() && eventSysStatus != Status.SIGNED) {
                    return List.of();
                }
                return viewEditRemoveActions(studySubject, studyEvent, showLabel, currentRole, currentUser, studyBean, resword, "findSubjects-");
            }

            @Override
            public PopupItemLayout layoutFor(Context ctx, EventOccurrence item) {
                return sed.isRepeating() && item.studyEvent.isPresent() ? PopupItemLayout.COMPACT : PopupItemLayout.SIMPLE;
            }
        };

        return new LCPopup<>(ctx -> ctx.occurrences, EventStatusPopup::renderTriggerIcons, headerRenderer, extraHeaderControl,
            itemRenderer, "event-trigger", "ViewSubjectsPopup");
    }

    // -- per-occurrence (listEventsForSubjects) -------------------------------------------------------------

    /**
     * Builds the per-occurrence ({@code listEventsForSubjects}-style) popup: scoped to a single, pre-selected
     * event definition, one trigger per occurrence (each rendered with a singleton items list, via
     * {@code LCTablePopupColumn.perOccurrence}).
     */
    public static LCPopup<Context, EventOccurrence> perOccurrence(StudyEventDefinitionBean selectedSed, StudyBean studyBean,
            StudyUserRoleBean currentRole, UserAccountBean currentUser, ResourceBundle resword, ResourceBundle resformat) {

        BiConsumer<Div<?>, Context> headerRenderer = (header, ctx) ->
            header.div().attrClass("lc-popup-header-text")
                .text(resword.getString("subject") + ": " + ctx.studySubject.getLabel()).br().__()
                .text(resword.getString("event") + ": " + selectedSed.getName())
                .__();

        PopupItemRenderer<Context, EventOccurrence> itemRenderer = new PopupItemRenderer<>() {
            @Override
            public void renderStatus(Div<?> row, Context ctx, EventOccurrence item, int index, int total) {
                renderSimpleStatus(row, item, resword, resformat);
            }

            /** Ported from the legacy {@code singleEventDivBuilder(...)}: schedule / view+enter-data / edit / remove. */
            @Override
            public List<PopupAction> actionsFor(Context ctx, EventOccurrence item) {
                StudySubjectBean studySubject = ctx.studySubject;
                Status eventSysStatus = studySubject.getStatus();
                SubjectEventStatus eventStatus = item.status;
                Optional<StudyEventBean> studyEvent = item.studyEvent;
                List<PopupAction> actions = new ArrayList<>();
                String view = resword.getString("view") + "/" + resword.getString("enter_data");
                boolean studyDirectorOrSysAdmin = currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin();
                boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;

                if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {
                    if (eventStatus == SubjectEventStatus.NOT_SCHEDULED) {
                        if (currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                            actions.add(of("listEventsForSubject-schedule-event-" + studySubject.getId() + "-" + selectedSed.getId(),
                                resword.getString("schedule"),
                                url("CreateNewStudyEvent").param("studySubjectId", studySubject.getId())
                                    .param("studyEventDefinition", selectedSed.getId()),
                                "bt_Schedule.gif", true));
                        }
                    } else if (eventStatus == SubjectEventStatus.LOCKED) {
                        if (studyDirectorOrSysAdmin) {
                            StudyEventBean se = studyEvent.get();
                            actions.add(of("listEventsForSubject-view-occurrence-" + se.getId(), view,
                                url("EnterDataForStudyEvent").param("eventId", se.getId()), "bt_View.gif", true));
                            if (studyAvailable) {
                                actions.add(of("listEventsForSubject-remove-occurrence-" + se.getId(), resword.getString("remove"),
                                    url("RemoveStudyEvent").param("action", "confirm").param("id", se.getId()).param("studySubId", studySubject.getId()),
                                    "bt_Remove.gif", true));
                            }
                        }
                    } else {
                        // COMPLETED and all other actual-occurrence statuses: identical action set in the legacy table
                        StudyEventBean se = studyEvent.get();
                        actions.add(of("listEventsForSubject-view-occurrence-" + se.getId(), view,
                            url("EnterDataForStudyEvent").param("eventId", se.getId()), "bt_View.gif", true));
                        if (studyDirectorOrSysAdmin && studyAvailable) {
                            actions.add(of("listEventsForSubject-edit-occurrence-" + se.getId(), resword.getString("edit"),
                                url("UpdateStudyEvent").param("event_id", se.getId()).param("ss_id", studySubject.getId()), "bt_Edit.gif", true));
                            actions.add(of("listEventsForSubject-remove-occurrence-" + se.getId(), resword.getString("remove"),
                                url("RemoveStudyEvent").param("action", "confirm").param("id", se.getId()).param("studySubId", studySubject.getId()),
                                "bt_Remove.gif", true));
                        }
                    }
                }
                if ((eventSysStatus == Status.DELETED || eventSysStatus == Status.AUTO_DELETED) && studyEvent.isPresent()) {
                    StudyEventBean se = studyEvent.get();
                    actions.add(of("listEventsForSubject-view-occurrence-" + se.getId(), view,
                        url("EnterDataForStudyEvent").param("eventId", se.getId()), "bt_View.gif", true));
                }
                return actions;
            }

            @Override
            public PopupItemLayout layoutFor(Context ctx, EventOccurrence item) {
                return PopupItemLayout.SIMPLE;
            }
        };

        return new LCPopup<>(ctx -> ctx.occurrences, EventStatusPopup::renderTriggerIcons, headerRenderer, Optional.empty(),
            itemRenderer, "event-trigger", "ViewSubjectsPopup");
    }
}

