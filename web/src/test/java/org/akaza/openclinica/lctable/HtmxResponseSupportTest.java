/*
* LibreClinica is distributed under the
* GNU Lesser General Public License (GNU LGPL).

* For details see: https://libreclinica.org/license
* copyright (C) 2026 LibreClinica
*/
package org.akaza.openclinica.lctable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HtmxResponseSupportTest extends TestCase {

    public void testNormalRequestAddsVaryWithoutWritingOrChangingContentType() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertFalse(HtmxResponseSupport.writeFragmentIfRequested(request, response, "<div>panel</div>"));

        verify(response).addHeader("Vary", "HX-Request");
        verify(response, never()).setContentType(anyString());
        verify(response, never()).getWriter();
    }

    public void testAnyPresentHeaderWritesExactHtmlWithoutFlushingOrClosing() throws Exception {
        for (String value : new String[] { "true", "TRUE", "false", "" }) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(request.getHeader("HX-Request")).thenReturn(value);
            StringWriter output = new StringWriter();
            PrintWriter writer = spy(new PrintWriter(output));
            when(response.getWriter()).thenReturn(writer);
            String html = "<div id=\"panel\">Étude &amp; 試験</div>";

            assertTrue(HtmxResponseSupport.writeFragmentIfRequested(request, response, html));

            assertEquals(html, output.toString());
            verify(response).addHeader("Vary", "HX-Request");
            verify(response).setContentType("text/html;charset=UTF-8");
            verify(writer, never()).flush();
            verify(writer, never()).close();
        }
    }

    public void testExistingVaryValueIsPreserved() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.addHeader("Vary", "Accept-Encoding");

        assertFalse(HtmxResponseSupport.writeFragmentIfRequested(request, response, "panel"));

        assertTrue(response.getHeaders("Vary").contains("Accept-Encoding"));
        assertTrue(response.getHeaders("Vary").contains("HX-Request"));
    }

    public void testWriterAcquisitionFailurePropagatesRatherThanAllowingPageFallback() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader("HX-Request")).thenReturn("true");
        IOException failure = new IOException("Writer unavailable");
        when(response.getWriter()).thenThrow(failure);

        try {
            HtmxResponseSupport.writeFragmentIfRequested(request, response, "panel");
            fail("The helper must not return false after attempting fragment output");
        } catch (IOException expected) {
            assertSame(failure, expected);
        }
    }
}