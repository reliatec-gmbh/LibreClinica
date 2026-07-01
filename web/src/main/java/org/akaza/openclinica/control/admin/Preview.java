/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.admin;



import java.util.Map;

import org.akaza.openclinica.likepoi.ss.usermodel.Workbook;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: Jun 15, 2007
 *
 */
public interface Preview {
	@SuppressWarnings("rawtypes")
	Map<String, Map> createCrfMetaObject(Workbook workbook);

    Map<Integer, Map<String, String>> createItemsOrSectionMap(Workbook workbook, String itemsOrSection);

    Map<Integer, Map<String, String>> createGroupsMap(Workbook workbook);

    Map<String, String> createCrfMap(Workbook workbook);
}
