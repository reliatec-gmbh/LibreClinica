/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 *
 * Created on Jul 11, 2005
 */
package org.akaza.openclinica.bean.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author ssachs
 */
public abstract class ReportBean<I> {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected ArrayList<ArrayList<String>> metadata; // header block - includes SED CRF codes
    protected ArrayList<ArrayList<String>> data;
    protected ArrayList<I> items; // items in the database
    private boolean metadataClosed;
    protected ArrayList<String> currentRow;

    public ReportBean() {
        metadata = new ArrayList<>();
        data = new ArrayList<>();
        currentRow = new ArrayList<>();
        metadataClosed = false;
        items = new ArrayList<>();
    }

    @Override
    public abstract String toString();

    public void nextCell(String value) {
        currentRow.add(value);
    }

    public void nextRow() {
        logger.info("*** current row count: " + currentRow.size());
        if (!metadataClosed) {
            metadata.add(currentRow);
        } else {
            data.add(currentRow);
        }

        currentRow = new ArrayList<>();
    }

    public void closeMetadata() {
        if (currentRow.size() > 0) {
            metadata.add(currentRow);
        }
        currentRow = new ArrayList<>();
        metadataClosed = true;
    }

    protected String getDataColumnEntry(int col, int rowNum) {
        if (data.size() > rowNum) {
            ArrayList<String> row = data.get(rowNum);

            if (row != null && row.size() > col) {
                String s = (String) row.get(col);

                if (s != null) {
                    return s;
                }
            }
        }

        return "";
    }

    /**
     * @return Returns the items.
     */
    public ArrayList<I> getItems() {
        return items;
    }

    /**
     * @param items
     *            The items to set.
     */
    public void setItems(ArrayList<I> items) {
        this.items = items;
    }
}
