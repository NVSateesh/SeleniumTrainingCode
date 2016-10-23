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
 * Class: SikuliFixtures
 * 
 * Purpose: This class implements the sikuli methods
 */

package com.ags.aft.fixtures.sikuli;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.sikuli.script.App;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.ags.aft.webdriver.common.UIFixtureUtils;

/**
 * The Class SikuliFixture.
 */
public final class SikuliFixture {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(SikuliFixture.class);
	/** The SikuliFixture */
	private static SikuliFixture sikuliFixture = null;

	private String parsedValue = null;
	private String errorMessage = null;

	/** Constant Literals */
	private String strConstException = "Exception::";
	private String strConstFileExtn = ".png";
	private String strConstCreateObj = "Creating a new pattern using object [";
	private String strConstNoValue = "novalue";
	private Match imageMatch = null;

	private int intConst100 = 100;
	private int intConst500 = 500;
	private int intConst1000 = 1000;

	/**
	 * Instantiates a new AFT Sikuli Fixture
	 */
	private SikuliFixture() {
		super();
	}

	/**
	 * Gets the single instance of SikuliFixture.
	 * 
	 * @return single instance of SikuliFixture
	 * @throws Exception
	 */
	public static SikuliFixture getInstance() {
		if (sikuliFixture == null) {
			sikuliFixture = new SikuliFixture();
			LOGGER.trace("Creating instance of SikuliFixture");
		}

		return sikuliFixture;
	}

