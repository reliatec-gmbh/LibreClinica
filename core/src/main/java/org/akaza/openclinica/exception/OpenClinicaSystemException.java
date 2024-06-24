/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.exception;

/**
 * @author Krikor Krumlian
 */
@SuppressWarnings("serial")
public class OpenClinicaSystemException extends RuntimeException {
    private String errorCode;
    private Object[] errorParams;

    public OpenClinicaSystemException(String code, String message) {
        this(message);
        this.errorCode = code;
    }

    public OpenClinicaSystemException(String code, String message, Throwable cause) {
        this(message, cause);
        this.errorCode = code;
    }

    public OpenClinicaSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenClinicaSystemException(Throwable cause) {
        super(cause);
    }

    public OpenClinicaSystemException(String message) {
        super(message);
        this.errorCode = message;
    }

    public OpenClinicaSystemException(String code, Object[] errorParams) {
        this.errorCode = code;
        this.errorParams = errorParams;
    }

    public OpenClinicaSystemException(String code, Object[] errorParams, String message) {
        this(message);
        this.errorCode = code;
        this.errorParams = errorParams;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getErrorParams() {
        return errorParams;
    }

    public void setErrorParams(Object[] errorParams) {
        this.errorParams = errorParams;
    }
}