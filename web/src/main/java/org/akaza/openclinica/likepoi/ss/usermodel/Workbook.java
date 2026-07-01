package org.akaza.openclinica.likepoi.ss.usermodel;  //derived from de.reliatec.likepoi.ss.usermodel
 
import java.io.Closeable;

/**
 * This mimics the apache poi interface, as far as used here.<br>
 * See: https://github.com/apache/poi/blob/trunk/poi/src/main/java/org/apache/poi/ss/usermodel/Workbook.java .
 */
public interface Workbook extends Closeable, Iterable<Sheet> {
    Sheet getSheetAt(int index);
    int getNumberOfSheets();
    String getSheetName(int index);
}
