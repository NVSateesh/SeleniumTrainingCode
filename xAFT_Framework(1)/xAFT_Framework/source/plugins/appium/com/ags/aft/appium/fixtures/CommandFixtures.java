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
 * Class: CommandFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the UI objects like click, type, select, remove
 */

package com.ags.aft.appium.fixtures;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.appium.common.AFTAppiumBase;
import com.ags.aft.appium.common.UIFixtureUtils;

/**
 * The Class CommandFixtures.
 * 
 */
public class CommandFixtures {

	private WaitFixtures waitFixtures = new WaitFixtures();
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(CommandFixtures.class);

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public CommandFixtures() {
	}

	/** The wait for element. */
	private boolean waitForElement;

	/**
	 * Click Command: Clicks on a link, button, checkbox or radio button.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @param elementValue
	 *            the elementValue
	 * @throws AFTException
	 *             the application exception
	 */
	public void click(String objectID, String elementName, String elementValue)
			throws AFTException {
		String objectLocator = objectID;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			waitForElement = waitFixtures.waitForElementPresent(objectLocator,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectLocator + "] is found");
				LOGGER.trace("Executing command: [click]");
				UIFixtureUtils.getInstance()
						.findElement(repositoryObject, objectLocator).click();
				LOGGER.info("[click] executed on [" + objectLocator + "]");
			} else {
				LOGGER.error("Element [" + objectLocator + "] not found");
				throw new AFTException("Element [" + objectLocator
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

	}

	/**
	 * It clears the previous value and Sets the new value of an input field.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param value
	 *            : value to be written
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */
	public Boolean type(String objectID, String value, String elementName)
			throws AFTException {
		Boolean bSuccess = true;
		String objectLocator = objectID;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		try {
			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Waiting for element [" + objectLocator
						+ "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(
						objectLocator, UIFixtureUtils.getInstance()
								.getElementWaitTime(), elementName);
				if (waitForElement) {
					WebElement element = UIFixtureUtils.getInstance()
							.findElement(repositoryObject, objectLocator);
					LOGGER.trace("Element [" + objectLocator + "] is found");
					LOGGER.debug("Trying to type the text");
					element.sendKeys(value);
					LOGGER.info("value [" + value + "] typed in ["
							+ objectLocator + "]");
				}

				else {
					LOGGER.error("Element [" + objectLocator
							+ "] appears disabled");
					throw new AFTException("Element [" + objectLocator
							+ "] appears disabled");
				}
			}

		} catch (WebDriverException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return bSuccess;
	}

	/**
	 * Sets the new value of an input field.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param value
	 *            : value to be written
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void clearText(String objectID, String elementName)
			throws AFTException {
		String objectLocator = objectID;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		try {
			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				if (repositoryObject != null
						&& repositoryObject.getType().equalsIgnoreCase(
								"Textbox")) {
					LOGGER.trace("Executing command: [clearText]");

					UIFixtureUtils.getInstance()
							.findElement(repositoryObject, objectLocator)
							.clear();

				}
				LOGGER.info("value cleared from [" + objectLocator + "]");
			}
		} catch (WebDriverException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Gets the (whitespace-trimmed) value of an input field (or anything else
	 * with a value parameter).
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @return the value
	 * @throws AFTException
	 *             the application exception
	 */
	public String getValue(String objectID, String elementName)
			throws AFTException {
		String value = "";
		String objectLocator = objectID;

		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Executing command: [getValue]");
				value = UIFixtureUtils.getInstance()
						.findElement(repositoryObject, objectLocator)
						.getAttribute("value");
				LOGGER.info("Value retrieved [" + value + "] from ["
						+ objectLocator + "]");
			}
		} catch (WebDriverException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return value;
	}

