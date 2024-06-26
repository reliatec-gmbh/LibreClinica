/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;



/**
 *
 * @author ywang (Aug, 2010)
 *
 */

public class AgeBean {
    private String minimumAge;
    private String maximumAge;
    
    public String getMinimumAge() {
        return minimumAge;
    }
    public void setMinimumAge(String minimumAge) {
        this.minimumAge = minimumAge;
    }
    public String getMaximumAge() {
        return maximumAge;
    }
    public void setMaximumAge(String maximumAge) {
        this.maximumAge = maximumAge;
    }
}