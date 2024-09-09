/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.masking.rules;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.logic.core.BusinessRule;

/**
 * @author thickerson
 * Created on Sep 1, 2005
 */
public class MaskSubjectDOBRule implements BusinessRule {
    public boolean isPropertyTrue(String s) {
        if (s.equals(this.getClass().getName())) {
            return true;
        } else {
            return false;
        }
    }

    public EntityBean doAction(EntityBean sb) {
        // cast to a subject bean
        SubjectBean ssb = (SubjectBean) sb;
        ssb.setDateOfBirth(null);// effectively xx-xx-xxxx
        return sb;
    }

}
