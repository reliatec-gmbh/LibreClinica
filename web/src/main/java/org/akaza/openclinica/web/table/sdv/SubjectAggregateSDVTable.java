/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.web.table.sdv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.StudySubjectSDVFilter;
import org.akaza.openclinica.dao.StudySubjectSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableFilterDef;
import org.akaza.openclinica.lctable.LCTableParams;
import org.xmlet.htmlapifaster.EnumTypeButtonType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdColWithContext;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textColHidden;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;

/** A request-scoped, DAO-paged replacement candidate for the subject aggregate {@code s_sdv} table. */
public final class SubjectAggregateSDVTable {
    private static final Set<String> TEXT_FILTERS = Set.of("studySubjectId", "siteId");

    private final StudySubjectDAO studySubjects;
    private final EventCRFDAO eventCrfs;
    private final StudyDAO studies;
    private final StudyGroupDAO groups;
    private final StudyEventDAO events;
    private final EventDefinitionCRFDAO definitions;
    private final CRFDAO crfs;
    private final int studyId;
    private final Locale locale;
    private final LCTable<Row> table;

    public SubjectAggregateSDVTable(StudySubjectDAO studySubjects, EventCRFDAO eventCrfs, StudyDAO studies,
            StudyGroupDAO groups, StudyEventDAO events, EventDefinitionCRFDAO definitions, CRFDAO crfs,
            int studyId, Locale locale) {
        this.studySubjects = studySubjects;
        this.eventCrfs = eventCrfs;
        this.studies = studies;
        this.groups = groups;
        this.events = events;
        this.definitions = definitions;
        this.crfs = crfs;
        this.studyId = studyId;
        this.locale = locale;
        this.table = new LCTable<>("s_sdv", columns(), this::fetch, List.of("studyId"));
    }

    /** The containing page owns {@code sdvForm}; do not nest this table in a form. */
    public String render(HttpServletRequest request) {
        return table.render(request);
    }

    private List<LCTableColumnDef<Row>> columns() {
        ResourceBundle words = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", locale);
        return Arrays.asList(
            customTdColWithContext("sdvStatus", key("SDV_status"), "sdv-status", 0, NOT_SORTABLE,
                new LCTableFilterDef.Select<>(List.of("complete", "none"), words::getString, v -> v), row -> row.subject,
                (td, row, ctx) -> {
                    if (!row.eligible) return;
                    if (row.verified) {
                        if (row.study.getStatus().isLocked()) {
                            td.img().attrSrc(ctx.resourcePath + "/images/icon_DoubleCheck.gif").attrAlt("SDV Complete").__();
                        } else {
                            td.a().attrHref("#").addAttr("onclick", "prompt(document.sdvForm," + row.subject.getId() + ");return false;")
                                .img().attrSrc(ctx.resourcePath + "/images/icon_DoubleCheck.gif").attrAlt("SDV Complete").__().__();
                        }
                    } else if (!row.study.getStatus().isLocked()) {
                        td.input().attrType(EnumTypeInputType.CHECKBOX).attrClass("sdvCheck")
                            .attrName("sdvCheck_" + row.subject.getId()).addAttr("form", "sdvForm").__();
                    }
                }),
            textCol("studySubjectId", key("study_subject_ID"), "study-subject-id", 0, VISIBLE, SORTABLE,
                new LCTableFilterDef.Text(), row -> row.subject.getLabel()),
            textColHidden("siteId", key("site_id"), "site-id", 0, NOT_SORTABLE,
                new LCTableFilterDef.Text(), row -> row.study.getIdentifier()),
            textColHidden("personId", key("person_ID"), "person-id", 0, NOT_SORTABLE, NO_FILTER,
                row -> row.subject.getUniqueIdentifier()),
            textColHidden("studySubjectStatus", key("study_subject_status"), "subject-status", 0, NOT_SORTABLE, NO_FILTER,
                row -> row.subject.getStatus().getName()),
            textCol("group", key("group"), "group", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.group),
            textCol("numberCRFComplete", key("num_CRFs_completed"), "completed-count", 0, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> Integer.toString(row.completed)),
            textCol("numberOfCRFsSDV", key("num_CRFs_SDV"), "verified-count", 0, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> Integer.toString(row.sdvCount)),
            textCol("totalEventCRF", key("total_events_CRF"), "total-count", 0, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> Integer.toString(row.total)),
            customTdColWithContext("actions", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(),
                row -> row.subject, (td, row, ctx) -> {
                    // Filter the Event-CRF table using its LCTable query parameter, not Jmesa's sdv_f_*.
                    String label = org.springframework.web.util.UriUtils.encodeQueryParam(row.subject.getLabel(), java.nio.charset.StandardCharsets.UTF_8);
                    td.a().attrHref(ctx.resourcePath + "/pages/viewAllSubjectSDVtmp?studyId=" + studyId
                        + "&q.studySubjectId=" + label)
                        .img().attrSrc(ctx.resourcePath + "/images/bt_View.gif").attrAlt("View").__().__();
                    if (!row.verified && row.eligible && !row.study.getStatus().isLocked()) {
                        td.button().attrType(EnumTypeButtonType.SUBMIT).attrClass("button")
                            .addAttr("form", "sdvForm").addAttr("formmethod", "get")
                            .addAttr("formaction", ctx.resourcePath + "/pages/sdvStudySubject")
                            .addAttr("onclick", "document.sdvForm.theStudySubjectId.value=" + row.subject.getId() + ";")
                            .text("SDV").__();
                    } else if (!row.verified && !row.eligible) {
                        td.text("SDV N/A");
                    }
                })
        );
    }

