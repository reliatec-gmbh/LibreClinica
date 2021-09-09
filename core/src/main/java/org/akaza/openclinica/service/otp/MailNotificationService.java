package org.akaza.openclinica.service.otp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
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
	private static final String MAIL_NOTIFICATION_ACTIVATED_SETTING = "mailNotification";
	private CoreResources coreResources;
	private StudyDAO studyDao;
	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;
	@Autowired
	private OpenClinicaMailSender mailSender;

	public void setMailSender(OpenClinicaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Returns true if mail notifications for login are activated system wide, false
	 * otherwise.
	 */
	public boolean mailNotificationActivated() {
		return Boolean.valueOf(coreResources.getDATAINFO().getProperty(MAIL_NOTIFICATION_ACTIVATED_SETTING, "false"));
	}

	/**
	 * Checks whether mail notification is enabled or disabled for study. Returns true if enabled - false otherwise.
	 * 
	 * @param studyId The study unique identifier.
	 */
	public boolean isMailNotificationEnabled(int studyId) {
		StudyBean studyBean = getStudyDao().findByPK(studyId);
		return MailNotificationType.ENABLED.name().equals(studyBean);
	}

	/**
	 * Sends mail notification for successful login.
	 * 
	 * @param bean Bean with addressee information.
	 */
	public void sendLoginMail(UserAccountBean bean) {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("successfulLoginMail.txt")) {
			String message = String.format(IOUtils.toString(inputStream, "UTF-8"), bean.getFirstName(),
					bean.getLastName(), getStudyName(bean.getActiveStudyId()), new Date(),
					TimeZone.getDefault().getDisplayName(Locale.ENGLISH));

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
