/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

public class StudyDAO extends AuditableEntityDAO<StudyBean> {

    public StudyDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    private void setQueryNames() {
        getNextPKName = "findNextKey";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDY;
    }

    @Override
    public void setTypesExpected() {
        // 1 study_id serial NOT NULL,
        // 2 parent_study_id numeric,
        // 3 unique_identifier varchar(30),
        // 4 secondary_identifier varchar(255),
        // 5 name varchar(60),
        // 6 summary varchar(255),
        // 7 date_planned_start date,
        // 8 date_planned_end date,
        // 9 date_created date,
        // 10 date_updated date,
        // 11 owner_id numeric,
        // 12 update_id numeric,
        // 13 type_id numeric,
        // 14 status_id numeric,
        // 15 principal_investigator varchar(255),
        // 16 facility_name varchar(255),
        // 17 facility_city varchar(255),
        // 18 facility_state varchar(20),
        // 19 facility_zip varchar(64),
        // 20 facility_country varchar(64),
        // 21 facility_recruitment_status varchar(60),
        // 22 facility_contact_name varchar(255),
        // 23 facility_contact_degree varchar(255),
        // 24 facility_contact_phone varchar(255),
        // 25 facility_contact_email varchar(255),
        // 26 protocol_type varchar(30),
        // 27 protocol_description varchar(1000),
        // 28 protocol_date_verification date,
        // 29 phase varchar(30),
        // 30 expected_total_enrollment numeric,
        // 31 sponsor varchar(255),
        // 32 collaborators varchar(1000),
        // 33 medline_identifier varchar(255),
        // 34 url varchar(255),
        // 35 url_description varchar(255),
        // 36 conditions varchar(500),
        // 37 keywords varchar(255),
        // 38 eligibility varchar(500),
        // 39 gender varchar(30),
        // 40 age_max varchar(3),
        // 41 age_min varchar(3),
        // 42 healthy_volunteer_accepted bool,
        // 43 purpose varchar(64),
        // 44 allocation varchar(64),
        // 45 masking varchar(30),
        // 46 control varchar(30),
        // 47 "assignment" varchar(30),
        // 48 endpoint varchar(64),
        // 49 interventions varchar(1000),
        // 50 duration varchar(30),
        // 51 selection varchar(30),
        // 52 timing varchar(30),
        // 53 official_title varchar(50)
        // 54 results_reference boolean

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        this.setTypeExpected(2, TypeNames.INT);// parent id
        this.setTypeExpected(3, TypeNames.STRING);// ident
        this.setTypeExpected(4, TypeNames.STRING);// second ident
        this.setTypeExpected(5, TypeNames.STRING);// name
        this.setTypeExpected(6, TypeNames.STRING);// summary
        this.setTypeExpected(7, TypeNames.DATE);// start
        this.setTypeExpected(8, TypeNames.DATE);// end
        this.setTypeExpected(9, TypeNames.DATE);// create
        this.setTypeExpected(10, TypeNames.DATE);// update
        this.setTypeExpected(11, TypeNames.INT);// owner
        this.setTypeExpected(12, TypeNames.INT);// updater
        this.setTypeExpected(13, TypeNames.INT);// type id
        this.setTypeExpected(14, TypeNames.INT);// status
        this.setTypeExpected(15, TypeNames.STRING);// pi
        this.setTypeExpected(16, TypeNames.STRING);// fname
        this.setTypeExpected(17, TypeNames.STRING);// fcity
        this.setTypeExpected(18, TypeNames.STRING);// fstate
        this.setTypeExpected(19, TypeNames.STRING);// fzip
        this.setTypeExpected(20, TypeNames.STRING);// country
        this.setTypeExpected(21, TypeNames.STRING);// frs
        this.setTypeExpected(22, TypeNames.STRING);// fcn
        this.setTypeExpected(23, TypeNames.STRING);// fcdegree
        this.setTypeExpected(24, TypeNames.STRING);// fcphone
        this.setTypeExpected(25, TypeNames.STRING);// fcemail
        this.setTypeExpected(26, TypeNames.STRING);// prottype
        this.setTypeExpected(27, TypeNames.STRING);// protdesc
        this.setTypeExpected(28, TypeNames.DATE);// pdateverif
        this.setTypeExpected(29, TypeNames.STRING);// phase
        this.setTypeExpected(30, TypeNames.INT);// expectotenroll
        // this.setTypeExpected(31, TypeNames.BOOL);//genetic
        this.setTypeExpected(31, TypeNames.STRING);// sponsor
        this.setTypeExpected(32, TypeNames.STRING);// collab
        this.setTypeExpected(33, TypeNames.STRING);// medline
        this.setTypeExpected(34, TypeNames.STRING);// url
        this.setTypeExpected(35, TypeNames.STRING);// url-desc
        this.setTypeExpected(36, TypeNames.STRING);// conds
        this.setTypeExpected(37, TypeNames.STRING);// keyw
        this.setTypeExpected(38, TypeNames.STRING);// eligible
        this.setTypeExpected(39, TypeNames.STRING);// gender, no char avail.
        this.setTypeExpected(40, TypeNames.STRING);// agemax
        this.setTypeExpected(41, TypeNames.STRING);// agemin
        this.setTypeExpected(42, TypeNames.BOOL);// healthy volunteer
        this.setTypeExpected(43, TypeNames.STRING);// purpose
        this.setTypeExpected(44, TypeNames.STRING);// allocation
        this.setTypeExpected(45, TypeNames.STRING);// masking
        this.setTypeExpected(46, TypeNames.STRING);// control
        this.setTypeExpected(47, TypeNames.STRING);// assignment
        this.setTypeExpected(48, TypeNames.STRING);// endpoint
        this.setTypeExpected(49, TypeNames.STRING);// interv
        this.setTypeExpected(50, TypeNames.STRING);// duration
        this.setTypeExpected(51, TypeNames.STRING);// selection
        this.setTypeExpected(52, TypeNames.STRING);// timing
        this.setTypeExpected(53, TypeNames.STRING);// official_title
        this.setTypeExpected(54, TypeNames.BOOL);// results_reference
        this.setTypeExpected(55, TypeNames.STRING);// oc oid
        // this.setTypeExpected(56, TypeNames.BOOL);//discrepancy_management
        this.setTypeExpected(56, TypeNames.INT);
        this.setTypeExpected(57, TypeNames.STRING); // e-mail notification
        this.setTypeExpected(58, TypeNames.STRING); // contact e-mail
    }

