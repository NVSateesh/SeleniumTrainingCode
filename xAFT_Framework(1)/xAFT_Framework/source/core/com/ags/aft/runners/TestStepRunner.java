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
 * Class: TestStepRunner
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */

package com.ags.aft.runners;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.Reporting.TCMReportGenerator;
import com.ags.aft.common.ScreenshotCapture;
import com.ags.aft.common.Util;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Annotations;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.main.Parser;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * Test Step runner implementation. Implements ITestRunner to execute one test
 * step. Instantiated and called by TestSuiteRunner
 * 
 */
public class TestStepRunner implements ITestRunner {
	/**
	 * CONSTANT LOGGER Object Instance
	 */
	private static final Logger LOGGER = Logger.getLogger(TestStepRunner.class);

	// Test Step specific information for use within this runner
	private String bizScenarioID;
	private TestCase testCase;
	private int iTestStepMapId;

	/**
	 * Test Suite Runner object instance
	 */
	private TestSuiteRunner testSuiteRunner;

	private boolean terminateExecution;

	private String serviceRequestPath, screenShotPath;
	private String testDataSheetName = null;
	private String testStepID = null;
	private boolean isReusable = false;
	private TestStep reportTestStep;

	/**
	 * Custom TestStepRunner constructor. Initializes the runner, sets required
	 * parameters and initializes system variables
	 * 
	 * @param suiteRunner
	 *            test suite runner object
	 * 
	 * @param bizScenarioID
	 *            - Id of the Test scenario being executed by the method
	 * @param testCase
	 *            - testCase
	 * @param sortId
	 *            - sortId
	 * @param isReusable
	 *            - isReusable
	 * @throws ApplicationException
	 */
	public TestStepRunner(TestSuiteRunner suiteRunner, String bizScenarioID,
			TestCase testCase, int sortId, boolean isReusable)
			throws AFTException {

		this.testSuiteRunner = suiteRunner;
		this.bizScenarioID = bizScenarioID;
		this.iTestStepMapId = sortId;
		this.testCase = testCase;
		this.terminateExecution = false;
		this.isReusable = isReusable;
	}

	/**
	 * This method will execute test steps. Instantiated and called by
	 * TestSuiteRunner
	 * 
	 * @throws AFTException
	 */
	public void execute() throws AFTException {

		String testCaseId = testCase.getTestCaseId();
		String testCaseDescription = testCase.getTestCaseDesc();
		// The list objects to hold values of excel Pre-steps
		LOGGER.trace("Reading pre-step action...");

		if (!getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			// Adding test case details
			// Construct TestStep reporting object
			TestCase testCaseDetails = new TestCase();
			initTestCaseDetailsReportingObj(testCaseDetails, testCaseId,
					testCaseDescription);
		}

		// execute the all test steps
		executeAllSteps();
		// When isDataTraversed flag is set to true then increment the counter
		// of testData traversed.
		setTestDataSheetList();
	}

	/**
	 * This method will execute steps
	 * 
	 * @throws AFTException
	 * 
	 */
	private void executeAllSteps() throws AFTException {
		String errormessage = null;
		AFTException applException = null;
		Map<String, TestStep> preSteps = testCase.getPreSteps();
		Map<String, TestStep> postSteps = testCase.getPostSteps();
		String testCaseId = testCase.getTestCaseId();
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		String onErrorValue = null;
		if (!getTestSuiteRunner().isTestSetInitialization()) {
			onErrorValue = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.ONERROR);
			if (onErrorValue == null) {
				onErrorValue = ConfigProperties.DEFAULT_ONERROR;
			}
		} else {
			onErrorValue = ConfigProperties.ONERROR_RESUMENEXTTESTSUITE;
		}
		// Execute pre-step actions...
		if (isFileSystemRequest) {
			try {
				executeStep(testCaseId, Constants.PRESTEPPREFIX, preSteps);

			} catch (AFTException e) {
				errormessage = e.getErrorMessage();
				applException = e;
			}
		}

		// Execute step actions...
		applException = executeSteps(applException);

		// Check if JumpTo has been called by user or not
		if (!testSuiteRunner.isJumpTo() && !terminateExecution
				&& isFileSystemRequest) {
			try {

				// Execute post-step actions...
				executeStep(testCaseId, Constants.POSTSTEPPREFIX, postSteps);

			} catch (AFTException e) {
				errormessage = e.getErrorMessage();
				applException = e;
			}
		}

