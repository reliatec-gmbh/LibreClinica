// Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
package org.akaza.openclinica.domain.datamap;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * CrfVersion generated by hbm2java
 */
@Entity
@Table(name = "crf_version", uniqueConstraints = @UniqueConstraint(columnNames = "oc_oid"))
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "crf_version_crf_version_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CrfVersion extends DataMapDomainObject {

    private int crfVersionId;
    private UserAccount userAccount;
    private Status status;
    private CrfBean crf;
    private String name;
    private String description;
    private String revisionNotes;
    private Date dateCreated;
    private Date dateUpdated;
    private Integer updateId;
    private String ocOid;
    private String xform;
    private String xformName;
    private Set filterCrfVersionMaps = new HashSet(0);
    private List<VersioningMap> versioningMaps;
    private List<EventCrf> eventCrfs;
    private List<Section> sections;
    private List<EventDefinitionCrf> eventDefinitionCrfs;
    private Set decisionConditions = new HashSet(0);
    private Set<ItemGroupMetadata> itemGroupMetadatas;

    public CrfVersion() {
    }

    public CrfVersion(int crfVersionId, CrfBean crf, String ocOid) {
        this.crfVersionId = crfVersionId;
        this.crf = crf;
        this.ocOid = ocOid;
    }

    public CrfVersion(int crfVersionId, UserAccount userAccount, Status status, CrfBean crf, String name, String description, String revisionNotes,
            Date dateCreated, Date dateUpdated, Integer updateId, String ocOid, String xform, String xformName, Set filterCrfVersionMaps,
            List<VersioningMap> versioningMaps, List<EventCrf> eventCrfs, List<Section> sections, List<EventDefinitionCrf> eventDefinitionCrfs,
            Set decisionConditions, Set<ItemGroupMetadata> itemGroupMetadatas) {
        this.crfVersionId = crfVersionId;
        this.userAccount = userAccount;
        this.status = status;
        this.crf = crf;
        this.name = name;
        this.description = description;
        this.revisionNotes = revisionNotes;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.updateId = updateId;
        this.ocOid = ocOid;
        this.xform = xform;
        this.xformName = xformName;
        this.filterCrfVersionMaps = filterCrfVersionMaps;
        this.versioningMaps = versioningMaps;
        this.eventCrfs = eventCrfs;
        this.sections = sections;
        this.eventDefinitionCrfs = eventDefinitionCrfs;
        this.decisionConditions = decisionConditions;
        this.itemGroupMetadatas = itemGroupMetadatas;
    }

    @Id
    @Column(name = "crf_version_id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")
    public int getCrfVersionId() {
        return this.crfVersionId;
    }

    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @Type(type = "status")
    @Column(name = "status_id")
    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crf_id", nullable = false)
    public CrfBean getCrf() {
        return this.crf;
    }

    public void setCrf(CrfBean crf) {
        this.crf = crf;
    }

    @Column(name = "name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description", length = 4000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "revision_notes")
    public String getRevisionNotes() {
        return this.revisionNotes;
    }

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created", length = 4, updatable = false)
    public Date getDateCreated() {
        if (dateCreated != null) {
            return dateCreated;
        } else
            return new Date();
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

    @Column(name = "update_id")
    public Integer getUpdateId() {
        return this.updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    @Column(name = "oc_oid", unique = true, nullable = false, length = 40)
    public String getOcOid() {
        return this.ocOid;
    }

    public void setOcOid(String ocOid) {
        this.ocOid = ocOid;
    }

    @Column(name = "xform")
    public String getXform() {
        return xform;
    }

    public void setXform(String xform) {
        this.xform = xform;
    }

    @Column(name = "xform_name")
    public String getXformName() {
        return xformName;
    }

    public void setXformName(String xformName) {
        this.xformName = xformName;
    }

    /*
     * @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion") public Set getFilterCrfVersionMaps() { return
     * this.filterCrfVersionMaps; }
     * 
     * public void setFilterCrfVersionMaps(Set filterCrfVersionMaps) { this.filterCrfVersionMaps = filterCrfVersionMaps;
     * }
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion")
    public List<VersioningMap> getVersioningMaps() {
        return this.versioningMaps;
    }

    public void setVersioningMaps(List<VersioningMap> versioningMaps) {
        this.versioningMaps = versioningMaps;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion")
    public List<EventCrf> getEventCrfs() {
        return this.eventCrfs;
    }

    public void setEventCrfs(List<EventCrf> eventCrfs) {
        this.eventCrfs = eventCrfs;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion")
    public List<Section> getSections() {
        return this.sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion")
    public List<EventDefinitionCrf> getEventDefinitionCrfs() {
        return this.eventDefinitionCrfs;
    }

    public void setEventDefinitionCrfs(List<EventDefinitionCrf> eventDefinitionCrfs) {
        this.eventDefinitionCrfs = eventDefinitionCrfs;
    }

    /*
     * @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion") public Set getDecisionConditions() { return
     * this.decisionConditions; }
     * 
     * public void setDecisionConditions(Set decisionConditions) { this.decisionConditions = decisionConditions; }
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "crfVersion")
    public Set<ItemGroupMetadata> getItemGroupMetadatas() {
        return this.itemGroupMetadatas;
    }

    public void setItemGroupMetadatas(Set<ItemGroupMetadata> itemGroupMetadatas) {
        this.itemGroupMetadatas = itemGroupMetadatas;
    }

}
