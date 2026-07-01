package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.EnumTypeInputType;
import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.Tr;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public abstract class LCTableFilterDef {
    protected LCTableFilterDef() {}

    public static final LCTableFilterDef noFilter = null;

    /*** Factory methods for creating a text filter definition. */
    public static Text textFilter() { return new Text(); }
    public static Text textFilter(String pattern) { return new Text(pattern, null); }
    public static Text textFilter(String pattern, String message) { return new Text(pattern, message); }

    /*** Factory methods for creating a select filter definition. */
    public static <T> Select<T> selectFilter(List<T> values, Function<T, String> valueToString) {
        return new Select<>(values, valueToString);
    }

    /*** Signature of rendering function (to be implemented by specific filter def types) */
    public abstract <T1, R extends Element<?, ?>> void renderFilter(Tr<R> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col, LCTable<T1> table);

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
        public <T1, R extends Element<?, ?>> void renderFilter(Tr<R> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col, LCTable<T1> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String filterValue = ctx.filters.getOrDefault(col.columnName, "");

            String trigger = "input changed delay:400ms";

            var input = tr.td().input()
                .attrType(EnumTypeInputType.TEXT)
                .attrName(filterName)
                .attrValue(filterValue)
                .attrClass("filter-input")
                .attrId(table.panelId + "-filter-" + col.columnName);

            // If a pattern is provided, add HTML5 validation and HTMX event filter
            if (this.pattern != null) {
                input.attrPattern(this.pattern);
                if (this.message != null) {
                    input.attrTitle(this.message);
                }
                // ONLY fire HTMX if the HTML5 validity state is 'valid'
                trigger = "input[this.validity.valid] changed delay:400ms";
            }

            input.addAttr(LCTable.HX_GET, table.entityPath)
                .addAttr(LCTable.HX_TARGET, "#" + table.panelId)
                .addAttr(LCTable.HX_SWAP, "outerHTML")
                .addAttr(LCTable.HX_PUSH_URL, "true")
                .addAttr(LCTable.HX_TRIGGER, trigger)
                .addAttr(LCTable.HX_INCLUDE, "closest form")
                .__().__();
        }
    }

    /**
     * Select filter specialization: renders a <select> with provided values
     */
    public static final class Select<T> extends LCTableFilterDef {
        public final List<T> values;
        public final Function<T, String> valueToString;

        public Select(List<T> values, Function<T, String> valueToString) {
            this.values = values;
            this.valueToString = valueToString;
        }

        /**
         * Returns the display label for a given optional value.
         */
        public String label(T value) {
            return Optional.ofNullable(value).map(this.valueToString).orElse("");
        }

        /**
         * Reconstructs the strongly-typed domain object from an HTTP query parameter string.
         */
        public Optional<T> parseParam(String paramValue) {
            if (paramValue == null || paramValue.isEmpty()) {
                return Optional.empty(); // No filtering requested
            } else {
                // Find the domain object whose string representation matches the submitted text
                return values.stream().filter(val -> label(val).equals(paramValue)).findFirst();       // empty if nothing found
            }
        }

        @Override
        public <T1, R extends Element<?, ?>> void renderFilter(Tr<R> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col, LCTable<T1> table) {
            final String filterName = LCTableParams.PARAM_FILTER_PREFIX + col.columnName;
            final String rawSelected = ctx.filters.getOrDefault(col.columnName, "");

            // UI Safety Check: Verify if the URL parameter actually matches a real option
            final boolean isValidOption = rawSelected.isEmpty() || this.values.stream()
                .map(this::label)
                .anyMatch(label -> label.equals(rawSelected));

            // If it's invalid (e.g., "broken"), treat it as empty ("All") so the UI snaps back to a valid state
            final String currentSelected = isValidOption ? rawSelected : "";

            org.xmlet.htmlapifaster.Select<Td<Tr<R>>> select = tr.td().select()
                .attrName(filterName)
                .attrId(table.panelId + "-filter-" + col.columnName)
                .attrClass("filter-select")
                .addAttr(LCTable.HX_GET, table.entityPath)
                .addAttr(LCTable.HX_TARGET, "#" + table.panelId)
                .addAttr(LCTable.HX_SWAP, "outerHTML")
                .addAttr(LCTable.HX_PUSH_URL, "true")
                .addAttr(LCTable.HX_TRIGGER, "change")
                .addAttr(LCTable.HX_INCLUDE, "closest form");

            select.option().attrValue("").attrSelected(currentSelected.isEmpty()).text("").__();
            this.values.forEach(option -> {
                final String labelText = this.label(option);
                boolean isSelected = labelText.equals(currentSelected);
                select.option().attrValue(labelText).attrSelected(isSelected).text(labelText).__();
            });
            select.__().__();
        }
    }

}
