/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.rule.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A dry run previews a rule action and must never write it.
 */
@RunWith(MockitoJUnitRunner.class)
public class DryRunActionProcessorTest {
    @Mock
    private DynamicsMetadataService metadataService;

    private final ItemDataBean itemData = new ItemDataBean();

    @Test
    public void insertDryRunDoesNotSave() {
        assertDryRunDoesNotSave(new InsertActionProcessor(null, metadataService, null, null, null), new InsertActionBean());
    }

    @Test
    public void showDryRunDoesNotSave() {
        assertDryRunDoesNotSave(new ShowActionProcessor(null, metadataService, null), new ShowActionBean());
    }

    @Test
    public void hideDryRunDoesNotSave() {
        assertDryRunDoesNotSave(new HideActionProcessor(null, metadataService, null), new HideActionBean());
    }

    private void assertDryRunDoesNotSave(ActionProcessor processor, RuleActionBean action) {
        for (RuleRunnerMode mode : new RuleRunnerMode[] { RuleRunnerMode.CRF_BULK, RuleRunnerMode.RULSET_BULK, RuleRunnerMode.IMPORT_DATA }) {
            RuleActionBean result = processor.execute(mode, ExecutionMode.DRY_RUN, action, itemData, "1", null, null);
            assertThat(mode.name(), result, sameInstance(action));
        }
        verifyZeroInteractions(metadataService);
    }
}
