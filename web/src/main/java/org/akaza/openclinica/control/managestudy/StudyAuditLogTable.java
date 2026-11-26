/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogFilter;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogSort;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.*;
import org.xmlet.htmlapifaster.Td;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.enumCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableFilterDef.textFilter;
import static org.akaza.openclinica.lctable.SafeUrl.url;
import static org.akaza.openclinica.lctable.LCTableText.key;

/**
 * LCTable-based study-subject audit-log index served by {@link StudyAuditLogServlet}.
 *
 * <p>The legacy DAO filter/sort classes and per-row DAO lookups are intentionally retained so this
 * migration changes only the table UI. The sole data-ordering change is a deterministic default sort
 * by study-subject label.</p>
 */
public class StudyAuditLogTable {

    private static final String TABLE_NAME = "studyAuditLogs";
    private static final String DOB_FILTER_PATTERN = "(?:\\d{4}|(?:0[1-9]|[12]\\d|3[01])-[A-Za-z]{3}-\\d{4})";

    private final StudySubjectDAO studySubjectDao;
    private final SubjectDAO subjectDao;
    private final UserAccountDAO userAccountDao;
    private final StudyBean currentStudy;
    private final Locale locale;
    private final ResourceBundle resword;
    private final ResourceBundle resformat;
    private final LCTable<StudyAuditLogRow> table;

    public StudyAuditLogTable(StudySubjectDAO studySubjectDao, SubjectDAO subjectDao,
            UserAccountDAO userAccountDao, StudyBean currentStudy, Locale locale) {
        this.studySubjectDao = studySubjectDao;
        this.subjectDao = subjectDao;
        this.userAccountDao = userAccountDao;
        this.currentStudy = currentStudy;
        this.locale = locale;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.resformat = ResourceBundleProvider.getFormatBundle(locale);
        this.table = new LCTable<>(TABLE_NAME, buildColumns(), this::fetchData)
            .setRowTestAttributes(row -> Map.of("subject", row.studySubject.getLabel()));
    }

    private static String actionId(String action, StudyAuditLogRow row) {
        return TABLE_NAME + "-" + action + "-" + row.studySubject.getId();
    }

    private List<LCTableColumnDef<StudyAuditLogRow>> buildColumns() {
        return Arrays.asList(
            textCol("studySubject.label",          key("study_subject_ID"),     "study-subject-id",     0, textFilter(), row -> row.studySubject.getLabel()),
            textCol("studySubject.secondaryLabel", key("secondary_subject_ID"), "secondary-subject-id", 0, textFilter(), row -> row.studySubject.getSecondaryLabel()),
            textCol("studySubject.oid",            key("study_subject_oid"),    "study-subject-oid",    0, textFilter(), row -> row.studySubject.getOid()),
            textCol("subject.dateOfBirth",         key("date_of_birth"),        "date-of-birth",        0,
                textFilter(DOB_FILTER_PATTERN, LCTableText.formattedKey("lctable_year_or_date_filter_message", getDateFormat())),
                row -> resolveBirthDay(row.subject.getDateOfBirth(), row.subject.isDobCollected())
            ),
            textCol("subject.uniqueIdentifier",    key("person_ID"),            "person-id",            0, textFilter(), row -> row.subject.getUniqueIdentifier()),
            textCol("studySubject.owner",          key("created_by"),           "created-by",           0, textFilter(), row -> row.owner, UserAccountBean::getName),
            enumCol("studySubject.status",         key("status"),               "status",               0,
                row -> row.studySubject.getStatus(), Status.toActiveArrayList(), Status::getName,
                status -> Integer.toString(status.getId())
            ),
            customTdCol("actions",                 key("actions"),              "actions",              0, NOT_SORTABLE, clearFilter(), this::renderActionsCell)
        );
    }

    private void renderActionsCell(Td<?> td, StudyAuditLogRow row) {
        SafeUrl href = url("ViewStudySubjectAuditLog").param("id", row.studySubject.getId());
        td.of(LCTableUtil.actionLink(actionId("view", row), resword.getString("view"),
            "javascript:openDocWindow('" + href.toUriString() + "')", "bt_View.gif", "view"));
    }

    private LCTableData<StudyAuditLogRow> fetchData(LCTableParams params) {
        StudyAuditLogFilter filter = new StudyAuditLogFilter(getDateFormat());
        params.filters.forEach(filter::addFilter);

        boolean noSort = params.sortProp == null || params.sortProp.isEmpty();
        StudyAuditLogSort sort = new StudyAuditLogSort();
        sort.addSort(noSort ? "studySubject.label" : params.sortProp, noSort ? "asc" : params.sortDir);

        int rowStart = params.page * params.maxRows;
        int rowEnd = rowStart + params.maxRows;
        Collection<StudySubjectBean> studySubjects = studySubjectDao
            .getWithFilterAndSort(currentStudy, filter, sort, rowStart, rowEnd);

        List<StudyAuditLogRow> rows = new ArrayList<>();
        for (StudySubjectBean studySubject : studySubjects) {
            SubjectBean subject = subjectDao.findByPK(studySubject.getSubjectId());
            UserAccountBean owner = userAccountDao.findByPK(studySubject.getOwnerId());
            rows.add(new StudyAuditLogRow(studySubject, subject, owner));
        }

        int total = studySubjectDao.getCountWithFilter(filter, currentStudy);
        return new LCTableData<>(rows, total);
    }

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    private String resolveBirthDay(Date birthDate, boolean dobCollected) {
        if (birthDate == null) {
            return "";
        }
        if (dobCollected) {
            return I18nFormatUtil.getDateFormat(locale).format(birthDate);
        }
        Calendar calendar = Calendar.getInstance(locale);
        calendar.setTime(birthDate);
        return Integer.toString(calendar.get(Calendar.YEAR));
    }

    public String render(HttpServletRequest request) {
        LCTableParams params = new LCTableParams(request.getQueryString(), table);
        return table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }

    private static final class StudyAuditLogRow {
        private final StudySubjectBean studySubject;
        private final SubjectBean subject;
        private final UserAccountBean owner;

        private StudyAuditLogRow(StudySubjectBean studySubject, SubjectBean subject, UserAccountBean owner) {
            this.studySubject = studySubject;
            this.subject = subject;
            this.owner = owner;
        }
    }
}
