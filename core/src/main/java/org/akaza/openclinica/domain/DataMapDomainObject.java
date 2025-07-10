/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain;

import java.io.Serializable;

import javax.persistence.Transient;

public class DataMapDomainObject implements MutableDomainObject,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -301662448289660793L;

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transient
	public Integer getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVersion(Integer version) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transient
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
