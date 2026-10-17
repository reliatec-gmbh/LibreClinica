/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */

/*
 * LCTable client-side helpers.
 *
 * Filtering/sorting/pagination themselves are all handled server-side via HTMX (see useLCTable.jsp) --
 * most of this file only contains small, purely-cosmetic DOM helpers that cannot reasonably be done
 * in pure CSS. The one exception is the "strip empty parameters" listener directly below, which is a
 * necessary client-side companion to the URL clean-up done in LCTable.java (see LCTable.url()).
 *
 * "Smart" popup positioning: an LCTable popup (e.g. the findSubjects event-detail popup) is
 * normally left-aligned with its trigger element (class "lc-popup-trigger", containing a child
 * element with class "lc-popup"). If left-aligning it would make it overflow past the right edge
 * of the viewport, this flips it to right-align with the trigger instead (class "lc-popup-align-right"),
 * so the popup never gets truncated/cut off by the browser window edge.
 */
(function () {
    function repositionIfNeeded(trigger) {
        var popup = trigger.querySelector('.lc-popup');
        if (!popup) return;
        // Reset first, then re-measure: the trigger/popup may be reused (e.g. after an HTMX
        // morph-swap of the table), and the previous viewport width/scroll position may no longer apply.
        popup.classList.remove('lc-popup-align-right');
        if (popup.getBoundingClientRect().right > window.innerWidth) {
            popup.classList.add('lc-popup-align-right');
        }
    }

    // Event delegation on 'document' (capture phase) so this works for any current or future
    // '.lc-popup-trigger' element, without needing to attach a listener to each one individually.
    document.addEventListener('mouseover', function (event) {
        var trigger = event.target.closest('.lc-popup-trigger');
        if (trigger) repositionIfNeeded(trigger);
    }, true);

    document.addEventListener('focusin', function (event) {
        var trigger = event.target.closest('.lc-popup-trigger');
        if (trigger) repositionIfNeeded(trigger);
    }, true);
})();

/*
 * Popup "fixing" fix: an ".lc-popup-trigger" (or one of its own descendants, e.g. an actions-menu link)
 * is a focusable element (see LCPopup#render's "tabindex=0"), on purpose, so the popup can be reached
 * by keyboard (Tab) as well as by mouse hover -- the popup is shown on ":hover" OR ":focus-within" (see
 * lctable.css). The trouble: *clicking* a trigger (or a link inside its popup) with the mouse also
 * focuses it in every major browser, and that focus -- unlike hover -- does not go away merely by
 * moving the mouse elsewhere; it only goes away once something *else* receives focus. Net effect: after
 * one single click, the popup stays "fixed" open (":focus-within" keeps matching) even after the mouse
 * has moved away, and can end up stacked on top of whichever *other* trigger is now being hovered.
 *
 * Fix: when the mouse actually leaves a trigger, if the currently focused element is inside that
 * trigger, and that focus was *not* reached via the keyboard (i.e. it does not match ":focus-visible" --
 * the standard, browser-native way to distinguish "focused because Tab was pressed" from "focused
 * because it was clicked"), blur it. This leaves genuine keyboard (Tab) navigation completely
 * unaffected -- a Tab-focused trigger's popup still stays open until Tab moves focus elsewhere, exactly
 * as before -- while a mouse click no longer leaves any lasting, "fixed" popup behind once the mouse
 * moves away: hovering (or true keyboard focus) becomes the only way to keep a popup visible.
 */
(function () {
    document.addEventListener('mouseout', function (event) {
        var trigger = event.target.closest && event.target.closest('.lc-popup-trigger');
        if (!trigger) return;
        // still moving within the same trigger (e.g. into its own popup) -- nothing to do yet
        if (event.relatedTarget && trigger.contains(event.relatedTarget)) return;
        var active = document.activeElement;
        if (active && trigger.contains(active) && !active.matches(':focus-visible')) {
            active.blur();
        }
    }, true);
})();


