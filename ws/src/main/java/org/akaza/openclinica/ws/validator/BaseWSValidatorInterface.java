/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.ws.validator;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.validation.Errors;
import org.akaza.openclinica.bean.core.Status;

public interface BaseWSValidatorInterface {
	public abstract boolean verifyRole(UserAccountBean user,int study_id, int site_id, Role excluded_role, Errors errors);
	public abstract boolean verifyRole(UserAccountBean user, int study_id, int site_id,  Errors errors);
	
	public abstract StudyBean verifyStudy( StudyDAO dao, String study_id, Status[] included_status,  Errors errors);
	public abstract StudyBean verifyStudyByOID( StudyDAO dao, String study_id, Status[] included_status,	 Errors errors);
	public abstract StudyBean verifySite( StudyDAO dao, String study_id,  String site_id,Status[] included_status,  Errors errors);
	
	public abstract StudyBean verifyStudySubject(String study_id, String subjectId, int max_length, Errors errors);
	
}
