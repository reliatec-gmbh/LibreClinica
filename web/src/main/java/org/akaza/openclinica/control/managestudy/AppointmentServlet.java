package org.akaza.openclinica.control.managestudy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;

public class AppointmentServlet extends SecureController {
    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    @Override
    protected void processRequest() throws Exception {
        int subjectId = 0;

        try {
            String parameterSubjectId = request.getParameter("id");
            if(parameterSubjectId == null || parameterSubjectId.isEmpty())
                throw new NumberFormatException();
            subjectId = Integer.parseInt(parameterSubjectId) ;
            if(subjectId<0) throw new NumberFormatException();
        }
        catch (NumberFormatException ex) {
            addPageMessage("The subject Id parameter has an invalid format");
            forwardPage(Page.VIEW_STUDY);
            return;
        }


        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean studySubjectBean = studySubjectDAO
                .findBySubjectIdAndStudy(subjectId, currentStudy);
        if(studySubjectBean == null || studySubjectBean.getSubjectId()<1) {
            addPageMessage("The subject could not be retrieved");
            forwardPage(Page.VIEW_STUDY);
            return;
        }

        TreeMap<Integer, String> hashMap = new TreeMap<Integer, String>();
        try {
            String appointments_url = CoreResources.getField("dmm.url") + "/appointments/" +
                    studySubjectBean.getSubjectId();
            URL url = new URL(appointments_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "LC-"+getSaltString());
            
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(
                        new InputStreamReader(connection.getInputStream(), "utf-8")
                );
                JsonNode appointments = jsonNode.get("appointments");
                Iterator<JsonNode> iterator = appointments.iterator();

                while(iterator.hasNext()) {
                    JsonNode appointment = iterator.next();
                    hashMap.put(appointment.get(0).asInt(), appointment.get(1).asText());
                }
                connection.disconnect();

            }
            else if(responseCode == 500) {
                addPageMessage("An error occurred when retrieving the appointment schedule");
                //Dump dummy data
                /*
                hashMap.put(1, "01-01-1990");
                hashMap.put(2, "02-01-1990");
                hashMap.put(3, "03-01-1990");*/
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            addPageMessage("An error occurred when retrieving the appointment schedule");
            //throw new RuntimeException(e);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
            addPageMessage("Could not fetch the appointment schedule");
            //throw new RuntimeException(e);
        }

        request.setAttribute("subject", studySubjectBean);
        request.setAttribute("appointments", hashMap);
        forwardPage(Page.APPOINTMENT);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
