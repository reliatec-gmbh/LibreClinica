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
 * @author sshamim
 *
 */
public class CRFReadingException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8163759260095010460L;
	public String message;

    public CRFReadingException() {
        message = "";
    }

    public CRFReadingException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
