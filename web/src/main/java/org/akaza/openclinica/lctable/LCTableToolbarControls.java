/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Div;

import java.util.function.BiConsumer;

import static org.akaza.openclinica.lctable.LCTableUtil.NO_HX_TRIGGER;
import static org.akaza.openclinica.lctable.LCTableUtil.hxGetAttrs;

/** Reusable controls rendered in an LCTable toolbar. */
public final class LCTableToolbarControls {

    private LCTableToolbarControls() {
    }

    public static <T> BiConsumer<Div<?>, LCTableContext<T>> clearFilter(LCTable<T> table) {
        return (container, context) -> {
            container.a()
                .attrId(table.tableName + "-clear-filter-toolbar")
                .attrClass("text-btn")
                .addAttr("data-testid", "clear-filter-button")
                .addAttr("data-test-action", "clear-filter")
                .of(hxGetAttrs(context.entityPath, LCTableFilterDef.ClearFilter.selectorFor(table),
                    "#" + table.panelId, NO_HX_TRIGGER))
                .text(context.words.getString("table_clear_filter"))
                .__();
        };
    }
}
