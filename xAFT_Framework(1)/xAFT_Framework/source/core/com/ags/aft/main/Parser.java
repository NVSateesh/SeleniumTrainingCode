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
 * Class: Parser
 * 
 * Purpose: This class creates instances of other classes, parses the commands
 * and invokes the appropriate or relevant methods.
 */

package com.ags.aft.main;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.Reporting.TCMReportGenerator;
import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.RuntimeProperties;
import com.ags.aft.common.Util;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Annotations;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.fixtures.common.CommandFixtures;
import com.ags.aft.fixtures.common.VerifyFixtures;
import com.ags.aft.fixtures.ruby.EmbedRubyEngine;
import com.ags.aft.fixtures.database.DatabaseFixture;
import com.ags.aft.fixtures.externalScript.ScriptInstance;
import com.ags.aft.fixtures.externalScript.ScriptInstanceManager;
import com.ags.aft.fixtures.externalScript.ScriptRunner;
import com.ags.aft.fixtures.linkchecker.LinkChecker;
import com.ags.aft.fixtures.sikuli.SikuliFixture;
import com.ags.aft.fixtures.sikuli.SikuliInstance;
import com.ags.aft.fixtures.spellChecker.SpellChecker;
import com.ags.aft.fixtures.webservices.WebServicesFixture;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Notifications;
import com.ags.aft.util.ScrollableFrame;
import com.ags.aft.util.Variable;
import com.ags.aft.webdriver.fixtures.UICommandFixtures;

/**
 * The Class seleniumMethodSelector.
 */
