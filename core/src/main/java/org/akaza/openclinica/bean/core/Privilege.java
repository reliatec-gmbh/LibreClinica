/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class Privilege extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = 613513351441734881L;
	public static final Privilege ADMIN = new Privilege(1, "admin");
    public static final Privilege STUDYDIRECTOR = new Privilege(2, "director");
    public static final Privilege INVESTIGATOR = new Privilege(3, "investigator");
    public static final Privilege RESEARCHASSISTANT = new Privilege(4, "ra");
    public static final Privilege MONITOR = new Privilege(5, "monitor");
    public static final Privilege RESEARCHASSISTANT2 = new Privilege(6, "ra2");

    private static final Privilege[] members = { ADMIN, STUDYDIRECTOR, INVESTIGATOR, RESEARCHASSISTANT, MONITOR,RESEARCHASSISTANT2 };
    public static final List<Privilege> list = Arrays.asList(members);

    private Privilege(int id, String name) {
        super(id, name);
    }

    private Privilege() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static Privilege get(int id) {
    	Optional<Privilege> o = list.stream().filter(r -> r.getId() == id).findFirst();
    	return o.orElse(new Privilege());
    }

    public static ArrayList<Privilege> toArrayList() {
        return new ArrayList<>(list);
    }
}
