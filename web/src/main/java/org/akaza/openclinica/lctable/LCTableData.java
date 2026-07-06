/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.lctable;

import java.util.List;


public class LCTableData<T> {

    public final List<T> pageItems;          // data items (rows) in current page
    public final int totalCountWithFilter;   // total number in rows matching filter in the whole dataset (not only in current page)

    public LCTableData(List<T> pageItems, int totalCountWithFilter) {
        this.pageItems = pageItems;
        this.totalCountWithFilter = totalCountWithFilter;
    }

}
