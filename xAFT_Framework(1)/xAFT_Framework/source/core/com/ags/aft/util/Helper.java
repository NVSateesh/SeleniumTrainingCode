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
 * Class: Helper
 * 
 * Purpose: This class contains utility methods to read configuration files,
 * validate test data header and send email notifications etc
 */

package com.ags.aft.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.PageErrors;
import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.Reporting.TCMReportGenerator;
import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.RuntimeProperties;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.fixtures.database.DatabaseInstanceManager;
import com.ags.aft.fixtures.externalScript.ScriptInstanceManager;
import com.ags.aft.fixtures.sikuli.SikuliObjectManager;
import com.ags.aft.integrations.TCMIntegration;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.runners.TestBatchRunner;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.runners.TestSuiteRunner;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;

/**
 * The Class AftHelper.
 */
public final class Helper {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	/** The aft helper. */
	private static Helper aftHelper;

	// If any of specified file not found then below flag sets to false.
	/** The all framework file found. */
	private boolean allFrameworkFileFound = true;

	// to check whether the rquest is coming from file system or not.
	/** The is file system request. */
	private boolean isFileSystemRequest = true;

	// to check whether the last action is a sikuli action
	/** The is sikuli called. */
	private boolean isSikuliCalled = false;


	/**
	 * Instantiates a new aft helper.
	 */
	private Helper() {
		super();
	}

	/**
	 * Gets the single instance of AftHelper.
	 * 
	 * @return single instance of AftHelper
	 */
	public static Helper getInstance() {
		if (aftHelper == null) {
			aftHelper = new Helper();
			LOGGER.trace("Creating instance of AftHelper");
		}

		return aftHelper;
	}

