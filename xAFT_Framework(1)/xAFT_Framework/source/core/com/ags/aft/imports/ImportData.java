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
 * Class: TestData
 * 
 * Purpose: This class is an abstract implementation to read test suite.
 */

package com.ags.aft.imports;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ags.aft.exception.AFTException;
import com.ags.aft.testObjects.TestScenario;

/**
 * The Class TestData.
 */
public abstract class ImportData {

	/**
	 * Gets the target handle.
	 * 
	 * @return the target handle
	 */
	public abstract Workbook getTargetHandle();

	/**
	 * Read the data from opened workflow from the sheetname, starting with the
	 * header row number passed as parameter.
	 * 
	 * @param sheetName
	 *            the sheet name
	 * 
	 * @param headerRowNumber
	 *            The header row number from which to start reading the excel
	 * @param expectedColCount
	 *            expectedColCount
	 * @param testStepsSheetName
	 *            the sheet name
	 * 
	 * 
	 * @return the list
	 * 
	 * @throws AFTException
	 *             the application specific exception
	 */
	public abstract List<TestScenario> readSheetData(final String sheetName,
			int headerRowNumber, int expectedColCount,
			final String testStepsSheetName) throws AFTException;

	/**
	 * Gets the row count.
	 * 
	 * @param targetHandle
	 *            the workbook handle
	 * @param sheetName
	 *            the sheet name for which row count is to be returned
	 * @return the row count
	 */
	public abstract int getRowCount(Workbook targetHandle, String sheetName);

	/**
	 * Gets the row count for the given sheetname and columnheader
	 * 
	 * @param targetHandle
	 *            the workbook handle
	 * @param sheetName
	 *            the sheet name for which row count is to be returned
	 * 
	 * @param columnHeader
	 *            column header for which row count is to be returned
	 * 
	 * @return the row count
	 * 
	 * @throws AFTException
	 */
	public abstract int getRowCount(Workbook targetHandle, String sheetName,
			String columnHeader) throws AFTException;

	/**
	 * Gets the column names.
	 * 
	 * @param targetHandle
	 *            the workbook handle
	 * @param sheetName
	 *            the sheet name
	 * @return the column names
	 */
	public abstract List<String> getColumnNames(Workbook targetHandle,
			String sheetName);

	/**
	 * Gets the cell value.
	 * @param sheet
	 *          sheet
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return the cell value
	 */
	public abstract String getCellValue(Sheet sheet, int row, int column);
}
