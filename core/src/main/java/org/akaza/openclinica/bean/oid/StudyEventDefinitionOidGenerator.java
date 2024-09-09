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
 * @author Krikor Krumlian
 *
 */
public class StudyEventDefinitionOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = "SE_";
        String key = keys[0];
        oid = oid + truncateToXChars(capitalize(stripNonAlphaNumeric(key)), 28);
        if (oid.length() == 3) {
            oid = randomizeOid(oid);

        }
        return oid;
    }

}
