package org.aakaza.openclinica.likepoi.ss.usermodel;

public interface Row extends Iterable<Cell> {
    Cell getCell(int cellNum);
    int getRowNum();
    short getLastCellNum();
}
