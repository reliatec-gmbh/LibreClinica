/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: Feb 8, 2009
 * Time: 3:13:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class HideCRFManager {

    private HideCRFManager() {
    }

    public void optionallyCheckHideCRFProp(
      DisplayStudyEventBean displayStudyEventBean) {

        EventDefinitionCRFBean tempEventCRFBean = new EventDefinitionCRFBean();

            for(DisplayEventCRFBean deCRFBean : displayStudyEventBean.getAllEventCRFs()){
                 tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
                if(tempEventCRFBean.isHideCrf()) {
                    tempEventCRFBean.setHidden(true);
                }
            }

             for(DisplayEventCRFBean deCRFBean : displayStudyEventBean.getDisplayEventCRFs()){
                 tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
                if(tempEventCRFBean.isHideCrf()) {
                    tempEventCRFBean.setHidden(true);
                }
            }


    }
}
