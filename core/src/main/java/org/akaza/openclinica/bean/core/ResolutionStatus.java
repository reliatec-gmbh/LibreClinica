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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A type-safe enumeration class for resolution status of discrepancy notes
 *
 * @author Jun Xu
 *
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class ResolutionStatus extends Term {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1667893627441880526L;

	protected static final Logger logger = LoggerFactory.getLogger(ResolutionStatus.class.getName());

    public static final ResolutionStatus INVALID = new ResolutionStatus(0, "invalid", null, null);

    public static final ResolutionStatus OPEN = new ResolutionStatus(1, "New", null, "images/icon_Note.gif");

    public static final ResolutionStatus UPDATED = new ResolutionStatus(2, "Updated", null, "images/icon_flagYellow.gif");

    public static final ResolutionStatus RESOLVED = new ResolutionStatus(3, "Resolution_Proposed", null, "images/icon_flagGreen.gif");

    public static final ResolutionStatus CLOSED = new ResolutionStatus(4, "Closed", null, "images/icon_flagBlack.gif");

    public static final ResolutionStatus NOT_APPLICABLE = new ResolutionStatus(5, "Not_Applicable", null, "images/icon_flagWhite.gif");

    private String iconFilePath;

    public boolean isInvalid() {
        return this == ResolutionStatus.INVALID;
    }

    public boolean isOpen() {
        return this == ResolutionStatus.OPEN;
    }

    public boolean isClosed() {
        return this == ResolutionStatus.CLOSED;
    }

    public boolean isUpdated() {
        return this == ResolutionStatus.UPDATED;
    }

    public boolean isResolved() {
        return this == ResolutionStatus.RESOLVED;
    }

    public boolean isNotApplicable() {
        return this == ResolutionStatus.NOT_APPLICABLE;
    }

    private static final ResolutionStatus[] members = { OPEN, UPDATED, RESOLVED, CLOSED, NOT_APPLICABLE };

    public static ResolutionStatus[] getMembers() {
        return members;
    }

    public static final List<ResolutionStatus> list = Arrays.asList(members);

    private List<Privilege> privileges;

    private ResolutionStatus(int id, String name, Privilege[] myPrivs, String path) {
        super(id, name);
        this.iconFilePath = path;
    }

    private ResolutionStatus() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static ResolutionStatus get(int id) {
        Optional<ResolutionStatus> result = list.stream().filter(t -> new Term(id, "").equals(t)).findFirst();
        return result.orElse(new ResolutionStatus());
    }

    public static ResolutionStatus getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            ResolutionStatus temp = list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }

    public boolean hasPrivilege(Privilege p) {
        Iterator<Privilege> it = privileges.iterator();

        while (it.hasNext()) {
            Privilege myPriv = (Privilege) it.next();
            if (myPriv.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public String getIconFilePath() {
        return iconFilePath;
    }

    public void setIconFilePath(String iconFilePath) {
        this.iconFilePath = iconFilePath;
    }
}