    /**
     * <b>update </b>, the method that returns an updated study bean after it
     * updates the database. Note that we can use the three stages from our
     * creation use case.
     * 
     * @return sb an updated study bean.
     */
    @Override
    public StudyBean update(StudyBean sb) {
        sb = this.updateStepOne(sb);
        sb = this.createStepTwo(sb);
        sb = this.createStepThree(sb);
        sb = this.createStepFour(sb);
        return sb;
    }

    /**
     * <P>
     * updateStepOne, the update method for the database. This method takes the
     * place of createStepOne, since it runs an update and assumes you already
     * have a primary key in the study bean object.
     *
     * @param sb the study bean which will be updated.
     * @return sb the study bean after it is updated with this phase.
     */
    public StudyBean updateStepOne(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        if (sb.getParentStudyId() == 0) {
            nullVars.put(1, Types.INTEGER);
            variables.put(1, null);
        } else {
            variables.put(1, sb.getParentStudyId());
        }
        variables.put(2, sb.getName());
        variables.put(3, sb.getOfficialTitle());
        variables.put(4, sb.getIdentifier());
        variables.put(5, sb.getSecondaryIdentifier());
        variables.put(6, sb.getSummary());
        variables.put(7, sb.getPrincipalInvestigator());
        if (sb.getDatePlannedStart() == null) {
            nullVars.put(8, Types.DATE);
            variables.put(8, null);
        } else {
            variables.put(8, sb.getDatePlannedStart());
        }

        if (sb.getDatePlannedEnd() == null) {
            nullVars.put(9, Types.DATE);
            variables.put(9, null);
        } else {
            variables.put(9, sb.getDatePlannedEnd());
        }

        variables.put(10, sb.getFacilityName());
        variables.put(11, sb.getFacilityCity());
        variables.put(12, sb.getFacilityState());
        variables.put(13, sb.getFacilityZip());
        variables.put(14, sb.getFacilityCountry());
        variables.put(15, sb.getFacilityRecruitmentStatus());
        variables.put(16, sb.getFacilityContactName());
        variables.put(17, sb.getFacilityContactDegree());
        variables.put(18, sb.getFacilityContactPhone());
        variables.put(19, sb.getFacilityContactEmail());
        variables.put(20, sb.getStatus().getId());// status
        // id
        // variables.put(new Integer(19), sb.getStatus())//need to get a
        // function
        // to get the id
        // variables.put(new Integer(19), sb.getCreatedDate());
        variables.put(21, sb.getUpdaterId());// owner
        // id
        variables.put(22, sb.getUpdatedDate());// date updated
        variables.put(23, sb.getOldStatus().getId());// study id
        // variables.put(new Integer(22), new Integer(1));
        // stop gap measure for owner and updater id
        variables.put(24, sb.getMailNotification());
        
        if (sb.contactEmailAbsent()) {
            nullVars.put(25, Types.VARCHAR);
            variables.put(25, null);
        } else {
            variables.put(25, sb.getContactEmail());
        }
        
        // SQL Update where
        variables.put(26, sb.getId());// study id
        
        this.executeUpdate(digester.getQuery("updateStepOne"), variables, nullVars);
        return sb;
    }

