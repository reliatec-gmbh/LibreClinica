/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewCRFVersionServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -920947631341814197L;

	/**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfVersionId = fp.getInt("id");

        if (crfVersionId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            // tbh
            CRFDAO crfdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) crfdao.findByPK(version.getCrfId());
            CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(sm.getDataSource());
            ArrayList<SectionBean> sections = metadataUtil.retrieveFormMetadata(version); 
            request.setAttribute("sections", sections);
            request.setAttribute("version", version);
            // tbh
            request.setAttribute("crfname", crf.getName());
            // tbh
            forwardPage(Page.VIEW_CRF_VERSION);

        }
    }

}
