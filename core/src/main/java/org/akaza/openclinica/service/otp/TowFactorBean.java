/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.otp;

/**
 * A simple Java bean holding information about 2-fa related secret and image.
 * 
 * @author thillger
 */
public final class TowFactorBean {
    private String authSecret;
    private String imageUrl;

    public String getAuthSecret() {
        return authSecret;
    }

    public void setAuthSecret(String authSecret) {
        this.authSecret = authSecret;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String asImageUrl() {
        return "data:image/png;base64," + imageUrl;
    }

    @Override
    public String toString() {
        return "TowFactorBean [authSecret=" + authSecret + ", imageUrl=" + imageUrl + "]";
    }
}
