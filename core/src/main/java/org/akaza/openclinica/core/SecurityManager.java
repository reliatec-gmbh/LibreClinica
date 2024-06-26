/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.core;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityManager
 * 
 * @author Krikor Krumlian
 */
public class SecurityManager {

    private PasswordEncoder encoder;

    private AuthenticationProvider providers[];

    /**
     * Generates a random password with default length
     */
    public String genPassword() {
        return genPassword(8);
    }

    /**
     * Generates a random password by length
     *
     * @param howMany how many characters
     */
    public String genPassword(int howMany) {
        StringBuilder password = new StringBuilder();
        String core = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();

        for (int i = 0; i < howMany; i++) {
            int index = rand.nextInt(core.length());
            char oneCharacter = core.charAt(index);
            password.append(oneCharacter);
        }

        return password.toString();
    }

    public String encryptPassword(String password, boolean isSoapUser) throws NoSuchAlgorithmException {
        String result = null;

        // Use spring security encoder for non SOAP user
        if (!isSoapUser) {
            result = encoder.encode(password);
        } else { // otherwise, use plain SHA-1 password encoder compatible with SOAP web services
            if (encoder instanceof OpenClinicaPasswordEncoder) {
                result = ((OpenClinicaPasswordEncoder) encoder).soapEncode(password);
            }
        }
        
        return result;
    }

    public boolean verifyPassword(String clearTextPassword, UserDetails userDetails) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails.getUsername(),
            clearTextPassword
        );

        for (AuthenticationProvider p : providers) {
            try {
                p.authenticate(authentication);
                return true;
            } catch (AuthenticationException e) {
                // Nothing to do
            }
        }

        return false;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public AuthenticationProvider[] getProviders() {
        return providers;
    }

    public void setProviders(AuthenticationProvider[] providers) {
        this.providers = providers;
    }

}