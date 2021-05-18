package org.akaza.openclinica.service.otp;

/**
 * A simple Java bean holding information about 2-fa related codes and image.
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

    @Override
    public String toString() {
        return "TowFactorBean [authSecret=" + authSecret + ", imageUrl=" + imageUrl + "]";
    }
}
