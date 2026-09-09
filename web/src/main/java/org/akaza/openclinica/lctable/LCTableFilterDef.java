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
    public static Text textFilter(String pattern, LCTableText message) { return new Text(pattern, message); }

    /*** Factory methods for creating a select filter definition. */
    public static <T> Select<T> selectFilter(List<T> values, Function<T, String> valueToString) {
        return new Select<>(values, valueToString);
    }

    public static ClearFilter clearFilter() { return new ClearFilter(); }

    /*** Signature of rendering function (to be implemented by specific filter def types) */
    public abstract <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table);

    /*----------------------------------------------------------------------------------------------------------------*/
    /**
     * Text filter specialization: renders an {@code <input type="text">} with optional pattern/title
     */
    public static final class Text extends LCTableFilterDef {
        public final String pattern;   // Regex pattern for HTML5 validation
        public final LCTableText message;   // Tooltip message displaying requested format

        public Text() {
            this.pattern = null;
            this.message = null;
        }

        public Text(String pattern, LCTableText message) {
            this.pattern = pattern;
            this.message = message;
        }

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String filterValue = ctx.filters.getOrDefault(col.columnName, "");

            // Every change in the input triggers a request with a delay (resetting the debounce timer).
            // Validity is checked via 'hx-on' right before sending out the request, to prevent submitting invalid values.
            final String trigger = "input changed delay:400ms";

            tr.td().div().attrClass("filter-wrapper").of(div -> {
                var input = div.input()
                    .attrType(EnumTypeInputType.TEXT)
                    .attrName(filterName)
                    .attrValue(filterValue)
                    .attrClass("filter-input")
                    .addAttr("data-testid", "filter-input")
                    .addAttr("data-test-column", col.columnName)
                    .attrSize(1L)
                    .attrId(table.tableName + "-text-filter-" + col.columnName);

                // If a pattern is provided, add HTML5 validation and HTMX event filter
                if (this.pattern != null) {
                    input.attrPattern(this.pattern);
                    if (this.message != null) {
                        input.attrTitle(this.message.resolve(ctx.words, ctx.locale));
                    }
                    // Cancel the debounced request if the value is no longer valid by the time it actually fires.
                    input.addAttr("hx-on:htmx:before-request", "if(!this.validity.valid){event.preventDefault();}");
                }

                // use hxGetAttrsIgnoreActiveValue to avoid replacing the input value with the value from the response while the user is typing
                input.of(hxGetAttrsIgnoreActiveValue(ctx.entityPath, "closest form", "#" + table.panelId, trigger))
                    .__().__();
            }).__();
        }
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /**
     * Select filter specialization: renders a {@code <select>} with provided values
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
                return values.stream().filter(val -> urlParam(val).equals(paramValue)).findFirst();       // empty if nothing found
            }
        }

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String rawSelected = ctx.filters.getOrDefault(col.columnName, "");

            // UI Safety Check: Verify the URL parameter matches a valid option; otherwise, default to empty.
            final boolean isValidOption = rawSelected.isEmpty() || this.values.stream()
                .map(this::urlParam)
                .anyMatch(val -> val.equals(rawSelected));
            final String currentSelected = isValidOption ? rawSelected : "";

            tr.td().div().attrClass("filter-wrapper").of(div -> {
                var select = div.select()
                    .attrName(filterName)
                    .attrId(table.tableName + "-select-filter-" + col.columnName)
                    .attrClass("filter-select")
                    .addAttr("data-testid", "filter-select")
                    .addAttr("data-test-column", col.columnName)
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
     * Clear filter specialization: renders a button that clears all filters.
     * Uses a precise CSS selector to include only non-filter parameters in the HTMX request.
     */
    public static final class ClearFilter extends LCTableFilterDef {
        // CSS selector targeting pagination/sort/hidden columns fields, excluding filter inputs.
        private static final String NON_FILTER_PARAMS_SELECTOR =
            "[name=" + LCTableParams.PARAM_PAGE + "]" +
                ",[name=" + LCTableParams.PARAM_MAX_ROWS + "]" +
                ",[name=" + LCTableParams.PARAM_SORT_PROP + "]" +
                ",[name=" + LCTableParams.PARAM_SORT_DIR + "]" +
                ",[name=" + LCTableParams.PARAM_SHOW_HIDDEN_COLS + "]";

        @Override
        public <T, E extends Element<?, ?>> void renderFilter(Tr<E> tr, LCTableContext<T> ctx, LCTableColumnDef<T> col, LCTable<T> table) {
            StringBuilder selector = new StringBuilder(NON_FILTER_PARAMS_SELECTOR);
            // Append the per-table "sticky" parameter names to the selector, to preserve them when clearing filters.
            for (String stickyParamName : table.getStickyParamNames()) {
                selector.append(",[name=").append(stickyParamName).append(']');
            }
            tr.td().a()
                .attrId(table.tableName + "-clear-filter-" + col.columnName)
                .attrClass("text-btn")
                .addAttr("data-testid", "clear-filter-button")
                .addAttr("data-test-column", col.columnName)     // this refers to the column where the button appears, but the button clears all filters, not just that column
                .of(hxGetAttrs(ctx.entityPath, selector.toString(), "#" + table.panelId, "click"))
                .text(ctx.words.getString("table_clear_filter"))
                .__().__();
        }
    }

}


