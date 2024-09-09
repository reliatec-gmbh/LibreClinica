/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view;

/**
 * @author jxu
 *
 */
public class StudyInfoPanelLineSubmit extends StudyInfoPanelLine {
    private int eventId;
    private int eventCRFId;

    public StudyInfoPanelLineSubmit(String title, String info, boolean colon, boolean lastCRF, int eventId, int eventCRFId) {
        super(title, info, colon, lastCRF);
        this.eventId = eventId;
        this.eventCRFId = eventCRFId;

    }

    /**
     * @return Returns the eventCRFId.
     */
    public int getEventCRFId() {
        return eventCRFId;
    }

    /**
     * @param eventCRFId
     *            The eventCRFId to set.
     */
    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    /**
     * @return Returns the eventId.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     *            The eventId to set.
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

}
