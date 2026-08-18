/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Typed row for the "listEventsForSubject" table (LCTable-based {@link ListEventsForSubjectTable}),
 * replacing the legacy {@code HashMap<Object,Object>} "pseudo-row" + inner {@code DisplayBean} pattern
 * used by {@code ListEventsForSubjectTableFactory} (jmesa).
 *
 * <p>Unlike {@code FindSubjectsRow} (used by the "findSubjects" table), this table is already scoped to
 * a single selected {@code StudyEventDefinitionBean}, so instead of one entry per event-definition column,
 * each row carries a list of {@link OccurrenceData}, one per occurrence of that single event definition
 * (or a single synthetic "not scheduled" placeholder occurrence if there are none).
 */
public final class ListEventsForSubjectRow {

    public final StudySubjectBean studySubject;
    public final SubjectBean subject;
    public final String enrolledAt;                                       // site/study identifier this subject is enrolled at
    public final Map<Integer, GroupAssignment> groupAssignmentsByClassId;  // studyGroupClassId -> group assignment (if any)
    public final List<OccurrenceData> occurrences;                        // always >= 1 (synthetic "not scheduled" placeholder when empty)

    public ListEventsForSubjectRow(StudySubjectBean studySubject, SubjectBean subject, String enrolledAt,
            Map<Integer, GroupAssignment> groupAssignmentsByClassId, List<OccurrenceData> occurrences) {
        this.studySubject = studySubject;
        this.subject = subject;
        this.enrolledAt = enrolledAt;
        this.groupAssignmentsByClassId = groupAssignmentsByClassId;
        this.occurrences = occurrences;
    }

    /** The study-group the subject is assigned to, within a given study-group-class. */
    public static final class GroupAssignment {
        public final Integer groupId;
        public final String groupName;

        public GroupAssignment(Integer groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }
    }

    /** One occurrence (real or synthetic "not scheduled" placeholder) of the selected event definition, for one subject. */
    public static final class OccurrenceData {
        public final StudyEventBean studyEvent;                  // null for the synthetic "not scheduled" placeholder
        public final SubjectEventStatus eventStatus;
        public final Date eventDate;
        public final Map<Integer, CrfOccurrenceData> crfByCrfId; // keyed by crf.getId()

        public OccurrenceData(StudyEventBean studyEvent, SubjectEventStatus eventStatus, Date eventDate,
                Map<Integer, CrfOccurrenceData> crfByCrfId) {
            this.studyEvent = studyEvent;
            this.eventStatus = eventStatus;
            this.eventDate = eventDate;
            this.crfByCrfId = crfByCrfId;
        }
    }

    /** CRF-column data for one occurrence x one CRF column. */
    public static final class CrfOccurrenceData {
        public final CRFBean crf;
        public final EventDefinitionCRFBean eventDefinitionCrf;
        public final EventCRFBean eventCrf;   // nullable: no event-CRF exists yet for this occurrence/CRF
        public final DataEntryStage stage;

        public CrfOccurrenceData(CRFBean crf, EventDefinitionCRFBean eventDefinitionCrf, EventCRFBean eventCrf, DataEntryStage stage) {
            this.crf = crf;
            this.eventDefinitionCrf = eventDefinitionCrf;
            this.eventCrf = eventCrf;
            this.stage = stage;
        }
    }

}

