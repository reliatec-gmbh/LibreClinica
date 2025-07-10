/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.job;

import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Customized {@link SchedulerFactoryBean} which replaces the default scheduler implementation by
 * {@link OpenClinicaStdSchedulerFactory}.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * @see OpenClinicaStdSchedulerFactory
 *
 */
public class OpenClinicaSchedulerFactoryBean extends SchedulerFactoryBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setSchedulerFactoryClass(OpenClinicaStdSchedulerFactory.class);
        super.afterPropertiesSet();
    }

}
