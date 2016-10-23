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
 * Class: ExcelRead
 * 
 * Purpose: This class implements parsing the test suite and tests from excel.
 */

package com.ags.aft.imports;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.Util;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.util.Variable;

/**
 * The Class aftExcelRead.
 */
public class ExcelRead extends ImportData {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(com.ags.aft.imports.ExcelRead.class);

	/** The Constant SDF. */
	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"mm/dd/yyyy");

	/** The file path being operated upon **/
	private String filePath;

	/** The Functional Scenarios Sheet Name to be read **/
	private String functionalScenariosSheetName;

	/** The Reusable Test Scenarios Sheet Name to be read **/
	private String reusableScenariosSheetName;

	/** The workbook object. */
	private org.apache.poi.ss.usermodel.Workbook workbook;

	/** The step values delimiter character. */
	private String stepValuesDelimiterCharacter = "";

	/**
	 * Instantiates a new aft excel read.
	 * 
	 * @param filepath
	 *            the filepath
	 * @param functionalScenariosSheetName
	 *            functionalScenariosSheetName
	 * @param reusableScenariosSheetName
	 *            reusableScenariosSheetName
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 * @throws AFTException
	 *             the application exception
	 */
	public ExcelRead(String filepath, String functionalScenariosSheetName,
			String reusableScenariosSheetName) throws AFTException, IOException, InvalidFormatException
			 {

		if (filepath == null) {
			throw new IOException(
					"Test Suite file path need to be specified in config file");
		} else {
			LOGGER.trace("Creating workbook object...");

			workbook = WorkbookFactory.create(new FileInputStream(filepath));

			filePath = filepath;
			this.functionalScenariosSheetName = functionalScenariosSheetName;
			this.reusableScenariosSheetName = reusableScenariosSheetName;

		}

		LOGGER.trace("reading [Step_Values_Delimiter_Character] property...");
		stepValuesDelimiterCharacter = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.STEP_VALUES_DELIMITER);
		if (stepValuesDelimiterCharacter.length() != 1
				|| stepValuesDelimiterCharacter.toUpperCase().charAt(0) < 32
				|| stepValuesDelimiterCharacter.toUpperCase().charAt(0) > 127) {
			throw new AFTException(
					"Invalid step value delimiter value ["
							+ stepValuesDelimiterCharacter
							+ "] specified. Pls check the documentation and specify correct value for [Step_Values_Delimiter_Character] property in AFTConfig.properties file.");
		}
		// TODO:: Test with this value to make sure it works and also add pipe
		// to this escape character list
		if (stepValuesDelimiterCharacter.equals("^")) {
			stepValuesDelimiterCharacter = "\\^";
		}
		LOGGER.debug("[Step_Values_Delimiter_Character] property value is ["
				+ stepValuesDelimiterCharacter + "]");
	}

	/**
	 * This method will read sheet data
	 * 
	 * @param sheetName
	 *            the sheetName
	 * @param headerRowNumber
	 *            headerRowNumber
	 * @param expectedColCount
	 *            expectedColCount
	 * @param testStepsSheetName
	 *            testStepsSheetName
	 * @return scenarioList
	 * @throws AFTException
	 *             the application exception
	 */
	@Override
	public List<TestScenario> readSheetData(final String sheetName,
			int headerRowNumber, int expectedColCount,
			final String testStepsSheetName) throws AFTException {

		LOGGER.debug("Reading tests from sheet [" + sheetName + "]");

		List<TestScenario> scenarioList = new ArrayList<TestScenario>();
		List<String> errMsgs = null;

		try {
			Sheet sheet = validateSheet(sheetName, headerRowNumber,
					expectedColCount);

			for (int iRowCnt = headerRowNumber; iRowCnt <= sheet
					.getLastRowNum(); iRowCnt++) {
				Map<String, List<String>> data = new TreeMap<String, List<String>>();

				LOGGER.trace("reading row #" + (iRowCnt + 1) + " from sheet ["
						+ sheetName + "]");
				final Row row = sheet.getRow(iRowCnt);
				int iTotalColCount = row.getLastCellNum();

				if (iTotalColCount >= expectedColCount) {
					final Row headerRow = sheet.getRow(headerRowNumber - 1);
					int iColCount = headerRow.getLastCellNum();
					data = getRowData(iColCount, headerRow, iRowCnt, sheet,
							sheetName);

					if (data.size() > 0) {
						LOGGER.trace("Adding row #" + (iRowCnt + 1)
								+ " data to test steps map...");
						// Validating column headers and do proper exception
						// handling
						errMsgs = validateColumnHeadings(sheetName, data);
						TestScenario testScenario = DatabaseUtil
								.getInstance()
								.createTestScenario(
										data.get("Business Scenario Id").get(0),
										data.get(
												"Test Case Id / Requirement Id")
												.get(0),
										data.get(
												"Business Scenario Description")
												.get(0),
										data.get("Execution Flag").get(0),
										data.get("Category").get(0), "");
						Sheet testStepsSheet = validateSheet(
								testStepsSheetName, 2, 12);
						int istepRowCnt = 2;
						for (; istepRowCnt <= testStepsSheet.getLastRowNum(); istepRowCnt++) {
							Map<String, List<String>> stepData = new TreeMap<String, List<String>>();
							
							LOGGER.trace("reading row #" + (istepRowCnt + 1)
									+ " from sheet [" + testStepsSheetName
									+ "]");
							final Row stepRow = testStepsSheet
									.getRow(istepRowCnt);
							int iTotalStepColCount = stepRow.getLastCellNum();

							if (iTotalStepColCount >= 12) {
								final Row headerStepRow = testStepsSheet
										.getRow(2 - 1);
								int iColStepCount = headerStepRow
										.getLastCellNum();
								stepData = getRowData(iColStepCount,
										headerStepRow, istepRowCnt,
										testStepsSheet, testStepsSheetName);

								errMsgs = validateColumnHeadings(
										testStepsSheetName, stepData);

								if (testScenario.getBusinessScenarioId()
										.equalsIgnoreCase(
												stepData.get(
														"Business Scenario Id")
														.get(0))) {
									Map<String, TestStep> preSteps = DatabaseUtil
											.getInstance()
											.getTestSteps(
													stepData.get("Pre-Step Action"),
													stepData.get("Pre-Step ElementPath/Name (Visible)"),
													stepData.get("Pre-Step ElementValue"),
													"pre",
													testScenario
															.getBusinessScenarioId());

									Map<String, TestStep> steps = DatabaseUtil
											.getInstance()
											.getTestSteps(
													stepData.get("Step Action"),
													stepData.get("Step ElementPath/Name (Visible)"),
													stepData.get("Step ElementValue"),
													Constants.STEPPREFIX,
													testScenario
															.getBusinessScenarioId());

									Map<String, TestStep> postSteps = DatabaseUtil
											.getInstance()
											.getTestSteps(
													stepData.get("Post-Step Action"),
													stepData.get("Post-Step ElementPath/Name (Visible)"),
													stepData.get("Post-Step ElementValue"),
													"post",
													testScenario
															.getBusinessScenarioId());
									// create a test case object
									TestCase testCase = DatabaseUtil
											.getInstance()
											.createTestCase(
													stepData.get("Test Case Id")
															.get(0),
													stepData.get(
															"Test Case Description")
															.get(0), preSteps,
													steps, postSteps);

									testScenario.addTestCase(testCase);
								}

							} else {
								String errMsg = getErrorMessage(istepRowCnt,
										testStepsSheetName);
								LOGGER.error(errMsg);
								throw new AFTException(errMsg);
							}
						}
						scenarioList.add(testScenario);
					} else {
						LOGGER.info("No data found in Row #" + (iRowCnt + 1)
								+ " to add to data map...");
					}
				} else {
					String errMsg = getErrorMessage(iRowCnt, sheetName);
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			}
			if (errMsgs.size() > 0) {
				for (String errMsg : errMsgs) {
					LOGGER.error(errMsg);
				}
				LOGGER.error("System can not proceed further without above expected data");
				throw new AFTException("Expected columns data not available");
			}
			LOGGER.debug("Finished loading of tests from sheet [" + sheetName
					+ "]. Total rows read #" + scenarioList.size());
		} catch (IOException io) {
			LOGGER.error("Exception ::", io);
			throw new AFTException(io);
		} catch (Exception ne) {
			LOGGER.error("Exception ::", ne);
			throw new AFTException(ne);
		}

		return scenarioList;

	}

	/**
	 * This method will validate the sheet data
	 * 
	 * @param sheetName
	 *            the sheetName
	 * @param headerRowNumber
	 *            headerRowNumber
	 * @param expectedColCount
	 *            expectedColCount
	 * @return sheet
	 * @throws IOException
	 *             IOException
	 * @throws AFTException
	 *             the application exception
	 */
	private Sheet validateSheet(String sheetName, int headerRowNumber,
			int expectedColCount) throws IOException, AFTException {
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			LOGGER.error("Could not open sheet [" + sheetName
					+ "]. Looks like an invalid sheetname.");
			throw new IOException("Could not open sheet [" + sheetName
					+ "]. Looks like an invalid sheetname.");
		}

		LOGGER.trace("reading header row from sheet...");
		final Row headerRow = sheet.getRow(headerRowNumber - 1);
		int iColCount = headerRow.getLastCellNum();
		if (iColCount < expectedColCount) {
			String errMsg = "Invalid data row ["
					+ (headerRowNumber)
					+ "] found in sheet ["
					+ sheetName
					+ "]. The total number of columns in this row does not match the expected number of standard columns. Ignoring this row and moving forward loading the rest of test suite.";

			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		return sheet;
	}

	/**
	 * This method will gets the row data
	 * 
	 * @param iColCount
	 *            the iColCount
	 * @param headerRow
	 *            headerRow
	 * @param iRowCnt
	 *            iRowCnt
	 * @param sheet
	 *            sheet
	 * @param sheetName
	 *            sheetName
	 * @return data
	 */
	private Map<String, List<String>> getRowData(int iColCount, Row headerRow,
			int iRowCnt, Sheet sheet, String sheetName) {
		Map<String, List<String>> data = new TreeMap<String, List<String>>();
		for (int jColCnt = 0; jColCnt < iColCount; jColCnt++) {
			String colValue = null;

			LOGGER.trace("reading cell value for #" + (iRowCnt + 1) + ",#"
					+ (jColCnt + 1) + " from sheet [" + sheetName + "]");
			colValue = getCellValue(sheet, iRowCnt, jColCnt).trim();
			if (colValue.trim().length() > 0) {
				LOGGER.trace("Splitting value: [" + colValue + "]");

				try {
					String colHeader = headerRow.getCell(jColCnt)
							.getStringCellValue();
					List<String> colSplitValues = splitWithDelimiter(colValue);

					if (colHeader.length() > 0 && colSplitValues.size() > 0) {
						data.put(colHeader, colSplitValues);
					}
				} catch (Exception ne) {
					// /do nothing...
					LOGGER.debug("Invalid data in cell value for #"
							+ (iRowCnt + 1) + ",#" + (jColCnt + 1)
							+ " from sheet [" + sheetName + "]. Error ["
							+ ne.getLocalizedMessage() + "]. Ignoring it.");
				}
			} else {
				LOGGER.trace("Read value [" + colValue
						+ "] is empty. Not adding to the map.");

			}
		}

		return data;
	}

	/**
	 * This method will gets error message
	 * 
	 * @param iRowCnt
	 *            the iRowCnt
	 * @param sheetName
	 *            sheetName
	 * @throws AFTException
	 * @return string
	 */
	private String getErrorMessage(int iRowCnt, String sheetName)
			throws AFTException {
		String errMsg = "Invalid data row ["
				+ (iRowCnt + 1)
				+ "] found in sheet ["
				+ sheetName
				+ "]. The total number of columns in this row does not match the expected number of standard columns. Ignoring this row and moving forward loading the rest of test suite.";

		LOGGER.error(errMsg);
		throw new AFTException(errMsg);
	}

	/**
	 * Retrieves the value from a column in the sheet matching the column header
	 * and row number.
	 * 
	 * @param sheetName
	 *            the sheet name from which to retrieve value
	 * @param rowNum
	 *            the row number
	 * @param colHeader
	 *            the column header from which the value to retrieved
	 * @return the retrieved value string
	 * @throws AFTException
	 *             the application exception
	 */
	public String getColumnData(String sheetName, int rowNum, String colHeader)
			throws AFTException {
		String value = "";
		int colNum = 0;
		boolean isEndOfTestData = false;

		int headerRow = 0;
		LOGGER.debug("Reading test data from sheet [" + sheetName + "]");
		try {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				LOGGER.error("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
				throw new IOException("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
			}

			// Find the column number in the sheet matching the column header
			//
			String[] testDataColumnHeaders = null;
			int headerPosition = 0;
			int iColCnt;
			String testDataColumnDelimiter = ConfigProperties.getInstance()
					.getConfigProperty(
							ConfigProperties.TEST_DATA_COLUMN_DELIMITER);
			String escaptedTestDataColumnDelimiter = testDataColumnDelimiter;
			// If it is pipe, add a escape character so that split fn could work
			// correctly
			//
			if (testDataColumnDelimiter.equalsIgnoreCase("|")
					|| testDataColumnDelimiter.equalsIgnoreCase("^")) {
				escaptedTestDataColumnDelimiter = "\\"
						+ testDataColumnDelimiter;
			}

			colNum = -1;
			for (iColCnt = 0; iColCnt < sheet.getRow(0).getLastCellNum(); iColCnt++) {
				value = getCellValue(sheet, headerRow, iColCnt).trim()
						.toString();
				if (value.equalsIgnoreCase(colHeader)) {
					colNum = iColCnt;
					break;
				} else if (value.contains(testDataColumnDelimiter)) {
					testDataColumnHeaders = value.trim().split(
							escaptedTestDataColumnDelimiter);
					for (int j = 0; j < testDataColumnHeaders.length; j++) {
						if (testDataColumnHeaders[j].trim().equalsIgnoreCase(
								colHeader)) {
							colNum = iColCnt;
							headerPosition = j;
							break;
						}
					}
				}

				// If we have found a match for the column header, let us exit
				// the
				// for loop...
				if (colNum >= 0) {
					break;
				}
			}

			value = "";
			if (colNum == -1) {
				throw new AFTException("No match found for column header ["
						+ colHeader + "] in sheet [" + sheetName
						+ "]. Returning blank value");
			} else {
				if (rowNum <= sheet.getPhysicalNumberOfRows()) {
					try {
						value = getCellValue(sheet, rowNum, colNum);
					} catch (Exception ne) {
						LOGGER.info("Exception:: ["
								+ ne.getLocalizedMessage()
								+ "], row id ["
								+ rowNum
								+ "], columnid ["
								+ colNum
								+ "]. Looks like we reached end of test data file.");
						value = "";
						isEndOfTestData = true;
					}

					if (value.compareToIgnoreCase(stepValuesDelimiterCharacter
							+ Constants.EMPTYVALUE) == 0) {
						value = "";
					}
					if (value.contains(testDataColumnDelimiter)) {
						String[] testDataArray = value.trim().split(
								escaptedTestDataColumnDelimiter);
						if (testDataArray.length != testDataColumnHeaders.length) {
							isEndOfTestData = true;
							Variable.getInstance()
									.setVariableValue(
											Variable.getInstance()
													.generateSysVarName(
															SystemVariables.AFT_ISENDOFTESTDATA),
											true, "true");
							throw new AFTException(
									"Looks to be missing test data for column ["
											+ colHeader
											+ "]. Please check test data in row ["
											+ rowNum + "], column [" + colNum
											+ "] in sheet [" + sheetName
											+ "]. Returning blank value");
						} else {
							value = testDataArray[headerPosition].trim();
						}
					}
				} else {
					isEndOfTestData = true;
				}
			}

			// check if we have reached test of test data so that system
			// variable could be set properly...
			try {
				isEndOfTestData = isNextRowAvailable(sheetName, rowNum);
			} catch (IOException e) {
				// oops, an exception is thrown. Let is consider it as a fact
				// that we have reached end of test data and set the variable to
				// true so that system variable could be set properly...
				isEndOfTestData = true;
			}

			// Now let us see if isEndOfTestData = true due to one of the
			// conditions
			// and if yes, set the value of AFT_IsEndOfTestData = true.
			if (isEndOfTestData) {
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_ISENDOFTESTDATA), true,
						"true");
			} else {
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_ISENDOFTESTDATA), true,
						"false");
			}
		} catch (IOException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return value;
	}

	/**
	 * Split with delimeter.
	 * 
	 * @param value
	 *            the value
	 * @return the list
	 */
	private List<String> splitWithDelimiter(String value) {

		List<String> valueList = new ArrayList<String>();

		String[] splitValues = value.split(stepValuesDelimiterCharacter);
		for (int i = 0; i < splitValues.length; i++) {
			if ((splitValues[i] != null)
					&& (splitValues[i].trim().length() > 0)) {

				String trimmedValue = Util.getInstance().trimUnusedCharacters(
						splitValues[i]);

				LOGGER.trace("Storing value: [" + trimmedValue
						+ "] into the map");
				valueList.add(trimmedValue);
			}
		}
		return valueList;
	}

	/**
	 * Write to excel.
	 * 
	 * @param sheetName
	 *            the sheet name
	 * @param colHeader
	 *            the col header
	 * @param rowNum
	 *            the row num
	 * @param value
	 *            the value
	 * @throws AFTException
	 */
	public void writeToExcel(String sheetName, String colHeader, String rowNum,
			String value) throws AFTException {

		try {
			LOGGER.debug("Writing test data to sheet [" + sheetName + "]");
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				LOGGER.error("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
				throw new IOException("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
			}

			int colNum = -1;
			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				String cellValue = getCellValue(sheet, 0, i).trim().toString();
				if (cellValue.equalsIgnoreCase(colHeader)) {
					colNum = i;
					break;
				}
			}
			if (colNum == -1) {
				LOGGER.error("Could not find column [" + colHeader
						+ "]. Looks like an invalid column header.");
				throw new AFTException("Could not find column [" + colHeader
						+ "]. Looks like an invalid column header.");
			}

			int rowNumber = Integer.parseInt(rowNum);
			LOGGER.debug("Writing test data to row [" + rowNumber + "], Col ["
					+ colNum + "]");

			Row row1 = sheet.getRow((short) rowNumber);
			if (row1 == null) {
				row1 = sheet.createRow((short) rowNumber);
			}
			Cell cell = row1.createCell((short) colNum);

			LOGGER.debug("Value [" + value + "] is being written to sheet ["
					+ sheetName + "], row [" + rowNumber + "], Col [" + colNum
					+ "]");
			cell.setCellValue(value);

			FileOutputStream fileOut = new FileOutputStream(filePath);
			workbook.write(fileOut);

			fileOut.flush();
			fileOut.close();
			LOGGER.info("Value [" + value
					+ "] has been successfully written to sheet [" + sheetName
					+ "], row [" + rowNumber + "], Col [" + colNum + "]");
		} catch (IOException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * get the target handle.
	 * 
	 * @return workbook
	 */
	@Override
	public Workbook getTargetHandle() {
		return workbook;
	}

	/**
	 * get the row count.
	 * 
	 * @param targetHandle
	 *            targetHandle
	 * @param sheetName
	 *            sheetName
	 * @return rowCount
	 */
	@Override
	public int getRowCount(Workbook targetHandle, String sheetName) {
		int rowCount = 0;
		Sheet sheet = targetHandle.getSheet(sheetName);
		rowCount = sheet.getPhysicalNumberOfRows();
		return rowCount;
	}

	/**
	 * get the row count.
	 * 
	 * @param targetHandle
	 *            targetHandle
	 * @param sheetName
	 *            sheetName
	 * @param columnHeader
	 *            columnHeader
	 * @return rowNum
	 */
	@Override
	public int getRowCount(Workbook targetHandle, String sheetName,
			String columnHeader) throws AFTException {

		// Find the column number in the sheet matching the column header
		//
		String[] testDataColumnHeaders = null;
		int headerRow = 0;
		String value = "";
		int colNum = 0;
		int rowNum = 1;
		try {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				LOGGER.error("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
				throw new IOException("Could not open sheet [" + sheetName
						+ "]. Looks like an invalid sheetname.");
			}

			String testDataColumnDelimiter = ConfigProperties.getInstance()
					.getConfigProperty(
							ConfigProperties.TEST_DATA_COLUMN_DELIMITER);
			String escaptedTestDataColumnDelimiter = testDataColumnDelimiter;
			// If it is pipe, add a escape character so that split fn could work
			// correctly
			//
			if (testDataColumnDelimiter.equalsIgnoreCase("|")
					|| testDataColumnDelimiter.equalsIgnoreCase("^")) {
				escaptedTestDataColumnDelimiter = "\\"
						+ testDataColumnDelimiter;
			}

			if (columnHeader.length() > 0) {
				colNum = -1;
				for (int iColCnt = 0; iColCnt < sheet.getRow(0)
						.getLastCellNum(); iColCnt++) {
					value = getCellValue(sheet, headerRow, iColCnt).trim()
							.toString();
					if (value.equalsIgnoreCase(columnHeader)) {
						colNum = iColCnt;
						break;
					} else if (value.contains(testDataColumnDelimiter)) {
						testDataColumnHeaders = value.trim().split(
								escaptedTestDataColumnDelimiter);
						for (int j = 0; j < testDataColumnHeaders.length; j++) {
							if (testDataColumnHeaders[j].trim()
									.equalsIgnoreCase(columnHeader)) {
								colNum = iColCnt;
								break;
							}
						}
					}

					// If we have found a match for the column header, let us
					// exit
					// the
					// for loop...
					if (colNum >= 0) {
						break;
					}
				}
			} else {
				colNum = 0;
			}
			if (colNum == -1) {
				LOGGER.error("Could not find column [" + columnHeader
						+ "]. Looks like an invalid column header.");
				throw new IOException("Could not find column [" + columnHeader
						+ "]. Looks like an invalid column header.");
			}

			boolean bExit = false;
			do {
				try {
					value = getCellValue(sheet, rowNum, colNum);
				} catch (Exception ne) {
					value = null;
				}
				if (value != null
						&& value.compareToIgnoreCase(stepValuesDelimiterCharacter
								+ Constants.EMPTYVALUE) == 0) {
					value = "";
				}
				if (value == null || value.trim().length() <= 0) {
					rowNum -= 1;
					bExit = true;
				} else {
					rowNum++;
				}
			} while (!bExit);
		} catch (IOException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return rowNum;
	}

	/**
	 * get the column names.
	 * 
	 * @param targetHandle
	 *            targetHandle
	 * @param sheetName
	 *            sheetName
	 * @return headerNames
	 */
	@Override
	public List<String> getColumnNames(Workbook targetHandle, String sheetName) {
		List<String> headerNames = null;
		Sheet sheet = targetHandle.getSheet(sheetName);
		final Row headerRow = sheet.getRow(0);
		headerNames = new ArrayList<String>();
		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			final org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
			String cellValue = cell.getStringCellValue();
			headerNames.add(cellValue);
		}

		return headerNames;
	}

	/**
	 * get the cell value.
	 * 
	 * @param sheet
	 *            sheet
	 * @param row
	 *            row
	 * @param column
	 *            column
	 * @return value
	 */
	@Override
	public String getCellValue(Sheet sheet, int row, int column) {

		Row rowObj = sheet.getRow(row);
		Cell cell = rowObj.getCell(column);
		String value = stepValuesDelimiterCharacter + Constants.EMPTYVALUE;

		if (cell == null) {
			return value;
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_FORMULA:
			FormulaEvaluator eval = workbook.getCreationHelper()
					.createFormulaEvaluator();
			CellValue cellValue = eval.evaluate(cell);
			// Get the type of Formula field
			switch (cellValue.getCellType()) {

			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				LOGGER.trace("Found string value [" + value + "] in cell");
				break;
			case Cell.CELL_TYPE_NUMERIC:
				double v = cellValue.getNumberValue();
				String dateCellFormat = convertToJavaSupportFrmt(cell
						.getCellStyle().getDataFormatString());
				if (DateUtil.isCellDateFormatted(cell)) {
					Date dateValue = DateUtil.getJavaDate(v);
					DateFormat foramte = new SimpleDateFormat(dateCellFormat);
					value = foramte.format(dateValue);
					LOGGER.trace("Found date value [" + value + "] in cell");
				} else {
					value = String.valueOf(v);
					LOGGER.trace("Found numeric value [" + value + "] in cell");
				}
				break;
			default:
				LOGGER.debug("Unable to understand the format of the cell type at row #"
						+ (cell.getRowIndex() + 1)
						+ ", col #"
						+ (cell.getColumnIndex() + 1)
						+ ", type #"
						+ cell.getCellType() + ". Setting it to 'novalue'");
			}
			break;

		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				value = SDF.format(cell.getDateCellValue());
				LOGGER.trace("Found date value [" + value + "] in cell");
			} else {
				// We need to check if we are retrieving a integer or double
				// value to preseve formatting...
				int iValue = (int) cell.getNumericCellValue();
				double dValue = cell.getNumericCellValue();
				// to check if the value is integer or double, let us find the
				// diff
				if ((dValue - iValue) == 0) {
					// it is integer, let us convert integer to string
					value = String.valueOf(iValue);
				} else {
					// it is double, let us convert integer to string
					value = String.valueOf(dValue);
				}
				LOGGER.trace("Found numeric value [" + value + "] in cell");
			}
			break;
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			LOGGER.trace("Found string value [" + value + "] in cell");
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = cell.getStringCellValue();
			LOGGER.trace("Found boolean value [" + value + "] in cell");
			break;

		case Cell.CELL_TYPE_BLANK:
			break;

		case Cell.CELL_TYPE_ERROR:
		default:
			LOGGER.debug("Unable to understand the format of the cell type at row #"
					+ (cell.getRowIndex() + 1)
					+ ", col #"
					+ (cell.getColumnIndex() + 1)
					+ ", type #"
					+ cell.getCellType() + ". Setting it to 'novalue'");
		}
		return value;
	}

	/**
	 * This method returns the workbook object created for reading excel files.
	 * 
	 * @return workbook object
	 */
	public Workbook getWorkbook() {
		return workbook;
	}

	/**
	 * Retrieves the sheet names for the excel initialized workbook object.
	 * 
	 * @return List of sheet names in opened workbook
	 */
	public List<String> getSheetNames() {

		// Create a new List object to store sheet names
		List<String> sheetNames = new ArrayList<String>();

		int iSheetIndex = 0;
		while (true) {
			// retrieve the sheet name and store in the arraylist object
			// if the sheet does not exist at the index, it will throw
			// IllegalArgumentException on which we should exit the while loop
			try {
				String sheet = workbook.getSheetName(iSheetIndex++);
				sheetNames.add(sheet);
			} catch (IllegalArgumentException ie) {
				// exception throw. Looks like there are no more sheets in the
				// workbook
				break;
			}
		}

		return sheetNames;
	}

	/**
	 * Returns True if rowNum is the last row in the supplied sheet or else
	 * false.
	 * 
	 * @param sheetName
	 *            the sheet name from which to retrieve value
	 * @param rowNum
	 *            the row number
	 * @return True if rowNum is the last row in the supplied sheet or else
	 *         false
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isNextRowAvailable(String sheetName, int rowNum)
			throws IOException {
		boolean isEndOfTestData = false;

		LOGGER.debug("Trying to read test data sheet [" + sheetName + "]");
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			LOGGER.error("Could not open sheet [" + sheetName
					+ "]. Looks like an invalid sheetname.");
			throw new IOException("Could not open sheet [" + sheetName
					+ "]. Looks like an invalid sheetname.");
		}

		// Validate if we have reached the end of test data in the sheet by
		// trying to retrieve the next row value.
		//
		if (!isEndOfTestData) {
			if ((rowNum) <= sheet.getPhysicalNumberOfRows()) {
				try {
					getCellValue(sheet, rowNum, 0);
				} catch (Exception ne) {
					isEndOfTestData = true;
				}
			} else {
				isEndOfTestData = true;
			}
		}

		return isEndOfTestData;
	}

	/**
	 * This method gives proper java supported format for date
	 * 
	 * @param dateCellFormat
	 *            the dateCellFormat
	 * @return String
	 */
	private String convertToJavaSupportFrmt(String dateCellFormat) {
		String dataCellValue = dateCellFormat;
		dataCellValue = dataCellValue.replace("\\", "").replace('D', 'd')
				.replace('Y', 'y').replace("ddd", "EEE")
				.replace("dddd", "EEEE");
		return dataCellValue;
	}

	/**
	 * This is method validate all column headings of test suites
	 * 
	 * @param sheetName
	 *            sheetName
	 * @param testData
	 *            testData
	 * @return List
	 */
	private List<String> validateColumnHeadings(String sheetName,
			Map<String, List<String>> testData) {
		LOGGER.trace("Validating test data sheet [" + sheetName
				+ "] column headings");
		List<String> errMsg = new ArrayList<String>();
		if (testData != null && testData.size() > 0) {
			if (sheetName.equalsIgnoreCase(functionalScenariosSheetName)
					|| sheetName.equalsIgnoreCase(reusableScenariosSheetName)) {
				for (String columnHeading : Constants.TESTSCENARIOCOLUMNHEADINGS) {
					if (!testData.containsKey(columnHeading)) {
						errMsg.add("Expected column [" + columnHeading
								+ "] is not available");
					}
				}
			} else {
				for (String columnHeading : Constants.TESTSTEPCOLUMNHEADINGS) {
					if (!testData.containsKey(columnHeading)) {
						errMsg.add("Expected column [" + columnHeading
								+ "] is not available");
					}
				}
			}
		}
		return errMsg;
	}
}