/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller;

/**
 * This class defines a series of constants for use in declaring whether the various
 * boxes in the sidebar (see sidebar.tag and decorator.jsp) should be initially
 * shown as closed or open.
 * Date: Jan 14, 2009
 * Time: 11:50:59 AM
 */
public enum SidebarEnumConstants {
    OPENALERTS,
    CLOSEALERTS,
    OPENINSTRUCTIONS,
    CLOSEINSTRUCTIONS,
    OPENINFO,
    CLOSEINFO,
    SHOWICONS,
    DISABLEICONS
}
