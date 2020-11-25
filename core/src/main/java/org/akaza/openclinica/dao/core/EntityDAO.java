/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.cache.EhCacheWrapper;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

/**
 * <p/>
 * EntityDAO.java, the generic data access object class for the database layer, by Tom Hickerson, 09/24/2004
 * <p/>
 * A signalling system was added on 7 Dec 04 to indicate the success or failure of a query. A query is considered
 * successful iff a SQLException was not thrown
 * in the process of executing the query.
 * <p/>
 * The system can be used by outside classes / subclasses as follows: - Immediately after calling select or execute,
 * isQuerySuccessful() is <code>true</code> if
 * the query was successful, <code>false</code> otherwise. - If isQuerySuccessful returns <code>false</code>
 * getFailureDetails() returns the SQLException which
 * was thrown.
 * <p/>
 * In order to maintain the system, the following invariants must be maintained by developers: 1. Every method executing
 * a query must call clearSignals() as the
 * first statement. 2. Every method executing a query must call either signalSuccess or signalFailure before returning.
 * <p/>
 * At the time of writing, the only methods which execute queries are select and execute.
 *
 * @author thickerson
 * @param <V>
 * @param <K>
 */
public abstract class EntityDAO<B> implements DAOInterface<B> {
    protected DataSource ds;

    protected String digesterName;

    protected DAODigester digester;

    private HashMap<Integer, Integer> setTypes = new HashMap<>();

    /* Here is the cache reference */
    protected EhCacheWrapper<String, ArrayList<HashMap<String, Object>>> cache;
    // protected EhCacheWrapper cache = new EhCacheWrapper();
    protected EhCacheManagerFactoryBean cacheManager;

    // set the types we expect from the database

    // private ArrayList results = new ArrayList();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private boolean querySuccessful;

    private SQLException failureDetails;

    /**
     * Should the name of a query which refers to a SQL command of the following form:
     * <code>SELECT currval('sequence') AS key</code> The column name "key" is required, as
     * getCurrentPK() relies on it.
     */
    protected String getCurrentPKName;

    /**
     * Should the name of a query which refers to a SQL command of the following form:
     * <code>SELECT nextval('sequence') AS key</code> The column name "key" is required, as getNextPK()
     * relies on it.
     */
    protected String getNextPKName;

    protected abstract void setDigesterName();

    // YW 11-26-2007, at this time, it is set only by the method
    // "executeWithPK".
    private int latestPK;

    protected Locale locale = ResourceBundleProvider.getLocale();
    // BWP>> these Strings are initialized from the constructor: the
    // initializeI18nStrings() method; for JUnit tests
    protected String oc_df_string = "";
    protected String local_df_string = "";

    public EntityDAO(DataSource ds) {
        this.ds = ds;
        setDigesterName();
        digester = SQLFactory.getInstance().getDigester(digesterName);
        initializeI18nStrings();
        setCache(SQLFactory.getInstance().getEhCacheWrapper());
    }

    /**
     * This is the method added to cache the queries
     * 
     * @param cache
     */
    public void setCache(final EhCacheWrapper<String, ArrayList<HashMap<String, Object>>> cache) {
        this.cache = cache;
    }

    public EhCacheWrapper<String, ArrayList<HashMap<String, Object>>> getCache() {
        return cache;
    }

    /**
     * setTypeExpected, expects to enter the type of object to retrieve from the database
     *
     * @param num
     *            the order the column should be extracted from the database
     * @param type
     *            the number that is equal to TypeNames
     */
    public void setTypeExpected(int num, int type) {
        setTypes.put(Integer.valueOf(num), Integer.valueOf(type));
    }

    public void unsetTypeExpected() {
        setTypes = new HashMap<>();
    }
    
    /**
     * Returns a connection for the given {@link DataSource}
     * @param ds the data source
     * @return a connection
     * @throws SQLException if a database access error occurs
     * @see DataSource#getConnection()
     */
    public Connection getConnection(DataSource ds) throws SQLException {
    	Connection con = ds.getConnection();
        if (con.isClosed()) {
            if (logger.isWarnEnabled())
                logger.warn("Connection is closed!");
            throw new SQLException();
        }
        return con;
    }

    /**
     * select, a static query interface to the database, returning an array of hashmaps that contain key->object pairs.
     * <P>
     * This is the first operation created for the database, so therefore it is the simplest; cull information from the
     * database but not specify any parameters.
     *
     * @param query
     *            a static query of the database.
     * @return ArrayList of HashMaps carrying the database values.
     */
    public ArrayList<HashMap<String, Object>> select(String query) {
    	return select(query, new HashMap<Integer, Object>());
    }

    public ArrayList<HashMap<String, Object>> select(String query, Connection connection) {
    	return select(query, new HashMap<Integer, Object>(), connection, false);
    }

    public ArrayList<HashMap<String, Object>> select(String query, HashMap<Integer, Object> variables) {
    	return select(query, variables, false);
    }
    
