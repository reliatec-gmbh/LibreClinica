/*
* LibreClinica is distributed under the
* GNU Lesser General Public License (GNU LGPL).

* For details see: https://libreclinica.org/license
* copyright (C) 2026 LibreClinica
*/
package org.akaza.openclinica.control.submit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;

/** Bulk rule XML using the unchanged container and mapping of DownloadRuleSetXmlServlet. */
final class RuleAssignmentsXmlWriter {
    private final RulesPostImportContainer container = new RulesPostImportContainer();

    RuleAssignmentsXmlWriter(List<RuleSetRuleBean> assignments, int studyId) {
        // Check the complete selection before populate() or any response output.
        for (RuleSetRuleBean assignment : assignments) {
            if (assignment == null || assignment.getRuleSetBean() == null
                || !Integer.valueOf(studyId).equals(assignment.getRuleSetBean().getStudyId())) {
                throw new SecurityException("Rule assignment does not belong to the current study.");
            }
        }
        container.populate(assignments);
    }

    void write(OutputStream output) throws IOException {
        URL resource = RulesPostImportContainer.class.getResource("/properties/mappingMarshaller.xml");
        if (resource == null) throw new IOException("Rule XML mapping is unavailable.");
        try {
            Mapping mapping = new Mapping();
            mapping.loadMapping(resource);
            XMLContext context = new XMLContext();
            context.addMapping(mapping);
            Marshaller marshaller = context.createMarshaller();
            OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            marshaller.setWriter(writer);
            marshaller.marshal(container);
            writer.flush(); // The response, not this writer, owns the stream.
        } catch (MappingException | MarshalException | ValidationException e) {
            throw new IOException("Unable to serialize rule assignments as XML.", e);
        }
    }
}