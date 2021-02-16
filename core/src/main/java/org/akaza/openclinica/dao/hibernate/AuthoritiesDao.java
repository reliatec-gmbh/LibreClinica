/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.AuthoritiesBean;


public class AuthoritiesDao extends AbstractDomainDao<AuthoritiesBean> {

    @Override
    public Class<AuthoritiesBean> domainClass() {
        return AuthoritiesBean.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public AuthoritiesBean findByUsername(String username) {
        String query = "from " + getDomainClassName() + " authorities  where authorities.username = :username ";
        org.hibernate.query.Query<AuthoritiesBean> q = getCurrentSession().createQuery(query, AuthoritiesBean.class);
        q.setString("username", username);
        return q.uniqueResult();
    }
}
