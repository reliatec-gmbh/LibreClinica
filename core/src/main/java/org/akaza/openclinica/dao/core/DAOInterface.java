/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.exception.OpenClinicaException;

/**
 * DAOInterface.java, created to enforce several methods in our EntityDAO and
 * AuditableEntityDAO framework. Note that we have to enforce them as basic
 * objects, since we will be using them across all DAOs. This is the spot for
 * adding required classes such as update() insert() and other selects().
 *
 * @author thickerson
 *
 *
 */
public interface DAOInterface<T> {
    // problem here is to prevent beans which recursively access themselves;
    // if we don't have a special boolean, the user account bean will recurse
    // until
    // the virtual machine runs out of memory, looking for its owner of its
    // owner.
    T getEntityFromHashMap(HashMap<String, Object> hm);

    ArrayList<T> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException;

    ArrayList<T> findAll() throws OpenClinicaException;

    EntityBean findByPK(int id) throws OpenClinicaException;

    T create(T eb) throws OpenClinicaException;

    T update(T eb) throws OpenClinicaException;

    ArrayList<T> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException;

    ArrayList<T> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException;
    // perhaps also add one with just object and int????

    void setTypesExpected();
}
