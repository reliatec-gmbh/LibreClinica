/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.domain.rule.action;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Rules run in batch or on import leave a locked, signed or stopped study event alone (#481);
 * data entry is not affected.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosedEventRuleActionTest {
    private static final RuleRunnerMode[] BATCH_AND_IMPORT = { RuleRunnerMode.CRF_BULK, RuleRunnerMode.RULSET_BULK, RuleRunnerMode.IMPORT_DATA };

    @Mock
    private DynamicsMetadataService metadataService;

    private final ItemDataBean itemData = new ItemDataBean();

    @Test
    public void closedEventSkipsBatchAndImportSaves() {
        when(metadataService.isInClosedStudyEvent(itemData)).thenReturn(true);
        assertSkipped(new InsertActionProcessor(null, metadataService, null, null, null), new InsertActionBean());
        assertSkipped(new ShowActionProcessor(null, metadataService, null), new ShowActionBean());
        assertSkipped(new HideActionProcessor(null, metadataService, null), new HideActionBean());
        verify(metadataService, times(3 * BATCH_AND_IMPORT.length)).isInClosedStudyEvent(itemData);
        verifyNoMoreInteractions(metadataService);
    }

    @Test
    public void openEventStillSavesInBatch() {
        when(metadataService.isInClosedStudyEvent(itemData)).thenReturn(false);
        new ShowActionProcessor(null, metadataService, null).execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, new ShowActionBean(), itemData, "1", null, null);
        new HideActionProcessor(null, metadataService, null).execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, new HideActionBean(), itemData, "1", null, null);
        verify(metadataService).showNew(anyInt(), anyListOf(PropertyBean.class), any(UserAccountBean.class), any(RuleSetBean.class));
        verify(metadataService).hideNew(anyInt(), anyListOf(PropertyBean.class), any(UserAccountBean.class), any(RuleSetBean.class));
    }

    @Test
    public void dataEntryDoesNotConsultTheEventStatus() {
        new ShowActionProcessor(null, metadataService, null).execute(RuleRunnerMode.DATA_ENTRY, ExecutionMode.SAVE, new ShowActionBean(), itemData, "1", null, null);
        new HideActionProcessor(null, metadataService, null).execute(RuleRunnerMode.DATA_ENTRY, ExecutionMode.SAVE, new HideActionBean(), itemData, "1", null, null);
        verify(metadataService, never()).isInClosedStudyEvent(any(ItemDataBean.class));
        verify(metadataService).showNew(anyInt(), anyListOf(PropertyBean.class), any(UserAccountBean.class), any(RuleSetBean.class));
    }

    private void assertSkipped(ActionProcessor processor, RuleActionBean action) {
        for (RuleRunnerMode mode : BATCH_AND_IMPORT) {
            assertNull(mode.name(), processor.execute(mode, ExecutionMode.SAVE, action, itemData, "1", null, null));
        }
    }
}
