package org.akaza.openclinica.likepoi.ss.usermodel;   //derived from de.reliatec.likepoi.ss.usermodelw

public interface Cell {
    CellType getCellType();
    String getStringCellValue();
    double getNumericCellValue();
    boolean getBooleanCellValue();
    int getRowIndex();
    int getColumnIndex();
    String getCellFormula();
    java.util.Date getDateCellValue();
}
