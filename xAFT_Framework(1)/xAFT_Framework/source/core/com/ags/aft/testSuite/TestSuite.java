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
 * Class: TestSuite
 * 
 * Purpose: This class store Testsuite data for reporting.
 */

package com.ags.aft.testSuite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.common.Util;

/**
 * Class to store Testsuite data.
 * 
 */
public class TestSuite {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TestSuite.class);

	private String idTestSuite;
	private String testSuiteName;
	private String category;
	private String browserName;
	private String url;
	private String executionEngine;
	private int passCount = 0;
	private int failCount = 0;
	private int totalCount = 0;
	private long executionTime;
	private String description;
	private List<TestScenario> testScenario;
	private boolean isExecuted = false;

	private int idReportTestSuite;

	/**
	 * Constructs a new TestSuite instance.
	 */
	public TestSuite() {
		LOGGER.trace("Creating new TestSuite object for reporting");
		testScenario = new ArrayList<TestScenario>();
	}

	/**
	 * @return the executionTime
	 */
	public long getExecutionTime() {
		return executionTime;
	}

	/**
	 * @param executionTime
	 *            the executionTime to set
	 */
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	/**
	 * Returns the name of the Testsuite.
	 * 
	 * @return testSuiteName String object with value as Testsuite name.
	 */
	public String getTestSuiteName() {
		String suiteName = this.testSuiteName;
		if (suiteName.indexOf('^') > 0) {
			String[] suiteNameArr = suiteName.split("\\^");
			suiteName = suiteNameArr[0];
		}
		return suiteName;
	}

	/**
	 * Sets the name of the Testsuite.
	 * 
	 * @param testSuiteName
	 *            String object with value as Testsuite name.
	 */
	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	/**
	 * @param browser
	 *            set browser name
	 */
	public void setBrowserName(String browser) {
		this.browserName = browser;
	}

	/**
	 * @return gets the browser name
	 */
	public String getBrowserName() {
		return browserName;
	}

	/**
	 * @param url
	 *            set application url
	 */
	public void seturl(String url) {
		this.url = url;
	}

	/**
	 * @return get application url
	 */
	public String geturl() {
		return url;
	}

	/**
	 * @return get test suite category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            set test suite category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the executionEngine
	 */
	public String getExecutionEngine() {
		return executionEngine;
	}

	/**
	 * @param executionEngine
	 *            the executionEngine to set
	 */
	public void setExecutionEngine(String executionEngine) {
		this.executionEngine = executionEngine;
	}

	/**
	 * @param ts
	 *            add a new test scenario for reporting results to this test
	 *            suite
	 */
	public void addTestScenario(TestScenario ts) {
		LOGGER.trace("Adding a new testSuite [" + ts.getBusinessScenarioId()
				+ "], description [" + ts.getBusinessScenarioDesc()
				+ "] for reporting");

		ts.setTestSuite(this);
		testScenario.add(ts);
	}

	/**
	 * Initialize Test Scenario Object
	 * 
	 */
	public void initTestScenarioObj() {
		LOGGER.debug("Initialize Test Scenarios object...");
		TestScenario ts = getTestScenario();
		ts.initTestScenariosObj();
		ts.setExecutionResult("FAIL");
		ts.setTestScenarioStartTime(Util.getInstance().getCurrentTime());
	}

	/**
	 * Returns ArrayList<TestScenario> objects of a Testsuite.
	 * 
	 * @return ArrayList<TestScenario>
	 */
	public List<TestScenario> getTestScenariosArr() {
		return testScenario;
	}

	/**
	 * @return latest test Sceanrio reporting object
	 */
	public TestScenario getTestScenario() {
		return testScenario.get(testScenario.size() - 1);
	}

	/**
	 * summarize results tp consolidated results and log to log file so that
	 * these could be used when generating report
	 */
	public void summarizeResults() {
		LOGGER.trace("Executing summarizeResults to compute total/pass/fail test scenarios counts");

		passCount = 0;
		failCount = 0;
		totalCount = 0;

		Iterator<TestScenario> testScenarioIter = testScenario.iterator();
		while (testScenarioIter.hasNext()) {
			TestScenario scenario = testScenarioIter.next();
			if (scenario != null) {
				if (scenario.getExecutionResult() != null
						&& scenario.getExecutionResult().equalsIgnoreCase(
								"Pass")) {
					passCount++;
				} else {
					failCount++;
				}
			}
			totalCount++;
		}

		LOGGER.info("--------------------------Executed ["
				+ ReportGenerator.getInstance().getLatestTestSuite()
						.getTotalCount()
				+ "]"
				+ ", Passed ["
				+ ReportGenerator.getInstance().getLatestTestSuite()
						.getPassCount()
				+ "]"
				+ ", Failed ["
				+ ReportGenerator.getInstance().getLatestTestSuite()
						.getFailCount() + "]--------------------------");
	}

	/**
	 * Returns total pass count
	 * 
	 * @return total pass scenarios count
	 */
	public int getPassCount() {
		return passCount;
	}

	/**
	 * Returns total fail count
	 * 
	 * @return total fail scenarios count
	 */
	public int getFailCount() {
		return failCount;
	}

	/**
	 * Returns total count(pass and fail)
	 * 
	 * @return total executed scenarios count
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @return the idTestSuite
	 */
	public String getIdTestSuite() {
		return idTestSuite;
	}

	/**
	 * @param idTestSuite
	 *            the idTestSuite to set
	 */
	public void setIdTestSuite(String idTestSuite) {
		this.idTestSuite = idTestSuite;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the idReportTestSuite
	 */
	public int getIdReportTestSuite() {
		return idReportTestSuite;
	}

	/**
	 * @param idReportTestSuite
	 *            the idReportTestSuite to set
	 */
	public void setIdReportTestSuite(int idReportTestSuite) {
		this.idReportTestSuite = idReportTestSuite;
	}
	/**
	 * @return the isExecuted
	 */
	public boolean isExecuted() {
		return isExecuted;
	}

	/**
	 * @param isExecuted the isExecuted to set
	 */
	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}
}
