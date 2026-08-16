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

import org.xmlet.htmlapifaster.Td;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Adapts an {@link LCPopup} into a {@code BiConsumer<Td<?>, RowType>} usable as an
 * {@code LCTableColumnDef.customTdCol(...)} cell renderer. The two current tables differ in exactly one respect --
 * how many popups appear per cell -- which is exactly what {@link #single} vs {@link #perOccurrence} capture; the
 * {@link LCPopup} instance itself (header/status/actions rendering) can be shared unchanged between both.
 */
public final class LCTablePopupColumn {
    private LCTablePopupColumn() {}

    /** One aggregate popup per cell (e.g. {@code findSubjects}'s per-event-definition-column popup). */
    public static <RowType, T> BiConsumer<Td<?>, RowType> single(LCPopup<T, ?> popup, Function<RowType, T> dataExtractor) {
        return (td, row) -> popup.render(td, dataExtractor.apply(row));
    }

    /**
     * One popup per occurrence, stacked vertically inside the cell (e.g. {@code listEventsForSubjects}'s Event
     * Status / CRF columns). Each occurrence's trigger is rendered as a direct sibling {@code <div>} within the
     * cell (see {@link LCPopup#render}); the stacking/spacing itself is pure CSS (see {@code lctable.css}'s
     * {@code #listEventsForSubject-panel} rules), keyed off the trigger's own CSS class.
     */
    public static <RowType, T> BiConsumer<Td<?>, RowType> perOccurrence(LCPopup<T, ?> popup, Function<RowType, List<T>> dataListExtractor) {
        return (td, row) -> dataListExtractor.apply(row).forEach(data -> popup.render(td, data));
    }
}

