/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudiesUsingCrfTableTest extends TestCase {

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    public void testRendersLocalizedColumnsTypedActionAndStickyScope() {
        StudyBean study = study(42, "Alpha Study", "PROTO-A");

        String html = new StudiesUsingCrfTable(Collections.singletonList(study)).render(
            request("module=admin&crfId=7"));

        assertTrue(html.contains("Study Name"));
        assertTrue(html.contains("Unique Protocol ID"));
        assertTrue(html.contains("data-test-column=\"study-name\""));
        assertTrue(html.contains("data-test-column=\"unique-protocol-id\""));
        assertTrue(html.contains("data-test-study=\"42\""));
        assertTrue(html.contains("data-test-action=\"view\""));
        assertTrue(html.contains("ViewStudy?id=42"));
        assertTrue(html.contains("viewFull=yes"));
        assertTrue(html.contains("name=\"module\" value=\"admin\""));
        assertTrue(html.contains("name=\"crfId\" value=\"7\""));
        assertTrue(html.contains("clear-filter-button"));
    }

    public void testFiltersSortsAndPaginatesStudies() {
        StudyBean alpha = study(1, "Alpha", "PROTO-A");
        StudyBean beta = study(2, "Beta", "PROTO-B");
        StudyBean gamma = study(3, "Gamma", "OTHER");

        String html = new StudiesUsingCrfTable(Arrays.asList(alpha, beta, gamma)).render(
            request("module=admin&crfId=7&q.uniqueProtocolid=proto&sortProp=name&sortDir=desc&maxRows=1&page=2"));

        assertTrue(html.contains("Alpha"));
        assertFalse(html.contains(">Beta<"));
        assertFalse(html.contains(">Gamma<"));
        assertTrue(html.contains("name=\"q.uniqueProtocolid\" value=\"proto\""));
        assertTrue(html.contains("data-test-sort=\"desc\""));
    }

    private static HttpServletRequest request(String queryString) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getRequestURI()).thenReturn("/ViewCRF");
        when(request.getContextPath()).thenReturn("");
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.singletonList(Locale.ENGLISH)));
        return request;
    }

    private static StudyBean study(int id, String name, String identifier) {
        StudyBean study = new StudyBean();
        study.setId(id);
        study.setName(name);
        study.setIdentifier(identifier);
        return study;
    }
}