    public ArrayList<HashMap<String, Object>> select(String query, HashMap<Integer, Object> variables, boolean useCache) {
		try {
			Connection connection = getConnection(ds);
	    	return select(query, variables, connection, useCache);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void assertConnectionIsValid(Connection connection) throws IllegalArgumentException, RuntimeException {
    	if(connection == null) {
    		throw new IllegalArgumentException("The given connection is null.");
    	}
    	try {
			if(connection.isClosed()) {
				throw new RuntimeException("The given connection is closed.");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    public ArrayList<HashMap<String, Object>> select(String query, HashMap<Integer, Object> variables, Connection connection, boolean useCache) {
    	assertConnectionIsValid(connection);
        clearSignals();

        ArrayList<HashMap<String, Object>> results = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        PreparedStatementFactory psf = null;
        if(variables != null && !variables.isEmpty()) {
        	psf = new PreparedStatementFactory(variables);
        }

        try {
            ps = connection.prepareStatement(query);
            if(psf != null) {
            	ps = psf.generate(ps);// enter variables here!
            }
			String key = null;

            if(useCache) {
            	// use the cache
				key = ps.toString();
				results = cache.get(key);
            }
            
			if (!useCache || results == null) {            
				rs = ps.executeQuery();
				logger.debug("Executing query, EntityDAO.select:query %s", query);
				signalSuccess();
				results = this.processResultRows(rs);
			}

			if (useCache && results != null) {
				// update the cache
				cache.put(key, results);
			}
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing query, EntityDAO.select: %s:message: %s", query, sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(connection, rs, ps);
        }
        return results;
    }

    // JN: The following method is added for when certain queries needed caching...

    public ArrayList<HashMap<String, Object>> selectByCache(String query, HashMap<Integer, Object> variables) {
    	try {
			Connection connection = getConnection(ds);
	    	return select(query, variables, connection, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * execute, the static version of executing an update or insert on a table in the database.
     *
     * @param query
     *            a static SQL statement which updates or inserts.
     */

    public int executeUpdate(String query) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        boolean isTransactional = false;
        return executeUpdate(query, variables, nullVars, isTransactional, null);
    }

    /*
     * this function is used for transactional updates to allow all updates in
     * one actions to run as one transaction
     */
    public int executeUpdate(String query, Connection connection) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
    	boolean isTransactional;
        if (connection == null) {
        	isTransactional = false;
        	try {
            	connection = getConnection(ds);
    		} catch (SQLException e) {
    			throw new RuntimeException(e);
    		}
        } else {
        	isTransactional = true;
        }
        return executeUpdate(query, variables, nullVars, isTransactional, connection);
    }

    public int executeUpdate(String query, HashMap<Integer, Object> variables) {
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        return executeUpdate(query, variables, nullVars, null);
    }

    public int executeUpdate(String query, HashMap<Integer, Object> variables, Connection connection) {
    	boolean isTransactional;
        if (connection == null) {
        	isTransactional = false;
        	try {
            	connection = getConnection(ds);
    		} catch (SQLException e) {
    			throw new RuntimeException(e);
    		}
        } else {
        	isTransactional = true;
        }
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        return executeUpdate(query, variables, nullVars, isTransactional, connection);
    }

    public int executeUpdate(String query, HashMap<Integer, Object> variables, HashMap<Integer, Integer> nullVars) {
    	return executeUpdate(query, variables, nullVars, null);
    }

    public int executeUpdate(String query, HashMap<Integer, Object> variables, HashMap<Integer, Integer> nullVars, Connection connection) {
        boolean isTransactional;
        if (connection == null) {
        	isTransactional = false;
        	try {
            	connection = getConnection(ds);
    		} catch (SQLException e) {
    			throw new RuntimeException(e);
    		}
        } else {
        	isTransactional = true;
        }
        return executeUpdate(query, variables, nullVars, isTransactional, connection);
    }

    public int executeUpdate(String query, HashMap<Integer, Object> variables, HashMap<Integer, Integer> nullVars, boolean isTransactional, Connection connection) {
    	assertConnectionIsValid(connection);
        clearSignals();
        
        PreparedStatement ps = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables, nullVars);
        try {
            ps = connection.prepareStatement(query);
            ps = psf.generate(ps);// enter variables here!
            int rowCount = ps.executeUpdate();
            if (rowCount < 1) {
                logger.warn("Executing update query did not change anything, EntityDAO: %s", query);
                throw new SQLException();
            }
            signalSuccess();
            logger.debug("Executing dynamic query, EntityDAO: %s", query);
        	return rowCount;
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing statement, EntityDAO.executeUpdate: %s: %s", query, sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
            return -1;
        } finally {
            if (isTransactional) {
                this.closeIfNecessary(ps);
            } else {
                this.closeIfNecessary(connection, ps);
            }
        }    	
    }

    /**
     * This method inserts one row for an entity table and gets latestPK of this row.
     *
     * @param query
     * @param variables
     * @param nullVars
     *
     * @author ywang 11-26-2007
     */
    public void executeUpdateWithPK(String query, HashMap<Integer, Object> variables, HashMap<Integer, Integer> nullVars) {
        clearSignals();

        Connection connection = null;
        boolean isTransactional = true;
        try {
            connection = getConnection(ds);
            assertConnectionIsValid(connection);
            int rowCount = executeUpdate(query, variables, nullVars, isTransactional, connection);
            if (rowCount != 1) {
                logger.warn("Problem with executing query, EntityDAO: %s", query);
                throw new SQLException();
            }
            logger.debug("Executing query, EntityDAO: %s", query);
            this.latestPK = getCurrentPK(connection);
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing statement, EntityDAO.execute: %s: %s", query, sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(connection);
        }
    }

    /*
     * Currently, latestPK is set only in executeWithPK() after inserting has been executed successfully. So, this
     * method should be called only immediately
     * after executeWithPK()
     * 
     * @return ywang 11-26-2007
     */
    protected int getLatestPK() {
        return latestPK;
    }

    /**
     * Processes the current active row from the given {@link ResultSet}.
     * 
     * @param rs the {@link ResultSet} to process
     * @return a list of column name to cell value mapping
     * @see ResultSet#next()
     */
    public ArrayList<HashMap<String, Object>> processResultRows(ResultSet rs) {
        ArrayList<HashMap<String, Object>> al = new ArrayList<>();

        try {
            while (rs.next()) {
                HashMap<String, Object> columnValues = processCurrentRow(rs);
                al.add(columnValues);
            }
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.select: " + ": " + sqle.getMessage() + ": array length: " + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }
        return al;
    }
    
    /**
     * 
     * @param rs
     * @return
     * @throws SQLException
     */
    public HashMap<String, Object> processCurrentRow(ResultSet rs) throws SQLException {
        HashMap<String, Object> cellValues = new HashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        
        for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
            String columnName = rsmd.getColumnName(columnIndex).toLowerCase();
            
            Integer type = getColumnType(setTypes, columnIndex);            
			if (type == null) {
				/* TODO Throwing an exception seems to be more
				 * more reasonable then just silently ignore the
				 * missing type. 
				 */
				continue;
			}

	        Class<?> resultClass = TypeNames.getReturnType(type);
            Object cellValue = rs.getObject(columnIndex, resultClass);
			cellValue = returnSelfOrDefault(rs.wasNull(), columnName, cellValue, type);
			cellValues.put(columnName, cellValue);
        }
        return cellValues;
    }
	
	/**
	 * Returns a default value if necessary. The necessity is based on the value of wasNull, the type and 
	 * the columnName.
	 *  
	 * @param wasNull indicates if the value for requested cell was null {@link ResultSet#wasNull()} 
	 * @param columnName name of the column
	 * @param cellValue the current cell value
	 * @param type data type of the cell {@link #getColumnType(HashMap, int)}
	 * 
	 * @return a default value if necessary, other the given cellValue 
	 */
	public Object returnSelfOrDefault(boolean wasNull, String columnName, Object cellValue, Integer type) {
		if(!wasNull) {
			return cellValue;
		}
		switch (type.intValue()) {
		case TypeNames.DATE:
        case TypeNames.TIMESTAMP:
			// no default value just echo the value
        	break;
        case TypeNames.DOUBLE:
        	cellValue = Double.valueOf(0d);
        	break;
        case TypeNames.FLOAT:
        	cellValue = Float.valueOf(0f);
        	break;
        case TypeNames.INT:
        	cellValue = Integer.valueOf(0);
        	break;
        case TypeNames.LONG:
        	cellValue = Long.valueOf(0);
        	break;
		case TypeNames.BOOL:
			if (columnName.equalsIgnoreCase("start_time_flag")
					|| columnName.equalsIgnoreCase("end_time_flag")) {
				cellValue = new Boolean(false);
			} else {
				cellValue = new Boolean(true);
			}
        	break;
		case TypeNames.STRING:
			cellValue = "";
        	break;
		case TypeNames.CHAR:
			cellValue = new Character('x');
        	break;
		}
		return cellValue;
	}
	
	/**
	 * Returns the column type for the given column index.
	 * 
	 * @param types mapping of column index and column type
	 * @param columnIndex index of the requested column (starting with 1)
	 * 
	 * @return integer that represents the column type
	 * @see TypeNames
	 */
	public Integer getColumnType(HashMap<Integer, Integer> types, int columnIndex) {
		if(types == null) {
			throw new IllegalArgumentException("The given type map is null.");
		}
		if(columnIndex < 1 || columnIndex > types.size()) {
			String msg = "The given column index '%d' is not within the allowed range of [1,%d].";
			throw new IllegalArgumentException(String.format(msg, columnIndex, types.size()));
		}
		Integer type = types.get(columnIndex);
		if(type == null) {
			String msg = "No type defined for column '%d'";
			throw new RuntimeException(String.format(msg, columnIndex));
		}
		return type;
	}

    /*
     * @return the current value of the primary key sequence, if <code> getNextPKName </code> is non-null, or null if
     * <code> getNextPKName </code> is null.
     */
    public int getNextPK() {
        int answer = 0;

        if (getNextPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.LONG);

        String query = digester.getQuery(getNextPKName);
        ArrayList<HashMap<String, Object>> al = select(query);

        if (al.size() > 0) {
            HashMap<String, ?> h = al.get(0);
            answer = ((Long) h.get("key")).intValue();
        }

        return answer;
    }

    /*
     * @return the current value of the primary key sequence, if <code> getCurrentPKName </code> is non-null, or null if
     * <code> getCurrentPKName </code> is
     * null.
     */
    public int getCurrentPK() {
    	Connection connection = null;
    	try {
			connection = getConnection(ds);
	    	return getCurrentPK(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			this.closeIfNecessary(connection);
		}
    }
    
    public int getCurrentPK(Connection connection) {
        int answer = 0;

        if (getCurrentPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.LONG);

        String queryForPK = digester.getQuery(getCurrentPKName);
        ArrayList<HashMap<String, Object>> al = select(queryForPK, connection);

        if (al.size() > 0) {
            HashMap<String, Object> h = al.get(0);
            answer = ((Long) h.get("key")).intValue();
        }
        return answer;
    }

    /**
     * This method executes a "findByPK-style" query. Such a query has two characteristics:
     * <ol>
     * <li>The columns SELECTed by the SQL are all of the columns in the table relevant to the DAO, and only those
     * columns. (e.g., in StudyDAO, the columns
     * SELECTed are all of the columns in the study table, and only those columns.)
     * <li>It returns at most one EntityBean.
     * <ul>
     * <li>Typically this means that the WHERE clause includes the columns in a candidate key with "=" criteria.
     * <li>e.g., "WHERE item_id = ?" when selecting from item
     * <li>e.g., "WHERE item_id = ? AND event_crf_id=?" when selecting from item_data
     * </ol>
     *
     * Note that queries which join two tables may be included in the definition of "findByPK-style" query, as long as
     * the first criterion is met.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @param variables
     *            The set of variables used to populate the PreparedStatement; should be empty if none are needed.
     * @return The EntityBean selected by the query.
     */
    public EntityBean executeFindByPKQuery(String queryName, HashMap<Integer, Object> variables) {
        EntityBean answer = new EntityBean();

        String sql = digester.getQuery(queryName);
        logger.debug("query: %s, variables: %s", queryName, variables);

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        if(rows.size() > 0) {
        	answer = (EntityBean) this.getEntityFromHashMap(rows.get(0));
        }

        return answer;
    }

    /**
     * Exactly equivalent to calling <code>executeFindByPKQuery(queryName, new HashMap())</code>.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @return The EntityBean selected by the query.
     */
    public EntityBean executeFindByPKQuery(String queryName) {
        return executeFindByPKQuery(queryName, new HashMap<>());
    }

    /*
     * HELPER FUNCTIONS FOR CLOSING 
     * connections, result sets and prepared statements
     */
    public void closeIfNecessary(Connection con) {
        try {
            if (con != null)
                con.close();
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown while closing the connection.");
                logger.error(sqle.getMessage(), sqle);
            }
        }
    }

    public void closeIfNecessary(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown while closing the ResultSet.");
                logger.error(sqle.getMessage(), sqle);
            }
        }
    }

    public void closeIfNecessary(PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown while closing the PreparedStatement.");
                logger.error(sqle.getMessage(), sqle);
            }
        }
    }

    public void closeIfNecessary(Connection con, ResultSet rs) {
    	closeIfNecessary(rs);
    	closeIfNecessary(con);
    }

    public void closeIfNecessary(Connection con, ResultSet rs, PreparedStatement ps) {
    	closeIfNecessary(ps);
    	closeIfNecessary(rs);
    	closeIfNecessary(con);
    }

    public void closeIfNecessary(ResultSet rs, PreparedStatement ps) {
    	closeIfNecessary(ps);
    	closeIfNecessary(rs);
    }

    public void closeIfNecessary(Connection con, PreparedStatement ps) {
    	closeIfNecessary(ps);
    	closeIfNecessary(con);
    }

    /**
     * getDS, had to add it to allow queries of other daos within the daos
     *
     * @return Returns the ds.
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * @param ds
     *            The ds to set.
     */
    // public void setDs(DataSource ds) {
    // this.ds = ds;
    // }
    /**
     * Clear the signals which indicate the success or failure of the query. This method should be called at the
     * beginning of every select or execute method.
     */
    protected void clearSignals() {
        querySuccessful = false;
    }

    /**
     * Signal that the query was successful. Either this method or signalFailure should be called by the time a select
     * or execute method returns.
     */
    protected void signalSuccess() {
        querySuccessful = true;
    }

    /**
     * Signal that the query was unsuccessful. Either this method or signalSuccess should be called by the time a select
     * or execute method returns.
     *
     * @param sqle
     *            The SQLException which was thrown by PreparedStatement.execute/executeUpdate.
     */
    protected void signalFailure(SQLException sqle) {
        querySuccessful = false;
        failureDetails = sqle;
    }

    /**
     * @return Returns the failureDetails.
     */
    public SQLException getFailureDetails() {
        return failureDetails;
    }

    /**
     * @return Returns the querySuccessful.
     */
    public boolean isQuerySuccessful() {
        return querySuccessful;
    }
    
    protected <T> T getValueOrDefault(HashMap<String, Object> hm, String column, T defaultValue, Class<T> resultType) {
        if (hm.containsKey(column)) {
            try {
                @SuppressWarnings("unchecked")
				T value = (T) hm.get(column);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    protected String selectString(HashMap<String, Object> hm, String column) {
    	return getValueOrDefault(hm, column, "", String.class);
    }

    protected int selectInt(HashMap<String, Object> hm, String column) {
    	return getValueOrDefault(hm, column, 0, Integer.class).intValue();
    }

    protected boolean selectBoolean(HashMap<String, Object> hm, String column) {
    	return getValueOrDefault(hm, column, false, Boolean.class);
    }

    public void initializeI18nStrings() {
        if (locale != null) {
            // oc_df_string = ResourceBundleProvider.getFormatBundle(locale).getString("oc_date_format_string");
            oc_df_string = ApplicationConstants.getDateFormatInItemData();
            local_df_string = ResourceBundleProvider.getFormatBundle(locale).getString("date_format_string");
        }
    }

    /**
     * ********************************************************************************
     * ********************************************************************************
     *
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - a new section that uses a different way to access the data -
     *      this is to improve performance and fix
     *      some bugs with the data extraction
     *      *******************************************************************************
     *
     *
     *      @ywang, 09-09-2008, modified syntax of some sql scripts for oracle database.
     *
     */
    /**
     * select, a static query interface to the database, returning an array of hashmaps that contain key->object pairs.
     * <P>
     * This is the first operation created for the database, so therefore it is the simplest; cull information from the
     * database but not specify any parameters.
     *
     * @param query
     *            a static query of the database.
     * @return ArrayList of HashMaps carrying the database values.
     */
    public ArrayList<StudySubjectBean> selectStudySubjects(int studyid, int parentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        clearSignals();
        String query = getSQLSubjectStudySubjectDataset(studyid, parentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint,
                CoreResources.getDBName());
        logger.debug("sqlSubjectStudySubjectDataset=" + query);
        ArrayList<StudySubjectBean> results = new ArrayList<>();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection(ds);
            con.setAutoCommit(false);
            assertConnectionIsValid(con);

            ps = con.prepareStatement(query);
            ps.setFetchSize(50);
            rs = ps.executeQuery();
            if (logger.isInfoEnabled()) {
                logger.debug("Executing static query, GenericDAO.select: " + query);
            }
            signalSuccess();
            results = this.processStudySubjects(rs);
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }
    
    /*
     * HELPER METHODS FOR ACCESSING THE RESULTSET VALUES
     */
    
    /**
     * Returns the {@link ResultSet} value from the given column as Integer. If the
     * value is null the given default value will be returned.
     *  
     * @param rs {@link ResultSet} that holds the values
     * @param columnLabel label to access the column value
     * @param defaultValue value to return when the result set contains no value for this column
     * @return the value from the {@link ResultSet} or the default value
     * 
     * @throws SQLException
     * @see {@link ResultSet#getInt(String)}
     */
    public Integer getAsInt(ResultSet rs, String columnLabel, Integer defaultValue) throws SQLException {
    	if(rs == null) {    		
    		throw new IllegalArgumentException("The given ResultSet is null");
    	}
        Integer result = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            result = defaultValue;
        }
        return result;
    }
    
    /**
     * Returns the {@link ResultSet} value from the given column as String. If the
     * value is null the given default value will be returned.
     *  
     * @param rs {@link ResultSet} that holds the values
     * @param columnLabel label to access the column value
     * @param defaultValue value to return when the result set contains no value for this column
     * @return the value from the {@link ResultSet} or the default value
     * 
     * @throws SQLException
     * @see {@link ResultSet#getString(String)}
     */
    public String getAsString(ResultSet rs, String columnLabel, String defaultValue) throws SQLException {
    	if(rs == null) {    		
    		throw new IllegalArgumentException("The given ResultSet is null");
    	}
    	String result = rs.getString(columnLabel);
        if (rs.wasNull()) {
            result = defaultValue;
        }
        return result;
    }
    
    /**
     * Returns the {@link ResultSet} value from the given column as Boolean. If the
     * value is null the given default value will be returned.
     *  
     * @param rs {@link ResultSet} that holds the values
     * @param columnLabel label to access the column value
     * @param defaultValue value to return when the result set contains no value for this column
     * @return the value from the {@link ResultSet} or the default value
     * 
     * @throws SQLException
     * @see {@link ResultSet#getString(String)}
     */
    public Boolean getAsBoolean(ResultSet rs, String columnLabel, Boolean defaultValue) throws SQLException {
    	if(rs == null) {    		
    		throw new IllegalArgumentException("The given ResultSet is null");
    	}
    	Boolean result = rs.getBoolean(columnLabel);
        if (rs.wasNull()) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Converts the given {@link ResultSet} into a list of {@link StudySubjectBean}.
     * 
     * @param rs the result set from a previous database query
     * 
     * @return a list of {@link StudySubjectBean}
     */
    public ArrayList<StudySubjectBean> processStudySubjects(ResultSet rs) {
        ArrayList<StudySubjectBean> al = new ArrayList<>();
        
        try {
            while (rs.next()) {
                StudySubjectBean obj = new StudySubjectBean();
                // first column
                obj.setId(getAsInt(rs, "study_subject_id", 0));

                // second column                
                obj.setSubjectId(getAsInt(rs, "subject_id", 0));

                // old subject_identifier
                obj.setLabel(getAsString(rs, "label", ""));

                obj.setDateOfBirth(rs.getDate("date_of_birth"));
                
                String gender = getAsString(rs, "gender", null);
                if (gender != null && gender.length() > 0) {
                    obj.setGender(gender.charAt(0));
                } else {
                    obj.setGender(' ');
                }

                obj.setUniqueIdentifier(getAsString(rs, "unique_identifier", ""));

                // Date of birth
                if (CoreResources.getDBName().equals("oracle")) {
                    obj.setDobCollected(getAsString(rs, "dob_collected", "").equals("1") ? true : false);
                } else {
                    obj.setDobCollected(getAsBoolean(rs, "dob_collected", false));
                }

                Status status = Status.get(getAsInt(rs, "status_id", 0));
                obj.setStatus(status);

                obj.setSecondaryLabel(getAsString(rs, "secondary_label", ""));

                // add
                al.add(obj);
            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Exception while processing result rows, EntityDAO.processStudySubjects: " + ": " + sqle.getMessage() + ": array length: " + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    protected String getSQLSubjectStudySubjectDataset(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint,
            String ecStatusConstraint, String itStatusConstraint, String databaseName) {
        /**
         *
         * SELECT
         *
         * DISTINCT ON (study_subject.study_subject_id ) study_subject.study_subject_id , study_subject.subject_id,
         * study_subject.label, subject.date_of_birth,
         * subject.gender, subject.unique_identifier,subject.dob_collected, subject.status_id,
         * study_subject.secondary_label, study_event.start_time_flag,
         * study_event.end_time_flag FROM study_subject
         *
         *
         * JOIN subject ON (study_subject.subject_id = subject.subject_id::numeric) JOIN study_event ON
         * (study_subject.study_subject_id =
         * study_event.study_subject_id)
         *
         *
         * WHERE
         *
         * study_subject.study_subject_id IN (
         *
         * SELECT DISTINCT studysubjectid FROM
         *
         * (SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO )
         *
         *
         *
         */
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return "SELECT distinct study_subject.study_subject_id , study_subject.label,  study_subject.subject_id, "
                    + "  subject.date_of_birth, subject.gender, subject.unique_identifier, subject.dob_collected,  "
                    + "  subject.status_id, study_subject.secondary_label" + "  FROM  " + "     study_subject "
                    + "  JOIN subject ON (study_subject.subject_id = subject.subject_id)  " + "  WHERE  " + "     study_subject.study_subject_id IN  " + "  ( "
                    + "SELECT DISTINCT studysubjectid FROM " + "( "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint) + " ) SBQTWO "
                    + "  ) order by study_subject.study_subject_id";
        } else {
            return " SELECT   " + " DISTINCT ON (study_subject.study_subject_id ) "
                    + " study_subject.study_subject_id , study_subject.label,  study_subject.subject_id, "
                    + "  subject.date_of_birth, subject.gender, subject.unique_identifier, subject.dob_collected,  "
                    + "  subject.status_id, study_subject.secondary_label, study_event.start_time_flag, study_event.end_time_flag  " + "  FROM  "
                    + "   study_subject " + "  JOIN subject ON (study_subject.subject_id = subject.subject_id::numeric)  "
                    + "  JOIN study_event ON (study_subject.study_subject_id = study_event.study_subject_id) " + "  WHERE  "
                    + "   study_subject.study_subject_id IN  " + "  ( " + "SELECT DISTINCT studysubjectid FROM " + "( "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint)
                    + " ) AS SBQTWO " + "  ) ";

        }
    }

    /**
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @param it_in
     * @param eb
     * @return
     */
    public boolean loadBASE_ITEMGROUPSIDEHashMap(int studyid, int parentid, String sedin, String it_in, ExtractBean eb) {
        clearSignals();
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLDatasetBASE_ITEMGROUPSIDE(studyid, parentid, sedin, it_in, genDatabaseDateConstraint(eb), ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008 >>
        logger.error("sqlDatasetBase_itemGroupside=" + query);
        boolean bret = false;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            ps.setFetchSize(50);

            rs = ps.executeQuery();
            if (logger.isInfoEnabled()) {
                logger.debug("Executing static query, GenericDAO.select: " + query);
            }
            signalSuccess();
            processBASE_ITEMGROUPSIDERecords(rs, eb);
            bret = true;
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return bret;

    }//

    /**
     * Main function call
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @param it_in
     * @param eb
     * @return
     */
    public boolean loadBASE_EVENTINSIDEHashMap(int studyid, int parentid, String sedin, String it_in, ExtractBean eb) {
        clearSignals();
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLDatasetBASE_EVENTSIDE(studyid, parentid, sedin, it_in, this.genDatabaseDateConstraint(eb), ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008>>
        logger.error("sqlDatasetBase_eventside=" + query);
        boolean bret = false;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);
            ps.setFetchSize(50);

            rs = ps.executeQuery();
            logger.debug("Executing static query, GenericDAO.select: " + query);
            signalSuccess();
            bret = processBASE_EVENTSIDERecords(rs, eb);
            bret = true;
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return bret;

    }//

    /**
     * For each item_data_id stores a getSQLDatasetBASE_ITEMGROUPSIDE object
     *
     * @param rs
     * @return
     */
    public boolean processBASE_ITEMGROUPSIDERecords(ResultSet rs, ExtractBean eb) {
        /**
         * fields are: SELECT itemdataid, itemdataordinal, item_group_metadata.item_group_id , item_group.name,
         * itemdesc, itemname, itemvalue, itemunits,
         * crfversioname, crfversionstatusid, dateinterviewed, interviewername, eventcrfdatecompleted,
         * eventcrfdatevalidatecompleted,
         * eventcrfcompletionstatusid, repeat_number, crfid,
         *
         *
         * //and ids studysubjectid, eventcrfid, itemid, crfversionid
         *
         */
        try {
            String defaultItemValue = Utils.convertedItemDateValue("", oc_df_string, local_df_string);
            
            while (rs.next()) {

                // itemdataid
                Integer vitemdataid = getAsInt(rs, "itemdataid", null);
                if (vitemdataid == null) {
                    // TODO ERROR - should always be different than NULL
                }

                // itemdataordinal
                Integer vitemdataordinal = getAsInt(rs, "itemdataordinal", null);
                if (vitemdataordinal == null) {
                    // TODO ERROR - should always be different than NULL
                }

                // item_group_id
                Integer vitem_group_id = getAsInt(rs, "item_group_id", null);
                if (vitem_group_id == null) {
                    // TODO ERROR - should always be different than NULL
                }

                // itemgroupname
                String vitemgroupname = getAsString(rs, "name", "");
                if ("ungrouped".equalsIgnoreCase(vitemgroupname) && vitemdataordinal <= 0) {
                    vitemdataordinal = 1;
                }

                // itemdesc
                String vitemdesc = rs.getString("itemdesc");
                if (rs.wasNull()) {
                    vitemdesc = new String("");
                }

                // itemname
                String vitemname = rs.getString("itemname");
                if (rs.wasNull()) {
                    vitemname = new String("");
                }

                String vitemvalue = getAsString(rs, "itemvalue", defaultItemValue);
                
                // itemunits
                String vitemunits = getAsString(rs, "itemunits", "");

                // crfversioname
                String vcrfversioname = getAsString(rs, "crfversioname", "");

                // crfversionstatusid
                Integer vcrfversionstatusid = getAsInt(rs, "crfversionstatusid", null);
                if (vcrfversionstatusid == null) {
                    // TODO - what value default
                    // vcrfversionstatusid = Integer.valueOf(?);
                }

                // dateinterviewed
                Date vdateinterviewed = rs.getDate("dateinterviewed");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // interviewername
                String vinterviewername = rs.getString("interviewername");
                if (rs.wasNull()) {
                    vinterviewername = new String("");
                }

                // eventcrfdatecompleted
                Timestamp veventcrfdatecompleted = rs.getTimestamp("eventcrfdatecompleted");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfdatevalidatecompleted
                Timestamp veventcrfdatevalidatecompleted = rs.getTimestamp("eventcrfdatevalidatecompleted");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfcompletionstatusid
                Integer veventcrfcompletionstatusid = Integer.valueOf(rs.getInt("eventcrfcompletionstatusid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // repeat_number
                Integer vrepeat_number = Integer.valueOf(rs.getInt("repeat_number"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfid
                Integer vcrfid = Integer.valueOf(rs.getInt("crfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // studysubjectid
                Integer vstudysubjectid = Integer.valueOf(rs.getInt("studysubjectid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfid
                Integer veventcrfid = Integer.valueOf(rs.getInt("eventcrfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // itemid
                Integer vitemid = Integer.valueOf(rs.getInt("itemid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfversionid
                Integer vcrfversionid = Integer.valueOf(rs.getInt("crfversionid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                Integer eventcrfstatusid = Integer.valueOf(rs.getInt("eventcrfstatusid"));
                Integer itemdatatypeid = Integer.valueOf(rs.getInt("itemDataTypeId"));

                // add it to the HashMap
                eb.addEntryBASE_ITEMGROUPSIDE(/* Integer pitemDataId */vitemdataid, /* Integer vitemdataordinal */vitemdataordinal,
                        /* Integer pitemGroupId */vitem_group_id, /* String pitemGroupName */vitemgroupname, itemdatatypeid,
                        /* String pitemDescription */vitemdesc, /* String pitemName */vitemname, /* String pitemValue */vitemvalue,
                        /* String pitemUnits */vitemunits, /* String pcrfVersionName */vcrfversioname,
                        /* Integer pcrfVersionStatusId */vcrfversionstatusid, /* Date pdateInterviewed */vdateinterviewed,
                        /* String pinterviewerName, */vinterviewername, /* Timestamp peventCrfDateCompleted */veventcrfdatecompleted,
                        /* Timestamp peventCrfDateValidateCompleted */veventcrfdatevalidatecompleted,
                        /* Integer peventCrfCompletionStatusId */veventcrfcompletionstatusid,
                        /* Integer repeat_number */vrepeat_number, /* Integer crfId */vcrfid,
                        /* Integer pstudySubjectId */vstudysubjectid, /* Integer peventCrfId */veventcrfid,
                        /* Integer pitemId */vitemid, /* Integer pcrfVersionId */vcrfversionid, eventcrfstatusid

                );

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.addHashMapEntryBASE_ITEMGROUPSIDE: " + ": " + sqle.getMessage()
                        + ": array length: " + eb.getHBASE_ITEMGROUPSIDE().size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        // if (logger.isInfoEnabled()) {
        logger.debug("Loaded addHashMapEntryBASE_ITEMGROUPSIDE: " + eb.getHBASE_EVENTSIDE().size());
        // logger.info("fond information about result set: was null: "+
        // rs.wasNull());
        // }

        return true;
    }

    /**
     * For each item_data_id stores a getSQLDatasetBASE_ITEMGROUPSIDE object
     *
     * @param rs
     * @return
     */
    public boolean processBASE_EVENTSIDERecords(ResultSet rs, ExtractBean eb) {
        /**
         * fields are: SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * study_event.start_time_flag study_event.end_time_flag study_event.status_id
         * study_event.subject_event_status_id
         *
         *
         * //ids itemid, crfversionid, eventcrfid, studyeventid
         *
         */
    	
        try {
            while (rs.next()) {

                // itemdataid
                Integer vitemdataid = Integer.valueOf(rs.getInt("itemdataid"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // studysubjectid
                Integer vstudysubjectid = Integer.valueOf(rs.getInt("studysubjectid"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // sample_ordinal
                Integer vsample_ordinal = rs.getInt("sample_ordinal");
                if (rs.wasNull()) {
                    // TODO
                }

                // study_event_definition_id
                Integer vstudy_event_definition_id = Integer.valueOf(rs.getInt("study_event_definition_id"));
                if (rs.wasNull()) {
                    //
                }

                // name
                String vname = rs.getString("name");
                if (rs.wasNull()) {
                    vname = new String("");
                }

                String vlocation = rs.getString("location");
                // store the
                if (rs.wasNull()) {
                    vlocation = new String("");
                }

                // date_start
                Timestamp vdate_start = rs.getTimestamp("date_start");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // date_end
                Timestamp vdate_end = rs.getTimestamp("date_end");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // BADS FLAG

                // start_time_flag
                Boolean vstart_time_flag;
                if (CoreResources.getDBName().equals("oracle")) {
                    vstart_time_flag = new Boolean(rs.getString("start_time_flag").equals("1") ? true : false);
                    if (rs.wasNull()) {
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vstart_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                    }
                } else {
                    vstart_time_flag = new Boolean(rs.getBoolean("start_time_flag"));
                    if (rs.wasNull()) {
                        // YW 08-17-2007 << Since I didn't investigate
                        // what's the impact if changing true to false,
                        // I only do change for the columns of
                        // "start_time_flag" and "end_time_flag" in the
                        // table study_event
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vstart_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                        // bad idea? what to put, then?
                    }
                } // if

                // end_time_flag
                Boolean vend_time_flag;
                if (CoreResources.getDBName().equals("oracle")) {
                    vend_time_flag = new Boolean(rs.getString("end_time_flag").equals("1") ? true : false);
                    if (rs.wasNull()) {
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vend_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                    }
                } else {
                    vend_time_flag = new Boolean(rs.getBoolean("end_time_flag"));
                    if (rs.wasNull()) {
                        // YW 08-17-2007 << Since I didn't investigate
                        // what's the impact if changing true to false,
                        // I only do change for the columns of
                        // "start_time_flag" and "end_time_flag" in the
                        // table study_event
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vend_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                        // bad idea? what to put, then?
                    }
                } // if

                // status_id
                Integer vstatus_id = Integer.valueOf(rs.getInt("status_id"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // subject_event_status_id
                Integer vsubject_event_status_id = Integer.valueOf(rs.getInt("subject_event_status_id"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // studyeventid
                Integer vstudyeventid = Integer.valueOf(rs.getInt("studyeventid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfid
                Integer veventcrfid = Integer.valueOf(rs.getInt("eventcrfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // itemid
                Integer vitemid = Integer.valueOf(rs.getInt("itemid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfversionid
                Integer vcrfversionid = Integer.valueOf(rs.getInt("crfversionid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // add it to the HashMap
                eb.addEntryBASE_EVENTSIDE(/* Integer pitemDataId */vitemdataid, /* Integer pstudySubjectId */vstudysubjectid,
                        /* Integer psampleOrdinal */vsample_ordinal, /* Integer pstudyEvenetDefinitionId */vstudy_event_definition_id,
                        /* String pstudyEventDefinitionName */vname, /* String pstudyEventLoacation */vlocation,
                        /* Timestamp pstudyEventDateStart */vdate_start, /* Timestamp pstudyEventDateEnd */vdate_end,
                        /* Boolean pstudyEventStartTimeFlag */vstart_time_flag, /* Boolean pstudyEventEndTimeFlag */vend_time_flag,
                        /* Integer pstudyEventStatusId */vstatus_id, /* Integer pstudyEventSubjectEventStatusId */vsubject_event_status_id,
                        /* Integer pitemId */vitemid, /* Integer pcrfVersionId */vcrfversionid,
                        /* Integer peventCrfId */veventcrfid, /* Integer pstudyEventId */vstudyeventid

                );

                // add the item_data_id
                eb.addItemDataIdEntry(vitemdataid);

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.processBASE_EVENTSIDERecords: " + ": " + sqle.getMessage() + ": array length: "
                        + eb.getHBASE_EVENTSIDE().size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        // if (logger.isInfoEnabled()) {
        logger.debug("Loaded addHashMapEntryBASE_EVENTSIDE: " + eb.getHBASE_EVENTSIDE().size());
        // logger.info("fond information about result set: was null: "+
        // rs.wasNull());
        // }

        return true;
    }

    /**
     * There are two base querries for dataset
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @param it_in
     * @return
     */
    protected String getSQLDatasetBASE_EVENTSIDE(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {

        /**
         * NEEEDS to replace four elements: - item_id IN (...) from dataset sql - study_event_definition_id IN (...)
         * from sql dataset - study_id and
         * parent_study_id from current study
         *
         * SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id)
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " SELECT  " + " itemdataid,  " + " studysubjectid, study_event.sample_ordinal,  " + " study_event.study_event_definition_id,   "
                    + " study_event_definition.name, study_event.location, study_event.date_start, study_event.date_end, "
                    + " study_event.start_time_flag , study_event.end_time_flag , study_event.status_id, study_event.subject_event_status_id, "
                    + " itemid,  crfversionid,  eventcrfid, studyeventid " + " FROM " + " ( "
                    + " 	SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item.name AS itemname, item.description AS itemdesc,  "
                    + " 	item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + " 	event_crf.study_subject_id as studysubjectid, event_crf.study_event_id AS studyeventid " + " 	FROM item_data, item, event_crf "
                    + " 	JOIN crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + " 	WHERE  " + " 	item_data.item_id = item.item_id " + " 	AND " + " 	item_data.event_crf_id = event_crf.event_crf_id " + " 	AND "
                    + " 	item_data.item_id IN " + it_in + " 	AND item_data.event_crf_id IN  " + " 	( " + " 		SELECT event_crf_id FROM event_crf "
                    + " 		WHERE  " + " 			event_crf.study_event_id IN  " + " 			( "
                    + " 				SELECT study_event_id FROM study_event  " + " 				WHERE "
                    + " 					study_event.study_event_definition_id IN " + sedin + " 				   AND  "
                    + " 					(	study_event.sample_ordinal IS NOT NULL AND "
                    // + " study_event.location IS NOT NULL AND " //JN:Starting 3.1 study event location is no longer
                    // null
                    + " 						study_event.date_start IS NOT NULL  " + " 					) " + " 				   AND "
                    + " 					study_event.study_subject_id IN " + " 				   ( "
                    + " 					SELECT DISTINCT study_subject.study_subject_id " + " 					 FROM  	study_subject   "
                    + " 					 JOIN	study  			ON ( " + " 										study.study_id = study_subject.study_id  "
                    + " 									   AND " + " 										(study.study_id= " + studyid
                    + "OR study.parent_study_id= " + studyparentid + ") " + " 									   ) "
                    + " 					 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 					 JOIN	study_event_definition  ON ( "
                    + " 										study.study_id = study_event_definition.study_id " + " 									    OR "
                    + " 										study.parent_study_id = study_event_definition.study_id "
                    + " 									   ) " + " 					 JOIN	study_event  		ON ( "
                    + " 										study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 									   AND "
                    + " 										study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 									   ) " + " 					 JOIN	event_crf  		ON ( "
                    + " 										study_event.study_event_id = event_crf.study_event_id  "
                    + " 									   AND  "
                    + " 										study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 									   AND " + " 										(event_crf.status_id " + ecStatusConstraint
                    + ") " + " 									   ) " + " 					WHERE " + dateConstraint + " 					    AND "
                    + " 						study_event_definition.study_event_definition_id IN " + sedin + " 				   )  " + " 			) "
                    + " 			AND study_subject_id IN ( " + " 				SELECT DISTINCT study_subject.study_subject_id "
                    + " 				 FROM  	study_subject   " + " 				 JOIN	study  			ON ( "
                    + " 									study.study_id  = study_subject.study_id  " + " 								   AND "
                    + " 									(study.study_id= " + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + " 								   ) " + " 				 JOIN	subject  		ON study_subject.subject_id = subject.subject_id  "
                    + " 				 JOIN	study_event_definition  ON ( "
                    + " 									study.study_id  = study_event_definition.study_id  " + " 								    OR  "
                    + " 									study.parent_study_id = study_event_definition.study_id " + " 								   ) "
                    + " 				 JOIN	study_event  		ON ( "
                    + " 									study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 								   AND "
                    + " 									study_event_definition.study_event_definition_id  = study_event.study_event_definition_id  "
                    + " 								   ) " + " 				 JOIN	event_crf  		ON ( "
                    + " 									study_event.study_event_id = event_crf.study_event_id  "
                    + " 								   AND  "
                    + " 									study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 								   AND " + " 									(event_crf.status_id " + ecStatusConstraint + ") "
                    + " 								   ) " + " 				WHERE " + dateConstraint + " 				    AND "
                    + " 					study_event_definition.study_event_definition_id IN " + sedin + " 			) " + " 			AND "
                    + " 			(event_crf.status_id " + ecStatusConstraint + ") " + " 	)  " + " 	AND  " + " 	(item_data.status_id " + itStatusConstraint
                    + ")  " + " ) SBQONE, study_event, study_event_definition " + " WHERE  " + " (study_event.study_event_id = SBQONE.studyeventid) " + " AND "
                    + " (study_event.study_event_definition_id = study_event_definition.study_event_definition_id) " + " ORDER BY itemdataid asc ";
        } else {
            /*
             * TODO: why date constraint has been hard-coded ???
             */
            return " SELECT  " + " itemdataid,  " + " studysubjectid, study_event.sample_ordinal,  " + " study_event.study_event_definition_id,   "
                    + " study_event_definition.name, study_event.location, study_event.date_start, study_event.date_end, "
                    + " study_event.start_time_flag , study_event.end_time_flag , study_event.status_id, study_event.subject_event_status_id, "
                    + " itemid,  crfversionid,  eventcrfid, studyeventid " + " FROM " + " ( "
                    + "   SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item.name AS itemname, item.description AS itemdesc,  "
                    + "   item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + "   event_crf.study_subject_id as studysubjectid, event_crf.study_event_id AS studyeventid " + "   FROM item_data, item, event_crf "
                    + "   JOIN crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + "   WHERE  " + "   item_data.item_id = item.item_id " + "   AND " + "   item_data.event_crf_id = event_crf.event_crf_id " + "   AND "
                    + "   item_data.item_id IN " + it_in + "   AND item_data.event_crf_id IN  " + "   ( " + "       SELECT event_crf_id FROM event_crf "
                    + "       WHERE  " + "           event_crf.study_event_id IN  " + "           ( "
                    + "               SELECT study_event_id FROM study_event  " + "               WHERE "
                    + "                   study_event.study_event_definition_id IN " + sedin + "                  AND  "
                    + "                   (   study_event.sample_ordinal IS NOT NULL AND "
                    // + " study_event.location IS NOT NULL AND " JN: starting 3.1 study_event.location can be null
                    + "                       study_event.date_start IS NOT NULL  " + "                   ) " + "                  AND "
                    + "                   study_event.study_subject_id IN " + "                  ( "
                    + "                   SELECT DISTINCT study_subject.study_subject_id " + "                    FROM   study_subject   "
                    + "                    JOIN   study           ON ( "
                    + "                                       study.study_id::numeric = study_subject.study_id  " + "                                      AND "
                    + "                                       (study.study_id= " + studyid + "OR study.parent_study_id= " + studyparentid + ") "
                    + "                                      ) "
                    + "                    JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                    JOIN   study_event_definition  ON ( "
                    + "                                       study.study_id::numeric = study_event_definition.study_id "
                    + "                                       OR "
                    + "                                       study.parent_study_id = study_event_definition.study_id "
                    + "                                      ) " + "                    JOIN   study_event         ON ( "
                    + "                                       study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                      AND "
                    + "                                       study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                      ) " + "                    JOIN   event_crf       ON ( "
                    + "                                       study_event.study_event_id = event_crf.study_event_id  "
                    + "                                      AND  "
                    + "                                       study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                      AND " + "                                       (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                      ) " + "                   WHERE " + dateConstraint + "                       AND "
                    + "                       study_event_definition.study_event_definition_id IN " + sedin + "                  )  " + "           ) "
                    + "           AND study_subject_id IN ( " + "               SELECT DISTINCT study_subject.study_subject_id "
                    + "                FROM   study_subject   " + "                JOIN   study           ON ( "
                    + "                                   study.study_id::numeric = study_subject.study_id  " + "                                  AND "
                    + "                                   (study.study_id= " + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + "                                  ) "
                    + "                JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                JOIN   study_event_definition  ON ( "
                    + "                                   study.study_id::numeric = study_event_definition.study_id  "
                    + "                                   OR  " + "                                   study.parent_study_id = study_event_definition.study_id "
                    + "                                  ) " + "                JOIN   study_event         ON ( "
                    + "                                   study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                  AND "
                    + "                                   study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                  ) " + "                JOIN   event_crf       ON ( "
                    + "                                   study_event.study_event_id = event_crf.study_event_id  " + "                                  AND  "
                    + "                                   study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                  AND " + "                                   (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                  ) " + "               WHERE " + dateConstraint + "                   AND "
                    + "                   study_event_definition.study_event_definition_id IN " + sedin + "           ) " + "           AND "
                    + "           (event_crf.status_id " + ecStatusConstraint + ") " + "   )  " + "   AND  " + "   (item_data.status_id " + itStatusConstraint
                    + ")  " + " ) AS SBQONE, study_event, study_event_definition " + " WHERE  " + " (study_event.study_event_id = SBQONE.studyeventid) "
                    + " AND " + " (study_event.study_event_definition_id = study_event_definition.study_event_definition_id) " + " ORDER BY itemdataid asc ";
        }
    }// getSQLDatasetBASE_EVENTSIDE

    /**
     * This is the second base sql
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @param it_in
     * @return
     */
    protected String getSQLDatasetBASE_ITEMGROUPSIDE(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint,
            String ecStatusConstraint, String itStatusConstraint) {
        /**
         * NEEEDS to replace four elements: - item_id IN (...) from dataset sql - study_event_definition_id IN (...)
         * from sql dataset - study_id and
         * parent_study_id from current study
         *
         *
         * SELECT itemdataid, itemdataordinal, item_group_metadata.item_group_id , item_group.name, itemdesc, itemname,
         * itemvalue, itemunits, crfversioname,
         * crfversionstatusid, crfid, item_group_metadata.repeat_number, dateinterviewed, interviewername,
         * eventcrfdatevalidatecompleted,eventcrfcompletionstatusid,
         *
         *
         * studysubjectid, eventcrfid, itemid, crfversionid FROM ( SELECT item_data.item_data_id AS itemdataid,
         * item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item_data.ordinal AS itemdataordinal, item.name AS itemname, item.description AS itemdesc,
         * item.units AS itemunits, event_crf.event_crf_id
         * AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,
         * crf_version.crf_id AS crfid, event_crf.study_subject_id
         * as studysubjectid, crf_version.status_id AS crfversionstatusid, event_crf.date_interviewed AS
         * dateinterviewed, event_crf.interviewer_name as
         * interviewername, event_crf.date_validate_completed AS eventcrfdatevalidatecompleted,
         * event_crf.completion_status_id AS eventcrfcompletionstatusid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, item_group_metadata, item_group
         *
         *
         *
         * WHERE
         *
         * (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid)
         *
         * AND
         *
         * (item_group.item_group_id = item_group_metadata.item_group_id)
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " SELECT  " + " itemdataid,  itemdataordinal,"
                    + " item_group_metadata.item_group_id , item_group.name, itemdatatypeid, itemdesc, itemname, itemvalue, itemunits, "
                    + " crfversioname, crfversionstatusid, crfid, item_group_metadata.repeat_number, "
                    + " dateinterviewed, interviewername, eventcrfdatevalidatecompleted, eventcrfdatecompleted, eventcrfcompletionstatusid, "
                    + " studysubjectid, eventcrfid, itemid, crfversionid, eventcrfstatusid " + " FROM " + " ( "
                    + " 	SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item_data.ordinal AS itemdataordinal, item.item_data_type_id As itemdatatypeid, item.name AS itemname, item.description AS itemdesc,  "
                    + " 	item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + " 	event_crf.study_subject_id as studysubjectid, crf_version.status_id AS crfversionstatusid, crf_version.crf_id AS crfid, "
                    + "   event_crf.date_interviewed AS dateinterviewed, event_crf.interviewer_name AS interviewername, event_crf.date_completed AS eventcrfdatecompleted, "
                    + " 	event_crf.date_validate_completed AS eventcrfdatevalidatecompleted, event_crf.completion_status_id AS eventcrfcompletionstatusid, event_crf.status_id AS eventcrfstatusid "
                    + " 	FROM item_data, item, event_crf "
                    + " 	join crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + " 	WHERE  " + " 	item_data.item_id = item.item_id " + " 	AND " + " 	item_data.event_crf_id = event_crf.event_crf_id " + " 	AND "
                    + " 	item_data.item_id IN " + it_in + " 	AND item_data.event_crf_id IN " + " 	( " + " 		SELECT event_crf_id FROM event_crf "
                    + " 		WHERE  " + " 			event_crf.study_event_id IN  " + " 			( "
                    + " 				SELECT study_event_id FROM study_event  " + " 				WHERE "
                    + " 					study_event.study_event_definition_id IN " + sedin + " 				   AND  "
                    + " 					(	study_event.sample_ordinal IS NOT NULL AND " + " 						study_event.location IS NOT NULL AND "
                    + " 						study_event.date_start IS NOT NULL  " + " 					) " + " 				   AND "
                    + " 					study_event.study_subject_id IN " + " 				   ( "
                    + " 					SELECT DISTINCT study_subject.study_subject_id " + " 					 FROM  	study_subject   "
                    + " 					 JOIN	study  			ON ( " + " 										study.study_id = study_subject.study_id  "
                    + " 									   AND " + " 										(study.study_id=" + studyid
                    + " OR study.parent_study_id= " + studyparentid + ") " + " 									   ) "
                    + " 					 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 					 JOIN	study_event_definition  ON ( "
                    + " 										study.study_id = study_event_definition.study_id  "
                    + " 									    OR  "
                    + " 										study.parent_study_id = study_event_definition.study_id "
                    + " 									   ) " + " 					 JOIN	study_event  		ON ( "
                    + " 										study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 									   AND "
                    + " 										study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 									   ) " + " 					 JOIN	event_crf  		ON ( "
                    + " 										study_event.study_event_id = event_crf.study_event_id  "
                    + " 									   AND  "
                    + " 										study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 									   AND " + " 										(event_crf.status_id " + ecStatusConstraint
                    + ") " + " 									   ) " + " 					WHERE " + dateConstraint + " 					    AND "
                    + " 						study_event_definition.study_event_definition_id IN " + sedin + " 				   )  " + " 			) "
                    + " 			AND study_subject_id IN ( " + " 				SELECT DISTINCT study_subject.study_subject_id "
                    + " 				 FROM  	study_subject   " + " 				 JOIN	study  			ON ( "
                    + " 									study.study_id = study_subject.study_id  " + " 								   AND "
                    + " 									(study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + " )"
                    + " 								   ) " + " 				 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 				 JOIN	study_event_definition  ON ( "
                    + " 									study.study_id = study_event_definition.study_id  " + " 								    OR  "
                    + " 									study.parent_study_id = study_event_definition.study_id " + " 								   ) "
                    + " 				 JOIN	study_event  		ON ( "
                    + " 									study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 								   AND "
                    + " 									study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 								   ) " + " 				 JOIN	event_crf  		ON ( "
                    + " 									study_event.study_event_id = event_crf.study_event_id  "
                    + " 								   AND  "
                    + " 									study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 								   AND " + " 									(event_crf.status_id " + ecStatusConstraint + ") "
                    + " 								   ) " + " 				WHERE " + dateConstraint + " 				    AND "
                    + " 					study_event_definition.study_event_definition_id IN " + sedin + " 			) " + " 			AND "
                    + " 			(event_crf.status_id " + ecStatusConstraint + ") " + " 	)  " + " 	AND  " + " 	(item_data.status_id " + itStatusConstraint
                    + ")  " + " ) SBQONE, item_group_metadata, item_group " + " WHERE  "
                    + " (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid) " + " AND "
                    + " (item_group.item_group_id = item_group_metadata.item_group_id) " + "  ORDER BY itemdataid asc ";
        } else {
            /*
             * TODO: why date constraint has been hard-coded ???
             */

            return " SELECT  " + " itemdataid,  itemdataordinal,"
                    + " item_group_metadata.item_group_id , item_group.name, itemdatatypeid, itemdesc, itemname, itemvalue, itemunits, "
                    + " crfversioname, crfversionstatusid, crfid, item_group_metadata.repeat_number, "
                    + " dateinterviewed, interviewername, eventcrfdatevalidatecompleted, eventcrfdatecompleted, eventcrfcompletionstatusid, "
                    + " studysubjectid, eventcrfid, itemid, crfversionid, eventcrfstatusid " + " FROM " + " ( "
                    + "   SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item_data.ordinal AS itemdataordinal, item.item_data_type_id AS itemdatatypeid, item.name AS itemname, item.description AS itemdesc,  "
                    + "   item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + "   event_crf.study_subject_id as studysubjectid, crf_version.status_id AS crfversionstatusid, crf_version.crf_id AS crfid, "
                    + "   event_crf.date_interviewed AS dateinterviewed, event_crf.interviewer_name AS interviewername, event_crf.date_completed AS eventcrfdatecompleted, "
                    + "   event_crf.date_validate_completed AS eventcrfdatevalidatecompleted, event_crf.completion_status_id AS eventcrfcompletionstatusid, event_crf.status_id AS eventcrfstatusid "
                    + "   FROM item_data, item, event_crf "
                    + "   join crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + "   WHERE  " + "   item_data.item_id = item.item_id " + "   AND " + "   item_data.event_crf_id = event_crf.event_crf_id " + "   AND "
                    + "   item_data.item_id IN " + it_in + "   AND item_data.event_crf_id IN " + "   ( " + "       SELECT event_crf_id FROM event_crf "
                    + "       WHERE  " + "           event_crf.study_event_id IN  " + "           ( "
                    + "               SELECT study_event_id FROM study_event  " + "               WHERE "
                    + "                   study_event.study_event_definition_id IN " + sedin + "                  AND  "
                    + "                   (   study_event.sample_ordinal IS NOT NULL AND " + "                       study_event.location IS NOT NULL AND "
                    + "                       study_event.date_start IS NOT NULL  " + "                   ) " + "                  AND "
                    + "                   study_event.study_subject_id IN " + "                  ( "
                    + "                   SELECT DISTINCT study_subject.study_subject_id " + "                    FROM   study_subject   "
                    + "                    JOIN   study           ON ( "
                    + "                                       study.study_id::numeric = study_subject.study_id  " + "                                      AND "
                    + "                                       (study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + "                                      ) "
                    + "                    JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                    JOIN   study_event_definition  ON ( "
                    + "                                       study.study_id::numeric = study_event_definition.study_id  "
                    + "                                       OR  "
                    + "                                       study.parent_study_id = study_event_definition.study_id "
                    + "                                      ) " + "                    JOIN   study_event         ON ( "
                    + "                                       study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                      AND "
                    + "                                       study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                      ) " + "                    JOIN   event_crf       ON ( "
                    + "                                       study_event.study_event_id = event_crf.study_event_id  "
                    + "                                      AND  "
                    + "                                       study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                      AND " + "                                       (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                      ) " + "                   WHERE " + dateConstraint + "                       AND "
                    + "                       study_event_definition.study_event_definition_id IN " + sedin + "                  )  " + "           ) "
                    + "           AND study_subject_id IN ( " + "               SELECT DISTINCT study_subject.study_subject_id "
                    + "                FROM   study_subject   " + "                JOIN   study           ON ( "
                    + "                                   study.study_id::numeric = study_subject.study_id  " + "                                  AND "
                    + "                                   (study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + " )"
                    + "                                  ) "
                    + "                JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                JOIN   study_event_definition  ON ( "
                    + "                                   study.study_id::numeric = study_event_definition.study_id  "
                    + "                                   OR  " + "                                   study.parent_study_id = study_event_definition.study_id "
                    + "                                  ) " + "                JOIN   study_event         ON ( "
                    + "                                   study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                  AND "
                    + "                                   study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                  ) " + "                JOIN   event_crf       ON ( "
                    + "                                   study_event.study_event_id = event_crf.study_event_id  " + "                                  AND  "
                    + "                                   study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                  AND " + "                                   (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                  ) " + "               WHERE " + dateConstraint + "                   AND "
                    + "                   study_event_definition.study_event_definition_id IN " + sedin + "           ) " + "           AND "
                    + "           (event_crf.status_id " + ecStatusConstraint + ") " + "   )  " + "   AND  " + "   (item_data.status_id " + itStatusConstraint
                    + ")  " + " ) AS SBQONE, item_group_metadata, item_group " + " WHERE  "
                    + " (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid) " + " AND "
                    + " (item_group.item_group_id = item_group_metadata.item_group_id) " + "  ORDER BY itemdataid asc ";
        }
    }// getSQLDatasetBASE_ITEMGROUPSIDE

    /**
     *
     * @param sedin
     * @param itin
     * @param currentstudyid
     * @param parentstudyid
     * @return
     */
    protected String getSQLInKeyDatasetHelper(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        /**
         * SELECT DISTINCT study_event.study_event_definition_id, study_event.sample_ordinal, crfv.crf_id, it.item_id,
         * ig.name AS item_group_name FROM event_crf
         * ec
         *
         * JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id = 2::numeric OR
         * ec.status_id = 6::numeric) JOIN item_form_metadata
         * ifm ON crfv.crf_version_id = ifm.crf_version_id LEFT JOIN item_group_metadata igm ON ifm.item_id =
         * igm.item_id AND crfv.crf_version_id::numeric =
         * igm.crf_version_id LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id::numeric JOIN item it ON
         * ifm.item_id = it.item_id::numeric JOIN
         * study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id =
         * ec.study_subject_id
         *
         * WHERE ec.event_crf_id IN (
         *
         * SELECT DISTINCT eventcrfid FROM ( SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 ) AND item_data.event_crf_id IN ( SELECT event_crf_id
         * FROM event_crf WHERE
         * event_crf.study_event_id IN ( SELECT study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO )
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " 	SELECT DISTINCT  " + "	study_event.study_event_definition_id,  " + "	study_event.sample_ordinal,  " + "	crfv.crf_id,  "
                    + "	it.item_id,  " + "	ig.name AS item_group_name  " + "	 FROM  " + " 	event_crf ec  "
                    + " JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id " + ecStatusConstraint + ") "
                    + " JOIN item_form_metadata ifm ON crfv.crf_version_id = ifm.crf_version_id  "
                    + " LEFT JOIN item_group_metadata igm ON ifm.item_id = igm.item_id AND crfv.crf_version_id = igm.crf_version_id  "
                    + " LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id  " + " JOIN item it ON ifm.item_id = it.item_id  "
                    + " JOIN study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id = ec.study_subject_id   "
                    + " WHERE ec.event_crf_id IN  " + " (  " + "	SELECT DISTINCT eventcrfid FROM  " + "	(     "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint) + "	) SBQTWO "
                    + " ) ";

        } else {
            return "   SELECT DISTINCT  " + "   study_event.study_event_definition_id,  " + "   study_event.sample_ordinal,  " + "   crfv.crf_id,  "
                    + "   it.item_id,  " + "   ig.name AS item_group_name  " + "    FROM  " + "   event_crf ec  "
                    + " JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id " + ecStatusConstraint + ") "
                    + " JOIN item_form_metadata ifm ON crfv.crf_version_id = ifm.crf_version_id  "
                    + " LEFT JOIN item_group_metadata igm ON ifm.item_id = igm.item_id AND crfv.crf_version_id::numeric = igm.crf_version_id  "
                    + " LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id::numeric  " + " JOIN item it ON ifm.item_id = it.item_id::numeric  "
                    + " JOIN study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id = ec.study_subject_id   "
                    + " WHERE ec.event_crf_id IN  " + " (  " + "   SELECT DISTINCT eventcrfid FROM  " + "   (     "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint)
                    + "   ) AS SBQTWO " + " ) ";
        }
        /**
         * TODO - replace with sql
         */
    }

    /**
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @return
     */
    public HashMap<String, Boolean> setHashMapInKeysHelper(int studyid, int parentid, String sedin, String itin, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        clearSignals();
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLInKeyDatasetHelper(studyid, parentid, sedin, itin, dateConstraint, ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008 >>

        HashMap<String, Boolean> results = new HashMap<>();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: setHashMapInKeysHelper.select!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing static query, setHashMapInKeysHelper.select: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // }
            // ps.close();
            signalSuccess();
            results = this.processInKeyDataset(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return results;

    }//

    /**
     * Return directly the HashMap with the key It shouldn't be NULL !! TODO - throw an error if any of the fields is
     * null!
     *
     * @param rs
     * @return
     */
    public HashMap<String, Boolean> processInKeyDataset(ResultSet rs) {// throws SQLException
        HashMap<String, Boolean> al = new HashMap<>();

        try {
            while (rs.next()) {
                String stsed = new String("");
                stsed = ((Integer) rs.getInt("study_event_definition_id")).toString();
                if (rs.wasNull()) {
                    stsed = new String("");
                }

                // second column
                String stso = new String("");
                stso = ((Integer) rs.getInt("sample_ordinal")).toString();
                if (rs.wasNull()) {
                    stso = new String("");
                }

                String stcrf = new String("");
                stcrf = ((Integer) rs.getInt("crf_id")).toString();
                if (rs.wasNull()) {
                    stcrf = new String("");
                }

                String stitem = new String("");
                stitem = ((Integer) rs.getInt("item_id")).toString();
                if (rs.wasNull()) {
                    stitem = new String("");
                }

                String stgn = new String("");
                stgn = rs.getString("item_group_name");
                if (rs.wasNull()) {
                    stgn = new String("");
                }

                /**
                 * build the key as [study_event_definition_id]_[sample_ordinal]_[crf_id]_[item_id]_[item_group_name]
                 */
                String key = stsed + "_" + stso + "_" + stcrf + "_" + stitem + "_" + stgn;

                // add
                al.put(key, new Boolean(true));

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.loadExtractStudySubject: " + ": " + sqle.getMessage() + ": array length: "
                        + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    public String genDatabaseDateConstraint(ExtractBean eb) {
        String dateConstraint = "";
        String dbName = CoreResources.getDBName();
        String sql = eb.getDataset().getSQLStatement();
        String[] os = sql.split("'");
        if ("postgres".equalsIgnoreCase(dbName)) {
            dateConstraint = 
            		String.format(" (date(study_subject.enrollment_date) >= date('%s')) and (date(study_subject.enrollment_date) <= date('%s'))", 
            				os[1], os[3]);
        } else if ("oracle".equalsIgnoreCase(dbName)) {
            dateConstraint = 
            		String.format(" trunc(study_subject.enrollment_date) >= to_date('%s') and trunc(study_subject.enrollment_date) <= to_date('%s')", 
            				os[1], os[3]);
        }
        return dateConstraint;
    }

    public String getECStatusConstraint(int datasetItemStatusId) {
    	boolean in;
    	List<Status> status;
    	
        switch (datasetItemStatusId) {
        default:
        case 0:
        case 1:
        	in = true;
        	status = Arrays.asList(Status.UNAVAILABLE, Status.LOCKED);
            break;
        case 2:
        	in = false;
        	status = Arrays.asList(Status.UNAVAILABLE, Status.LOCKED, Status.DELETED, Status.AUTO_DELETED);
            break;
        case 3:
        	in = false;
        	status = Arrays.asList(Status.DELETED, Status.AUTO_DELETED);
        }
        return statusListToConstraint(in, status);
    }

    public String getItemDataStatusConstraint(int datasetItemStatusId) {
    	boolean in;
    	List<Status> status;
    	
        switch (datasetItemStatusId) {
        default:
        case 0:
        case 1:
        	in = true;
        	status = Arrays.asList(Status.UNAVAILABLE, Status.LOCKED);
            break;
        case 2:
        	in = false;
        	status = Arrays.asList(Status.LOCKED, Status.DELETED, Status.AUTO_DELETED);
            break;
        case 3:
        	in = false;
        	status = Arrays.asList(Status.DELETED, Status.AUTO_DELETED);
            break;
        }        
        return statusListToConstraint(in, status);
    }
    
    /**
     * Converts the given list of status into an SQL-Constraint.
     * e.g. 
     * <ul>
     * <li>in (2,6)</li>
     * <li>not in (2,6)</li>
     * </ul>
     * 
     * @param in indicates it should be "in" or "not in"
     * @param status list of status
     * 
     * @return SQL-Constraint
     */
    public String statusListToConstraint(boolean in, List<Status> status) {    	
    	String msg = in ? "in (%s)" : "not in (%s)";
    	String statusList = status.stream().map(s -> String.valueOf(s.getId())).collect(Collectors.joining(","));
    	
    	return String.format(msg, statusList);
    }
    
    public UserAccountBean getUserById(Integer userId) {
		UserAccountBean user = null;
		if (userId != null) {
			UserAccountDAO udao = new UserAccountDAO(SessionManager.getStaticDataSource());
			user = (UserAccountBean) udao.findByPK(userId);
		}
		return user;
    }
    
    public Integer getCountByQuery(String query, HashMap<Integer, Object> variables) {
    	return getCountByQuery(query, variables, "count");
    }
    
    public Integer getCountByQuery(String query, HashMap<Integer, Object> variables, String columnName) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.LONG);

        ArrayList<HashMap<String, Object>> rows = select(query, variables);
        if (rows != null && !rows.isEmpty()) {
            Long count = (Long) rows.get(0).get(columnName);
            return count.intValue();
        } else {
            return null;
        }
    }
    
    public Integer getCountByQueryOrDefault(String query, HashMap<Integer, Object> variables, Integer defaultValue) {
    	return getCountByQueryOrDefault(query, variables, "count", defaultValue);
    }
    
    public Integer getCountByQueryOrDefault(String query, HashMap<Integer, Object> variables, String columnName, Integer defaultValue) {
        Integer result = getCountByQuery(query, variables, columnName);
        if(result == null) {
        	result = defaultValue;
        }
        return result;
    }
    
    public abstract B emptyBean();
}