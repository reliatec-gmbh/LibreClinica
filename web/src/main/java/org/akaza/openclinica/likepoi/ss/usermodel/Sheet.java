package org.akaza.openclinica.likepoi.ss.usermodel;  //derived from de.reliatec.likepoi.ss.usermodel

public interface Sheet extends Iterable<Row> {
    String getSheetName();
    Row getRow(int rowIndex);
    int getLastRowNum();
    @Deprecated //leads to problems with empty rows in ods
    int getPhysicalNumberOfRows();
}
