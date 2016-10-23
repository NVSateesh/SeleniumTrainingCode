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
 * Class: UICommandFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the UI objects like click,type, select, remove
 */

package com.ags.aft.frankensteinDriver.fixtures;

import java.util.List;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.thoughtworks.frankenstein.drivers.FrankensteinDriver;

/**
 * The Class UICommandFixtures.
 * 
 */
public class UICommandFixtures {

	/** Default wait time **/
	private int WAITTIME_MS = 400;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(UICommandFixtures.class);

	/** The wait fixtures. */
	private final WaitFixtures waitFixtures;

	/** The wait for element. */
	private boolean waitForElement;

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public UICommandFixtures() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 * Click Command: Clicks on a link, button, checkbox or radio button.
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void click(String objectID, String elementName) throws AFTException {
		try {

			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [click]");
				String name = repositoryObject.getName();
				if (objType.equalsIgnoreCase("button")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.clickButton(name);
				} else if (objType.equalsIgnoreCase("Radiobutton")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.clickRadioButton(name, true);
				}
				LOGGER.info("[click] executed on [" + objectID + "]");
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
	 * mouseClick Command: Just fires click event of object
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void mouseClick(String objectID, String elementName)
			throws AFTException {
		try {

			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [click]");
				String name = repositoryObject.getName();
				if (objType.equalsIgnoreCase("button")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.mouseClick(name);
				} else if (objType.equalsIgnoreCase("Radiobutton")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.clickRadioButton(name, true);
				}
				LOGGER.info("[Mouse Click] executed on [" + objectID + "]");
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
	 * mouseUp Command: Just fires mouse up event on the object
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void mouseUp(String objectID, String elementName)
			throws AFTException {
		try {

			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [click]");
				String name = repositoryObject.getName();
				if (objType.equalsIgnoreCase("button")) {
					AFTFrankensteinBase.getInstance().getDriver().mouseUp(name);
				} else if (objType.equalsIgnoreCase("Radiobutton")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.clickRadioButton(name, true);
				}
				LOGGER.info("[Mouse Click] executed on [" + objectID + "]");
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
	 * mouseDown Command: Just fires mouse down event of object
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void mouseDown(String objectID, String elementName)
			throws AFTException {
		try {

			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [click]");
				String name = repositoryObject.getName();
				if (objType.equalsIgnoreCase("button")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.mouseDown(name);
				} else if (objType.equalsIgnoreCase("Radiobutton")) {
					AFTFrankensteinBase.getInstance().getDriver()
							.clickRadioButton(name, true);
				}
				LOGGER.info("[Mouse Click] executed on [" + objectID + "]");
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
	 * Custom Click Command: Clicks on a button to specified element
	 * 
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void customClick(String objectID, String elementName)
			throws AFTException {
		try {

			LOGGER.trace("Waiting for element [" + elementName
					+ "] to be present");
			Thread.sleep(WAITTIME_MS);
			LOGGER.trace("Element [" + elementName + "] is found");
			LOGGER.trace("Executing command: [customclick]");
			AFTFrankensteinBase.getInstance().getDriver()
					.clickButton(elementName);
			LOGGER.info("[click] executed on [" + elementName + "]");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Checks if is text present on page.
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
	public void check(String objectID, String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [check]");
				String name = repositoryObject.getName();
				FrankensteinDriver driver = AFTFrankensteinBase.getInstance()
						.getDriver();
				driver.clickCheckbox(name, true);

				LOGGER.info("[check] executed on [" + objectID + "]");
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
	 * Unchecks the Check box.
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
	public void uncheck(String objectID, String elementName)
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
				LOGGER.trace("Executing command: [uncheck]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.clickCheckbox(name, false);
				LOGGER.info("[uncheck] executed on [" + objectID + "]");
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
	 * select item from the Check box.
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
	public void selectListOptions(String objectID, String parsedElementValue,
			String elementName, String type) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [selectListOptions] for listbox");
				String typelement = repositoryObject.getType();
				if (typelement.equalsIgnoreCase("listbox")) {
					String name = repositoryObject.getName();
					String[] parsedElementValueNav = parsedElementValue
							.split(",");
					AFTFrankensteinBase.getInstance().getDriver()
							.selectList(name, parsedElementValueNav,type);

					LOGGER.info("[selectlist] executed on [" + objectID + "]");
				} else if (typelement.equalsIgnoreCase("combobox")) {
					LOGGER.trace("Element [" + objectID + "] is found");
					LOGGER.trace("Executing command: [selectListOptions] for dropdownbox");
					AFTFrankensteinBase.getInstance().getDriver()
							.selectDropDown(objectID, parsedElementValue, type);

				}
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
	 * Verifying the specified values are avalable in the listbox
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
	public boolean verifyListOptions(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		String searchText="";
		try {
			if(objectID.isEmpty()){
				objectID=elementName;
			}
			String[] parsedElementValueArr = objectID.split(",");
			if(parsedElementValueArr[1].contains("#")){
				searchText=Helper.getInstance().getActionValue(parsedElementValueArr[1]);
			}else{
				searchText=parsedElementValueArr[1];
			}
			
			
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(parsedElementValueArr[0]);
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [verifyListOptions] for listbox");
				String name = repositoryObject.getName();
				String[] parsedElementValueNav = searchText.split(",");
				String typelement = repositoryObject.getType();
				if (typelement.equalsIgnoreCase("listbox")) {
					return AFTFrankensteinBase.getInstance().getDriver().verifyListOptions(name, parsedElementValueNav);
				} else if( typelement.equalsIgnoreCase("combobox")) {
					LOGGER.trace("Element [" + objectID + "] is found");
					LOGGER.trace("Executing command: [selectListOptions] for dropdownbox");
					return AFTFrankensteinBase.getInstance().getDriver().verifyComboOptions(name, parsedElementValueNav);
				}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return false;
	}
	
	/**
	 * Get the selected item from the combobox
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return String, the selected item
	 * @throws AFTException
	 *             the application exception
	 */
	public String getSelectedValue(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		String selectedItem = "";
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [getSelectedValue] for listbox");
				String typelement = repositoryObject.getType();
				if (typelement.equalsIgnoreCase("combobox")) {
					LOGGER.trace("Element [" + objectID + "] is found");
					LOGGER.trace("Executing command: [getSelectedValue] for dropdownbox");
					selectedItem=AFTFrankensteinBase.getInstance().getDriver()
							.getSelectedValue(objectID,typelement);
				}
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return selectedItem;
	}

	/**
	 * select tab from the Check box.
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
	public void selectTab(String objectID, String elementName,
			String parsedElementValue) throws AFTException {
		try {

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				if (repositoryObject != null) {
					String name = repositoryObject.getName();
					AFTFrankensteinBase.getInstance().getDriver()
							.switchTab(name, parsedElementValue);
					LOGGER.info("[selectTab] executed on [" + objectID + "]");
				} else {
					LOGGER.trace("Element [" + objectID + "] is null");
					throw new AFTException("object   [" + objectID
							+ "] not found");
				}

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
	 * select item from Menu.
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
	public void selectMenu(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [selectmenu]");

				AFTFrankensteinBase.getInstance().getDriver()
						.navigate(objectID);
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
	 * Verify the element is enable mode status
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
	public boolean isEnabled(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		boolean flag=false;
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [isEnabled]");
				if(!parsedElementValue.isEmpty()){
					flag=waitFixtures.waitForElementEnabled(objectID, 0, parsedElementValue);
				}else{
					flag=waitFixtures.waitForElementEnabled(objectID, 0, elementName);
				}
				LOGGER.info("[isEnabled] executed on [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
		return flag;
	}

	/**
	 * Count Command: Retrieves the count of items from a drop down list and
	 * list box.
	 * 
	 * @param objectID
	 *            = Object ID in the properties file
	 * @param elementName
	 *            the element name
	 * @return Integer value - count of the items in the specified object
	 * @throws AFTException
	 *             the application exception
	 */
	public int getOptionCount(String objectID, String elementName)
			throws AFTException {
		int itemCount = 0;
		if (objectID.isEmpty()) {
			LOGGER.error("Element [" + elementName
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + elementName
					+ "] not found in Object Repository");
		} else {
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
					LOGGER.trace("Executing command: [getOptionCount]");

					String name = repositoryObject.getName();
					String type=repositoryObject.getType();

					itemCount = AFTFrankensteinBase.getInstance().getDriver()
							.getOptionCount(name,type);
					LOGGER.info("Value retrieved [" + itemCount + "] from ["
							+ objectID + "]");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return itemCount;
	}

	/**
	 * Count Command: Retrieves the name of Tab
	 * 
	 * 
	 * @param objectID
	 *            = Object ID in the properties file
	 * @param elementName
	 *            the element name
	 * @return Integer value - count of the items in the specified object
	 * @throws AFTException
	 *             the application exception
	 */
	public String getTabName(String objectID, String elementName)
			throws AFTException {
		String tabName = "";
		if (objectID.isEmpty()) {
			LOGGER.error("Element [" + elementName
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + elementName
					+ "] not found in Object Repository");
		} else {
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
					LOGGER.trace("Executing command: [getTabName]");

					String name = repositoryObject.getName();

					tabName = AFTFrankensteinBase.getInstance().getDriver()
							.getSelectedTab(name);
					LOGGER.info("Value retrieved [" + tabName + "] from ["
							+ objectID + "]");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return tabName;
	}

	/**
	 * Count Command: Retrieves the name of label
	 * 
	 * 
	 * @param objectID
	 *            = Object ID in the properties file
	 * @param elementName
	 *            the element name
	 * @return Integer value - count of the items in the specified object
	 * @throws AFTException
	 *             the application exception
	 */
	public String getLabel(String objectID, String elementName)
			throws AFTException {
		String lablebName = "";
		if (objectID.isEmpty()) {
			LOGGER.error("Element [" + elementName
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + elementName
					+ "] not found in Object Repository");
		} else {
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
					LOGGER.trace("Executing command: [getlabel]");

					String name = repositoryObject.getName();

					lablebName = AFTFrankensteinBase.getInstance().getDriver()
							.getLabel(name);
					LOGGER.info("Value retrieved [" + lablebName + "] from ["
							+ objectID + "]");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return lablebName;
	}

	/**
	 * enter text in text box.
	 * 
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the value
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */

	public void type(String objectID, String parsedElementValue,
			String elementName, String optional) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [type]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.clickTextbox(name);
				Thread.sleep(WAITTIME_MS);
				AFTFrankensteinBase.getInstance().getDriver()
						.enterText(name, parsedElementValue);
				Thread.sleep(WAITTIME_MS);
				AFTFrankensteinBase.getInstance().getDriver()
						.clickTextbox(name);
				LOGGER.info("[type] executed on [" + objectID + "]");
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
	 * appendText text in text box.
	 * 
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the value
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */

	public void appendText(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [type]");
				String name = repositoryObject.getName();
				String recivetext = AFTFrankensteinBase.getInstance()
						.getDriver().getText(name);
				String appText = recivetext + parsedElementValue;
				AFTFrankensteinBase.getInstance().getDriver()
						.enterText(name, appText);
				LOGGER.info("[type] executed on [" + objectID + "]");
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
	 * 
	 * 
	 * 
	 * generates a new objectId and adds to object repository dynamically
	 * 
	 * 
	 * 
	 * @param testStepRunner
	 * 
	 *            test step runner object
	 * 
	 * @param elementName
	 * 
	 *            Object identifier in object repository
	 * 
	 * @param value
	 * 
	 *            list of values to form the new object repository
	 * 
	 * @throws AFTException
	 */

	public void generateDynamicObjectId(TestStepRunner testStepRunner,
			String elementName, String value, String actualValue)
			throws AFTException {
		// Parse the element value
		String newObjectID = "";
		try {
			List<String> paramArray = Helper.getInstance()
					.parseActionParameterList(
							testStepRunner.getTestSuiteRunner(), actualValue,
							true);
			for (String newObjParam : paramArray) {
				newObjectID += newObjParam;
			}
			// Check if the objectID already exists in object repository
			LOGGER.trace("Checking if the elementname [" + elementName
					+ "] specified exists in object repository");
			if (ObjectRepositoryManager.getInstance().getObjectID(elementName) != null) {
				// Check if the objectID already exists in object repository
				LOGGER.debug("Found the object" + "[" + elementName
						+ "] in the Object Repository");
				LOGGER.debug("Setting new ObjectID [" + newObjectID + "] for ["
						+ elementName + "]");
				// Append original OR value to new OR value
				ObjectRepositoryManager.getInstance().setObjectID(elementName,
						newObjectID);
			} else {
				LOGGER.debug("Element" + "[" + elementName
						+ "] not found in Object Repository");
				LOGGER.debug("Creating new object"
						+ "["
						+ elementName
						+ "] and adding it to Object Repository with ObjectId as ["
						+ newObjectID + "]");
				// add a new OR value
				ObjectRepositoryManager.getInstance().setObjectID(elementName,
						newObjectID);
				// Check if the Object is added successfully to Object
				// Repository
				if (ObjectRepositoryManager.getInstance()
						.getObjectID(elementName)
						.compareToIgnoreCase(newObjectID) == 0) {
					LOGGER.info("Successfully added new objectId ["
							+ newObjectID + "] for element [" + elementName
							+ "] to object repository");
				} else {
					throw new AFTException("Failed to add the new element "
							+ "[" + elementName + "] to object repository");
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Gets the text.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            the element name
	 * @return the text
	 * @throws AFTException
	 *             the application exception
	 */
	public String getText(String objectID, String elementName)
			throws AFTException {
		String innerText = "";
		try {

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [getText]");
				String name = repositoryObject.getName();
				String objType = "";
				if (repositoryObject != null) {
					objType = repositoryObject.getType();
				}
				if (objType.equalsIgnoreCase("Textbox")
						|| objType.equalsIgnoreCase("TextField")) {
					innerText = AFTFrankensteinBase.getInstance().getDriver()
							.getText(name);
				} else if (objType.equalsIgnoreCase("Button")) {
					innerText = AFTFrankensteinBase.getInstance().getDriver()
							.getButtonText(name);
				}else if (objType.equalsIgnoreCase("Dialog")) {
					innerText = AFTFrankensteinBase.getInstance().getDriver()
							.getDialogMessage(name);
				}

				if (innerText == null) {
					innerText = "EMPTY";
				}
				LOGGER.info("Got the text [" + innerText + "] from ["
						+ objectID + "]");
			} else {
				LOGGER.error("Element [" + objectID + "] not found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);

		}
		return innerText;
	}
	
	
	/**
	 * Gets the tool tip text.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            the element name
	 * @return the text
	 * @throws AFTException
	 *             the application exception
	 */
	public String getTooltip(String objectID, String elementName)
			throws AFTException {
		String tooltipText = "";
		try {

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [getTooltip]");
				String name = repositoryObject.getName();
				String objType = "";
				if (repositoryObject != null) {
					objType = repositoryObject.getType();
				}
				if (objType.equalsIgnoreCase("Textbox")
						|| objType.equalsIgnoreCase("TextField")) {
					tooltipText = AFTFrankensteinBase.getInstance().getDriver()
							.getTextTooltip(name);
				} else if (objType.equalsIgnoreCase("Button")) {
					tooltipText = AFTFrankensteinBase.getInstance().getDriver()
							.getButtonText(name);
				}

				if (tooltipText == null) {
					tooltipText = "EMPTY";
				}
				LOGGER.info("Got the tool tip text [" + tooltipText + "] from ["
						+ objectID + "]");
			} else {
				LOGGER.error("Element [" + objectID + "] not found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);

		}
		return tooltipText;
	}

	public void clickLabel(String objectID) {
		// TODO Auto-generated method stub
		AFTFrankensteinBase.getInstance().getDriver().clickLabel(objectID);
	}

}