/** Validate the five supported partial timestamp formats without timezone-dependent Date objects. */
function isValidPartialTimestamp(value) {
    var parts = /^(\d{4})(?:-(\d{2})(?:-(\d{2})(?: (\d{2})(?::(\d{2}))?)?)?)?$/.exec(value);
    var valid = value === '';
    if (parts && parts[0] === value) {
        var year = Number(parts[1]);
        var month = parts[2] === undefined ? 1 : Number(parts[2]);
        var day = parts[3] === undefined ? 1 : Number(parts[3]);
        var hour = parts[4] === undefined ? 0 : Number(parts[4]);
        var minute = parts[5] === undefined ? 0 : Number(parts[5]);
        var leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
        var daysInMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
        valid = year >= 1 && month >= 1 && month <= 12 && day >= 1 && day <= daysInMonth[month - 1]
            && hour <= 23 && minute <= 59;
    }
    return valid;
}

(function () {
    // Stable keys are shared with LCTableFilterDef.ClientValidator; never evaluate attribute values as code.
    var validators = { 'partial-timestamp': isValidPartialTimestamp };
    var selector = 'input[data-lctable-validator]';

    function validateTextFilter(input) {
        var key = input.getAttribute('data-lctable-validator');
        var message = input.getAttribute('data-lctable-validation-message');
        if (!Object.prototype.hasOwnProperty.call(validators, key)) {
            console.error('Unknown LCTable client validator: ' + key);
            input.setCustomValidity(message);
            return;
        }
        // Optional empty filters pass custom validation; native required/pattern constraints remain independent.
        var valid = input.value === '' || validators[key](input.value);
        input.setCustomValidity(valid ? '' : message);
    }

    function validateAll(root) {
        if (root.matches && root.matches(selector)) validateTextFilter(root);
        if (root.querySelectorAll) root.querySelectorAll(selector).forEach(validateTextFilter);
    }

    document.addEventListener('input', function (event) {
        if (event.target.matches(selector)) validateTextFilter(event.target);
    }, true);
    // Capture before HTMX's per-input guard; also validate included custom-validated fields for page-size/select requests.
    document.addEventListener('htmx:beforeRequest', function (event) {
        var target = event.target;
        if (!target.closest || !target.closest('.lctable')) return;
        var form = target.closest('form');
        if (!form) return;
        validateAll(form);
        var includeSource = target.closest('[hx-include]');
        if (!includeSource || includeSource.getAttribute('hx-include') !== 'closest form') return;
        var included = event.detail.requestConfig && event.detail.requestConfig.parameters;
        form.querySelectorAll(selector).forEach(function (input) {
            // Clear-filter and navigation links omit form fields; do not block them on an unsent draft value.
            if (included && Object.prototype.hasOwnProperty.call(included, input.name) && !input.validity.valid) {
                event.preventDefault();
            }
        });
    }, true);
    document.addEventListener('htmx:load', function (event) { validateAll(event.target); });
    document.addEventListener('htmx:afterSettle', function (event) { validateAll(event.target); });
    document.addEventListener('htmx:historyRestore', function () { validateAll(document); });
    window.addEventListener('pageshow', function () { validateAll(document); });
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () { validateAll(document); });
    } else {
        validateAll(document);
    }
})();



/*
 * History snapshots serialize HTML attributes, not live input values. Idiomorph deliberately leaves
 * the focused text filter's value attribute unchanged, so normalize detached snapshots from their
 * applied-filter URL instead of copying drafts from the live controls. Repair both newly created
 * items and older cached items before HTMX initializes restored inputs (including its changed guard).
 */
