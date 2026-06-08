package org.akaza.openclinica.lctable;

import java.util.Objects;

/**
 * One slot in the pagination window used by the lctable renderer.
 *
 * <p>Immutable, but implemented as a plain class (not a record) for Java 11 compatibility.
 */
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LCTablePageSlot that = (LCTablePageSlot) o;
		return page == that.page && ellipsis == that.ellipsis && current == that.current;
	}

	@Override
	public int hashCode() {
		return Objects.hash(page, ellipsis, current);
	}

	@Override
	public String toString() {
		return "LCTablePageSlot[page=" + page + ", ellipsis=" + ellipsis + ", current=" + current + "]";
	}

}
