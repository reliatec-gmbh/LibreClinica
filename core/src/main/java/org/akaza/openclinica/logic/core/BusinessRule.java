/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.core;

import org.akaza.openclinica.bean.core.EntityBean;

/**
 * @author thickerson
 * Created on Sep 1, 2005
 */
public interface BusinessRule {
    public abstract boolean isPropertyTrue(String s);

    public abstract EntityBean doAction(EntityBean o);
}
