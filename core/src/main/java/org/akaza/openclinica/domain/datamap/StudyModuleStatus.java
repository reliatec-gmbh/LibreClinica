/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.datamap;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;

/**
 * StudyModuleStatus generated by hbm2java
 * Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
 */
@Entity
@Table(name = "study_module_status")
public class StudyModuleStatus  extends AbstractMutableDomainObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6903614844392782392L;
	private int id;
	private Integer version;
	private Study study;
	private Integer study_1;
	private Integer crf;
	private Integer eventDefinition;
	private Integer subjectGroup;
	private Integer rule;
	private Integer site;
	private Integer users;
	private Date dateCreated;
	private Date dateUpdated;
	private Integer ownerId;
	private Integer updateId;
	private Integer statusId;

	public StudyModuleStatus() {
	}

	public StudyModuleStatus(int id) {
		this.id = id;
	}

	public StudyModuleStatus(int id, Study study, Integer study_1, Integer crf,
			Integer eventDefinition, Integer subjectGroup, Integer rule,
			Integer site, Integer users, Date dateCreated, Date dateUpdated,
			Integer ownerId, Integer updateId, Integer statusId) {
		this.id = id;
		this.study = study;
		this.study_1 = study_1;
		this.crf = crf;
		this.eventDefinition = eventDefinition;
		this.subjectGroup = subjectGroup;
		this.rule = rule;
		this.site = site;
		this.users = users;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.ownerId = ownerId;
		this.updateId = updateId;
		this.statusId = statusId;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Version
	@Column(name = "version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	public Study getStudy() {
		return this.study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	@Column(name = "study")
	public Integer getStudy_1() {
		return this.study_1;
	}

	public void setStudy_1(Integer study_1) {
		this.study_1 = study_1;
	}

	@Column(name = "crf")
	public Integer getCrf() {
		return this.crf;
	}

	public void setCrf(Integer crf) {
		this.crf = crf;
	}

	@Column(name = "event_definition")
	public Integer getEventDefinition() {
		return this.eventDefinition;
	}

	public void setEventDefinition(Integer eventDefinition) {
		this.eventDefinition = eventDefinition;
	}

	@Column(name = "subject_group")
	public Integer getSubjectGroup() {
		return this.subjectGroup;
	}

	public void setSubjectGroup(Integer subjectGroup) {
		this.subjectGroup = subjectGroup;
	}

	@Column(name = "rule")
	public Integer getRule() {
		return this.rule;
	}

	public void setRule(Integer rule) {
		this.rule = rule;
	}

	@Column(name = "site")
	public Integer getSite() {
		return this.site;
	}

	public void setSite(Integer site) {
		this.site = site;
	}

	@Column(name = "users")
	public Integer getUsers() {
		return this.users;
	}

	public void setUsers(Integer users) {
		this.users = users;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "date_created", length = 4)
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "date_updated", length = 4)
	public Date getDateUpdated() {
		return this.dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	@Column(name = "owner_id")
	public Integer getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	@Column(name = "update_id")
	public Integer getUpdateId() {
		return this.updateId;
	}

	public void setUpdateId(Integer updateId) {
		this.updateId = updateId;
	}

	@Column(name = "status_id")
	public Integer getStatusId() {
		return this.statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

}
