/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: https://libreclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author ywang (Augest, 2010)
 *
 */

public class ItemDetailsBean extends ElementOIDBean {
    private List<ItemPresentInFormBean> itemPresentInForms = new ArrayList<ItemPresentInFormBean>();

    public List<ItemPresentInFormBean> getItemPresentInForm() {
        return itemPresentInForms;
    }

    public void setItemPresentInForm(List<ItemPresentInFormBean> itemPresentInForms) {
        this.itemPresentInForms = itemPresentInForms;
    }
}