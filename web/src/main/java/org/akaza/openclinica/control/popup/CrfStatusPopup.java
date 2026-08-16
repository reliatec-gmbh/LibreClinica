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

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.control.managestudy.ListEventsForSubjectRow.CrfOccurrenceData;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
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

import static org.akaza.openclinica.lctable.LCPopup.PopupAction.of;
import static org.akaza.openclinica.lctable.LCPopup.PopupAction.ofRawHref;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/**
 * {@link LCPopup} specialization for {@code listEventsForSubjects}'s CRF-status popup (one trigger per occurrence,
 * one per CRF column). Ported from {@code ListEventsForSubjectTable}'s {@code renderCrfCell}/{@code renderCrfStatusIcon}/
 * {@code renderCrfPopup}/{@code renderCrfOccurrenceActions}, in turn ported from the legacy
 * {@code EventCrfLayerBuilder.clickToEnterData()} -- unchanged branching, only the HTML-emitting glue moved
 * into {@link LCPopup}/{@link PopupItemRenderer}.
 *
 * <p>Application-specific (depends on the LibreClinica domain model, and specifically on
 * {@code control.managestudy.ListEventsForSubjectRow.CrfOccurrenceData}) -- deliberately kept out of the generic
 * {@code org.akaza.openclinica.lctable} library; see that package's {@link LCPopup} for the mechanical, reusable
 * part this class builds on.
 *
 * <p>Not yet built in "aggregate" mode (one trigger per cell, grouping several occurrences the way
 * {@link EventStatusPopup#aggregate} does for event status) -- {@code findSubjects} has no CRF-status column yet;
 * see the implementation-plan document for that follow-up.
 */
public final class CrfStatusPopup {
    private CrfStatusPopup() {}

    private static final Map<Integer, String> CRF_STATUS_ICON_PATHS = new HashMap<>();
    static {
        CRF_STATUS_ICON_PATHS.put(0, "images/CRF_status_icon_Invalid.gif");
        CRF_STATUS_ICON_PATHS.put(1, "images/CRF_status_icon_Scheduled.gif");
        CRF_STATUS_ICON_PATHS.put(2, "images/CRF_status_icon_Started.gif");
        CRF_STATUS_ICON_PATHS.put(3, "images/CRF_status_icon_InitialDEcomplete.gif");
        CRF_STATUS_ICON_PATHS.put(4, "images/CRF_status_icon_DDE.gif");
        CRF_STATUS_ICON_PATHS.put(5, "images/CRF_status_icon_Complete.gif");
        CRF_STATUS_ICON_PATHS.put(6, "images/CRF_status_icon_Complete.gif");
        CRF_STATUS_ICON_PATHS.put(7, "images/CRF_status_icon_Locked.gif");
    }

    /** The popup's own subject data: the study subject, the (nullable) occurrence's study event, and its CRF data. */
    public static final class Context {
        public final StudySubjectBean studySubject;
        public final StudyEventBean studyEvent;   // nullable: the synthetic "not scheduled" placeholder occurrence
        public final CrfOccurrenceData data;

        public Context(StudySubjectBean studySubject, StudyEventBean studyEvent, CrfOccurrenceData data) {
            this.studySubject = studySubject;
            this.studyEvent = studyEvent;
            this.data = data;
        }

        private int occurrenceKey() {
            return studyEvent == null ? 0 : studyEvent.getId();
        }
    }

    private static void renderTriggerIcon(Div<?> trigger, List<CrfOccurrenceData> items) {
        DataEntryStage stage = items.get(0).stage;
        String iconPath = CRF_STATUS_ICON_PATHS.get(stage.getId());
        trigger.span().attrClass("lc-popup-status-badge")
            .of(badge -> {
                if (iconPath != null) {
                    badge.img().attrSrc(iconPath).attrAlt(stage.getName()).__();
                }
            })
            .__();
    }

    private static String formatDate(Date date, ResourceBundle resformat) {
        if (date == null) return "";
        return new SimpleDateFormat(resformat.getString("date_format_string")).format(date);
    }

