/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.submit;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.servlet.ServletOutputStream;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.io.FilenameUtils;

/**
 * @author ywang (Dec., 2008)
 */
public class DownloadAttachedFileServlet extends SecureController {
    
	private static final long serialVersionUID = 3098103596566845378L;

    private static final int BUFFER_SIZE = 4096;

	/**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        request.setAttribute("downloadStatus", "false");
        addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
        throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        String fileName = fp.getString("fileName");
        
        String filePathName = "";
        File file = null;

        // When filename was provided check the file existence in current study
        if (fileName != null && fileName.length() > 0) {

            String attachedStudyFileRelDir = currentStudy.getOid() + File.separator;

            String testName = FilenameUtils.getName(fileName);
            file = getCoreResources().getAttachedFile(testName, attachedStudyFileRelDir);

            if (file != null && file.exists()) {
                filePathName = testName;
                logger.info(currentStudy.getName() + " existing filePathName=" + filePathName);

            } else { // When file does not existence in current study then check the parent study

                file = null;
                int parentStudyId = currentStudy.getParentStudyId();

                if (currentStudy.isSite(parentStudyId)) {

                    attachedStudyFileRelDir = (new StudyDAO(sm.getDataSource()).findByPK(parentStudyId)).getOid() +
                            File.separator;
                    file = getCoreResources().getAttachedFile(testName, attachedStudyFileRelDir);

                    if (file.exists()) {
                        filePathName = testName;
                        logger.info("parent existing filePathName=" + filePathName);
                    } else {
                        file = null;
                    }

                } else { // Current study is parent study, check the existence in any of the study site

                    ArrayList<StudyBean> sites = new StudyDAO(sm.getDataSource()).findAllByParent(currentStudy.getId());
                    for (StudyBean site : sites) {

                        attachedStudyFileRelDir = site.getOid() + File.separator;
                        file = getCoreResources().getAttachedFile(testName, attachedStudyFileRelDir);

                        if (file != null && file.exists()) {
                            filePathName = testName;
                            logger.info("site of currentStudy existing filePathName=" + filePathName);
                            break;
                        } else {
                            file = null;
                        }
                    }
                }
            }
        }

        logger.info("filePathName=" + filePathName + " fileName=" + fileName);

        if (file == null || !file.exists() || file.length() <= 0) {
            addPageMessage("File " + filePathName + " " + respage.getString("not_exist"));
        } else {
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\";");
            response.setContentType("application/download");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            response.setContentLength((int) file.length());

            try (InputStream in = Files.newInputStream(file.toPath())) {
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
