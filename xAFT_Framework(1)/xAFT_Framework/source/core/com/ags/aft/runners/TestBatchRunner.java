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
 * Class: TestBatchRunner
 * 
 * Purpose: This class implements ITestRunner to execute one test batch.
 * Instantiated and called by main driver for executing configured test batch
 * sets
 */

package com.ags.aft.runners;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.PageErrors;
import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.Reporting.TCMReportGenerator;
import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.RuntimeProperties;
import com.ags.aft.common.Util;
import com.ags.aft.common.XMLParser;
import com.ags.aft.config.AppConfigProperties;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.testObjects.TestSet;
import com.ags.aft.testObjects.TestSuite;
import com.ags.aft.util.Helper;
import com.ags.aft.util.StackTestData;
import com.ags.aft.util.Variable;

/**
 * Test Batch runner implementation. Extends from AFTSeleniumBase and Implements
 * ITestRunner to execute one test suite. Instantiated and called by Driver
 * 
 */
public class TestBatchRunner implements ITestRunner {
	/**
	 * CONSTANT LOGGER Object Instance
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TestBatchRunner.class);

	// Test Batch specific information for use within this runner
	private java.util.Date testStartTime;
	private String testSetName;

	// execution engine name to be populated from test batch set
	private String executionEngineName;

	private boolean isExecutionCompleted;

	private Map<String, String> commandArgs = null;

	// private IxAFTEngine executionEngineObj = null;

	private TestSet dynamicTestSet;
	private boolean disableStandardReportingFlag = false;

	/**
	 * Custom TestBatchRunner constructor. Initializes the runner, Load config
	 * properties, sets required parameters, initializes system variables and
	 * starts selenium server
	 * 
	 * @param startTime
	 *            Test Start time
	 * @throws AFTException
	 * 
	 */
	public TestBatchRunner(java.util.Date startTime) throws AFTException {
		this.testStartTime = startTime;
		this.commandArgs = RuntimeProperties.getInstance().getFileCommandArgs();
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);

		// Init ReportGenerator Engine and set start datetime
		ReportGenerator.getInstance().setStartTime(testStartTime);

		// also set start time for the test suite in the TCM report
		// generator for logging results to test case management tool
		TCMReportGenerator.getInstance().setStartTime(
				dateFormat.format(testStartTime));

