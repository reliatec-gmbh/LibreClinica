/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform.dto;

public interface UserControl {

	public String getRef();
	public void setRef(String ref);
	public String getAppearance();
	public void setAppearance(String appearance);
	public Label getLabel();
	public void setLabel(Label label);
	public Hint getHint();
	public void setHint(Hint hint);

}