    /** "Status: [icon] text" line -- same layout pattern as {@code EventStatusPopup#renderStatusLine}. */
    private static void renderStatusLine(Div<?> line, DataEntryStage stage, ResourceBundle resword) {
        line.text(resword.getString("status") + ": ");
        String iconPath = CRF_STATUS_ICON_PATHS.get(stage.getId());
        if (iconPath != null) {
            line.img().attrClass("lc-popup-status-icon-inline").attrSrc(iconPath).attrAlt(stage.getName()).__();
        }
        line.text(" " + stage.getName());
    }

    /**
     * Builds the per-occurrence CRF-status popup for one CRF column.
     *
     * @param contextPath needed for building REST "print" links (see {@code printExistingCrfHref}/{@code printBlankCrfHref})
     */
    public static LCPopup<Context, CrfOccurrenceData> perOccurrence(StudyEventDefinitionBean selectedSed, StudyBean studyBean,
            StudyUserRoleBean currentRole, UserAccountBean currentUser, CRFVersionDAO crfVersionDAO, ResourceBundle resword,
            ResourceBundle resformat, String contextPath) {

        BiConsumer<Div<?>, Context> headerRenderer = (header, ctx) ->
            header.div().attrClass("lc-popup-header-text")
                .text(resword.getString("subject") + ": " + ctx.studySubject.getLabel()).br().__()
                .text(resword.getString("CRF") + ": " + ctx.data.crf.getName())
                .__();

        PopupItemRenderer<Context, CrfOccurrenceData> itemRenderer = new PopupItemRenderer<>() {
            @Override
            public void renderStatus(Div<?> row, Context ctx, CrfOccurrenceData item, int index, int total) {
                // consistent with EventStatusPopup#renderSimpleStatus: an "Event date: ..." line, a separator,
                // then the "Status: [icon] text" line (previously shown, icon-less, in the header instead)
                String date = ctx.studyEvent == null ? "" : formatDate(ctx.studyEvent.getDateStarted(), resformat);
                if (!date.isEmpty()) {
                    row.div().attrClass("table_cell_left").text(resword.getString("event_date") + ": " + date).__();
                    row.div().attrClass("lc-popup-row-separator").__();
                }
                row.div().attrClass("table_cell_left").of(line -> renderStatusLine(line, item.stage, resword)).__();
            }

            @Override
            public List<PopupAction> actionsFor(Context ctx, CrfOccurrenceData item) {
                return crfOccurrenceActions(ctx, studyBean, currentRole, currentUser, crfVersionDAO, resword, contextPath, selectedSed);
            }

            @Override
            public PopupItemLayout layoutFor(Context ctx, CrfOccurrenceData item) {
                return PopupItemLayout.SIMPLE;
            }
        };

        return new LCPopup<>(ctx -> List.of(ctx.data), CrfStatusPopup::renderTriggerIcon, headerRenderer, Optional.empty(),
            itemRenderer, "crf-trigger", "ViewSubjectsPopup");
    }

