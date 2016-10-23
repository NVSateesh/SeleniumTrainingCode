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
 * Class: WindowFixtures
 * 
 * Purpose: This class allow user to perform actions on Windows objects like
 * winCheck, winClick, winGet
 */

package com.ags.aft.frankensteinDriver.fixtures;

import org.apache.log4j.Logger;


import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;


/**
 * The Class AFTWindowFixtures.
 */
public class WindowFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WindowFixtures.class);

	/** The wait fixtures. */
	private final WaitFixtures waitFixtures;

	/** The wait for element. */
	private boolean waitForElement;

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public WindowFixtures() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 * activateWindow: Activate the window.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void activateWindow(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [Activatewindow]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.activateWindow(name);
				LOGGER.info("[Activated] window [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Close a dialog.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void closeDialog(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [closeDialog]");

				AFTFrankensteinBase.getInstance().getDriver()
						.dialogClosed(objectID);
				LOGGER.info("[selectMenu] executed on [" + objectID + "]");

			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Activate a dialog.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void activateDialog(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [activateDialog]");

				AFTFrankensteinBase.getInstance().getDriver()
						.activateDialog(objectID);
				LOGGER.info("[activateDialog] executed on [" + objectID + "]");

			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	
	public void setParentContext(String elementName){
		
		LOGGER.trace("Executing command: [setParentContext]");
		AFTFrankensteinBase.getInstance().getDriver().setParentContext(elementName);
	}
	
	/**
	 * set parent.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void setParent(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [activateDialog]");

				AFTFrankensteinBase.getInstance().getDriver().setParent(objectID);
				LOGGER.info("[activateDialog] executed on [" + objectID + "]");

			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	/**
	 * set default parent.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void setDefaultParent(){
		LOGGER.trace("Executing command: [setDefaultParent]");
		AFTFrankensteinBase.getInstance().getDriver().setParent(null);
	}
	
	/**
	 * select dialog.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectDialog(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [selectDialog]");

				AFTFrankensteinBase.getInstance().getDriver()
						.dialogShown(objectID);
				LOGGER.info("[selectMenu] executed on [" + objectID + "]");

			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}


	/**
	 * Count Command: Retrieves the name of Tab
	 * 
	 * 
	 * @param objectID
	 *            = Object ID in the properties file
	 * @param elementName
	 *            the element name
	 * @return String : Text present in the dialog
	 * @throws AFTException
	 *             the application exception
	 */
	public String getDialogtext(String objectID, String elementName)
			throws AFTException {
		String dialogtext = "";

		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [getTabName]");
				dialogtext = AFTFrankensteinBase.getInstance().getDriver()
						.getDialogMessage(objectID);
				LOGGER.info("Value retrieved [" + dialogtext + "] from ["
						+ objectID + "]");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return dialogtext;
	}
	
	/**
	 * MaximizeWindow: Maximize the window.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void maximizeWindow(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [MaximizeWindow]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.windowMaximize(name);
				LOGGER.info("[Maximiz] window [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	public String getActiveWindowTitle()
	{
		return AFTFrankensteinBase.getInstance().getDriver()
		.getWindowTitle();
	}
	public String getLastWindowTitle()
	{
		return AFTFrankensteinBase.getInstance().getDriver()
		.getLastWindowTitle();
	}
	/**
	 * MinimizeWindow : Minimize the window.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void minimizeWindow(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [MinimizeWindow]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.windowMinimize(name);
				LOGGER.info("[Maximiz] window [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	
	
	/**
	 * Gets the Activate window Title.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public String getchildWindowTitle(String objectID, String elementName)
			throws AFTException {
		String wintitle;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String name = repositoryObject.getName();
		if (name.isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Waiting for element [" + objectID
						+ "] to be present");
					LOGGER.trace("Executing command [getXpathCount]...");
					wintitle = AFTFrankensteinBase.getInstance().getDriver().getWindowTitle();
					

					LOGGER.info("Total number of rows in the table ["
							+ objectID + "] is [" + wintitle + "]");
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return wintitle;
	}
	/**
	 * CloseWindow : Close the window.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	
	
	public void closeWindow(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID
					+ "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [CloseWindow]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver().windowClose(name);
				LOGGER.info("[Maximiz] window on [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}	

}
