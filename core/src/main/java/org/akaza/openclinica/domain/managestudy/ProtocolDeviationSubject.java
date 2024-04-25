package org.akaza.openclinica.domain.managestudy;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "protocol_deviation_subject")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "protocol_deviation_subject_id_seq") })
public class ProtocolDeviationSubject extends AbstractAuditableMutableDomainObject {
    private static final long serialVersionUID = -29612749282806891L;
    private int subjectId;

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
}
