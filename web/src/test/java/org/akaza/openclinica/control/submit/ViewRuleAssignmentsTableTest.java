/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentSort;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyString;

public class ViewRuleAssignmentsTableTest extends TestCase {
    private RuleSetServiceInterface service;
    private ItemFormMetadataDAO metadata;
    private StudyBean study;

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        service = mock(RuleSetServiceInterface.class);
        metadata = mock(ItemFormMetadataDAO.class);
        study = new StudyBean();
        study.setId(37);
        study.setOid("S_TEST");
    }

    public void testToolbarExportsPreserveFiltersAndSortWithoutHtmxOrPageBounds() {
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(0);
        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(
            request("page=3&maxRows=15&q.crfName=A%26B&q.ruleSetRuleStatus=5&sortProp=ruleName&sortDir=desc"));
        for (String format : List.of("csv", "xls", "xml")) {
            int start = html.indexOf("id=\"ruleAssignments-export-" + format + "\"");
            assertTrue(start >= 0);
            String anchor = html.substring(html.lastIndexOf("<a", start), html.indexOf("</a>", start));
            assertTrue(anchor.contains("export=" + format));
            assertTrue(anchor.contains("q.crfName=A%26B"));
            assertTrue(anchor.contains("q.ruleSetRuleStatus=5"));
            assertTrue(anchor.contains("sortProp=ruleName"));
            assertTrue(anchor.contains("sortDir=desc"));
            assertTrue(anchor.contains("aria-label=\"Download " + format.toUpperCase(Locale.ROOT) + "\""));
            assertFalse(anchor.contains("hx-get"));
            assertFalse(anchor.contains("page="));
            assertFalse(anchor.contains("maxRows="));
        }
    }

    public void testCsvExportsAllFilteredRowsInBatchesInsteadOfRequestedPage() throws Exception {
        RuleSetRuleBean rule = assignment(Status.DELETED);
        rule.getRuleBean().setName("Rule \"quoted\" 雪");
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(300);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(0), eq(256)))
            .thenReturn(Collections.nCopies(256, rule));
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(256), eq(300)))
            .thenReturn(Collections.nCopies(44, rule));
        Download download = new Download();
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(
            exportRequest("csv", "page=8&maxRows=15&q.crfName=CRF_A&q.ruleSetRuleStatus=5&q.studyId=999&sortProp=ruleName&sortDir=desc"), download.response);
        String csv = download.text();
        assertEquals(300, csv.split("\r\n").length);
        assertTrue(csv.contains("\"Rule \"\"quoted\"\" 雪\""));
        assertFalse(csv.contains("DownloadRuleSetXml"));
        assertFalse(csv.contains("<table"));
        assertFalse(csv.contains(",\r\n"));
        org.mockito.ArgumentCaptor<ViewRuleAssignmentFilter> filters = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentFilter.class);
        org.mockito.ArgumentCaptor<ViewRuleAssignmentSort> sort = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentSort.class);
        verify(service).getWithFilterAndSort(filters.capture(), sort.capture(), eq(0), eq(256));
        verify(service).getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(256), eq(300));
        assertTrue(filters.getValue().execute("").contains("rs.study_id = 37"));
        assertTrue(filters.getValue().execute("").contains("rsr.status_id = 5"));
        assertTrue(filters.getValue().execute("").contains("CRF_A"));
        assertFalse(filters.getValue().execute("").contains("999"));
        assertTrue(sort.getValue().execute("").contains("rname desc"));
        verify(service, times(2)).getObjects(rule.getRuleSetBean());
        verify(download.response).setContentType("text/plain;charset=UTF-8");
        verify(download.response).setHeader(eq("Content-Disposition"), org.mockito.Matchers.matches("attachment; filename=\"rulesS_TEST-[0-9]+\\.txt\""));
        verify(download.response).setHeader("Cache-Control", "no-store");
    }

    public void testXlsHasSeventeenLegacyStringColumnsIncludingHiddenAndMissingValues() throws Exception {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        rule.getRuleBean().setDescription(null);
        DiscrepancyNoteActionBean first = new DiscrepancyNoteActionBean();
        first.setExpressionEvaluatesTo(true);
        first.setMessage("First");
        DiscrepancyNoteActionBean second = new DiscrepancyNoteActionBean();
        second.setExpressionEvaluatesTo(false);
        second.setMessage("Second");
        rule.setActions(List.of(first, second));
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));
        Download download = new Download();
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("xls", "page=9"), download.response);
        HSSFWorkbook book = new HSSFWorkbook(new ByteArrayInputStream(download.bytes.toByteArray()));
        assertEquals("JMesa Export", book.getSheetName(0));
        HSSFRow header = book.getSheetAt(0).getRow(0);
        HSSFRow data = book.getSheetAt(0).getRow(1);
        assertEquals(17, header.getPhysicalNumberOfCells());
        assertEquals(17, data.getPhysicalNumberOfCells());
        assertEquals("CRF&#160;&#160;&#160;&#160;&#160;", header.getCell((short) 4).toString().trim());
        assertEquals("CRF Validations", header.getCell((short) 13).toString().trim());
        assertEquals("", data.getCell((short) 1).toString());
        assertEquals("I_ITEM", data.getCell((short) 2).toString());
        assertEquals("Rule", data.getCell((short) 8).toString());
        assertEquals("R-1", data.getCell((short) 9).toString());
        assertEquals(Status.AVAILABLE.getI18nDescription(Locale.ENGLISH), data.getCell((short) 10).toString());
        assertEquals("", data.getCell((short) 11).toString());
        assertEquals("", data.getCell((short) 13).toString());
        assertEquals("true - false", data.getCell((short) 14).toString());
        assertEquals(first.getActionType().getDescription() + " ; " + second.getActionType().getDescription(), data.getCell((short) 15).toString());
        assertEquals(first.getSummary() + " ; " + second.getSummary(), data.getCell((short) 16).toString());
        assertNull(data.getCell((short) 17));
        verify(download.response).setContentType("application/vnd.ms-excel;charset=UTF-8");
        verify(download.response).setHeader(eq("Content-Disposition"), org.mockito.Matchers.matches("attachment; filename=\"rulesS_TEST-[0-9]+\\.xls\""));
    }

    public void testValidationExportIsPlainYesAndCsvMissingValuesAreNull() throws Exception {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        rule.getRuleBean().setDescription(null);
        ItemBean item = new ItemBean();
        item.setId(42);
        item.setName("Item");
        rule.getRuleSetBean().setItem(item);
        when(metadata.findAllByItemIdAndHasValidations(42)).thenReturn(new java.util.ArrayList<>(List.of(new ItemFormMetadataBean())));
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));
        Download download = new Download();
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("csv", ""), download.response);
        String csv = download.text();
        assertEquals(17, csv.substring(0, csv.length() - 2).split(",", -1).length);
        assertTrue(csv.contains("\"null\""));
        assertTrue(csv.contains("\"yes\""));
        assertFalse(csv.contains("<a"));
        assertFalse(csv.contains("ViewItemDetail"));
    }

    public void testXlsOverflowReturns422BeforeAttachmentOrFetching() throws Exception {
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(65536);
        Download download = new Download();
        StringWriter message = new StringWriter();
        when(download.response.getWriter()).thenReturn(new PrintWriter(message));
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("xls", ""), download.response);
        verify(download.response).setStatus(422);
        assertTrue(message.toString().contains("65,535"));
        assertTrue(message.toString().contains("65536"));
        verify(download.response, never()).setHeader(eq("Content-Disposition"), anyString());
        verify(download.response, never()).getOutputStream();
        verify(service, never()).getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt());
    }

    public void testEmptyExportsAndUnknownFormat() throws Exception {
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(0);
        ViewRuleAssignmentsTable table = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH);
        Download csv = new Download();
        table.export(exportRequest("csv", ""), csv.response);
        assertEquals(0, csv.bytes.size());
        Download xls = new Download();
        table.export(exportRequest("xls", ""), xls.response);
        HSSFWorkbook book = new HSSFWorkbook(new ByteArrayInputStream(xls.bytes.toByteArray()));
        assertEquals(1, book.getSheetAt(0).getPhysicalNumberOfRows());
        Download xml = new Download();
        table.export(exportRequest("xml", ""), xml.response);
        assertTrue(xml.text().contains("<RuleImport/>"));
        Download unknown = new Download();
        table.export(exportRequest("pdf", ""), unknown.response);
        verify(unknown.response).sendError(eq(400), anyString());
        verify(unknown.response, never()).getOutputStream();
        verify(service, times(3)).getCountWithFilter(any(ViewRuleAssignmentFilter.class));
        verify(service, never()).getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt());
    }

    public void testXmlExportsAllFilteredAssignmentsWithoutDisplayEnrichment() throws Exception {
        List<RuleSetRuleBean> rules = new java.util.ArrayList<>();
        for (int id = 1; id <= 300; id++) {
            RuleSetRuleBean rule = assignment(Status.DELETED);
            rule.setId(id);
            rule.getRuleBean().setId(id);
            rule.getRuleBean().setOid("R_" + id);
            rule.getRuleBean().setDescription("雪 & <description>");
            rules.add(rule);
        }
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(300);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(0), eq(256)))
            .thenReturn(rules.subList(0, 256));
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(256), eq(300)))
            .thenReturn(rules.subList(256, 300));
        Download download = new Download();
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("xml",
            "page=8&maxRows=15&q.crfName=CRF_A&q.ruleSetRuleStatus=5&q.studyId=999&sortProp=ruleName&sortDir=desc&ruleSetRuleIds=999"), download.response);
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document xml = factory.newDocumentBuilder().parse(new ByteArrayInputStream(download.bytes.toByteArray()));
        assertEquals("RuleImport", xml.getDocumentElement().getNodeName());
        assertEquals(1, xml.getElementsByTagName("RuleAssignment").getLength());
        assertEquals(300, xml.getElementsByTagName("RuleRef").getLength());
        assertEquals(300, xml.getElementsByTagName("RuleDef").getLength());
        assertEquals("雪 & <description>", xml.getElementsByTagName("Description").item(0).getTextContent());
        org.mockito.ArgumentCaptor<ViewRuleAssignmentFilter> filters = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentFilter.class);
        org.mockito.ArgumentCaptor<ViewRuleAssignmentSort> sort = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentSort.class);
        verify(service).getWithFilterAndSort(filters.capture(), sort.capture(), eq(0), eq(256));
        verify(service).getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), eq(256), eq(300));
        assertTrue(filters.getValue().execute("").contains("rs.study_id = 37"));
        assertTrue(filters.getValue().execute("").contains("rsr.status_id = 5"));
        assertTrue(filters.getValue().execute("").contains("CRF_A"));
        assertFalse(filters.getValue().execute("").contains("999"));
        assertTrue(sort.getValue().execute("").contains("rname desc"));
        verify(service, never()).getObjects(any(RuleSetBean.class));
        verify(download.response).setContentType("text/xml;charset=UTF-8");
        verify(download.response).setHeader(eq("Content-Disposition"), org.mockito.Matchers.matches("attachment; filename=\"rulesS_TEST-[0-9]+\\.xml\""));
        verify(download.response).setHeader("Cache-Control", "no-store");
        verify(download.response, never()).getWriter();
    }

    public void testXmlRejectsCrossStudyAssignmentsBeforeResponseOutput() throws Exception {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        rule.getRuleSetBean().setStudyId(999);
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));
        Download download = new Download();
        new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("xml", ""), download.response);
        verify(download.response).sendError(eq(403), anyString());
        verify(download.response, never()).setHeader(eq("Content-Disposition"), anyString());
        verify(download.response, never()).getOutputStream();
    }

    public void testXmlRejectsRepeatedOrIncompleteBatchesBeforeResponseOutput() throws Exception {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(2);
        for (List<RuleSetRuleBean> batch : List.of(List.of(rule, rule), List.of(rule))) {
            when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
                .thenReturn(batch);
            Download download = new Download();
            try {
                new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).export(exportRequest("xml", ""), download.response);
                fail("Invalid selection should fail before XML delivery");
            } catch (java.io.IOException expected) {
                verify(download.response, never()).getOutputStream();
                verify(download.response, never()).setHeader(eq("Content-Disposition"), anyString());
            }
        }
    }

    public void testServletDownloadBypassesHtmlEvenForHtmxRequest() throws Exception {
        HttpServletRequest downloadRequest = exportRequest("xml", "");
        when(downloadRequest.getHeader("HX-Request")).thenReturn("true");
        Download download = new Download();
        class DownloadServlet extends ViewRuleAssignmentNewServlet {
            void initialize() {
                this.request = downloadRequest;
                this.response = download.response;
                this.currentStudy = study;
                this.locale = Locale.ENGLISH;
                this.ruleSetService = service;
                this.itemFormMetadataDAO = metadata;
            }
        }
        DownloadServlet servlet = new DownloadServlet();
        servlet.initialize();
        servlet.processRequest();
        verify(download.response).setContentType("text/xml;charset=UTF-8");
        verify(download.response, never()).getWriter();
        verify(downloadRequest, never()).setAttribute(anyString(), any());
    }

    private static HttpServletRequest exportRequest(String format, String query) {
        HttpServletRequest request = request("export=" + format + (query.isEmpty() ? "" : "&" + query));
        when(request.getParameter(ViewRuleAssignmentsTable.EXPORT_PARAM)).thenReturn(format);
        return request;
    }

    private static final class Download {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final HttpServletResponse response = mock(HttpServletResponse.class);

        Download() throws Exception {
            when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
                @Override public void write(int value) { bytes.write(value); }
            });
        }

        String text() { return new String(bytes.toByteArray(), StandardCharsets.UTF_8); }
    }

    public void testStatusIsUnfilteredByDefaultAndActionsIncludeSingleXml() {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));
        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(request(null));
        assertTrue(html.contains("DownloadRuleSetXml?ruleSetRuleIds=15"));
        assertTrue(html.contains("RunRuleSet?ruleSetId=10&amp;ruleId=25") || html.contains("RunRuleSet?ruleSetId=10&ruleId=25"));
        assertTrue(html.contains("UpdateRuleSetRule?action=remove"));
        assertTrue(html.contains("id=\"ruleAssignments-remove-15\""));
        assertTrue(html.contains("aria-label=\"Remove\""));
        assertTrue(html.contains("data-tooltip=\"Remove\""));
        assertTrue(html.contains("alt=\"Remove\""));
        assertTrue(html.contains("aria-label=\"Run\""));
        assertFalse(html.contains("ruleAssignments-download-all"));
        assertTrue(html.contains("data-test-action=\"show-more\""));
        assertFalse(html.contains("data-test-column=\"status\""));
        verify(service).getObjects(rule.getRuleSetBean());
        org.mockito.ArgumentCaptor<ViewRuleAssignmentFilter> selected = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentFilter.class);
        verify(service).getCountWithFilter(selected.capture());
        assertFalse(selected.getValue().execute("").contains("rsr.status_id"));
    }

    public void testDeletedRuleOffersRestoreNotRun() {
        RuleSetRuleBean rule = assignment(Status.DELETED);
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));
        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(request("showHiddenCols=true"));
        assertTrue(html.contains("data-test-action=\"restore\""));
        assertFalse(html.contains("data-test-action=\"run\""));
        assertTrue(html.contains("data-test-column=\"status\""));
        assertTrue(html.contains("data-test-column=\"action-type\""));
        assertTrue(html.contains("data-test-action=\"hide\""));
    }

    public void testPageAndFiltersDelegateToExistingService() {
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(0);
        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(
            request("page=2&maxRows=15&q.crfName=CRF_A&q.ruleSetRuleStatus=5&sortProp=ruleName&sortDir=desc"));
        org.mockito.ArgumentCaptor<ViewRuleAssignmentFilter> filters = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentFilter.class);
        org.mockito.ArgumentCaptor<ViewRuleAssignmentSort> sort = org.mockito.ArgumentCaptor.forClass(ViewRuleAssignmentSort.class);
        verify(service).getWithFilterAndSort(filters.capture(), sort.capture(), org.mockito.Matchers.eq(15), org.mockito.Matchers.eq(30));
        assertTrue(filters.getValue().execute("").contains("rs.study_id = 37"));
        assertTrue(filters.getValue().execute("").contains("rsr.status_id = 5"));
        assertTrue(filters.getValue().execute("").contains("CRF_A"));
        assertTrue(sort.getValue().execute("").contains("rname desc"));
        assertTrue(html.contains("No results"));
    }

    public void testEscapesRuleAndActionDataWithoutEscapingTheNestedSummaryTable() {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        rule.getRuleBean().setName("<script>alert(1)</script>");
        ((DiscrepancyNoteActionBean) rule.getActions().get(0)).setMessage("<img src=x>");
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));

        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(request(null));
        assertFalse(html.contains("<script>alert(1)</script>"));
        assertFalse(html.contains("<img src=x>"));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(html.contains("&lt;img src=x&gt;"));
        assertTrue(html.contains("<table"));
    }

    public void testEventTargetHasRunIconWithoutExecutableLink() {
        RuleSetRuleBean rule = assignment(Status.AVAILABLE);
        rule.getRuleSetBean().getTarget().setValue("SE_EVENT.STARTDATE");
        when(service.getCountWithFilter(any(ViewRuleAssignmentFilter.class))).thenReturn(1);
        when(service.getWithFilterAndSort(any(ViewRuleAssignmentFilter.class), any(ViewRuleAssignmentSort.class), anyInt(), anyInt()))
            .thenReturn(List.of(rule));

        String html = new ViewRuleAssignmentsTable(service, metadata, study, Locale.ENGLISH).render(request(null));
        assertTrue(html.contains("bt_ExexuteRules.gif"));
        assertFalse(html.contains("RunRuleSet?ruleSetId="));
        assertTrue(html.contains("DownloadRuleSetXml?ruleSetRuleIds=15"));
    }

    private static HttpServletRequest request(String query) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn(query);
        when(request.getRequestURI()).thenReturn("/ViewRuleAssignment");
        when(request.getContextPath()).thenReturn("");
        when(request.getLocales()).thenReturn(Collections.enumeration(List.of(Locale.ENGLISH)));
        return request;
    }

    private static RuleSetRuleBean assignment(Status status) {
        RuleSetBean set = new RuleSetBean();
        set.setId(10);
        set.setStudyId(37);
        ExpressionBean target = new ExpressionBean();
        target.setValue("I_ITEM");
        set.setTarget(target);
        set.setOriginalTarget(target);
        RuleBean ruleBean = new RuleBean();
        ruleBean.setId(25);
        ruleBean.setName("Rule");
        ruleBean.setOid("R-1");
        ruleBean.setExpression(target);
        RuleSetRuleBean rule = new RuleSetRuleBean();
        rule.setId(15);
        rule.setStatus(status);
        rule.setRuleSetBean(set);
        rule.setRuleBean(ruleBean);
        DiscrepancyNoteActionBean action = new DiscrepancyNoteActionBean();
        action.setMessage("Check");
        action.setRuleSetRule(rule);
        rule.setActions(new java.util.ArrayList<>(List.of(action)));
        return rule;
    }
}
