/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class Section {
	private Label label;
	private List<Group> group;
	private String appearance;
	private List<UserControl> usercontrol;
	
	public List<Group> getGroup() {
		return group;
	}
	public void setGroup(List<Group> group) {
		this.group = group;
	}
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}	
	public String getAppearance() {
		return appearance;
	}
	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}
	public List<UserControl> getUsercontrol() {
		return usercontrol;
	}
	public void setUsercontrol(List<UserControl> usercontrol) {
		this.usercontrol = usercontrol;
	}
}