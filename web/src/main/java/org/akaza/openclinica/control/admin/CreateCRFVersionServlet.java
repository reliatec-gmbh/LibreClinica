/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.exception.CRFReadingException;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.likepoi.ss.usermodel.Workbook;
import org.akaza.openclinica.likepoi.ss.usermodel.WorkbookFactory;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.util.SpreadsheetTypeDetector;

/**
 * Create a new CRF version by uploading Excel file
 * 
 * @author jxu
 */
public class CreateCRFVersionServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2414948228788037586L;
	Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();

    // < ResourceBundleresword,resexception,respage;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(true);

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        // keep the module in the session
        session.setAttribute(MODULE, module);
        request.setAttribute("xformEnabled", CoreResources.getField("xform.enabled"));
        String action = request.getParameter("action");
        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");

        if (action == null || action.trim().isEmpty()) {
            logger.debug("action is blank");
            request.setAttribute("version", version);
            forwardPage(Page.CREATE_CRF_VERSION);
        } else if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.debug("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(resword.getString("the_filepath_you_defined"));
                forwardPage(Page.CREATE_CRF_VERSION);
                // BWP 01/13/2009 >>
                return;
                // >>
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "crf" + File.separator + "original" + File.separator;
            if (!new File(theDir).isDirectory()) {
                new File(theDir).mkdirs();
                logger.debug("Made the directory " + theDir);
            }
            // MultipartRequest multi = new MultipartRequest(request, theDir, 50 * 1024 * 1024);
            String tempFile = "";
            try {
                tempFile = uploadFile(theDir, version);
            } catch (CRFReadingException crfException) {
                Validator.addError(errors, "excel_file", crfException.getMessage());
                request.setAttribute("formMessages", errors);
                forwardPage(Page.CREATE_CRF_VERSION);
                return;
            } catch (Exception e) {
                //
                logger.error("*** Found exception during file upload***", e);
            }
            session.setAttribute("tempFileName", tempFile);
            // YW, at this point, if there are errors, they point to no file
            // provided and/or not xls format
            if (errors.isEmpty()) {
                String s = ((NewCRFBean) session.getAttribute("nib")).getVersionName();
                if (s.length() > 255) {
                    Validator.addError(errors, "excel_file", resword.getString("the_version_CRF_version_more_than_255"));
                } else if (s.length() <= 0) {
                    Validator.addError(errors, "excel_file", resword.getString("the_VERSION_column_was_blank"));
                }
                version.setName(s);
                if (version.getCrfId() == 0) {
                    version.setCrfId(fp.getInt("crfId"));
                }
                session.setAttribute("version", version);
            }
            if (!errors.isEmpty()) {
                logger.debug("has validation errors ");
                request.setAttribute("formMessages", errors);
                forwardPage(Page.CREATE_CRF_VERSION);
            } else {
                CRFBean crf = (CRFBean) cdao.findByPK(version.getCrfId());
                ArrayList<CRFVersionBean> versions = vdao.findAllByCRF(crf.getId());
                for (int i = 0; i < versions.size(); i++) {
                    CRFVersionBean version1 = (CRFVersionBean) versions.get(i);
                    if (version.getName().equals(version1.getName())) {
                        // version already exists
                        logger.debug("Version already exists; owner or not:" + ub.getId() + "," + version1.getOwnerId());
                        if (ub.getId() != version1.getOwnerId()) {// not owner
                            addPageMessage(respage.getString("CRF_version_try_upload_exists_database") + version1.getOwner().getName()
                                    + respage.getString("please_contact_owner_to_delete"));
                            forwardPage(Page.CREATE_CRF_VERSION);
                            return;
                        } else {// owner,
                            ArrayList<EventDefinitionCRFBean> definitions = edao.findByDefaultVersion(version1.getId());
                            if (!definitions.isEmpty()) {// used in
                                // definition
                                request.setAttribute("definitions", definitions);
                                forwardPage(Page.REMOVE_CRF_VERSION_DEF);
                                return;
                            } else {// not used in definition
                                int previousVersionId = version1.getId();
                                version.setId(previousVersionId);
                                session.setAttribute("version", version);
                                session.setAttribute("previousVersionId", new Integer(previousVersionId));
                                forwardPage(Page.REMOVE_CRF_VERSION_CONFIRM);
                                return;
                            }
                        }
                    }
                }
                // didn't find same version in the DB,let user upload the excel
                // file
                logger.debug("didn't find same version in the DB,let user upload the excel file.");

                // List excelErr =
                // ((ArrayList)request.getAttribute("excelErrors"));
                ArrayList<String> excelErr = asArrayList(session.getAttribute("excelErrors"), String.class);
                logger.debug("excelErr.isEmpty()=" + excelErr.isEmpty());
                if (excelErr != null && excelErr.isEmpty()) {
                    addPageMessage(resword.getString("congratulations_your_spreadsheet_no_errors"));
                    forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PREVIEW);
                } else {
                    logger.debug("OpenClinicaException thrown, forwarding to CREATE_CRF_VERSION_CONFIRM.");
                    forwardPage(Page.CREATE_CRF_VERSION_CONFIRM);
                }

                return;
            }
        } else if ("confirmsql".equalsIgnoreCase(action)) {
            NewCRFBean nib = (NewCRFBean) session.getAttribute("nib");
            if (nib != null && nib.getItemQueries() != null) {
                request.setAttribute("openQueries", nib.getItemQueries());
            } else {
                request.setAttribute("openQueries", new HashMap<>());
            }
            boolean canDelete = false;
            // check whether need to delete previous version
            Boolean deletePreviousVersion = (Boolean) session.getAttribute("deletePreviousVersion");
            Integer previousVersionId = (Integer) session.getAttribute("previousVersionId");
            if (deletePreviousVersion != null && deletePreviousVersion.equals(Boolean.TRUE) && previousVersionId != null && previousVersionId.intValue() > 0) {
                logger.debug("Need to delete previous version");
                // whether we can delete
                canDelete = canDeleteVersion(previousVersionId.intValue());
                if (!canDelete) {
                    logger.debug("but cannot delete previous version");
                    if (session.getAttribute("itemsHaveData") == null && session.getAttribute("eventsForVersion") == null) {
                        addPageMessage(respage.getString("you_are_not_owner_some_items_cannot_delete"));
                    }
                    if (session.getAttribute("itemsHaveData") == null) {
                        session.setAttribute("itemsHaveData", new ArrayList<>());
                    }
                    if (session.getAttribute("eventsForVersion") == null) {
                        session.setAttribute("eventsForVersion", new ArrayList<>());
                    }
                    forwardPage(Page.CREATE_CRF_VERSION_NODELETE);
                    return;
                }
                ArrayList<ItemBean> nonSharedItems = (ArrayList<ItemBean>) vdao.findNotSharedItemsByVersion(previousVersionId.intValue());
                // htaycher: here is the trick we need to put in nib1.setItemQueries()
                // update statements for shared items and insert for nonShared that were just deleted 5927
                HashMap<String, String> item_table_statements = new HashMap<>();
                ArrayList<String> temp = new ArrayList<String>(nonSharedItems.size());

                for (ItemBean item : nonSharedItems) {
                    temp.add(item.getName());
                    item_table_statements.put(item.getName(), nib.getBackupItemQueries().get(item.getName()));
                }
                for (String item_name : (Set<String>) nib.getItemQueries().keySet()) {
                    // check if item shared
                    if (!temp.contains(item_name)) {
                        item_table_statements.put(item_name, nib.getItemQueries().get(item_name));
                    }
                }
                // statements to run
                if (!nonSharedItems.isEmpty()) {
                    request.setAttribute("openQueries", item_table_statements);
                }

                // htaycher: put all statements in
                nib.setItemQueries(item_table_statements);
                session.setAttribute("nib", nib);
            }

            // submit
            logger.debug("commit sql");
            NewCRFBean nib1 = (NewCRFBean) session.getAttribute("nib");
            if (nib1 != null) {
                try {
                    if (canDelete) {
                        nib1.deleteInsertToDB();
                    } else {
                        nib1.insertToDB();
                    }
                    request.setAttribute("queries", nib1.getQueries());
                    // YW << for add a link to "View CRF Version Data Entry".
                    // For this purpose, CRFVersion id is needed.
                    // So the latest CRFVersion Id of A CRF Id is it.
                    CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
                    ArrayList<CRFVersionBean> crfvbeans = new ArrayList<>();

                    logger.debug("CRF-ID [" + version.getCrfId() + "]");
                    int crfVersionId = 0;
                    if (version.getCrfId() != 0) {
                        crfvbeans = cvdao.findAllByCRFId(version.getCrfId());
                        CRFVersionBean lastCvbean = crfvbeans.get(crfvbeans.size() - 1);
                        crfVersionId = lastCvbean.getId();
                        for(CRFVersionBean cvbean : crfvbeans) {
                            if (crfVersionId < cvbean.getId()) {
                                crfVersionId = cvbean.getId();
                            }
                        }
                    }
                    // Not needed; crfVersionId will be autoboxed in Java 5
                    // this was added for the old CVS java compiler
                    Integer cfvID = new Integer(crfVersionId);
                    if (cfvID == 0) {
                        cfvID = cvdao.findCRFVersionId(nib1.getCrfId(), nib1.getVersionName());
                    }
                    CRFVersionBean finalVersion = (CRFVersionBean) cvdao.findByPK(cfvID);
                    version.setCrfId(nib1.getCrfId());

                    version.setOid(finalVersion.getOid());

                    CRFBean crfBean = (CRFBean) cdao.findByPK(version.getCrfId());
                    crfBean.setUpdatedDate(version.getCreatedDate());
                    crfBean.setUpdater(ub);
                    cdao.update(crfBean);

                    // workaround to get a correct file name below, tbh 06/2008
                    request.setAttribute("crfVersionId", cfvID);
                    // YW >>
                    // return those properties to initial values
                    session.removeAttribute("version");
                    session.removeAttribute("eventsForVersion");
                    session.removeAttribute("itemsHaveData");
                    session.removeAttribute("nib");
                    session.removeAttribute("deletePreviousVersion");
                    session.removeAttribute("previousVersionId");

                    // save new version spreadsheet
                    String tempFile = (String) session.getAttribute("tempFileName");
					if (tempFile != null) {
						logger.debug("*** ^^^ *** saving new version spreadsheet" + tempFile);
						try {
							String dir = SQLInitServlet.getField("filePath");
							File f = new File(dir + "crf" + File.separator + "original" + File.separator + tempFile);
							// check to see whether crf/new/ folder exists
							// inside, if not,
							// creates
							// the crf/new/ folder
							String finalDir = dir + "crf" + File.separator + "new" + File.separator;

							if (!new File(finalDir).isDirectory()) {
								logger.debug("need to create folder for excel files" + finalDir);
								new File(finalDir).mkdirs();
							}

							// Determine the real file extension from content (magic bytes),
							// not from the original upload's file name, which may not be
							// reliable and previously was hard-coded to ".xls" regardless
							// of the actual format (xls/xlsx/ods).
							String extension = SpreadsheetTypeDetector.detectExtension(f.toPath());
							String newFile = version.getCrfId() + version.getOid() + extension;

							logger.debug("*** ^^^ *** new file: " + newFile);
							File nf = new File(finalDir + newFile);
							logger.debug("copying old file " + f.getName() + " to new file " + nf.getName());
							copy(f, nf);
							// ?
						} catch (IOException ie) {
							logger.debug("==============");
							addPageMessage(respage.getString("CRF_version_spreadsheet_could_not_saved_contact"));
						}

					}
                    session.removeAttribute("tempFileName");
                    session.removeAttribute(MODULE);
                    session.removeAttribute("excelErrors");
                    session.removeAttribute("htmlTab");
                    forwardPage(Page.CREATE_CRF_VERSION_DONE);
                } catch (OpenClinicaException pe) {
                    logger.debug("--------------");
                    session.setAttribute("excelErrors", nib1.getErrors());
                    // request.setAttribute("excelErrors", nib1.getErrors());
                    forwardPage(Page.CREATE_CRF_VERSION_ERROR);
                }
            } else {
                forwardPage(Page.CREATE_CRF_VERSION);
            }
        } else if ("delete".equalsIgnoreCase(action)) {
            logger.debug("user wants to delete previous version");
            ArrayList<String> excelErr = asArrayList(session.getAttribute("excelErrors"), String.class);
            logger.debug("for overwrite CRF version, excelErr.isEmpty()=" + excelErr.isEmpty());
            if (excelErr != null && excelErr.isEmpty()) {
                addPageMessage(resword.getString("congratulations_your_spreadsheet_no_errors"));
                session.setAttribute("deletePreviousVersion", Boolean.TRUE);// should be moved to excelErr != null block
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PREVIEW);
            } else {
                session.setAttribute("deletePreviousVersion", Boolean.FALSE);// should be moved to excelErr != null
                                                                             // block
                logger.debug("OpenClinicaException thrown, forwarding to CREATE_CRF_VERSION_CONFIRM.");
                forwardPage(Page.CREATE_CRF_VERSION_CONFIRM);
            }

        }
    }

    /**
     * Uploads the excel version file
     * 
     * @param version
     * @throws Exception
     */    
    public String uploadFileOld(String theDir, CRFVersionBean version) throws Exception {
        List<File> theFiles = uploadHelper.returnFiles(request, context, theDir);
        // Enumeration files = multi.getFileNames();
        errors.remove("excel_file");
        String tempFile = null;
        for (File f : theFiles) {
            // while (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // File f = multi.getFile(name);
            if (f == null || f.getName() == null) {
                logger.debug("file is empty.");
                Validator.addError(errors, "excel_file", resword.getString("you_have_to_provide_spreadsheet"));
                session.setAttribute("version", version);
                return tempFile;
            } else if ((f.getName().indexOf(".xlsx") < 0 & f.getName().indexOf(".XLSX") < 0 &
            		f.getName().indexOf(".ods") < 0 & f.getName().indexOf(".ODS") < 0)) {
                logger.debug("file name:" + f.getName());
                Validator.addError(errors, "excel_file", respage.getString("file_you_uploaded_not_seem_excel_spreadsheet"));
                session.setAttribute("version", version);
                return tempFile;
            } else {
                logger.debug("file name:" + f.getName());
                tempFile = f.getName();
                // create the inputstream here, so that it can be enclosed in a
                // try/finally block and closed :: BWP, 06/08/2007
//                FileInputStream inStream = null;
                FileInputStream inStreamClassic = null;
                SpreadSheetTableRepeating htab = null;
                SpreadSheetTableClassic sstc = null;
                // create newCRFBean here
                NewCRFBean nib = null;
                try {
                	Path path = Paths.get(theDir, tempFile);
                	logger.info("the file is at: {}", path);
                	
//                    inStream = new FileInputStream(theDir + tempFile);

                    // *** now change the code here to generate sstable, tbh
                    // 06/07
                    try {
						htab = new SpreadSheetTableRepeating(/*inStream,*/ ub, version.getName(), locale, currentStudy.getId(), path);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                    htab.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));

                    if (!htab.isRepeating()) {
                        inStreamClassic = new FileInputStream(theDir + tempFile);
                        sstc = new SpreadSheetTableClassic(inStreamClassic, ub, version.getName(), locale, currentStudy.getId());
                        sstc.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));
                    }
                    // logger.debug("finishing with feedin file-input-stream, did
                    // we error out here?");

                    if (htab.isRepeating()) {
                        htab.setCrfId(version.getCrfId());
                        // not the best place for this but for now...
                        session.setAttribute("new_table", "y");
                    } else {
                        sstc.setCrfId(version.getCrfId());
                    }

                    if (htab.isRepeating()) {
                        nib = htab.toNewCRF(sm.getDataSource(), respage);
                    } else {
                        nib = sstc.toNewCRF(sm.getDataSource(), respage);
                    }

                    // bwp; 2/28/07; updated 6/11/07;
                    // This object is created to pull preview information out of
                    // the
                    // spreadsheet
//                    Workbook workbook = null;
//                    FileInputStream inputStream = null;
                    try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(theDir, tempFile))); Workbook wb = WorkbookFactory.create(bis)) {
//                        inputStream = new FileInputStream(theDir + tempFile);
//                        workbook = WorkbookFactory.create(bis);//new HSSFWorkbook(inputStream);
                        // Store the Sections, Items, Groups, and CRF name and
                        // version information
                        // so they can be displayed in a preview. The Map
                        // consists of the
                        // names "sections," "items," "groups," and "crf_info"
                        // as keys, each of which point
                        // to a Map containing data on those CRF sections.

                        // Check if it's the old template
                        Preview preview;
                        if (htab.isRepeating()) {

                            // the preview uses date formatting with default
                            // values in date fields: yyyy-MM-dd
                            preview = new SpreadsheetPreviewNw();

                        } else {
                            preview = new SpreadsheetPreview();

                        }
                        session.setAttribute("preview_crf", preview.createCrfMetaObject(wb));
                    } catch (Exception exc) { // opening the stream could
                        // throw FileNotFoundException
                        String message = resword.getString("the_application_encountered_a_problem_uploading_CRF");
                        logger.error("{} : {}", message , exc.getMessage(), exc);
                        this.addPageMessage(message);
                    } 
