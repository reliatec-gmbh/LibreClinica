/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class SectionTextWidget implements Widget {

	private String versionOid = null;
	private String text = null;
	private Integer sectionId = 0;
	private String textType = null;
	
	public SectionTextWidget(String versionOid, String text, Integer sectionId, String textType)
	{
		this.versionOid = versionOid;
		this.text = text;
		this.sectionId = sectionId;
		this.textType = textType;
	}
	
	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel(text);
		input.setLabel(label);
		input.setRef("/" + versionOid + "/SECTION_" + String.valueOf(sectionId) + "." + textType);
		return input;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		binding.setNodeSet("/" + versionOid + "/SECTION_" + String.valueOf(sectionId) + "." + textType);
		binding.setType("string");
		binding.setReadOnly("true()");
		return binding;
	}

}
