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
 * Class: Variable
 * 
 * Purpose: This class contains Variable handling methods to read and write to
 * user-defined and system variables
 */

package com.ags.aft.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ags.aft.common.Util;
import com.ags.aft.config.AppConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestSuiteRunner;

/**
 * variable class
 * 
 */
public final class Variable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	/** The aft helper. */
	private static Variable variable;

	/** The stored variables map. */
	private Map<String, String> storedVariablesMap = new HashMap<String, String>();

	/** The stored array variables map. */
	private Map<String, String[][]> storedArrayVariablesMap = new HashMap<String, String[][]>();

	/**
	 * Instantiates a new aft helper.
	 */
	private Variable() {
		super();
	}

	/**
	 * Gets the single instance of AftHelper.
	 * 
	 * @return single instance of AftHelper
	 */
	public static Variable getInstance() {
		if (variable == null) {
			variable = new Variable();
			LOGGER.trace("Creating instance of AftHelper");
		}

		return variable;
	}

	/**
	 * clear all variables
	 */
	public void clearAllVariables() {
		storedVariablesMap.clear();
	}

	/**
	 * get the unformatted current time in hhmmssS format.
	 * 
	 * @return time string in hh:mm:ss format
	 */
	public String getUnFormattedCurrentTime() {
		// Construct a new date object
		java.util.Date curTime = new java.util.Date();
		// Set format to HH:mm:ss
		DateFormat dateFormat = new SimpleDateFormat("HHmmssS");
		String currentTime = dateFormat.format(curTime);

		return currentTime;

	}

	/**
	 * This method substitutes the variable value in "value" either by
	 * generating a random value, dynamic value or reading from variable
	 * hashmap.
	 * 
	 * @param value
	 *            String in which the variable name needs to be substituted with
	 *            variable value
	 * @param variableName
	 *            variable name whose value needs to be substituted in value
	 * @param delimiter
	 *            variable name delimiter used in test suite
	 * @return the string
	 * @throws AFTException
	 */
	public String substituteVariableValue(String value, String variableName,
			char delimiter) throws AFTException {
		// Added code to return values for AFT_CurDate
		// and AFT_CurTime system variables...
		String parsedValue = value;
		String variableValue = "";
		char[] variableEscapeCharDelimiterArr = Constants.DYNAMICVARIABLEESCAPECHARDELIMITER
				.toCharArray();
		char[] escapeCharDelimiterArr = Constants.ESCAPECHARDELIMITERS
				.toCharArray();

		// If a system variable is passed and it is
		// AFT_CurDate
		if (variableName.compareToIgnoreCase(SystemVariables.AFT_CURDATE) == 0) {
			// Set format to MM-dd-YYYY
			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

			// Construct a new date object
			java.util.Date curDate = new java.util.Date();
			// retrieve the date in specified format...
			variableValue = dateFormat.format(curDate);

		} else if (variableName
				.compareToIgnoreCase(SystemVariables.AFT_CURUNFORMATTEDDATE) == 0) {
			// Set format to MM-dd-YYYY
			DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");

			// Construct a new date object
			java.util.Date curDate = new java.util.Date();
			// retrieve the date in specified format...
			variableValue = dateFormat.format(curDate);

			// If a system variable is passed and it is
			// AFT_CurTime
		} else if (variableName
				.compareToIgnoreCase(SystemVariables.AFT_CURTIME) == 0) {
			// retrieve the time in specified format...
			variableValue = Util.getInstance().getCurrentTime();

		} else if (variableName
				.compareToIgnoreCase(SystemVariables.AFT_CURUNFORMATTEDTIME) == 0) {
			// retrieve the time in specified format...
			variableValue = getUnFormattedCurrentTime();

		} else if (variableName.toLowerCase().startsWith(
				SystemVariables.AFT_APPCONFIG.toLowerCase())) {
			LOGGER.debug("Looks like user has specified a AppConfig property. Let us retrieve the variable and verify it.");
			// Extract the portion beyond '.' and then retrieve the value from
			// AppConfig
			String[] appConfigArr = variableName.split("\\.");
			if (appConfigArr.length != 2) {
				LOGGER.error("Invalid App Config variable ["
						+ variableName
						+ "]. AppConfig parameter name not found."
						+ " Please refer to AFT documentation on how to use AppConfig properties file.");
				throw new AFTException(
						"Invalid App Config variable ["
								+ variableName
								+ "]. AppConfig parameter name not found."
								+ " Please refer to AFT documentation on how to use AppConfig properties file.");
			}

			LOGGER.debug("Found a AppConfig property [" + appConfigArr[1] + "]");
			String appConfigPropertyValue = AppConfigProperties.getInstance()
					.getConfigProperty(appConfigArr[1]);
			LOGGER.debug("AppConfig property [" + appConfigArr[1]
					+ "], value [" + appConfigPropertyValue + "]");

			variableValue = appConfigPropertyValue;

			// Check if the variable is one for which
			// random data needs to be generated
		} else if (StackTestData.getInstance().randomDataVariable(variableName)) {
			variableValue = StackTestData.getInstance().generateRandomValue(
					variableName);
			storedVariablesMap.put(variableName.toLowerCase(), variableValue);

			// Check if the variable is an ArrayVariable
			// Return value from Array hash map
		} else if (isArrayVariable(variableName)) {
			LOGGER.debug("Looks like user has specified an Array variable. Let us retrieve the value.");
			variableValue = getArrayVariableValue(variableName);

		} else {
			// Else retrieve the value for all other
			// variables from hash map
			variableValue = storedVariablesMap.get(variableName.toLowerCase());
		}

		if ((variableValue == null) || (variableValue.length() <= 0)) {
			variableValue = "";
		}
		LOGGER.trace("Current value retrieved for variable [" + variableName
				+ "] is [" + variableValue + "]");

		// If the variable value contains escape character "#" and if this
		// character doesn't precedes with
		// "\" then this functionality adds "\" before "#"
		if (variableValue != null && !(variableValue.isEmpty())
				&& variableValue.contains(Constants.DYNAMICVARIABLEDELIMITER)) {
			StringBuffer sb = new StringBuffer();
			for (int position = 0; position < variableValue.length(); position++) {
				if (variableValue.charAt(position) == escapeCharDelimiterArr[1]
						&& (variableValue.charAt(position - 1) != variableEscapeCharDelimiterArr[0])) {
					sb.append(variableEscapeCharDelimiterArr[0]);
					sb.append(variableValue.charAt(position));
				} else {
					sb.append(variableValue.charAt(position));
				}
			}
			variableValue = sb.toString();
		}
		parsedValue = parsedValue.replace(delimiter + variableName + delimiter,
				variableValue);

		return parsedValue;
	}

	/**
	 * Method : Store value. Jira Issue : AFT-42 Sets the VariableName with
	 * Value against it in the HashTable.
	 * 
	 * @param testSuiteRunner
	 *            testSuiteRunner reference object
	 * @param commandName
	 *            command name for which this method is called
	 * @param variableName
	 *            the variable name
	 * @param isSystemVariable
	 *            is this a system variable?
	 * @param value
	 *            the value
	 * @throws AFTException
	 */
	public void setVariableValue(TestSuiteRunner testSuiteRunner,
			String commandName, String variableName, boolean isSystemVariable,
			String value) throws AFTException {
		String varName;
		boolean isValidVariableName = false;
		boolean isVariable = true;

		varName = "";
		if (isSystemVariable) {
			LOGGER.trace("Removing prefix and suffix characters for variable name from ["
					+ variableName + "]");
			varName = variableName.substring(1, variableName.length() - 1);
			LOGGER.trace("Variable name after removing prefix and suffix characters is ["
					+ varName + "]");

			isValidVariableName = true;
		} else {
			List<String> paramList = Helper.getInstance()
					.parseActionParameterList(testSuiteRunner, variableName,
							false);

			if (paramList != null && paramList.size() > 0) {
				if ((paramList.size() > 1)
						|| (paramList.get(0).compareToIgnoreCase(variableName) != 0)) {
					LOGGER.info("Looks like this is not a target variable name but just parameter list. Ignoring this call");
					isVariable = false;
				} else {
					varName = paramList.get(0);
					LOGGER.trace("Removing prefix and suffix characters for variable name from ["
							+ paramList.get(0) + "]");
					varName = paramList.get(0).substring(1,
							paramList.get(0).length() - 1);
					LOGGER.trace("Variable name after removing prefix and suffix characters is ["
							+ varName + "]");
					isValidVariableName = isValidUserVariable(varName);
				}
			}
		}

		if (isVariable) {
			if (isValidVariableName) {
				// Put the variable name and Value to the
				// storedVariablesMap - Hash table
				storedVariablesMap.put(varName.toLowerCase(), value);
				LOGGER.debug("Value [" + value
						+ "] has been stored in variable [" + varName + "]");
			} else {
				LOGGER.error("Variable name ["
						+ variableName
						+ "] specified looks invalid. Please check the syntax in test suite.");
			}
		}
	}

	/**
	 * This method returns all the Dynamic Variables and its values as a map
	 * object
	 * 
	 * @return the map
	 */
	public Map<String, String> variablesMap() {
		return storedVariablesMap;
	}

	/**
	 * Generates system variable name from the System variable constants defined
	 * in AFTSystemVariables.java Uses variable dynamic delimiter property
	 * defined in AFTConfig.properties file to add as suffix and prefix to make
	 * it a system variable
	 * 
	 * @param sysVarStr
	 *            - System variable String
	 * @return - constructed system variable name
	 */
	public String generateSysVarName(String sysVarStr) {
		String systemVariableName = "";

		try {
			systemVariableName = Constants.DYNAMICVARIABLEDELIMITER + sysVarStr
					+ Constants.DYNAMICVARIABLEDELIMITER;
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}

		return systemVariableName;
	}

	/**
	 * Increments the value of the system variable whose string is passed. Uses
	 * the system variable string to generate the system variable name,
	 * increments value and then store it back in hashmap
	 * 
	 * @param sysVarStr
	 *            - System variable String
	 * @throws AFTException
	 */
	public void incSysVarValue(String sysVarStr) throws AFTException {
		String curVarValue;
		String sysVarName;
		int iCurVarValue;
		int iNewVarValue;

		// Construct the variable name...
		sysVarName = generateSysVarName(sysVarStr);

		// Fetch current variable value...
		curVarValue = Helper.getInstance().getActionValue(sysVarName);

		// Convert current variable value from String to Int...
		try {
			iCurVarValue = Integer.parseInt(curVarValue);
		} catch (NumberFormatException ne) {
			iCurVarValue = 1;
		}

		// Increment current value...
		iNewVarValue = iCurVarValue + 1;

		// Store the new (incremented) value back to the hashmap...
		setVariableValue(sysVarName, true, Integer.toString(iNewVarValue));
	}

	/**
	 * Checks if is valid system variable.
	 * 
	 * @param variableName
	 *            the variable name
	 * @return true, if is valid user variable
	 */
	public boolean isValidSystemVariable(String variableName) {
		boolean isValidVariableName = false;

		if (isValidVariableName(variableName)) {
			if (variableName.toUpperCase().startsWith("AFT_")
					|| variableName.toUpperCase().startsWith("ERROR_")) {
				LOGGER.trace("Variable name [" + variableName + "] is valid");
				isValidVariableName = true;
			} else {
				LOGGER.trace("Invalid Variable Name! Variable Name should not start with AFT_ , ERROR_ , and should not be greater than 255 characters... Please check the AFT variable definition guidelines for details!");
				isValidVariableName = false;
			}
		} else {
			isValidVariableName = false;
		}

		return isValidVariableName;
	}

	/**
	 * Checks if is valid user variable.
	 * 
	 * @param variableName
	 *            the variable name
	 * @return true, if is valid user variable
	 */
	public boolean isValidUserVariable(String variableName) {
		boolean isValidVariableName = false;

		if (isValidVariableName(variableName)) {
			if (variableName.toUpperCase().startsWith("AFT_")
					|| variableName.toUpperCase().startsWith("ERROR_")) {
				LOGGER.trace("Invalid Variable Name! Variable Name should not start with AFT_ , ERROR_ , and should not be greater than 255 characters... Please check the AFT variable definition guidelines for details!");
				isValidVariableName = false;
			} else {
				LOGGER.trace("Variable name [" + variableName + "] is valid");
				isValidVariableName = true;
			}
		} else {
			isValidVariableName = false;
		}

		return isValidVariableName;
	}

	/**
	 * Method : Sets the VariableName with Value against it in the HashTable.
	 * 
	 * @param variableName
	 *            the variable name
	 * @param isSystemVariable
	 *            is this a system variable?
	 * @param value
	 *            the value
	 * @throws AFTException
	 */
	public void setVariableValue(String variableName, boolean isSystemVariable,
			String value) throws AFTException {
		if (isSystemVariable) {
			Variable.getInstance().setVariableValue(null, null, variableName,
					isSystemVariable, value);
		} else {
			LOGGER.error("Invalid call to setVariableValue. Must call with TestSuiteRunner reference and command name");
		}
	}

	/**
	 * Method: validateVariableName validates the Variable Name is as per the
	 * guidelines. In general the variable name should be alphanumeric ex:
	 * A,Val1,val2,val_123 etc.
	 * 
	 * @param variableName
	 *            the variable name to be validated if it is valid or not
	 * @return boolean - true if the variable is valid or false if the variable
	 *         is not valid
	 */
	public boolean isValidVariableName(String variableName) {
		boolean isValid = false;

		try {
			if (variableName.matches("^[0-9][a-z0-9A-Z_.]+$")) {
				LOGGER.debug("Invalid Variable Name ["
						+ variableName
						+ "], variable name should not begin with a number. Please check the AFT variable definition documentation for details!");
				isValid = false;

			} else {
				if (variableName.matches("^[a-zA-Z][a-z0-9A-Z_.]+$")) {
					// LOGGER
					// .trace("Variable name [" + variableName
					// + "] is valid");
					isValid = true;
				} else {
					LOGGER.debug("Invalid Variable Name ["
							+ variableName
							+ "], variable name should not contain special characters. Please check the AFT variable definition documentation for details!");
					isValid = false;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			LOGGER.error("Invalid Variable Name ["
					+ variableName
					+ "], variable name should not contain Special Characters... Please check the AFT variable definition documentation for details!");
		}

		return isValid;
	}

	/**
	 * Sets the VariableName with Value against it in the Array HashTable.
	 * 
	 * @param testSuiteRunner
	 *            testSuiteRunner reference object
	 * @param arrayType
	 *            the array type(Database/WebTable/UserCreated)
	 * @param variableName
	 *            the variable name
	 * @param value
	 *            the value
	 * @throws AFTException
	 */
	public void setArrayVariableValue(TestSuiteRunner testSuiteRunner,
			String arrayType, String variableName, String[][] value)
			throws AFTException {
		String varName;
		boolean isValidVariableName = false;
		boolean isVariable = true;

		varName = "";
		List<String> paramList = Helper.getInstance().parseActionParameterList(
				testSuiteRunner, variableName, false);

		if (paramList != null && paramList.size() > 0) {
			if ((paramList.size() > 1)
					|| (paramList.get(0).compareToIgnoreCase(variableName) != 0)) {
				LOGGER.info("Looks like this is not a target variable name but just parameter list. Ignoring this call");
				isVariable = false;
			} else {
				varName = paramList.get(0);
				LOGGER.trace("Removing prefix and suffix characters for variable name from ["
						+ paramList.get(0) + "]");
				varName = paramList.get(0).substring(1,
						paramList.get(0).length() - 1);
				LOGGER.trace("Variable name after removing prefix and suffix characters is ["
						+ varName + "]");
				isValidVariableName = isValidUserVariable(varName);
			}
		}

		if (isVariable) {
			if (isValidVariableName) {
				// Put the (arrayType+variableName) and Value to the
				// storedArrayVariablesMap - Hash table
				String key = arrayType.toLowerCase() + varName.toLowerCase();
				storedArrayVariablesMap.put(key, value);
				LOGGER.debug("Value[array] has been stored in variable ["
						+ varName + "]");
			} else {
				LOGGER.error("Variable name ["
						+ variableName
						+ "] specified looks invalid. Please check the syntax in test suite.");
			}
		}
	}

	/**
	 * Method : Checks if given variable is an ArrayVariable type.
	 * 
	 * @param variableName
	 *            the variable name
	 * @return true/false
	 * @throws AFTException
	 */
	public boolean isArrayVariable(String variableName) throws AFTException {

		boolean isArrayVar = false;
		String withoutArrayNotation = null;
		String checkValidName = null;
		String accessNotation = Constants.ARRAYACCESSNOTATIONCHAR;

		boolean containsArrayNotation = variableName.contains(accessNotation);

		// user has passed variable name with array access notation
		// lets remove the dot[.] chars from the name
		if (containsArrayNotation) {
			LOGGER.trace("Removing [" + accessNotation
					+ "] occurences for variable name from [" + variableName
					+ "]");
			int firstOccurence = variableName.indexOf(accessNotation);
			withoutArrayNotation = variableName.substring(0, firstOccurence);
			LOGGER.trace("Variable name after removing [" + accessNotation
					+ "] occurences is [" + withoutArrayNotation + "]");
		}

		if (containsArrayNotation) {
			checkValidName = withoutArrayNotation;
		} else {
			checkValidName = variableName;
		}
		Set<String> allKeys = storedArrayVariablesMap.keySet();
		for (String currentKey : allKeys) {
			if (currentKey.startsWith(Constants.DBARRAYTYPE)) {
				currentKey = currentKey.substring(Constants.DBARRAYTYPE
						.length());
			} else if (currentKey.startsWith(Constants.WEBTABLEARRAYTYPE)) {
				currentKey = currentKey.substring(Constants.WEBTABLEARRAYTYPE
						.length());
			} else if (currentKey.startsWith(Constants.EXCELARRAYTYPE)) {
				currentKey = currentKey.substring(Constants.EXCELARRAYTYPE
						.length());
			} else if (currentKey.startsWith(Constants.USERCREATEDARRAYTYPE)) {
				currentKey = currentKey
						.substring(Constants.USERCREATEDARRAYTYPE.length());
			}

			if (currentKey.equals(checkValidName.toLowerCase())) {
				isArrayVar = true;
				break;
			}
		}

		return isArrayVar;
	}

	/**
	 * This method returns value by reading from array variable hashmap.
	 * 
	 * @param variableName
	 *            variable name whose value is read from array hashmap
	 * @return the string
	 * @throws AFTException
	 */
	public String getArrayVariableValue(String variableName)
			throws AFTException {

		String[][] variableArray = null;
		int notationOccurence, rowIndex, columnIndex;
		String strRow, strColumn, tempVarName, variableValue, withoutArrayNotation;
		String strColumnName = null;
		String accessNotation = Constants.ARRAYACCESSNOTATIONCHAR;
		boolean isColumnNamePassed = false;
		boolean isDBArray = false;
		boolean isWebTableArray = false;
		boolean isExcelArray = false;
		boolean isUserCreatedArray = false;

		variableValue = "";
		rowIndex = 0;
		columnIndex = 0;
		boolean containsArrayNotation = variableName.contains(accessNotation);

		// if user passed dot notation, lets check if it is valid syntax
		if (containsArrayNotation) {
			int count = 0;
			for (int i = 0; i < variableName.length(); i++) {
				if (variableName.charAt(i) == accessNotation.toCharArray()[0]) {
					count++;
				}
			}
			// less than or more than 2 dot[.] chars is not valid
			if (count != 2) {
				LOGGER.error("Exception::" + "Invalid array notation passed!!!");
				throw new AFTException("Invalid array notation passed.");
			}
		}

		// get the stored array from the map
		// also set the type of array
		if (containsArrayNotation) {
			notationOccurence = variableName.indexOf(accessNotation);
			withoutArrayNotation = variableName.substring(0, notationOccurence);
		} else {
			withoutArrayNotation = variableName;
		}

		Set<String> allKeys = storedArrayVariablesMap.keySet();
		for (String currentKey : allKeys) {
			if (currentKey.contains(withoutArrayNotation.toLowerCase())) {
				variableArray = storedArrayVariablesMap.get(currentKey);
				if (currentKey.startsWith(Constants.DBARRAYTYPE)) {
					isDBArray = true;
				} else if (currentKey.startsWith(Constants.WEBTABLEARRAYTYPE)) {
					isWebTableArray = true;
				} else if (currentKey.startsWith(Constants.EXCELARRAYTYPE)) {
					isExcelArray = true;
				} else if (currentKey
						.startsWith(Constants.USERCREATEDARRAYTYPE)) {
					isUserCreatedArray = true;
				}
				break;
			}
		}

		// user has passed variable name with array access notation
		// lets get the row and column index
		if (containsArrayNotation) {
			notationOccurence = variableName.indexOf(accessNotation);
			withoutArrayNotation = variableName.substring(0, notationOccurence);
			tempVarName = variableName.substring(notationOccurence + 1,
					variableName.length());
			notationOccurence = tempVarName.indexOf(accessNotation);
			strColumn = tempVarName.substring(0, notationOccurence);
			strRow = tempVarName.substring(notationOccurence + 1,
					tempVarName.length());

			// if user passed userdefinedvariable for column index
			// then get the stored value from variable map
			if (storedVariablesMap.containsKey(strColumn)) {
				strColumn = storedVariablesMap.get(strColumn);
			}

			// if user passed a column name(DB resultset/Webtable array)
			// then set the column number as per column name
			for (int i = 0; i < variableArray[0].length; i++) {
				strColumnName = variableArray[0][i];
				if (strColumnName != null
						&& strColumnName.equalsIgnoreCase(strColumn)) {
					strColumn = String.valueOf(i);
					isColumnNamePassed = true;
					break;
				}
			}

			// if user passed userdefinedvariable for row index
			// then get the stored value from variable map
			if (storedVariablesMap.containsKey(strRow)) {
				strRow = storedVariablesMap.get(strRow);
			}

			try {
				if (isDBArray || isWebTableArray) {
					if (isColumnNamePassed) {
						LOGGER.debug("User passed column name as ["
								+ strColumnName + "]" + " and row number as ["
								+ strRow + "]");
						rowIndex = Integer.parseInt(strRow);
						columnIndex = Integer.parseInt(strColumn);
					} else {
						LOGGER.debug("User passed column number as ["
								+ strColumn + "]" + " and row number as ["
								+ strRow + "]");
						rowIndex = Integer.parseInt(strRow);
						columnIndex = Integer.parseInt(strColumn) - 1;
					}

				} else if (isExcelArray) {
					LOGGER.debug("User passed column number as [" + strColumn
							+ "]" + " and row number as [" + strRow + "]");
					rowIndex = Integer.parseInt(strRow);
					columnIndex = Integer.parseInt(strColumn);

					// TODO:: change as per user created array
				} else if (isUserCreatedArray) {
					LOGGER.debug("User passed column number as [" + strColumn
							+ "]" + " and row number as [" + strRow + "]");
					rowIndex = Integer.parseInt(strRow) - 1;
					columnIndex = Integer.parseInt(strColumn) - 1;
				}

				if ((!isDBArray && !isWebTableArray)
						&& (strColumn.equals("0") || strRow.equals("0"))) {
					LOGGER.error("Column/Row number cannot be less than 1.");
					return variableValue;
				}
			} catch (NumberFormatException nfe) {
				LOGGER.error("Invalid column/row number passed.");
				return variableValue;
			}
		} else {
			LOGGER.debug("User has not passed column/row number."
					+ " column=1/row=1 will be considered as default.");
			rowIndex = 1;
			columnIndex = 1;
		}

		String strWarnMsg = "Data beyond [" + Constants.MAXARRAYROWCOUNT + "x"
				+ Constants.MAXARRAYCOLUMNCOUNT + "] cells is not available...";

		if (rowIndex >= Constants.MAXARRAYROWCOUNT
				|| columnIndex >= Constants.MAXARRAYCOLUMNCOUNT) {
			LOGGER.warn(strWarnMsg);
			return variableValue;
		}

		try {
			variableValue = variableArray[rowIndex][columnIndex];
		} catch (Exception e) {
			LOGGER.error("No value found for given row/column");
			variableValue = "";
		}

		return variableValue;
	}

}