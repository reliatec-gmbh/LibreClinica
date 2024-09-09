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

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class UserType extends Term {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8795365475283947302L;
	public static final UserType INVALID = new UserType(0, "invalid");
    public static final UserType SYSADMIN = new UserType(1, "business_administrator");
    public static final UserType USER = new UserType(2, "user");
    public static final UserType TECHADMIN = new UserType(3, "technical_administrator");

    private static final UserType[] members = { INVALID, USER, SYSADMIN, TECHADMIN };
    public static final List<UserType> list = Arrays.asList(members);

    private UserType(int id, String name) {
        super(id, name);
    }

    private UserType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static UserType get(int id) {
    	Optional<UserType> userType = list.stream().filter(u -> u.getId() == id).findFirst();
    	return userType.orElse(new UserType());
    }

    public static ArrayList<UserType> toArrayList() {
        return new ArrayList<>(list);
    }
}
