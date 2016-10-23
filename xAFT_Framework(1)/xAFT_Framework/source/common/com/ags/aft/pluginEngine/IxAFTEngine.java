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
 * Interface: IxAFTEngine
 * 
 * Purpose: This class implements common interface for implementing Common
 * Interface for execution engines
 */

package com.ags.aft.pluginEngine;

import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;

/**
 * Inteface for implementing a execution engine
 * 
 */
public interface IxAFTEngine {

	/**
	 * Initializes the execution engine
	 * 
	 * @param rootPath
	 *            root path from where system is executing
	 * @param appURL
	 *            application url or path to execute
	 * @param appName
	 *            Name of the application being executed
	 * @param browserName
	 *            Browser on which to execute
	 * @param oRFilePath
	 *            path of the OR file
	 * @param  isFileSystemRequest
	 * 				isFileSystemRequest
	 * @throws AFTException
	 *             Exception
	 */
	void initialize(String rootPath, String appURL, String appName,
			String browserName, String oRFilePath, boolean isFileSystemRequest) throws AFTException;

	/**
	 * Tear down execution engine
	 * 
	 * @throws AFTException
	 */
	void tearDown() throws AFTException;

	/**
	 * Execute an action on the AUT UI
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @param action
	 *            action to execute
	 * @param elementName
	 *            Logical element id on which to perform action
	 * @param elementValue
	 *            value to be used while performing action
	 * @param actualValue
	 *            actualValue
	 * @return action result
	 * @throws AFTException
	 *             Exception
	 */
	String executeAction(TestStepRunner testStepRunner, String action,
			String elementName, String elementValue, String actualValue)
			throws AFTException;

	/**
	 * Execute annotation specific to Execution engine
	 * 
	 * @param annotationName
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the aFT exception
	 */
	void executeAnnotation(String annotationName, String annotationValue)
			throws AFTException;

	/**
	 * Capture the screenshot of the AUT using execution engine specific
	 * implementation
	 * 
	 * @param screenShotFilePath
	 *            file path where to capture screenshot
	 * @throws AFTException
	 *             Exception
	 */
	void captureScreenshot(String screenShotFilePath)
			throws AFTException;
	
	/**
	 * load object repository dynamically at runtime
	 * 
	 * @param objectReposFilePath
	 *            path of the object repository to load
	 * 
	 * @throws AFTException
	 *             Exception
	 */
	void loadObjectRepository(String objectReposFilePath) throws AFTException;

	/**
	 * unload object repository loaded dynamically at runtime using
	 * loadObjectRepository action
	 * 
	 * @throws AFTException
	 *             Exception
	 */
	void unLoadObjectRepository() throws AFTException;

	/**
	 * returns the object id associated with a logical element name
	 * 
	 * @param elementName
	 *            logical name of the element
	 * @return the object id
	 * @throws AFTException
	 *             Exception
	 */
	String getObjectId(String elementName) throws AFTException;

	/**
	 * @return the browserVersion
	 */
	String getBrowserVersion();
	
	/**
	 * Gets the current url.
	 *
	 * @return the current url
	 */
	String getCurrentURL();
}
