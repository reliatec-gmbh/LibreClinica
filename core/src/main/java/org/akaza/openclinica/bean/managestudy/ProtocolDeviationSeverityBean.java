package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

public class ProtocolDeviationSeverityBean extends EntityBean {
    private static final long serialVersionUID = -8498587413753118474L;
    private int protocolDeviationSeverityId;
    private String label;

    public int getProtocolDeviationSeverityId() {
        return protocolDeviationSeverityId;
    }

    public void setProtocolDeviationSeverityId(int protocolDeviationSeverityId) {
        this.protocolDeviationSeverityId = protocolDeviationSeverityId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
