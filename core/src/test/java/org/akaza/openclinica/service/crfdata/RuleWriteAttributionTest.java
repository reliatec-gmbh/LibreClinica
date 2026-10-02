/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.crfdata;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.junit.Test;

/**
 * A value written by a rule (Insert, Randomize) is audited under the user whose save ran the rule,
 * not user 0 and not the item's previous editor; a row whose value does not change keeps its author.
 */
public class RuleWriteAttributionTest {
    private final DynamicsMetadataService service = new DynamicsMetadataService(null);

    @Test
    public void newRowIsAttributedToTheSavingUser() {
        ItemDataBean created = new ItemDataBean();
        created.setOwner(user(7));
        assertEquals(7, service.prepareRuleValue(created, "ARM-A", null, user(7)).getUpdaterId());
    }

    @Test
    public void changedRowIsNotAttributedToItsPreviousEditor() {
        ItemDataBean existing = row("old", 5);
        assertEquals(7, service.prepareRuleValue(existing, "new", null, user(7)).getUpdaterId());
        assertEquals("new", existing.getValue());
    }

    @Test
    public void unchangedRowKeepsItsAuthor() {
        assertEquals(5, service.prepareRuleValue(row("same", 5), "same", null, user(7)).getUpdaterId());
    }

    /** Every rule write in DynamicsMetadataService must go through saveRuleValue, or it skips the attribution. */
    @Test
    public void ruleWritesGoThroughOneHelper() throws Exception {
        String src = new String(Files.readAllBytes(Paths.get(
                "src/main/java/org/akaza/openclinica/service/crfdata/DynamicsMetadataService.java")), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("(?m)^(?!\\s*//).*getItemDataDAO\\(\\)\\.updateValue\\(").matcher(src);
        int calls = 0;
        while (m.find()) {
            calls++;
        }
        assertEquals("updateValue outside saveRuleValue", 1, calls);
    }

    private static ItemDataBean row(String value, int updaterId) {
        ItemDataBean row = new ItemDataBean();
        row.setOwner(user(3));
        row.setUpdater(user(updaterId));
        row.setValue(value);
        return row;
    }

    private static UserAccountBean user(int id) {
        UserAccountBean ub = new UserAccountBean();
        ub.setId(id);
        return ub;
    }
}
