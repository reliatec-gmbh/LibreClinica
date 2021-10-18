/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.extract;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ArchivedDatasetFileDAO extends AuditableEntityDAO<ArchivedDatasetFileBean> {
    private DAODigester digester;

    public ArchivedDatasetFileDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    public ArchivedDatasetFileDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ARCHIVED_DATASET_FILE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// file id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.INT);// dataset id
        this.setTypeExpected(4, TypeNames.INT);// export format id
        this.setTypeExpected(5, TypeNames.STRING);// file_reference
        this.setTypeExpected(6, TypeNames.INT);// run_time
        this.setTypeExpected(7, TypeNames.INT);// file_size
        this.setTypeExpected(8, TypeNames.TIMESTAMP);// date_created
        this.setTypeExpected(9, TypeNames.INT);// owner id
    }

    @Override
    public ArchivedDatasetFileBean create(ArchivedDatasetFileBean eb) {
        ArchivedDatasetFileBean fb = eb;
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(Integer.valueOf(1), fb.getName());
        variables.put(Integer.valueOf(2), Integer.valueOf(fb.getDatasetId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(fb.getExportFormatId()));
        variables.put(Integer.valueOf(4), fb.getFileReference());
        variables.put(Integer.valueOf(5), Integer.valueOf(fb.getFileSize()));
        variables.put(Integer.valueOf(6), new Double(fb.getRunTime()));
        variables.put(Integer.valueOf(7), Integer.valueOf(fb.getOwnerId()));
        this.executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            fb.setId(getLatestPK());
        }
        return fb;
    }

    @Override
    public ArchivedDatasetFileBean update(ArchivedDatasetFileBean eb) {
        ArchivedDatasetFileBean fb = eb;
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(Integer.valueOf(1), fb.getName());
        variables.put(Integer.valueOf(2), Integer.valueOf(fb.getDatasetId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(fb.getExportFormatId()));
        variables.put(Integer.valueOf(4), fb.getFileReference());
        variables.put(Integer.valueOf(5), Integer.valueOf(fb.getFileSize()));
        variables.put(Integer.valueOf(6), new Double(fb.getRunTime()));
        variables.put(Integer.valueOf(7), Integer.valueOf(fb.getOwnerId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(fb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables, nullVars);
        return fb;
    }

    @Override
    public ArchivedDatasetFileBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
        fb.setId(((Integer) hm.get("archived_dataset_file_id")).intValue());
        fb.setDateCreated((Date) hm.get("date_created"));

        fb.setName((String) hm.get("name"));
        fb.setId(((Integer) hm.get("archived_dataset_file_id")).intValue());
        fb.setDatasetId(((Integer) hm.get("dataset_id")).intValue());
        fb.setExportFormatId(((Integer) hm.get("export_format_id")).intValue());
        fb.setFileReference((String) hm.get("file_reference"));
        fb.setRunTime(((Integer) hm.get("run_time")).doubleValue());
        fb.setFileSize(((Integer) hm.get("file_size")).intValue());
        fb.setOwnerId(((Integer) hm.get("owner_id")).intValue());
        UserAccountBean owner = (UserAccountBean) getUserAccountDAO().findByPK(fb.getOwnerId());
        fb.setOwner(owner);
        return fb;
    }

    public void deleteArchiveDataset(ArchivedDatasetFileBean adfBean){
        HashMap<Integer, Object> variables = variables(adfBean.getId());
        String query = digester.getQuery("deleteArchiveDataset");
        this.executeUpdate(query, variables);
    }

    @Override
    public ArrayList<ArchivedDatasetFileBean> findAll() {
    	return executeFindAllQuery("findAll");
    }

    /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<ArchivedDatasetFileBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    @Override
    public EntityBean findByPK(int ID) {
        HashMap<Integer, Object> variables = variables(ID);
        String queryName = "findByPK";
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<ArchivedDatasetFileBean> findByDatasetId(int did) {
        HashMap<Integer, Object> variables = variables(did);
        String queryName = "findByDatasetId";
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<ArchivedDatasetFileBean> findByDatasetIdByDate(int did) {
        HashMap<Integer, Object> variables = variables(did);
        String queryName = "findByDatasetIdByDate";
        return executeFindAllQuery(queryName, variables);
    }
    
    /**
     * NOT IMPELEMENTED
     */
    @Override
    public ArrayList<ArchivedDatasetFileBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<ArchivedDatasetFileBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

	@Override
	public ArchivedDatasetFileBean emptyBean() {
		return new ArchivedDatasetFileBean();
	}
}
