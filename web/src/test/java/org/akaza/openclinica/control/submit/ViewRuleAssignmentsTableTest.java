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

import javax.servlet.http.HttpServletRequest;

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
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        rule.setActions(List.of(action));
        return rule;
    }
}