	/**
	 * Get state.
	 * 
	 * @param elementName
	 *            the element name
	 * @param expectedValue
	 *            the expected value
	 * @return true, if successful
	 * @throws AFTException
	 *             the AFTException
	 */
	public boolean getState(String elementName, String objectID,
			String expectedValue) throws AFTException {
		boolean objectState = false;
		String objType = "";
		String objectLocator = objectID;
		if (objectLocator.isEmpty()) {
			LOGGER.error("Element [" + elementName
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + elementName
					+ "] not found in Object Repository");
		}
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}
			if ((objType.equalsIgnoreCase("Textbox"))
					|| (objType.equalsIgnoreCase("Calendar"))
					|| (objType.equalsIgnoreCase("Combobox"))
					|| (objType.equalsIgnoreCase("Dropdown"))
					|| (objType.equalsIgnoreCase("Checkbox"))
					|| (objType.equalsIgnoreCase("Radiobutton"))
					|| (objType.equalsIgnoreCase("Listbox"))
					|| (objType.equalsIgnoreCase("Radiobutton"))
					|| (objType.equalsIgnoreCase("SpanArea"))
					|| (objType.equalsIgnoreCase("Button"))
					|| (objectLocator.toLowerCase().contains("input"))
					|| (objectLocator.toLowerCase().contains("select"))) {
				LOGGER.trace("Element [" + elementName + "], [" + objType
						+ "] is of input type.");
				WebElement webElement = UIFixtureUtils.getInstance()
						.findElement(repositoryObject, objectLocator);
				if (expectedValue.toLowerCase().contains("exists")) {
					LOGGER.debug("User has passed the expected state as [EXISTS] ");
					LOGGER.info("Checking for [" + elementName + "] existance");
					if (isElementPresent(objectLocator, elementName)) {
						LOGGER.debug("Found the element + [" + elementName
								+ "]");
						objectState = true;
					} else {
						LOGGER.debug("Element + [" + elementName
								+ "] [NOT FOUND]");
					}
				} else if (expectedValue.toLowerCase().contains("visible")) {
					LOGGER.debug("User has passed the expected state as [VISIBLE] ");
					LOGGER.info("Checking for [" + elementName + "] visibility");

					if (webElement != null && webElement.isDisplayed()) {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [VISIBLE]");
						objectState = true;
					} else {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [HIDDEN]");
					}
				} else if (expectedValue.toLowerCase().contains("enabled")) {
					LOGGER.debug("User has passed the expected state as enabled ");
					LOGGER.info("Checking for [" + elementName + "] state");
					if (webElement != null && webElement.isEnabled()) {
						LOGGER.debug("Found the object [" + elementName
								+ "] state as [ENABLED]");
						objectState = true;
					} else {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [DISABLED]");
					}
				} else if (expectedValue.toLowerCase().contains("editable")) {
					LOGGER.debug("User has passed the expected state as [EDITABLE]");
					LOGGER.info("Checking for [" + elementName + "] state");
					if (webElement != null && webElement.isEnabled()) {
						LOGGER.debug("Found the object [" + elementName
								+ "] state as [EDITABLE]");
						objectState = true;
					} else {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [NON-EDITABLE]");
					}
				} else if (expectedValue.toLowerCase().contains("checked")) {
					LOGGER.debug("User has passed the expected state as [CHECKED]");
					LOGGER.info("Checking for [" + elementName + "] state");
					if (webElement != null && webElement.isSelected()) {
						LOGGER.debug("Found the object [" + elementName
								+ "] state as [CHECKED]");
						objectState = true;
					} else {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [UNCHECKED]");
					}
				} else if (expectedValue.toLowerCase().contains("selected")) {
					LOGGER.debug("User has passed the expected state as [SELECTED]");
					LOGGER.info("Checking for [" + elementName + "] state");
					if (webElement != null && webElement.isSelected()) {
						LOGGER.debug("Found the object [" + elementName
								+ "] state as [SELECTED]");
						objectState = true;
					} else {
						LOGGER.debug("Found the element + [" + elementName
								+ "] state as [UNSELECTED]");
					}
				} else {
					String errMsg = "Unknown state passed for Element ["
							+ elementName + "]";
					LOGGER.error("Unknown state [" + expectedValue
							+ "] passed for object + [" + elementName + "]");
					throw new AFTException(errMsg);
				}
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

		return objectState;
	}

