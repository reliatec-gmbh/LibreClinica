/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.core;

import org.apache.commons.dbcp2.BasicDataSource;

// TODO: this is candidate for removal, and use simply BasicDataSource directly
// TODO: I think BigStringTryClob was only relevant for Oracle
public class ExtendedBasicDataSource extends BasicDataSource {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }
    
}
