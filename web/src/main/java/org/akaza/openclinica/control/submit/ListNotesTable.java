/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.*;
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesService;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.Td;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableFilterDef.*;
import static org.akaza.openclinica.lctable.SafeUrl.url;
import static org.akaza.openclinica.lctable.LCTableText.key;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * LCTable-based "listNotes" table ("Notes and Discrepancies" page, reachable via {@code ViewNotes}).
 *
 * <p>Columns are fixed, but a new instance is created per request to handle request-scoped
 * variables ({@code discNoteType}, {@code resolutionStatus}, {@code module}) used in toolbar links.
 * Row fetching delegates to {@link ViewNotesService}, filtering and sorting to {@link ViewNotesFilterCriteria} and
 * {@link ViewNotesSortCriteria}.
 *
 * <p>Preserves legacy behaviour for parity: exact-integer matches for {@code age}/{@code days} filters,
 * unmodified "numberOfNotes" counts, and omission of the per-row download link (legacy dead code).
 * Fixes a legacy bug: compares entity type by value instead of reference.
 */
public class ListNotesTable {

    private final ViewNotesService viewNotesService;
    private final StudyBean currentStudy;
    private final Integer discNoteType;         // top-level "type" request param -- NOT a column filter (see class javadoc)
    private final Integer resolutionStatus;     // top-level "resolutionStatus" request param -- NOT a column filter
    private final String module;

    private final ResourceBundle resword;
    private final ResourceBundle resformat;

    private final List<NoteFilterOption> discNoteTypeFilterOptions;
    private final List<NoteFilterOption> resolutionStatusFilterOptions;
    private final Map<String, String> discNoteTypeDecoder;
    private final Map<String, String> resolutionStatusDecoder;

    private final LCTable<DiscrepancyNoteBean> table;

    /** Set during {@link #fetchData} and read by {@link #getNotesSummary()} (single-threaded per-request use). */
    private DiscrepancyNotesSummary notesSummary;

    public ListNotesTable(
        ViewNotesService viewNotesService, StudyBean currentStudy, Integer discNoteType, Integer resolutionStatus,
        String module, Locale locale
    ) {
        this.viewNotesService = viewNotesService;
        this.currentStudy = currentStudy;
        this.discNoteType = discNoteType;
        this.resolutionStatus = resolutionStatus;
        this.module = module;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.resformat = ResourceBundleProvider.getFormatBundle(locale);

        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(locale);
        this.discNoteTypeFilterOptions = buildDiscNoteTypeFilterOptions(resterm);
        this.resolutionStatusFilterOptions = buildResolutionStatusFilterOptions(resterm);
        this.discNoteTypeDecoder = identityDecoder(discNoteTypeFilterOptions);
        this.resolutionStatusDecoder = identityDecoder(resolutionStatusFilterOptions);

        // "type", "resolutionStatus", and "module" are top-level sticky parameters used for download/print links.
        this.table = new LCTable<>("listNotes", buildColumns(), this::fetchData, List.of("module", "type", "resolutionStatus"))
            .setRowTestAttributes(row -> Map.of("note", String.valueOf(row.getId())));

        // Only show download/print links (and their separators) if the current page has rows.
        java.util.function.Predicate<LCTableContext<DiscrepancyNoteBean>> hasContent = ctx -> !ctx.data.pageItems.isEmpty();
        this.table.addCustomToolbarControl(hasContent, this::renderDownloadControl);
        this.table.addCustomToolbarControl(hasContent, this::renderPrintControl);
    }

    // -- Filter option types for the "type"/"resolution status" column dropdowns (label/value decoding) ------------
    //
    // Neither DiscrepancyNoteType nor ResolutionStatus alone is enough here: the legacy dropdown filters
    // also offer one extra, combined option each ("Query_and_Failed_Validation_Check" -> "1,3",
    // "New_and_Updated" -> "1,2"), submitted as a comma-separated id list that the SQL "in (...)" filter fragment
    // accepts directly (see viewnotes.properties). This small typed option class models exactly that: a
    // (submitted id/ids, display label) pair -- preferred here over a loose Map<String,String> per the project's
    // type-safety conventions.
    private static final class NoteFilterOption {
        private final String urlParam;
        private final String label;

        NoteFilterOption(String urlParam, String label) {
            this.urlParam = urlParam;
            this.label = label;
        }

        String getUrlParam() {
            return urlParam;
        }

        String getLabel() {
            return label;
        }
    }

