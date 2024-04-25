package org.akaza.openclinica.domain.managestudy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "protocol_deviation")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = { @Parameter(name = "sequence_name", value = "protocol_deviation_protocol_deviation_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolDeviation extends AbstractAuditableMutableDomainObject {
    private static final long serialVersionUID = -3852612749282796891L;

    private int protocolDeviationId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public int getProtocolDeviationSeverityId() {
        return protocolDeviationSeverityId;
    }

    public void setProtocolDeviationSeverityId(int protocolDeviationSeverityId) {
        this.protocolDeviationSeverityId = protocolDeviationSeverityId;
    }

    private int protocolDeviationSeverityId;
    private int studyId;
    @Id
    @Column(name = "protocol_deviation_id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")
    public int getProtocolDeviationId() {
        return protocolDeviationId;
    }

    public void setProtocolDeviationId(int protocolDeviationId) {
        this.protocolDeviationId = protocolDeviationId;
    }


    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

}
