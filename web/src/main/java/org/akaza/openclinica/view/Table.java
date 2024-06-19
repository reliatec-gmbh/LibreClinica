/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.control.form.FormProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Table {
    public static final int NUM_ROWS_PER_PAGE = 10;

    protected ArrayList<EntityBean> rows; // an array of Entities
    protected ArrayList<String> columns; // an array of Strings which are column
    // headings, setup during initialization
    protected int numColumns; // provided for convenience in showTable class;
    // always equals columns.size()

    protected int currPageNumber; // which page are we viewing now?
    protected int totalPageNumbers; // how many page numbers are there total?
    // always equal to ceil(rows.size() /
    // NUM_ROWS_PER_PAGE)
    protected int sortingColumnInd; // index into arrColumns of the Column were
    // current sorting by
    protected boolean ascendingSort; // true for ascending sort, false
    // otherwise
    protected boolean filtered; // true if we use the keyword filter, false
    // otherwise
    protected String keywordFilter; // String the user wants to search for among
    // the rows

    protected String postAction;
    protected HashMap<String, String> postArgs;
    protected String baseGetQuery;

    protected String noRowsMessage;
    protected String noColsMessage;

    public Table() {
        rows = new ArrayList<>();
        columns = new ArrayList<>();
        numColumns = 0;

        currPageNumber = 0;
        totalPageNumbers = 0;
        sortingColumnInd = 0;
        ascendingSort = true;
        filtered = false;
        keywordFilter = "";

        postAction = "";
        postArgs = new HashMap<>();
        baseGetQuery = "";

        noRowsMessage = "";
        noColsMessage = "";
    }

    /**
     * @return Returns the totalPageNumbers.
     */
    public int getTotalPageNumbers() {
        return totalPageNumbers;
    }

    /**
     * @return Returns the columns.
     */
    public ArrayList<String> getColumns() {
        return columns;
    }

    /**
     * @param columns
     *            The columns to set.
     */
    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
        numColumns = columns.size();
    }

    /**
     * @return Returns the ascendingSort.
     */
    public boolean isAscendingSort() {
        return ascendingSort;
    }

    /**
     * @return Returns the currPageNumber.
     */
    public int getCurrPageNumber() {
        return currPageNumber;
    }

    /**
     * @return Returns the keywordFilter.
     */
    public String getKeywordFilter() {
        return keywordFilter;
    }

    /**
     * @return Returns the rows.
     */
    public ArrayList<EntityBean> getRows() {
        return rows;
    }

    /**
     * Re-computes the correct value of page numbers as:
     * <code>rows.size() / NUM_ROWS_PER_PAGE</code>
     */
    private void updateTotalPageNumbers() {
        totalPageNumbers = rows.size() / NUM_ROWS_PER_PAGE;
    }

    /**
     *
     * @param rows
     */
    public void setRows(ArrayList<EntityBean> rows) {
        this.rows = rows;
        updateTotalPageNumbers();
    }

    /**
     * Adds an entity to display at the bottom of the table.
     *
     * @param e
     *            The entity to add to the table.
     */
    public void addRow(EntityBean e) {
        rows.add(e);
        updateTotalPageNumbers();
    }

    public void processGetQuery(FormProcessor fp) {

    }

    /**
     * @return Returns the filtered.
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * @return Returns the sortingColumnInd.
     */
    public int getSortingColumnInd() {
        return sortingColumnInd;
    }

    /**
     * @return Returns the numColumns.
     */
    public int getNumColumns() {
        return numColumns;
    }

    public void setQuery(String baseURL, HashMap<String, String> args) {
        postAction = baseURL;
        postArgs = args;

        baseGetQuery = baseURL + "?";
        baseGetQuery += FormProcessor.FIELD_SUBMITTED + "=" + 1;

        Iterator<String> it = args.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) args.get(key);
            // TODO: provide URL Encoding!
            baseGetQuery += "&" + key + "=" + value;
        }

    }

    /**
     * @return Returns the baseGetQuery.
     */
    public String getBaseGetQuery() {
        return baseGetQuery;
    }

    /**
     * @return Returns the postAction.
     */
    public String getPostAction() {
        return postAction;
    }

    /**
     * @return Returns the postArgs.
     */
    public HashMap<String, String> getPostArgs() {
        return postArgs;
    }

    /***************************************************************************
     * old functions from when we had view code in the Java
     **************************************************************************/

    /**
     * @deprecated
     * @return header
     */
    @Deprecated
    protected String showHeader() {
        String header = "<table border>\n";
        header += "<tr>\n";

        Iterator<String> columnsIt = columns.iterator();

        while (columnsIt.hasNext()) {
            String column = (String) columnsIt.next();
            header += "<td>" + column + "</td>\n";
        }

        header += "</tr>\n";

        return header;
    }

    /**
     * @deprecated
     * @return footer
     */
    @Deprecated
    protected String showFooter() {
        return "</table>\n";
    }

    /**
     * @deprecated
     * @param e
     */
    @Deprecated
    protected abstract String showRow(EntityBean e);

    /**
     * @deprecated
     */
    @Deprecated
    protected abstract String getEntitiesNamePlural();

    /**
     * @deprecated
     */
    @Deprecated
    public String showTable() {
        if (rows.size() <= 0) {
            return "<p><i>There are no " + getEntitiesNamePlural() + " available for display.</i></p>";
        }

        String table = showHeader();

        Iterator<EntityBean> rowsIt = rows.iterator();
        while (rowsIt.hasNext()) {
            EntityBean e = rowsIt.next();
            table += showRow(e);
        }

        table += showFooter();

        return table;
    }
}