/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2008
 * Time: 1:45:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChooseDownloadFormat extends SecureController{
    /**
	 * 
	 */
	private static final long serialVersionUID = 8667766472280862827L;

	protected void processRequest() throws Exception {
        //FormProcessor fp = new FormProcessor(request);
        String subjectId=request.getParameter("subjectId");
        request.setAttribute("subjectId",subjectId);
        String resolutionStatus=request.getParameter("resolutionStatus");
        request.setAttribute("resolutionStatus",resolutionStatus);
        String discNoteType=request.getParameter("discNoteType");
        request.setAttribute("discNoteType",discNoteType);
        //provide the study name or identifier
        String studyIdentifier="";
        if(this.currentStudy != null) {
             studyIdentifier= currentStudy.getIdentifier();
        }
        request.setAttribute("studyIdentifier",studyIdentifier);
        forwardPage(Page.CHOOSE_DOWNLOAD_FORMAT);

    }

    protected void mayProceed() throws InsufficientPermissionException {

    }
}
