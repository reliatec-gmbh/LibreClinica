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

public class DatasetItemStatus extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3460188291953236215L;
	public static final DatasetItemStatus COMPLETED = new DatasetItemStatus(1, "completed", "completed_items");
    public static final DatasetItemStatus NONCOMPLETED = new DatasetItemStatus(2, "non_completed", "non_completed_items");
    public static final DatasetItemStatus COMPLETED_AND_NONCOMPLETED =
        new DatasetItemStatus(3, "completed_and_non_completed", "completed_and_non_completed_items");

    private static final DatasetItemStatus[] members = { COMPLETED, NONCOMPLETED, COMPLETED_AND_NONCOMPLETED };
    private static List<DatasetItemStatus> list = Arrays.asList(members);

    private DatasetItemStatus(int id, String name, String description) {
        super(id, name, description);
    }

    private DatasetItemStatus() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static DatasetItemStatus get(int id) {
    	Optional<DatasetItemStatus> result = list.stream().filter(t -> new Term(id, "").equals(t)).findFirst();
    	return result.orElse(new DatasetItemStatus());
    }

    public static ArrayList<DatasetItemStatus> toArrayList() {
        return new ArrayList<DatasetItemStatus>(list);
    }

    public static List<DatasetItemStatus> getList() {
        return list;
    }

    public static void setList(List<DatasetItemStatus> list) {
        DatasetItemStatus.list = list;
    }

    public static DatasetItemStatus getCOMPLETED() {
        return COMPLETED;
    }

    public static DatasetItemStatus getNONCOMPLETED() {
        return NONCOMPLETED;
    }

    public static DatasetItemStatus getCOMPLETEDANDNONCOMPLETED() {
        return COMPLETED_AND_NONCOMPLETED;
    }

    public static DatasetItemStatus[] getMembers() {
        return members;
    }
}
