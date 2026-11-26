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

/** A single pagination slot (page number or ellipsis). */
public final class LCTablePageSlot {

	private final int page;
	private final boolean ellipsis;
	private final boolean current;

	public LCTablePageSlot(int page, boolean ellipsis, boolean current) {
		this.page = page;
		this.ellipsis = ellipsis;
		this.current = current;
	}

	/** Create a numbered page slot. */
	public static LCTablePageSlot forPage(int p, boolean current) {
		return new LCTablePageSlot(p, false, current);
	}

	/** Create an ellipsis placeholder slot. */
	public static LCTablePageSlot forEllipsis() {
		return new LCTablePageSlot(-1, true, false);
	}

	public int page() {
		return page;
	}

	public boolean ellipsis() {
		return ellipsis;
	}

	public boolean current() {
		return current;
	}

	@Override
	public String toString() {
		return "LCTablePageSlot[page=" + page + ", ellipsis=" + ellipsis + ", current=" + current + "]";
	}

}
