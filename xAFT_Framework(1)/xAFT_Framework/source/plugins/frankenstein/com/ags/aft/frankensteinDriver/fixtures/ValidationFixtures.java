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

package com.ags.aft.frankensteinDriver.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;

/**
 * The Class aftValidationFixtures.
 */
public class ValidationFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ValidationFixtures.class);

	/** Default wait time for an object **/
	private int WAITTIME_MS = 500;

	public static final String ENABLED = "enabled";
	public static final String VISIBLE = "visible";

	/**
	 * Instantiates a new aft validation fixtures.
	 * 
	 */
	public ValidationFixtures() {
		super();
	}

	/**
	 * Verifies the innertext of the text box with the text pattern to be
	 * validated
	 * 
	 * @param objectID
	 *            The element to retrieve the inner text OR path of .pdf file
	 * @param elementName
	 *            element name of the control as provided by user OR path of the
	 *            .pdf file
	 * @param textPattern
	 *            The text pattern to validate
	 * 
	 * @return boolean: true/false
	 * 
	 * @throws AFTException
	 */
	public boolean verifyText(String objectID, String elementName,
			String textPattern) throws AFTException {
		String innerText = "";
		boolean bTextMatch = false;
		try {

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (objectID.isEmpty()) {
				LOGGER.error("Element [" + objectID
						+ "] not found in Object Repository");
				throw new AFTException("Object [" + objectID
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Waiting for element [" + objectID
						+ "] to be present");
				String name = repositoryObject.getName();

				if (!name.isEmpty()) {
					LOGGER.trace("Executing command: [getText]");
					innerText = AFTFrankensteinBase.getInstance().getDriver()
							.getText(name);
					if (innerText.equalsIgnoreCase(textPattern)) {
						bTextMatch = true;
						LOGGER.info("Verify: Success, value [" + textPattern
								+ "] is  match to the text");
					} else {
						LOGGER.error("Verify: Failed, Value of Actuval is  ["
								+ innerText + "], expected value is ["
								+ textPattern + "]");
						bTextMatch = false;
					}
					if (!bTextMatch) {
						throw new AFTException(
								"Verify: Failed, The expected value ["
										+ innerText
										+ "] for object ["
										+ objectID
										+ "] does not match with actual value ["
										+ textPattern + "]");
					}

				} else {
					LOGGER.error("Element [" + objectID + "] not found");
					throw new AFTException("Element [" + objectID
							+ "] not found");
				}

			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);

		}
		return bTextMatch;
	}

	/**
	 * check checkbox state with in a cell of a table.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean verifyTableCheckboxState(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		boolean checkboxsate = false;
		boolean expectedCheckBoxState;
		try {
			if (elementName.isEmpty()) {
				String[] parsedElementValueArr = parsedElementValue.split(",");
				RepositoryObject repositoryObject = ObjectRepositoryManager
						.getInstance().getObject(parsedElementValueArr[0]);
				String tableName = repositoryObject.getName();
				int row = Integer.parseInt(parsedElementValueArr[1]);
				int column = Integer.parseInt(parsedElementValueArr[2]);
				String checkboxname = "";
				expectedCheckBoxState = Boolean
						.parseBoolean(parsedElementValueArr[3]);
				if (parsedElementValueArr.length == 5) {
					checkboxname = parsedElementValueArr[4];
				}
				Thread.sleep(WAITTIME_MS);
				AFTFrankensteinBase.getInstance().getDriver()
						.doubleClickTableRow(tableName, row);
				Thread.sleep(WAITTIME_MS);
				AFTFrankensteinBase.getInstance().getDriver()
						.editTableCell(tableName, row, column);
				Thread.sleep(WAITTIME_MS);
				checkboxsate = AFTFrankensteinBase
						.getInstance()
						.getDriver()
						.isTableCheckboxChecked(tableName, row, column,
								checkboxname);
				AFTFrankensteinBase.getInstance().getDriver()
						.stopTableEdit(tableName);
				LOGGER.info("Chekbox" + checkboxname + " in the table ["
						+ objectID + "] is checked/unchecked");
			} else {
				LOGGER.error("Table [" + objectID + "] not found");
				throw new AFTException("Table [" + objectID + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		if (expectedCheckBoxState == checkboxsate) {
			LOGGER.info("Verify table checkbox state in Table [" + objectID
					+ "] PASSED");
		} else {
			String logMessage = "Verify table checkbox state in Table ["
					+ objectID + "] FAILED; Expected:" + expectedCheckBoxState
					+ " Actual:" + checkboxsate;
			LOGGER.error(logMessage);
			throw new AFTException(logMessage);
		}
		return checkboxsate;
	}

	/**
	 * Verifies the innertext of the combo box with the text pattern to be
	 * validated
	 * 
	 * @param objectID
	 *            The element to retrieve the inner text OR path of .pdf file
	 * @param elementName
	 *            element name of the control as provided by user OR path of the
	 *            .pdf file
	 * @param textPattern
	 *            The text pattern to validate
	 * 
	 * @return boolean: true/false
	 * 
	 * @throws AFTException
	 */
	public boolean verifySelectedOption(String objectID, String elementName,
			String textPattern) throws AFTException {
		String innerText = "";
		boolean bTextMatch = false;
		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String type = repositoryObject.getType();
			if (objectID.isEmpty()) {
				LOGGER.error("Element [" + objectID
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + objectID
						+ "] not found in Object Repository");
			} else {
				LOGGER.trace("Waiting for element [" + objectID
						+ "] to be present");
				String name = repositoryObject.getName();

				if (!name.isEmpty()) {

					LOGGER.trace("Element [" + objectID + "] is found");
					LOGGER.trace("Executing command: [getText]");
					if (type.equalsIgnoreCase("listbox")) {

						List<Object> objlist = AFTFrankensteinBase
								.getInstance().getDriver()
								.getselectedlistValues(name);
						for (Object obj : objlist) {
							innerText = innerText + "," + obj.toString();
						}
						innerText = innerText.replaceFirst(",", "");

						if (innerText.equalsIgnoreCase(textPattern)) {
							bTextMatch = true;
							LOGGER.info("Verify: Success, value ["
									+ textPattern + "] is  match to the text");
						} else {
							LOGGER.error("Verify: Failed, Value of Actuval is  ["
									+ innerText
									+ "], expected value is ["
									+ textPattern + "]");
							bTextMatch = false;
						}

					} else if (type.equalsIgnoreCase("combobox")) {

						innerText = AFTFrankensteinBase.getInstance()
								.getDriver().getSelectedValue(name,type);
						if (innerText.equalsIgnoreCase(textPattern)) {
							bTextMatch = true;
							LOGGER.info("Verify: Success, value ["
									+ textPattern + "] is  match to the text");
						} else {
							LOGGER.error("Verify: Failed, Value of Actuval is  ["
									+ innerText
									+ "], expected value is ["
									+ textPattern + "]");
							bTextMatch = false;
						}
					}
					if (!bTextMatch) {
						throw new AFTException(
								"Verify: Failed, The expected value ["
										+ innerText
										+ "] for object ["
										+ objectID
										+ "] does not match with actual value ["
										+ textPattern + "]");
					}

				} else {
					LOGGER.error("Element [" + objectID + "] not found");
					throw new AFTException("Element [" + objectID
							+ "] not found");
				}

			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);

		}
		return bTextMatch;
	}

	/**
	 * verifyState control state for editable objects like textbox, combobox,
	 * list control, radi button etc
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	public boolean verifyState(String objectID, String elementName,
			String expectedValue) throws AFTException {
		boolean stateValue = false;

		try {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String objType = "";
			if (repositoryObject != null) {
				objType = repositoryObject.getType();
			}
			if (objType.equalsIgnoreCase("button")) {
				stateValue = verifyButton(objectID, elementName, expectedValue,
						objType);
			} else if (objType.equalsIgnoreCase("checkbox")) {
				stateValue = verifycheckboxstate(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("Radiobutton")) {
				stateValue = verifyradioButton(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("listbox")) {
				stateValue = verifylistbox(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("combobox")) {
				stateValue = verifycomboboxstate(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("Textbox")) {
				stateValue = verifytextboxstate(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("Dialog")) {
				stateValue = verifydialogstate(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("frame")) {
				stateValue = verifyframestate(objectID, elementName,
						expectedValue, objType);
			} else if (objType.equalsIgnoreCase("tab")) {
				stateValue = verifytabstate(objectID, elementName,
						expectedValue, objType);
			}
			if (!stateValue) {
				throw new AFTException(
						"Verify: Failed, The expected value is not ["
								+ expectedValue + "] for object [" + objectID
								+ "] ");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return stateValue;
	}

	/**
	 * verifycomboboxstate control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifycomboboxstate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;

		if ((expectedValue.toLowerCase().contains("enabled"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isDropDownEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("visible"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isDropDownVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("disabled"))
				&& !(AFTFrankensteinBase.getInstance().getDriver()
						.isDropDownEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifycontrol state for tab
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifytabstate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isTableEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isTableVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifydialogstate control state for dialog
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifydialogstate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isDialogEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isDialogVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifyframestate control state for frame
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifyframestate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isFrameEnable(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isFrameVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifytextboxstate control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifytextboxstate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains("enabled"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isTextBoxEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("visible"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isTextBoxVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("disabled"))
				&& !(AFTFrankensteinBase.getInstance().getDriver()
						.isTextBoxEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}else if ((expectedValue.toLowerCase().contains("editable"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isTextBoxEditble(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}else if ((expectedValue.toLowerCase().contains("noteditable"))
					&& !(AFTFrankensteinBase.getInstance().getDriver()
							.isTextBoxEditble(objectID))) {
				LOGGER.info("Verify: Success, State of [" + objType + "] element ["
						+ elementName + "] matches with expected value ["
						+ expectedValue + "]");
				stateValue = true;
		}
		return stateValue;
	}

	/**
	 * verifycheckboxstate control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifylistbox(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isListBoxEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isListBoxVisble(objectID))) {

			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}
		return stateValue;
	}

	/**
	 * verifyradioButton control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifycheckboxstate(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;

		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isCheckboxEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isCheckboxVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("disabled"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isCheckboxEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("checked"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isCheckboxChecked(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("unchecked"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isCheckboxChecked(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifyradioButton control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifyradioButton(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;

		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isRadioButtonEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isRadioButtonVisible(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("checked"))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isRadioButtonSelected(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("unchecked"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isRadioButtonSelected(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifybuttonState control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	private boolean verifyButton(String objectID, String elementName,
			String expectedValue, String objType) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.ENABLED))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isbuttonEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase()
				.contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isbuttonVisble(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("disabled"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isbuttonEnabled(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("invisible"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isbuttonVisble(objectID))) {
			LOGGER.info("Verify: Success, State of [" + objType + "] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
	}

	/**
	 * verifybuttonState control state for button
	 * 
	 * @param objectID
	 *            unique object id of the control to verify selected value from
	 * @param elementName
	 *            element name of the control as provided by user
	 * @param expectedValue
	 *            the value to verify
	 * 
	 * @return Boolean value - true if count matches and false if count
	 *         mismatches
	 * @throws AFTException
	 */
	public boolean verifyLabel(String objectID, String elementName,
			String expectedValue) {
		boolean stateValue = false;
		if ((expectedValue.toLowerCase().contains(ValidationFixtures.VISIBLE))
				&& (AFTFrankensteinBase.getInstance().getDriver()
						.isLabelPresent(objectID))) {
			LOGGER.info("Verify: Success, State of [Label] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		} else if ((expectedValue.toLowerCase().contains("invisible"))
				&& (!AFTFrankensteinBase.getInstance().getDriver()
						.isLabelPresent(objectID))) {
			LOGGER.info("Verify: Success, State of [Label] element ["
					+ elementName + "] matches with expected value ["
					+ expectedValue + "]");
			stateValue = true;
		}

		return stateValue;
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
		int optionCount;
		LOGGER.trace("Verifying that element [" + objectID + "] to be present");
		if (!objectID.isEmpty()) {
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String type = repositoryObject.getType();

			LOGGER.trace("Element [" + objectID + "] is found");

			String[] inputItemList = value.split(",");
			int expectedCount = inputItemList.length;
			optionCount = AFTFrankensteinBase.getInstance().getDriver()
					.getOptionCount(objectID,type);
			ArrayList<String> listItems = new ArrayList<String>();
			for (int item = 0; item < optionCount; item++) {
				listItems.add(AFTFrankensteinBase.getInstance().getDriver()
						.getOptionValueByIndex(objectID, item));
			}
			String[] itemList = listItems.toArray(new String[listItems.size()]);
			int actualCount = itemList.length;
			int matchCount = 0;

			// Verify the items are present in the List or Dropdown
			for (int subitem = 0; subitem < expectedCount; subitem++) {
				for (int item = 0; item < actualCount; item++) {
					if (itemList[item].trim().equals(
							inputItemList[subitem].trim())) {
						matchCount++;
						if (matchCount == expectedCount) {
							LOGGER.info("Verify: Success, Expected list of items "
									+ "["
									+ value
									+ "]"
									+ "found in "
									+ Arrays.toString(itemList));
							result = true;
							break;
						}
						break;
					}
				}
			}
			if (matchCount != expectedCount) {
				String errMsg = ("Verify: Failure, Expected list of items "
						+ "[" + value + "]" + "not found in " + Arrays
						.toString(itemList));
				LOGGER.error(errMsg);
				result = false;
				throw new AFTException(errMsg);
			}
		} else {
			String errorMsg = "Element [" + objectID + "] not found";
			LOGGER.error(errorMsg);
			throw new AFTException(errorMsg);
		}

		return result;
	}

}