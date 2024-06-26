/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.IdtView;
import org.hibernate.query.Query;

public class IdtViewDao extends AbstractDomainDao<IdtView> {

    @Override
    Class<IdtView> domainClass() {
        // TODO Auto-generated method stub
        return IdtView.class;
    }


    
    public List<IdtView> findFilter1(int studyId, int pStudyId, int per_page, int page, ArrayList<String> studySubjects,
            ArrayList<String>eventDefs,ArrayList<String> crfs , int tagId, String operation) {

        String query = " from " + getDomainClassName() + " where tagId ="+ tagId ;

        if (studySubjects.size() !=0)
            query = query + " and studySubjectId in (" + getListOf(studySubjects) + ")";

        if (eventDefs.size()!=0)
            query = query + " and sedOid in (" + getListOf(eventDefs) + ")";

         query = query + " and eventCrfId in (select eventCrfId from " +getDomainClassName() + " where  path is not null and (itemDataWorkflowStatus is null or itemDataWorkflowStatus!='done') group by eventCrfId))";  // EventCrf done       
        
        query = query + " and ((";

        if (crfs.size() !=0)
            query = query + " crfName in (" + getListOf(crfs) + ") and";
        query = query + " eventCrfStatusId=1) or eventCrfStatusId=2)  and (studyId= " + studyId + " " + operation + " parentStudyId=" + pStudyId +") ) "; 
 
        query = query + " order by itemDataId";
       
        Query<IdtView> q = getCurrentSession().createQuery(query, IdtView.class);
        q.setMaxResults(per_page); // limit
        q.setFirstResult((page - 1) * per_page); // offset
        return q.list();
    }

    
    public String getListOf(ArrayList<String> objects){
        String str="";
        String netStr="";
        for (String object:objects){
            str= str+ ",'"+object+"'";            
        }
        
        netStr=str.substring(1);
       return netStr; 
    }
    
    
}
