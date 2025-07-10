/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;

/**
 * @author jxu
 *
 */
public class DisplayEventDefinitionCRFBean extends AuditableEntityBean {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6451406000935867787L;
	private EventDefinitionCRFBean edc;
    private EventCRFBean eventCRF;
    private boolean completedEventCRFs;

    /**
     * @return Returns the completedEventCRFs.
     */
    public boolean isCompletedEventCRFs() {
        return completedEventCRFs;
    }

    /**
     * @param completedEventCRFs
     *            The completedEventCRFs to set.
     */
    public void setCompletedEventCRFs(boolean completedEventCRFs) {
        this.completedEventCRFs = completedEventCRFs;
    }

    /**
     * @return Returns the edc.
     */
    public EventDefinitionCRFBean getEdc() {
        return edc;
    }

    /**
     * @param edc
     *            The edc to set.
     */
    public void setEdc(EventDefinitionCRFBean edc) {
        this.edc = edc;
    }

    /**
     * @return Returns the eventCRF.
     */
    public EventCRFBean getEventCRF() {
        return eventCRF;
    }

    /**
     * @param eventCRF
     *            The eventCRF to set.
     */
    public void setEventCRF(EventCRFBean eventCRF) {
        this.eventCRF = eventCRF;
    }
}
