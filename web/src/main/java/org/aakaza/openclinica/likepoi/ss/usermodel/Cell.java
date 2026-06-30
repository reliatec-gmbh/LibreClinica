package org.aakaza.openclinica.likepoi.ss.usermodel;

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
