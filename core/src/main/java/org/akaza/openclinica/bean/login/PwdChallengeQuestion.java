/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.akaza.openclinica.bean.core.Term;

/**
 * @author jxu
 * @version CVS: $Id: PwdChallengeQuestion.java 9771 2007-08-28 15:26:26Z
 *          thickerson $
 *
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class PwdChallengeQuestion extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3257460938361987325L;

	public static final PwdChallengeQuestion MAIDEN_NAME = new PwdChallengeQuestion(1, "Mother_Maiden_Name");

    public static final PwdChallengeQuestion FARORITE_PET = new PwdChallengeQuestion(2, "Favorite_Pet");

    public static final PwdChallengeQuestion CITY_OF_BIRTH = new PwdChallengeQuestion(3, "City_of_Birth");

    private static final PwdChallengeQuestion[] members = { MAIDEN_NAME, FARORITE_PET, CITY_OF_BIRTH };

    public static final List<PwdChallengeQuestion> list = Arrays.asList(members);

    private PwdChallengeQuestion(int id, String name) {
        super(id, name);
    }

    private PwdChallengeQuestion() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static PwdChallengeQuestion get(int id) {
    	Optional<PwdChallengeQuestion> o = list.stream().filter(r -> r.getId() == id).findFirst();
    	return o.orElse(new PwdChallengeQuestion());
    }

    public static ArrayList<PwdChallengeQuestion> toArrayList() {
        return new ArrayList<>(list);
    }

}
