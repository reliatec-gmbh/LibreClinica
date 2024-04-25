/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.List;

/**
 * @author thickerson
 */
public class LaboratoryBean extends AuditableEntityBean {
    /**
     * 
     */
    private static final long serialVersionUID = 3455655567554756756L;
    private int labId = 0;
    private String labName = null;
    private String address1 = null;
    private String address2 = null;
    private String stateProvinceRegion = null;
    private String city = null;
    private String country = null;
    private String zipPostal = null;
    private Boolean activeLab = null;
    private List labTypes = null;

    public int getLabId() {
        return labId;
    }

    public void setLabId(int labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getStateProvinceRegion() {
        return stateProvinceRegion;
    }

    public void setStateProvinceRegion(String stateProvinceRegion) {
        this.stateProvinceRegion = stateProvinceRegion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipPostal() {
        return zipPostal;
    }

    public void setZipPostal(String zipPostal) {
        this.zipPostal = zipPostal;
    }

    public Boolean getActiveLab() {
        return activeLab;
    }

    public void setActiveLab(Boolean activeLab) {
        this.activeLab = activeLab;
    }

    public List getLabTypes() {
        return labTypes;
    }

    public void setLabTypes(List labTypes) {
        this.labTypes = labTypes;
    }
}
