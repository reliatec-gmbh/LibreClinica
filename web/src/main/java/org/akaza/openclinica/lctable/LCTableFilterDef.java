package org.akaza.openclinica.lctable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public class LCTableFilterDef {
    private LCTableFilterDef() {}

    public static final class Text extends LCTableFilterDef {
//        public final String placeholder;
//
//        public Text(String placeholder) {
//            this.placeholder = placeholder;
//        }
    }

    public static final class Select<T> extends LCTableFilterDef {
        public final List<T> values;
        public final Function<T, String> valueToString;
        public final String emptyLabel;

        public Select(List<T> values, Function<T, String> valueToString, String emptyLabel) {
            this.values = values;
            this.valueToString = valueToString;
            this.emptyLabel = emptyLabel;
        }

        /**
         * Returns the display label for a given optional value.
         */
        public String label(Optional<T> optValue) {
            return optValue.map(this.valueToString).orElse(this.emptyLabel);
        }

        /**
         * Reconstructs the strongly-typed domain object from an HTTP query parameter string.
         */
        public Optional<T> parseParam(String paramValue) {
            if (paramValue == null || paramValue.trim().isEmpty() || paramValue.equals(emptyLabel)) {
                return Optional.empty(); // No filtering requested
            }

            // Find the domain object whose string representation matches the submitted text
            return values.stream()
                .filter(val -> label(Optional.ofNullable(val)).equals(paramValue))
                .findFirst();       // empty if nothing found
        }

    }

}