		// Load the resources files for random data returned by system
		// variables...
		StackTestData.getInstance().loadRandomDataResourceFiles();
		this.disableStandardReportingFlag = ConfigProperties.getInstance()
				.isStandardReportingDisabled();
	}

	/**
	 * This method will execute one test suite. Instantiated and called by
	 * Driver
	 * 
	 */
	public void execute() throws AFTException {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		try {
			LOGGER.info("Starting execution of test batch...");
			if (isFileSystemRequest) {
				executeFileSystemTestBatch();
			} else {
				executeDBTestBatch();
			}
			setExecutionCompleted(true);

			// Process test report
			processTestReport();

			LOGGER.info("Exiting execution of test batch...");

		} finally {
			// Generate Link Error Report
			PageErrors.getInstance().generatePageErrors();

			// Generate Spell Error Report
			PageErrors.getInstance().generateSpellErrors();

			if (isFileSystemRequest) {
				// Generate Spell & Link error result file
				PageErrors.getInstance().generateFinalXml();
			}
		}
	}

	/**
	 * Method to execute the File system test batch
	 * 
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	private void executeFileSystemTestBatch() throws AFTException {
		String testBatchfilePath;
		List<Map<String, String>> testBatchList = new ArrayList<Map<String, String>>();
		List<String> testSetNameList = new ArrayList<String>();
		List<TestSet> testSetList = null;

		// get the test batch file path
		testBatchfilePath = getTestBatchFilePath();
		LOGGER.info("Reading test batch set file [" + testBatchfilePath + "]");
		File file = new File(testBatchfilePath);
		String path = file.getPath();
		// Create xmlParser Object
		XMLParser xmlParser = new XMLParser();
		xmlParser.readXML(path);
		testBatchList = xmlParser.getAttributeNameList("TestSet");
		// Get the TestSet Names
		testSetNameList = xmlParser.getAttributeList("TestSet", "Name");
		// execute test batch
		LOGGER.info("Starting execution of test batch...");
		if (commandArgs != null
				&& commandArgs.get(Constants.BROWSERTYPE) == null) {
			// flag for multiple browsers
			boolean isMultipleBrowsers = false;
			// check if multiple browsers are found in test batch
			isMultipleBrowsers = checkIfMultipleBrowsersExists(testBatchList);
			// if test set having multiple browsers, then are calling
			if (isMultipleBrowsers) {
				// Verifying multiple browser values and creating multiple test
				// cases for each browser
				testBatchList = verifyAndCreateMultiBrowserTests(testBatchList,
						testSetNameList);
			}
		}
		// check if user has specified duplicate test batch names
		checkIfDuplicateTestBatchExists(testSetNameList);
		LOGGER.trace("Reading test batch set...");
		// Run the loop to read each test batch set from the list and call
		// test scenarios executor function
		LOGGER.info("Total test batch sets read from testbatch file are ["
				+ testBatchList.size() + "]");
		// execute the test batch
		for (int iBatchSetCnt = 0; iBatchSetCnt < testBatchList.size(); iBatchSetCnt++) {
			Map<String, String> testBatch = testBatchList.get(iBatchSetCnt);
			LOGGER.trace("Starting reading of testbatch from teMap");
			boolean testSetFound = false;

			String testName = testSetNameList.get(iBatchSetCnt);
			testSetFound = isTestSetMatchFound(testName);
			if (commandArgs != null && commandArgs.size() > 1 && !testSetFound) {
				continue;
			}
			// Check for every batch whether the ExecuteSuite Property is
			// Set to YES
			// If set to YES, then only execute the suite else skip the
			// batch
			String executeTestBatch = testBatch.get(Constants.EXECUTESUITE);

			if (executeTestBatch.equalsIgnoreCase("No") && !(testSetFound)) {
				continue;
			}
			// get the testSet
			testSetList = getTestSet(testSetFound, testBatch);
			if (testSetList != null && testSetList.size() > 0) {
				TestSet testSet = testSetList.get(0);
				// Assign the Test Set to a dynamic Test Set
				setDynamicTestSet(testSet);

				executionEngineName = testSet.getExecutionEngine();
				this.testSetName = testName.replace(" ", "_");
				// Check if this test suite should be executed
				if (!(executionEngineName.equalsIgnoreCase("Core") || (executionEngineName
						.equalsIgnoreCase("ETL")))
						&& testSet.getBrowser().length() <= 0
						&& (testSet.getApplicationUrl()).length() <= 0) {
					LOGGER.error("Browser type and Application url/path is not specified. This test set will not be executed");
					continue;
				} else if (testSet.getTestSuitePath().length() <= 0) {
					LOGGER.error("Test suite path is not specified. This test set will not be executed");
					continue;
				} else if (!(executionEngineName.equalsIgnoreCase("Core") || (executionEngineName
						.equalsIgnoreCase("ETL")))
						&& testSet.getObjectRepositoryPath().length() <= 0) {
					LOGGER.error("Object respository path is not specified. This test set will not be executed");
					continue;
				}
				for (TestSet set : testSetList) {
					// execute the test batch
					executeTestBatch(set);
				}
			}
		}
	}

	/**
	 * Method to get the test batch file path
	 * 
	 * @return testBatchfilePath
	 */
	private String getTestBatchFilePath() throws AFTException {
		String testBatchfilePath;
		if (commandArgs != null && commandArgs.size() > 0) {
			testBatchfilePath = commandArgs.get("testbatchfile");
		} else {
			testBatchfilePath = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.TESTBATCH_PATH);
		}
		return testBatchfilePath;
	}

	/**
	 * Method to check whether command line test set matches with test set in
	 * batch file
	 * 
	 * @param testName
	 *            testName
	 * @return testSetFound
	 */
	private boolean isTestSetMatchFound(String testName) {
		boolean testSetFound = false;
		String name = testName;
		if (commandArgs != null && commandArgs.size() > 1) {
			String testSet = commandArgs.get("testset");
			if (testSet != null) {
				if (name.indexOf('^') > 0) {
					String[] setNameArr = name.split("\\^");
					name = setNameArr[0];
					LOGGER.info("Test Set Name [" + name + "]");
				}
				if (name.equalsIgnoreCase(testSet)) {
					LOGGER.info("Command line Test Set Name [" + testSet + "]");
					testSetFound = true;
					LOGGER.info("Inside isTestSetMatchFound [" + testSetFound
							+ "]");
				}
			}
		}
		return testSetFound;
	}

	/**
	 * Method to get test set object
	 * 
	 * @param testSetFound
	 *            testSetFound
	 * @param testBatch
	 *            testBatch
	 * @return testSetlist
	 */
	private List<TestSet> getTestSet(boolean testSetFound,
			Map<String, String> testBatch) {
		List<TestSet> testSetlist = new ArrayList<TestSet>();
		TestSet testSet = null;
		if (testSetFound) {
			// create test set object with the data
			testSet = createTestSet(testBatch);
			if (commandArgs != null && commandArgs.size() > 2) {
				// update test set object with the data
				testSet = updateTestSet(testSet);
				String browserType = commandArgs.get(Constants.BROWSERTYPE);
				if (browserType != null && browserType.contains(",")) {
					String[] browsers = browserType.split(",");
					testSet.setBrowser(browsers[0]);
					testSetlist.add(testSet);
					TestSet set = createTestSet(testBatch);
					set = updateTestSet(set);
					set.setBrowser(browsers[1]);
					testSetlist.add(set);
				} else {
					if (browserType != null) {
						testSet.setBrowser(browserType);
					}
					testSetlist.add(testSet);
				}

			} else {
				testSetlist.add(testSet);
			}
		} else {
			// create test set object with the data
			testSet = createTestSet(testBatch);
			testSetlist.add(testSet);
		}
		return testSetlist;
	}

	/**
	 * Method to execute the DB test batch
	 * 
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	private void executeDBTestBatch() throws AFTException {
		try {

			List<TestSet> testSetList = DatabaseUtil.getInstance()
					.loadTestBatch();
			for (TestSet testSet : testSetList) {
				setDynamicTestSet(testSet);
				executionEngineName = testSet.getExecutionEngine();
				testSet.setCategory("");
				this.testSetName = testSet.getTestSetName();

				// execute the test batch
				executeTestBatch(testSet);
			}
		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Method to execute the test batch
	 * 
	 * @param testSet
	 *            testSet
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	private void executeTestBatch(TestSet testSet) throws AFTException {
		try {
			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();
			// log the test batch information
			logTestBatchInfo(testSet);

			// validate test batch information
			validateTestBatch(testSet);

			// Create Execution Engine
			EngineManager.getInstance().getExecutionEngine(
					testSet.getExecutionEngine(), testSet.getApplicationUrl(),
					testSet.getApplicationName(), testSet.getBrowser(),
					testSet.getObjectRepositoryPath(), isFileSystemRequest);

			// initialize the reporting object
			initializeReportingObject(testSet);

			// Create & Init Test Suite Object
			LOGGER.info("Create & Init Test Suite Object");
			TestSuiteRunner testSuiteRunner = new TestSuiteRunner(this, testSet);

			// execute the functional test scenarios
			testSuiteRunner.initSystemVariables();
			// initialize the test suite
			testSuiteRunner.initializeTestSuite();
			testSuiteRunner.execute();

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			// Catch all exception here for the test suite so that
			// we can start execution of next test suite
			//
			exitTestBatchExecution(testSet.getTestSuitePath());
			setExecutionCompleted(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
	}

	/**
	 * This method will create new test set object with the data given in test
	 * batch
	 * 
	 * @param testBatch
	 *            testBatch
	 * @return testSet
	 */
	private TestSet createTestSet(Map<String, String> testBatch) {
		TestSet testSet = new TestSet();
		testSet.setBrowser(testBatch.get(Constants.BROWSERTYPE));
		testSet.setApplicationUrl(testBatch.get(Constants.APPLICATIONURL));
		testSet.setApplicationName(testBatch.get(Constants.APPLICATIONNAME));
		testSet.setTestSuitePath(testBatch.get(Constants.TESTSUITEPATH));
		testSet.setScenariosSheetName(testBatch
				.get(Constants.SCENARIOSHEETNAME));
		testSet.setTestStepsSheetName(testBatch
				.get(Constants.TESTSTEPSSHEETNAME));
		testSet.setObjectRepositoryPath(testBatch
				.get(Constants.OBJECTREPOSITORYPATH));
		testSet.setTestDataTablePath(testBatch.get(Constants.TESTDATATABLEPATH));
		testSet.setReusableTestSuitePath(testBatch
				.get(Constants.REUSABLETESTSUITEPATH));
		testSet.setReusableScenariosSheetName(testBatch
				.get(Constants.REUSABLESCENARIOSSHEETNAME));
		testSet.setReusableTestStepsSheetName(testBatch
				.get(Constants.REUSABLETESTSTEPSSHEETNAME));
		testSet.setAppConfigFilePath(testBatch.get(Constants.APPCONFIGFILEPATH));
		testSet.setAppExecutionConfiguration(testBatch
				.get(Constants.APPEXECUTIONCONFIGURATION));
		testSet.setTcmIntegrationConfigFilePath(testBatch
				.get(Constants.TCMINTEGRATIONCONFIGFILEPATH));
		testSet.setExecutionEngine(testBatch.get(Constants.EXECUTIONENGINE));
		testSet.setCategory(testBatch.get(Constants.CATEGORY));
		testSet.setScenarioInitializationIDs(testBatch
				.get(Constants.SCENARIOINITIALIZATIONIDS));
		testSet.setScenarioCleanupIDs(testBatch
				.get(Constants.SCENARIOCLEANUPIDS));
		testSet.setTestSetInitializationIDs(testBatch
				.get(Constants.TESTSETINITIALIZATIONIDS));
		testSet.setTestSetCleanupIDs(testBatch.get(Constants.TESTSETCLEANUPIDS));
		testSet.setCustomDictionaryPath(testBatch
				.get(Constants.CUSTOMDICTIONARYPATH));
		testSet.setSpellCheckLanguage(testBatch
				.get(Constants.SPELLCHECKLANGUAGE));
		testSet.setSpellCheckSuggestion(testBatch
				.get(Constants.SPELLCHECKSUGGESTION));
		return testSet;
	}

	/**
	 * This method will update test set object with the data given through
	 * command line arguments
	 * 
	 * @param testSet
	 *            testSet
	 * @return testSet
	 */
	private TestSet updateTestSet(TestSet testSet) {
		LOGGER.info("Inside Update TestSet");
		if (commandArgs.get(Constants.APPLICATIONURL) != null) {
			testSet.setApplicationUrl(commandArgs.get(Constants.APPLICATIONURL));
		}
		if (commandArgs.get(Constants.APPEXECUTIONCONFIGURATION) != null) {
			testSet.setAppExecutionConfiguration(commandArgs
					.get(Constants.APPEXECUTIONCONFIGURATION));
		}
		if (commandArgs.get(Constants.EXECUTIONENGINE) != null) {
			testSet.setExecutionEngine(commandArgs
					.get(Constants.EXECUTIONENGINE));
		}
		if (commandArgs.get(Constants.CATEGORY) != null) {
			testSet.setCategory(commandArgs.get(Constants.CATEGORY));
		}
		return testSet;
	}

	/**
	 * check if multiple browsers are found in test batch list
	 * 
	 * @param testBatchList
	 *            testBatchList
	 * @return boolean
	 */
	private boolean checkIfMultipleBrowsersExists(
			List<Map<String, String>> testBatchList) {
		// flag for multiple browsers
		boolean isMultipleBrowsers = false;
		// finding multiple browsers
		if (testBatchList != null && testBatchList.size() > 0) {
			for (Map<String, String> testBatch : testBatchList) {
				if (testBatch != null) {
					String tmpBrowserType = testBatch
							.get(Constants.BROWSERTYPE);
					if (tmpBrowserType.contains(",")) {
						isMultipleBrowsers = true;
						break;
					}
				}
			}
		}
		return isMultipleBrowsers;
	}

	/**
	 * Method to check if user has specified duplicate test batch names
	 * 
	 * @param testSetNameList
	 *            testSetNameList
	 * @throws AFTException
	 */
	private void checkIfDuplicateTestBatchExists(List<String> testSetNameList)
			throws AFTException {
		// check if user has specified duplicate test batch names
		for (int i = 0; i < testSetNameList.size(); i++) {
			String testName = testSetNameList.get(i);
			for (int j = 0; j < testSetNameList.size(); j++) {
				if ((testSetNameList.get(j).compareToIgnoreCase(testName) == 0)
						&& (i != j)) {
					String errMsg = "["
							+ testName
							+ "] is a duplicate test set name in AFTBatch.xml."
							+ " Please ensure that the test set names are unique"
							+ " and rerun the test suite.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			}
		}
	}

	/**
	 * Method to log the test batch information
	 * 
	 * @param testSet
	 *            testSet
	 */
	private void logTestBatchInfo(TestSet testSet) {
		LOGGER.info("Read test batch set: test set name [" + testSetName
				+ "], " + Constants.BROWSERTYPE + "[" + testSet.getBrowser()
				+ "], " + Constants.APPLICATIONURL + "["
				+ testSet.getApplicationUrl() + "], "
				+ Constants.APPLICATIONNAME + "["
				+ testSet.getApplicationName() + "], "
				+ Constants.TESTSUITEPATH + "[" + testSet.getTestSuitePath()
				+ "], " + Constants.SCENARIOSHEETNAME + "["
				+ testSet.getScenariosSheetName() + "], "
				+ Constants.TESTSTEPSSHEETNAME + "["
				+ testSet.getTestStepsSheetName() + "], "
				+ Constants.OBJECTREPOSITORYPATH + "["
				+ testSet.getObjectRepositoryPath() + "], "
				+ Constants.TESTDATATABLEPATH + "["
				+ testSet.getTestDataTablePath() + "], "
				+ Constants.REUSABLETESTSUITEPATH + "["
				+ testSet.getReusableTestSuitePath() + "], "
				+ Constants.REUSABLESCENARIOSSHEETNAME + "["
				+ testSet.getReusableScenariosSheetName() + "], "
				+ Constants.REUSABLETESTSTEPSSHEETNAME + "["
				+ testSet.getReusableTestStepsSheetName() + "], "
				+ Constants.APPCONFIGFILEPATH + "["
				+ testSet.getAppConfigFilePath() + "], "
				+ Constants.APPEXECUTIONCONFIGURATION + "["
				+ testSet.getAppExecutionConfiguration() + "], "
				+ Constants.TCMINTEGRATIONCONFIGFILEPATH + "["
				+ testSet.getTcmIntegrationConfigFilePath() + "], "
				+ Constants.CATEGORY + "[" + testSet.getCategory() + "], "
				+ Constants.EXECUTIONENGINE + "["
				+ testSet.getExecutionEngine() + "], "
				+ Constants.SCENARIOINITIALIZATIONIDS + "["
				+ testSet.getScenarioInitializationIDs() + "], "
				+ Constants.SCENARIOCLEANUPIDS + "["
				+ testSet.getScenarioCleanupIDs() + "], "
				+ Constants.TESTSETINITIALIZATIONIDS + "["
				+ testSet.getTestSetInitializationIDs() + "], "
				+ Constants.TESTSETCLEANUPIDS + "["
				+ testSet.getTestSetCleanupIDs() + "], "
				+ Constants.CUSTOMDICTIONARYPATH + "["
				+ testSet.getCustomDictionaryPath() + "], "
				+ Constants.SPELLCHECKLANGUAGE + "["
				+ testSet.getSpellCheckLanguage() + "], "
				+ Constants.SPELLCHECKSUGGESTION + "["
				+ testSet.getSpellCheckSuggestion() + "]");
	}

	/**
	 * Method to log the test batch information
	 * 
	 * @param testSet
	 *            testSet
	 * @throws AFTException
	 */
	private void validateTestBatch(TestSet testSet) throws AFTException {
		try {

			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();
			// Init Application Url system variable
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_APPLICATIONURL), true,
					testSet.getApplicationUrl());

			LOGGER.trace("Value for system variable [AFT_APPLICATIONURL] set to ["
					+ Helper.getInstance().getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_APPLICATIONURL)) + "]");

			LOGGER.debug("Starting execution of AFT test suite ["
					+ testSet.getTestSuitePath() + "] for application ["
					+ testSet.getApplicationName() + "] on url ["
					+ testSet.getApplicationUrl());
			// Check if the Configuration type is specified in TestBatch.xml
			if (isFileSystemRequest) {
				if (testSet.getAppExecutionConfiguration() != null
						&& testSet.getAppExecutionConfiguration().length() > 0) {
					LOGGER.info("User has specified the Configuration ["
							+ testSet.getAppExecutionConfiguration() + "]");
					if (isFileSystemRequest) {
						LOGGER.info("Loading App Config file ["
								+ ConfigProperties
										.getInstance()
										.getConfigProperty(
												ConfigProperties.APP_CONFIG_FILE_PATH)
								+ "], execution configuration ["
								+ testSet.getAppExecutionConfiguration() + "]");
						AppConfigProperties
								.getInstance()
								.loadConfigProperties(
										ConfigProperties
												.getInstance()
												.getConfigProperty(
														ConfigProperties.APP_CONFIG_FILE_PATH),
										testSet.getAppExecutionConfiguration());
					}
				} else {
					LOGGER.warn("No Configuration Specified in TestBatch.xml");
					LOGGER.warn("User may not be able to use the Applevel"
							+ " Configurations except the Common Configuration");
				}
			} else {
				AppConfigProperties.getInstance()
						.loadAppConfigPropertiesFromDB(
								testSet.getAppExecutionConfiguration());
			}
			if (testSet.getCategory() != null
					&& testSet.getCategory().length() <= 0) {
				LOGGER.warn("User has not specified any value for "
						+ Constants.CATEGORY
						+ ". All business scenarios will be executed");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Method to initialize the engine.
	 * 
	 * @param testSet
	 *            testSet
	 * @throws AFTException
	 */
	private void initializeReportingObject(TestSet testSet) throws AFTException {
		try {
			// Construct TestSuite reporting object and initialize...
			initTestSuiteReportingObj(testSet.getApplicationUrl(),
					testSet.getCategory(), testSet.getExecutionEngine(),
					testSet.getApplicationName(), testSet.getIdTestSuite());

			// Initialize the TCM object for test suite reporting...
			initTestSuiteTCMReportingObj(testSet.getApplicationUrl(),
					testSet.getCategory(), testSet.getExecutionEngine(),
					testSet.getApplicationName(), testSet.getIdTestSuite());
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Initializes TestSuite reporting object
	 * 
	 * @param applicationURL
	 *            application under test url
	 * @param category
	 *            category
	 * @param executionEngine
	 *            executionEngine
	 * @param applicationName
	 *            applicationName
	 * @throws AFTException
	 */
	private void initTestSuiteReportingObj(String applicationURL,
			String category, String executionEngine, String applicationName,
			String idTestSuite) throws AFTException {

		TestSuite testSuite = null;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (isFileSystemRequest) {
			testSuite = new TestSuite();
		} else {
			testSuite = DatabaseUtil.getInstance().getTestSuite(idTestSuite);
		}
		testSuite.setTestSuiteName(this.testSetName);
		// for TWIN or Frankenstein
		if (executionEngine != null
				&& (executionEngine.equalsIgnoreCase("TWIN") || executionEngine
						.equalsIgnoreCase("Frankenstein"))) {
			testSuite.setBrowserName(applicationName);
		} else {
			testSuite.setBrowserName(Helper.getInstance().getActionValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_BROWSERVERSION)));
		}
		testSuite.seturl(applicationURL);
		testSuite.setCategory(category);
		testSuite.setExecutionEngine(executionEngine);
		if (!isFileSystemRequest) {
			java.util.Date startTime = new java.util.Date();
			int reportTestSuiteId = DatabaseUtil.getInstance()
					.insertReportTestSuiteData(testSuite, startTime);

			testSuite.setIdReportTestSuite(reportTestSuiteId);
		}
		ReportGenerator.getInstance().addTestSuite(testSuite);
	}

	/**
	 * Initializes TestSuite reporting object for reporting to TCM tool
	 * 
	 * @param applicationURL
	 *            application under test url
	 * @param category
	 *            category
	 * @param executionEngine
	 *            executionEngine
	 * @param applicationName
	 *            applicationName
	 * @throws AFTException
	 */
	private void initTestSuiteTCMReportingObj(String applicationURL,
			String category, String executionEngine, String applicationName,
			String idTestSuite) throws AFTException {
		// also initialize the TCM report generator with the new test suite for
		// logging results to test case management tool
		TestSuite ts = null;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (isFileSystemRequest) {
			ts = new TestSuite();
		} else {
			ts = DatabaseUtil.getInstance().getTestSuite(idTestSuite);
		}
		ts.setTestSuiteName(this.testSetName);
		// for TWIN or Frankenstein
		if (executionEngine != null
				&& (executionEngine.equalsIgnoreCase("TWIN") || executionEngine
						.equalsIgnoreCase("Frankenstein"))) {
			ts.setBrowserName(applicationName);
		} else {
			ts.setBrowserName(Helper.getInstance().getActionValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_BROWSERVERSION)));
		}
		ts.seturl(applicationURL);
		ts.setCategory(category);
		ts.setExecutionEngine(executionEngine);

		TCMReportGenerator.getInstance().setTestSuite(ts);
	}

	/**
	 * processes report results and generate result xml file
	 */
	private void processTestReport() {
		try {
			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();
			if (isFileSystemRequest) {
				DateFormat dateFormat = new SimpleDateFormat(
						Constants.DATEFORMAT);
				ReportGenerator.getInstance().setEndTime(
						dateFormat.format(new Date()));
				// also set end time for the test suite in the TCM report
				// generator for logging results to test case management tool
				TCMReportGenerator.getInstance().setEndTime(
						dateFormat.format(new Date()));
				ReportGenerator.getInstance().generateXmlReport();
			}
			ReportGenerator.getInstance().summarizeResults();
		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
		}
	}

	/**
	 * This method will set the system variables.
	 * 
	 * @throws AFTException
	 */
	public void initSystemVariables() throws AFTException {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

		// Init test start date (MM-dd-yyyy)
		String dateStr = dateFormat.format(testStartTime).substring(0, 10);
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTSTARTDATE), true, dateStr);

		LOGGER.trace("Value for system variable [AFT_TestStartDate] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TESTSTARTDATE)) + "]");

		// Init test start time (hh24:mm:ss)
		String timeStr = dateFormat.format(testStartTime).substring(12, 19);
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTSTARTTIME), true, timeStr);

		LOGGER.trace("Value for system variable [AFT_TestStartTime] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TESTSTARTTIME)) + "]");

		// Checks for Browser version whether it is an Android, if Not Init AFT
		// OS Version system variable
		if (!(Helper.getInstance().getActionValue(Variable.getInstance()
				.generateSysVarName(SystemVariables.AFT_BROWSERVERSION)))
				.matches("(?i)^android.*")) {
			String osVersion = System.getProperty("os.name");

			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_OSVERSION), true, osVersion);

			LOGGER.debug("Value for system variable [AFT_OSVersion] set to ["
					+ Helper.getInstance().getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_OSVERSION)) + "]");
		}

		// Init Test Suite path system variable
		String testBatchfilePath;
		if (commandArgs != null && commandArgs.size() > 0) {
			testBatchfilePath = commandArgs.get("testbatchfile");
		} else {
			testBatchfilePath = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.TESTBATCH_PATH);
		}

		// Retrieve the individual file path elements
		//
		String[] path = Util.getInstance().splitFilePath(testBatchfilePath);

		// replace file name in the file path with blank to retrieve just
		// the
		// path
		//
		String testBatchSetFilePath = testBatchfilePath.replace(
				path[path.length - 1], "");

		// Save AFT_TestBatchPath value to test batch file path
		//
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTBATCHPATH), true,
				testBatchSetFilePath);
		// Retrieve the value and print to validate that it is saved
		// correctly
		//
		LOGGER.trace("Value for system variable [AFT_TestBatchPath] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TESTBATCHPATH)) + "]");

		// Init Test Suite file name system variable

		// retrieve just the last elements which is the file name
		//
		String testBatchSetFileName = path[path.length - 1];

		// Save AFT_TestBatchFileName value to test batch file path
		//
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTBATCHFILENAME), true,
				testBatchSetFileName);
		// Retrieve the value and print to validate that it is saved
		// correctly
		//
		LOGGER.trace("Value for system variable [AFT_TestBatchFileName] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TESTBATCHFILENAME)) + "]");

		// Initialize AFT_TotalBusinessScenarios to 0
		//
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TOTALBUSINESSSCENARIOS), true, "0");

		// Initialize AFT_TotalTestCases to 0
		//
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TOTALTESTCASES), true, "0");
	}

	/**
	 * @return test start time
	 */
	public java.util.Date getTestStartTime() {
		return testStartTime;
	}

	/**
	 * @return the isExecutionSuccess
	 */
	public boolean isExecutionCompleted() {
		return isExecutionCompleted;
	}

	/**
	 * @param isExecutionCompleted
	 *            the isExecutionCompleted to set
	 */
	private void setExecutionCompleted(boolean isExecutionCompleted) {
		this.isExecutionCompleted = isExecutionCompleted;
	}

	/**
	 * Tears down Selenium client
	 * 
	 * @throws AFTException
	 */
	private void tearDown() throws AFTException {
		EngineManager.getInstance().tearDownAllExecutionEngines();
	}

	/**
	 * This method process the driver tear down and report generation
	 * 
	 * @param testSuitePath
	 *            testSuitePath
	 * @throws AFTException
	 */
	public void exitTestBatchExecution(String testSuitePath)
			throws AFTException {
		LOGGER.info("----------------------------------------------------------------------------------");
		LOGGER.info("Test Set Name=[" + testSetName + "]"
				+ ", Test Suite Path=[" + testSuitePath + "]");
		ReportGenerator.getInstance().summarizeResults();

		LOGGER.info("----------------------------"
				+ "------------------------------------------------------");

		LOGGER.info("calling teardown for execution engine");
		tearDown();
	}

	/**
	 * This method verify the multiple browsers in Browser Type attribute of
	 * TestSet and split into separate. Then create Test Set for each browser.
	 * THK2101
	 * @param list
	 *            list
	 * @param testSetNameList
	 *            testSetNameList
	 * @return testBatchList
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, String>> verifyAndCreateMultiBrowserTests(
			List<Map<String, String>> list, List<String> testSetNameList) {
		LOGGER.info("User had configured multiple browsers in Test Batch");
		LOGGER.debug("Creating cloned object of testBatchList List object to prevent"
				+ " ConcurrentModificationException while modifing actual object");
		ArrayList<Map<String, String>> testBatchList = null;
		if (list instanceof ArrayList) {
			testBatchList = (ArrayList<Map<String, String>>) list;
		}
		ArrayList<HashMap<String, String>> tmpTestBatchList = (ArrayList<HashMap<String, String>>) testBatchList
				.clone();
		testSetNameList.clear();
		testBatchList.clear();
		if (tmpTestBatchList != null && tmpTestBatchList.size() > 0) {
			for (HashMap<String, String> testBatch : tmpTestBatchList) {
				String browserType = null;
				String setName = null;
				if (testBatch != null) {
					browserType = testBatch.get(Constants.BROWSERTYPE);
					setName = testBatch.get(Constants.TESTNAME);
					// Split the browser type
					String browserTypes[] = browserType.split(",");
					if (browserTypes.length > 1) {
						for (String browser : browserTypes) {
							// Removing spaces from values
							browser = browser.trim();
							// Replacing '*' with space as file does not take
							// any asterisks.
							// System will create screen shots with test set
							// names
							browser = browser.replace("*", "");
							if (browser.length() > 0) {
								HashMap<String, String> innerTestBatch = (HashMap<String, String>) testBatch
										.clone();
								innerTestBatch.put(Constants.BROWSERTYPE,
										browser);
								innerTestBatch.put(Constants.TESTNAME, setName
										+ "^" + browser);
								testBatchList.add(innerTestBatch);
								testSetNameList.add(setName + "^" + browser);
							}
						}
					} else {
						testBatchList.add(testBatch);
						testSetNameList.add(setName);
					}
				}
			}
		}
		LOGGER.info("End of [verifyAndCreateMultiBrowserTests] method");
		return testBatchList;
	}

	/**
	 * @return test set name
	 */
	public String getTestSetName() {
		return testSetName;
	}

	public TestSet getDynamicTestSet() {
		return dynamicTestSet;
	}

	public void setDynamicTestSet(TestSet dynamicTestSet) {
		this.dynamicTestSet = dynamicTestSet;
	}

	/**
	 * @return the disableStandardReportingFlag
	 */
	public boolean isDisableStandardReportingFlag() {
		return disableStandardReportingFlag;
	}
}
