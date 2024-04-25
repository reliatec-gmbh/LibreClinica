package org.akaza.openclinica.control.submit;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ErrorResponse {
    @JsonProperty("error")
    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
