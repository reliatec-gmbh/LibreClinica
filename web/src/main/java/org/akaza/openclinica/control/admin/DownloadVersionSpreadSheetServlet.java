/* LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import javax.servlet.ServletOutputStream;

/**
 * @author jxu
 */
public class DownloadVersionSpreadSheetServlet extends SecureController {

	private static final long serialVersionUID = 6969545356468114843L;

    private static final int BUFFER_SIZE = 4096;

	public static String CRF_ID = "crfId";

    public static String CRF_VERSION_ID = "crfVersionId";

    public static String CRF_VERSION_TEMPLATE = "CRF_Template_lc_v1.0.xls";

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        String crfIdString = fp.getString(CRF_ID);
        int crfVersionId = fp.getInt(CRF_VERSION_ID);
        boolean isTemplate = fp.getBoolean("template");
        
        String excelFileName;
        File excelFile;

        if (isTemplate) {
            String originalCrfRelDir = "crf" + File.separator + "original" + File.separator;

            excelFileName = CRF_VERSION_TEMPLATE;
            excelFile = getCoreResources().getFile(excelFileName, originalCrfRelDir);
        } else {
            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
            CRFVersionBean version = cvdao.findByPK(crfVersionId);

            String newCrfRelDir = "crf" + File.separator + "new" + File.separator;

            excelFileName = crfIdString + version.getOid() + ".xls";
            excelFile = getCoreResources().getFile(excelFileName, newCrfRelDir);

            // If it's the old style CRF filenames used? next line is for backwards compatibility
            String oldExcelFileName = crfIdString + version.getName() + ".xls";
            File oldExcelFile = getCoreResources().getFile(oldExcelFileName, newCrfRelDir);

            // When old name exists and the new name does not...
            if (oldExcelFile != null && oldExcelFile.exists() && oldExcelFile.length() > 0) {
                if (excelFile == null || !excelFile.exists() || excelFile.length() <= 0) {
                    excelFile = oldExcelFile;
                    excelFileName = oldExcelFileName;
                }
            }
        }

        logger.info("looking for : " + excelFileName);
        
        if (excelFile == null || !excelFile.exists() || excelFile.length() <= 0) {
            addPageMessage(respage.getString("the_excel_is_not_available_on_server_contact"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            response.setHeader("Content-disposition", "attachment; filename=\"" + excelFileName + "\";");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Pragma", "public");
            response.setContentLength((int) excelFile.length());

            try (InputStream in = Files.newInputStream(excelFile.toPath())) {
                ServletOutputStream out = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception ee) {
                logger.error("Input Stream is not working properly: ", ee);
            }
        }
        
    }

    private CoreResources getCoreResources() {
        return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
    }
    
}