    private static List<NoteFilterOption> buildDiscNoteTypeFilterOptions(ResourceBundle resterm) {
        List<NoteFilterOption> options = new ArrayList<>();
        for (DiscrepancyNoteType type : DiscrepancyNoteType.list) {
            options.add(new NoteFilterOption(Integer.toString(type.getId()), type.getName()));
        }
        options.add(new NoteFilterOption("1,3", resterm.getString("Query_and_Failed_Validation_Check")));
        return options;
    }

    private static List<NoteFilterOption> buildResolutionStatusFilterOptions(ResourceBundle resterm) {
        List<NoteFilterOption> options = new ArrayList<>();
        for (ResolutionStatus status : ResolutionStatus.list) {
            options.add(new NoteFilterOption(Integer.toString(status.getId()), status.getName()));
        }
        options.add(new NoteFilterOption("1,2", resterm.getString("New_and_Updated")));
        return options;
    }

    /**
     * {@link LCTableFilterDef.Select} submits the id/ids string directly as the URL parameter value (see
     * {@code NoteFilterOption#getUrlParam()}), rather than a display label needing decoding -- so the decoder
     * maps required by {@link ViewNotesFilterCriteria#buildFilterCriteria(Map, String, Map, Map)} are simply
     * identity maps.
     */
    private static Map<String, String> identityDecoder(List<NoteFilterOption> options) {
        Map<String, String> decoder = new HashMap<>();
        for (NoteFilterOption option : options) {
            decoder.put(option.getUrlParam(), option.getUrlParam());
        }
        return decoder;
    }

    // -- Element id helper -------------------------------------------------------------------------

    /** Per-row action id, prefixed with the table's name (e.g., "listNotes-view-123"). */
    private String actionId(String action, DiscrepancyNoteBean row) {
        return actionId(action) + "-" + row.getId();
    }

    /** Toolbar-level action id, prefixed with the table's name (e.g., "listNotes-download"). */
    private String actionId(String action) {
        return table.getTableName() + "-" + action;
    }

    // -- Toolbar: "download all" / "print" links (custom, non-tabular toolbar controls) --------------

    /**
     * Renders the "download all discrepancy notes" toolbar icon link.
     * Only shown when the current page has content.
     */
    private void renderDownloadControl(Div<?> container, LCTableContext<DiscrepancyNoteBean> ctx) {
        // Rebuilds the "ChooseDownloadFormat" URL including current filter/sort states and sticky
        // parameters (resolutionStatus, discNoteType, module) to ensure the export matches the table view.
        SafeUrl downloadHref = url("ChooseDownloadFormat")
            .param("resolutionStatus", resolutionStatus)
            .param("discNoteType", discNoteType)
            .param("module", module);
        ctx.filters.forEach(downloadHref::param);
        if (ctx.sortProp != null && !ctx.sortProp.isEmpty()) {
            // Must uppercase sortDir because DiscrepancyNoteOutputServlet expects "ASC"/"DESC".
            downloadHref.param("sort." + ctx.sortProp, ctx.sortDir.toUpperCase(Locale.ROOT));
        }

        container.of(LCTableUtil.actionLink(actionId("download"), resword.getString("download_all_discrepancy_notes"),
            "javascript:openDocWindow('" + downloadHref.toUriString() + "')", "bt_Download.gif", "download"));
    }

    /**
     * Renders the "print discrepancy notes" toolbar icon link.
     * Only shown when the current page has content.
     */
    private void renderPrintControl(Div<?> container, LCTableContext<DiscrepancyNoteBean> ctx) {
        // Rebuilds a clean "ViewNotes...&print=yes" URL including current filters and sort state.
        // Deliberately omits pagination params (page, maxRows, showHiddenCols) as they are ignored
        // by findAllNotes(request), and omits discNoteType/resolutionStatus which are unused here.
        SafeUrl printHref = url("ViewNotes")
            .param("module", module)
            .param("print", "yes");
        ctx.filters.forEach((prop, val) -> printHref.param(LCTableParams.PARAM_FILTER_PREFIX + prop, val));
        if (ctx.sortProp != null && !ctx.sortProp.isEmpty()) {
            printHref.param(LCTableParams.PARAM_SORT_PROP, ctx.sortProp).param(LCTableParams.PARAM_SORT_DIR, ctx.sortDir);
        }

        container.of(LCTableUtil.actionLink(actionId("print"), resword.getString("print"),
            "javascript:openDocWindow('" + printHref.toUriString() + "')", "bt_Print.gif", "print"));
    }

    // -- Column definitions --------------------------------------------------------------------------

