/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 */
package org.akaza.openclinica.lctable;

import junit.framework.TestCase;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.LCPopup.PopupAction;
import org.akaza.openclinica.lctable.LCPopup.PopupItemLayout;
import org.akaza.openclinica.lctable.LCPopup.PopupItemRenderer;
import org.xmlet.htmlapifaster.Div;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.mockito.Mockito.mock;

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

    private static final class Row {
        private final String subject;
        private final String event;

        private Row(String subject, String event) {
            this.subject = subject;
            this.event = event;
        }
    }
}
