/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.managestudy.StudyModuleStatus;
import org.hibernate.query.Query;

/**
 * @author: Shamim
 * Date: Feb 18, 2009
 * Time: 8:01:42 PM
 */
public class StudyModuleStatusDao extends AbstractDomainDao<StudyModuleStatus> {
    @Override
    Class<StudyModuleStatus> domainClass() {
        return StudyModuleStatus.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public StudyModuleStatus findByStudyId(int studyId) {
        String query = "from " + getDomainClassName() + " sms  where sms.studyId = :studyId ";
        Query<StudyModuleStatus> q = getCurrentSession().createQuery(query, StudyModuleStatus.class);
        q.setInteger("studyId", studyId);
        return q.uniqueResult();
    }

}
