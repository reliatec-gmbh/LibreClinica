package org.akaza.openclinica.likepoi.ss.usermodel; //derived from de.reliatec.likepoi.ss.usermodel

public interface Row extends Iterable<Cell> {
    Cell getCell(int cellNum);
    int getRowNum();
    short getLastCellNum();
}