//                    finally {
//                        if (inputStream != null) {
//                            try {
//                                inputStream.close();
//                            } catch (IOException io) {
//                                // ignore this close()-related exception
//                            }
//                        }                       
//                    }
                    ArrayList<ItemBean> ibs = isItemSame(nib.getItems(), version);

                    if (!ibs.isEmpty()) {
                        ArrayList<String> warnings = new ArrayList<>();
                        warnings.add(resexception.getString("you_may_not_modify_items"));
                        for (int i = 0; i < ibs.size(); i++) {
                            ItemBean ib = (ItemBean) ibs.get(i);
                            if (ib.getOwner().getId() == ub.getId()) {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). UNITS " + resword.getString("and") + " DATA_TYPE(PDATE to DATE) "
                                        + resexception.getString("will_not_be_changed_if") + " PHI, DESCRIPTION, DATA_TYPE from PDATE to DATE "
                                        + resexception.getString("will_be_changed_if_you_continue"));
                            } else {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). " + resexception.getString("these_field_cannot_be_modified_because_not_owner"));
                            }

                            request.setAttribute("warnings", warnings);
                        }
                    }
                    ItemBean ib = isResponseValid(nib.getItems(), version);
                    if (ib != null) {

                        nib.getErrors().add(
                                resword.getString("the_item") + ": " + ib.getName() + " " + resexception.getString("in_your_spreadsheet_already_exits_in_DB"));
                    }
                } catch (IOException io) {
                    logger.warn("Opening up the Excel file caused an error. the error message is: " + io.getMessage());

                } finally {
//                    if (inStream != null) {
//                        try {
//                            inStream.close();
//                        } catch (IOException ioe) {
//                        }
//                    }
                    if (inStreamClassic != null) {
                        try {
                            inStreamClassic.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
                // request.setAttribute("excelErrors", .getErrors());
                session.setAttribute("excelErrors", nib.getErrors());
                session.setAttribute("htmlTable", nib.getHtmlTable());
                session.setAttribute("nib", nib);
            }
        }
        return tempFile;
    }

    /**
     * Checks whether the version can be deleted
     * 
     * @param previousVersionId
     * @return
     */
    private boolean canDeleteVersion(int previousVersionId) {
        CRFVersionDAO cdao = new CRFVersionDAO(sm.getDataSource());
        ArrayList<ItemBean> items = null;
        ArrayList<ItemBean> itemsHaveData = new ArrayList<>();
        // boolean isItemUsedByOtherVersion =
        // cdao.isItemUsedByOtherVersion(previousVersionId);
        // if (isItemUsedByOtherVersion) {
        // ArrayList itemsUsedByOtherVersion = (ArrayList)
        // cdao.findItemUsedByOtherVersion(previousVersionId);
        // session.setAttribute("itemsUsedByOtherVersion",itemsUsedByOtherVersion);
        // return false;
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        ArrayList<EventCRFBean> events = ecdao.findAllByCRFVersion(previousVersionId);
        if (!events.isEmpty()) {
            session.setAttribute("eventsForVersion", events);
            return false;
        }
        items = cdao.findNotSharedItemsByVersion(previousVersionId);
        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);
            if (ub.getId() != item.getOwner().getId()) {
                logger.debug("not owner" + item.getOwner().getId() + "<>" + ub.getId());
                return false;
            }
            if (cdao.hasItemData(item.getId())) {
                itemsHaveData.add(item);
                logger.debug("item has data");
                session.setAttribute("itemsHaveData", itemsHaveData);
                return false;
            }
        }

        // user is the owner and item not have data,
        // delete previous version with non-shared items
        NewCRFBean nib = (NewCRFBean) session.getAttribute("nib");
        nib.setDeleteQueries(cdao.generateDeleteQueries(previousVersionId, items));
        session.setAttribute("nib", nib);
        return true;

    }

    /**
     * Checks whether the item with same name has the same other fields: units, phi_status if no, they are two different
     * items, cannot have the same same
     * 
     * @param items
     *            items from excel
     * @return the items found
     */
    private ArrayList<ItemBean> isItemSame(HashMap<String, ItemBean> items, CRFVersionBean version) {
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ArrayList<ItemBean> diffItems = new ArrayList<>();
        for(String name : items.keySet()) {
            ItemBean newItem = (ItemBean) idao.findByNameAndCRFId(name, version.getCrfId());
            ItemBean item = (ItemBean) items.get(name);
            if (newItem.getId() > 0) {
                if (!item.getUnits().equalsIgnoreCase(newItem.getUnits()) || item.isPhiStatus() != newItem.isPhiStatus()
                        || item.getDataType().getId() != newItem.getDataType().getId() || !item.getDescription().equalsIgnoreCase(newItem.getDescription())) {

                    logger.debug("found two items with same name but different units/phi/datatype/description");

                    diffItems.add(newItem);
                }
            }
        }

        return diffItems;
    }

    private ItemBean isResponseValid(HashMap<String, ItemBean> items, CRFVersionBean version) {
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO metadao = new ItemFormMetadataDAO(sm.getDataSource());
        Set<String> names = items.keySet();
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            ItemBean oldItem = idao.findByNameAndCRFId(name, version.getCrfId());
            ItemBean item = items.get(name);
            if (oldItem.getId() > 0) {// found same item in DB
                ArrayList<ItemFormMetadataBean> metas = metadao.findAllByItemId(oldItem.getId());
                for (int i = 0; i < metas.size(); i++) {
                    ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(i);
                    ResponseSetBean rsb = ifmb.getResponseSet();
                    if (hasDifferentOption(rsb, item.getItemMeta().getResponseSet()) != null) {
                        return item;
                    }
                }

            }
        }
        return null;

    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    /**
     * When the version is added, for each non-new item LibreClinica should check the RESPONSE_OPTIONS_TEXT, and
     * RESPONSE_VALUES used for the item in other versions of the CRF.
     * 
     * For a given RESPONSE_VALUES code, the associated RESPONSE_OPTIONS_TEXT string is different than in a previous
     * version
     * 
     * For a given RESPONSE_OPTIONS_TEXT string, the associated RESPONSE_VALUES code is different than in a previous
     * version
     * 
     * @param oldRes
     * @param newRes
     * @return The original option
     */
    public ResponseOptionBean hasDifferentOption(ResponseSetBean oldRes, ResponseSetBean newRes) {
        ArrayList<ResponseOptionBean> oldOptions = oldRes.getOptions();
        ArrayList<ResponseOptionBean> newOptions = newRes.getOptions();
        if (oldOptions.size() != newOptions.size()) {
            // if the sizes are different, means the options don't match
            return null;

        } else {
            for (int i = 0; i < oldOptions.size(); i++) {// from database
                ResponseOptionBean rob = (ResponseOptionBean) oldOptions.get(i);
                String text = rob.getText();
                String value = rob.getValue();
                // spreadsheet
                ResponseOptionBean rob1 = (ResponseOptionBean) newOptions.get(i);
                // changed by jxu on 08-29-06, to fix the problem of cannot
                // recognize
                // the same responses
                String text1 = restoreQuotes(rob1.getText());

                String value1 = restoreQuotes(rob1.getValue());

                if (
                		(text1 == null || text1.trim().isEmpty()) && 
                		(value1 == null || value1.trim().isEmpty())
                ) {
                    // this response label appears in the spreadsheet
                    // multiple times, so
                    // ignore the checking for the repeated ones
                    continue;
                }
                if (text1.equalsIgnoreCase(text) && !value1.equals(value)) {
                    logger.debug("different response value:" + value1 + "|" + value);
                    return rob;
                } else if (!text1.equalsIgnoreCase(text) && value1.equals(value)) {
                    logger.debug("different response text:" + text1 + "|" + text);
                    return rob;
                }
            }

        }
        return null;
    }

    /**
     * Copy one file to another
     * 
     * @param src
     * @param dst
     * @throws IOException
     */
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * restoreQuotes, utility function meant to replace double quotes in strings with single quote. Don''t -> Don't, for
     * example. If the option text has single quote, it is changed to double quotes for SQL compatability, so we will
     * change it back before the comparison
     * 
     * @param subj
     *            the subject line
     * @return A string with all the quotes escaped.
     */
    public String restoreQuotes(String subj) {
        if (subj == null) {
            return null;
        }
        String returnme = "";
        String[] subjarray = subj.split("''");
        if (subjarray.length == 1) {
            returnme = subjarray[0];
        } else {
            for (int i = 0; i < subjarray.length - 1; i++) {
                returnme += subjarray[i];
                returnme += "'";
            }
            returnme += subjarray[subjarray.length - 1];
        }
        return returnme;
    }

    /**
     * Uploads the excel version file
     * 
     * @param version
     * @throws Exception
     */
    public String uploadFile(String theDir, CRFVersionBean version) throws Exception {
        List<File> theFiles = uploadHelper.returnFiles(request, context, theDir);
        // Enumeration files = multi.getFileNames();
        errors.remove("excel_file");
        String tempFile = null;
        for (File f : theFiles) {
            // while (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // File f = multi.getFile(name);
            if (f == null || f.getName() == null) {
                logger.debug("file is empty.");
                Validator.addError(errors, "excel_file", resword.getString("you_have_to_provide_spreadsheet"));
                session.setAttribute("version", version);
                return tempFile;
            } else {
                String validationError = validateSpreadsheetFile(f);
                if (validationError != null) {
                    logger.debug("file name:" + f.getName());
                    Validator.addError(errors, "excel_file", validationError);
                    session.setAttribute("version", version);
                    return tempFile;
                }
                logger.debug("file name:" + f.getName());
                tempFile = f.getName();
                // create the inputstream here, so that it can be enclosed in a
                // try/finally block and closed :: BWP, 06/08/2007
//                    FileInputStream inStream = null;
                FileInputStream inStreamClassic = null;
                SpreadSheetTableRepeating htab = null;
                SpreadSheetTableClassic sstc = null;
                // create newCRFBean here
                NewCRFBean nib = null;
                try {
                    Path path = Paths.get(theDir, tempFile);
                    logger.info("the file is at: {}", path);
                    
//                        inStream = new FileInputStream(theDir + tempFile);

                    // *** now change the code here to generate sstable, tbh
                    // 06/07
                    try {
    					htab = new SpreadSheetTableRepeating(/*inStream,*/ ub, version.getName(), locale, currentStudy.getId(), path);
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}

                    htab.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));

                    if (!htab.isRepeating()) {
                        inStreamClassic = new FileInputStream(theDir + tempFile);
                        sstc = new SpreadSheetTableClassic(inStreamClassic, ub, version.getName(), locale, currentStudy.getId());
                        sstc.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));
                    }
                    // logger.debug("finishing with feedin file-input-stream, did
                    // we error out here?");

                    if (htab.isRepeating()) {
                        htab.setCrfId(version.getCrfId());
                        // not the best place for this but for now...
                        session.setAttribute("new_table", "y");
                    } else {
                        sstc.setCrfId(version.getCrfId());
                    }

                    if (htab.isRepeating()) {
                        nib = htab.toNewCRF(sm.getDataSource(), respage);
                    } else {
                        nib = sstc.toNewCRF(sm.getDataSource(), respage);
                    }

                    // bwp; 2/28/07; updated 6/11/07;
                    // This object is created to pull preview information out of
                    // the
                    // spreadsheet