public class Parser {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Parser.class);

	// fixture objects
	/** The Command fixture object. */
	private CommandFixtures objCommand = new CommandFixtures();

	/** The Command fixture object. */
	private VerifyFixtures objVerify = new VerifyFixtures();

	/** The external script object. */
	private ScriptRunner objExtScript = new ScriptRunner();

	private EmbedRubyEngine rubyEngine = EmbedRubyEngine.getInstance();

	/** The Database fixture object. */
	private DatabaseFixture objDatabase = new DatabaseFixture();

	/** The WebServices singleton fixture object. */
	private WebServicesFixture objWebServices = WebServicesFixture
			.getInstance();

	/** The SikuliFixture singleton object. */
	private SikuliFixture objSikuli = SikuliFixture.getInstance();

	/**
	 * Parses action, calls method on the corresponding fixture to execution
	 * action.
	 * 
	 * @param testStepRunner
	 *            Test Step Runner Object Instance
	 * @param action
	 *            action to perform
	 * @param elementName
	 *            User defined elementName for the object
	 * @param elementValue
	 *            the original action value
	 * @param parsedElementValue
	 *            parsed action value
	 * @param testStepData
	 *            testStepData
	 * @throws AFTException
	 *             the application exception
	 */
	public void callToAction(TestStepRunner testStepRunner, String action,
			String elementName, String elementValue, String parsedElementValue,
			TestStep reportTestStep, TestStep testStepData) throws AFTException {
		String actionValue = action;
		String result = "";
		String actualElementName = elementName;
		String externalScriptMethodPrefix = ConfigProperties.getInstance()
				.getConfigProperty(
						ConfigProperties.EXTERNAL_SCRIPT_METHOD_PREFIX);

		// Check if the action is Sikuli type
		String value = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), elementName);
		String[] values = value.split("\\^");
		boolean callSikuli = false;
		if (values.length >= 1) {
			// Get the elementName
			elementName = values[0];
			if (elementName.contains(".png")) {
				callSikuli = true;
			} else if (actionValue.equalsIgnoreCase("openApplication")
					|| actionValue.equalsIgnoreCase("selectApplication")
					|| actionValue.equalsIgnoreCase("closeApplication")) {
				callSikuli = true;
			} else {
				// Check the ObjectRepository if the element if of type image
				RepositoryObject objectId = ObjectRepositoryManager
						.getInstance().getObject(elementName);
				if (objectId != null
						&& ObjectRepositoryManager.getInstance()
								.getObject(elementName).getType()
								.equalsIgnoreCase("screenshot")) {
					callSikuli = true;
				}
			}

		}
		Helper.getInstance().setSikuliCalled(callSikuli);
		elementName = actualElementName;
		try {

			// Checking for EMPTY_VALUE
			actionValue = Util.getInstance().checkForEmptyValue(actionValue)
					.trim();

			LOGGER.debug("Calling actionValue [" + actionValue
					+ "], element Name [" + elementName + "], original value ["
					+ actualElementName + "], parsed value ["
					+ parsedElementValue + "]");

			// If Object type is "imageName" or User passed variable is listed
			// in
			// Sikuli Object map or User passed an image filename
			// let us call sikuli fixtures instead
			// if (ObjectRepositoryManager.getInstance()
			// .isObjectRepositoryLoaded()) {
			// callSikuli = Helper.getInstance().checkSikuliCall(
			// testStepRunner, actionValue, elementName,
			// parsedElementValue);
			// }

			if (callSikuli) {
				if (actionValue.equalsIgnoreCase("click")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + parsedElementValue + "]");
					objSikuli.sikuliClick(testStepRunner, elementName);
				} else if (actionValue.equalsIgnoreCase("type")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + parsedElementValue + "]");
					objSikuli.sikuliType(testStepRunner, elementName,
							parsedElementValue);
				} else if (actionValue.equalsIgnoreCase("doubleClick")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + parsedElementValue + "]");
					objSikuli.sikuliDoubleClick(testStepRunner, elementName);
				} else if (elementName.equalsIgnoreCase("mousemove")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + parsedElementValue + "]");
					objSikuli.mouseHover(testStepRunner, elementValue);
				} else if (actionValue
						.equalsIgnoreCase("waitForScreenshotObjectToVanish")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + elementValue + "]");
					result = String.valueOf(objSikuli.waitForObjectToVanish(
							testStepRunner, elementName, elementValue));
				} else if (actionValue.equalsIgnoreCase("verifyImage")) {
					LOGGER.trace("Command [" + actionValue + "], object ["
							+ elementName + "], value [" + elementValue + "]");
					boolean stateValue = objSikuli.verifyImage(testStepRunner,
							elementName, parsedElementValue);
					result = Boolean.valueOf(stateValue).toString();
				} else if (actionValue
						.equalsIgnoreCase("waitForElementPresent")) {
					LOGGER.trace("Command [" + actionValue + "], object ["
							+ elementName + "], value [" + elementValue + "]");
					boolean stateValue = objSikuli.waitForImage(testStepRunner,
							elementName, parsedElementValue);
					result = Boolean.valueOf(stateValue).toString();
				} else if (actionValue.equalsIgnoreCase("isElementPresent")) {
					LOGGER.trace("Command [" + actionValue + "], object ["
							+ elementName + "], value [" + elementValue + "]");
					boolean stateValue = objSikuli.isImagePresent(
							testStepRunner, elementName, elementValue);
					result = Boolean.valueOf(stateValue).toString();
				} else if (actionValue.equalsIgnoreCase("openApplication")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + elementValue + "]");
					objSikuli.openApplication(testStepRunner, elementName,
							elementValue);
				} else if (actionValue.equalsIgnoreCase("selectApplication")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + elementValue + "]");
					objSikuli.selectApplication(testStepRunner, elementName);
				} else if (actionValue.equalsIgnoreCase("closeApplication")) {
					LOGGER.trace("Command [" + actionValue
							+ "], element name [" + elementName + "]"
							+ "value [" + elementValue + "]");
					objSikuli.closeApplication(testStepRunner, elementName);
				}
				return;
			}

			// other sikuli fixtures
			if (actionValue.equalsIgnoreCase("createPattern")) {
				LOGGER.trace("Command [" + actionValue + "], element name ["
						+ elementName + "]" + "value [" + parsedElementValue
						+ "]");
				result = objSikuli.createPattern(testStepRunner, elementName,
						parsedElementValue);
			}

			// Common Command fixtures
			//
			else if (actionValue.equalsIgnoreCase("setRubyEngine")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				
				rubyEngine.setRubyEngineEnv(parsedElementValue);
			}
			else if (actionValue.equalsIgnoreCase("startRubyEngine")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				
				rubyEngine.startEngine();
			//objCommand.ifThenElse(parsedElementValue, testStepRunner, this);
			}
			else if (actionValue.equalsIgnoreCase("rubyCmd")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				result = rubyEngine.runCommand(parsedElementValue) ;
			}
			else if (actionValue.equalsIgnoreCase("IfThenElse")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				objCommand.ifThenElse(parsedElementValue, testStepRunner, this);
			} else if (actionValue.equalsIgnoreCase("copyData")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ elementValue + "]");

				objCommand.copyData(testStepRunner, elementValue);
			} else if (actionValue.equalsIgnoreCase("sendEmail")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				Notifications.getInstance().sendEmail(parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("loadObjectRepository")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				EngineManager.getInstance().getCurrentExecutionEngine()
						.loadObjectRepository(parsedElementValue);
			} else if (actionValue
					.equalsIgnoreCase("executeReusableTestScenario")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				objCommand.executeReusableTestScenario(testStepRunner,
						parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("getTestDataRowCount")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				objCommand.getTestDataRowCount(testStepRunner,
						parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("getTestDataRowNumber")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ elementValue + "]");

				int iResult = objCommand.getTestDataRowNumber(testStepRunner,
						elementValue);

				result = Integer.valueOf(iResult).toString();
			} else if (actionValue.equalsIgnoreCase("getCurrentTestDataRow")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				result = objCommand.getCurrentTestDataRow(testStepRunner,
						parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("readDataToArray")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");

				objCommand.readDataToArray(testStepRunner, elementName,
						elementValue, parsedElementValue);
			}

			// external script
			//
			else if (actionValue.equalsIgnoreCase("createScriptInstance")) {
				LOGGER.trace("Command [" + actionValue + "], elements ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objExtScript.createScriptInstance(testStepRunner,
						elementName, elementValue);

			} else if (actionValue.equalsIgnoreCase("executeScript")) {
				LOGGER.trace("Command [" + actionValue + "], elements ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objExtScript.executeScript(testStepRunner,
						elementName, elementValue);

			} else if (actionValue.startsWith(externalScriptMethodPrefix)) {
				LOGGER.trace("Command [" + actionValue + "], elements ["
						+ elementName + "], value [" + parsedElementValue + "]");
				StringBuffer parsedName = new StringBuffer();
				String methodName = actionValue
						.substring(externalScriptMethodPrefix.length());
				parsedName.append(methodName);
				parsedName.append(",");
				parsedName.append(elementName);
				result = objExtScript.executeScript(testStepRunner,
						parsedName.toString(), elementValue);
			} else if (actionValue.equalsIgnoreCase("destroyScriptInstance")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				objExtScript.destroyScriptInstance(parsedElementValue);
			}
			// Database Controls fixture
			//
			else if (actionValue.equalsIgnoreCase("openDBConnection")) {
				LOGGER.trace("Command [" + actionValue + "], element name ["
						+ elementName + "]" + "value [" + parsedElementValue
						+ "]");
				result = objDatabase.openDBConnection(testStepRunner,
						elementName, parsedElementValue);
			}

			else if (actionValue.equalsIgnoreCase("executeDBQuery")) {
				LOGGER.trace("Command [" + actionValue + "], element name ["
						+ elementName + "]" + "value [" + parsedElementValue
						+ "]");
				result = objDatabase.executeDBQuery(testStepRunner,
						elementName, parsedElementValue);
			}

			else if (actionValue.equalsIgnoreCase("executeStoredProc")) {
				LOGGER.trace("Command [" + actionValue + "], element name ["
						+ elementName + "]" + "value [" + parsedElementValue
						+ "]");
				result = objDatabase.executeStoredProc(testStepRunner,
						elementName, parsedElementValue);
			}

			else if (actionValue.equalsIgnoreCase("executeDBCommand")) {
				LOGGER.trace("Command [" + actionValue + "], element name ["
						+ elementName + "]" + "value [" + parsedElementValue
						+ "]");
				result = objDatabase.executeDBCommand(testStepRunner,
						elementName, parsedElementValue);
			}

			else if (actionValue.equalsIgnoreCase("closeDBConnection")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				objDatabase.closeDBConnection(testStepRunner,
						parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("verifySpelling")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "]");
				boolean bResult = objVerify.verifySpelling(actionValue,
						testStepRunner, elementName, elementValue,
						parsedElementValue);
				result = Boolean.valueOf(bResult).toString();

			} else if (actionValue.equalsIgnoreCase("verifyLinks")) {
				LOGGER.trace("Command [" + actionValue + "], elementValue ["
						+ parsedElementValue + "]");
				boolean bResult = objVerify.verifyLinks(testStepRunner,
						parsedElementValue);
				result = Boolean.valueOf(bResult).toString();
			} else if (action.equalsIgnoreCase("verifyData")) {
				LOGGER.trace("Command [" + actionValue + "], elementValue ["
						+ parsedElementValue + "]");
				boolean bResult = objVerify.verifyDataValues(
						parsedElementValue, testStepRunner);

				result = Boolean.valueOf(bResult).toString();
			}

			// Arithmetic Controls fixture
			//
			else if (actionValue.equalsIgnoreCase("operator+")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objCommand.operatorAdd(testStepRunner, elementName,
						parsedElementValue);
				LOGGER.trace("Result received: " + result);
			} else if (actionValue.equalsIgnoreCase("operator-")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objCommand.operatorSub(testStepRunner, elementName,
						parsedElementValue);
				LOGGER.trace("Result received: " + result);
			} else if (actionValue.equalsIgnoreCase("operator*")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objCommand.operatorMul(testStepRunner, elementName,
						parsedElementValue);
				LOGGER.trace("Result received: " + result);
			} else if (actionValue.equalsIgnoreCase("operator/")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objCommand.operatorDiv(testStepRunner, elementName,
						parsedElementValue);
				LOGGER.trace("Result received: " + result);
			}

			// Web services Control Fixtures
			//
			else if (actionValue.equalsIgnoreCase("ws_SubstituteXMLValue")
					|| actionValue.equalsIgnoreCase("ws_SubstituteValue")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				result = objWebServices.wsSubstituteValue(elementName,
						parsedElementValue, testStepRunner);
			} else if (actionValue.equalsIgnoreCase("ws_LoadXMLRequest")
					|| actionValue.equalsIgnoreCase("ws_LoadRequest")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				result = objWebServices.wsLoadRequest(parsedElementValue);
			} else if (actionValue.equalsIgnoreCase("ws_PostRequest")
					|| actionValue.equalsIgnoreCase("ws_SendRequest")) {
				LOGGER.trace("Command [" + actionValue + "], value ["
						+ parsedElementValue + "]");
				result = objWebServices.wsSendRequest(parsedElementValue,
						testStepRunner);
			} else if (actionValue.equalsIgnoreCase("ws_ValidateXMLValue")
					|| actionValue.equalsIgnoreCase("ws_ValidateValue")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + parsedElementValue + "]");
				boolean bResult = objWebServices.wsValidateValue(elementName,
						parsedElementValue, testStepRunner);
				result = Boolean.valueOf(bResult).toString();

			} else if (actionValue.equalsIgnoreCase("ws_GetXMLValue")
					|| actionValue.equalsIgnoreCase("ws_GetValue")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + elementValue + "]");
				result = objWebServices.wsGetValue(elementName, elementValue,
						testStepRunner, objCommand);
			} else if (actionValue.equalsIgnoreCase("ws_Authenticate")) {
				LOGGER.trace("Command [" + actionValue + "], object ["
						+ elementName + "], value [" + elementValue + "]");
				objWebServices.wsAuthenticate(elementName, elementValue);
			}

			// log results to Test Case Management system...
			else if (actionValue.equalsIgnoreCase("logTestResultInTCM")) {
				LOGGER.trace("Command [" + actionValue + "]");
				objCommand.logTestExecutionResults();
			}

			// Looks like it is not a common command fixture...
			// Let us check if the execution engine is WebDriver and execute
			// actionValue...
			//
			else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName()
					.compareToIgnoreCase("webdriver") == 0) {
				LOGGER.trace(elementValue);
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);
			}
			// TWIN execution Engine
			else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName()
					.compareToIgnoreCase("TWIN") == 0) {
				/** TWINEngine instance */
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);
			}
			// Is Execution Engine QTP?...
			//
			else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName().compareToIgnoreCase("qtp") == 0) {

				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);
			} else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName()
					.compareToIgnoreCase("Robotium") == 0) {
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);

			} else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName()
					.compareToIgnoreCase("Frankenstein") == 0) {
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);

			} else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName()
					.compareToIgnoreCase("appium") == 0) {
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);

			} else if (EngineManager.getInstance()
					.getCurrentExecutionEngineName().compareToIgnoreCase("etl") == 0) {
				result = EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAction(testStepRunner, actionValue,
								elementName, parsedElementValue, elementValue);

			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			// Set the system variable AFT_LastOperatedControl
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTOPERATEDCONTROL), true,
					elementName);

			// for verifyLinks and verifySpelling get the error count
			if (actionValue.equalsIgnoreCase("verifyLinks")) {
				result = String.valueOf(LinkChecker.getInstance()
						.getBrokenLinkCount());
			}

			if (actionValue.equalsIgnoreCase("verifySpelling")) {
				result = String.valueOf(SpellChecker.getInstatnce()
						.getSpellErrorCount());
			}

			// Set the system variable AFT_LastActionResponse
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTACTIONRESPONSE), true,
					result);

			// Set the system variable AFT_LastValidationResult if a
			// verification is performed
			if (actionValue.length() > 6
					&& actionValue.substring(0, 6)
							.compareToIgnoreCase("verify") == 0) {
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_LASTVALIDATIONRESULT),
						true, result);
			}
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_LASTACTION), true, actionValue);

			// added explicit wait as implemented in AFT
			try {
				if (testStepRunner.getTestSuiteRunner().getExecutionSpeed() > 0) {
					Thread.sleep(testStepRunner.getTestSuiteRunner()
							.getExecutionSpeed());
				}
			} catch (InterruptedException ie) {
				LOGGER.error("Exception::", ie);
				throw new AFTException(ie);
			}
		}
	}

	/**
	 * This method executes annotations supported by AFT.
	 * 
	 * @param testStepRunner
	 *            Test Step Runner Object Instance
	 * @param annotationName
	 *            annotation to be executed
	 * @param annotationOrgValue
	 *            annotation parameter passed by user
	 * @param annotationParsedValue
	 *            annotation parameter value after parsing
	 * @param isCommand
	 *            isCommand
	 * @param testStepData
	 *            testStepData
	 * @throws AFTException
	 *             the application exception
	 */
	public void callToAnnotation(TestStepRunner testStepRunner,
			String annotationName, String annotationOrgValue,
			String annotationParsedValue, boolean isCommand,
			TestStep testStepData) throws AFTException {

		LOGGER.trace("Execute [callToAnnotation] with action ["
				+ annotationName + "], parameters [" + annotationParsedValue
				+ "]");

		try {
			if (annotationName.equalsIgnoreCase(Annotations.JUMPTOTESTCASE)) {

				callToAnnotationJumpToTestCase(testStepRunner, annotationName,
						annotationParsedValue, isCommand);

			} else if (annotationName.equalsIgnoreCase(Annotations.LOGLEVEL)) {

				callToAnnotationLoglevel(testStepRunner, annotationName,
						annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.SETEXECUTIONSPEED)) {

				callToAnnotationsetExecutionSpeed(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.CAPTURESCREENSHOT)) {

				callToAnnotationCaptureScreenShot(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.CUSTOMDICTIONARYPATH)) {

				callToAnnotationCustomDictionaryPath(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName.equalsIgnoreCase(Annotations.ONERROR)) {

				callToAnnotationOnError(testStepRunner, annotationName,
						annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.MOVETONEXTTESTDATAROW)) {

				callToAnnotationMoveToNextTestDataRow(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.MOVETOPREVTESTDATAROW)) {

				callToAnnotationMoveToPrevTestDataRow(testStepRunner,
						annotationName, annotationParsedValue);
			} else if (annotationName
					.equalsIgnoreCase(Annotations.SETTESTDATAROW)) {

				callToAnnotationsetTestDataRow(testStepRunner, annotationName,
						annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.TERMINATECURRENTTESTCASE)) {

				callToAnnotationTerminateCurrentTestCase(annotationName);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.TERMINATECURRENTTESTSUITE)) {

				callToAnnotationTerminateCurrentTestSuite(testStepRunner,
						annotationName);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.TERMINATECURRENTTESTSCENARIO)) {

				callToAnnotationTerminateCurrentTestScenario(testStepRunner,
						annotationName);

			} else if (annotationName.equalsIgnoreCase(Annotations.CONTINUE)) {

				callToAnnotationContinue(testStepRunner, annotationName,
						isCommand);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.RAISEEXCEPTION)) {

				callToAnnotationRaiseException(testStepRunner, annotationName,
						annotationParsedValue);

			} else if (annotationName.equalsIgnoreCase(Annotations.LOGMESSAGE)) {
				callToAnnotationlogMessage(testStepRunner,
						annotationParsedValue);
			} else if (annotationName
					.equalsIgnoreCase(Annotations.SETTCMINTEGRATIONTESTCASEID)) {

				callToAnnotationSetTCMIntegrationTestCaseID(testStepRunner,
						annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.USESCRIPTINSTANCE)) {

				callToAnnotationUseScriptInstance(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.DISPLAYSCROLLABLEFRAME)) {

				callToAnnotationDisplayScrollableFrame(testStepRunner,
						annotationName, annotationParsedValue);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.SETSIKULIIMAGESPATH)) {

				callToAnnotationSetSikuliImagesPath(annotationName,
						annotationParsedValue, testStepRunner);

			} else if (annotationName
					.equalsIgnoreCase(Annotations.REPORTTESTSTEP)) {
				callToAnnotationreportTestStep(testStepRunner, testStepData,
						annotationParsedValue);
			} else if (annotationName
					.equalsIgnoreCase(Annotations.SWITCHTOENGINE)) {
				callToAnnotationSwitchToEngine(testStepRunner,
						annotationParsedValue);
			} else {
				EngineManager
						.getInstance()
						.getCurrentExecutionEngine()
						.executeAnnotation(annotationName,
								annotationParsedValue);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Function to run Annotation JumpToTestCase.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @param isCommand
	 *            isCommand
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationJumpToTestCase(TestStepRunner testStepRunner,
			String annotationName, String annotationValue, boolean isCommand)
			throws AFTException {

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (isFileSystemRequest) {
			// Annotation for jumping to some testcase
			String[] jumpCaseID = annotationValue.split(",");
			String scenarioID = jumpCaseID[0].trim();
			String testCaseID = jumpCaseID[1].trim();

			LOGGER.info("Executing jumpTo Test Case for Scenario ID ["
					+ scenarioID + "] and Test Case Id [" + testCaseID + "]");

			testStepRunner.getTestSuiteRunner().setForJumpTo(scenarioID,
					testCaseID);
		} else {
			String scenarioID = testStepRunner.getBizScenarioID();
			String annotationParsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), annotationValue);
			if (!isCommand) {
				String annotationDesc = "Executing Annotation ["
						+ annotationName + "], with the parameter value ["
						+ annotationParsedValue + "]";
				LOGGER.debug(annotationDesc);
				updateTestStepDescription(testStepRunner, annotationDesc,
						annotationValue);
			}
			String testStepID = annotationParsedValue;
			LOGGER.info("Executing jumpTo Test Case for Scenario ID ["
					+ scenarioID + "] and Test Step Id [" + testStepID + "]");
			testStepRunner.getTestSuiteRunner().setForJumpTo(scenarioID,
					testStepID);
			testStepRunner.getTestSuiteRunner().setJumpToStep(true);

		}
	}

	/**
	 * Function to run Annotation Loglevel.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationLoglevel(TestStepRunner testStepRunner,
			String annotationName, String annotationValue) throws AFTException {

		String annotationParsedValue = annotationValue;
		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationParsedValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		// Validate that the entered value is valid
		if (!(annotationParsedValue
				.equalsIgnoreCase(ConfigProperties.LOGLEVEL_DEBUG)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.LOGLEVEL_ERROR)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.LOGLEVEL_FATAL)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.LOGLEVEL_INFO)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.LOGLEVEL_TRACE) || annotationParsedValue
					.equalsIgnoreCase(ConfigProperties.LOGLEVEL_WARN))) {

			LOGGER.warn("Invalid value [" + annotationParsedValue
					+ "] specified for annotation [" + Annotations.LOGLEVEL
					+ "]. Default value [" + ConfigProperties.DEFAULT_LOGLEVEL
					+ "] is assigned.");

			annotationParsedValue = ConfigProperties.DEFAULT_LOGLEVEL;

		}

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + annotationParsedValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationParsedValue);
		}

		// Set log level
		Log4JPlugin.getInstance().setLogLevel(annotationParsedValue);
	}

	/**
	 * Function to run Annotation setExecutionSpeed.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationsetExecutionSpeed(
			TestStepRunner testStepRunner, String annotationName,
			String annotationValue) throws AFTException {

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		int executionSpeed = Constants.DEFAULTEXECUTIONSPEEDVALUE;

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationValue + "]");

		// Verify and assign value. If not valid assign default value.
		if (annotationValue.equalsIgnoreCase(Constants.EXECUTIONSPEEDSLOW)) {
			executionSpeed = Constants.EXECUTIONSPEEDSLOWVALUE;
		} else if (annotationValue
				.equalsIgnoreCase(Constants.EXECUTIONSPEEDMEDIUM)) {
			executionSpeed = Constants.EXECUTIONSPEEDMEDIUMVALUE;
		} else if (annotationValue
				.equalsIgnoreCase(Constants.EXECUTIONSPEEDNORMAL)) {
			executionSpeed = Constants.EXECUTIONSPEEDNORMALVALUE;
		} else {

			LOGGER.warn("Invalid value [" + annotationValue
					+ "] specified for annotation [" + annotationName
					+ "]. Default value [" + Constants.DEFAULTEXECUTIONSPEED
					+ "] is assigned.");
		}

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + annotationValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationValue);
		}
		testStepRunner.getTestSuiteRunner().setExecutionSpeed(executionSpeed);
	}

	/**
	 * Function to run Annotation CaptureScreenShot.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationCaptureScreenShot(
			TestStepRunner testStepRunner, String annotationName,
			String annotationValue) throws AFTException {

		String annotationParsedValue = annotationValue;
		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationParsedValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (!isFileSystemRequest) {
			String parsedValue = "";
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + parsedValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationParsedValue);
		}

		// Validate that the entered value is valid. If not assign config
		// value to property
		if ((!(annotationParsedValue
				.equalsIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ERROR)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ALL)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_NONE) || annotationParsedValue
					.equalsIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_VERIFY)))
				&& annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ERROR)) {

			LOGGER.warn("Invalid value ["
					+ annotationParsedValue
					+ "] specified for annotation ["
					+ Annotations.CAPTURESCREENSHOT
					+ "]. Config value ["
					+ ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.CAPTURESCREENSHOT)
					+ "] is assigned.");

			annotationParsedValue = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.CAPTURESCREENSHOT);

			ConfigProperties.getInstance().setConfigProperty(
					ConfigProperties.CAPTURESCREENSHOT, annotationParsedValue);
		} else {
			ConfigProperties.getInstance().setConfigProperty(
					ConfigProperties.CAPTURESCREENSHOT, annotationParsedValue);
		}
		if (annotationParsedValue.equalsIgnoreCase(ConfigProperties.DEFAULT)) {
			ConfigProperties.getInstance().setConfigProperty(
					ConfigProperties.CAPTURESCREENSHOT,
					ConfigProperties.CAPTURESCREENSHOT_ALL);
		}

	}

	/**
	 * Function to run Annotation Custom Dictionary Path.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationname
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationCustomDictionaryPath(
			TestStepRunner testStepRunner, String annotationname,
			String annotationValue) throws AFTException {

		LOGGER.info("Executing [" + annotationname
				+ "] annotation with value [" + annotationValue + "]");

		testStepRunner.getTestSuiteRunner().getTestSet()
				.setCustomDictionaryPath(annotationValue);
	}

	/**
	 * Function to run Annotation OnError.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationOnError(TestStepRunner testStepRunner,
			String annotationName, String annotationValue) throws AFTException {

		String annotationParsedValue = annotationValue;
		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationParsedValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + annotationValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationValue);
		}
		// Validate that the entered value is valid. If not assign config
		// value to property
		if (!(annotationParsedValue
				.equalsIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTCASE)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSCENARIO)
				|| annotationParsedValue
						.equalsIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSTEP) || annotationParsedValue
					.equalsIgnoreCase(ConfigProperties.ONERROR_RESUMENEXTTESTSUITE))) {

			LOGGER.warn("Invalid value ["
					+ annotationParsedValue
					+ "] specified for annotation ["
					+ Annotations.ONERROR
					+ "]. Config value ["
					+ ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.ONERROR) + "] is assigned.");

			annotationParsedValue = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.ONERROR);
		}

		ConfigProperties.getInstance().setConfigProperty(
				ConfigProperties.ONERROR, annotationParsedValue);
	}

	/**
	 * Function to run Annotation MoveToPrevTestDataRow.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	public void callToAnnotationMoveToPrevTestDataRow(
			TestStepRunner testStepRunner, String annotationName,
			String annotationValue) throws AFTException {

		String annotationParsedValue = annotationValue.toLowerCase();

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationParsedValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (!isFileSystemRequest) {
			String parsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), annotationParsedValue);
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + parsedValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					parsedValue);
		}

		// Get the current test data row value...
		int testDataRowId = testStepRunner.getTestSuiteRunner()
				.getTestDataCurrentRowId(annotationParsedValue);

		if ((testDataRowId - 1) < 1) {
			LOGGER.warn("Test data row counter is already on first row ["
					+ testDataRowId + "]. Cannot move to previous row "
					+ " for sheet [" + annotationParsedValue + "]");

		} else {
			// decrement the value...
			testDataRowId = testDataRowId - 1;

			testStepRunner.getTestSuiteRunner().setTestDataRowId(
					annotationParsedValue, testDataRowId);

			LOGGER.info("Decremented test data row counter to previous row ["
					+ testDataRowId + "] for sheet [" + annotationParsedValue
					+ "]");
		}
	}

	/**
	 * Function to run Annotation MoveToNextTestDataRow.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationMoveToNextTestDataRow(
			TestStepRunner testStepRunner, String annotationName,
			String annotationValue) throws AFTException {

		String annotationParsedValue = annotationValue.toLowerCase();

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationParsedValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		int rowCount = 0;

		if (!isFileSystemRequest) {
			String parsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), annotationParsedValue);
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + parsedValue + "]";
			LOGGER.debug(annotationDesc);
			annotationParsedValue = parsedValue;
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationParsedValue);
			String projectId = RuntimeProperties.getInstance().getProjectId();
			String name = annotationParsedValue.toLowerCase();
			name = new StringBuffer(name.length())
					.append(Character.toTitleCase(name.charAt(0)))
					.append(name.substring(1)).toString();
			StringBuffer tableName = new StringBuffer();
			tableName.append(Constants.TABLEPREFIX).append(projectId)
					.append("_").append(name);
			// get test data row count from DB
			rowCount = DatabaseUtil.getInstance().getTestDataRowCount(
					tableName.toString());
		}

		// Get the current test data row value...
		int testDataRowId = testStepRunner.getTestSuiteRunner()
				.getTestDataCurrentRowId(annotationParsedValue);

		// increment the value...
		testDataRowId = testDataRowId + 1;

		testStepRunner.getTestSuiteRunner().setTestDataRowId(
				annotationParsedValue, testDataRowId);

		LOGGER.info("Moved test data row counter to [" + testDataRowId
				+ "] for sheet [" + annotationParsedValue + "]");

		// check if we have reached test of test data so that system
		// variable could be set properly...
		try {
			if (isFileSystemRequest
					&& testStepRunner.getTestSuiteRunner().getTestDataReader() != null) {
				boolean isEndOfTestData = testStepRunner
						.getTestSuiteRunner()
						.getTestDataReader()
						.isNextRowAvailable(annotationParsedValue,
								testDataRowId);
				testStepRunner.getTestSuiteRunner().setEndOfTestData(
						isEndOfTestData);
				// added +1 to handle AFT_IsEndOfTestData in UI.
			} else if (!isFileSystemRequest && rowCount + 1 == testDataRowId) {
				testStepRunner.getTestSuiteRunner().setEndOfTestData(true);
			}
		} catch (IOException e) {
			// oops, an exception is thrown. Let is consider it as a fact
			// that we have reached end of test data and set the variable to
			// true so that system variable could be set properly...
			testStepRunner.getTestSuiteRunner().setEndOfTestData(true);
		}

		// Now let us see if isEndOfTestData = true due to one of the
		// conditions
		// and if yes, set the value of AFT_IsEndOfTestData = true.
		if (testStepRunner.getTestSuiteRunner().isEndOfTestData()) {
			LOGGER.info("Reached end of test data in sheet ["
					+ annotationParsedValue
					+ "]. Setting AFT_IsEndOfTestData as true");

			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_ISENDOFTESTDATA), true, "true");
		} else {
			LOGGER.info("Still more data in sheet [" + annotationParsedValue
					+ "]. Setting AFT_IsEndOfTestData as false");

			Variable.getInstance()
					.setVariableValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_ISENDOFTESTDATA), true,
							"false");
		}
	}

	/**
	 * Function to run Annotation TestDataRow.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationsetTestDataRow(TestStepRunner testStepRunner,
			String annotationName, String annotationValue) throws AFTException {

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		String sheetName = "";
		int testDataRowId = -1;
		if (isFileSystemRequest) {
			String[] getSetTestDataRowParameters = annotationValue.split(",");
			sheetName = getSetTestDataRowParameters[0].trim().toLowerCase();
			String rowId = getSetTestDataRowParameters[1].trim();

			// first check: is it a numeric value
			testDataRowId = Integer.parseInt(rowId);
			LOGGER.info("Set test data rowid [" + testDataRowId
					+ "] for sheet [" + sheetName + "]");

		} else {
			if (annotationValue
					.startsWith(Constants.TESTDATASTARTVARIABLEIDENTIFIER)) {
				String value = annotationValue
						.substring(Constants.TESTDATASTARTVARIABLEIDENTIFIER
								.length());
				value = value.replace(",", "~");
				String[] valueList = value.split("~");
				if (valueList[0]
						.endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER)) {
					sheetName = valueList[0].substring(0,
							valueList[0].length() - 2);
				}
				sheetName = sheetName.toLowerCase();
				// if element value starts with "#" then get the value
				String parsedValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), valueList[1]);
				String annotationDesc = "Executing Annotation ["
						+ annotationName + "], with parameter value ["
						+ parsedValue + "]";
				LOGGER.debug(annotationDesc);
				String elementValue = sheetName + "," + parsedValue;
				updateTestStepDescription(testStepRunner, annotationDesc,
						elementValue);
				if (parsedValue != null && !parsedValue.equals("")) {
					// first check: is it a numeric value
					testDataRowId = Integer.parseInt(parsedValue);
				}
			}
			LOGGER.info("Set test data rowid [" + testDataRowId
					+ "] for table [" + sheetName + "]");
		}
		// Looks valid, now assign the test data row system variable to new
		// value
		// defined by the user....
		testStepRunner.getTestSuiteRunner().setTestDataRowId(sheetName,
				testDataRowId);

	}

	/**
	 * Update the test step action description
	 * 
	 * @param annotationDesc
	 *            annotationDesc
	 * @param annotationValue
	 *            annotationValue
	 * @param testStepRunner
	 *            testStepRunner
	 */
	public void updateTestStepDescription(TestStepRunner testStepRunner,
			String annotationDesc, String annotationValue) {
		if (!testStepRunner.getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			String value = annotationValue;
			TestScenario testScenario = ReportGenerator.getInstance()
					.getLatestTestSuite().getTestScenario();

			if (testScenario != null
					&& testScenario.getTestCaseDetails().size() > 0) {
				List<TestStep> stepList = testScenario.getTestCase()
						.getTestStepDetails();
				if (stepList != null && stepList.size() > 0) {
					TestStep step = stepList.get(stepList.size() - 1);
					step.setActionDescription(annotationDesc);
					step.setElementName("");
					if (value == null
							|| (value != null && value.equals("novalue"))) {
						value = "";
					}
					step.setElementValue(value);
				}
			}
		}
	}

	/**
	 * Function to run Annotation TerminateCurrentTestCase.
	 * 
	 * @param annotationname
	 *            the annotation name
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationTerminateCurrentTestCase(String annotationname)
			throws AFTException {

		LOGGER.info("Executing Annotation [" + annotationname + "]");
		LOGGER.info("Terminating Current Test Case");
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTCASE), true,
				"true");
	}

	/**
	 * Function to run Annotation TerminateCurrentTestSuite.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationTerminateCurrentTestSuite(
			TestStepRunner testStepRunner, String annotationName)
			throws AFTException {

		LOGGER.info("Executing Annotation [" + annotationName + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc, "");
		}
		LOGGER.info("Terminating Current Test Suite");
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSUITE), true,
				"true");

	}

	/**
	 * Function to run Annotation TerminateCurrentTestScenario.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationTerminateCurrentTestScenario(
			TestStepRunner testStepRunner, String annotationName)
			throws AFTException {

		LOGGER.info("Executing Annotation [" + annotationName + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc, "");
		}
		LOGGER.info("Terminating Current Test Scenario");
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TERMINATECURRENTTESTSCENARIO),
				true, "true");
	}

	/**
	 * Function to run Annotation Continue.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param isCommand
	 *            isCommand
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationContinue(TestStepRunner testStepRunner,
			String annotationName, boolean isCommand) throws AFTException {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "]";
			if (!isCommand) {
				LOGGER.debug(annotationDesc);
				updateTestStepDescription(testStepRunner, annotationDesc, "");
			}
		}
		LOGGER.info("Executing Annotation [" + annotationName + "]");
		LOGGER.debug("Continue with execution of test suite");
	}

	/**
	 * Function to call Annotation Raise Exception.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationRaiseException(TestStepRunner testStepRunner,
			String annotationName, String annotationValue) throws AFTException {

		LOGGER.info("Executing [" + annotationName
				+ "] annotation with value [" + annotationValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing [" + annotationName
					+ "] annotation with value [" + annotationValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationValue);
		}
		String strExceptionMsg = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), annotationValue);

		throw new AFTException(strExceptionMsg);
	}

	/**
	 * Function to call Annotation Log Message
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationlogMessage(TestStepRunner testStepRunner,
			String annotationValue) throws AFTException {

		LOGGER.info("Executing annotation logMessage");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		if (!isFileSystemRequest) {
			String annotationDesc = "Executing [logMessage] annotation with value ["
					+ annotationValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationValue);
		}

		// Splitting the annotation value

		List<String> arrParam = Helper.getInstance().parseActionParameterList(
				testStepRunner.getTestSuiteRunner(), annotationValue, true);
		String loglevel = "";
		String logMessage = "";
		if (arrParam.size() < 2) {
			String errMsg = "Invalid annotation usage ["
					+ annotationValue
					+ "] specified. Please refer technical documentation on how to use [@logMessage] annotaion";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		loglevel = arrParam.get(0).trim();
		logMessage = arrParam.get(1).trim();

		// LOGGER.info("[" + logMessage + "]");
		// LOGGER.info("[" + loglevel + "]");
		// Printing the Message as per the log level

		if (loglevel.equalsIgnoreCase("info")) {
			LOGGER.info("[" + logMessage + "]");
		} else if (loglevel.equalsIgnoreCase("debug")) {
			LOGGER.debug("[" + logMessage + "]");
		} else if (loglevel.equalsIgnoreCase("warn")) {
			LOGGER.warn("[" + logMessage + "]");
		} else if (loglevel.equalsIgnoreCase("trace")) {
			LOGGER.trace("[" + logMessage + "]");
		} else if (loglevel.equalsIgnoreCase("fatal")) {
			LOGGER.fatal("[" + logMessage + "]");
		} else if (loglevel.equalsIgnoreCase("error")) {
			LOGGER.error("[" + logMessage + "]");
		} else {
			LOGGER.warn("Please refer wiki and specify appropriate log level "
					+ "to print the logMessage");
		}
	}

	/**
	 * Call to annotation set tcm integration test case id.
	 * 
	 * @param testStepRunner
	 *            the testStepRunner
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationSetTCMIntegrationTestCaseID(
			TestStepRunner testStepRunner, String annotationValue)
			throws AFTException {

		String strTestCaseId = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), annotationValue);

		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TCMINTEGRATION_TESTCASEID), true,
				strTestCaseId);
	}

	/**
	 * Call to annotation set UseScriptInstance.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationUseScriptInstance(
			TestStepRunner testStepRunner, String annotationName,
			String annotationValue) throws AFTException {

		LOGGER.info("Storing script instance [" + annotationValue
				+ "] in system variable [AFT_CURRENTSCRIPTINSTANCE] by default");

		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + annotationValue + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(testStepRunner, annotationDesc,
					annotationValue);
		}

		String strScriptInstance = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), annotationValue);

		ScriptInstance scriptInstance = ScriptInstanceManager.getInstance()
				.getStoredScriptInstance(strScriptInstance);

		if (scriptInstance != null) {
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_CURRENTSCRIPTINSTANCE), true,
					annotationValue);
		} else {
			String errMsg = "Invalid script identifier ["
					+ annotationValue
					+ "] specified. Pls verify that the script instance is valid and has not been destroyed already.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

	}

	/**
	 * Call to annotation set DISPLAYSCROLLABLEFRAME.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @param annotationname
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationDisplayScrollableFrame(
			TestStepRunner testStepRunner, String annotationname,
			String annotationValue) throws AFTException {

		LOGGER.info("Executing Annotation [" + annotationname + "]"
				+ " with value [" + annotationValue + "]");

		if (annotationValue.equalsIgnoreCase("true")) {
			ConfigProperties
					.getInstance()
					.setConfigProperty(
							ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME,
							"Yes");

			testStepRunner.getTestSuiteRunner().displayScrollableFrame();
		} else if (annotationValue.equalsIgnoreCase("false")) {
			ConfigProperties.getInstance().setConfigProperty(
					ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME, "No");
			LOGGER.info("Scrollable frame stopped");
			ScrollableFrame.getInstance().stop();
		} else {
			LOGGER.info("Invalid annotation value ["
					+ annotationValue
					+ "] passed."
					+ "Global Configuration value ["
					+ ConfigProperties
							.getInstance()
							.getConfigProperty(
									ConfigProperties.SHOW_SCROLLABLE_TEST_SCENARIO_FRAME)
					+ "] will be used");
		}
	}

	/**
	 * Call to annotation set SETSIKULIIMAGESPATH.
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @param stepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationSetSikuliImagesPath(String annotationName,
			String annotationValue, TestStepRunner stepRunner)
			throws AFTException {

		LOGGER.info("Executing Annotation [" + annotationName + "]"
				+ " with value [" + annotationValue + "]");
		String value = annotationValue;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		if (!isFileSystemRequest) {
			String annotationDesc = "Executing Annotation [" + annotationName
					+ "], with parameter value [" + value + "]";
			LOGGER.debug(annotationDesc);
			updateTestStepDescription(stepRunner, annotationDesc, value);
		}

		if (value == null || value.isEmpty()) {
			LOGGER.info("Invalid annotation value ["
					+ value
					+ "] passed."
					+ "Global Configuration value ["
					+ ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.SIKULI_IMAGESPATH)
					+ "] will be used");
		} else {
			if (!isFileSystemRequest) {
				value = Helper.getInstance().getActionValue(
						stepRunner.getTestSuiteRunner(), value);
			}
			SikuliInstance.getInstance().setImagePath(value);
		}
	}

	/**
	 * Function to call Annotation Report TestStep
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param TestStepData
	 *            the testStepData
	 * @throws AFTException
	 *             the application exception
	 */
	private void callToAnnotationreportTestStep(TestStepRunner testStepRunner,
			TestStep testStepData, String annotationParsedValue)
			throws AFTException {
		if (testStepRunner.getTestSuiteRunner().getTestBatchRunner()
				.isDisableStandardReportingFlag()) {
			LOGGER.info("Executing annotation reportTestStep");
			String[] params = null;
			String exeDescription = "";
			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();
			testStepRunner.getTestSuiteRunner()
					.setTestCaseIdCount(
							testStepRunner.getTestSuiteRunner()
									.getTestCaseIdCount() + 1);

			TestCase testCaseReportingObj = new TestCase();
			testCaseReportingObj.setTestCaseId("TestCase_"
					+ testStepRunner.getTestSuiteRunner().getTestCaseIdCount()
					+ "");
			testCaseReportingObj.setSortId(String.valueOf(testStepRunner
					.getiTestStepMapID()));
			testCaseReportingObj.setReusable(testStepRunner.isReusable());
			testCaseReportingObj.setTestCaseDesc(Helper.getInstance()
					.getActionValue(testStepRunner.getTestSuiteRunner(),
							testStepData.getElementValue()));
			// add to report generator instance
			ReportGenerator.getInstance().getLatestTestSuite()
					.getTestScenario().addTestCase(testCaseReportingObj);
			TCMReportGenerator.getInstance().getTestSuite().getTestScenario()
					.addTestCase(testCaseReportingObj);

			// setting test step execution time
			long execTime = testStepRunner.getTestSuiteRunner()
					.getTotalTestStepExecTime();
			testStepRunner.getTestSuiteRunner().setTotalTestStepExecTime(0);
			if (isFileSystemRequest) {
				params = annotationParsedValue.split(",");
				exeDescription = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(),
						testStepData.getElementValue());
			} else {
				String[] paramsList = annotationParsedValue.split("\\^");
				params = paramsList[0].split(",");
				exeDescription = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), paramsList[1]);
			}
			// Init Test step reporting object

			TestStep reportTestStep = new TestStep();
			reportTestStep.setActionDescription(exeDescription);
			reportTestStep.setErrorMessage(testStepData.getErrorMessage());
			reportTestStep.setResult(params[0]);
			if (params[0].equalsIgnoreCase(Constants.FAIL)) {
				ReportGenerator.getInstance().getLatestTestSuite()
						.getTestScenario().setExecutionResult(Constants.FAIL);
			}
			reportTestStep.setElementName(testStepData.getElementName());
			reportTestStep.setElementValue(testStepData.getElementValue());
			reportTestStep.setAction(testStepData.getAction());
			reportTestStep.setStepType(testStepData.getStepType());
			reportTestStep.setStepId(testStepData.getStepId());
			reportTestStep.setSortId(String.valueOf(testStepRunner
					.getiTestStepMapID()));
			reportTestStep.setTestStepExecutionTime(execTime);

			if (params.length > 1) {
				// screen capture for full screen
				if (params[1].equalsIgnoreCase(Constants.FULLSCREEN)) {
					testStepRunner.captureScreenShot(reportTestStep,
							testStepRunner.getTestSuiteRunner()
									.getCurrentBusinessScenarioTestStepCount(),
							testStepData.getStepType(), false, true);
				} else {
					if (EngineManager.getInstance().getCurrentExecutionEngine() != null) {
						String objectId = EngineManager.getInstance()
								.getCurrentExecutionEngine()
								.getObjectId(params[1]);
						UICommandFixtures objUICommand = new UICommandFixtures();
						testStepRunner.setReportTestStep(reportTestStep);
						// screen capture for specific element
						objUICommand.getElementScreenShot(testStepRunner,
								objectId, testStepData.getElementValue(),
								params[1]);

					}
				}
			}

			// add to report generator instance
			ReportGenerator.getInstance().getLatestTestSuite()
					.getTestScenario().getTestCase()
					.addTestStep(reportTestStep);
		} else {
			LOGGER.info("Skipped the execution of annotation reportTestStep as config property 'DisableStandardReportingFlag' is not set to Yes");
		}
	}

	/**
	 * Call to annotation switch to engine.
	 * 
	 * @param testStepRunner
	 *            the test step runner
	 * @param annotationValue
	 *            the annotation value
	 * @return the ix aft engine
	 * @throws AFTException
	 *             the aFT exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void callToAnnotationSwitchToEngine(TestStepRunner testStepRunner,
			String annotationValue) throws AFTException, IOException {
		int position = annotationValue.indexOf('^');
		String annotationName = annotationValue.substring(0, position);
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		// Update the Test set with the values specified by user from Test Suite
		List<String> parsedValues = Helper.getInstance()
				.parseActionParameterList(testStepRunner.getTestSuiteRunner(),
						annotationValue, true);

		// If user has specified less than 2 params, mandate
		if (parsedValues.size() < 2) {
			String errMsg = "Missing parameters!.Please specify "
					+ "the [appUrl] and [browser] type to Switch the Engine";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

		// Iterate through parameters passed
		for (String parsedValue : parsedValues) {
			// Check if the parameter is OR/appname/browser
			if (parsedValue.endsWith("xml")) {
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet()
						.setObjectRepositoryPath(parsedValue);
			} else if (parsedValue.toLowerCase().contains("frankenstein")
					|| parsedValue.toLowerCase().contains("http")) {
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().setApplicationUrl(parsedValue);
			} else if (parsedValue.contains("firefox")
					|| parsedValue.contains("iexplore")
					|| parsedValue.contains("chrome")
					|| parsedValue.contains("safari")) {
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().setBrowser(parsedValue);
			} else {
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().setApplicationName(parsedValue);
			}
		}
		// if the Engine Map is not empty
		EngineManager.getInstance().getExecutionEngine(
				annotationName,
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().getApplicationUrl(),
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().getApplicationName(),
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().getBrowser(),
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getDynamicTestSet().getObjectRepositoryPath(),
				isFileSystemRequest);

	}
}