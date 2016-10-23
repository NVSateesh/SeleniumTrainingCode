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
 * Class: ScreenShot
 * 
 * Purpose: This class contains setters and getters for ScreenShot.
 */
package com.ags.enlace.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ScreenShot.
 */
@XmlRootElement
public class ScreenShot {
	
	private String projectId;
	private String testSuiteId;
	private String testSuiteName;
	private String imageDataString;
	private List<ImagePath> imagePaths;
	private String testScenarioId;
	private String testScenarioDescription;
	private String path;
	private String responseType;

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the imagePaths
	 */
	public List<ImagePath> getImagePaths() {
		return imagePaths;
	}
	/**
	 * @param imagePaths the imagePaths to set
	 */
	public void setImagePaths(List<ImagePath> imagePaths) {
		this.imagePaths = imagePaths;
	}

	/**
	 * @return the imageDataString
	 */
	public String getImageDataString() {
		return imageDataString;
	}
	/**
	 * @param imageDataString the imageDataString to set
	 */
	public void setImageDataString(String imageDataString) {
		this.imageDataString = imageDataString;
	}
	/**
	 * @return the testSuiteId
	 */
	public String getTestSuiteId() {
		return testSuiteId;
	}
	/**
	 * @param testSuiteId the testSuiteId to set
	 */
	public void setTestSuiteId(String testSuiteId) {
		this.testSuiteId = testSuiteId;
	}
	/**
	 * @return the testSuiteName
	 */
	public String getTestSuiteName() {
		return testSuiteName;
	}
	/**
	 * @param testSuiteName the testSuiteName to set
	 */
	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
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
	 * @return the testScenarioId
	 */
	public String getTestScenarioId() {
		return testScenarioId;
	}
	/**
	 * @param testScenarioId the testScenarioId to set
	 */
	public void setTestScenarioId(String testScenarioId) {
		this.testScenarioId = testScenarioId;
	}
	/**
	 * @return the testScenarioDescription
	 */
	public String getTestScenarioDescription() {
		return testScenarioDescription;
	}
	/**
	 * @param testScenarioDescription the testScenarioDescription to set
	 */
	public void setTestScenarioDescription(String testScenarioDescription) {
		this.testScenarioDescription = testScenarioDescription;
	}
	/**
	 * @return the responseType
	 */
	public String getResponseType() {
		return responseType;
	}
	/**
	 * @param responseType the responseType to set
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
}
