/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.oid;

import java.io.Serializable;

/**
 * @author Krikor Krumlian
 *
 */
public class GenericOidGenerator extends OidGenerator implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2545386297047178606L;
	private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid;
        String key = keys[0];

        oid = truncateTo4Chars(capitalize(stripNonAlphaNumeric(key)));

        // If oid is made up of all special characters then
        if (oid.equals("_")) {
            oid = randomizeOid("");
        }
        logger.debug("OID : " + oid);
        return oid;
    }
}
