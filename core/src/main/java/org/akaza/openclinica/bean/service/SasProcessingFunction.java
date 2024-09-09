/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.service;

/**
 * SasProcessingFunction, a post-processing function for Extract Data
 * By Tom Hickerson, 09/2010
 * @author thickerson
 *
 */
public class SasProcessingFunction extends ProcessingFunction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8857284892637732961L;

	public SasProcessingFunction() {
        fileType = "sas";
    }
    
    public ProcessingResultType run() {
        return null;
    }
    
}
