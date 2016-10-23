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

package com.ags.aft.engine.appium;

import org.apache.log4j.Logger;

import com.ags.aft.appium.fixtures.CommandFixtures;
import com.ags.aft.appium.fixtures.ValidationFixtures;
import com.ags.aft.appium.fixtures.WaitFixtures;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.appium.common.UIFixtureUtils;

/**
 * The Class seleniumMethodSelector.
 */
public class AppiumActionParser {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(AppiumActionParser.class);

	// fixture objects
	/** The UICommand fixture object. */
	private CommandFixtures objUICommand = new CommandFixtures();
	/** The Wait fixture object. */
	private WaitFixtures objWait = new WaitFixtures();
	/** The Validation fixture object. */
	private ValidationFixtures objValidate = new ValidationFixtures();

	/** The Start Time. */
	private long testActionStartTime;

	/** The Executing Action Flag. */
	private boolean isExecutingAction = false;

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
	 * @param objectID
	 *            parsed object id
	 * @param parsedElementValue
	 *            parsed action value
	 * @param actualValue
	 *            actualValue
	 * @return returned value
	 * @throws AFTException
	 *             the application exception
	 */
	String parseAndExecute(TestStepRunner testStepRunner, String action,
			String elementName, String objectID, String parsedElementValue,
			String actualValue) throws AFTException {
		String result = "";
		try {
			if (!action.equals("")) {
				// set action start time
				setTestActionStartTime(System.currentTimeMillis());
				// set Executing Action flag to true.
				setExecutingAction(true);
				// Command fixtures
				if (action.equalsIgnoreCase("click")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.click(objectID, elementName,
							parsedElementValue);
				} else if (action.equalsIgnoreCase("swipe")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objUICommand.swipe(parsedElementValue);

					result = Boolean.valueOf(bResult).toString();

				} else if (action.equalsIgnoreCase("tap")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objUICommand.tap(parsedElementValue);

					result = Boolean.valueOf(bResult).toString();

				}
				else if (action.equalsIgnoreCase("type")
						|| action.equalsIgnoreCase("selectOptionByLabel")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand
							.type(objectID, parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("clearText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.clearText(objectID, elementName);
				} else if (action.equalsIgnoreCase("getValue")
						|| action.equalsIgnoreCase("getSelectOptionByLabel")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUICommand.getValue(objectID, elementName);
				} else if (action.equalsIgnoreCase("check")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.check(objectID, elementName);
				} else if (action.equalsIgnoreCase("uncheck")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.unCheck(objectID, elementName);
				} else if (action.equalsIgnoreCase("isTextPresent")) {
					LOGGER.trace("Command [" + action + "], element name ["
							+ elementName + "], object [" + objectID
							+ "], value [" + parsedElementValue + "]");
					boolean bResult = objUICommand.isTextPresent(objectID,
							parsedElementValue, elementName);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("getState")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objUICommand.getState(objectID,
							elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("isElementPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					boolean bResult = objUICommand.isElementPresent(objectID,
							elementName);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifyValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifyValue(objectID,
							elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifyState")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean stateValue = objValidate.verifyState(elementName,
							objectID, parsedElementValue);
					result = Boolean.valueOf(stateValue).toString();
				} else if (action.equalsIgnoreCase("verifyText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = elementName;
					value = String.valueOf(Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(), value));
					boolean bResult = objValidate.verifyText(value,
							parsedElementValue, elementName);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifyElement")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean stateValue = objValidate.verifyElement(objectID,
							parsedElementValue, elementName);
					result = Boolean.valueOf(stateValue).toString();
				} else if (action.equalsIgnoreCase("verifySelectOptions")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifySelectOptions(objectID,
							parsedElementValue, elementName);
					result = Boolean.valueOf(bResult).toString();
				}
				// Wait fixtures...
				else if (action.equalsIgnoreCase("wait")) {
					LOGGER.trace("Command [" + action + "], value ["
							+ parsedElementValue + "]");
					String value = parsedElementValue;
					objWait.wait(value);
				} else if (action.equalsIgnoreCase("waitForText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objWait.waitForText(objectID, parsedElementValue);
				} else if (action.equalsIgnoreCase("waitforelementpresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value == null || value.isEmpty()) {
						value = String.valueOf(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}
					objWait.waitForElementPresent(objectID,
							Integer.parseInt(value), elementName);
				} else if (action.equalsIgnoreCase("waitForElementToVanish")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value == null || value.isEmpty()) {
						value = String.valueOf(UIFixtureUtils.getInstance()
								.getElementWaitTime());

					}
					objWait.waitForElementToVanish(objectID,
							Integer.parseInt(value), elementName);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			// set Executing Action flag to false.
			setExecutingAction(false);
		}
		return result;
	}

	/**
	 * @return isExecutingAction
	 */
	public boolean isExecutingAction() {
		return isExecutingAction;
	}

	/**
	 * @param isExecutingAction
	 *            the isExecutingAction to set
	 */
	public void setExecutingAction(boolean isExecutingAction) {
		this.isExecutingAction = isExecutingAction;
	}

	/**
	 * @return testActionStartTime
	 */
	public long getTestActionStartTime() {
		return testActionStartTime;
	}

	/**
	 * @param testActionStartTime
	 *            the testActionStartTime to set
	 */
	public void setTestActionStartTime(long testActionStartTime) {
		this.testActionStartTime = testActionStartTime;
	}
}