/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentSort;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionType;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.HideActionBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RandomizeActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.rule.action.StratificationFactorBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableContext;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableFilterDef;
import org.akaza.openclinica.lctable.LCTableParams;
import org.akaza.openclinica.lctable.SafeUrl;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.EnumTypeInputType;
import org.xmlet.htmlapifaster.Td;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.LCTableUtil.actionLink;
import static org.akaza.openclinica.lctable.LCTableUtil.hxGetAttrs;
import static org.akaza.openclinica.lctable.LCTableUtil.NO_HX_INCLUDE;
import static org.akaza.openclinica.lctable.LCTableUtil.NO_HX_TRIGGER;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/** Request-scoped rule assignments table with paged display and filtered CSV/XLS/XML downloads. */
public final class ViewRuleAssignmentsTable {
    public static final String EXPORT_PARAM = "export";
    private static final String STATUS_FILTER = "ruleSetRuleStatus";
    private final RuleSetServiceInterface service;
    private final ItemFormMetadataDAO metadataDao;
    private final StudyBean study;
    private final Locale locale;
    private final ResourceBundle words;
    private final LCTable<AssignmentRow> table;

    static final class AssignmentRow {
        final RuleSetRuleBean assignment;
        final RuleSetBean set;
        final String validation;

        AssignmentRow(RuleSetRuleBean assignment, RuleSetBean set, String validation) {
            this.assignment = assignment;
            this.set = set;
            this.validation = validation;
        }
    }

    public ViewRuleAssignmentsTable(RuleSetServiceInterface service, ItemFormMetadataDAO metadataDao, StudyBean study, Locale locale) {
        this.service = service;
        this.metadataDao = metadataDao;
        this.study = study;
        this.locale = locale;
        this.words = ResourceBundleProvider.getWordsBundle(locale);
        table = new LCTable<>("ruleAssignments", columns(), this::fetchData);
        table.setRowTestAttributes(row -> Map.of("rule-assignment", String.valueOf(row.assignment.getId())));
        table.addCustomToolbarControl(this::statusControl);
        table.addCustomToolbarControl(this::testRuleControl);
        table.addCustomToolbarControl((container, ctx) -> exportControl(container, ctx, "csv", "table/csv.gif"));
        table.addCustomToolbarControl((container, ctx) -> exportControl(container, ctx, "xls", "table/excel.gif"));
        table.addCustomToolbarControl((container, ctx) -> exportControl(container, ctx, "xml", "bt_Download.gif"));
    }

    /** Normalize the view mode before either rendering or export constructs its selection. */
    private LCTableParams paramsFor(HttpServletRequest request) {
        LCTableParams params = new LCTableParams(request.getQueryString(), table);
        if (!"5".equals(params.filters.get(STATUS_FILTER))) {
            params.filters.put(STATUS_FILTER, "1");
        }
        return params;
    }

    private void statusControl(Div<?> container, LCTableContext<AssignmentRow> ctx) {
        String status = ctx.filters.get(STATUS_FILTER);
        // NO_FILTER has no widget; retain the mode when other filters/page size submit the form.
        container.input().attrType(EnumTypeInputType.HIDDEN)
            .attrName(LCTableParams.PARAM_FILTER_PREFIX + STATUS_FILTER).attrValue(status).__();
        SafeUrl href = url(ctx.entityPath).param(LCTableParams.PARAM_PAGE, 1)
            .param(LCTableParams.PARAM_MAX_ROWS, ctx.maxRows);
        ctx.filters.forEach((name, value) -> {
            if (!STATUS_FILTER.equals(name)) href.param(LCTableParams.PARAM_FILTER_PREFIX + name, value);
        });
        boolean removed = "5".equals(status);
        href.param(LCTableParams.PARAM_FILTER_PREFIX + STATUS_FILTER, removed ? "1" : "5");
        if (!ctx.sortProp.isEmpty()) {
            href.param(LCTableParams.PARAM_SORT_PROP, ctx.sortProp).param(LCTableParams.PARAM_SORT_DIR, ctx.sortDir);
        }
        if (ctx.showHiddenCols) href.param(LCTableParams.PARAM_SHOW_HIDDEN_COLS, true);
        String target = href.toUriString();
        container.a().attrId("ruleAssignments-toggle-status").attrClass("text-btn").attrHref(target)
            .addAttr("data-test-action", removed ? "show-available-rules" : "show-removed-rules")
            .of(hxGetAttrs(target, NO_HX_INCLUDE, "#ruleAssignments-panel", NO_HX_TRIGGER))
            .text(ctx.words.getString(removed ? "view_rule_assignment_show_available" : "view_rule_assignment_show_removed"))
            .__();
    }

