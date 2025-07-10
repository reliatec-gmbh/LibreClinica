/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller.openrosa;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.akaza.openclinica.controller.openrosa.processor.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

@Component
public class SubmissionProcessorChain {
    
    @Autowired
    List<Processor> processors;
    
    @PostConstruct
    public void init() {
        Collections.sort(processors, AnnotationAwareOrderComparator.INSTANCE);
    }

    public void processSubmission(SubmissionContainer container) throws Exception {
        for (Processor processor:processors) {
            processor.process(container);
        }
    }
}
