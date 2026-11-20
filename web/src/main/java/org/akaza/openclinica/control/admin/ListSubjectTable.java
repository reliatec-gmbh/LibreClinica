/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.ListSubjectFilter;
import org.akaza.openclinica.dao.submit.ListSubjectSort;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableParams;
import org.akaza.openclinica.lctable.LCTableText;
import org.akaza.openclinica.lctable.LCTableUtil;
import org.xmlet.htmlapifaster.Td;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.enumCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableFilterDef.textFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/** LCTable-based system subject list served by {@link ListSubjectServlet}. */
public class ListSubjectTable {

    private static final String TABLE_NAME = "listSubjects";
    private static final String DATE_FILTER_PATTERN = "(?:\\d{4}|(?:0[1-9]|[12]\\d|3[01])-[A-Za-z]{3}-\\d{4})";
    private static final Set<String> FILTERABLE_COLUMNS = Set.of(
        "subject.uniqueIdentifier", "studySubjectIdAndStudy", "subject.gender",
        "subject.createdDate", "subject.updatedDate", "subject.status");
    private static final Set<String> SORTABLE_COLUMNS = Set.of(
        "subject.uniqueIdentifier", "subject.gender", "subject.createdDate",
        "subject.updatedDate", "subject.status");

    private final SubjectDAO subjectDao;
    private final StudySubjectDAO studySubjectDao;
    private final UserAccountDAO userAccountDao;
    private final StudyDAO studyDao;
    private final StudyBean currentStudy;
    private final Locale locale;
    private final ResourceBundle resword;
    private final ResourceBundle resformat;
    private final LCTable<ListSubjectRow> table;

    public ListSubjectTable(SubjectDAO subjectDao, StudySubjectDAO studySubjectDao,
            UserAccountDAO userAccountDao, StudyDAO studyDao, StudyBean currentStudy, Locale locale) {
        this.subjectDao = subjectDao;
        this.studySubjectDao = studySubjectDao;
        this.userAccountDao = userAccountDao;
        this.studyDao = studyDao;
        this.currentStudy = currentStudy;
        this.locale = locale;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.resformat = ResourceBundleProvider.getFormatBundle(locale);
        this.table = new LCTable<>(TABLE_NAME, buildColumns(), this::fetchData)
            .setRowTestAttributes(row -> Map.of("subject", row.subject.getUniqueIdentifier()));
    }

    private List<LCTableColumnDef<ListSubjectRow>> buildColumns() {
        return Arrays.asList(
            textCol("subject.uniqueIdentifier", key("person_ID"), "person-id", 0,
                row -> row.subject.getUniqueIdentifier()),
            textCol("studySubjectIdAndStudy", key("Protocol_Study_subject_IDs"), "protocol-study-subject-ids", 0,
                VISIBLE, NOT_SORTABLE, textFilter(), row -> row.studySubjectIdAndStudy),
            textCol("subject.gender", key("gender"), "sex", 0,
                row -> String.valueOf(row.subject.getGender())),
            textCol("subject.createdDate", key("date_created"), "date-created", 0,
                textFilter(DATE_FILTER_PATTERN, dateFilterMessage()), row -> formatDate(row.subject.getCreatedDate())),
            textCol("subject.owner", key("owner"), "owner", 0, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> row.owner, UserAccountBean::getName),
            textCol("subject.updatedDate", key("date_updated"), "date-updated", 0,
                textFilter(DATE_FILTER_PATTERN, dateFilterMessage()), row -> formatDate(row.subject.getUpdatedDate())),
            textCol("subject.updater", key("last_updated_by"), "last-updated-by", 0, VISIBLE, NOT_SORTABLE, NO_FILTER,
                row -> row.updater == null ? "" : row.updater.getName()),
            enumCol("subject.status", key("status"), "status", 0,
                row -> row.subject.getStatus(), Status.toSubjectDropDownArrayList(), Status::getName, Status::getName),
            customTdCol("actions", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(), this::renderActionsCell)
        );
    }

    private LCTableText dateFilterMessage() {
        return LCTableText.formattedKey("lctable_year_or_date_filter_message", getDateFormat());
    }

