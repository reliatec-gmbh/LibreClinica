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

    function getTooltipEl() {
        if (!tooltipEl) {
            tooltipEl = document.createElement('div');
            tooltipEl.className = 'lc-tooltip';
            tooltipEl.setAttribute('id', 'lc-tooltip');
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
        if (!text) return;
        var el = getTooltipEl();
        el.textContent = text;
        el.classList.add('lc-tooltip-visible');
        positionTooltip(target, el);
    }

    function hideTooltip() {
        if (tooltipEl) tooltipEl.classList.remove('lc-tooltip-visible');
    }

    document.addEventListener('mouseover', function (event) {
        var target = event.target.closest('[data-tooltip]');
        if (target) showTooltip(target);
    }, true);

    document.addEventListener('mouseout', function (event) {
        var target = event.target.closest('[data-tooltip]');
        // don't hide if the pointer merely moved onto a descendant of the same tooltipped element
        if (target && (!event.relatedTarget || !target.contains(event.relatedTarget))) hideTooltip();
    }, true);

    document.addEventListener('focusin', function (event) {
        var target = event.target.closest('[data-tooltip]');
        if (target) showTooltip(target);
    }, true);

    document.addEventListener('focusout', function (event) {
        var target = event.target.closest('[data-tooltip]');
        if (target) hideTooltip();
    }, true);

    // A tooltip's position is only computed once, when it is shown: if the user scrolls (e.g. the
    // event-detail popup's internal body) while it is visible, just hide it rather than letting it
    // float, stale, over unrelated content.
    window.addEventListener('scroll', hideTooltip, true);
})();


