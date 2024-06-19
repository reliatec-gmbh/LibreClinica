/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Import Stateful Job, by Tom Hickerson 04/2009
 * Establishing stateful-ness on the Java side to avoid locking
 */

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ImportStatefulJob
    extends ImportSpringJob {

    public ImportStatefulJob() {
        super();
    }
}
