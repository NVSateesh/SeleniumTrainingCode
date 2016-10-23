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

package com.ags.aft.appium.common;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.ags.aft.webdriver.fixtures.UICommandFixtures;
import com.ags.aft.webdriver.fixtures.WaitFixtures;
import com.ags.aft.webdriver.fixtures.WebTableFixture;
import com.thoughtworks.selenium.Selenium;

/**
 * The Class UIFixtureUtils.
 */
public final class UIFixtureUtils {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(UIFixtureUtils.class);

	/** The UIFixtureUtils */
	private static UIFixtureUtils uiUtils;

	private static String browserVersion;

	/** The wait fixtures. */
	private final WaitFixtures waitFixtures = new WaitFixtures();

	/** The UI command fixtures. */
	private final UICommandFixtures uiCommandFixtures = new UICommandFixtures();

	/** UI element to highlight. */
	private WebElement highlightElement;

	/** Hold's style property of UI element to highlight. */
	private String style;

	/** Toggle switch to Highlight/Un-highlight UI element */
	private boolean bSetHighlight;

	/** valid actions to highlight UI objects */
	private String[] highlightActions = { "type", "click", "clearText",
			"clickForFileBrowse", "check", "uncheck", "selectOption",
			"selectListOptions", "unselectListOptions", "doubleClick",
			"unselectAllListOptions", "selectOptionByLabel" };

	/**
	 * Instantiates a new UIFixtureUtils
	 */
	private UIFixtureUtils() {
		super();
	}

	/**
	 * Gets the single instance of UIFixtureUtils.
	 * 
	 * @return single instance of UIFixtureUtils
	 */
	public static UIFixtureUtils getInstance() {
		if (uiUtils == null) {
			uiUtils = new UIFixtureUtils();
			LOGGER.trace("Creating instance of UIFixtureUtils");
		}

		return uiUtils;
	}

