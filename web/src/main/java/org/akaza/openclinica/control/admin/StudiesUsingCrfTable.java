/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableInMemoryColumn;
import org.akaza.openclinica.lctable.LCTableInMemoryDataSource;
import org.akaza.openclinica.lctable.LCTableUtil;
import org.akaza.openclinica.lctable.SafeUrl;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdColWithContext;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/** LCTable showing the studies that use the CRF currently displayed by {@link ViewCRFServlet}. */
public final class StudiesUsingCrfTable {

    private static final String TABLE_NAME = "studies";

    private final LCTable<StudyBean> table;

    public StudiesUsingCrfTable(List<StudyBean> studies) {
        List<LCTableColumnDef<StudyBean>> columns = List.of(
            textCol("name", key("study_name"), "study-name", 0, StudyBean::getName),
            textCol("uniqueProtocolid", key("unique_protocol_ID"), "unique-protocol-id", 0, StudyBean::getIdentifier),
            customTdColWithContext("actions", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(),
                StudyBean::getId,
                (td, study, context) -> {
                    SafeUrl href = url("ViewStudy").param("id", study.getId()).param("viewFull", "yes");
                    td.of(LCTableUtil.actionLink(
                        TABLE_NAME + "-view-" + study.getId(), context.words.getString("view"),
                        href, "bt_View.gif", "view"));
                })
        );
        LCTableInMemoryDataSource<StudyBean> dataSource = new LCTableInMemoryDataSource<>(studies, List.of(
            LCTableInMemoryColumn.text("name", StudyBean::getName),
            LCTableInMemoryColumn.text("uniqueProtocolid", StudyBean::getIdentifier)
        ));
        this.table = new LCTable<>(TABLE_NAME, columns, dataSource, List.of("module", "crfId"))
            .setRowTestAttributes(study -> Map.of("study", String.valueOf(study.getId())));
    }

    public String render(HttpServletRequest request) {
        return table.render(request);
    }
}
