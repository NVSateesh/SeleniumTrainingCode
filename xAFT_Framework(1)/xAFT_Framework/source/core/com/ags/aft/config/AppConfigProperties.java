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
 * Class: AppConfigProperties
 * 
 * Purpose: Parses AFT App Config properties file and implements methods to
 * read/set (in memory only) app config properties
 */

package com.ags.aft.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.Util;
import com.ags.aft.common.XMLParser;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.config.ConfigProperties;

/**
 * App Configuration Properties class
 * 
 */
public class AppConfigProperties {

	private static final Logger LOGGER = Logger
			.getLogger(AppConfigProperties.class);

	private static AppConfigProperties appConfig;

	private Map<String, String> teAppConfigMap = new HashMap<String, String>();

	/**
	 * Singleton instance method
	 * 
	 * @return AppConfigProperties object
	 * 
	 */
	public static AppConfigProperties getInstance() {
		if (appConfig == null) {
			LOGGER.trace("Creating instance of App Configuration Properties");
			appConfig = new AppConfigProperties();
		}
		return appConfig;
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param appConfigFilePath
	 *            the app config file path
	 * @param appExecConfig
	 *            app execution configuration
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadConfigProperties(String appConfigFilePath,
			String appExecConfig) throws AFTException {
		try {
			if (ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.APP_CONFIG_FILE_PATH) != null
					&& ConfigProperties
							.getInstance()
							.getConfigProperty(
									ConfigProperties.APP_CONFIG_FILE_PATH)
							.length() > 0) {

				LOGGER.info("Loading App Config file ["
						+ ConfigProperties.getInstance().getConfigProperty(
								ConfigProperties.APP_CONFIG_FILE_PATH) + "]");
				// Reading AFT Config file for configuration properties
				LOGGER.debug("Creating file stream object to load app configuration properties file ["
						+ appConfigFilePath + "]");
				LOGGER.info("Reading app config file [" + appConfigFilePath
						+ "]");
				File configFile = new File(appConfigFilePath);
				// Create an XMLParser Object
				XMLParser xmlParser = new XMLParser();
				xmlParser.readXML(configFile.getAbsolutePath());
				// Get the Config Properties as a list
				List<Map<String, String>> teMapList = xmlParser
						.getAttributeNameList("Configuration");
				LOGGER.debug("Reading execution configuration ["
						+ appExecConfig + "] from app config file ["
						+ appConfigFilePath + "]");
				// Read the first config block...
				if (appExecConfig.length() <= 0) {
					LOGGER.info("No Configuration Specified. Loading the Common Config");
				}
				int iCount = 0;
				for (int i = 0; i < teMapList.size(); i++) {
					Map<String, String> tempConfigAppMap = new HashMap<String, String>();
					tempConfigAppMap = teMapList.get(i);
					if ((tempConfigAppMap.get("Name").compareToIgnoreCase(
							appExecConfig) == 0)
							|| tempConfigAppMap
									.get("Name")
									.compareToIgnoreCase(Constants.COMMONCONFIG) == 0) {
						// If Name attribute is found add the Config
						// attributes to global map
						teAppConfigMap.putAll(tempConfigAppMap);
						iCount = iCount + 1;
						if (iCount > 2) {
							break;
						}
						continue;
					}
				}

			}
		} catch (Exception e) {
			LOGGER.error("File [" + appConfigFilePath + "] not found");
			throw new AFTException(e);
			 
		}
	}

	
	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param appExecConfig
	 *            app execution configuration
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadAppConfigPropertiesFromDB(String appExecConfig)
			throws AFTException {
		try {
			DatabaseUtil.getInstance().loadAppConfigProperties(teAppConfigMap, appExecConfig);
		} catch (Exception e) {
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

		teAppConfigMap.put(propertyName, propertyValue);
	}

	/**
	 * Method to get the value of a configuration property.
	 * 
	 * @param propertyName
	 *            the property name for which the value needs to be fetched
	 * @return property value
	 * @throws AFTException
	 * 
	 */
	public String getConfigProperty(String propertyName) throws AFTException {

		String configPropValue;

		try {
			LOGGER.trace("Retrieving value for property [" + propertyName + "]");

			configPropValue = teAppConfigMap.get(propertyName);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return Util.getInstance().replaceNull(configPropValue);
	}
}