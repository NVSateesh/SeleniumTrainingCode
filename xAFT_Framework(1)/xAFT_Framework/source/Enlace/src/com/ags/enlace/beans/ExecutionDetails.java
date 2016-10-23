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
 * Class: ExecutionDetails
 * 
 * Purpose: This class contains setters and getters for ExecutionDetails.
 */
package com.ags.enlace.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ExecutionDetails.
 */
@XmlRootElement
public class ExecutionDetails {
	
	private String executionId;
	private String projectId;
	private String testBatchId;
	private String executionStatus;
	private int executionOrder;
	private String testSetId;
	private String executionTime;
	private String hostName;
	private String hostIp;
	private String dbUrl;
	private String dbUserName;
	private String dbPassword;
	private boolean isInserted;
	
	/**
	 * @return the isInserted
	 */
	public boolean isInserted() {
		return isInserted;
	}
	/**
	 * @param isInserted the isInserted to set
	 */
	public void setInserted(boolean isInserted) {
		this.isInserted = isInserted;
	}
	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}
	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	/**
	 * @return the testBatchId
	 */
	public String getTestBatchId() {
		return testBatchId;
	}
	/**
	 * @param testBatchId the testBatchId to set
	 */
	public void setTestBatchId(String testBatchId) {
		this.testBatchId = testBatchId;
	}
	/**
	 * @return the executionStatus
	 */
	public String getExecutionStatus() {
		return executionStatus;
	}
	/**
	 * @param executionStatus the executionStatus to set
	 */
	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}
	/**
	 * @return the executionOrder
	 */
	public int getExecutionOrder() {
		return executionOrder;
	}
	/**
	 * @param executionOrder the executionOrder to set
	 */
	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}
	/**
	 * @return the testSetId
	 */
	public String getTestSetId() {
		return testSetId;
	}
	/**
	 * @param testSetId the testSetId to set
	 */
	public void setTestSetId(String testSetId) {
		this.testSetId = testSetId;
	}
	/**
	 * @return the executionTime
	 */
	public String getExecutionTime() {
		return executionTime;
	}
	/**
	 * @param executionTime the executionTime to set
	 */
	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	/**
	 * @return the hostIp
	 */
	public String getHostIp() {
		return hostIp;
	}
	/**
	 * @param hostIp the hostIp to set
	 */
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	/**
	 * @return the executionId
	 */
	public String getExecutionId() {
		return executionId;
	}
	/**
	 * @param executionId the executionId to set
	 */
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}
	/**
	 * @return the dbUrl
	 */
	public String getDbUrl() {
		return dbUrl;
	}
	/**
	 * @param dbUrl the dbUrl to set
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	/**
	 * @return the dbUserName
	 */
	public String getDbUserName() {
		return dbUserName;
	}
	/**
	 * @param dbUserName the dbUserName to set
	 */
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}
	/**
	 * @return the dbPassword
	 */
	public String getDbPassword() {
		return dbPassword;
	}
	/**
	 * @param dbPassword the dbPassword to set
	 */
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
}
