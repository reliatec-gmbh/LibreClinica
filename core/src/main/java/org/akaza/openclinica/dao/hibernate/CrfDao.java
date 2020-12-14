/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.CrfOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.hibernate.query.Query;

public class CrfDao extends AbstractDomainDao<CrfBean> {

    @Override
    Class<CrfBean> domainClass() {
        // TODO Auto-generated method stub
        return CrfBean.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public CrfBean findByName(String crfName) {
        String query = "from " + getDomainClassName() + " crf  where crf.name = :crfName ";
        Query<CrfBean> q = getCurrentSession().createQuery(query, CrfBean.class);
        q.setString("crfName", crfName);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public CrfBean findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        Query<CrfBean> q = getCurrentSession().createQuery(query, CrfBean.class);
        q.setString("OCOID", OCOID);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public CrfBean findByCrfId(Integer crfId) {
        String query = "from " + getDomainClassName() + " crf  where crf.crfId = :crfId ";
        Query<CrfBean> q = getCurrentSession().createQuery(query, CrfBean.class);
        q.setInteger("crfId", crfId);
        return q.uniqueResult();
    }
    
    private String getOid(CrfBean crf, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid;
        try {
            oid = crf.getOcOid() != null ? crf.getOcOid() : oidGenerator.generateOid(crfName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CrfBean crfBean, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid = getOid(crfBean, crfName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

}
