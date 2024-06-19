/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller.openrosa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.springframework.validation.Errors;

public class SubmissionContainer {
    private String requestBody = null;
    private HashMap<String, String> subjectContext = null;
    private Study study = null;
    private StudyEvent studyEvent = null;
    private StudySubject subject = null;
    private UserAccount user = null;
    private EventCrf eventCrf = null;
    private List<ItemData> items = null;
    private Errors errors = null;
    private Locale locale = null;
    private ArrayList<HashMap<String,String>> listOfUploadFilePaths;

    public SubmissionContainer(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,ArrayList<HashMap<String,String>> listOfUploadFilePaths) {
        this.study = study;
        this.requestBody = requestBody;
        this.subjectContext = subjectContext;
        this.errors = errors;
        this.locale = locale;
        this.listOfUploadFilePaths=listOfUploadFilePaths;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public HashMap<String, String> getSubjectContext() {
        return subjectContext;
    }

    public void setSubjectContext(HashMap<String, String> subjectContext) {
        this.subjectContext = subjectContext;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public StudyEvent getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEvent studyEvent) {
        this.studyEvent = studyEvent;
    }

    public StudySubject getSubject() {
        return subject;
    }

    public void setSubject(StudySubject subject) {
        this.subject = subject;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }

    public List<ItemData> getItems() {
        return items;
    }

    public void setItems(List<ItemData> items) {
        this.items = items;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public ArrayList<HashMap<String,String>> getListOfUploadFilePaths() {
        return listOfUploadFilePaths;
    }

    public void setListOfUploadFilePaths(ArrayList<HashMap<String,String>> listOfUploadFilePaths) {
        this.listOfUploadFilePaths = listOfUploadFilePaths;
    }

}
