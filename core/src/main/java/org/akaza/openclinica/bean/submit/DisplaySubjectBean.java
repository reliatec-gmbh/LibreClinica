/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.EntityBean;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DisplaySubjectBean extends EntityBean {
    private SubjectBean subject;
    private String studySubjectIds;

    /**
     * @return Returns the studySubjectIds.
     */
    public String getStudySubjectIds() {
        return studySubjectIds;
    }

    /**
     * @param studySubjectIds
     *            The studySubjectIds to set.
     */
    public void setStudySubjectIds(String studySubjectIds) {
        this.studySubjectIds = studySubjectIds;
    }

    /**
     * @return Returns the subject.
     */
    public SubjectBean getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            The subject to set.
     */
    public void setSubject(SubjectBean subject) {
        this.subject = subject;
    }
}
