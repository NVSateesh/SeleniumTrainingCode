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
 * Class: databaseFixture
 * 
 * Purpose: This class implements methods that allows users to execute sql
 * queries to perform CRUD operations
 */

package com.ags.aft.fixtures.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * The Class databaseFixture.
 */
public class DatabaseFixture {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(DatabaseFixture.class);

	/** The Connection properties object. */
	private Properties connUidPwdProp = new Properties();

	/** Driver class for MSSQL. */
	private static final String MSSQLDRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	/** Driver class for User DSN or MS Access. */
	private static final String DSNACCCESSDRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

	/** Driver class for Oracle. */
	private static final String ORACLEDRIVER = "oracle.jdbc.driver.OracleDriver";

	/** Driver class for MySQL. */
	private static final String MYSQLDRIVER = "com.mysql.jdbc.Driver";

	private String errorMessage = null;

	private final int maxRows = Constants.MAXARRAYROWCOUNT;

	private final int maxColumns = Constants.MAXARRAYCOLUMNCOUNT;

	private int resultsetRowCount = 0;

	/**
	 * Instantiates a new AFT database Fixture
	 * 
	 */
	public DatabaseFixture() {
		connUidPwdProp.clear();
	}

	/**
	 * Opens a new DB connection and stores the databaseInstance object in
	 * databaseInstanceMap
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param connIdentifier
	 *            - User defined connection identifier variable
	 * @param parameterList
	 *            - User passed parameters
	 * @return connection identifier as String
	 * @throws AFTException
	 */
	public String openDBConnection(TestStepRunner testStepRunner,
			String connIdentifier, String parameterList) throws AFTException {

		String[] paramArray = null;
		String connString, jdbcClassString, parmListLowerCase, dbInstanceIdentifier, dbType = null;

		/** The Database connection object. */
		Connection connectionObject = null;

		// convert to lower case and create connString accordingly
		parmListLowerCase = parameterList.toLowerCase();
		testStepRunner.getTestSuiteRunner().setDbParameterLst(parameterList);
		testStepRunner.getTestSuiteRunner().setDbConnIdentifier(connIdentifier);
		LOGGER.trace("Executing [openDBConnection] with connection string ["
				+ parameterList + "]");

		paramArray = parameterList.split(Constants.DBFIXTUREPARAMDELIMITER);

		// if user has provided dbType = MSSQL
		if (parmListLowerCase.contains("mssql")) {
			connString = createMSSqlConnString(paramArray);
			jdbcClassString = MSSQLDRIVER;
			dbType = "MSSQL";
			// if user has provided DSN without dbType
		} else if (parmListLowerCase.contains("dsn")) {
			connString = createDSNConnString(paramArray);
			jdbcClassString = DSNACCCESSDRIVER;
			dbType = "DSN";
			// if user has provided Access data file name
		} else if (parmListLowerCase.contains("dbq")) {
			connString = createAccessConnString(paramArray);
			jdbcClassString = DSNACCCESSDRIVER;
			dbType = "ACCESS";
			// if user has provided dbType = Oracle
		} else if (parmListLowerCase.contains("oracle")) {
			connString = createOracleConnString(paramArray);
			jdbcClassString = ORACLEDRIVER;
			dbType = "ORACLE";
			// if user has provided dbType = MYSQL
		} else if (parmListLowerCase.contains("mysql")) {
			connString = createMySqlConnString(paramArray);
			jdbcClassString = MYSQLDRIVER;
			dbType = "MYSQL";
		} else {
			errorMessage = "Please refer to Wiki for the type of databases supported by AFT";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		LOGGER.debug("Connection string [" + connString + "]");

		try {

			Class.forName(jdbcClassString);
			if (!connUidPwdProp.isEmpty()) { // if user provided userid and
				// password for connection

				LOGGER.debug("Username = " + connUidPwdProp.get("uid")
						+ " Password = " + connUidPwdProp.get("pwd"));

				connectionObject = DriverManager.getConnection(connString,
						connUidPwdProp.getProperty("uid"),
						connUidPwdProp.getProperty("pwd"));

			} else { // for mssql and connections without userid and password
				// Added security flag to support windows authentication
				if (parmListLowerCase.contains("mssql")) {
					connectionObject = DriverManager.getConnection(connString
							+ "integratedSecurity=true;");
				} else {
					connectionObject = DriverManager.getConnection(connString);
				}

			}
			// create an object of databaseInstance
			DatabaseInstance dbInstance = new DatabaseInstance(
					connectionObject, dbType);

			// generate an unique identifier for the opened database connection
			dbInstanceIdentifier = dbInstance.getConnectionIdentifier();

			// store the (dbInstanceIdentifier, dbInstance object) in
			// databaseInstanceMap
			DatabaseInstanceManager.getInstance().addDBInstance(
					dbInstanceIdentifier, dbInstance);
			LOGGER.info("Connection [" + dbInstanceIdentifier
					+ "] has been opened successfully");
			// Added the below code to maintain db details Instance wise for ETL
			// data verification purpose
			DatabaseInstanceManager.getInstance().addDBInstanceParameters(
					dbInstanceIdentifier, paramArray);
			// if user did not provide any identifier variable
			if (connIdentifier == null || connIdentifier.isEmpty()
					|| connIdentifier.equalsIgnoreCase(Constants.EMPTYVALUE)) {
				LOGGER.info("No user variable passed");
			} else {
				// assign dbInstanceIdentifier to connIdentifier
				LOGGER.info("Storing connection [" + dbInstanceIdentifier
						+ "] in user variable [" + connIdentifier + "]");
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(),
						"openDBConnection", connIdentifier, false,
						dbInstanceIdentifier);
			}
			// store the dbInstanceIdentifier in system variable
			// AFT_LastDBConnection
			LOGGER.info("Storing connection [" + dbInstanceIdentifier
					+ "] in system variable [AFT_LastDBConnection] as default");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTDBCONNECTION), true,
					dbInstanceIdentifier);
		} catch (ClassNotFoundException e) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (SQLException e) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("Exception::", e);
			throw new AFTException(e);

		} finally {
			connUidPwdProp.clear();
		}
		return dbInstanceIdentifier;
	}

	/**
	 * Closes the DB Connection associated with the given Connection
	 * identifier/System variable
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param connIdentifier
	 *            - identifier passed by user
	 * 
	 * @throws AFTException
	 */
	public void closeDBConnection(TestStepRunner testStepRunner,
			String connIdentifier) throws AFTException {

		String dbInstanceIdentifier = null, lastClosedConnection = null, connName = null;

		connName = connIdentifier;

		LOGGER.trace("Executing [closeDBConnection] for connection identifier ["
				+ connName + "]");

		// if user has not passed an identifier
		if (connName == null || connName.isEmpty()) {
			LOGGER.info("User has not passed connection identifier. "
					+ "Getting connection from AFT_LastDBConnection");
			connName = Variable.getInstance().generateSysVarName(
					SystemVariables.AFT_LASTDBCONNECTION);
		}

		// get the stored value of the identifier variable
		dbInstanceIdentifier = Helper.getInstance().getActionValue(connName);

		// if there is no value present
		if (dbInstanceIdentifier.isEmpty()
				|| !DatabaseInstanceManager.getInstance()
						.checkDBInstanceExists(dbInstanceIdentifier)) {
			errorMessage = "No connection found for the given identifier."
					+ "Please refer to Wiki for more details on [closeDBConnection]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// get the databaseInstance object mapped to the instance identifier
		DatabaseInstance dbInstance = DatabaseInstanceManager.getInstance()
				.getDBInstance(dbInstanceIdentifier);

		// get the connection object for the dbInstance object
		Connection objConnection = dbInstance.getConnectionObject();

		if (objConnection != null) {
			try {

				// if the connection is not already closed
				if (!objConnection.isClosed()) {
					objConnection.close();
					LOGGER.info("Connection [" + connName
							+ "] has been closed successfully");
				} else {
					LOGGER.warn("Connection [" + connName
							+ "] is already closed");
				}

				// remove the (instanceIdentifier,databaseInstance)pair from
				// databaseInstanceMap
				DatabaseInstanceManager.getInstance().removeDBInstance(
						dbInstanceIdentifier);

			} catch (SQLException e) {
				// set onDbErrorValue
				setOnDbErrorValue(testStepRunner);

				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} finally {
				try {
					if (objConnection != null && !objConnection.isClosed()) {
						objConnection.close();
					}
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
				}
				objConnection = null;
			}
		} else {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			errorMessage = "Invalid connection information. Connection object not found.";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// if the user has closed the last opened connection,
		// then clear the AFT_LastDBConnection system variable
		lastClosedConnection = Helper.getInstance().getActionValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_LASTDBCONNECTION));
		if (lastClosedConnection.compareToIgnoreCase(dbInstanceIdentifier) == 0) {
			LOGGER.info("User has closed the connection stored in system variable [AFT_LastDBConnection]."
					+ "Let us clear this system variable also.");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTDBCONNECTION), true, "");
		}
	}

	/**
	 * Executes the given query with given connection identifier. Stores the DB
	 * result(if any) in returnVariable(if any) and AFT_LastDBResult
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable in which to store result
	 * 
	 * @param parameterList
	 *            parameter list consisting of connection identifier, sql query
	 *            string with parameters
	 * 
	 * @return Result value returned by sql query as String
	 * @throws AFTException
	 * 
	 */
	public String executeDBQuery(TestStepRunner testStepRunner,
			String returnVariable, String parameterList) throws AFTException {

		String connIdentifier, sqlQuery, dbInstanceIdentifier = null;
		String returnValue = null;

		LOGGER.trace("Executing [executeDBQuery] for sql query ["
				+ parameterList + "]");

		// if user has not passed any parameter
		if (parameterList == null
				|| (parameterList != null && parameterList.isEmpty())) {
			errorMessage = "No sql query passed. "
					+ "Please refer to wiki on how to use [executeDBQuery]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		boolean selectQuery = false;

		String[] paramArray = parameterList
				.split(Constants.DBFIXTUREPARAMDELIMITER);

		// if user has not passed an identifier
		if (paramArray.length < 2) {
			LOGGER.info("No connection identifier passed. "
					+ "Getting connection from AFT_LastDBConnection");

			connIdentifier = Variable.getInstance().generateSysVarName(
					SystemVariables.AFT_LASTDBCONNECTION);

			sqlQuery = paramArray[0];

		} else {
			connIdentifier = paramArray[0].trim();

			sqlQuery = paramArray[1];
		}

		// check if it is select or update query type
		if (sqlQuery.trim().toLowerCase().startsWith("select")) {
			selectQuery = true;
		}

		// get the actual value if using AFT_LastDBConnection
		dbInstanceIdentifier = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), connIdentifier);

		// if identifier does not exists
		if (dbInstanceIdentifier == null
				|| dbInstanceIdentifier.isEmpty()
				|| !DatabaseInstanceManager.getInstance()
						.checkDBInstanceExists(dbInstanceIdentifier)) {
			errorMessage = "No open DB connection is available";
			// log the error message and throw the exception.
			logException(testStepRunner);
		}

		// get the database instance
		DatabaseInstance dbInstance = DatabaseInstanceManager.getInstance()
				.getDBInstance(dbInstanceIdentifier);
		// String[] id = DatabaseInstanceManager.getInstance()
		// .getDBInstanceParameters(dbInstanceIdentifier);
		Statement objStatement = null;
		ResultSet objResultSet = null;
		String updateQueryResult = null;
		int rowCount = 0;
		String[][] arrayResults = null;
		String printMsg = null;

		try {

			if (dbInstance.getConnectionObject().isClosed()) {
				errorMessage = "Cannot execute the query as DB connection is closed. "
						+ "Please open a new connection and execute the query";
				LOGGER.info("No open DB connection is available");
				// log the error message and throw the exception.
				logException(testStepRunner);

			}
			LOGGER.info("Executing the query [" + sqlQuery
					+ "] with Connection [" + dbInstanceIdentifier + "]");
			objStatement = dbInstance.getConnectionObject().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			while (dbInstanceIdentifier != null) {
				try {
					if (selectQuery) {
						objResultSet = objStatement.executeQuery(sqlQuery);
						break;
					} else {
						rowCount = objStatement.executeUpdate(sqlQuery);
						break;
					}
				} catch (SQLSyntaxErrorException se) {
					LOGGER.error("Sql Syntax Exception::", se);
					throw new AFTException(se);
				} catch (SQLWarning sw) {
					LOGGER.error("SQL Warning Exception::", sw);
					throw new AFTException(sw);
				} catch (SQLException s) {
					LOGGER.error("SQL Exception::", s);
					LOGGER.info("Network Connection has been lost...");
					int counter = testStepRunner.getTestSuiteRunner()
							.getDbConnCounter();
					if (counter == 0) {
						dbInstanceIdentifier = retryDbConnection(testStepRunner);
						// get the database instance
						dbInstance = DatabaseInstanceManager.getInstance()
								.getDBInstance(dbInstanceIdentifier);
						objStatement = dbInstance.getConnectionObject()
								.createStatement(
										ResultSet.TYPE_SCROLL_INSENSITIVE,
										ResultSet.CONCUR_READ_ONLY);
					} else {
						break;
					}
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
				}
			}
			LOGGER.info("Query [" + sqlQuery
					+ "] has been executed successfully");

			if ((selectQuery)
					&& (objResultSet == null || !objResultSet.isBeforeFirst())) {
				errorMessage = "Empty result set returned by query ["
						+ sqlQuery
						+ "]. Please check the query and execute again";
				LOGGER.error(errorMessage);
				throw new AFTException(errorMessage);
			}

			if (selectQuery) {
				arrayResults = convertResultSetToArray(objResultSet);
				// store the number of records retrieved
				returnValue = String.valueOf(resultsetRowCount);
				printMsg = "number of rows retrieved";

			} else {
				updateQueryResult = Integer.toString(rowCount);
				// store the number of records updated
				returnValue = updateQueryResult;
				printMsg = "number of records modified";
			}

		} catch (DataTruncation d) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.warn("Data Truncation Exception::", d);
			throw new AFTException(d);
		} catch (SQLWarning sw) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.warn("Sql Warnings Exception::", sw);
			throw new AFTException(sw);
		} catch (SQLException s) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("SQL Exception::", s);
			throw new AFTException(s);
		} catch (Exception e) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (objResultSet != null) {
					objResultSet.close();
				}
				if (objStatement != null) {
					objStatement.close();
				}
			} catch (SQLException e) {
				// set onDbErrorValue
				setOnDbErrorValue(testStepRunner);
				LOGGER.warn("Exception while closing statement and resultset objects");
				throw new AFTException(e);
			}
		}

		// if user has provided returnVariable
		if (returnVariable != null && !returnVariable.isEmpty()
				&& !returnVariable.equalsIgnoreCase("novalue")) {
			if (selectQuery) {
				LOGGER.info("Storing query result[array] in user passed return variable ["
						+ returnVariable + "]");
				Variable.getInstance().setArrayVariableValue(
						testStepRunner.getTestSuiteRunner(),
						Constants.DBARRAYTYPE, returnVariable, arrayResults);
			} else {
				LOGGER.info("Storing " + printMsg + " [" + returnValue
						+ "] in user passed return variable [" + returnVariable
						+ "]");
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(), "executeDBQuery",
						returnVariable, false, updateQueryResult);
			}
		} else {
			LOGGER.info("No return variable passed by user.");
		}

		// store the result in AFT_LastDBResult by default
		LOGGER.info("Storing " + printMsg + " [" + returnValue
				+ "] in system variable [AFT_LastDBResult] by default");
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_LASTDBRESULT), true, returnValue);

		return returnValue;
	}

	/**
	 * Executes the given Stored procedure with given connection identifier.
	 * Stores the DB result in returnVariable(if any) and AFT_LastDBResult
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable in which to store result
	 * 
	 * @param parameterList
	 *            parameter list consisting of connection identifier,stored
	 *            procedure name and input parameter list
	 * 
	 * @return Result value returned by Stored procedure as String
	 * @throws AFTException
	 * 
	 */
	public String executeStoredProc(TestStepRunner testStepRunner,
			String returnVariable, String parameterList) throws AFTException {

		String connIdentifier = null;
		String storedProcName = null;
		String parameters = null;
		String dbInstanceIdentifier = null;
		boolean connIdentifierPassed = true;

		LOGGER.trace("Executing [executeStoredProc] for parameters ["
				+ parameterList + "]");

		// if user has not passed any parameter
		if (parameterList == null || parameterList.isEmpty()) {
			errorMessage = "No parameters passed. Please refer to wiki on how to use [executeStoredProc]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// if user has not provided stored procedure
		if (!parameterList.toLowerCase().contains(
				Constants.EXECUTEDBPROCEDUREPARAMPROCEDURENAME)) {
			errorMessage = "Parameters does not include Stored procedure name. "
					+ "Please refer to wiki on how to use [executeStoredProc]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		String[] connIdentifierArray = null;
		String[] storedProcArray = null;
		String[] paramsArray = null;
		String strEqualSign = "=";
		String callStr = null;
		String strParam = "";
		String[] parametersArray = null;
		int inputParamCount = 0;
		CallableStatement objCallable = null;
		ResultSet objResultSet = null;
		String returnValue = null;
		boolean executeResult = false;

		try {

			String[] paramArray = parameterList
					.split(Constants.DBFIXTUREPARAMDELIMITER);

			for (String param : paramArray) {
				if (param
						.contains(Constants.EXECUTEDBPROCEDUREPARAMCONNECTIONIDENTIFIER)) {
					connIdentifierArray = param.split(strEqualSign);
				} else if (param
						.contains(Constants.EXECUTEDBPROCEDUREPARAMPROCEDURENAME)) {
					storedProcArray = param.split(strEqualSign);
				} else if (param
						.contains(Constants.EXECUTEDBPROCEDUREPARAMPARAMETERS)) {
					paramsArray = param.split(strEqualSign);
				} else {
					// throwing error message if there is no proper inputs
					errorMessage = "Provided stored procedure information is not correct. Please see the wiki how the data script to be provided.";
					LOGGER.error(errorMessage);
					throw new AFTException(errorMessage);
				}
			}

			// get connection identifier
			if (connIdentifierArray != null) {
				connIdentifier = connIdentifierArray[1];
			} else {
				connIdentifierPassed = false;
			}

			// get stored procedure name
			storedProcName = storedProcArray[1];

			// get input parameter list
			if (paramsArray != null && paramsArray.length > 0) {
				parameters = paramsArray[1];
			}

			LOGGER.debug("Identifer = [" + connIdentifier + "], storedProc = ["
					+ storedProcName + "], Params = [" + parameters + "]");

			// if user has not passed an identifier
			if (!connIdentifierPassed) {
				LOGGER.info("No connection identifier passed. "
						+ "Getting connection from AFT_LastDBConnection");
				connIdentifier = Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_LASTDBCONNECTION);
			} else {
				LOGGER.info("User passed identifier [" + connIdentifier
						+ "] will be used to execute Stored procedure");
			}

			// get the stored value of identifier
			dbInstanceIdentifier = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), connIdentifier);

			// if identifier does not exists
			if (dbInstanceIdentifier == null
					|| dbInstanceIdentifier.isEmpty()
					|| !DatabaseInstanceManager.getInstance()
							.checkDBInstanceExists(dbInstanceIdentifier)) {
				errorMessage = "No open DB connection is available";
				// log the error message and throw the exception.
				logException(testStepRunner);
			}

			// get the database instance
			DatabaseInstance dbInstance = DatabaseInstanceManager.getInstance()
					.getDBInstance(dbInstanceIdentifier);

			// split the input parameters
			if (parameters != null && !parameters.isEmpty()) {
				// if more than one parameter passed
				if (parameters != null && !parameters.isEmpty()) {
					if (parameters
							.contains(Constants.DBFIXTURESTOREDPROCINPUTPARAMLISTDELIMITER)) {
						parametersArray = parameters
								.split("\\"
										+ Constants.DBFIXTURESTOREDPROCINPUTPARAMLISTDELIMITER);
					} else {
						parametersArray = new String[1];
						parametersArray[0] = parameters;
					}
				}

				if (parametersArray != null) {
					inputParamCount = parametersArray.length;
				}
				// Setting parameters
				for (int i = 0; i < (inputParamCount); i++) {
					strParam = strParam + "?";
					if (i != (inputParamCount) - 1) {
						strParam = strParam + ",";
					}
				}
			}

			// construct the call string
			if (strParam != null && !strParam.isEmpty()) {
				callStr = "{call " + storedProcName + "(" + strParam + ")}";
			} else {
				callStr = "{call " + storedProcName + "()}";
			}

			LOGGER.debug("Call string [" + callStr + "]");

			if (dbInstance.getConnectionObject().isClosed()) {
				errorMessage = "Cannot execute the stored procedure as DB connection is closed. "
						+ "Please open a new connection and execute the procedure";
				// log the error message and throw the exception.
				logException(testStepRunner);
			}

			objCallable = dbInstance.getConnectionObject().prepareCall(callStr);
			int parameterIndex = 0;

			// passing input(in) parameters
			if (inputParamCount > 0) {

				String currInputParam = null;
				String[] currParamArray = null;
				String currDataType = null;
				String currDataValue = null;
				String currParamType = null;

				for (int loopCounter = 0; loopCounter < inputParamCount; loopCounter++) {
					currInputParam = parametersArray[loopCounter];
					currParamArray = currInputParam
							.trim()
							.split("\\"
									+ Constants.DBFIXTURESTOREDPROCDATATYPENVALUEDELIMITER);

					if (currParamArray.length == 3) {
						currDataType = currParamArray[0];
						currDataValue = currParamArray[1];
						currParamType = currParamArray[2];
					} else if (currParamArray.length == 2) {
						currDataType = currParamArray[0];
						currParamType = currParamArray[1];
					} else {
						// Throwing exception in case parameter data type and
						// parameter type not passed
						errorMessage = "Paramater data type and parameter type (in, out and inout) are mandatory";
						LOGGER.error(errorMessage);
						throw new AFTException(errorMessage);
					}

					// get the stored value of identifier
					currDataType = Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(), currDataType);
					if (currDataValue != null) {
						currDataValue = Helper.getInstance().getActionValue(
								testStepRunner.getTestSuiteRunner(),
								currDataValue);
					}
					currParamType = Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(), currParamType);

					LOGGER.trace("datatype = [" + currDataType
							+ "], datavalue = [" + currDataValue
							+ "], paramtype = [" + currParamType + "]");

					parameterIndex++;

					// Get the field value using Reflection
					Class<?> sqlTypeClass = Class.forName("java.sql.Types");
					if (currDataType.toLowerCase().startsWith("varchar2")
							|| currDataType.toLowerCase().startsWith("varchar")) {
						LOGGER.info("User has passed datatype as ["
								+ currDataType + "]");
						currDataType = "varchar".toUpperCase();
					} else if (currDataType.toLowerCase().startsWith("int")
							|| currDataType.toLowerCase().startsWith("number")) {
						LOGGER.info("User has passed datatype as ["
								+ currDataType + "]");
						currDataType = "integer".toUpperCase();
					} else if (currDataType.toLowerCase().startsWith("short")
							|| currDataType.toLowerCase().startsWith("byte")) {
						LOGGER.info("User has passed datatype as ["
								+ currDataType + "]");
						currDataType = "smallint".toUpperCase();
					} else {
						LOGGER.error("Error::Datatype ["
								+ currDataType
								+ "] is currently not supported by executeStoredProc.");
						throw new AFTException(
								"Datatype ["
										+ currDataType
										+ "] is currently not supported by executeStoredProc.");
					}
					int priFieldType = Integer.parseInt(sqlTypeClass
							.getDeclaredField(currDataType).get(currDataType)
							.toString());
					setParameterToStatement(objCallable, parameterIndex,
							currDataValue, priFieldType, currParamType);

				}
			}

			// execute callable statement
			executeResult = objCallable.execute();

			LOGGER.info("Stored procedure [" + storedProcName
					+ "] has been executed successfully");

			if (executeResult) {
				objResultSet = objCallable.getResultSet();
				// If cursor is before first row
				if (objResultSet.isBeforeFirst()) {
					objResultSet.next();
				}
				returnValue = objResultSet.getString(1);

			} else {
				returnValue = Integer.toString(objCallable.getUpdateCount());
			}

			if (returnVariable != null && !returnVariable.isEmpty()) {
				LOGGER.debug("Storing execution result [" + returnValue
						+ "] in user passed return variable [" + returnVariable
						+ "]");
				Variable.getInstance()
						.setVariableValue(testStepRunner.getTestSuiteRunner(),
								"executeStoredProc", returnVariable, false,
								returnValue);
			} else {
				LOGGER.info("No return variable passed by user");
			}

			// store the result in AFT_LastDBResult by default
			LOGGER.info("Storing exection result [" + returnValue
					+ "] in system variable [AFT_LastDBResult] by default");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTDBRESULT), true,
					returnValue);
		} catch (DataTruncation d) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.warn("Data Truncation Exception::", d);
			throw new AFTException(d);
		} catch (SQLWarning sw) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.warn("Sql Warnings Exception::", sw);
			throw new AFTException(sw);
		} catch (SQLException s) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("SQL Exception::", s);
			throw new AFTException(s);
		} catch (Exception e) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			try {
				if (objResultSet != null) {
					objResultSet.close();
				}
				if (objCallable != null) {
					objCallable.close();
				}
			} catch (SQLException e) {
				// set onDbErrorValue
				setOnDbErrorValue(testStepRunner);

				LOGGER.warn("Exception while closing Callable/ResultSet object");
				throw new AFTException(e);
			}
		}

		return returnValue;
	}

	/**
	 * Setting input parameters based on parameter type (in, out or inout)
	 * 
	 * @param callableStatement
	 *            callableStatement
	 * @param index
	 *            index
	 * @param value
	 *            value
	 * @param dataType
	 *            dataType
	 * @param paramType
	 *            paramType
	 * @throws SQLException
	 */
	private void setParameterToStatement(CallableStatement callableStatement,
			int index, String value, int dataType, String paramType)
			throws SQLException {
		LOGGER.info("index = [" + index + "], dataType = [" + dataType
				+ "], value = [" + value + "], paramType = [" + paramType + "]");

		if (paramType.equalsIgnoreCase("in")
				|| paramType.equalsIgnoreCase("inout")) {
			if (value != null) {
				callableStatement.setString(index, value);
			} else {
				callableStatement.setNull(index, dataType);
			}
		} else if (paramType.equalsIgnoreCase("out")) {
			callableStatement.registerOutParameter(index, dataType);
		}
		if (paramType.equalsIgnoreCase("inout")) {
			callableStatement.registerOutParameter(index, dataType);
		}
	}

	/**
	 * Executes the given Database job with given connection identifier. Stores
	 * the execution result in returnVariable(if any) and AFT_LastDBResult
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable in which to store result
	 * 
	 * @param parameterList
	 *            parameter list consisting of connection identifier,job name
	 *            and time out
	 * 
	 * @return Execution status
	 * @throws AFTException
	 * 
	 */
	public String executeDBCommand(TestStepRunner testStepRunner,
			String returnVariable, String parameterList) throws AFTException {

		String connIdentifier = null;
		String jobName = null;
		String timeoutMilliSecs = null;
		String dbInstanceIdentifier = null;
		boolean timeoutPassed = true;
		boolean connIdentifierPassed = true;

		LOGGER.trace("Executing [executeDBCommand] for parameters ["
				+ parameterList + "]");

		// if user has not passed any parameter
		if (parameterList == null || parameterList.isEmpty()) {
			errorMessage = "No parameters passed. "
					+ "Please refer to wiki on how to use [executeDBCommand]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// if user has not provided stored procedure
		if (!parameterList.toLowerCase().contains("jobname")) {
			errorMessage = "Parameters does not include database Job name. "
					+ "Please refer to wiki on how to use [executeDBCommand]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		String[] jobNameArray = null;
		String[] timeoutArray = null;
		String strEqualSign = "=";
		String returnValue = null;

		try {
			// if user has not provided timeout
			if (!parameterList.toLowerCase().contains("timeout")) {
				timeoutPassed = false;
			}

			String[] paramArray = parameterList
					.split(Constants.DBFIXTUREPARAMDELIMITER);

			// check if user has passed an identifier
			if (paramArray.length == 3) {
				connIdentifier = paramArray[0];
				jobNameArray = paramArray[1].split(strEqualSign);
				timeoutArray = paramArray[2].split(strEqualSign);
			} else if (paramArray.length < 3 && timeoutPassed) {
				jobNameArray = paramArray[0].split(strEqualSign);
				timeoutArray = paramArray[1].split(strEqualSign);
				connIdentifierPassed = false;
			} else if (paramArray.length > 1 && paramArray.length < 3
					&& !timeoutPassed) {
				connIdentifier = paramArray[0];
				jobNameArray = paramArray[1].split(strEqualSign);
			} else {
				jobNameArray = paramArray[0].split(strEqualSign);
				connIdentifierPassed = false;
			}

			// get job name
			jobName = jobNameArray[1];

			// get timeout value
			if (timeoutPassed) {
				timeoutMilliSecs = timeoutArray[1];
			} else {
				LOGGER.info("No timeout passed. "
						+ "Using Default execution timeout");
				timeoutMilliSecs = Constants.DEFAULTDBFIXTURETIMEOUT;
			}

			LOGGER.trace("Identifer = [" + connIdentifier + "], storedProc = ["
					+ jobName + "] timeout = [" + timeoutMilliSecs + "]");

			// if user has not passed an identifier
			if (!connIdentifierPassed) {
				LOGGER.info("No connection identifier passed. "
						+ "Getting connection from AFT_LastDBConnection");

				connIdentifier = Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_LASTDBCONNECTION);
			} else {
				LOGGER.info("User passed identifier [" + connIdentifier
						+ "] will be used to execute Job");
			}

			// get the stored value of identifier
			dbInstanceIdentifier = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), connIdentifier);

			// if identifier does not exists
			if (dbInstanceIdentifier == null
					|| dbInstanceIdentifier.isEmpty()
					|| !DatabaseInstanceManager.getInstance()
							.checkDBInstanceExists(dbInstanceIdentifier)) {
				errorMessage = "No open DB connection is available";
				// log the error message and throw the exception.
				logException(testStepRunner);
			}

			// get the database instance
			DatabaseInstance dbInstance = DatabaseInstanceManager.getInstance()
					.getDBInstance(dbInstanceIdentifier);

			if (dbInstance.getConnectionObject().isClosed()) {
				errorMessage = "Cannot execute the job as DB connection is closed. "
						+ "Please open a new connection and execute the job";
				// log the error message and throw the exception.
				logException(testStepRunner);
			}

			// convert timeout to integer in secs
			int timeoutSecs = Integer.parseInt(timeoutMilliSecs) / 1000;

			// get the database type
			String dbType = dbInstance.getDatabaseType();

			int jobStatus = 1;

			if (dbType.equalsIgnoreCase("mssql")) {
				jobStatus = executeMSSqlCommand(
						dbInstance.getConnectionObject(), jobName, timeoutSecs);
			} else if (dbType.equalsIgnoreCase("oracle")) {
				jobStatus = executeOracleCommand(
						dbInstance.getConnectionObject(), jobName, timeoutSecs);
			} else {
				errorMessage = "This database type is currently not supported by [executeDBCommand]";
				LOGGER.error(errorMessage);
				throw new AFTException(errorMessage);
			}

			if (jobStatus == 1) {
				returnValue = "PASSED"; // PASSED
			} else if (jobStatus == 0) {
				returnValue = "FAILED"; // FAILED
			} else if (jobStatus == 3) {
				returnValue = "CANCELLED"; // CANCELLED
			} else if (jobStatus == 4 || timeoutSecs <= 0) {
				returnValue = "TIMEDOUT"; // TIMEDOUT
			}

			if (returnVariable != null && !returnVariable.isEmpty()) {
				LOGGER.info("Storing execution result [" + returnValue
						+ "] in user passed return variable [" + returnVariable
						+ "]");
				Variable.getInstance()
						.setVariableValue(testStepRunner.getTestSuiteRunner(),
								"executeStoredProc", returnVariable, false,
								returnValue);
			} else {
				LOGGER.info("No return variable passed by user");
			}

			// store the result in AFT_LastDBResult by default
			LOGGER.info("Storing exection result [" + returnValue
					+ "] in system variable [AFT_LastDBResult] by default");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTDBRESULT), true,
					returnValue);
		} catch (SQLException se) {
			// set onDbErrorValue
			setOnDbErrorValue(testStepRunner);

			LOGGER.error("Exception::", se);
			throw new AFTException(se);
		}

		return returnValue;
	}

	/**
	 * Executes the MSSql database command and returns the job status
	 * 
	 * @param objConnection
	 *            - Connection object
	 * @param jobName
	 *            - User passed job name
	 * @param timeoutSecs
	 *            - Time out in seconds
	 * @return jobStatus - Job execution status
	 * @throws AFTException
	 * @throws SQLException
	 */
	private int executeMSSqlCommand(Connection objConnection, String jobName,
			int timeoutSecs) throws AFTException, SQLException {

		int jobStatus = 0;
		PreparedStatement objPrepStmt = null;
		Statement objStmt = null;
		ResultSet objectResultSet = null;

		try {

			// construct the execute statement string
			String stmtStr = "EXEC msdb.dbo.sp_start_job" + " '" + jobName
					+ "'";

			LOGGER.trace("Statement string [" + stmtStr + "]");

			// create the PreparedStatement object
			objPrepStmt = objConnection.prepareStatement(stmtStr);

			// execute the job
			objPrepStmt.execute();

			// create the Statement object
			objStmt = objConnection.createStatement();

			// construct the job status query string
			String strStatusQuery = "SELECT current_execution_status FROM OPENROWSET"
					+ "('SQLNCLI', 'Trusted_Connection=yes;',"
					+ "'EXEC MSDB.dbo.sp_help_job @job_name = ''"
					+ jobName
					+ "'', @job_aspect = ''JOB''')";

			LOGGER.trace("Status query string [" + strStatusQuery + "]");

			// execute the query
			objectResultSet = objStmt.executeQuery(strStatusQuery);
			objectResultSet.first();

			// get the job status value
			jobStatus = objectResultSet.getInt(1);

			int timeOutSeconds = timeoutSecs;

			// execute the query till either timeout(secs) lapses or job is
			// timed
			// out
			if (jobStatus != 4) {
				while (timeOutSeconds > 0 && jobStatus != 4) {
					objectResultSet = objStmt.executeQuery(strStatusQuery);
					objectResultSet.first();
					jobStatus = objectResultSet.getInt(1);
					timeOutSeconds = timeOutSeconds - 1;
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (!objPrepStmt.isClosed()) {
				objPrepStmt.close();
			}
			if (!objStmt.isClosed()) {
				objStmt.close();
			}
			if (!objectResultSet.isClosed()) {
				objectResultSet.close();
			}
		}

		return jobStatus;
	}

	/**
	 * Executes the Oracle database command and returns the job status
	 * 
	 * @param objConnection
	 *            Connection object
	 * @param jobName
	 *            User passed job name
	 * @param timeoutSecs
	 *            Time out in seconds
	 * @return jobStatus - Job execution status
	 * @throws AFTException
	 * @throws SQLException
	 */
	private int executeOracleCommand(Connection objConnection, String jobName,
			int timeoutSecs) throws AFTException, SQLException {

		int jobStatus = 0;
		PreparedStatement objPrepStmt = null;
		Statement objStmt = null;
		ResultSet objectResultSet = null;

		try {

			// construct the execute statement string
			String stmtStr = "EXEC dbms_job.run(" + jobName + ")";

			LOGGER.trace("Statement string [" + stmtStr + "]");

			// create the PreparedStatement object
			objPrepStmt = objConnection.prepareStatement(stmtStr);

			// execute the job
			objPrepStmt.execute();

			// create the Statement object
			objStmt = objConnection.createStatement();

			// construct the job status query string
			String strStatusQuery = "";

			LOGGER.trace("Status query string [" + strStatusQuery + "]");

			// execute the query
			objectResultSet = objStmt.executeQuery(strStatusQuery);
			objectResultSet.first();

			// get the job status value
			jobStatus = objectResultSet.getInt(1);

			int timeOutSeconds = timeoutSecs;

			// execute the query till either timeout(secs) lapses or job is
			// timed
			// out
			if (jobStatus != 4) {
				while (timeOutSeconds > 0 && jobStatus != 4) {
					objectResultSet = objStmt.executeQuery(strStatusQuery);
					objectResultSet.first();
					jobStatus = objectResultSet.getInt(1);
					timeOutSeconds = timeOutSeconds - 1;
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (!objPrepStmt.isClosed()) {
				objPrepStmt.close();
			}
			if (!objStmt.isClosed()) {
				objStmt.close();
			}
			if (!objectResultSet.isClosed()) {
				objectResultSet.close();
			}
		}

		return jobStatus;
	}

	/**
	 * Creates a connection string for a DSN
	 * 
	 * @param paramArray
	 *            - Array of user passed parameters
	 * @return connString - Connection string
	 * @throws AFTException
	 */
	private String createDSNConnString(String[] paramArray) throws AFTException {

		LOGGER.debug("User passed DSN without DB type");

		String connString = null;

		String[] connPropertyNames = new String[] { "dsn", "uid", "pwd" };

		String paramList = Arrays.asList(paramArray).toString();

		// if user has not provided name of dsn
		if (!paramList.toLowerCase().contains("dsn")) {
			errorMessage = "Parameters does not include DSN name. "
					+ "Please refer to wiki FAQ on how to create connection string for database wtih DSN";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		Map<String, String> connPropertyValues = new HashMap<String, String>();

		connPropertyValues = extractPropertyValues(paramArray,
				connPropertyNames);

		connString = "jdbc:odbc:" + connPropertyValues.get("dsn");

		if (connPropertyValues.containsKey("uid")) {

			connUidPwdProp.put("uid", connPropertyValues.get("uid"));

			connUidPwdProp.put("pwd", connPropertyValues.get("pwd"));

		}

		connPropertyValues.clear();

		return connString;
	}

	/**
	 * Creates a connection string for a MS SQL database
	 * 
	 * @param paramArray
	 *            - Array of user passed parameters
	 * @return connString - Connection string
	 * @throws AFTException
	 */
	private String createMSSqlConnString(String[] paramArray)
			throws AFTException {

		LOGGER.debug("User passed MsSql DB type");

		String connString = null;

		String[] connPropertyNames = new String[] { "host", "port", "database",
				"uid", "pwd" };

		String paramList = Arrays.asList(paramArray).toString();

		// if user has not provide name of a database on sql server
		if (!paramList.toLowerCase().contains("database")) {
			errorMessage = "Parameters does not include database name. "
					+ "Please refer to wiki FAQ on how to create connection string for MS Sql database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		Map<String, String> connPropertyValues = new HashMap<String, String>();

		connPropertyValues = extractPropertyValues(paramArray,
				connPropertyNames);

		connString = "jdbc:sqlserver://";

		if (connPropertyValues.containsKey("host")) {
			connString = connString + connPropertyValues.get("host");
		} else {
			connString = connString + "localhost";
		}

		if (connPropertyValues.containsKey("port")) {
			connString = connString + ":" + connPropertyValues.get("port")
					+ ";";
		} else {
			connString = connString + ";";
		}

		if (connPropertyValues.containsKey("database")) {
			connString = connString + "database="
					+ connPropertyValues.get("database") + ";";
		}

		if (connPropertyValues.containsKey("uid")) {

			connString = connString + "user=" + connPropertyValues.get("uid")
					+ ";";
			connUidPwdProp.setProperty("user", connPropertyValues.get("uid"));

			connString = connString + "password="
					+ connPropertyValues.get("pwd");
			connUidPwdProp.setProperty("pwd", connPropertyValues.get("pwd"));

		}

		connPropertyValues.clear();

		return connString;
	}

	/**
	 * Creates a connection string for a MS ACCESS database
	 * 
	 * @param paramArray
	 *            - Array of user passed parameters
	 * @return connString - Connection string
	 * @throws AFTException
	 */
	private String createAccessConnString(String[] paramArray)
			throws AFTException {

		LOGGER.debug("User passed MS Access file DB");

		String connString, connDBQ = null;

		String[] connPropertyNames = new String[] { "dbq" };

		String paramList = Arrays.asList(paramArray).toString();

		// if user has not provide name of a database on sql server
		if (!paramList.toLowerCase().contains("dbq")) {
			errorMessage = "Parameters does not include database file name. "
					+ "Please refer to wiki FAQ on how to create connection string for Access database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		Map<String, String> connPropertyValues = new HashMap<String, String>();

		connPropertyValues = extractPropertyValues(paramArray,
				connPropertyNames);

		connDBQ = connPropertyValues.get("dbq");

		String parsedFilePath = connDBQ.replace("\\", "/");

		connString = "jdbc:odbc:"
				+ "Driver=Microsoft Access Driver (*.mdb, *.accdb);" + "DBQ="
				+ parsedFilePath + ";";

		if (connPropertyValues.containsKey("uid")) {

			connUidPwdProp.put("uid", connPropertyValues.get("uid"));

			connUidPwdProp.put("pwd", connPropertyValues.get("pwd"));
		}

		connPropertyValues.clear();

		return connString;
	}

	/**
	 * Creates a connection string for a MY SQL database
	 * 
	 * @param paramArray
	 *            - Array of user passed parameters
	 * @return connString - Connection string
	 * @throws AFTException
	 */
	private String createMySqlConnString(String[] paramArray)
			throws AFTException {

		LOGGER.debug("User passed MySql DB type");

		String connString = null;

		String[] connPropertyNames = new String[] { "host", "port", "database",
				"uid", "pwd" };

		String paramList = Arrays.asList(paramArray).toString();

		// if user has not provide name of a database on sql server
		if (!paramList.toLowerCase().contains("database")) {
			errorMessage = "Parameters does not include database name. "
					+ "Please refer to wiki FAQ on how to create connection string for MySql database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		Map<String, String> connPropertyValues = new HashMap<String, String>();

		connPropertyValues = extractPropertyValues(paramArray,
				connPropertyNames);

		connString = "jdbc:mysql:" + "//";

		if (connPropertyValues.containsKey("host")) {
			connString = connString + connPropertyValues.get("host");
		} else {
			connString = connString + "localhost";
		}

		if (connPropertyValues.containsKey("port")) {
			connString = connString + ":" + connPropertyValues.get("port")
					+ "/";
		} else {
			connString = connString + "/";
		}

		if (connPropertyValues.containsKey("database")) {
			connString = connString + connPropertyValues.get("database");
		}

		if (connPropertyValues.containsKey("uid")) {
			connString = connString + "?" + "user="
					+ connPropertyValues.get("uid");
		}

		if (connPropertyValues.containsKey("pwd")) {
			connString = connString + "&" + "password="
					+ connPropertyValues.get("pwd");
		}

		connPropertyValues.clear();

		return connString;
	}

	/**
	 * Creates a connection string for a ORACLE database
	 * 
	 * @param paramArray
	 *            - Array of user passed parameters
	 * @return connString - Connection string
	 * @throws AFTException
	 */
	private String createOracleConnString(String[] paramArray)
			throws AFTException {

		LOGGER.debug("User passed Oracle DB type");

		String connString = null;

		String[] connPropertyNames = new String[] { "uid", "pwd", "host",
				"port", "sid" };

		String paramList = Arrays.asList(paramArray).toString();

		// if user has not provided SID
		if (!paramList.toLowerCase().contains("sid")) {
			errorMessage = "Parameter does not include SID. "
					+ "Please refer to wiki FAQ on how to create connection string for Oracle database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// if user has not provided UID
		if (!paramList.toLowerCase().contains("uid")) {
			errorMessage = "Parameter does not include Username. "
					+ "Please refer to wiki FAQ on how to create connection string for Oracle database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// if user has not provided PWD
		if (!paramList.toLowerCase().contains("pwd")) {
			errorMessage = "Parameter does not include Password. "
					+ "Please refer to wiki FAQ on how to create connection string for Oracle database";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		Map<String, String> connPropertyValues = new HashMap<String, String>();

		connPropertyValues = extractPropertyValues(paramArray,
				connPropertyNames);

		connString = "jdbc:oracle:thin:@";

		if (connPropertyValues.containsKey("host")) {
			connString = connString + connPropertyValues.get("host");
		}

		if (connPropertyValues.containsKey("port")) {
			connString = connString + ":" + connPropertyValues.get("port");
		}

		if (connPropertyValues.containsKey("sid")) {
			connString = connString + ":" + connPropertyValues.get("sid");
		}

		if (connPropertyValues.containsKey("uid")) {

			connUidPwdProp.put("uid", connPropertyValues.get("uid"));

			connUidPwdProp.put("pwd", connPropertyValues.get("pwd"));
		}

		connPropertyValues.clear();

		return connString;
	}

	/**
	 * Extracts the values from parameters for given properties and returns a
	 * Map of <Property,Value>
	 * 
	 * @param paramArray
	 *            - Array of parameters passed by user
	 * @param connPropertyNames
	 *            - Array of properties required for a connection
	 * @return Map<String,String>
	 */
	private Map<String, String> extractPropertyValues(String[] paramArray,
			String[] connPropertyNames) {

		Map<String, String> connPropValues = new HashMap<String, String>();
		String[] temp = null;
		String userPassedValue = null;

		for (int i = 0; i < paramArray.length; i++) {
			temp = paramArray[i].split("=");
			for (int j = 0; j < connPropertyNames.length; j++) {
				if (temp[0].equalsIgnoreCase(connPropertyNames[j])) {
					if (temp.length < 2) {
						userPassedValue = "";
					} else {
						userPassedValue = temp[1];
					}
					connPropValues.put(connPropertyNames[j], userPassedValue);
				}
			}
		}

		return connPropValues;
	}

	/**
	 * When specific database connection is lost, this method will attempt to
	 * re-establish the connection once.Again if it fails then throws exception.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @return dbInstanceIdentifier
	 * @throws AFTException
	 */
	private String retryDbConnection(TestStepRunner testStepRunner)
			throws AFTException {
		String dbInstanceIdentifier = null;
		int counter = testStepRunner.getTestSuiteRunner().getDbConnCounter();
		if (counter == 0) {
			LOGGER.info("Open new DB connection.");
			dbInstanceIdentifier = openDBConnection(testStepRunner,
					testStepRunner.getTestSuiteRunner().getDbConnIdentifier(),
					testStepRunner.getTestSuiteRunner().getDbParameterLst());
			counter++;
			testStepRunner.getTestSuiteRunner().setDbConnCounter(counter);
		} else {
			logException(testStepRunner);
		}

		return dbInstanceIdentifier;
	}

	/**
	 * This method will log the exception and throws the exception.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 */
	private void logException(TestStepRunner testStepRunner)
			throws AFTException {
		LOGGER.error(errorMessage);

		String onDbErrorValue = setOnDbErrorValue(testStepRunner);

		LOGGER.info("User has defined '"
				+ ConfigProperties.ONERROR_DB_CONNECTION
				+ "' to ["
				+ onDbErrorValue
				+ "]. This test case execution will be stopped and test execution will continue as per '"
				+ ConfigProperties.ONERROR_DB_CONNECTION + "' configuration.");
		throw new AFTException(errorMessage);
	}

	/**
	 * This method will set onDbErrorValue based on the config parameter value
	 * ONERROR_DB_CONNECTION
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @return onDbErrorValue
	 * @throws AFTException
	 */
	private String setOnDbErrorValue(TestStepRunner testStepRunner)
			throws AFTException {
		String onDbErrorValue = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.ONERROR_DB_CONNECTION);
		if (onDbErrorValue == null || onDbErrorValue.equals("")) {
			onDbErrorValue = ConfigProperties.DEFAULT_ONERROR;
		}
		testStepRunner.getTestSuiteRunner().setOnDBErrorValue(onDbErrorValue);
		return onDbErrorValue;
	}

	/**
	 * Returns a String array containing the data of the ResultSet instance.
	 * 
	 * @param objResultSet
	 *            objResultSet
	 * @return allValues
	 */
	public String[][] convertResultSetToArray(ResultSet objResultSet)
			throws AFTException {

		String[][] allValues = new String[maxRows][maxColumns];
		int rowNum = 0;

		try {

			ResultSetMetaData rsmd = objResultSet.getMetaData();

			int colCount = rsmd.getColumnCount();

			// Lets get the column names in first row
			for (int i = 0; i < colCount; i++) {
				allValues[rowNum][i] = rsmd.getColumnName(i + 1);
			}

			rowNum++;

			// If cursor is not at first row
			if (!objResultSet.isFirst()) {
				while (objResultSet.next()) {

					// cannot store more than maxRows rows
					if (rowNum >= maxRows) {
						LOGGER.warn("Only the first " + maxRows
								+ " rows data can be stored.");
						return allValues;
					}

					for (int col = 0; col < colCount; col++) {
						// cannot store more than maxColumns columns
						if (col >= maxColumns) {
							LOGGER.warn("Only the first " + maxColumns
									+ " columns data can be stored.");
							return allValues;
						}

						allValues[rowNum][col] = objResultSet
								.getString(col + 1);
					}

					rowNum++;
				}
			}

		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new AFTException(e);
		}

		resultsetRowCount = rowNum - 1;

		return allValues;
	}

}