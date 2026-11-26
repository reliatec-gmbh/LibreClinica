/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import junit.framework.TestCase;
import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableFilterDef;
import org.akaza.openclinica.lctable.LCTableParams;
import org.akaza.openclinica.lctable.LCTableText;
import org.akaza.openclinica.lctable.LCTableUtil;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuditDatabaseServletTest extends TestCase {

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    public void testRendersLocalizedColumnsTimestampValidationAndToolbarClearFilter() {
        DatabaseChangeLogBean row = row("001", "alice", "db/changelog.xml", new Date(0L),
            "9:abc", "create table", "initial schema", "release-1", "4.27");
        TestAuditDatabaseServlet servlet = servlet(null);

        String html = servlet.render(Collections.singletonList(row));

        assertColumn(html, "id", "Id");
        assertColumn(html, "author", "Author");
        assertColumn(html, "file-name", "File Name");
        assertColumn(html, "date-executed", "Date Executed");
        assertColumn(html, "md5-sum", "md5 sum");
        assertColumn(html, "description", "Description");
        assertColumn(html, "comments", "Comments");
        assertColumn(html, "tag", "Tag");
        assertColumn(html, "liquibase", "Liquibase");
        assertTrue(html.contains(LCTableUtil.timestampToString(row.getDataExecuted())));
        assertTimestampInput(html, "");
        assertTrue(html.contains("id=\"databaseChangeLogs-clear-filter-toolbar\""));
        assertTrue(html.contains("data-test-action=\"clear-filter\""));
        assertTrue(html.contains("Clear Filter"));
        assertTrue(html.indexOf("data-testid=\"lctable-toolbar\"") < html.indexOf("Clear Filter"));
    }

    public void testRendersTimestampPartialDateValuesWithoutHtmlPattern() {
        for (String value : List.of("0001", "9999", "2024-02", "2024-02-29", "2024-02-29 23", "2024-02-29 23:59")) {
            TestAuditDatabaseServlet servlet = servlet("q.dataExecuted=" + value.replace(" ", "%20"));
            assertTimestampInput(servlet.render(Collections.emptyList()), value);
        }
    }

    public void testTimestampFactoryDoesNotChangeExistingTextConstructors() {
        LCTableFilterDef.Text timestamp = LCTableFilterDef.timestampFilter();
        assertSame(LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP, timestamp.validator);
        assertEquals("partial-timestamp", timestamp.validator.key);
        assertNull(timestamp.pattern);
        assertSame(LCTableUtil.TIMESTAMP_FILTER_MESSAGE, timestamp.message);

        LCTableFilterDef.Text plain = new LCTableFilterDef.Text();
        assertNull(plain.validator);
        assertNull(plain.pattern);
        assertNull(plain.message);

        LCTableFilterDef.Text patterned = new LCTableFilterDef.Text("[0-9]+", LCTableUtil.TIMESTAMP_FILTER_MESSAGE);
        assertNull(patterned.validator);
        assertEquals("[0-9]+", patterned.pattern);
        assertSame(LCTableUtil.TIMESTAMP_FILTER_MESSAGE, patterned.message);
    }

    public void testLegacyTextFactoriesAndOptionalValidatorRetainRendering() {
        for (LCTableFilterDef.Text plain : List.of(new LCTableFilterDef.Text(),
                LCTableFilterDef.textFilter(), LCTableFilterDef.textFilter(null),
                LCTableFilterDef.textFilter(null, null), new LCTableFilterDef.Text(null, null, null),
                LCTableFilterDef.textFilter(null, null, null))) {
            assertNull(plain.validator);
            assertNull(plain.pattern);
            assertNull(plain.message);
            String input = renderTextInput(plain);
            assertFalse(input.contains("data-lctable-"));
            assertFalse(input.contains(" pattern="));
            assertFalse(input.contains(" title="));
            assertFalse(input.contains("hx-on:htmx:before-request="));
        }
        for (LCTableFilterDef.Text patterned : List.of(new LCTableFilterDef.Text("[0-9]+", null),
                LCTableFilterDef.textFilter("[0-9]+"), LCTableFilterDef.textFilter("[0-9]+", null),
                LCTableFilterDef.textFilter("[0-9]+", null, null))) {
            assertNull(patterned.validator);
            String input = renderTextInput(patterned);
            assertTrue(input.contains("pattern=\"[0-9]+\""));
            assertFalse(input.contains("data-lctable-"));
            assertFalse(input.contains(" title="));
            assertTrue(input.contains("hx-on:htmx:before-request="));
        }
        String input = renderTextInput(LCTableFilterDef.textFilter("[0-9]+", LCTableText.literal("Digits only")));
        assertTrue(input.contains("pattern=\"[0-9]+\""));
        assertTrue(input.contains("title=\"Digits only\""));
        assertFalse(input.contains("data-lctable-"));
    }

    public void testConfiguredValidatorRequiresMessageInConstructorAndFactory() {
        try {
            new LCTableFilterDef.Text(null, null, LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP);
            fail("Configured validator must require a message");
        } catch (NullPointerException expected) {
            assertEquals("message is required for a configured validator", expected.getMessage());
        }
        try {
            LCTableFilterDef.textFilter("[0-9]+", null, LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP);
            fail("Factory must require a message for a configured validator");
        } catch (NullPointerException expected) {
            assertEquals("message is required for a configured validator", expected.getMessage());
        }
    }

    public void testPatternAndCustomValidatorRenderEscapedMetadataWithSingleTitle() {
        LCTableText message = LCTableText.literal("Use \"date\" & <time> 'format'");
        String escapedMessage = HtmlUtils.htmlEscape(message.resolve(
            ResourceBundleProvider.getWordsBundle(Locale.ENGLISH), Locale.ENGLISH));
        for (LCTableFilterDef.Text filter : List.of(
                new LCTableFilterDef.Text("[0-9]+", message, LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP),
                LCTableFilterDef.textFilter("[0-9]+", message, LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP))) {
            assertSame(LCTableFilterDef.ClientValidator.PARTIAL_TIMESTAMP, filter.validator);
            String input = renderTextInput(filter);
            assertTrue(input.contains("pattern=\"[0-9]+\""));
            assertTrue(input.contains("data-lctable-validator=\"partial-timestamp\""));
            assertTrue(input.contains("data-lctable-validation-message=\"" + escapedMessage + "\""));
            assertTrue(input.contains("title=\"" + escapedMessage + "\""));
            assertEquals(input.indexOf(" title="), input.lastIndexOf(" title="));
            assertFalse(input.contains("data-lctable-timestamp-message"));
            assertFalse(input.contains("<time>"));
            assertTrue(input.contains("hx-on:htmx:before-request="));
        }
    }

    public void testFiltersSortsAndPaginatesInMemoryRows() {
        DatabaseChangeLogBean alpha = row("001", "Alice", "alpha.xml", new Date(1_000L),
            "a", "first", "keep", "v1", "4.0");
        DatabaseChangeLogBean beta = row("002", "Bob", "beta.xml", new Date(2_000L),
            "b", "second", "keep", "v2", "4.1");
        DatabaseChangeLogBean gamma = row("003", "Carol", "gamma.xml", new Date(3_000L),
            "c", "third", "drop", "v3", "4.2");
        TestAuditDatabaseServlet servlet = servlet(
            "q.comments=keep&sortProp=author&sortDir=desc&maxRows=1&page=2");

        String html = servlet.render(Arrays.asList(alpha, beta, gamma));

        assertTrue(html.contains("Alice"));
        assertFalse(html.contains("Bob"));
        assertFalse(html.contains("Carol"));
        assertTrue(html.contains("name=\"q.comments\" value=\"keep\""));
        assertTrue(html.contains("data-test-sort=\"desc\""));
    }

    private static void assertTimestampInput(String html, String value) {
        int inputStart = html.indexOf("<input type=\"text\" name=\"q.dataExecuted\"");
        assertTrue("Missing timestamp input", inputStart >= 0);
        int inputEnd = html.indexOf('>', inputStart);
        assertTrue("Missing timestamp input closing bracket", inputEnd > inputStart);
        String input = html.substring(inputStart, inputEnd);
        String message = HtmlUtils.htmlEscape(LCTableUtil.TIMESTAMP_FILTER_MESSAGE.resolve(
            ResourceBundleProvider.getWordsBundle(Locale.ENGLISH), Locale.ENGLISH));
        assertTrue(input.contains("value=\"" + value + "\""));
        assertTrue(input.contains("data-lctable-validator=\"partial-timestamp\""));
        assertTrue(input.contains("data-lctable-validation-message=\"" + message + "\""));
        assertFalse(input.contains("data-lctable-timestamp-message"));
        assertTrue(input.contains("title=\"" + message + "\""));
        assertEquals(input.indexOf(" title="), input.lastIndexOf(" title="));
        assertFalse(input.contains(" pattern="));
        assertTrue(input.contains("hx-on:htmx:before-request=\"if(!this.validity.valid){event.preventDefault();}\""));
    }

    private static String renderTextInput(LCTableFilterDef.Text filter) {
        LCTableColumnDef<String> column = LCTableColumnDef.textCol("value", LCTableText.literal("Value"),
            null, 0, LCTableColumnDef.VISIBLE, LCTableColumnDef.NOT_SORTABLE, filter, value -> value);
        LCTable<String> table = new LCTable<>("validationTest", List.of(column),
            params -> new LCTableData<>(List.of(), 0));
        String html = table.render("/test", new LCTableParams("", table), "", Locale.ENGLISH);
        int inputStart = html.indexOf("<input type=\"text\" name=\"q.value\"");
        assertTrue("Missing text input", inputStart >= 0);
        int inputEnd = html.indexOf('>', inputStart);
        assertTrue("Missing text input closing bracket", inputEnd > inputStart);
        return html.substring(inputStart, inputEnd);
    }

    private static TestAuditDatabaseServlet servlet(String queryString) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getRequestURI()).thenReturn("/AuditDatabase");
        when(request.getContextPath()).thenReturn("");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));

        TestAuditDatabaseServlet servlet = new TestAuditDatabaseServlet();
        servlet.setRequest(request);
        servlet.locale = Locale.ENGLISH;
        return servlet;
    }

    private static DatabaseChangeLogBean row(String id, String author, String fileName, Date dataExecuted,
            String md5Sum, String description, String comments, String tag, String liquibase) {
        DatabaseChangeLogBean row = new DatabaseChangeLogBean();
        row.setId(id);
        row.setAuthor(author);
        row.setFileName(fileName);
        row.setDataExecuted(dataExecuted);
        row.setMd5Sum(md5Sum);
        row.setDescription(description);
        row.setComments(comments);
        row.setTag(tag);
        row.setLiquibase(liquibase);
        return row;
    }

    private static void assertColumn(String html, String testName, String displayName) {
        assertTrue("Missing data-test-column=" + testName,
            html.contains("data-test-column=\"" + testName + "\""));
        assertTrue("Missing localized column title " + displayName,
            html.contains(displayName));
    }

    private static final class TestAuditDatabaseServlet extends AuditDatabaseServlet {
        private void setRequest(HttpServletRequest request) {
            this.request = request;
        }

        private String render(List<DatabaseChangeLogBean> rows) {
            return renderAuditDatabaseLCTable(rows);
        }
    }
}
