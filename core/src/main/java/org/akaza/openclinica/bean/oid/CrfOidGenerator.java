/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.bean.oid;

/**
 * @author Krikor Krumlian
 * 
 */
public class CrfOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = "F_";
        String crfName = keys[0];

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 12);
        oid = oid + crfName;

        if (oid.equals("F_")) {
            oid = randomizeOid("F_");
        }
        return oid;
    }
}