    private LCTableData<ListSubjectRow> fetchData(LCTableParams params) {
        ListSubjectFilter filter = new ListSubjectFilter(getDateFormat());
        params.filters.forEach((property, value) -> {
            if (FILTERABLE_COLUMNS.contains(property)
                    && (!"subject.status".equals(property) || isValidStatusFilter(value))) {
                filter.addFilter(property, value);
            }
        });

        ListSubjectSort sort = new ListSubjectSort();
        if (SORTABLE_COLUMNS.contains(params.sortProp)
                && ("asc".equals(params.sortDir) || "desc".equals(params.sortDir))) {
            sort.addSort(params.sortProp, params.sortDir);
        }

        int rowStart = params.page * params.maxRows;
        int rowEnd = rowStart + params.maxRows;
        Collection<SubjectBean> subjects = subjectDao
            .getWithFilterAndSort(currentStudy, filter, sort, rowStart, rowEnd);

        List<ListSubjectRow> rows = new ArrayList<>();
        for (SubjectBean subject : subjects) {
            UserAccountBean owner = userAccountDao.findByPK(subject.getOwnerId());
            UserAccountBean updater = subject.getUpdaterId() == 0 ? null : userAccountDao.findByPK(subject.getUpdaterId());
            rows.add(new ListSubjectRow(subject, owner, updater, resolveStudySubjectIds(subject)));
        }

        Integer count = subjectDao.getCountWithFilter(filter, currentStudy);
        return new LCTableData<>(rows, count == null ? 0 : count);
    }

    private boolean isValidStatusFilter(String value) {
        return Status.toSubjectDropDownArrayList().stream()
            .map(Status::getName)
            .anyMatch(value::equals);
    }

    private String resolveStudySubjectIds(SubjectBean subject) {
        StringBuilder value = new StringBuilder();
        List<StudySubjectBean> studySubjects = studySubjectDao.findAllBySubjectId(subject.getId());
        for (StudySubjectBean studySubject : studySubjects) {
            StudyBean study = studyDao.findByPK(studySubject.getStudyId());
            if (value.length() > 0) {
                value.append(',');
            }
            value.append(study.getIdentifier()).append('-').append(studySubject.getLabel());
        }
        return value.toString();
    }

    private void renderActionsCell(Td<?> td, ListSubjectRow row) {
        int subjectId = row.subject.getId();
        td.of(LCTableUtil.actionLink(actionId("view", subjectId), resword.getString("view"),
            url("ViewSubject").param("action", "show").param("id", subjectId), "bt_View.gif", "view"));
        if (row.subject.getStatus() != Status.DELETED) {
            td.of(LCTableUtil.actionLink(actionId("edit", subjectId), resword.getString("edit"),
                url("UpdateSubject").param("action", "show").param("id", subjectId), "bt_Edit.gif", "edit"));
            td.of(LCTableUtil.actionLink(actionId("remove", subjectId), resword.getString("remove"),
                url("RemoveSubject").param("action", "confirm").param("id", subjectId), "bt_Remove.gif", "remove"));
        } else {
            td.of(LCTableUtil.actionLink(actionId("restore", subjectId), resword.getString("restore"),
                url("RestoreSubject").param("action", "confirm").param("id", subjectId), "bt_Restore.gif", "restore"));
        }
    }

    private static String actionId(String action, int subjectId) {
        return TABLE_NAME + "-" + action + "-" + subjectId;
    }

    private String formatDate(Date date) {
        return date == null ? "" : I18nFormatUtil.getDateFormat(locale).format(date);
    }

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    public String render(HttpServletRequest request) {
        LCTableParams params = new LCTableParams(request.getQueryString(), table);
        return table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }

    private static final class ListSubjectRow {
        private final SubjectBean subject;
        private final UserAccountBean owner;
        private final UserAccountBean updater;
        private final String studySubjectIdAndStudy;

        private ListSubjectRow(SubjectBean subject, UserAccountBean owner, UserAccountBean updater,
                String studySubjectIdAndStudy) {
            this.subject = subject;
            this.owner = owner;
            this.updater = updater;
            this.studySubjectIdAndStudy = studySubjectIdAndStudy;
        }
    }
}