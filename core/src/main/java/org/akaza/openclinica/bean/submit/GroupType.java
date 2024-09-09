/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.akaza.openclinica.bean.core.Term;

/**
 * @author jxu, modified by ywang
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class GroupType extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1786119573009877427L;
	public static final GroupType INVALID = new GroupType(0, "invalid");
    // YW 08-19-2007 << modification is made here to match the updated database
    // table group_class_types
    // although this class has not been used at this time.
    // public static final GroupType TREATMENT = new GroupType(1, "treatment");
    // public static final GroupType CONTROL = new GroupType(1, "control");
    public static final GroupType ARM = new GroupType(1, "Arm");
    public static final GroupType FAMILY = new GroupType(2, "Family/Pedigree");
    public static final GroupType DEMOGRAPHIC = new GroupType(3, "Demographic");
    public static final GroupType OTHER = new GroupType(4, "Other");
    // private static final GroupType[] members = {TREATMENT,CONTROL};
    private static final GroupType[] members = { ARM, FAMILY, DEMOGRAPHIC, OTHER };
    // YW >>

    public static final List<GroupType> list = Arrays.asList(members);

    private GroupType(int id, String name) {
        super(id, name);
    }

    private GroupType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static GroupType get(int id) {
    	Optional<GroupType> groupRole = list.stream().filter(r -> r.getId() == id).findFirst();
    	return groupRole.orElse(new GroupType());
    }

    public static ArrayList<GroupType> toArrayList() {
        return new ArrayList<>(list);
    }

}
