package org.akaza.openclinica.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

@Controller
public class AppointmentController {
    @RequestMapping(value = "/appointments/{subjectId}", method = RequestMethod.GET)
    public String appointments(@PathVariable("subjectId") long subjectId, Model model) {
        model.addAttribute("subjectId", subjectId);

        HashMap<Integer, String> hashMap = new HashMap<>();
        try {
            String appointments_url = CoreResources.getField("dmm.url") + "/appointments/" + subjectId;
            URL url = new URL(appointments_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                //Dump dummy data
                hashMap.put(1, "01-01-1990");
                hashMap.put(2, "02-01-1990");
                hashMap.put(3, "03-01-1990");
            }
        } catch (MalformedURLException e) {
            //throw new RuntimeException(e);
        }
        catch (IOException e) {
            //throw new RuntimeException(e);
        }

        model.addAttribute("appointments", hashMap);
        return "admin/appointments";
    }
}
