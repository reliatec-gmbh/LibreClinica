package org.akaza.openclinica.lctable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public class LCTableFilterDef {
    private LCTableFilterDef() {}

    public static final class Text extends LCTableFilterDef {
    }

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

    }

}
