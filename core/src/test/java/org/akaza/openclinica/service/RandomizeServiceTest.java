/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

/**
 * Blank or unresolvable stratification factors must reach the randomisation module as an empty value (which the
 * module refuses), never as a made-up value or an exception (#497).
 */
@RunWith(MockitoJUnitRunner.class)
public class RandomizeServiceTest {
    private static final String TARGET = "SE_OID.F_OID.IG_OID.I_TARGET";
    private static final String FACTOR = "SE_OID.F_OID.IG_OID.I_FACTOR";

    @Mock
    private ExpressionService expressionService;
    @Mock
    private ItemDataDAO itemDataDAO;
    private RandomizeService service;
    private EventCRFBean eventCrf;
    private RuleSetBean ruleSet;

    @Before
    public void setUp() throws Exception {
        // built without its constructor: RandomizationRegistrar() opens the ehcache CacheManager, which the test
        // classpath configuration rejects; the logger is the only field initialiser getExpressionValue needs
        service = mock(RandomizeService.class, CALLS_REAL_METHODS);
        Field logger = RandomizeService.class.getDeclaredField("logger");
        logger.setAccessible(true);
        logger.set(service, LoggerFactory.getLogger(RandomizeService.class));
        doReturn(expressionService).when(service).getExpressionService();
        doReturn(itemDataDAO).when(service).getItemDataDAO();

        eventCrf = new EventCRFBean();
        eventCrf.setId(7);
        ExpressionBean target = new ExpressionBean();
        target.setValue(TARGET);
        ruleSet = new RuleSetBean();
        ruleSet.setTarget(target);
    }

    @Test
    public void testSexValue_Blank() {
        // SubjectDAO maps a missing gender to ' '
        assertThat(RandomizeService.sexValue(' '), is(""));
        assertThat(RandomizeService.sexValue('\0'), is(""));
    }

    @Test
    public void testSexValue_MaleFemale() {
        assertThat(RandomizeService.sexValue('m'), is("Male"));
        assertThat(RandomizeService.sexValue('f'), is("Female"));
    }

    @Test
    public void testBirthDateValue_Missing() {
        assertThat(RandomizeService.birthDateValue(null), is(""));
    }

    @Test
    public void testBirthDateValue_Present() {
        Date dob = new Date(0);
        assertThat(RandomizeService.birthDateValue(dob), is(dob.toString()));
    }

    @Test
    public void testGetExpressionValue_EmptyExpression() {
        when(expressionService.constructFullExpressionIfPartialProvided("", TARGET)).thenReturn("");

        assertThat(service.getExpressionValue("", eventCrf, ruleSet), is(""));
        verifyZeroInteractions(itemDataDAO);
    }

    @Test
    public void testGetExpressionValue_UnresolvedItemOid() {
        when(expressionService.constructFullExpressionIfPartialProvided(FACTOR, TARGET)).thenReturn(FACTOR);
        when(expressionService.getItemBeanFromExpression(FACTOR)).thenReturn(null);

        assertThat(service.getExpressionValue(FACTOR, eventCrf, ruleSet), is(""));
        verifyZeroInteractions(itemDataDAO);
    }

    @Test
    public void testGetExpressionValue_UnresolvedBareItemOid() {
        // expanding a bare OID looks the item up and dereferences it, so it must be checked before expansion
        when(expressionService.getItemFromExpression("I_MISSING")).thenReturn(null);

        assertThat(service.getExpressionValue("I_MISSING", eventCrf, ruleSet), is(""));
        verify(expressionService, never()).constructFullExpressionIfPartialProvided(anyString(), anyString());
        verifyZeroInteractions(itemDataDAO);
    }

    @Test
    public void testGetExpressionValue_NoItemDataRow() {
        mockResolvedItem();
        when(itemDataDAO.findByItemIdAndEventCRFIdAndOrdinal(anyInt(), anyInt(), anyInt())).thenReturn(null);

        assertThat(service.getExpressionValue(FACTOR, eventCrf, ruleSet), is(""));
    }

    @Test
    public void testGetExpressionValue_NullValue() {
        mockResolvedItem();
        ItemDataBean itemData = new ItemDataBean();
        itemData.setValue(null);
        when(itemDataDAO.findByItemIdAndEventCRFIdAndOrdinal(anyInt(), anyInt(), anyInt())).thenReturn(itemData);

        assertThat(service.getExpressionValue(FACTOR, eventCrf, ruleSet), is(""));
    }

    @Test
    public void testGetExpressionValue_Value() {
        mockResolvedItem();
        ItemDataBean itemData = new ItemDataBean();
        itemData.setValue("2");
        when(itemDataDAO.findByItemIdAndEventCRFIdAndOrdinal(42, 7, 1)).thenReturn(itemData);

        assertThat(service.getExpressionValue(FACTOR, eventCrf, ruleSet), is("2"));
    }

    @Test
    public void testGetExpressionValue_ResolvedBareItemOid() {
        mockResolvedItem();
        when(expressionService.getItemFromExpression("I_FACTOR")).thenReturn(new ItemBean());
        ItemDataBean itemData = new ItemDataBean();
        itemData.setValue("1");
        when(itemDataDAO.findByItemIdAndEventCRFIdAndOrdinal(42, 7, 1)).thenReturn(itemData);

        assertThat(service.getExpressionValue("I_FACTOR", eventCrf, ruleSet), is("1"));
    }

    private void mockResolvedItem() {
        ItemBean item = new ItemBean();
        item.setId(42);
        when(expressionService.constructFullExpressionIfPartialProvided(anyString(), anyString())).thenReturn(FACTOR);
        when(expressionService.getItemBeanFromExpression(FACTOR)).thenReturn(item);
        when(expressionService.getGroupOrdninalCurated(FACTOR)).thenReturn("");
    }
}