    /**
     * Ported from {@code EventCrfLayerBuilder.clickToEnterData()}: view/enter-data/print/edit/remove/restore/delete
     * links, branching on {@link DataEntryStage} and gated by role/study-status, exactly as the legacy table. The
     * {@code hiddenCrf()} condition (site-level hidden CRFs, issue 3212) is preserved.
     */
    private static List<PopupAction> crfOccurrenceActions(Context ctx, StudyBean studyBean, StudyUserRoleBean currentRole,
            UserAccountBean currentUser, CRFVersionDAO crfVersionDAO, ResourceBundle resword, String contextPath,
            StudyEventDefinitionBean selectedSed) {
        StudySubjectBean studySubject = ctx.studySubject;
        StudyEventBean studyEvent = ctx.studyEvent;
        CrfOccurrenceData data = ctx.data;
        DataEntryStage stage = data.stage;
        EventCRFBean eventCrf = data.eventCrf;
        EventDefinitionCRFBean edc = data.eventDefinitionCrf;
        CRFBean crf = data.crf;
        boolean hidden = studyBean.getParentStudyId() > 0 && edc.isHideCrf();
        boolean studyAvailable = studyBean.getStatus() == Status.AVAILABLE;
        boolean directorOrSysAdmin = currentRole.isDirector() || currentUser.isSysAdmin();
        int occurrenceKey = ctx.occurrenceKey();

        List<PopupAction> actions = new ArrayList<>();

        if (stage == DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE || stage == DataEntryStage.ADMINISTRATIVE_EDITING) {
            if (!hidden) {
                actions.add(viewSectionDataEntryLink(eventCrf, edc, resword));
                actions.add(printExistingCrfLink(studyBean, studySubject, studyEvent, eventCrf, crfVersionDAO, resword, contextPath, selectedSed));
            }
            if (!currentRole.isMonitor() && studyAvailable) {
                if (!hidden) {
                    actions.add(of(null, resword.getString("edit"),
                        url("AdministrativeEditing").param("eventCRFId", eventCrf.getId()).param("exitTo", "ListStudySubjects"), "bt_Edit.gif", true));
                }
                if (directorOrSysAdmin) {
                    actions.add(removeEventCrfLink(studySubject, crf, eventCrf, occurrenceKey, resword));
                }
            }
            if (studyAvailable && currentUser.isSysAdmin()) {
                actions.add(deleteEventCrfLink(studySubject, crf, eventCrf, occurrenceKey, resword));
            }
        } else if (stage == DataEntryStage.LOCKED) {
            if (!hidden) {
                actions.add(viewSectionDataEntryLink(eventCrf, edc, resword));
                actions.add(printExistingCrfLink(studyBean, studySubject, studyEvent, eventCrf, crfVersionDAO, resword, contextPath, selectedSed));
            }
            if (studyAvailable && directorOrSysAdmin) {
                actions.add(removeEventCrfLink(studySubject, crf, eventCrf, occurrenceKey, resword));
            }
        } else if (stage == DataEntryStage.UNCOMPLETED) {
            if (studyEvent != null && !currentRole.isMonitor() && studyAvailable && !hidden) {
                int eventCrfId = eventCrf == null ? 0 : eventCrf.getId();
                actions.add(of("listEventsForSubject-enter-data-crf-" + studySubject.getId() + "-" + crf.getId() + "-" + occurrenceKey,
                    resword.getString("enter_data"),
                    url("InitialDataEntry").param("eventDefinitionCRFId", edc.getId()).param("studyEventId", studyEvent.getId())
                        .param("subjectId", studySubject.getSubjectId()).param("eventCRFId", eventCrfId).param("crfVersionId", edc.getDefaultVersionId())
                        .param("exitTo", "ListStudySubjects"),
                    "bt_Edit.gif", true));
            }
            if (!hidden) {
                actions.add(of(null, resword.getString("view"),
                    url("ViewSectionDataEntry").param("eventDefinitionCRFId", edc.getId()).param("crfVersionId", edc.getDefaultVersionId())
                        .param("tabId", 1).param("exitTo", "ListStudySubjects"),
                    "bt_View.gif", true));
                if (eventCrf == null) {
                    int sampleOrdinal = studyEvent == null ? 1 : studyEvent.getSampleOrdinal();
                    actions.add(ofRawHref(null, resword.getString("print"),
                        printHref(contextPath, studyBean, studySubject, selectedSed, sampleOrdinal, edc.getDefaultCRF().getOid()), "bt_Print.gif", true));
                } else {
                    actions.add(printExistingCrfLink(studyBean, studySubject, studyEvent, eventCrf, crfVersionDAO, resword, contextPath, selectedSed));
                }
            }
        } else if (stage == DataEntryStage.INVALID) {
            if (!hidden) {
                actions.add(viewSectionDataEntryLink(eventCrf, edc, resword));
                actions.add(printExistingCrfLink(studyBean, studySubject, studyEvent, eventCrf, crfVersionDAO, resword, contextPath, selectedSed));
            }
            if (studySubject.getStatus() != Status.DELETED && studySubject.getStatus() != Status.AUTO_DELETED && directorOrSysAdmin) {
                actions.add(of("listEventsForSubject-restore-crf-" + studySubject.getId() + "-" + crf.getId() + "-" + occurrenceKey,
                    resword.getString("restore"),
                    url("RestoreEventCRF").param("action", "confirm").param("id", eventCrf.getId()).param("studySubId", studySubject.getId()),
                    "bt_Restore.gif", true));
            }
        } else {
            // INITIAL_DATA_ENTRY / INITIAL_DATA_ENTRY_COMPLETE / DOUBLE_DATA_ENTRY
            if (!currentRole.isMonitor() && studyAvailable && !hidden) {
                if (stage == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE || stage == DataEntryStage.DOUBLE_DATA_ENTRY) {
                    actions.add(of("listEventsForSubject-double-data-entry-crf-" + eventCrf.getStudySubjectId() + "-" + crf.getId() + "-" + occurrenceKey,
                        resword.getString("enter_data"),
                        url("DoubleDataEntry").param("eventCRFId", eventCrf.getId()).param("exitTo", "ListStudySubjects"), "bt_Edit.gif", true));
                } else {
                    actions.add(of("listEventsForSubject-enter-data-crf-" + eventCrf.getStudySubjectId() + "-" + crf.getId() + "-" + occurrenceKey,
                        resword.getString("enter_data"),
                        url("InitialDataEntry").param("eventCRFId", eventCrf.getId()).param("exitTo", "ListStudySubjects"), "bt_Edit.gif", true));
                }
            }
            if (!hidden) {
                actions.add(viewSectionDataEntryLink(eventCrf, edc, resword));
                actions.add(printExistingCrfLink(studyBean, studySubject, studyEvent, eventCrf, crfVersionDAO, resword, contextPath, selectedSed));
            }
            if (studyAvailable && directorOrSysAdmin) {
                actions.add(removeEventCrfLink(studySubject, crf, eventCrf, occurrenceKey, resword));
            }
            if (studyAvailable && currentUser.isSysAdmin()) {
                actions.add(deleteEventCrfLink(studySubject, crf, eventCrf, occurrenceKey, resword));
            }
        }
        return actions;
    }

