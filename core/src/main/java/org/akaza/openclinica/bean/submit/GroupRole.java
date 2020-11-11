/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jxu
 * @deprecated
 */
@Deprecated
public class GroupRole extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8639072725087871745L;
	public static final GroupRole INVALID = new GroupRole(0, "invalid");
    public static final GroupRole PROBAND = new GroupRole(1, "proband");

    private static final GroupRole[] members = { PROBAND };

    public static final List list = Arrays.asList(members);

    private GroupRole(int id, String name) {
        super(id, name);
    }

    private GroupRole() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static GroupRole get(int id) {
        return (GroupRole) Term.get(id, list);
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    @Override
    public String getName() {
        return name;
    }

}