	/**
	 * Checks if is element present.currently supported for button.
	 * 
	 * @param objectID
	 *            the object id
	 * @param elementName
	 *            elementName
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean isElementPresent(String objectID, String elementName)
			throws AFTException {
		boolean isElementPresent = false;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);

		try {

			AFTAppiumBase.getInstance().getDriver().manage().timeouts()
					.implicitlyWait(100, TimeUnit.MILLISECONDS);
			repositoryObject = ObjectRepositoryManager.getInstance().getObject(
					elementName);
			WebElement element = UIFixtureUtils.getInstance().findElement(
					repositoryObject, objectID);
			if (element != null) {
				isElementPresent = true;
			}
		} catch (Exception e) {
			LOGGER.warn("Object [" + objectID + "] not found.");
		} finally {
			String elementPollTime = "10";
			// Poll for element existence in DOM
			int pollTime = Integer.parseInt(elementPollTime);
			if (pollTime > 0) {
				AFTAppiumBase.getInstance().getDriver().manage().timeouts()
						.implicitlyWait(pollTime, TimeUnit.SECONDS);
			}
		}

		return isElementPresent;
	}

	/**
	 * Checks if is text present on page.
	 * 
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean isTextPresent(String objectID, String value,
			String elementName) throws AFTException {
		boolean isTextPresent = false;
		LOGGER.trace("Executing command: [isTextPresent]");
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (UIFixtureUtils.getInstance()
					.findElement(repositoryObject, objectID).getText()
					.equalsIgnoreCase(value)) {
				isTextPresent = true;
			}
			LOGGER.trace("is Text present: [isTextPresent]");
		} catch (IllegalThreadStateException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (NumberFormatException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return isTextPresent;
	}

	/**
	 * Check Command: Check a toggle-button (checkbox/radio).
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void check(String objectID, String elementName) throws AFTException {
		String objectLocator = objectID;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Waiting for element [" + objectLocator
						+ "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(
						objectLocator, UIFixtureUtils.getInstance()
								.getElementWaitTime(), elementName);
				if (waitForElement) {
					LOGGER.trace("Element [" + objectLocator + "] is found");
					LOGGER.trace("Executing command: [check]");
					if (UIFixtureUtils.getInstance()
							.findElement(repositoryObject, objectLocator)
							.isSelected()) {
						LOGGER.info("The object in [" + objectLocator
								+ "]is already Checked ");
					} else {
						UIFixtureUtils.getInstance()
								.findElement(repositoryObject, objectLocator)
								.click();
						LOGGER.info("Checked [" + objectLocator + "]");
					}
				} else {
					LOGGER.error("Element [" + objectLocator + "] not found");
					throw new AFTException("Element [" + objectLocator
							+ "] not found");
				}
			}
		} catch (WebDriverException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Uncheck a toggle-button (checkbox/radio).
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void unCheck(String objectID, String elementName)
			throws AFTException {
		String objectLocator = objectID;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Waiting for element [" + objectLocator
						+ "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(
						objectLocator, UIFixtureUtils.getInstance()
								.getElementWaitTime(), elementName);
				if (waitForElement) {
					LOGGER.trace("Element [" + objectLocator + "] is found");
					if (UIFixtureUtils.getInstance()
							.findElement(repositoryObject, objectLocator)
							.isSelected()) {
						LOGGER.trace("Executing command: [uncheck]");
						UIFixtureUtils.getInstance()
								.findElement(repositoryObject, objectLocator)
								.click();
						LOGGER.info("UnChecked [" + objectLocator + "]");
					} else {
						LOGGER.info("Element [" + objectLocator
								+ "] is already unchecked ");
					}
				} else {
					LOGGER.error("Element [" + objectLocator + "] not found");
					throw new AFTException("Element [" + objectLocator
							+ "] not found");
				}
			}
		} catch (WebDriverException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Performs swipe action based on Screen Start and End X&Y coordinates
	 * 
	 * @param value
	 *            the value
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean swipe(String elementValue) throws AFTException {
		boolean swiped;
		String[] coordinates = elementValue.split("\\^");
		String TouchCount = coordinates[0];
		String swipeStartXCoord = coordinates[1];
		String swipeStartYCoord = coordinates[2];
		String swipeEndXCoord = coordinates[3];
		String swipeEndYCoord = coordinates[4];
		LOGGER.debug("Executing command [Swipe] with Start X Coordinate : ["
				+ swipeStartXCoord + "]" + "Swipe Y Start Coordinate"
				+ swipeStartYCoord + "] Swipe End X Coornidate["
				+ swipeEndXCoord + "]" + "Swipe End Y Coordinate["
				+ swipeEndYCoord + "]");

		// Finger Touch count : With how many fingers, user wants to use to
		// simulate
		// the actual swipe action
		int fingerTouchCount = Integer.parseInt(TouchCount);
		int startXCoordinate = Integer.parseInt(swipeStartXCoord);
		int startYCoordinate = Integer.parseInt(swipeStartYCoord);
		int endXCoordinate = Integer.parseInt(swipeEndXCoord);
		int endYCoordinate = Integer.parseInt(swipeEndYCoord);

		HashMap<String, Double> map = new HashMap<String, Double>();
		try {
			map.put("touchCount", (double) fingerTouchCount);
			map.put("startX", (double) startXCoordinate);
			map.put("startY", (double) startYCoordinate);
			map.put("endX", (double) endXCoordinate);
			map.put("endY", (double) endYCoordinate);
			map.put("duration", (double) 0.5);
			LOGGER.debug("Performing swipe action....");
			((JavascriptExecutor) AFTAppiumBase.getInstance().getDriver())
					.executeScript("mobile: swipe", map);
			LOGGER.debug("Swipe Performed!");
			swiped = true;
		} catch (Exception e) {
			swiped = false;
			LOGGER.warn("Faield to perform swipe. Please check the swipe Coordinates and retry ["
					+ e.getMessage() + "]");

			LOGGER.warn("Exception::", e);
		}
		return swiped;
	}

	/**
	 * Performs Tap Simulation based on X&Y coordinates and Tap Duration
	 * 
	 * @param value
	 *            the value
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean tap(String elementValue) throws AFTException {

		boolean tapped;
		String[] coordinates = elementValue.split("\\^");
		String XCoord = coordinates[0];
		String YCoord = coordinates[1];
		String tapDuration = coordinates[2];

		LOGGER.debug("Executing command [Tap] with X Coordinate : [" + XCoord
				+ "]" + "Y Start Coordinate" + YCoord + "] with Tap Duration ["
				+ tapDuration + "]");

		// Finger Touch count : With how many fingers, user wants to use to
		// simulate
		// the actual swipe action
		int XCoordinate = Integer.parseInt(XCoord);
		int YCoordinate = Integer.parseInt(YCoord);
		// Convert String to double
		double duration = Double.parseDouble(tapDuration);
		HashMap<String, Double> map = new HashMap<String, Double>();
		try {
			map.put("tapCount", (double) 1);
			map.put("touchCount", (double) 1);
			map.put("duration", duration);
			map.put("x", (double) XCoordinate);
			map.put("y", (double) YCoordinate);

			LOGGER.debug("Performing [Tap] action....");
			((JavascriptExecutor) AFTAppiumBase.getInstance().getDriver())
					.executeScript("mobile: tap", map);
			LOGGER.debug("Tap Performed!");
			tapped = true;
		} catch (Exception e) {
			tapped = false;
			LOGGER.warn("Failed to perform TAP. Please check the swipe coordinates and retry ["
					+ e.getMessage() + "]");

			LOGGER.warn("Exception::", e);
		}
		return tapped;
	}

	/**
	 * This method will click on Ok or Cancel button of Javascript
	 * Alert/Confirmation.It will return the prompt text.
	 * 
	 * @param action
	 *            the action
	 * @return Retrieved prompt text
	 * @throws AFTException
	 *             the application exception
	 */
	public String getConfirmation(String action) throws AFTException {
		Alert alert = null;
		try {
			alert = AFTAppiumBase.getInstance().getDriver().switchTo().alert();
			LOGGER.debug("Clicking on alert dialoue");
			if (action.toLowerCase().contains("ok")) {
				alert.accept();
				LOGGER.debug("Clicked on Alert OK button");
			} else if (action.toLowerCase().contains("cancel")) {
				alert.dismiss();
				LOGGER.debug("Clicked on Alert CANCEL button");
			} else {
				LOGGER.debug("Invalid input action specified for Alert!");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return alert.getText();
	}

	/**
	 * Java Script executor.
	 * 
	 * @param value
	 *            the value
	 * @return the string
	 * @throws AFTException
	 *             the application exception
	 */
	public String jsExecutor(String value) throws AFTException {
		String returnValue;
		LOGGER.debug("Executing java script [" + value
				+ "] using JavascriptExecutor::executeScript");
		try {
			JavascriptExecutor js = (JavascriptExecutor) AFTAppiumBase
					.getInstance().getDriver();
			returnValue = (String) js.executeScript(value);
			LOGGER.debug("Successfully executed the java script + [" + value
					+ "]");
		} catch (WebDriverException we) {
			LOGGER.error("Exception::", we);
			throw new AFTException(we);
		}

		return returnValue;
	}

	/**
	 * Checks if is alert is present or not. Returns TRUE if Alert is present
	 * Else Return FALSE
	 * 
	 * @return true, if is alert present
	 */
	public boolean isAlertPresent() {
		boolean isAlertPresent = false;
		try {
			Alert alert = AFTAppiumBase.getInstance().getDriver().switchTo()
					.alert();
			isAlertPresent = true;
			LOGGER.debug("Found Alert Box with Message : " + alert.getText());
		} catch (Exception e) {
			LOGGER.warn("Exception::", e);
			isAlertPresent = false;
		}
		return isAlertPresent;
	}

}