	/**
	 * Creates a new Pattern object and stores the object in sikuliObjectMap
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param userVariable
	 *            - User defined return variable
	 * @param parameterList
	 *            - User passed parameters
	 * @return object identifier as String
	 * @throws AFTException
	 */
	public String createPattern(TestStepRunner testStepRunner,
			String userVariable, String parameterList) throws AFTException {
		String objectIdentifier = null;
		RepositoryObject repoObject = null;

		try {
			// if user has not passed variable
			// if (isEmptyValue(userVariable)) {
			// errorMessage = "No user variable passed. "
			// + "Please refer to wiki on how to use [createPattern]";
			// LOGGER.error(strConstException + errorMessage);
			// throw new AFTException(errorMessage);
			// }

			// split parameterList
			String[] splitParams = parameterList.trim().split(
					"\\" + Constants.ATTRIBUTESDELIMITER);

			// get elementName
			String elementName = splitParams[0];

			// get RepositoryObject
			repoObject = ObjectRepositoryManager.getInstance().getObject(
					elementName);

			// if objectID not found
			if (repoObject == null) {
				errorMessage = "Element [" + elementName
						+ "] not found in Object Repository";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			// check if pattern object can be created
			Pattern objPattern = parseParametersnCreatePatternObject(
					repoObject, splitParams);

			// generate an unique identifier for the created Pattern object
			objectIdentifier = SikuliObjectManager.getInstance()
					.createUniqueObjectIdentifier("Pattern");

			// store the (objectIdentifier, RepositoryObject object) in
			// sikuliObjectMap
			SikuliObjectManager.getInstance().addSikuliPatternObject(
					objectIdentifier, objPattern);

			// assign objectIdentifier to user passed variable
			LOGGER.info("Storing object identifier [" + objectIdentifier
					+ "] in user variable [" + userVariable + "]");
			Variable.getInstance().setVariableValue(
					testStepRunner.getTestSuiteRunner(), "createPattern",
					userVariable, false, objectIdentifier);

		} catch (Exception e) {

			LOGGER.error(strConstException + e.getMessage());
			throw new AFTException(e);
		}

		return objectIdentifier;
	}

	/**
	 * Opens an application
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param userVariable
	 * 
	 *            User defined return variable
	 * @param strAppNameOrPath
	 * 
	 *            Application name or path
	 * @throws AFTException
	 */
	public void openApplication(TestStepRunner testStepRunner,
			String userVariable, String strAppNameOrPath) throws AFTException {

		App sikuliApp = null;
		try {

			// get parsed value in case user passed
			// application name/path using variable/test data
			parsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), strAppNameOrPath);

			// if user has not passed application name/path
			if (isEmptyValue(parsedValue)) {
				errorMessage = "No application name/path passed. "
						+ "Please refer to wiki on how to use [openApplication]";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			// if user has not passed variable
			if (userVariable == null || userVariable.isEmpty()
					|| userVariable.equalsIgnoreCase(strConstNoValue)) {
				errorMessage = "No user variable passed. "
						+ "Please refer to wiki on how to use [openApplication]";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			// call sikuli open
			sikuliApp = App.open(parsedValue);

			if (sikuliApp == null) {
				errorMessage = "Application[" + parsedValue
						+ "] not found or could not be opened";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			// generate an unique identifier for the created Application object
			String objectIdentifier = SikuliObjectManager.getInstance()
					.createUniqueObjectIdentifier("Application");

			// store the (objectIdentifier, Application object) in
			// sikuliApplicationMap
			SikuliObjectManager.getInstance().addSikuliAppObject(
					objectIdentifier, sikuliApp);

			// assign objectIdentifier to user passed variable
			LOGGER.info("Storing object identifier [" + objectIdentifier
					+ "] in user variable [" + userVariable + "]");
			Variable.getInstance().setVariableValue(
					testStepRunner.getTestSuiteRunner(), "openApplication",
					userVariable, false, objectIdentifier);

		} catch (Exception e) {
			LOGGER.error(strConstException + e.getMessage());
			throw new AFTException(e);
		}
	}

	/**
	 * Brings the application to focus
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param userVariableOrWindowTitle
	 * 
	 *            User Variable/Window title
	 * @throws AFTException
	 */
	public void selectApplication(TestStepRunner testStepRunner,
			String userVariableOrWindowTitle) throws AFTException {

		App sikuliApp = null;
		boolean noFocus = false;

		try {
			// if user has not passed application details
			if (isEmptyValue(userVariableOrWindowTitle)) {
				errorMessage = "No application details passed. "
						+ "Please refer to wiki on how to use [selectApplication]";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			parsedValue = Helper.getInstance().getActionValue(
					userVariableOrWindowTitle);

			// user passed a variable
			if (userVariableOrWindowTitle
					.startsWith(Constants.DYNAMICVARIABLEDELIMITER)) {

				LOGGER.info("User has passed a variable. Lets get the stored SikuliApp.");

				// get the stored application
				sikuliApp = getApplicationObject();

				if (sikuliApp.focus() == null) {
					noFocus = true;
				}

			} else {
				// user passed a window title
				LOGGER.info("User has passed a window title");

				if (App.focus(parsedValue) == null) {
					noFocus = true;
				}
			}

			if (noFocus) {

				errorMessage = "Could not get focus to application["
						+ parsedValue + "]";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

		} catch (Exception e) {

			LOGGER.error(strConstException + e.getMessage());
			throw new AFTException(e);

		} finally {

			sikuliApp = null;
		}
	}

	/**
	 * Closes the application
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param userVariableOrWindowTitle
	 * 
	 *            User Variable/Window title
	 * @throws AFTException
	 */
	public void closeApplication(TestStepRunner testStepRunner,
			String userVariableOrWindowTitle) throws AFTException {

		App sikuliApp = null;

		try {
			// if user has not passed application details
			if (isEmptyValue(userVariableOrWindowTitle)) {
				errorMessage = "No application details passed. "
						+ "Please refer to wiki on how to use [closeApplication]";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

			parsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(),
					userVariableOrWindowTitle);

			// user passed a variable
			if (userVariableOrWindowTitle
					.startsWith(Constants.DYNAMICVARIABLEDELIMITER)) {

				LOGGER.info("User has passed a variable. Lets get the stored SikuliApp.");

				// get the stored application
				sikuliApp = getApplicationObject();

				if (sikuliApp.focus() == null) {
					errorMessage = "Could not get focus to application["
							+ parsedValue + "]";
					LOGGER.error(strConstException + errorMessage);
					throw new AFTException(errorMessage);
				}

				// close the application
				sikuliApp.close();

			} else {

				// user passed a window title
				LOGGER.info("User has passed a window title");

				if (App.focus(parsedValue) == null) {
					errorMessage = "Could not get focus to application["
							+ parsedValue + "]";
					LOGGER.error(strConstException + errorMessage);
					throw new AFTException(errorMessage);
				}

				// close the application
				App.close(parsedValue);
			}

		} catch (Exception e) {

			LOGGER.error(strConstException + e.getMessage());
			throw new AFTException(e);
		} finally {
			sikuliApp = null;
		}
	}

	/**
	 * Clicks on a given object(Pattern/String/Region/Match) on the screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @throws AFTException
	 *             the exception
	 * @throws IOException
	 */
	public void sikuliClick(TestStepRunner testStepRunner,
			String objectIdOrUserVariable) throws AFTException, IOException {

		String value = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), objectIdOrUserVariable);
		String[] values = value.split("\\^");
		String image = values[0];
		Screen objScreen = null;
		Pattern objPattern = null;
		if (image.contains(".png")) {
			// Append the OR path to image name
			image = SikuliInstance.getInstance().getImagePath() + "\\" + image;
		}
		try {
			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				objScreen.click(image);
			} else {
				// User flagged multipleImages in OR
				if (checkMultipleImages(testStepRunner, value)) {
					LOGGER.info("User has flagged multipleImages attribute in OR");
					objPattern = createPatternAndCheckExists(testStepRunner,
							value);
				} else {
					// user passed an OR object or user variable
					objPattern = createOrRetrievePatternObject(testStepRunner,
							objectIdOrUserVariable);
				}
				objScreen.click(objPattern);
			}

			/*
			 * // if user passed an image name[eg:-xxxx.png] if
			 * (isImageName(testStepRunner, objectIdOrUserVariable)) {
			 * objScreen.click(parsedValue); } else { // user passed an OR
			 * object or user variable objPattern =
			 * createOrRetrievePatternObject(testStepRunner,
			 * objectIdOrUserVariable); objScreen.click(objPattern); }
			 */
			LOGGER.info("[click] executed on the given object/image");

		} catch (FindFailed ff) {
			LOGGER.error(strConstException + ff);
			throw new AFTException(ff);
		} catch (Exception e) {
			LOGGER.error(strConstException + e);
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}
	}

	/**
	 * Gets focus to the element, clears existing text and types new text
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @param typeText
	 *            text to be written
	 * @throws AFTException
	 *             the exception
	 */
	public void sikuliType(TestStepRunner testStepRunner,
			String objectIdOrUserVariable, String typeText) throws AFTException {

		Screen objScreen = null;
		Pattern objPattern = null;

		try {

			objScreen = SikuliInstance.getInstance().getSikuliScreen();

			// Doing doubleClick to get focus and select the existing text
			// Calling type will then overwrite the existing text

			LOGGER.trace("Getting focus on the object");

			String value = Helper.getInstance()
					.getActionValue(testStepRunner.getTestSuiteRunner(),
							objectIdOrUserVariable);
			String[] values = value.split("\\^");
			String image = values[0];

			if (image.contains(".png")) {
				// Append the OR path to image name
				image = SikuliInstance.getInstance().getImagePath() + "\\"
						+ image;
			}

			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				objScreen.doubleClick(image);
			} else {
				// User flagged multipleImages in OR
				if (checkMultipleImages(testStepRunner, value)) {
					LOGGER.info("User has flagged multipleImages attribute in OR");
					objPattern = createPatternAndCheckExists(testStepRunner,
							value);
				} else {
					// user passed an OR object or user variable
					objPattern = createOrRetrievePatternObject(testStepRunner,
							objectIdOrUserVariable);
				}
				objScreen.doubleClick(objPattern);
			}

			//
			// // if user passed an image name[eg:-xxxx.png]
			// if (isImageName(testStepRunner, objectIdOrUserVariable)) {
			// objScreen.doubleClick(parsedValue);
			// } else {
			// // user passed an OR object or user variable
			// objPattern = createOrRetrievePatternObject(testStepRunner,
			// objectIdOrUserVariable);
			// objScreen.doubleClick(objPattern);
			// }

			Thread.sleep(intConst500);

			LOGGER.info("Executing command: [type] with value [" + typeText
					+ "]");
			objScreen.type(typeText);

			LOGGER.info("[type] executed with value[" + typeText + "]");

		} catch (FindFailed ff) {
			LOGGER.error(strConstException + ff);
			throw new AFTException(ff);
		} catch (Exception e) {
			LOGGER.error(strConstException + e);
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}

	}

