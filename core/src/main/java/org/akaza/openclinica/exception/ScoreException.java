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
 * Subclass of OpenClinicaException
 *
 * @author ywang (Mar. 2008)
 *
 */
public class ScoreException extends OpenClinicaException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4587135032315735800L;

	public ScoreException(String message, String errorId) {
        super(message, errorId);
    }
}