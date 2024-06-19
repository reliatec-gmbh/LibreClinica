/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.datamap;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * StudyParameterValueId generated by hbm2java
 */
@Embeddable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class StudyParameterValueId  extends AbstractMutableDomainObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7998524344628954923L;
	private int studyParameterValueId;
	private Integer studyId;
	private String value;
	private String parameter;

	public StudyParameterValueId() {
	}

	public StudyParameterValueId(int studyParameterValueId) {
		this.studyParameterValueId = studyParameterValueId;
	}

	public StudyParameterValueId(int studyParameterValueId, Integer studyId,
			String value, String parameter) {
		this.studyParameterValueId = studyParameterValueId;
		this.studyId = studyId;
		this.value = value;
		this.parameter = parameter;
	}

	@Column(name = "study_parameter_value_id", nullable = false)
	public int getStudyParameterValueId() {
		return this.studyParameterValueId;
	}

	public void setStudyParameterValueId(int studyParameterValueId) {
		this.studyParameterValueId = studyParameterValueId;
	}

	@Column(name = "study_id")
	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	@Column(name = "value", length = 50)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "parameter")
	public String getParameter() {
		return this.parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof StudyParameterValueId))
			return false;
		StudyParameterValueId castOther = (StudyParameterValueId) other;

		return (this.getStudyParameterValueId() == castOther
				.getStudyParameterValueId())
				&& ((this.getStudyId() == castOther.getStudyId()) || (this
						.getStudyId() != null && castOther.getStudyId() != null && this
						.getStudyId().equals(castOther.getStudyId())))
				&& ((this.getValue() == castOther.getValue()) || (this
						.getValue() != null && castOther.getValue() != null && this
						.getValue().equals(castOther.getValue())))
				&& ((this.getParameter() == castOther.getParameter()) || (this
						.getParameter() != null
						&& castOther.getParameter() != null && this
						.getParameter().equals(castOther.getParameter())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getStudyParameterValueId();
		result = 37 * result
				+ (getStudyId() == null ? 0 : this.getStudyId().hashCode());
		result = 37 * result
				+ (getValue() == null ? 0 : this.getValue().hashCode());
		result = 37 * result
				+ (getParameter() == null ? 0 : this.getParameter().hashCode());
		return result;
	}

}
