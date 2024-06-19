/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.query.Query;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {

    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }

    // TODO update to CriteriaQuery
    public UserAccount findByUserName(String userName) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        Query<UserAccount> q = getCurrentSession()
                .createQuery(query, UserAccount.class)
                .setParameter("user_name", userName);
        return q.uniqueResult();
    }

    public UserAccount findByUserId(Integer userId) {
        getSessionFactory().getStatistics().logSummary();
        return getCurrentSession().byId(UserAccount.class).load(userId);
    }
}
