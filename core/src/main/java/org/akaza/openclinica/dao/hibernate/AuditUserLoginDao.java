/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.query.Query;

public class AuditUserLoginDao extends AbstractDomainDao<AuditUserLoginBean> {

    @Override
    public Class<AuditUserLoginBean> domainClass() {
        return AuditUserLoginBean.class;
    }

    // TODO update to CriteriaQuery 
    public ArrayList<AuditUserLoginBean> findAll() {
        String query = "from " + getDomainClassName() + " aul order by aul.loginAttemptDate desc ";
        Query<AuditUserLoginBean> q = getCurrentSession().createQuery(query, AuditUserLoginBean.class);
        return new ArrayList<>(q.list());
    }

    public int getCountWithFilter(final AuditUserLoginFilter filter) {
        // TODO update to CriteriaQuery 
        @SuppressWarnings("deprecation")
		Criteria criteria = getCurrentSession().createCriteria(domainClass());
        criteria = filter.execute(criteria);
        criteria.setProjection(Projections.rowCount()).uniqueResult();
        return ((Long) criteria.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> getWithFilterAndSort(final AuditUserLoginFilter filter, final AuditUserLoginSort sort, final int rowStart,
            final int rowEnd) {
        // TODO update to CriteriaQuery 
        @SuppressWarnings("deprecation")
        Criteria criteria = getCurrentSession().createCriteria(domainClass());
        criteria = filter.execute(criteria);
        criteria = sort.execute(criteria);
        criteria.setFirstResult(rowStart);
        criteria.setMaxResults(rowEnd - rowStart);
        return new ArrayList<>(criteria.list());
    }

}
