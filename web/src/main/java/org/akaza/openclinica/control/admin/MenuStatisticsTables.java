/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableInMemoryDataSource;
import org.akaza.openclinica.lctable.LCTableParams;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.akaza.openclinica.lctable.LCTable.FooterMode.HIDE_FOOTER;
import static org.akaza.openclinica.lctable.LCTable.PaginationMode.UNPAGINATED;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableText.key;

/** The four small, unpaginated study statistics tables on the main menu. */
public final class MenuStatisticsTables {
    private final StudyBean currentStudy;
    private final StudyDAO studyDao;
    private final StudySubjectDAO subjectDao;
    private final StudyEventDAO eventDao;

    public MenuStatisticsTables(StudyBean currentStudy, StudyDAO studyDao, StudySubjectDAO subjectDao, StudyEventDAO eventDao) {
        this.currentStudy = currentStudy;
        this.studyDao = studyDao;
        this.subjectDao = subjectDao;
        this.eventDao = eventDao;
    }

    public void populate(HttpServletRequest request) {
        Locale locale = LocaleResolver.getLocale(request);
        request.setAttribute("studySiteStatistics", render("studySiteStatistics", "subject_enrollment",
            enrollmentColumns("site"), siteRows(), request, locale));
        request.setAttribute("subjectEventStatusStatistics", render("subjectEventStatusStatistics", "study_progress",
            statusColumns("event_status", "n_events", "number-of-events"), eventRows(), request, locale));
        request.setAttribute("studySubjectStatusStatistics", render("studySubjectStatusStatistics", "subject_status_count",
            statusColumns("study_subject_status", "n_study_subjects", "number-of-study-subjects"), subjectRows(), request, locale));
        if (currentStudy.getParentStudyId() == 0) {
            request.setAttribute("studyStatistics", render("studyStatistics", "subject_enrollment_for_study",
                enrollmentColumns("study"), List.of(enrollment(currentStudy,
                    subjectDao.getCountofStudySubjectsAtStudy(currentStudy))), request, locale));
        }
    }

    private List<EnrollmentRow> siteRows() {
        List<EnrollmentRow> rows = new ArrayList<>();
        for (StudyBean study : studyDao.findAll(currentStudy.getId())) {
            rows.add(enrollment(study, subjectDao.getCountofStudySubjectsAtStudyOrSite(study)));
        }
        return rows;
    }

    private List<StatusRow> subjectRows() {
        int total = subjectDao.getCountofStudySubjects(currentStudy);
        List<StatusRow> rows = new ArrayList<>();
        for (Status status : new Status[]{Status.AVAILABLE, Status.SIGNED, Status.DELETED}) {
            int count = subjectDao.getCountofStudySubjectsBasedOnStatus(currentStudy, status);
            rows.add(new StatusRow(status.getName(), count, percentage(count, total)));
        }
        return rows;
    }

    private List<StatusRow> eventRows() {
        int total = eventDao.getCountofEvents(currentStudy);
        List<StatusRow> rows = new ArrayList<>();
        for (SubjectEventStatus status : new SubjectEventStatus[]{SubjectEventStatus.SCHEDULED,
                SubjectEventStatus.DATA_ENTRY_STARTED, SubjectEventStatus.COMPLETED, SubjectEventStatus.SIGNED,
                SubjectEventStatus.LOCKED, SubjectEventStatus.SKIPPED, SubjectEventStatus.STOPPED}) {
            int count = eventDao.getCountofEventsBasedOnEventStatus(currentStudy, status);
            rows.add(new StatusRow(status.getName(), count, percentage(count, total)));
        }
        return rows;
    }

    private static EnrollmentRow enrollment(StudyBean study, int enrolled) {
        int expected = study.getExpectedTotalEnrollment();
        return new EnrollmentRow(study.getName(), enrolled, expected, percentage(enrolled, expected));
    }

    private static long percentage(int numerator, int denominator) {
        return denominator == 0 ? 0 : Math.round((numerator / (double) denominator) * 100);
    }

    private static List<LCTableColumnDef<EnrollmentRow>> enrollmentColumns(String firstHeader) {
        return List.of(
            textCol("name", key(firstHeader), firstHeader, 6, VISIBLE, NOT_SORTABLE, NO_FILTER, EnrollmentRow::name),
            textCol("enrolled", key("enrolled"), "enrolled", 3, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> String.valueOf(row.enrolled())),
            textCol("expectedTotalEnrollment", key("expected_enrollment"), "expected-enrollment", 7,
                VISIBLE, NOT_SORTABLE, NO_FILTER, row -> String.valueOf(row.expected())),
            percentageColumn());
    }

    private static List<LCTableColumnDef<StatusRow>> statusColumns(String firstHeader, String countHeader, String countTestName) {
        return List.of(
            textCol("status", key(firstHeader), firstHeader.replace('_', '-'), 8.3, VISIBLE, NOT_SORTABLE, NO_FILTER, StatusRow::status),
            textCol("count", key(countHeader), countTestName, 8, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> String.valueOf(row.count())),
            percentageColumn());
    }

    private static <T extends PercentageRow> LCTableColumnDef<T> percentageColumn() {
        // Both three- and four-column layouts target roughly 23rem overall (before borders/padding).
        return customTdCol("percentage", key("percentage"), "percentage", 7, NOT_SORTABLE, NO_FILTER,
            (td, row) -> td.div().attrClass("graph").div().attrClass("bar")
                .attrStyle("width: " + Math.max(0, Math.min(100, row.percentage())) + "%")
                .text(row.percentage() + "%").__().__());
    }

    private static <T> String render(String id, String title, List<LCTableColumnDef<T>> columns, List<T> rows,
            HttpServletRequest request, Locale locale) {
        LCTable<T> table = new LCTable<>(id, columns, LCTableInMemoryDataSource.allRows(rows, List.of()))
            .setTitle(key(title)).setPaginationMode(UNPAGINATED).setFooterMode(HIDE_FOOTER);
        // These widgets intentionally have no URL state until multi-table parameter names are settled.
        return table.render(request.getContextPath() + "/MainMenu",
            new LCTableParams(new org.springframework.util.LinkedMultiValueMap<>(), table), request.getContextPath(), locale);
    }

    private interface PercentageRow { long percentage(); }
    private static final class EnrollmentRow implements PercentageRow {
        private final String name;
        private final int enrolled;
        private final int expected;
        private final long percentage;

        private EnrollmentRow(String name, int enrolled, int expected, long percentage) {
            this.name = name;
            this.enrolled = enrolled;
            this.expected = expected;
            this.percentage = percentage;
        }

        String name() { return name; }
        int enrolled() { return enrolled; }
        int expected() { return expected; }
        public long percentage() { return percentage; }
    }

    private static final class StatusRow implements PercentageRow {
        private final String status;
        private final int count;
        private final long percentage;

        private StatusRow(String status, int count, long percentage) {
            this.status = status;
            this.count = count;
            this.percentage = percentage;
        }

        String status() { return status; }
        int count() { return count; }
        public long percentage() { return percentage; }
    }
}
