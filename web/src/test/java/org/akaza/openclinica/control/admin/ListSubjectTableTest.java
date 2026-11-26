/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.ListSubjectFilter;
import org.akaza.openclinica.dao.submit.ListSubjectSort;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListSubjectTableTest extends TestCase {

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    public void testRendersTypedRowsFiltersNaturalStudySubjectOrderAndStatusActions() {
        SubjectDAO subjectDao = mock(SubjectDAO.class);
        StudySubjectDAO studySubjectDao = mock(StudySubjectDAO.class);
        UserAccountDAO userAccountDao = mock(UserAccountDAO.class);
        StudyDAO studyDao = mock(StudyDAO.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        StudyBean currentStudy = new StudyBean();

        UserAccountBean owner = user(11, "owner1");
        UserAccountBean updater = user(12, "updater1");
        SubjectBean available = subject(41, "PERSON-001", Status.AVAILABLE, owner, updater);
        SubjectBean deleted = subject(42, "PERSON-002", Status.DELETED, owner, null);

        StudySubjectBean secondNaturalRow = studySubject(102, 41, 202, "SUB-B");
        StudySubjectBean firstNaturalRow = studySubject(101, 41, 201, "SUB-A");
        ArrayList<StudySubjectBean> memberships = new ArrayList<>();
        memberships.add(secondNaturalRow);
        memberships.add(firstNaturalRow);

        StudyBean secondStudy = study(202, "STUDY-B");
        StudyBean firstStudy = study(201, "STUDY-A");
        ArrayList<SubjectBean> subjects = new ArrayList<>();
        subjects.add(available);
        subjects.add(deleted);

        when(subjectDao.getWithFilterAndSort(
            same(currentStudy), any(ListSubjectFilter.class), any(ListSubjectSort.class), anyInt(), anyInt()
        )).thenReturn(subjects);
        when(subjectDao.getCountWithFilter(any(ListSubjectFilter.class), same(currentStudy))).thenReturn(2);
        when(userAccountDao.findByPK(11)).thenReturn(owner);
        when(userAccountDao.findByPK(12)).thenReturn(updater);
        when(studySubjectDao.findAllBySubjectId(41)).thenReturn(memberships);
        when(studySubjectDao.findAllBySubjectId(42)).thenReturn(new ArrayList<StudySubjectBean>());
        when(studyDao.findByPK(202)).thenReturn(secondStudy);
        when(studyDao.findByPK(201)).thenReturn(firstStudy);
        when(request.getQueryString()).thenReturn("q.subject.status=available");
        when(request.getRequestURI()).thenReturn("/ListSubject");
        when(request.getContextPath()).thenReturn("");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));

        String html = new ListSubjectTable(
            subjectDao, studySubjectDao, userAccountDao, studyDao, currentStudy, Locale.ENGLISH
        ).render(request);

        assertTrue(html.contains("data-test-subject=\"PERSON-001\""));
        assertTrue(html.contains("STUDY-B-SUB-B,STUDY-A-SUB-A"));
        assertTrue(html.contains("owner1"));
        assertTrue(html.contains("updater1"));
        assertDateFilterValidation(html, "subject.createdDate");
        assertDateFilterValidation(html, "subject.updatedDate");
        assertColumn(html, "person-id");
        assertColumn(html, "protocol-study-subject-ids");
        assertColumn(html, "sex");
        assertColumn(html, "date-created");
        assertColumn(html, "owner");
        assertColumn(html, "date-updated");
        assertColumn(html, "last-updated-by");
        assertColumn(html, "status");
        assertColumn(html, "actions");
        assertFalse(html.contains("name=\"q.subject.owner\""));
        assertFalse(html.contains("name=\"q.subject.updater\""));
        assertTrue(html.contains("value=\"available\""));
        assertTrue(html.contains("data-test-action=\"view\""));
        assertTrue(html.contains("data-test-action=\"edit\""));
        assertTrue(html.contains("data-test-action=\"remove\""));
        assertTrue(html.contains("data-test-action=\"restore\""));
        assertTrue(html.contains("UpdateSubject?action=show"));
        assertTrue(html.contains("id=41"));
        assertTrue(html.contains("RestoreSubject?action=confirm"));
        assertTrue(html.contains("id=42"));

        verify(studySubjectDao).findAllBySubjectId(41);
        verify(studyDao).findByPK(202);
        verify(studyDao).findByPK(201);
        verify(userAccountDao, never()).findByPK(0);
        verify(subjectDao).getWithFilterAndSort(
            same(currentStudy), any(ListSubjectFilter.class), any(ListSubjectSort.class), anyInt(), anyInt()
        );
    }

    public void testIgnoresForgedOwnerUpdaterFiltersAndUnsupportedSorts() {
        SubjectDAO subjectDao = mock(SubjectDAO.class);
        StudySubjectDAO studySubjectDao = mock(StudySubjectDAO.class);
        UserAccountDAO userAccountDao = mock(UserAccountDAO.class);
        StudyDAO studyDao = mock(StudyDAO.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        StudyBean currentStudy = new StudyBean();

        when(subjectDao.getWithFilterAndSort(
            same(currentStudy), any(ListSubjectFilter.class), any(ListSubjectSort.class), anyInt(), anyInt()
        )).thenAnswer(invocation -> {
            ListSubjectFilter filter = (ListSubjectFilter) invocation.getArguments()[1];
            ListSubjectSort sort = (ListSubjectSort) invocation.getArguments()[2];
            assertEquals("", filter.execute(""));
            assertEquals("", sort.execute(""));
            return new ArrayList<SubjectBean>();
        });
        when(subjectDao.getCountWithFilter(any(ListSubjectFilter.class), same(currentStudy))).thenReturn(0);
        when(request.getQueryString()).thenReturn(
            "q.subject.owner=owner&q.subject.updater=updater&sortProp=studySubjectIdAndStudy&sortDir=asc");
        when(request.getRequestURI()).thenReturn("/ListSubject");
        when(request.getContextPath()).thenReturn("");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));

        new ListSubjectTable(
            subjectDao, studySubjectDao, userAccountDao, studyDao, currentStudy, Locale.ENGLISH
        ).render(request);

        verify(subjectDao, atLeastOnce()).getWithFilterAndSort(
            same(currentStudy), any(ListSubjectFilter.class), any(ListSubjectSort.class), anyInt(), anyInt()
        );
    }

    private static void assertColumn(String html, String columnName) {
        assertTrue("Missing data-test-column=" + columnName,
            html.contains("data-test-column=\"" + columnName + "\""));
    }

    private static void assertDateFilterValidation(String html, String columnName) {
        String inputId = "id=\"listSubjects-text-filter-" + columnName + "\"";
        int inputStart = html.indexOf(inputId);
        assertTrue("Missing date filter input for " + columnName, inputStart >= 0);

        int inputEnd = html.indexOf('>', inputStart);
        assertTrue("Unclosed date filter input for " + columnName, inputEnd >= 0);
        String input = html.substring(inputStart, inputEnd);
        assertTrue(input.contains("pattern=\"(?:\\d{4}|(?:0[1-9]|[12]\\d|3[01])-[A-Za-z]{3}-\\d{4})\""));
        assertTrue(input.contains("title=\"Please enter a year (yyyy) or date in dd-MMM-yyyy format\""));
        assertTrue(input.contains("hx-on:htmx:before-request=\"if(!this.validity.valid){event.preventDefault();}\""));
    }

    private static UserAccountBean user(int id, String name) {
        UserAccountBean user = new UserAccountBean();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private static SubjectBean subject(int id, String identifier, Status status, UserAccountBean owner,
            UserAccountBean updater) {
        SubjectBean subject = new SubjectBean();
        subject.setId(id);
        subject.setUniqueIdentifier(identifier);
        subject.setGender('m');
        subject.setStatus(status);
        subject.setOwner(owner);
        if (updater != null) {
            subject.setUpdater(updater);
        }
        subject.setCreatedDate(new Date(0));
        subject.setUpdatedDate(new Date(86400000));
        return subject;
    }

    private static StudySubjectBean studySubject(int id, int subjectId, int studyId, String label) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setId(id);
        studySubject.setSubjectId(subjectId);
        studySubject.setStudyId(studyId);
        studySubject.setLabel(label);
        return studySubject;
    }

    private static StudyBean study(int id, String identifier) {
        StudyBean study = new StudyBean();
        study.setId(id);
        study.setIdentifier(identifier);
        return study;
    }
}
