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
 * Class: RuntimeProperties
 * 
 * Purpose: This class contains utility methods to read command line arguments,
 * and set the values to thie class.
 */


package com.ags.aft.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.util.Helper;

/**
 * The Class RuntimeProperties.
 */
public final class RuntimeProperties {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(RuntimeProperties.class);
	/** The file command args. */
	private Map<String, String> fileCommandArgs = new HashMap<String, String>();
	
	// testBatchId constant.
	private String testBatchId;
	// testsetIds constant.
	private List<String> testSetIds;
	private String hostName;
	// user name constant.
	private String userName;
	// password constant.
	private String password;
	// db url constant.
	private String dbUrl;
	// project id constant.
	private String projectId;
	private String testExecutionId;

	/** The aft helper. */
	private static RuntimeProperties runtimeProperties;

	/**
	 * Instantiates a new aft helper.
	 */
	private RuntimeProperties() {
		super();
	}

	/**
	 * Gets the single instance of RuntimeProperties.
	 * 
	 * @return single instance of RuntimeProperties
	 */
	public static RuntimeProperties getInstance() {
		if (runtimeProperties == null) {
			runtimeProperties = new RuntimeProperties();
			LOGGER.trace("Creating instance of RuntimeProperties");
		}

		return runtimeProperties;
	}
	
	/**
	 * This method will read command line arguments.
	 * 
	 * @param args
	 *            the args
	 */
	public void readCommandLineArguments(String[] args) {
		String[] values = args[0].split("=");
		if (values[0].startsWith("/testbatchfile")) {
			Helper.getInstance().setFileSystemRequest(true);
			setFileSystemArguments(args);
		} else if (values[0].startsWith("Db")) {
			Helper.getInstance().setFileSystemRequest(false);
			setDbArguments(args);
		}
	}
	
	/**
	 * This method will set file system command line arguments.
	 * 
	 * @param args
	 *            the new file system arguments
	 */
	private void setFileSystemArguments(String[] args) {
		Map<String, String> commandArgs = new HashMap<String, String>();
		for (String arg : args) {
			String[] values = arg.split(":", 2);
			String value = values[0].substring(1, values[0].length());
			commandArgs.put(value, values[1]);
		}
		getFileCommandArgs().putAll(commandArgs);
	}

	/**
	 * This method will set DB command line arguments.
	 * 
	 * @param args
	 *            the new db arguments
	 */
	private void setDbArguments(String[] args) {
		Map<String, String> commandArgs = new HashMap<String, String>();
		String[] values;
		for (String value : args) {
			if (value.contains(":")) {
				if (value.contains("dbUrl")) {
					values = value.split(":", 2);
				} else {
					values = value.split(":");
				}
				commandArgs.put(values[0], values[1]);
			}
		}
		setDBValues(commandArgs);
	}
	
	/**
	 * This method will set DB command line arguments to DatabaseUtils
	 * parameters.
	 * 
	 * @param commandArgs
	 *            the command args
	 */
	private void setDBValues(Map<String, String> commandArgs) {
		List<String> testsetList = new ArrayList<String>();
		setDbUrl(commandArgs.get("dbUrl"));
		setUserName(commandArgs.get("userName"));
		setPassword(commandArgs.get("password"));

		setProjectId(commandArgs.get("project"));
		setTestBatchId(commandArgs.get("testBatch"));
		setTestExecutionId(commandArgs.get("executionId"));
		String[] testSets = commandArgs.get("testSets").split("#");
		for (String testsetId : testSets) {
			testsetList.add(testsetId);
		}
		setTestSetIds(testsetList);
		setHostName(commandArgs.get("hostName"));

	}
	
	/**
	 * Gets the file command args.
	 * 
	 * @return the fileCommandArgs
	 */
	public Map<String, String> getFileCommandArgs() {
		return fileCommandArgs;
	}

	/**
	 * Sets the file command args.
	 * 
	 * @param fileCommandArgs
	 *            the fileCommandArgs to set
	 */
	public void setFileCommandArgs(Map<String, String> fileCommandArgs) {
		this.fileCommandArgs = fileCommandArgs;
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
	 * @return the testBatchId
	 */
	public String getTestBatchId() {
		return testBatchId;
	}

	/**
	 * @param testBatchId
	 *            the testBatchId to set
	 */
	public void setTestBatchId(String testBatchId) {
		this.testBatchId = testBatchId;
	}
	
	/**
	 * @return the testSetIds
	 */
	public List<String> getTestSetIds() {
		return testSetIds;
	}

	/**
	 * @param testSetIds the testSetIds to set
	 */
	public void setTestSetIds(List<String> testSetIds) {
		this.testSetIds = testSetIds;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the dbUrl
	 */
	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * @param dbUrl
	 *            the dbUrl to set
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	/**
	 * @return the testExecutionId
	 */
	public String getTestExecutionId() {
		return testExecutionId;
	}

	/**
	 * @param testExecutionId the testExecutionId to set
	 */
	public void setTestExecutionId(String testExecutionId) {
		this.testExecutionId = testExecutionId;
	}
}
