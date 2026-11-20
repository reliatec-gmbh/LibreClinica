/*
* LibreClinica is distributed under the
* GNU Lesser General Public License (GNU LGPL).

* For details see: https://libreclinica.org/license
* copyright (C) 2026 LibreClinica
*/
package org.akaza.openclinica.control.submit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.action.EmailActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.w3c.dom.Document;

public class RuleAssignmentsXmlWriterTest extends TestCase {
    public void testMatchesLegacyXmlBytesWithGroupingSharedRulesAndFullActions() throws Exception {
        RuleBean definition = new RuleBean();
        definition.setId(25);
        definition.setOid("R_TEST");
        definition.setName("Quoted \"rule\" 雪");
        definition.setDescription("Check <value> & more");
        ExpressionBean expression = new ExpressionBean();
        expression.setValue("I_ITEM eq \"\"");
        definition.setExpression(expression);
        RuleSetRuleBean first = assignment(1, 10, definition);
        first.getRuleSetBean().setRunSchedule(true);
        first.getRuleSetBean().setRunTime("12:30");
        RuleSetRuleBean second = assignment(2, 10, definition);
        RuleSetRuleBean third = assignment(3, 11, definition);
        List<RuleSetRuleBean> assignments = List.of(first, second, third);
        String legacy = legacyXml(assignments);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new RuleAssignmentsXmlWriter(assignments, 37).write(output);
        assertEquals(legacy, new String(output.toByteArray(), StandardCharsets.UTF_8));
        Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new ByteArrayInputStream(output.toByteArray()));
        assertEquals(2, xml.getElementsByTagName("RuleAssignment").getLength());
        assertEquals(3, xml.getElementsByTagName("RuleRef").getLength());
        assertEquals(1, xml.getElementsByTagName("RuleDef").getLength());
        assertEquals(3, xml.getElementsByTagName("EmailAction").getLength());
        assertEquals(3, xml.getElementsByTagName("DiscrepancyNoteAction").getLength());
        assertEquals(1, xml.getElementsByTagName("RunOnSchedule").getLength());
    }

    public void testEmptyXmlMatchesLegacyBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new RuleAssignmentsXmlWriter(List.of(), 37).write(output);
        assertEquals(legacyXml(List.of()), new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    public void testMissingStudyIsRejectedBeforeContainerPreparation() {
        RuleSetRuleBean assignment = new RuleSetRuleBean();
        assignment.setRuleSetBean(new RuleSetBean());
        try {
            new RuleAssignmentsXmlWriter(List.of(assignment), 37);
            fail("Unscoped assignments must not be serialized");
        } catch (SecurityException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // The legacy servlet's populate/Mapping/XMLContext/Marshaller sequence, without its flawed file transport.
    private static String legacyXml(List<RuleSetRuleBean> assignments) throws Exception {
        RulesPostImportContainer container = new RulesPostImportContainer();
        container.populate(assignments);
        Mapping mapping = new Mapping();
        mapping.loadMapping(RulesPostImportContainer.class.getResource("/properties/mappingMarshaller.xml"));
        XMLContext context = new XMLContext();
        context.addMapping(mapping);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.setWriter(writer);
        marshaller.marshal(container);
        return writer.toString();
    }

    private static RuleSetRuleBean assignment(int id, int setId, RuleBean definition) {
        RuleSetBean set = new RuleSetBean();
        set.setId(setId);
        set.setStudyId(37);
        ExpressionBean target = new ExpressionBean();
        target.setValue("I_ITEM_" + setId);
        set.setTarget(target);
        RuleSetRuleBean assignment = new RuleSetRuleBean();
        assignment.setId(id);
        assignment.setRuleSetBean(set);
        assignment.setRuleBean(definition);
        EmailActionBean email = new EmailActionBean();
        email.setTo("user@example.org");
        email.setMessage("Email 雪");
        email.setExpressionEvaluatesTo(false);
        email.setRuleActionRun(new RuleActionRunBean());
        DiscrepancyNoteActionBean note = new DiscrepancyNoteActionBean();
        note.setMessage("Message < & \"");
        note.setExpressionEvaluatesTo(true);
        note.setRuleActionRun(new RuleActionRunBean());
        assignment.setActions(new ArrayList<>(List.of(email, note)));
        return assignment;
    }
}