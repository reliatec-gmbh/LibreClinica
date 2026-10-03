package org.akaza.openclinica.control.admin;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class MenuStatisticsTablesTest extends TestCase {
    private StudyDAO studies;
    private StudySubjectDAO subjects;
    private StudyEventDAO events;
    private HttpServletRequest request;
    private StudyBean current;
    private Map<String, String> rendered;

    @Override
    protected void setUp() {
        studies = mock(StudyDAO.class);
        subjects = mock(StudySubjectDAO.class);
        events = mock(StudyEventDAO.class);
        request = mock(HttpServletRequest.class);
        rendered = new HashMap<>();
        doAnswer(invocation -> {
            rendered.put((String) invocation.getArguments()[0], (String) invocation.getArguments()[1]);
            return null;
        }).when(request).setAttribute(any(String.class), any(String.class));
        when(request.getContextPath()).thenReturn("/LibreClinica");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));
        current = new StudyBean();
        current.setId(10);
        current.setName("Study & Co");
        current.setExpectedTotalEnrollment(4);
        when(subjects.getCountofStudySubjects(current)).thenReturn(3);
        when(subjects.getCountofStudySubjectsAtStudy(current)).thenReturn(5);
        when(events.getCountofEvents(current)).thenReturn(0);
    }

    public void testTopStudyRendersAllWidgetsAndEverySiteWithoutQueryState() {
        ArrayList<StudyBean> sites = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            StudyBean site = new StudyBean();
            site.setId(100 + i);
            site.setName("Site " + i);
            site.setExpectedTotalEnrollment(2);
            sites.add(site);
        }
        when(studies.findAll(10)).thenReturn(sites);
        when(request.getQueryString()).thenReturn("page=20&maxRows=1&sortProp=name&sortDir=desc&q.status=deleted");

        new MenuStatisticsTables(current, studies, subjects, events).populate(request);

        String siteHtml = rendered.get("studySiteStatistics");
        assertTrue(siteHtml.contains("Site 0"));
        assertTrue(siteHtml.contains("Site 19"));
        assertTrue(siteHtml.contains("data-test-column=\"expected-enrollment\""));
        assertTrue(siteHtml.contains("class=\"lctable-title-row\""));
        assertTrue(siteHtml.contains("style=\"width: 6.0rem\""));
        assertTrue(siteHtml.contains("style=\"width: 3.0rem\""));
        assertTrue(siteHtml.contains("style=\"width: 7.0rem\""));
        assertTrue(rendered.get("subjectEventStatusStatistics").contains("style=\"width: 8.0rem\""));
        assertFalse(siteHtml.contains("sort-header-link"));
        assertFalse(siteHtml.contains("<form"));
        assertTrue(rendered.get("studyStatistics").contains("125%"));
        verify(events, times(1)).getCountofEvents(current);
    }

    public void testSiteDoesNotRenderStudyWideTableAndKeepsThreeAndSevenStatuses() {
        current.setParentStudyId(1);
        ArrayList<StudyBean> sites = new ArrayList<>();
        sites.add(current);
        when(studies.findAll(10)).thenReturn(sites);
        when(subjects.getCountofStudySubjectsBasedOnStatus(any(StudyBean.class), any(Status.class))).thenReturn(1);
        when(events.getCountofEventsBasedOnEventStatus(any(StudyBean.class), any(SubjectEventStatus.class))).thenReturn(0);

        new MenuStatisticsTables(current, studies, subjects, events).populate(request);

        assertFalse(rendered.containsKey("studyStatistics"));
        assertTrue(rendered.get("studySubjectStatusStatistics").contains("number-of-study-subjects"));
        assertTrue(rendered.get("subjectEventStatusStatistics").contains("number-of-events"));
        verify(events, times(1)).getCountofEvents(current);
    }
}
