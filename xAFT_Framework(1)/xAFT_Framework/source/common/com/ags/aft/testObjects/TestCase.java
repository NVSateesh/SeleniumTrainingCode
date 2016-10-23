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
 * Class: TestCase
 * 
 * Purpose: This class stores Testcase data for reporting.
 */

package com.ags.aft.testObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.ags.aft.testObjects.TestStep;

/**
 * Stores the Testcase data.
 * 
 */
public class TestCase {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TestCase.class);

	// basic test case properties
	private String testCaseId;
	private String testCaseDesc;
	private Map<String, TestStep> preSteps;
	private Map<String, TestStep> steps;
	private Map<String, TestStep> postSteps;

	// test steps associated with this test scenario
	private List<TestStep> testStep;

	// test scenario execution data
	private String sortId;
	private boolean isReusable;

	// test scenario to which this test case object belong
	private TestScenario testScenario = null;
	private long testCaseExecutionTime;


	/**
	 * Constructs a new TestCase instance.
	 */
	public TestCase() {
		LOGGER.trace("Creating new TestCase object for reporting");
		testStep = new ArrayList<TestStep>();
	}

	/**
	 * @return the testCaseId
	 */
	public String getTestCaseId() {
		return testCaseId;
	}

	/**
	 * @param testCaseId
	 *            the testCaseId to set
	 */
	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	/**
	 * @return the testCaseDesc
	 */
	public String getTestCaseDesc() {
		return testCaseDesc;
	}

	/**
	 * @param testCaseDesc
	 *            the testCaseDesc to set
	 */
	public void setTestCaseDesc(String testCaseDesc) {
		this.testCaseDesc = testCaseDesc;
	}

	/**
	 * @return the testStep
	 */
	public List<TestStep> getTestStepDetails() {
		return testStep;
	}

	/**
	 * @param ts
	 *            test step object
	 */
	public void addTestStep(TestStep ts) {
		LOGGER.trace("Adding a new testStep [" + ts.getStepId() + "]");
		ts.setTestCase(this);
		testStep.add(ts);
	}

	/**
	 * @param testStep
	 *            the testStep to set
	 */
	public void setTestStepDetails(List<TestStep> testStep) {
		this.testStep = testStep;
	}

	/**
	 * @return the testScenario
	 */
	public TestScenario getTestScenario() {
		return testScenario;
	}

	/**
	 * @param testScenario
	 *            the testScenario to set
	 */
	public void setTestScenario(TestScenario testScenario) {
		this.testScenario = testScenario;
	}

	/**
	 * @return the sortId
	 */
	public String getSortId() {
		return sortId;
	}

	/**
	 * @param sortId
	 *            the sortId to set
	 */
	public void setSortId(String sortId) {
		this.sortId = sortId;
	}

	/**
	 * @return the isReusable
	 */
	public boolean isReusable() {
		return isReusable;
	}

	/**
	 * @param isReusable
	 *            the isReusable to set
	 */
	public void setReusable(boolean isReusable) {
		this.isReusable = isReusable;
	}

	/**
	 * @return the preSteps
	 */
	public Map<String, TestStep> getPreSteps() {
		return preSteps;
	}

	/**
	 * @param preSteps
	 *            the preSteps to set
	 */
	public void setPreSteps(Map<String, TestStep> preSteps) {
		this.preSteps = preSteps;
	}

	/**
	 * @return the steps
	 */
	public Map<String, TestStep> getSteps() {
		return steps;
	}

	/**
	 * @param steps
	 *            the steps to set
	 */
	public void setSteps(Map<String, TestStep> steps) {
		this.steps = steps;
	}

	/**
	 * @return the postSteps
	 */
	public Map<String, TestStep> getPostSteps() {
		return postSteps;
	}

	/**
	 * @param postSteps
	 *            the postSteps to set
	 */
	public void setPostSteps(Map<String, TestStep> postSteps) {
		this.postSteps = postSteps;
	}
	/**
	 * @return the testCaseExecutionTime
	 */
	public long getTestCaseExecutionTime() {
		return testCaseExecutionTime;
	}

	/**
	 * @param testCaseExecutionTime the testCaseExecutionTime to set
	 */
	public void setTestCaseExecutionTime(long testCaseExecutionTime) {
		this.testCaseExecutionTime = testCaseExecutionTime;
	}
}
