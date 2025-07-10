/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain;

public interface MutableDomainObject extends DomainObject {

    /**
     * @return the optimistic locking version value for this object.
     */
    Integer getVersion();

    /**
     * Set the optimistic locking version value for this object.  In practice this should not be
     * called by application code -- just the persistence mechanism.
     * @param version
     */
    void setVersion(Integer version);

}
