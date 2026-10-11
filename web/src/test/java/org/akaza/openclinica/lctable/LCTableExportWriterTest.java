/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.lctable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTable.Export.Value.number;
import static org.akaza.openclinica.lctable.LCTable.Export.Value.text;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.LCTableText.literal;

public class LCTableExportWriterTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    private static final class Row {
        final String value;
        final Integer amount;
        Row(String value, Integer amount) { this.value = value; this.amount = amount; }
    }

    public void testExportIsOptInOrderedAndSeparateFromHtml() throws Exception {
        LCTableColumnDef<Row> hidden = LCTableColumnDef.textColHidden("hidden", literal("HTML heading"), "hidden", 0,
            row -> "HTML:" + row.value);
        LCTableColumnDef<Row> amount = textCol("amount", literal("Number"), "amount", 0,
            row -> String.valueOf(row.amount));
        LCTable<Row> table = new LCTable<>("test",
            List.of(hidden, textCol("actions", literal("Actions"), "actions", 0, row -> "not exported"), amount),
            p -> new LCTableData<>(List.of(), 0));
        assertFalse(table.export().isPresent());
        assertSame(hidden, hidden.setCellExporter(key("subject"), row -> text(row.value)));
        amount.setCellExporter(row -> number(row.amount));
        StringWriter csv = new StringWriter();
        table.export().orElseThrow().toCsv(1, (offset, limit) -> List.of(new Row("abc", 4)), csv);
        assertEquals("\"abc\",\"4\"\r\n", csv.toString());
        assertFalse(csv.toString().contains("HTML:"));
        ByteArrayOutputStream xls = new ByteArrayOutputStream();
        table.export().orElseThrow().toXls(0, (offset, limit) -> List.of(),
            ResourceBundleProvider.getWordsBundle(Locale.ENGLISH), Locale.ENGLISH, xls);
        HSSFWorkbook book = new HSSFWorkbook(new ByteArrayInputStream(xls.toByteArray()));
        assertEquals("Subject", book.getSheetAt(0).getRow(0).getCell((short) 0).toString().trim());
        assertEquals("Number", book.getSheetAt(0).getRow(0).getCell((short) 1).toString());
        assertNull(book.getSheetAt(0).getRow(0).getCell((short) 2));
    }

    private static <T> LCTable<T> table(List<LCTableColumnDef<T>> columns) {
        return new LCTable<>("test", columns, p -> new LCTableData<>(List.of(), 0));
    }

    public void testCsvQuotesMissingUnicodeAndSpreadsheetFormulas() throws Exception {
        LCTable<Row> definition = table(List.of(
            LCTableColumnDef.<Row>textCol("value", literal("Value"), "value", 0, row -> row.value).setCellExporter(row -> text(row.value)),
            LCTableColumnDef.<Row>textCol("amount", literal("Number"), "amount", 0, row -> String.valueOf(row.amount)).setCellExporter(row -> number(row.amount))));
        StringWriter writer = new StringWriter();
        definition.export().orElseThrow().toCsv(3,
            (offset, limit) -> List.of(
                new Row("a,\"b\"\n雪", null), new Row(" \t=SUM(1,1)", -2), new Row(null, null)).subList(offset, 3), writer);
        assertEquals("\"a,\"\"b\"\"\n雪\",\"null\"\r\n"
                + "\"' \t=SUM(1,1)\",\"-2\"\r\n\"null\",\"null\"\r\n", writer.toString());
    }

    public void testCsvOutputStreamIsUtf8WithoutBomAndDoesNotCloseStream() throws Exception {
        LCTable<Row> definition = table(List.of(
            LCTableColumnDef.<Row>textCol("value", literal("Value"), "value", 0, row -> row.value).setCellExporter(row -> text(row.value))));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        definition.export().orElseThrow().toCsv(2,
            (offset, limit) -> List.of(new Row("雪", null), new Row(null, null)).subList(offset, 2), output);
        output.write('!');
        assertEquals("\"雪\"\r\n\"null\"\r\n!", new String(output.toByteArray(), StandardCharsets.UTF_8));
        assertEquals('"', output.toByteArray()[0]);
    }

    public void testXlsHeaderTypesMissingAndFormulaSafety() throws Exception {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        LCTable<Row> definition = table(List.of(
            LCTableColumnDef.<Row>textCol("value", key("subject"), "value", 0, row -> row.value).setCellExporter(row -> text(row.value)),
            LCTableColumnDef.<Row>textCol("amount", literal("Number"), "amount", 0, row -> String.valueOf(row.amount)).setCellExporter(row -> number(row.amount))));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ResourceBundle words = ResourceBundleProvider.getWordsBundle(Locale.ENGLISH);
        definition.export().orElseThrow().toXls(2,
            (offset, limit) -> List.of(new Row("=1+1", 42), new Row(null, null)).subList(offset, 2),
            words, Locale.ENGLISH, bytes);
        HSSFWorkbook book = new HSSFWorkbook(new ByteArrayInputStream(bytes.toByteArray()));
        assertEquals("JMesa Export", book.getSheetName(0));
        assertNotNull(book.getSheetAt(0).getRow(0).getCell((short) 1));
        assertEquals("Subject", book.getSheetAt(0).getRow(0).getCell((short) 0).toString().trim());
        assertEquals("'=1+1", book.getSheetAt(0).getRow(1).getCell((short) 0).toString());
        assertEquals(42.0, book.getSheetAt(0).getRow(1).getCell((short) 1).getNumericCellValue());
        assertEquals("", book.getSheetAt(0).getRow(2).getCell((short) 0).toString());
        assertEquals("", book.getSheetAt(0).getRow(2).getCell((short) 1).toString());
    }

    public void testBatchesDoNotFetchPastTotalAndEmptyExportHasHeader() throws Exception {
        List<Integer> offsets = new ArrayList<>();
        LCTable<Integer> definition = table(List.of(
            LCTableColumnDef.<Integer>textCol("index", literal("Index"), "index", 0, String::valueOf).setCellExporter(index -> text(String.valueOf(index)))));
        StringWriter csv = new StringWriter();
        definition.export().orElseThrow().toCsv(258, (offset, limit) -> {
            offsets.add(offset);
            List<Integer> result = new ArrayList<>();
            for (int i = offset; i < offset + limit; i++) result.add(i);
            return result;
        }, csv);
        assertEquals(List.of(0, 256), offsets);
        assertEquals(258, csv.toString().split("\r\n").length);
        ByteArrayOutputStream xls = new ByteArrayOutputStream();
        definition.export().orElseThrow().toXls(0, (offset, limit) -> { fail("No rows should be fetched"); return List.of(); },
            ResourceBundleProvider.getWordsBundle(Locale.ENGLISH), Locale.ENGLISH, xls);
        assertEquals(1, new HSSFWorkbook(new ByteArrayInputStream(xls.toByteArray())).getSheetAt(0).getPhysicalNumberOfRows());
    }

    public void testXlsRejectsOverflowBeforeFetchingOrWriting() throws Exception {
        LCTable<Integer> definition = table(List.of(
            LCTableColumnDef.<Integer>textCol("index", literal("Index"), "index", 0, String::valueOf).setCellExporter(index -> text(String.valueOf(index)))));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            definition.export().orElseThrow().toXls(65536, (offset, limit) -> {
                fail("Overflow must be rejected before fetching"); return List.of();
            }, ResourceBundleProvider.getWordsBundle(Locale.ENGLISH), Locale.ENGLISH, output);
            fail("Expected XLS limit");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("65535"));
        }
        assertEquals(0, output.size());
    }
}
