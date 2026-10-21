/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.rule.action;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("8")
public class RandomizeActionBean extends RuleActionBean {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8873774452920678216L;
	private List<PropertyBean> properties;
    private List<StratificationFactorBean> stratificationFactors;
      
    
    public RandomizeActionBean() {
        setActionType(ActionType.RANDOMIZE);
        setRuleActionRun(new RuleActionRunBean(true, true, true, false, false));
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "rule_action_id", nullable = true)
    public List<PropertyBean> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyBean> properties) {
        this.properties = properties;
    }
    


    public void addProperty(PropertyBean property) {
        if (properties == null) {
            properties = new ArrayList<PropertyBean>();
        }
        properties.add(property);
    }
     
   

    @Override
    @Transient
    public String getSummary() {
        return "";
    }

    @Override
    @Transient
    public HashMap<String, Object> getPropertiesForDisplay() {
        LinkedHashMap<String, Object> p = new LinkedHashMap<String, Object>();
        p.put("rule_action_type", getActionType().getDescription());
        return p;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (properties == null ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomizeActionBean other = (RandomizeActionBean) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else {// if (!properties.equals(other.properties))
            if (properties.size() != other.properties.size())
                return false;
            for (PropertyBean propertyBean : other.properties) {
                if (!properties.contains(propertyBean))
                    return false;
            }
        }
        if (stratificationFactors == null) {
            if (other.stratificationFactors != null)
                return false;
        } else {// if (!properties.equals(other.properties))
            if (other.stratificationFactors==null || stratificationFactors.size() != other.stratificationFactors.size())
                return false;
            for (StratificationFactorBean stratificationFactorBean : other.stratificationFactors) {
                if (!stratificationFactors.contains(stratificationFactorBean))
                    return false;
            }
        }
        return true;
    }


    // The factors are sent to the randomization module as question1..N in the order of this
    // list, so the order has to be stable. Without @OrderBy it is whatever order the database
    // returns the rows in. The rows are only ever created by a rule import, which saves them in
    // the order of the StratificationFactor elements in the rule XML, so the id order is the
    // order of the rule as it was imported. That last step relies on how castor and Hibernate
    // currently behave (document order when reading the XML, ids assigned in list order on the
    // cascaded save); re-check it when upgrading either library.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_action_id", nullable = true)
    @OrderBy("id")
    public List<StratificationFactorBean> getStratificationFactors() {
        return stratificationFactors;
    }

    public void setStratificationFactors(List<StratificationFactorBean> stratificationFactors) {
        this.stratificationFactors = stratificationFactors;
    }
    
    public void addStratificationFactor(StratificationFactorBean stratificationFactor) {
        if (stratificationFactors == null) {
            stratificationFactors = new ArrayList<StratificationFactorBean>();
        }
        stratificationFactors.add(stratificationFactor);
    }    

}


