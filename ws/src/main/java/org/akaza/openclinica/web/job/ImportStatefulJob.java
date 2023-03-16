/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Import Stateful Job, by Tom Hickerson 04/2009
 * Establishing stateful-ness on the Java side to avoid locking
 */
// TODO duplicate of the version in the web module?
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ImportStatefulJob
    extends ImportSpringJob {

    public ImportStatefulJob() {
        super();
    }
}