    private void exportControl(Div<?> container, LCTableContext<AssignmentRow> ctx, String format, String icon) {
        SafeUrl href = url(ctx.entityPath).param(EXPORT_PARAM, format);
        ctx.filters.forEach((name, value) -> href.param(LCTableParams.PARAM_FILTER_PREFIX + name, value));
        if (!ctx.sortProp.isEmpty()) {
            href.param(LCTableParams.PARAM_SORT_PROP, ctx.sortProp).param(LCTableParams.PARAM_SORT_DIR, ctx.sortDir);
        }
        container.of(actionLink("ruleAssignments-export-" + format,
            ctx.words.getString("download") + " " + format.toUpperCase(Locale.ROOT), href, icon, "export-" + format));
    }

    private void testRuleControl(Div<?> container, LCTableContext<AssignmentRow> ctx) {
        container.a().attrHref(ctx.resourcePath + "/TestRule").text(ctx.words.getString("view_rules_test")).__();
    }

    private List<LCTableColumnDef<AssignmentRow>> columns() {
        List<LCTableColumnDef<AssignmentRow>> cols = new ArrayList<>();
        cols.add(enumColHidden("ruleSetRunSchedule", key("view_rule_assignment_run_schedule"), "run-schedule", 0,
            row -> row.set.isRunSchedule(), List.of(Boolean.TRUE, Boolean.FALSE), String::valueOf, String::valueOf));
        cols.add(textColHidden("ruleSetRunTime", key("view_rule_assignment_run_time"), "run-time", 0, row -> row.set.getRunTime()));
        cols.add(textColHidden("targetValue", key("view_rule_assignment_target"), "target", 0, row -> row.set.getTarget().getValue()));
        cols.add(textColHidden("studyEventDefinitionName", key("view_rule_assignment_study_event"), "study-event", 0,
            row -> row.set.getStudyEventDefinitionName()));
        cols.add(textCol("crfName", key("view_rule_assignment_crf"), "crf", 0, row -> row.set.getCrfName()));
        cols.add(textColHidden("crfVersionName", key("view_rule_assignment_version"), "version", 0,
            row -> row.set.getCrfVersionName()));
        cols.add(textColHidden("groupLabel", key("view_rule_assignment_group"), "group", 0, row -> row.set.getGroupLabel()));
        cols.add(customTdCol("itemName", key("view_rule_assignment_item_name"), "item-name", 0, SORTABLE,
            new LCTableFilterDef.Text(), (td, row) -> {
                if (row.set.getItem() != null) {
                    td.a().attrHref(url("ViewItemDetail").param("itemId", row.set.getItem().getId()).toUriString())
                        .addAttr("onclick", "openDocWindow(this.href); return false;")
                        .text(row.set.getItemName()).__();
                } else {
                    td.text(row.set.getItemName());
                }
            }));
        cols.add(textCol("ruleName", key("view_rule_assignment_rule_name"), "rule-name", 0,
            row -> row.assignment.getRuleBean().getName()));
        cols.add(textCol("ruleOid", key("view_rule_assignment_rule_oid"), "rule-oid", 0,
            row -> row.assignment.getRuleBean().getOid()));
        cols.add(textCol(STATUS_FILTER, key("view_rule_assignment_rule_status"), "status", 0,
            VISIBLE, SORTABLE, NO_FILTER, row -> row.assignment.getStatus(), status -> status.getI18nDescription(locale)));
        cols.add(textColHidden("ruleDescription", key("view_rule_assignment_rule_description"), "description", 0,
            row -> row.assignment.getRuleBean().getDescription()));
        cols.add(textCol("ruleExpressionValue", key("view_rule_assignment_expression"), "expression", 0,
            row -> row.assignment.getRuleBean().getExpression().getValue()));
        cols.add(textColHidden("validations", key("view_rule_assignment_crf_validations"), "validations", 0,
            NOT_SORTABLE, NO_FILTER, row -> row.validation));
        cols.add(customTdCol("actionExecuteOn", key("view_rule_assignment_execute_on"), "execute-on", 0,
            NOT_SORTABLE, new LCTableFilterDef.Select<>(List.of(Boolean.TRUE, Boolean.FALSE), String::valueOf),
            (td, row) -> renderActionLines(td, row, false)));
        cols.add(columnDef("actionType", key("view_rule_assignment_action_type"), "action-type", 0, HIDDEN,
            NOT_SORTABLE, new LCTableFilterDef.Select<>(List.of(ActionType.values()), ActionType::getDescription,
                type -> type.getCode().toString()), (tr, row, ctx) -> {
                Td<?> td = tr.td().attrClass("lc-col-actionType").addAttr("data-test-column", "action-type");
                renderActionLines(td, row, true);
                td.__();
            }));
        cols.add(customTdCol("actionSummary", key("view_rule_assignment_action_summary"), "action-summary", 0,
            NOT_SORTABLE, new LCTableFilterDef.Text(), this::renderSummary));
        cols.add(customTdCol("actions", key("actions"), "actions", 0, NOT_SORTABLE,
            new LCTableFilterDef.ClearFilter(), this::renderActions));
        for (LCTableColumnDef<AssignmentRow> col : cols) {
            Function<AssignmentRow, String> exporter;
            switch (col.columnName) {
                case "ruleSetRunSchedule": exporter = row -> String.valueOf(row.set.isRunSchedule()); break;
                case "ruleSetRunTime": exporter = row -> row.set.getRunTime(); break;
                case "targetValue": exporter = row -> row.set.getTarget().getValue(); break;
                case "studyEventDefinitionName": exporter = row -> row.set.getStudyEventDefinitionName(); break;
                case "crfName": exporter = row -> row.set.getCrfName(); break;
                case "crfVersionName": exporter = row -> row.set.getCrfVersionName(); break;
                case "groupLabel": exporter = row -> row.set.getGroupLabel(); break;
                case "itemName": exporter = row -> row.set.getItemName(); break;
                case "ruleName": exporter = row -> row.assignment.getRuleBean().getName(); break;
                case "ruleOid": exporter = row -> row.assignment.getRuleBean().getOid(); break;
                case "ruleSetRuleStatus": exporter = row -> row.assignment.getStatus().getI18nDescription(locale); break;
                case "ruleDescription": exporter = row -> row.assignment.getRuleBean().getDescription(); break;
                case "ruleExpressionValue": exporter = row -> row.assignment.getRuleBean().getExpression().getValue(); break;
                case "validations": exporter = row -> row.set.getItem() == null && row.set.getCrfVersion() == null
                    && row.set.getCrf() == null ? null : row.validation; break;
                case "actionExecuteOn": exporter = row -> joinActions(row, " - ", action -> String.valueOf(action.getExpressionEvaluatesTo())); break;
                case "actionType": exporter = row -> joinActions(row, " ; ", action -> action.getActionType().getDescription()); break;
                case "actionSummary": exporter = row -> joinActions(row, " ; ", action -> String.valueOf(action.getSummary())); break;
                default: continue; // Actions are display-only.
            }
            col.setCellExporter(row -> LCTable.Export.Value.text(exporter.apply(row)));
            if ("crfName".equals(col.columnName)) {
                // Preserve the heading found in the supplied legacy workbook, including its literal padding.
                col.setCellExporter(org.akaza.openclinica.lctable.LCTableText.literal(
                    words.getString("view_rule_assignment_crf") + "&#160;&#160;&#160;&#160;&#160;"),
                    row -> LCTable.Export.Value.text(exporter.apply(row)));
            }
        }
        return cols;
    }

