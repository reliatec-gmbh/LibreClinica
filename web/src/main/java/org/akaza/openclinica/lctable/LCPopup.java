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

import org.xmlet.htmlapifaster.Div;
import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.FlowContent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.akaza.openclinica.lctable.LCTableUtil.actionLink;

/**
 * A generic, {@code LCTable}-style popup abstraction: a small hover/focus-triggered popup attached to a trigger
 * element (typically a status icon in a table cell), showing a header and a scrollable list of "occurrence rows"
 * (each with status content and a handful of gated action links).
 *
 * <p>Factors out the purely <em>mechanical</em> rendering (trigger + header + scrollable body + per-item rows +
 * gated action links) shared by every popup currently in the codebase (the {@code findSubjects} "Subject Matrix"
 * aggregate event-status popup, the {@code listEventsForSubjects} per-occurrence event-status popup, and its
 * per-occurrence CRF-status popup); all business logic -- which actions are visible, in what order, gated by which
 * role/status, and how the status line(s) look -- stays in the {@link PopupItemRenderer} supplied by the caller
 * (see {@code EventStatusPopup}/{@code CrfStatusPopup}, in {@code org.akaza.openclinica.control.popup}, for the
 * concrete application-specific specializations).
 *
 * @param <T> the popup's own "subject" type -- what row/cell it is attached to, plus whatever else the caller's
 *            {@link PopupItemRenderer} needs (e.g. a study subject bean)
 * @param <I> the item type listed in the popup body, one per rendered occurrence row
 */
public class LCPopup<T, I> {

    private final Function<T, List<I>> itemsExtractor;
    private final BiConsumer<Div<?>, List<I>> triggerRenderer;
    private final BiConsumer<Div<?>, T> headerRenderer;
    private final Optional<BiConsumer<Div<?>, T>> extraHeaderControl;
    private final PopupItemRenderer<T, I> itemRenderer;
    private final String triggerClass;
    private final String popupModifierClass;

    /**
     * @param itemsExtractor      {@code T -> } the (already-fetched/sorted, never empty) items to list in the popup
     *                            body
     * @param triggerRenderer     renders the trigger's own icon(s)/badge(s) from the resolved items list (e.g. one
     *                            badge per distinct status, +count, for an aggregate popup; a single badge for a
     *                            singleton items list)
     * @param headerRenderer      renders the popup header (e.g. "Subject: X / Event: Y") from {@code T}
     * @param extraHeaderControl  optional extra header control (e.g. "add another occurrence"), rendered right
     *                            after {@code headerRenderer}
     * @param itemRenderer        renders each item's status content, actions and layout (see {@link PopupItemRenderer})
     * @param triggerClass        extra CSS class applied to the trigger {@code <div>} alongside the generic
     *                            {@code "lc-popup-trigger"} (e.g. {@code "event-trigger"}/{@code "crf-trigger"} --
     *                            these stay table/kind-specific on purpose, since trigger *layout* -- one aggregate
     *                            trigger per cell vs. one per occurrence stacked vertically -- is not unified here)
     * @param popupModifierClass  extra CSS class applied to the popup box itself (e.g. {@code "ViewSubjectsPopup"})
     */
    public LCPopup(Function<T, List<I>> itemsExtractor, BiConsumer<Div<?>, List<I>> triggerRenderer,
            BiConsumer<Div<?>, T> headerRenderer, Optional<BiConsumer<Div<?>, T>> extraHeaderControl,
            PopupItemRenderer<T, I> itemRenderer, String triggerClass, String popupModifierClass) {
        this.itemsExtractor = Objects.requireNonNull(itemsExtractor, "itemsExtractor");
        this.triggerRenderer = Objects.requireNonNull(triggerRenderer, "triggerRenderer");
        this.headerRenderer = Objects.requireNonNull(headerRenderer, "headerRenderer");
        this.extraHeaderControl = Objects.requireNonNull(extraHeaderControl, "extraHeaderControl");
        this.itemRenderer = Objects.requireNonNull(itemRenderer, "itemRenderer");
        this.triggerClass = Objects.requireNonNull(triggerClass, "triggerClass");
        this.popupModifierClass = Objects.requireNonNull(popupModifierClass, "popupModifierClass");
    }

