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

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class EntityAction extends Term {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2965559245827075121L;
	public static final EntityAction VIEW = new EntityAction(1, "view");
    public static final EntityAction EDIT = new EntityAction(2, "edit");
    public static final EntityAction DELETE = new EntityAction(3, "delete");
    public static final EntityAction RESTORE = new EntityAction(4, "restore");
    public static final EntityAction DEPLOY = new EntityAction(5, "deploy");

    private static final EntityAction[] members = { VIEW, EDIT, DELETE, RESTORE, DEPLOY };
    public static final List<EntityAction> list = Arrays.asList(members);

    private EntityAction(int id, String name) {
        super(id, name);
    }

    private EntityAction() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static EntityAction get(int id) {
    	Optional<EntityAction> result = list.stream().filter(t -> new Term(id, "").equals(t)).findFirst();
    	return result.orElse(new EntityAction());
    }
}
