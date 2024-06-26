/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;

public class ItemItemDataContainer {
	private ItemBean itemBean;
	private ItemDataBean itemDataBean;
	private Integer responseTypeId;
	
	public ItemItemDataContainer(ItemBean itemBean, ItemDataBean itemDataBean , Integer responseTypeId) {
		super();
		this.itemBean = itemBean;
		this.itemDataBean = itemDataBean;
		this.responseTypeId=responseTypeId;
	}
	public ItemBean getItemBean() {
		return itemBean;
	}
	public void setItemBean(ItemBean itemBean) {
		this.itemBean = itemBean;
	}
	public ItemDataBean getItemDataBean() {
		return itemDataBean;
	}
	public void setItemDataBean(ItemDataBean itemDataBean) {
		this.itemDataBean = itemDataBean;
	}
	public Integer getResponseTypeId() {
		return responseTypeId;
	}
	public void setResponseTypeId(Integer responseTypeId) {
		this.responseTypeId = responseTypeId;
	}
	
}
