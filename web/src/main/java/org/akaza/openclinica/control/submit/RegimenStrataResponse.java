package org.akaza.openclinica.control.submit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegimenStrataResponse {
    @JsonProperty(value = "weight", required = false)
    public String weight;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format("weight:%s", getWeight());
    }
}