//                        Workbook workbook = null;
//                        FileInputStream inputStream = null;
                    try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(theDir, tempFile))); Workbook wb = WorkbookFactory.create(bis)) {
//                            inputStream = new FileInputStream(theDir + tempFile);
//                            workbook = WorkbookFactory.create(bis);//new HSSFWorkbook(inputStream);
                        // Store the Sections, Items, Groups, and CRF name and
                        // version information
                        // so they can be displayed in a preview. The Map
                        // consists of the
                        // names "sections," "items," "groups," and "crf_info"
                        // as keys, each of which point
                        // to a Map containing data on those CRF sections.

                        // Check if it's the old template
                        Preview preview;
                        if (htab.isRepeating()) {

                            // the preview uses date formatting with default
                            // values in date fields: yyyy-MM-dd
                            preview = new SpreadsheetPreviewNw();

                        } else {
                            preview = new SpreadsheetPreview();

                        }
                        session.setAttribute("preview_crf", preview.createCrfMetaObject(wb));
                    } catch (Exception exc) { // opening the stream could
                        // throw FileNotFoundException
                        String message = resword.getString("the_application_encountered_a_problem_uploading_CRF");
                        logger.error("{} : {}", message , exc.getMessage(), exc);
                        this.addPageMessage(message);
                    } 
