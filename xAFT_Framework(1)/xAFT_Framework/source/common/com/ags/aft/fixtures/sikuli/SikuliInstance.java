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
 * Class: SikuliInstance
 * 
 * Purpose: This class creates the sikuli instance
 */

package com.ags.aft.fixtures.sikuli;

import org.apache.log4j.Logger;
import org.sikuli.script.Screen;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.ImageLocator;
import org.sikuli.basics.Settings;
import org.sikuli.basics.proxies.Vision;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;

public final class SikuliInstance {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(SikuliInstance.class);

	/** The SikuliInstance */
	private static SikuliInstance sikuliInstance = null;

	/** The SikuliScreen */
	private Screen sikuliScreen = null;

	private String bundlePath = null;

	/**
	 * The Sikuli Instance constructor
	 * 
	 * */
	private SikuliInstance() throws AFTException {
		setSikuliRunSettings();
		LOGGER.trace("Creating new Sikuli Screen object");
		sikuliScreen = new Screen();
	}

	/**
	 * Gets the single instance of sikuliInstance.
	 * 
	 * @return single instance of sikuliInstance
	 * @throws AFTException
	 */
	public static SikuliInstance getInstance() throws AFTException {

		if (sikuliInstance == null) {
			LOGGER.trace("Creating instance of aft SikuliInstance");
			sikuliInstance = new SikuliInstance();

		}

		return sikuliInstance;
	}

	/**
	 * Checks if sikuliInstance exists or not
	 * 
	 * @return true/false
	 * @throws AFTException
	 */
	public static boolean doesSikuliInstanceExists() throws AFTException {

		if (sikuliInstance == null) {
			return false;
		}

		return true;
	}

	/**
	 * Gets the Screen object
	 * 
	 * @return sikuliScreen
	 */
	public Screen getSikuliScreen() {

		return sikuliScreen;
	}

	/**
	 * Destroy the sikuli instance
	 * 
	 */
	public void destroySikuliInstance() {
		sikuliInstance = null;
		sikuliScreen = null;
		bundlePath = null;
	}

	/**
	 * Sets the values for Sikuli runtime settings
	 * 
	 * @throws AFTException
	 * 
	 * @throws AFTException
	 */
	private void setSikuliRunSettings() throws AFTException {

		try {
			LOGGER.trace("Setting values for Sikuli runtime settings...");

			// if user did not call @setSikuliImagesPath annotation
			if (bundlePath == null) {
				// get images path specified in aftconfig
				String configImagesPath = ConfigProperties.getInstance()
						.getConfigProperty(ConfigProperties.SIKULI_IMAGESPATH);

				setImagePath(configImagesPath);
			}

			Float moveMouseDelayValue = new Float(ConfigProperties
					.getInstance().getConfigProperty(
							ConfigProperties.SIKULI_MOVEMOUSEDELAY));

			LOGGER.debug("Setting MoveMouseDelay as [" + moveMouseDelayValue
					+ "]");
			Settings.MoveMouseDelay = moveMouseDelayValue;
			LOGGER.debug("MoveMouseDelay set as [" + moveMouseDelayValue + "]");

			int minTargetSizeValue = Integer.parseInt(ConfigProperties
					.getInstance().getConfigProperty(
							ConfigProperties.SIKULI_RECOGNITIONEFFICIENCY));

			LOGGER.debug("Setting minTargetSizeValue as [" + minTargetSizeValue
					+ "]");
			System.getenv("path");
			LOGGER.debug("Calling FileManager.loadLibrary(VisionProxy)");
			FileManager.loadLibrary("VisionProxy");
			Vision.setParameter("MinTargetSize", minTargetSizeValue);
			LOGGER.debug("minTargetSizeValue set as [" + minTargetSizeValue
					+ "]");
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new AFTException("Failed to setup Sikuli Runtime Settings!"
					+ e.getMessage());
		}
	}

	/**
	 * Sets the value for Sikuli BundlePath
	 * 
	 * @param imagesPath
	 *            imagesPath
	 * @throws AFTException
	 */
	public void setImagePath(String imagesPath) throws AFTException {

		try {
			String workingDir = System.getProperty("user.dir");
			String absoluteImagesFolderPath = workingDir
					+ imagesPath.substring(1);
			ImageLocator.setBundlePath(absoluteImagesFolderPath);
			bundlePath = absoluteImagesFolderPath;
			LOGGER.debug("Images present under [" + absoluteImagesFolderPath
					+ "] will be used for sikuli actions");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException("Failed to set Image Path");
		}

	}

	public String getImagePath() {
		return bundlePath;
	}

}