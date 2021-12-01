package org.akaza.openclinica.bean.managestudy;

import static org.akaza.openclinica.domain.managestudy.MailNotificationType.DISABLED;
import static org.akaza.openclinica.domain.managestudy.MailNotificationType.ENABLED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test verifying some {@link StudyBean} class functionality.
 * 
 * @author thillger
 */
@SuppressWarnings("deprecation")
public class StudyBeanTest {
    private StudyBean study;

    @Before
    public void setUp() throws Exception {
        study = new StudyBean();
    }

    @Test
    public void testContactEmailAbsent_ContactEmailNull() {
        study.setContactEmail(null);

        assertThat(study.contactEmailAbsent(), is(true));
    }

    @Test
    public void testContactEmailAbsent_ContactEmailEmptyString() {
        study.setContactEmail("");
        
        assertThat(study.contactEmailAbsent(), is(true));
    }

    @Test
    public void testContactEmailAbsent_ContactEmailPresent() {
        study.setContactEmail("abc@def.de");

        assertThat(study.contactEmailAbsent(), is(false));
    }

    @Test
    public void testContactEmailAbsentButNotification_NotificationDisabled_EmailNull() {
        study.setMailNotification(DISABLED.name());
        study.setContactEmail(null);

        assertThat(study.contactEmailAbsentButNotification(), is(false));
    }

    @Test
    public void testContactEmailAbsentButNotification_NotificationDisabled_EmailEmptyString() {
        study.setMailNotification(DISABLED.name());
        study.setContactEmail("");

        assertThat(study.contactEmailAbsentButNotification(), is(false));
    }

    @Test
    public void testContactEmailAbsentButNotification_NotificationEnabled_EmailNull() {
        study.setMailNotification(ENABLED.name());
        study.setContactEmail(null);

        assertThat(study.contactEmailAbsentButNotification(), is(true));
    }

    @Test
    public void testContactEmailAbsentButNotification_NotificationEnabled_EmailEmptyString() {
        study.setMailNotification(ENABLED.name());
        study.setContactEmail("");

        assertThat(study.contactEmailAbsentButNotification(), is(true));
    }

    @Test
    public void testContactEmailAbsentButNotification_NotificationEnabled_EmailPresent() {
        study.setMailNotification(ENABLED.name());
        study.setContactEmail("abc@def.de");

        assertThat(study.contactEmailAbsentButNotification(), is(false));
    }
}