	/**
	 * This method is developed to handle null value or empty value for
	 * ElementWaitTime config property. This method checks if the property value
	 * is null or empty, it assigns the default value
	 * 
	 * @return Element wait time value (either defined in config property or if
	 *         not defined, default constant value defined
	 * 
	 */
	public int getElementWaitTime() {
		int iElementWaitTime = Integer
				.parseInt(AppiumConfigProperties.DEFAULT_ELEMENT_WAIT_TIME);

		try {
			String elementWaitTime = AppiumConfigProperties.getInstance()
					.getConfigProperty(
							AppiumConfigProperties.ELEMENT_WAIT_TIME_MS);

			if ((elementWaitTime != null) && !elementWaitTime.isEmpty()) {
				iElementWaitTime = Integer.parseInt(elementWaitTime);
			} else {
				LOGGER.warn("Element Wait time not set in AFTConfig.properties file... defaulting the element wait time to ["
						+ AppiumConfigProperties.DEFAULT_ELEMENT_WAIT_TIME
						+ "]");
				iElementWaitTime = Integer
						.parseInt(AppiumConfigProperties.DEFAULT_ELEMENT_WAIT_TIME);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}

		return iElementWaitTime;
	}

	/**
	 * Find element.
	 * 
	 * @param repositoryObject
	 *            the repositoryObject
	 * @param locator
	 *            the locator
	 * @return the web element
	 */
	public WebElement findElement(RepositoryObject repositoryObject,
			String locator) throws AFTException {
		WebElement webElement = null;

		if (repositoryObject != null
				&& repositoryObject.getIdentificationType().equalsIgnoreCase(
						"xpath")) {
			try {
				LOGGER.trace("Appium : User has specified xpath");
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.xpath(locator));
				LOGGER.trace("Appium : Found the element using xpath");
			} catch (Exception e) {
				LOGGER.error("Element [" + locator + "]" + "not found");
			}
		} else if (repositoryObject != null
				&& repositoryObject.getIdentificationType().equalsIgnoreCase(
						"id")) {
			try {
				LOGGER.trace("Appium : User has specified Id");
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.id(locator));
				LOGGER.trace("Appium : Found the element using ID");
			} catch (Exception e) {
				LOGGER.error("Element [" + locator + "]" + "not found");
			}
		} else if (repositoryObject != null
				&& repositoryObject.getIdentificationType().equalsIgnoreCase(
						"name")) {
			try {
				LOGGER.trace("User has specified Name");
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.name(locator));
				LOGGER.trace("Appium : Found the element using Name");
			} catch (Exception e) {
				LOGGER.error("Element [" + locator + "]" + "not found");

			}
		} else if (repositoryObject != null
				&& repositoryObject.getIdentificationType().equalsIgnoreCase(
						"css")) {
			try {
				LOGGER.trace("User has specified css");
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.cssSelector(locator));
				LOGGER.trace("Appium : Found the element using css");
			} catch (Exception e) {
				LOGGER.error("Element [" + locator + "]" + "not found");

			}
		} else if (repositoryObject != null
				&& repositoryObject.getIdentificationType().equalsIgnoreCase(
						"link")) {
			try {
				LOGGER.trace("User has specified link");
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.linkText(locator));
				LOGGER.trace("Appium : Found the element using link");
			} catch (Exception e) {
				LOGGER.error("Element [" + locator + "]" + "not found");

			}
		} else {

			webElement = findElementSub(repositoryObject, locator);

		}
		if (webElement == null) {
			LOGGER.error("Element [" + locator + "]" + " Not Found!");
			throw new AFTException("Element [" + locator + "]" + " Not Found!");
		}
		return webElement;
	}

	/**
	 * This method is sub method of Find element .
	 * 
	 * @param repositoryObject
	 *            the repositoryObject
	 * @param locator
	 *            the locator
	 * @return the web element
	 */

	public WebElement findElementSub(RepositoryObject repositoryObject,
			String locator) throws AFTException {
		WebElement webElement = null;

		try {
			webElement = AFTAppiumBase.getInstance().getDriver()
					.findElement(By.id(locator));
			LOGGER.trace("WebDriver : Found the element using ID");
		} catch (Exception e) {
			try {
				webElement = AFTAppiumBase.getInstance().getDriver()
						.findElement(By.name(locator));
				LOGGER.trace("WebDriver : Found the element using NAME");
			} catch (Exception e1) {
				try {
					webElement = AFTAppiumBase.getInstance().getDriver()
							.findElement(By.xpath(locator));
					LOGGER.trace("WebDriver : Found the element using XPATH");
				} catch (Exception e2) {
					try {
						webElement = AFTAppiumBase.getInstance().getDriver()
								.findElement(By.cssSelector(locator));
						LOGGER.trace("WebDriver : Found the element using CSS");
					} catch (Exception e3) {
						try {
							webElement = AFTAppiumBase.getInstance()
									.getDriver()
									.findElement(By.linkText(locator));
							LOGGER.trace("WebDriver : Found the element using Link");
						} catch (Exception e4) {
							LOGGER.error("Element [" + locator + "]"
									+ " Not Found!");
						}
					}
				}
			}
		}
		return webElement;

	}

	/**
	 * Constructs browser version string and initializes AFT_BrowserVersion
	 * system variable...
	 * 
	 * @param selenium
	 *            selenium object
	 * @return browser version
	 * 
	 * @throws AFTException
	 * 
	 */
	public String getBrowserVersion(Selenium selenium) throws AFTException {

		// if (browserVersion.length() <= 0) {
		// Construct the browser version
		//
		String userAgentString = selenium.getEval("navigator.userAgent");
		browserVersion = userAgentString;
		int index = -1;

		LOGGER.debug("Navigator/Browser user agent details are ["
				+ userAgentString + "]");

		// check if the browser is Internet Explorer
		//
		if (userAgentString.indexOf("MSIE") != -1) {
			browserVersion = getBrowserVersionSub(selenium, index,
					userAgentString);

			// check if the browser is Firefox
			//
		} else if (userAgentString.indexOf("Firefox") != -1) {
			// Extract Firefox version number...
			//
			index = userAgentString.indexOf("Firefox");
			String versionNumber = userAgentString.substring(index + 8);
			// Construct string containing browser name and version
			//
			browserVersion = "Firefox " + versionNumber;

			// check if the browser is Chrome
			//
		} else if (userAgentString.indexOf("Chrome") != -1) {
			// Extract Chrome version number...
			//
			index = userAgentString.indexOf("Chrome");
			String versionNumber = userAgentString.substring(index + 7,
					index + 11);
			// Construct string containing browser name and version
			//
			browserVersion = "Google Chrome " + versionNumber;

			// check if the browser is Android
			//
		} else if (userAgentString.indexOf("Android") != -1) {

			// Extract Android version number...
			//
			index = userAgentString.indexOf("Android");
			String versionNumber = userAgentString.substring(index + 8,
					index + 13);
			// Construct string containing browser name
			//
			browserVersion = "Android ";

			// Init AFT OS Version system variable
			//
			String osVersion = "Android " + versionNumber;
			/*
			 * Below code is commented as the os.name is overriding if we run
			 * the first test set for andriod and second test for firefox .
			 */
			// System.setProperty("os.name", osVersion);

			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_OSVERSION), true, osVersion);

			LOGGER.debug("Value for system variable [AFT_OSVersion] set to ["
					+ Helper.getInstance().getActionValue(
							Variable.getInstance().generateSysVarName(
									SystemVariables.AFT_OSVERSION)) + "]");
		} else if (userAgentString.indexOf("Safari") != -1) {

			// Extract Safari version number...
			//
			index = userAgentString.indexOf("Safari");
			int temp = index;
			String patchNumber = userAgentString.substring(index + 7);
			index = userAgentString.indexOf("Version");
			String versionNumber = userAgentString.substring(index + 8, temp);

			// Construct string containing browser name and version
			//
			browserVersion = "Safari " + versionNumber + "(" + patchNumber
					+ ")";
		}

		// }

		return browserVersion;
	}

	/**
	 * This method is sub method of getBrowserVersion .
	 * 
	 * @param selenium
	 *            selenium object
	 * @param index
	 *            index
	 * @param userAgentString
	 *            userAgentString
	 * @return browser version
	 * 
	 * @throws AFTException
	 * 
	 */
	public String getBrowserVersionSub(Selenium selenium, int index,
			String userAgentString) throws AFTException {

		// If Internet Explorer, extract version number
		//
		int indexNo = index;
		indexNo = userAgentString.indexOf("MSIE");
		String versionNumber = userAgentString.substring(indexNo + 5,
				indexNo + 8);

		String xDomainRequest = "null";
		try {
			xDomainRequest = selenium.getEval("XDomainRequest");
			LOGGER.debug("XDomainRequest object found on browser ["
					+ xDomainRequest + "]");
		} catch (Exception e) {
			// Looks like XDomainRequest is not available, let us keep
			// it
			// initialized to NULL
			xDomainRequest = "null";
		}

		String msPerformance = "";
		try {
			msPerformance = selenium.getEval("window.performance");
			LOGGER.debug("window.msPerformance object found on browser ["
					+ msPerformance + "]");
			// if (msPerformance == null)
			if (msPerformance.length() <= 0) {
				msPerformance = "null";
			}
		} catch (Exception e) {
			// Looks like msPerformance is not available, let us keep it
			// initialized to NULL
			// msPerformance = "null";
		}

		if ((xDomainRequest.compareToIgnoreCase("null") != 0)
				&& (msPerformance.compareToIgnoreCase("null") == 0)) {
			LOGGER.debug("Additional check show that the browser version is 8.0");
			versionNumber = "8.0";
		} else if ((xDomainRequest.compareToIgnoreCase("null") != 0)
				&& (msPerformance.compareToIgnoreCase("null") != 0)) {
			LOGGER.debug("Additional check show that the browser version is 9.0");
			versionNumber = "9.0";
		}

		// Construct string containing browser name and version
		//
		browserVersion = "Microsoft Internet Explorer " + versionNumber;

		return browserVersion;
	}

	/**
	 * This method will return the value in string Byte array to string.
	 * 
	 * @param aByteArray
	 *            the a byte array
	 * @return the string
	 */
	public String byteArrayToString(byte[] aByteArray) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();

		for (int i = 0; i < aByteArray.length; i++) {
			if (aByteArray[i] != 0) {
				bs.write(aByteArray[i]);
			}
		}

		return bs.toString();
	}

	/**
	 * Highlight's UI element in the HTML of a page.
	 * 
	 * @param action
	 *            action to perform
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @param parsedElementValue
	 *            parsed action value
	 * @param bHighlightObj
	 *            True indicates to highlight UI element o/w false
	 */
	public void highlight(String action, String objectID, String elementName,
			String parsedElementValue, boolean bHighlightObj)
			throws AFTException {
		JavascriptExecutor js = (JavascriptExecutor) AFTAppiumBase
				.getInstance().getDriver();
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String objectLocator = objectID;
		try {
			// Doesn't highlight/unhighlight object for the actions not
			// specified in the array 'highlightActions'
			if (!Arrays.asList(highlightActions).contains(action)) {
				LOGGER.trace("Invalid action [" + action
						+ "] to highlight UI object");
				return;
			}
			if (bHighlightObj) {
				LOGGER.info("Highlight border, element Name [" + elementName
						+ "], object [" + objectLocator + "], parsed value ["

						+ parsedElementValue + "]");
			} else {
				// Skip Unhighlighting the UI object if there is any alert
				// present on browser o/w unhandledAlertException dismisses the
				// alert.
				// Cause: Exception raises whenever any action (except alert
				// specific actions) is performed on browser having alert
				if (uiCommandFixtures.isAlertPresent()) {
					LOGGER.warn("Cannot Unhighlight the Element ["
							+ objectLocator
							+ "] as an Alert/Confirm dialog is found!");
					return;
				}
				LOGGER.info("UnHighlight border, element Name [" + elementName
						+ "], object [" + objectLocator + "], parsed value ["
						+ parsedElementValue + "]");
			}
			if (repositoryObject != null
					&& repositoryObject.getType().equalsIgnoreCase("WebTable")) {
				LOGGER.trace("Generating object id using the paramters ["
						+ objectLocator + "]");
				objectLocator = WebTableFixture
						.generateTableObjectID(elementName) + "/input";
			}
			if (objectLocator.isEmpty()) {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			} else {
				highlightSub(action, objectLocator, elementName,
						parsedElementValue, bHighlightObj, repositoryObject, js);
			}
		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
		}
	}

	/**
	 * This method is sub method of highlight.
	 * 
	 * @param action
	 *            action
	 * @param objectLocator
	 *            objectLocator
	 * @param elementName
	 *            elementName
	 * @param parsedElementValue
	 *            parsedElementValue
	 * @param bHighlightObj
	 *            bHighlightObj
	 * @param repositoryObject
	 *            repositoryObject
	 * @param js
	 *            javascript executor
	 * @throws AFTException
	 * 
	 */
	public void highlightSub(String action, String objectLocator,
			String elementName, String parsedElementValue,
			boolean bHighlightObj, RepositoryObject repositoryObject,
			JavascriptExecutor js) throws AFTException {

		// wait for the element and highlight/unhighlight
		if (bHighlightObj) {
			LOGGER.trace("Waiting for element [" + objectLocator
					+ "] to be present");
			boolean waitForElement = waitFixtures.waitForElementPresent(
					objectLocator, UIFixtureUtils.getInstance()
							.getElementWaitTime(), elementName);
			if (waitForElement) {
				highlightElement = UIFixtureUtils.getInstance().findElement(
						repositoryObject, objectLocator);

				LOGGER.trace("Element [" + objectLocator + "] is found");
				style = (String) js.executeScript(
						"return arguments[0].style.border", highlightElement);
				js.executeScript(
						"arguments[0].style.border = '2px solid green'",
						highlightElement);
				// toggle switch to un-highlight UI element
				bSetHighlight = true;
			}
		} else {
			// Unhighlight the UI object if it is highlighted
			try {
				if (bSetHighlight) {
					if (style != null && !style.isEmpty()) {
						js.executeScript(
								"arguments[0].style.border = arguments[1]",
								highlightElement, style);
					} else {
						js.executeScript("arguments[0].style.cssText = ''",
								highlightElement);
					}
				}
			} catch (WebDriverException e) {
				LOGGER.warn("Cannot Unhighlight, as the Element ["
						+ objectLocator + "] is not found!");
			} finally {
				bSetHighlight = false;
			}
		}

	}
}
