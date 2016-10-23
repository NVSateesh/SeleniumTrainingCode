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
package com.ags.aft.engine.frankensteinDriver;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import com.ags.aft.frankensteinDriver.common.FrankensteinDriverConfigProperties;
import com.ags.aft.common.Util;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.pluginEngine.IxAFTEngine;
import com.ags.aft.runners.TestStepRunner;
import com.thoughtworks.frankenstein.drivers.FrankensteinDriver;

/**
 * The Class XAFTFrankensteinDriverEngine.
 */
public final class XAFTFrankensteinDriverEngine implements IxAFTEngine {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(XAFTFrankensteinDriverEngine.class);

	// singleton XAFTEngine object
	/** The engine. */
	private static XAFTFrankensteinDriverEngine engine = null;
	private String baseClass = null;
	// define driver
	FrankensteinDriver driver = null;

	// Frankenstein Config file...
	/** The aft web driver config file path. */
	private static String aftFrankensteinDriverConfigFilePath = "/configuration/AFTFrankensteinDriverConfig.xml";

	/** The parser. */
	private FrankensteinDriverActionParser parser = null;

	// private constructor as XAFTFrankensteinDriverEngine is a singleton
	// class...
	/**
	 * Instantiates a new xAFT web driver engine.
	 */
	private XAFTFrankensteinDriverEngine() {
		parser = new FrankensteinDriverActionParser();
	}

	public void setBaseClass(String baseClass) {
		this.baseClass = baseClass;
	}

	public String getBaseClass() {
		return this.baseClass;
	}

	// create and return instance of XAFTFrankensteinDriverEngine if not already
	// exists...

	public static XAFTFrankensteinDriverEngine getFrankensteinDriverEngineInstance() {
		if (engine == null) {
			LOGGER.info("Creating singleton  instance of Frankenstein Engine");
			engine = new XAFTFrankensteinDriverEngine();
		}
		return engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#initialize(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */

	public void initialize(String rootPath, String sBaseClass, String appName,
			String browserName, String oRFilePath, boolean isFileSystemRequest)
			throws AFTException {

		LOGGER.info("Executing [initialize], root path [" + rootPath
				+ "], application URL [" + sBaseClass + "], application name ["
				+ appName + "], browser [" + browserName
				+ "], object repository [" + oRFilePath + "]");

		LOGGER.info("Loading Frankenstein config properties file ["
				+ aftFrankensteinDriverConfigFilePath + "]");
		// Load configuration properties file...
		FrankensteinDriverConfigProperties.getInstance().loadConfigProperties(
				rootPath + aftFrankensteinDriverConfigFilePath);
		LOGGER.info("Successfully loaded Frankenstein config properties file ["
				+ aftFrankensteinDriverConfigFilePath + "]");
		setBaseClass(sBaseClass);

		// Load object repository associated with the test suite
		//
		if (oRFilePath != null && oRFilePath.length() > 0) {
			// Load the object repository file...
			ObjectRepositoryManager.getInstance().loadObjectRepository(
					oRFilePath, isFileSystemRequest);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#tearDown()
	 */
	@Override
	public void tearDown() {
		LOGGER.info("Executing command [tearDown]");
		engine = null;
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
	 * @see com.ags.aft.common.IxAFTEngine#executeAnnotation(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void executeAnnotation(String annotationName, String annotationValue)
			throws AFTException {
		LOGGER.info("Executing [executeAnnotation], annotation Name ["
				+ annotationName + "], annotation value [" + annotationValue
				+ "]");

		try {
			if (annotationName.equalsIgnoreCase("@"
					+ FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MS)) {

				parser.callToAnnotationElementWaitTime(annotationName,
						annotationValue);
			} else {
				String errMsg = "Invalid annotation ["
						+ annotationName
						+ "] specified. Please refer to documentation on valid annotations.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

		} catch (AFTException e) {
			throw new AFTException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ags.aft.common.IxAFTEngine#captureScreenshot(java.lang.String)
	 */
	@Override
	public void captureScreenshot(String screenShotFilePath) {
		LOGGER.debug("Executing [captureScreenshot] with screen shot file path ["
				+ screenShotFilePath + "]");

		try {

			// Create a new Robot object and creates an image containing
			// pixels
			// read from the screen
			// Toolkit object is used to return full screen rectangle
			BufferedImage screenCapture = new Robot()
					.createScreenCapture(new Rectangle(Toolkit
							.getDefaultToolkit().getScreenSize()));

			// Create a new file object
			File file = new File(screenShotFilePath);

			// Save the captured image containing pixels to the file
			ImageIO.write(screenCapture, "png", file);

			LOGGER.debug("Successfully captured screen shot to file ["
					+ screenShotFilePath + "]");

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
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

	@Override
	public String getBrowserVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentURL() {
		return null;
	}
}
