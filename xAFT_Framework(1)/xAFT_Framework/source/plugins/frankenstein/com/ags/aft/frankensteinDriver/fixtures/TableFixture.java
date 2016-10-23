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
 * Class: WebTableFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the Table Object in the UI
 */

package com.ags.aft.frankensteinDriver.fixtures;

import org.apache.log4j.Logger;

import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * The Class TableFixture.
 * 
 */
public class TableFixture {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TableFixture.class);

	/** The wait fixtures. */

	/** The wait for element. */
	private boolean waitForElement;
	/** The wait fixtures. */
	private final WaitFixtures waitFixtures;
	private int tableWaitTime_ms = 3000;

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public TableFixture() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */

	/**
	 * Gets the table row count.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public int getTableRowCount(String objectID, String elementName)
			throws AFTException {
		int rowCount = 0;
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
				waitForElement = waitFixtures.waitForElementPresent(objectID,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						elementName);

				if (waitForElement) {
					LOGGER.trace("Element [" + objectID + "] is found");
					LOGGER.trace("Executing command [getXpathCount]");
					rowCount = AFTFrankensteinBase.getInstance().getDriver()
							.getTableRowCount(name);

					LOGGER.info("Total number of rows in the table ["
							+ objectID + "] is [" + rowCount + "]");
				} else {
					LOGGER.error("Table [" + objectID + "] not found");
					throw new AFTException("Table [" + objectID + "] not found");
				}
				// Setting the Row Count value to the System Variable.
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TABLEROWCOUNT), true,
						Integer.toString(rowCount));
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return rowCount;
	}
	
	/**
	 * Gets the all table row count and comparing the row text with existing text.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean getAllTableRowTextValidation(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		boolean isSearchString=false;
		String returnVariable="";
		try {
			LOGGER.trace("Waiting for elemaent [" + objectID + "] to be present");
			/*String parsedValues[]=parsedElementValue.split(",");
			returnVariable=parsedValues[parsedValues.length-1];
			String actualValue="";
			for(int i=0;i<(parsedValues.length-1);i++){
				actualValue=actualValue+","+parsedValues[i];
			}
			actualValue=actualValue.replaceFirst(",", "");*/
			/*String values[]=elementName.split(",");
			String searchString="";
			for(int i=1;i<values.length;i++){
				searchString=searchString+","+values[i];
			}
			searchString=searchString.replaceFirst(",", "");
			searchString=searchString.replace("[[","");
			searchString=searchString.replace("]]","");*/
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				Thread.sleep(tableWaitTime_ms);
				isSearchString=AFTFrankensteinBase.getInstance().getDriver()
						.getAllTableRowTextValidation(name,parsedElementValue);
				Thread.sleep(tableWaitTime_ms);
				LOGGER.info("[getAllTableRowTextValidation] executed on ["
						+ name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		returnVariable=isSearchString+"";
		return isSearchString;
	}
	
	/**
	 * Gets the table column count.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public String getEMSApprovedTask(String objectID, String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				
				LOGGER.trace("Waiting for element [" + name + "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(name,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + name + "] is found");
					cellvalue = AFTFrankensteinBase.getInstance().getDriver()
							.getEMSApprovedTask(name, row, column);
					LOGGER.info("cell value in the table [" + name + "] is ["
							+ cellvalue + "]");
				} else {
					LOGGER.error("Table [" + name + "] not found");
					throw new AFTException("Table [" + name + "] not found");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("Table [" + name + "] Cell value: " + cellvalue + " len("
				+ cellvalue.length() + ")");
		return cellvalue;
	}
	/**
	 * Gets the all table row count and comparing the row text with existing text.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public void setEMSValidationComent(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		
		try {
			LOGGER.trace("Waiting for elemaent [" + objectID + "] to be present");
			
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				Thread.sleep(tableWaitTime_ms);
				AFTFrankensteinBase.getInstance().getDriver()
						.setEMSValidationComent(name,parsedElementValue);
				Thread.sleep(tableWaitTime_ms);
				LOGGER.info("[setEMSValidationComent] executed on ["
						+ name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	
		
	}
	
	


	/**
	 * Gets the table column count.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table column count
	 * @throws AFTException
	 *             the application exception
	 */
	public int getTableColumnCount(String objectID, String elementName)
			throws AFTException {
		int columnCount = 0;
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

				if (waitForElement) {
					LOGGER.trace("Element [" + objectID + "] is found");
					// Let us form the first element xpath and retrieve to
					// correctly
					// retrieve the number of columns
					LOGGER.trace("Executing command [getXpathCount]");
					columnCount = AFTFrankensteinBase.getInstance().getDriver()
							.getTableColumnCount(objectID);

					LOGGER.info("Total number of columns in the table ["
							+ objectID + "] is [" + columnCount + "]");
				} else {
					LOGGER.error("Table [" + objectID + "] not found");
					throw new AFTException("Table [" + objectID + "] not found");
				}
				// Setting the Column Count value to the System Variable.
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TABLECOLUMNCOUNT), true,
						Integer.toString(columnCount));
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return columnCount;
	}

	/**
	 * Gets the table column count.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public String getCellValue(String objectID, String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				
				LOGGER.trace("Waiting for element [" + name + "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(name,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + name + "] is found");
					cellvalue = AFTFrankensteinBase.getInstance().getDriver()
							.getColumnValue(name, row, column);
					LOGGER.info("cell value in the table [" + name + "] is ["
							+ cellvalue + "]");
					if(null==cellvalue){
						cellvalue="";
					}
				} else {
					LOGGER.error("Table [" + name + "] not found");
					throw new AFTException("Table [" + name + "] not found");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("Table [" + name + "] Cell value: " + cellvalue + " len("
				+ cellvalue.length() + ")");
		return cellvalue;
	}
	
	/**
	 *isCellEditable 
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return is cell editable
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean isCellEditable(String objectID, String elementName)
			throws AFTException {
		boolean isEditable=false;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				
				LOGGER.trace("Waiting for element [" + name + "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(name,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + name + "] is found");
					isEditable = AFTFrankensteinBase.getInstance().getDriver()
							.isCellEditable(name, row, column);
					LOGGER.info("cell value in the table [" + name + "] is ["
							+ isEditable + "]");
				} else {
					LOGGER.error("Table [" + name + "] not found");
					throw new AFTException("Table [" + name + "] not found");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("Table [" + name + "] Cell value: " + isEditable +")");
		return isEditable;
	}
	
	

	public String getCellPlainText(String objectID, String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}

		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Waiting for element [" + name + "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(name,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + name + "] is found");
					cellvalue = AFTFrankensteinBase.getInstance().getDriver()
							.getColumnPlainText(name, row, column);
					LOGGER.info("cell value in the table [" + name + "] is ["
							+ cellvalue + "]");
				} else {
					LOGGER.error("Table [" + name + "] not found");
					throw new AFTException("Table [" + name + "] not found");
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		LOGGER.info("Table [" + name + "] Cell value: " + cellvalue + " len("
				+ cellvalue.length() + ")");
		return cellvalue;
	}

	
	
	/**
	 * To check if the column header is present
	 * 
	 * @param objectID
	 *            : Table id
	 * @param elementName
	 *            : Header name
	 * @return boolean
	 * @throws AFTException
	 */
	public String isTableHeaderPresent(String objectID, String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		String columnHeader = null;
		if (parsedElementValueArr[1].trim().contains("#")) {
			columnHeader = Helper.getInstance().getActionValue(
					parsedElementValueArr[1].trim());
		} else {
			columnHeader = parsedElementValueArr[1].trim();
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String tableId = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + tableId
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + tableId
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Waiting for element [" + tableId
						+ "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(tableId,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + tableId + "] is found");
					cellvalue = AFTFrankensteinBase.getInstance().getDriver()
							.isTableHeaderPresent(tableId, columnHeader)
							+ "";
					if (cellvalue.equalsIgnoreCase("true")) {
						LOGGER.info("Table header: " + columnHeader
								+ " available in the table: " + tableId);
					} else {
						LOGGER.info("Table header: " + columnHeader
								+ " not available in the table: " + tableId);
					}
				} else {
					throw new AFTException("Table header: " + columnHeader
							+ " not available in the table: " + tableId);
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return cellvalue;
	}
	
	/**
	 * To get the column header
	 * 
	 * 
	 * @param elementName
	 *            : Table id and column number of the required header
	 * @return String, the header value
	 * @throws AFTException
	 */
	public String getTableHeader(String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		String columnHeader = null;
		if (parsedElementValueArr[1].trim().contains("#")) {
			columnHeader = Helper.getInstance().getActionValue(
					parsedElementValueArr[1].trim());
		} else {
			columnHeader = parsedElementValueArr[1].trim();
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String tableId = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + tableId
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + tableId
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Waiting for element [" + tableId
						+ "] to be present");
				waitForElement = waitFixtures.waitForElementPresent(tableId,
						UIFixtureUtils.getInstance().getElementWaitTime(),
						parsedElementValueArr[0]);
				if (waitForElement) {
					LOGGER.trace("Element [" + tableId + "] is found");
					cellvalue = AFTFrankensteinBase.getInstance().getDriver()
							.getTableHeader(tableId, columnHeader);
					if (null!=cellvalue) {
						LOGGER.info("Table header: " + columnHeader);
					} else {
						LOGGER.info("Table header: " + columnHeader
								+ " is not available in the table: " + tableId);
					}
				} else {
					throw new AFTException("Table header: " + columnHeader
							+ " is not available in the table: " + tableId);
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return cellvalue;
	}

	/**
	 * Gets the custom table cell value.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name, row
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public String getCustomTableCellValue(String elementName)
			throws AFTException {
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = Integer.parseInt(parsedElementValueArr[1]);
		int column = Integer.parseInt(parsedElementValueArr[2]);
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + parsedElementValueArr[0]
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + parsedElementValueArr[0]
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Waiting for element [" + parsedElementValueArr[0]
						+ "] to be present");
				LOGGER.trace("Element [" + parsedElementValueArr[0]
						+ "] is found");
				cellvalue = AFTFrankensteinBase.getInstance().getDriver()
						.getColumnValue(parsedElementValueArr[0], row, column);
				LOGGER.info("cell value in the table ["
						+ parsedElementValueArr[0] + "] is [" + cellvalue + "]");
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return cellvalue;
	}

	/**
	 * Edit cell in the table.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public void edittablecell(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				String[] parsedElementValueArr = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueArr[0]);
				int column = Integer.parseInt(parsedElementValueArr[1]);
				AFTFrankensteinBase.getInstance().getDriver()
						.editTableCell(objectID, row, column);
				LOGGER.info("cell value in the table [" + objectID
						+ "] is edited");
			} else {
				LOGGER.error("Table [" + objectID + "] not found");
				throw new AFTException("Table [" + objectID + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Sets the table cell value.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public void setTableCellValue(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				String[] parsedElementValueArr = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueArr[0]);
				int column = Integer.parseInt(parsedElementValueArr[1]);
				String text="";
				if(parsedElementValueArr.length>2){
					for(int count=2;count<parsedElementValueArr.length;count++){
						text=text+","+parsedElementValueArr[count];
					}
					text=text.replaceFirst(",","");
				}else{
					text = parsedElementValueArr[2];
				}
				AFTFrankensteinBase.getInstance().getDriver()
						.setTableCellValue(objectID, row, column, text);
				LOGGER.info("cell value in the table [" + objectID
						+ "] is edited");
			} else {
				LOGGER.error("Table [" + objectID + "] not found");
				throw new AFTException("Table [" + objectID + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	/**
	 * Sets the table cell value by index.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectCellValueByIndex(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				String[] parsedElementValueArr = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueArr[0]);
				int column = Integer.parseInt(parsedElementValueArr[1]);
				int comboboxIndex= Integer.parseInt(parsedElementValueArr[2]);
				int optionIndex= Integer.parseInt(parsedElementValueArr[3]);
				AFTFrankensteinBase.getInstance().getDriver()
						.selectCellValueByIndex(objectID, row, column,comboboxIndex,optionIndex);
				LOGGER.info("cell value in the table [" + objectID
						+ "] is edited");
			} else {
				LOGGER.error("Table [" + objectID + "] not found");
				throw new AFTException("Table [" + objectID + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	
	
	/**
	 *gets the cell option count
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public int getCellOptionCount(String objectID,String elementName) throws AFTException {
		int optionCount=0,comboboxIndex;
		String cellvalue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}
		if (parsedElementValueArr[3].contains("#")) {
			comboboxIndex = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[3]));
		} else {
			comboboxIndex = Integer.parseInt(parsedElementValueArr[3]);
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
		try {
			LOGGER.trace("Waiting for element [" + parsedElementValueArr[0] + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				optionCount=AFTFrankensteinBase.getInstance().getDriver()
						.getCellOptionCount(name, row, column, comboboxIndex);
				LOGGER.info("The option count in the combo box:"+optionCount);
			} else {
				LOGGER.error("Table [" + name + "] not found");
				throw new AFTException("Table [" + name + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		}
		return optionCount;
	}
	public String getCellSelectedValue(String objectID,String elementName) throws AFTException {
		int comboboxIndex;
		String cellvalue,selctedValue;
		String[] parsedElementValueArr = elementName.split(",");
		int row = 0;
		int column = 0;

		if (parsedElementValueArr[1].contains("#")) {
			row = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[1]));
		} else {
			row = Integer.parseInt(parsedElementValueArr[1]);
		}
		if (parsedElementValueArr[2].contains("#")) {
			column = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[2]));
		} else {
			column = Integer.parseInt(parsedElementValueArr[2]);
		}
		if (parsedElementValueArr[3].contains("#")) {
			comboboxIndex = Integer.parseInt(Helper.getInstance().getActionValue(
					parsedElementValueArr[3]));
		} else {
			comboboxIndex = Integer.parseInt(parsedElementValueArr[3]);
		}
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(parsedElementValueArr[0]);
		String name = repositoryObject.getName();
		if (parsedElementValueArr[0].isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
		try {
			LOGGER.trace("Waiting for element [" + parsedElementValueArr[0] + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				selctedValue=AFTFrankensteinBase.getInstance().getDriver()
						.getCellSelectedValue(name, row, column, comboboxIndex);
				LOGGER.info("The option count in the combo box:"+selctedValue);
			} else {
				LOGGER.error("Table [" + name + "] not found");
				throw new AFTException("Table [" + name + "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		}
		return selctedValue;
	}
	

	/**
	 * select item selectContextMenuTablecellRightClick.
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

	public void selectContextMenuTablecellRightClick(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();
				String[] parsedElementValueNav = parsedElementValue.split(",");
				if (parsedElementValueNav.length == 3) {
					int row = Integer.parseInt(parsedElementValueNav[0]);
					int column = Integer.parseInt(parsedElementValueNav[1]);
					String columnIndex = parsedElementValueNav[2];
					AFTFrankensteinBase.getInstance().getDriver()
							.rightClickTableCell(objectID, row, column);
					Thread.sleep(tableWaitTime_ms);
					AFTFrankensteinBase.getInstance().getDriver()
							.navigate(columnIndex);
				} else if (parsedElementValueNav.length == 4) {
					int row = Integer.parseInt(parsedElementValueNav[0]);
					int column = Integer.parseInt(parsedElementValueNav[1]);
					String columnIndex = parsedElementValueNav[2];
					// String columnInnerIndex = parsedElementValueNav[3];
					AFTFrankensteinBase.getInstance().getDriver()
							.rightClickTableCell(objectID, row, column);
					Thread.sleep(tableWaitTime_ms);
					AFTFrankensteinBase.getInstance().getDriver()
							.navigate(columnIndex);
					// Thread.sleep(500);
					// AFTFrankensteinBase.getInstance().getDriver()
					// .navigate(columnInnerIndex);
				}
				LOGGER.info("[selectContextMenuTreeRightClick] executed on ["
						+ name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Select the table cell based on the specified row and column numbers
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

	public void selectTablecell(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for elemaent [" + objectID + "] to be present");

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();
				String[] parsedElementValueNav = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueNav[0]);
				int column = Integer.parseInt(parsedElementValueNav[1]);
				String columnIndex = parsedElementValueNav[2];
				Thread.sleep(tableWaitTime_ms);
				AFTFrankensteinBase.getInstance().getDriver()
						.selectTablecell(objectID, row, column);
				Thread.sleep(tableWaitTime_ms);
				AFTFrankensteinBase.getInstance().getDriver()
						.navigate(columnIndex);
				Thread.sleep(tableWaitTime_ms);

				LOGGER.info("[selectContextMenuTreeRightClick] executed on ["
						+ name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * select table checkbox in the table.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @param parsedElementValue
	 *            row,column value
	 * @return the table cell value
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectTableCheckbox(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				String[] parsedElementValueArr = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueArr[0]);
				int column = Integer.parseInt(parsedElementValueArr[1]);
				String checkboxname = parsedElementValueArr[2];
				boolean blnValue = Boolean
						.parseBoolean(parsedElementValueArr[3]);
				/*
				 * Thread.sleep(500);
				 * AFTFrankensteinBase.getInstance().getDriver()
				 * .doubleClickTableRow(objectID, row); Thread.sleep(500);
				 * AFTFrankensteinBase.getInstance().getDriver()
				 * .editTableCell(objectID, row, column); Thread.sleep(3000);
				 */
				AFTFrankensteinBase.getInstance().getDriver()
						.clickTableCheckbox(objectID, row, column, blnValue);
				// AFTFrankensteinBase.getInstance().getDriver()
				// .stopTableEdit(objectID);
				LOGGER.info("chekbox" + checkboxname + " in the table ["
						+ objectID + "] is checked/unchecked ");
			} else {
				LOGGER.error("Table [" + objectID + "] not found");
				throw new AFTException("Table [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * right Click in Table row.
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
	public void rightClickOnTableRow(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				try {
					AFTFrankensteinBase
							.getInstance()
							.getDriver()
							.rightClickTableRow(objectID,
									Integer.parseInt(parsedElementValue));
					LOGGER.info("[rightclick on row ] edited on [" + objectID
							+ "]");
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
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
	 * right Click and select context-menu in Table cell.
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
	public void rightclicktablecell(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				try {

					String[] parsedElementValueNav = parsedElementValue
							.split(",");

					AFTFrankensteinBase
							.getInstance()
							.getDriver()
							.rightClickTableCell(objectID,
									Integer.parseInt(parsedElementValueNav[0]),
									Integer.parseInt(parsedElementValueNav[1]));

					Thread.sleep(tableWaitTime_ms);

					AFTFrankensteinBase.getInstance().getDriver()
							.navigate(parsedElementValueNav[2]);

					LOGGER.info("[rightclick and select-menu context on specified cell ] edited on ["
							+ objectID + "]");
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
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
	 * right Click and select context-menu in Table cell.
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
	public void clicktablecell(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				try {

					String[] parsedElementValueNav = parsedElementValue
							.split(",");

					AFTFrankensteinBase
							.getInstance()
							.getDriver()
							.selectTablecell(objectID,
									Integer.parseInt(parsedElementValueNav[0]),
									Integer.parseInt(parsedElementValueNav[1]));

					Thread.sleep(tableWaitTime_ms);

					AFTFrankensteinBase.getInstance().getDriver()
							.navigate(parsedElementValueNav[2]);

					LOGGER.info("[rightclick and select-menu context on specified cell ] edited on ["
							+ objectID + "]");
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
					throw new AFTException(e);
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
	 * select Table Row from the Check box.
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
	public void selectTableRowWithText_old(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			String SearchText;
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {
					int rowCount = AFTFrankensteinBase.getInstance()
							.getDriver().getTableRowCount(name);
					int columnCount = AFTFrankensteinBase.getInstance()
							.getDriver().getTableColumnCount(name);
					for (int i = 0; i < columnCount; i++) {
						for (int j = 0; j < rowCount; j++) {
							SearchText = AFTFrankensteinBase.getInstance()
									.getDriver().getColumnValue(name, j, i);
							if (SearchText.equalsIgnoreCase(parsedElementValue)) {
								int[] x = new int[1];
								x[0] = j;
								AFTFrankensteinBase.getInstance().getDriver()
										.selectTableRow(objectID, x);
								i = columnCount;
								break;
							}
						}
					}

					LOGGER.info("[selectTableRowWithText] executed on ["
							+ elementName + "]");
				}

			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * select Table Row from the Check box.
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
	public void selectTableRowWithText(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			// String SearchText;
			String params[] = parsedElementValue.split(",");
			// params[0]-Search text
			// params[1]-Column name
			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			// String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {
					int row;
					if (params.length == 1) {
						row = AFTFrankensteinBase.getInstance().getDriver()
								.getRowWithText(objectID, params[0]);
					} else {
						row = AFTFrankensteinBase
								.getInstance()
								.getDriver()
								.getRowWithText(objectID, params[0],
										Integer.parseInt(params[1]));
					}
					if (row != -1) {
						LOGGER.info("[selectTableRowWithText] executed on ["
								+ elementName + "]");
						int rows[] = { row };
						AFTFrankensteinBase.getInstance().getDriver()
								.selectTableRow(objectID, rows);
					} else {
						LOGGER.error("[selectTableRowWithText] executed on ["
								+ elementName + "; Expected Text not found: "
								+ params[0]);
					}

					LOGGER.info("[selectTableRowWithText] executed on ["
							+ elementName + "]");
				}

			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * select Table Row from the Check box.
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
	public void selectTableRow(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {

					String[] parsedElementValueArr = parsedElementValue
							.split(",");
					int[] parsedElementValueArrint = new int[parsedElementValueArr.length];
					for (int i = 0; i < parsedElementValueArr.length; i++) {
						parsedElementValueArrint[i] = Integer
								.parseInt(parsedElementValueArr[i]);
					}

					AFTFrankensteinBase.getInstance().getDriver()
							.selectTableRow(objectID, parsedElementValueArrint);
					LOGGER.info("[selectTableRow] executed on [" + objectID
							+ "]");
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
			LOGGER.error("Exception:", e);
			throw new AFTException(e);
		}
	}
	
	/**
	 * select all the table rows
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectAllTableRows(String objectID,
			String elementName) throws AFTException {
		try {

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command [selectAllTableRows]");
				if (repositoryObject != null) {

					AFTFrankensteinBase.getInstance().getDriver()
							.selectAllTableRows(objectID);
					LOGGER.info("[selectAllTableRows] executed on [" + objectID
							+ "]");
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
			LOGGER.error("Exception:", e);
			throw new AFTException(e);
		}
	}
		/**
	 * Get the table row number based on the specified text in the table
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
	public String getRowWithText(String objectID, String elementName)
			throws AFTException {
		String rowNumber = "Empty";
		String SearchText;

		try {

			String[] parsedElementValueArr = elementName.split(",");

			if (parsedElementValueArr[1].contains("#")) {
				SearchText = Helper.getInstance().getActionValue(
						parsedElementValueArr[1]);
			} else {
				SearchText = parsedElementValueArr[1];
			}

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(parsedElementValueArr[0]);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {
					// int row;
					if (parsedElementValueArr.length == 1)
						rowNumber = AFTFrankensteinBase.getInstance()
								.getDriver().getRowWithText(name, SearchText)
								+ "";
					else
						rowNumber = AFTFrankensteinBase.getInstance()
								.getDriver().getRowWithText(name, SearchText)
								+ "";
				}
			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return rowNumber;
	}
	
	
	
	/**
	 * Get the claim task name from the table
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
	public String getEMSClaimTask(String objectID, String elementName)
			throws AFTException {
		int row=0;
		int col=0;
		String task="EMPTY";

		try {

			String[] parsedElementValueArr = elementName.split(",");

			if (parsedElementValueArr[1].contains("#")) {
				row = Integer.parseInt(Helper.getInstance().getActionValue(
						parsedElementValueArr[1]));
			} else {
				row = Integer.parseInt(parsedElementValueArr[1]);
			}

			if (parsedElementValueArr[1].contains("#")) {
				col = Integer.parseInt(Helper.getInstance().getActionValue(
						parsedElementValueArr[2]));
			} else {
				col = Integer.parseInt(parsedElementValueArr[2]);
			}

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(parsedElementValueArr[0]);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				LOGGER.trace("Executing command [getEMSClaimTask]");
				if (repositoryObject != null) {
					task = AFTFrankensteinBase.getInstance()
								.getDriver().getEMSClaimTask(name,row,col);

				}
			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return task;
	}
	
	
	/**
	 * Get the table row number based on the specified text in the table
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
	public String getEMSRowWithTask(String objectID, String elementName)
			throws AFTException {
		String rowNumber = "Empty";
		String SearchText;
		int col=0;

		try {

			String[] parsedElementValueArr = elementName.split(",");

			if (parsedElementValueArr[2].contains("#")) {
				SearchText = Helper.getInstance().getActionValue(
						parsedElementValueArr[2]);
			} else {
				SearchText = parsedElementValueArr[2];
			}

			if (parsedElementValueArr[1].contains("#")) {
				col = Integer.parseInt(Helper.getInstance().getActionValue(
						parsedElementValueArr[1]));
			} else {
				col = Integer.parseInt(parsedElementValueArr[1]);
			}

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(parsedElementValueArr[0]);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {
					// int row;
					System.out.println("sdfgf");
				//	if (parsedElementValueArr.length == 1)
						rowNumber = AFTFrankensteinBase.getInstance()
								.getDriver().getEMSRowWithTask(name,col, SearchText)
								+ "";
				}
			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return rowNumber;
	}
	
	
	
	/**
	 * Get the number of table rows with partial matched text
	 * 
	 * @param elementName
	 *            the element name contains the table name,column number, required search string
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */
	public String getEMSRowCountWithPartialTask(String objectID, String elementName)
			throws AFTException {
		String rowNumber = "Empty";
		String SearchText;
		int col=0;

		try {

			String[] parsedElementValueArr = elementName.split(",");

			if (parsedElementValueArr[2].contains("#")) {
				SearchText = Helper.getInstance().getActionValue(
						parsedElementValueArr[2]);
			} else {
				SearchText = parsedElementValueArr[2];
			}

			if (parsedElementValueArr[1].contains("#")) {
				col = Integer.parseInt(Helper.getInstance().getActionValue(
						parsedElementValueArr[1]));
			} else {
				col = Integer.parseInt(parsedElementValueArr[1]);
			}

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(parsedElementValueArr[0]);
			String name = repositoryObject.getName();
			waitForElement = waitFixtures.waitForElementPresent(name,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					parsedElementValueArr[0]);
			if (waitForElement) {
				LOGGER.trace("Element [" + name + "] is found");
				LOGGER.trace("Executing command [selectTableRow]");
				if (repositoryObject != null) {
						rowNumber = AFTFrankensteinBase.getInstance()
								.getDriver().getEMSRowCountWithPartialTask(name,col, SearchText)
								+ "";
				}
			} else {
				LOGGER.trace("Element [" + elementName + "] is  not found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return rowNumber;
	}


	
	/**
	 * Double click in Table cell.
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

	public void doubleClickTableCell(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();
				String[] parsedElementValueNav = parsedElementValue.split(",");
				int row = Integer.parseInt(parsedElementValueNav[0]);
				int column = Integer.parseInt(parsedElementValueNav[1]);
				Thread.sleep(4000);
				AFTFrankensteinBase.getInstance().getDriver()
						.DoubleClickTableCell(objectID, row, column);
				Thread.sleep(3000);

				LOGGER.info("[Double Click on Table Cell] executed on [" + name
						+ "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

}
