/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.enumsupport;

@SuppressWarnings("serial")
public class CodedEnumPersistenceException extends RuntimeException {

    public CodedEnumPersistenceException(String message, Object... messageFormats) {
        super(String.format(message, messageFormats));
    }

    public CodedEnumPersistenceException(String message, Throwable cause, Object... messsageFormats) {
        super(String.format(message, messsageFormats), cause);
    }

    public CodedEnumPersistenceException(Throwable cause) {
        super(cause);
    }

}
