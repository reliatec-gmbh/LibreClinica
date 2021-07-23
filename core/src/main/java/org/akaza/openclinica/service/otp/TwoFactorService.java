package org.akaza.openclinica.service.otp;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Splitter.fixedLength;
import static dev.samstevens.totp.code.HashingAlgorithm.SHA1;
import static java.lang.Enum.valueOf;
import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;
import static org.akaza.openclinica.domain.admin.TwoFactorType.APPLICATION;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.pdfbox.pdmodel.font.PDType1Font.COURIER;
import static rst.pdfbox.layout.elements.PositionControl.createMovePosition;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Base64;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.admin.TwoFactorType;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.ImageElement;
import rst.pdfbox.layout.elements.Paragraph;

/**
 * Service class provding access to 2-FA related use cases and settings.
 * 
 * @author thillger
 */
@Component("factorService")
public class TwoFactorService {
    private static final String TWO_FACTOR_ACTIVATED_VERIFICATION_TYPE = "2fa.type";
    private static final String TWO_FACTOR_ACTIVATION_DUE_DATE = "2fa.dueDate";
    private static final String TWO_FACTOR_ACTIVATED_SETTING = "2fa.activated";
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    private CoreResources coreResources;

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    /**
     * Returns true if 2-FA is activated system wide in general - false
     * otherwise (also default).
     */
    public boolean getTwoFactorActivated() {
        return Boolean.valueOf(coreResources.getDATAINFO().getProperty(TWO_FACTOR_ACTIVATED_SETTING, "false"));
    }

    /**
     * Returns true if 2-FA is activated system and the desired workflow is
     * acceptance via the application/browser.
     */
    public boolean getTwoFactorActivatedApplication() {
        return getTwoFactorActivated() && isTwoFactorApplication();
    }

    /**
     * Returns true if 2-FA is activated system and the desired workflow is
     * acceptance via letter.
     */
    public boolean getTwoFactorActivatedLetter() {
        return getTwoFactorActivated() && isTwoFactorLetter();
    }

    /**
     * Like {@link #getTwoFactorActivatedLetter()} in conjunction with
     * {@link #isTwoFactorOutdated()}.
     */
    public boolean isTwoFactorActivatedLetterAndOutDated() {
        return getTwoFactorActivatedLetter() && isTwoFactorOutdated();
    }

    /**
     * Returns true if 2-FA security code presentation should be handled via the
     * application (browser).
     */
    public boolean isTwoFactorApplication() {
        // @formatter:off
		String settingValue = extractedVerificationTypeSetting();
		return APPLICATION.equals(valueOf(TwoFactorType.class, settingValue));
		// @formatter:on
    }

    /**
     * The negation of {@link #isTwoFactorApplication()}.
     */
    public boolean isTwoFactorLetter() {
        return !isTwoFactorApplication();
    }

    /**
     * Returns true if the due date is outdated to the current date for 2-FA -
     * false otherwise.
     */
    public boolean isTwoFactorOutdated() {
        if (!isTwoFactorLetter()) {
            return false;
        }

        // @formatter:off
        String settingValue = extractedDueDateSetting();
        return parse(settingValue).isBefore(now());
        // @formatter:on
    }

    /**
     * Verifies a 2-FA internal secret against a provided one-time password.
     * Return true if valid - false otherwise.
     * 
     * @param secret The private key (inside the system).
     * @param oneTimePassword The user's one-time password.
     */
    public boolean verify(String secret, String oneTimePassword) {
        return verifier.isValidCode(secret, defaultIfEmpty(oneTimePassword, ""));
    }

    /**
     * Generates a {@link TowFactorBean} holding specifc 2-FA information needed
     * for client user configuration (secret, QR-code as image url).
     * 
     * @throws Exception In cases of errors.
     */
    public TowFactorBean generate() throws Exception {
        DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator(64);
        return generate(secretGenerator.generate());
    }

    /**
     * Like {@link #generate()} but generates {@link TowFactorBean} with image
     * of an already existing or given secret.
     * 
     * @param secret The secret to use for QR-code.
     * @throws Exception In cases of errors.
     */
    public TowFactorBean generate(String secret) throws Exception {
        try {
            byte[] imageData = generateImageData(secret);

            TowFactorBean factorBean = new TowFactorBean();
            factorBean.setAuthSecret(secret);
            factorBean.setImageUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(imageData));
            return factorBean;
        } catch (QrGenerationException e) {
            throw new Exception(e);
        }
    }

    /**
     * Generates a 2-FA certifcate as PDF and streams it to an
     * {@link OutputStream}.
     * 
     * @param bean Bean with certificate information.
     * @param outStream The target stream.
     * @throws Exception In cases of errors.
     */
    public void printoutCertificate(CertificateBean bean, OutputStream outStream) throws Exception {
        Paragraph paragraph1 = new Paragraph();
        paragraph1.setMaxWidth(500);
        paragraph1.addText("LibreClinica 2-Factor Certificate\n\n", 24, COURIER);
        paragraph1.addText("General Information\n\n", 16, COURIER);
        paragraph1.addText(
                "Your user account is registered to use two-factor authentication in the future. A random secret has been generated by the system and associated with your user account. The security key is only available in our system and - after a successful scan - in your authenticator app.",
                14, COURIER);

        String fourBlockSecret = on(" ").join(fixedLength(4).split(bean.getSecret()));

        Paragraph paragraph2 = new Paragraph();
        paragraph2.setMaxWidth(500);
        paragraph2.addText("Account Information\n\n", 16, COURIER);
        paragraph2.addText("Login: " + bean.getLogin() + "\n", 14, COURIER);
        paragraph2.addText("E-Mail: " + bean.getEmail() + "\n", 14, COURIER);
        paragraph2.addText("Secret: " + fourBlockSecret + "\n", 14, COURIER);

        Paragraph paragraph3 = new Paragraph();
        paragraph3.setMaxWidth(500);
        paragraph3.addText("QR-Code\n\n", 16, COURIER);
        paragraph3.addText("Scan the QR code shown on bottom with your authenticator app. Alternatively you can also enter the secret manually into your authenticator app.", 14, COURIER);
        paragraph3.addText("If you have any problems, please contact your administrator.", 14, COURIER);

        byte[] imageData = generateImageData(bean.getSecret());

        // TODO: Finally close inputstream?
        ImageElement image = new ImageElement(new ByteArrayInputStream(imageData));
        image.setHeight(100);
        image.setWidth(100);

        Document document = new Document();
        document.add(createMovePosition(50, 0));
        document.add(paragraph1);
        document.add(createMovePosition(0, -10));
        document.add(paragraph2);
        document.add(createMovePosition(0, -10));
        document.add(paragraph3);
        document.add(createMovePosition(0, -10));
        document.add(image);
        document.save(outStream);
    }

    @VisibleForTesting
    String extractedDueDateSetting() {
        return coreResources.getDATAINFO().getProperty(TWO_FACTOR_ACTIVATION_DUE_DATE);
    }

    @VisibleForTesting
    String extractedVerificationTypeSetting() {
        return coreResources.getDATAINFO().getProperty(TWO_FACTOR_ACTIVATED_VERIFICATION_TYPE, APPLICATION.name());
    }

    private byte[] generateImageData(String secret) throws QrGenerationException {
        // @formatter:off
        QrData data = new QrData.Builder().
                issuer("LibreClinica").
                label("LibreClinica").
                algorithm(SHA1).
                secret(secret).
                digits(6).
                period(30).
                build();
        // @formatter:on

        return new ZxingPngQrGenerator().generate(data);
    }
}