(function () {
    // Body history restoration can execute this script again; keep exactly one pair of handlers.
    var registration = Symbol.for('LibreClinica.LCTable.historyFilters');
    if (document[registration]) return;

    function normalizeHistoryItem(item, path, historyElt) {
        var fragment = document.createElement('template');
        // Template parsing is inert and preserves table fragments, unlike parsing into a document body.
        fragment.innerHTML = item.content;
        var withinTable = historyElt && historyElt.closest('.lctable');
        var selector = 'input.filter-input[name^="q."], select.filter-select[name^="q."]';
        var controls = fragment.content.querySelectorAll(withinTable ? selector
            : '.lctable input.filter-input[name^="q."], .lctable select.filter-select[name^="q."]');
        if (controls.length === 0) return;
        var params = new URL(path, location.origin).searchParams;
        var changed = false;
        controls.forEach(function (control) {
            // Like LCTableParams.readFilters(), use the first value without trimming or double decoding.
            var value = params.get(control.name) || '';
            if (control.matches('input')) {
                if (control.getAttribute('value') !== value) {
                    control.setAttribute('value', value);
                    changed = true;
                }
            } else {
                var options = Array.from(control.options);
                if (!options.some(function (option) { return option.value === value; })) value = '';
                options.forEach(function (option) {
                    var selected = option.value === value;
                    if (option.hasAttribute('selected') !== selected) {
                        option.toggleAttribute('selected', selected);
                        changed = true;
                    }
                });
            }
        });
        if (changed) item.content = fragment.innerHTML;
    }

    document.addEventListener('htmx:historyItemCreated', function (event) {
        var historyElt = document.querySelector('[hx-history-elt], [data-hx-history-elt]') || document.body;
        normalizeHistoryItem(event.detail.item, event.detail.item.url, historyElt);
    });
    document.addEventListener('htmx:historyCacheHit', function (event) {
        normalizeHistoryItem(event.detail.item, event.detail.path, event.detail.historyElt);
    });
    document[registration] = true;
})();



/*
 * Strip empty-valued request parameters from LCTable-issued HTMX requests.
 *
 * LCTable.java's own URL builder (LCTable.url(), used for pagination/sort/"Show More" links) already
 * omits null/empty parameters (e.g. it will never emit "sortProp=" or "showHiddenCols="): see the
 * javadoc on LCTable.url() for the rationale. However, *not every* LCTable-issued request goes through
 * that builder: the maxRows <select> and the filter inputs/selects (see LCTableFilterDef) instead use
 * HTMX's own "hx-include" mechanism (e.g. hx-include="closest form") to gather the current values of
 * sibling form controls -- this is plain HTMX/browser form serialisation, entirely bypassing
 * LCTable.url(), and it naively submits every named form control's current value, blank or not. Two
 * concrete sources of blank values:
 *  - filter <input>/<select> elements are always rendered (the user needs to see/use them, whether or
 *    not they currently hold a value), so an untouched filter legitimately serialises as "q.foo=";
 *  - LCTable.java only renders the sortProp/sortDir/showHiddenCols hidden inputs when they hold a
 *    non-default value (mirroring the same rationale as url()), which handles most cases, but doesn't
 *    (and can't) help with the filter inputs above.
 *
 * Rather than duplicating LCTable.url()'s clean-up logic in every hx-include selector (fragile, and
 * impossible for the always-rendered filter inputs), this single, generic listener removes any
 * empty-string parameter right before HTMX builds the request, for every request that originates from
 * inside an ".lctable" panel. This keeps the resulting (and pushed, see hx-push-url) URL clean
 * regardless of which mechanism (Java-built href, or HTMX form serialisation) produced it, without
 * changing which parameters are considered "empty" server-side (LCTableParams already treats a missing
 * parameter the same as an empty one).
 */
document.body.addEventListener('htmx:configRequest', function (event) {
    if (!event.target.closest || !event.target.closest('.lctable')) return;
    var params = event.detail.parameters;
    if (!params) return;
    Object.keys(params).forEach(function (key) {
        var value = params[key];
        if (Array.isArray(value)) {
            // repeated parameter name: drop only the blank entries, drop the whole key if nothing is left
            value = value.filter(function (v) { return v !== '' && v != null; });
            if (value.length === 0) {
                delete params[key];
            } else {
                params[key] = value;
            }
        } else if (value === '' || value == null) {
            delete params[key];
        }
    });
});



