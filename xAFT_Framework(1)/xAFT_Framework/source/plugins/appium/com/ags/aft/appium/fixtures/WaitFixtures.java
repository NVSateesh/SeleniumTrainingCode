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
 * Class: WaitFixtures
 * 
 * Purpose: This class implements methods that allows users to perform wait
 * actions like wait, waitForAlert, waitforAttribute
 */

package com.ags.aft.appium.fixtures;

import java.util.Calendar;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.appium.common.UIFixtureUtils;

/**
 * The Class aftWaitFixtures.
 */
public class WaitFixtures {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WaitFixtures.class);

	/**
	 * constructor
	 */
	public WaitFixtures() {
		super();
	}

	/**
	 * To pause the test execution.
	 * 
	 * @param time
	 *            : time to wait till next command
	 * @throws AFTException
	 * 
	 */
	public void wait(String time) throws AFTException {

		try {
			LOGGER.trace("Executing command [wait] with time [" + time + "]");

			Thread.sleep(Integer.parseInt(time));
			LOGGER.info("Executed command [wait] with time [" + time + "]");

		} catch (RuntimeException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Wait for element present. Currently waitForElementPresent is supported
	 * for Button and TextBox
	 * 
	 * @param element
	 *            the element
	 * @param waitTime
	 *            the wait time
	 * @param elementName
	 *            the elementName
	 * @return true/false if element is present or not
	 * @throws AFTException
	 */
	public boolean waitForElementPresent(String element, int waitTime,
			String elementName) throws AFTException {

		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);

		LOGGER.debug("Executing command [waitForElementPresent] with timeout ["
				+ waitTime + "], Element [" + element + "]");

		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;

		repositoryObject = ObjectRepositoryManager.getInstance().getObject(
				elementName);

		do {
			LOGGER.trace("Current attempt #" + iterationCnt
					+ " to find element [" + element + "]");
			try {
				if (UIFixtureUtils.getInstance().findElement(repositoryObject,
						element) != null
						&& UIFixtureUtils.getInstance()
								.findElement(repositoryObject, element)
								.isDisplayed()) {
					LOGGER.debug("Found element [" + element + "] after ["
							+ iterationCnt + "] iterations");
					return true;
				}

			} catch (Exception e) {
				LOGGER.trace("Element [" + element + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + element + "] not found after waiting for ["
				+ waitTime + "ms]!");
		throw new AFTException("Element [" + element
				+ "] not found/displayed after waiting for [" + waitTime
				+ "ms]!");
	}

	/**
	 * Check for the Text pattern till the specified timeout period
	 * 
	 * @param expectedText
	 *            the text to wait for
	 * @return String pattern waiting for
	 * @throws ApplicationException
	 */
	public String waitForText(String ObjectID, String expectedText)
			throws AFTException {
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(ObjectID);
		LOGGER.trace("Executing command [waitForText] with text ["
				+ expectedText + "]");
		try {
			if (UIFixtureUtils.getInstance().findElement(repositoryObject,
					ObjectID) != null
					&& UIFixtureUtils.getInstance()
							.findElement(repositoryObject, ObjectID)
							.isDisplayed()) {
				// if (UIFixtureUtils.getInstance()
				// .findElement(repositoryObject, ObjectID)
				// .getAttribute("value").equalsIgnoreCase(expectedText))
				// ;
				LOGGER.info("Found text pattern [" + expectedText + "] ");
				return expectedText;
			} else {
				LOGGER.error("text [" + expectedText + "] not found!");
				throw new AFTException("text [" + expectedText
						+ "] not found !");
			}

		} catch (WebDriverException we) {
			LOGGER.error("Exception::", we);
			throw new AFTException(we);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Check for the Text pattern till the specified timeout period
	 * 
	 * @param expectedText
	 *            the text to wait for
	 * @return String pattern waiting for
	 * @throws ApplicationException
	 */
	public boolean waitForElementToVanish(String element, int waitTime,
			String elementName) throws AFTException {

		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);

		LOGGER.debug("Executing command [waitForElementToVanish] with timeout ["
				+ waitTime + "], Element [" + element + "]");

		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;

		repositoryObject = ObjectRepositoryManager.getInstance().getObject(
				elementName);
		do {
			LOGGER.debug("Current attempt #" + iterationCnt
					+ " to check if the element is vanished [" + element + "]");
			iterationCnt++;
			try {
				if (!UIFixtureUtils.getInstance()
						.findElement(repositoryObject, element).isDisplayed()) {
					LOGGER.debug("Element vanished [" + element + "] after ["
							+ iterationCnt + "] iterations");
					return true;
				}
				Thread.sleep(300);
			} catch (Exception e) {
				LOGGER.trace("Element [" + element + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + element
				+ "] did not vanish after waiting for [" + waitTime + "ms]!");
		throw new AFTException("Element [" + element
				+ "] did not vanish after [" + waitTime + "ms]!");
	}

}
