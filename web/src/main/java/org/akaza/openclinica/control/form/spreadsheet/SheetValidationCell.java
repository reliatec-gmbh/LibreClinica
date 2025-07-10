/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.form.spreadsheet;

/**
 * <p>Class contains final SheetValidationType initialized with Constructor.</p>
 * ywang (Aug., 2011)
 */
public class SheetValidationCell {
    private final SheetValidationType type;
    private SheetArgumentCell sheetArgumentCell;

    /**
     * SheetValidationType has been set as NONE.
     * @param sheetCell
     */
    public SheetValidationCell(SheetCell sheetCell) {
        this.type = SheetValidationType.NONE;
        this.sheetArgumentCell = new SheetArgumentCell(sheetCell);
    }

    public SheetValidationCell(SheetValidationType sheetValidationType, SheetCell sheetCell) {
        this.type = sheetValidationType;
        this.sheetArgumentCell = new SheetArgumentCell(sheetCell);
    }

    public SheetArgumentCell getSheetArgumentCell() {
        return sheetArgumentCell;
    }

    public void setSheetArgumentCell(SheetArgumentCell sheetArgumentCell) {
        this.sheetArgumentCell = sheetArgumentCell;
    }

    public SheetValidationType getType() {
        return type;
    }
}