    /**
     * <b>create </b>, the method that creates a study in the database.
     * <P>
     * note: create is split up into four custom functions, per the use case; we
     * are creating the standard create function here which calls all four
     * functions at once, but the seperate functions may be required in the
     * control servlets.
     * 
     * @return eb the created entity bean.
     */
    @Override
    public StudyBean create(StudyBean sb) {
        sb = this.createStepOne(sb);
        // in the above step, we will have created a primary key,
        // and in the next steps, we update the study bean
        // in phases
        sb = this.createStepTwo(sb);
        sb = this.createStepThree(sb);
        sb = this.createStepFour(sb);

        return sb;
    }

    /**
     * <P>
     * findNextKey, a method to return a simple int from the database.
     * 
     * @return int, which is the next primary key for creating a study.
     */
    public int findNextKey() {
        return getNextPK();
    }

    /**
     * <P>
     * createStepOne, per the 'Detailed use case for administer system document
     * v1.0rc1' document. We insert the study in this method, and then update
     * the same study in the next three steps.
     * <P>
     * The next three steps, by the way, can then be used to update studies as well.
     *
     * @param sb Study bean about to be created.
     * @return same study bean with a primary key in the ID field.
     */
    public StudyBean createStepOne(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        sb.setId(this.findNextKey());
        variables.put(1, sb.getId());
        if (sb.getParentStudyId() == 0) {
            nullVars.put(2, Types.INTEGER);
            variables.put(2, null);
        } else {
            variables.put(2, sb.getParentStudyId());
        }

        variables.put(3, sb.getName());
        variables.put(4, sb.getOfficialTitle());
        variables.put(5, sb.getIdentifier());
        variables.put(6, sb.getSecondaryIdentifier());
        variables.put(7, sb.getSummary());
        variables.put(8, sb.getPrincipalInvestigator());

        if (sb.getDatePlannedStart() == null) {
            nullVars.put(9, Types.DATE);
            variables.put(9, null);
        } else {
            variables.put(9, sb.getDatePlannedStart());
        }

        if (sb.getDatePlannedEnd() == null) {
            nullVars.put(10, Types.DATE);
            variables.put(10, null);
        } else {
            variables.put(10, sb.getDatePlannedEnd());
        }

        variables.put(11, sb.getFacilityName());
        variables.put(12, sb.getFacilityCity());
        variables.put(13, sb.getFacilityState());
        variables.put(14, sb.getFacilityZip());
        variables.put(15, sb.getFacilityCountry());
        variables.put(16, sb.getFacilityRecruitmentStatusKey());
        variables.put(17, sb.getFacilityContactName());
        variables.put(18, sb.getFacilityContactDegree());
        variables.put(19, sb.getFacilityContactPhone());
        variables.put(20, sb.getFacilityContactEmail());
        variables.put(21, sb.getStatus().getId());
        // variables.put(new Integer(19), sb.getStatus())//need to get a
        // function
        // to get the id
        variables.put(22, new java.util.Date());
        variables.put(23, sb.getOwnerId());
        variables.put(24, getValidOid(sb));

        variables.put(25, sb.getMailNotification());
        if (sb.contactEmailAbsent()) {
            variables.put(26, null);
            nullVars.put(26, Types.VARCHAR);
        } else {
            variables.put(26, sb.getContactEmail());
        }

        // replace this with the owner id
        this.executeUpdate(digester.getQuery("createStepOne"), variables, nullVars);
        return sb;
    }

