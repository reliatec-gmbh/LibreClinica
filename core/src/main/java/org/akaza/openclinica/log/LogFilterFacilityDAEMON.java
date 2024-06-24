/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.log;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author pgawade
 * @version 1.0 (22/Nov/2010) Logback log filter to get logs for facility DAEMON
 */
public class LogFilterFacilityDAEMON extends LogFilterBase {

        @Override
    public FilterReply decide(LoggingEvent event) {
        if ((MDC.get(FACILITY_CODE_KEY) != null) && (Integer.parseInt(MDC.get(FACILITY_CODE_KEY)) == (SYSLOG_FACILITY_DAEMON)))
 {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
          }
    }// decide

}// class LogFilterFacilityDAEMON


