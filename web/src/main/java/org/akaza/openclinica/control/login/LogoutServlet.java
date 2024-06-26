/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 * @version CVS: $Id: LogoutServlet.java 13689 2009-12-16 21:10:37Z kkrumlian $
 *
 * Performs Log out action
 */
public class LogoutServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6098692858165722854L;

	@Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        sm = null;// set sm to null after user logs out
        logger.info("User  : {} , email address : {} Logged Out ", ub.getName(), ub.getEmail());
        getCrfLocker().unlockAllForUser(ub.getId());
        session.removeAttribute("userBean");
        session.removeAttribute("study");
        session.removeAttribute("userRole");
        session.removeAttribute("passwordExpired");
        session.invalidate();
        // forwardPage is set to false to avoid checking the session, tbh
        // 01.2005
        // forwardPage(Page.MENU, false);
        forwardPage(Page.LOGOUT, false);
    }

}