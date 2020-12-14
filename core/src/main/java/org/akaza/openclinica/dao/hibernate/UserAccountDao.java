/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
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
    @SuppressWarnings("deprecation")
    public UserAccount findByUserName(String userName) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        Query<UserAccount> q = getCurrentSession().createQuery(query, UserAccount.class);
        q.setString("user_name", userName);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public UserAccount findByUserId(Integer userId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userId = :user_id";
        Query<UserAccount> q = getCurrentSession().createQuery(query, UserAccount.class);
        q.setInteger("user_id", userId);
        return q.uniqueResult();
    }

}
