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
 * @author ywang (Augest, 2010)
 *
 */

public class PresentInFormBean {
    //attributes
    private String formOid;
    private String showGroup;
    //elements
    private String itemGroupHeader;
    private ItemGroupRepeatBean itemGroupRepeatBean;
    
    public PresentInFormBean() {
        itemGroupRepeatBean = new ItemGroupRepeatBean();
    }
    
    public String getFormOid() {
        return formOid;
    }
    public void setFormOid(String formOid) {
        this.formOid = formOid;
    }
    public String getItemGroupHeader() {
        return itemGroupHeader;
    }
    public void setItemGroupHeader(String itemGroupHeader) {
        this.itemGroupHeader = itemGroupHeader;
    }
    public ItemGroupRepeatBean getItemGroupRepeatBean() {
        return itemGroupRepeatBean;
    }
    public void setItemGroupRepeatBean(ItemGroupRepeatBean itemGroupRepeatBean) {
        this.itemGroupRepeatBean = itemGroupRepeatBean;
    }

    public String getShowGroup() {
        return showGroup;
    }

    public void setShowGroup(String showGroup) {
        this.showGroup = showGroup;
    }
}