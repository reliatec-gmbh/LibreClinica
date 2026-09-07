/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogFilter;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogSort;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StudyAuditLogTableTest extends TestCase {

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    public void testRendersTypedRowFiltersAndPopupActionWithDefaultSort() {
        StudySubjectDAO studySubjectDao = mock(StudySubjectDAO.class);
        SubjectDAO subjectDao = mock(SubjectDAO.class);
        UserAccountDAO userAccountDao = mock(UserAccountDAO.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        StudyBean study = new StudyBean();
        UserAccountBean owner = new UserAccountBean();
        owner.setId(9);
        owner.setName("owner1");

        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setId(42);
        studySubject.setLabel("SUB-001");
        studySubject.setSecondaryLabel("SECONDARY-001");
        studySubject.setOid("SS_SUB_001");
        studySubject.setSubjectId(7);
        studySubject.setOwner(owner);
        studySubject.setStatus(Status.AVAILABLE);

        SubjectBean subject = new SubjectBean();
        subject.setUniqueIdentifier("PERSON-001");

        ArrayList<StudySubjectBean> rows = new ArrayList<>();
        rows.add(studySubject);
        when(studySubjectDao.getWithFilterAndSort(
            same(study), any(StudyAuditLogFilter.class), any(StudyAuditLogSort.class), anyInt(), anyInt()
        )).thenReturn(rows);
        when(studySubjectDao.getCountWithFilter(any(StudyAuditLogFilter.class), same(study))).thenReturn(1);
        when(subjectDao.findByPK(7)).thenReturn(subject);
        when(userAccountDao.findByPK(9)).thenReturn(owner);
        when(request.getQueryString()).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/StudyAuditLog");
        when(request.getContextPath()).thenReturn("");

        String html = new StudyAuditLogTable(
            studySubjectDao, subjectDao, userAccountDao, study, Locale.ENGLISH
        ).render(request);

        assertTrue(html.contains("data-test-subject=\"SUB-001\""));
        assertTrue(html.contains("SECONDARY-001"));
        assertTrue(html.contains("SS_SUB_001"));
        assertTrue(html.contains("PERSON-001"));
        assertTrue(html.contains("owner1"));
        assertTrue(html.contains("value=\"1\""));
        assertTrue(html.contains("javascript:openDocWindow"));
        assertTrue(html.contains("ViewStudySubjectAuditLog?id=42"));
        assertTrue(html.contains("data-test-action=\"view\""));
        assertTrue(html.contains("pattern=\"(?:\\d{4}|(?:0[1-9]|[12]\\d|3[01])-[A-Za-z]{3}-\\d{4})\""));

        verify(subjectDao).findByPK(7);
        verify(userAccountDao).findByPK(9);
        verify(studySubjectDao).getWithFilterAndSort(
            same(study), any(StudyAuditLogFilter.class), any(StudyAuditLogSort.class), anyInt(), anyInt()
        );
    }
}
