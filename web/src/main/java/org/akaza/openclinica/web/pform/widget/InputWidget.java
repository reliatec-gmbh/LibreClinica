/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class InputWidget extends BaseWidget {
	private ItemBean item = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private ItemGroupBean itemGroupBean = null;
	private ItemFormMetadataBean itemFormMetadataBean = null;
	private boolean isItemRequired;
	private String expression;

	public InputWidget(CRFVersionBean version, ItemBean item, String appearance, ItemGroupBean itemGroupBean,
			ItemFormMetadataBean itemFormMetadataBean,  boolean isItemRequired, 
			String expression) {
		this.item = item;
		this.version = version;
		this.itemGroupBean = itemGroupBean;
		this.itemFormMetadataBean = itemFormMetadataBean;
		this.isItemRequired = isItemRequired;
		this.appearance = appearance;
		this.expression = expression;
	}

	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel(itemFormMetadataBean.getLeftItemText());

		input.setLabel(label);
		// Hint hint = new Hint();
		// hint.setHint(item.getItemMeta().getLeftItemText());
		// input.setHint(hint);
		if (appearance != null)
			input.setAppearance(appearance);
		input.setRef("/" + version.getOid() + "/" + itemGroupBean.getOid() + "/" + item.getOid());
		return input;
	}

	@Override
	public Bind getBinding() {
		String relevant = null;
		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() + "/" + itemGroupBean.getOid() + "/" + item.getOid());
		Integer responseTypeId = itemFormMetadataBean.getResponseSet().getResponseTypeId();
		if (responseTypeId == 8 || responseTypeId == 9)
			binding.setReadOnly("true()");
		relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);
		binding.setType(getDataType(item));

		if (isItemRequired)
			binding.setRequired("true()");
		return binding;
	}

}
