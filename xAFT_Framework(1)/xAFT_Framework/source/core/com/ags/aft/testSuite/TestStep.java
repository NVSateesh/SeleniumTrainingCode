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
 * Class: TestStep
 * 
 * Purpose: This class stores TestStep data for reporting
 */

package com.ags.aft.testSuite;

/**
 * Stores TestStep data.
 * 
 */
public class TestStep {
	private String stepType;

	// basic test scenario properties
	private String action = null;
	private String elementName;
	private String elementValue;

	// test scenario execution data
	private String sortId = null;
	private String actionDescription = null;
	private String errorMessage = null;
	private String imageName = "";
	private String result = null;
	private String serviceRequestName = "";
	private String serviceResponseName = "";
	private long testStepExecutionTime;

	// used for logging results to db
	private String stepId;
	private String wsResponseType;
	
	// test case object to which this test step belongs
	private TestCase testCase;
	
	/**
	 * @return the stepId
	 */
	public String getStepId() {
		return stepId;
	}

	/**
	 * @param stepId
	 *            the stepId to set
	 */
	public void setStepId(String stepId) {
		this.stepId = stepId;
	}

	/**
	 * @return the stepType
	 */
	public String getStepType() {
		return stepType;
	}

	/**
	 * @param stepType
	 *            the stepType to set
	 */
	public void setStepType(String stepType) {
		this.stepType = stepType;
	}

	/**
	 * @return the testCase
	 */
	public TestCase getTestCase() {
		return testCase;
	}

	/**
	 * @param testCase
	 *            the testCase to set
	 */
	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the actionDescription
	 */
	public String getActionDescription() {
		return actionDescription;
	}

	/**
	 * @param actionDescription
	 *            the actionDescription to set
	 */
	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the imageName
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * @param imageName
	 *            the imageName to set
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the serviceRequestName
	 */
	public String getServiceRequestName() {
		return serviceRequestName;
	}

	/**
	 * @param serviceRequestName
	 *            the serviceRequestName to set
	 */
	public void setServiceRequestName(String serviceRequestName) {
		this.serviceRequestName = serviceRequestName;
	}

	/**
	 * @return the serviceResponseName
	 */
	public String getServiceResponseName() {
		return serviceResponseName;
	}

	/**
	 * @param serviceResponseName
	 *            the serviceResponseName to set
	 */
	public void setServiceResponseName(String serviceResponseName) {
		this.serviceResponseName = serviceResponseName;
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
	 * @return the testStepExecutionTime
	 */
	public long getTestStepExecutionTime() {
		return testStepExecutionTime;
	}

	/**
	 * @param testStepExecutionTime
	 *            the testStepExecutionTime to set
	 */
	public void setTestStepExecutionTime(long testStepExecutionTime) {
		this.testStepExecutionTime = testStepExecutionTime;
	}

	/**
	 * @return the elementName
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * @param elementName
	 *            the elementName to set
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * @return the elementValue
	 */
	public String getElementValue() {
		return elementValue;
	}

	/**
	 * @param elementValue
	 *            the elementValue to set
	 */
	public void setElementValue(String elementValue) {
		this.elementValue = elementValue;
	}

	/**
	 * @return the wsResponseType
	 */
	public String getWsResponseType() {
		return wsResponseType;
	}

	/**
	 * @param wsResponseType the wsResponseType to set
	 */
	public void setWsResponseType(String wsResponseType) {
		this.wsResponseType = wsResponseType;
	}
}
