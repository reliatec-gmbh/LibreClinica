/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.odmExport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.LinkedHashMap;

import org.junit.Test;

/**
 * JUnit test verifying some {@link MetadataUnit} class functionality.
 * 
 * @author thillger
 */
public class MetadataUnitTest {

    @Test
    public void testParseCodeStringStringSimple() throws Exception {
        String texts = ",Komplette Remission, Partielle Remission";
        String values = "0,1,3";

        LinkedHashMap<String, String> map = MetadataUnit.parseCode(texts, values);
        
        assertThat(map.size(), is(3));
        assertThat(map.get("0"), is(""));
        assertThat(map.get("1"), is("Komplette Remission"));
        assertThat(map.get("3"), is("Partielle Remission"));
    }

    @Test
    public void testParseCodeStringStringSimple2() throws Exception {
        String texts = " ,Komplette Remission, Partielle Remission ";
        String values = "0,1,3";

        LinkedHashMap<String, String> map = MetadataUnit.parseCode(texts, values);

        assertThat(map.size(), is(3));
        assertThat(map.get("0"), is(""));
        assertThat(map.get("1"), is("Komplette Remission"));
        assertThat(map.get("3"), is("Partielle Remission"));
    }
}
