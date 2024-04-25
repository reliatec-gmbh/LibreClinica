package org.akaza.openclinica.control.managestudy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.akaza.openclinica.bean.managestudy.ProtocolDeviationBean;
import org.akaza.openclinica.bean.managestudy.ProtocolDeviationSeverityBean;
import org.akaza.openclinica.bean.managestudy.ProtocolDeviationSubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.submit.ProtocolDeviationTableFactory;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class ProtocolDeviationServlet extends SecureController {
    private ProtocolDeviationSeverityDAO protocolDeviationSeverityDAO;
    private ProtocolDeviationDAO protocolDeviationDAO;
    private ProtocolDeviationSubjectDAO protocolDeviationSubjectDAO;
    private ProtocolDeviationSeverityDAO getProtocolDeviationSeverityDAO() {
        if(protocolDeviationSeverityDAO == null)
            protocolDeviationSeverityDAO = new ProtocolDeviationSeverityDAO(sm.getDataSource());

        return protocolDeviationSeverityDAO;
    }

    private ProtocolDeviationDAO getProtocolDeviationDAO() {
        if(protocolDeviationDAO == null)
            protocolDeviationDAO = new ProtocolDeviationDAO(sm.getDataSource());

        return protocolDeviationDAO;
    }

    private short shortValueOrZero(String field) {
        short retval = 0;
        if(request.getParameter(field)!= null) {
            retval = Short.parseShort(request.getParameter(field));
        }

        return retval;
    }

    private Date dateValueOrNull(String field) {
        Date retval = null;
        if(request.getParameter(field) == null) return null;

        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        try {
            retval = dateTimeFormatter.parse(request.getParameter(field));
        }
        catch (ParseException ex) {
            //ignore the error, we'll return null at the next statement
        }

        return retval;
    }



    private ProtocolDeviationSubjectDAO getProtocolDeviationSubjectDAO() {
        if(protocolDeviationSubjectDAO == null)
            protocolDeviationSubjectDAO = new ProtocolDeviationSubjectDAO(sm.getDataSource());

        return protocolDeviationSubjectDAO;
    }

    private void processSaveProtocolDeviation() throws OpenClinicaException {
        ProtocolDeviationDAO protocolDeviationDAO = new ProtocolDeviationDAO(sm.getDataSource());

        int protocolDeviationId = -1;
        try {
            String stringProtocolDeviationId = request.getParameter("protocol_deviation_id");
            if(stringProtocolDeviationId != null && !stringProtocolDeviationId.isEmpty())
                protocolDeviationId = Integer.parseInt(stringProtocolDeviationId);
        }
        catch(NumberFormatException ex) {
            addPageMessage("Protocol deviation id not found or it has an incorrect format");
            return;
        }

        String[] subjectsId = request.getParameterValues("subjects[]");
        ProtocolDeviationBean pdb = protocolDeviationDAO.findByPKAndStudy(protocolDeviationId, currentStudy);
        if(pdb == null || pdb.getId()<1) {
            pdb = new ProtocolDeviationBean();
            pdb.setStudyId(currentStudy.getId());
        }

        //Populate data {{{
        pdb.setItemA1(shortValueOrZero("item_a_1"));
        pdb.setItemA2(shortValueOrZero("item_a_2"));
        pdb.setItemA3(dateValueOrNull("item_a_3"));
        pdb.setItemA4(dateValueOrNull("item_a_4"));
        pdb.setItemA5(dateValueOrNull("item_a_5"));
        pdb.setItemA6(shortValueOrZero("item_a_6"));
        pdb.setItemA7(shortValueOrZero("item_a_7"));
        pdb.setItemA7_1(dateValueOrNull("item_a_7_1"));
        pdb.setItemA8(shortValueOrZero("item_a_8"));
        pdb.setItemB1(shortValueOrZero("item_b_1"));
        pdb.setItemB2(shortValueOrZero("item_b_2"));
        pdb.setItemB3(shortValueOrZero("item_b_3"));
        pdb.setItemB4(shortValueOrZero("item_b_4"));
        pdb.setItemB5(shortValueOrZero("item_b_5"));
        pdb.setItemB6(shortValueOrZero("item_b_6"));
        pdb.setItemB7(shortValueOrZero("item_b_7"));
        pdb.setItemB8(shortValueOrZero("item_b_8"));
        pdb.setItemB9(shortValueOrZero("item_b_9"));
        pdb.setItemB10(shortValueOrZero("item_b_10"));
        pdb.setItemB11(shortValueOrZero("item_b_11"));
        pdb.setItemB12(shortValueOrZero("item_b_12"));
        pdb.setItemB13(shortValueOrZero("item_b_13"));
        pdb.setItemB14(shortValueOrZero("item_b_14"));
        pdb.setItemB15(shortValueOrZero("item_b_15"));
        pdb.setItemB16(shortValueOrZero("item_b_16"));
        pdb.setItemB17(shortValueOrZero("item_b_17"));
        pdb.setItemB18(shortValueOrZero("item_b_18"));

        pdb.setItemC1_1(shortValueOrZero("item_c_1_1"));
        pdb.setItemC1_2(shortValueOrZero("item_c_1_2"));
        pdb.setItemC1_3(shortValueOrZero("item_c_1_3"));
        pdb.setItemC1_4(shortValueOrZero("item_c_1_4"));
        pdb.setItemC1_5(shortValueOrZero("item_c_1_5"));
        pdb.setItemC1_6(shortValueOrZero("item_c_1_6"));
        pdb.setItemC1_7(shortValueOrZero("item_c_1_7"));
        pdb.setItemC1_8(shortValueOrZero("item_c_1_8"));
        pdb.setItemC1_9(shortValueOrZero("item_c_1_9"));

        pdb.setItemC1_10(request.getParameter("item_c_1_10"));
        pdb.setItemC2(request.getParameter("item_c_2"));
        //pdb.setItemD1_A(request.getParameter("item_d_1_a"));
        pdb.setItemD1_A(dateValueOrNull("item_d_1_a"));
        pdb.setItemD1_B(request.getParameter("item_d_1_b"));
        pdb.setItemE1(request.getParameter("item_e_1"));
        pdb.setItemE2(request.getParameter("item_e_2"));
        pdb.setItemE3(request.getParameter("item_e_3"));
        pdb.setItemE4(request.getParameter("item_e_4"));
        pdb.setItemF1(request.getParameter("item_f_1"));
        pdb.setItemF2(request.getParameter("item_f_2"));
        //pdb.setItemF3(request.getParameter("item_f_3"));
        pdb.setItemF3(dateValueOrNull("item_f_3"));

        pdb.setItemG1(shortValueOrZero("item_g_1"));
        pdb.setItemG2_1(shortValueOrZero("item_g_2_1"));
        pdb.setItemG2_2(shortValueOrZero("item_g_2_2"));
        pdb.setItemG2_3(shortValueOrZero("item_g_2_3"));
        pdb.setItemG2_4(shortValueOrZero("item_g_2_4"));
        pdb.setItemG3(shortValueOrZero("item_g_3"));
        pdb.setItemG4(shortValueOrZero("item_g_4"));
        pdb.setItemG5(shortValueOrZero("item_g_5"));
        //pdb.setItemG6(request.getParameter("item_g_6"));
        pdb.setItemG6(dateValueOrNull("item_g_6"));
        pdb.setItemG6_1_A(request.getParameter("item_g_6_1_a"));
        pdb.setItemG6_1_B(shortValueOrZero("item_g_6_1_b"));
        pdb.setItemG6_1_C(request.getParameter("item_g_6_1_c"));
        pdb.setItemG6_2_A(request.getParameter("item_g_6_2_a"));
        pdb.setItemG6_2_B(shortValueOrZero("item_g_6_2_b"));
        pdb.setItemG6_2_C(request.getParameter("item_g_6_2_c"));
        pdb.setItemG6_3_A(request.getParameter("item_g_6_3_a"));
        pdb.setItemG6_3_B(shortValueOrZero("item_g_6_3_b"));
        pdb.setItemG6_3_C(request.getParameter("item_g_6_3_c"));
        pdb.setItemG6_4_A(request.getParameter("item_g_6_4_a"));
        pdb.setItemG6_4_B(shortValueOrZero("item_g_6_4_b"));
        pdb.setItemG6_4_C(request.getParameter("item_g_6_4_c"));
        pdb.setItemG7(request.getParameter("item_g_7"));
        pdb.setItemG8(request.getParameter("item_g_8"));
        pdb.setItemG9(request.getParameter("item_g_9"));
        //}}}

        ProtocolDeviationSubjectDAO protocolDeviationSubjectDAO =
                new ProtocolDeviationSubjectDAO(sm.getDataSource());

        if(pdb.getId()<1) {
            pdb = protocolDeviationDAO.create(pdb);


        }
        else {
            protocolDeviationDAO.update(pdb);
        }

        //add subjects to the protocol deviation
        if(subjectsId == null) return;
        
        for(String s: subjectsId) {
            int subjectId = Integer.parseInt(s);
            ProtocolDeviationSubjectBean pdsb = protocolDeviationSubjectDAO
                    .findSubjectById(pdb.getProtocolDeviationId(), subjectId);
            if(pdsb == null || pdsb.getId()<1) {
                pdsb = new ProtocolDeviationSubjectBean();
                pdsb.setProtocolDeviationId(pdb.getProtocolDeviationId());
                pdsb.setSubjectId(subjectId);
                protocolDeviationSubjectDAO.create(pdsb);
            }
        }

    }
    @Override
    protected void processRequest() throws Exception {
        String action = request.getParameter("action");

        if(request.getMethod().compareTo("POST")==0)
            processSaveProtocolDeviation();
        else if(action != null && (action.compareTo("get")==0) &&
                request.getParameter("pdid") != null) {
            String stringProtocolDeviationId = request.getParameter("pdid");
            int protocolDeviationId = Integer.parseInt(stringProtocolDeviationId);
            getProtocolDeviationWithSubjects(protocolDeviationId);
            return;
        }


        createTable();


        forwardPage(Page.PROTOCOL_DEVIATIONS);
        /*
        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(showMoreLink);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setStudyParameterValueDAO(getStudyParameterValueDAO());
        String findSubjectsHtml = factory.createTable(request, response).render();

        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
        * */
    }

    private void getProtocolDeviationWithSubjects(int id) throws IOException {
        PrintWriter printWriter = response.getWriter();;
        ProtocolDeviationBean protocolDeviationBean =
                getProtocolDeviationDAO().findByPKAndStudy(id, currentStudy);
        ArrayList<ProtocolDeviationSubjectBean> subjects =
                getProtocolDeviationSubjectDAO()
                        .findByProtocolDeviation(protocolDeviationBean.getId());
        protocolDeviationBean.setSubjects(subjects);

        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        printWriter.print(objectWriter.writeValueAsString(protocolDeviationBean));
    }

    private void createTable() {
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());

        ProtocolDeviationTableFactory  factory = new ProtocolDeviationTableFactory();
        factory.setStudyBean(currentStudy);
        factory.setProtocolDeviationDAO(getProtocolDeviationDAO());

        String findProtocolDeviationsHtml = factory.createTable(request, response).render();
        request.setAttribute("findProtocolDeviationsHtml", findProtocolDeviationsHtml);
        request.setAttribute("subjects", studySubjectDAO.findAllByStudyId(currentStudy.getId()));
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
