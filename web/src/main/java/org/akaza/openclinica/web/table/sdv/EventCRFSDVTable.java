/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.web.table.sdv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.EventCRFSDVFilter;
import org.akaza.openclinica.dao.EventCRFSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableFilterDef;
import org.akaza.openclinica.lctable.LCTableParams;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdColWithContext;
import static org.akaza.openclinica.lctable.LCTableColumnDef.enumColNotSortable;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textColHidden;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.LCTableText.literal;

/** Study-wide Event CRF SDV table. One instance belongs to one study and one request locale. */
public final class EventCRFSDVTable {
    private static final Set<String> TEXT_FILTERS = Set.of("studySubjectId", "studyIdentifier", "eventName");
    private static final Set<String> SELECT_FILTERS = Set.of("sdvStatus", "crfStatus", "sdvRequirementDefinition");

    private final EventCRFDAO eventCrfDao;
    private final StudySubjectDAO studySubjectDao;
    private final StudyEventDAO studyEventDao;
    private final StudyEventDefinitionDAO eventDefinitionDao;
    private final SubjectDAO subjectDao;
    private final StudyDAO studyDao;
    private final EventDefinitionCRFDAO eventDefinitionCrfDao;
    private final CRFVersionDAO versionDao;
    private final CRFDAO crfDao;
    private final int studyId;
    private final Locale locale;
    private final LCTable<Row> table;

    public EventCRFSDVTable(EventCRFDAO eventCrfDao, StudySubjectDAO studySubjectDao, StudyEventDAO studyEventDao,
            StudyEventDefinitionDAO eventDefinitionDao, SubjectDAO subjectDao, StudyDAO studyDao,
            EventDefinitionCRFDAO eventDefinitionCrfDao, CRFVersionDAO versionDao, CRFDAO crfDao,
            int studyId, Locale locale) {
        this.eventCrfDao = eventCrfDao;
        this.studySubjectDao = studySubjectDao;
        this.studyEventDao = studyEventDao;
        this.eventDefinitionDao = eventDefinitionDao;
        this.subjectDao = subjectDao;
        this.studyDao = studyDao;
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
        this.versionDao = versionDao;
        this.crfDao = crfDao;
        this.studyId = studyId;
        this.locale = locale;
        this.table = new LCTable<>("sdv", columns(), this::fetch, List.of("studyId"));
    }

    /** Use a fresh instance per request; the surrounding page owns {@code sdvForm}. */
    public String render(HttpServletRequest request) {
        // Existing subject links and bookmarks use the jmesa filter name. Translate only this
        // known property, retaining the encoded value and all other query state unchanged.
        String query = request.getQueryString();
        if (query != null && (query.startsWith("sdv_f_studySubjectId=") || query.contains("&sdv_f_studySubjectId="))) {
            LCTableParams params = new LCTableParams(
                query.replaceAll("(^|&)sdv_f_studySubjectId=", "$1q.studySubjectId="), table);
            return table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
        }
        return table.render(request);
    }

