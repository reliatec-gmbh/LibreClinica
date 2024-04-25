package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.DefaultToolbar;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.*;

public class ProtocolDeviationTableToolbar extends DefaultToolbar  {
    protected void addToolbarItems() {
        addToolbarItem(createAddProtocolDeviation());
        addToolbarItem(ToolbarItemType.SEPARATOR);

    }

    public ToolbarItem createAddProtocolDeviation() {

        AddNewProtocolDeviation item = new AddNewProtocolDeviation();
        item.setCode(ToolbarItemType.CLEAR_ITEM.toCode());
        ToolbarItemRenderer renderer = new ClearItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }


    private class AddNewProtocolDeviation extends AbstractItem {

        @Override
        public String disabled() {
            HtmlBuilder html = new HtmlBuilder();
            return html.toString();
            // TODO Auto-generated method stub
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            //@pgawade 25-June-2012: fix for issue 14427
            //html.a().href("#").id("addSubject");// onclick(
            //html.a().href("javascript:;").id("addSubject");
            //html.a().append(" onclick='jQuery.blockUI({ message: jQuery(\"#addSubjectForm\"), css:{left: \"300px\", top:\"10px\" } })' ").id("addSubject");
            html.a().href("javascript:").id("addSubject");
            // "initmb();sm('box', 730,100);"
            // );
            html.quote();
            html.quote().close();
            html.nbsp().append("Add new protocol deviation").nbsp().aEnd();

            return html.toString();
        }

    }

}
