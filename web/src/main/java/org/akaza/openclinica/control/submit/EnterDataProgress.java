/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.submit;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: Dec 1, 2007 Time: 10:40:57
 * AM To change this template use File | Settings | File Templates.
 */
public interface EnterDataProgress {
    boolean getSectionVisited(int sectionNumber, int eventCRFId);

    void setSectionVisited(int eventCRFId, int sectionNumber, boolean hasVisited);
}
