package org.akaza.openclinica.lctable;

import java.util.List;


public class LCTableData<T> {

    public final List<T> pageItems;          // data items (rows) in current page
    public final int totalCountWithFilter;   // total number in rows matching filter in the whole dataset (not only in current page)

    // Convenience constructor used by SSR adapters to wrap service DtResponse into LCTableData
    public LCTableData(List<T> pageItems, int totalCountWithFilter) {
        this.pageItems = pageItems;
        this.totalCountWithFilter = totalCountWithFilter;
    }

}