/*
 * Custom tooltip for icon-only action links (elements with a "data-tooltip" attribute -- see
 * LCTableUtil.actionLink()), replacing the native "title"-attribute tooltip.
 *
 * Two problems with relying on the native title tooltip (and, before this, on a pure-CSS "::after"
 * bubble anchored to the link itself) motivated this:
 *  - the browser's native per-window title-tooltip hover timer is known to stop reliably reappearing
 *    once the browser window has lost and regained focus;
 *  - a "::after" bubble, being a descendant of a scrolling ancestor (the event-detail popup's
 *    scrollable body), would get silently clipped by that ancestor's "overflow-y: auto" whenever there
 *    wasn't enough room left within the *scrollable container* -- which is exactly what happened for
 *    the last occurrence's actions (nothing below them to give the downward-pointing bubble room).
 *
 * The fix: a single reusable tooltip element ("#lc-tooltip"), created once and appended directly to
 * <body> (i.e. NOT a descendant of any scrolling/clipping ancestor), positioned with "position: fixed"
 * from the hovered/focused element's getBoundingClientRect(). This can never be clipped by an ancestor,
 * regardless of which row/action triggered it, and does not depend on any native per-window timer.
 */
(function () {
    var tooltipEl = null;
    var tooltipSelector = '[data-tooltip], [data-tooltip-source]';

    function getTooltipEl() {
        if (!tooltipEl) {
            tooltipEl = document.createElement('div');
            tooltipEl.className = 'lc-tooltip';
            tooltipEl.setAttribute('id', 'lc-tooltip');
            tooltipEl.setAttribute('role', 'tooltip');
            document.body.appendChild(tooltipEl);
        }
        return tooltipEl;
    }

    function positionTooltip(target, el) {
        var targetRect = target.getBoundingClientRect();
        var tipRect = el.getBoundingClientRect();
        var gap = 6;

        // prefer below the target; flip above it if there is not enough room below the viewport
        var top = targetRect.bottom + gap;
        if (top + tipRect.height > window.innerHeight) {
            top = targetRect.top - tipRect.height - gap;
        }
        top = Math.max(2, top);

        // center horizontally on the target, clamped to stay within the viewport
        var left = targetRect.left + (targetRect.width / 2) - (tipRect.width / 2);
        left = Math.max(2, Math.min(left, window.innerWidth - tipRect.width - 2));

        el.style.top = top + 'px';
        el.style.left = left + 'px';
    }

    function showTooltip(target) {
        var text = target.getAttribute('data-tooltip');
        var sourceId = target.getAttribute('data-tooltip-source');
        var source = sourceId ? document.getElementById(sourceId) : null;
        if (!text && !source) return;
        var el = getTooltipEl();
        el.textContent = source ? '' : text;
        el.classList.toggle('lc-tooltip-filters', !!source);
        if (source) {
            Array.from(source.childNodes).forEach(function (node) { el.appendChild(node.cloneNode(true)); });
        }
        el.classList.add('lc-tooltip-visible');
        positionTooltip(target, el);
    }

    function hideTooltip() {
        if (tooltipEl) tooltipEl.classList.remove('lc-tooltip-visible');
    }

    document.addEventListener('mouseover', function (event) {
        var target = event.target.closest(tooltipSelector);
        if (target) showTooltip(target);
    }, true);

    document.addEventListener('mouseout', function (event) {
        var target = event.target.closest(tooltipSelector);
        // don't hide if the pointer merely moved onto a descendant of the same tooltipped element
        if (target && (!event.relatedTarget || !target.contains(event.relatedTarget))) hideTooltip();
    }, true);

    document.addEventListener('focusin', function (event) {
        var target = event.target.closest(tooltipSelector);
        if (target) showTooltip(target);
    }, true);

    document.addEventListener('focusout', function (event) {
        var target = event.target.closest(tooltipSelector);
        if (target) hideTooltip();
    }, true);

    // A tooltip's position is only computed once, when it is shown: if the user scrolls (e.g. the
    // event-detail popup's internal body) while it is visible, just hide it rather than letting it
    // float, stale, over unrelated content.
    window.addEventListener('scroll', hideTooltip, true);
    window.addEventListener('resize', hideTooltip);
    document.addEventListener('keydown', function (event) { if (event.key === 'Escape') hideTooltip(); });
    document.addEventListener('htmx:beforeSwap', hideTooltip);
})();


