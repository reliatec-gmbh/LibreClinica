/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventCrf;
import org.hibernate.query.Query;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventCrf.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public EventCrf findByStudyEventIdStudySubjectIdCrfVersionId(int study_event_id, int study_subject_id, int crf_version_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crfVersionId = :crfversionid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        Query<EventCrf> q = getCurrentSession().createQuery(query, EventCrf.class);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfversionid", crf_version_id);
        return (EventCrf) q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public EventCrf findByStudyEventIdStudySubjectIdCrfId(int study_event_id, int study_subject_id, int crf_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crf.crfId = :crfid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        Query<EventCrf> q = getCurrentSession().createQuery(query, EventCrf.class);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfid", crf_id);
        return (EventCrf) q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
	public List<EventCrf> findByStudyEventIdStudySubjectId(Integer studyEventId, String studySubjectOid) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.ocOid= :studysubjectoid";
        Query<EventCrf> q = getCurrentSession().createQuery(query, EventCrf.class);
        q.setInteger("studyeventid", studyEventId);
        q.setString("studysubjectoid", studySubjectOid);
        return q.list();
	}

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<EventCrf> findByStudyEventStatus(Integer studyEventId, Integer statusCode) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId = :statusid";
        Query<EventCrf> q = getCurrentSession().createQuery(query, EventCrf.class);
        q.setInteger("studyeventid", studyEventId);
        q.setInteger("statusid", statusCode);
        return q.list();
    }
    
        
}
