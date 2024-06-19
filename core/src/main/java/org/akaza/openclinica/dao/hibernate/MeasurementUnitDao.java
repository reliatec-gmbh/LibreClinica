/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */

package org.akaza.openclinica.dao.hibernate;

import java.util.TreeSet;

import org.akaza.openclinica.domain.admin.MeasurementUnit;
import org.hibernate.query.Query;

public class MeasurementUnitDao extends AbstractDomainDao<MeasurementUnit> {
    @Override
    Class<MeasurementUnit> domainClass() {
        return MeasurementUnit.class;
    }

    // TODO update to CriteriaQuery
    public TreeSet<String> findAllOIDs() {
        String query = "select mu.ocOid from  " + this.getDomainClassName() + " mu order by mu.ocOid asc";
        Query<String> q = this.getCurrentSession().createQuery(query, String.class);
        return new TreeSet<String>(q.list());
    }

    // TODO update to CriteriaQuery
    public TreeSet<String> findAllNames() {
        String query = "select distinct mu.name from  " + this.getDomainClassName() + " mu order by mu.name asc";
        Query<String> q = this.getCurrentSession().createQuery(query, String.class);
        return new TreeSet<String>(q.list());
    }

    // TODO update to CriteriaQuery 
    public TreeSet<String> findAllNamesInUpperCase() {
        String query = "select upper(mu.name) from  " + this.getDomainClassName() + " mu order by mu.name asc";
        Query<String> q = this.getCurrentSession().createQuery(query, String.class);
        return new TreeSet<String>(q.list());
    }
}