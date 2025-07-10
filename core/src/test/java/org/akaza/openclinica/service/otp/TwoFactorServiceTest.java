/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.otp;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.akaza.openclinica.domain.admin.TwoFactorType.LETTER;
import static org.akaza.openclinica.service.otp.TwoFactorService.TWO_FACTOR_ACTIVATED_SETTING;
import static org.akaza.openclinica.service.otp.TwoFactorService.TWO_FACTOR_ACTIVATION_DUE_DATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Properties;

import org.akaza.openclinica.dao.core.CoreResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test verifying some {@link TwoFactorService} class functionality.
 * 
 * @author thillger
 */
@RunWith(MockitoJUnitRunner.class)
public class TwoFactorServiceTest {
    @Mock
    private CoreResources coreResources;
    private TwoFactorService service;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        service = new TwoFactorService() {

            @Override
            String extractedVerificationTypeSetting() {
                return LETTER.name();
            }
        };
        service.coreResources = coreResources;

        properties = new Properties();
        when(coreResources.getDATAINFO()).thenReturn(properties);
    }

    @Test
    public void testIsTwoFactorOutdated_CurrentDate() {
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, LocalDate.now().format(ISO_DATE));

        assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_FutureDate() {
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, LocalDate.now().plus(1, DAYS).format(ISO_DATE));

        assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_Yesterday() {
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, LocalDate.now().minus(1, DAYS).format(ISO_DATE));

        assertThat(service.isTwoFactorOutdated(), is(true));
    }

    @Test
    public void testGetTowFactorActivated_EmptyStringReturnsFalse() {
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "");

        assertThat(service.getTwoFactorActivated(), is(false));
    }

    @Test
    public void testGetTowFactorActivated_InvalidValueReturnsFalse() {
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "abc");

        assertThat(service.getTwoFactorActivated(), is(false));
    }

    @Test
    public void testGetTowFactorActivated_TrueReturnsTrue() {
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "true");
        assertThat(service.getTwoFactorActivated(), is(TRUE));

        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "TrUe");
        assertThat(service.getTwoFactorActivated(), is(TRUE));
    }

    @Test
    public void testGetTowFactorActivated_FalseReturnsFalse() {
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "false");
        assertThat(service.getTwoFactorActivated(), is(FALSE));

        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "FalSe");
        assertThat(service.getTwoFactorActivated(), is(FALSE));
    }

    @Test
    public void testIsTwoFactorActivatedLetterAndOutDated_NoDueDateProvided() {
        properties.put(TwoFactorService.TWO_FACTOR_ACTIVATED_VERIFICATION_TYPE, "letter");
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "true");
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, "");

        assertThat(service.isTwoFactorActivatedLetterAndOutDated(), is(FALSE));
    }

    @Test
    public void testIsTwoFactorActivatedLetterAndOutDated_OutdatedDueDateProvided() {
        properties.put(TwoFactorService.TWO_FACTOR_ACTIVATED_VERIFICATION_TYPE, "LETTER");
        properties.put(TWO_FACTOR_ACTIVATED_SETTING, "true");
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, "2020-01-01");

        assertThat(service.isTwoFactorActivatedLetterAndOutDated(), is(TRUE));
    }

    @Test
    public void testIsTwoFactorOutdated_EmptySettingString() {
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, "");

        assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_NullSettingString() {
        assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_InValidSettingWrongFormat() {
        properties.put(TWO_FACTOR_ACTIVATION_DUE_DATE, "01.01.2000");

        assertThat(service.isTwoFactorOutdated(), is(false));
    }
    
    @Test
    public void testExtractSystemInfo_UsingHttp() {
        String systemSetting = "http://some-system.elsewhere.com:8080/HolyStudy/MainMenu";

        assertThat(service.extractSystemInfo(systemSetting), is("some-system.elsewhere.com:8080/HolyStudy"));
    }

    @Test
    public void testExtractSystemInfo_UsingHttps() {
        String systemSetting = "https://some-system.elsewhere.com:8080/HolyStudy/MainMenu";
        
        assertThat(service.extractSystemInfo(systemSetting), is("some-system.elsewhere.com:8080/HolyStudy"));
    }
}
