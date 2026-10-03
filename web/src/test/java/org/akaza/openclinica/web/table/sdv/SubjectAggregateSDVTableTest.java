/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.web.table.sdv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.StudySubjectSDVFilter;
import org.akaza.openclinica.dao.StudySubjectSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubjectAggregateSDVTableTest extends TestCase {
    private StudySubjectDAO subjects;
    private EventCRFDAO eventCrfs;
    private StudyDAO studies;
    private StudyGroupDAO groups;
    private StudyEventDAO events;
    private EventDefinitionCRFDAO definitions;
    private CRFDAO crfs;
    private HttpServletRequest request;
    private StudyBean study;
    private EventCRFBean eventCrf;
    private EventDefinitionCRFBean definition;

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        subjects = mock(StudySubjectDAO.class);
        eventCrfs = mock(EventCRFDAO.class);
        studies = mock(StudyDAO.class);
        groups = mock(StudyGroupDAO.class);
        events = mock(StudyEventDAO.class);
        definitions = mock(EventDefinitionCRFDAO.class);
        crfs = mock(CRFDAO.class);
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/app/pages/viewSubjectAggregate");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singleton(Locale.ENGLISH)));
        when(subjects.countAllByStudySDV(eq(21), eq(21), any(StudySubjectSDVFilter.class))).thenReturn(26);
        StudySubjectBean subject = new StudySubjectBean();
        subject.setId(15);
        subject.setStudyId(21);
        subject.setLabel("SUB & 55");
        subject.setUniqueIdentifier("PERSON");
        subject.setStatus(Status.AVAILABLE);
        study = new StudyBean();
        study.setIdentifier("SITE");
        study.setStatus(Status.AVAILABLE);
        when(studies.findByPK(21)).thenReturn(study);
        ArrayList<StudySubjectBean> page = new ArrayList<>();
        page.add(subject);
        when(subjects.findAllByStudySDV(eq(21), eq(21), any(StudySubjectSDVFilter.class),
            any(StudySubjectSDVSort.class), eq(25), eq(50))).thenReturn(page);
        eventCrf = new EventCRFBean();
        eventCrf.setStatus(Status.UNAVAILABLE);
        eventCrf.setStudyEventId(17);
        eventCrf.setCRFVersionId(19);
        ArrayList<EventCRFBean> crfPage = new ArrayList<>();
        crfPage.add(eventCrf);
        when(eventCrfs.getEventCRFsByStudySubject(15, 21, 21)).thenReturn(crfPage);
        when(eventCrfs.countEventCRFsByStudySubject(15, 21, 21)).thenReturn(2);
        StudyEventBean event = new StudyEventBean();
        event.setStudyEventDefinitionId(23);
        when(events.findByPK(17)).thenReturn(event);
        CRFBean crf = new CRFBean();
        crf.setId(24);
        when(crfs.findByVersionId(19)).thenReturn(crf);
        definition = new EventDefinitionCRFBean();
        definition.setId(1);
        definition.setSourceDataVerification(SourceDataVerification.AllREQUIRED);
        when(definitions.findByStudyEventDefinitionIdAndCRFIdAndStudyId(23, 24, 21)).thenReturn(definition);
        StudyGroupBean group = new StudyGroupBean();
        group.setName("Treatment");
        ArrayList<StudyGroupBean> groupPage = new ArrayList<>();
        groupPage.add(group);
        when(groups.getGroupByStudySubject(15, 21, 21)).thenReturn(groupPage);
        when(request.getQueryString()).thenReturn("studyId=21&page=2&maxRows=25");
    }

    public void testCountsActionsAndFilterRoute() {
        when(request.getQueryString()).thenReturn("studyId=21&page=2&maxRows=25&showHiddenCols=true");
        String html = table().render(request);
        assertEquals(10, countBodyCells(html));
        assertTrue(html.contains("SUB &amp; 55"));
        assertTrue(html.contains("Treatment"));
        assertTrue(html.contains("name=\"sdvCheck_15\""));
        assertTrue(html.contains("form=\"sdvForm\""));
        assertTrue(html.contains("formaction=\"/app/pages/sdvStudySubject\""));
        assertTrue(html.contains("theStudySubjectId.value=15"));
        assertTrue(html.contains("q.studySubjectId=SUB%20%26%2055"));
        assertTrue(html.contains("name=\"studyId\" value=\"21\""));
        assertTrue(html.contains("data-test-action=\"hide\""));
    }

    public void testVerifiedAndLockedBehavior() {
        eventCrf.setSdvStatus(true);
        String verified = table().render(request);
        assertTrue(verified.contains("prompt(document.sdvForm,15)"));
        assertFalse(verified.contains("name=\"sdvCheck_15\""));
        study.setStatus(Status.LOCKED);
        String locked = table().render(request);
        assertFalse(locked.contains("prompt(document.sdvForm,15)"));
        assertTrue(locked.contains("icon_DoubleCheck.gif"));
        eventCrf.setSdvStatus(false);
        String unverifiedLocked = table().render(request);
        assertFalse(unverifiedLocked.contains("name=\"sdvCheck_15\""));
        assertFalse(unverifiedLocked.contains("formaction=\"/app/pages/sdvStudySubject\""));
    }

    public void testNotRequiredCrfDoesNotOfferVerification() {
        definition.setSourceDataVerification(SourceDataVerification.NOTREQUIRED);
        String html = table().render(request);
        assertTrue(html.contains("SDV N/A"));
        assertFalse(html.contains("name=\"sdvCheck_15\""));
        assertFalse(html.contains("formaction=\"/app/pages/sdvStudySubject\""));
    }

    public void testFilterAndSortAreWhitelisted() {
        when(subjects.findAllByStudySDV(eq(21), eq(21), any(StudySubjectSDVFilter.class),
            any(StudySubjectSDVSort.class), eq(0), eq(15))).thenAnswer(call -> {
                StudySubjectSDVFilter filter = (StudySubjectSDVFilter) call.getArguments()[2];
                StudySubjectSDVSort sort = (StudySubjectSDVSort) call.getArguments()[3];
                assertTrue(filter.execute("").contains("mss.label"));
                assertFalse(filter.execute("").contains("null"));
                assertEquals(" order by mss.label desc", sort.execute(""));
                return new ArrayList<StudySubjectBean>();
            });
        when(request.getQueryString()).thenReturn("studyId=21&q.studySubjectId=SUB&q.personId=ignored"
            + "&sortProp=studySubjectId&sortDir=desc");
        table().render(request);
    }

    private SubjectAggregateSDVTable table() {
        return new SubjectAggregateSDVTable(subjects, eventCrfs, studies, groups, events, definitions, crfs, 21, Locale.ENGLISH);
    }

    private static int countBodyCells(String html) {
        int start = html.indexOf("data-testid=\"lctable-data\"");
        int end = html.indexOf("</tbody>", start);
        int count = 0;
        for (int i = html.indexOf("data-test-column=\"", start); i >= 0 && i < end;
                i = html.indexOf("data-test-column=\"", i + 18)) count++;
        return count;
    }
}