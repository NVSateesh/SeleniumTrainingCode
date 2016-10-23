/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General Public License, Version 3.0 (the "License") you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Class: AFTDbUtil
 * 
 * Purpose: This class contains DB methods to connect to a DB and read the data
 * from DB.
 */

package com.ags.aft.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.remotag.XAFTAppiumConfigProperties;
import com.ags.aft.remotag.XAFTConfigProperties;
import com.ags.aft.remotag.XAFTRobotiumConfigProperties;
import com.ags.aft.remotag.XAFTWebdriverConfigProperties;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestSet;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.testObjects.TestSuite;
import com.ags.aft.util.Helper;

// TODO: Auto-generated Javadoc
/**
 * This Class represents all DB related operations.
 */
public final class DatabaseUtil {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class);

	/** The etlrecords map. */
	private Map<String, String> etlrecordsMap;

	/** The etl data completeness count. */
	private String etlDataCompletenessCount;

	/** The aft dbUtil. */
	private static DatabaseUtil dbUtil;

	/** The enlace url. */
	private String enlaceURL = null;

	private String schemaValidationResult;

	private String schemaType;

	/**
	 * Instantiates a new aft dbUtil.
	 */
	private DatabaseUtil() {
		super();
	}

	/**
	 * Gets the single instance of dbUtil.
	 * 
	 * @return single instance of dbUtil
	 */
	public static DatabaseUtil getInstance() {
		if (dbUtil == null) {
			dbUtil = new DatabaseUtil();
			LOGGER.trace("Creating instance of AFTDbUtil");
		}

		return dbUtil;
	}

	/**
	 * This method will get the test batch information from DB based on project
	 * id.
	 * 
	 * @return testSetList
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<TestSet> loadTestBatch() throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		StringBuffer query = new StringBuffer();
		query.append("select distinct(tb.idTestBatch),te.executionOrder,t.idTestSuite,t.testSet,t.description,t.execution_engine,t.url,t.browser,t.appConfig,t.scenarioInitializationId,t.scenarioCleanupId,t.batchInitializationId,t.batchCleanupId,t.customDictionaryPath,t.spellCheckLanguage,t.spellCheckSuggestion,te.hostName,t.mobileip from testexecutions te INNER JOIN testbatch tb on te.idTestBatch=tb.idTestBatch INNER JOIN testset t on te.idTestSet=t.idTestSet WHERE te.idproject=");
		query.append(RuntimeProperties.getInstance().getProjectId());
		query.append(" and te.idTestexecutions=");
		query.append(RuntimeProperties.getInstance().getTestExecutionId());
		query.append(" and tb.idTestBatch=");
		query.append(RuntimeProperties.getInstance().getTestBatchId());
		query.append(" and te.hostName=");
		query.append("'");
		query.append(RuntimeProperties.getInstance().getHostName());
		query.append("'");
		query.append(" and t.idTestSet in(");
		for (String id : RuntimeProperties.getInstance().getTestSetIds()) {
			query.append(id);
			query.append(",");
		}
		query.deleteCharAt(query.length() - 1);
		query.append(")");
		query.append(" order by te.executionOrder");

		LOGGER.info("Query " + query);
		List<TestSet> testSetList = new ArrayList<TestSet>();
		try {
			LOGGER.info("---------Inside loadTestBatchFromDB method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next()) {
				if (rs.getString("browser").contains(",")) {
					String[] browsers = rs.getString("browser").split(",");
					for (String browser : browsers) {
						TestSet testSet = createTestSet(rs);
						testSet.setBrowser(browser);
						testSet.setTestSetName(rs.getString("testSet") + "^"
								+ browser);
						testSetList.add(testSet);
					}
				} else {
					TestSet testSet = createTestSet(rs);
					String browser = rs.getString("browser");
					String mobileIp = rs.getString("mobileip");
					if ((browser.equalsIgnoreCase("iphone") || browser
							.equalsIgnoreCase("android")) && mobileIp != null) {
						StringBuffer strBuffer = new StringBuffer();
						strBuffer.append(browser).append(":").append(mobileIp);
						testSet.setBrowser(strBuffer.toString());
					} else {
						testSet.setBrowser(browser);
					}
					testSet.setTestSetName(rs.getString("testSet"));
					testSetList.add(testSet);
				}

			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return testSetList;
	}

	/**
	 * This method will insert the link error record into DB.
	 * 
	 * @param idProject
	 *            idProject
	 * @param testSetName
	 *            testSetName
	 * @param testScenarioName
	 *            testScenarioName
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param page
	 *            page
	 * @param exceptionCount
	 *            exceptionCount
	 * @param error404Count
	 *            error404Count
	 * @param error500Count
	 *            error500Count
	 * @param reportTestSuiteId
	 *            reportTestSuiteId
	 * @return int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int insertLinkErrorsData(int idProject, String testSetName,
			String testScenarioName, String startDate, String endDate,
			String page, int exceptionCount, int error404Count,
			int error500Count, int reportTestSuiteId) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int linkErrorId = 0;
		Timestamp sqlStartDate = null;
		Timestamp sqlEndDate = null;
		DateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);
		try {
			Date startDateTime = formatter.parse(startDate);
			sqlStartDate = new Timestamp(startDateTime.getTime());
			if (endDate != null && endDate.length() > 0) {
				Date endDateTime = formatter.parse(endDate);
				sqlEndDate = new Timestamp(endDateTime.getTime());
			}
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "INSERT INTO rpt_link_errors (testSetName, testScenarioName, page, exceptionCount, error404Count, error500Count, idProject,startDate,endDate, idTestSuiteReport) VALUES (?,?,?,?,?,?,?,?,?,?)";
			preparedStatementInsert = conn.prepareStatement(insertSQL,
					Statement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setString(1, testSetName);
			preparedStatementInsert.setString(2, testScenarioName);
			preparedStatementInsert.setString(3, page);
			preparedStatementInsert.setInt(4, exceptionCount);
			preparedStatementInsert.setInt(5, error404Count);
			preparedStatementInsert.setInt(6, error500Count);
			preparedStatementInsert.setInt(7, idProject);
			preparedStatementInsert.setTimestamp(8, sqlStartDate);
			preparedStatementInsert.setTimestamp(9, sqlEndDate);
			preparedStatementInsert.setInt(10, reportTestSuiteId);
			preparedStatementInsert.executeUpdate();
			rs = preparedStatementInsert.getGeneratedKeys();
			if (rs.next()) {
				linkErrorId = rs.getInt(1);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return linkErrorId;
	}

	/**
	 * This method will insert the link error details record into DB.
	 * 
	 * @param errorType
	 *            errorType
	 * @param errorUrl
	 *            errorUrl
	 * @param errorDetails
	 *            errorDetails
	 * @param idrptLinkErrors
	 *            idrptLinkErrors
	 * @return int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int insertLinkErrorsDetails(String errorType, String errorUrl,
			String errorDetails, int idrptLinkErrors) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int count = 0;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "INSERT INTO rpt_linkerror_details (errorType, errorUrl, errorDetails, idrptLinkErrors) VALUES (?,?,?,?)";
			preparedStatementInsert = conn.prepareStatement(insertSQL);
			preparedStatementInsert.setString(1, errorType);
			preparedStatementInsert.setString(2, errorUrl);
			preparedStatementInsert.setString(3, errorDetails);
			preparedStatementInsert.setInt(4, idrptLinkErrors);
			count = preparedStatementInsert.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return count;
	}

	/**
	 * This method will update the link error record into DB.
	 * 
	 * @param idrptLinkErrors
	 *            idrptLinkErrors
	 * @param exceptionCount
	 *            exceptionCount
	 * @param error404Count
	 *            error404Count
	 * @param error500Count
	 *            error500Count
	 * @return int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int updateLinkErrorsData(int idrptLinkErrors, int exceptionCount,
			int error404Count, int error500Count) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int updatedValue = 0;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "UPDATE rpt_link_errors SET exceptionCount =? " + ","
					+ "error404Count =? " + "," + "error500Count =? "
					+ "WHERE idrptLinkErrors = ?";
			preparedStatementInsert = conn.prepareStatement(insertSQL);
			preparedStatementInsert.setInt(1, exceptionCount);
			preparedStatementInsert.setInt(2, error404Count);
			preparedStatementInsert.setInt(3, error500Count);
			preparedStatementInsert.setInt(4, idrptLinkErrors);
			updatedValue = preparedStatementInsert.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return updatedValue;
	}

	/**
	 * This method will update the spell error record into DB.
	 * 
	 * @param idrptSpellErrors
	 *            idrptSpellErrors
	 * @param spellErrorCount
	 *            spellErrorCount
	 * @param grammarErrorCount
	 *            grammarErrorCount
	 * @return int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int updateSpellErrorsData(int idrptSpellErrors, int spellErrorCount,
			int grammarErrorCount) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int updatedValue = 0;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "UPDATE rpt_spell_errors SET spellErrorCount =? " + ","
					+ "grammarErrorCount =? " + "WHERE idrptSpellErrors = ?";
			preparedStatementInsert = conn.prepareStatement(insertSQL);
			preparedStatementInsert.setInt(1, spellErrorCount);
			preparedStatementInsert.setInt(2, grammarErrorCount);
			preparedStatementInsert.setInt(3, idrptSpellErrors);
			updatedValue = preparedStatementInsert.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return updatedValue;
	}

	/**
	 * This method will insert the spell error record into DB.
	 * 
	 * @param idProject
	 *            idProject
	 * @param testSetName
	 *            testSetName
	 * @param testScenarioName
	 *            testScenarioName
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param page
	 *            page
	 * @param spellErrorCount
	 *            spellErrorCount
	 * @param grammarErrorCount
	 *            grammarErrorCount
	 * @param reportTestSuiteId
	 *            reportTestSuiteId
	 * @return intport
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int insertSpellErrorsData(int idProject, String testSetName,
			String testScenarioName, String startDate, String endDate,
			String page, int spellErrorCount, int grammarErrorCount,
			int reportTestSuiteId) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int linkErrorId = 0;
		Timestamp sqlStartDate = null;
		Timestamp sqlEndDate = null;
		DateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);
		try {
			Date startDateTime = formatter.parse(startDate);
			sqlStartDate = new Timestamp(startDateTime.getTime());
			if (endDate != null && endDate.length() > 0) {
				Date endDateTime = formatter.parse(endDate);
				sqlEndDate = new Timestamp(endDateTime.getTime());
			}
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "INSERT INTO rpt_spell_errors (testSetName, testScenarioName, page, spellErrorCount, grammarErrorCount,idProject,startDate,endDate, idTestSuiteReport) VALUES (?,?,?,?,?,?,?,?,?)";
			preparedStatementInsert = conn.prepareStatement(insertSQL,
					Statement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setString(1, testSetName);
			preparedStatementInsert.setString(2, testScenarioName);
			preparedStatementInsert.setString(3, page);
			preparedStatementInsert.setInt(4, spellErrorCount);
			preparedStatementInsert.setInt(5, grammarErrorCount);
			preparedStatementInsert.setInt(6, idProject);
			preparedStatementInsert.setTimestamp(7, sqlStartDate);
			preparedStatementInsert.setTimestamp(8, sqlEndDate);
			preparedStatementInsert.setInt(9, reportTestSuiteId);
			preparedStatementInsert.executeUpdate();
			rs = preparedStatementInsert.getGeneratedKeys();
			if (rs.next()) {
				linkErrorId = rs.getInt(1);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return linkErrorId;
	}

	/**
	 * This method will insert the spell error details record into DB.
	 * 
	 * @param errorType
	 *            errorType
	 * @param errorDetails
	 *            errorDetails
	 * @param idrptSpellErrors
	 *            idrptSpellErrors
	 * @return int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int insertSpellErrorsDetails(String errorType, String errorDetails,
			int idrptSpellErrors) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		int count = 0;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			insertSQL = "INSERT INTO rpt_spellerror_details (errorType, errorDetails, idrptSpellErrors) VALUES (?,?,?)";
			preparedStatementInsert = conn.prepareStatement(insertSQL);
			preparedStatementInsert.setString(1, errorType);
			preparedStatementInsert.setString(2, errorDetails);
			preparedStatementInsert.setInt(3, idrptSpellErrors);
			count = preparedStatementInsert.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return count;
	}

	/**
	 * /** This method will set the test set information.
	 * 
	 * @param rs
	 *            resultSet
	 * @return testSet
	 * @throws AFTException
	 *             the aFT exception
	 */
	private TestSet createTestSet(ResultSet rs) throws AFTException {
		TestSet testSet = new TestSet();
		try {
			testSet.setIdTestSuite(rs.getString("idTestSuite"));
			testSet.setDescription(rs.getString("description"));
			testSet.setExecutionEngine(rs.getString("execution_engine"));
			testSet.setApplicationUrl(rs.getString("url"));
			testSet.setHostMachine(rs.getString("hostName"));
			testSet.setAppExecutionConfiguration(rs.getString("appConfig"));
			testSet.setScenarioInitializationIDs(rs
					.getString("scenarioInitializationId"));
			testSet.setScenarioCleanupIDs(rs.getString("scenarioCleanupId"));
			testSet.setTestSetCleanupIDs(rs.getString("batchCleanupId"));
			testSet.setTestSetInitializationIDs(rs
					.getString("batchInitializationId"));
			testSet.setIdProject(RuntimeProperties.getInstance().getProjectId());

			testSet.setCustomDictionaryPath(rs
					.getString(Constants.CUSTOMDICTIONARYPATH));
			testSet.setSpellCheckLanguage(rs
					.getString(Constants.SPELLCHECKLANGUAGE));
			testSet.setSpellCheckSuggestion(rs
					.getString(Constants.SPELLCHECKSUGGESTION));

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return testSet;
	}

	/**
	 * This method will get the test data column value from DB based on table
	 * name and column name.
	 * 
	 * @param tableName
	 *            tableName
	 * @param columnName
	 *            columnName
	 * @param rowNum
	 *            rowNum
	 * @return the String
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getTestDataValueFromDB(String tableName, String columnName,
			int rowNum) throws AFTException {
		LOGGER.debug("Reading funactional scenarios from DB");
		Connection conn = null;
		ResultSet rs = null;
		String columnValue = null;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();

		sqlQuery.append("SELECT ").append(columnName).append(" from ")
				.append(tableName).append(" where idsequence = ")
				.append(rowNum);
		try {
			LOGGER.info("---------Inside getTestDataValueFromDB method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				columnValue = rs.getString(columnName);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting getTestDataValueFromDB---------------------------");
		return columnValue;

	}

	/**
	 * This method will get the test data row count from DB based on table name.
	 * 
	 * @param tableName
	 *            tableName
	 * @return the String
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int getTestDataRowCount(String tableName) throws AFTException {
		LOGGER.debug("Reading funactional scenarios from DB");
		Connection conn = null;
		ResultSet rs = null;
		int columnValue = 0;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		sqlQuery.append("SELECT ").append("count(*) as count").append(" from ")
				.append(tableName);
		try {
			LOGGER.info("---------Inside getTestDataValueFromDB method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				columnValue = rs.getInt("count");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting getTestDataValueFromDB---------------------------");
		return columnValue;

	}

	/**
	 * read the object repository values from DB.
	 * 
	 * @return the attrList
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<Map<String, RepositoryObject>> loadObjectRepository()
			throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String sqlQuery = "select * from objectrepository where idproject ="
				+ RuntimeProperties.getInstance().getProjectId();
		List<Map<String, RepositoryObject>> attrList = new ArrayList<Map<String, RepositoryObject>>();
		Map<String, RepositoryObject> attrMap = new HashMap<String, RepositoryObject>();
		XMLParser xmlParser = new XMLParser();
		try {
			LOGGER.info("---------Inside loadObjRepositoryFromDB method---------------------------");
			// Reading AFT Config values from DB for configuration properties
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				try {
					// set the object repository values.
					attrMap = xmlParser.setObjectRepositoryValues(rs);
					attrList.add(attrMap);
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting loadObjRepositoryFromDB---------------------------");
		return attrList;
	}

	/**
	 * This method will load the functional scenarios from DB and put the data
	 * into map.
	 * 
	 * @param idTestSuite
	 *            the id test suite
	 * @param loadReusableTestScenarios
	 *            the reusableValue
	 * @return scenarioList
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<TestScenario> loadTestScenarios(String idTestSuite,
			String loadReusableTestScenarios) throws AFTException {
		LOGGER.debug("Reading functional scenarios from DB");
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		List<TestScenario> scenarioList = new ArrayList<TestScenario>();
		TestScenario testScenario = null;
		String sqlQuery = "SELECT DISTINCT(ts.idTestScenario),ts.testScenario,ts.description FROM testsuitescenarios tss INNER JOIN modules m ON tss.idModule=m.idModule INNER JOIN testscenario ts ON tss.idTestScenario=ts.idTestScenario INNER JOIN testsuite t ON tss.idtestsuite=t.idtestsuite WHERE m.idproject="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and tss.idtestsuite="
				+ idTestSuite
				+ " and ts.isReusable="
				+ loadReusableTestScenarios + " order by tss.idsequence";
		try {
			LOGGER.info("---------Inside loadTestScenariosFromDB method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				// create a new test scenario object.
				testScenario = createTestScenario(rs.getString("testScenario"),
						Constants.EMPTYVALUE, rs.getString("description"), "Y",
						"", rs.getString("idTestScenario"));
				// get the test steps from DB from corresponding test scenario.
				// As test case concept was not implemented in DB ,creating a
				// dummy test case with id as BusinessScenarioId appended with
				// "_1"
				TestCase testCase = loadTestSteps(
						testScenario.getIdTestScenario(),
						testScenario.getBusinessScenarioId() + "_1",
						testScenario.getBusinessScenarioDesc());
				if (testCase != null) {
					// add testScenario to testCase.
					testCase.setTestScenario(testScenario);
					// add the testCase to the testScenario
					testScenario.addTestCase(testCase);
				}

				// add the functional scenarios to list
				scenarioList.add(testScenario);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("Functional Scenarios size:" + scenarioList.size());
		return scenarioList;

	}

	/**
	 * This method will load the reusable scenarios from DB and put the data
	 * into map.
	 * 
	 * @param reusableValue
	 *            the reusableValue
	 * @return the scenarioList
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<TestScenario> loadReusableScenarios(String reusableValue)
			throws AFTException {
		LOGGER.debug("Reading functional scenarios from DB");
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		List<TestScenario> scenarioList = new ArrayList<TestScenario>();
		TestScenario testScenario = null;
		String sqlQuery = "SELECT DISTINCT(ts.idTestScenario),ts.testScenario,ts.description FROM testscenario ts INNER JOIN modules m ON ts.idModule=m.idModule WHERE m.idproject="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and ts.isReusable=" + reusableValue;
		try {
			LOGGER.info("---------Inside loadReusableScenarios method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				// create a new test scenario object.
				testScenario = createTestScenario(rs.getString("testScenario"),
						Constants.EMPTYVALUE, rs.getString("description"), "Y",
						"", rs.getString("idTestScenario"));
				// get the test steps from DB from corresponding test scenario.
				// As test case concept was not implemented in DB ,creating a
				// dummy test case with id as BusinessScenarioId appended with
				// "_1"
				TestCase testCase = loadTestSteps(
						testScenario.getIdTestScenario(),
						testScenario.getBusinessScenarioId() + "_1",
						testScenario.getBusinessScenarioDesc());
				if (testCase != null) {
					// add testScenario to testCase.
					testCase.setTestScenario(testScenario);
					// add the testCase to the testScenario
					testScenario.addTestCase(testCase);
				}

				// add the functional scenarios to list
				scenarioList.add(testScenario);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("Functional Scenarios size:" + scenarioList.size());
		return scenarioList;

	}

	/**
	 * This method will create a new TestScenario object and sets the data to
	 * this object.
	 * 
	 * @param businessScenarioId
	 *            businessScenarioId
	 * @param requirementId
	 *            the requirementId
	 * @param description
	 *            the description
	 * @param executionFlag
	 *            the executionFlag
	 * @param category
	 *            the category
	 * @param idTestScenario
	 *            the idTestScenario
	 * @return the testScenario
	 * @throws AFTException
	 *             the aFT exception
	 */
	public TestScenario createTestScenario(String businessScenarioId,
			String requirementId, String description, String executionFlag,
			String category, String idTestScenario) throws AFTException {
		TestScenario testScenario = null;
		try {
			testScenario = new TestScenario();
			// set the testSceanrio data to testScenario object
			testScenario.setBusinessScenarioId(businessScenarioId);
			testScenario.setTestScenarioRequirementId(requirementId);
			testScenario.setBusinessScenarioDesc(description);
			testScenario.setExecutionFlag(executionFlag);
			testScenario.setCategory(category);
			testScenario.setIdTestScenario(idTestScenario);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return testScenario;
	}

	/**
	 * This method will read from DB and put the data into map.
	 * 
	 * @param idTestScenario
	 *            idTestScenario
	 * @param testCaseID
	 *            testCaseID
	 * @param testCaseDesc
	 *            testCaseDesc
	 * @return the testCase
	 * @throws AFTException
	 *             the aFT exception
	 */
	public TestCase loadTestSteps(String idTestScenario, String testCaseID,
			String testCaseDesc) throws AFTException {
		LOGGER.debug("Reading test steps from DB");
		Connection conn = null;
		ResultSet rs = null;
		TestCase testCase = null;
		Statement stmt = null;
		Map<String, String> steps = new TreeMap<String, String>();
		List<Map<String, String>> stepList = new ArrayList<Map<String, String>>();
		StringBuffer sqlQuery = new StringBuffer();
		sqlQuery.append("select tstep.stepid,tstep.description,tstep.etlObjectType,c.action,o.elementName,tstep.elementName as tstepElementName,tstep.elementValue from teststep tstep INNER JOIN command c on tstep.action=c.idCommand LEFT JOIN objectrepository o on tstep.elementName=o.idObjectRepository where idTestScenario =");
		sqlQuery.append(idTestScenario);
		sqlQuery.append(" order by tstep.stepid");
		try {
			LOGGER.info("---------Inside loadTestStepsFromDB method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				// read test steps from result set
				steps = readTestStepsData(rs);
				// add the steps data map to list
				stepList.add(steps);
			}
			if (stepList.size() > 0) {
				// creates a new test case dummy object and sets the pre steps
				// and post steps and srteps to the test case object.
				testCase = getTestCase(stepList, testCaseID, testCaseDesc);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting loadTestStepsFromDB---------------------------");
		return testCase;

	}

	/**
	 * This method will read the test steps from DB and put the data into map.
	 * 
	 * @param rs
	 *            ResultSet
	 * @return the stepList
	 * @throws AFTException
	 *             the aFT exception
	 */
	private Map<String, String> readTestStepsData(ResultSet rs)
			throws AFTException {
		Map<String, String> steps = new TreeMap<String, String>();
		try {
			String elementName = rs.getString("elementName");
			if (elementName == null) {
				elementName = rs.getString("tstepElementName");
			}
			steps.put("Step Id", rs.getString("stepid"));
			steps.put("Action", rs.getString("action"));
			steps.put("Element Name", elementName);
			steps.put("Element Value", rs.getString("elementValue"));
			steps.put("etlObjectType", rs.getString("etlObjectType"));
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return steps;
	}

	/**
	 * This method will create a TestCase object.
	 * 
	 * @param testCaseID
	 *            testCaseID
	 * @param testCaseDesc
	 *            testCaseDesc
	 * @param preSteps
	 *            preSteps
	 * @param steps
	 *            steps
	 * @param postSteps
	 *            postSteps
	 * @return the TestCase
	 * @throws AFTException
	 *             the aFT exception
	 */
	public TestCase createTestCase(String testCaseID, String testCaseDesc,
			Map<String, TestStep> preSteps, Map<String, TestStep> steps,
			Map<String, TestStep> postSteps) throws AFTException {
		TestCase testCase = null;
		try {
			testCase = new TestCase();
			testCase.setTestCaseId(testCaseID);
			testCase.setTestCaseDesc(testCaseDesc);
			// set the pre steps, steps and post steps to a TestCase object
			testCase.setPreSteps(preSteps);
			testCase.setSteps(steps);
			testCase.setPostSteps(postSteps);

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return testCase;
	}

	/**
	 * This method will create a new test case object and sets the pre steps,
	 * steps and post steps to a TestCase object.
	 * 
	 * @param stepList
	 *            stepList
	 * @param testCaseID
	 *            testCaseID
	 * @param testCaseDesc
	 *            testCaseDesc
	 * @return the TestCase
	 * @throws AFTException
	 *             the aFT exception
	 */
	private TestCase getTestCase(List<Map<String, String>> stepList,
			String testCaseID, String testCaseDesc) throws AFTException {

		Map<String, TestStep> preSteps = new LinkedHashMap<String, TestStep>();
		Map<String, TestStep> steps = new LinkedHashMap<String, TestStep>();
		Map<String, TestStep> postSteps = new LinkedHashMap<String, TestStep>();

		TestStep testStep = null;
		TestCase testCase = null;
		try {
			for (Map<String, String> testSteps : stepList) {
				testStep = createTestStep(testSteps.get("Action"),
						testSteps.get("Element Name"),
						testSteps.get("Element Value"), Constants.STEPPREFIX,
						testSteps.get("etlObjectType"));
				testStep.setStepId(testSteps.get("Step Id"));
				// set the step id and test step data to map.
				steps.put(testSteps.get("Step Id"), testStep);
			}
			// create dummy TestCase object
			testCase = createTestCase(testCaseID, testCaseDesc, preSteps,
					steps, postSteps);

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return testCase;
	}

	/**
	 * This method will create a new test step object.
	 * 
	 * @param action
	 *            action
	 * @param elementName
	 *            elementName
	 * @param elementValue
	 *            elementValue
	 * @param stepType
	 *            stepType
	 * @param etlObjectType
	 *            the etl object type
	 * @return the testStep
	 */
	private TestStep createTestStep(String action, String elementName,
			String elementValue, String stepType, String etlObjectType) {

		TestStep testStep = new TestStep();
		testStep.setAction(action);
		testStep.setElementName(elementName);
		testStep.setElementValue(elementValue);
		testStep.setStepType(stepType);
		testStep.setEtlObjectType(etlObjectType);
		// Below check has been made to get the Delta flag from Database for ETL
		// engine
		if (!Helper.getInstance().isFileSystemRequest()) {
			// testStep.setEtlObjectType(etlObjectType);
		}
		return testStep;
	}

	/**
	 * This method will set the individual steps data (action,element name,
	 * element value) to TestStep object and sets these objects to individual
	 * map.
	 * 
	 * @param preStepActionList
	 *            preStepActionList
	 * @param preElementNameList
	 *            preElementNameList
	 * @param preElementValueList
	 *            preElementValueList
	 * @param stepType
	 *            stepType
	 * @param scenarioId
	 *            scenarioId
	 * @return stepData
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Map<String, TestStep> getTestSteps(List<String> preStepActionList,
			List<String> preElementNameList, List<String> preElementValueList,
			String stepType, String scenarioId) throws AFTException {
		Map<String, TestStep> stepData = new TreeMap<String, TestStep>();
		TestStep testSteps = null;
		try {
			if (preStepActionList != null && preElementNameList != null
					&& preElementValueList != null) {
				if (preStepActionList.size() != preElementNameList.size()
						|| preStepActionList.size() != preElementValueList
								.size()) {
					String errMsg = "Count of actions, element names, element values for test scenario ["
							+ scenarioId
							+ "] and step type  ["
							+ stepType
							+ "] does not match";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
				for (int iStepCnt = 0; iStepCnt <= preStepActionList.size() - 1; iStepCnt++) {
					testSteps = createTestStep(preStepActionList.get(iStepCnt),
							preElementNameList.get(iStepCnt),
							preElementValueList.get(iStepCnt), stepType, null);
					stepData.put(Integer.valueOf(iStepCnt).toString(),
							testSteps);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return stepData;
	}

	/**
	 * Load config properties.
	 * 
	 * @param teConfigMap
	 *            the te config map
	 * @param configtype
	 *            the configtype
	 * @return the map
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Map<String, String> loadConfigProperties(
			Map<String, String> teConfigMap, int configtype)
			throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String sqlQuery = "select propertyName,propertyValue from configchild where idproject ="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and configtype=" + configtype;
		try {
			// Reading AFT Config values from DB for configuration pr4operties
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				try {
					teConfigMap.put(rs.getString("propertyName"),
							rs.getString("propertyValue"));
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
				}
			}
			System.out.println("");

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		return teConfigMap;
	}

	/**
	 * Method to load the remotag Config properties.
	 * 
	 * @param teConfigMap
	 *            the teConfigMap
	 * @param configtype
	 *            the configtype
	 * @return teConfigMap
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public Map<String, String> loadRemoTagConfigProperties(
			Map<String, String> teConfigMap, int configtype)
			throws AFTException {
		Connection conn = null;

		ResultSet rs = null;
		ResultSet rs_child = null;
		ResultSet rs_master = null;
		Statement stmt = null;
		PreparedStatement preparedStatement = null;
		String configChildQuery = "select propertyName,propertyValue from configchild where idproject ="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and configtype=" + configtype;
		String configMasterQuery = "select property,defaultValue f"
				+ "rom configmaster";
		String updateQuery = "update configchild set propertyValue = ? where propertyName = ? and idproject = ? and configtype=?";
		String masterConfigQuery = "insert into configmaster (property,displayName,propertyType,"
				+ "propertySize,defaultValue,configType,displayOrder,isDeleted,isUIProperty) values (?,?,?,?,?,?,?,?,?)";
		String childConfigQuery = "insert into configchild (propertyName,propertyValue,configType,"
				+ "idConfigMaster,isDeleted,isUIProperty,idproject) values (?,?,?,?,?,?,?)";
		try {
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			switch (configtype) {
			case 1:
				if (System.getProperty("os.name").contains("Mac")) {
					
					rs_master = stmt.executeQuery(configMasterQuery);
					
					Map<String, String> frameworkConfigChildMap = new HashMap<String, String>();
					Map<String, String> frameworkConfigMasterMap = new HashMap<String, String>();
					while (rs_master.next()) {
						if (rs_master.getString("property").equalsIgnoreCase(
								"IOSExecutionPath")) {
							frameworkConfigMasterMap.put(
									rs_master.getString("property"),
									rs_master.getString("defaultValue"));
						}
					}
					rs_child = stmt.executeQuery(configChildQuery);

					while (rs_child.next()) {
						if (rs_child.getString("propertyName")
								.equalsIgnoreCase("IOSExecutionPath")) {
							frameworkConfigChildMap.put(
									rs_child.getString("propertyName"),
									rs_child.getString("propertyValue"));
						}
					}
					int iosExePathAutoGeneratedID = 0;

					// If the property does not exist in master config table,
					// add the property
					if (!frameworkConfigMasterMap
							.containsKey("IOSExecutionPath")) {
						// Insert into Master Config table

						preparedStatement = conn.prepareStatement(
								masterConfigQuery,
								Statement.RETURN_GENERATED_KEYS);
						preparedStatement.setString(1, "IOSExecutionPath");
						preparedStatement.setString(2,
								"Display MAC execution Path");
						preparedStatement.setString(3, "textfield");
						preparedStatement.setString(4, "30");
						preparedStatement.setString(5, XAFTConfigProperties
								.getInstance().getMacExecutionpath());
						preparedStatement.setString(6,
								String.valueOf(configtype));
						preparedStatement.setString(7, "0");
						preparedStatement.setString(8, "0");
						preparedStatement.setString(9, "0");
						preparedStatement.executeUpdate();
						ResultSet tableKeys = preparedStatement
								.getGeneratedKeys();

						// Get the Auto Incremented ID
						tableKeys.next();
						iosExePathAutoGeneratedID = tableKeys.getInt(1);
					} else {
						// if the property exists in Master Config table get the
						// AutoIncremented id
						String selectQuery = "select idConfigMaster from configmaster where property = 'IOSExecutionPath'";
						rs = stmt.executeQuery(selectQuery);
						while (rs.next()) {
							iosExePathAutoGeneratedID = Integer.parseInt(rs
									.getString("idConfigMaster"));
						}
					}
					// Check if the Config Child contains property
					if (frameworkConfigChildMap.containsKey("IOSExecutionPath")) {
						LOGGER.info("ConfigProperty exists in database. Checking for property value..");
						// Check if the property needs to be updated
						if (frameworkConfigChildMap.get("IOSExecutionPath")
								.equalsIgnoreCase(
										XAFTConfigProperties.getInstance()
												.getMacExecutionpath())) {
							LOGGER.debug("Values in Database matches with that"
									+ " of the Remotag config property. Not updating existing property value.");
						} else {
							// Update if the the property in remotag and the
							// property in DB does not match
							LOGGER.debug("Updating the existing config property Application Path with value ["
									+ XAFTConfigProperties.getInstance()
											.getMacExecutionpath() + "]");
							preparedStatement = conn
									.prepareStatement(updateQuery);
							preparedStatement.setString(1, XAFTConfigProperties
									.getInstance().getMacExecutionpath());
							preparedStatement.setString(2, "IOSExecutionPath");
							preparedStatement.setString(3, RuntimeProperties
									.getInstance().getProjectId());
							preparedStatement.setString(4,
									String.valueOf(configtype));
							preparedStatement.executeUpdate();
						}
					} else {
						// If the property name does not exist in child config
						// table.
						// Insert the property name and value in child config
						// table
						preparedStatement = conn
								.prepareStatement(childConfigQuery);
						preparedStatement.setString(1, "IOSExecutionPath");
						preparedStatement.setString(2, XAFTConfigProperties
								.getInstance().getMacExecutionpath());
						preparedStatement.setString(3,
								String.valueOf(configtype));
						preparedStatement.setString(4,
								String.valueOf(iosExePathAutoGeneratedID));
						preparedStatement.setString(5, "0");
						preparedStatement.setString(6, "0");
						preparedStatement.setString(7, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.executeUpdate();
					}
				}

				break;
			case 2:
				Map<String, String> configValueMap = new HashMap<String, String>();
				Map<String, String> configValueMasterMap = new HashMap<String, String>();
				rs_master = stmt.executeQuery(configMasterQuery);

				while (rs_master.next()) {
					
					if (rs_master.getString("property").equalsIgnoreCase(
							"IEDriverExecutablePath")) {
						configValueMasterMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("ChromeDriverExecutablePath")) {
						configValueMasterMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("FirefoxCustomProfilePath")) {
						configValueMasterMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					}
				}

				rs_child = stmt.executeQuery(configChildQuery);
				while (rs_child.next()) {
					if (rs_child.getString("propertyName").equalsIgnoreCase(
							"IEDriverExecutablePath")) {
						configValueMap.put(rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("ChromeDriverExecutablePath")) {
						configValueMap.put(rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("FirefoxCustomProfilePath")) {
						configValueMap.put(rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					}
				}

				// IE driver path property Check
				int ieAutoGeneratedID = 0;
				// Check if the property exists in master table.If not, add
				// property to master table and then add to Child
				if (!configValueMasterMap.containsKey("IEDriverExecutablePath")) {
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "IEDriverExecutablePath");
					preparedStatement.setString(2, "Display IE Driver Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5,
							XAFTWebdriverConfigProperties.getInstance()
									.getIeDriverPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					ieAutoGeneratedID = tableKeys.getInt(1);
				} else {
					// If the prpperty already exists, get the idConfigMaster id
					String selectQuery = "select idConfigMaster from configmaster where property = 'IEDriverExecutablepath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						ieAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				// Check if the property exists in Config Child table
				if (configValueMap.containsKey("IEDriverExecutablePath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (configValueMap.get("IEDriverExecutablePath")
							.equalsIgnoreCase(
									XAFTWebdriverConfigProperties.getInstance()
											.getIeDriverPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						// Update the property value of the value does not match
						// with Remotag Config value
						LOGGER.debug("Updating the existing config property IEDriverExecutablePath with value ["
								+ XAFTWebdriverConfigProperties.getInstance()
										.getIeDriverPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTWebdriverConfigProperties.getInstance()
										.getIeDriverPath());
						preparedStatement
								.setString(2, "IEDriverExecutablePath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// if the property does not exists,Insert new property into
					// Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "IEDriverExecutablePath");
					preparedStatement.setString(2,
							XAFTWebdriverConfigProperties.getInstance()
									.getIeDriverPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(String.valueOf(ieAutoGeneratedID)));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();

				}
				// Chrome Driver Path Property Check
				int chromeautoGeneratedID = 0;
				if (!configValueMasterMap
						.containsKey("ChromeDriverExecutablePath")) {
					// Insert into Master Config table

					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement
							.setString(1, "ChromeDriverExecutablePath");
					preparedStatement
							.setString(2, "Display Chrome Driver Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5,
							XAFTWebdriverConfigProperties.getInstance()
									.getChromeDriverPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					chromeautoGeneratedID = tableKeys.getInt(1);

				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'ChromeDriverExecutablePath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						chromeautoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (configValueMap.containsKey("ChromeDriverExecutablePath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (configValueMap.get("ChromeDriverExecutablePath")
							.equalsIgnoreCase(
									XAFTWebdriverConfigProperties.getInstance()
											.getChromeDriverPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property ChromeDriverExecutablePath with value ["
								+ XAFTWebdriverConfigProperties.getInstance()
										.getChromeDriverPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTWebdriverConfigProperties.getInstance()
										.getChromeDriverPath());
						preparedStatement.setString(2,
								"ChromeDriverExecutablePath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {

					// Insert into Child config table

					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement
							.setString(1, "ChromeDriverExecutablePath");
					preparedStatement.setString(2,
							XAFTWebdriverConfigProperties.getInstance()
									.getChromeDriverPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4, String.valueOf(String
							.valueOf(chromeautoGeneratedID)));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}

				// Firefox Profile Path Check
				int ffautoGeneratedID = 0;
				if (!configValueMasterMap
						.containsKey("FirefoxCustomProfilePath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "FirefoxCustomProfilePath");
					preparedStatement.setString(2,
							"Display Firefox Profile Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5,
							XAFTWebdriverConfigProperties.getInstance()
									.getFirefoxProfilePath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");

					// preparedStatement.setString(3,
					// RuntimeProperties.getInstance()
					// .getProjectId());
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					ffautoGeneratedID = tableKeys.getInt(1);

				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'FirefoxCustomProfilePath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						ffautoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (configValueMap.containsKey("FirefoxCustomProfilePath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (configValueMap.get("FirefoxCustomProfilePath")
							.equalsIgnoreCase(
									XAFTWebdriverConfigProperties.getInstance()
											.getFirefoxProfilePath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property FirefoxCustomProfilePath with value ["
								+ XAFTWebdriverConfigProperties.getInstance()
										.getFirefoxProfilePath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTWebdriverConfigProperties.getInstance()
										.getFirefoxProfilePath());
						preparedStatement.setString(2,
								"FirefoxCustomProfilePath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "FirefoxCustomProfilePath");
					preparedStatement.setString(2,
							XAFTWebdriverConfigProperties.getInstance()
									.getFirefoxProfilePath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(ffautoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}
				break;
			case 5:
				// Reading AFT Config values from DB for configuration
				// pr4operties
				// get the Db connection
				conn = DriverManager.getConnection(RuntimeProperties
						.getInstance().getDbUrl(), RuntimeProperties
						.getInstance().getUserName(), RuntimeProperties
						.getInstance().getPassword());

				// call the statement
				stmt = conn.createStatement();
				Map<String, String> robotiumChildConfigMap = new HashMap<String, String>();
				Map<String, String> robotiumMasterConfigMap = new HashMap<String, String>();
				rs_master = stmt.executeQuery(configMasterQuery);
				while (rs_master.next()) {
					if (rs_master.getString("property").equalsIgnoreCase(
							"AndroidSDKPath")) {
						robotiumMasterConfigMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("RobotiumTestRunnerHomePath")) {
						robotiumMasterConfigMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("AUTPath")) {
						robotiumMasterConfigMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("EmulatorPath")) {
						robotiumMasterConfigMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					} else if (rs_master.getString("property")
							.equalsIgnoreCase("ANTHomePath")) {
						robotiumMasterConfigMap.put(
								rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					}
				}

				
				rs_child = stmt.executeQuery(configChildQuery);
				while (rs_child.next()) {
					if (rs_child.getString("propertyName").equalsIgnoreCase(
							"AndroidSDKPath")) {
						robotiumChildConfigMap.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("RobotiumTestRunnerHomePath")) {
						robotiumChildConfigMap.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("AUTPath")) {
						robotiumChildConfigMap.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("EmulatorPath")) {
						robotiumChildConfigMap.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					} else if (rs_child.getString("propertyName")
							.equalsIgnoreCase("ANTHomePath")) {
						robotiumChildConfigMap.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					}

				}
				// Check for Android ADK Path Config Property
				int sdkPathAutoGeneratedID = 0;
				if (!robotiumMasterConfigMap.containsKey("AndroidSDKPath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "AndroidSDKPath");
					preparedStatement.setString(2, "Display Android SDK Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTRobotiumConfigProperties
							.getInstance().getAndroidSDKHomePath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					sdkPathAutoGeneratedID = tableKeys.getInt(1);
				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'AndroidSDKPath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						sdkPathAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}

				}
				if (robotiumChildConfigMap.containsKey("AndroidSDKPath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (robotiumChildConfigMap.get("AndroidSDKPath")
							.equalsIgnoreCase(
									XAFTRobotiumConfigProperties.getInstance()
											.getAndroidSDKHomePath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property AndroidSDKPath with value ["
								+ XAFTRobotiumConfigProperties.getInstance()
										.getAndroidSDKHomePath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTRobotiumConfigProperties.getInstance()
										.getAndroidSDKHomePath());
						preparedStatement.setString(2, "AndroidSDKPath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table

					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "AndroidSDKPath");
					preparedStatement.setString(2, XAFTRobotiumConfigProperties
							.getInstance().getAndroidSDKHomePath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4, String.valueOf(String
							.valueOf(sdkPathAutoGeneratedID)));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}

				// Check for Robotium Test Runner Home Path Property
				int testRunnerPathAutoGeneratedID = 0;
				if (!robotiumMasterConfigMap
						.containsKey("RobotiumTestRunnerHomePath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement
							.setString(1, "RobotiumTestRunnerHomePath");
					preparedStatement.setString(2,
							"Display Test Runner SDK Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTRobotiumConfigProperties
							.getInstance().getTestRunnerPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					testRunnerPathAutoGeneratedID = tableKeys.getInt(1);

				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'RobotiumTestRunnerHomePath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						testRunnerPathAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (robotiumChildConfigMap
						.containsKey("RobotiumTestRunnerHomePath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (robotiumChildConfigMap
							.get("RobotiumTestRunnerHomePath")
							.equalsIgnoreCase(
									XAFTRobotiumConfigProperties.getInstance()
											.getTestRunnerPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property RobotiumTestRunnerHomePath with value ["
								+ XAFTRobotiumConfigProperties.getInstance()
										.getTestRunnerPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTRobotiumConfigProperties.getInstance()
										.getTestRunnerPath());
						preparedStatement.setString(2,
								"RobotiumTestRunnerHomePath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement
							.setString(1, "RobotiumTestRunnerHomePath");
					preparedStatement.setString(2, XAFTRobotiumConfigProperties
							.getInstance().getTestRunnerPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(testRunnerPathAutoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}

				// Check for Android AUT Path
				int autPathAutoGeneratedID = 0;
				if (!robotiumMasterConfigMap.containsKey("AUTPath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "AUTPath");
					preparedStatement.setString(2, "Display AUT Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTRobotiumConfigProperties
							.getInstance().getAndroidAUTPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					autPathAutoGeneratedID = tableKeys.getInt(1);

				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'AUTPath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						autPathAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (robotiumChildConfigMap.containsKey("AUTPath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (robotiumChildConfigMap.get("AUTPath").equalsIgnoreCase(
							XAFTRobotiumConfigProperties.getInstance()
									.getAndroidAUTPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property AUTPath with value ["
								+ XAFTRobotiumConfigProperties.getInstance()
										.getAndroidAUTPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTRobotiumConfigProperties.getInstance()
										.getAndroidAUTPath());
						preparedStatement.setString(2, "AUTPath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "AUTPath");
					preparedStatement.setString(2, XAFTRobotiumConfigProperties
							.getInstance().getAndroidAUTPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(autPathAutoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}

				// Check for Android Emulator Path
				int emulatorPathAutoGeneratedID = 0;
				if (!robotiumMasterConfigMap.containsKey("EmulatorPath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "EmulatorPath");
					preparedStatement.setString(2, "Display Emulator Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTRobotiumConfigProperties
							.getInstance().getEmulatorPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					emulatorPathAutoGeneratedID = tableKeys.getInt(1);
				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'EmulatorPath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						emulatorPathAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (robotiumChildConfigMap.containsKey("EmulatorPath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (robotiumChildConfigMap.get("EmulatorPath")
							.equalsIgnoreCase(
									XAFTRobotiumConfigProperties.getInstance()
											.getEmulatorPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property Emulator Path with value ["
								+ XAFTRobotiumConfigProperties.getInstance()
										.getEmulatorPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTRobotiumConfigProperties.getInstance()
										.getEmulatorPath());
						preparedStatement.setString(2, "EmulatorPath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "EmulatorPath");
					preparedStatement.setString(2, XAFTRobotiumConfigProperties
							.getInstance().getEmulatorPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(emulatorPathAutoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();

				}

				// Check for ANT Home path property
				int antPathAutoGeneratedID = 0;
				if (!robotiumMasterConfigMap.containsKey("ANTHomePath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "ANTHomePath");
					preparedStatement.setString(2, "Display Ant Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTRobotiumConfigProperties
							.getInstance().getAntHomePath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					antPathAutoGeneratedID = tableKeys.getInt(1);
				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'ANTHomePath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						antPathAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (robotiumChildConfigMap.containsKey("ANTHomePath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (robotiumChildConfigMap.get("ANTHomePath")
							.equalsIgnoreCase(
									XAFTRobotiumConfigProperties.getInstance()
											.getAntHomePath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property Emulator Path with value ["
								+ XAFTRobotiumConfigProperties.getInstance()
										.getAntHomePath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTRobotiumConfigProperties.getInstance()
										.getAntHomePath());
						preparedStatement.setString(2, "ANTHomePath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "ANTHomePath");
					preparedStatement.setString(2, XAFTRobotiumConfigProperties
							.getInstance().getAntHomePath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(antPathAutoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}
				break;
			case 7:
				conn = DriverManager.getConnection(RuntimeProperties
						.getInstance().getDbUrl(), RuntimeProperties
						.getInstance().getUserName(), RuntimeProperties
						.getInstance().getPassword());

				// call the statement
				stmt = conn.createStatement();
				Map<String, String> appiumChildConfig = new HashMap<String, String>();
				Map<String, String> appiumMasterConfig = new HashMap<String, String>();
				rs_master = stmt.executeQuery(configMasterQuery);
				
				while (rs_master.next()) {
					if (rs_master.getString("property").equalsIgnoreCase(
							"applicationPath")) {
						appiumMasterConfig.put(rs_master.getString("property"),
								rs_master.getString("defaultValue"));
					}
				}
				
				rs_child = stmt.executeQuery(configChildQuery);
				while (rs_child.next()) {
					if (rs_child.getString("propertyName").equalsIgnoreCase(
							"applicationPath")) {
						appiumChildConfig.put(
								rs_child.getString("propertyName"),
								rs_child.getString("propertyValue"));
					}
				}

				// Check for IOS AUT Path
				int iosAUTAutoGeneratedID = 0;
				if (!appiumMasterConfig.containsKey("applicationPath")) {
					// Insert into Master Config table
					preparedStatement = conn.prepareStatement(
							masterConfigQuery, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, "applicationPath");
					preparedStatement.setString(2, "Display Application Path");
					preparedStatement.setString(3, "textfield");
					preparedStatement.setString(4, "30");
					preparedStatement.setString(5, XAFTAppiumConfigProperties
							.getInstance().getIosAUTPath());
					preparedStatement.setString(6, String.valueOf(configtype));
					preparedStatement.setString(7, "0");
					preparedStatement.setString(8, "0");
					preparedStatement.setString(9, "0");
					preparedStatement.executeUpdate();
					ResultSet tableKeys = preparedStatement.getGeneratedKeys();

					// Get the Auto Incremented ID
					tableKeys.next();
					iosAUTAutoGeneratedID = tableKeys.getInt(1);
				} else {
					String selectQuery = "select idConfigMaster from configmaster where property = 'applicationPath'";
					rs = stmt.executeQuery(selectQuery);
					while (rs.next()) {
						iosAUTAutoGeneratedID = Integer.parseInt(rs
								.getString("idConfigMaster"));
					}
				}
				if (appiumChildConfig.containsKey("applicationPath")) {
					LOGGER.info("ConfigProperty exists in database. Checking for property value..");
					if (appiumChildConfig.get("applicationPath")
							.equalsIgnoreCase(
									XAFTAppiumConfigProperties.getInstance()
											.getIosAUTPath())) {
						LOGGER.debug("Values in Database matches with that"
								+ " of the Remotag config property. Not updating existing property value.");
					} else {
						LOGGER.debug("Updating the existing config property Application Path with value ["
								+ XAFTAppiumConfigProperties.getInstance()
										.getIosAUTPath() + "]");
						preparedStatement = conn.prepareStatement(updateQuery);
						preparedStatement.setString(1,
								XAFTAppiumConfigProperties.getInstance()
										.getIosAUTPath());
						preparedStatement.setString(2, "applicationPath");
						preparedStatement.setString(3, RuntimeProperties
								.getInstance().getProjectId());
						preparedStatement.setString(4,
								String.valueOf(configtype));
						preparedStatement.executeUpdate();
					}
				} else {
					// Insert into Child config table
					preparedStatement = conn.prepareStatement(childConfigQuery);
					preparedStatement.setString(1, "applicationPath");
					preparedStatement.setString(2, XAFTAppiumConfigProperties
							.getInstance().getIosAUTPath());
					preparedStatement.setString(3, String.valueOf(configtype));
					preparedStatement.setString(4,
							String.valueOf(iosAUTAutoGeneratedID));
					preparedStatement.setString(5, "0");
					preparedStatement.setString(6, "0");
					preparedStatement.setString(7, RuntimeProperties
							.getInstance().getProjectId());
					preparedStatement.executeUpdate();
				}
				break;

			default:
				break;
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs_child != null) {
					rs_child.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs_child = null;
			stmt = null;
			conn = null;
		}
		return teConfigMap;
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param teConfigMap
	 *            teConfigMap
	 * @param appExecConfig
	 *            appExecConfig
	 * @return teConfigMap
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public Map<String, String> loadAppConfigProperties(
			Map<String, String> teConfigMap, String appExecConfig)
			throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		String appConfig = appExecConfig;
		if (StringUtils.isEmpty(appConfig)) {
			appConfig = Constants.COMMONCONFIG;
		}
		sqlQuery.append("select * from appconfig_");
		sqlQuery.append(RuntimeProperties.getInstance().getProjectId());
		sqlQuery.append(" where AppConfig = ");
		sqlQuery.append("'");
		sqlQuery.append(appConfig);
		sqlQuery.append("'");
		try {
			// Reading AFT Config values from DB for configuration pr4operties
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int numberOfColumns = rsMetaData.getColumnCount();
				List<String> columnNames = new ArrayList<String>();
				// get the column names; column indexes start from 1
				for (int i = 1; i < numberOfColumns + 1; i++) {
					columnNames.add(rsMetaData.getColumnName(i));
				}
				for (String columnName : columnNames) {
					teConfigMap.put(columnName, rs.getString(columnName));
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		return teConfigMap;
	}

	/**
	 * This method will get the test suite record from DB based on test suite id
	 * and project id.
	 * 
	 * @param idTestSuite
	 *            the id test suite
	 * @return the TestSuite
	 * @throws AFTException
	 *             the aFT exception
	 */
	public TestSuite getTestSuite(String idTestSuite) throws AFTException {
		LOGGER.debug("Reading test suite from DB");
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		TestSuite testSuite = null;

		String sqlQuery = "select * from testsuite where idproject ="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and idTestSuite =" + idTestSuite;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				testSuite = new TestSuite();
				testSuite.setIdTestSuite(rs.getString("idTestSuite"));
				testSuite.setTestSuiteName(rs.getString("testSuite"));
				testSuite.setDescription(rs.getString("description"));
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		return testSuite;

	}

	/**
	 * This method will insert the test suite record into DB.
	 * 
	 * @param testSuite
	 *            testSuite
	 * @param testStartTime
	 *            testStartTime
	 * @return the TestSuite
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int insertReportTestSuiteData(TestSuite testSuite, Date testStartTime)
			throws AFTException {
		LOGGER.debug("Inserting test suite to DB");
		Connection dbConnection = null;
		int idTestSuite = 0;
		Timestamp sqlStartDate = null;
		try {
			if (testStartTime != null) {
				sqlStartDate = new Timestamp(testStartTime.getTime());
			}
			// get the Db connection
			dbConnection = DriverManager.getConnection(RuntimeProperties
					.getInstance().getDbUrl(), RuntimeProperties.getInstance()
					.getUserName(), RuntimeProperties.getInstance()
					.getPassword());
			dbConnection.setAutoCommit(false);
			// insert test suite data
			idTestSuite = insertTestSuiteExecutionReport(dbConnection,
					testSuite, sqlStartDate);
			dbConnection.commit();
			LOGGER.info("Transaction commit...");
		} catch (Exception e) {
			if (dbConnection != null) {
				try {
					dbConnection.rollback();
					LOGGER.info("Connection rollback...");
				} catch (SQLException e1) {
					LOGGER.error("Exception::", e1);
				}
			}
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (dbConnection != null && !dbConnection.isClosed()) {
					dbConnection.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			dbConnection = null;
		}
		return idTestSuite;
	}

	/**
	 * This method will insert the test suite record into DB.
	 * 
	 * @param dbConnection
	 *            dbConnection
	 * @param testSuite
	 *            testSuite
	 * @param sqlStartDate
	 *            sqlStartDate
	 * @return the TestSuite id
	 * @throws AFTException
	 *             the aFT exception
	 */
	private int insertTestSuiteExecutionReport(Connection dbConnection,
			TestSuite testSuite, Timestamp sqlStartDate) throws AFTException {
		InetAddress addr = null;
		PreparedStatement preparedStatementInsert = null;
		ResultSet rs = null;
		int idTestSuite = 0;
		int standardReportingValue = 0;
		try {
			addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			String os = System.getProperty("os.name");
			boolean disableStandardReporting = ConfigProperties.getInstance()
					.isStandardReportingDisabled();
			if (disableStandardReporting) {
				standardReportingValue = 1;
			}
			String insertSQL = "INSERT INTO rpt_testsuite (testSuiteName, description, browser, url, operatingSystem, hostName, executionEngine, startDate, idProject,disableStandardReporting) VALUES (?,?,?,?,?,?,?,?,?,?)";
			preparedStatementInsert = dbConnection.prepareStatement(insertSQL,
					Statement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setString(1, testSuite.getTestSuiteName());
			preparedStatementInsert.setString(2, testSuite.getDescription());
			preparedStatementInsert.setString(3, testSuite.getBrowserName());
			preparedStatementInsert.setString(4, testSuite.geturl());
			preparedStatementInsert.setString(5, os);
			preparedStatementInsert.setString(6, hostname);
			preparedStatementInsert
					.setString(7, testSuite.getExecutionEngine());
			preparedStatementInsert.setTimestamp(8, sqlStartDate);
			preparedStatementInsert.setInt(9, Integer
					.parseInt(RuntimeProperties.getInstance().getProjectId()));
			preparedStatementInsert.setInt(10, standardReportingValue);
			preparedStatementInsert.executeUpdate();
			rs = preparedStatementInsert.getGeneratedKeys();
			if (rs.next()) {
				idTestSuite = rs.getInt(1);
			}
		} catch (Exception e) {
			if (dbConnection != null) {
				try {
					dbConnection.rollback();
					LOGGER.info("Connection rollback...");
				} catch (SQLException e1) {
					LOGGER.error("Exception::", e1);
				}
			}
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			preparedStatementInsert = null;
		}

		return idTestSuite;
	}

	/**
	 * This method will insert the test scenario and test step record into DB.
	 * 
	 * @param suiteResult
	 *            suiteResult
	 * @param endTime
	 *            endTime
	 * @param isEndOfTestSuite
	 *            isEndOfTestSuite
	 * @throws AFTException
	 *             the aFT exception
	 * @throws SQLException
	 *             the sQL exception
	 */
	public void insertTestExecutionData(List<TestSuite> suiteResult,
			String endTime, boolean isEndOfTestSuite) throws AFTException,
			SQLException {
		Connection dbConnection = null;
		Timestamp sqlEndDate = null;
		DateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);
		try {
			// get the Db connection
			dbConnection = DriverManager.getConnection(RuntimeProperties
					.getInstance().getDbUrl(), RuntimeProperties.getInstance()
					.getUserName(), RuntimeProperties.getInstance()
					.getPassword());
			dbConnection.setAutoCommit(false);
			if (endTime != null && !endTime.equals("")) {
				Date endDate = formatter.parse(endTime);
				sqlEndDate = new Timestamp(endDate.getTime());
			}
			// insert data
			insertTestSuiteExecutionData(suiteResult, dbConnection, sqlEndDate,
					isEndOfTestSuite);

		} catch (Exception e) {
			if (dbConnection != null) {
				dbConnection.rollback();
				LOGGER.info("Connection rollback...");
			}
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (dbConnection != null && !dbConnection.isClosed()) {
					dbConnection.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			dbConnection = null;
		}
	}

	/**
	 * This method will insert the test suite report record into DB.
	 * 
	 * @param suiteResult
	 *            suiteResult
	 * @param dbConnection
	 *            dbConnection
	 * @param sqlEndDate
	 *            sqlEndDate
	 * @param isEndOfTestSuite
	 *            isEndOfTestSuite
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void insertTestSuiteExecutionData(List<TestSuite> suiteResult,
			Connection dbConnection, Timestamp sqlEndDate,
			boolean isEndOfTestSuite) throws AFTException {
		TestScenario scenario = null;
		try {
			for (TestSuite suite : suiteResult) {
				if (isEndOfTestSuite && !suite.isSuiteCommitted()) {
					updateTestSuiteEndDate(dbConnection, suite, sqlEndDate);
				} else {
					updateTestSuiteExecutionData(dbConnection, suite);
				}
				List<TestScenario> testScenarios = suite.getTestScenariosArr();
				scenario = insertTestScenarioExecutionData(dbConnection, suite,
						testScenarios);

				// Committing the transaction
				dbConnection.commit();
				if (scenario != null) {
					scenario.setCommitted(true);
				}
				LOGGER.info("Connection commit...");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * This method will update the test suite report record into DB.
	 * 
	 * @param dbConnection
	 *            dbConnection
	 * @param suite
	 *            suite
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void updateTestSuiteExecutionData(Connection dbConnection,
			TestSuite suite) throws AFTException {
		PreparedStatement pstmt = null;
		String insertSQL = null;

		try {
			insertSQL = "UPDATE rpt_testsuite SET total =? " + "," + "pass =? "
					+ "," + "fail =? " + "WHERE idTestSuiteReport = ?";
			pstmt = dbConnection.prepareStatement(insertSQL);
			pstmt.setInt(1, suite.getTotalCount());
			pstmt.setInt(2, suite.getPassCount());
			pstmt.setInt(3, suite.getFailCount());
			pstmt.setInt(4, suite.getIdReportTestSuite());
			pstmt.executeUpdate();
		} catch (SQLException sql) {
			LOGGER.error("Exception::", sql);
			throw new AFTException(sql);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
	}

	/**
	 * This method will update the test suite end date.
	 * 
	 * @param dbConnection
	 *            dbConnection
	 * @param suite
	 *            suite
	 * @param sqlEndDate
	 *            sqlEndDate
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void updateTestSuiteEndDate(Connection dbConnection,
			TestSuite suite, Timestamp sqlEndDate) throws AFTException {
		PreparedStatement pstmt = null;
		String insertSQL = null;
		try {
			insertSQL = "UPDATE rpt_testsuite SET endDate =? "
					+ "WHERE idTestSuiteReport = ?";
			pstmt = dbConnection.prepareStatement(insertSQL);
			pstmt.setTimestamp(1, sqlEndDate);
			pstmt.setInt(2, suite.getIdReportTestSuite());
			pstmt.executeUpdate();
			suite.setSuiteCommitted(true);
		} catch (SQLException sql) {
			LOGGER.error("Exception::", sql);
			throw new AFTException(sql);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
	}

	/**
	 * This method will insert the test scenario report record into DB.
	 * 
	 * @param dbConnection
	 *            dbConnection
	 * @param suite
	 *            suite
	 * @param testScenarios
	 *            testScenarios
	 * @return scenario
	 * @throws AFTException
	 *             the aFT exception
	 */
	private TestScenario insertTestScenarioExecutionData(
			Connection dbConnection, TestSuite suite,
			List<TestScenario> testScenarios) throws AFTException {
		TestScenario scenario = null;
		PreparedStatement preparedStatementInsert = null;
		ResultSet rs = null;
		String insertSQL;
		int reportScenarioId = 0;

		try {
			for (TestScenario testScenario : testScenarios) {
				if (testScenario.isCommitted()) {
					continue;
				}
				scenario = testScenario;
				Timestamp sqlStartDate = null;
				Timestamp sqlEndDate = null;

				DateFormat formatter = new SimpleDateFormat(
						Constants.DATEFORMAT);
				if (testScenario.getStartTime() != null
						&& !testScenario.getStartTime().equals("")) {
					Date startDate = formatter.parse(testScenario
							.getStartTime());
					sqlStartDate = new Timestamp(startDate.getTime());
				}
				if (testScenario.getEndTime() != null
						&& !testScenario.getEndTime().equals("")) {
					Date endDate = formatter.parse(testScenario.getEndTime());
					sqlEndDate = new Timestamp(endDate.getTime());
				}
				insertSQL = "INSERT INTO rpt_testscenarios (testScenario, description, result, startDate, endDate, idTestSuiteReport) VALUES (?,?,?,?,?,?)";
				preparedStatementInsert = dbConnection.prepareStatement(
						insertSQL, Statement.RETURN_GENERATED_KEYS);
				preparedStatementInsert.setString(1,
						testScenario.getBusinessScenarioId());
				preparedStatementInsert.setString(2,
						testScenario.getBusinessScenarioDesc());
				preparedStatementInsert.setString(3,
						testScenario.getExecutionResult());
				preparedStatementInsert.setTimestamp(4, sqlStartDate);
				preparedStatementInsert.setTimestamp(5, sqlEndDate);
				preparedStatementInsert.setInt(6, suite.getIdReportTestSuite());
				preparedStatementInsert.executeUpdate();
				rs = preparedStatementInsert.getGeneratedKeys();
				if (rs.next()) {
					reportScenarioId = rs.getInt(1);
				}
				// insert test step report data into DB.
				insertTestStepExecutionData(testScenario, dbConnection,
						reportScenarioId);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
					preparedStatementInsert = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return scenario;
	}

	/**
	 * This method will insert the test step report record into DB.
	 * 
	 * @param testScenario
	 *            testScenario
	 * @param dbConnection
	 *            dbConnection
	 * @param reportScenarioId
	 *            reportScenarioId
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void insertTestStepExecutionData(TestScenario testScenario,
			Connection dbConnection, int reportScenarioId) throws AFTException {
		File image = null;
		try {
			if (testScenario.getTestCaseDetails().size() > 0) {
				for (TestCase testCase : testScenario.getTestCaseDetails()) {
					// TestCase testCase =
					// testScenario.getTestCaseDetails().get(0);
					for (TestStep testStep : testCase.getTestStepDetails()) {
						String evidenceType = null;
						// System.out.println(testStep.getEtlObjectType());
						if (testStep.getAction().toUpperCase()
								.startsWith("WS_")) {
							if (testStep.getServiceRequestName().length() > 0) {
								evidenceType = "XML";
								image = new File(
										testStep.getServiceRequestName());
							} else {
								if (testStep.getServiceResponseName().length() > 0) {
									image = new File(
											testStep.getServiceResponseName());
									evidenceType = "XML";
								}
							}
						} else {
							if (testStep.getImageName() != null
									&& !testStep.getImageName().equals("")) {
								image = new File(testStep.getImageName());
								evidenceType = "IMAGE";
							}
						}
						// insert report test step data
						insertReportTestStepData(dbConnection, testStep,
								evidenceType, image, reportScenarioId,
								testScenario);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * This method will insert the test step report record into DB.
	 * 
	 * @param dbConnection
	 *            dbConnection
	 * @param testStep
	 *            testStep7
	 * @param evidenceType
	 *            evidenceType
	 * @param image
	 *            image
	 * @param reportScenarioId
	 *            reportScenarioId
	 * @param testScenario
	 *            the test scenario
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void insertReportTestStepData(Connection dbConnection,
			TestStep testStep, String evidenceType, File image,
			int reportScenarioId, TestScenario testScenario)
			throws AFTException {

		PreparedStatement preparedStatementInsert = null;
		String insertSQL;
		// String imagePath = "";
		Timestamp sqlStartDate = null;
		Timestamp sqlEndDate = null;
		DateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);
		FileInputStream fis = null;
		try {
			String duration = Util.getInstance().formatTimeMilliSec(
					testStep.getTestStepExecutionTime());

			/*
			 * if (image != null) { imagePath = insertScreenshotImage(image,
			 * testScenario, evidenceType, testStep.getWsResponseType());
			 * 
			 * }
			 */
			if (testStep.getStartTime() != null
					&& !testStep.getStartTime().equals("")) {
				Date startDate = formatter.parse(testStep.getStartTime());
				sqlStartDate = new Timestamp(startDate.getTime());
			}
			if (testStep.getEndTime() != null
					&& !testStep.getEndTime().equals("")) {
				Date endDate = formatter.parse(testStep.getEndTime());
				sqlEndDate = new Timestamp(endDate.getTime());
			}

			// Get the command alias name and insert into Reporttable JIRA -
			// 1692
			String selectAliasQuery = "select displayAction from command where action  = '"
					+ testStep.getAction() + "'";
			PreparedStatement selectStatement = dbConnection
					.prepareStatement(selectAliasQuery);
			ResultSet resultSet = selectStatement
					.executeQuery(selectAliasQuery);
			String aliasActiion = null;
			while (resultSet.next()) {
				aliasActiion = resultSet.getString("displayAction");
			}

			insertSQL = "INSERT INTO rpt_teststeps (testStep, description, action, elementName, elementValue, result, startDate, "
					+ "endDate, idTestScenariosReport, errorMessage, evidenceType, executionDuration, executionEvidence,displayAction) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			preparedStatementInsert = dbConnection.prepareStatement(insertSQL);
			preparedStatementInsert.setString(1, testStep.getStepId());
			preparedStatementInsert.setString(2,
					testStep.getActionDescription());
			preparedStatementInsert.setString(3, testStep.getAction());
			preparedStatementInsert.setString(4, testStep.getElementName());
			preparedStatementInsert.setString(5, testStep.getElementValue());
			preparedStatementInsert.setString(6, testStep.getResult());
			preparedStatementInsert.setTimestamp(7, sqlStartDate);
			preparedStatementInsert.setTimestamp(8, sqlEndDate);
			preparedStatementInsert.setInt(9, reportScenarioId);
			preparedStatementInsert.setString(10, testStep.getErrorMessage());
			preparedStatementInsert.setString(11, evidenceType);
			preparedStatementInsert.setString(12, duration);
			// preparedStatementInsert.setString(13, imagePath);

			if (null != image && image.isFile()) {
				fis = new FileInputStream(image);
				preparedStatementInsert.setBinaryStream(13, (InputStream) fis,
						(int) (image.length()));
			} else {
				preparedStatementInsert.setBinaryStream(13, null);
			}
			
			preparedStatementInsert.setString(14, aliasActiion);

			preparedStatementInsert.executeUpdate();

			// Insert testStepReportid along with the ETL Blob into database
			if (testStep.getAction().equalsIgnoreCase("verifyDataCorrectness")) {
				// Insert ETL report data
				Map<String, String> recordMap = DatabaseUtil.getInstance()
						.getEtlrecordsMap();
				String matchedRecords = recordMap.get("matched").toString();
				String unMatchedRecords = recordMap.get("unmatched").toString();
				String unMatchedColumns = recordMap.get("columns").toString();
				Statement pStatement = dbConnection
						.prepareStatement("select idTestStepsReports from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate + "'");
				ResultSet rs = pStatement
						.executeQuery("select idTestStepsReport from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate
								+ "' and action = 'verifyDataCorrectness'");
				String reportIdTestStep = null;
				while (rs.next()) {
					reportIdTestStep = rs.getString("idTestStepsReport");
				}
				if (unMatchedColumns.length() > 2) {
					unMatchedColumns = unMatchedColumns.substring(1,
							unMatchedColumns.length() - 1);
					String[] columns = unMatchedColumns.split(",");
					StringBuilder builder = new StringBuilder();
					for (String column : columns) {
						column = column.substring(1, column.length() - 1);
						builder.append(column + ",");
					}
					unMatchedColumns = builder.toString();
					unMatchedColumns = unMatchedColumns.substring(0,
							unMatchedColumns.length() - 1);
				}

				preparedStatementInsert = dbConnection
						.prepareStatement("insert into rpt_etl_datacorrectness(matchedRecords,unmatchedRecords,"
								+ "unmatchedColumns,idTestStepsReport,dataDelta) values (?,?,?,?,?)");
				preparedStatementInsert.setObject(1, matchedRecords);
				preparedStatementInsert.setObject(2, unMatchedRecords);
				preparedStatementInsert.setObject(3, unMatchedColumns);
				preparedStatementInsert.setObject(4, reportIdTestStep);
				preparedStatementInsert.setObject(5,
						testStep.getEtlObjectType());
				preparedStatementInsert.execute();
			} else if (testStep.getAction().equalsIgnoreCase(
					"verifyDataCompleteness")) {
				// Insert ETL report data
				String recordMap = DatabaseUtil.getInstance()
						.getEtlDataCompletenessCount();

				// String source = recordMap.get("source").toString();
				// String destination = recordMap.get("destination").toString();
				//
				// String finalCount = "{\"SourceRecordColumns\":\"" + source
				// + "\",\"TargetRecordColumns\":\"" + destination + "\"}";
				Statement pStatement = dbConnection
						.prepareStatement("select idTestStepsReports from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate + "'");
				ResultSet rs = pStatement
						.executeQuery("select idTestStepsReport from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate
								+ "' and action = 'verifyDataCompleteness'");
				String reportIdTestStep = null;
				while (rs.next()) {
					reportIdTestStep = rs.getString("idTestStepsReport");
				}

				preparedStatementInsert = dbConnection
						.prepareStatement("insert into rpt_etl_datacompleteness(dataComplete,idTestStepsReport) values (?,?)");
				preparedStatementInsert.setObject(1, recordMap);
				preparedStatementInsert.setObject(2, reportIdTestStep);
				preparedStatementInsert.execute();
			} else if (testStep.getAction().equalsIgnoreCase("verifySchema")) {

				Statement pStatement = dbConnection
						.prepareStatement("select idTestStepsReports from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate + "'");
				ResultSet rs = pStatement
						.executeQuery("select idTestStepsReport from rpt_teststeps where idTestScenariosReport = "
								+ reportScenarioId
								+ " and endDate ='"
								+ sqlEndDate + "' and action = 'verifySchema'");
				String reportIdTestStep = null;
				while (rs.next()) {
					reportIdTestStep = rs.getString("idTestStepsReport");
				}

				preparedStatementInsert = dbConnection
						.prepareStatement("insert into rpt_etl_schemavalidation(schemaResult,idTestStepsReport,schemaType) values (?,?,?)");
				preparedStatementInsert.setObject(1, DatabaseUtil.getInstance()
						.getSchemaValidationResult());
				preparedStatementInsert.setObject(2, reportIdTestStep);
				preparedStatementInsert.setObject(3, DatabaseUtil.getInstance()
						.getSchemaType());
				preparedStatementInsert.execute();
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (preparedStatementInsert != null) {
					preparedStatementInsert.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			preparedStatementInsert = null;
		}

	}

	/**
	 * This method will save the screen shot image into tomcat folder using rest
	 * service. service.
	 * 
	 * @param file
	 *            file
	 * @param testScenario
	 *            testScenario
	 * @param evidenceType
	 *            the evidence type
	 * @param responseType
	 *            responseType
	 * @return String
	 * @throws AFTException
	 *             the aFT exception
	 */
	@SuppressWarnings("unused")
	private String insertScreenshotImage(File file, TestScenario testScenario,
			String evidenceType, String responseType) throws AFTException {
		JSONObject jObj = null;
		String imagePath = "";
		String postUrl = "";
		FileInputStream imageInFile = null;
		try {
			if (evidenceType != null) {
				// if the evidence type is xml then form the request url
				// accordingly.
				if (evidenceType.equals("XML")) {
					postUrl = enlaceURL + "/storeServiceRequest";
				} else if (evidenceType.equals("IMAGE")) {
					postUrl = enlaceURL + "/storeScreenshot";
				}
				// Reading a Image file from file system
				imageInFile = new FileInputStream(file);
				byte imageData[] = new byte[(int) file.length()];
				imageInFile.read(imageData);

				// Converting Image byte array into Base64 String
				String imageDataString = encodeImage(imageData);
				// call the rest service to store the image in the tomcat folder
				// and get the path of the stored image as a json response.
				jObj = postScreenshotData(postUrl, imageDataString,
						testScenario, responseType);
				if (jObj != null) {
					// get the path from the json response.
					imagePath = jObj.getString("path");
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (imageInFile != null) {
				try {
					imageInFile.close();
				} catch (IOException e) {
					LOGGER.info("Exception raised" + e);
				}
			}
		}
		// return the image path
		return imagePath;
	}

	/**
	 * Call the rest service and post the screen shot image data into tomcat
	 * folder.
	 * 
	 * @param url
	 *            url
	 * @param imageDataString
	 *            the image data string
	 * @param testScenario
	 *            testScenario
	 * @param responseType
	 *            responseType
	 * @return jObj
	 * @throws AFTException
	 *             the aFT exception
	 */
	public JSONObject postScreenshotData(String url, String imageDataString,
			TestScenario testScenario, String responseType) throws AFTException {
		InputStream is = null;
		JSONObject jObj = null;
		String jsonString = "";
		// Making HTTP request
		try {
			HttpContext localContext = new BasicHttpContext();

			// defaultHttpClient
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			JSONObject json = new JSONObject();
			// put the iamge data, project id, test suite name and scenario id
			// information the json object in order to send the information to
			// json request.
			json.put("projectId", RuntimeProperties.getInstance()
					.getProjectId());
			json.put("imageDataString", imageDataString);
			json.put("testSuiteName", testScenario.getTestSuite()
					.getTestSuiteName());
			json.put("testScenarioId", testScenario.getBusinessScenarioId());
			json.put("responseType", responseType);

			httpPost.setEntity(new StringEntity(json.toString(), "UTF-8"));
			// Set header
			httpPost.setHeader("Content-Type", "application/json");
			// execute post request
			HttpResponse response = httpClient.execute(httpPost, localContext);
			// get the response
			HttpEntity httpEntity = response.getEntity();
			is = httpEntity.getContent();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (JSONException je) {
			LOGGER.error("Exception::", je);
			throw new AFTException(je);
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		try {
			// parse the json response.
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			jsonString = sb.toString();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOGGER.info("Exception raised" + e);
				}
			}
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(jsonString);
		} catch (JSONException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		// return JSON String
		return jObj;
	}

	/**
	 * This method will get the test data row number from DB based on table name
	 * and column name.
	 * 
	 * @param tableName
	 *            tableName
	 * @param columnName
	 *            columnName
	 * @param columnValue
	 *            columnValue
	 * @param flag
	 *            flag
	 * @param paramArray
	 *            paramArray
	 * @return the int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int getTestDataRowNumber(String tableName, String columnName,
			String columnValue, boolean flag, List<String> paramArray)
			throws AFTException {
		LOGGER.debug("Reading functional scenarios from DB");
		Connection conn = null;
		ResultSet rs = null;
		int rowId = -1;
		Statement stmt = null;
		StringBuffer sqlQuery = null;
		try {
			LOGGER.info("---------Inside getTestDataRowNumber method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			if (flag) {
				int count = getTestDataRowCount(tableName);
				int idSequence = Integer.parseInt(paramArray.get(3));
				for (int id = idSequence; id <= count; id++) {
					sqlQuery = new StringBuffer();
					sqlQuery.append("SELECT ").append("idsequence")
							.append(" from ").append(tableName)
							.append(" where ").append(columnName).append(" = ")
							.append("'").append(columnValue).append("'");
					sqlQuery.append(" and idsequence").append(" = ").append(id);
					rowId = getRowId(rs, stmt, sqlQuery);
					if (rowId != -1) {
						break;
					}
				}
			} else {
				sqlQuery = new StringBuffer();
				sqlQuery.append("SELECT ").append("idsequence")
						.append(" from ").append(tableName).append(" where ")
						.append(columnName).append(" = ").append("'")
						.append(columnValue).append("'");
				rowId = getRowId(rs, stmt, sqlQuery);
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting getTestDataRowNumber---------------------------");
		return rowId;

	}

	/**
	 * This method will get the row id from DB.
	 * 
	 * @param rs
	 *            rs
	 * @param stmt
	 *            stmt
	 * @param sqlQuery
	 *            sqlQuery
	 * @return the int
	 * @throws SQLException
	 *             the sQL exception
	 */
	private int getRowId(ResultSet rs, Statement stmt, StringBuffer sqlQuery)
			throws SQLException {
		int rowId = -1;
		sqlQuery.append(" order by idsequence limit 1");
		rs = stmt.executeQuery(sqlQuery.toString());
		while (rs.next()) {
			rowId = rs.getInt("idsequence");
		}
		return rowId;
	}

	/**
	 * This method will get the module id from DB based on moduleName and
	 * project id.
	 * 
	 * @param moduleName
	 *            moduleName
	 * @return the int
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int getCustomScriptModuleId(String moduleName) throws AFTException {
		LOGGER.debug("Reading custom script from DB");
		Connection conn = null;
		ResultSet rs = null;
		int moduleId = -1;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		try {
			LOGGER.info("---------Inside getCustomScript method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			sqlQuery.append(
					"SELECT idCustomScriptsModule FROM custom_scripts_modules")
					.append(" where idProject = ")
					.append(RuntimeProperties.getInstance().getProjectId())
					.append(" and module_name= ").append("'")
					.append(moduleName).append("'");
			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				moduleId = rs.getInt("idCustomScriptsModule");
			}
			LOGGER.debug("Retrieved module id [" + moduleId + "] for module ["
					+ moduleName + "]");

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("---------Exiting getCustomScript---------------------------");
		return moduleId;

	}

	/**
	 * This method will get the custom scripts data from DB based on moduleId,
	 * customScriptName and project id.
	 * 
	 * @param moduleId
	 *            moduleId
	 * @param customScriptName
	 *            customScriptName
	 * @return the Map<String,String>
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Map<String, String> getCustomScriptsData(int moduleId,
			String customScriptName) throws AFTException {
		LOGGER.debug("Reading custom script from DB");
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		Map<String, String> customScriptsData = new HashMap<String, String>();
		try {
			LOGGER.info("---------Inside getCustomScript method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());
			sqlQuery.append("SELECT * FROM custom_scripts")
					.append(" where idProject = ")
					.append(RuntimeProperties.getInstance().getProjectId())
					.append(" and idCustomScriptsModule= ").append(moduleId)
					.append(" and name= ").append("'").append(customScriptName)
					.append("'");
			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				customScriptsData.put("scriptType", rs.getString("scriptType"));
				customScriptsData.put("scriptValue", rs.getString("script"));
				LOGGER.trace("Retrieved module id [" + moduleId
						+ "], script type [" + rs.getString("scriptType")
						+ "], script value [" + rs.getString("script") + "]");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("---------Exiting getCustomScript---------------------------");
		return customScriptsData;
	}

	/**
	 * This method will get the column names from DB based on table name.
	 * 
	 * @param tableName
	 *            tableName
	 * @return the List<String>
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<String> getColumnNames(String tableName) throws AFTException {
		LOGGER.debug("Reading fgetColumnNames from DB");
		Connection conn = null;
		ResultSet rs = null;
		List<String> headerNames = null;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		sqlQuery.append(
				"select column_name from information_schema.columns where table_name=")
				.append("'").append(tableName).append("'");
		try {
			LOGGER.info("---------Inside getColumnNames method---------------------------");
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery.toString());
			headerNames = new ArrayList<String>();
			while (rs.next()) {
				if (!rs.getString("column_name").startsWith("id")) {
					headerNames.add(rs.getString("column_name"));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		LOGGER.info("---------Exiting getColumnNames---------------------------");
		return headerNames;

	}

	/**
	 * This method will get the execution engine name configured in UI for the
	 * specific TestSet ID.
	 * 
	 * @param testSetId
	 *            the test set id
	 * @return the TestSuite
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getExecutionEngine(String testSetId) throws AFTException {
		LOGGER.debug("Reading test suite from DB");
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;

		String sqlQuery = "select execution_engine from testset where idTestSet ="
				+ testSetId;
		LOGGER.info(sqlQuery);
		String engineName = null;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);

			while (rs.next()) {
				engineName = rs.getString("execution_engine");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		return engineName;

	}

	/**
	 * This method will get blob from Db and writed the blob to an XML file.
	 * 
	 * @return the OR XML file path
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getQTPObjectRepository() throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		String sqlQuery;
		String tempFilePath = "";
		try {

			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// Query to get the OR Blob from Database and store in Execution
			// directory
			LOGGER.debug("Executing SQL to fetch the OR BLOB Object and store it to OR directory.");
			sqlQuery = "select script from custom_scripts where name='QTP Imported TSR' and idProject="
					+ "'"
					+ RuntimeProperties.getInstance().getProjectId()
					+ "'";
			statement = conn.prepareStatement(sqlQuery);
			LOGGER.debug("Fetching the result set");
			rs = statement.executeQuery(sqlQuery);
			LOGGER.debug("Executing the Query :[ " + sqlQuery + "]");
			while (rs.next()) {
				InputStream stream = rs.getBinaryStream(1);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						stream);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int data = -1;
				while ((data = bufferedInputStream.read()) != -1) {
					byteArrayOutputStream.write(data);
				}
				bufferedInputStream.close();
				StringBuffer sb = new StringBuffer();
				byte[] text = byteArrayOutputStream.toByteArray();
				for (byte ab : text) {
					sb.append((char) ab);
				}
				LOGGER.debug("Successfully fetched the Blob Object");
				DateFormat dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				String dateValue = dt.format(date).toString();
				tempFilePath = System.getenv("TMP") + dateValue + ".xml";
				OutputStream outputStream = new FileOutputStream(tempFilePath);
				byteArrayOutputStream.writeTo(outputStream);
				LOGGER.debug("Successfully Copied the OR file" + "["
						+ tempFilePath + "]to User Temp Directory");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (statement != null) {
					statement.close();
					statement = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return tempFilePath;
	}

	/**
	 * This method will get VBS file dump from the database and write to the
	 * External script folder as .qa file
	 * 
	 * @return List of XML file paths
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<String> getVbsFileDump() throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		String sqlQuery;
		String orFilePath = "";
		List<String> filePaths;
		try {
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// Query to get the VBS files from Database and store in Execution
			// directory
			LOGGER.debug("Executing SQL to fetch the VBS files and store it to External Script directory.");
			sqlQuery = "select name,script from custom_scripts where scriptType='vbs' and idProject="
					+ "'"
					+ RuntimeProperties.getInstance().getProjectId()
					+ "'";
			statement = conn.prepareStatement(sqlQuery);
			rs = statement.executeQuery(sqlQuery);
			filePaths = new ArrayList<String>();
			while (rs.next()) {
				String fileName = rs.getString("name");
				LOGGER.debug("Executing the Query :[ " + sqlQuery + "]");
				InputStream stream = rs.getBinaryStream(1);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						stream);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int data = -1;
				while ((data = bufferedInputStream.read()) != -1) {
					byteArrayOutputStream.write(data);
				}
				bufferedInputStream.close();
				StringBuffer sb = new StringBuffer();
				byte[] text = byteArrayOutputStream.toByteArray();
				for (byte ab : text) {
					sb.append((char) ab);
				}
				LOGGER.debug("Successfully fetched the vbs file Objects");
				// Need to change the FileName as user parameter
				orFilePath = "./resource/ExternalScripts/" + fileName + ".qa";
				OutputStream outputStream = new FileOutputStream(orFilePath);
				byteArrayOutputStream.writeTo(outputStream);
				LOGGER.debug("Successfully Copied the VBS file " + "["
						+ fileName + ".qa]" + "to External Scripts Folder");
				filePaths.add(orFilePath);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (statement != null) {
					statement.close();
					statement = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return filePaths;
	}

	/**
	 * Update config properties.
	 * 
	 * @param configType
	 *            the config type
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void updateConfigProperties(int configType) throws AFTException {
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		// Check if the property exists in database.if exists update else insert
		// new record

		String sqlQuery = "select propertyName,propertyValue from configchild where idproject ="
				+ RuntimeProperties.getInstance().getProjectId()
				+ " and configtype=" + configType;
		try {
			// Reading AFT Config values from DB for configuration pr4operties
			// get the Db connection
			conn = DriverManager.getConnection(RuntimeProperties.getInstance()
					.getDbUrl(), RuntimeProperties.getInstance().getUserName(),
					RuntimeProperties.getInstance().getPassword());

			// Get the config values from Remotag Config
			// call the statement
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			PreparedStatement preparedStatementInsert = null;
			while (rs.next()) {
				if (!rs.getString("propertyValue").contains(
						XAFTWebdriverConfigProperties.getInstance()
								.getFirefoxProfilePath())) {
					// Update db with config value
				} else {
					// Insert into database
					String insertQuery = "insert into configchild (firefoxProfilePath) values (?)";
					preparedStatementInsert = conn
							.prepareStatement(insertQuery);
					preparedStatementInsert.setString(1,
							XAFTWebdriverConfigProperties.getInstance()
									.getFirefoxProfilePath());
					preparedStatementInsert.executeUpdate();
				}
				if (!rs.getString("propertyValue").equalsIgnoreCase(
						XAFTWebdriverConfigProperties.getInstance()
								.getIeDriverPath())) {
					// Update db with config value

				} else {
					// Insert into database
					String insertQuery = "insert into configchild (ieDriverPath) values (?)";
					preparedStatementInsert = conn
							.prepareStatement(insertQuery);
					preparedStatementInsert.setString(1,
							XAFTWebdriverConfigProperties.getInstance()
									.getFirefoxProfilePath());
					preparedStatementInsert.executeUpdate();
				}
				if (!rs.getString("propertyValue").equalsIgnoreCase(
						XAFTWebdriverConfigProperties.getInstance()
								.getChromeDriverPath())) {
					// Update db with config value

				} else {
					// Insert into database
					String insertQuery = "insert into configchild (chromeDriverPath) values (?)";
					preparedStatementInsert = conn
							.prepareStatement(insertQuery);
					preparedStatementInsert.setString(1,
							XAFTWebdriverConfigProperties.getInstance()
									.getFirefoxProfilePath());
					preparedStatementInsert.executeUpdate();
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
	}

	/**
	 * Encodes the byte array into base64 string.
	 * 
	 * @param imageByteArray
	 *            - byte array
	 * @return String a {@link java.lang.String}
	 */
	public static String encodeImage(byte[] imageByteArray) {
		return Base64.encodeBase64URLSafeString(imageByteArray);
	}

	/**
	 * Gets the enlace url.
	 * 
	 * @return the enlace url
	 */
	public String getEnlaceURL() {
		return enlaceURL;
	}

	/**
	 * Sets the enlace url.
	 * 
	 * @param enlaceURL
	 *            the new enlace url
	 */
	public void setEnlaceURL(String enlaceURL) {
		this.enlaceURL = enlaceURL;
	}

	/**
	 * Gets the etlrecords map.
	 * 
	 * @return the etlrecords map
	 */
	public Map<String, String> getEtlrecordsMap() {
		return etlrecordsMap;
	}

	/**
	 * Sets the etlrecords map.
	 * 
	 * @param etlrecordsMap
	 *            the etlrecords map
	 */
	public void setEtlrecordsMap(Map<String, String> etlrecordsMap) {
		this.etlrecordsMap = etlrecordsMap;
	}

	/**
	 * Gets the etl data completeness count.
	 * 
	 * @return the etl data completeness count
	 */
	public String getEtlDataCompletenessCount() {
		return etlDataCompletenessCount;
	}

	/**
	 * Sets the etl data completeness count.
	 * 
	 * @param etlDataCompletenessCount
	 *            the new etl data completeness count
	 */
	public void setEtlDataCompletenessCount(String etlDataCompletenessCount) {
		this.etlDataCompletenessCount = etlDataCompletenessCount;
	}

	public String getSchemaValidationResult() {
		return schemaValidationResult;
	}

	public void setSchemaValidationResult(String schemaValidationResult) {
		this.schemaValidationResult = schemaValidationResult;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
	}

}
