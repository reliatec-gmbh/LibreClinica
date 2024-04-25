/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao;

import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import org.junit.Test;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Properties;

public class CoreResourcesTest {

    public CoreResourcesTest() {
        super();
    }

    @Test
    public void testReplaceEnvVariables() {
        Properties properties = new Properties();
        CoreResources coreResources = null;
        try {
            coreResources = new CoreResources(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Environment env = coreResources.getEnvironment();
        String osvar = env.getProperty("OS");
        String expanded = coreResources.replaceEnvVariables("I am in ${OS}, so the system is ${OS}.");
        String target = "I am in " + osvar + ", so the system is " + osvar + ".";
        assert expanded.equals(target);
        String unfindable_str = "I am in ${sOS}, so the system is ${sOS}.";
        assert coreResources.replaceEnvVariables(unfindable_str).equals(unfindable_str);
    }

}