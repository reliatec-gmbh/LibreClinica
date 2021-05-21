package org.akaza.openclinica.service.otp;

import static dev.samstevens.totp.code.HashingAlgorithm.SHA1;

import java.util.Base64;

import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.stereotype.Component;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

/**
 * Service class provding access to 2-FA related use cases.
 * 
 * @author thillger
 */
@Component("factorService")
public class TwoFactorService {
    private static final String TWO_FACTOR_ACTIVATED_SETTING = "2fa.activated";
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator(64);
    private CoreResources coreResources;

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    /**
     * Returns true if 2-FA is activated system wide - false otherwise (also
     * default).
     */
    public boolean getTwoFactorActivated() {
        return Boolean.valueOf(coreResources.getDATAINFO().getProperty(TWO_FACTOR_ACTIVATED_SETTING, "false"));
    }

    /**
     * Verifies a 2-FA internal secret against a provided one-time password.
     * Return true if valid - false otherwise.
     * 
     * @param secret The private key (inside the system).
     * @param oneTimePassword The user's one-time password.
     */
    public boolean verify(String secret, String oneTimePassword) {
        return verifier.isValidCode(secret, oneTimePassword);
    }

    /**
     * Generates a {@link TowFactorBean} holding specifc 2-FA information needed
     * for client user configuration (secret, QR-code as image url).
     * 
     * @throws Exception In cases of errors.
     */
    public TowFactorBean generate() throws Exception {
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

            byte[] imageData = new ZxingPngQrGenerator().generate(data);

            TowFactorBean factorBean = new TowFactorBean();
            factorBean.setAuthSecret(secret);
            factorBean.setImageUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(imageData));
            return factorBean;
        } catch (QrGenerationException e) {
            throw new Exception(e);
        }
    }
}
