package org.akaza.openclinica.web.table.sdv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.dao.EventCRFSDVFilter;
import org.akaza.openclinica.dao.EventCRFSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventCRFSDVTableTest extends TestCase {
    private EventCRFDAO eventCrfs;
    private StudySubjectDAO studySubjects;
    private StudyEventDAO events;
    private StudyEventDefinitionDAO eventDefinitions;
    private SubjectDAO subjects;
    private StudyDAO studies;
    private EventDefinitionCRFDAO eventDefinitionCrfs;
    private CRFVersionDAO versions;
    private CRFDAO crfs;
    private HttpServletRequest request;

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        eventCrfs = mock(EventCRFDAO.class);
        studySubjects = mock(StudySubjectDAO.class);
        events = mock(StudyEventDAO.class);
        eventDefinitions = mock(StudyEventDefinitionDAO.class);
        subjects = mock(SubjectDAO.class);
        studies = mock(StudyDAO.class);
        eventDefinitionCrfs = mock(EventDefinitionCRFDAO.class);
        versions = mock(CRFVersionDAO.class);
        crfs = mock(CRFDAO.class);
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/app/pages/viewAllSubjectSDVtmp");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singleton(Locale.ENGLISH)));
        when(eventCrfs.getCountWithFilter(org.mockito.Matchers.eq(21), org.mockito.Matchers.eq(21), any(EventCRFSDVFilter.class))).thenReturn(26);
    }

    public void testRendersSixteenColumnsAndExternalFormActions() {
        EventCRFBean eventCrf = new EventCRFBean();
        eventCrf.setId(55);
        eventCrf.setStudySubjectId(15);
        eventCrf.setStudyEventId(17);
        eventCrf.setCRFVersionId(19);
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setId(15);
        studySubject.setSubjectId(18);
        studySubject.setStudyId(21);
        studySubject.setLabel("SUB-55");
        studySubject.setStatus(Status.AVAILABLE);
        StudyEventBean event = new StudyEventBean();
        event.setId(17);
        event.setStudyEventDefinitionId(23);
        event.setStatus(Status.AVAILABLE);
        StudyEventDefinitionBean definition = new StudyEventDefinitionBean();
        definition.setName("Visit");
        StudyBean study = new StudyBean();
        study.setIdentifier("SITE-1");
        study.setStatus(Status.AVAILABLE);
        SubjectBean subject = new SubjectBean();
        subject.setUniqueIdentifier("PERSON-1");
        CRFVersionBean version = new CRFVersionBean();
        version.setCrfId(24);
        version.setName("v1");
        CRFBean crf = new CRFBean();
        crf.setName("Form");
        when(studySubjects.findByPK(15)).thenReturn(studySubject);
        when(events.findByPK(17)).thenReturn(event);
        when(eventDefinitions.findByPK(23)).thenReturn(definition);
        when(studies.findByPK(21)).thenReturn(study);
        when(subjects.findByPK(18)).thenReturn(subject);
        when(versions.findByPK(19)).thenReturn(version);
        when(crfs.findByPK(24)).thenReturn(crf);
        ArrayList<EventCRFBean> page = new ArrayList<>();
        page.add(eventCrf);
        when(eventCrfs.getWithFilterAndSort(org.mockito.Matchers.eq(21), org.mockito.Matchers.eq(21), any(EventCRFSDVFilter.class),
            any(EventCRFSDVSort.class), org.mockito.Matchers.eq(25), org.mockito.Matchers.eq(50))).thenReturn(page);
        when(request.getQueryString()).thenReturn("studyId=21&page=2&maxRows=25&showHiddenCols=true");

        String html = table().render(request);

        assertEquals(html, 16, count(html, "data-test-column=\"", "data-testid=\"lctable-data\""));
        assertTrue(html.contains("name=\"sdvCheck_55\""));
        assertTrue(html.contains("form=\"sdvForm\""));
        assertTrue(html.contains("formaction=\"/app/pages/handleSDVGet\""));
        assertTrue(html.contains("document.sdvForm.crfId.value=55;"));
        assertTrue(html.contains("SUB-55"));
        assertTrue(html.contains("Form/ v1"));
        assertTrue(html.contains("name=\"studyId\" value=\"21\""));
        assertTrue(html.contains("data-test-action=\"hide\""));
    }

    public void testWhitelistsFiltersAndDoesNotPassUnsupportedSortToDao() {
        when(eventCrfs.getWithFilterAndSort(org.mockito.Matchers.eq(21), org.mockito.Matchers.eq(21), any(EventCRFSDVFilter.class),
            any(EventCRFSDVSort.class), org.mockito.Matchers.eq(0), org.mockito.Matchers.eq(15))).thenAnswer(call -> {
                EventCRFSDVFilter filter = (EventCRFSDVFilter) call.getArguments()[2];
                EventCRFSDVSort sort = (EventCRFSDVSort) call.getArguments()[3];
                assertTrue(filter.execute("").contains("ss.label"));
                assertFalse(filter.execute("").contains("null"));
                assertTrue(sort.getSorts().isEmpty());
                return new ArrayList<EventCRFBean>();
            });
        when(request.getQueryString()).thenReturn("studyId=21&q.studySubjectId=SUB&q.personId=ignored&sortProp=eventDate&sortDir=desc");
        table().render(request);
    }

    public void testLegacySubjectDrillDownFilterStillWorks() {
        when(eventCrfs.getWithFilterAndSort(org.mockito.Matchers.eq(21), org.mockito.Matchers.eq(21), any(EventCRFSDVFilter.class),
            any(EventCRFSDVSort.class), org.mockito.Matchers.eq(0), org.mockito.Matchers.eq(15))).thenAnswer(call -> {
                EventCRFSDVFilter filter = (EventCRFSDVFilter) call.getArguments()[2];
                assertTrue(filter.execute("").contains("ss.label"));
                return new ArrayList<EventCRFBean>();
            });
        when(request.getQueryString()).thenReturn("studyId=21&sdv_f_studySubjectId=SUB-55");
        String html = table().render(request);
        assertTrue(html.contains("name=\"q.studySubjectId\" value=\"SUB-55\""));
    }

    private EventCRFSDVTable table() {
        return new EventCRFSDVTable(eventCrfs, studySubjects, events, eventDefinitions, subjects, studies,
            eventDefinitionCrfs, versions, crfs, 21, Locale.ENGLISH);
    }

    private static int count(String html, String token, String from) {
        int start = html.indexOf(from);
        int end = html.indexOf("</tbody>", start);
        int result = 0;
        for (int i = html.indexOf(token, start); i >= 0 && i < end; i = html.indexOf(token, i + token.length())) result++;
        return result;
    }
}