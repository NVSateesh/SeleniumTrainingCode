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
 * Class: ValidationFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * to validate the check point specified for the test case or business scenario
 */

package com.ags.aft.appium.fixtures;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.robotium.common.AFTRobotiumBase;
import com.ags.aft.appium.fixtures.CommandFixtures;
import com.ags.aft.appium.common.UIFixtureUtils;
import com.jayway.android.robotium.remotecontrol.solo.Solo;

/**
 * The Class aftValidationFixtures.
 */
public class ValidationFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ValidationFixtures.class);

	/**
	 * Instantiates a new aft validation fixtures.
	 * 
	 */
	public ValidationFixtures() {
		super();
	}

	/**
	 * Verifies the innertext of the element with the text pattern to be
	 * validated OR verify the text present in a .pdf file
	 * 
	 * @param textPattern
	 *            The text pattern to validate
	 * 
	 * @return boolean: true/false
	 * 
	 * @throws AFTException
	 */
	public boolean verifyText(String obJectID, String textPattern,
			String elementName) throws AFTException {
		LOGGER.trace("Executing command: [verifyText] with textPattern ["
				+ textPattern + "]");
		try {
			CommandFixtures command = new CommandFixtures();
			boolean isPresent = command.isTextPresent(obJectID, textPattern,
					elementName);
			if (isPresent) {
				LOGGER.info("Verify: Success, the expected inner text value ["
						+ textPattern + "] found");
				return isPresent;
			} else {
				throw new AFTException(
						"Verify: Failed, the expected inner text value ["
								+ textPattern + "] does not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Validates value of the ObjectId against the expected value
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param value
	 *            the value to verify
	 * 
	 * @return boolean: true/false
	 * @throws AFTException
	 */
	public boolean verifyValue(String objectID, String elementName, String value)
			throws AFTException {
		boolean bTextValueMatch = false;
		String objectLocator = objectID;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			LOGGER.trace("Executing command: [verifyValue] for ObjectId ["
					+ objectLocator + "], value [" + value + "]");
			if (new CommandFixtures().isElementPresent(objectLocator,
					elementName)) {
				LOGGER.trace("Element [" + objectLocator + "] is found");
				String objectValue = UIFixtureUtils.getInstance()
						.findElement(repositoryObject, objectLocator)
						.getAttribute("value");
				if (objectValue.equalsIgnoreCase(value)) {
					LOGGER.info("Verify: Success, value in object is ["
							+ objectValue + "], expected value is [" + value
							+ "]");
					bTextValueMatch = true;
				} else {
					LOGGER.error("Verify: Failed, Value in object is  ["
							+ objectValue + "], expected value is [" + value
							+ "]");
					bTextValueMatch = false;
				}
				if (!bTextValueMatch) {
					throw new AFTException(
							"Verify: Failed, The expected value [" + value
									+ "] for object [" + objectLocator
									+ "] does not match with actual value ["
									+ objectValue + "]");
				}

			} else {
				String errorMsg = "Element [" + objectLocator + "] not found";
				LOGGER.error(errorMsg);
				throw new AFTException(errorMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return bTextValueMatch;
	}

	/**
	 * Verify control state for editable objects like textbox, combobox, list
	 * control, radi button etc
	 * 
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	public boolean verifyState(String elementName, String objectID,
			String expectedValue) throws AFTException {
		boolean stateValue = false;
		String objectLocator = objectID;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}
			if (new CommandFixtures().isElementPresent(objectLocator,
					elementName)) {
				stateValue = objectState(elementName, expectedValue,
						objectLocator, objType, repositoryObject);
			} else {
				String errMsg = "Element [" + elementName + "] not found";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return stateValue;
	}

	/**
	 * This method is related to verifyState method Verify control state for
	 * editable objects like textbox, combobox, list control, radi button etc
	 * 
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * @param objectLocator
	 *            objectLocator
	 * @param objType
	 *            objType
	 * @param repositoryObject
	 *            repositoryObject
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */

	private boolean objectState(String elementName, String expectedValue,
			String objectLocator, String objType,
			RepositoryObject repositoryObject) throws AFTException {
		boolean stateValue = false;
		try {
			LOGGER.trace("Element [" + objectLocator
					+ "] found. Checking the state of the element.");
			if (objType.equalsIgnoreCase("Textbox")
					|| objType.equalsIgnoreCase("Combobox")
					|| objType.equalsIgnoreCase("Dropdown")) {

				stateValue = objectType(elementName, expectedValue,
						objectLocator, objType, repositoryObject);
			} else if (objType.equalsIgnoreCase("Checkbox")
					|| objType.equalsIgnoreCase("Radiobutton")
					|| objType.equalsIgnoreCase("Listbox")) {

				stateValue = objectType(elementName, expectedValue,
						objectLocator, objType, repositoryObject);
			} else if (objType.equalsIgnoreCase("SpanArea")
					|| objectLocator.toLowerCase().contains("input")
					|| objectLocator.toLowerCase().contains("select")) {

				stateValue = objectType(elementName, expectedValue,
						objectLocator, objType, repositoryObject);

			} else {
				String errMsg = "Verify: Failed, Element [" + elementName
						+ "], [" + objType + "] is not of input type.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return stateValue;
	}

	/**
	 * This method is related to objectState method Verify control state for
	 * editable objects like textbox, combobox, list control, radi button etc
	 * 
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * @param objectLocator
	 *            objectLocator
	 * @param objType
	 *            objType
	 * @param repositoryObject
	 *            repositoryObject
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */

	private boolean objectType(String elementName, String expectedValue,
			String objectLocator, String objType,
			RepositoryObject repositoryObject) throws AFTException {
		boolean stateValue = false;
		try {
			LOGGER.trace("Element [" + objectLocator + "], [" + objType
					+ "] is of input type.");

			boolean isEditable = false;
			boolean isVisible = false;
			try {
				WebElement element = UIFixtureUtils.getInstance().findElement(
						repositoryObject, objectLocator);
				isVisible = element.isDisplayed();
				if (!objType.equalsIgnoreCase("spn")
						|| !objType.equalsIgnoreCase("img")) {
					isEditable = element.isEnabled();
				}
			} catch (WebDriverException se) {
				LOGGER.error("WebDriverException:: " + se);
				throw new AFTException(se);
			}

			if (expectedValue.toLowerCase().contains("visible")
					|| expectedValue.toLowerCase().contains("hidden")) {

				stateValue = verifyVisible(elementName, expectedValue,
						objectLocator, objType, stateValue, isVisible);

			}

			if (expectedValue.toLowerCase().contains("enabled")
					|| expectedValue.toLowerCase().contains("disabled")) {
				if (isEditable
						&& expectedValue.toLowerCase().contains("enabled")) {
					LOGGER.trace("Element [" + objectLocator + "] is editable");
					LOGGER.info("Verify: Success, State of [" + objType
							+ "] element [" + elementName
							+ "] matches with expected value [" + expectedValue
							+ "]");

					stateValue = true;
				} else if (!isEditable
						&& expectedValue.toLowerCase().contains("disabled")) {
					LOGGER.trace("Element [" + objectLocator
							+ "] is not editable");
					LOGGER.info("Verify: Success, State of [" + objType
							+ "] element [" + elementName
							+ "] matches with expected value [" + expectedValue
							+ "]");

					stateValue = true;
				} else {
					String errMsg = "Verify: Failed, Element [" + elementName
							+ "] is ["
							+ (isEditable ? "editable" : "not editable")
							+ "]. Expected value [" + expectedValue
							+ "] does not match actual value.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return stateValue;

	}

	/**
	 * This method is subMethod of objectType method
	 * 
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * @param objectLocator
	 *            objectLocator
	 * @param objType
	 *            objType
	 * @param stateValue
	 *            stateValue
	 * @param isVisible
	 *            isVisible
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	public boolean verifyVisible(String elementName, String expectedValue,
			String objectLocator, String objType, boolean stateValue,
			boolean isVisible) throws AFTException {
		boolean objState = stateValue;
		try {
			if (isVisible && expectedValue.toLowerCase().contains("visible")) {
				LOGGER.trace("Element [" + objectLocator + "] is visible");
				LOGGER.info("Verify: Success, State of [" + objType
						+ "] element [" + elementName
						+ "] matches with expected value [" + expectedValue
						+ "]");

				objState = true;
			} else if (!isVisible
					&& expectedValue.toLowerCase().contains("hidden")) {
				LOGGER.trace("Element [" + objectLocator + "] is visible");
				LOGGER.info("Verify: Success, State of [" + objType
						+ "] element [" + elementName
						+ "] matches with expected value [" + expectedValue
						+ "]");

				objState = true;
			} else {
				String errMsg = "Verify: Failed, Element [" + elementName
						+ "] is [" + (isVisible ? "visible" : "not visible")
						+ "]. Expected value [" + expectedValue
						+ "] does not match actual value.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return objState;
	}

	/**
	 * Verifies the specified item list exists in the list box or drop down
	 * items.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param value
	 *            the list of items to be verified
	 * @param elementName
	 *            elementName
	 * @return true or false based on the verification
	 * @throws AFTException
	 */
	public boolean verifySelectOptions(String objectID, String value,
			String elementName) throws AFTException {
		boolean result = false;
		LOGGER.trace("Verifying that element [" + objectID + "] to be present");
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			Solo solo = AFTRobotiumBase.getInstance().getSolo();
			if (repositoryObject.getType().equalsIgnoreCase("Combobox")) {
				result = solo.isSpinnerTextSelected(
						Integer.parseInt(repositoryObject.getIndex()), value);
			}
			if (result) {
				LOGGER.info("Verify: Success, Expected item " + "[" + value
						+ "]" + "was selected");
			} else {
				String errMsg = ("Verify: Failure, Expected item " + "["
						+ value + "]" + "was not selected");
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return result;
	}

	/**
	 * Verifies the existence of the element
	 * 
	 * @param objectID
	 *            The element to check the existence
	 * @param value
	 *            Expected existence of the element
	 * @param elementName
	 *            elementName
	 * @return boolean: true/false
	 * @throws AFTException
	 */
	public boolean verifyElement(String objectID, String value,
			String elementName) throws AFTException {
		boolean isElementPresent = false;
		boolean actualExistence = false;

		if (value == null || value.length() <= 0) {
			LOGGER.error("User must specify if they are looking for existence or non-existence of the element. Refer to xAFT documentation for more details");
			throw new AFTException(
					"User must specify if they are looking for existence or non-existence of the element. Refer to xAFT documentation for more details");
		}

		boolean expectedExistence = Boolean.parseBoolean(value);
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);

		if (repositoryObject == null) {
			LOGGER.error("Element [" + elementName
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + elementName
					+ "] not found in Object Repository");
		}
		try {
			WebElement element = UIFixtureUtils.getInstance().findElement(
					repositoryObject, objectID);
			if (element != null) {
				actualExistence = true;
			}
		} catch (Exception e) {
			if (expectedExistence) {
				LOGGER.error("Element [" + objectID + "] not found");
			} else {
				LOGGER.info("Element [" + objectID + "] not found");
			}
		}

		LOGGER.trace("Executing command: [verifyElement] with ObjectId ["
				+ objectID + "], value [" + value + "]");

		if (actualExistence == expectedExistence) {
			LOGGER.info("Verify: Success, expected element[" + objectID
					+ "] existence as [" + expectedExistence
					+ "] and actual existence is [" + actualExistence + "]");
			isElementPresent = true;
		} else {
			LOGGER.error("Verify: Failed, expected element[" + objectID
					+ "] existence as [" + expectedExistence
					+ "] and actual existence is [" + actualExistence + "]");
			isElementPresent = false;

			throw new AFTException(
					"Verify: Failed, The expected element existence ["
							+ expectedExistence + "] for object [" + objectID
							+ "] does not match the actual element existence ["
							+ actualExistence + "]");
		}

		return isElementPresent;
	}

}