/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 */
package org.akaza.openclinica.control.submit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.springframework.security.access.AccessDeniedException;

/** Object-level study checks for rule assignment endpoints; roles are checked by their servlets. */
final class RuleAssignmentAccess {
    private final int studyId;

    static void sendFailure(HttpServletResponse response, RuntimeException failure) throws IOException {
        if (failure instanceof AccessDeniedException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (failure instanceof NoSuchElementException) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (failure instanceof IllegalArgumentException) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            throw failure;
        }
    }

    RuleAssignmentAccess(StudyBean currentStudy) {
        if (currentStudy == null || currentStudy.getId() <= 0) {
            throw new IllegalArgumentException("A current study is required");
        }
        studyId = currentStudy.getId();
    }

    RuleSetBean ruleSet(String id, IntFunction<RuleSetBean> lookup) {
        RuleSetBean ruleSet = lookup.apply(parseId(id));
        if (ruleSet == null) {
            throw new NoSuchElementException("Rule set not found");
        }
        checkStudy(ruleSet);
        return ruleSet;
    }

    RuleSetRuleBean rule(String id, IntFunction<RuleSetRuleBean> lookup) {
        RuleSetRuleBean rule = lookup.apply(parseId(id));
        if (rule == null) {
            throw new NoSuchElementException("Rule assignment not found");
        }
        checkStudy(rule.getRuleSetBean());
        return rule;
    }

    List<RuleSetRuleBean> rules(String ids, IntFunction<RuleSetRuleBean> lookup) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Rule assignment IDs are required");
        }
        // Parse every ID before looking anything up; do not produce partial downloads.
        String[] parts = ids.split(",", -1);
        List<Integer> parsed = new ArrayList<>(parts.length);
        for (String part : parts) {
            parsed.add(parseId(part));
        }
        List<RuleSetRuleBean> result = new ArrayList<>(parts.length);
        for (Integer id : parsed) {
            RuleSetRuleBean rule = lookup.apply(id);
            if (rule == null) {
                throw new NoSuchElementException("Rule assignment not found");
            }
            checkStudy(rule.getRuleSetBean());
            result.add(rule);
        }
        return result;
    }

    private void checkStudy(RuleSetBean ruleSet) {
        if (ruleSet == null || ruleSet.getStudyId() == null || ruleSet.getStudyId() != studyId) {
            throw new AccessDeniedException("Rule set does not belong to the current study");
        }
    }

    private static int parseId(String id) {
        if (id == null || !id.matches("[0-9]+")) {
            throw new IllegalArgumentException("Invalid rule ID");
        }
        try {
            int parsed = Integer.parseInt(id);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // Out-of-range numbers are invalid too.
        }
        throw new IllegalArgumentException("Invalid rule ID");
    }
}