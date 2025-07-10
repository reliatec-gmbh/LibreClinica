/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.job;

/**
 * Thrown to indicate that a job was interrupted.
 * @author Leonel Gayard, leonel.gayard@openclinica.com
 */
@SuppressWarnings("serial")
public class JobInterruptedException extends RuntimeException {
	public JobInterruptedException() {}

	public JobInterruptedException(String message) {
		super(message);
	}

	public JobInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}
