package org.akaza.openclinica.service.otp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.managestudy.MailNotificationType;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.hibernate.validator.internal.util.privilegedactions.GetResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.io.Resources;

/**
 * Service class providing access to E-Mail Notification related use cases.
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

	/**
	 * Returns true if mail notifications for login are activated system wide, false
	 * otherwise.
	 */

	public void setMailSender(OpenClinicaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public boolean mailNotificationActivated() {

		return Boolean.valueOf(coreResources.getDATAINFO().getProperty(MAIL_NOTIFICATION_ACTIVATED_SETTING, "false"));
	}

	/**
	 * Checks whether mail notification is enabled or disabled for study
	 * 
	 * @param studyId
	 * @return "ENABLED" or "DISABLED"
	 */

	public boolean isMailNotificationEnabled(int studyId) {
		StudyBean sb = getStudyDao().findByPK(studyId);
		boolean result = MailNotificationType.ENABLED.name().equals(sb.getMailNotification());
		return result;
	}

	/**
	 * 
	 * @param studyId
	 * @return returns the identifier of specific study
	 */

	private String getStudyName(int studyId) {
		StudyBean sb = getStudyDao().findByPK(studyId);
		return sb.getName();
	}

	/**
	 * Sends mail notification for successful login
	 * 
	 * @param to
	 */

	public void sendLoginMail(UserAccountBean bean) {
		try {
			Date date = new Date();
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("successfulLoginMail.txt");
			String message = String.format(IOUtils.toString(inputStream, "UTF-8"), bean.getFirstName(), bean.getLastName(),getStudyName(bean.getActiveStudyId()), date, TimeZone.getDefault().getDisplayName(Locale.ENGLISH) );

			mailSender.sendEmail(bean.getEmail(), "Login Notification", message, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the studyDao
	 */
	private StudyDAO getStudyDao() {
		studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
		return studyDao;
	}

}
