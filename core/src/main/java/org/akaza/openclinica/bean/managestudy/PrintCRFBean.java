/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;

import java.util.List;

/**
 * @author: Shamim
 * Date: Nov 23, 2009
 * Time: 8:41:41 AM
 */
public class PrintCRFBean {
    private StudyEventBean studyEventBean;
    private CRFBean crfBean;
    private CRFVersionBean crfVersionBean;
    private EventCRFBean eventCrfBean;
    private DisplaySectionBean displaySectionBean;
    private List<DisplaySectionBean> displaySectionBeans;
    private List<DisplaySectionBean> allSections;
    private boolean grouped;

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public StudyEventBean getStudyEventBean() {
        return studyEventBean;
    }

    public void setStudyEventBean(StudyEventBean studyEventBean) {
        this.studyEventBean = studyEventBean;
    }

    public CRFBean getCrfBean() {
        return crfBean;
    }

    public void setCrfBean(CRFBean crfBean) {
        this.crfBean = crfBean;
    }

    public CRFVersionBean getCrfVersionBean() {
        return crfVersionBean;
    }

    public void setCrfVersionBean(CRFVersionBean crfVersionBean) {
        this.crfVersionBean = crfVersionBean;
    }

    public EventCRFBean getEventCrfBean() {
        return eventCrfBean;
    }

    public void setEventCrfBean(EventCRFBean eventCrfBean) {
        this.eventCrfBean = eventCrfBean;
    }

    public DisplaySectionBean getDisplaySectionBean() {
        return displaySectionBean;
    }

    public void setDisplaySectionBean(DisplaySectionBean displaySectionBean) {
        this.displaySectionBean = displaySectionBean;
    }

    public List<DisplaySectionBean> getDisplaySectionBeans() {
        return displaySectionBeans;
    }

    public void setDisplaySectionBeans(List<DisplaySectionBean> displaySectionBeans) {
        this.displaySectionBeans = displaySectionBeans;
    }

    public List<DisplaySectionBean> getAllSections() {
        return allSections;
    }

    public void setAllSections(List<DisplaySectionBean> allSections) {
        this.allSections = allSections;
    }
}
