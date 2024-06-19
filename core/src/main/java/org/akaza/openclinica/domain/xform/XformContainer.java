/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.xform;

import java.util.List;

public class XformContainer {
    private List<XformGroup> groups;
    private List<XformItem> items;
    private String instanceName;

    public List<XformGroup> getGroups() {
        return groups;
    }

    public XformItem findItemByGroupAndRef(XformGroup xformGroup, String ref) {
        for (XformGroup group : groups) {
            if (group.getGroupPath().equals(xformGroup.getGroupPath()))
                for (XformItem item : group.getItems()) {
                    if (item.getItemPath().equals(ref))
                        return item;
                }
        }
        return null;
    }

    public XformGroup findGroupByRef(String ref) {
        for (XformGroup group : groups) {
            if (group.getGroupPath().equals(ref))
                return group;
        }
        return null;
    }

    public void setGroups(List<XformGroup> groups) {
        this.groups = groups;
    }

    public List<XformItem> getItems() {
        return items;
    }

    public void setItems(List<XformItem> items) {
        this.items = items;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

}
