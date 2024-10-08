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
 * @author ywang (Nov, 2008)
 * 
 */

public class StudyGroupItemBean {
    private String name;
    private String description;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String des) {
        this.description = des;
    }

    public String getDescription() {
        return this.description;
    }
}