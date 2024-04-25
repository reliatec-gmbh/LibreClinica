package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.control.ListStudyView;
import org.akaza.openclinica.dao.managestudy.FindSubjectsFilter;
import org.akaza.openclinica.dao.managestudy.ProtocolDeviationDAO;
import org.akaza.openclinica.dao.managestudy.ProtocolDeviationFilter;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ProtocolDeviationTableFactory extends AbstractTableFactory {
    private String[] columnNames = new String[] {};
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private ProtocolDeviationDAO protocolDeviationDAO;
    private StudyBean studyBean;

    public ProtocolDeviationDAO getProtocolDeviationDAO() {
        return protocolDeviationDAO;
    }

    public void setProtocolDeviationDAO(ProtocolDeviationDAO protocolDeviationDAO) {
        this.protocolDeviationDAO = protocolDeviationDAO;
    }

    @Override
    protected String getTableName() {
        return "protocolDeviations";
    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        getColumnNamesMap();
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("protocolDeviation.label");
        //columnNamesList.add("protocolDeviation.severityLabel");
    }

    public void configureTableFacadeCustomView(TableFacade tableFacade, HttpServletRequest request) {
        tableFacade.setView(new ProtocolDeviationView(getLocale(), request));
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("protocolDeviation.label");
        //columnNamesList.add("protocolDeviation.severityLabel");
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        /*resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);*/
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index]), "PDID", null, null);
        ++index;
        /*configureColumn(row.getColumn(columnNames[index]), "Severity", null, null);
        ++index;*/

        //configureColumn(row.getColumn(columnNames[index]), "Actions", null, null);
        ++index;

        configureColumn(row.getColumn(columnNames[columnNames.length - 1]), "Actions", new ActionsCellEditor(), null, false,
                false);
        ++index;
    }

    @Override
    // To avoid showing title in other pages, the request element is used to determine where the request came from.
    public TableFacade createTable(HttpServletRequest request, HttpServletResponse response) {
        TableFacade tableFacade = getTableFacadeImpl(request, response);
        tableFacade.setStateAttr("restore");
        setDataAndLimitVariables(tableFacade);
        configureTableFacade(response, tableFacade);
        if (!tableFacade.getLimit().isExported()) {
            configureColumns(tableFacade, locale);
            tableFacade.setMaxRowsIncrements(getMaxRowIncrements());
            configureTableFacadePostColumnConfiguration(tableFacade);
            configureTableFacadeCustomView(tableFacade, request);
            configureUnexportedTable(tableFacade, locale);
        } else {
            configureExportColumns(tableFacade, locale);
        }
        return tableFacade;
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        Limit limit = tableFacade.getLimit();

        Collection<ProtocolDeviationBean> items = getProtocolDeviationDAO()
                .findByStudy(
                getStudyBean().getId()
        );

        /*int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();*/
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        for(ProtocolDeviationBean pdb: items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("protocolDeviation.id", pdb.getProtocolDeviationId());
            theItem.put("protocolDeviation.label", pdb.getLabel());
            /*theItem.put("protocolDeviation.severityLabel", pdb.getSeverityLabel());
            theItem.put("protocolDeviation.description", pdb.getDescription());*/
            theItems.add(theItem);
        }

        tableFacade.setItems(theItems);
        tableFacade.setTotalRows(theItems.size());
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        tableFacade.setToolbar(
                new ProtocolDeviationTableToolbar(/*addSubjectLinkShow*/));
    }


    protected ProtocolDeviationFilter getSubjectFilter(Limit limit) {
        ProtocolDeviationFilter auditUserLoginFilter = new ProtocolDeviationFilter();
        /*
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            if ("studySubject.status".equalsIgnoreCase(property)) {
                value = Status.getByName(value).getId() + "";
            }
            else if (property.startsWith("sgc_")) {
                int studyGroupClassId = property.endsWith("_") ? 0 : Integer.valueOf(property.split("_")[1]);
                value = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId).getId() + "";
            }
            auditUserLoginFilter.addFilter(property, value);
        }
        */
        return auditUserLoginFilter;
    }


    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    private class ActionsCellEditor implements CellEditor {

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            int rowId = (Integer) ((HashMap<Object, Object>) item).get("protocolDeviation.id");
            StringBuilder url = new StringBuilder();
            url.append("<a href=\"javascript:\" class=\"protocol-deviation-editor\" data-id=\"" + rowId + "\">Edit</a>");


            return url.toString();
        }

    }
}