//                        finally {
//                            if (inputStream != null) {
//                                try {
//                                    inputStream.close();
//                                } catch (IOException io) {
//                                    // ignore this close()-related exception
//                                }
//                            }                       
//                        }
                    ArrayList<ItemBean> ibs = isItemSame(nib.getItems(), version);

                    if (!ibs.isEmpty()) {
                        ArrayList<String> warnings = new ArrayList<>();
                        warnings.add(resexception.getString("you_may_not_modify_items"));
                        for (int i = 0; i < ibs.size(); i++) {
                            ItemBean ib = (ItemBean) ibs.get(i);
                            if (ib.getOwner().getId() == ub.getId()) {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). UNITS " + resword.getString("and") + " DATA_TYPE(PDATE to DATE) "
                                        + resexception.getString("will_not_be_changed_if") + " PHI, DESCRIPTION, DATA_TYPE from PDATE to DATE "
                                        + resexception.getString("will_be_changed_if_you_continue"));
                            } else {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). " + resexception.getString("these_field_cannot_be_modified_because_not_owner"));
                            }

                            request.setAttribute("warnings", warnings);
                        }
                    }
                    ItemBean ib = isResponseValid(nib.getItems(), version);
                    if (ib != null) {

                        nib.getErrors().add(
                                resword.getString("the_item") + ": " + ib.getName() + " " + resexception.getString("in_your_spreadsheet_already_exits_in_DB"));
                    }
                } catch (IOException io) {
                    logger.warn("Opening up the Excel file caused an error. the error message is: " + io.getMessage());

                } finally {
//                        if (inStream != null) {
//                            try {
//                                inStream.close();
//                            } catch (IOException ioe) {
//                            }
//                        }
                    if (inStreamClassic != null) {
                        try {
                            inStreamClassic.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
                // request.setAttribute("excelErrors", .getErrors());
                session.setAttribute("excelErrors", nib.getErrors());
                session.setAttribute("htmlTable", nib.getHtmlTable());
                session.setAttribute("nib", nib);
            }
        }
        return tempFile;
    }

    /**
     * Detects the spreadsheet type by inspecting the file's magic bytes.
     * xlsx and ods are both ZIP containers (signature 50 4B 03 04) and are
     * distinguished by inspecting the zip content. xls (BIFF) has the OLE2
     * signature D0 CF 11 E0 A1 B1 1A E1.
     *
     * @param f the file to inspect
     * @return "xlsx", "ods", "xls", or null if no known format was detected
     */
    private String detectSpreadsheetTypeByMagicBytes(File f) {
        byte[] header = new byte[8];
        int read;
        try (InputStream is = new FileInputStream(f)) {
            read = is.read(header);
            if (read < 4) {
                return null;
            }
        } catch (IOException e) {
            logger.warn("Could not read file header for {}: {}", f.getName(), e.getMessage());
            return null;
        }

        boolean isZip = (header[0] == 0x50 && header[1] == 0x4B &&
                          header[2] == 0x03 && header[3] == 0x04);
        if (isZip) {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(f))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();
                    if ("mimetype".equals(name)) {
                        String mimeType = readZipEntryAsString(zis).trim();
                        if (mimeType.contains("opendocument.spreadsheet")) {
                            return "ods";
                        }
                    } else if ("[Content_Types].xml".equals(name) || name.startsWith("xl/")) {
                        return "xlsx";
                    }
                }
            } catch (IOException e) {
                logger.warn("Could not inspect zip content for {}: {}", f.getName(), e.getMessage());
            }
            return null;
        }

        boolean isOle2 = (read >= 8 &&
                (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF &&
                (header[2] & 0xFF) == 0x11 && (header[3] & 0xFF) == 0xE0 &&
                (header[4] & 0xFF) == 0xA1 && (header[5] & 0xFF) == 0xB1 &&
                (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0xE1);
        if (isOle2) {
            return "xls";
        }

        return null;
    }

    /**
     * Reads the remaining bytes of the current zip entry from the given stream
     * and returns them as a UTF-8 string. Java-8-compatible replacement for
     * InputStream#readAllBytes(), which is only available since Java 9.
     *
     * @param zis the zip input stream positioned at the entry to read
     * @return the entry's content as a UTF-8 string
     * @throws IOException if reading fails
     */
    private String readZipEntryAsString(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Validates that the uploaded file's extension matches its actual content,
     * based on magic-byte detection. Accepts xlsx and ods files only; legacy
     * xls files (and any other unsupported format) result in a specific
     * error message.
     *
     * @param f the file to validate
     * @return a localized error message if validation fails, or null if the file is valid
     */
    private String validateSpreadsheetFile(File f) {
        String name = f.getName();
        boolean hasXlsxExt = name.toLowerCase().endsWith(".xlsx");
        boolean hasOdsExt = name.toLowerCase().endsWith(".ods");

        String detectedType = detectSpreadsheetTypeByMagicBytes(f);

        if (hasXlsxExt || hasOdsExt) {
            if (detectedType == null) {
                return resword.getString("file_extension_does_not_match_file_content");
            }
            if (hasXlsxExt && !"xlsx".equals(detectedType)) {
                return resword.getString("file_extension_does_not_match_file_content");
            }
            if (hasOdsExt && !"ods".equals(detectedType)) {
                return resword.getString("file_extension_does_not_match_file_content");
            }
            return null; // valid
        }

        // Extension is not xlsx/ods -> check whether it's a legacy xls file
        if ("xls".equals(detectedType)) {
            logger.debug("file appears to be a legacy .xls file (BIFF format), name: {}", name);
            return resword.getString("xls_format_not_supported_please_use_xlsx_or_ods");
        }

        return respage.getString("file_you_uploaded_not_seem_excel_spreadsheet");
    }
    
    
    
    
//    /**
//     * Detects the spreadsheet type by inspecting the file's magic bytes.
//     * xlsx and ods are both ZIP containers (signature 50 4B 03 04) and are
//     * distinguished by inspecting the zip content. xls (BIFF) has the OLE2
//     * signature D0 CF 11 E0 A1 B1 1A E1.
//     *
//     * @param f the file to inspect
//     * @return "xlsx", "ods", "xls", or null if no known format was detected
//     */
//    private String detectSpreadsheetTypeByMagicBytes(File f) {
//        byte[] header = new byte[8];
//        int read;
//        try (InputStream is = new FileInputStream(f)) {
//            read = is.read(header);
//            if (read < 4) {
//                return null;
//            }
//        } catch (IOException e) {
//            logger.warn("Could not read file header for {}: {}", f.getName(), e.getMessage());
//            return null;
//        }
//
//        boolean isZip = (header[0] == 0x50 && header[1] == 0x4B &&
//                          header[2] == 0x03 && header[3] == 0x04);
//        if (isZip) {
//            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(f))) {
//                ZipEntry entry;
//                while ((entry = zis.getNextEntry()) != null) {
//                    String name = entry.getName();
//                    if ("mimetype".equals(name)) {
//                        String mimeType = readZipEntryAsString(zis).trim();
//                        if (mimeType.contains("opendocument.spreadsheet")) {
//                            return "ods";
//                        }
//                    } else if ("[Content_Types].xml".equals(name) || name.startsWith("xl/")) {
//                        return "xlsx";
//                    }
//                }
//            } catch (IOException e) {
//                logger.warn("Could not inspect zip content for {}: {}", f.getName(), e.getMessage());
//            }
//            return null;
//        }
//
//        boolean isOle2 = (read >= 8 &&
//                (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF &&
//                (header[2] & 0xFF) == 0x11 && (header[3] & 0xFF) == 0xE0 &&
//                (header[4] & 0xFF) == 0xA1 && (header[5] & 0xFF) == 0xB1 &&
//                (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0xE1);
//        if (isOle2) {
//            return "xls";
//        }
//
//        return null;
//    }
//    
//    /**
//     * Reads the remaining bytes of the current zip entry from the given stream
//     * and returns them as a UTF-8 string. Java-8-compatible replacement for
//     * InputStream#readAllBytes(), which is only available since Java 9.
//     *
//     * @param zis the zip input stream positioned at the entry to read
//     * @return the entry's content as a UTF-8 string
//     * @throws IOException if reading fails
//     */
//    private String readZipEntryAsString(ZipInputStream zis) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int len;
//        while ((len = zis.read(buffer)) > 0) {
//            baos.write(buffer, 0, len);
//        }
//        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
//    }
}
