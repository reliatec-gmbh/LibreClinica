/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.core;

import java.util.*;

// Internationalized name and description in Term.getName and
// Term.getDescription()
// Description identifiers are taken from facilityinfo.properties
public class Role extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8374394924123285829L;
	public static final Role INVALID = new Role(0, "invalid", "invalid", null);
    public static final Role ADMIN = new Role(1, "admin", "System_Administrator", null);
    public static final Role COORDINATOR = new Role(2, "coordinator", "Study_Coordinator", null);
    public static final Role STUDYDIRECTOR = new Role(3, "director", "Study_Director", null);
    public static final Role INVESTIGATOR = new Role(4, "Investigator", "Investigator", null);
    public static final Role RESEARCHASSISTANT = new Role(5, "ra", "Data_Entry_Person", null);
    public static final Role MONITOR = new Role(6, "monitor", "Monitor", null);
    public static final Role RESEARCHASSISTANT2 = new Role(7, "ra2", "site_Data_Entry_Person2", null);
   
    
    private static final Role[] members = { ADMIN, COORDINATOR, STUDYDIRECTOR, INVESTIGATOR, MONITOR, RESEARCHASSISTANT,RESEARCHASSISTANT2};
    public static final List<Role> list = Arrays.asList(members);
    
    public static final Map<Integer, String> studyRoleMap = new LinkedHashMap<>();
    static {
        studyRoleMap.put(2, "Study_Coordinator");
        studyRoleMap.put(3, "Study_Director");
        studyRoleMap.put(4, "Investigator");
        studyRoleMap.put(5, "Data_Entry_Person");
        studyRoleMap.put(6, "Monitor");
           }

    public static final Map<Integer, String> siteRoleMap = new LinkedHashMap<>();
    static {
        siteRoleMap.put(2, "site_Study_Coordinator");
        siteRoleMap.put(3, "site_Study_Director");
        siteRoleMap.put(4, "site_investigator");
        siteRoleMap.put(5, "site_Data_Entry_Person");
        siteRoleMap.put(6, "site_monitor");
        siteRoleMap.put(7, "site_Data_Entry_Person2");
    }

    private List<Privilege> privileges;

    private Role(int id, String name, String description, Privilege[] myPrivs) {
        super(id, name, description);
        // privileges = Arrays.asList(myPrivs);
    }

    private Role() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static Role get(int id) {
    	Optional<Role> role = list.stream().filter(r -> r.getId() == id).findFirst();
    	return role.orElse(new Role());
    }

    public static Role getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            Role temp = (Role) list.get(i);
            if (temp.getName().equals(name) || temp.name.equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }

    public static ArrayList<Role> toArrayList() {
        return new ArrayList<Role>(list);
    }

    public boolean hasPrivilege(Privilege p) {
        for(Privilege myPriv : privileges) {
            if (myPriv.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implicitly orders the Role set in the following way:
     * <ul>
     * <li> null is the lowest possible Role
     * <li> INVALID is the next lowest possible Role
     * <li> The max of two non-null, non-INVALID roles r1 and r2 is the role
     * with the lowest id.
     * </ul>
     *
     * @param r1
     * @param r2
     * @return The maximum of (r1, r2).
     */
    public static Role max(Role r1, Role r2) {
        if (r1 == null) {
            return r2;
        }
        if (r2 == null) {
            return r1;
        }
        if (r1 == INVALID) {
            return r2;
        }
        if (r2 == INVALID) {
            return r1;
        }

        if (r1.getId() < r2.getId()) {
            return r1;
        }
        return r2;
    }
}
