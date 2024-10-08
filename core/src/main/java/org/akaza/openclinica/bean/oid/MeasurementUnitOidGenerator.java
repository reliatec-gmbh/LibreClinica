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
 * Maximum length of measurement_unit_oid is 40.
 */
public class MeasurementUnitOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = this.truncateToXChars("MU_" + capitalize(stripNonAlphaNumeric(keys[0])),35);

        // If oid is made up of all special characters then
        if (oid.equals("MU_")) {
            oid = randomizeOid("MU_");
        }
        logger.info("OID : " + oid);
        return oid;
    }

    @Override
    String stripNonAlphaNumeric(String input) {
        return input.trim().replaceAll("[^a-zA-Z_0-9]", "");
    }

    public String generateOidNoValidation(String... keys) throws Exception {
        verifyArgumentLength(keys);
        String oid = createOid(keys);
        return oid;
    }

}
