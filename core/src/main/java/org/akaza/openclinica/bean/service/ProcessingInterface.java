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
 * ProcessingFunction, by Tom Hickerson, 09/2010
 * Interface of the pre- and post-processing functions determined by the
 * new extract data interface
 * Currently only implementing one post-processing function per XSL type
 * Future implementations will be with multiple pre- and post-functions
 * @author thickerson
 *
 */
public interface ProcessingInterface {
    ProcessingResultType run();

}
