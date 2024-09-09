/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

public class SectionDetails {

	private Integer SectionId;
	private String SectionLabel;
	private String SectionTitle;
	private String SectionSubtitle;
	private String SectionInstructions;
	private String SectionPageNumber;
	public Integer getSectionId() {
		return SectionId;
	}
	public void setSectionId(Integer sectionId) {
		SectionId = sectionId;
	}
	public String getSectionLabel() {
		return SectionLabel;
	}
	public void setSectionLabel(String sectionLabel) {
		SectionLabel = sectionLabel;
	}
	public String getSectionTitle() {
		return SectionTitle;
	}
	public void setSectionTitle(String sectionTitle) {
		SectionTitle = sectionTitle;
	}
	public String getSectionSubtitle() {
		return SectionSubtitle;
	}
	public void setSectionSubtitle(String sectionSubtitle) {
		SectionSubtitle = sectionSubtitle;
	}
	public String getSectionInstructions() {
		return SectionInstructions;
	}
	public void setSectionInstructions(String sectionInstructions) {
		SectionInstructions = sectionInstructions;
	}
	public String getSectionPageNumber() {
		return SectionPageNumber;
	}
	public void setSectionPageNumber(String sectionPageNumber) {
		SectionPageNumber = sectionPageNumber;
	}
	

}
