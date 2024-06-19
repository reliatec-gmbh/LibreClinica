/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.submit;

import java.util.Comparator;

/**
 * @author ssachs
 */
public class ChildDisplayItemBeanComparator implements Comparator<DisplayItemBean> {
    private static ChildDisplayItemBeanComparator instance = null;

    private ChildDisplayItemBeanComparator() {

    }

    public static ChildDisplayItemBeanComparator getInstance() {
        if (instance == null) {
            instance = new ChildDisplayItemBeanComparator();
        }
        return instance;
    }

    /**
     * Sorts DisplayItemBean objects first by column number, then by ordinal.
     * Should only be used on DisplayItemBean objects which correspond to child
     * items, that is, items with parentId != 0.
     *
     * @param o1
     *            The first obje
     *
     */
    public int compare(DisplayItemBean o1, DisplayItemBean o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }

        if (!o1.getClass().equals(o2.getClass())) {
            return 0;
        }

        if (!o1.getClass().equals(DisplayItemBean.class)) {
            return 0;
        }

        int column1 = o1.getMetadata().getColumnNumber();
        int column2 = o2.getMetadata().getColumnNumber();

        int ordinal1 = o1.getMetadata().getOrdinal();
        int ordinal2 = o2.getMetadata().getOrdinal();

        return column1 != column2 ? column1 - column2 : ordinal1 - ordinal2;
    }

}
