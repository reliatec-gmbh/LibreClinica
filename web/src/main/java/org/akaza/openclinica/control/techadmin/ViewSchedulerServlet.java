/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.techadmin;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author thickerson
 * Created on Sep 23, 2005
 */
public class ViewSchedulerServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4488744121360370880L;
	Locale locale;

    // < ResourceBundleresexception;

    @Override
    protected void processRequest() throws Exception {
        // Log psqlLogger = new SimpleLog("QuartzJobLog");
        // //ServletContext ctx = request.getSession().getServletContext();
        // PostgreSQLDelegate delegate = new PostgreSQLDelegate(psqlLogger,
        // "qrtz_",
        // "OpenClinica Jobs");
        // CronTrigger trigger =
        // (CronTrigger)delegate.selectTrigger(sm.getConnection(),
        // "Refresh Data View",
        // "OpenClinica Jobs");
        //
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (!ub.isTechAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, "You may not perform technical administrative functions", "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

}