    private LCTableData<Row> fetch(LCTableParams params) {
        StudySubjectSDVFilter filter = new StudySubjectSDVFilter();
        params.filters.forEach((name, value) -> {
            if (TEXT_FILTERS.contains(name) && !value.trim().isEmpty()) filter.addFilter(name, value.trim());
            if ("sdvStatus".equals(name) && ("complete".equals(value) || "none".equals(value))) filter.addFilter(name, value);
        });
        // The stock sort maps to ss.*, but findAllByStudySDV actually aliases these tables mss/mst.
        StudySubjectSDVSort sort = new AggregateSort(params.sortProp, params.sortDir);
        int count = studySubjects.countAllByStudySDV(studyId, studyId, filter);
        int size = params.maxRows;
        int start = (int) Math.min((long) params.page * size, Integer.MAX_VALUE - size);
        List<Row> rows = new ArrayList<>();
        for (StudySubjectBean subject : studySubjects.findAllByStudySDV(studyId, studyId, filter, sort, start, start + size)) {
            rows.add(toRow(subject));
        }
        return new LCTableData<>(rows, count);
    }

    private Row toRow(StudySubjectBean subject) {
        StudyBean study = (StudyBean) studies.findByPK(subject.getStudyId()); // legacy EntityDAO returns EntityBean
        List<EventCRFBean> subjectCrfs = eventCrfs.getEventCRFsByStudySubject(subject.getId(), subject.getStudyId(), subject.getStudyId());
        int completed = 0;
        int verifiedCount = 0;
        boolean eligible = false;
        boolean unverifiedRequired = false;
        for (EventCRFBean eventCrf : subjectCrfs) {
            boolean complete = eventCrf.getStatus() == Status.UNAVAILABLE || eventCrf.getStatus() == Status.LOCKED;
            if (complete) completed++;
            if (eventCrf.isSdvStatus()) verifiedCount++;
            StudyEventBean event = (StudyEventBean) events.findByPK(eventCrf.getStudyEventId()); // legacy DAO
            CRFBean crf = crfs.findByVersionId(eventCrf.getCRFVersionId());
            EventDefinitionCRFBean definition = definitions.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                event.getStudyEventDefinitionId(), crf.getId(), subject.getStudyId());
            if (definition.getId() == 0) {
                definition = definitions.findForStudyByStudyEventDefinitionIdAndCRFId(event.getStudyEventDefinitionId(), crf.getId());
            }
            SourceDataVerification requirement = definition.getSourceDataVerification();
            boolean required = requirement == SourceDataVerification.AllREQUIRED || requirement == SourceDataVerification.PARTIALREQUIRED;
            if (complete && required) {
                eligible = true;
                if (!eventCrf.isSdvStatus()) unverifiedRequired = true;
            }
        }
        List<StudyGroupBean> subjectGroups = groups.getGroupByStudySubject(subject.getId(), subject.getStudyId(), subject.getStudyId());
        String group = subjectGroups == null || subjectGroups.isEmpty() ? "" : subjectGroups.get(0).getName();
        int total = eventCrfs.countEventCRFsByStudySubject(subject.getId(), subject.getStudyId(), subject.getStudyId());
        // Legacy status is 'verified' when at least one required, completed CRF exists and none is unverified.
        return new Row(subject, study, group, completed, verifiedCount, total, eligible, eligible && !unverifiedRequired);
    }

    /** Only SQL fragments selected from constants are passed to the legacy DAO. */
    private static final class AggregateSort extends StudySubjectSDVSort {
        private final String clause;

        AggregateSort(String property, String direction) {
            String column = "studySubjectId".equals(property) ? "mss.label"
                : "siteId".equals(property) ? "mst.unique_identifier" : null;
            clause = column == null ? "" : " order by " + column + ("desc".equals(direction) ? " desc" : " asc");
        }

        @Override
        public String execute(String criteria) {
            return clause;
        }
    }

    private static final class Row {
        final StudySubjectBean subject;
        final StudyBean study;
        final String group;
        final int completed;
        final int sdvCount;
        final int total;
        final boolean eligible;
        final boolean verified;

        Row(StudySubjectBean subject, StudyBean study, String group, int completed, int sdvCount, int total,
                boolean eligible, boolean verified) {
            this.subject = subject;
            this.study = study;
            this.group = group;
            this.completed = completed;
            this.sdvCount = sdvCount;
            this.total = total;
            this.eligible = eligible;
            this.verified = verified;
        }
    }
}