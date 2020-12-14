/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

public class AuditLogEventDao extends AbstractDomainDao<AuditLogEvent> {

    @Override
    public Class<AuditLogEvent> domainClass() {
        return AuditLogEvent.class;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findByParam(AuditLogEvent auditLogEvent, String anotherAuditTable) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName();
        String buildQuery = "";
        if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable == null) {
            buildQuery += "do.entityId =:entity_id ";
            buildQuery += " and  do.auditTable =:audit_table order by do.auditId ";
        } else if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable != null) {
            buildQuery += "do.entityId =:entity_id ";
            buildQuery += " and ( do.auditTable =:audit_table or do.auditTable =:anotherAuditTable) order by do.auditId ";
        }
        if (!buildQuery.isEmpty())
            query = "from " + getDomainClassName() + " do  where " + buildQuery;
        else
            query = "from " + getDomainClassName();
        org.hibernate.query.Query<T> q = getCurrentSession().createQuery(query);
        if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable == null) {
            q.setParameter("entity_id", auditLogEvent.getEntityId(), IntegerType.INSTANCE);
            q.setParameter("audit_table", auditLogEvent.getAuditTable(), StringType.INSTANCE);
        } else if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable != null) {
            q.setParameter("entity_id", auditLogEvent.getEntityId(), IntegerType.INSTANCE);
            q.setParameter("audit_table", auditLogEvent.getAuditTable(), StringType.INSTANCE);
            q.setParameter("anotherAuditTable", anotherAuditTable, StringType.INSTANCE);
        }
        return q.list();
    }
}