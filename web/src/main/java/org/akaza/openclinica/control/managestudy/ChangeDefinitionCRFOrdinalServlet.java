/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.view.Page;

/**
 * Processes request to change CRF ordinals in a study event definition
 * 
 * @author jxu
 */
public class ChangeDefinitionCRFOrdinalServlet extends ChangeOrdinalServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8530927860588404341L;

	/**
     * Override processRequest in super class
     */
    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int current = fp.getInt("current");
        int previous = fp.getInt("previous");
        int currOrdinal = fp.getInt("currentOrdinal");
        int prevOrdinal = fp.getInt("previousOrdinal");

        int definitionId = fp.getInt("id");
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        increase(current, previous, currOrdinal, prevOrdinal, definitionId, edcdao);
        int siteId = fp.getInt("siteId");
        if (siteId > 0) {
            request.setAttribute("idToSort", new Integer(definitionId).toString());
            request.setAttribute("siteId", siteId);
            forwardPage(Page.VIEW_SITE_SERVLET);
        } else {
            request.setAttribute("id", new Integer(definitionId).toString());
            forwardPage(Page.VIEW_EVENT_DEFINITION_SERVLET);
        }
    }

    /**
     * Increases the ordinal for current object and decrease the ordinal of the
     * previous one
     * 
     * @param idCurrent
     * @param idPrevious
     * @param dao
     */
    private void increase(int idCurrent, int idPrevious, int currOrdinal, int prevOrdinal, int defId, EventDefinitionCRFDAO dao) {
        EventDefinitionCRFBean current = (EventDefinitionCRFBean) dao.findByPK(idCurrent);
        EventDefinitionCRFBean previous = (EventDefinitionCRFBean) dao.findByPK(idPrevious);

        if (current.getOrdinal() == currOrdinal && previous.getOrdinal() == prevOrdinal) {
            if (idCurrent > 0) {
                int currentOrdinal = current.getOrdinal();
                current.setOrdinal(currentOrdinal - 1);
                current.setUpdater((UserAccountBean) session.getAttribute("userBean"));
                dao.update(current);
            }
            if (idPrevious > 0) {
                int previousOrdinal = previous.getOrdinal();
                previous.setOrdinal(previousOrdinal + 1);
                previous.setUpdater((UserAccountBean) session.getAttribute("userBean"));
                dao.update(previous);
            }

            ArrayList<EventDefinitionCRFBean> currOrdlist = dao.findAllByEventDefinitionIdAndOrdinal(defId, current.getOrdinal());
            ArrayList<EventDefinitionCRFBean> prevOrdlist = dao.findAllByEventDefinitionIdAndOrdinal(defId, previous.getOrdinal());
            if (currOrdlist.size() > 1 || prevOrdlist.size() > 1 ) {
                fixDuplicates(defId, dao);
            }
        }
    }

    /**
     * Fixes ordinal values if there is any duplicates
     *
     * @param definitionId
     * @param dao
     */
    private void fixDuplicates(int definitionId, EventDefinitionCRFDAO dao) {
        ArrayList<EventDefinitionCRFBean> list = dao.findAllByEventDefinitionId(definitionId);
        boolean incrementNextOrdinal = false;
        for (int i =0; i < list.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) list.get(i);
            if (i == 0) {
                if (edc.getOrdinal() != 0) {
                    edc.setOrdinal(i);
                    dao.update(edc);
                }
                continue;
            }
            if (incrementNextOrdinal) {
                edc.setOrdinal(i);
                dao.update(edc);
                continue;
            }
            if (edc.getOrdinal() != i) {
                edc.setOrdinal(i);
                dao.update(edc);
                incrementNextOrdinal = true;
            }
        }
    }


}
