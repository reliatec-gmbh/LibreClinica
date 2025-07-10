/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.otp;

import static org.akaza.openclinica.domain.managestudy.MailNotificationType.DISABLED;
import static org.akaza.openclinica.domain.managestudy.MailNotificationType.ENABLED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test verifying some {@link MailNotificationService} class
 * functionality.
 * 
 * @author thillger
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceTest {
    private MailNotificationService service;
    @Mock
    private StudyDAO studyDao;
    private StudyBean study;

    @Before
    public void setUp() throws Exception {
        service = new MailNotificationService();
        service.studyDao = studyDao;

        study = new StudyBean();
    }

    @Test
    public void testIsMailNotificationEnabled_IsEnabled() {
        study.setMailNotification(ENABLED.name());

        when(studyDao.findByPK(1)).thenReturn(study);
        
        assertThat(service.isMailNotificationEnabled(1), is(true));
    }

    @Test
    public void testIsMailNotificationEnabled_IsDisabled() {
        study.setMailNotification(DISABLED.name());

        when(studyDao.findByPK(1)).thenReturn(study);

        assertThat(service.isMailNotificationEnabled(1), is(false));
    }

    @Test
    public void testIsMailNotificationEnabled_IsDisabled_WhenNullValue() {
        study.setMailNotification(null);

        when(studyDao.findByPK(1)).thenReturn(study);

        assertThat(service.isMailNotificationEnabled(1), is(false));
    }
}