    private List<LCTableColumnDef<DiscrepancyNoteBean>> buildColumns() {
        List<LCTableColumnDef<DiscrepancyNoteBean>> columns = new ArrayList<>();

        columns.add(textCol    ("studySubject.label",                   key("study_subject_ID"),  "study_subject_ID",
            0, VISIBLE, SORTABLE, textFilter(), row -> row.getStudySub().getLabel())
        );
        columns.add(customTdCol("discrepancyNoteBean.disType",          key("type"),              "type",
            0, NOT_SORTABLE, new LCTableFilterDef.Select<>(discNoteTypeFilterOptions, NoteFilterOption::getLabel, NoteFilterOption::getUrlParam),
            this::renderNoteTypeCell
        ));
        columns.add(customTdCol("discrepancyNoteBean.resolutionStatus", key("resolution_status"), "resolution_status", 0, NOT_SORTABLE,
            new LCTableFilterDef.Select<>(resolutionStatusFilterOptions, NoteFilterOption::getLabel, NoteFilterOption::getUrlParam),
            this::renderResolutionStatusCell
        ));
        columns.add(textCol    ("siteId",                            key("site_id"),            "site_id",            0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getSiteId));
        columns.add(textCol    ("discrepancyNoteBean.createdDate",   key("date_created"),       "date_created",       0, HIDDEN,  SORTABLE,     textFilter(), DiscrepancyNoteBean::getCreatedDate, this::formatDate));
        columns.add(textCol    ("discrepancyNoteBean.updatedDate",   key("date_updated"),       "date_updated",       0, HIDDEN,  NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getUpdatedDate, this::formatDate));
        columns.add(textCol    ("age",                               key("days_open"),          "days_open",          0, VISIBLE, SORTABLE,     textFilter("\\d*", key("lctable_numeric_filter_message")), DiscrepancyNoteBean::getAge,  Object::toString));
        columns.add(textCol    ("days",                              key("days_since_updated"), "days_since_updated", 0, VISIBLE, SORTABLE,     textFilter("\\d*", key("lctable_numeric_filter_message")), DiscrepancyNoteBean::getDays, Object::toString));
        columns.add(textCol    ("eventName",                         key("event_name"),         "event_name",         0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getEventName));
        columns.add(textCol    ("eventStartDate",                    key("event_date"),         "event_date",         0, HIDDEN,  NOT_SORTABLE, NO_FILTER,    DiscrepancyNoteBean::getEventStart, this::formatDate));
        columns.add(textCol    ("crfName",                           key("CRF"),                "CRF",                0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getCrfName));
        columns.add(textCol    ("crfStatus",                         key("CRF_status"),         "CRF_status",         0, HIDDEN,  NOT_SORTABLE, NO_FILTER,    DiscrepancyNoteBean::getCrfStatus));
        columns.add(textCol    ("entityName",                        key("entity_name"),        "entity_name",        0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getEntityName));
        columns.add(textCol    ("entityValue",                       key("entity_value"),       "entity_value",       0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getEntityValue));
        columns.add(textCol    ("discrepancyNoteBean.entityType",    key("entity_type"),        "entity_type",        0, HIDDEN,  NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getEntityType));
        columns.add(textCol    ("discrepancyNoteBean.description",   key("description"),        "description",        0, VISIBLE, NOT_SORTABLE, textFilter(), DiscrepancyNoteBean::getDescription));
        columns.add(textCol    ("discrepancyNoteBean.detailedNotes", key("detailed_notes"),     "detailed_notes",     0, HIDDEN,  NOT_SORTABLE, NO_FILTER,    DiscrepancyNoteBean::getDetailedNotes));
        columns.add(textCol    ("numberOfNotes",                     key("of_notes"), "of_notes",           0, HIDDEN,  NOT_SORTABLE, NO_FILTER,    DiscrepancyNoteBean::getNumChildren, x -> Integer.toString(x)));
        columns.add(customTdCol("discrepancyNoteBean.user",          key("assigned_user"), "assigned_user",      0,          NOT_SORTABLE, textFilter(), this::renderAssignedUserCell));
        columns.add(textCol    ("discrepancyNoteBean.owner",         key("owner"), "owner",              0, HIDDEN,  NOT_SORTABLE, NO_FILTER,
            row -> {
                UserAccountBean owner = row.getOwner();
                String ownerName = owner == null ? null : owner.getName();
                return ownerName == null || ownerName.isEmpty() ? null : ownerName;
            }
        ));
        columns.add(customTdCol("actions", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(), this::renderActionsCell));

        return columns;
    }

    // -- Cell renderers -------------------------------------------------------------------------------

    private void renderNoteTypeCell(Td<?> td, DiscrepancyNoteBean row) {
        DiscrepancyNoteType type = row.getDisType();
        td.text(type == null ? NULL_PLACEHOLDER : type.getName());
    }

