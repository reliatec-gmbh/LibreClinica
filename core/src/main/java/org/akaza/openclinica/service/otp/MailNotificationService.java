package org.akaza.openclinica.service.otp;


import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
     * Returns true if mail notifications for login are activated system wide,
     * false otherwise.
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

    public String getMailNotificationEnabled(int studyId) {
        StudyBean sb = getStudyDao().findByPK(studyId);       
        return sb.getMailNotification();
    }

    /**
     * Sends mail notification for successful login
     * 
     * @param to
     */

    public void sendLoginMail(String to) {
        
        
        String message = "Login Notfication";
        
        mailSender.sendEmail(to, "Login Notification", message, null);

    }

    /**
     * @return the studyDao
     */
    private StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }
    
    
    

}
