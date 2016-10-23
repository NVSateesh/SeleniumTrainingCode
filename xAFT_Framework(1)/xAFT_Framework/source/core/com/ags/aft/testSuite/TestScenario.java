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
 * Class: TestScenario
 * 
 * Purpose: This class stores TestCase data for reporting.
 */

package com.ags.aft.testSuite;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Stores the TestScenario data.
 * 
 */
public class TestScenario {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TestScenario.class);

	// basic test scenario properties
	private String businessScenarioId;
	private String businessScenarioDesc;
	private String testScenarioRequirementId;

	// used for execution of this test scenario
	private String category;
	private String executionFlag;

	// test cases associated with this test scenario
	private List<TestCase> testCase;

	// test scenario execution data
	private String testScenarioStartTime;
	private String executionResult;
	private long executionTime;

	// used for logging results to db
	private String idTestScenario;
	private boolean isCommitted = false;

	// test suite object to which this test scenario belongs
	private TestSuite testSuite = null;

	/**
	 * Constructs a new TestScenario instance.
	 */
	public TestScenario() {
		LOGGER.trace("Creating new TestScenario object");
		testCase = new ArrayList<TestCase>();
	}

	/**
	 * Initializes Test cases object at the end of a test case execution
	 */
	public void initTestScenariosObj() {
		LOGGER.debug("Initialize Test Cases object...");
		testCase.clear();
		testCase = new ArrayList<TestCase>();
	}

	/**
	 * @param tc
	 *            the testCase to set
	 */
	public void addTestCase(TestCase tc) {
		LOGGER.trace("Adding a new Test CaseDetails [" + tc.getTestCaseId()
				+ "], description [" + tc.getTestCaseDesc() + "] for reporting");
		tc.setTestScenario(this);
		testCase.add(tc);
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
	 * @return the idTestScenario
	 */
	public String getIdTestScenario() {
		return idTestScenario;
	}

	/**
	 * @param idTestScenario
	 *            the idTestScenario to set
	 */
	public void setIdTestScenario(String idTestScenario) {
		this.idTestScenario = idTestScenario;
	}

	/**
	 * @return the businessScenarioId
	 */
	public String getBusinessScenarioId() {
		return businessScenarioId;
	}

	/**
	 * @param businessScenarioId
	 *            the businessScenarioId to set
	 */
	public void setBusinessScenarioId(String businessScenarioId) {
		this.businessScenarioId = businessScenarioId;
	}

	/**
	 * @return the businessScenarioDesc
	 */
	public String getBusinessScenarioDesc() {
		return businessScenarioDesc;
	}

	/**
	 * @param businessScenarioDesc
	 *            the businessScenarioDesc to set
	 */
	public void setBusinessScenarioDesc(String businessScenarioDesc) {
		this.businessScenarioDesc = businessScenarioDesc;
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
	 * @return the executionFlag
	 */
	public String getExecutionFlag() {
		return executionFlag;
	}

	/**
	 * @param executionFlag
	 *            the executionFlag to set
	 */
	public void setExecutionFlag(String executionFlag) {
		this.executionFlag = executionFlag;
	}

	/**
	 * @return the testScenarioRequirementId
	 */
	public String getTestScenarioRequirementId() {
		return testScenarioRequirementId;
	}

	/**
	 * @param testScenarioRequirementId
	 *            the testScenarioRequirementId to set
	 */
	public void setTestScenarioRequirementId(String testScenarioRequirementId) {
		this.testScenarioRequirementId = testScenarioRequirementId;
	}

	/**
	 * @return the testCase
	 */
	public List<TestCase> getTestCaseDetails() {
		return testCase;
	}

	/**
	 * @param testCaseDetails
	 *            testCaseDetails
	 */
	public void setTestCaseDetails(List<TestCase> testCaseDetails) {
		this.testCase = testCaseDetails;
	}

	/**
	 * @param tcStartTime
	 *            test scenario execution start time
	 */
	public void setTestScenarioStartTime(String tcStartTime) {
		this.testScenarioStartTime = tcStartTime;
	}

	/**
	 * @return test scenario execution start time
	 */
	public String getTestScenarioStartTime() {
		return testScenarioStartTime;
	}

	/**
	 * @return latest test case reporting object
	 */
	public TestCase getTestCase() {
		return testCase.get(testCase.size() - 1);
	}

	/**
	 * Returns Test scenario Result.
	 * 
	 * @return the result
	 */
	public String getExecutionResult() {
		return executionResult;
	}

	/**
	 * Sets Test scenario result.
	 * 
	 * @param result
	 *            the result to set
	 */
	public void setExecutionResult(String result) {
		this.executionResult = result;
	}

	/**
	 * @return the isCommitted
	 */
	public boolean isCommitted() {
		return isCommitted;
	}

	/**
	 * @param isCommitted
	 *            the isCommitted to set
	 */
	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	/**
	 * @return the testSuite
	 */
	public TestSuite getTestSuite() {
		return testSuite;
	}

	/**
	 * @param testSuite
	 *            the testSuite to set
	 */
	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

}