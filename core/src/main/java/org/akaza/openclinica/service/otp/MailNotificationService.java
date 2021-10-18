package org.akaza.openclinica.service.otp;

import static java.util.Locale.ENGLISH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.TimeZone;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.managestudy.MailNotificationType;
import org.akaza.openclinica.exception.MailNotificationException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Service class providing access to E-Mail notification related use cases.
 * 
 * @author jbley
 */
@Component("mailNotificationService")
public class MailNotificationService {
    @Autowired
    private OpenClinicaMailSender mailSender;
	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;
    private StudyDAO studyDao;

	public void setMailSender(OpenClinicaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Checks whether mail notification is enabled or disabled for study. Returns true if enabled - false otherwise.
	 * 
	 * @param studyId The study unique identifier.
	 */
	public boolean isMailNotificationEnabled(int studyId) {
		StudyBean studyBean = getStudyDao().findByPK(studyId);
        return MailNotificationType.ENABLED.name().equals(studyBean.getMailNotification());
	}

	/**
     * Sends mail notification for successful login attempts.
     * 
     * @param bean Bean with addressee information.
     * @param loginResult The login result context.
     */
    public void sendSuccessfulLoginMail(UserAccountBean bean) {
        sendMail(bean, LoginResult.SUCCESSFUL_LOGIN);
	}
	
	/**
	 * Sends mail notification for denied login attempts.
	 * 
	 * @param bean Bean with addressee information.
	 * @param loginResult The login result context.
	 */
    public void sendDeniedLoginMail(UserAccountBean bean) {
        sendMail(bean, LoginResult.DENIED_LOGIN);
	}

    private void sendMail(UserAccountBean bean, LoginResult loginResult) {
        StudyBean studyBean = getStudyDao().findByPK(bean.getActiveStudyId());

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("successfulLoginMail.txt")) {
	        // @formatter:off
	        String message = String.format(IOUtils.toString(inputStream, "UTF-8"), 
	                bean.getFirstName(),
	                bean.getLastName(), 
	                getStudyName(bean.getActiveStudyId()), 
	                new Date(),
	                TimeZone.getDefault().getDisplayName(ENGLISH), 
	                loginResult.textual(),
	                studyBean.getContactEmail());
	        // @formatter:off
	        
	        mailSender.sendEmail(bean.getEmail(), "Login Notification", message, false);
	    } catch (IOException e) {
	        new MailNotificationException(e.getMessage(), e.getCause());
	    }
    }

    private String getStudyName(int studyId) {
        StudyBean studyBean = getStudyDao().findByPK(studyId);
        return studyBean.getName();
    }

	private StudyDAO getStudyDao() {
		studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
		return studyDao;
	}
}
