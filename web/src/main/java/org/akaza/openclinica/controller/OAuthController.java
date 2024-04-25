package org.akaza.openclinica.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static org.akaza.openclinica.web.filter.OpenClinicaUsernamePasswordAuthenticationFilter.*;


@Controller
public class OAuthController {
    @Value("${oauth.server}")
    private String oauth_server;
    private ApplicationContext context;
    private DataSource dataSource;
    private SecurityManager securityManager;
    private AuthenticationManager authenticationManager;

    final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    public OAuthController(ApplicationContext context, DataSource dataSource,
                           SecurityManager securityManager, AuthenticationManager authenticationManager) {
        this.context = context;
        this.dataSource = dataSource;
        this.securityManager = securityManager;
        this.authenticationManager = authenticationManager;
    }

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

    @RequestMapping("/oauth")
    public String oauth(HttpServletRequest request, HttpServletResponse response/*ModelMap modelMap*/) {
        String oauth_server = CoreResources.getField("oauth.url"); // "https://cdcoauth.alagant.com";
        String self_url = request.getRequestURL().toString().replace(request.getRequestURI(),"");
        String oauth_redirect_uri = self_url + "/pages/login/login";

        String oauth_code = request.getParameter("code");
        String oauth_client_id = CoreResources.getField("oauth.clientId");
        String oauth_client_secret = CoreResources.getField("oauth.clientSecret");

        StudyDAO studyDAO = new StudyDAO(dataSource);

        if(oauth_code == null || oauth_code.isEmpty()) {
            return "redirect:/";
        }

        try {
            String oauth_token_url = CoreResources.getField("oauth.tokenUrl"); /* oauth_base_url + "/token";*/
            URL url = new URL(oauth_token_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "LC-"+getSaltString());
            //connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String bheader = Base64.getEncoder().encodeToString( (oauth_client_id+":"+oauth_client_secret).getBytes());
            connection.setRequestProperty("Authorization", "Basic " + bheader );

            StringBuilder sbRequest = new StringBuilder("grant_type=authorization_code");
            sbRequest.append("&code="+oauth_code);
            sbRequest.append("&client_id="+oauth_client_id);
            sbRequest.append("&client_secret="+oauth_client_secret);
            sbRequest.append("&redirect_uri="+ URLEncoder.encode(oauth_redirect_uri, "utf-8"));

            String access_token = "";
            ObjectMapper objectMapper = new ObjectMapper();

            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream()));
            bufferedWriter.write(sbRequest.toString());
            bufferedWriter.flush();
            bufferedWriter.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                JsonNode jsonNode = objectMapper.readTree(
                        new InputStreamReader(connection.getInputStream(), "utf-8")
                );

                access_token = jsonNode.get("access_token").asText();
                connection.disconnect();
            }
            else {
                InputStreamReader isr = new InputStreamReader(connection.getErrorStream(), "utf-8");
                String responseError = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
                logger.info(responseError);
            }


            connection = (HttpURLConnection) new URL( CoreResources.getField("oauth.userInfoUrl") ).openConnection();
            connection.setRequestProperty("User-Agent", "LC-"+getSaltString());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + access_token);
            connection.setDoOutput(true);

            JsonNode jsonNode = objectMapper.readTree(
                    new InputStreamReader(connection.getInputStream(), "utf-8")
            );
            String a3rd_email = jsonNode.get("email").asText();

            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
            //find the Bean by username,
            UserAccountBean oauthAccount = userAccountDAO.findByEmail(a3rd_email);

            // the useraccount does not exist create {{{
            if(oauthAccount == null || oauthAccount.getId()<1) {
                return "redirect:/pages/login/login?action=nooauthlogin";
            }
            //}}}


            userAccountDAO.disableUpdatePassword(oauthAccount);
            if(oauthAccount.getPasswdTimestamp()==null)
                oauthAccount.setPasswdTimestamp(new Date());

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    oauthAccount.getName(), ""/*oauthAccount.getPasswd()*/);


            // Place the last username attempted into HttpSession for views
            HttpSession session = request.getSession(false);

            if (session != null ) {
                request.getSession().setAttribute(
                        SPRING_SECURITY_LAST_USERNAME_KEY,
                        TextEscapeUtils.escapeEntities(oauthAccount.getName() ));
            }

            ArrayList<StudyUserRoleBean> roles = userAccountDAO.findAllRolesByUserName(oauthAccount.getName());


            Authentication authentication = new OAuthAuthentication(oauthAccount);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute(SecureController.USER_BEAN_NAME, oauthAccount);

            request.getSession().setAttribute("oauth_code", oauth_code);

            return "redirect:/MainMenu";
        }
        catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return "redirect:/pages/login/login";
        }
        /*
        catch(NoSuchAlgorithmException ex) {
                logger.error(ex.getMessage(), ex);
            return "redirect:/pages/login/login";
        }*/
        catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "redirect:/pages/login/login";
        }
    }
}