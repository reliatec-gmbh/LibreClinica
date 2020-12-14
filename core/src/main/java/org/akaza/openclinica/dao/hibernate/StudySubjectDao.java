/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.hibernate.query.Query;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<StudySubject> findAllByStudy(Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid";
        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setInteger("studyid", studyId);
        return q.list();
      
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public StudySubject findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setString("OCOID", OCOID);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public StudySubject findByLabelAndStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.studyId = :studyid and do.label = :label";
        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setInteger("studyid", study.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public StudySubject findByLabelAndStudyOrParentStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid) and do.label = :label";
        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setInteger("studyid", study.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ArrayList<StudySubject> findByLabelAndParentStudy(String embeddedStudySubjectId, Study parentStudy) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.study.studyId = :studyid and do.label = :label";
        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setInteger("studyid", parentStudy.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return new ArrayList<>(q.list());
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        Query<StudyEvent> q = getCurrentSession().createQuery(query, StudyEvent.class);
        q.setString("id", id.toString());

        return new ArrayList<>(q.list());

    }
    public String getValidOid(StudySubject studySubject, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid = getOid(studySubject);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(StudySubject studySubject) {
        OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid;
        try {
            oid = studySubject.getOcOid() != null ? studySubject.getOcOid() : oidGenerator.generateOid(studySubject.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public int findTheGreatestLabelByStudy(Integer studyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid)";

        Query<StudySubject> q = getCurrentSession().createQuery(query, StudySubject.class);
        q.setInteger("studyid", studyId);
        List<StudySubject> allStudySubjects = q.list();
        
        int greatestLabel = 0;
        for (StudySubject subject:allStudySubjects) {
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(subject.getLabel());
            } catch (NumberFormatException ne) {
                labelInt = 0;
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        return greatestLabel;
    }

}
