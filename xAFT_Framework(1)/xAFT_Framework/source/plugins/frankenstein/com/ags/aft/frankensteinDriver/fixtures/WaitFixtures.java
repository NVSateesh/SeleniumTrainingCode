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

package com.ags.aft.frankensteinDriver.fixtures;

import java.util.Calendar;
import org.apache.log4j.Logger;
import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.runners.TestSuiteRunner;
import com.ags.aft.util.Helper;


/**
 * The Class aftWaitFixtures.
 */
public class WaitFixtures {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WaitFixtures.class);

	/** Default wait time **/
	private int SLEEPTIME_MS = 1000;

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
	 * Wait for element present.
	 * 
	 * @param Element
	 *            the element
	 * @param waitTime
	 *            the wait time
	 * @param elementName
	 *            the elementName
	 * @return true/false if element is present or not
	 * @throws AFTException
	 */
	public boolean waitForElementPresent(String objectID, int waitTime,
			String elementName) throws AFTException {
		LOGGER.debug("Executing command [waitForElementPresent] with timeout ["
				+ waitTime + "], Element [" + objectID + "]");
		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;
		
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String typelement = repositoryObject.getType();
		do {
			LOGGER.trace("Current attempt #" + iterationCnt
					+ " to find element [" + objectID + "]");
			try {

				if (typelement.equalsIgnoreCase("button")
						|| typelement.equalsIgnoreCase("link")) {
					if (elementName.contains("<")) {
						if (AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(elementName)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					} else {
						if (AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(objectID)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					}
				} else if (typelement.equalsIgnoreCase("listbox")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isListBoxVisble(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("label")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isLabelPresent(objectID)) {
						LOGGER.debug("Found element [" + objectID
								+ "] after [" + iterationCnt
								+ "] iterations");
						return true;
					}
					
				} else if (typelement.equalsIgnoreCase("combobox")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							
							.isDropDownVisible(objectID)) {
						
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				}else if (typelement.equalsIgnoreCase("JGraph")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							
							.isGraphPresent(objectID)) {
						
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("window")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isWindowPresent(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Dialog")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isDialogVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Frame")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isFrameVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("checkbox")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isCheckboxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Radiobutton")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isRadioButtonVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("tree")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTreeExists(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("textbox")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("menu")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isMenuItemVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("Tab")) {
					String name = repositoryObject.getName();
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTabVisible(objectID, name)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("table")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTableVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("jgraph")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isGraphPresent(objectID)) {
						LOGGER.debug("Found element [" + elementName + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				}else{
					throw new AFTException(typelement+" action not found!!");
				}
			} catch (Exception e) {
				LOGGER.trace("Element [" + objectID + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + objectID + "] not found after waiting for ["
				+ waitTime + "ms]!");
		throw new AFTException("Element [" + objectID
				+ "] not found/displayed after waiting for [" + waitTime
				+ "ms]! ");
	}

	/**
	 * Wait till element is not present. ********************** obnly implemented for lable , need to implement for others
	 * 
	 * @param Element
	 *            the element
	 * @param waitTime
	 *            the wait time
	 * @param elementName
	 *            the elementName
	 * @return true/false if element is present or not
	 * @throws AFTException
	 */
	public boolean waitForElementNotPresent(String objectID, int waitTime,
			String elementName) throws AFTException {
		LOGGER.debug("Executing command [waitForElementPresent] with timeout ["
				+ waitTime + "], Element [" + objectID + "]");
		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String typelement = repositoryObject.getType();
		do {
			LOGGER.trace("Current attempt #" + iterationCnt
					+ " to find element [" + objectID + "]");
			try {

				if (typelement.equalsIgnoreCase("button") || typelement.equalsIgnoreCase("link")) {
					if (elementName.contains("<")) {
						if (!AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(elementName)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					} else {
						if (!AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(objectID)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					}
				} else if (typelement.equalsIgnoreCase("listbox")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isListBoxVisble(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("label")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isLabelPresent(objectID)) {
						LOGGER.debug("Found element [" + objectID
								+ "] after [" + iterationCnt
								+ "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("combobox")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isDropDownVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("window")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isWindowPresent(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Dialog")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isDialogVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				}  else if (typelement.equalsIgnoreCase("Frame")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isFrameVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("checkbox")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isCheckboxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Radiobutton")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isRadioButtonVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("tree")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTreeVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("textbox")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("menu")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isMenuItemVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("Tab")) {
					String name = repositoryObject.getName();
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTabVisible(objectID, name)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("table")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTableVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				}
			} catch (Exception e) {
				LOGGER.trace("Element [" + objectID + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + objectID + "] found even after waiting for ["
				+ waitTime + "ms]!");
		throw new AFTException("Element [" + objectID
				+ "] found/displayed even after waiting for [" + waitTime
				+ "ms]! ");
	}

	
	/**
	 * Wait till element is not present. ********************only for lable, need to implement for others
	 * 
	 * @param Element
	 *            the element
	 * @param waitTime
	 *            the wait time
	 * @param elementName
	 *            the elementName
	 * @return true/false if element is present or not
	 * @throws AFTException
	 */
	public boolean waitTillElementIsNotVisible(String objectID, int waitTime,
			String elementName) throws AFTException {
		LOGGER.debug("Executing command [waitForElementPresent] with timeout ["
				+ waitTime + "], Element [" + objectID + "]");
		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String typelement = repositoryObject.getType();
		do {
			LOGGER.trace("Current attempt #" + iterationCnt
					+ " to find element [" + objectID + "]");
			try {

				if (typelement.equalsIgnoreCase("button") || typelement.equalsIgnoreCase("link")) {
					if (elementName.contains("<")) {
						if (!AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(elementName)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					} else {
						if (!AFTFrankensteinBase.getInstance().getDriver()
								.isbuttonVisble(objectID)) {
							LOGGER.debug("Found element [" + objectID
									+ "] after [" + iterationCnt
									+ "] iterations");
							return true;
						}
					}
				} else if (typelement.equalsIgnoreCase("listbox")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isListBoxVisble(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("label")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isLabelVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID
								+ "] after [" + iterationCnt
								+ "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("combobox")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isDropDownVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("window")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isWindowPresent(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Dialog")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isDialogVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				}  else if (typelement.equalsIgnoreCase("Frame")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isFrameVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("checkbox")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isCheckboxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Radiobutton")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isRadioButtonVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("tree")) {

					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTreeVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("textbox")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("menu")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isMenuItemVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("Tab")) {
					String name = repositoryObject.getName();
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTabVisible(objectID, name)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("table")) {
					if (!AFTFrankensteinBase.getInstance().getDriver()
							.isTableVisible(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				}
			} catch (Exception e) {
				LOGGER.trace("Element [" + objectID + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + objectID + "] found even after waiting for ["
				+ waitTime + "ms]!");
		throw new AFTException("Element [" + objectID
				+ "] found/displayed even after waiting for [" + waitTime
				+ "ms]! ");
	}
	/**
	 * Check for the Text pattern till the specified timeout period
	 * 
	 * @param pattern
	 *            the pattern to wait for
	 * @param waitTime
	 *            the wait time parameter (msec)
	 * @return String pattern waiting for
	 * @throws ApplicationException
	 */
	public String waitForText(TestSuiteRunner testSuiteRunner, String pattern,
			int waitTime, String objectID) throws AFTException {
		int waitTimems = waitTime / SLEEPTIME_MS;
		LOGGER.trace("Executing command [waitForTextPresent] with timeout ["
				+ waitTime + "], text pattern [" + pattern + "]");
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(objectID);
		String typelement = repositoryObject.getType();
		String varpattern = Helper.getInstance().getActionValue(
				testSuiteRunner, pattern);
		try {
			for (int i = 0; i < waitTimems; i++) {
				LOGGER.info("Current attempt #" + i + " to find text pattern ["
						+ varpattern + "]");
				if (typelement.equalsIgnoreCase("button")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.getText(objectID).contains(varpattern)) {
						LOGGER.info("Found text pattern [" + varpattern
								+ "] after [" + i + "] iterations");
						return varpattern;
					}
					Thread.sleep(SLEEPTIME_MS);
				} else if (typelement.equalsIgnoreCase("listbox")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.getSelectedValue(objectID,typelement).contains(varpattern)) {
						LOGGER.info("Found text pattern [" + varpattern
								+ "] after [" + i + "] iterations");
						return varpattern;
					}
				}
				Thread.sleep(SLEEPTIME_MS);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		LOGGER.error("text pattern [" + varpattern
				+ "] not found after waiting for [" + waitTime + "ms]!");
		throw new AFTException("text pattern [" + varpattern
				+ "] not found after waiting for [" + waitTime + "ms]!");

	}

	
	/**
	 * Wait for element enabled.
	 * 
	 * @param Element
	 *            the element
	 * @param waitTime
	 *            the wait time
	 * @param elementName
	 *            the elementName
	 * @return true/false if element is present or not
	 * @throws AFTException
	 */
	public boolean waitForElementEnabled(String objectID, int waitTime,
			String elementName) throws AFTException {
		LOGGER.debug("Executing command [waitForElementenabled] with timeout ["
				+ waitTime + "], Element [" + objectID + "]");
		long startTime = Calendar.getInstance().getTimeInMillis();
		long timeDiff = 0;
		int iterationCnt = 1;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String typelement = repositoryObject.getType();

		do {
			LOGGER.trace("Current attempt #" + iterationCnt
					+ " to find element [" + objectID + "]");
			try {

				if (typelement.equalsIgnoreCase("button")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isbuttonEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("listbox")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isDropDownEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("window")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isWindowEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("Dialog")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isDialogEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("checkbox")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isCheckboxEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				}  else if (typelement.equalsIgnoreCase("menu")) {
					if (AFTFrankensteinBase.getInstance().getDriver()
							.isMenuItemEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}
				} else if (typelement.equalsIgnoreCase("Radiobutton")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isRadioButtonEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("tree")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTreeEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("text")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("textbox")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				} else if (typelement.equalsIgnoreCase("table")) {

					if (AFTFrankensteinBase.getInstance().getDriver()
							.isTableEnabled(objectID)) {
						LOGGER.debug("Found element [" + objectID + "] after ["
								+ iterationCnt + "] iterations");
						return true;
					}

				}

			} catch (Exception e) {
				LOGGER.trace("Element [" + objectID + "] not found");
			} finally {
				long curTime = Calendar.getInstance().getTimeInMillis();
				timeDiff = curTime - startTime;
			}
		} while (timeDiff <= waitTime);

		LOGGER.error("Element [" + objectID + "] disabled after waiting for ["
				+ waitTime + "ms]!");
		throw new AFTException("Element [" + objectID
				+ "] not enabled after waiting for [" + waitTime + "ms]!");
	}

}
