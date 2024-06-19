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
public class ItemGroupOidGenerator extends OidGenerator {

    private final int argumentLength = 2;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = "IG_";
        String crfName = keys[0];
        String itemGroupLabel = keys[1];

        logger.debug(crfName);
        logger.debug(itemGroupLabel);

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 5);
        itemGroupLabel = truncateToXChars(capitalize(stripNonAlphaNumeric(itemGroupLabel)), 26);

        oid = oid + crfName + "_" + itemGroupLabel;

        // If oid is made up of all special characters then
        if (oid.equals("IG_") || oid.equals("IG__")) {
            oid = randomizeOid("IG_");
        }
        logger.debug("OID : " + oid);
        return oid;
    }
}
