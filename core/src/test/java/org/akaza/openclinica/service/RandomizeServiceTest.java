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
import java.sql.Date;
import java.util.Locale;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
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
    @Mock
    private StudyDAO studyDao;
    @Mock
    private StudyParameterValueDAO studyParameterValueDAO;
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
        doReturn(studyParameterValueDAO).when(service).getStudyParameterValueDAO();

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
        assertThat(RandomizeService.birthDateValue(null, false), is(""));
        assertThat(RandomizeService.birthDateValue(null, true), is(""));
    }

    @Test
    public void testBirthDateValue_Present() {
        // SubjectDAO reads the column with rs.getDate(), so the value is a java.sql.Date
        assertThat(RandomizeService.birthDateValue(Date.valueOf("1980-05-12"), false), is("1980-05-12"));
        assertThat(RandomizeService.birthDateValue(Date.valueOf("1980-01-01"), false), is("1980-01-01"));
    }

    @Test
    public void testBirthDateValue_YearOfBirthOnly() {
        // collectDob = 2 stores 1 January of the year entered; 31 December checks that the year does not shift
        assertThat(RandomizeService.birthDateValue(Date.valueOf("1980-01-01"), true), is("1980"));
        assertThat(RandomizeService.birthDateValue(Date.valueOf("1979-12-31"), true), is("1979"));
    }

    @Test
    public void testBirthDateValue_YearOfBirthOnly_ServerLocale() {
        // the Thai, Japanese imperial and Saudi locales format "yyyy" as 2523, 55 and (from Java 9) Arabic-Indic digits by default
        Locale saved = Locale.getDefault();
        try {
            for (Locale locale : new Locale[] { new Locale("th", "TH"), new Locale("ja", "JP", "JP"), new Locale("ar", "SA") }) {
                Locale.setDefault(locale);
                assertThat(locale.toString(), RandomizeService.birthDateValue(Date.valueOf("1980-01-01"), true), is("1980"));
            }
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    public void testIsYearOfBirthOnly_StudySubject() {
        mockStudy(10, 0);
        mockCollectDob(10, "2");

        assertThat(service.isYearOfBirthOnly(subjectIn(10), studyDao), is(true));
    }

    @Test
    public void testIsYearOfBirthOnly_SiteSubjectUsesParent() {
        mockStudy(11, 10);
        mockCollectDob(10, "2");

        assertThat(service.isYearOfBirthOnly(subjectIn(11), studyDao), is(true));
        verify(studyParameterValueDAO, never()).findByHandleAndStudy(11, "collectDob");
    }

    @Test
    public void testIsYearOfBirthOnly_OtherValues() {
        // 1 = full date, 3 = not used, "" = no parameter row (findByHandleAndStudy returns an empty bean)
        mockStudy(10, 0);
        for (String value : new String[] { "1", "3", "" }) {
            mockCollectDob(10, value);
            assertThat(value, service.isYearOfBirthOnly(subjectIn(10), studyDao), is(false));
        }
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

    private void mockStudy(int id, int parentStudyId) {
        StudyBean study = new StudyBean();
        study.setId(id);
        study.setParentStudyId(parentStudyId);
        when(studyDao.findByPK(id)).thenReturn(study);
    }

    private void mockCollectDob(int studyId, String value) {
        StudyParameterValueBean collectDob = new StudyParameterValueBean();
        collectDob.setValue(value);
        when(studyParameterValueDAO.findByHandleAndStudy(studyId, "collectDob")).thenReturn(collectDob);
    }

    private static StudySubjectBean subjectIn(int studyId) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setStudyId(studyId);
        return studySubject;
    }

    private void mockResolvedItem() {
        ItemBean item = new ItemBean();
        item.setId(42);
        when(expressionService.constructFullExpressionIfPartialProvided(anyString(), anyString())).thenReturn(FACTOR);
        when(expressionService.getItemBeanFromExpression(FACTOR)).thenReturn(item);
        when(expressionService.getGroupOrdninalCurated(FACTOR)).thenReturn("");
    }
}
