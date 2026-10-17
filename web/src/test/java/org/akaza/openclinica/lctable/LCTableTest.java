/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.lctable;

import junit.framework.TestCase;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.LCPopup.PopupAction;
import org.akaza.openclinica.lctable.LCPopup.PopupItemLayout;
import org.akaza.openclinica.lctable.LCPopup.PopupItemRenderer;
import org.xmlet.htmlapifaster.Div;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.VISIBLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdColWithContext;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textColHidden;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.SafeUrl.url;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.LCTableText.literal;

public class LCTableTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        ResourceBundleProvider.updateLocale(Locale.JAPANESE);
    }

    public void testRendersRowCellPopupAndActionTestAttributes() {
        ResourceBundle mockResword = mock(ResourceBundle.class);
        
        LCPopup<Row, String> popup = new LCPopup<>(
            row -> List.of("item"),
            (trigger, items) -> trigger.text("trigger"),
            (header, row) -> header.text("header"),
            Optional.empty(),
            new PopupItemRenderer<>() {
                @Override
                public void renderStatus(Div<?> row, Row context, String item, int index, int total) {
                    row.text(item);
                }

                @Override
                public List<PopupAction> actionsFor(Row context, String item) {
                    return List.of(PopupAction.of(null, "View", url("View"), "bt_View.gif", true, "view"));
                }

                @Override
                public PopupItemLayout layoutFor(Row context, String item) {
                    return PopupItemLayout.SIMPLE;
                }
            },
            "event-trigger",
            "TestPopup",
            mockResword
        );

        LCTableColumnDef<Row> popupColumn = customTdCol(
            "event", literal("Event"), null, 0, NOT_SORTABLE, NO_FILTER,
            popup::render
        );
        popupColumn.setTestAttributes(row -> Map.of("event", row.event));

        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, row -> row.subject), popupColumn),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1)
        ).setRowTestAttributes(row -> Map.of("subject", row.subject));

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "", Locale.ENGLISH);

        assertTrue(html.contains("data-test-subject=\"M-001\""));
        assertTrue(html.contains("data-test-event=\"Baseline\""));
        assertTrue(html.contains("data-test-column=\"subject\""));
        assertTrue(html.contains("data-testid=\"lc-popup-trigger\""));
        assertTrue(html.contains("data-testid=\"lc-popup-header\""));
        assertTrue(html.contains("data-testid=\"lc-popup-body\""));
        assertTrue(html.contains("class=\"lc-popup-item\""));
        assertTrue(html.contains("data-testid=\"lc-popup-item\""));
        assertTrue(html.contains("data-test-index=\"1\""));
        assertTrue(html.contains("data-test-action=\"view\""));
    }

    public void testOmitsNullAndEmptyTestAttributeValues() {
        LCTableColumnDef<Row> column = textCol("subject", literal("Subject"), null, 0, row -> row.subject);
        column.setTestAttributes(row -> {
            java.util.LinkedHashMap<String, String> attributes = new java.util.LinkedHashMap<>();
            attributes.put("null-value", null);
            attributes.put("empty-value", "");
            attributes.put("subject", row.subject);
            return attributes;
        });

        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(column),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1)
        );

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "", Locale.ENGLISH);

        assertTrue(html.contains("data-test-subject=\"M-001\""));
        assertFalse(html.contains("data-test-null-value"));
        assertFalse(html.contains("data-test-empty-value"));
        int bodyStart = html.indexOf("<tbody");
        int bodyEnd = html.indexOf("</tbody>");
        assertTrue(html, bodyStart >= 0 && bodyEnd > bodyStart);
        assertFalse(html, html.substring(bodyStart, bodyEnd).contains("data-test-column"));
    }

    public void testReusedTableResolvesTextForEachRequestLocale() {
        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1)
        );
        LCTableParams params = new LCTableParams(0, 15, "", "asc", Collections.emptyMap());

        String english = table.render("/test", params, "", Locale.ENGLISH);
        String japanese = table.render("/test", params, "", Locale.JAPANESE);

        assertTrue(english, english.contains("Subject"));
        assertTrue(japanese, japanese.contains("\u88ab\u9a13\u8005"));
        assertTrue(english.contains("data-test-column=\"subject\""));
        assertTrue(japanese.contains("data-test-column=\"subject\""));
    }

    public void testRejectsDuplicateKeyBackedHeaders() {
        try {
            new LCTable<>("testTable", List.of(
                textCol("subject1", key("subject"), "subject", 0, (Row row) -> row.subject),
                textCol("subject2", key("subject"), "subject", 0, (Row row) -> row.subject)
            ), params -> new LCTableData<>(List.of(), 0));
            fail("Expected duplicate resource keys to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("subject"));
        }
    }

    public void testRejectsExplicitColumnAttributeForKeyBackedCell() {
        LCTableColumnDef<Row> column = textCol("subject", key("subject"), "subject", 0, (Row row) -> row.subject)
            .setTestAttributes((Row row) -> Map.of("column", "legacy"));
        LCTable<Row> table = new LCTable<>("testTable", List.of(column),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1));

        try {
            table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "", Locale.ENGLISH);
            fail("Expected conflicting column attributes to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("subject"));
        }
    }

    public void testContextAwareCustomCellMergesAttributesAndRejectsColumnCollision() {
        LCTableColumnDef<Row> column = customTdColWithContext(
            "subject", key("subject"), "subject", 0, NOT_SORTABLE, NO_FILTER,
            (Row row) -> row.subject, (td, row, context) -> td.text(row.subject)
        ).setTestAttributes(row -> Map.of("subject", row.subject));
        LCTable<Row> table = new LCTable<>("testTable", List.of(column),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1));
        LCTableParams params = new LCTableParams(0, 15, "", "asc", Collections.emptyMap());

        String html = table.render("/test", params, "", Locale.ENGLISH);

        assertTrue(html.contains("data-test-column=\"subject\""));
        assertTrue(html.contains("data-test-subject=\"M-001\""));

        column.setTestAttributes(row -> Map.of("column", "legacy"));
        try {
            table.render("/test", params, "", Locale.ENGLISH);
            fail("Expected conflicting context-aware column attributes to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("subject"));
        }
    }

    public void testSharedChromeUsesRequestLocaleAndEnglishFallback() {
        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(
                textCol("subject", key("subject"), "subject", 0, VISIBLE, NOT_SORTABLE, clearFilter(), row -> row.subject),
                textColHidden("details", key("details"), "details", 0, row -> row.event)
            ),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1)
        );

        String japanese = table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "", Locale.JAPANESE);

        assertTrue(japanese.contains("\u30d5\u30a3\u30eb\u30bf\u30fc\u3092\u30af\u30ea\u30a2"));
        assertTrue(japanese.contains("\u3082\u3063\u3068\u8868\u793a"));
        assertTrue(japanese.contains("Results 1-1 of 1."));

        LCTable<Row> emptyTable = new LCTable<>("emptyTable",
            List.of(textCol("subject", key("subject"), "subject", 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(), 0));
        String english = emptyTable.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "", Locale.ENGLISH);
        assertTrue(english.contains("No results."));
    }

    public void testSelectParsesStableUrlValueInsteadOfDisplayLabel() {
        LCTableFilterDef.Select<String> filter = new LCTableFilterDef.Select<>(
            List.of("ACTIVE"), value -> "Localized active", String::toLowerCase
        );

        assertEquals(Optional.of("ACTIVE"), filter.parseParam("active"));
        assertEquals(Optional.empty(), filter.parseParam("Localized active"));
    }

    public void testRendersDirectlyFromRequestAndPreservesStickyParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn("scope=S-1&q.subject=M-001");
        when(request.getRequestURI()).thenReturn("/Subjects");
        when(request.getContextPath()).thenReturn("/LibreClinica");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));
        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(new Row(params.filters.get("subject"), "Baseline")), 1),
            List.of("scope")
        );

        String html = table.render(request);

        assertTrue(html.contains("name=\"scope\" value=\"S-1\""));
        assertTrue(html.contains("name=\"q.subject\" value=\"M-001\""));
        assertTrue(html.contains("hx-get=\"/Subjects"));
        assertTrue(html.contains("/LibreClinica/images/table/sortAsc.gif"));
    }

    public void testToolbarClearFilterPreservesStickyAndNonFilterState() {
        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1),
            List.of("scope")
        );
        table.addCustomToolbarControl(LCTableToolbarControls.clearFilter(table));
        LCTableParams params = new LCTableParams(
            0, 15, "subject", "desc", Collections.singletonMap("subject", "M"), false,
            Collections.singletonMap("scope", "S-1"));

        String html = table.render("/Subjects", params, "", Locale.ENGLISH);

        assertTrue(html.contains("id=\"testTable-clear-filter-toolbar\""));
        assertTrue(html.contains("data-test-action=\"clear-filter\""));
        assertTrue(html.contains("hx-include=\"[name=page],[name=maxRows],[name=sortProp],[name=sortDir],[name=showHiddenCols],[name=scope]\""));
        assertTrue(html.contains("Clear Filter"));
    }

    public void testUnpaginatedStaticTableRendersLocalizedTitleWithoutControls() {
        LCTable<Row> table = new LCTable<>("statistics",
            List.of(textCol("subject", key("subject"), "subject", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.subject)),
            LCTableInMemoryDataSource.allRows(List.of(new Row("S-1", ""), new Row("S-2", "")), List.of()))
            .setTitle(key("subject_enrollment"))
            .setPaginationMode(LCTable.PaginationMode.UNPAGINATED)
            .setFooterMode(LCTable.FooterMode.HIDE_FOOTER);
        LCTableParams params = new LCTableParams(50, 1, "subject", "desc", Map.of());

        String english = table.render("/MainMenu", params, "", Locale.ENGLISH);
        String japanese = table.render("/MainMenu", params, "", Locale.JAPANESE);

        assertTrue(english.contains("Subject Enrollment By Site"));
        assertTrue(japanese.contains("data-testid=\"lctable-title-row\""));
        assertTrue(english.contains("colspan=\"1\""));
        assertTrue(english.contains("S-1") && english.contains("S-2"));
        for (String absent : List.of("<form", "hx-ext", "hx-get", "lctable-toolbar", "lctable-column-filter-row",
                "<tfoot", "sort-header-link", "name=\"page\"")) {
            assertFalse(absent, english.contains(absent));
        }
    }

    public void testUnpaginatedTableRejectsPagedDataSource() {
        LCTable<Row> table = new LCTable<>("testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.subject)),
            new LCTableInMemoryDataSource<>(List.of(new Row("A", ""), new Row("B", "")), List.of()))
            .setPaginationMode(LCTable.PaginationMode.UNPAGINATED);
        try {
            table.render("/test", new LCTableParams(0, 1, "", "asc", Map.of()), "", Locale.ENGLISH);
            fail("Expected unpaginated mode to reject truncated data");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("all rows"));
        }
    }

    public void testUnpaginatedToolbarRendersOnlyCustomControls() {
        LCTable<Row> table = new LCTable<>("testTable",
            List.of(textCol("subject", key("subject"), "subject", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.subject)),
            LCTableInMemoryDataSource.allRows(List.of(new Row("A", "")), List.of()))
            .setPaginationMode(LCTable.PaginationMode.UNPAGINATED);
        table.addCustomToolbarControl((div, ctx) -> div.span().text("Custom action").__());
        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of()), "", Locale.ENGLISH);
        assertTrue(html.contains("Custom action"));
        assertTrue(html.contains("lctable-toolbar"));
        assertFalse(html.contains("select-max-rows"));
        assertFalse(html.contains("page-button"));
    }

    public void testRequestSortMustNameSortableColumnAndHaveValidDirection() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", key("subject"), "subject", 0, row -> row.subject),
            textCol("event", literal("Event"), "event", 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.event)),
            p -> new LCTableData<>(List.of(), 0));
        assertEquals("", new LCTableParams("sortProp=event&sortDir=asc", table).sortProp);
        assertEquals("", new LCTableParams("sortProp=bogus&sortDir=desc", table).sortProp);
        assertEquals("", new LCTableParams("sortProp=subject&sortDir=sideways", table).sortProp);
        assertEquals("", new LCTableParams("sortProp=subject", table).sortProp);
        assertEquals("subject", new LCTableParams("sortProp=subject&sortDir=desc", table).sortProp);
        assertEquals("desc", new LCTableParams("sortProp=subject&sortDir=desc", table).sortDir);
    }

    public void testActiveFiltersCountOnlyNonblankDeclaredColumnsIncludingImplicitStatus() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", literal("Subject label"), null, 0, row -> row.subject),
            textCol("status", literal("Implicit status"), null, 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.event),
            textCol("blank", literal("Blank label"), null, 0, row -> row.subject),
            textCol("empty", literal("Empty label"), null, 0, row -> row.subject),
            textCol("null", literal("Null label"), null, 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.subject),
            textCol("absent", literal("Absent label"), null, 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(), 0));
        Map<String, String> filters = new LinkedHashMap<>();
        // Deliberately reverse request order: tooltip names must follow declared column order.
        filters.put("status", "available");
        filters.put("subject", " M-001 ");
        filters.put("blank", " \t\n\u2003");
        filters.put("empty", "");
        filters.put("null", null);
        filters.put("unknown", "not a declared column");

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", filters), "", Locale.ENGLISH);
        String tooltip = activeFiltersTooltip(html);
        String visible = filterSection(tooltip, false);

        assertEquals("Filters: 2", elementText(activeFiltersIndicator(html)));
        assertTrue(visible, visible.contains("2 Visible Filters:"));
        assertOrdered(visible, "Subject label", "Implicit status");
        assertEquals(2, occurrences(visible, "<li>"));
        for (String excluded : List.of("Blank label", "Empty label", "Null label", "Absent label", "unknown",
                "M-001", "available", "not a declared column")) {
            assertFalse(excluded, tooltip.contains(excluded));
        }
        assertEquals("No Hidden Filters", elementText(filterSection(tooltip, true)));
        assertTrue(activeFiltersIndicator(html).contains("class=\"lc-active-filters lc-active-filters-active\""));
        assertFalse(activeFiltersIndicator(html).contains("lc-active-filters-warning"));
    }

    public void testActiveFiltersLocalizeColumnLabelsWithEnglishIndicatorFallback() {
        LCTable<Row> table = new LCTable<>("testTable",
            List.of(textCol("subject", key("subject"), null, 0, row -> row.subject)),
            params -> new LCTableData<>(List.of(), 0));
        LCTableParams params = new LCTableParams(0, 15, "", "asc", Map.of("subject", "M-001"));

        String english = table.render("/test", params, "", Locale.ENGLISH);
        String japanese = table.render("/test", params, "", Locale.JAPANESE);

        assertEquals("Filters: 1", elementText(activeFiltersIndicator(english)));
        assertEquals("Filters: 1", elementText(activeFiltersIndicator(japanese)));
        assertEquals("1 Visible Filter: Subject", elementText(filterSection(activeFiltersTooltip(english), false)));
        assertEquals("1 Visible Filter: \u88ab\u9a13\u8005",
            elementText(filterSection(activeFiltersTooltip(japanese), false)));
        assertFalse(activeFiltersTooltip(japanese).contains("Subject"));
        assertEquals("No Hidden Filters", elementText(filterSection(activeFiltersTooltip(japanese), true)));
    }

    public void testActiveFilterTooltipEscapesVisibleAndHiddenColumnNames() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", literal("<script>visible & name</script>"), null, 0, row -> row.subject),
            textColHidden("details", literal("<img>hidden & name</img>"), null, 0, row -> row.event)),
            params -> new LCTableData<>(List.of(), 0));

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc",
            Map.of("subject", "A", "details", "B")), "", Locale.ENGLISH);
        String tooltip = activeFiltersTooltip(html);

        assertTrue(tooltip, filterSection(tooltip, false).contains("&lt;script&gt;visible &amp; name&lt;/script&gt;"));
        assertTrue(tooltip, filterSection(tooltip, true).contains("&lt;img&gt;hidden &amp; name&lt;/img&gt;"));
        assertFalse(tooltip.contains("<script>"));
        assertFalse(tooltip.contains("<img>"));
        assertEquals(2, occurrences(tooltip, "<li>"));
    }

    public void testHiddenColumnToggleReclassifiesActiveFiltersAndWarning() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", literal("Subject"), null, 0, row -> row.subject),
            textColHidden("details", literal("Details"), null, 0, row -> row.event),
            textColHidden("event", literal("Event"), null, 0, row -> row.event)),
            params -> new LCTableData<>(List.of(), 0));
        Map<String, String> filters = Map.of("subject", "A", "details", "B", "event", "C");

        String collapsed = table.render("/test", new LCTableParams(0, 15, "", "asc", filters, false), "", Locale.ENGLISH);
        String expanded = table.render("/test", new LCTableParams(0, 15, "", "asc", filters, true), "", Locale.ENGLISH);
        String collapsedTooltip = activeFiltersTooltip(collapsed);
        String expandedTooltip = activeFiltersTooltip(expanded);

        assertEquals("Filters: 3", elementText(activeFiltersIndicator(collapsed)));
        assertEquals("Filters: 3", elementText(activeFiltersIndicator(expanded)));
        assertTrue(activeFiltersIndicator(collapsed).contains("class=\"lc-active-filters lc-active-filters-active lc-active-filters-warning\""));
        assertTrue(activeFiltersIndicator(collapsed).contains("lc-active-filters-warning"));
        assertFalse(filterSection(collapsedTooltip, false).contains("lc-active-filters-warning"));
        assertEquals("1 Visible Filter: Subject", elementText(filterSection(collapsedTooltip, false)));
        assertEquals("2 Hidden Filters: Details Event", elementText(filterSection(collapsedTooltip, true)));
        assertTrue(filterSection(collapsedTooltip, true).contains("lc-active-filters-warning"));
        assertFalse(renderedElement(collapsed, "tr", "data-testid", "lctable-column-header-row").contains("Details"));
        assertFalse(renderedElement(collapsed, "tr", "data-testid", "lctable-column-header-row").contains("Event"));
        // Visible means rendered, including columns outside a possible browser viewport.
        assertOrdered(renderedElement(expanded, "tr", "data-testid", "lctable-column-header-row"), "Subject", "Details", "Event");
        assertEquals("3 Visible Filters: Subject Details Event", elementText(filterSection(expandedTooltip, false)));
        assertEquals("No Hidden Filters", elementText(filterSection(expandedTooltip, true)));
        assertTrue(activeFiltersIndicator(expanded).contains("class=\"lc-active-filters lc-active-filters-active\""));
        assertFalse(activeFiltersIndicator(expanded).contains("lc-active-filters-warning"));
        assertFalse(expandedTooltip.contains("lc-active-filters-warning"));
        assertTrue(renderedElement(collapsed, "a", "data-testid", "toggle-hidden-columns-button").contains("showHiddenCols=true"));
        assertTrue(renderedElement(expanded, "a", "data-testid", "toggle-hidden-columns-button").contains("data-test-action=\"hide\""));

        String hiddenOnly = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of("details", "B")), "", Locale.ENGLISH);
        assertEquals("No Visible Filters", elementText(filterSection(activeFiltersTooltip(hiddenOnly), false)));
        assertEquals("1 Hidden Filter: Details", elementText(filterSection(activeFiltersTooltip(hiddenOnly), true)));
        assertTrue(activeFiltersIndicator(hiddenOnly).contains("class=\"lc-active-filters lc-active-filters-active lc-active-filters-warning\""));
        assertTrue(activeFiltersIndicator(hiddenOnly).contains("lc-active-filters-warning"));
    }

    public void testZeroActiveFiltersRenderBothEmptySectionsWithoutWarningOrLists() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", literal("Subject"), null, 0, row -> row.subject),
            textColHidden("details", literal("Details"), null, 0, row -> row.event)),
            params -> new LCTableData<>(List.of(), 0));

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of()), "", Locale.ENGLISH);
        String indicator = activeFiltersIndicator(html);
        String tooltip = activeFiltersTooltip(html);

        assertEquals("Filters: 0", elementText(indicator));
        assertTrue(indicator.contains("class=\"lc-active-filters\""));
        assertFalse(indicator.contains("lc-active-filters-active"));
        assertEquals("No Visible Filters", elementText(filterSection(tooltip, false)));
        assertEquals("No Hidden Filters", elementText(filterSection(tooltip, true)));
        assertOrdered(tooltip, "No Visible Filters", "No Hidden Filters");
        assertEquals(2, occurrences(tooltip, "class=\"lc-filter-section\""));
        assertFalse(tooltip.contains("<ul"));
        assertFalse(tooltip.contains("<li"));
        assertFalse(indicator.contains("lc-active-filters-warning"));
        assertFalse(tooltip.contains("lc-active-filters-warning"));
        assertTrue(indicator.contains("tabindex=\"0\""));
        assertTrue(indicator.contains("aria-describedby=\"lc-tooltip\""));
        assertTrue(indicator.contains("data-tooltip-source=\"testTable-panel-active-filters-tooltip\""));
        assertTrue(tooltip.contains("hidden=\"hidden\""));
    }

    public void testPaginatedToolbarOrdersIndicatorAndConditionalCustomControlsWithoutTrailingSeparator() {
        LCTable<Row> table = new LCTable<>("testTable", List.of(
            textCol("subject", literal("Subject"), null, 0, row -> row.subject),
            textColHidden("details", literal("Details"), null, 0, row -> row.event)),
            params -> new LCTableData<>(List.of(), 0));
        table.addCustomToolbarControl(ctx -> false, (div, ctx) -> fail("Absent control must not render"));
        table.addCustomToolbarControl((div, ctx) -> div.span().text("First action").__());
        table.addCustomToolbarControl(ctx -> ctx.showHiddenCols, (div, ctx) -> div.span().text("Conditional action").__());
        table.addCustomToolbarControl((div, ctx) -> div.span().text("Last action").__());
        table.addCustomToolbarControl(ctx -> false, (div, ctx) -> fail("Trailing absent control must not render"));

        for (boolean showHidden : List.of(false, true)) {
            String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of(), showHidden), "", Locale.ENGLISH);
            String toolbar = renderedElement(html, "div", "class", "toolbar-container");
            assertOrdered(toolbar, "<nav", "dropdown-list", "toggle-hidden-columns-button", "active-filters-indicator",
                "<div id=\"testTable-panel-active-filters-tooltip\"", "First action", "Last action");
            assertEquals(showHidden ? 6 : 5, occurrences(toolbar, "class=\"toolbar-separator\""));
            if (showHidden) {
                assertOrdered(toolbar, "First action", "Conditional action", "Last action");
            } else {
                assertFalse(toolbar.contains("Conditional action"));
            }
            assertTrue(toolbar, toolbar.lastIndexOf("toolbar-separator") < toolbar.indexOf("Last action"));
        }
    }

    public void testUnpaginatedFilterableToolbarRendersZeroIndicatorBeforeConditionalControls() {
        LCTable<Row> table = new LCTable<Row>("testTable",
            List.of(textCol("subject", literal("Subject"), null, 0, row -> row.subject)),
            LCTableInMemoryDataSource.allRows(List.of(), List.of()))
            .setPaginationMode(LCTable.PaginationMode.UNPAGINATED);
        String withoutCustomControls = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of()), "", Locale.ENGLISH);
        assertTrue(withoutCustomControls.contains("data-testid=\"lctable-toolbar\""));
        assertEquals("Filters: 0", elementText(activeFiltersIndicator(withoutCustomControls)));
        assertFalse(renderedElement(withoutCustomControls, "div", "class", "toolbar-container").contains("toolbar-separator"));

        table.addCustomToolbarControl(ctx -> false, (div, ctx) -> fail("Absent control must not render"));
        table.addCustomToolbarControl((div, ctx) -> div.span().text("First action").__());
        table.addCustomToolbarControl((div, ctx) -> div.span().text("Last action").__());
        table.addCustomToolbarControl(ctx -> false, (div, ctx) -> fail("Trailing absent control must not render"));

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of()), "", Locale.ENGLISH);
        String toolbar = renderedElement(html, "div", "class", "toolbar-container");

        assertEquals("Filters: 0", elementText(activeFiltersIndicator(html)));
        assertTrue(html.contains("data-testid=\"lctable-column-filter-row\""));
        assertOrdered(toolbar, "active-filters-indicator", "toolbar-separator", "First action", "Last action");
        assertEquals(2, occurrences(toolbar, "class=\"toolbar-separator\""));
        assertTrue(toolbar, toolbar.lastIndexOf("toolbar-separator") < toolbar.indexOf("Last action"));
        assertFalse(toolbar.contains("<nav"));
        assertFalse(toolbar.contains("select-max-rows"));
        assertFalse(toolbar.contains("page-button"));
    }

    public void testUnpaginatedImplicitActiveFilterRendersToolbarWithoutFilterInputsOrPagination() {
        LCTable<Row> table = new LCTable<Row>("testTable",
            List.of(textCol("status", literal("Implicit status"), null, 0, VISIBLE, NOT_SORTABLE, NO_FILTER, row -> row.event)),
            LCTableInMemoryDataSource.allRows(List.of(), List.of()))
            .setPaginationMode(LCTable.PaginationMode.UNPAGINATED);

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Map.of("status", "available")), "", Locale.ENGLISH);
        String toolbar = renderedElement(html, "div", "class", "toolbar-container");

        assertTrue(html.contains("data-testid=\"lctable-toolbar\""));
        assertEquals("Filters: 1", elementText(activeFiltersIndicator(html)));
        assertEquals("1 Visible Filter: Implicit status", elementText(filterSection(activeFiltersTooltip(html), false)));
        assertEquals(0, occurrences(toolbar, "toolbar-separator"));
        assertFalse(html.contains("lctable-column-filter-row"));
        assertFalse(toolbar.contains("<nav"));
        assertFalse(toolbar.contains("select-max-rows"));
        assertFalse(toolbar.contains("page-button"));
    }

    private static String activeFiltersIndicator(String html) {
        return renderedElement(html, "span", "data-testid", "active-filters-indicator");
    }

    private static String activeFiltersTooltip(String html) {
        return renderedElement(html, "div", "id", "testTable-panel-active-filters-tooltip");
    }

    private static String filterSection(String tooltip, boolean hidden) {
        int first = tooltip.indexOf("class=\"lc-filter-section");
        int attribute = hidden ? tooltip.indexOf("class=\"lc-filter-section", first + 1) : first;
        assertTrue(tooltip, attribute >= 0);
        return renderedElementAt(tooltip, "div", tooltip.lastIndexOf("<div", attribute));
    }

    private static String renderedElement(String html, String tag, String attribute, String value) {
        int position = html.indexOf(attribute + "=\"" + value + "\"");
        assertTrue("Missing " + attribute + "=" + value + " in " + html, position >= 0);
        int start = html.lastIndexOf("<" + tag, position);
        assertTrue(html, start >= 0);
        return renderedElementAt(html, tag, start);
    }

    private static String renderedElementAt(String html, String tag, int start) {
        Matcher tags = Pattern.compile("<" + tag + "\\b[^>]*>|</" + tag + ">").matcher(html);
        tags.region(start, html.length());
        int depth = 0;
        while (tags.find()) {
            depth += tags.group().startsWith("</") ? -1 : 1;
            if (depth == 0) return html.substring(start, tags.end());
        }
        fail("Unclosed " + tag + " in " + html);
        return "";
    }

    private static String elementText(String html) {
        return html.replaceAll("<[^>]+>", " ").trim().replaceAll("\\s+", " ");
    }

    private static int occurrences(String html, String token) {
        return html.split(Pattern.quote(token), -1).length - 1;
    }

    private static void assertOrdered(String html, String... tokens) {
        int previous = -1;
        for (String token : tokens) {
            int position = html.indexOf(token, previous + 1);
            assertTrue("Expected " + token + " after position " + previous + " in " + html, position > previous);
            previous = position;
        }
    }

    private static final class Row {
        private final String subject;
        private final String event;

        private Row(String subject, String event) {
            this.subject = subject;
            this.event = event;
        }
    }
}
