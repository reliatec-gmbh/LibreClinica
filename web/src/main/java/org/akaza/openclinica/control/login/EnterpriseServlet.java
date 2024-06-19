/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * Created on Sep 28, 2005
 *
 *
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author thickerson
 *
 *
 */
public class EnterpriseServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3332626368916404656L;

	@Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        setToPanel("", "");
        forwardPage(Page.ENTERPRISE);
    }

}
