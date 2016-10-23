/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General Public License, Version 3.0 (the "License") you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.gnu.or g/licenses/gpl-3.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Class: Configuration
 * 
 * Purpose: Parses AFT Appium Config properties file and implements methods
 * to read/set (in memory only) config properties
 */

package com.ags.aft.appium.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.Util;
import com.ags.aft.common.XMLParser;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;

public class AppiumConfigProperties {

	private static final Logger LOGGER = Logger
			.getLogger(AppiumConfigProperties.class);

	private static AppiumConfigProperties config;

	private Map<String, String> teConfigMap;

	private List<String> configMap;

	public static final String APPIUM_TEST_RUNNER_PATH = "RobotiumTestRunnerPath";
	public static final String SAFSTCP_MESSENGER_PATH = "SafSTCPMessengerPath";
	public static final String ROBOTIUM_REMOTE_CONTROL_PATH = "RobotiumRemoteControlPath";
	public static final String TARGET_APPLICATION_PATH = "TargetApplicationPath";
	public static final String TARGET_APP_CLASS_NAME = "TargetAppClassName";
	public static final String DEVICE_SERIAL_NUMBER = "DeviceSerialNumber";
	public static final String INSTALL_AUT = "InstallAUT";
	public static final String INSTALL_SAFSTCP_MESSENGER = "InstallSAFSTCPMessenger";
	public static final String REBUILD_TEST_RUNNER = "RebuildTestRunner";
	public static final String INSTALL_TEST_RUNNER = "InstallTestRunner";
	public static final String LAUNCH_ANDROID_SCREEN_PATH = "launchAndroidScreen";
	public static final String ANDROID_SCREEN_SHARE_PATH = "AndroidScreenSharePath";
	public static final String ELEMENT_WAIT_TIME_MS = "ElementWaitTime_ms";
	public static final String ELEMENT_WAIT_TIME_MIN = "1000";
	public static final String ELEMENT_WAIT_TIME_MAX = "1800000";
	public static final String DEFAULT_INSTALL_AUT = "NO";
	public static final String DEFAULT_INSTALL_SAFSTCP_MESSENGER = "NO";
	public static final String DEFAULT_REBUILD_TEST_RUNNER = "NO";
	public static final String DEFAULT_INSTALL_TEST_RUNNER = "NO";
	public static final String DEFAULT_ELEMENT_WAIT_TIME = "30000";

	public static final String[] CONFIG_PARAMS = { SAFSTCP_MESSENGER_PATH,
			ROBOTIUM_REMOTE_CONTROL_PATH, INSTALL_AUT,
			INSTALL_SAFSTCP_MESSENGER, REBUILD_TEST_RUNNER,
			INSTALL_TEST_RUNNER, ELEMENT_WAIT_TIME_MS };

