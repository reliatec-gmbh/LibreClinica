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

import org.akaza.openclinica.domain.DataMapDomainObject;

/**
 * VersioningMapId generated by hbm2java
 * Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
 */
@Embeddable
public class VersioningMapId extends DataMapDomainObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7124609831795227927L;
	private Integer crfVersionId;
	private Integer itemId;

	public VersioningMapId() {
	}

	public VersioningMapId(Integer crfVersionId, Integer itemId) {
		this.crfVersionId = crfVersionId;
		this.itemId = itemId;
	}

	@Column(name = "crf_version_id")
	public Integer getCrfVersionId() {
		return this.crfVersionId;
	}

	public void setCrfVersionId(Integer crfVersionId) {
		this.crfVersionId = crfVersionId;
	}

	@Column(name = "item_id")
	public Integer getItemId() {
		return this.itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof VersioningMapId))
			return false;
		VersioningMapId castOther = (VersioningMapId) other;

		return ((this.getCrfVersionId() == castOther.getCrfVersionId()) || (this
				.getCrfVersionId() != null
				&& castOther.getCrfVersionId() != null && this
				.getCrfVersionId().equals(castOther.getCrfVersionId())))
				&& ((this.getItemId() == castOther.getItemId()) || (this
						.getItemId() != null && castOther.getItemId() != null && this
						.getItemId().equals(castOther.getItemId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37
				* result
				+ (getCrfVersionId() == null ? 0 : this.getCrfVersionId()
						.hashCode());
		result = 37 * result
				+ (getItemId() == null ? 0 : this.getItemId().hashCode());
		return result;
	}


}