	/**
	 * Overloaded function without test runner object. Implemented for backward
	 * support
	 * 
	 * @param actionValue
	 *            the action value string in which to search for variables and
	 *            test data placeholders and replace them with the actual values
	 * @return Modified action value string with values substituted for
	 *         variables and test data
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getActionValue(String actionValue) throws AFTException {
		return getActionValue(null, actionValue);
	}

	/**
	 * Parses action value string to substitute variables and test data
	 * placeholders with their actual value. For variables current runtime value
	 * is replaced in the action value string
	 * 
	 * @param testSuiteRunner
	 *            Test Suite runner object. Used for retrieving test data value
	 * @param actionValue
	 *            the action value string in which to search for variables and
	 *            test data placeholders and replace them with the actual values
	 * @return Modified action value string with values substituted for
	 *         variables and test data
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getActionValue(TestSuiteRunner testSuiteRunner,
			String actionValue) throws AFTException {
		boolean continueVariableSearch = true;
		String variableName = "";
		String testDataHeader = "";
		String newValue = "";

		char[] staticParameterStartDelimiter = Constants.STATICPARAMETERSTARTIDENTIFIER
				.toCharArray();
		char[] staticParameterEndDelimiter = Constants.STATICPARAMETERENDIDENTIFIER
				.toCharArray();

		char[] variableDelimiterArr = Constants.DYNAMICVARIABLEDELIMITER
				.toCharArray();

		char[] testDataStartDelimiterArr = Constants.TESTDATASTARTVARIABLEIDENTIFIER
				.toCharArray();
		char[] testDataEndDelimiterArr = Constants.TESTDATAENDVARIABLEIDENTIFIER
				.toCharArray();
		char[] variableEscapeCharDelimiterArr = Constants.DYNAMICVARIABLEESCAPECHARDELIMITER
				.toCharArray();

		char[] escapeCharDelimiterArr = Constants.ESCAPECHARDELIMITERS
				.toCharArray();

		try {
			LOGGER.trace("Looking for variables/test data in action value string ["
					+ actionValue + "] and substituting with values");

			if (actionValue.length() > 0) {
				int searchPosition = 0;
				newValue = actionValue;

				do {
					if (newValue.charAt(searchPosition) == variableDelimiterArr[0]
							&& (searchPosition == 0
									|| (newValue.charAt(searchPosition - 1) == variableEscapeCharDelimiterArr[0] && newValue
											.charAt(searchPosition - 2) == variableEscapeCharDelimiterArr[0]) || (newValue
									.charAt(searchPosition - 1) != variableEscapeCharDelimiterArr[0]))) {
						LOGGER.trace("Looks like user specified a variable. Let us look for the variable name to substitute with it's value");

						String searchString = newValue.substring(searchPosition
								+ variableDelimiterArr.length);
						boolean variableFound = false;
						boolean error = false;

						for (int loopItrCnt = 0; loopItrCnt <= (searchString
								.length() - 1); loopItrCnt++) {
							if (searchString.charAt(loopItrCnt) == variableDelimiterArr[0]) {
								variableFound = true;

								variableName = searchString.substring(0,
										loopItrCnt);

								break;
							}
						}

						if (!Variable.getInstance().isArrayVariable(
								variableName)
								&& !Variable.getInstance().isValidVariableName(
										variableName)) {
							LOGGER.error("Variable name ["
									+ variableName
									+ "] specified looks invalid. Please check the syntax in test suite.");

							error = true;
						}

						else {
							LOGGER.trace("Found a valid variable ["
									+ variableName
									+ "]. Getting the current variable value and substituting in the action value string.");

							newValue = Variable.getInstance()
									.substituteVariableValue(newValue,
											variableName,
											variableDelimiterArr[0]);

							LOGGER.trace("Action value string after substituting the variable value in ["
									+ variableName + "] is [" + newValue + "]");

							// since we substituted the variable value, the
							// string length may have changed. Let us start
							// looking for variable from beginning
							//
							// We are setting it to -1 as the searchPosition
							// value is incremented below before comparing
							// is starting the loop all over again
							searchPosition = -1;
						}

						if ((!variableFound) && (!error)) {
							LOGGER.error("Termination character not found for variable start with characters ["
									+ variableName
									+ "]. Please check the syntax in test suite.");

							// Since the variable termination not found, this
							// could lead to incorrect results. break from this
							// loop and return from method...
							continueVariableSearch = false;
						}
					} else if ((testSuiteRunner != null)
							&& ((searchPosition + 1) < newValue.length())
							&& ((newValue.charAt(searchPosition) == testDataStartDelimiterArr[0]) && (newValue
									.charAt(searchPosition + 1) == testDataStartDelimiterArr[1]))) {
						LOGGER.trace("Looks like user specified a test data header. Let us look for the test data header to substitute for it's value");

						String searchString = newValue.substring(searchPosition
								+ testDataStartDelimiterArr.length);
						boolean testDataFound = false;
						boolean error = false;

						for (int loopItrCnt = 0; loopItrCnt <= (searchString
								.length() - 1); loopItrCnt++) {
							if ((searchString.charAt(loopItrCnt) == testDataEndDelimiterArr[0])
									&& (searchString.charAt(loopItrCnt + 1) == testDataEndDelimiterArr[1])) {
								testDataFound = true;

								testDataHeader = searchString.substring(0,
										loopItrCnt);

								break;
							}
						}

						if (!validateTestDataHeader(testDataHeader)) {
							LOGGER.error("Test data header ["
									+ testDataHeader
									+ "] specified looks invalid. Please check the syntax in test suite.");

							error = true;
						} else {
							LOGGER.debug("Found a valid Test data header ["
									+ testDataHeader
									+ "]. Getting the test data and substituting in the action value string.");

							// Construct the variable name...

							String value = testDataHeader.replace(".", "~");
							String[] valueList = value.split("~");

							// If the sheet name or header name contains space,
							// user will use single quote to enclose the name.
							// Before we pass it to testdata reader object, we
							// need to parse it out...
							//
							for (int i = 0; i < valueList.length; i++) {
								if ((valueList[i].charAt(0) == '\'')
										&& (valueList[i].charAt(valueList[i]
												.length() - 1) == '\'')) {
									valueList[i] = valueList[i].substring(1,
											valueList[i].length() - 1);
								}
							}

							// It is quite possible that user may specify the
							// sheet name and column name as variable. Let us
							// parse them to extract actual value...
							String sheetName = getActionValue(testSuiteRunner,
									valueList[0]).toLowerCase();
							String columnHeader = getActionValue(
									testSuiteRunner, valueList[1]);

							int testDataRowId = -1;
							if (valueList.length == 3) {
								// looks like user specified the rowid along
								// with test data identifier, let us use it
								// instead
								String strRowId = getActionValue(
										testSuiteRunner, valueList[2]);
								LOGGER.debug("user has specified test data row id ["
										+ strRowId
										+ "] along with test data identified. Using the specified rowid");
								testDataRowId = Integer.parseInt(strRowId);
							} else {
								// oops, user did not specify the rowid along
								// with test data identifier, let us pick the
								// rowid from the one maintained by system
								testDataRowId = testSuiteRunner
										.getTestDataCurrentRowId(sheetName);
								LOGGER.debug("Test Data RowId is ["
										+ testDataRowId + "]");
							}
							String testDataValue = null;
							if (!isFileSystemRequest) {
								LOGGER.debug("Sheet name is [" + sheetName
										+ "] and column header is ["
										+ columnHeader + "]");
								String projectId = RuntimeProperties.getInstance()
										.getProjectId();
								String name = sheetName.toLowerCase();
								name = new StringBuffer(name.length())
										.append(Character.toTitleCase(name
												.charAt(0)))
										.append(name.substring(1)).toString();
								StringBuffer tableName = new StringBuffer();
								tableName.append(Constants.TABLEPREFIX)
										.append(projectId).append("_")
										.append(name);
								testDataValue = DatabaseUtil.getInstance()
										.getTestDataValueFromDB(
												tableName.toString(),
												columnHeader, testDataRowId);
							} else {
								LOGGER.debug("Table name is [" + sheetName
										+ "] and column name is ["
										+ columnHeader + "]");
								if (testSuiteRunner.getTestDataReader() != null) {
									testDataValue = testSuiteRunner
											.getTestDataReader().getColumnData(
													sheetName, testDataRowId,
													columnHeader);
								}
							}
							LOGGER.debug("Value retrieved from sheet ["
									+ sheetName + "], column [" + columnHeader
									+ "], row number [" + testDataRowId
									+ "] is [" + testDataValue + "]");

							if (testDataValue != null) {
								newValue = newValue
										.replace(
												Constants.TESTDATASTARTVARIABLEIDENTIFIER
														+ testDataHeader
														+ Constants.TESTDATAENDVARIABLEIDENTIFIER,
												testDataValue);
							} else {
								newValue = newValue
										.replace(
												Constants.TESTDATASTARTVARIABLEIDENTIFIER
														+ testDataHeader
														+ Constants.TESTDATAENDVARIABLEIDENTIFIER,
												"");
							}

							LOGGER.trace("Action value string after substituting the test data header ["
									+ testDataHeader
									+ "] is ["
									+ newValue
									+ "]");

							// since we substituted the variable value, the
							// string length may have changed. Let us start
							// looking for variable from beginning
							//
							// We are setting it to -1 as the searchPosition
							// value is incremented below before comparing
							// is starting the loop all over again
							searchPosition = -1;
						}

						if ((!testDataFound) && (!error)) {
							LOGGER.error("Termination character not found for variable start with characters ["
									+ variableName
									+ "]. Please check the syntax in test suite.");

							// Since the variable termination not found, this
							// could lead to incorrect results. break from this
							// loop and return from method...
							continueVariableSearch = false;
						}
					} else if ((newValue.charAt(searchPosition) == staticParameterStartDelimiter[0])
							&& ((searchPosition + 1) < newValue.length())
							&& (newValue.charAt(searchPosition + 1) == staticParameterStartDelimiter[1])) {
						LOGGER.trace("Looks like user specified a static value. Let us look for the variable name to substitute with it's value");

						String searchString = newValue.substring(searchPosition
								+ staticParameterStartDelimiter.length);

						boolean found = false;
						String parameter = "";
						for (int loopItrCnt = 0; loopItrCnt <= (searchString
								.length() - 1); loopItrCnt++) {
							if (staticParameterEndDelimiter.length == 1) {
								if (searchString.charAt(loopItrCnt) == staticParameterEndDelimiter[0]) {

									found = true;
								}
							} else {
								if ((searchString.charAt(loopItrCnt) == staticParameterEndDelimiter[0])
										&& (searchString.charAt(loopItrCnt + 1) == staticParameterEndDelimiter[1])) {

									found = true;
								}
							}

							if (found) {
								parameter = searchString.substring(0,
										loopItrCnt);
								searchPosition += staticParameterStartDelimiter.length
										+ parameter.length()
										+ staticParameterEndDelimiter.length;
								LOGGER.trace("Found a valid static value ["
										+ parameter
										+ "], adding to the script parameter list");

								newValue = newValue
										.replace(
												Constants.STATICPARAMETERSTARTIDENTIFIER
														+ parameter
														+ Constants.STATICPARAMETERENDIDENTIFIER,
												parameter);

								// since we substituted the static value, the
								// string length may have changed. Let us start
								// looking for variable from beginning
								//
								// We are setting it to -1 as the searchPosition
								// value is incremented below before comparing
								// is starting the loop all over again
								searchPosition = -1;

								break;
							}
						}
					}

					searchPosition++;

					if (searchPosition >= newValue.length()) {
						continueVariableSearch = false;
					}
				} while (continueVariableSearch);
			}
			// remove the forward slash before the value
			newValue = removeEscapeCharacter(newValue,
					variableEscapeCharDelimiterArr[0], escapeCharDelimiterArr);

			// Display this log message only when the action string value has
			// changed
			if (actionValue.compareToIgnoreCase(newValue) != 0) {
				LOGGER.debug("Action value string [" + actionValue
						+ "] after substituting for variables/test data is ["
						+ newValue + "]");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return newValue;
	}

	/**
	 * This method is used to strip the forward slash before the string.
	 * 
	 * @param newValue
	 *            the new value
	 * @param variableEscapeCharDelimiterArr
	 *            the variable escape char delimiter arr
	 * @param escapeCharDelimiterArr
	 *            the escape char delimiter array
	 * @return string
	 */
	private String removeEscapeCharacter(String newValue,
			char variableEscapeCharDelimiterArr, char[] escapeCharDelimiterArr) {
		LOGGER.trace("---------Inside removeForwardSlash-----------");
		StringBuffer sb = new StringBuffer();
		for (int position = 0; position < newValue.length(); position++) {
			if (newValue.charAt(position) == variableEscapeCharDelimiterArr) {
				if ((position + 1) < newValue.length()
						&& (newValue.charAt(position + 1) != escapeCharDelimiterArr[0]
								&& newValue.charAt(position + 1) != escapeCharDelimiterArr[1]
								&& newValue.charAt(position + 1) != escapeCharDelimiterArr[2]
								&& newValue.charAt(position + 1) != escapeCharDelimiterArr[3] && newValue
								.charAt(position + 1) != escapeCharDelimiterArr[4])) {
					LOGGER.trace("Removing the Escape charater..");
					sb.append(newValue.charAt(position));
				}
			} else {
				LOGGER.trace("Removing the Escape charater..");
				sb.append(newValue.charAt(position));
			}
		}
		LOGGER.trace("---------Exiting removeForwardSlash-----------");
		return sb.toString();
	}