    private List<LCTableColumnDef<Row>> columns() {
        ResourceBundle terms = ResourceBundle.getBundle("org.akaza.openclinica.i18n.terms", locale);
        String required = terms.getString(SourceDataVerification.AllREQUIRED.getDescription());
        String partial = terms.getString(SourceDataVerification.PARTIALREQUIRED.getDescription());
        String notRequired = terms.getString(SourceDataVerification.NOTREQUIRED.getDescription());
        List<String> requirements = List.of(required + " & " + partial, required, partial, notRequired);
        ResourceBundle words = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", locale);
        return Arrays.asList(
            customTdColWithContext("sdvStatus", key("SDV_status"), "sdv-status", 0, NOT_SORTABLE,
            new LCTableFilterDef.Select<>(List.of("complete", "none"), words::getString, v -> v),
                row -> row.crf, (td, row, ctx) -> {
                    if (row.crf.isSdvStatus()) {
                        if (!row.study.getStatus().isLocked()) {
                            // Unverification is handled by the page's existing confirmation function.
                            td.a().attrHref("#").addAttr("onclick", "prompt(document.sdvForm," + row.crf.getId() + ");return false;")
                                .img().attrSrc(ctx.resourcePath + "/images/icon_DoubleCheck.gif").attrAlt("SDV Complete").__().__();
                        } else {
                            td.img().attrSrc(ctx.resourcePath + "/images/icon_DoubleCheck.gif")
                                .attrAlt("SDV Complete").__();
                        }
                    } else if (!row.study.getStatus().isLocked()) {
                        td.input().attrType(org.xmlet.htmlapifaster.EnumTypeInputType.CHECKBOX)
                            .attrClass("sdvCheck").attrName("sdvCheck_" + row.crf.getId())
                            .addAttr("form", "sdvForm").__();
                    }
                }),
            textCol("studySubjectId", key("study_subject_ID"), "study-subject-id", 0, VISIBLE, NOT_SORTABLE,
                new LCTableFilterDef.Text(), row -> row.studySubject.getLabel()),
            textCol("studyIdentifier", key("site_id"), "site-id", 0, VISIBLE, NOT_SORTABLE,
                new LCTableFilterDef.Text(), row -> row.study.getIdentifier()),
            textColHidden("personId", key("person_ID"), "person-id", 0, NOT_SORTABLE, NO_FILTER,
                row -> row.subject == null ? "" : row.subject.getUniqueIdentifier()),
            textColHidden("secondaryId", key("secondary_ID"), "secondary-id", 0, NOT_SORTABLE, NO_FILTER, row -> row.studySubject.getSecondaryLabel()),
            textCol("eventName", key("event_name"), "event-name", 0, VISIBLE, NOT_SORTABLE,
                new LCTableFilterDef.Text(), row -> row.eventName),
            textCol("eventDate", key("event_date"), "event-date", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> date(row.event.getDateStarted())),
            textColHidden("enrollmentDate", key("enrollment_date"), "enrollment-date", 0, NOT_SORTABLE, NO_FILTER, row -> date(row.studySubject.getEnrollmentDate())),
            textColHidden("studySubjectStatus", key("subject_status"), "subject-status", 0, NOT_SORTABLE, NO_FILTER, row -> row.studySubject.getStatus().getName()),
            textCol("crfNameVersion", literal(org.akaza.openclinica.i18n.util.ResourceBundleProvider.getWordsBundle(locale).getString("CRF_name")
                + " / " + org.akaza.openclinica.i18n.util.ResourceBundleProvider.getWordsBundle(locale).getString("version")),
                "crf-name-version", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.crfNameVersion),
            enumColNotSortable("sdvRequirementDefinition", key("SDV_requirement"), "sdv-requirement", 0,
                requirements, v -> v, v -> v, row -> row.requirement),
            customTdColWithContext("crfStatus", key("CRF_status"), "crf-status", 0, NOT_SORTABLE,
                new LCTableFilterDef.Select<>(List.of("Completed", "Locked"), v -> v),
                row -> row.crf, (td, row, ctx) -> {
                    int stage = row.crf.getStage() == null ? 0 : row.crf.getStage().getId();
                    if (row.event.getSubjectEventStatus() == SubjectEventStatus.LOCKED
                            || row.event.getSubjectEventStatus() == SubjectEventStatus.STOPPED
                            || row.event.getSubjectEventStatus() == SubjectEventStatus.SKIPPED) {
                        stage = DataEntryStage.LOCKED.getId();
                    }
                    String[] icons = {"Invalid", "NotStarted", "InitialDE", "InitialDEComplete", "DDE", "DEcomplete", "InitialDE", "Locked"};
                    String image = icons[stage >= 0 && stage < icons.length ? stage : 0];
                    td.a().attrHref(ctx.resourcePath + "/ViewSectionDataEntry?eventDefinitionCRFId=" + row.crf.getId()
                        + "&crfVersionId=" + row.crf.getCRFVersionId() + "&tabId=1&studySubjectId=" + row.studySubject.getId())
                        .img().attrSrc(ctx.resourcePath + "/images/icon_" + image + ".gif")
                        .attrAlt("Event CRF Status").__().__();
                }),
            textColHidden("lastUpdatedDate", key("last_updated_date"), "last-updated-date", 0, NOT_SORTABLE, NO_FILTER, row -> date(row.crf.getUpdatedDate())),
            textColHidden("lastUpdatedBy", key("last_updated_by"), "last-updated-by", 0, NOT_SORTABLE, NO_FILTER,
                row -> row.crf.getUpdater() == null ? "" : row.crf.getUpdater().getFirstName() + " " + row.crf.getUpdater().getLastName()),
            textColHidden("studyEventStatus", key("study_event_status"), "study-event-status", 0, NOT_SORTABLE, NO_FILTER, row -> row.event.getStatus().getName()),
            customTdColWithContext("sdvStatusActions", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(),
                row -> row.crf, (td, row, ctx) -> {
                    if (!row.crf.isSdvStatus() && !row.study.getStatus().isLocked()) {
                        td.button().attrType(org.xmlet.htmlapifaster.EnumTypeButtonType.SUBMIT)
                            .attrClass("button_medium").addAttr("onclick", "document.sdvForm.crfId.value=" + row.crf.getId() + ";")
                            .addAttr("form", "sdvForm").addAttr("formmethod", "get")
                            .addAttr("formaction", ctx.resourcePath + "/pages/handleSDVGet")
                            .text("SDV").__();
                    }
                })
        );
    }

