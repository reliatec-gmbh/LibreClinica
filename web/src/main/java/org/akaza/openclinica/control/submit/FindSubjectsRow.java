/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.List;
import java.util.Map;

/**
 * Typed row for the "findSubjects" table (LCTable-based {@link ListStudySubjectTable}),
 * replacing the legacy {@code HashMap<Object,Object>} "pseudo-row" pattern used by
 * {@code ListStudySubjectTableFactory} (jmesa).
 */
public final class FindSubjectsRow {

    public final StudySubjectBean studySubject;
    public final SubjectBean subject;
    public final String enrolledAt;                                          // site/study identifier this subject is enrolled at
    public final boolean isSignable;
    public final Map<Integer, GroupAssignment> groupAssignmentsByClassId;     // studyGroupClassId -> group assignment (if any)
    public final Map<Integer, EventColumnData> eventDataByDefinitionId;       // studyEventDefinitionId -> event column data

    public FindSubjectsRow(StudySubjectBean studySubject, SubjectBean subject, String enrolledAt, boolean isSignable,
            Map<Integer, GroupAssignment> groupAssignmentsByClassId, Map<Integer, EventColumnData> eventDataByDefinitionId) {
        this.studySubject = studySubject;
        this.subject = subject;
        this.enrolledAt = enrolledAt;
        this.isSignable = isSignable;
        this.groupAssignmentsByClassId = groupAssignmentsByClassId;
        this.eventDataByDefinitionId = eventDataByDefinitionId;
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

    /** Aggregate status + occurrences for one study-event-definition column, for one subject. */
    public static final class EventColumnData {
        public final SubjectEventStatus aggregateStatus;   // status of the first occurrence, or NOT_SCHEDULED if none
        public final List<StudyEventBean> occurrences;
        public final StudyEventDefinitionBean definition;

        public EventColumnData(SubjectEventStatus aggregateStatus, List<StudyEventBean> occurrences, StudyEventDefinitionBean definition) {
            this.aggregateStatus = aggregateStatus;
            this.occurrences = occurrences;
            this.definition = definition;
        }
    }

}