    /**
     * Renders one full "trigger + popup" unit into an existing container (typically a {@code <td>}; calling this
     * more than once on the same container, as {@code LCTablePopupColumn.perOccurrence} does, stacks multiple
     * triggers as siblings, exactly as the pre-{@code LCPopup} per-occurrence markup did).
     */
    public <C extends Element<C, Z> & FlowContent<C, Z>, Z extends Element> void render(C container, T data) {
        List<I> items = itemsExtractor.apply(data);
        container.div().attrClass(triggerClass.isEmpty() ? "lc-popup-trigger" : triggerClass + " lc-popup-trigger").addAttr("tabindex", "0")
            .addAttr("data-testid", "lc-popup-trigger")
            .of(trigger -> {
                triggerRenderer.accept(trigger, items);
                trigger.div().attrClass("lc-popup " + popupModifierClass)
                    .addAttr("data-testid", "lc-popup")
                    .of(popup -> {
                        popup.div().attrClass("lc-popup-header table_header_row_left")
                            .addAttr("data-testid", "lc-popup-header")
                            .of(header -> {
                                headerRenderer.accept(header, data);
                                extraHeaderControl.ifPresent(control -> control.accept(header, data));
                            })
                            .__();
                        popup.div().attrClass("lc-popup-body")
                            .addAttr("data-testid", "lc-popup-body")
                            .of(body -> {
                                int total = items.size();
                                for (int i = 0; i < total; i++) {
                                    renderItemRow(body, data, items.get(i), i, total);
                                }
                            })
                            .__();
                    })
                    .__();
            })
            .__();
    }

    private void renderItemRow(Div<?> body, T context, I item, int index, int total) {
        boolean compact = itemRenderer.layoutFor(context, item) == PopupItemLayout.COMPACT;
        body.div().attrClass(compact ? "lc-popup-item lc-popup-item-compact" : "lc-popup-item")
            .addAttr("data-testid", "lc-popup-item")
            .addAttr("data-test-index", String.valueOf(index + 1))
            .of(row -> {
                itemRenderer.renderStatus(row, context, item, index, total);
                // generic layout chrome owned by LCPopup itself: separates the status content above from the
                // actions menu below, in both SIMPLE and COMPACT layout
                row.div().attrClass("lc-popup-row-separator").__();
                row.div().attrClass(compact ? "lc-popup-actions lc-popup-actions-compact" : "lc-popup-actions lc-popup-actions-vertical")
                    .of(actions -> {
                        if (compact) {
                            actions.text("Actions: ");   // generic layout chrome owned by LCPopup itself; i18n deferred (see LCTable)
                        }
                        for (PopupAction action : itemRenderer.actionsFor(context, item)) {
                            actions.of(actionLink(action.elementId, action.label, action.href, action.iconImage, action.showLabel, action.actionName));
                        }
                    })
                    .__();
            })
            .__();
    }

    /**
     * How one item row is rendered inside an {@link LCPopup} body.
     *
     * <ul>
     * <li>{@link #SIMPLE}: one status line (or a couple of lines, e.g. a date line followed by a status line), then
     * one action per line (icon + visible label) -- used for non-repeating items, and for every item when a popup
     * never lists more than one occurrence at a time (e.g. {@code listEventsForSubjects}'s one-popup-per-occurrence
     * layout).</li>
     * <li>{@link #COMPACT}: a single "Occurrence #i of N &middot; date &middot; status" summary line, followed by
     * one line of icon-only actions -- used for repeating items shown all together in one aggregate popup (e.g.
     * {@code findSubjects}'s per-column aggregate layout).</li>
     * </ul>
     */
    public enum PopupItemLayout {
        SIMPLE,
        COMPACT
    }