    private void renderResolutionStatusCell(Td<?> td, DiscrepancyNoteBean row) {
        ResolutionStatus status = row.getResStatus();
        if (status == null) {
            td.text(NULL_PLACEHOLDER);
            return;
        }
        Td<?> afterIcon = td.img().attrSrc(status.getIconFilePath()).attrAlt(status.getName()).__();
        afterIcon.text(" " + status.getName());
    }

    private void renderAssignedUserCell(Td<?> td, DiscrepancyNoteBean row) {
        UserAccountBean user = row.getAssignedUser();
        if (user == null || user.getName() == null || user.getName().isEmpty()) {
            td.text(NULL_PLACEHOLDER);
        } else {
            td.text(user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ")");
        }
    }

    /** Renders action links (see class javadoc for intentional legacy parity fix/omission). */
    private void renderActionsCell(Td<?> td, DiscrepancyNoteBean row) {
        String createNoteUrl = CreateDiscrepancyNoteServlet.getAddChildURL(row, ResolutionStatus.CLOSED, true) + "&viewAction=1";
        td.of(LCTableUtil.actionLink(actionId("view", row), resword.getString("view"), "javascript:openDNWindow('" + createNoteUrl + "');", "bt_View.gif", "view"));

        if (!currentStudy.getStatus().isLocked()) {
            // Fixed: the legacy renderer compared getEntityType() != "eventCrf" by reference instead of by value.
            boolean isEventCrf = "eventCrf".equals(row.getEntityType());
            if (!isEventCrf || row.getStageId() == 5) {
                td.of(LCTableUtil.actionLink(actionId("resolve", row), resword.getString("view_within_crf"),
                    url("ResolveDiscrepancy").param("noteId", row.getId()), "bt_Reassign.gif", "resolve"));
            }
        }

        // NOTE: the legacy jmesa ActionsCellEditor also built a per-row "download discrepancy notes" link here
        // (see the now-removed downloadNotesLinkBuilder(studySubjectBean)), but never actually appended it to the
        // rendered HTML -- a pre-existing dead-code bug, intentionally preserved (not restored) in this migration.
        // If the per-row download icon is ever wanted back, this is the place to add it, reusing the same
        // "ChooseDownloadFormat?subjectId=...&discNoteType=...&resolutionStatus=...(&module=...)" URL pattern
        // as the toolbar-level "download all" link built in #renderDownloadAndPrintControls.
    }

    // -- Date formatting --------------------------------------------------------------------------------

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat(getDateFormat()).format(date);
    }

    // -- Data fetching -----------------------------------------------------------------------------

    private LCTableData<DiscrepancyNoteBean> fetchData(LCTableParams p) {
        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(p.filters, getDateFormat(), discNoteTypeDecoder,
            resolutionStatusDecoder);

        DiscrepancyNotesSummary summary = viewNotesService.calculateNotesSummary(currentStudy, filter);
        this.notesSummary = summary;
        int total = summary.getTotal();

        // If requested page exceeds dataset size, fetch the last page instead (preserves legacy behaviour).
        int pageSize = p.maxRows;
        int firstRecordShown = p.page * pageSize;
        int effectivePage1Based = (total != 0 && firstRecordShown > total) ? (int) Math.ceil((double) total / pageSize) : p.page + 1;

        filter.withPagination(effectivePage1Based, pageSize);
        ViewNotesSortCriteria sort = ViewNotesSortCriteria.buildFilterCriteria(p.sortProp, p.sortDir);
        List<DiscrepancyNoteBean> items = viewNotesService.listNotes(currentStudy, filter, sort);

        return new LCTableData<>(items, total);
    }

    /** Notes summary computed during the last {@link #fetchData}/{@link #render} call. */
    public DiscrepancyNotesSummary getNotesSummary() {
        return notesSummary;
    }

    /** Fetches all notes matching current filters/sort (unpaginated) for the "print" flow. */
    public List<DiscrepancyNoteBean> findAllNotes(HttpServletRequest request) {
        LCTableParams params = new LCTableParams(request.getQueryString(), this.table);
        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(params.filters, getDateFormat(), discNoteTypeDecoder,
            resolutionStatusDecoder);
        ViewNotesSortCriteria sort = ViewNotesSortCriteria.buildFilterCriteria(params.sortProp, params.sortDir);
        return viewNotesService.listNotes(currentStudy, filter, sort);
    }

    // -- Rendering entry point ---------------------------------------------------------------------

    public String render(HttpServletRequest request) {
        LCTableParams params = new LCTableParams(request.getQueryString(), this.table);
        return this.table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }

}
