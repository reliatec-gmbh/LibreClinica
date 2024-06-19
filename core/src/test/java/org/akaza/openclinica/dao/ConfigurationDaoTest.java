/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

public class ConfigurationDaoTest extends HibernateOcDbTestCase {

    public ConfigurationDaoTest() {
        super();
    }

    public void testSaveOrUpdate() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = new ConfigurationBean();
        configurationBean.setKey("user.test");
        configurationBean.setValue("test");
        configurationBean.setDescription("Testing attention please");

        configurationBean = configurationDao.saveOrUpdate(configurationBean);

        assertNotNull("Persistant id is null", configurationBean.getId());
    }

    public void testfindById() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = configurationDao.findById(-1);

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }

    public void testfindByKey() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = configurationDao.findByKey("test.test");

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }
}