    private static String joinActions(AssignmentRow row, String delimiter, Function<RuleActionBean, String> value) {
        return row.assignment.getActions().stream().map(value).collect(Collectors.joining(delimiter));
    }

    private ViewRuleAssignmentFilter filterFor(LCTableParams params) {
        ViewRuleAssignmentFilter filter = new ViewRuleAssignmentFilter();
        // Column names are declared by the table; only actual legacy DAO filter properties are forwarded.
        params.filters.forEach((property, value) -> {
            switch (property) {
                case "ruleSetRunSchedule":
                case "actionExecuteOn":
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        filter.addFilter(property, Boolean.parseBoolean(value));
                    }
                    break;
                case "ruleSetRuleStatus":
                    if ("1".equals(value) || "5".equals(value)) filter.addFilter(property, value);
                    break;
                case "actionType":
                    for (ActionType type : ActionType.values()) {
                        if (type.getCode().toString().equals(value)) filter.addFilter(property, value);
                    }
                    break;
                case "targetValue": case "studyEventDefinitionName": case "crfName": case "crfVersionName":
                case "groupLabel": case "itemName": case "ruleName": case "ruleOid": case "ruleDescription":
                case "ruleExpressionValue": case "ruleSetRunTime": case "actionSummary":
                    filter.addFilter(property, value);
                    break;
                default:
                    break;
            }
        });
        filter.addFilter("studyId", study.getId());
        return filter;
    }

    private ViewRuleAssignmentSort sortFor(LCTableParams params) {
        ViewRuleAssignmentSort sort = new ViewRuleAssignmentSort();
        sort.addSort(params.sortProp.isEmpty() ? "itemName" : params.sortProp,
            params.sortProp.isEmpty() ? "asc" : params.sortDir);
        return sort;
    }

    private LCTableData<AssignmentRow> fetchData(LCTableParams params) {
        ViewRuleAssignmentFilter filter = filterFor(params);
        ViewRuleAssignmentSort sort = sortFor(params);
        int total = service.getCountWithFilter(filter);
        int start = params.page * params.maxRows;
        return new LCTableData<>(fetchRows(filter, sort, start, params.maxRows), total);
    }

    private List<AssignmentRow> fetchRows(ViewRuleAssignmentFilter filter, ViewRuleAssignmentSort sort, int offset, int limit) {
        List<RuleSetRuleBean> assignments = service.getWithFilterAndSort(filter, sort, offset, offset + limit);
        Map<Integer, RuleSetBean> enriched = new HashMap<>();
        List<AssignmentRow> rows = new ArrayList<>();
        for (RuleSetRuleBean assignment : assignments) {
            RuleSetBean set = assignment.getRuleSetBean();
            if (!enriched.containsKey(set.getId())) {
                service.getObjects(set);
                enriched.put(set.getId(), set);
            }
            RuleSetBean pageSet = enriched.get(set.getId());
            rows.add(new AssignmentRow(assignment, pageSet, validationFor(pageSet)));
        }
        return rows;
    }

    /** Writes a normal download response; called before either full-page or HTMX rendering. */
    public void export(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String format = request.getParameter(EXPORT_PARAM);
        if (!"csv".equals(format) && !"xls".equals(format) && !"xml".equals(format)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported rule-assignment export format. Use csv, xls or xml.");
            return;
        }
        LCTableParams params = paramsFor(request);
        ViewRuleAssignmentFilter filter = filterFor(params);
        ViewRuleAssignmentSort sort = sortFor(params);
        int total = service.getCountWithFilter(filter);
        if ("xml".equals(format)) {
            exportXml(filter, sort, total, response);
            return;
        }
        if ("xls".equals(format) && total > LCTable.Export.XLS_MAX_DATA_ROWS) {
            response.setStatus(422);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("XLS export supports at most 65,535 data rows. The current filters select "
                + total + " rows. Narrow the filters or download CSV instead.");
            return;
        }
        String filename = "rules" + study.getOid() + "-" + System.currentTimeMillis() + ("csv".equals(format) ? ".txt" : ".xls");
        response.setContentType("csv".equals(format) ? "text/plain;charset=UTF-8" : "application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-store");
        LCTable.Export<AssignmentRow> export = table.export().orElseThrow();
        LCTable.Export.Rows<AssignmentRow> rows = (offset, limit) -> fetchRows(filter, sort, offset, limit);
        if ("csv".equals(format)) export.toCsv(total, rows, response.getOutputStream());
        else export.toXls(total, rows, words, locale, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private void exportXml(ViewRuleAssignmentFilter filter, ViewRuleAssignmentSort sort, int total,
                           HttpServletResponse response) throws IOException {
        List<RuleSetRuleBean> assignments = new ArrayList<>();
        HashSet<Integer> ids = new HashSet<>();
        for (int offset = 0; offset < total;) {
            int limit = Math.min(256, total - offset);
            // XML needs domain objects, not display enrichment or the scalar export projection.
            List<RuleSetRuleBean> batch = service.getWithFilterAndSort(filter, sort, offset, offset + limit);
            if (batch == null || batch.size() != limit) {
                throw new IOException("Rule-assignment selection changed during XML export. Retry the download.");
            }
            for (RuleSetRuleBean assignment : batch) {
                if (assignment == null || assignment.getId() == null || !ids.add(assignment.getId())) {
                    throw new IOException("Invalid or repeated assignment in XML export. Retry the download.");
                }
            }
            assignments.addAll(batch);
            offset += limit;
        }
        RuleAssignmentsXmlWriter writer;
        try {
            writer = new RuleAssignmentsXmlWriter(assignments, study.getId());
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Rule assignment does not belong to the current study.");
            return;
        }
        response.setContentType("text/xml;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"rules" + study.getOid()
            + "-" + System.currentTimeMillis() + ".xml\"");
        response.setHeader("Cache-Control", "no-store");
        writer.write(response.getOutputStream());
    }

    private String validationFor(RuleSetBean set) {
        if (set.getCrfVersion() != null && set.getItem() != null) {
            ItemFormMetadataBean meta = metadataDao.findByItemIdAndCRFVersionId(set.getItem().getId(), set.getCrfVersion().getId());
            return meta != null && meta.getId() != 0 && meta.getRegexp() != null && !meta.getRegexp().isEmpty() ? "yes" : "no";
        }
        if (set.getItem() == null) return "no";
        if (set.getCrf() != null) {
            return metadataDao.findAllByCRFIdItemIdAndHasValidations(set.getCrf().getId(), set.getItem().getId()).isEmpty() ? "no" : "yes";
        }
        return metadataDao.findAllByItemIdAndHasValidations(set.getItem().getId()).isEmpty() ? "no" : "yes";
    }

    private void renderActionLines(Td<?> td, AssignmentRow row, boolean types) {
        for (RuleActionBean action : row.assignment.getActions()) {
            td.text(types ? action.getActionType().getDescription() : String.valueOf(action.getExpressionEvaluatesTo())).br().__();
        }
    }

    private void renderSummary(Td<?> td, AssignmentRow row) {
        td.table().of(table -> {
            for (RuleActionBean action : row.assignment.getActions()) {
                action.getPropertiesForDisplay().forEach((name, value) -> summaryLine(table, words.getString(name), String.valueOf(value)));
                RuleActionRunBean run = action.getRuleActionRun();
                if (run != null) {
                    String target = row.set.getOriginalTarget() == null ? row.set.getTarget().getValue() : row.set.getOriginalTarget().getValue();
                    List<String> on = new ArrayList<>();
                    if (eventTarget(target)) {
                        if (action.getActionType().getCode() != 7) {
                            if (Boolean.TRUE.equals(run.getNot_started())) on.add(words.getString("not_scheduled_comma"));
                            if (Boolean.TRUE.equals(run.getScheduled())) on.add(words.getString("scheduled_comma"));
                            if (Boolean.TRUE.equals(run.getData_entry_started())) on.add(words.getString("data_entry_started_comma"));
                            if (Boolean.TRUE.equals(run.getComplete())) on.add(words.getString("completed_comma"));
                            if (Boolean.TRUE.equals(run.getSkipped())) on.add(words.getString("skipped_comma"));
                            if (Boolean.TRUE.equals(run.getStopped())) on.add(words.getString("stopped_comma"));
                        }
                    } else {
                        if (Boolean.TRUE.equals(run.getInitialDataEntry())) on.add(words.getString("IDE_comma"));
                        if (Boolean.TRUE.equals(run.getDoubleDataEntry())) on.add(words.getString("DDE_comma"));
                        if (Boolean.TRUE.equals(run.getAdministrativeDataEntry())) on.add(words.getString("ADE_comma"));
                        if (Boolean.TRUE.equals(run.getImportDataEntry())) on.add(words.getString("import_comma"));
                        if (Boolean.TRUE.equals(run.getBatch())) on.add(words.getString("batch_comma"));
                    }
                    if (!on.isEmpty()) summaryLine(table, words.getString("run_on_colon"), String.join(" ", withoutTrailingDelimiter(on)));
                }
                List<PropertyBean> properties = null;
                if (action instanceof RandomizeActionBean) properties = ((RandomizeActionBean) action).getProperties();
                else if (action instanceof InsertActionBean) properties = ((InsertActionBean) action).getProperties();
                else if (action instanceof EventActionBean) properties = ((EventActionBean) action).getProperties();
                else if (action instanceof ShowActionBean) properties = ((ShowActionBean) action).getProperties();
                else if (action instanceof HideActionBean) properties = ((HideActionBean) action).getProperties();
                if (properties != null) {
                    List<String> destinations = new ArrayList<>();
                    for (PropertyBean prop : properties) {
                        if (prop.getOid() != null) destinations.add(prop.getOid().trim());
                        else if (prop.getProperty() != null) destinations.add(prop.getProperty().trim());
                    }
                    if (!destinations.isEmpty()) summaryLine(table, words.getString("dest_prop_colon"), String.join(", ", destinations));
                }
                if (action instanceof RandomizeActionBean) {
                    List<String> factors = new ArrayList<>();
                    List<StratificationFactorBean> values = ((RandomizeActionBean) action).getStratificationFactors();
                    if (values != null) for (StratificationFactorBean factor : values) {
                        if (factor.getStratificationFactor() != null) factors.add(factor.getStratificationFactor().getValue());
                    }
                    if (!factors.isEmpty()) summaryLine(table, words.getString("stratification_factor_colon"), String.join(", ", factors));
                }
            }
        }).__();
    }

    private void summaryLine(org.xmlet.htmlapifaster.Table<?> table, String label, String value) {
        table.tr().td().i().text(label).__().__().td().text(value).__().__();
    }

    private static List<String> withoutTrailingDelimiter(List<String> values) {
        List<String> result = new ArrayList<>(values);
        String last = result.get(result.size() - 1).trim();
        int delimiterStart = last.offsetByCodePoints(last.length(), -1);
        result.set(result.size() - 1, last.substring(0, delimiterStart));
        return result;
    }

    private static boolean eventTarget(String target) {
        return target != null && target.startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY)
            && (target.endsWith(ExpressionService.STARTDATE) || target.endsWith(ExpressionService.STATUS));
    }

    private void renderActions(Td<?> td, AssignmentRow row) {
        int id = row.assignment.getId();
        int setId = row.set.getId();
        td.of(actionLink("ruleAssignments-view-" + id, words.getString("view"), url("ViewRuleSet").param("ruleSetId", setId), "bt_View.gif", "view"));
        if (row.assignment.getStatus() != Status.DELETED) {
            if (!eventTarget(row.set.getTarget().getValue())) {
                td.of(actionLink("ruleAssignments-run-" + id, words.getString("rule_run"), url("RunRuleSet").param("ruleSetId", setId)
                    .param("ruleId", row.assignment.getRuleBean().getId()), "bt_ExexuteRules.gif", "run"));
            } else {
                td.img().attrSrc("images/bt_ExexuteRules.gif").attrAlt(words.getString("rule_run")).__();
            }
            renderUpdateAction(td, id, setId, "remove", words.getString("remove"), "bt_Remove.gif", "rule_if_you_remove_this");
        } else {
            renderUpdateAction(td, id, setId, "restore", words.getString("restore"), "bt_Restore.gif", "rule_if_you_restore_this");
        }
        // This is the existing single-assignment XML endpoint, not a bulk LCTable export.
        td.of(actionLink("ruleAssignments-xml-" + id, words.getString("view_rules_download_xml"),
            url("DownloadRuleSetXml").param("ruleSetRuleIds", id), "bt_Download.gif", "download-xml"));
        td.of(actionLink("ruleAssignments-test-" + id, words.getString("test_rules_title"),
            url("TestRule").param("ruleSetRuleId", id), "bt_Reassign_d.gif", "test"));
    }

    private void renderUpdateAction(Td<?> td, int id, int setId, String action, String altText, String icon, String confirmationKey) {
        String prompt = words.getString(confirmationKey);
        String href = url("UpdateRuleSetRule").param("action", action).param("ruleSetRuleId", id)
            .param("ruleSetId", setId).toUriString();
        td.a().attrId("ruleAssignments-" + action + "-" + id).attrClass("action-link").attrHref(href).addAttr("aria-label", altText)
            .addAttr("data-testid", "action-link").addAttr("data-test-action", action).addAttr("data-tooltip", altText)
            .addAttr("onclick", "return confirm('" + org.springframework.web.util.JavaScriptUtils.javaScriptEscape(prompt) + "');")
            .img().attrSrc("images/" + icon).attrAlt(altText).__().__();
    }

    public String render(HttpServletRequest request) {
        LCTableParams params = paramsFor(request);
        return table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }
}
