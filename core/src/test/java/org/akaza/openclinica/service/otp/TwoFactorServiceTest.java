package org.akaza.openclinica.service.otp;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.akaza.openclinica.domain.admin.TwoFactorType.LETTER;
import static org.hamcrest.CoreMatchers.is;

import java.time.LocalDate;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test verifying some {@link TwoFactorService} class functionality.
 * 
 * @author thillger
 */
public class TwoFactorServiceTest {
    private TwoFactorService service;
    private String settingDueDateString;

    @Before
    public void setUp() throws Exception {
        service = new TwoFactorService() {

            @Override
            String extractedVerificationTypeSetting() {
                return LETTER.name();
            }
            
            @Override
            String extractedDueDateSetting() {
                return settingDueDateString;
            }
        };
    }

    @Test
    public void testIsTwoFactorOutdated_CurrentDate() {
        settingDueDateString = LocalDate.now().format(ISO_DATE);

        MatcherAssert.assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_FutureDate() {
        settingDueDateString = LocalDate.now().plus(1, DAYS).format(ISO_DATE);

        MatcherAssert.assertThat(service.isTwoFactorOutdated(), is(false));
    }

    @Test
    public void testIsTwoFactorOutdated_Yesterday() {
        settingDueDateString = LocalDate.now().minus(1, DAYS).format(ISO_DATE);

        MatcherAssert.assertThat(service.isTwoFactorOutdated(), is(true));
    }

    @Test(expected = RuntimeException.class)
    public void testIsTwoFactorOutdated_EmptySettingString() {
        settingDueDateString = "";

        service.isTwoFactorOutdated();
    }

    @Test(expected = RuntimeException.class)
    public void testIsTwoFactorOutdated_NullSettingString() {
        settingDueDateString = null;

        service.isTwoFactorOutdated();
    }
}
