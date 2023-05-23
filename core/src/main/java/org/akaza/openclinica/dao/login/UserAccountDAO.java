/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.login;

import static org.akaza.openclinica.dao.core.TypeNames.STRING;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Privilege;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <P>
 * UserAccountDAO, the data access object for the User_Account table in the LibreClinica 2.0 database.
 *
 * @author thickerson
 *
 *         TODO
 *         <P>
 *         add functions for admin use cases such as assign user to study, remove user from study, etc.
 *         <P>
 *         add ability to get role and priv objects from database when U select
 *         <P>
 *         expand on query to get all that from a select star?
 */
public class UserAccountDAO extends AuditableEntityDAO<UserAccountBean> {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_USERACCOUNT;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
    }

    @Autowired
    public UserAccountDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public UserAccountDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        // assuming select star query on user_account
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.DATE);// created
        this.setTypeExpected(12, TypeNames.DATE);// updated
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// lastvisit, changed
        // from date
        this.setTypeExpected(14, TypeNames.DATE);// passwd timestamp
        this.setTypeExpected(15, TypeNames.STRING);
        this.setTypeExpected(16, TypeNames.STRING);
        this.setTypeExpected(17, TypeNames.STRING);
        this.setTypeExpected(18, TypeNames.INT);
        this.setTypeExpected(19, TypeNames.INT);
        this.setTypeExpected(20, TypeNames.BOOL);
        this.setTypeExpected(21, TypeNames.BOOL);
        this.setTypeExpected(22, TypeNames.INT);
        this.setTypeExpected(23, TypeNames.BOOL);
        this.setTypeExpected(24, TypeNames.STRING);    // access_doe
        this.setTypeExpected(25, TypeNames.STRING);    // timezone
        this.setTypeExpected(26, TypeNames.BOOL);      // enable_api_key 
        this.setTypeExpected(27, TypeNames.STRING);    // api_key
        this.setTypeExpected(28, TypeNames.STRING);    // authtype
        this.setTypeExpected(29, TypeNames.STRING);    // authsecret
    }

    public void setPrivilegeTypesExpected() {
        this.unsetTypeExpected();
        // assuming we are selecting privs from a join on privilege and
        // role_priv_map tables, tbh
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);

    }

    public void setRoleTypesExpected() {
        this.unsetTypeExpected();
        // assuming select star from study_user_role
        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);
        this.setTypeExpected(6, TypeNames.DATE);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);
    }

    @Override
    public UserAccountBean update(UserAccountBean uab) {
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        
        // update user_account set date_lastvisit=?, passwd_timestamp=?, passwd_challenge_question=?, passwd_challenge_answer=?, phone=? where user_name=?

        variables.put(1, uab.getName());
        variables.put(2, uab.getPasswd());
        variables.put(3, uab.getFirstName());
        variables.put(4, uab.getLastName());
        variables.put(5, uab.getEmail());
        if (uab.getActiveStudyId() == 0) {
            nullVars.put(6, TypeNames.INT);
            variables.put(6, null);
        } else {
            variables.put(6, uab.getActiveStudyId());
        }
        variables.put(7, uab.getInstitutionalAffiliation());
        variables.put(8, uab.getStatus().getId());
        variables.put(9, uab.getUpdaterId());
        if (uab.getLastVisitDate() == null) {
            nullVars.put(10, TypeNames.TIMESTAMP);
            variables.put(10, null);
        } else {
            variables.put(10, new Timestamp(uab.getLastVisitDate().getTime()));
        }
        if (uab.getPasswdTimestamp() == null) {
            nullVars.put(11, TypeNames.DATE);
            variables.put(11, null);
        } else {
            variables.put(11, uab.getPasswdTimestamp());
        }
        variables.put(12, uab.getPasswdChallengeQuestion());
        variables.put(13, uab.getPasswdChallengeAnswer());
        variables.put(14, uab.getPhone());

        if (uab.isTechAdmin()) {
            variables.put(15, UserType.TECHADMIN.getId());
        } else if (uab.isSysAdmin()) {
            variables.put(15, UserType.SYSADMIN.getId());
        } else {
            variables.put(15, UserType.USER.getId());
        }

        variables.put(16, uab.getAccountNonLocked());
        variables.put(17, uab.getLockCounter());
        variables.put(18, uab.getRunWebservices());

        if (uab.getAccessCode() == null || uab.getAccessCode().equals("") || uab.getAccessCode().equals("null")) {
            nullVars.put(19, TypeNames.STRING);
            variables.put(19, null);
        } else {
            variables.put(19, uab.getAccessCode());
        }
        
        if (uab.getTime_zone() == null || uab.getTime_zone().equals("")) {
            nullVars.put(20, TypeNames.STRING);
            variables.put(20, null);
        } else {
            variables.put(20, uab.getTime_zone());
        }
        variables.put(21, uab.isEnableApiKey());
        
        if (uab.getApiKey() == null || uab.getApiKey().equals("")) {
            nullVars.put(22, TypeNames.STRING);
            variables.put(22, null);
        } else {
            variables.put(22, uab.getApiKey());
        }

        variables.put(23, uab.getAuthtype());
        variables.put(24, uab.getAuthsecret());
        // Identifier at last position!!!
        variables.put(25, uab.getId());

        String sql = digester.getQuery("update");
        this.executeUpdate(sql, variables, nullVars);

        if (!uab.isTechAdmin()) {
            setSysAdminRole(uab, false);
        }

        if (!this.isQuerySuccessful()) {
            uab.setId(0);
            logger.warn("query failed: " + sql);
        }

        return uab;
    }

    /**
     * deleteTestOnly, used only to clean up after unit testing, tbh
     *
     * @param name name
     */
    public void deleteTestOnly(String name) {
        HashMap<Integer, Object> variables = variables(name);
        this.executeUpdate(digester.getQuery("deleteTestOnly"), variables);
    }

    public void delete(UserAccountBean u) {
        HashMap<Integer, Object> variables = variables(u.getName());
        this.executeUpdate(digester.getQuery("deleteStudyUserRolesIncludeAutoRemove"), variables);

        variables = variables(u.getUpdaterId(), u.getId());
        this.executeUpdate(digester.getQuery("delete"), variables);
    }

    public void restore(UserAccountBean u) {
        HashMap<Integer, Object> variables = variables(u.getPasswd(), u.getUpdaterId(), u.getId());
        this.executeUpdate(digester.getQuery("restore"), variables);

        variables = variables(u.getName());
        this.executeUpdate(digester.getQuery("restoreStudyUserRolesByUserID"), variables);
    }

    public void updateLockCounter(Integer id, Integer newCounterNumber) {
        HashMap<Integer, Object> variables = variables(newCounterNumber, id);
        this.executeUpdate(digester.getQuery("updateLockCounter"), variables);
    }

    public void lockUser(Integer id) {
        HashMap<Integer, Object> variables = variables(false, Status.LOCKED.getId(), id);
        this.executeUpdate(digester.getQuery("lockUser"), variables);
    }

    @SuppressWarnings({ "unlikely-arg-type" })
	@Override
    public UserAccountBean create(UserAccountBean uab) {
        HashMap<Integer, Object> variables = new HashMap<>();
        int id = getNextPK();
        variables.put(1, id);
        variables.put(2, uab.getName());
        variables.put(3, uab.getPasswd());
        variables.put(4, uab.getFirstName());
        variables.put(5, uab.getLastName());
        variables.put(6, uab.getEmail());
        variables.put(7, uab.getActiveStudyId());
        variables.put(8, uab.getInstitutionalAffiliation());
        variables.put(9, uab.getStatus().getId());
        variables.put(10, uab.getOwnerId());
        variables.put(11, uab.getPasswdChallengeQuestion());
        variables.put(12, uab.getPasswdChallengeAnswer());
        variables.put(13, uab.getPhone());

        if (uab.isTechAdmin()) {
            variables.put(14, UserType.TECHADMIN.getId());
        } else if (uab.isSysAdmin()) {
            variables.put(14, UserType.SYSADMIN.getId());
        } else {
            variables.put(14, UserType.USER.getId());
        }
        
        variables.put(15, uab.getRunWebservices());
        variables.put(16, uab.getAccessCode());
        variables.put(17, uab.isEnableApiKey());
        variables.put(18, uab.getApiKey());
        variables.put(19, uab.getAuthtype());
        variables.put(20, uab.getAuthsecret());

        HashMap<Integer, Integer> nullables = new HashMap<>();
        if (uab.isAuthsecretAbsent()) {
            nullables.put(20, STRING);
        }

        this.executeUpdate(digester.getQuery("insert"), variables, nullables);
        boolean success = isQuerySuccessful();

        setSysAdminRole(uab, true);
        
        for (StudyUserRoleBean studyRole : uab.getRoles()) {

            // TODO: Role.ADMIN is an unlikely argument for equals, check this
            // TODO: it should be probably studyRole.getRole()
            // TODO: sys admin role is created in setSysAdminRole, that is why it should be skipped here
            if (studyRole.equals(Role.ADMIN)) {
                continue;
            }

            createStudyUserRole(uab, studyRole);
            success = success && isQuerySuccessful();
        }

        if (success) {
            uab.setId(id);
        }

        return uab;
    }

    public StudyUserRoleBean createStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole) {
        Locale currentLocale = ResourceBundleProvider.getLocale();
        ResourceBundleProvider.updateLocale(Locale.US); 
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyRole.getRoleName());
        variables.put(2, studyRole.getStudyId());
        variables.put(3, studyRole.getStatus().getId());
        variables.put(4, user.getName());
        variables.put(5, studyRole.getOwnerId());
        this.executeUpdate(digester.getQuery("insertStudyUserRole"), variables);
        ResourceBundleProvider.updateLocale(currentLocale);
        return studyRole;
    }

    public UserAccountBean findStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole) {
        this.setTypesExpected();
        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);
        this.setTypeExpected(6, TypeNames.DATE);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(1,  studyRole.getRoleName());
        variables.put(2, studyRole.getStudyId());
        variables.put(3, studyRole.getStatus().getId());
        variables.put(4, user.getName());

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findStudyUserRole"), variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
        	eb.setName((String) (alist.get(0)).get("user_name"));
        }
        return eb;
    }

    @Override
    public UserAccountBean getEntityFromHashMap(HashMap<String, Object> hm) {
        return this.getEntityFromHashMap(hm, true);
    }

    public StudyUserRoleBean getRoleFromHashMap(HashMap<String, Object> hm) {
        StudyUserRoleBean surb = new StudyUserRoleBean();

        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Integer statusId = (Integer) hm.get("status_id");
        Integer studyId = (Integer) hm.get("study_id");

        surb.setName((String) hm.get("user_name"));
        surb.setUserName((String) hm.get("user_name"));
        surb.setRoleName((String) hm.get("role_name"));
        surb.setCreatedDate(dateCreated);
        surb.setUpdatedDate(dateUpdated);
        surb.setStatus(Status.get(statusId));
        surb.setStudyId(studyId);
        return surb;
    }

    public Privilege getPrivilegeFromHashMap(HashMap<String, Object> hm) {
        Integer privId = (Integer) hm.get("priv_id");

        return Privilege.get(privId);
    }

    // TODO remove SuppressWarnings when a solution for the recursion problem with 'owner' and 'updater' is found
    @SuppressWarnings("deprecation")
	public UserAccountBean getEntityFromHashMap(HashMap<String, Object> hm, boolean findOwner) {
        UserAccountBean eb = new UserAccountBean();

        // pull out objects from hashmap
        String firstName = (String) hm.get("first_name");
        String lastName = (String) hm.get("last_name");
        String userName = (String) hm.get("user_name");
        eb.setEmail((String) hm.get("email"));
        eb.setPasswd((String) hm.get("passwd"));
        Integer userId = (Integer) hm.get("user_id");
        Integer activeStudy = (Integer) hm.get("active_study");
        Integer statusId = (Integer) hm.get("status_id");
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Date dateLastVisit = (Date) hm.get("date_lastvisit");
        Date pwdTimestamp = (Date) hm.get("passwd_timestamp");
        String passwdChallengeQuestion = (String) hm.get("passwd_challenge_question");
        String passwdChallengeAnswer = (String) hm.get("passwd_challenge_answer");
        Integer userTypeId = (Integer) hm.get("user_type_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        Integer updateId = (Integer) hm.get("update_id");
        String accessCode = (String) hm.get("access_code");
        String time_zone = (String) hm.get("time_zone");
        Boolean enableApiKey = (Boolean) hm.get("enable_api_key");
        String apiKey = (String) hm.get("api_key");
        String authtype = (String) hm.get("authtype");
        String authsecret = (String) hm.get("authsecret");

        // begin to set objects in the bean
        eb.setId(userId);
        eb.setActiveStudyId(activeStudy);
        eb.setInstitutionalAffiliation((String) hm.get("institutional_affiliation"));
        eb.setStatus(Status.get(statusId));
        eb.setCreatedDate(dateCreated);
        eb.setUpdatedDate(dateUpdated);
        eb.setLastVisitDate(dateLastVisit);
        eb.setPasswdTimestamp(pwdTimestamp);
        eb.setPhone((String) hm.get("phone"));
        eb.addUserType(UserType.get(userTypeId));
        eb.setEnabled((Boolean) hm.get("enabled"));
        eb.setAccountNonLocked((Boolean) hm.get("account_non_locked"));
        eb.setLockCounter(((Integer) hm.get("lock_counter")));
        eb.setRunWebservices((Boolean) hm.get("run_webservices"));
        eb.setAccessCode(accessCode);
        eb.setTime_zone(time_zone);
        eb.setEnableApiKey(enableApiKey);
        eb.setApiKey(apiKey);
        eb.setAuthsecret(authsecret);
        eb.setAuthtype(authtype);
        eb.setOwnerId(ownerId);
        eb.setUpdaterId(updateId);

        //TODO: I think this is not necessary because owner and updater can be lazy loaded
        // below block is set up to avoid recursion, etc.
        if (findOwner && !userName.contains(".")) {
            UserAccountBean owner = this.findByPK(ownerId, false);
            eb.setOwner(owner);
            UserAccountBean updater = this.findByPK(updateId, false);
            eb.setUpdater(updater);
        }
        // end of if block to avoid recursion

        eb.setFirstName(firstName);
        eb.setLastName(lastName);
        eb.setName(userName);
        eb.setPasswdChallengeQuestion(passwdChallengeQuestion);
        eb.setPasswdChallengeAnswer(passwdChallengeAnswer);

        // pull out the roles and privs here, tbh
        if (!userName.contains(".")) {
        	ArrayList<StudyUserRoleBean> userRoleBeans = this.findAllRolesByUserName(eb.getName());
        	eb.setRoles(userRoleBeans);
        }
        eb.setActive(true);
        return eb;
    }

    @Override
    public ArrayList<UserAccountBean> findAll() {
        return findAllByLimit(false);
    }

    public ArrayList<UserAccountBean> findAllByLimit(boolean hasLimit) {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist;
        if (hasLimit) {
            alist = this.select(digester.getQuery("findAllByLimit"));
        } else {
            alist = this.select(digester.getQuery("findAll"));
        }
        ArrayList<UserAccountBean> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            UserAccountBean eb = this.getEntityFromHashMap(hm, true);
            al.add(eb);
        }
        return al;
    }

    /*
     * next on our list, how can we affect the query??? SELECT FROM USER_ACCOUNT ORDER BY ? DESC?
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAll(java.lang.String, boolean, java.lang.String)
     */

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<UserAccountBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public UserAccountBean findByPK(int id) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = variables(id);

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0), true);
        }

        return eb;
    }

    public UserAccountBean findByPK(int id, boolean findOwner) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findByPK"), variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0), findOwner);
        }
        return eb;
    }

    public UserAccountBean findByUserName(String name) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(1, name);

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findByUserName"), variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0), true);
        }
        return eb;
    }
    
    public UserAccountBean findByAccessCode(String name) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(name);

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findByAccessCode"), variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0), true);
        }
        return eb;
    }

    public UserAccountBean findByApiKey(String name) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(name);

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findByApiKey"), variables);
        UserAccountBean eb = new UserAccountBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0), true);
        }
        return eb;
    }

    public ArrayList<UserAccountBean> findAllParticipantsByStudyOid(String studyOid) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(studyOid + ".%");
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllParticipantsByStudyOid"), variables);

        ArrayList<UserAccountBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            UserAccountBean eb = this.getEntityFromHashMap(hm,false);
            al.add(eb);
        }
        return al;
    }

    /**
     * Finds all the studies with roles for a user
     *
     * @param userName user name
     * @param allStudies all studies
     * @return The result of calling StudyDAO.findAll();
     */
    public ArrayList<StudyUserRoleBean> findStudyByUser(String userName, ArrayList<StudyBean> allStudies) {
        this.unsetTypeExpected();

        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        HashMap<Integer, StudyUserRoleBean> allStudyUserRoleBeans = new HashMap<>();

        HashMap<Integer, Object> variables = variables(userName);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findStudyByUser"), variables);

        for(HashMap<String, Object> hm : alist) {
            String roleName = (String) hm.get("role_name");
            String studyName = (String) hm.get("name");
            Integer studyId = (Integer) hm.get("study_id");
            StudyUserRoleBean sur = new StudyUserRoleBean();
            sur.setRoleName(roleName);
            sur.setStudyId(studyId);
            sur.setStudyName(studyName);
            allStudyUserRoleBeans.put(studyId, sur);
        }

        // pseudocode:
        // for each parent study P in the system
        // if the user has a role in that study, add it to the answer
        // otherwise, let parentAdded = false
        //
        // for each study, C, which is a child of P
        // if the user has a role in C,
        // if parentAdded = false
        // add a StudyUserRole with study = P, role = invalid to the answer
        // let parentAdded = true
        // add the user's role in C to the answer

        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();

        StudyDAO sdao = new StudyDAO(ds);

        HashMap<Integer, ArrayList<StudyBean>> childrenByParentId = sdao.getChildrenByParentIds(allStudies);

        for (StudyBean parent : allStudies) {
            if (parent == null || parent.getParentStudyId() > 0) {
                continue;
            }

            boolean parentAdded = false;
            int studyId = parent.getId();
            StudyUserRoleBean roleInStudy;

            ArrayList<StudyUserRoleBean> subTreeRoles = new ArrayList<>();

            if (allStudyUserRoleBeans.containsKey(studyId)) {
                roleInStudy = allStudyUserRoleBeans.get(studyId);

                subTreeRoles.add(roleInStudy);
                parentAdded = true;
            } else { // we do this so that we can compute Role.max below
                // without
                // throwing a NullPointerException
                roleInStudy = new StudyUserRoleBean();
            }

            ArrayList<StudyBean> children = childrenByParentId.get(studyId);
            if (children == null) {
                children = new ArrayList<>();
            }

            for (StudyBean child : children) {

                if (allStudyUserRoleBeans.containsKey(child.getId())) {
                    if (!parentAdded) {
                        roleInStudy.setStudyId(studyId);
                        roleInStudy.setRole(Role.INVALID);
                        roleInStudy.setStudyName(parent.getName());
                        subTreeRoles.add(roleInStudy);
                        parentAdded = true;
                    }

                    StudyUserRoleBean roleInChild = allStudyUserRoleBeans.get(child.getId());
                    Role max = Role.max(roleInChild.getRole(), roleInStudy.getRole());
                    roleInChild.setRole(max);
                    roleInChild.setParentStudyId(studyId);
                    subTreeRoles.add(roleInChild);
                } else {
                    StudyUserRoleBean roleInChild = new StudyUserRoleBean();
                    roleInChild.setStudyId(child.getId());
                    roleInChild.setStudyName(child.getName());
                    roleInChild.setRole(roleInStudy.getRole());
                    roleInChild.setParentStudyId(studyId);
                    subTreeRoles.add(roleInChild);
                }
            }
            if (parentAdded) {
                answer.addAll(subTreeRoles);
            }
        }

        return answer;
    }

    public ArrayList<StudyUserRoleBean> findAllRolesByUserName(String userName) {
        this.setRoleTypesExpected();

        HashMap<Integer, Object> variables = variables(userName);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllRolesByUserName"), variables);
        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            StudyUserRoleBean surb = this.getRoleFromHashMap(hm);
            answer.add(surb);
        }

        return answer;
    }

    /**
     * Finds all user and roles in a study
     *
     * @param studyId study id
     */
    public ArrayList<StudyUserRoleBean> findAllByStudyId(int studyId) {
        return findAllUsersByStudyIdAndLimit(studyId, false);
    }

    /**
     * Finds all user and roles in a study
     *
     * @param studyId study id
     */
    public ArrayList<StudyUserRoleBean> findAllUsersByStudyIdAndLimit(int studyId, boolean isLimited) {
        this.setRoleTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, studyId);
        ArrayList<HashMap<String, Object>> alist;
        if (isLimited) {
            alist = this.select(digester.getQuery("findAllByStudyIdAndLimit"), variables);
        } else {
            alist = this.select(digester.getQuery("findAllByStudyId"), variables);
        }
        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            StudyUserRoleBean surb = this.getRoleFromHashMap(hm);
            answer.add(surb);
        }
        return answer;
    }

    /**
     * Finds all user and roles in a study
     *
     * @param studyId study id
     */
    public ArrayList<StudyUserRoleBean> findAllUsersByStudy(int studyId) {
        // SELECT ua.user_name,ua.first_name, ua.last_name, sur.role_name,
        // sur.study_id,sur.status_id,sur.date_updated,sur.update_id, s.name
        // ua.user_id
        // FROM user_account ua, study_user_role sur, study s
        // WHERE ua.user_name=sur.user_name
        // AND (sur.study_id=s.study_id)
        // AND (sur.study_id=?
        // OR sur.study_id in (select s.study_id FROM study s WHERE
        // s.parent_study_id=? ))
        // order by ua.date_created asc
        this.unsetTypeExpected();

        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(studyId, studyId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllUsersByStudy"), variables);

        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            StudyUserRoleBean surb = new StudyUserRoleBean();
            surb.setUserName((String) hm.get("user_name"));
            surb.setLastName((String) hm.get("last_name"));
            surb.setFirstName((String) hm.get("first_name"));
            surb.setRoleName((String) hm.get("role_name"));
            surb.setStudyName((String) hm.get("name"));
            surb.setStudyId((Integer) hm.get("study_id"));
            surb.setParentStudyId((Integer) hm.get("parent_study_id"));
            surb.setUserAccountId((Integer) hm.get("user_id"));
            Integer statusId = (Integer) hm.get("status_id");
            Date dateUpdated = (Date) hm.get("date_updated");

            surb.setUpdatedDate(dateUpdated);
            surb.setStatus(Status.get(statusId));
            answer.add(surb);
        }

        return answer;

    }

    /**
     * Find all assigned users in a study
     * @param studyId study id
     */
    public ArrayList<StudyUserRoleBean> findAllAssignedUsersByStudy(int studyId) {
        this.unsetTypeExpected();

        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(studyId, studyId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllAssignedUsersByStudy"), variables);
        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            StudyUserRoleBean surb = new StudyUserRoleBean();
            surb.setUserName((String) hm.get("user_name"));
            surb.setLastName((String) hm.get("last_name"));
            surb.setFirstName((String) hm.get("first_name"));
            surb.setRoleName((String) hm.get("role_name"));
            surb.setStudyName((String) hm.get("name"));
            surb.setStudyId((Integer) hm.get("study_id"));
            surb.setParentStudyId((Integer) hm.get("parent_study_id"));
            surb.setUserAccountId((Integer) hm.get("user_id"));
            Integer statusId = (Integer) hm.get("status_id");
            Date dateUpdated = (Date) hm.get("date_updated");

            surb.setUpdatedDate(dateUpdated);
            surb.setStatus(Status.get(statusId));
            answer.add(surb);
        }
        return answer;
    }

    public ArrayList<StudyUserRoleBean> findAllUsersByStudyOrSite(int studyId, int parentStudyId, int studySubjectId) {
        this.unsetTypeExpected();

        this.setTypeExpected(1, TypeNames.STRING);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(studyId, parentStudyId, studySubjectId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllUsersByStudyOrSite"), variables);
        ArrayList<StudyUserRoleBean> answer = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            StudyUserRoleBean surb = new StudyUserRoleBean();
            surb.setUserName((String) hm.get("user_name"));
            surb.setLastName((String) hm.get("last_name"));
            surb.setFirstName((String) hm.get("first_name"));
            surb.setRoleName((String) hm.get("role_name"));
            surb.setStudyName((String) hm.get("name"));
            surb.setStudyId((Integer) hm.get("study_id"));
            surb.setParentStudyId((Integer) hm.get("parent_study_id"));
            surb.setUserAccountId((Integer) hm.get("user_id"));
            Integer statusId = (Integer) hm.get("status_id");
            Date dateUpdated = (Date) hm.get("date_updated");

            surb.setUpdatedDate(dateUpdated);
            surb.setStatus(Status.get(statusId));
            answer.add(surb);
        }

        return answer;

    }

    public ArrayList<Privilege> findPrivilegesByRole(int roleId) {
        this.setPrivilegeTypesExpected();
        HashMap<Integer, Object> variables = variables(roleId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findPrivilegesByRole"), variables);
        ArrayList<Privilege> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            Privilege pb = this.getPrivilegeFromHashMap(hm);
            al.add(pb);
        }
        return al;
    }

    public ArrayList<Privilege> findPrivilegesByRoleName(String roleName) {
        this.setPrivilegeTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, roleName);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findPrivilegesByRoleName"), variables);
        ArrayList<Privilege> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            Privilege p = this.getPrivilegeFromHashMap(hm);
            al.add(p);
        }
        return al;
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<UserAccountBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<UserAccountBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public StudyUserRoleBean updateStudyUserRole(StudyUserRoleBean s, String userName) {
        Locale currentLocale = ResourceBundleProvider.getLocale();
        ResourceBundleProvider.updateLocale(Locale.US);
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(1, s.getRoleName());
        variables.put(2, s.getStatus().getId());
        variables.put(3, s.getUpdaterId());
        variables.put(4, s.getStudyId());
        variables.put(5, userName);

        String sql = digester.getQuery("updateStudyUserRole");
        this.executeUpdate(sql, variables);

        ResourceBundleProvider.updateLocale(currentLocale);
        return s;
    }

    public StudyUserRoleBean findRoleByUserNameAndStudyId(String userName, int studyId) {
        ArrayList<StudyUserRoleBean> roles = findAllRolesByUserName(userName);
        for (StudyUserRoleBean s : roles) {
            if (s.getStudyId() == studyId) {
                s.setActive(true);
                return s;
            }
        }

        StudyUserRoleBean doesntExist = new StudyUserRoleBean();
        doesntExist.setActive(false);
        return doesntExist;
    }

    public int findRoleCountByUserNameAndStudyId(String userName, int studyId, int childStudyId) {

        this.setRoleTypesExpected();
        HashMap<Integer, Object> variables = variables(userName, studyId);

        ArrayList<HashMap<String, Object>> alist;
        if(childStudyId == 0){
            alist = this.select(digester.getQuery("findRoleCountByUserNameAndStudyId"), variables);
        } else {
            variables.put(3, childStudyId);
            alist = this.select(digester.getQuery("findRoleByUserNameAndStudyIdOrSiteId"), variables);
        }
        return alist.size();
    }


    public void setSysAdminRole(UserAccountBean uab, boolean creating) {
        HashMap<Integer, Object> variables = variables(uab.getName());
        
        boolean failOnEmptyUpdate = false;

        if (uab.isSysAdmin() && !uab.isTechAdmin()) {
            // we remove first so that there are no duplicate roles
            this.executeUpdate(digester.getQuery("removeSysAdminRole"), variables, failOnEmptyUpdate);

            int ownerId = creating ? uab.getOwnerId() : uab.getUpdaterId();
            variables.put(2, ownerId);
            variables.put(3, ownerId);
            this.executeUpdate(digester.getQuery("addSysAdminRole"), variables);
        } else {
            this.executeUpdate(digester.getQuery("removeSysAdminRole"), variables, failOnEmptyUpdate);
        }
    }

    public ArrayList<UserAccountBean> findAllByRole(String role) {
        return this.findAllByRole(role, "");
    }

    public ArrayList<UserAccountBean> findAllByRole(String role1, String role2) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(role1, role2);
        ArrayList<HashMap<String, Object>> alist;
        alist = this.select(digester.getQuery("findAllByRole"), variables);
        ArrayList<UserAccountBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            UserAccountBean eb = this.getEntityFromHashMap(hm, true);
            al.add(eb);
        }
        return al;
    }

	@Override
	public UserAccountBean emptyBean() {
		return new UserAccountBean();
	}
}