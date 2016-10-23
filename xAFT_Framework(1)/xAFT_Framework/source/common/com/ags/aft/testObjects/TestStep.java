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

package com.ags.aft.testObjects;

import com.ags.aft.testObjects.TestCase;

// TODO: Auto-generated Javadoc
/**
 * Stores TestStep data.
 * 
 */
public class TestStep {
	
	/** The step type. */
	private String stepType;
	
	/** The etl object type. */
	private String etlObjectType;
	// basic test scenario properties
	/** The action. */
	private String action = null;
	
	/** The element name. */
	private String elementName;
	
	/** The element value. */
	private String elementValue;

	// test scenario execution data
	/** The sort id. */
	private String sortId = null;
	
	/** The action description. */
	private String actionDescription = null;
	
	/** The error message. */
	private String errorMessage = null;
	
	/** The image name. */
	private String imageName = "";
	
	/** The result. */
	private String result = null;
	
	/** The service request name. */
	private String serviceRequestName = "";
	
	/** The service response name. */
	private String serviceResponseName = "";
	
	/** The test step execution time. */
	private long testStepExecutionTime;

	// used for logging results to db
	/** The step id. */
	private String stepId;
	
	/** The ws response type. */
	private String wsResponseType;
	
	/** The start time. */
	private String startTime;
	
	/** The end time. */
	private String endTime;
	// test case object to which this test step belongs
	/** The test case. */
	private TestCase testCase;

	/**
	 * Gets the step id.
	 *
	 * @return the stepId
	 */
	public String getStepId() {
		return stepId;
	}

	/**
	 * Sets the step id.
	 *
	 * @param stepId the stepId to set
	 */
	public void setStepId(String stepId) {
		this.stepId = stepId;
	}

	/**
	 * Gets the step type.
	 *
	 * @return the stepType
	 */
	public String getStepType() {
		return stepType;
	}

	/**
	 * Sets the step type.
	 *
	 * @param stepType the stepType to set
	 */
	public void setStepType(String stepType) {
		this.stepType = stepType;
	}

	/**
	 * Gets the test case.
	 *
	 * @return the testCase
	 */
	public TestCase getTestCase() {
		return testCase;
	}

	/**
	 * Sets the test case.
	 *
	 * @param testCase the testCase to set
	 */
	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Gets the action description.
	 *
	 * @return the actionDescription
	 */
	public String getActionDescription() {
		return actionDescription;
	}

	/**
	 * Sets the action description.
	 *
	 * @param actionDescription the actionDescription to set
	 */
	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}

	/**
	 * Gets the error message.
	 *
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the error message.
	 *
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the image name.
	 *
	 * @return the imageName
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * Sets the image name.
	 *
	 * @param imageName the imageName to set
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 *
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Gets the service request name.
	 *
	 * @return the serviceRequestName
	 */
	public String getServiceRequestName() {
		return serviceRequestName;
	}

	/**
	 * Sets the service request name.
	 *
	 * @param serviceRequestName the serviceRequestName to set
	 */
	public void setServiceRequestName(String serviceRequestName) {
		this.serviceRequestName = serviceRequestName;
	}

	/**
	 * Gets the service response name.
	 *
	 * @return the serviceResponseName
	 */
	public String getServiceResponseName() {
		return serviceResponseName;
	}

	/**
	 * Sets the service response name.
	 *
	 * @param serviceResponseName the serviceResponseName to set
	 */
	public void setServiceResponseName(String serviceResponseName) {
		this.serviceResponseName = serviceResponseName;
	}

	/**
	 * Gets the sort id.
	 *
	 * @return the sortId
	 */
	public String getSortId() {
		return sortId;
	}

	/**
	 * Sets the sort id.
	 *
	 * @param sortId the sortId to set
	 */
	public void setSortId(String sortId) {
		this.sortId = sortId;
	}

	/**
	 * Gets the test step execution time.
	 *
	 * @return the testStepExecutionTime
	 */
	public long getTestStepExecutionTime() {
		return testStepExecutionTime;
	}

	/**
	 * Sets the test step execution time.
	 *
	 * @param testStepExecutionTime the testStepExecutionTime to set
	 */
	public void setTestStepExecutionTime(long testStepExecutionTime) {
		this.testStepExecutionTime = testStepExecutionTime;
	}

	/**
	 * Gets the element name.
	 *
	 * @return the elementName
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * Sets the element name.
	 *
	 * @param elementName the elementName to set
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Gets the element value.
	 *
	 * @return the elementValue
	 */
	public String getElementValue() {
		return elementValue;
	}

	/**
	 * Sets the element value.
	 *
	 * @param elementValue the elementValue to set
	 */
	public void setElementValue(String elementValue) {
		this.elementValue = elementValue;
	}

	/**
	 * Gets the ws response type.
	 *
	 * @return the wsResponseType
	 */
	public String getWsResponseType() {
		return wsResponseType;
	}

	/**
	 * Sets the ws response type.
	 *
	 * @param wsResponseType the wsResponseType to set
	 */
	public void setWsResponseType(String wsResponseType) {
		this.wsResponseType = wsResponseType;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets the etl object type.
	 *
	 * @return the etl object type
	 */
	public String getEtlObjectType() {
		return etlObjectType;
	}

	/**
	 * Sets the etl object type.
	 *
	 * @param etlObjectType the new etl object type
	 */
	public void setEtlObjectType(String etlObjectType) {
		this.etlObjectType = etlObjectType;
	}

}
