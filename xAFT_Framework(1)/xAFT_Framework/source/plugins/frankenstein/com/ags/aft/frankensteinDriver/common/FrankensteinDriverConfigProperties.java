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
 * Class: Configuration
 * 
 * Purpose: Parses AFT FrankensteinDriver Config properties file and implements
 * methods to read/set (in memory only) config properties
 */

package com.ags.aft.frankensteinDriver.common;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.common.Util;
import com.ags.aft.common.XMLParser;
import com.ags.aft.exception.AFTException;

public class FrankensteinDriverConfigProperties {

	private static final Logger LOGGER = Logger
			.getLogger(FrankensteinDriverConfigProperties.class);

	private static FrankensteinDriverConfigProperties config;

	private Map<String, String> teConfigMap;

	public static final String ELEMENT_WAIT_TIME_MS = "ElementWaitTime_ms";
	public static final String FRANKENSTEINDRIVER_COMMAND_SWITCH = "FrankensteinDriverCommandSwitch";

	public static final String[] CONFIG_PARAMS = { ELEMENT_WAIT_TIME_MS,
			FRANKENSTEINDRIVER_COMMAND_SWITCH };

	public static final String ELEMENT_WAIT_TIME_MIN = "1000";
	public static final String ELEMENT_WAIT_TIME_MAX = "1800000";
	public static final String DEFAULT_ELEMENT_WAIT_TIME = "60000";

	/**
	 * Singleton instance method
	 * 
	 */
	public static FrankensteinDriverConfigProperties getInstance() {
		if (config == null) {
			LOGGER.trace("Creating instance of FrankensteinDriver Configuration Properties");
			config = new FrankensteinDriverConfigProperties();
		}
		return config;
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param aftFrankensteinDriverConfigFilePath
	 *            the aft config file path
	 * @throws Exception
	 *             the exception
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadConfigProperties(String aftFrankensteinDriverConfigFilePath)
			throws AFTException {

		try {
			// Reading AFT Config file for configuration properties
			LOGGER.trace("Creating file stream object to load configuration properties file ["
					+ aftFrankensteinDriverConfigFilePath + "]");
			LOGGER.info("Reading config file ["
					+ aftFrankensteinDriverConfigFilePath + "]");

			File configFile = new File(aftFrankensteinDriverConfigFilePath);

			// Create an XMLParser Object
			XMLParser xmlParser = new XMLParser();
			xmlParser.readXML(configFile.getAbsolutePath());
			// Get the Config Properties as a list
			List<Map<String, String>> teMapList = xmlParser
					.getAttributeNameList("TestEnvironment");

			// Read the first config block...
			teConfigMap = teMapList.get(0);

			// Validate all attributes are present in config property file
			validateConfigAttributes();

			LOGGER.debug("Reading first set of environment data from config file ["
					+ aftFrankensteinDriverConfigFilePath + "]");
		}

		catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
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
	 * AFTFrankensteinDriverConfig.xml
	 * 
	 * @throws AFTException
	 * 
	 */
	public void validateConfigAttributes() throws AFTException {

		String attributeName = null;
		boolean attributePresent = false;
		for (int iLoop = 0; iLoop < CONFIG_PARAMS.length; iLoop++) {
			attributeName = CONFIG_PARAMS[iLoop];
			attributePresent = teConfigMap.containsKey(attributeName);
			if (!attributePresent) {
				String errMsg = "Required attribute ["
						+ attributeName
						+ "] missing in AFTFrankensteinDriverConfig.xml. "
						+ "Refer to AFT wiki for a complete list of required attributes.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		}
	}
}