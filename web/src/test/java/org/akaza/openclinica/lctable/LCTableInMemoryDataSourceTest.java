/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.lctable;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LCTableInMemoryDataSourceTest extends TestCase {

    public void testFiltersSortsPagesAndPreservesSourceList() {
        List<Row> source = new ArrayList<>(Arrays.asList(
            new Row("Charlie", "keep", new Date(3_000L)),
            new Row("alice", "keep", new Date(1_000L)),
            new Row("Bob", "drop", new Date(2_000L))
        ));
        List<Row> originalOrder = new ArrayList<>(source);
        LCTableInMemoryDataSource<Row> dataSource = new LCTableInMemoryDataSource<>(source, List.of(
            LCTableInMemoryColumn.text("name", row -> row.name),
            LCTableInMemoryColumn.text("category", row -> row.category),
            LCTableInMemoryColumn.value("created", row -> row.created, Date::toString)
        ));
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("category", "KEEP");

        LCTableData<Row> data = dataSource.apply(new LCTableParams(1, 1, "name", "desc", filters));

        assertEquals(2, data.totalCountWithFilter);
        assertEquals(1, data.pageItems.size());
        assertEquals("Charlie", data.pageItems.get(0).name);
        assertEquals(originalOrder, source);
    }

    public void testFormattedValueFilteringNullsAndUnknownSort() {
        List<Row> source = Arrays.asList(
            new Row("Alpha", null, new Date(0L)),
            new Row(null, "other", null)
        );
        LCTableInMemoryDataSource<Row> dataSource = new LCTableInMemoryDataSource<>(source, List.of(
            LCTableInMemoryColumn.text("name", row -> row.name),
            LCTableInMemoryColumn.text("category", row -> row.category),
            LCTableInMemoryColumn.value("created", row -> row.created, date -> "epoch-" + date.getTime())
        ));

        LCTableData<Row> filtered = dataSource.apply(new LCTableParams(
            0, 15, "unknown", "asc", Collections.singletonMap("created", "EPOCH-0")));
        LCTableData<Row> nullFiltered = dataSource.apply(new LCTableParams(
            0, 15, "", "asc", Collections.singletonMap("category", "missing")));

        assertEquals(1, filtered.totalCountWithFilter);
        assertEquals("Alpha", filtered.pageItems.get(0).name);
        assertEquals(0, nullFiltered.totalCountWithFilter);
    }

    public void testClampsOutOfRangePage() {
        LCTableInMemoryDataSource<Row> dataSource = new LCTableInMemoryDataSource<>(
            List.of(new Row("Alpha", "one", new Date(0L))),
            List.of(LCTableInMemoryColumn.text("name", row -> row.name))
        );

        LCTableData<Row> data = dataSource.apply(new LCTableParams(99, 15, "", "asc", Collections.emptyMap()));

        assertEquals(1, data.totalCountWithFilter);
        assertTrue(data.pageItems.isEmpty());
    }

    private static final class Row {
        private final String name;
        private final String category;
        private final Date created;

        private Row(String name, String category, Date created) {
            this.name = name;
            this.category = category;
            this.created = created;
        }
    }
}