    private static PopupAction viewSectionDataEntryLink(EventCRFBean eventCrf, EventDefinitionCRFBean edc, ResourceBundle resword) {
        return of(null, resword.getString("view"),
            url("ViewSectionDataEntry").param("eventDefinitionCRFId", edc.getId()).param("ecId", eventCrf.getId())
                .param("tabId", 1).param("exitTo", "ListStudySubjects"),
            "bt_View.gif", true);
    }

    private static PopupAction removeEventCrfLink(StudySubjectBean studySubject, CRFBean crf, EventCRFBean eventCrf, int occurrenceKey,
            ResourceBundle resword) {
        return of("listEventsForSubject-remove-crf-" + studySubject.getId() + "-" + crf.getId() + "-" + occurrenceKey, resword.getString("remove"),
            url("RemoveEventCRF").param("action", "confirm").param("id", eventCrf.getId()).param("studySubId", studySubject.getId()),
            "bt_Remove.gif", true);
    }

    private static PopupAction deleteEventCrfLink(StudySubjectBean studySubject, CRFBean crf, EventCRFBean eventCrf, int occurrenceKey,
            ResourceBundle resword) {
        return of("listEventsForSubject-delete-crf-" + studySubject.getId() + "-" + crf.getId() + "-" + occurrenceKey, resword.getString("delete"),
            url("DeleteEventCRF").param("action", "confirm").param("ssId", studySubject.getId()).param("ecId", eventCrf.getId()),
            "bt_Delete.gif", true);
    }

    /** REST print link for an existing event CRF (mirrors {@code EventCrfLayerBuilder.printDataEntry(...)}). */
    private static PopupAction printExistingCrfLink(StudyBean studyBean, StudySubjectBean studySubject, StudyEventBean studyEvent,
            EventCRFBean eventCrf, CRFVersionDAO crfVersionDAO, ResourceBundle resword, String contextPath, StudyEventDefinitionBean selectedSed) {
        CRFVersionBean crfVersion = crfVersionDAO.findByPK(eventCrf.getCRFVersionId());
        int sampleOrdinal = studyEvent == null ? 1 : studyEvent.getSampleOrdinal();
        String href = printHref(contextPath, studyBean, studySubject, selectedSed, sampleOrdinal, crfVersion.getOid());
        return ofRawHref(null, resword.getString("print"), href, "bt_Print.gif", true);
    }

    private static String printHref(String contextPath, StudyBean studyBean, StudySubjectBean studySubject, StudyEventDefinitionBean sed,
            int sampleOrdinal, String crfVersionOid) {
        return contextPath + "/rest/clinicaldata/html/print/" + studyBean.getOid() + "/" + studySubject.getOid() + "/" + sed.getOid() + "%5b"
            + sampleOrdinal + "%5d/" + crfVersionOid;
    }
}

