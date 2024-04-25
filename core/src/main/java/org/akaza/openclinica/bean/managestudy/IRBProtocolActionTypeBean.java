package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

public class IRBProtocolActionTypeBean extends EntityBean {
    private static final long serialVersionUID = -8498550403423118479L;
    private int protocolActionTypeId;
    private String label;

    public int getProtocolActionTypeId() {
        return protocolActionTypeId;
    }

    public void setProtocolActionTypeId(int protocolActionTypeId) {
        this.protocolActionTypeId = protocolActionTypeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
