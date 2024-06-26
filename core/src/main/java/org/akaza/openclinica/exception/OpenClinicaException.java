/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * 
 * @author thickerson
 */
public class OpenClinicaException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1690013927444642305L;
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public StringBuffer logInfo = new StringBuffer("----> ");

    public String errorID;
    public String className;
    public String message;

    /** Creates a new instance of OpenClinicaException */
    public OpenClinicaException(String message, String type, String methodName, String className, String errorid) {
        if (message == null)
            message = "< No Message >";
        this.message = message;
        if (type == null)
            type = "< No Type >";
        if (methodName == null)
            methodName = "< No Method Name >";
        if (className == null)
            className = "<No Class Name >";
        if (errorid == null)
            errorid = "<No Error ID >";
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        java.util.Date todayDate = new java.util.Date();
        String pDate = df.format(todayDate);
        logInfo.append("Timestamp: ").append(pDate);
        logInfo.append(" Message Info: ").append(message).append("; Type Info: ").append(type);
        logInfo.append("; Method Info: ").append(methodName).append("; Class Info: ").append(className);
        errorID = errorid;
        logger.info(logInfo.toString());
    }

    public OpenClinicaException(String message, String errorid) {
        if (message == null)
            message = "< No Message >";
        this.message = message;
        errorID = errorid;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        java.util.Date todayDate = new java.util.Date();
        String pDate = df.format(todayDate);
        logInfo.append("Timestamp: ").append(pDate);
        logInfo.append(" Message Info: ").append(message);
        logger.info(logInfo.toString());
    }

    public void addInfo(String type, String methodName, String className) {
        if (type == null)
            type = "< No Type >";
        if (methodName == null)
            methodName = "< No Method Name >";
        if (className == null)
            className = "<No Class Name >";
        logInfo.append(" --> propagated to: Type ").append(type).append("; Method: ").append(methodName);
        logInfo.append("; Class: ").append(className);
        logger.info(logInfo.toString());
    }

    public String getOpenClinicaMessage() {
        return message;
    }

    public void setOpenCinicaMessage(String m) {
        this.message = m;
    }

}
