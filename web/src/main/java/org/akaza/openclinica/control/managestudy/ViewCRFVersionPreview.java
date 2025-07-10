/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.util.List;
import java.util.Map;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author Bruce Perry 3/8/2007
 *
 */
public class ViewCRFVersionPreview extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5409675615393802287L;

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

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @SuppressWarnings("unchecked")
	@Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int crfId = fp.getInt("crfId");
        // CRFVersionBean
        // SectionBean
        CRFVersionBean version = new CRFVersionBean();
        List<SectionBean> sections;
		Map<String, Map<?, ?>> crfMap = (Map<String, Map<?, ?>>) session.getAttribute("preview_crf");
        Map<String, String> crfIdnameInfo = null;
        if (crfMap != null) {
            crfIdnameInfo = (Map<String, String>) crfMap.get("crf_info");
        } else {
            logger.info("The crfMap session attribute has expired or gone out of scope in: " + this.getClass().getName());
        }
        String crfName = "";
        String verNumber = "";
        if (crfIdnameInfo != null) {
            crfName = crfIdnameInfo.get("crf_name");
            verNumber = crfIdnameInfo.get("version");
        }
        version.setName(verNumber);
        version.setCrfId(crfId);
        // A Map containing the section names as the index
        Map<Integer, Map<String, String>> sectionsMap = null;
        if (crfMap != null)
            sectionsMap = (Map<Integer, Map<String, String>>) crfMap.get("sections");
        // The itemsMap contains the index of the spreadsheet table items row,
        // followed by a map of the column names/values; it contains values for
        // display
        // such as 'left item text'
        Map<Integer, Map<String, String>> itemsMap = null;
        if (crfMap != null)
            itemsMap = (Map<Integer, Map<String, String>>) crfMap.get("items");
        // Get groups meta info
        Map<Integer, Map<String, String>> groupsMap = null;
        if (crfMap != null)
            groupsMap = (Map<Integer, Map<String, String>>) crfMap.get("groups");
        // Set up sections for the preview
        BeanFactory beanFactory = new BeanFactory();
        sections = beanFactory.createSectionBeanList(sectionsMap, itemsMap, crfName, groupsMap);
        request.setAttribute("sections", sections);
        request.setAttribute("version", version);
        forwardPage(Page.VIEW_CRF_VERSION);

    }
}