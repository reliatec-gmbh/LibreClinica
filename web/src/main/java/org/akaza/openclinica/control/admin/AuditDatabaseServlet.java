/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableFilterDef.timestampFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;

import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableInMemoryColumn;
import org.akaza.openclinica.lctable.LCTableInMemoryDataSource;
import org.akaza.openclinica.lctable.LCTableToolbarControls;
import org.akaza.openclinica.lctable.LCTableUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.lctable.HtmxResponseSupport;
import org.jmesa.facade.TableFacade;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;

/**
 * Servlet for creating a user account.
 *
 * @author Krikor Krumlian
 */
public class AuditDatabaseServlet extends SecureController {

    private static final long serialVersionUID = 1L;
    private static final String TABLE_NAME = "databaseChangeLogs";

    // < ResourceBundle restext;
    Locale locale;
    private DatabaseChangeLogDao databaseChangeLogDao;

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);

        if (!ub.isSysAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        List<DatabaseChangeLogBean> databaseChangeLogs = getDatabaseChangeLogDao().findAll();
        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            request.setAttribute("tableRenderingMode", "jmesa");
            request.setAttribute("auditDatabaseHtml", renderAuditDatabaseTable(databaseChangeLogs));
            forwardPage(Page.AUDIT_DATABASE);
        } else {
            request.setAttribute("tableRenderingMode", "htmlflow");
            String auditDatabaseHtml = renderAuditDatabaseLCTable(databaseChangeLogs);
            if (HtmxResponseSupport.writeFragmentIfRequested(request, response, auditDatabaseHtml)) {
                response.getWriter().flush();
            } else {
                request.setAttribute("auditDatabaseHtml", auditDatabaseHtml);
                forwardPage(Page.AUDIT_DATABASE);
            }
        }

    }

    String renderAuditDatabaseLCTable(List<DatabaseChangeLogBean> databaseChangeLogs) {
        List<LCTableColumnDef<DatabaseChangeLogBean>> columns = List.of(
            textCol("id",           key("Id"),            "id",            0, DatabaseChangeLogBean::getId),
            textCol("author",       key("author"),        "author",        0, DatabaseChangeLogBean::getAuthor),
            textCol("fileName",     key("file_name"),     "file-name",     0, DatabaseChangeLogBean::getFileName),
            textCol("dataExecuted", key("date_executed"), "date-executed", 0,
                timestampFilter(),
                DatabaseChangeLogBean::getDataExecuted, LCTableUtil::timestampToString),
            textCol("md5Sum",       key("md5_sum"),       "md5-sum",       0, DatabaseChangeLogBean::getMd5Sum),
            textCol("description",  key("description"),   "description",   0, DatabaseChangeLogBean::getDescription),
            textCol("comments",     key("comments"),      "comments",      0, DatabaseChangeLogBean::getComments),
            textCol("tag",          key("tag"),           "tag",           0, DatabaseChangeLogBean::getTag),
            textCol("liquibase",    key("liquibase"),     "liquibase",     0, DatabaseChangeLogBean::getLiquibase)
        );
        LCTableInMemoryDataSource<DatabaseChangeLogBean> dataSource = new LCTableInMemoryDataSource<>(
            databaseChangeLogs,
            List.of(
                LCTableInMemoryColumn.text("id", DatabaseChangeLogBean::getId),
                LCTableInMemoryColumn.text("author", DatabaseChangeLogBean::getAuthor),
                LCTableInMemoryColumn.text("fileName", DatabaseChangeLogBean::getFileName),
                LCTableInMemoryColumn.value("dataExecuted", DatabaseChangeLogBean::getDataExecuted, LCTableUtil::timestampToString),
                LCTableInMemoryColumn.text("md5Sum", DatabaseChangeLogBean::getMd5Sum),
                LCTableInMemoryColumn.text("description", DatabaseChangeLogBean::getDescription),
                LCTableInMemoryColumn.text("comments", DatabaseChangeLogBean::getComments),
                LCTableInMemoryColumn.text("tag", DatabaseChangeLogBean::getTag),
                LCTableInMemoryColumn.text("liquibase", DatabaseChangeLogBean::getLiquibase)
            )
        );
        LCTable<DatabaseChangeLogBean> table = new LCTable<>(TABLE_NAME, columns, dataSource);
        table.addCustomToolbarControl(LCTableToolbarControls.clearFilter(table));
        return table.render(request);
    }

    private String renderAuditDatabaseTable(List<DatabaseChangeLogBean> databaseChangeLogs) {

        // Collection<StudyRowContainer> items = getStudyRows(studyBeans);
        TableFacade tableFacade = createTableFacade("databaseChangeLogs", request);
        tableFacade.setColumnProperties("id", "author", "fileName", "dataExecuted", "md5Sum", "description", "comments", "tag", "liquibase");

        tableFacade.setItems(databaseChangeLogs);
        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();

        table.setCaption("");
        HtmlRow row = table.getRow();

        HtmlColumn id = row.getColumn("id");
        id.setTitle("Id");

        HtmlColumn author = row.getColumn("author");
        author.setTitle("Author");

        HtmlColumn fileName = row.getColumn("fileName");
        fileName.setTitle("File Name");

        HtmlColumn dataExecuted = row.getColumn("dataExecuted");
        dataExecuted.setTitle("Date Executed");
        dataExecuted.getCellRenderer().setCellEditor(new DateCellEditor("yyyy-MM-dd hh:mm:ss"));

        HtmlColumn md5Sum = row.getColumn("md5Sum");
        md5Sum.setTitle("md5 sum");

        HtmlColumn description = row.getColumn("description");
        description.setTitle("Description");

        HtmlColumn comments = row.getColumn("comments");
        comments.setTitle("Comments");

        HtmlColumn tag = row.getColumn("tag");
        tag.setTitle("Tag");

        HtmlColumn liquibase = row.getColumn("liquibase");
        liquibase.setTitle("Liquibase");

        return tableFacade.render();
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    public DatabaseChangeLogDao getDatabaseChangeLogDao() {
        databaseChangeLogDao =
            this.databaseChangeLogDao != null ? databaseChangeLogDao : (DatabaseChangeLogDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "databaseChangeLogDao");
        return databaseChangeLogDao;
    }

    public void setDatabaseChangeLogDao(DatabaseChangeLogDao databaseChangeLogDao) {
        this.databaseChangeLogDao = databaseChangeLogDao;
    }

}
