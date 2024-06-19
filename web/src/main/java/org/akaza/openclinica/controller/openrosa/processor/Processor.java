/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;

public interface Processor {

    public void process(SubmissionContainer container) throws Exception;

}
