/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 */
package org.akaza.openclinica.lctable;

import junit.framework.TestCase;
import org.akaza.openclinica.lctable.LCPopup.PopupAction;
import org.akaza.openclinica.lctable.LCPopup.PopupItemLayout;
import org.akaza.openclinica.lctable.LCPopup.PopupItemRenderer;
import org.xmlet.htmlapifaster.Div;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.NO_FILTER;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.SafeUrl.url;

public class LCTableTest extends TestCase {

    public void testRendersRowCellPopupAndActionTestAttributes() {
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
            "TestPopup"
        );

        LCTableColumnDef<Row> popupColumn = customTdCol(
            "event", "Event", 0, NOT_SORTABLE, NO_FILTER,
            (td, row) -> popup.render(td, row)
        );
        popupColumn.setTestAttributes(row -> Map.of("event", row.event));

        LCTable<Row> table = new LCTable<>(
            "testTable",
            List.of(textCol("subject", "Subject", 0, row -> row.subject), popupColumn),
            params -> new LCTableData<>(List.of(new Row("M-001", "Baseline")), 1)
        ).setRowTestAttributes(row -> Map.of("subject", row.subject));

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "");

        assertTrue(html.contains("data-test-subject=\"M-001\""));
        assertTrue(html.contains("data-test-event=\"Baseline\""));
        assertTrue(html.contains("data-testid=\"lc-popup-trigger\""));
        assertTrue(html.contains("data-testid=\"lc-popup-header\""));
        assertTrue(html.contains("data-testid=\"lc-popup-body\""));
        assertTrue(html.contains("class=\"lc-popup-item\""));
        assertTrue(html.contains("data-testid=\"lc-popup-item\""));
        assertTrue(html.contains("data-test-index=\"1\""));
        assertTrue(html.contains("data-test-action=\"view\""));
    }

    public void testOmitsNullAndEmptyTestAttributeValues() {
        LCTableColumnDef<Row> column = textCol("subject", "Subject", 0, row -> row.subject);
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

        String html = table.render("/test", new LCTableParams(0, 15, "", "asc", Collections.emptyMap()), "");

        assertTrue(html.contains("data-test-subject=\"M-001\""));
        assertFalse(html.contains("data-test-null-value"));
        assertFalse(html.contains("data-test-empty-value"));
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
