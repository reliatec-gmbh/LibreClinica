/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.managestudy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.akaza.openclinica.bean.core.Term;

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class StudyType extends Term {

    /**
	 * 
	 */
	private static final long serialVersionUID = 9061803693706806754L;

	public static final StudyType INVALID = new StudyType(0, "");

    public static final StudyType GENETIC = new StudyType(1, "genetic");

    public static final StudyType NONGENETIC = new StudyType(2, "non_genetic");

    private static final StudyType[] members = { GENETIC, NONGENETIC };

    public static final List<StudyType> list = Arrays.asList(members);

    private StudyType(int id, String name) {
        super(id, name);
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static StudyType get(int id) {
    	Optional<StudyType> o = list.stream().filter(r -> r.getId() == id).findFirst();
    	StudyType t = o.orElse(StudyType.INVALID);
    	
        if (!t.isActive()) {
            return StudyType.INVALID;
        }
        return t;
    }

    public static ArrayList<StudyType> toArrayList() {
        return new ArrayList<>(list);
    }

}
