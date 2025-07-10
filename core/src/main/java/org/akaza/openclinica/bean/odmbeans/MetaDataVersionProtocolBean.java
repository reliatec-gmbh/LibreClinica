/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class MetaDataVersionProtocolBean {
    private List<ElementRefBean> studyEventRefs;
    
    public MetaDataVersionProtocolBean() {
        studyEventRefs = new ArrayList<ElementRefBean>();
    }

    public void setStudyEventRefs(List<ElementRefBean> sers) {
        this.studyEventRefs = sers;
    }

    public List<ElementRefBean> getStudyEventRefs() {
        return this.studyEventRefs;
    }
}