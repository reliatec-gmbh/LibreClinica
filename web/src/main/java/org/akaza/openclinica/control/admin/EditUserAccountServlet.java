/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.otp.TowFactorBean;
import org.akaza.openclinica.service.otp.TwoFactorService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * Servlet for creating a user account.
 *
 * @author ssachs
 */
public class EditUserAccountServlet extends SecureController {

	private static final long serialVersionUID = -6961254006943513921L;
    
	public static final String INPUT_FIRST_NAME = "firstName";
    public static final String INPUT_LAST_NAME = "lastName";
    public static final String INPUT_EMAIL = "email";
    public static final String INPUT_INSTITUTION = "institutionalAffiliation";
    public static final String INPUT_RESET_PASSWORD = "resetPassword";
    public static final String INPUT_USER_TYPE = "userType";
    public static final String INPUT_CONFIRM_BUTTON = "submit";
    public static final String INPUT_DISPLAY_PWD = "displayPwd";
    public static final String PATH = "EditUserAccount";
    public static final String ARG_USERID = "userId";
    public static final String ARG_STEPNUM = "stepNum";
    public static final String INPUT_RUN_WEBSERVICES = "runWebServices";
    public static final String FLAG_LDAP_USER = "ldapUser";
    public static final String INPUT_AUTHTYPE = "authtype";
    // possible values of ARG_STEPNUM
    public static final int EDIT_STEP = 1;
    public static final int CONFIRM_STEP = 2;
    // possible values of INPUT_CONFIRM_BUTTON
    public static final String BUTTON_CONFIRM_VALUE = "Confirm";
    public static final String BUTTON_BACK_VALUE = "Back";
    public static final String USER_ACCOUNT_NOTIFICATION = "notifyPassword";

    private ArrayList<StudyBean> getAllStudies() {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        return sdao.findAll();
    }

