/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.oid;

/**
 * Assumes we are getting the unique protocol id from a study, and truncating to
 * eight chars.
 *
 * @author thickerson
 *
 */
public class StudyOidGenerator extends OidGenerator {

    public int getArgumentLength() {
		return argumentLength;
	}

	private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
    	String oid = "S_";
        String uniqueProtocolID = keys[0];
        uniqueProtocolID = truncateTo8Chars(capitalize(stripNonAlphaNumeric(uniqueProtocolID)));

        if (uniqueProtocolID.length() == 0) {
            uniqueProtocolID = randomizeOid("");
        }
        oid = oid + uniqueProtocolID;
        return oid;
    }

}
