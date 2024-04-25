/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.filter;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.acl.Owner;
import java.util.*;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;

/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.login.AccountConfigurationException;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.otp.MailNotificationService;
import org.akaza.openclinica.service.otp.TowFactorBean;
import org.akaza.openclinica.service.otp.TwoFactorService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.util.Assert;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes an authentication form submission. Called
 * {@code AuthenticationProcessingFilter} prior to Spring Security
 * 3.0.
 * <p>
 * Login forms must present two parameters to this filter: a username and
 * password. The default parameter names to use are contained in the
 * static fields {@link #SPRING_SECURITY_FORM_USERNAME_KEY} and
 * {@link #SPRING_SECURITY_FORM_PASSWORD_KEY}.
 * The parameter names can also be changed by setting the
 * {@code usernameParameter} and {@code passwordParameter}
 * properties.
 * <p>
 * This filter by default responds to the URL {@code /j_spring_security_check}.
 *
 * @author Ben Alex
 * @author Colin Sampaleanu
 * @author Luke Taylor
 * @since 3.0
 */
public class OpenClinicaUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
    public static final String SPRING_SECURITY_FORM_FACTOR = "j_factor";
    private static final String BAD_CREDENTIALS_MESSAGE = "Bad Credentials";
    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
    private boolean postOnly = true;
    private AuditUserLoginDao auditUserLoginDao;
    private ConfigurationDao configurationDao;
    private TwoFactorService factorService;
    private UserAccountDAO userAccountDao;
    private DataSource dataSource;
    private CRFLocker crfLocker;
    private MailNotificationService mailNotificationService;

    public OpenClinicaUsernamePasswordAuthenticationFilter() {
        super("/j_spring_security_check");
    }

    public void setFactorService(TwoFactorService factorService) {
        this.factorService = factorService;
    }

    public void setMailNotificationService(MailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
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

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        // Fail fast if anything mandatory is missing for authentication
        if (isBlank(username) || isBlank(password)) {
            throw new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username.trim(), password);

        // Place the last username attempted into HttpSession for views
        HttpSession session = request.getSession(false);

        if (session != null || getAllowSessionCreation()) {
            request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextEscapeUtils.escapeEntities(username));
        }

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        Authentication authentication = null;
        UserAccountBean userAccountBean = null;
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        //Aca se hace la autenticacion
        try {
            userAccountBean = getUserAccountDao().findByUserName(username);

            if (!userAccountBean.isActive()) {
                throw new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);
            }

            if (factorService.getTwoFactorActivated() && userAccountBean.isTwoFactorActivated()) {
                String factor = request.getParameter(SPRING_SECURITY_FORM_FACTOR);

                if (!factorService.verify(userAccountBean.getAuthsecret(), factor)) {
                    throw new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);
                }
            }
            if (factorService.isTwoFactorActivatedLetterAndOutDated() && !userAccountBean.isTwoFactorActivated()) {
                notifyDeniedLogin(userAccountBean);
                throw new AccountConfigurationException();
            }

            // Manually Checking if the user is locked which should be thrown by authenticate. Mantis Issue: 9016
            // TODO: somebody should find why getAuthenticationManager().authenticate is not working!
            if (userAccountBean.getStatus().isLocked()) {
                throw new LockedException("locked");
            }
            authentication = this.getAuthenticationManager().authenticate(authRequest);
            auditUserLogin(username, LoginStatus.SUCCESSFUL_LOGIN, userAccountBean);
            resetLockCounter(username, LoginStatus.SUCCESSFUL_LOGIN, userAccountBean);
            request.getSession().setAttribute(SecureController.USER_BEAN_NAME, userAccountBean);
            // To remove the locking of Event CRFs previously locked by this user.
            crfLocker.unlockAllForUser(userAccountBean.getId());
        } catch (LockedException le) {
            auditUserLogin(username, LoginStatus.FAILED_LOGIN_LOCKED, userAccountBean);
            notifyDeniedLogin(userAccountBean);
            throw le;
        } catch (BadCredentialsException au) {
            try {
                if (authenticateViaThirdPartyAPI(username, password)) {
                    authentication = this.getAuthenticationManager().authenticate(authRequest);
                    auditUserLogin(username, LoginStatus.SUCCESSFUL_LOGIN, userAccountBean);
                    resetLockCounter(username, LoginStatus.SUCCESSFUL_LOGIN, userAccountBean);
                    request.getSession().setAttribute(SecureController.USER_BEAN_NAME, userAccountBean);
                } else {
                    throw au;
                }
            } catch (BadCredentialsException ex) {
                auditUserLogin(username, LoginStatus.FAILED_LOGIN, userAccountBean);
                lockAccount(username, LoginStatus.FAILED_LOGIN, userAccountBean);
                notifyDeniedLogin(userAccountBean);
                throw ex;
            }

        } catch (AuthenticationException ae) {
            auditUserLogin(username, LoginStatus.FAILED_LOGIN, userAccountBean);
            lockAccount(username, LoginStatus.FAILED_LOGIN, userAccountBean);
            notifyDeniedLogin(userAccountBean);
            throw ae;
        }

        if (mailNotificationService.isMailNotificationEnabled(userAccountBean.getActiveStudyId())) {
            mailNotificationService.sendSuccessfulLoginMail(userAccountBean);
        }

        return authentication;
    }
    private boolean authenticateViaThirdPartyAPI(String username, String password) {
        try {

            System.out.println("Authenticating via third party API");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            String apiUrl = "http://localhost:3000/auth";
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "LC-"+getSaltString());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                byte[] postDataBytes = mapToJsonBytes(credentials);
                wr.write(postDataBytes);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(response.toString());

                    System.out.println("username:" +jsonNode.get("username").asText());
                    System.out.println(jsonNode.get("email").asText());
                    System.out.println(jsonNode.get("firstName").asText());
                    System.out.println(jsonNode.get("lastName").asText());
                    System.out.println(jsonNode.get("institutionalAffiliation").asText());
                    System.out.println(jsonNode.get("role").asText());
                    System.out.println(jsonNode.get("activeStudy").asText());
                    System.out.println(jsonNode.get("userType").asText());
                    System.out.println(jsonNode.get("authorizeSoap").asText());
                    String authToken = jsonNode.get("token").asText();
                    System.out.println("Auth token: " + authToken);

                    UserAccountBean createdUserAccountBean = new UserAccountBean();

                    createdUserAccountBean.setName(jsonNode.get("username").asText());
                    createdUserAccountBean.setFirstName(jsonNode.get("firstName").asText());
                    createdUserAccountBean.setLastName(jsonNode.get("lastName").asText());
                    createdUserAccountBean.setEmail(jsonNode.get("email").asText());
                    createdUserAccountBean.setInstitutionalAffiliation(jsonNode.get("institutionalAffiliation").asText());


                    /*
                    StudyUserRoleBean surb = new StudyUserRoleBean();
                    surb.setRole(Role.MONITOR);
                    createdUserAccountBean.addRole(surb);
                    createdUserAccountBean.add
                     */

                    ServletContext context = getServletContext();
                    SecurityManager sm = (SecurityManager) SpringServletAccess.getApplicationContext(context)
                            .getBean("securityManager");


                    String newDigestPass = sm.encryptPassword(password, createdUserAccountBean.getRunWebservices());
                    createdUserAccountBean.setPasswd(newDigestPass);
                    createdUserAccountBean.setPasswdTimestamp(null);
                    createdUserAccountBean.setLastVisitDate(null);
                    createdUserAccountBean.setStatus(Status.AVAILABLE);
                    createdUserAccountBean.setPasswdChallengeQuestion("");
                    createdUserAccountBean.setPasswdChallengeAnswer("");
                    createdUserAccountBean.setPhone("");
                    //createdUserAccountBean.setOwner(createdUserAccountBean.getOwner());
                    createdUserAccountBean.setRunWebservices(null);
                    createdUserAccountBean.setAccessCode("null");
                    createdUserAccountBean.setEnableApiKey(true);
                    createdUserAccountBean.setRunWebservices(false);

                    getUserAccountDao().create(createdUserAccountBean);
                    AuthoritiesDao authoritiesDao = (AuthoritiesDao)
                            SpringServletAccess.getApplicationContext(context).getBean("authoritiesDao");
                    authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));

                    if (createdUserAccountBean.isTwoFactorMarked()) {
                        TwoFactorService factorService = (TwoFactorService) getWebApplicationContext(getServletContext()).getBean("factorService");
                        if (factorService.isTwoFactorLetter()) {
                            TowFactorBean bean = factorService.generate();
                            createdUserAccountBean.setAuthsecret(bean.getAuthSecret());
                        }
                    }

                    String apiKey = getRandom32ChApiKey() ;
                    //UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                    //createdUserAccountBean = udao.create(createdUserAccountBean);
                    return authTokenIsValid(authToken);
                }
            } else {
                System.out.println("Bad request: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getRandom32ChApiKey() {
        String uuid = UUID.randomUUID().toString();
        //logger.debug(uuid.replaceAll("-", ""));
        return uuid.replaceAll("-", "");
    }

    /*public Boolean isApiKeyExist(String uuid) {
        UserAccountDAO userDao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean user = userDao.findByApiKey(uuid);
        return user != null && user.isActive();
    }*/
    private static byte[] mapToJsonBytes(Map<String, String> map) throws Exception {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, String> jsonToMap(String jsonString) {
        Map<String, String> map = new HashMap<>();
        String[] keyValuePairs = jsonString.substring(1, jsonString.length() - 1).split(",");
        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":");
            map.put(entry[0].trim(), entry[1].trim());
        }
        return map;
    }

    private static boolean authTokenIsValid(String authToken) {
        return authToken != null && !authToken.isEmpty();
    }

    private Authentication createSuccessfulAuthenticationToken(String username, String password) {
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    private void notifyDeniedLogin(UserAccountBean userAccount) {
        if (userAccount != null && userAccount.isActive()) {
            if (mailNotificationService.isMailNotificationEnabled(userAccount.getActiveStudyId())) {
                mailNotificationService.sendDeniedLoginMail(userAccount);
            }
        }
    }

    private void auditUserLogin(String username, LoginStatus loginStatus, UserAccountBean userAccount) {
        AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
        auditUserLogin.setUserName(username);
        auditUserLogin.setLoginStatus(loginStatus);
        auditUserLogin.setLoginAttemptDate(new Date());
        auditUserLogin.setUserAccountId((userAccount != null && userAccount.isActive()) ? userAccount.getId() : null);
        getAuditUserLoginDao().saveOrUpdate(auditUserLogin);
    }

    private void resetLockCounter(String username, LoginStatus loginStatus, UserAccountBean userAccount) {
        if (userAccount != null && userAccount.isActive()) {
            getUserAccountDao().updateLockCounter(userAccount.getId(), 0);
        }
    }

    private void lockAccount(String username, LoginStatus loginStatus, UserAccountBean userAccount) {
        Boolean lockFeatureActive = Boolean.valueOf(getConfigurationDao().findByKey("user.lock.switch").getValue());
        if (userAccount != null && userAccount.isActive() && lockFeatureActive) {
            Integer count = userAccount.getLockCounter();
            String lockCountString = getConfigurationDao().findByKey("user.lock.allowedFailedConsecutiveLoginAttempts").getValue();
            Integer lockThreshold = Integer.valueOf(lockCountString);
            if (count < lockThreshold) {
                getUserAccountDao().updateLockCounter(userAccount.getId(), ++count);
            }
            if (count >= lockThreshold) {
                getUserAccountDao().lockUser(userAccount.getId());
            }
        }
    }

    /**
     * Enables subclasses to override the composition of the password, such as
     * by including additional values
     * and a separator.
     * <p>
     * This might be used for example if a postcode/zipcode was required in
     * addition to the
     * password. A delimiter such as a pipe (|) should be used to separate the
     * password and extended value(s). The
     * <code>AuthenticationDao</code> will need to generate the expected
     * password in a corresponding manner.
     * </p>
     *
     * @param request so that request attributes can be retrieved
     * @return the password that will be presented in the
     *         <code>Authentication</code> request token to the
     *         <code>AuthenticationManager</code>
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(passwordParameter);
    }

    /**
     * Enables subclasses to override the composition of the username, such as
     * by including additional values
     * and a separator.
     *
     * @param request so that request attributes can be retrieved
     * @return the username that will be presented in the
     *         <code>Authentication</code> request token to the
     *         <code>AuthenticationManager</code>
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(usernameParameter);
    }

    /**
     * Provided so that subclasses may configure what is put into the
     * authentication request's details
     * property.
     *
     * @param request that an authentication request is being created for
     * @param authRequest the authentication request object that should have its
     *            details set
     */
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    /**
     * Sets the parameter name which will be used to obtain the username from
     * the login request.
     *
     * @param usernameParameter the parameter name. Defaults to "j_username".
     */
    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    /**
     * Sets the parameter name which will be used to obtain the password from the login request
     *
     * @param passwordParameter the parameter name. Defaults to "j_password".
     */
    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    /**
     * Defines whether only HTTP POST requests will be allowed by this filter.
     * If set to true, and an authentication request is received which is not a
     * POST request, an exception will
     * be raised immediately and authentication will not be attempted. The
     * <tt>unsuccessfulAuthentication()</tt> method
     * will be called as if handling a failed authentication.
     * <p>
     * Defaults to <tt>true</tt> but may be overridden by subclasses.
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getUsernameParameter() {
        return usernameParameter;
    }

    public final String getPasswordParameter() {
        return passwordParameter;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserAccountDAO getUserAccountDao() {
        return userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
    }

    public CRFLocker getCrfLocker() {
        return crfLocker;
    }

    public void setCrfLocker(CRFLocker crfLocker) {
        this.crfLocker = crfLocker;
    }

}