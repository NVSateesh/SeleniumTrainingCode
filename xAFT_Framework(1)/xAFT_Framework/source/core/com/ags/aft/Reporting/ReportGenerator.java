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
 * Class: Generator
 * 
 * Purpose: This is utility class to generate reports for the Testsuite result
 */

package com.ags.aft.Reporting;

import java.io.File;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.testObjects.TestSuite;
import com.ags.aft.util.Helper;

/**
 * Utility class to generate reports for the Testsuite result.
 * 
 */
public final class ReportGenerator {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ReportGenerator.class);

	private static ReportGenerator reportGenerator = null;
	private List<TestSuite> testSuite = null;
	private java.util.Date startTime = null;
	private String endTime = null;
	private String startDate = null;
	private String endDate = null;
	private int passCount = 0;
	private int failCount = 0;
	private int totalCount = 0;
	private String category = null;
	private String fileName = null;
	private String resultFileName = null;
	private static final String PASS = "PASS";
	private static final String FAIL = "FAIL";
	private boolean isEndOfTestSuite = false;

	/**
	 * Constructs a new ReportGenerator instance.
	 */
	private ReportGenerator() {
		LOGGER.debug("Creating new ReportGenerator object for reporting");
		testSuite = new ArrayList<TestSuite>();
	}

	/**
	 * Method to return ReportGenerator singleton instance.
	 * 
	 * @return ReportGenerator instance.
	 */
	public static ReportGenerator getInstance() {
		if (reportGenerator == null) {
			reportGenerator = new ReportGenerator();
		}
		return reportGenerator;
	}

	/**
	 * Gets ArrayList of Testsuite objects.
	 * 
	 * @return ArrayList<TestSuite> objects.
	 */
	public List<TestSuite> getSuiteResult() {
		return testSuite;
	}

	/**
	 * @return the startTime
	 */
	public java.util.Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(java.util.Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the report fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the report fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Generates report in XML format for the ArrayList<TestSuite> objects.
	 * 
	 * @throws ApplicationException
	 */
	public void generateXmlReport() throws AFTException {
		LOGGER.debug("Generating test result xml file");
		try {
			if ((testSuite != null) && (testSuite.size() > 0)) {
				LOGGER.trace("Creating a new XMLWriter object for creating test result xml file");
				boolean isFileSystemRequest = Helper.getInstance()
						.isFileSystemRequest();
				DateFormat dateFormat = new SimpleDateFormat(
						Constants.DATEFORMAT);
				String date = dateFormat.format(startTime);
				setStartDate(date);
				setEndDate(endTime);
				Writer writer = new Writer(testSuite, date, endTime);
				if (!isFileSystemRequest) {
					setEndTime(null);
				}
				if (isFileSystemRequest) {
					writer.writeToFile();
					LOGGER.trace("Saving test result to test result xml file");
					writer.saveTestResult(startTime, InetAddress.getLocalHost()
							.getHostName());
					LOGGER.debug("Successfully generated test result xml file for this run");
					setResultFileName(writer.getResultFileName());
				} else {
					writer.writeToDB(isEndOfTestSuite);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * @return Pass count
	 */
	public int getPassCount() {
		return passCount;
	}

	/**
	 * 
	 * @return Fail count
	 */
	public int getFailCount() {
		return failCount;
	}

	/**
	 * 
	 * @return Total (Pass and Fail) count
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Add TestSuite object to testSuite array.
	 * 
	 * @param ts
	 *            testSuite
	 */
	public void addTestSuite(TestSuite ts) {
		LOGGER.debug("Adding a new testSuite [" + ts.getTestSuiteName()
				+ "] for reporting");
		testSuite.add(ts);
	}

	/**
	 * 
	 * @return Latest TestSuite object
	 */
	public TestSuite getLatestTestSuite() {
		int size = testSuite.size();
		TestSuite ts = null;
		if (size > 0) {
			ts = testSuite.get(size - 1);
		}
		return ts;
	}

	/**
	 * It updates the total count of results executed.
	 */
	public void summarizeResults() {
		LOGGER.trace("Summarizing test results");
		
		Iterator<TestSuite> suiteIterator = testSuite.iterator();
		while (suiteIterator.hasNext()) {
			TestSuite suite = suiteIterator.next();
			List<TestScenario> testcases = suite.getTestScenariosArr();
			Iterator<TestScenario> testcaseIter = testcases.iterator();
			while (testcaseIter.hasNext()) {
				TestScenario testcase = testcaseIter.next();
				if (testcase != null) {
					if (testcase.getExecutionResult() != null
							&& testcase.getExecutionResult().equalsIgnoreCase(
									"Pass")) {
						passCount++;
					} else {
						failCount++;
					}
				}
				totalCount++;
			}
		}

		LOGGER.info("");
		LOGGER.info("--------------Total Executed ["
				+ ReportGenerator.getInstance().getTotalCount() + "]"
				+ ", Total Passed ["
				+ ReportGenerator.getInstance().getPassCount() + "]"
				+ ", Total Failed ["
				+ ReportGenerator.getInstance().getFailCount()
				+ "]--------------");
	}

	/**
	 * This method will create new test step object with the given values and
	 * will be added to test case.
	 * 
	 * @param actionName
	 *            actionName
	 * @param elementName
	 *            elementName
	 * @param elementValue
	 *            elementValue
	 * @param actionDescription
	 *            actionDescription
	 * @param executionResult
	 *            executionResult
	 * @param executionEvidence
	 *            executionEvidence
	 */
	public static void reportResult(String actionName, String elementName,
			String elementValue, String actionDescription,
			String executionResult, String executionEvidence) {

		TestScenario testScenario = ReportGenerator.getInstance()
				.getLatestTestSuite().getTestScenario();

		// Construct TestStep reporting object
		TestStep testStep = new TestStep();
		// Init Test step reporting object
		testStep.setAction(actionName);
		testStep.setElementName(elementName);
		testStep.setElementValue(elementValue);
		if (ReportGenerator.PASS.compareToIgnoreCase(executionResult) == 0) {
			testStep.setResult(executionResult);
		} else {
			testStep.setResult(ReportGenerator.FAIL);
			testScenario.setExecutionResult(ReportGenerator.FAIL);
		}
		List<TestStep> testCaseDetails = testScenario.getTestCase()
				.getTestStepDetails();
		TestStep step = testCaseDetails.get(testCaseDetails.size() - 1);
		testStep.setSortId(step.getSortId());
		File file = new File(executionEvidence);
		testStep.setImageName(file.getPath());
		testStep.setActionDescription(actionDescription);

		// add to report generator instance
		testScenario.getTestCase().addTestStep(testStep);
	}

	/**
	 * @return the resultFileName
	 */
	public String getResultFileName() {
		return resultFileName;
	}

	/**
	 * @param resultFileName
	 *            the resultFileName to set
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	/**
	 * @param isEndOfTestSuite the isEndOfTestSuite to set
	 */
	public void setEndOfTestSuite(boolean isEndOfTestSuite) {
		this.isEndOfTestSuite = isEndOfTestSuite;
	}
}
