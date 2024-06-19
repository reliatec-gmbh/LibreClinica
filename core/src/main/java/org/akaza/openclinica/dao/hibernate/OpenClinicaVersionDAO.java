/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/**
 * 
 */
package org.akaza.openclinica.dao.hibernate;

import java.sql.Timestamp;

import org.akaza.openclinica.domain.OpenClinicaVersionBean;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pgawade
 *
 */
public class OpenClinicaVersionDAO extends AbstractDomainDao<OpenClinicaVersionBean> {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
    @Override
    public Class<OpenClinicaVersionBean> domainClass() {
        return OpenClinicaVersionBean.class;
    }

    @Transactional
    public OpenClinicaVersionBean findDefault() {
        String query = "from " + getDomainClassName() + " ocVersion";
        Query<OpenClinicaVersionBean> q = getCurrentSession().createQuery(query, OpenClinicaVersionBean.class);
        return q.uniqueResult();
    }

    @Transactional
    public void saveOCVersionToDB(String OpenClinicaVersion) {
        logger.debug("OpenClinicaVersionDAO -> saveOCVersionToDB");
        logger.debug("OpenClinicaVersion: " + OpenClinicaVersion);
        // Delete the previous entry if exists in the database
        deleteDefault();
        // Insert new entry
        Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
        OpenClinicaVersionBean openClinicaVersionBean = new OpenClinicaVersionBean();
        openClinicaVersionBean.setName(OpenClinicaVersion);
        openClinicaVersionBean.setUpdate_timestamp(currentTimestamp);
        saveOrUpdate(openClinicaVersionBean);

    }

    @Transactional
    public int deleteDefault() {
        String query = "delete from " + getDomainClassName() + " ocVersion";
        Query<?> q = getCurrentSession().createQuery(query);
        return q.executeUpdate();
    }

}
