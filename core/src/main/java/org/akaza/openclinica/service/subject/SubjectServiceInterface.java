/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.Date;
import java.util.List;

public interface SubjectServiceInterface {

    public abstract String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId);

    public List<StudySubjectBean> getStudySubject(StudyBean study);

}