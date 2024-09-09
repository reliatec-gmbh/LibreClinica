/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.password.PasswordEncoder;

public class OpenClinicaPasswordEncoder implements PasswordEncoder {

    PasswordEncoder currentPasswordEncoder;
    PasswordEncoder oldPasswordEncoder;

    public OpenClinicaPasswordEncoder() {
        // NOOP
    }

    public PasswordEncoder getCurrentPasswordEncoder() {
        return currentPasswordEncoder;
    }

    public void setCurrentPasswordEncoder(PasswordEncoder currentPasswordEncoder) {
        this.currentPasswordEncoder = currentPasswordEncoder;
    }

    public PasswordEncoder getOldPasswordEncoder() {
        return oldPasswordEncoder;
    }

    public void setOldPasswordEncoder(PasswordEncoder oldPasswordEncoder) {
        this.oldPasswordEncoder = oldPasswordEncoder;
    }

    public String soapEncode(CharSequence rawPassword) throws NoSuchAlgorithmException {
        String hexResult;
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(Utf8.encode(rawPassword));
        hexResult = new String(Hex.encode(digest.digest()));

        return hexResult;
    }

	@Override
	public String encode(CharSequence rawPassword) {
		return currentPasswordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean result = currentPasswordEncoder.matches(rawPassword, encodedPassword);
        result = result || oldPasswordEncoder.matches(rawPassword, encodedPassword);
		return result;
	}

}
