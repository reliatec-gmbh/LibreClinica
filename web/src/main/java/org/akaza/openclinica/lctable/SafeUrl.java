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

import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Function;

public final class SafeUrl {
    private final UriComponentsBuilder builder;

    private SafeUrl(String basePath) {
        this.builder = UriComponentsBuilder.fromUriString(basePath);
    }

    public static SafeUrl url(String basePath) {
        return new SafeUrl(basePath);
    }

    // 1. Accept strings (the builder will handle the URL escaping)
    public SafeUrl param(String name, String value) {
        if (value != null && !value.isEmpty()) {
            this.builder.queryParam(name, value);
        }
        return this;
    }

    // 2. Accept specific primitives that we know how to handle safely (the builder will handle the URL escaping)
    public SafeUrl param(String name, int value) {
        this.builder.queryParam(name, value);
        return this;
    }

    // Boxed Integer overload: needed whenever the value may legitimately be null (e.g. a foreign-key id that no
    // longer resolves, such as AuditUserLoginBean#getUserAccountId() for a login attempt with an unknown/deleted
    // user account). Without this overload, callers passing an Integer would silently fall back to the int
    // overload above via auto-unboxing, throwing a NullPointerException whenever the value is null.
    public SafeUrl param(String name, Integer value) {
        if (value != null) {
            this.builder.queryParam(name, value);
        }
        return this;
    }

    public SafeUrl param(String name, long value) {
        this.builder.queryParam(name, value);
        return this;
    }

    // Boxed Long overload: same rationale as the boxed Integer overload above -- protects any current/future
    // caller passing a nullable Long (e.g. a Hibernate-generated Long id, or a boxed getter) from an
    // auto-unboxing NullPointerException. No current caller happens to pass a nullable Long, but this closes
    // the gap for consistency and defense-in-depth, exactly like the Integer overload.
    public SafeUrl param(String name, Long value) {
        if (value != null) {
            this.builder.queryParam(name, value);
        }
        return this;
    }

    public SafeUrl param(String name, boolean value) {
        this.builder.queryParam(name, value);
        return this;
    }

    // Boxed Boolean overload: same rationale as the boxed Integer/Long overloads above.
    public SafeUrl param(String name, Boolean value) {
        if (value != null) {
            this.builder.queryParam(name, value);
        }
        return this;
    }

    // 3. Accept Enums safely by using their name or a custom enum-to-string conversion function (the builder will handle the URL escaping)
    public SafeUrl param(String name, Enum<?> value) {
        if (value != null) {
            this.builder.queryParam(name, value.name());
        }
        return this;
    }

    public SafeUrl param(String name, Enum<?> value, Function<Enum<?>, String> enumToString) {
        if (value != null) {
            this.builder.queryParam(name, enumToString.apply(value));
        }
        return this;
    }

    // Other types (e.g., Date, arrays) are not supported and must be converted to String or primitives by the caller.

    public String toUriString() {
        return this.builder.build().encode().toUriString();
    }
}