		if (errormessage != null || applException != null) {
			throw applException;
		}
	}

	/**
	 * This method will execute steps
	 * 
	 * @param appException
	 *            appException
	 * @return applException
	 * @throws AFTException
	 * 
	 */
	private AFTException executeSteps(AFTException appException)
			throws AFTException {
		Map<String, TestStep> steps = testCase.getSteps();
		String testCaseId = testCase.getTestCaseId();
		AFTException applException = appException;
		String errormessage = null;
		if (appException != null) {
			errormessage = appException.getErrorMessage();
		}
		String onErrorValue = null;
		if (!getTestSuiteRunner().isTestSetInitialization()) {
			onErrorValue = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.ONERROR);
			if (onErrorValue == null) {
				onErrorValue = ConfigProperties.DEFAULT_ONERROR;
			}
		} else {
			onErrorValue = ConfigProperties.ONERROR_RESUMENEXTTESTSUITE;
		}
		// Check if JumpTo has been called by user or not
		if (!testSuiteRunner.isJumpTo() && !terminateExecution) {
			// On Error Recovery handling...
			if ((errormessage == null)
					|| (onErrorValue
							.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) == 0)
					|| (getTestSuiteRunner().getOnDBErrorValue() != null && getTestSuiteRunner()
							.getOnDBErrorValue()
							.compareToIgnoreCase(
									ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) == 0)) {
				try {
					// Execute step actions...
					executeStep(testCaseId, "Steps", steps);

				} catch (AFTException e) {
					errormessage = e.getErrorMessage();
					applException = e;
				}
			} else {
				LOGGER.info("Skipping step actions for test case ["
						+ testCaseId + "] as '" + ConfigProperties.ONERROR
						+ "' is set to [" + onErrorValue + "]");
			}
		}
		return applException;
	}

	/**
	 * set test data sheet data to suite runner in order to have how many times
	 * test data traversed.
	 * 
	 */
	private void setTestDataSheetList() {
		if (testSuiteRunner.isDataTraversed()) {
			if (testSuiteRunner.getTestDataSheetList() != null
					&& testSuiteRunner.getTestDataSheetList().containsKey(
							testDataSheetName)) {
				int val = testSuiteRunner.getTestDataSheetList().get(
						testDataSheetName);

				if (testSuiteRunner.isMoveToDataRow() || val == 0) {
					testSuiteRunner.getTestDataSheetList().put(
							testDataSheetName, val + 1);
				}
			}

			// set the values to default.
			testSuiteRunner.setDataTraversed(false);
			testSuiteRunner.setMoveToDataRow(false);
			testDataSheetName = null;
		}

	}

	/**
	 * Constructs the screenshot file name
	 * 
	 * @param testCaseId
	 *            Test case for which screenshot is captured
	 * @param isError
	 *            is this being captured for error
	 * 
	 * @return screenshot file name
	 */
	public String getScreenShotFileName(String testCaseId, boolean isError) {
		String screenShotFileName;

		DateFormat formatter = new SimpleDateFormat(
				Constants.DATEFORMATSCREENSHOT);
		String stimestamp = formatter.format(new Date());

		if (isError) {
			screenShotFileName = testCaseId + "_Error" + " " + stimestamp
					+ ".png";
		} else {
			screenShotFileName = testCaseId + " " + stimestamp + ".png";
		}
		LOGGER.debug("Created screenshot file name [" + screenShotFileName
				+ "] for capturing screen shot");

		return screenShotFileName;
	}

	/**
	 * Constructs the service request/response file name
	 * 
	 * @param testCaseId
	 *            Test case for which screenshot is captured
	 * @param isError
	 *            is this being captured for error
	 * 
	 * @return service request/response file name
	 */
	private String getServiceRequestFileName(String testCaseId, boolean isError) {
		String serviceRequestFileName;

		DateFormat formatter = new SimpleDateFormat(
				Constants.DATEFORMATSCREENSHOT);
		String stimestamp = formatter.format(new Date());

		if (isError) {
			serviceRequestFileName = testCaseId + "_Error" + " " + stimestamp
					+ ".xml";
		} else {
			serviceRequestFileName = testCaseId + " " + stimestamp + ".xml";
		}
		LOGGER.debug("Created file name [" + serviceRequestFileName
				+ "] for saving XML request/response");

		return serviceRequestFileName;
	}

	/**
	 * captureServiceRequest for service request/response capturing and setting
	 * the image path in TestStep reporting object
	 * 
	 * @param ts
	 *            TestStep object
	 * @param iStepActionPosition
	 *            Step Position
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param isError
	 *            indicates if an error has occurred while executing this step
	 * @throws AFTException
	 */
	private void captureServiceRequest(TestStep ts, int iStepActionPosition,
			String stepTypePrefix, boolean isError) throws AFTException {

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		String stepPrefix = null;

		if (stepTypePrefix.contains("-")) {
			stepPrefix = stepTypePrefix.replace("-", "-Step");
		} else {
			stepPrefix = stepTypePrefix;
		}

		// JIRA Issue 669
		// check if we should create the place holder for captured screen shots
		if (ScreenshotCapture.getInstance()
				.isCaptureServiceRequest(ts, isError)) {

			// Create the service request/response file name
			String serviceRequestResponseXMLValue = Helper.getInstance()
					.getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_WSREQUEST_RESPONSE));
			String responseType = Variable.getInstance().generateSysVarName(
					SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE);
			// get the actual value
			responseType = Helper.getInstance().getActionValue(responseType);
			if (isFileSystemRequest) {
				if (serviceRequestPath == null) {
					try {
						serviceRequestPath = ScreenshotCapture.getInstance()
								.createServiceRequestPath(
										testSuiteRunner.getTestBatchRunner()
												.getTestStartTime(),
										testSuiteRunner.getTestBatchRunner()
												.getTestSetName());
					} catch (Exception e) {
						LOGGER.error("Exception::", e);
						throw new AFTException(e);
					}
				}
				// looks like we should capture service request/response
				ScreenshotCapture.getInstance().captureServiceRequest(
						ts,
						serviceRequestResponseXMLValue,
						serviceRequestPath,
						getServiceRequestFileName(
								bizScenarioID
										+ "_"
										+ testCase.getTestCaseId()
										+ "_"
										+ stepPrefix
										+ "_"
										+ String.format("%03d",
												iStepActionPosition), isError),
						responseType);
			} else {
				// looks like we should capture service request/response
				ScreenshotCapture.getInstance().captureServiceRequest(
						ts,
						serviceRequestResponseXMLValue,
						System.getenv("TMP"),
						getServiceRequestFileName(
								bizScenarioID
										+ "_"
										+ testCase.getTestCaseId()
										+ "_"
										+ stepPrefix
										+ "_"
										+ String.format("%03d",
												iStepActionPosition), isError),
						responseType);
			}

		}
	}

	/**
	 * captureScreenShot for screenshot capturing and setting the image path in
	 * TestStep reporting object
	 * 
	 * @param ts
	 *            TestStep object
	 * @param iStepActionPosition
	 *            Step Position
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param isError
	 *            indicates if an error has occurred while executing this step
	 * @param isCaptureScreenShotNow
	 *            isCaptureScreenShotNow
	 * @throws AFTException
	 */
	public void captureScreenShot(TestStep ts, int iStepActionPosition,
			String stepTypePrefix, boolean isError,
			boolean isCaptureScreenShotNow) throws AFTException {

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		String screenShotFile = null;
		String stepPrefix = null;
		if (stepTypePrefix.contains("-")) {
			stepPrefix = stepTypePrefix.replace("-", "-Step");
		} else {
			stepPrefix = stepTypePrefix;
		}

		if ((!ts.getAction().equalsIgnoreCase("verifyPDFText"))
				&& (isCaptureScreenShotNow || ScreenshotCapture.getInstance()
						.isCaptureScreenShot(ts, isError))) {
			// check if we should capture the screen shot or not
			//

			// JIRA Issue 669
			// check if we should create the place holder for captured
			// screen
			// shots

			if (isFileSystemRequest) {
				// get the screen shot path
				screenShotFile = getScreeenShotPath(isCaptureScreenShotNow)
						+ '/'
						+ getScreenShotFileName(
								bizScenarioID
										+ "_"
										+ testCase.getTestCaseId()
										+ "_"
										+ stepPrefix
										+ "_"
										+ String.format("%03d",
												iStepActionPosition), isError);
			} else {
				screenShotFile = System.getenv("TMP")
						+ '/'
						+ getScreenShotFileName(
								bizScenarioID
										+ "_"
										+ testCase.getTestCaseId()
										+ "_"
										+ stepPrefix
										+ "_"
										+ String.format("%03d",
												iStepActionPosition), isError);
			}

			// looks like we should capture screenshot, let us call the
			// method to capture screen shot
			//
			// Construct the screenshot file name
			// call engine specific method to capture screenshot
			if (EngineManager.getInstance().getCurrentExecutionEngine() != null) {
				if (!EngineManager.getInstance()
						.getCurrentExecutionEngineName()
						.equalsIgnoreCase("etl")) {
					EngineManager.getInstance().getCurrentExecutionEngine()
							.captureScreenshot(screenShotFile);
					// Create a new file object
					File file = new File(screenShotFile);
					ts.setImageName(file.getPath());
				}
			} else {
				ScreenshotCapture.getInstance().writeScreenShot(screenShotFile);
				File file = new File(screenShotFile);
				ts.setImageName(file.getPath());
			}
		}
	}

	/**
	 * Get the screenshot path
	 * 
	 * @param isCaptureScreenShotNow
	 *            isCaptureScreenShotNow
	 * 
	 * @return screenShotPath
	 */
	public String getScreeenShotPath(boolean isCaptureScreenShotNow)
			throws AFTException {
		if (screenShotPath == null) {
			try {
				screenShotPath = ScreenshotCapture.getInstance()
						.createScreenShotPath(
								testSuiteRunner.getTestBatchRunner()
										.getTestStartTime(),
								testSuiteRunner.getTestBatchRunner()
										.getTestSetName(),
								isCaptureScreenShotNow);
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return screenShotPath;
	}

	/**
	 * Executes pre/step/post test steps of a test case
	 * 
	 * @param testCaseId
	 *            testCaseId
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param steps
	 *            steps
	 * @throws AFTException
	 */
	private void executeStep(String testCaseId, String stepTypePrefix,
			Map<String, TestStep> steps) throws AFTException {
		LOGGER.trace("creating new object for Parser...");
		long testStepStartTime;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		/*
		 * Define a variable to initialize to teststep start time for capturing
		 * test step execution time
		 */

		if (steps != null) {
			LOGGER.info("Starting execution of "
					+ stepTypePrefix
					+ (stepTypePrefix.toLowerCase().startsWith("step") ? ""
							: "steps") + " for test case #" + testCaseId);

			LOGGER.trace("stepAction " + steps);
			testStepStartTime = System.currentTimeMillis();
			// execute test steps
			executeTestSteps(testCaseId, stepTypePrefix, steps,
					testStepStartTime, isFileSystemRequest);
		} else {
			LOGGER.warn("No "
					+ stepTypePrefix
					+ (stepTypePrefix.toLowerCase().startsWith("step") ? ""
							: "steps") + " specified for test step #"
					+ testCaseId);
		}

		while (!isFileSystemRequest && testSuiteRunner.isJumpTo()) {
			// execute test steps
			testStepStartTime = System.currentTimeMillis();
			executeTestSteps(testCaseId, stepTypePrefix, steps,
					testStepStartTime, isFileSystemRequest);
		}
		LOGGER.info("Completed execution of "
				+ stepTypePrefix
				+ (stepTypePrefix.toLowerCase().startsWith("step") ? ""
						: "steps") + " for test case #" + testCaseId);
	}

	/**
	 * Executes pre/step/post test steps of a test case
	 * 
	 * @param testCaseId
	 *            testCaseId
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param steps
	 *            steps
	 * @param testStepStartTime
	 *            testStepStartTime
	 * @param isFileSystemRequest
	 *            isFileSystemRequest
	 * @throws AFTException
	 */
	private void executeTestSteps(String testCaseId, String stepTypePrefix,
			Map<String, TestStep> steps, long testStepStartTime,
			boolean isFileSystemRequest) throws AFTException {
		AFTException curException = null;
		AFTException prevException = null;
		boolean flag = false;
		int tesetStepLength = steps.size();
		int counter = 0;
		for (Map.Entry<String, TestStep> step : steps.entrySet()) {

			// Construct TestStep reporting object
			TestStep reportTestStep = new TestStep();
			TestStep testStepData = step.getValue();
			String action = testStepData.getAction().trim();

			try {

				// check for UI Jump to test step
				if (!isFileSystemRequest && testSuiteRunner.isJumpToStep()) {
					testSuiteRunner.setJumpToStep(false);
					int rowId = testSuiteRunner.getJumpToTestCaseRowId();
					if (Integer.parseInt(step.getKey()) > rowId) {
						flag = true;
						break;
					}
				}
				counter++;
				// check for UI Jump to test step
				if (!isFileSystemRequest && testSuiteRunner.isJumpTo()) {
					String rowId = Integer.valueOf(
							testSuiteRunner.getJumpToTestCaseRowId())
							.toString();
					if (!step.getKey().equals(rowId)) {
						continue;
					} else {
						testSuiteRunner.setJumpTo(false);
					}
				}
				if (action.equalsIgnoreCase(Constants.EMPTYVALUE)) {
					continue;
				}
				// Check if current execution should be terminated as a
				// result of user calling a terminate specific annotation
				if (terminateCurrentExecution(testCaseId)) {
					break;
				}
				// Let us now increment the step count as we have completed
				// execution of one step
				testSuiteRunner
						.setCurrentBusinessScenarioTestStepCount(testSuiteRunner
								.getCurrentBusinessScenarioTestStepCount() + 1);
				// execte the test step action
				executeAction(testStepData, stepTypePrefix, testCaseId,
						testStepStartTime, reportTestStep);
				if ((!isFileSystemRequest && counter == tesetStepLength)
						&& action.equalsIgnoreCase("ifThenElse")
						&& testSuiteRunner.isJumpToStep()) {
					testSuiteRunner.setJumpToStep(false);
					int rowId = testSuiteRunner.getJumpToTestCaseRowId();
					if (Integer.parseInt(step.getKey()) > rowId) {
						flag = true;
						break;
					}
				}

			} catch (AFTException ae) {
				curException = ae;
				setReportTestStepFailResult(reportTestStep, stepTypePrefix,
						testStepStartTime, ae);
			} finally {
				// On Error Recovery handling...
				if (curException != null) {
					String[] onError = getTestSuiteRunner().getOnErrorValue();

					if (isOnErrorValueConfigured(onError, stepTypePrefix)) {
						break;
					}
					// save this exception before setting curException to
					// NULL for correct reporting
					prevException = curException;
					curException = null;
				}
			}
		}
		if (!isFileSystemRequest && flag) {
			executeTestSteps(testCaseId, stepTypePrefix, steps,
					testStepStartTime, isFileSystemRequest);
		}
		// If an exception has been raised during any step execution, let us
		// raise it up the chain for reporting...
		raiseException(curException, prevException);
	}

	/**
	 * This method will check if current exection to be terminated or not..
	 * 
	 * @param testCaseId
	 *            testCaseId
	 * @return boolean either true/false.
	 * @throws AFTException
	 */
	private boolean terminateCurrentExecution(String testCaseId)
			throws AFTException {
		boolean terminate = false;
		if (Helper.getInstance().terminateCurrentExecution(Constants.TESTCASE)) {
			terminateExecution = true;
			LOGGER.info("It seems user has called '"
					+ Annotations.TERMINATECURRENTTESTCASE
					+ "' annotation, quitting current test case [" + testCaseId
					+ "]");
			terminate = true;
		}
		if (testSuiteRunner.isJumpTo()) {
			LOGGER.info("Stopping execution of current test steps. User has called JumpTo");
			terminate = true;
		}
		return terminate;
	}

	/**
	 * This method will set test step result into report
	 * 
	 * @param testStep
	 *            testStep
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param testStepStartTime
	 *            testStepStartTime
	 * @param ae
	 *            aftExaception
	 * @throws AFTException
	 */
	private void setReportTestStepFailResult(TestStep testStep,
			String stepTypePrefix, long testStepStartTime, AFTException ae)
			throws AFTException {

		if (!getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			// Set reporting results...
			String errMsg = "";
			if (ae.getErrorMessage() != null
					&& ae.getErrorMessage().length() > 0) {
				errMsg = ae.getErrorMessage();
			} else if (ae.getMessage() != null && ae.getMessage().length() > 0) {
				errMsg = ae.getMessage();
			} else {
				errMsg = ae.getLocalizedMessage();
			}
			setTestStepExecutionResults(testStep, "FAIL", errMsg,
					testStepStartTime);
			captureScreenShot(testStep,
					testSuiteRunner.getCurrentBusinessScenarioTestStepCount(),
					stepTypePrefix, true, false);
			captureServiceRequest(testStep,
					testSuiteRunner.getCurrentBusinessScenarioTestStepCount(),
					stepTypePrefix, false);
		}
	}

	/**
	 * This method will check if on error value is configured or not.
	 * 
	 * @param onError
	 *            onError
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @return boolean either true/false.
	 */
	private boolean isOnErrorValueConfigured(String[] onError,
			String stepTypePrefix) {
		boolean isOnError = false;
		if ((onError[1]
				.compareToIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) != 0)
				&& (stepTypePrefix.compareToIgnoreCase("post-") != 0)) {
			LOGGER.info("User has defined '"
					+ onError[0]
					+ "' to ["
					+ onError[1]
					+ "]. This test case execution will be stopped after executing 'post-steps' and test execution will continue as per '"
					+ onError[0] + "' configuration.");
			isOnError = true;
		}
		if (stepTypePrefix.compareToIgnoreCase("post-") != 0) {
			LOGGER.info("User has defined '"
					+ onError[0]
					+ "' to ["
					+ onError[1]
					+ "]. The remaining test steps in the current test cases will continue for execution as  '"
					+ onError[0] + "' configuration.");
		}
		return isOnError;
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
	 * This method will execute the step action
	 * 
	 * @param testStepData
	 *            testStepData
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param testCaseId
	 *            testCaseId
	 * @param testStepStartTime
	 *            testStepStartTime
	 * @param reportTestStep
	 *            reportTestStep
	 * @throws AFTException
	 */
	private void executeAction(TestStep testStepData, String stepTypePrefix,
			String testCaseId, long testStepStartTime, TestStep reportTestStep)
			throws AFTException {

		String action = testStepData.getAction().trim();
		String stepType = testStepData.getStepType();
		String stepId = testStepData.getStepId();
		Parser parser = new Parser();
		LOGGER.trace("creating new object for Parser...");

		if (action.startsWith("@")) {
			String actionDesc = "Calling action [" + action + "]";
			LOGGER.debug(actionDesc);
			// execute annotation
			executeAnnotation(testStepData, stepTypePrefix, testCaseId,
					testStepStartTime, reportTestStep);
		} else {

			// user has specified an action (and not annotation).
			// Let us execute it...
			//
			String elementName = testStepData.getElementName().trim();
			elementName = elementName.replaceAll("\\n", "");
			String elementValue = testStepData.getElementValue().trim();
			String actionDesc = "Calling action [" + action
					+ "], Element Name [" + elementName + "], Element value ["
					+ elementValue + "]";
			LOGGER.debug(actionDesc);

			// set the flag isDataTraversed flag to true when action
			// value starts with '${'
			if (elementValue
					.startsWith(Constants.TESTDATASTARTVARIABLEIDENTIFIER)) {
				String value = elementValue
						.substring(Constants.TESTDATASTARTVARIABLEIDENTIFIER
								.length());
				value = value.replace(".", "~");
				String[] valueList = value.split("~");
				if ((valueList[0].charAt(0) == '\'')
						&& (valueList[0].charAt(valueList[0].length() - 1) == '\'')) {
					valueList[0] = valueList[0].substring(1,
							valueList[0].length() - 1);

				}
				testDataSheetName = valueList[0].toLowerCase();
				testSuiteRunner.setDataTraversed(true);
			}
			String parsedElementValue = null;
			try {
				// parse the value string to substitute values for
				// variables and test data
				parsedElementValue = Helper.getInstance().getActionValue(
						testSuiteRunner, elementValue);
				if ((action.equalsIgnoreCase("IfThenElse") && elementValue
						.toUpperCase().contains(
								SystemVariables.AFT_ISENDOFTESTDATA))
						&& (getTestSuiteRunner().isEndOfTestData())) {
					getTestSuiteRunner().setEndOfTestData(false);
					Variable.getInstance().setVariableValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_ISENDOFTESTDATA), true,
							"false");
				}
			} catch (AFTException ae) {
				LOGGER.error(ae);
			} finally {
				parsedElementValue = Util.getInstance().checkForEmptyValue(
						parsedElementValue);
				String objectId = "";
				if (EngineManager.getInstance().getCurrentExecutionEngine() != null
						&& ObjectRepositoryManager.getInstance()
								.isObjectRepositoryLoaded()) {
					objectId = EngineManager.getInstance()
							.getCurrentExecutionEngine()
							.getObjectId(elementName);
				} else if (ObjectRepositoryManager.getInstance()
						.isObjectRepositoryLoaded()) {
					objectId = ObjectRepositoryManager.getInstance()
							.getObjectID(elementName);
				}
				/*
				 * actionDesc = "Calling action [" + action +
				 * "], Element Name [" + elementName + "], objectID [" +
				 * objectId + "], original value [" + elementValue +
				 * "], parsed value [" + parsedElementValue + "]";
				 */

				actionDesc = getActionDescription(action, elementName,
						parsedElementValue, objectId, elementValue);

				// Init TestStep reporting object
				createTestStepReportingObj(reportTestStep, testCaseId,
						actionDesc, action, elementName, elementValue,
						stepType, stepId, parsedElementValue,
						testStepData.getEtlObjectType());
				setReportTestStep(reportTestStep);
			}

			try {
				// Process the action specified by user
				parser.callToAction(this, action, elementName, elementValue,
						parsedElementValue, reportTestStep, testStepData);
			} catch (AFTException e) {
				LOGGER.error(e);
				throw e;
			}

			// Get the value of last action response...
			String lastActionResponse = Helper.getInstance().getActionValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTACTIONRESPONSE));
			LOGGER.trace("lastActionResponse Value retrieved ["
					+ lastActionResponse + "]");

			// Calling to store value into user defined variable
			storeToUserDefinedVar(action, lastActionResponse, elementName,
					elementValue);

			// Set reporting results...
			setTestStepExecutionResults(reportTestStep, "PASS", "",
					testStepStartTime);
			// this one is added for standard reporting
			getTestSuiteRunner().setTotalTestStepExecTime(
					getTestSuiteRunner().getTotalTestStepExecTime()
							+ reportTestStep.getTestStepExecutionTime());
			if (!getTestSuiteRunner().getTestBatchRunner()
					.isDisableStandardReportingFlag()) {
				if (!EngineManager.getInstance()
						.getCurrentExecutionEngineName()
						.equalsIgnoreCase("etl")) {
					if (!action
							.equalsIgnoreCase(Constants.GETELEMENTSCREENSHOT)) {
						captureScreenShot(
								reportTestStep,
								testSuiteRunner
										.getCurrentBusinessScenarioTestStepCount(),
								stepTypePrefix, false, false);
					}
					captureServiceRequest(reportTestStep,
							testSuiteRunner
									.getCurrentBusinessScenarioTestStepCount(),
							stepTypePrefix, false);
				}

			}
		}
	}

	/**
	 * This method will get action description based on action
	 * 
	 * @param action
	 *            action
	 * @param elementName
	 *            elementName
	 * @param parsedElementValue
	 *            parsedElementValue
	 * @param objectId
	 *            objectId
	 * @param elementValue
	 *            elementValue
	 * @return actionDesc
	 */
	private String getActionDescription(String action, String elementName,
			String parsedElementValue, String objectId, String elementValue) {
		String actionDesc = null;
		// browser actions
		if (action.equalsIgnoreCase("open")) {
			actionDesc = "Launch the URL " + parsedElementValue + "";
		} else if (action.equalsIgnoreCase("openWindow")) {
			actionDesc = "Open new page in a new window with the given URL "
					+ elementName + "";
		} else if (action.equalsIgnoreCase("close")
				|| action.equalsIgnoreCase("CloseWindow")) {
			actionDesc = "Close the active window which is currently focused  "
					+ parsedElementValue;
		} else if (action.equalsIgnoreCase("windowMaximize")) {
			actionDesc = "Re-sizes currently selected window "
					+ parsedElementValue;
		} else if (action.equalsIgnoreCase("isFramePresent")) {
			actionDesc = "Checks whether frame " + parsedElementValue
					+ " is present or not and returns Boolean value";
		} else if (action.equalsIgnoreCase("back")
				|| action.equalsIgnoreCase("refresh")
				|| action.equalsIgnoreCase("switchToDefaultContent")) {
			actionDesc = "Executing the [" + action + "]";
		}
		// wait actions
		else if (action.equalsIgnoreCase("wait")) {
			actionDesc = "Waiting till " + parsedElementValue
					+ " milliseconds ";
		} else if (action.equalsIgnoreCase("waitForPagetoLoad")) {
			actionDesc = "Waiting for page to load";
		} else if (action.toLowerCase().startsWith("waitfor")) {
			actionDesc = "Waiting for <<" + elementName + ">>";
		}
		// command actions
		else if (action.toLowerCase().startsWith("click")) {
			String objType = "";
			if (elementName.startsWith("btn")) {
				objType = "button ";
			} else if (elementName.startsWith("lnk")) {
				objType = "link ";
			} else if (elementName.startsWith("spn")) {
				objType = "span ";
			}
			actionDesc = "Clicking on the " + objType + "<<" + elementName
					+ ">>";
		} else if (action.equalsIgnoreCase("doubleClick")) {
			actionDesc = "Double click the object <<" + elementName + ">>";
		} else if (action.equalsIgnoreCase("generateDynamicObjectId")) {
			actionDesc = "Dynamically assign a new value for the object <<"
					+ elementName + ">>";
		} else if (action.equalsIgnoreCase("loadObjectRepository")) {
			actionDesc = "Load the object repository [" + parsedElementValue
					+ "] during runtime";
		} else if (action.equalsIgnoreCase("clearText")) {
			actionDesc = "Clear the text present in object <<" + elementName
					+ ">>";
		} else if (action.equalsIgnoreCase("jsExecutor")) {
			actionDesc = "Execute custom java script [" + parsedElementValue
					+ "]";
		} else if (action.toLowerCase().startsWith("appendtext")) {
			actionDesc = "Appends value " + parsedElementValue
					+ " to an existing text in a textbox <<" + elementName
					+ ">>";
		} else if (action.toLowerCase().startsWith("check")) {
			actionDesc = "Check the object <<" + elementName + ">>";
		} else if (action.toLowerCase().startsWith("uncheck")) {
			actionDesc = "UnCheck the object <<" + elementName + ">>";
		} else if (action.toLowerCase().startsWith(
				"executeReusableTestScenario")) {
			actionDesc = "Execute reusable test scenario ["
					+ parsedElementValue + "]";
		} else if (action.toLowerCase().startsWith("get")) {
			String text = action.substring(3, action.length());
			actionDesc = "Return the " + text + " from the object <<"
					+ elementName + ">>";
		} else if (action.equalsIgnoreCase("isElementPresent")
				|| action.equalsIgnoreCase("isAlertPresent")
				|| action.equalsIgnoreCase("isTextPresent")) {
			String text = action.substring(2, action.length());
			actionDesc = "Check whether " + text + " on the page or not.";
		} else if (action.equalsIgnoreCase("type")) {
			actionDesc = "Typing the Text " + parsedElementValue + " in <<"
					+ elementName + ">>";
		} else if (action.equalsIgnoreCase("maximizewindow")) {
			actionDesc = "Maximize the active window <<" + elementName + ">>";
		} else if (action.equalsIgnoreCase("createpattern")) {
			actionDesc = "Create pattern to an image based on values ["
					+ parsedElementValue + "]";
		} else if (action.equalsIgnoreCase("copydata")) {
			actionDesc = "Copy the data from one variable to another using the values ["
					+ parsedElementValue + "]";
		} else if (action.equalsIgnoreCase("ifthenelse")) {
			actionDesc = "Executing the [" + action
					+ "] on the given expression [" + parsedElementValue + "]";
		} else if (action.toLowerCase().startsWith("select")) {
			String select = "select";
			String actionText = action.substring(select.length(),
					action.length());

			actionDesc = "Selects the " + actionText + " ["
					+ parsedElementValue + "]" + " from the object <<"
					+ elementName + ">>";
		} else if (action.toLowerCase().startsWith("unselect")) {
			String select = "unselect";
			String actionText = action.substring(select.length(),
					action.length());

			actionDesc = "UnSelects the " + actionText + " ["
					+ parsedElementValue + "]" + " from the object <<"
					+ elementName + ">>";
		} else if (action.toLowerCase().startsWith("win")) {
			actionDesc = "Executing the [" + action + "]";
		} else if (action.toLowerCase().startsWith("operator")) {
			actionDesc = "Executing the arithmetic action [" + action + "]";
		} else if (action.toLowerCase().startsWith("ws")) {
			actionDesc = "Executing the webservice action [" + action + "]";
		}
		// DB actions
		else if (action.equalsIgnoreCase("openDBConnection")) {
			actionDesc = "Open the DB connection with the given values ["
					+ parsedElementValue + "]";
		} else if (action.equalsIgnoreCase("closeDBConnection")) {
			actionDesc = "Closes the DB connection [" + parsedElementValue
					+ "]";
		} else if (action.equalsIgnoreCase("executeDBQuery")) {
			actionDesc = "Execute database query [" + parsedElementValue + "]";
		} else if (action.equalsIgnoreCase("executeDBQuery")) {
			actionDesc = "Execute stored proc [" + parsedElementValue + "]";
		}
		// execute script actions
		else if (action.equalsIgnoreCase("createScriptInstance")) {
			actionDesc = "Create the script instance using the file <<"
					+ elementName + ">>";
		} else if (action.equalsIgnoreCase("executeScript")) {
			actionDesc = "Execute the given script";
		} else if (action.equalsIgnoreCase("destroyScriptInstance")) {
			actionDesc = "Destroy the script instance [" + parsedElementValue
					+ "]";
		}
		// validation actions
		else if (action.equalsIgnoreCase("verifyState")) {
			actionDesc = "Verifying the State of the Object <<" + elementName
					+ ">> to be [" + parsedElementValue + "]";
		} else if (action.equalsIgnoreCase("verifyLinks")) {
			actionDesc = "Verifying Links in the URL [" + parsedElementValue
					+ "]";
		} else if (action.equalsIgnoreCase("verifySpelling")) {
			actionDesc = "Verifying Spelling  in the URL ["
					+ EngineManager.getInstance().getCurrentExecutionEngine()
							.getCurrentURL() + "]";
		} else if (action.toLowerCase().startsWith("verify")) {
			String verify = "verify";
			String actionText = action.substring(verify.length(),
					action.length());
			if (elementName == null || elementName.equalsIgnoreCase("novalue")) {
				actionDesc = "Verifying " + actionText + " to be ["
						+ parsedElementValue + "]";
			} else {
				actionDesc = "Verifying " + actionText + " for object <<"
						+ elementName + ">> to be [" + parsedElementValue + "]";
			}
		} else {
			actionDesc = "Calling action [" + action + "], Element Name <<"
					+ elementName + ">>, objectID [" + objectId
					+ "], original value [" + elementValue
					+ "], parsed value [" + parsedElementValue + "]";
		}
		return actionDesc;
	}

	/**
	 * This method will execute the annotation step action
	 * 
	 * @param testStepData
	 *            testStepData
	 * @param stepTypePrefix
	 *            stepTypePrefix
	 * @param testCaseId
	 *            testCaseId
	 * @param testStepStartTime
	 *            testStepStartTime
	 * @param reportTestStep
	 *            reportTestStep
	 * @throws AFTException
	 */
	private void executeAnnotation(TestStep testStepData,
			String stepTypePrefix, String testCaseId, long testStepStartTime,
			TestStep reportTestStep) throws AFTException {
		String[] parsedStr = null;
		String annotationName = null;
		String annotationOrgValue = null;
		String annotationParsedValue = null;

		String action = testStepData.getAction().trim();
		String stepType = testStepData.getStepType();
		String stepId = testStepData.getStepId();
		String annotationDesc = "";

		if (action.contains("=")) {
			parsedStr = action.split("=");
			if (parsedStr.length < 2) {
				String errMsg = "Invalid annotation call ["
						+ action
						+ "]. Please check documentation for more details on how to use annotations.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			annotationName = parsedStr[0].trim();
			annotationOrgValue = parsedStr[1].trim();
		} else {
			annotationName = action.trim();
			annotationOrgValue = null;
		}

		// parse the value string to substitute values for
		// variables and test data
		if (annotationOrgValue != null) {

			if (annotationName.equals(Annotations.LOGMESSAGE)
					|| annotationName.equals(Annotations.REPORTTESTSTEP)
					|| annotationName.startsWith(Annotations.SWITCHTOENGINE)) {
				annotationParsedValue = annotationOrgValue;
			} else {
				annotationParsedValue = Helper.getInstance().getActionValue(
						testSuiteRunner, annotationOrgValue);
			}
			annotationDesc = "Execute the Annotation [" + annotationName
					+ "], with the value [" + annotationParsedValue + "]";
			LOGGER.debug(annotationDesc);
		}
		if (!annotationName.equals(Annotations.REPORTTESTSTEP)) {
			createTestStepReportingObj(reportTestStep, testCaseId,
					annotationDesc, action, "", "", stepType, stepId, "",
					testStepData.getEtlObjectType());
		}

		if (annotationName.equalsIgnoreCase(Annotations.CAPTURESCREENSHOTNOW)) {
			if (!getTestSuiteRunner().getTestBatchRunner()
					.isDisableStandardReportingFlag()) {
				captureScreenShot(reportTestStep,
						testSuiteRunner
								.getCurrentBusinessScenarioTestStepCount(),
						stepTypePrefix, false, true);
			}
		} else {
			callAnnotation(testStepData, annotationName, annotationOrgValue,
					annotationParsedValue);
		}
		// Set reporting results...
		setTestStepExecutionResults(reportTestStep, "PASS", "",
				testStepStartTime);
	}

	/**
	 * This method will execute the annotation.
	 * 
	 * @param testStepData
	 *            testStepData
	 * @param annotationName
	 *            annotationName
	 * @param annotationOrgValue
	 *            annotationOrgValue
	 * @param annotationParsedValue
	 *            annotationParsedValue
	 * @throws AFTException
	 */
	private void callAnnotation(TestStep testStepData, String annotationName,
			String annotationOrgValue, String annotationParsedValue)
			throws AFTException {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		Parser parser = new Parser();
		if (isFileSystemRequest) {
			parser.callToAnnotation(this, annotationName, annotationOrgValue,
					annotationParsedValue, false, testStepData);
		} else {
			/*
			 * String elementValue = stepElementValue.get(iStepCnt) .trim();
			 */
			String elementValue = testStepData.getElementValue().trim();
			parser.callToAnnotation(this, annotationName, annotationOrgValue,
					elementValue, false, testStepData);
		}
		String isEndOfTestData = Helper.getInstance().getActionValue(
				testSuiteRunner, "#AFT_IsEndOfTestData#");
		// set the flag moveToDataRow to true when
		// moveToPrevTestDataRow/moveToNextTestDataRow/setTestDataRow
		// annotaion is parsed
		if ((annotationName.equalsIgnoreCase(Annotations.MOVETOPREVTESTDATAROW)
				|| annotationName
						.equalsIgnoreCase(Annotations.MOVETONEXTTESTDATAROW) || annotationName
					.equalsIgnoreCase(Annotations.SETTESTDATAROW))
				&& !(isEndOfTestData.equals("true"))) {
			// setting the flag moveToDataRow to true
			testSuiteRunner.setMoveToDataRow(true);

		}
	}

	/**
	 * Initializes TestStep reporting object
	 * 
	 * @param testStepReportingObj
	 *            TestStep object
	 * @param testCaseId
	 *            TestCase Id currently being executed
	 * @param actionDesc
	 *            Description string for action
	 * @param action
	 *            action name
	 * @param elementName
	 *            Element name on which action is being performed
	 * @param elementValue
	 *            elementValue
	 * @param stepType
	 *            stepType
	 * @param stepId
	 *            stepId
	 * @param parsedElementValue
	 *            parsedElementValue
	 * @throws AFTException
	 */
	private void createTestStepReportingObj(TestStep testStepReportingObj,
			String testCaseId, String actionDesc, String action,
			String elementName, String elementValue, String stepType,
			String stepId, String parsedElementValue, String etlObjectType)
			throws AFTException {
		String name = elementName;
		String parsedValue = parsedElementValue;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		boolean flag = true;
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
		if (!getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			testStepReportingObj.setStartTime(dateFormat.format(new Date()));
			// Init Test step reporting object
			testStepReportingObj.setActionDescription(actionDesc);
			testStepReportingObj.setAction(action);
			if (EngineManager.getInstance().getCurrentExecutionEngineName()
					.equalsIgnoreCase("ETL")) {
				testStepReportingObj.setEtlObjectType(etlObjectType);
			}
			/*
			 * if (isFileSystemRequest) {
			 * testStepReportingObj.setElementName(name);
			 * testStepReportingObj.setElementValue(elementValue); } else {
			 */
			if (name == null || (name != null && name.equals("novalue"))) {
				name = "";
			}
			testStepReportingObj.setElementName(name);
			if (parsedValue == null
					|| (parsedValue != null && parsedValue.equals("novalue"))) {
				parsedValue = "";
			}
			testStepReportingObj.setElementValue(parsedValue);
			if (action.toLowerCase().startsWith("ws_loadrequest")) {
				testStepReportingObj.setElementValue("");
			}
			if (action.toLowerCase().startsWith("ws_validatevalue")) {
				// testStepReportingObj.setElementValue(elementValue);
				String value = elementValue.replace("^^", ",");
				String[] values = value.split(",");
				if (values.length > 1) {
					String parsedVal = Helper.getInstance().getActionValue(
							testSuiteRunner, values[1]);
					testStepReportingObj.setElementValue(parsedVal);
				}
			}
			// }
			testStepReportingObj.setStepType(stepType);
			testStepReportingObj.setStepId(stepId);
			testStepReportingObj.setSortId(String.valueOf(iTestStepMapId));
			if (!isFileSystemRequest
					&& (getTestSuiteRunner().isScenarioInitialization() || getTestSuiteRunner()
							.isTestSetInitialization())) {
				flag = false;
			}

			if (flag) {
				// add to report generator instance
				ReportGenerator.getInstance().getLatestTestSuite()
						.getTestScenario().getTestCase()
						.addTestStep(testStepReportingObj);

				// also initialize the TCM report generator for logging results
				// to test
				// case management tool
				TCMReportGenerator.getInstance().getTestSuite()
						.getTestScenario().getTestCase()
						.addTestStep(testStepReportingObj);
			}
			// Added as part of AFT-1533
			if (isFileSystemRequest) {
				testStepReportingObj.setTestCase(testCase);
			}
			// To count number of test steps executed.
			// Check if test case was executed previously or not in order to
			// avoid
			// the count of test steps
			// inside loops if executed then step will not be added to test step
			// list.
			if (!(testSuiteRunner.getTestCaseExecutedList()
					.contains(testCaseId))) {
				if (testSuiteRunner.getReusableTestStepsExecutedList() != null
						&& testSuiteRunner.getReusableTestStepsExecutedList()
								.size() > 0) {
					// check if the
					if (!(testSuiteRunner.getReusableTestStepsExecutedList()
							.contains(bizScenarioID))) {
						// add the test step to testSteps executed list.
						testSuiteRunner.getTestStepsExecutedList().add(
								testStepReportingObj);
						// if action is verify then add the test step to
						// testSteps executed list.
						if (action.startsWith("verify")) {
							testSuiteRunner.getVerifyTestStepsExecutedList()
									.add(testStepReportingObj);
						}
					}

				} else {
					testSuiteRunner.getTestStepsExecutedList().add(
							testStepReportingObj);
					if (action.startsWith("verify")) {
						testSuiteRunner.getVerifyTestStepsExecutedList().add(
								testStepReportingObj);
					}
				}
			}
		}

	}

	/**
	 * Initializes TestCaseDetails reporting object
	 * 
	 * @param testCaseReportingObj
	 *            testCaseReportingObj
	 * @param testCaseId
	 *            testCaseId
	 * @param testCaseDesc
	 *            testCaseDesc
	 * @throws AFTException
	 */
	private void initTestCaseDetailsReportingObj(TestCase testCaseReportingObj,
			String testCaseId, String testCaseDesc) throws AFTException {

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		String description = testCaseDesc;
		// Init Test case details reporting object
		testCaseReportingObj.setTestCaseId(testCaseId);

		if (testCaseDesc.contains(Constants.TESTDATASTARTVARIABLEIDENTIFIER)
				|| testCaseDesc.contains(Constants.DYNAMICVARIABLEDELIMITER)) {
			description = Helper.getInstance().getActionValue(testSuiteRunner,
					testCaseDesc);
		}

		testCaseReportingObj.setTestCaseDesc(description);
		testCaseReportingObj.setSortId(String.valueOf(iTestStepMapId));
		testCaseReportingObj.setReusable(isReusable);

		// add to report generator instance
		if (isFileSystemRequest) {
			ReportGenerator.getInstance().getLatestTestSuite()
					.getTestScenario().addTestCase(testCaseReportingObj);
		} else {
			if (!isFileSystemRequest
					&& !isReusable
					&& ReportGenerator.getInstance().getLatestTestSuite()
							.getTestScenario().getBusinessScenarioId()
							.equals(bizScenarioID)) {
				ReportGenerator.getInstance().getLatestTestSuite()
						.getTestScenario().addTestCase(testCaseReportingObj);
			}
		}
		TestCase tcmTestCaseObj = new TestCase();
		tcmTestCaseObj.setTestCaseId(testCaseId);
		tcmTestCaseObj.setTestCaseDesc(testCaseDesc);
		tcmTestCaseObj.setSortId(String.valueOf(iTestStepMapId));
		tcmTestCaseObj.setReusable(isReusable);
		// also initialize the TCM report generator for logging results to test
		// case management tool
		TCMReportGenerator.getInstance().getTestSuite().getTestScenario()
				.addTestCase(tcmTestCaseObj);
	}

	/**
	 * @param action
	 *            action
	 * @param lastActionResponse
	 *            lastActionResponse
	 * @param elementName
	 *            elementName
	 * @param value
	 *            value
	 * @throws ApplicationException
	 */
	private void storeToUserDefinedVar(String action,
			String lastActionResponse, String elementName, String value)
			throws AFTException {
		// Store the value back into any variable specified by
		// user...
		String dynamicVariableDelimiter = Constants.DYNAMICVARIABLEDELIMITER;

		// Do not store value for sikuli actions
		if (Helper.getInstance().isSikuliCall()) {
			return;
		}

		setUserDefinedVar(action, value, lastActionResponse);

		if ((elementName.startsWith(dynamicVariableDelimiter) && elementName
				.endsWith(dynamicVariableDelimiter))
				&& ((!lastActionResponse.equalsIgnoreCase("(null)")) && (!action
						.trim().toLowerCase().startsWith("verify")
						&& !action.trim().equalsIgnoreCase("executeDBQuery") && !action
						.trim().equalsIgnoreCase("readTableDataToArray")))) {
			setVariableValue(action, lastActionResponse, elementName);
		}
	}

	/**
	 * This method will set variablle value.
	 * 
	 * @param action
	 *            action
	 * @param lastActionResponse
	 *            lastActionResponse
	 * @param elementName
	 *            elementName
	 * @throws ApplicationException
	 */
	private void setVariableValue(String action, String lastActionResponse,
			String elementName) throws AFTException {
		String dbFixtureParamDelimiter = Constants.DBFIXTUREPARAMDELIMITER;
		if (!elementName.contains(dbFixtureParamDelimiter)
				&& !elementName.contains(",")) {
			String varName = elementName.substring(1, elementName.length() - 1);
			if (!Variable.getInstance().isValidSystemVariable(varName)) {
				LOGGER.info("Assigning result [" + lastActionResponse
						+ "] to Variable [" + elementName + "]");
				Variable.getInstance().setVariableValue(testSuiteRunner,
						action, elementName, false, lastActionResponse);
			}
		}
	}

	/**
	 * This method will set user defined variablle
	 * 
	 * @param action
	 *            action
	 * @param value
	 *            value
	 * @param lastActionResponse
	 *            lastActionResponse
	 * @throws AFTException
	 */
	private void setUserDefinedVar(String action, String value,
			String lastActionResponse) throws AFTException {
		String dynamicVariableDelimiter = Constants.DYNAMICVARIABLEDELIMITER;
		if (((value.startsWith(dynamicVariableDelimiter)
				&& value.endsWith(dynamicVariableDelimiter)
				&& !lastActionResponse.equalsIgnoreCase("(null)") && lastActionResponse
				.length() > 0)
				&& (!action.trim().toLowerCase().startsWith("verify")) && (!action
				.trim().toLowerCase().startsWith("getrowid") && !action.trim()
				.toLowerCase().startsWith("getcolumnid")))) {
			setVariable(action, value, lastActionResponse);
		}
	}

	/**
	 * This method will set user defined variablle
	 * 
	 * @param action
	 *            action
	 * @param value
	 *            value
	 * @param lastActionResponse
	 *            lastActionResponse
	 * @throws AFTException
	 */
	private void setVariable(String action, String value,
			String lastActionResponse) throws AFTException {
		String dbFixtureParamDelimiter = Constants.DBFIXTUREPARAMDELIMITER;
		if ((!action.trim().toLowerCase().startsWith("wait")
				&& !action.trim().toLowerCase().startsWith("selectoption") && !action
				.trim().toLowerCase().startsWith("executescript"))
				&& (!value.contains(dbFixtureParamDelimiter) && !value
						.contains(","))) {
			String varName = value.substring(1, value.length() - 1);
			if (!Variable.getInstance().isValidSystemVariable(varName)) {
				LOGGER.info("Assigning result [" + lastActionResponse
						+ "] to Variable [" + value + "]");
				Variable.getInstance().setVariableValue(testSuiteRunner,
						action, value, false, lastActionResponse);
			}
		}
	}

	/**
	 * This method will set the system variables.
	 * 
	 * @throws AFTException
	 */
	public void initSystemVariables() throws AFTException {
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_CURTESTCASEID), true,
				testCase.getTestCaseId());

		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_CURTESTCASEDESCRIPTION), true,
				testCase.getTestCaseDesc());

		// Set the system variable AFT_TotalTestCases by incrementing previous
		// value...
		Variable.getInstance().incSysVarValue(
				SystemVariables.AFT_TOTALTESTCASES);

		/*
		 * Reset the system variable AFT_TerminateCurrentTest to default value
		 */
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTCASE), true,
				"false");

	}

	/**
	 * This method will set execution result for a test step.
	 * 
	 * @param testStep
	 *            testStep
	 * @param result
	 *            result
	 * @param errMsg
	 *            errMsg
	 * @param testStepStartTime
	 *            testStepStartTime
	 */
	private void setTestStepExecutionResults(TestStep testStep, String result,
			String errMsg, long testStepStartTime) {
		String testResult = result;
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
		if (!getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			if (testResult.equalsIgnoreCase("PASS")
					&& !(testStep.getAction() != null && testStep.getAction()
							.startsWith("verify"))) {
				testResult = "INFO";
			}
			testStep.setResult(testResult);
		}

		if (errMsg != null && errMsg.length() > 0) {
			testStep.setErrorMessage(errMsg);
		}
		// setting test step execution time
		long tsEndTime = System.currentTimeMillis();
		long execTime = tsEndTime - testStepStartTime;
		testStep.setTestStepExecutionTime(execTime);
		testStep.setEndTime(dateFormat.format(new Date()));
	}

	/**
	 * @return test suite object
	 */
	public TestSuiteRunner getTestSuiteRunner() {
		return testSuiteRunner;
	}

	/**
	 * @return the testStepID
	 */
	public String getTestStepID() {
		return testStepID;
	}

	/**
	 * @param testStepID
	 *            the testStepID to set
	 */
	public void setTestStepID(String testStepID) {
		this.testStepID = testStepID;
	}

	/**
	 * @return the bizScenarioID
	 */
	public String getBizScenarioID() {
		return bizScenarioID;
	}

	/**
	 * @return the TestCase
	 */
	public TestCase getTestCase() {
		return this.testCase;
	}

	/**
	 * @return the iTestStepMapId
	 */
	public int getiTestStepMapID() {
		return this.iTestStepMapId;
	}

	/**
	 * @return the isReusable
	 */
	public boolean isReusable() {
		return isReusable;
	}

	/**
	 * 
	 * @return the report step object
	 */
	public TestStep getReportTestStep() {
		return reportTestStep;
	}

	/**
	 * Sets the report step object
	 * 
	 * @param reportTestStep
	 */
	public void setReportTestStep(TestStep reportTestStep) {
		this.reportTestStep = reportTestStep;
	}
}