    public static String getLink(int userId) {
        return PATH + '?' + ARG_USERID + '=' + userId;
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        // because we need to use this in the confirmation and error parts too
        ArrayList<StudyBean> studies = getAllStudies();
        request.setAttribute("studies", studies);

        int userId = fp.getInt(ARG_USERID);
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean user = udao.findByPK(userId);
        boolean was2FaDeactivated = user.isTwoFactorDeactivated();

        int stepNum = fp.getInt(ARG_STEPNUM);

        if (!fp.isSubmitted()) { // Request for edit user.
            addEntityList("userTypes", getUserTypes(), respage.getString("the_user_could_not_be_edited_because_no_user_types"), Page.ADMIN_SYSTEM);
            loadPresetValuesFromBean(fp, user);
            fp.addPresetValue(ARG_STEPNUM, EDIT_STEP);
            setPresetValues(fp.getPresetValues());

            // addEntityList("userTypes", getUserTypes(),
            // "The user could not be edited because there are no user types
            // available.",
            // Page.ADMIN_SYSTEM);
            request.setAttribute("userName", user.getName());
            forwardPage(Page.EDIT_ACCOUNT);
        } else if (stepNum == EDIT_STEP) { // Request before confirm changes.
            Validator v = new Validator(request);

            v.addValidation(INPUT_FIRST_NAME, Validator.NO_BLANKS);
            v.addValidation(INPUT_LAST_NAME, Validator.NO_BLANKS);

            v.addValidation(INPUT_FIRST_NAME, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);
            v.addValidation(INPUT_LAST_NAME, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);

            v.addValidation(INPUT_EMAIL, Validator.NO_BLANKS);
            v.addValidation(INPUT_EMAIL, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 120);
            v.addValidation(INPUT_EMAIL, Validator.IS_A_EMAIL);

            v.addValidation(INPUT_INSTITUTION, Validator.NO_BLANKS);
            v.addValidation(INPUT_INSTITUTION, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

            HashMap<String, ArrayList<String>> errors = v.validate();

            if (errors.isEmpty()) {
                loadPresetValuesFromForm(fp);
                fp.addPresetValue(ARG_STEPNUM, CONFIRM_STEP);

                setPresetValues(fp.getPresetValues());
                request.setAttribute("userName", user.getName());
                forwardPage(Page.EDIT_ACCOUNT_CONFIRM);

            } else {
                loadPresetValuesFromForm(fp);
                fp.addPresetValue(ARG_STEPNUM, EDIT_STEP);
                setInputMessages(errors);

                setPresetValues(fp.getPresetValues());
                addEntityList("userTypes", getUserTypes(), respage.getString("the_user_could_not_be_edited_because_no_user_types"), Page.ADMIN_SYSTEM);

                addPageMessage(respage.getString("there_were_some_errors_submission") + respage.getString("see_below_for_details"));
                forwardPage(Page.EDIT_ACCOUNT);
            }
        } else if (stepNum == CONFIRM_STEP) {
            String button = fp.getString(INPUT_CONFIRM_BUTTON);

            if (button.equals(resword.getString("back"))) {
                loadPresetValuesFromForm(fp);
                fp.addPresetValue(ARG_STEPNUM, EDIT_STEP);

                addEntityList("userTypes", getUserTypes(), respage.getString("the_user_could_not_be_edited_because_no_user_types"), Page.ADMIN_SYSTEM);
                setPresetValues(fp.getPresetValues());
                request.setAttribute("userName", user.getName());
                forwardPage(Page.EDIT_ACCOUNT);
            } else if (button.equals(resword.getString("confirm"))) {
                user.setFirstName(fp.getString(INPUT_FIRST_NAME));
                user.setLastName(fp.getString(INPUT_LAST_NAME));
                user.setEmail(fp.getString(INPUT_EMAIL));
                user.setInstitutionalAffiliation(fp.getString(INPUT_INSTITUTION));
                user.setUpdater(ub);
                boolean isSoap = fp.getBoolean(INPUT_RUN_WEBSERVICES);
                user.setRunWebservices(isSoap);
                user.setEnableApiKey(true);
                user.setAuthtype(fp.getString("authtype"));

                if (was2FaDeactivated && user.isTwoFactorMarked()) {
                    TwoFactorService factorService = (TwoFactorService) getWebApplicationContext(getServletContext()).getBean("factorService");
                    if (factorService.isTwoFactorLetter()) {
                        TowFactorBean bean = factorService.generate();
                        user.setAuthsecret(bean.getAuthSecret());
                    }
                }

                String apiKey;
                do {
                    apiKey=getRandom32ChApiKey() ;
                } while(isApiKeyExist(apiKey));
                user.setApiKey(apiKey);
                
                UserType ut = UserType.get(fp.getInt(INPUT_USER_TYPE));
                if (ut.equals(UserType.SYSADMIN)) {
                    user.addUserType(ut);
                } else if (ut.equals(UserType.TECHADMIN)) {
                    user.addUserType(ut);
                } else {
                    user.addUserType(UserType.USER);
                }

                if (fp.getBoolean(INPUT_RESET_PASSWORD)) {
                    SecurityManager sm = ((SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager"));
                    String password = sm.genPassword();
                    String passwordHash = sm.encryptPassword(password, isSoap);
                    user.setPasswd(passwordHash);
                    user.setPasswdTimestamp(null);

                    udao.update(user);
                    if ("no".equalsIgnoreCase(fp.getString(INPUT_DISPLAY_PWD))) {
                        logger.info("displayPwd is no");
                        try {
                            sendResetPasswordEmail(user, password);
                        } catch (Exception e) {
                            addPageMessage(respage.getString("there_was_an_error_sending_reset_email_try_reset"));
                        }
                    } else {
                        addPageMessage(respage.getString("new_user_password") + ":<br/> " + password + "<br/>"
                            + respage.getString("please_write_down_the_password_and_provide"));
                    }
                } else {
                    udao.update(user);
                }

                addPageMessage(respage.getString("the_user_account") + " \"" + user.getName() + "\" " + respage.getString("was_updated_succesfully"));
                forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
            } else {
                throw new InconsistentStateException(Page.ADMIN_SYSTEM, resexception.getString("an_invalid_submit_button_was_clicked"));
            }
        } else {
            throw new InconsistentStateException(Page.ADMIN_SYSTEM, resexception.getString("an_invalid_step_was_specified"));
        }
    }

    private void loadPresetValuesFromBean(FormProcessor fp, UserAccountBean user) {
        fp.addPresetValue(INPUT_FIRST_NAME, user.getFirstName());
        fp.addPresetValue(INPUT_LAST_NAME, user.getLastName());
        fp.addPresetValue(INPUT_EMAIL, user.getEmail());
        fp.addPresetValue(INPUT_INSTITUTION, user.getInstitutionalAffiliation());
        int userTypeId = UserType.USER.getId();
        if (user.isTechAdmin()) {
            userTypeId = UserType.TECHADMIN.getId();
        } else if (user.isSysAdmin()) {
            userTypeId = UserType.SYSADMIN.getId();
        }
        // int userTypeId = user.isSysAdmin() ? UserType.SYSADMIN.getId() :
        // UserType.USER.getId();
        fp.addPresetValue(INPUT_USER_TYPE, userTypeId);
        fp.addPresetValue(ARG_USERID, user.getId());
        fp.addPresetValue(INPUT_RUN_WEBSERVICES, user.getRunWebservices() ? 1 : 0);

        String sendPwd = SQLInitServlet.getField("user_account_notification");
        fp.addPresetValue(USER_ACCOUNT_NOTIFICATION, sendPwd);

        fp.addPresetValue(FLAG_LDAP_USER, user.isLdapUser());
        fp.addPresetValue(INPUT_AUTHTYPE, user.getAuthtype());
    }

    private void loadPresetValuesFromForm(FormProcessor fp) {
        fp.clearPresetValues();

        String textFields[] = {ARG_USERID, INPUT_FIRST_NAME, INPUT_LAST_NAME, INPUT_EMAIL, INPUT_INSTITUTION, INPUT_DISPLAY_PWD, INPUT_AUTHTYPE};
        fp.setCurrentStringValuesAsPreset(textFields);

        String ddlbFields[] = { INPUT_USER_TYPE, INPUT_RESET_PASSWORD, INPUT_RUN_WEBSERVICES };
        fp.setCurrentIntValuesAsPreset(ddlbFields);

        // String chkFields[] = { };
        // fp.setCurrentBoolValuesAsPreset(chkFields);
    }

    private ArrayList<UserType> getUserTypes() {

        ArrayList<UserType> types = UserType.toArrayList();
        types.remove(UserType.INVALID);
        if (!ub.isTechAdmin()) {
            types.remove(UserType.TECHADMIN);
        }
        return types;
    }

    private void sendResetPasswordEmail(UserAccountBean user, String password) throws Exception {
        logger.info("Sending password reset notification to " + user.getName());

        String body = resword.getString("dear") + " " +
            user.getFirstName() + " " + user.getLastName() + ",<br/>\n" +
            restext.getString("your_password_has_been_reset_on_openclinica") + ":<br/><br/>\n\n" +
            resword.getString("user_name") + ": " + user.getName() + "<br/>\n" +
            resword.getString("password") + ": " + password + "<br/><br/>\n\n" +
            restext.getString("please_test_your_login_information_and_let") + "<br/>\n" + "<a href='" +
            SQLInitServlet.getField("sysURL") + "'>" +
            SQLInitServlet.getField("sysURL") + "</a><br/>\n" +
            restext.getString("openclinica_system_administrator");

        sendEmail(user.getEmail().trim(), restext.getString("your_openclinica_account_password_reset"), body, false);
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

	public Boolean isApiKeyExist(String uuid) {
		UserAccountDAO userDao = new UserAccountDAO(sm.getDataSource());
		UserAccountBean user = userDao.findByApiKey(uuid);
        return user != null && user.isActive();
	}

	public String getRandom32ChApiKey() {
		String uuid = UUID.randomUUID().toString();
	    //logger.debug(uuid.replaceAll("-", ""));
		return uuid.replaceAll("-", "");
	}
}
