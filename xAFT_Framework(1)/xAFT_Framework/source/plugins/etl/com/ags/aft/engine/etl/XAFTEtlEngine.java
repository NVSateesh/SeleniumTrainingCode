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
package com.ags.aft.engine.etl;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;
import com.ags.aft.pluginEngine.IxAFTEngine;
import com.ags.aft.runners.TestStepRunner;

/**
 * The Class XAFTRobotiumEngine.
 */
public final class XAFTEtlEngine implements IxAFTEngine {
	// static org.apache.log4j.Logger LOGGER;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(XAFTEtlEngine.class);

	// singleton XAFTETLEngine object
	/** The engine. */
	private static XAFTEtlEngine engine = null;

	// Appium Config file...

	/** The parser. */
	private ETLActionParser parser = null;

	// private constructor as ETLEngine is a singleton class...
	/**
	 * Instantiates a new xAFT web driver engine.
	 */
	private XAFTEtlEngine() {
		// need to check for some initialization stuff
		parser = new ETLActionParser();
	}

	/**
	 * Gets the web driver engine instance.
	 * 
	 * @return the web driver engine instance
	 */
	public static XAFTEtlEngine getETLEngineInstance() {
		if (engine == null) {
			LOGGER.info("Creating singleton instance of ETL Engine");
			engine = new XAFTEtlEngine();
		}
		return engine;
	}

	/**
	 * This method will initialize AppiumEngine.
	 * 
	 * @param rootPath
	 *            rootPath
	 * @param sBaseClass
	 *            sBaseClass
	 * @param appName
	 *            appName
	 * @param browserName
	 *            browserName
	 * @param oRFilePath
	 *            oRFilePath
	 * @param isFileSystemRequest
	 *            isFileSystemRequest
	 */
	public void initialize(String rootPath, String sBaseClass, String appName,
			String browserName, String oRFilePath, boolean isFileSystemRequest)
			throws AFTException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#tearDown()
	 */
	@Override
	public void tearDown() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#executeAction(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String executeAction(TestStepRunner testStepRunner, String action,
			String elementName, String elementValue, String actualValue)
			throws AFTException {
		LOGGER.debug("Executing [executeAction], element Name [" + elementName
				+ "], element value [" + elementValue + "], actual value ["
				+ actualValue + "]");

		String result;
		String objectID = null;

		try {
			LOGGER.info("Calling action [" + action + "], element Name ["
					+ elementName + "], objectID [" + objectID
					+ "], parsed value [" + elementValue + "]");

			result = parser.parseAndExecute(testStepRunner, action,
					elementName, objectID, elementValue, actualValue);
		} catch (AFTException e) {
			throw new AFTException(e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ags.aft.common.IxAFTEngine#loadObjectRepository(java.lang.String)
	 */
	@Override
	public void loadObjectRepository(String objectReposFilePath)
			throws AFTException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#unLoadObjectRepository()
	 */
	@Override
	public void unLoadObjectRepository() throws AFTException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#getObjectId(java.lang.String)
	 */
	@Override
	public String getObjectId(String elementName) throws AFTException {

		return "";
	}

	/**
	 * Sets the object id.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @throws AFTException
	 */
	public void setObjectID(String elementName, String objectID)
			throws AFTException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#getBrowserVersion()
	 */
	@Override
	public String getBrowserVersion() {
		return null;
	}

	@Override
	public String getCurrentURL() {
		return null;
	}

	@Override
	public void executeAnnotation(String annotationName, String annotationValue)
			throws AFTException {
		// TODO Auto-generated method stub

	}

	@Override
	public void captureScreenshot(String screenShotFilePath)
			throws AFTException {

	}
}
