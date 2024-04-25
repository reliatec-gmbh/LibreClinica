package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

public class ProtocolDeviationSubjectBean extends EntityBean {
    private static final long serialVersionUID = -8498660903753888474L;
    private int protocolDeviationId;
    private int subjectId;
    private String label;
    private String secondaryLabel;

    public int getProtocolDeviationId() {
        return protocolDeviationId;
    }

    public void setProtocolDeviationId(int protocolDeviationId) {
        this.protocolDeviationId = protocolDeviationId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    public void setSecondaryLabel(String secondaryLabel) {
        this.secondaryLabel = secondaryLabel;
    }
}
