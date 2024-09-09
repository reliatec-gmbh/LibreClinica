/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.core;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// implicit controlled vocab, not stored in db

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class NumericComparisonOperator extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8976624823346368833L;
	public static final NumericComparisonOperator EQUALS = new NumericComparisonOperator(1, "equal_to", "equal_to");
    public static final NumericComparisonOperator NOT_EQUALS = new NumericComparisonOperator(2, "not_equal_to", "not_equal_to");
    public static final NumericComparisonOperator LESS_THAN = new NumericComparisonOperator(3, "less_than", "less_than");
    public static final NumericComparisonOperator LESS_THAN_OR_EQUAL_TO = new NumericComparisonOperator(4, "less_than_or_equal_to", "less_than_or_equal_to");
    public static final NumericComparisonOperator GREATER_THAN = new NumericComparisonOperator(5, "greater_than", "greater_than");
    public static final NumericComparisonOperator GREATER_THAN_OR_EQUAL_TO =
        new NumericComparisonOperator(6, "greater_than_or_equal_to", "greater_than_or_equal_to");

    private static final NumericComparisonOperator[] members = { EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO };
    public static final List<NumericComparisonOperator> list = Arrays.asList(members);

    private NumericComparisonOperator(int id, String name) {
        super(id, name);
    }

    private NumericComparisonOperator(int id, String name, String description) {
        super(id, name, description);
    }

    private NumericComparisonOperator() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static NumericComparisonOperator get(int id) {
    	Optional<NumericComparisonOperator> o = list.stream().filter(r -> r.getId() == id).findFirst();
    	return o.orElse(new NumericComparisonOperator());
    }
}
