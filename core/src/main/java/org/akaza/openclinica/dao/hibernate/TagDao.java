/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Tag;

public class TagDao extends AbstractDomainDao<Tag> {

    @Override
    Class<Tag> domainClass() {
        // TODO Auto-generated method stub
        return Tag.class;
    }

    
}