	/**
	 * Validate test data header to ensure that it is in correct format. The
	 * format should be 'Sheet Name'.'Column Header' OR SheetName.ColumnHeader
	 * 
	 * @param testDataHeader
	 *            test data header to validate
	 * 
	 * @return true/false
	 */
	private boolean validateTestDataHeader(String testDataHeader) {
		boolean isValid = true;

		// check if the test data header contains both sheetname and column
		// header...
		//
		String value = testDataHeader.replace(".", "~");
		String[] valueList = value.split("~");

		if ((valueList.length != 2) && (valueList.length != 3)) {
			LOGGER.error("Invalid format:: Test data header ["
					+ testDataHeader
					+ "] specified is incorrect. Please check AFT documentation for details on how to use test data in the test suite");
			isValid = false;
		}

		return isValid;
	}


	/**
	 * Creates a file name with time time stamp for reports.
	 * 
	 * @param path
	 *            the path
	 * @param testStartTime
	 *            time when the test started to create folder for capturing
	 *            screenshots or service requests or report xml's for this run
	 * @param hostName
	 *            the host name
	 * @param sourceName
	 *            the source name
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String createReportXmlFileName(String path,
			java.util.Date testStartTime, String hostName, String sourceName)
			throws AFTException {
		String filePath = path;
		try {
			File f = new File(filePath);
			if (!f.exists() || !f.isDirectory() || !f.canWrite()) {
				LOGGER.warn("Invalid path [" + filePath + "] specified for "
						+ sourceName + ". Pls check for [" + sourceName
						+ "] path value in AFTConfig.xml file.");
			} else {
				// Create a place holder to store the screen shots or service
				// requests.
				DateFormat formatter = new SimpleDateFormat(
						Constants.DATEFORMATFOLDERNAME);
				String timestamp = formatter.format(testStartTime);
				filePath = filePath + "/" + hostName + "_" + timestamp;
				PageErrors.getInstance().setFilePath(filePath);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return filePath;
	}

	/**
	 * Parses parameter string specific with actions like copydata,
	 * executeScript and returns parameter arraylist.
	 * 
	 * @param testSuiteRunner
	 *            the test suite runner
	 * @param parameterList
	 *            the external script parameter list
	 * @param parseValue
	 *            the parse value
	 * @return Arraylist containing parameters passed to the function
	 * @throws AFTException
	 *             the application exception
	 */
	public List<String> parseActionParameterList(
			TestSuiteRunner testSuiteRunner, String parameterList,
			boolean parseValue) throws AFTException {

		boolean continueSearch = true;
		ArrayList<String> arrParameterList = new ArrayList<String>();

		char[] staticParameterStartDelimiter = Constants.STATICPARAMETERSTARTIDENTIFIER
				.toCharArray();
		char[] staticParameterEndDelimiter = Constants.STATICPARAMETERENDIDENTIFIER
				.toCharArray();

		char[] testDataStartVarIdentifier = Constants.TESTDATASTARTVARIABLEIDENTIFIER
				.toCharArray();
		char[] testDataEndVarIdentifier = Constants.TESTDATAENDVARIABLEIDENTIFIER
				.toCharArray();
		char[] dynamicVarDelimiter = Constants.DYNAMICVARIABLEDELIMITER
				.toCharArray();

		char[] objectRepositoryIdentifierStartDelimiter = Constants.OBJECTREPOSITORYIDENTIFIERSTARTDELIMITER
				.toCharArray();
		char[] objectRepositoryIdentifierEndDelimiter = Constants.OBJECTREPOSITORYIDENTIFIERENDDELIMITER
				.toCharArray();

		char[] startDelimiterArr, endDelimiterArr;

		LOGGER.info("Parsing the parameters [" + parameterList + "]");

		if (parameterList.length() > 0) {
			int searchPosition = 0;

			do {
				startDelimiterArr = null;
				endDelimiterArr = null;

				if ((parameterList.charAt(searchPosition) == staticParameterStartDelimiter[0])
						&& (parameterList.charAt(searchPosition + 1) == staticParameterStartDelimiter[1])) {

					startDelimiterArr = staticParameterStartDelimiter;
					endDelimiterArr = staticParameterEndDelimiter;

				} else if ((parameterList.charAt(searchPosition) == testDataStartVarIdentifier[0])
						&& (parameterList.charAt(searchPosition + 1) == testDataStartVarIdentifier[1])) {

					startDelimiterArr = testDataStartVarIdentifier;
					endDelimiterArr = testDataEndVarIdentifier;

				} else if (parameterList.charAt(searchPosition) == dynamicVarDelimiter[0]) {

					startDelimiterArr = dynamicVarDelimiter;
					endDelimiterArr = dynamicVarDelimiter;

				} else if ((parameterList.charAt(searchPosition) == objectRepositoryIdentifierStartDelimiter[0])
						&& (parameterList.charAt(searchPosition + 1) == objectRepositoryIdentifierStartDelimiter[1])) {

					startDelimiterArr = objectRepositoryIdentifierStartDelimiter;
					endDelimiterArr = objectRepositoryIdentifierEndDelimiter;

				} else {
					searchPosition++;
				}

				if (endDelimiterArr != null) {

					LOGGER.trace("Looks like user specified a parameter. Let us parse the value and store in the parameter array.");

					String searchString = parameterList
							.substring(searchPosition
									+ startDelimiterArr.length);

					boolean found = false;
					String parameter = "";
					String unSubstitutedParameter = "";
					for (int loopItrCnt = 0; loopItrCnt <= (searchString
							.length() - 1); loopItrCnt++) {
						if (endDelimiterArr.length == 1) {
							if (searchString.charAt(loopItrCnt) == endDelimiterArr[0]) {

								found = true;
							}
						} else {
							if ((searchString.charAt(loopItrCnt) == endDelimiterArr[0])
									&& (searchString.charAt(loopItrCnt + 1) == endDelimiterArr[1])) {

								found = true;
							}
						}

						if (found) {
							unSubstitutedParameter = parameterList.substring(
									searchPosition, searchPosition + loopItrCnt
											+ startDelimiterArr.length
											+ endDelimiterArr.length);

							parameter = searchString.substring(0, loopItrCnt);
							searchPosition += startDelimiterArr.length
									+ parameter.length()
									+ endDelimiterArr.length;
							LOGGER.trace("Found a valid parameter ["
									+ parameter
									+ "], adding to the script parameter list");

							if (parseValue) {
								if (unSubstitutedParameter
										.startsWith(Constants.DYNAMICVARIABLEDELIMITER)
										|| unSubstitutedParameter
												.startsWith(Constants.TESTDATASTARTVARIABLEIDENTIFIER)) {
									parameter = getActionValue(testSuiteRunner,
											unSubstitutedParameter);

								} else if (unSubstitutedParameter
										.startsWith(Constants.OBJECTREPOSITORYIDENTIFIERSTARTDELIMITER)) {

									// get object id from execution engine
									// specific repository...
									if (EngineManager.getInstance().getCurrentExecutionEngine() != null
											&& ObjectRepositoryManager
													.getInstance()
													.isObjectRepositoryLoaded()) {
										parameter = Helper.getInstance()
												.getActionValue(
														testSuiteRunner,
														parameter);
										parameter = EngineManager.getInstance().getCurrentExecutionEngine()
												.getObjectId(parameter);
									} else if (ObjectRepositoryManager
											.getInstance()
											.isObjectRepositoryLoaded()) {
										parameter = ObjectRepositoryManager
												.getInstance().getObjectID(
														parameter);
									}
								}

								arrParameterList.add(parameter);
								break;

							} else {
								arrParameterList.add(unSubstitutedParameter);
								break;
							}
						}
					}

					if (!found) {
						throw new AFTException(
								"Valid parameter not found in paramter list ["
										+ parameterList + "].");
					}
				}

				if (searchPosition >= parameterList.length()) {
					continueSearch = false;
				}
			} while (continueSearch);
		}

		return arrParameterList;
	}

