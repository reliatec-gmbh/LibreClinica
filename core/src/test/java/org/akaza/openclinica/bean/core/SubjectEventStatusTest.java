/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.bean.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SubjectEventStatusTest {

    private static final List<SubjectEventStatus> CLOSED = Arrays.asList(SubjectEventStatus.LOCKED, SubjectEventStatus.SIGNED,
            SubjectEventStatus.STOPPED);

    @Test
    public void closedForImportIsLockedSignedOrStopped() {
        for (SubjectEventStatus status : SubjectEventStatus.toArrayList()) {
            assertEquals("status id " + status.getId(), CLOSED.contains(status), status.isClosedForImport());
        }
        assertEquals(false, SubjectEventStatus.INVALID.isClosedForImport());
    }

    @Test
    public void closedForImportComparesById() {
        // getFromMap returns a new instance, not the constant
        assertEquals(true, SubjectEventStatus.getFromMap(SubjectEventStatus.LOCKED.getId()).isClosedForImport());
        assertEquals(false, SubjectEventStatus.getFromMap(SubjectEventStatus.COMPLETED.getId()).isClosedForImport());
    }
}