    // we are generating and creating the valid oid at step one, tbh
    private String getOid(StudyBean sb) {
        String oid;
        try {
            oid = sb.getOid() != null ? sb.getOid() : sb.getOidGenerator().generateOid(sb.getIdentifier());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudyBean sb) {
        String oid = getOid(sb);
        logger.info("*** " + oid);
        String oidPreRandomization = oid;
        while (existStudyWithOid(oid)) {
            oid = sb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        logger.info("returning the following oid: " + oid);
        return oid;
    }

    /**
     * Checks whether a study with the given OID already exist.
     * 
     * @param oid the study OID
     * @return true if a study with the given OID exists, false otherwise
     */
    public boolean existStudyWithOid(String oid) {
        StudyBean foundStudy = findByOid(oid);
        return foundStudy != null && oid.equals(foundStudy.getOid());
    }

    public StudyBean findByOid(String oid) {
        String queryName = "findByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyBean findByUniqueIdentifier(String oid) {
        String queryName = "findByUniqueIdentifier";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyBean findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        String queryName = "findSiteByUniqueIdentifier";
        HashMap<Integer, Object> variables = variables(parentUniqueIdentifier, siteUniqueIdentifier);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyBean createStepTwo(StudyBean sb) {
        // UPDATE STUDY SET TYPE_ID=?, PROTOCOL_TYPE=?,PROTOCOL_DESCRIPTION=?,
        // PROTOCOL_DATE_VERIFICATION=?, PHASE=?, EXPECTED_TOTAL_ENROLLMENT=?,
        // SPONSOR=?, COLLABORATORS=?, MEDLINE_IDENTIFIER=?, URL=?,
        // URL_DESCRIPTION=?, CONDITIONS=?, KEYWORDS=?, ELIGIBILITY=?,
        // GENDER=?, AGE_MAX=?, AGE_MIN=?, HEALTHY_VOLUNTEER_ACCEPTED=?
        // WHERE STUDY_ID=?
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(1, sb.getType().getId());
        variables.put(2, sb.getProtocolTypeKey());
        variables.put(3, sb.getProtocolDescription());

        if (sb.getProtocolDateVerification() == null) {
            nullVars.put(4, Types.DATE);
            variables.put(4, null);
        } else {
            variables.put(4, sb.getProtocolDateVerification());
        }

        variables.put(5, sb.getPhaseKey());
        variables.put(6, sb.getExpectedTotalEnrollment());
        variables.put(7, sb.getSponsor());
        variables.put(8, sb.getCollaborators());
        variables.put(9, sb.getMedlineIdentifier());
        variables.put(10, sb.isResultsReference());
        variables.put(11, sb.getUrl());
        variables.put(12, sb.getUrlDescription());
        variables.put(13, sb.getConditions());
        variables.put(14, sb.getKeywords());
        variables.put(15, sb.getEligibility());
        variables.put(16, sb.getGenderKey());

        variables.put(17, sb.getAgeMax());
        variables.put(18, sb.getAgeMin());
        variables.put(19, sb.getHealthyVolunteerAccepted());
        // variables.put(new Integer(20), new Boolean(sb.isUsingDOB()));
        // variables.put(new Integer(21), new
        // Boolean(sb.isDiscrepancyManagement()));
        variables.put(20, sb.getId());
        this.executeUpdate(digester.getQuery("createStepTwo"), variables, nullVars);
        return sb;
    }

    public StudyBean createStepThree(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sb.getPurposeKey());
        variables.put(2, sb.getAllocationKey());
        variables.put(3, sb.getMaskingKey());
        variables.put(4, sb.getControlKey());
        variables.put(5, sb.getAssignmentKey());
        variables.put(6, sb.getEndpointKey());
        variables.put(7, sb.getInterventionsKey());
        variables.put(8, sb.getId());
        this.executeUpdate(digester.getQuery("createStepThree"), variables);
        return sb;
    }

    public StudyBean createStepFour(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sb.getDurationKey());
        variables.put(2, sb.getSelectionKey());
        variables.put(3, sb.getTimingKey());
        variables.put(4, sb.getId());
        this.executeUpdate(digester.getQuery("createStepFour"), variables);
        return sb;
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public StudyBean getEntityFromHashMap(HashMap<String, Object> hm) {
        StudyBean eb = new StudyBean();

        // first set all the strings
        eb.setIdentifier((String) hm.get("unique_identifier"));
        eb.setName((String) hm.get("name"));
        eb.setSummary((String) hm.get("summary"));
        eb.setSecondaryIdentifier((String) hm.get("secondary_identifier"));
        eb.setPrincipalInvestigator((String) hm.get("principal_investigator"));
        eb.setFacilityName((String) hm.get("facility_name"));
        eb.setFacilityCity((String) hm.get("facility_city"));
        eb.setFacilityState((String) hm.get("facility_state"));
        eb.setFacilityZip((String) hm.get("facility_zip"));
        eb.setFacilityCountry((String) hm.get("facility_country"));
        eb.setFacilityRecruitmentStatus((String) hm.get("facility_recruitment_status"));
        eb.setFacilityContactName((String) hm.get("facility_contact_name"));
        eb.setFacilityContactDegree((String) hm.get("facility_contact_degree"));
        eb.setFacilityContactPhone((String) hm.get("facility_contact_phone"));
        eb.setFacilityContactEmail((String) hm.get("facility_contact_email"));
        eb.setProtocolType((String) hm.get("protocol_type"));
        eb.setProtocolDescription((String) hm.get("protocol_description"));
        eb.setPhase((String) hm.get("phase"));
        eb.setSponsor((String) hm.get("sponsor"));
        eb.setCollaborators((String) hm.get("collaborators"));
        eb.setMedlineIdentifier((String) hm.get("medline_identifier"));
        eb.setUrl((String) hm.get("url"));
        eb.setUrlDescription((String) hm.get("url_description"));
        eb.setConditions((String) hm.get("conditions"));
        eb.setKeywords((String) hm.get("keywords"));
        eb.setEligibility((String) hm.get("eligibility"));
        eb.setGender((String) hm.get("gender"));
        eb.setAgeMax((String) hm.get("age_max"));
        eb.setAgeMin((String) hm.get("age_min"));
        eb.setPurpose((String) hm.get("purpose"));
        eb.setAllocation((String) hm.get("allocation"));
        eb.setMasking((String) hm.get("masking"));
        eb.setControl((String) hm.get("control"));
        eb.setAssignment((String) hm.get("assignment"));
        eb.setEndpoint((String) hm.get("endpoint"));
        eb.setInterventions((String) hm.get("interventions"));
        eb.setDuration((String) hm.get("duration"));
        eb.setSelection((String) hm.get("selection"));
        eb.setTiming((String) hm.get("timing"));
        eb.setOfficialTitle((String) hm.get("official_title"));
        
        eb.setHealthyVolunteerAccepted((Boolean) hm.get("healthy_volunteer_accepted"));
        eb.setResultsReference((Boolean) hm.get("results_reference"));
        //eb.setUsingDOB(((Boolean)hm.get("collect_dob")).booleanValue());
        //eb.setDiscrepancyManagement(((Boolean)hm.get("discrepancy_management")).booleanValue());


        // next set all the ints/dates
        Integer studyId = (Integer) hm.get("study_id");
        eb.setId(studyId);
        Integer parentStudyId = (Integer) hm.get("parent_study_id");
        if (parentStudyId == null) {
            eb.setParentStudyId(0);
        } else {
            eb.setParentStudyId(parentStudyId);
        }
        Integer ownerId = (Integer) hm.get("owner_id");
        // Even thou it is deprecated it creates more performant lazy loading behaviour
        eb.setOwnerId(ownerId);
        // Disabled because it generates additional SQL query
        //UserAccountBean owner = getUserAccountDAO().findByPK(ownerId);
        //eb.setOwner(owner);
        Integer updateId = (Integer) hm.get("update_id");
        // Even thou it is deprecated it creates more performant lazy loading behaviour
        eb.setUpdaterId(updateId);
        // Disabled because it generates additional SQL query
        //UserAccountBean updater = getUserAccountDAO().findByPK(updateId);
        //eb.setUpdater(updater);
        Integer typeId = (Integer) hm.get("type_id");
        eb.setType(StudyType.get(typeId));
        Integer statusId = (Integer) hm.get("status_id");
        eb.setStatus(Status.get(statusId));
        Integer expectedTotalEnrollment = (Integer) hm.get("expected_total_enrollment");
        eb.setExpectedTotalEnrollment(expectedTotalEnrollment);
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Date datePlannedStart = (Date) hm.get("date_planned_start");
        Date datePlannedEnd = (Date) hm.get("date_planned_end");
        Date dateProtocolVerification = (Date) hm.get("protocol_date_verification");

        eb.setCreatedDate(dateCreated);
        eb.setUpdatedDate(dateUpdated);
        eb.setDatePlannedStart(datePlannedStart);
        eb.setDatePlannedEnd(datePlannedEnd);
        eb.setProtocolDateVerification(dateProtocolVerification); // added by jxu
        eb.setStatus(Status.get(statusId));
        eb.setOid((String) hm.get("oc_oid"));
        Integer oldStatusId = (Integer) hm.get("old_status_id");
        eb.setOldStatus(Status.get(oldStatusId));
        eb.setMailNotification(((String) hm.get("mail_notification")));
        eb.setContactEmail(((String) hm.get("contact_email")));
        return eb;
    }

    public ArrayList<StudyBean> findAllByUser(String username) {
        String queryName = "findAllByUser";
        HashMap<Integer, Object> variables = variables(username);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<Integer> getStudyIdsByCRF(int crfId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        HashMap<Integer, Object> variables = variables(crfId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getStudyIdsByCRF"), variables);
        ArrayList<Integer> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            al.add((Integer) hm.get("study_id"));
        }
        return al;
    }

    // YW 10-18-2007
    public ArrayList<StudyBean> findAllByUserNotRemoved(String username) {
        String queryName = "findAllByUserNotRemoved";
        HashMap<Integer, Object> variables = variables(username);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyBean> findAllByStatus(Status status) {
        String queryName = "findAllByStatus";
        HashMap<Integer, Object> variables = variables(status.getId());
        return executeFindAllQuery(queryName, variables);
    }

    @Override
    public ArrayList<StudyBean> findAll() {
        return findAllByLimit(false);
    }

    public ArrayList<StudyBean> findAllByLimit(boolean isLimited) {
        this.setTypesExpected();
        String sql;
        // Updated for ORACLE and PGSQL compatibility
        if (isLimited) {
            if (CoreResources.getDBName().equals("oracle")) {
                sql = digester.getQuery("findAll") + " where ROWNUM <=5";
            } else {
                sql = digester.getQuery("findAll") + " limit 5";
            }
        } else {
            sql = digester.getQuery("findAll");
        }
        ArrayList<HashMap<String, Object>> alist = this.select(sql);
        return alist.stream().map(hm -> this.getEntityFromHashMap(hm)).collect(toCollection(ArrayList::new));
    }

    public ArrayList<StudyBean> findAllParents() {
        String queryName = "findAllParents";
        return executeFindAllQuery(queryName);
    }

    /**
     * isAParent(), finds out whether or not a study is a parent.
     * 
     * @return a boolean
     */
    public boolean isAParent(int studyId) {
        ArrayList<StudyBean> parents = findAllByParent(studyId);
        return parents != null && parents.size() > 0;
    }

    public ArrayList<StudyBean> findAllByParent(int parentStudyId) {
        return findAllByParentAndLimit(parentStudyId, false);
    }

    public ArrayList<StudyBean> findAllByParentAndLimit(int parentStudyId, boolean isLimited) {
        String queryName;
        if (isLimited) {
            queryName = "findAllByParentLimit5";
        } else {
            queryName = "findAllByParent";
        }
        HashMap<Integer, Object> variables = variables(parentStudyId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyBean> findAll(int studyId) {
        String queryName = "findAllByStudyId";
        HashMap<Integer, Object> variables = variables(studyId, studyId);
        return executeFindAllQuery(queryName, variables);
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudyBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StudyBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyBean findByName(String name) {
        String queryName = "findByName";
        HashMap<Integer, Object> variables = variables(name);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * deleteTestOnly, used only to clean up after unit testing
     * 
     * @param name name
     */
    public void deleteTestOnly(String name) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, name);
        this.executeUpdate(digester.getQuery("deleteTestOnly"), variables);
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudyBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort,
            String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudyBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param allStudies The result of findAll().
     * @return A HashMap where the keys are Integers whose intValue are studyIds
     *         and the values are ArrayLists; each element of the ArrayList is a
     *         StudyBean representing a child of the study whose id is the key
     *         <p>
     *         e.g., if A has children B and C, then this will return a HashMap
     *         h where h.get(A.getId()) returns an ArrayList whose elements are
     *         B and C
     */
    public HashMap<Integer, ArrayList<StudyBean>> getChildrenByParentIds(ArrayList<StudyBean> allStudies) {
        if (allStudies == null) {
            return new HashMap<>();
        }

        Stream<StudyBean> stream = allStudies.stream();
        // filter for child studies (studies with a parent study id)
        stream = stream.filter(s -> s.getParentStudyId() > 0);
        // group by parent study id
        return stream.collect(groupingBy(StudyBean::getParentStudyId, HashMap::new, toCollection(ArrayList::new)));
    }

    public ArrayList<Integer> findAllSiteIdsByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllSiteIdsByStudy"), variables);
        ArrayList<Integer> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            al.add((Integer) hm.get("study_id"));
        }
        return al;
    }

    public ArrayList<Integer> findOlnySiteIdsByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        HashMap<Integer, Object> variables = variables(study.getId());
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findOlnySiteIdsByStudy"), variables);
        ArrayList<Integer> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            al.add((Integer) hm.get("study_id"));
        }
        return al;
    }

    public StudyBean updateSitesStatus(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        variables.put(1, sb.getStatus().getId());
        variables.put(2, sb.getOldStatus().getId());
        variables.put(3, sb.getId());

        this.executeUpdate(digester.getQuery("updateSitesStatus"), variables, nullVars);
        return sb;
    }

    public StudyBean updateStudyStatus(StudyBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        variables.put(1, sb.getStatus().getId());
        variables.put(2, sb.getOldStatus().getId());
        variables.put(3, sb.getId());

        this.executeUpdate(digester.getQuery("updateStudyStatus"), variables, nullVars);
        return sb;
    }

    public StudyBean findByStudySubjectId(int studySubjectId) {
        String queryName = "findByStudySubjectId";
        HashMap<Integer, Object> variables = variables(studySubjectId);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<StudyBean> findAllByParentStudyIdOrderedByIdAsc(int parentStudyId) {
        String queryName = "findAllByParentStudyIdOrderedByIdAsc";
        HashMap<Integer, Object> variables = variables(parentStudyId, parentStudyId);
        return executeFindAllQuery(queryName, variables);
    }

    @Override
    public StudyBean emptyBean() {
        return new StudyBean();
    }

}