	/**
	 * Double clicks on a given object(Pattern/String/Region/Match) on the
	 * screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            Object id or User defined return variable
	 * @throws AFTException
	 *             the exception
	 * @throws IOException
	 */
	public void sikuliDoubleClick(TestStepRunner testStepRunner,
			String objectIdOrUserVariable) throws AFTException, IOException {

		String value = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), objectIdOrUserVariable);
		String[] values = value.split("\\^");
		String image = values[0];
		Screen objScreen = null;
		Pattern objPattern = null;
		if (image.contains(".png")) {
			// Append the OR path to image name
			image = SikuliInstance.getInstance().getImagePath() + "\\" + image;
		}
		try {
			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				objScreen.doubleClick(image);
			} else {
				// User flagged multipleImages in OR
				if (checkMultipleImages(testStepRunner, value)) {
					LOGGER.info("User has flagged multipleImages attribute in OR");
					objPattern = createPatternAndCheckExists(testStepRunner,
							value);
				} else {
					// user passed an OR object or user variable
					objPattern = createOrRetrievePatternObject(testStepRunner,
							objectIdOrUserVariable);
				}
				objScreen.doubleClick(objPattern);
			}
			LOGGER.info("[Double Click] executed on the given object/image");

		} catch (FindFailed ff) {
			LOGGER.error(strConstException + ff);
			throw new AFTException(ff);
		} catch (Exception e) {
			LOGGER.error(strConstException + e);
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}

	}

	/**
	 * Checks the presence of given object(Pattern/String/Region/Match) on the
	 * screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            Object id or User defined return variable
	 * @return boolean either true/false.
	 * @throws AFTException
	 *             the exception
	 * @throws IOException
	 */
	private boolean checkForImagePresence(TestStepRunner testStepRunner,
			String objectIdOrUserVariable) throws AFTException, IOException {

		boolean actionResult = false;
		Screen objScreen = null;
		String value = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), objectIdOrUserVariable);
		String[] values = value.split("\\^");
		String image = values[0];
		Pattern objPattern = null;
		if (image.contains(".png")) {
			// Append the OR path to image name
			image = SikuliInstance.getInstance().getImagePath() + "\\" + image;
		}
		try {
			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				imageMatch = objScreen.exists(image);
			} else {
				// user passed an OR object or user variable
				objPattern = createOrRetrievePatternObject(testStepRunner,
						objectIdOrUserVariable);
				imageMatch = objScreen.exists(objPattern);
			}

			if (imageMatch != null) {
				actionResult = true;
			}

		} catch (Exception e) {
			LOGGER.error(strConstException + e);
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}
		return actionResult;

		// try {
		//
		// // get the parsed element name
		// parsedValue = Helper.getInstance()
		// .getActionValue(testStepRunner.getTestSuiteRunner(),
		// objectIdOrUserVariable);
		//
		// // if user has not passed objectid/variable
		// if (parsedValue == null || parsedValue.isEmpty()
		// || parsedValue.equalsIgnoreCase(strConstNoValue)) {
		//
		// errorMessage = "No object id or user variable passed.";
		// LOGGER.error(strConstException + errorMessage);
		// throw new AFTException(errorMessage);
		//
		// }
		//
		// // if user passed an image filename
		// if (parsedValue.toLowerCase().contains(strConstFileExtn)) {
		//
		// fileNameWithExtn = parsedValue;
		//
		// } else {
		//
		// // user passed an objectID
		// // get repositoryobject from OR manager
		// repoObject = ObjectRepositoryManager.getInstance().getObject(
		// objectIdOrUserVariable);
		//
		// if (repoObject == null) {
		//
		// errorMessage = "Element [" + objectIdOrUserVariable
		// + "] not found in Object Repository.";
		// LOGGER.error(strConstException + errorMessage);
		// throw new AFTException(errorMessage);
		//
		// } else {
		// // get the imageName
		// fileNameWithExtn = repoObject.getImageName()
		// + strConstFileExtn;
		//
		// }
		//
		// }
		//
		// objScreen = SikuliInstance.getInstance().getSikuliScreen();
		//
		// imageMatch = objScreen.exists(fileNameWithExtn);
		//
		// if (imageMatch != null) {
		// actionResult = true;
		// }
		// } catch (Exception e) {
		// LOGGER.error(strConstException + e.toString());
		// throw new AFTException(e);
		// } finally {
		// imageMatch = null;
		// }
		//
		// return actionResult;

	}

	/**
	 * Checks the presence of given object(Pattern/String/Region/Match) on the
	 * screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            Object id or User defined return variable
	 * @param returnVariable
	 *            return variable
	 * @return boolean true/false;
	 * @throws AFTException
	 *             the exception
	 */
	public boolean isImagePresent(TestStepRunner testStepRunner,
			String objectIdOrUserVariable, String returnVariable)
			throws AFTException {

		boolean actionResult = false;

		LOGGER.trace("Executing command: [isImagePresent] with elementName ["
				+ objectIdOrUserVariable + "], elementValue [" + returnVariable
				+ "]");

		try {

			actionResult = checkForImagePresence(testStepRunner,
					objectIdOrUserVariable);

			String strImageFile = "Image [" + imageMatch + "]";

			if (actionResult) {
				LOGGER.info(strImageFile + " found successfully");
			} else {
				LOGGER.warn(strImageFile + " could not be found");
			}

			// if user has provided returnVariable
			if (returnVariable != null && !returnVariable.isEmpty()
					&& !returnVariable.equalsIgnoreCase(strConstNoValue)) {
				LOGGER.info("Storing action result in user passed return variable ["
						+ returnVariable + "]");
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(), "isImagePresent",
						returnVariable, false, String.valueOf(actionResult));
			} else {
				LOGGER.info("No return variable passed by user."
						+ "Action response is stored in AFT_LastActionResponse by default.");
			}

		} catch (Exception e) {
			LOGGER.error(strConstException + e.toString());
			throw new AFTException(e);
		}

		return actionResult;

	}

	/**
	 * Waits given time for given image to appear on the screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @param timeInMilliSecs
	 *            - Time to wait in millisecs
	 * @return true/false if object appears or not
	 * @throws AFTException
	 */
	public boolean waitForImage(TestStepRunner testStepRunner,
			String objectIdOrUserVariable, String timeInMilliSecs)
			throws AFTException {

		boolean actionResult = false;
		Match imageFound = null;
		Screen objScreen = null;
		Pattern objPattern = null;

		LOGGER.trace("Executing command: [waitForImage] with elementName ["
				+ objectIdOrUserVariable + "], elementValue ["
				+ timeInMilliSecs + "]");

		try {

			int waitTimeInSecs = 0;

			// get elementWaitTime from AFTConfig if user does not pass waitTime
			if (timeInMilliSecs.equalsIgnoreCase(strConstNoValue)
					|| timeInMilliSecs == null || timeInMilliSecs.isEmpty()) {
				waitTimeInSecs = (UIFixtureUtils.getInstance()
						.getElementWaitTime()) / intConst1000;
				LOGGER.info("No wait time passed by User, using elementWaitTime ["
						+ waitTimeInSecs + "] secs");
			} else {
				waitTimeInSecs = Integer.parseInt(timeInMilliSecs)
						/ intConst1000;
			}

			String value = Helper.getInstance()
					.getActionValue(testStepRunner.getTestSuiteRunner(),
							objectIdOrUserVariable);
			String[] values = value.split("\\^");
			String image = values[0];

			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				// Append the OR path to image name
				image = SikuliInstance.getInstance().getImagePath() + "\\"
						+ image;
				imageFound = objScreen.wait(image, waitTimeInSecs);
			} else {
				// user passed an OR object or user variable
				objPattern = createOrRetrievePatternObject(testStepRunner,
						objectIdOrUserVariable);
				imageFound = objScreen.wait(objPattern, waitTimeInSecs);
			}

			// // if user passed an image name[eg:-xxxx.png]
			// if (isImageName(testStepRunner, objectIdOrUserVariable)) {
			// imageFound = objScreen.wait(parsedValue, waitTimeInSecs);
			// } else {
			// // user passed an OR object or user variable
			// objPattern = createOrRetrievePatternObject(testStepRunner,
			// objectIdOrUserVariable);
			// imageFound = objScreen.wait(objPattern, waitTimeInSecs);
			// }

			LOGGER.info("Executed [waitForImage] with timeToWaitInSecs ["
					+ waitTimeInSecs + "] for the given object/image");

			if (imageFound != null) {
				LOGGER.info("Image was found successfully");
				actionResult = true;
			} else {
				errorMessage = "Image was NOT found";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			}

		} catch (Exception e) {
			LOGGER.error(strConstException + e.toString());
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
			if (imageFound != null) {
				imageFound = null;
			}
		}

		return actionResult;

	}

	/**
	 * Waits given time for given object to disappear from the screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @param timeInMilliSecs
	 *            - Time to wait in millisecs
	 * @return true/false if object is present or not
	 * @throws AFTException
	 */
	public boolean waitForObjectToVanish(TestStepRunner testStepRunner,
			String objectIdOrUserVariable, String timeInMilliSecs)
			throws AFTException {

		boolean waitForVanish = false;
		Screen objScreen = null;
		Pattern objPattern = null;

		try {

			int waitTimeInSecs = 0;

			// get elementWaitTime from AFTConfig if user does not pass waitTime
			if (timeInMilliSecs.equalsIgnoreCase(strConstNoValue)) {
				waitTimeInSecs = (UIFixtureUtils.getInstance()
						.getElementWaitTime()) / 1000;
			} else {
				waitTimeInSecs = Integer.parseInt(timeInMilliSecs) / 1000;
			}

			String value = Helper.getInstance()
					.getActionValue(testStepRunner.getTestSuiteRunner(),
							objectIdOrUserVariable);
			String[] values = value.split("\\^");
			String image = values[0];
			if (image.contains(".png")) {
				// Append the OR path to image name
				image = SikuliInstance.getInstance().getImagePath() + "\\"
						+ image;
			}

			objScreen = SikuliInstance.getInstance().getSikuliScreen();
			if (image.endsWith(".png") && values.length < 2) {
				LOGGER.info("User Specified Image Name with no Offset and accuracy");
				waitForVanish = objScreen.waitVanish(image, waitTimeInSecs);
			} else {
				// user passed an OR object or user variable
				objPattern = createOrRetrievePatternObject(testStepRunner,
						objectIdOrUserVariable);
				waitForVanish = objScreen
						.waitVanish(objPattern, waitTimeInSecs);
			}

			// objScreen = SikuliInstance.getInstance().getSikuliScreen();
			//
			// // if user passed an image name[eg:-xxxx.png]
			// if (isImageName(testStepRunner, objectIdOrUserVariable)) {
			// waitForVanish = objScreen.waitVanish(parsedValue,
			// waitTimeInSecs);
			// } else {
			// // user passed an OR object or user variable
			// objPattern = createOrRetrievePatternObject(testStepRunner,
			// objectIdOrUserVariable);
			// waitForVanish = objScreen
			// .waitVanish(objPattern, waitTimeInSecs);
			// }
			//
			LOGGER.info("Executed [waitForObjectToVanish] with timeToWaitInSecs ["
					+ waitTimeInSecs + "] for the given object/image");

			if (!waitForVanish) {
				errorMessage = "Object is STILL present";
				LOGGER.error(strConstException + errorMessage);
				throw new AFTException(errorMessage);
			} else {
				LOGGER.info("Object is not present");
			}

		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}

		return waitForVanish;
	}

	/**
	 * Verifies if the image is present/not present on a page
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @param objectIdOrUserVariable
	 *            objectID/userVariable/imageName
	 * @param parsedElementValue
	 *            true/false specified by user
	 * 
	 * @return boolean: true/false
	 * 
	 * @throws AFTException
	 */
	public boolean verifyImage(TestStepRunner testStepRunner,
			String objectIdOrUserVariable, String parsedElementValue)
			throws AFTException {

		boolean actionResult = false;

		LOGGER.trace("Executing command: [verifyImage] with elementName ["
				+ objectIdOrUserVariable + "], elementValue ["
				+ parsedElementValue + "]");

		try {

			actionResult = checkForImagePresence(testStepRunner,
					objectIdOrUserVariable);

			String strImageFile = "Image [" + imageMatch + "]";

			if (parsedElementValue.equalsIgnoreCase("true")
					|| parsedElementValue.equalsIgnoreCase(strConstNoValue)) {
				if (actionResult) {
					LOGGER.info(strImageFile + " found successfully");
					actionResult = true;
				} else {
					errorMessage = strImageFile + " could not be found";
					LOGGER.error(strConstException + errorMessage);
					throw new AFTException(errorMessage);
				}
			}

			if (parsedElementValue.equalsIgnoreCase("false")) {
				if (!actionResult) {
					LOGGER.info("PASSED:: " + strImageFile + " is not present");
					actionResult = true;
				} else {
					errorMessage = strImageFile + " is present";
					LOGGER.error(strConstException + errorMessage);
					throw new AFTException(errorMessage);
				}
			}
		} catch (Exception e) {
			LOGGER.error(strConstException + e.toString());
			throw new AFTException(e);
		}

		return actionResult;
	}

	/**
	 * Moves the mouse pointer on a given object(Pattern/String/Region/Match) on
	 * the screen
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @throws AFTException
	 *             the exception
	 */
	public void mouseHover(TestStepRunner testStepRunner,
			String objectIdOrUserVariable) throws AFTException {

		Screen objScreen = null;
		Pattern objPattern = null;

		try {

			objScreen = SikuliInstance.getInstance().getSikuliScreen();

			// if user passed an image name[eg:-xxxx.png]
			if (isImageName(testStepRunner, objectIdOrUserVariable)) {
				objScreen.mouseMove(parsedValue);
			} else {
				// user passed an OR object or user variable
				objPattern = createOrRetrievePatternObject(testStepRunner,
						objectIdOrUserVariable);
				objScreen.mouseMove(objPattern);
			}

			LOGGER.info("[mouseMove] executed on the given object/image");

		} catch (FindFailed ff) {
			LOGGER.error(strConstException + ff.toString());
			throw new AFTException(ff);
		} catch (Exception e) {
			LOGGER.error(strConstException + e.toString());
			throw new AFTException(e);
		} finally {
			if (objPattern != null) {
				objPattern = null;
			}
		}
	}

	/**
	 * Checks if user passed direct image name
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @return true if user passed an image file name
	 * @throws AFTException
	 */
	private boolean isImageName(TestStepRunner testStepRunner,
			String objectIdOrUserVariable) throws AFTException {

		boolean isImageFileName = false;

		// get the parsed value
		parsedValue = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), objectIdOrUserVariable);

		// if user has not passed objectid/variable/static data
		if (parsedValue == null || parsedValue.isEmpty()
				|| objectIdOrUserVariable.equalsIgnoreCase(strConstNoValue)) {
			errorMessage = "No data found in the passed variable";
			LOGGER.error(strConstException + errorMessage);
			throw new AFTException(errorMessage);
		}

		// if user passed an image filename
		if (parsedValue.toLowerCase().contains(strConstFileExtn)
				|| objectIdOrUserVariable.toLowerCase().contains(
						strConstFileExtn)) {
			isImageFileName = true;
		}

		return isImageFileName;
	}

	/**
	 * Creates/Retrieves the Pattern object as per user input either an OR
	 * object or User defined variable
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param objectIdOrUserVariable
	 *            - Object id or User defined return variable
	 * @return Pattern object
	 * @throws AFTException
	 */
	private Pattern createOrRetrievePatternObject(
			TestStepRunner testStepRunner, String objectIdOrUserVariable)
			throws AFTException {

		RepositoryObject repoObject = null;
		Pattern objPattern = null;

		// get the parsed value
		parsedValue = Helper.getInstance().getActionValue(
				testStepRunner.getTestSuiteRunner(), objectIdOrUserVariable);
		String[] parsedValues = parsedValue.split("\\^");
		String elementName = parsedValues[0];
		// // if user passed a variable
		// if (objectIdOrUserVariable
		// .startsWith(Constants.DYNAMICVARIABLEDELIMITER)) {
		// // check the stored value
		// if (!SikuliObjectManager.getInstance().isSikuliPatternObjectExists(
		// parsedValue)) {
		//
		// errorMessage = "No object found in user variable ["
		// + objectIdOrUserVariable + "]";
		// LOGGER.error(strConstException + errorMessage);
		// throw new AFTException(errorMessage);
		//
		// } else {
		// // get Pattern object from sikuliObjectMap
		// objPattern = SikuliObjectManager.getInstance()
		// .getSikuliPatternObject(parsedValue);
		// LOGGER.info("Using the Pattern object associated with ["
		// + parsedValue + "]");
		// }

		// user passed an objectID
		// } else {
		// get repositoryobject from OR manager
		if (!elementName.contains(".png")) {
			repoObject = ObjectRepositoryManager.getInstance().getObject(
					elementName);

		}

		// create new pattern object
		objPattern = parseParametersnCreatePatternObject(repoObject,
				parsedValues);
		// }

		return objPattern;
	}

	/**
	 * Creates and returns a new Pattern object
	 * 
	 * @param repoObject
	 *            repoObject
	 * @param paramList
	 *            paramList
	 * @return Pattern object
	 * @throws AFTException
	 */
	private Pattern parseParametersnCreatePatternObject(
			RepositoryObject repoObject, String[] paramList)
			throws AFTException {

		String strAccuracy = null;
		Float floatSimilar = null;
		boolean noAccuracy = true;
		String elementName = paramList[0];
		String strTargetOffset = null;
		int xCoord = 0;
		int yCoord = 0;

		boolean noOffset = true;

		// Get accuracy from & Target Offset from Test Suite
		if (paramList.length > 1) {
			for (int i = 1; i < paramList.length; i++) {
				if (paramList[i].contains(",")) {
					LOGGER.info("User had passed the target offset ["
							+ paramList[i] + "]");
					// Get the x & y coordinates
					String[] params = paramList[i].split(",");
					// Get the x coordinate
					xCoord = Integer.parseInt(params[0]);
					// Get the y coordinate
					yCoord = Integer.parseInt(params[1]);
					noOffset = false;
				} else {
					// Get the accuracy
					LOGGER.info("User had passed the Accuracy [" + paramList[i]
							+ "]");
					floatSimilar = new Float(paramList[i]);
					floatSimilar = floatSimilar / intConst100;
					noAccuracy = false;
					break;
				}
			}
		} else {
			// get accuracy if specified in OR
			strAccuracy = repoObject.getAccuracy();
			if (strAccuracy != null) {
				strAccuracy = strAccuracy.trim();
			}

			if (strAccuracy != null && !strAccuracy.isEmpty()) {
				floatSimilar = new Float(strAccuracy);
				floatSimilar = floatSimilar / intConst100;
				noAccuracy = false;
			}

			// get targetOffset if specified in OR
			strTargetOffset = repoObject.getTargetOffset();
			if (strTargetOffset != null) {
				strTargetOffset = strTargetOffset.trim();
			}

			if (strTargetOffset != null && !strTargetOffset.isEmpty()) {
				String[] splitTargetOffset = null;
				splitTargetOffset = strTargetOffset.split(",");
				xCoord = Integer.parseInt(splitTargetOffset[0]);
				yCoord = Integer.parseInt(splitTargetOffset[1]);
				noOffset = false;
			}
		}

		// Append the OR path
		if (elementName.endsWith(".png")) {
			elementName = SikuliInstance.getInstance().getImagePath() + "\\"
					+ elementName;
		}

		Pattern patternObject = createPatternObject(elementName, repoObject,
				noAccuracy, noOffset, floatSimilar, xCoord, yCoord);

		return patternObject;
	}

	/**
	 * Creates the new pattern object.
	 * 
	 * @param repoObject
	 *            the repo object
	 * @param noAccuracy
	 *            the no accuracy
	 * @param noOffset
	 *            the no offset
	 * @param floatSimilar
	 *            the float similar
	 * @param xCoord
	 *            the x coord
	 * @param yCoord
	 *            the y coord
	 * @return the pattern
	 * @throws AFTException
	 */
	private Pattern createPatternObject(String elementName,
			RepositoryObject repoObject, boolean noAccuracy, boolean noOffset,
			Float floatSimilar, int xCoord, int yCoord) throws AFTException {

		String objectID = "";
		Pattern patternObject = null;

		if (!elementName.contains(".png")) {
			// get the filename
			objectID = repoObject.getImageName();
			// Append the OR path to image name
			objectID = SikuliInstance.getInstance().getImagePath() + "\\"
					+ objectID + strConstFileExtn;
		} else {
			objectID = elementName;
		}

		// create new pattern object
		if (noAccuracy && noOffset) {

			LOGGER.info(strConstCreateObj + objectID + "] and no properties");

			patternObject = new Pattern(objectID);
		} else if (!noAccuracy && !noOffset) {

			LOGGER.info(strConstCreateObj + objectID

			+ "] with accuracy [" + floatSimilar

			+ "] and targetOffset [" + xCoord + "," + yCoord + "]");

			patternObject = new Pattern(objectID).

			similar(floatSimilar).

			targetOffset(xCoord, yCoord);
		} else if (!noOffset) {

			LOGGER.info(strConstCreateObj + objectID

			+ "] with targetOffset [" + xCoord + "," + yCoord + "]");

			patternObject = new Pattern(objectID).targetOffset(xCoord, yCoord);
		} else if (!noAccuracy) {

			LOGGER.info(strConstCreateObj + objectID

			+ "] with accuracy [" + floatSimilar + "]");

			patternObject = new Pattern(objectID).similar(floatSimilar);
		}

		LOGGER.info("Pattern object created successfully");

		return patternObject;
	}

	/**
	 * Checks for empty/null/novalue user input
	 * 
	 * @param valueToCheck
	 *            test step runner object
	 * @return true/false
	 */
	private boolean isEmptyValue(String valueToCheck) {

		boolean retVal = false;
		if (valueToCheck.isEmpty()
				|| valueToCheck.equalsIgnoreCase(strConstNoValue)) {

			retVal = true;
		}

		return retVal;
	}

	/**
	 * Gets the stored Sikuli App object
	 * 
	 * @return sikuli app object
	 * @throws AFTException
	 */
	private App getApplicationObject() throws AFTException {

		App storedSikuliApp = null;

		// check if identifier exists
		if (!SikuliObjectManager.getInstance().isSikuliAppObjectExists(
				parsedValue)) {
			errorMessage = "No SikuliApp object found in the passed variable";
			LOGGER.error(strConstException + errorMessage);
			throw new AFTException(errorMessage);
		}

		// get the stored application
		storedSikuliApp = SikuliObjectManager.getInstance().getSikuliAppObject(
				parsedValue);

		return storedSikuliApp;
	}

	/**
	 * Checks if user flagged multipleImages in OR
	 * 
	 * @param testStepRunner
	 *            the runner object
	 * @param objectID
	 *            user passed element name
	 * 
	 * @return boolean
	 * @throws AFTException
	 */
	private boolean checkMultipleImages(TestStepRunner testStepRunner,
			String objectID) throws AFTException {

		RepositoryObject repoObject = null;
		String multipleImagesValue = null;
		boolean isMultiple = false;

		repoObject = ObjectRepositoryManager.getInstance().getObject(objectID);

		if (repoObject != null) {
			multipleImagesValue = repoObject.getMultipleImages();
			if ((multipleImagesValue != null)
					&& (!isEmptyValue(multipleImagesValue))
					&& (multipleImagesValue.equalsIgnoreCase("yes"))) {
				isMultiple = true;
			}
		}

		return isMultiple;

	}

	/**
	 * Gets the list of images with matching name
	 * 
	 * @param testStepRunner
	 *            the runner object
	 * @param repoObject
	 *            the repository object
	 * 
	 * @return List<File>
	 * @throws AFTException
	 */
	private List<File> getAllSimilarImages(TestStepRunner testStepRunner,
			RepositoryObject repoObject) throws AFTException {

		final String imageName = repoObject.getImageName();

		LOGGER.info("Getting all the images with matching imagename["
				+ imageName + "] given in OR");

		String imagesDirPath = SikuliInstance.getInstance().getImagePath();

		LOGGER.info("Checking for images under the folder[" + imagesDirPath
				+ "]");

		File imagesDir = new File(imagesDirPath);

		List<File> imagesList = Arrays.asList(imagesDir
				.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(imageName);
					}
				}));

		int imageCount = 0;
		imageCount = imagesList.size();

		if (imageCount > 0) {
			LOGGER.info("Found [" + imageCount + "] matching images");
		} else {
			errorMessage = "No image[s] found with matching imagename["
					+ imageName + "]";
			LOGGER.error(strConstException + errorMessage);
			throw new AFTException(errorMessage);
		}

		return imagesList;

	}

	/**
	 * Creates Pattern and verifies existence on screen for given list of images
	 * 
	 * @param testStepRunner
	 *            the runner object
	 * @param objectID
	 *            user passed element name
	 * 
	 * @return Pattern
	 * @throws AFTException
	 */
	private Pattern createPatternAndCheckExists(TestStepRunner testStepRunner,
			String objectID) throws AFTException {

		boolean patternExists = false;
		Pattern objPattern = null;
		RepositoryObject repoObject = null;
		Screen objScreen = null;

		repoObject = ObjectRepositoryManager.getInstance().getObject(objectID);

		objScreen = SikuliInstance.getInstance().getSikuliScreen();

		List<File> imagesList = getAllSimilarImages(testStepRunner, repoObject);

		LOGGER.info("Checking if any of images exist on screen");
		for (int i = 0; i < imagesList.size(); i++) {
			String imagePath = imagesList.get(i).toString();
			int lastOccurenceofSlash = imagePath.lastIndexOf("\\");
			String imageNameOnly = imagePath
					.substring(lastOccurenceofSlash + 1);
			String[] imageNameArray = { imageNameOnly };
			objPattern = parseParametersnCreatePatternObject(repoObject,
					imageNameArray);
			if (objScreen.exists(objPattern) != null) {
				patternExists = true;
				LOGGER.info("Pattern for the image [" + imageNameOnly
						+ "] is present on the screen");
				break;
			} else {
				LOGGER.info("Pattern for the image [" + imageNameOnly
						+ "] is NOT present on the screen");
			}
		}

		if (!patternExists) {
			errorMessage = "None of the matching image[s] were found on the screen";
			LOGGER.error(strConstException + errorMessage);
			throw new AFTException(errorMessage);
		}

		return objPattern;
	}
}