	/**
	 * Singleton instance method
	 * 
	 * @return config
	 */
	public static AppiumConfigProperties getInstance() {
		if (config == null) {
			LOGGER.trace("Creating instance of Appium Configuration Properties");
			config = new AppiumConfigProperties();
		}
		return config;
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param aftRobotiumConfigFilePath
	 *            the aft config file path
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadConfigProperties(String aftAppiumConfigFilePath)
			throws AFTException {

		try {
			// Reading AFT Config file for configuration properties
			LOGGER.trace("Creating file stream object to load configuration properties file ["
					+ aftAppiumConfigFilePath + "]");
			LOGGER.info("Reading config file [" + aftAppiumConfigFilePath + "]");

			File configFile = new File(aftAppiumConfigFilePath);

			// Create an XMLParser Object
			XMLParser xmlParser = new XMLParser();
			xmlParser.readXML(configFile.getAbsolutePath());
			// Get the Config Properties as a list
			List<Map<String, String>> teMapList = xmlParser
					.getAttributeNameList("TestEnvironment");

			// Read the first config block...
			teConfigMap = teMapList.get(0);
			// set configuration attribute values
			setAppiumConfigAttributeValues();
			LOGGER.debug("Reading first set of environment data from config file ["
					+ aftAppiumConfigFilePath + "]");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadAppiumPropertiesFromDB() throws AFTException {
		teConfigMap = new HashMap<String, String>();
		try {
			teConfigMap = DatabaseUtil.getInstance().loadConfigProperties(
					teConfigMap, Constants.APPIUMCONFIGTYPE);
			configMap = new ArrayList<String>();
			configMap.add(AppiumConfigProperties.APPIUM_TEST_RUNNER_PATH);
			configMap.add(AppiumConfigProperties.SAFSTCP_MESSENGER_PATH);
			configMap.add(AppiumConfigProperties.TARGET_APPLICATION_PATH);
			configMap.add(AppiumConfigProperties.INSTALL_AUT);
			configMap.add(AppiumConfigProperties.INSTALL_SAFSTCP_MESSENGER);
			configMap.add(AppiumConfigProperties.REBUILD_TEST_RUNNER);
			configMap.add(AppiumConfigProperties.INSTALL_TEST_RUNNER);
			configMap.add(AppiumConfigProperties.ANDROID_SCREEN_SHARE_PATH);
			// set configuration attribute values
			setAppiumConfigAttributeValues();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Method to validate the config properties and to set the default values if
	 * the values are blank
	 * 
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void setAppiumConfigAttributeValues() throws AFTException {

		boolean defaultValueSet = false;
		String defaultValue = "";
		String attributeValue = "";
		String attributeName = "";

		try {

			// Validate all attributes are present in config property file
			// validateConfigAttributes();

			// If INSTALL_AUT value is blank or
			// invalid, so to default value
			attributeName = INSTALL_AUT;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_INSTALL_AUT)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_INSTALL_AUT;
				defaultValueSet = true;
				// Set INSTALL_AUT to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If INSTALL_SAFSTCP_MESSENGER value is blank or
			// invalid, so to default value
			attributeName = INSTALL_SAFSTCP_MESSENGER;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_INSTALL_SAFSTCP_MESSENGER)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_INSTALL_SAFSTCP_MESSENGER;
				defaultValueSet = true;
				// Set INSTALL_SAFSTCP_MESSENGER to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If REBUILD_TEST_RUNNER value is blank or
			// invalid, so to default value
			attributeName = REBUILD_TEST_RUNNER;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_REBUILD_TEST_RUNNER)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_REBUILD_TEST_RUNNER;
				defaultValueSet = true;
				// Set REBUILD_TEST_RUNNER to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If INSTALL_TEST_RUNNER value is blank or
			// invalid, so to default value
			attributeName = INSTALL_TEST_RUNNER;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_INSTALL_TEST_RUNNER)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_INSTALL_TEST_RUNNER;
				defaultValueSet = true;
				// Set INSTALL_TEST_RUNNER to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If the Element Load time out is invalid , set the value to
			// default page load time out
			attributeName = ELEMENT_WAIT_TIME_MS;
			attributeValue = getConfigProperty(attributeName);
			int minValue = Integer.parseInt(ELEMENT_WAIT_TIME_MIN);
			int maxValue = Integer.parseInt(ELEMENT_WAIT_TIME_MAX);

			// Check if the Page Load contains only numerics
			if (!Util.getInstance().containsOnlyNumbers(attributeValue)
					|| (Integer.parseInt(teConfigMap.get(attributeName)) < minValue)
					|| (Integer.parseInt(teConfigMap.get(attributeName)) > maxValue)) {
				defaultValue = DEFAULT_ELEMENT_WAIT_TIME;
				defaultValueSet = true;
				// Set to default Element Wait time
				LOGGER.info("Invalid Element wait time value ["
						+ attributeValue + "] specified in Config file");
				LOGGER.info("Setting the " + " [" + attributeName + "] "
						+ "value to default" + " [" + defaultValue + "]");

				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Sets default value if defaultValueSet is set of TRUE
	 * 
	 * @param defaultValueSet
	 *            defaultValueSet
	 * @param defaultValue
	 *            defaultValue
	 * @param propertyName
	 *            propertyName
	 * @param propertyValue
	 *            propertyValue
	 */
	public void setDefaultConfigAttributeValue(boolean defaultValueSet,
			String defaultValue, String propertyValue, String propertyName) {

		if (defaultValueSet) {
			LOGGER.warn("Invalid property Value [" + propertyValue
					+ "] specified for [" + propertyName
					+ "]. Resetting it to default value [" + defaultValue + "]");
		}

		teConfigMap.put(propertyName, defaultValue);
	}

	/**
	 * Method to set the value of a configuration property.
	 * 
	 * @param propertyName
	 *            the property name for which the value needs to be set
	 * 
	 * @param propertyValue
	 *            The property value to be set
	 * 
	 */
	public void setConfigProperty(String propertyName, String propertyValue) {

		LOGGER.trace("Setting value for property [" + propertyName + "] to ["
				+ propertyValue + "]");

		teConfigMap.put(propertyName, propertyValue);
	}

	/**
	 * Method to get the value of a configuration property.
	 * 
	 * @param propertyName
	 *            the property name for which the value needs to be get
	 * @return string
	 * @throws AFTException
	 * 
	 */
	public String getConfigProperty(String propertyName) throws AFTException {

		String configPropValue;

		try {
			LOGGER.trace("Retrieving value for property [" + propertyName + "]");
			configPropValue = teConfigMap.get(propertyName);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return Util.getInstance().replaceNull(configPropValue);
	}

	/**
	 * Method to validate all required attributes are present in
	 * AFTRobotiumConfig.xml
	 * 
	 * @throws AFTException
	 * 
	 */
	public void validateConfigAttributes() throws AFTException {

		String attributeName = null;
		boolean attributePresent = false;
		boolean flag = true;
		for (int iLoop = 0; iLoop < CONFIG_PARAMS.length; iLoop++) {
			attributeName = CONFIG_PARAMS[iLoop];
			attributePresent = teConfigMap.containsKey(attributeName);

			if (!attributePresent && configMap.contains(attributeName)) {
				flag = false;
			}

			if (!attributePresent && flag) {
				String errMsg = "Required attribute ["
						+ attributeName
						+ "] missing in AFTAppiumConfig.xml. "
						+ "Refer to AFT wiki for a complete list of required attributes.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		}
	}
}