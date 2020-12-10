/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for overcoming class cast warnings when accessing attributes from the request.
 * 
 * @author Christian H&auml;nsel
 *
 */
public class ClassCastHelper {

	/**
	 * Returns the value of the named attribute as the given result type,
     * or <code>null</code> if no attribute of the given name exists. 
     * 
	 * @param <T> the expected result type
	 * @param request the {@link HttpServletRequest} that holds the attributes
     *
     * @param name	a <code>String</code> specifying the name of 
     *			the attribute
     *
	 * @param resultType the expected result type
     *
     * @return		an <code>Object</code> containing the value 
     *			of the attribute, or <code>null</code> if
     *			the attribute does not exist
     * @see HttpServletRequest#getAttribute(String)
	 */
    @SuppressWarnings("unchecked")
	public static <T> T getAttribute(HttpServletRequest request, String name, Class<T> resultType) {    	
        return (T) request.getAttribute(name);
    }

	/**
	 * Returns the value of the named attribute as the given result type. 
     * 
	 * @param <T> the expected result type
	 * @param the object to cast
     *
	 * @param resultType the expected result type
     *
     * @return	the object casted to the given result type
     * @see HttpServletRequest#getAttribute(String)
	 */    
	@SuppressWarnings("unchecked")
	public static <T> T getAsType(Object o, Class<T> resultType) {   	
        return (T) o;
    }

	/**
	 * Returns the value of the named attribute as an ArrayList with the given result type,
     * or <code>null</code> if no attribute of the given name exists. 
     * 
	 * @param <T> the expected result type
	 * @param request the {@link HttpServletRequest} that holds the attributes
     *
     * @param name	a <code>String</code> specifying the name of 
     *			the attribute
     *
	 * @param resultType the expected result type
     *
     * @return		an <code>ArrayList</code> containing the value 
     *			of the attribute, or <code>null</code> if
     *			the attribute does not exist
     * @see HttpServletRequest#getAttribute(String)
	 */
	public static <T> ArrayList<T> getAttributeAsList(HttpServletRequest request, String name, Class<T> resultType) {    	
        return asArrayList(request.getAttribute(name), resultType);
    }

	/**
	 * Returns the value of the named attribute as an ArrayList with the given result type,
     * or <code>null</code> if no attribute of the given name exists. 
     * 
	 * @param <T> the expected result type
	 * @param the object to cast
     *
	 * @param resultType the expected result type
     *
     * @return		an <code>Enumeration</code>
     * @see HttpServletRequest#getAttribute(String)
	 */
    @SuppressWarnings("unchecked")
	public static <T> ArrayList<T> asArrayList(Object o, Class<T> resultType) {
    	ArrayList<T> result = null;
    	if(o instanceof ArrayList<?>) {
    		result = (ArrayList<T>) o;
    	} else if(o instanceof Collection<?>) {
    		result = new ArrayList<>((Collection<T>)o);
    	} else {
    		result = (ArrayList<T>) o;
    	}
        return result;
    }

	/**
	 * Returns the value of the named attribute as an Enumeration with the given result type,
     * or <code>null</code> if no attribute of the given name exists. 
     * 
	 * @param <T> the expected result type
	 * @param the object to cast
     *
	 * @param resultType the expected result type
     *
     * @return		an <code>Enumeration</code>
     * @see HttpServletRequest#getAttribute(String)
	 */
    @SuppressWarnings("unchecked")
	public static <T> Enumeration<T> asEnumeration(Object o, Class<T> resultType) {    	
        return (Enumeration<T>) o;
    }

	/**
	 * Returns the value of the named attribute as an HashMap with the given result type,
     * or <code>null</code> if no attribute of the given name exists. 
     * 
	 * @param <T> the expected key type
	 * @param <V> the expected value type
	 * @param the object to cast
     *
	 * @param keyType the expected type of the map key
	 * @param keyType the expected type of the map value
     *
     * @return		an <code>HashMap</code>
     * @see HttpServletRequest#getAttribute(String)
	 */
    @SuppressWarnings("unchecked")
	public static <T, V> HashMap<T, V> asHashMap(Object o, Class<T> keyType, Class<V> valueType) {    	
        return (HashMap<T, V>) o;
    }
}
