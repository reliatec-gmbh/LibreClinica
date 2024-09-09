/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;
import static org.akaza.openclinica.core.util.ClassCastHelper.asEnumeration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.SdvFilterDataBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.akaza.openclinica.web.table.sdv.SubjectIdSDVFactory;
import org.jmesa.facade.TableFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller("sdvController")
public class SDVController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public final static String SUBJECT_SDV_TABLE_ATTRIBUTE = "sdvTableAttribute";
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;

    @Autowired
    @Qualifier("sdvFactory")
    private SubjectIdSDVFactory sdvFactory;

    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    public SDVController() {
    }

    @RequestMapping("/viewSubjectAggregate")
    public ModelMap viewSubjectAggregateHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId) {
		if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }
        ModelMap gridMap = new ModelMap();
        HttpSession session = request.getSession();
        boolean showMoreLink = false;
        if(session.getAttribute("sSdvRestore") != null && session.getAttribute("sSdvRestore") == "false") {
            session.setAttribute("sSdvRestore", "true");
            showMoreLink = true;
        }else if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else if(session.getAttribute("s_sdv_showMoreLink")!=null) {
            showMoreLink = Boolean.parseBoolean(session.getAttribute("s_sdv_showMoreLink")+"");
        }else {
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");
        session.setAttribute("s_sdv_showMoreLink", showMoreLink+"");

        request.setAttribute("studyId", studyId);
        String restore = (String)request.getAttribute("s_sdv_restore");
        restore = restore != null && restore.length()>0 ? restore : "false";
        request.setAttribute("s_sdv_restore", restore);
        // request.setAttribute("studySubjectId",studySubjectId);
        /*SubjectIdSDVFactory tableFactory = new SubjectIdSDVFactory();
        * @RequestParam("studySubjectId") int studySubjectId,*/
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = asArrayList(request.getAttribute("pageMessages"), String.class);
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        sdvFactory.showMoreLink = showMoreLink;
        TableFacade facade = sdvFactory.createTable(request, response);
        String sdvMatrix = facade.render();
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDV")
    public ModelMap viewSubjectHandler(HttpServletRequest request, @RequestParam("studySubjectId") int studySubjectId, @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();
        /*EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        List<EventCRFBean> eventCRFBeans = eventCRFDAO.findAllByStudySubject(studySubjectId);*/

        request.setAttribute("studyId", studyId);
        request.setAttribute("studySubjectId", studySubjectId);
        //  request.setAttribute("isViewSubjectRequest","y");
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = asArrayList(request.getAttribute("pageMessages"), String.class);
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        String sdvMatrix = sdvUtil.renderSubjectsTableWithLimit(request, studyId, studySubjectId);
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDVtmp")
    public ModelMap viewAllSubjectHandler(HttpServletRequest request, @RequestParam("studyId") int studyId, HttpServletResponse response) {

        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }
    ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
        // Reseting the side info panel set by SecureControler Mantis Issue: 8680.
        // Todo need something to reset panel from all the Spring Controllers
        StudyInfoPanel panel = new StudyInfoPanel();
        panel.reset();
        HttpSession session = request.getSession();
        request.getSession().setAttribute("panel", panel);

        ModelMap gridMap = new ModelMap();
        //set up request attributes for sidebar
        //Not necessary when using old page design...
        // setUpSidebar(request);
        boolean showMoreLink = false;
        if(session.getAttribute("tableFacadeRestore") != null && session.getAttribute("tableFacadeRestore") == "false") {
            session.setAttribute("tableFacadeRestore","true");
            session.setAttribute("sSdvRestore", "false");
            showMoreLink = true;
        }else if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else if(session.getAttribute("sdv_showMoreLink")!=null) {
            showMoreLink = Boolean.parseBoolean(session.getAttribute("sdv_showMoreLink")+"");
        } else {
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");
        session.setAttribute("sdv_showMoreLink", showMoreLink+"");
        request.setAttribute("studyId", studyId);
        String restore = (String)request.getAttribute("sdv_restore");
        restore = restore != null && restore.length()>0 ? restore : "false";
        request.setAttribute("sdv_restore", restore);
        //request.setAttribute("imagePathPrefix","../");
        //We need a study subject id for the first tab;
        Integer studySubjectId = (Integer) request.getAttribute("studySubjectId");
        studySubjectId = studySubjectId == null || studySubjectId == 0 ? 0 : studySubjectId;
        request.setAttribute("studySubjectId", studySubjectId);

        //set up the elements for the view's filter box
        // sdvUtil.prepareSDVSelectElements(request,studyBean);

        ArrayList<String> pageMessages = asArrayList(request.getAttribute("pageMessages"), String.class);
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        String sdvMatrix = sdvUtil.renderEventCRFTableWithLimit(request, studyId, "../");

        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDVform")
    public ModelMap viewAllSubjectFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        //  List<StudyEventBean> studyEventBeans = studyEventDAO.findAllByStudy(studyBean);
        //  List<EventCRFBean> eventCRFBeans = sdvUtil.getAllEventCRFs(studyEventBeans);

        //set up the parameters to take part in filtering
        ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(new SdvFilterDataBean());
        dataBinder.setAllowedFields(new String[] { "study_subject_id", "studyEventDefinition", "studyEventStatus", "eventCRFStatus", "sdvRequirement",
            "eventcrfSDVStatus", "startUpdatedDate", "endDate", "eventCRFName" });

        dataBinder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(sdf, true));
        dataBinder.bind(request);
        
        request.setAttribute("studyId", studyId);

        ArrayList<String> pageMessages = asArrayList(request.getAttribute("pageMessages"), String.class);
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        String sdvMatrix = sdvUtil.renderEventCRFTableWithLimit(request, studyId, "");
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    /*  @RequestMapping("/viewSubjectAggregateSDV")
    public ModelMap viewSubjectAggregateHandler(HttpServletRequest request,
                                                @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();

        //set up request attributes for sidebar
        setUpSidebar(request);
        String sdvMatrix = sdvUtil.renderSubjectsAggregateTable(studyId,request);
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE,sdvMatrix);
        return gridMap;
    }*/

    //method = RequestMethod.POST
    @RequestMapping("/handleSDVPost")
    public String sdvAllSubjectsFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //The application is POSTing parameters with the name "sdvCheck_" plus the
        //Event CRF id, so the parameter is sdvCheck_534.

        Enumeration<String> paramNames = asEnumeration(request.getParameterNames(), String.class);
        Map<String, String> parameterMap = new HashMap<String, String>();
        String tmpName = "";
        for (; paramNames.hasMoreElements();) {
            tmpName = paramNames.nextElement();
            if (tmpName.contains(SDVUtil.CHECKBOX_NAME)) {
                parameterMap.put(tmpName, request.getParameter(tmpName));
            }
        }
        request.setAttribute("sdv_restore", "true");

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        //In this case, no checked event CRFs were submitted
        if (parameterMap.isEmpty()) {
            pageMessages.add("None of the Event CRFs were selected for SDV.");
            request.setAttribute("pageMessages", pageMessages);
            sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        }
        List<Integer> eventCRFIds = sdvUtil.getListOfSdvEventCRFIds(parameterMap.keySet());
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/handleSDVGet")
    public String sdvOneCRFFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }
        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        request.setAttribute("sdv_restore", "true");

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/handleSDVRemove")
    public String changeSDVHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), false);

        if (updateCRFs) {
            pageMessages.add("The application has unset SDV for the Event CRF.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("sdv_restore", "true");

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/sdvStudySubject")
    public String sdvStudySubjectHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("theStudySubjectId") int studySubjectId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> studySubjectIds = new ArrayList<Integer>();
        studySubjectIds.add(studySubjectId);
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Subject has been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("s_sdv_restore", "true");
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;
    }

    @RequestMapping("/unSdvStudySubject")
    public String unSdvStudySubjectHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("theStudySubjectId") int studySubjectId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        ArrayList<String> pageMessages = new ArrayList<String>();
        List<Integer> studySubjectIds = new ArrayList<Integer>();

        studySubjectIds.add(studySubjectId);
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), false);

        if (updateCRFs) {
            pageMessages.add("The application has unset SDV for the Event CRF.");
        } else {
            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");
        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("s_sdv_restore", "true");
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;

    }

    @RequestMapping("/sdvStudySubjects")
    public String sdvStudySubjectsHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //The application is POSTing parameters with the name "sdvCheck_" plus the
        //Event CRF id, so the parameter is sdvCheck_534.

        Enumeration<String> paramNames = asEnumeration(request.getParameterNames(), String.class);
        Map<String, String> parameterMap = new HashMap<String, String>();
        String tmpName = "";
        for (; paramNames.hasMoreElements();) {
            tmpName = paramNames.nextElement();
            if (tmpName.contains(SDVUtil.CHECKBOX_NAME)) {
                parameterMap.put(tmpName, request.getParameter(tmpName));
            }
        }
        request.setAttribute("s_sdv_restore", "true");

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        //In this case, no checked event CRFs were submitted
        if (parameterMap.isEmpty()) {
            pageMessages.add("None of the Study Subjects were selected for SDV.");
            request.setAttribute("pageMessages", pageMessages);
            sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        }
        List<Integer> studySubjectIds = sdvUtil.getListOfStudySubjectIds(parameterMap.keySet());
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    private UserAccountBean getCurrentUser (HttpServletRequest request){
        UserAccountBean ub = (UserAccountBean)request.getSession().getAttribute("userBean");
        return ub;
    }

    public static void main(String[] args) throws ParseException {

        String pattern = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        Date date = sdf.parse("01/01/2007");
        System.out.println("date = " + date);

    }

	 private boolean mayProceed(HttpServletRequest request) {
        StudyUserRoleBean currentRole = (StudyUserRoleBean)request.getSession().getAttribute("userRole");
        Role r = currentRole.getRole();

        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return true;
        }

        return false;
    }
}
