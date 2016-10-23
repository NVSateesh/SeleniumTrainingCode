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
 * Class: TestSuiteRunner
 * 
 * Purpose: Test Suite runner implementation. Implements IAFTTestRunner to
 * execute one test suite. Instantiated and called by TestBatchRunner
 */

package com.ags.aft.runners;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.Reporting.TCMReportGenerator;
import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.Util;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Annotations;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.imports.ExcelRead;
import com.ags.aft.integrations.TCMIntegration;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.testObjects.TestSet;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Notifications;
import com.ags.aft.util.ScrollableFrame;
import com.ags.aft.util.Variable;

/**
 * Test Suite runner implementation. Implements IAFTTestRunner to execute one
 * test suite. Instantiated and called by TestBatchRunner
 * 
 */
public class TestSuiteRunner implements ITestRunner {

	/**
	 * CONSTANT LOGGER Object Instance
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TestSuiteRunner.class);

	/**
	 * Test Batch Runner object instance
	 */
	private TestBatchRunner testBatchRunner;

	// Test Suite specific information for use within this runner
	/**
	 * Test Suite file name and path
	 */
	private String testSuitePath;
	/**
	 * Functional Scenarios Sheet name in Test Suite
	 */
	private String functionalScenarioSheetName;
	/**
	 * Test Cases Sheet name in Test Suite
	 */
	private String testStepsSheetName;

	/**
	 * Scenario ids read from test suite
	 */
	private List<TestScenario> testScenarios = null;

	/**
	 * Test Data file name and path associated with the test suite
	 */
	private String testDataFilePath;
	/**
	 * Test Data Reader object
	 */
	private ExcelRead testDataReader = null;
	/**
	 * Object to store RowIds for each sheet in the test data excel associated
	 * with this test suite
	 */
	private Map<String, String> testDataRowIds = new HashMap<String, String>();

	// Category of execution as specified in test batch set
	private String executionCategory;

	// reusable objects
	private String reusableTestSuitePath;
	private String reusableScenariosSheetName;
	private String reusableTestStepsSheetName;
	private String tcmINTConfigFile;
	private List<TestScenario> reusableTestScenarios = null;
	/**
	 * suite execution start time
	 */
	private long testSuiteExecutionStartTime;

	// Used for storing number of steps executed in a business scenario. This is
	// primarily used for reporting purpose...
	private int currentBusinessScenarioTestStepCount;

	// Jump to
	// These two variables store the business scenario and test case id passed
	// by user while calling jumpTo annotation
	private String jumpToBusinessScenarioId = "";
	private String jumpToTestCaseId = "";

	// These two variables store the corresponding RowId in reusable test
	// scenarios and test case maps for the business scenario and test case id
	// passed by user while calling jumpTo annotation
	private int jumpToBusinessScenarioRowId = -1;
	private int jumpToTestCaseRowId = -1;

	// This flag is used to store if the user has called jumpTo annotation or
	// not. If this flag is set to true, it means user has called jumptTo
	// annotation
	private boolean jumpTo = false;

	// This flag is used to indicate if we are executing a reusable test
	// scenario in which case the executionFlag needs to be ignored. If this is
	// set to true, it means the next scenario will always be executed by
	// framework
	private boolean jumpToExec = false;

	// This flag indicates that the scenario we are jumping to is a reusable
	// test scenario
	// private boolean executingResuableTestScenario = false;
	private Stack<String> executingResuableTestScenario;

	/**
	 * Flag to indicate if TCM integration is enabled and was successfully
	 * instantiated or not...
	 */
	private boolean tcmIntegrationEnabled = false;

	/**
	 * sort Id used to sort testcase /reusables in Report
	 */
	private int iTestStepMapId = 0;

	// Execution speed for this test suite
	private int executionSpeed = 0;

	// testStep list
	private List<TestStep> testStepsExecutedList = new ArrayList<TestStep>();

	// resusable steps list
	private Set<String> reusableTestStepsExecutedList = new HashSet<String>();

	// verify steps list
	private List<TestStep> verifyTestStepsExecutedList = new ArrayList<TestStep>();

	// test scenario list
	private Set<String> testCaseExecutedList = new HashSet<String>();

	// test data sheet list
	private Map<String, Integer> testDataSheetList = null;

	// moveData row annotation flag
	private boolean moveToDataRow = false;

	// data traversed flag
	private boolean isDataTraversed = false;

	// total steps count
	private int totalTestStepsExecuted = 0;

	// DB parameterLst
	private String dbParameterLst = null;

	// DB Connection Identifier
	private String dbConnIdentifier = null;

	// DB Connection Counter
	private int dbConnCounter = 0;

	// DB Error value
	private String onDBErrorValue = null;

	// common initialization and clean up scripts
	private String scenarioInitializationIDs = null;
	private String scenarioCleanupIDs = null;
	private String testSetInitializationIDs = null;
	private String testSetCleanupIDs = null;
	private boolean isTestSetInitialization = false;
	private boolean isTestSetInitializationFailed = false;
	private boolean isScenarioInitialization = false;

	// scrollable frame text values
	// these values will be used by @displayScrollableFrame annotation
	private String scenarioDescription = null;
	private String testCaseDescription = null;

	// Declaring TestSet object
	private TestSet testSet = null;
	private boolean isJumpToStep = false;
	private boolean isEndOfTestData;
	private int testCaseIdCount = 0;
	private long totalTestStepExecTime;

	/**
	 * Custom TestSuiteRunner constructor. Initializes the runner, sets required
	 * parameters and initializes system variables
	 * 
	 * @param batchRunner
	 *            the batch runner
	 * @param testSet
	 *            testSet
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws AFTException
	 *             the application exception
	 */
	public TestSuiteRunner(TestBatchRunner batchRunner, TestSet testSet)
			throws IOException, InvalidFormatException {

		// Init variables...
		testBatchRunner = batchRunner;
		testSuitePath = testSet.getTestSuitePath();
		functionalScenarioSheetName = testSet.getScenariosSheetName();
		testStepsSheetName = testSet.getTestStepsSheetName();
		testDataFilePath = testSet.getTestDataTablePath();
		reusableTestSuitePath = testSet.getReusableTestSuitePath();
		reusableScenariosSheetName = testSet.getReusableScenariosSheetName();
		reusableTestStepsSheetName = testSet.getReusableTestStepsSheetName();
		executingResuableTestScenario = new Stack<String>();
		this.executionCategory = testSet.getCategory();
		this.scenarioInitializationIDs = testSet.getScenarioInitializationIDs();
		this.scenarioCleanupIDs = testSet.getScenarioCleanupIDs();
		this.testSetInitializationIDs = testSet.getTestSetInitializationIDs();
		this.testSetCleanupIDs = testSet.getTestSetCleanupIDs();
		this.tcmINTConfigFile = testSet.getTcmIntegrationConfigFilePath();
		this.testSet = testSet;
		// Init suite execution start time
		testSuiteExecutionStartTime = System.currentTimeMillis();

	}

	/**
	 * Initializes the TestSuite
	 * 
	 * @throws InvalidFormatException
	 * @throws AFTException
	 * @throws IOException
	 */
	public void initializeTestSuite() throws AFTException {

		// initialize the frame
		ScrollableFrame.getInstance().initializeFrame();

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		// Load Test suite
		//
		loadTestSuite(isFileSystemRequest);

		// Load Reusable test suite
		//
		if ((reusableTestSuitePath != null && reusableTestSuitePath.length() > 0)
				|| !isFileSystemRequest) {
			loadReusableTestSuite();

			// verify all initialization and cleanup scenarios
			verifyInitCleanUpScenarios();
		}

		// Initialize test data file
		//
		if (isFileSystemRequest && (testDataFilePath != null)
				&& (testDataFilePath.length() > 0)
				&& (testDataFilePath.compareToIgnoreCase("novalue") != 0)) {
			initTestDataReader();
		}

		// Check if user has to integrate the test results into TCMTool
		checkIfTCMIntegrationEnabled();
	}

