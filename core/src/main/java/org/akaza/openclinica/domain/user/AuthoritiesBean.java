/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.user;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <p>
 * Spring Security authorities table
 * </p>
 * 
 * @author Krikor Krumlian
 */
@Entity
@Table(name = "authorities")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "authorities_id_seq") })
public class AuthoritiesBean extends AbstractMutableDomainObject {

    /**
	 * 
	 */
	private static final long serialVersionUID = -280415180770391391L;
	String username;
    String authority;

    public AuthoritiesBean() {
        setDefaultAuthority();
    }

    public AuthoritiesBean(String username) {
        this.username = username;
        setDefaultAuthority();
    }

    public void setDefaultAuthority() {
        this.authority = "ROLE_USER";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
