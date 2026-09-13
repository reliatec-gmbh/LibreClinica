/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.lctable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/** Immutable human-facing text that is either a resource key or a literal value. */
public final class LCTableText {
    private final String value;
    private final boolean resourceKey;
    private final List<String> arguments;

    private LCTableText(String value, boolean resourceKey, List<String> arguments) {
        this.value = Objects.requireNonNull(value, resourceKey ? "resourceKey" : "text");
        if (value.isEmpty()) {
            throw new IllegalArgumentException(resourceKey ? "resourceKey must not be empty" : "text must not be empty");
        }
        this.resourceKey = resourceKey;
        this.arguments = List.copyOf(arguments);
    }

    public static LCTableText key(String resourceKey) {
        return new LCTableText(resourceKey, true, List.of());
    }

    public static LCTableText formattedKey(String resourceKey, String argument) {
        return new LCTableText(resourceKey, true, List.of(Objects.requireNonNull(argument, "argument")));
    }

    public static LCTableText literal(String text) {
        return new LCTableText(text, false, List.of());
    }

    public String resolve(ResourceBundle words, Locale locale) {
        Objects.requireNonNull(words, "words");
        Objects.requireNonNull(locale, "locale");
        if (!resourceKey) {
            return value;
        }
        String pattern = words.getString(value);
        return arguments.isEmpty() ? pattern : new MessageFormat(pattern, locale).format(arguments.toArray(new String[0]));
    }

    public Optional<String> resourceKey() {
        return resourceKey ? Optional.of(value) : Optional.empty();
    }
}