	/**
	 * Check if TCM_INTEGRATION is enabled or not.
	 * 
	 * @throws AFTException
	 */
	private void checkIfTCMIntegrationEnabled() throws AFTException {
		if (ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.TCM_INTEGRATION)
				.equalsIgnoreCase("YES")) {
			if (tcmINTConfigFile != null) {
				if (TCMIntegration.getInstance().init(tcmINTConfigFile) != null) {
					this.tcmIntegrationEnabled = true;
					LOGGER.info("TCMIntegration Instantiation Successful");
				} else {
					LOGGER.error("Failed to instantiate TCMInstance class. Please check log and fix the error.");
					throw new AFTException(
							"Failed to instantiate TCMInstance class. Please check log and fix the error.");
				}
			} else {
				LOGGER.warn("TCM integration is enabled but no config file specified in Test Set. "
						+ "Test results will not be logged into TCM tool. Continuing test execution...");
			}
		}
	}

	/**
	 * Parses and loads test suite file
	 * 
	 * @param isFileSystemRequest
	 *            isFileSystemRequest
	 * @throws InvalidFormatException
	 * @throws AFTException
	 * @throws IOException
	 */
	private void loadTestSuite(boolean isFileSystemRequest) throws AFTException {
		LOGGER.debug("initializing scenarioIDs and testSteps list objects");

		try {
			if (isFileSystemRequest) {
				ExcelRead dataRead = null;
				dataRead = new ExcelRead(testSuitePath,
						functionalScenarioSheetName, reusableScenariosSheetName);

				if (dataRead.getWorkbook() == null) {
					return;
				}
				LOGGER.debug("Reading business scenarios...");
				testScenarios = dataRead.readSheetData(
						functionalScenarioSheetName, 2, 5, testStepsSheetName);
			} else {
				// Read functional scenarios from DB
				LOGGER.debug("Reading business scenarios...");
				testScenarios = DatabaseUtil.getInstance().loadTestScenarios(
						testSet.getIdTestSuite(),
						Constants.LOADNONREUSABLETESTSCENARIOS);
			}
		} catch (IOException e) {
			if (!e.toString().contains("java.io.FileNotFoundException")) {
				if (testScenarios == null || testScenarios.size() == 0) {
					LOGGER.error("INVALID business scenarios sheet name ["
							+ functionalScenarioSheetName
							+ "] specified. Pls check the ScenariosSheetName property value in Batch.xml file");

				}
			} else {
				Helper.getInstance().setFrameworkFileFound(false);
			}

			throw new AFTException(e);
		} catch (Exception ex) {
			LOGGER.error("Exception::", ex);
			throw new AFTException(ex);
		}
	}

	/**
	 * Parses and loads resuable test suite file
	 * 
	 * @throws InvalidFormatException
	 * @throws AFTException
	 * @throws IOException
	 */
	private void loadReusableTestSuite() throws AFTException {
		LOGGER.debug("initializing scenarioIDs and testSteps list objects for reusable test suite file ["
				+ reusableTestSuitePath + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		try {
			if (isFileSystemRequest) {
				ExcelRead dataRead = new ExcelRead(reusableTestSuitePath,
						functionalScenarioSheetName, reusableScenariosSheetName);

				if (dataRead.getWorkbook() == null) {
					return;
				}
				LOGGER.debug("Reading business scenarios...");

				reusableTestScenarios = dataRead.readSheetData(
						reusableScenariosSheetName, 2, 3,
						reusableTestStepsSheetName);
			} else {
				// Read reusable scenarios from DB
				LOGGER.debug("Reading business scenarios...");
				reusableTestScenarios = DatabaseUtil.getInstance()
						.loadReusableScenarios(
								Constants.LOADREUSABLETESTSCENARIOS);
			}
		} catch (IOException e) {
			if (!e.toString().contains("java.io.FileNotFoundException")) {
				if (reusableTestScenarios == null
						|| reusableTestScenarios.size() == 0) {
					LOGGER.error("INVALID business scenarios sheet name ["
							+ reusableScenariosSheetName
							+ "] specified. Pls check the ReusableScenariosSheetName property value in Batch.xml file");

				}
			} else {
				Helper.getInstance().setFrameworkFileFound(false);
			}

			throw new AFTException(e);
		} catch (Exception ex) {
			LOGGER.error("Exception::", ex);
			throw new AFTException(ex);
		}
	}

	/**
	 * Parses and loads test data file
	 * 
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws AFTException
	 */
	private void initTestDataReader() throws AFTException {

		try {
			LOGGER.debug("Initializing Test Data file [" + testDataFilePath
					+ "]");

			testDataReader = new ExcelRead(testDataFilePath, null, null);

			// We need to initialize the RoWId for each sheet in the test data
			// workbook
			//

			// First let us get the list of sheets in the test data workbook
			List<String> sheetNames = testDataReader.getSheetNames();

			testDataSheetList = new HashMap<String, Integer>(sheetNames.size());
			// Now store the sheet name and initial RowId as 1 for each sheet in
			// the testDataRowIds map
			for (int i = 0; i < sheetNames.size(); i++) {
				testDataRowIds.put(sheetNames.get(i).toLowerCase(), "1");
				testDataSheetList.put(sheetNames.get(i).toLowerCase(), 0);
			}

			// Init all variables related to test data...
			//
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_TESTDATASHEETCOUNT), true,
					Integer.toString(sheetNames.size()));

			Variable.getInstance()
					.setVariableValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_ISENDOFTESTDATA), true,
							"false");

		} catch (IOException e) {
			if (!e.toString().contains("java.io.FileNotFoundException")) {
				LOGGER.error("INVALID test data file ["
						+ testDataFilePath
						+ "] specified. Please check the configuration value in test batch file");
			} else {
				Helper.getInstance().setFrameworkFileFound(false);
			}

			throw new AFTException(e);
		} catch (Exception ex) {
			LOGGER.error("Exception::", ex);
			throw new AFTException(ex);
		}
	}

	/**
	 * Execute test steps for the business scenarios
	 * 
	 * @param testScenario
	 *            testScenario being executed
	 * @param isReusableScenario
	 *            isReusableScenario
	 * @throws AFTException
	 */
	public void executeTestCases(TestScenario testScenario,
			boolean isReusableScenario) throws AFTException {
		AFTException curException = null;
		AFTException prevException = null;

		// Initialize testcase execution position within teststeps map...
		String businessScenarioId = testScenario.getBusinessScenarioId();
		scenarioDescription = testScenario.getBusinessScenarioDesc();
		// If jumpToExec is set to true, we need to reset it to reusable
		// testcase execution position within teststeps map...
		int iCaseId = 0;
		if (jumpToExec) {
			iCaseId = jumpToTestCaseRowId;
		}
		int testScenarioCount = testScenario.getTestCaseDetails().size() - 1;
		for (; iCaseId <= testScenarioCount; iCaseId++) {
			try {
				// Read test case id...
				TestCase testCase = testScenario.getTestCaseDetails().get(
						iCaseId);
				String testCaseId = testCase.getTestCaseId();
				// Read business scenario id...
				testCaseDescription = testCase.getTestCaseDesc();
				LOGGER.trace("Read Test Case Id [" + testCaseId
						+ "], Test Case Description [" + testCaseDescription
						+ "] for business scenario [" + businessScenarioId
						+ "]");
				// Create & Init TestStepRunner object
				TestStepRunner testStepRunner = new TestStepRunner(this,
						businessScenarioId, testCase, iTestStepMapId,
						isReusableScenario);
				// Init test case level system variables
				testStepRunner.initSystemVariables();

				// display scrollable frame
				checkScrollableFrame(isReusableScenario);

				try {
					testStepRunner.execute();
				} catch (AFTException ae) {
					curException = ae;
				} finally {
					// Increment the sort Id
					incrementSortId(isReusableScenario);
					// add the test case id to the test case id list.
					getTestCaseExecutedList().add(testCaseId);
					// On Error Recovery handling...
					if (curException != null) {
						boolean flag = isTestCaseErrorConfigured();
						if (flag) {
							break;
						}
						// save this exception before setting curException
						// to
						// NULL for correct reporting
						prevException = curException;
						curException = null;
					}
					// check if current execution should be terminated as a
					// result of user calling a terminate specific annotation
					if (Helper.getInstance().terminateCurrentExecution(
							Constants.TESTSCENARIO)) {
						LOGGER.info("It seems user has called '"
								+ Annotations.TERMINATECURRENTTESTSCENARIO
								+ "' annotation, quitting current business scenario ["
								+ businessScenarioId + "]");
						break;
					}
				}
				// checking if the JumpTotestCase has set the scenario ID
				if (isJumpTo()) {
					if (jumpToBusinessScenarioId
							.compareToIgnoreCase(businessScenarioId) == 0) {
						LOGGER.info("It seems user has called JumpTo annotation. Since user is calling another test case ["
								+ jumpToTestCaseId
								+ "] in same business Scenario Id ["
								+ jumpToBusinessScenarioId
								+ "], we just need to reset the test case Id");
						iCaseId = jumpToTestCaseRowId - 1;
						jumpTo = false;
					} else {
						LOGGER.info("It seems user has called JumpTo annotation. Since user is calling test case ["
								+ jumpToTestCaseId
								+ "] in another business Scenario Id ["
								+ jumpToBusinessScenarioId
								+ "], we need to quit executing current business scenario.");
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		// If an exception has been raised during any step execution, let us
		// raise it up the chain for reporting...
		raiseException(curException, prevException);
	}

	/**
	 * This method will increment sort id
	 * 
	 * @param isReusableScenario
	 *            isReusableScenario
	 */
	private void incrementSortId(boolean isReusableScenario) {
		if (!isReusableScenario) {
			iTestStepMapId++;
		}
	}

	/**
	 * This method will throw exception if exception object is not null.
	 * 
	 * @param curException
	 *            curException
	 * @param prevException
	 *            prevException
	 * @throws AFTException
	 */
	private void raiseException(AFTException curException,
			AFTException prevException) throws AFTException {
		if (curException != null || prevException != null) {
			throw (prevException != null) ? prevException : curException;
		}
	}

	/**
	 * Check if Scrollable Frame is configured. if configured display the frame
	 * 
	 * @param isReusableScenario
	 *            isReusableScenario
	 * @throws AFTException
	 */
	private void checkScrollableFrame(boolean isReusableScenario)
			throws AFTException {
		if (ConfigProperties
				.getInstance()
				.getConfigProperty(
						ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME)
				.equalsIgnoreCase("Yes")) {
			LOGGER.debug("User has specified the Config Property ["
					+ ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME
					+ "] to [Yes]."
					+ "A new screen will showup at the bottom of "
					+ "the screen with Test Scenario ID and Description");
			if (!isReusableScenario) {
				displayScrollableFrame();
			}
		}
	}

	/**
	 * Check if test case error handling is configured
	 * 
	 * @return boolean
	 * @throws AFTException
	 */
	private boolean isTestCaseErrorConfigured() throws AFTException {
		boolean onErrorHandling = false;
		String[] onError = getOnErrorValue();
		boolean isErrorConfigured = false;

		if ((onError[1]
				.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) != 0)
				&& (onError[1]
						.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTCASE) != 0)) {
			onErrorHandling = true;

		}
		if (onErrorHandling) {
			LOGGER.info("User has defined '"
					+ onError[0]
					+ "' to ["
					+ onError[1]
					+ "]. This business scenario execution will be stopped and test execution will continue as per '"
					+ onError[0] + "' configuration.");

			// check if TCM integration is enabled and
			// call
			// logTestExecutionResults to log results...
			if (tcmIntegrationEnabled) {
				Helper.getInstance().logTestExecutionResults();
			}

			isErrorConfigured = true;
		}
		return isErrorConfigured;
	}

	/**
	 * Search and return position id for the business scenario in Scenarios map
	 * 
	 * @param businessScenarioId
	 *            business Scenario to search for
	 * @param scenarioIDs
	 *            test scenarios id list
	 * @return Position of the business scenarios in scenarios map
	 */
	public int getScenarioPositionId(String businessScenarioId,
			List<TestScenario> scenarioIDs) {

		int businessScenarioPositionId = -1;

		int scenariosCount = scenarioIDs.size() - 1;

		// Loop here for executable scenarios
		for (int iRow = 0; iRow <= scenariosCount; iRow++) {
			TestScenario scenario = scenarioIDs.get(iRow);
			String scenarioId = scenario.getBusinessScenarioId();

			if (scenarioId.compareToIgnoreCase(businessScenarioId) == 0) {
				businessScenarioPositionId = iRow;
				break;
			}
		}

		return businessScenarioPositionId;
	}

	/**
	 * Execute a business scenario
	 * 
	 * @param testScenario
	 *            business scenario to execute
	 * @param isReusableScenario
	 *            is this a reusable scenario being executed?
	 * @throws AFTException
	 */
	public void executeSingleBusinessScenario(TestScenario testScenario,
			boolean isReusableScenario) throws AFTException {
		String suitePath = null;
		String businessScenarioId = testScenario.getBusinessScenarioId();
		AFTException applException = null;
		LOGGER.trace("Read business scenario description ["
				+ testScenario.getBusinessScenarioDesc() + "]");
		if (!isReusableScenario) {
			suitePath = testSuitePath;
			LOGGER.debug("Starting execution of a scenario ["
					+ businessScenarioId
					+ "] in primary test suite. Pushing '' to [executingResuableTestScenario] stack.");
			executingResuableTestScenario.push("");
		} else {
			suitePath = reusableTestSuitePath;
			LOGGER.debug("Starting execution of a scenario ["
					+ businessScenarioId
					+ "] in resuable test suite. Pushing ["
					+ businessScenarioId
					+ "] to [executingResuableTestScenario] stack.");
			executingResuableTestScenario.push(businessScenarioId);
		}
		try {
			// execute the scenario
			executeScenario(testScenario, suitePath, isReusableScenario);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			applException = new AFTException(e);
			throw applException;
		} finally {
			if (isReusableScenario) {
				getReusableTestStepsExecutedList().add(businessScenarioId);
				setTestScenarioReportingResults(testScenario, applException,
						System.currentTimeMillis(), "NO");
			}
			String id = executingResuableTestScenario.pop();
			LOGGER.debug("Completed execution of a scenario ["
					+ businessScenarioId + "]. Poped out [" + id
					+ "] from [executingResuableTestScenario] stack.");

			if ((ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME)
					.equalsIgnoreCase("Yes")) && (!isReusableScenario)) {
				// stop the timer
				ScrollableFrame.getInstance().stop();
			}
		}
	}

	/**
	 * Execute a business scenario
	 * 
	 * @param testScenario
	 *            testScenario to execute
	 * @param suitePath
	 *            suitePath
	 * @param isReusableScenario
	 *            is this a reusable scenario being executed?
	 * @throws AFTException
	 */
	private void executeScenario(TestScenario testScenario, String suitePath,
			boolean isReusableScenario) throws AFTException {
		String executeFlag;
		String businessScenarioId = testScenario.getBusinessScenarioId();
		String description = testScenario.getBusinessScenarioDesc();
		try {

			LOGGER.info("Starting execution of business scenario id ["
					+ businessScenarioId
					+ "] and associated test steps in test suite file ["
					+ suitePath + "]");
			LOGGER.trace("Test cases size ["
					+ testScenario.getTestCaseDetails().size() + "]");
			if (!(testScenario.getTestCaseDetails().size() > 0)) {
				String errMsg = "No test cases found for Business scenarioId ["
						+ businessScenarioId + "].";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			// Read execution flag...
			executeFlag = testScenario.getExecutionFlag();
			if (executeFlag == null) {
				executeFlag = "";
				LOGGER.trace("Read execution flag [" + executeFlag + "]");
			}
			// If Execution flag is "Y", we will execute this scenario
			if (executeFlag.equalsIgnoreCase("Y") || jumpToExec) {

				// If Show Test Scenario frame is yes then this block will
				// execute
				// voila, business scenario is valid, let us execute all test
				// steps for this scenario...
				LOGGER.info("Executing test steps. Execution flag for business scenario id ["
						+ businessScenarioId
						+ "], description ["
						+ description
						+ "] is [" + executeFlag + "]");

				if (!isReusableScenario) {
					Variable.getInstance().setVariableValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_CURBUSINESSSCENARIOID),
							true, businessScenarioId);

					Variable.getInstance()
							.setVariableValue(
									Variable.getInstance()
											.generateSysVarName(
													SystemVariables.AFT_CURBUSINESSSCENARIODESCRIPTION),
									true, description);
				} else {
					Variable.getInstance()
							.setVariableValue(
									Variable.getInstance()
											.generateSysVarName(
													SystemVariables.AFT_CURRESBUSINESSSCENARIOID),
									true, businessScenarioId);

					Variable.getInstance()
							.setVariableValue(
									Variable.getInstance()
											.generateSysVarName(
													SystemVariables.AFT_CURRESBUSINESSSCENARIODESCRIPTION),
									true, description);
				}

				// Set the system variable AFT_TotalBusinessScenarios by
				// incrementing previous value...
				Variable.getInstance().incSysVarValue(
						SystemVariables.AFT_TOTALBUSINESSSCENARIOS);

				if (testScenario.getTestCaseDetails().size() > 0) {
					executeTestCases(testScenario, isReusableScenario);

				} else {
					LOGGER.error("No steps found on test step sheet ["
							+ testStepsSheetName
							+ "] for Business scenario Id ["
							+ businessScenarioId + "]");
				}

			} else if (executeFlag.equalsIgnoreCase("N")) {
				LOGGER.info("Execution flag for business scenario id ["
						+ businessScenarioId + "], description [" + description
						+ "] is [" + executeFlag
						+ "]. This scenario will not be executed.");
			} else {
				LOGGER.info("Execution flag for business scenario id ["
						+ businessScenarioId + "], description [" + description
						+ "] is [" + executeFlag
						+ "]. Ignoring business scenario.");
			}
			LOGGER.info("Completed execution of Single functional business scenarios and test steps for #"
					+ businessScenarioId);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Find Row Id in the Test Case list for the supplied TestCaseId
	 * 
	 * @param businessScenarioId
	 *            business scenario id to search for
	 * 
	 * @param testCaseId
	 *            Test Case id to search for
	 * @param scenario
	 *            test scenario
	 * @return test case position in test steps list
	 */
	public int getTestCasePositionId(String businessScenarioId,
			String testCaseId, TestScenario scenario) {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		int testCaseRowID = -1;
		if (isFileSystemRequest) {
			int testCaseCount = scenario.getTestCaseDetails().size() - 1;

			for (int iRow = 0; iRow <= testCaseCount; iRow++) {
				TestCase searchedTestCase = scenario.getTestCaseDetails().get(
						iRow);
				if (searchedTestCase.getTestCaseId().compareToIgnoreCase(
						testCaseId) == 0) {
					testCaseRowID = iRow;
					break;
				}
			}
		} else {
			Map<String, TestStep> steps = scenario.getTestCase().getSteps();
			for (Map.Entry<String, TestStep> step : steps.entrySet()) {
				if (step.getKey().compareToIgnoreCase(testCaseId) == 0) {
					testCaseRowID = Integer.parseInt(step.getKey());
					break;
				}
			}
		}
		if (testCaseRowID == -1) {
			LOGGER.info("Looks like no match found for Business Scenario Id ["
					+ businessScenarioId + "], test case Id [" + testCaseId
					+ "]");
		}

		return testCaseRowID;
	}

	/**
	 * Find Row Id in the Test Step list for the supplied TestStepId
	 * 
	 * @param businessScenarioId
	 *            business scenario id to search for
	 * 
	 * @param testStepId
	 *            Test Step id to search for
	 * @return test case position in test steps list
	 */
	public int getTestStepPositionId(String businessScenarioId,
			String testStepId) {
		int testStepRowID = -1;

		// get the test scenario based on businessScenarioId
		TestScenario scenario = getTestScenario(businessScenarioId);
		int testCaseCount = scenario.getTestCaseDetails().size() - 1;
		for (int iRow = 0; iRow <= testCaseCount; iRow++) {
			TestCase searchedTestCase = scenario.getTestCaseDetails().get(iRow);
			for (TestStep step : searchedTestCase.getTestStepDetails()) {
				if (step.getStepId().compareToIgnoreCase(testStepId) == 0) {
					testStepRowID = iRow;
					break;
				}
			}

		}

		if (testStepRowID == -1) {
			LOGGER.info("Looks like no match found for Business Scenario Id ["
					+ businessScenarioId + "], test case Id [" + testStepId
					+ "]");
		}

		return testStepRowID;
	}

	/**
	 * Get the TestScenario based on scenarioId
	 * 
	 * @param scenarioId
	 *            scenarioId
	 * @return TestScenario
	 * @throws AFTException
	 */
	private TestScenario getTestScenario(String scenarioId) {
		TestScenario testScenario = null;
		for (TestScenario scenario : testScenarios) {
			if (scenario.getBusinessScenarioId()
					.compareToIgnoreCase(scenarioId) == 0) {
				testScenario = scenario;
			}
		}
		return testScenario;
	}

	/**
	 * Get the Reusable TestScenario based on scenarioId
	 * 
	 * @param scenarioId
	 *            scenarioId
	 * @return testScenario
	 */
	public TestScenario getReusableTestScenario(String scenarioId) {
		TestScenario testScenario = null;
		for (TestScenario scenario : reusableTestScenarios) {
			if (scenario.getBusinessScenarioId()
					.compareToIgnoreCase(scenarioId) == 0) {
				testScenario = scenario;
			}
		}
		return testScenario;
	}

	/**
	 * execute method implementation
	 * 
	 */
	public void execute() throws AFTException {
		boolean executedTestSetCleanupScenario = false;
		try {
			// executes all the business scenarios in a test suite.
			executeTestScenarios();
		} finally {
			// Executing Test Set cleanup reusable scenario
			if ((!testSetCleanupIDs.isEmpty())
					&& (!executedTestSetCleanupScenario)) {

				try {
					executeReusableScenario(testSetCleanupIDs,
							"Test Set level cleanup");
					executedTestSetCleanupScenario = true;
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
				}
			}
		}

		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
		ReportGenerator.getInstance().setEndTime(dateFormat.format(new Date()));
		ReportGenerator.getInstance().setEndOfTestSuite(true);
		ReportGenerator.getInstance().generateXmlReport();
		// It interrupts thread of frame and shutdown window
		ScrollableFrame.getInstance().disposeFrame();

		LOGGER.info("--------------------------Total test Steps Executed ["
				+ getTotalTestStepsExecuted() + "]--------------------------");
		LOGGER.info("--------------------------Total verify test Steps Executed ["
				+ getVerifyTestStepsExecutedList().size()
				+ "]--------------------------");
		int totalTestData = 0;
		if (testDataSheetList != null) {
			for (Map.Entry<String, Integer> entry : testDataSheetList
					.entrySet()) {
				totalTestData += entry.getValue();
			}
		}
		LOGGER.info("--------------------------Total test data rows traversed ["
				+ totalTestData + "]--------------------------");
		// Calling exit Test Suite Runner to clear all dynamic variables and
		// data base instances
		Helper.getInstance().exitTestSuiteRunner(testSuiteExecutionStartTime);
		LOGGER.info("Completed execution of functional business scenarios and test steps...");
	}

	/**
	 * Executes all the business scenarios in a test suite.
	 * 
	 * @throws AFTException
	 */
	private void executeTestScenarios() throws AFTException {
		String businessScenarioId;
		AFTException applException = null;
		boolean executedTestSetInitializationScenario = false;
		// Define a variable to initialize to testcase start time for capturing
		// test case execution time
		long testCaseStartTime;
		String testFaliureEmailNotif = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.TEST_FAILURE_EMAILNOTIF);

		LOGGER.info("Starting execution of functional business scenarios and test steps in test suite");
		int scenariosCount = testScenarios.size() - 1;
		// Loop here for executable scenarios
		for (int iRow = 0; iRow <= scenariosCount; iRow++) {
			// Read business scenario id...
			TestScenario testScenario = testScenarios.get(iRow);
			businessScenarioId = testScenario.getBusinessScenarioId();
			LOGGER.trace("Read businessScenarioId [" + businessScenarioId + "]");
			// validate the test scenario
			boolean executeBusinessScenario = validateTestScenario(testScenario);
			if (executeBusinessScenario || jumpToExec) {
				resetTestStepCount(testScenario);
				testCaseStartTime = System.currentTimeMillis();

				try {
					executedTestSetInitializationScenario = executeTestSetInitializationReusableScenario(executedTestSetInitializationScenario);
					if (StringUtils.isNotBlank(scenarioInitializationIDs)) {
						setScenarioInitialization(true);
					}
					// Execute Business Scenario Initialization
					executeReusableScenario(scenarioInitializationIDs,
							"scenario level initialization");
					setScenarioInitialization(false);

					// Execute all test cases and test steps in a single
					// business scenario
					executeSingleBusinessScenario(testScenario, false);

					// unload if any dynamic OR was loaded...
					unLoadObjectRepository();

				} catch (AFTException ae) {
					applException = ae;

				} finally {
					if (!isTestSetInitializationFailed) {
						// Execute Business Scenario Cleanup
						executeScenarioCleanup(testScenario, applException,
								testCaseStartTime, testFaliureEmailNotif);
					}

					// On Error Recovery handling...
					if (applException != null) {
						boolean onErrorHandling = isScenarioErrorConfigured();
						if (onErrorHandling) {
							break;
						}
						// reset the applException object back to null
						applException = null;
					}

					// check if current execution should be terminated as a
					// result
					// of user calling a terminate specific annotation
					//
					if (Helper.getInstance().terminateCurrentExecution(
							Constants.TESTSUITE)) {
						LOGGER.info("It seems user has called '"
								+ Annotations.TERMINATECURRENTTESTSUITE
								+ "' annotation, quitting current test suite ["
								+ testSuitePath + "]");
						break;
					}

					// Now that we have executed the next scenario, let us
					// reset
					// this flag.
					setJumpToExec(false);
				}
				// checking if the JumpTotestCase has set the scenario ID
				if (isJumpTo()) {
					LOGGER.info("It seems user has called JumpTo annotation. Let us reset the business Scenario Id to ["
							+ jumpToBusinessScenarioId + "]");
					iRow = jumpToBusinessScenarioRowId - 1;

					jumpTo = false;
					setJumpToExec(true);
				}
			}
		}
	}

	/**
	 * This method will unload objectRepositroy
	 * 
	 * @throws AFTException
	 */
	private void unLoadObjectRepository() throws AFTException {
		if (EngineManager.getInstance().getCurrentExecutionEngine() != null) {
			EngineManager.getInstance().getCurrentExecutionEngine()
					.unLoadObjectRepository();
		} else if (ObjectRepositoryManager.getInstance()
				.isObjectRepositoryLoaded()) {
			ObjectRepositoryManager.getInstance().unLoadLocalObjectRepository();
		}
	}

	/**
	 * This method will execte test set initialization reusable scenario
	 * 
	 * @param executedTestSetInitializationScenario
	 *            executedTestSetInitializationScenario
	 * @return boolean
	 * @throws AFTException
	 */
	private boolean executeTestSetInitializationReusableScenario(
			boolean executedTestSetInitializationScenario) throws AFTException {
		boolean testSetIntialization = executedTestSetInitializationScenario;
		// Executing TestSet Initialization reusable scenario only
		// once before first business scenario with execution
		// flag
		// 'Y'
		try {
			if ((!testSetInitializationIDs.isEmpty())
					&& (!executedTestSetInitializationScenario)) {

				setTestSetInitialization(true);
				executeReusableScenario(testSetInitializationIDs,
						"Test Set level Initialization");
				testSetIntialization = true;

			}
			setTestSetInitialization(false);
		} catch (AFTException ae) {
			isTestSetInitializationFailed = true;
			throw ae;
		}
		return testSetIntialization;
	}

	/**
	 * This method will reset test step count to zero if jump to is false
	 * 
	 * @param testScenario
	 *            testScenario
	 */
	private void resetTestStepCount(TestScenario testScenario) {
		if (jumpToExec) {
			LOGGER.info("Executing Jump to scenario #"
					+ testScenario.getBusinessScenarioId());
		} else {
			// Since we are starting a new business scenario
			// execution,
			// let us reset the step count to 0
			setCurrentBusinessScenarioTestStepCount(0);
			LOGGER.info("executing business scenario #"
					+ testScenario.getBusinessScenarioId());
		}

		initTestScenarioReportingObject(testScenario.getBusinessScenarioId(),
				testScenario.getBusinessScenarioDesc().trim(), testScenario
						.getTestScenarioRequirementId().trim(),
				testScenario.getIdTestScenario());
	}

	/**
	 * This method will check if on error is configured at test scenario or not.
	 * 
	 * @return boolean
	 * @throws AFTException
	 */
	private boolean isScenarioErrorConfigured() throws AFTException {
		String[] onError = getOnErrorValue();
		boolean onErrorHandling = false;
		if ((onError[1]
				.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSCENARIO) != 0)
				&& (onError[1]
						.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) != 0)
				&& (onError[1]
						.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTCASE) != 0)) {
			onErrorHandling = true;
		}

		if (onErrorHandling) {
			LOGGER.info("User has defined '"
					+ onError[0]
					+ "' to ["
					+ onError[1]
					+ "]. This business scenario execution will be stopped and test execution will continue as per '"
					+ onError[0] + "' configuration.");

		}
		return onErrorHandling;
	}

	/**
	 * This method will validate the test Scenario
	 * 
	 * @param testScenario
	 *            testScenario
	 * @throws AFTException
	 * @return boolean
	 */
	private boolean validateTestScenario(TestScenario testScenario)
			throws AFTException {
		boolean executeBusinessScenario = false;
		String executeFlag = testScenario.getExecutionFlag();
		String businessScenarioCategory = testScenario.getCategory();
		String businessScenarioId = testScenario.getBusinessScenarioId();
		if (null != businessScenarioId
				&& !businessScenarioId.equalsIgnoreCase("")
				&& !businessScenarioId.equalsIgnoreCase("novalue")) {
			executeBusinessScenario = true;
		} else {
			LOGGER.warn("Business scenarioId ["
					+ businessScenarioId
					+ "] is Null for execution. This business scenario will be skipped.");
			throw new AFTException(
					"Business scenarioId ["
							+ businessScenarioId
							+ "] is Null for execution. This business scenario will be skipped.");
		}
		if (executeBusinessScenario
				&& testScenario.getTestCaseDetails().size() > 0) {
			executeBusinessScenario = true;

		} else {
			LOGGER.warn("Business scenarioId ["
					+ businessScenarioId
					+ "] is blank in Test Steps for execution. This business scenario will be skipped.");
			throw new AFTException(
					"Business scenarioId ["
							+ businessScenarioId
							+ "] is Null for execution. This business scenario will be skipped.");
		}

		if (executeBusinessScenario && executeFlag.equalsIgnoreCase("Y")) {
			if (executionCategory.length() > 0) {
				if (businessScenarioCategory.toLowerCase().contains(
						executionCategory.toLowerCase())) {
					executeBusinessScenario = true;
				} else {
					LOGGER.debug("Category specified in test batch ["
							+ executionCategory
							+ "] does not match category in test suite ["
							+ businessScenarioCategory
							+ "] for business scenario [" + businessScenarioId
							+ "]. This business scenario will be skipped.");
					executeBusinessScenario = false;
				}
			} else {
				LOGGER.info("User had not specified any category in Test Batch. Hence all business scenario will be executed");
				executeBusinessScenario = true;
			}
		} else {
			LOGGER.debug("Business scenario ["
					+ businessScenarioId
					+ "] is marked as 'N' for execution. This business scenario will be skipped.");
			executeBusinessScenario = false;
		}

		return executeBusinessScenario;
	}

	/**
	 * This method will execute Scenario Cleanup
	 * 
	 * @param testScenario
	 *            testScenario
	 * @param applException
	 *            applException
	 * @param testCaseStartTime
	 *            testCaseStartTime
	 * @param testFaliureEmailNotif
	 *            testFaliureEmailNotif
	 */
	private void executeScenarioCleanup(TestScenario testScenario,
			AFTException applException, long testCaseStartTime,
			String testFaliureEmailNotif) throws AFTException {

		/*
		 * Reset the system variable AFT_TerminateCurrentTest to default value
		 */
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSCENARIO),
				true, "false");

		try {
			// Execute Business Scenario Cleanup
			executeReusableScenario(scenarioCleanupIDs,
					"scenario level cleanup");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}

		setTestScenarioReportingResults(testScenario, applException,
				testCaseStartTime, testFaliureEmailNotif);

		// Save the intermediate report and log intermediate
		// results in logs...
		LOGGER.info("--------------------------Test Steps Executed ["
				+ getTestStepsExecutedList().size()
				+ "]--------------------------");
		totalTestStepsExecuted += getTestStepsExecutedList().size();
		// set the total steps executed for all scenarios
		setTotalTestStepsExecuted(totalTestStepsExecuted);
		getTestStepsExecutedList().clear();

		// Set testSuite execution duration...
		//
		long tcSuiteTotalTime = System.currentTimeMillis()
				- testSuiteExecutionStartTime;
		// set execution time for the test suite
		ReportGenerator.getInstance().getLatestTestSuite()
				.setExecutionTime(tcSuiteTotalTime);
		ReportGenerator.getInstance().getLatestTestSuite().summarizeResults();
		ReportGenerator.getInstance().setEndOfTestSuite(false);
		ReportGenerator.getInstance().generateXmlReport();
	}

	/**
	 * Creates and Initializes TestCase object for reporting
	 * 
	 * @param businessScenarioId
	 *            BusinessScenarios id being executed
	 * @param testCaseDesc
	 *            testcase Description as specified in test suite
	 * @param testCaseRequirementId
	 *            testCaseRequirementId
	 * @param idTestScenario
	 *            idTestScenario
	 */
	private void initTestScenarioReportingObject(String businessScenarioId,
			String testCaseDesc, String testCaseRequirementId,
			String idTestScenario) {
		// Initialize TestCase object for reporting purpose...
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
		TestScenario testScenario = new TestScenario();
		testScenario.setBusinessScenarioId(businessScenarioId);
		testScenario.setBusinessScenarioDesc(testCaseDesc);
		testScenario.setTestScenarioRequirementId(testCaseRequirementId);
		testScenario.setTestScenarioStartTime(Util.getInstance()
				.getCurrentTime());
		testScenario.setIdTestScenario(idTestScenario);
		testScenario.setStartTime(dateFormat.format(new Date()));
		// add to report generator instance
		ReportGenerator.getInstance().getLatestTestSuite()
				.addTestScenario(testScenario);

		// Initialize TestCase object for TCM reporting purpose...
		TestScenario tcmTestScenarioObj = new TestScenario();
		tcmTestScenarioObj.setBusinessScenarioId(businessScenarioId);
		tcmTestScenarioObj.setBusinessScenarioDesc(testCaseDesc);
		tcmTestScenarioObj.setTestScenarioRequirementId(testCaseRequirementId);
		tcmTestScenarioObj.setTestScenarioStartTime(Util.getInstance()
				.getCurrentTime());
		tcmTestScenarioObj.setIdTestScenario(idTestScenario);
		// also initialize the TCM report generator with the new test case for
		// logging results to test case management tool
		TCMReportGenerator.getInstance().getTestSuite()
				.addTestScenario(tcmTestScenarioObj);
	}

	/**
	 * Set testCase execution results and execution time
	 * 
	 * @param testScenario
	 *            testScenario
	 * @param applException
	 *            Application exception object
	 * @param testCaseStartTime
	 *            TestCase execution start time
	 * @param testFaliureEmailNotif
	 *            testFaliureEmailNotif flag value
	 */
	private void setTestScenarioReportingResults(TestScenario testScenario,
			AFTException applException, long testCaseStartTime,
			String testFaliureEmailNotif) {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		TestScenario businessScenario = null;
		String result = "PASS";
		// Set the results for both report generation as well as logging to TCM
		// tool

		businessScenario = getReportBusinessScenario(testScenario,
				isFileSystemRequest);
		String executionResult = ReportGenerator.getInstance()
				.getLatestTestSuite().getTestScenario().getExecutionResult();
		if (applException == null) {

			// No exception raised. This means the test case execution is
			// successful. Mark it pass.
			// if (isFileSystemRequest) {

			if (executionResult != null
					&& executionResult.equalsIgnoreCase("FAIL")) {
				result = "FAIL";
			}
			// }
		} else {
			if (!getTestBatchRunner().isDisableStandardReportingFlag()) {
				result = "FAIL";
			} else if (getTestBatchRunner().isDisableStandardReportingFlag()
					&& executionResult != null
					&& executionResult.equalsIgnoreCase("FAIL")) {
				result = "FAIL";
			}
			if (testFaliureEmailNotif.equalsIgnoreCase("Yes")) {
				try {
					Notifications.getInstance().sendTestFailedNotification(
							ReportGenerator.getInstance().getLatestTestSuite(),
							applException);
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
				}

			}
		}
		long tcEndTime = System.currentTimeMillis();
		long execTime = tcEndTime - testCaseStartTime;
		if (businessScenario != null) {
			businessScenario.setExecutionResult(result);
			businessScenario.setExecutionTime(execTime);
			DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
			businessScenario.setEndTime(dateFormat.format(new Date()));
		}
		TCMReportGenerator.getInstance().getTestSuite().getTestScenario()
				.setExecutionTime(execTime);
	}

	/**
	 * Gets the latest test scenario from report generator.
	 * 
	 * @param testScenario
	 *            testScenario
	 * @param isFileSystemRequest
	 *            isFileSystemRequest
	 * @return businessScenario
	 */
	private TestScenario getReportBusinessScenario(TestScenario testScenario,
			boolean isFileSystemRequest) {
		TestScenario businessScenario = null;
		if (!isFileSystemRequest) {
			List<TestScenario> testScenarioList = ReportGenerator.getInstance()
					.getLatestTestSuite().getTestScenariosArr();
			for (TestScenario scenario : testScenarioList) {
				if ((scenario.getIdTestScenario() != null && testScenario
						.getIdTestScenario() != null)
						&& (scenario.getIdTestScenario().equals(testScenario
								.getIdTestScenario()))) {
					businessScenario = scenario;
					break;
				}
			}
		} else {
			businessScenario = ReportGenerator.getInstance()
					.getLatestTestSuite().getTestScenario();
		}
		return businessScenario;
	}

	/**
	 * This method will system variables
	 * 
	 * @throws AFTException
	 */
	public void initSystemVariables() throws AFTException {
		// Init Test Suite path system variable

		// Retrieve the individual file path elements
		//
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (isFileSystemRequest) {
			String path[] = Util.getInstance().splitFilePath(testSuitePath);

			// replace file name in the file path with blank to retrieve just
			// the
			// path
			//
			String testSuiteFilePath = testSuitePath.replace(
					path[path.length - 1], "");

			// Save AFT_TestSuitePath value to test batch file path
			//
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_TESTSUITEPATH), true,
					testSuiteFilePath);
			// Retrieve the value and print to validate that it is saved
			// correctly
			//
			LOGGER.trace("Value for system variable [AFT_TestSuitePath] set to ["
					+ Helper.getInstance().getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_TESTSUITEPATH)) + "]");

			// Init Test Suite file name system variable

			// replace file name in the file path with blank to retrieve just
			// the
			// path
			//
			String testSuiteFileName = path[path.length - 1];

			// Save AFT_TestSuiteFileName value to test batch file path
			//
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_TESTSUITEFILENAME), true,
					testSuiteFileName);
			// Retrieve the value and print to validate that it is saved
			// correctly
			//
			LOGGER.trace("Value for system variable [AFT_TestSuiteFileName] set to ["
					+ Helper.getInstance().getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_TESTSUITEFILENAME))
					+ "]");

			// Set test data file path
			//
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_TESTDATAFILEPATH), true,
					testDataFilePath);
		}

		// Init all variables related to test data...
		//
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTDATASHEETCOUNT), true, "-1");

		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_ISENDOFTESTDATA), true, "true");

		/*
		 * Set the system variable AFT_TerminateCurrentScenario back with
		 * default value at start...
		 */
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSCENARIO),
				true, "false");

		/*
		 * Set the system variable AFT_TerminateAllTest back with default value
		 * at start...
		 */
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSUITE), true,
				"false");

		// root path of the project
		String rootPath = "";
		try {
			// Initialize startup system variables
			//
			rootPath = new java.io.File(".").getCanonicalPath();
		} catch (IOException io) {
			LOGGER.error("Exception:", io);
			throw new AFTException(io);
		}

		// Init Root Path system variable
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_ROOTPATH), true, rootPath);
		LOGGER.debug("Value for system variable [AFT_RootPath] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_ROOTPATH)) + "]");
	}

	/**
	 * Getter for test data generator object
	 * 
	 * @return test data reader object
	 */
	public ExcelRead getTestDataReader() {
		return testDataReader;
	}

	/**
	 * This method validates business scenario and test case to be jumped to and
	 * set the execution to jumpTo the specified business scenario and test case
	 * if it is valid.
	 * 
	 * @param scenarioId
	 *            Business scenario to jump to
	 * @param testCaseId
	 *            test case to jump to
	 * @return true/false
	 * @throws AFTException
	 */
	public boolean setForJumpTo(String scenarioId, String testCaseId)
			throws AFTException {

		boolean retVal = false;
		String reusableScenarioId = executingResuableTestScenario
				.get(executingResuableTestScenario.size() - 1);
		LOGGER.debug("Retrieving what we are executing currently - primary suite or reusable test suite. Got ["
				+ reusableScenarioId
				+ "] at top of [executingResuableTestScenario] stack.");

		if (reusableScenarioId.length() <= 0) {
			LOGGER.debug("User is executing a test scenario in primary suite, let us search for ["
					+ scenarioId + "],[" + testCaseId + "]");

			// User is not executing a reusable test scenario. Let us check look
			// for the scenario and test case in main test suite
			jumpToBusinessScenarioRowId = getScenarioPositionId(scenarioId,
					testScenarios);
			// get the test scenario based on businessScenarioId
			TestScenario scenario = getTestScenario(scenarioId);
			jumpToTestCaseRowId = getTestCasePositionId(scenarioId, testCaseId,
					scenario);
			setJumpToTestCaseRowId(jumpToTestCaseRowId);
		} else {
			// looks like user is executing a reusable test scenario. Let us
			// check if it is the same scenario that user is trying to execute
			// and then match for test case
			LOGGER.debug("User is executing a test scenario in reusable suite, let us search for ["
					+ scenarioId + "],[" + testCaseId + "]");

			if (reusableScenarioId.compareToIgnoreCase(scenarioId) == 0) {
				jumpToBusinessScenarioRowId = getScenarioPositionId(scenarioId,
						reusableTestScenarios);
				// get the test scenario based on businessScenarioId
				TestScenario scenario = getReusableTestScenario(scenarioId);
				jumpToTestCaseRowId = getTestCasePositionId(scenarioId,
						testCaseId, scenario);
				setJumpToTestCaseRowId(jumpToTestCaseRowId);
			} else {
				throw new AFTException(
						"jumpTo annotation is only supported to jumpTo another test case within the currently executed test scenario when executing a reusable test scenario. Currently executed reusable business scenario ["
								+ Helper.getInstance()
										.getActionValue(
												Variable.getInstance()
														.generateSysVarName(
																SystemVariables.AFT_CURBUSINESSSCENARIOID))
								+ "], jumpTo test scenario id ["
								+ scenarioId
								+ "], test case id ["
								+ testCaseId
								+ "]. Please correct this error and re-execute the test.");
			}
		}

		if (jumpToBusinessScenarioRowId >= 0 && jumpToTestCaseRowId >= 0) {
			LOGGER.info("Found valid business scenario id [" + scenarioId
					+ "], test case id [" + testCaseId
					+ "] and initialized for execution.");

			jumpToBusinessScenarioId = scenarioId;
			jumpToTestCaseId = testCaseId;
			jumpTo = true;
			retVal = true;
		} else {
			throw new AFTException(
					"Business scenario id ["
							+ scenarioId
							+ "] OR test case id ["
							+ testCaseId
							+ "] could not be found."
							+ " Please validate the business scenario Id and test Case Id in test suite and re-execute the test.");
		}

		return retVal;
	}

	/**
	 * Return the current test data row id for the given sheet
	 * 
	 * @param sheetName
	 *            test data sheet name
	 * 
	 * @return current row id for the given sheet
	 * @throws AFTException
	 */
	public int getTestDataCurrentRowId(String sheetName) throws AFTException {
		int currTestDataRowID = -1;
		String currRowId = null;
		// Get the current row id for the given sheet
		currRowId = testDataRowIds.get(sheetName);
		if (currRowId == null) {
			currRowId = "1";
		}
		// convert this rowid to integer value if it is not null
		if (currRowId != null && currRowId.length() > 0) {
			currTestDataRowID = Integer.parseInt(currRowId);
		} else {
			LOGGER.error("Sheetname [" + sheetName + "] not found");
			throw new AFTException("Sheetname [" + sheetName + "] not found");
		}

		return currTestDataRowID;
	}

	/**
	 * @param sheetName
	 *            test data sheet name
	 * @param rowId
	 *            row id in the test data sheet
	 */
	public void setTestDataRowId(String sheetName, int rowId) {
		String testDataRowID = Integer.toString(rowId);
		testDataRowIds.put(sheetName, testDataRowID);
	}

	/**
	 * @return jumpto flag
	 */
	public boolean isJumpTo() {
		return jumpTo;
	}

	/**
	 * @param jumpTo
	 *            the jumpTo to set
	 */
	public void setJumpTo(boolean jumpTo) {
		this.jumpTo = jumpTo;
	}

	/**
	 * @param jumpToExec
	 *            the jumpToExec to set
	 */
	private void setJumpToExec(boolean jumpToExec) {
		this.jumpToExec = jumpToExec;
	}

	/**
	 * @return reusable test scenarios list
	 */
	public List<TestScenario> getReusableTestSuiteScenarios() {
		return reusableTestScenarios;
	}

	/**
	 * @return resuable test suite path
	 */
	public String getReusableTestSuitePath() {
		return reusableTestSuitePath;
	}

	/**
	 * @return current business scenario test steps count
	 */
	public int getCurrentBusinessScenarioTestStepCount() {
		return currentBusinessScenarioTestStepCount;
	}

	/**
	 * @param currentBusinessScenarioTestStepCount
	 *            current business scenario test steps count
	 */
	public void setCurrentBusinessScenarioTestStepCount(
			int currentBusinessScenarioTestStepCount) {
		this.currentBusinessScenarioTestStepCount = currentBusinessScenarioTestStepCount;
	}

	/**
	 * @return test batch object
	 */
	public TestBatchRunner getTestBatchRunner() {
		return testBatchRunner;
	}

	/**
	 * @param speed
	 *            execution speed
	 */
	public void setExecutionSpeed(int speed) {
		executionSpeed = speed;
	}

	/**
	 * @return execution speed
	 */
	public int getExecutionSpeed() {
		return executionSpeed;
	}

	/**
	 * @return testStepsExecutedList
	 */
	public List<TestStep> getTestStepsExecutedList() {
		return testStepsExecutedList;
	}

	/**
	 * @param testStepsExecutedList
	 *            testStepsExecutedList
	 */
	public void setTestStepsExecutedList(List<TestStep> testStepsExecutedList) {
		this.testStepsExecutedList = testStepsExecutedList;
	}

	/**
	 * @return totalTestStepsExecuted
	 */
	public int getTotalTestStepsExecuted() {
		return totalTestStepsExecuted;
	}

	/**
	 * @param totalTestStepsExecuted
	 *            totalTestStepsExecuted
	 */
	public void setTotalTestStepsExecuted(int totalTestStepsExecuted) {
		this.totalTestStepsExecuted = totalTestStepsExecuted;
	}

	/**
	 * @return reusableTestStepsExecutedList
	 */
	public Set<String> getReusableTestStepsExecutedList() {
		return reusableTestStepsExecutedList;
	}

	/**
	 * @param reusableTestStepsExecutedList
	 *            reusableTestStepsExecutedList
	 */
	public void setReusableTestStepsExecutedList(
			Set<String> reusableTestStepsExecutedList) {
		this.reusableTestStepsExecutedList = reusableTestStepsExecutedList;
	}

	/**
	 * @return verifyTestStepsExecutedList
	 */
	public List<TestStep> getVerifyTestStepsExecutedList() {
		return verifyTestStepsExecutedList;
	}

	/**
	 * @param verifyTestStepsExecutedList
	 *            verifyTestStepsExecutedList
	 */
	public void setVerifyTestStepsExecutedList(
			List<TestStep> verifyTestStepsExecutedList) {
		this.verifyTestStepsExecutedList = verifyTestStepsExecutedList;
	}

	/**
	 * @return testCaseExecutedList
	 */
	public Set<String> getTestCaseExecutedList() {
		return testCaseExecutedList;
	}

	/**
	 * @param testCaseExecutedList
	 *            testCaseExecutedList
	 */
	public void setTestCaseExecutedList(Set<String> testCaseExecutedList) {
		this.testCaseExecutedList = testCaseExecutedList;
	}

	/**
	 * @return isDataTraversed flag
	 */
	public boolean isDataTraversed() {
		return isDataTraversed;
	}

	/**
	 * @param isDataTraversed
	 *            isDataTraversed
	 */
	public void setDataTraversed(boolean isDataTraversed) {
		this.isDataTraversed = isDataTraversed;
	}

	/**
	 * @return testDataSheetList
	 */
	public Map<String, Integer> getTestDataSheetList() {
		return testDataSheetList;
	}

	/**
	 * @param testDataSheetList
	 *            testDataSheetList
	 */
	public void setTestDataSheetList(Map<String, Integer> testDataSheetList) {
		this.testDataSheetList = testDataSheetList;
	}

	/**
	 * @return moveToDataRow flag
	 */
	public boolean isMoveToDataRow() {
		return moveToDataRow;
	}

	/**
	 * @param moveToDataRow
	 *            moveToDataRow
	 */
	public void setMoveToDataRow(boolean moveToDataRow) {
		this.moveToDataRow = moveToDataRow;
	}

	/**
	 * @param dbParameterLst
	 *            dbParameterLst
	 */
	public void setDbParameterLst(String dbParameterLst) {
		this.dbParameterLst = dbParameterLst;
	}

	/**
	 * @return dbParameterLst
	 */
	public String getDbParameterLst() {
		return dbParameterLst;
	}

	/**
	 * @return dbConnCounter
	 */
	public int getDbConnCounter() {
		return dbConnCounter;
	}

	/**
	 * @param dbConnCounter
	 *            dbConnCounter
	 */
	public void setDbConnCounter(int dbConnCounter) {
		this.dbConnCounter = dbConnCounter;
	}

	/**
	 * @return dbConnIdentifier
	 */
	public String getDbConnIdentifier() {
		return dbConnIdentifier;
	}

	/**
	 * @param dbConnIdentifier
	 *            dbConnIdentifier
	 */
	public void setDbConnIdentifier(String dbConnIdentifier) {
		this.dbConnIdentifier = dbConnIdentifier;
	}

	/**
	 * @return onDBErrorValue
	 */
	public String getOnDBErrorValue() {
		return onDBErrorValue;
	}

	/**
	 * @param onDBErrorValue
	 *            onDBErrorValue
	 */
	public void setOnDBErrorValue(String onDBErrorValue) {
		this.onDBErrorValue = onDBErrorValue;
	}

	/**
	 * @return the jumpToTestCaseRowId
	 */
	public int getJumpToTestCaseRowId() {
		return jumpToTestCaseRowId;
	}

	/**
	 * @param jumpToTestCaseRowId
	 *            the jumpToTestCaseRowId to set
	 */
	public void setJumpToTestCaseRowId(int jumpToTestCaseRowId) {
		this.jumpToTestCaseRowId = jumpToTestCaseRowId;
	}

	/**
	 * Verify the reusable scenario is valid or not
	 * 
	 * @param resuableScenarioId
	 *            Reusable scenarios being executed
	 * @param configarationLevel
	 *            Batch level or Scenario level
	 * @throws AFTException
	 */
	private void verifyReusableScenario(String resuableScenarioId,
			String configarationLevel) throws AFTException {

		int businessScenarioPositionId = 0;

		// Verify the reusable scenario is valid
		if (resuableScenarioId.contains(",") && (!resuableScenarioId.isEmpty())) {
			String[] resuableScenarioIds = resuableScenarioId.split(",");
			for (String scenarioId : resuableScenarioIds) {

				businessScenarioPositionId = getScenarioPositionId(
						scenarioId.trim(), reusableTestScenarios);

				if (businessScenarioPositionId == -1) {
					String errMsg = "Invalid reusable scenario [" + scenarioId
							+ "] . Please check the value specified for "
							+ configarationLevel + " and rerun the test.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				} else {
					LOGGER.info(configarationLevel + " [" + resuableScenarioId
							+ "] is a valid reusable scenario.");
				}

			}
		} else if (!resuableScenarioId.isEmpty()) {

			businessScenarioPositionId = getScenarioPositionId(
					resuableScenarioId, reusableTestScenarios);

			if (businessScenarioPositionId == -1) {
				String errMsg = "Invalid reusable scenario ["
						+ resuableScenarioId
						+ "] . Please check the value specified for "
						+ configarationLevel + " and rerun the test.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			} else {
				LOGGER.info(configarationLevel + " [" + resuableScenarioId
						+ "] is a valid reusable scenario.");
			}
		}
	}

	/**
	 * Execute the reusable scenario
	 * 
	 * @param resuableScenarioId
	 *            Reusable scenarios being executed
	 * @param configarationLevel
	 *            Batch level or Scenario level
	 * @throws AFTException
	 */
	private void executeReusableScenario(String resuableScenarioId,
			String configarationLevel) throws AFTException {

		// Execute the reusable scenario
		if (resuableScenarioId.contains(",") && (!resuableScenarioId.isEmpty())) {
			String[] resuableScenarioIds = resuableScenarioId.split(",");
			for (String scenarioId : resuableScenarioIds) {

				LOGGER.info("Starting execution of [" + configarationLevel
						+ "] reusable scenario [" + scenarioId + "]");
				// get the TestScenario object based scenarioId.
				TestScenario testScenario = getReusableTestScenario(scenarioId);
				if (testScenario != null) {
					executeSingleBusinessScenario(testScenario, true);
				} else {
					testScenario = getTestScenario(scenarioId);
					executeSingleBusinessScenario(testScenario, false);
				}
			}
		} else if (!resuableScenarioId.isEmpty()) {

			LOGGER.info("Starting execution of " + configarationLevel
					+ " reusable scenario [" + resuableScenarioId + "]");
			// get the TestScenario object based scenarioId.
			TestScenario testScenario = getReusableTestScenario(resuableScenarioId);
			if (testScenario != null) {
				executeSingleBusinessScenario(testScenario, true);
			} else {
				testScenario = getTestScenario(resuableScenarioId);
				executeSingleBusinessScenario(testScenario, false);
			}
		}
	}

	/**
	 * Verify the batch, scenario level initialization and cleanup reusable
	 * scenarios
	 * 
	 * @throws AFTException
	 */
	private void verifyInitCleanUpScenarios() throws AFTException {

		// verify the Test Set Initialization reusable scenario is valid
		verifyReusableScenario(testSetInitializationIDs,
				"Test Set Initialization");

		// verify the Test Set clean up reusable scenario is valid
		verifyReusableScenario(testSetCleanupIDs, "Test Set Clean up");

		// verify the scenario Initialization reusable scenario is valid
		verifyReusableScenario(scenarioInitializationIDs,
				"scenario Initialization");

		// verify the scenario cleanup reusable scenario is valid
		verifyReusableScenario(scenarioCleanupIDs, "scenario  cleanup");
	}

	/**
	 * Get the On Error value and the Error handler, 0 - > Error handler and 1 -
	 * > Error value
	 * 
	 * @return onError
	 * @throws AFTException
	 */
	public String[] getOnErrorValue() throws AFTException {
		String[] onError = new String[2];
		try {
			if (getOnDBErrorValue() != null) {
				onError[0] = ConfigProperties.ONERROR_DB_CONNECTION;
				onError[1] = getOnDBErrorValue();
			} else {
				onError[0] = ConfigProperties.ONERROR;
				if (isTestSetInitialization()) {
					onError[1] = ConfigProperties.ONERROR_RESUMENEXTTESTSUITE;
					LOGGER.info("As Test set initialization failed setting '"
							+ onError[0]
							+ "' to ["
							+ onError[1]
							+ "]. This business scenario execution will be stopped and test execution will continue as per '"
							+ onError[0] + "' configuration.");
				} else if (isScenarioInitialization()) {
					onError[1] = ConfigProperties.ONERROR_RESUMENEXTTESTSCENARIO;
					LOGGER.info("As Test scenario initialization failed setting '"
							+ onError[0]
							+ "' to ["
							+ onError[1]
							+ "]. This business scenario execution will be stopped and test execution will continue as per '"
							+ onError[0] + "' configuration.");
					onError[1] = ConfigProperties.ONERROR_RESUMENEXTTESTSCENARIO;
				} else {
					onError[1] = ConfigProperties.getInstance()
							.getConfigProperty(ConfigProperties.ONERROR);
				}
				if (onError[1] == null) {
					onError[1] = ConfigProperties.DEFAULT_ONERROR;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new AFTException(e);
		}

		return onError;
	}

	/**
	 * Get the TestSet object
	 * 
	 * @return TestSet object
	 */
	public TestSet getTestSet() {
		return this.testSet;
	}

	/**
	 * @return the isJumpToStep
	 */
	public boolean isJumpToStep() {
		return isJumpToStep;
	}

	/**
	 * @param isJumpToStep
	 *            the isJumpToStep to set
	 */
	public void setJumpToStep(boolean isJumpToStep) {
		this.isJumpToStep = isJumpToStep;
	}

	/**
	 * @return the isTestSetInitialization
	 */
	public boolean isTestSetInitialization() {
		return isTestSetInitialization;
	}

	/**
	 * @param isTestSetInitialization
	 *            the isTestSetInitialization to set
	 */
	public void setTestSetInitialization(boolean isTestSetInitialization) {
		this.isTestSetInitialization = isTestSetInitialization;
	}

	/**
	 * @return the isScenarioInitialization
	 */
	public boolean isScenarioInitialization() {
		return isScenarioInitialization;
	}

	/**
	 * @param isScenarioInitialization
	 *            the isScenarioInitialization to set
	 */
	public void setScenarioInitialization(boolean isScenarioInitialization) {
		this.isScenarioInitialization = isScenarioInitialization;
	}

	/**
	 * Display scrollable frame.
	 * 
	 * @throws AFTException
	 */
	public void displayScrollableFrame() throws AFTException {
		// start the window frame
		LOGGER.debug("Starting scrollable frame");
		StringBuffer scrollText = new StringBuffer();
		scrollText.append("Test Scenario ");
		scrollText.append("[");
		scrollText.append(scenarioDescription);
		scrollText.append("], ");
		scrollText.append("Test Case ");
		scrollText.append("[");
		if (testCaseDescription
				.contains(Constants.TESTDATASTARTVARIABLEIDENTIFIER)
				|| testCaseDescription
						.contains(Constants.DYNAMICVARIABLEDELIMITER)) {
			testCaseDescription = Helper.getInstance().getActionValue(this,
					testCaseDescription);
		}
		scrollText.append(testCaseDescription);
		scrollText.append("]");
		ScrollableFrame.getInstance().start(scrollText.toString());
	}

	/**
	 * @return the isEndOfTestData
	 */
	public boolean isEndOfTestData() {
		return isEndOfTestData;
	}

	/**
	 * @param isEndOfTestData
	 *            the isEndOfTestData to set
	 */
	public void setEndOfTestData(boolean isEndOfTestData) {
		this.isEndOfTestData = isEndOfTestData;
	}

	/**
	 * @return the testCaseIdCount
	 */
	public int getTestCaseIdCount() {
		return testCaseIdCount;
	}

	/**
	 * @param testCaseIdCount
	 *            the testCaseIdCount to set
	 */
	public void setTestCaseIdCount(int testCaseIdCount) {
		this.testCaseIdCount = testCaseIdCount;
	}

	/**
	 * @return the totalTestStepExecTime
	 */
	public long getTotalTestStepExecTime() {
		return totalTestStepExecTime;
	}

	/**
	 * @param totalTestStepExecTime
	 *            the totalTestStepExecTime to set
	 */
	public void setTotalTestStepExecTime(long totalTestStepExecTime) {
		this.totalTestStepExecTime = totalTestStepExecTime;
	}
}
