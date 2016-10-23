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
package com.ags.aft.engine.appium;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;

import com.ags.aft.appium.common.AFTAppiumBase;
import com.ags.aft.appium.common.AppiumConfigProperties;
import com.ags.aft.common.Util;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.pluginEngine.IxAFTEngine;
import com.ags.aft.runners.TestStepRunner;

/**
 * The Class XAFTRobotiumEngine.
 */
public final class XAFTAppiumEngine implements IxAFTEngine {
	// static org.apache.log4j.Logger LOGGER;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(XAFTAppiumEngine.class);

	// singleton XAFTRobotiumEngine object
	/** The engine. */
	private static XAFTAppiumEngine engine = null;

	// Appium Config file...
	/** The aft web driver config file path. */
	private static String aftAppiumConfigFilePath = "/configuration/AFTAppiumConfig.xml";

	/** The parser. */
	private AppiumActionParser parser = null;

	// private constructor as XAFTRobotiumEngine is a singleton class...
	/**
	 * Instantiates a new xAFT web driver engine.
	 */
	private XAFTAppiumEngine() {
		// need to check for some initialization stuff
		parser = new AppiumActionParser();
	}

	private Process process = null;

	// create and return instance of XAFTAppiumEngine if not already
	// exists...
	/**
	 * Gets the web driver engine instance.
	 * 
	 * @return the web driver engine instance
	 */
	public static XAFTAppiumEngine getAppiumEngineInstance() {
		if (engine == null) {
			LOGGER.info("Creating singleton  instance of Appium Engine");
			engine = new XAFTAppiumEngine();
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
		LOGGER.info("Loading Appium config properties file ["
				+ aftAppiumConfigFilePath + "]");
		// Load configuration properties file...
		if (isFileSystemRequest) {
			AppiumConfigProperties.getInstance().loadConfigProperties(
					rootPath + aftAppiumConfigFilePath);
			LOGGER.info("Successfully loaded Appium config properties file ["
					+ aftAppiumConfigFilePath + "]");
		} else {
			AppiumConfigProperties.getInstance().loadAppiumPropertiesFromDB();
			LOGGER.info("Successfully loaded Appium config properties from DB");
		}

		// Load object repository associated with the test suite
		if (isFileSystemRequest && oRFilePath != null
				&& oRFilePath.length() > 0) {
			// Load the object repository file...
			ObjectRepositoryManager.getInstance().loadObjectRepository(
					oRFilePath, isFileSystemRequest);
		} else if (!isFileSystemRequest) {
			// Load the object repository file...
			ObjectRepositoryManager.getInstance().loadObjectRepository(null,
					isFileSystemRequest);
		}

		// Start the appium server
		AFTAppiumBase.getInstance().startAppium();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#tearDown()
	 */
	@Override
	public void tearDown() {
		LOGGER.info("Executing command [tearDown]");
		try {
			if (AFTAppiumBase.getInstance().getDriver() != null) {
				LOGGER.debug("Stopping Appium instance");
				AFTAppiumBase.getInstance().stopAppium();
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
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
			objectID = getObjectId(elementName);

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
		LOGGER.info("Executing [loadObjectRepository] with repository file path ["
				+ objectReposFilePath + "]");

		try {
			ObjectRepositoryManager.getInstance().loadLocalObjectRepository(
					objectReposFilePath);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#unLoadObjectRepository()
	 */
	@Override
	public void unLoadObjectRepository() throws AFTException {
		LOGGER.info("Executing [unLoadObjectRepository]");

		try {
			ObjectRepositoryManager.getInstance().unLoadLocalObjectRepository();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#getObjectId(java.lang.String)
	 */
	@Override
	public String getObjectId(String elementName) throws AFTException {
		String objectID = null;

		LOGGER.debug("Executing [getObjectId] with element name ["
				+ elementName + "]");

		objectID = ObjectRepositoryManager.getInstance().getObjectID(
				elementName);
		objectID = Util.getInstance().checkForEmptyValue(objectID).trim();

		return objectID;
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
		LOGGER.info("Executing [setObjectID] with element name [" + elementName
				+ "], object id [" + objectID + "]");
		ObjectRepositoryManager.getInstance()
				.setObjectID(elementName, objectID);
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

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	@Override
	public void captureScreenshot(String screenShotFilePath)
			throws AFTException {
		File screenCapture = ((TakesScreenshot) new Augmenter()
				.augment(AFTAppiumBase.getInstance().getDriver()))
				.getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(screenCapture, new File(screenShotFilePath));
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
		LOGGER.debug("Successfully captured screen shot to file ["
				+ screenShotFilePath + "]");

	}
}
