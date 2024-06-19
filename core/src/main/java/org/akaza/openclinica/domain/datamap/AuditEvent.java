/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
// default package
// Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
package org.akaza.openclinica.domain.datamap;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;

/**
 * AuditEvent generated by hbm2java
 */
@Entity
@Table(name = "audit_event")
public class AuditEvent extends AbstractMutableDomainObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1167137940398154409L;
	private int auditId;
	private Date auditDate;
	private String auditTable;
	private Integer userId;
	private Integer entityId;
	private String reasonForChange;
	private String actionMessage;
	private Set<?> auditEventValueses = new HashSet<>(0);
	private Set<?> auditEventContexts = new HashSet<>(0);

	public AuditEvent() {
	}

	public AuditEvent(int auditId, Date auditDate, String auditTable) {
		this.auditId = auditId;
		this.auditDate = auditDate;
		this.auditTable = auditTable;
	}

	public AuditEvent(int auditId, Date auditDate, String auditTable,
			Integer userId, Integer entityId, String reasonForChange,
			String actionMessage, Set<?> auditEventValueses, Set<?> auditEventContexts) {
		this.auditId = auditId;
		this.auditDate = auditDate;
		this.auditTable = auditTable;
		this.userId = userId;
		this.entityId = entityId;
		this.reasonForChange = reasonForChange;
		this.actionMessage = actionMessage;
		this.auditEventValueses = auditEventValueses;
		this.auditEventContexts = auditEventContexts;
	}

	@Id
	@Column(name = "audit_id", unique = true, nullable = false)
	public int getAuditId() {
		return this.auditId;
	}

	public void setAuditId(int auditId) {
		this.auditId = auditId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "audit_date", nullable = false, length = 8)
	public Date getAuditDate() {
		return this.auditDate;
	}

	public void setAuditDate(Date auditDate) {
		this.auditDate = auditDate;
	}

	@Column(name = "audit_table", nullable = false, length = 500)
	public String getAuditTable() {
		return this.auditTable;
	}

	public void setAuditTable(String auditTable) {
		this.auditTable = auditTable;
	}

	@Column(name = "user_id")
	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Column(name = "entity_id")
	public Integer getEntityId() {
		return this.entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	@Column(name = "reason_for_change", length = 1000)
	public String getReasonForChange() {
		return this.reasonForChange;
	}

	public void setReasonForChange(String reasonForChange) {
		this.reasonForChange = reasonForChange;
	}

	@Column(name = "action_message", length = 4000)
	public String getActionMessage() {
		return this.actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

	/*
	 * TODO there is no class defined for table 'audit_event_values'
	 * so this mapping does not work
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "auditEvent")
	public Set<?> getAuditEventValueses() {
		return this.auditEventValueses;
	}

	public void setAuditEventValueses(Set<?> auditEventValueses) {
		this.auditEventValueses = auditEventValueses;
	}

	/*
	 * TODO there is no class defined for table 'audit_event_context'
	 * so this mapping does not work
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "auditEvent")
	public Set<?> getAuditEventContexts() {
		return this.auditEventContexts;
	}

	public void setAuditEventContexts(Set<?> auditEventContexts) {
		this.auditEventContexts = auditEventContexts;
	}

}
