/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Section;
import org.hibernate.query.NativeQuery;

public class SectionDao extends AbstractDomainDao<Section> {

    @Override
    Class<Section> domainClass() {
        // TODO Auto-generated method stub
        return Section.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public Section findByCrfVersionOrdinal(int crfVersionId, int ordinal) {
        // String query = "from " + getDomainClassName() + " section  where section.crfVersionId = :crfversionid ";
        // org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        // q.set.setInteger("crfversionid", crf_version_id);
        // return (Section) q.uniqueResult();

        String query = " select s.* from section s where s.crf_version_id = :crfVersionId and ordinal = :ordinal ";
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("ordinal", ordinal);
        q.setCacheable(true);
        return (Section) q.uniqueResult();
    }

}
