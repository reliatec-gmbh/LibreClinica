/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.datamap;

/**
 * Created by yogi on 4/27/17.
 */

/**
 * Enum representing the types of environment to which the protocol can be published
 */

public enum StudyEnvEnum {
    PROD("PROD"), TEST("TEST"), NOT_PUBLISHED("");
    private String env;

    StudyEnvEnum(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return env;
    }
}
