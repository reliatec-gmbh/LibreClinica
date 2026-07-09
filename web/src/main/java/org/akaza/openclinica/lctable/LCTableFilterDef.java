/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableUtil.*;

import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.EnumTypeInputType;
import org.xmlet.htmlapifaster.Tr;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public abstract class LCTableFilterDef {
    protected LCTableFilterDef() {}

    /*** Factory methods for creating a text filter definition. */
    public static Text textFilter() { return new Text(); }
    public static Text textFilter(String pattern) { return new Text(pattern, null); }
    public static Text textFilter(String pattern, String message) { return new Text(pattern, message); }

    /*** Factory methods for creating a select filter definition. */
    public static <T> Select<T> selectFilter(List<T> values, Function<T, String> valueToString) {
        return new Select<>(values, valueToString);
    }

    public static ClearFilter clearFilter() { return new ClearFilter(); }

    /*** Signature of rendering function (to be implemented by specific filter def types) */
    public abstract <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table);

    /*----------------------------------------------------------------------------------------------------------------*/
    /**
     * Text filter specialization: renders an <input type="text"> with optional pattern/title
     */
    public static final class Text extends LCTableFilterDef {
        public final String pattern;   // Regex pattern for HTML5 validation
        public final String message;   // Tooltip message displaying requested format

        public Text() {
            this.pattern = null;
            this.message = null;
        }

        public Text(String pattern, String message) {
            this.pattern = pattern;
            this.message = message;
        }

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String filterValue = ctx.filters.getOrDefault(col.columnName, "");

            final String trigger = this.pattern == null ? "input changed delay:400ms" : "input[this.validity.valid] changed delay:400ms";   // ONLY fire HTMX if the HTML5 validity state is 'valid'

            tr.td().div().attrClass("filter-wrapper").of(div -> {
                var input = div.input()
                    .attrType(EnumTypeInputType.TEXT)
                    .attrName(filterName)
                    .attrValue(filterValue)
                    .attrClass("filter-input")
                    .attrSize(1L)
                    .attrId(table.panelId + "-filter-" + col.columnName);

                // If a pattern is provided, add HTML5 validation and HTMX event filter
                if (this.pattern != null) {
                    input.attrPattern(this.pattern);
                    if (this.message != null) {
                        input.attrTitle(this.message);
                    }
                }

                input.of(hxGetAttrs(ctx.entityPath, "closest form", "#" + table.panelId, trigger))
                    .__().__();
            }).__();
        }
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /**
     * Select filter specialization: renders a <select> with provided values
     */
    public static final class Select<F> extends LCTableFilterDef {
        public final List<F> values;
        public final Function<F, String> valueToString;     // convert to string for display in the dropdown list
        public final Function<F, String> valueToUrlParam;   // convert to string for use in the URL query parameter

        public Select(List<F> values, Function<F, String> valueToString, Function<F, String> valueToUrlParam) {
            this.values = values;
            this.valueToString = valueToString;
            this.valueToUrlParam = valueToUrlParam;
        }

        /**
         * Convenience constructor: uses the same conversion-to-string function for both display and URL parameter conversion
         */
        public Select(List<F> values, Function<F, String> valueToString) {
            this(values, valueToString, valueToString);
        }

        /**
         * Returns the display label for a given optional value.
         */
        public String label(F value) {
            return Optional.ofNullable(value).map(this.valueToString).orElse("");
        }

        /**
         * Returns the URL parameter value for a given optional value.
         */
        public String urlParam(F value) {
            return Optional.ofNullable(value).map(this.valueToUrlParam).orElse("");
        }


        /**
         * Reconstructs the strongly-typed domain object from an HTTP query parameter string.
         */
        public Optional<F> parseParam(String paramValue) {
            if (paramValue == null || paramValue.isEmpty()) {
                return Optional.empty(); // No filtering requested
            } else {
                // Find the domain object whose string representation matches the submitted text
                return values.stream().filter(val -> label(val).equals(paramValue)).findFirst();       // empty if nothing found
            }
        }

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String rawSelected = ctx.filters.getOrDefault(col.columnName, "");

            // UI Safety Check: Verify if the URL parameter actually matches a real option
            final boolean isValidOption = rawSelected.isEmpty() || this.values.stream()
                .map(this::urlParam)
                .anyMatch(val -> val.equals(rawSelected));

            // If it's invalid (e.g., "broken"), treat it as empty ("All") so the UI snaps back to a valid state
            final String currentSelected = isValidOption ? rawSelected : "";

            tr.td().div().attrClass("filter-wrapper").of(div -> {
                var select = div.select()
                    .attrName(filterName)
                    .attrId(table.panelId + "-filter-" + col.columnName)
                    .attrClass("filter-select")
                    .attrStyle("width:1px;flex:1")
                    .of(hxGetAttrs(ctx.entityPath, "closest form", "#" + table.panelId, "change"));

                select.option().attrValue("").attrSelected(currentSelected.isEmpty()).text("").__();
                this.values.forEach(option -> {
                    final String labelText = this.label(option);
                    final String urlParamText = this.urlParam(option);
                    boolean isSelected = urlParamText.equals(currentSelected);
                    select.option().attrValue(urlParamText).attrSelected(isSelected).text(labelText).__();
                });
                select.__().__();
            }).__();
        }
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /**
     * Clear filter specialization: renders a button that clears all filters when clicked.
     *
     * <p>Uses a precise {@code hx-include} CSS selector that names only the non-filter parameters
     * (page, maxRows, sortProp, sortDir) by their exact {@code name} attribute. Filter inputs are
     * never selected, so they are never included in the HTMX request — no JavaScript required.
     */
    public static final class ClearFilter extends LCTableFilterDef {
        // CSS selector that picks up only the pagination/sort form fields, excluding all filter inputs.
        private static final String NON_FILTER_PARAMS_SELECTOR =
            "[name=" + LCTableParams.PARAM_PAGE + "]" +
            ",[name=" + LCTableParams.PARAM_MAX_ROWS + "]" +
            ",[name=" + LCTableParams.PARAM_SORT_PROP + "]" +
            ",[name=" + LCTableParams.PARAM_SORT_DIR + "]";

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            tr.td().a().attrClass("page-btn")
                .of(hxGetAttrs(ctx.entityPath, NON_FILTER_PARAMS_SELECTOR, "#" + table.panelId, "click"))
                .text("Clear Filter")
                .__().__();
        }
    }

}
