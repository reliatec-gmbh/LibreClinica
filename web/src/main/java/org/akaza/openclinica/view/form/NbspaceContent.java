/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view.form;

import org.jdom.Content;

/**
 * This class has the sole purpose of creating a non-breaking space character
 * for inside a textarea tag, in order to force JDOM to create a non-empty tag
 * (&lt;textarea> &lt;/textarea>, as opposed to &lt;textarea />).
 */
public class NbspaceContent extends Content {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4100153822124490389L;

	@Override
    public String getValue() {
        return "&nbsp;";
    }
}