    private String date(Date value) {
        return value == null ? "" : I18nFormatUtil.getDateFormat(locale).format(value);
    }

    private LCTableData<Row> fetch(LCTableParams params) {
        EventCRFSDVFilter filter = new EventCRFSDVFilter(studyId);
        params.filters.forEach((name, value) -> {
            if (TEXT_FILTERS.contains(name)) {
                filter.addFilter(name, value.trim());
            } else if (SELECT_FILTERS.contains(name) && validSelect(name, value)) {
                filter.addFilter(name, value);
            }
        });
        // The current DAO ignores its sort argument and always orders by ec.date_created ASC.
        EventCRFSDVSort sort = new EventCRFSDVSort();
        int count = eventCrfDao.getCountWithFilter(studyId, studyId, filter);
        int size = params.maxRows;
        int start = (int) Math.min((long) params.page * size, Integer.MAX_VALUE - size);
        List<Row> rows = new ArrayList<>();
        for (EventCRFBean crf : eventCrfDao.getWithFilterAndSort(studyId, studyId, filter, sort, start, start + size)) {
            rows.add(toRow(crf));
        }
        return new LCTableData<>(rows, count);
    }

    private boolean validSelect(String name, String value) {
        if ("sdvStatus".equals(name)) return "complete".equals(value) || "none".equals(value);
        if ("crfStatus".equals(name)) return "Completed".equals(value) || "Locked".equals(value);
        ResourceBundle terms = ResourceBundle.getBundle("org.akaza.openclinica.i18n.terms", locale);
        String required = terms.getString(SourceDataVerification.AllREQUIRED.getDescription());
        String partial = terms.getString(SourceDataVerification.PARTIALREQUIRED.getDescription());
        return (required + " & " + partial).equals(value) || required.equals(value) || partial.equals(value)
            || terms.getString(SourceDataVerification.NOTREQUIRED.getDescription()).equals(value);
    }

    private Row toRow(EventCRFBean crf) {
        StudySubjectBean studySubject = (StudySubjectBean) studySubjectDao.findByPK(crf.getStudySubjectId());
        StudyEventBean event = (StudyEventBean) studyEventDao.findByPK(crf.getStudyEventId());
        StudyBean study = (StudyBean) studyDao.findByPK(studySubject.getStudyId());
        SubjectBean subject = (SubjectBean) subjectDao.findByPK(studySubject.getSubjectId());
        StudyEventDefinitionBean definition = (StudyEventDefinitionBean) eventDefinitionDao.findByPK(event.getStudyEventDefinitionId());
        String eventName = crf.getEventName();
        if (eventName == null || eventName.isEmpty()) eventName = definition.getName() + "(" + event.getSampleOrdinal() + ")";
        CRFVersionBean version = (CRFVersionBean) versionDao.findByPK(crf.getCRFVersionId());
        CRFBean crfDefinition = (CRFBean) crfDao.findByPK(version.getCrfId());
        EventDefinitionCRFBean edc = eventDefinitionCrfDao.findByStudyEventIdAndCRFVersionId(study, event.getId(), crf.getCRFVersionId());
        SourceDataVerification requirement = edc == null ? null : edc.getSourceDataVerification();
        ResourceBundle terms = ResourceBundle.getBundle("org.akaza.openclinica.i18n.terms", locale);
        return new Row(crf, studySubject, event, study, subject, eventName,
            crfDefinition.getName() + "/ " + version.getName(),
            requirement == null ? "" : terms.getString(requirement.getDescription()));
    }

    private static final class Row {
        final EventCRFBean crf;
        final StudySubjectBean studySubject;
        final StudyEventBean event;
        final StudyBean study;
        final SubjectBean subject;
        final String eventName;
        final String crfNameVersion;
        final String requirement;

        Row(EventCRFBean crf, StudySubjectBean studySubject, StudyEventBean event, StudyBean study,
                SubjectBean subject, String eventName, String crfNameVersion, String requirement) {
            this.crf = crf;
            this.studySubject = studySubject;
            this.event = event;
            this.study = study;
            this.subject = subject;
            this.eventName = eventName;
            this.crfNameVersion = crfNameVersion;
            this.requirement = requirement;
        }
    }
}