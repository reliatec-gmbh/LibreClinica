/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.akaza.openclinica.bean.core.Privilege;
import org.akaza.openclinica.bean.core.Term;

/**
 * BL - Boolean
 *
 * BN  BooleanNonNull
 *
 * ED  Encapsulated Data (Files w/ specified MIME type e.g. ED-pdf, ED-jpg
 * defined in a separate ED Types table in the future)
 *
 * TEL - A telecommunication address (TEL), such as a URL for HTTP or FTP, which
 * will resolve to precisely the same binary data that could as well have been
 * provided as inline data.
 *
 * ST  Character String
 *
 * INT  Integer
 *
 * REAL  Floating
 *
 * SET - a value that contains other distinct values in no particular order.
 *
 * @author Jun Xu
 * @deprecated
 */
@Deprecated
public class DataType extends Term {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6282835406766655030L;
	public static final DataType INVALID = new DataType(0, "INVALID", null);
    public static final DataType BN = new DataType(1, "BN", null);
    public static final DataType ED = new DataType(2, "ED", null);
    public static final DataType TEL = new DataType(3, "TEL", null);
    public static final DataType ST = new DataType(4, "ST", null);
    public static final DataType INT = new DataType(5, "INT", null);
    public static final DataType REAL = new DataType(6, "REAL", null);
    public static final DataType SET = new DataType(7, "SET", null);

    private static final DataType[] members = { BN, ED, TEL, ST, INT, REAL, SET };

    public static final List<DataType> list = Arrays.asList(members);

    private DataType(int id, String name, Privilege[] myPrivs) {
        super(id, name);
    }

    private DataType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static DataType get(int id) {
    	Optional<DataType> result = list.stream().filter(t -> new Term(id, "").equals(t)).findFirst();
    	return result.orElse(new DataType());
    }

    public static DataType getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            DataType temp = (DataType) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return new DataType();
    }

    public static boolean findByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            DataType temp = (DataType) list.get(i);
            if (temp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<DataType> toArrayList() {
        return new ArrayList<DataType>(list);
    }

    @Override
    public String getName() {
        return name;
    }
}