	/**
	 * This methods check if one of the terminate annotations has been called to
	 * terminate execution.
	 * 
	 * @param executionLevel
	 *            executionLevel indicate which level of execution is this
	 *            method called to check if terminate annotations has been
	 *            called
	 * @return true if execution should be terminated OR else false if execution
	 *         should not be terminated
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean terminateCurrentExecution(int executionLevel)
			throws AFTException {

		/*
		 * Get the value of the system variable AFT_TerminateACurrentTestSuite
		 * and if it equals "true", terminate the loop
		 */
		String terminateCurrentTestSuiteFlag = getActionValue(Variable
				.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSUITE));

		/*
		 * Get the value of the system variable AFT_TerminateCurrentTestScenario
		 * and if it equals "true", terminate the loop
		 */
		String terminateCurrentTestScenarioFlag = getActionValue(Variable
				.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSCENARIO));

		/*
		 * Get the value of the system variable AFT_TerminateCurrentTestCase and
		 * if it equals "true", terminate the loop
		 */
		String terminateCurrentTestCaseFlag = getActionValue(Variable
				.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTCASE));

		if ((executionLevel == Constants.TESTSCENARIO)
				|| (executionLevel == Constants.TESTSUITE)) {
			terminateCurrentTestCaseFlag = "false";
		}
		if (executionLevel == Constants.TESTSUITE) {
			terminateCurrentTestScenarioFlag = "false";
		}

		return (terminateCurrentTestSuiteFlag.equalsIgnoreCase("true")
				|| terminateCurrentTestCaseFlag.equalsIgnoreCase("true") || terminateCurrentTestScenarioFlag
					.equalsIgnoreCase("true"));

	}

	/**
	 * Log test result in Test Case Management Tool.
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	public void logTestExecutionResults() throws AFTException {

		List<TestScenario> testScenarios = TCMReportGenerator.getInstance()
				.getTestSuite().getTestScenariosArr();
		String result = "PASS";
		TestScenario testScenario;
		// We can technically have more than 1 test scenario, and that should be
		// an
		// error condition.
		// If there are more than 1 test case objects in array, log a warning
		// message for the user and just use the last object
		LOGGER.debug("Test Case array has [" + testScenarios.size()
				+ "] objects");
		// Get the Test Case
		testScenario = testScenarios.get(testScenarios.size() - 1);
		if (testScenarios.size() > 1) {
			LOGGER.warn("Found more than one test scennario, will use the last test scenario with description ["
					+ testScenario.getBusinessScenarioDesc() + "]");
		}

		// TestCase object will give us the executionResult, additionalInfo
		// Iterating thru the test case object, we can construct the screenShot,
		// serviceRequest and serviceResponse arrays contains filepath
		List<TestCase> testCases = testScenario.getTestCaseDetails();

		// Get Test Case Execution Result
		// String result = testScenario.getExecutionResult();

		// Get Test Case additional Information
		String info = "";

		// Get Test Case ID
		String testCaseID = getActionValue(Variable.getInstance()
				.generateSysVarName(
						SystemVariables.AFT_TCMINTEGRATION_TESTCASEID));

		ArrayList<String> screenShotList = null;
		ArrayList<String> requestList = null;
		ArrayList<String> responseList = null;

		// check here if the evidence should be attached and if not, pass null
		// for screenShot, serviceRequest and serviceResponse arrays
		if (ConfigProperties
				.getInstance()
				.getConfigProperty(
						ConfigProperties.TCM_INTEGRATION_ATTACH_EVIDENCE)
				.equalsIgnoreCase("YES")) {
			screenShotList = new ArrayList<String>();
			requestList = new ArrayList<String>();
			responseList = new ArrayList<String>();

			for (TestCase testCase : testCases) {
				List<TestStep> testSteps = testCase.getTestStepDetails();
				for (TestStep testStep : testSteps) {
					if (!testStep.getServiceRequestName().isEmpty()) {
						requestList.add(testStep.getServiceRequestName());
					}
					if (!testStep.getServiceResponseName().isEmpty()) {
						responseList.add(testStep.getServiceResponseName());
					}
					if (!testStep.getImageName().isEmpty()) {
						screenShotList.add(testStep.getImageName());
					}

					if (testStep.getErrorMessage() != null
							&& testStep.getErrorMessage().length() > 0) {
						info = info + " " + testStep.getErrorMessage();
						result = "FAIL";
					}
				}
			}
		}
		TCMIntegration.getInstance().logQmetryTestExecutionResult(testCaseID,
				info, result, screenShotList, requestList, responseList);

		// After logging the results, let us clear the test case object in TCM
		// Report generator for storing new results
		// TCMReportGenerator.getInstance().getTestSuite().initTestScenarioObj();
	}

	/**
	 * Initiate the exit process of Test Suite Runner.
	 * 
	 * @param testBatchExecutionStartTime
	 *            the test batch execution start time
	 */
	public void exitTestSuiteRunner(long testBatchExecutionStartTime) {
		// Clearing all the Dynamic variables and its values from
		// the memory
		LOGGER.info("Clearing variable map...");
		Map<String, String> storedVariablesMap = Variable.getInstance()
				.variablesMap();

		storedVariablesMap.clear();

		// Close all open script instances
		//
		LOGGER.info("closing all open script instances...");
		ScriptInstanceManager.getInstance().closeAllOpenScriptInstances();

		// Closing all the open DB connection objects
		//
		LOGGER.info("Closing all open DB connections...");
		DatabaseInstanceManager.getInstance().destroyAllOpenDBInstances();

		// Clearing all the sikuli objects
		LOGGER.info("Clearing sikuli instance and map...");
		SikuliObjectManager.getInstance().destorySikuliObjects();

		// Set testSuite execution duration...
		//
		long tcSuiteTotalTime = System.currentTimeMillis()
				- testBatchExecutionStartTime;

		// set execution time for the test suite
		ReportGenerator.getInstance().getLatestTestSuite()
				.setExecutionTime(tcSuiteTotalTime);

		// also set execution time for the test suite in the TCM report
		// generator for logging results to test case management tool
		TCMReportGenerator.getInstance().getTestSuite()
				.setExecutionTime(tcSuiteTotalTime);
	}

	/**
	 * This method initiate to run shutdown hook to generate test reports and
	 * close all database resources.
	 * 
	 * @param testBatchRunner
	 *            the test batch runner
	 */
	public void attachShutDownHook(final TestBatchRunner testBatchRunner) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (!testBatchRunner.isExecutionCompleted()) {
					LOGGER.warn("User has pressed Ctrl+C or closed the command window and the xAFT execution will be stopped");
					LOGGER.info("---------Inside Add Shutdown Hook---------------------------");

					exitTestSuiteRunner(testBatchRunner.getTestStartTime()
							.getTime());
					// test batch result
					try {
						testBatchRunner.exitTestBatchExecution("");
					} catch (AFTException e) {
						LOGGER.error("Exception::", e);
					}
					LOGGER.info("---------Exiting Add Shutdown Hook---------------------------");
				}
			}
		});
	}

	
	/**
	 * All framework file found.
	 * 
	 * @return isFileFound
	 */
	public boolean allFrameworkFileFound() {
		return allFrameworkFileFound;
	}

	/**
	 * Sets the framework file found.
	 * 
	 * @param isFileFound
	 *            the isFileFound to set
	 */
	public void setFrameworkFileFound(boolean isFileFound) {
		this.allFrameworkFileFound = isFileFound;
	}

	/**
	 * Checks if is file system request.
	 * 
	 * @return isFileSystemRequest
	 */
	public boolean isFileSystemRequest() {
		return isFileSystemRequest;
	}

	/**
	 * Sets the file system request.
	 * 
	 * @param isFileSystemRequest
	 *            the isFileSystemRequest to set
	 */
	public void setFileSystemRequest(boolean isFileSystemRequest) {
		this.isFileSystemRequest = isFileSystemRequest;
	}

	/**
	 * This method checks if user wants sikuli action to be invoked.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param action
	 *            the action
	 * @param elementName
	 *            the element name
	 * @param parsedElementValue
	 *            the parsed element value
	 * @return true, if successful
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean checkSikuliCall(TestStepRunner testStepRunner,
			String action, String elementName, String parsedElementValue)
			throws AFTException {

		boolean callSikuli = false;

		try {

			// For mousemove action, check if user passed an image filename
			if (elementName.equalsIgnoreCase("mousemove")) {

				callSikuli = isScreenshotObject(parsedElementValue);

			} else {

				String parsedElementName = getActionValue(
						testStepRunner.getTestSuiteRunner(), elementName);

				callSikuli = isScreenshotObject(parsedElementName);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}

		isSikuliCalled = callSikuli;

		return callSikuli;

	}

	/**
	 * This method checks if user passed any screenshot object.
	 * 
	 * @param valueToCheck
	 *            the value to check
	 * @return true, if is screenshot object
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean isScreenshotObject(String valueToCheck) throws AFTException {

		boolean isScreenshotObject = false;

		try {

			// if user passed an image filename
			if (valueToCheck.toLowerCase().contains(".png")) {
				return true;
			}

			// Check if Object type is "imageName"
			RepositoryObject repoObject = ObjectRepositoryManager.getInstance()
					.getObject(valueToCheck);
			if (repoObject != null && repoObject.getImageName() != null) {
				return true;
			}

			// Check if User passed variable is listed in Sikuli Object map or
			if (SikuliObjectManager.getInstance().isSikuliPatternObjectExists(
					valueToCheck)) {
				return true;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}

		return isScreenshotObject;
	}

	/**
	 * Returns the isSikuliCalled value.
	 * 
	 * @return isSikuliCalled *
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean isSikuliCall() throws AFTException {

		return isSikuliCalled;
	}
	/**
	 * @param isSikuliCalled the isSikuliCalled to set
	 */
	public void setSikuliCalled(boolean isSikuliCalled) {
		this.isSikuliCalled = isSikuliCalled;
	}
}
