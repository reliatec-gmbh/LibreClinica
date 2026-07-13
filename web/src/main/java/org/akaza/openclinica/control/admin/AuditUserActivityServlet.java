/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import java.util.Locale;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import javax.servlet.ServletException;

/**
 * Servlet for creating a table.
 *
 * @author Krikor Krumlian
 */
public class AuditUserActivityServlet extends SecureController {

    private static final long serialVersionUID = 1L;
    private AuditUserLoginDao auditUserLoginDao;
    Locale locale;

    private AuditUserLoginTable auditUserLoginTable;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialized once when this specific Servlet instance is added to the pool
        this.auditUserLoginTable = new AuditUserLoginTable(getAuditUserLoginDao());
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        // Use JMesa rendering only when LC_TABLE_RENDERING is explicitly set to "jmesa"
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            // Legacy JMesa rendering path: unchanged behaviour (render and forward)
            request.setAttribute("tableRenderingMode", "jmesa");
            AuditUserLoginTableFactory factory = new AuditUserLoginTableFactory();
            factory.setAuditUserLoginDao(getAuditUserLoginDao());
            String auditUserLoginHtml = factory.createTable(request, response).render();
            request.setAttribute("auditUserLoginHtml", auditUserLoginHtml);
            forwardPage(Page.AUDIT_USER_ACTIVITY);
        } else {
            // HtmlFlow rendering path: supports HTMX partials (panel vs full page)
            request.setAttribute("tableRenderingMode", "htmlflow");
            String auditUserLoginHtml = auditUserLoginTable.render(request);
            // HTMX partial handling
            response.addHeader("Vary", "HX-Request");
            String hxReq = request.getHeader("HX-Request");
            if (hxReq != null) {
                // HTMX request: only return the table HTML fragment
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(auditUserLoginHtml);
                response.getWriter().flush();
            } else {
                // Non-HTMX request: embed into JSP and forward
                request.setAttribute("auditUserLoginHtml", auditUserLoginHtml);
                forwardPage(Page.AUDIT_USER_ACTIVITY);
            }
        }
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        auditUserLoginDao =
            this.auditUserLoginDao != null ? auditUserLoginDao : (AuditUserLoginDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "auditUserLoginDao");
        return auditUserLoginDao;
    }
}
