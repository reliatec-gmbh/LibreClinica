/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Type safe enumeration of study group types
 *
 * @author Jun Xu
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class GroupClassType extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7593727578100651577L;
	public static final GroupClassType INVALID = new GroupClassType(0, "invalid");
    public static final GroupClassType ARM = new GroupClassType(1, "Arm");

    public static final GroupClassType FAMILY = new GroupClassType(2, "Family/Pedigree");

    public static final GroupClassType DEMOGRAPHIC = new GroupClassType(3, "Demographic");

    public static final GroupClassType OTHER = new GroupClassType(4, "Other");

    private static final GroupClassType[] members = { ARM, FAMILY, DEMOGRAPHIC, OTHER };

    public static final List<GroupClassType> list = Arrays.asList(members);

    private GroupClassType(int id, String name) {
        super(id, name);
    }

    private GroupClassType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static GroupClassType get(int id) {
    	Optional<GroupClassType> optional = list.stream().filter(t -> new Term(id, "").equals(t)).findFirst();
    	GroupClassType result = optional.orElse(new GroupClassType());
    	
    	if(!result.isActive()) {
    		return INVALID;
    	} else {
    		return result;
    	}
    }

    public static boolean findByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            GroupClassType temp = (GroupClassType) list.get(i);
            if (temp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static GroupClassType getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            GroupClassType temp = (GroupClassType) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return GroupClassType.INVALID;
    }

    public static ArrayList<GroupClassType> toArrayList() {
        return new ArrayList<GroupClassType>(list);
    }

}