    /**
     * Fully generic, per-item rendering hook for an {@link LCPopup}: renders the "business" content of one
     * occurrence row (status line(s) and gated action links), given both the popup's own subject data ({@code T})
     * and the item itself ({@code I}). Kept in the same spirit as {@code LCTableColumnDef.cellRenderer}:
     * {@link LCPopup} only knows how to lay the content out (see {@link PopupItemLayout}); every actual
     * status/action-gating decision is made by the implementation supplied by the calling table (e.g.
     * {@code EventStatusPopup}, {@code CrfStatusPopup}, in {@code org.akaza.openclinica.control.popup}).
     *
     * @param <T> the popup's own subject type (e.g. a study subject, plus whatever context an implementation needs
     *            to build action hrefs/gate visibility -- see {@link LCPopup})
     * @param <I> the item type listed in the popup body (one per rendered occurrence row)
     */
    public interface PopupItemRenderer<T, I> {

        /**
         * Renders the status line(s)/summary content for one item -- everything above the actions in an occurrence row.
         *
         * @param row     the occurrence row's container to render into
         * @param context the popup's own subject data
         * @param item    the item being rendered
         * @param index   the item's 0-based position within the full items list (used by {@link PopupItemLayout#COMPACT}
         *                to render "Occurrence #i of N")
         * @param total   the total number of items in the full items list
         */
        void renderStatus(Div<?> row, T context, I item, int index, int total);

        /** The gated, ordered list of actions to render for one item (role/status checks already resolved by the caller). */
        List<PopupAction> actionsFor(T context, I item);

        /** {@link PopupItemLayout#SIMPLE} vs {@link PopupItemLayout#COMPACT} for this item. */
        PopupItemLayout layoutFor(T context, I item);
    }

    /**
     * One gated action link inside an {@link LCPopup} occurrence row (e.g. "view", "edit", "remove"). Immutable,
     * typed carrier for the same arguments {@code LCTableUtil.actionLink(...)} already takes, so that a
     * {@code Function<I, List<PopupAction>>}-shaped closure (see {@link PopupItemRenderer#actionsFor}) can be
     * handed to {@link LCPopup}, which renders each action generically, instead of every table hand-rolling the
     * same {@code actions.of(actionLink(...))} sequence.
     */
    public static final class PopupAction {

        /** Unique HTML element id, or {@code null} if none. */
        public final String elementId;
        /** e.g. {@code resword.getString("view")} -- i18n stays the caller's responsibility. */
        public final String label;
        /** The fully-built href (already percent-encoded); see {@link #of} / {@link #ofRawHref}. */
        public final String href;
        /** e.g. {@code "bt_View.gif"}. */
        public final String iconImage;
        /** if true, renders icon + visible text label; if false, icon-only (with a tooltip, see {@code LCTableUtil.actionLink}). */
        public final boolean showLabel;
        /** Stable, locale-independent action name used by automated tests. */
        public final String actionName;

        private PopupAction(String elementId, String label, String href, String iconImage, boolean showLabel, String actionName) {
            this.elementId = elementId;
            this.label = label;
            this.href = href;
            this.iconImage = iconImage;
            this.showLabel = showLabel;
            this.actionName = actionName;
        }

        /** Builds a {@code PopupAction} from a {@link SafeUrl} (the common case). */
        public static PopupAction of(String elementId, String label, SafeUrl href, String iconImage, boolean showLabel, String actionName) {
            return new PopupAction(elementId, label, href.toUriString(), iconImage, showLabel, actionName);
        }

        /**
         * Builds a {@code PopupAction} from an already-built, pre-encoded href string (e.g. the REST "print" links,
         * whose path segments must not be re-encoded by {@link SafeUrl}).
         */
        public static PopupAction ofRawHref(String elementId, String label, String href, String iconImage, boolean showLabel, String actionName) {
            return new PopupAction(elementId, label, href, iconImage, showLabel, actionName);
        }
    }
}

