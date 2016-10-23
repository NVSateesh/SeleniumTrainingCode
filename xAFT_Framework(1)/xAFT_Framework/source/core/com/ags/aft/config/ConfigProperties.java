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
 * Purpose: Parses AFT Config properties file and implements methods to read/set
 * (in memory only) config properties
 */

package com.ags.aft.config;

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
import com.ags.aft.util.Helper;
/**
 * The Class ConfigProperties.
 */
public class ConfigProperties {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ConfigProperties.class);

	/** The Config. */
	private static ConfigProperties config;
	/** The teConfigMap. */
	private Map<String, String> teConfigMap;
	/** The dbConfigMap. */
	private List<String> dbConfigMap;
	/** The OnError. */
	public static final String ONERROR = "OnError";
	/** The Log4JProperties filePath. */
	public static final String LOG4J_PROPERTIES_FILEPATH = "Log4JPropertiesFilePath";
	/** The Test batch path. */
	public static final String TESTBATCH_PATH = "TestBatch_Path";
	/** The Log file path. */
	public static final String LOGFILE_PATH = "LogFilePath";
	/** The Test result path. */
	public static final String TESTRESULT_PATH = "TestResultPath";
	/** The Screen shot path. */
	public static final String SCREENSHOT_PATH = "ScreenShotPath";
	/** The Service request path. */
	public static final String SERVICEREQUEST_PATH = "ServiceRequestPath";
	/** The default. */
	public static final String DEFAULT = "default";
	/** The Random data resources filePath. */
	public static final String RANDOM_DATA_RESOURCES_FILEPATH = "RandomDataResourcesFilePath";
	/** The capture screen shot. */
	public static final String CAPTURESCREENSHOT = "CaptureScreenShot";
	/** The Email notification from address. */
	public static final String EMAIL_NOTIF_FROM_ADDR = "Email_Notif_FromAddr";
	/** The Email notification to address. */
	public static final String EMAIL_NOTIF_TO_ADDR = "Email_Notif_ToAddr";
	/** The Email server address. */
	public static final String EMAIL_SERVER_ADDR = "Email_Server_Addr";
	/** The Email server port. */
	public static final String EMAIL_SERVER_PORT = "Email_Server_Port";
	/** The Email server authentication. */
	public static final String EMAIL_SERVER_AUTH = "Email_Server_Auth";
	/** The Email server start TLS. */
	public static final String EMAIL_SERVER_STARTTLS = "Email_Server_StartTLS";
	/** The Email user name. */
	public static final String EMAIL_USERNAME = "Email_userName";
	/** The Email password. */
	public static final String EMAIL_PASSWORD = "Email_password";
	/** The Email notification CC address. */
	public static final String EMAIL_NOTIF_CC_ADDR = "Email_Notif_CCAddr";
	/** The Step values delimiter character. */
	public static final String STEP_VALUES_DELIMITER = "Step_Values_Delimiter_Character";
	/** The Test data column delimiter. */
	public static final String TEST_DATA_COLUMN_DELIMITER = "Test_Data_Column_Delimiter";
	/** The Test execution result email notification. */
	public static final String TEST_EXECUTION_RESULT_EMAILNOTIF = "TestExecutionResultEmailNotif";
	/** The TCM integration. */
	public static final String TCM_INTEGRATION = "TCMIntegration";
	/** The TCM integration jar path. */
	public static final String TCM_INTEGRATION_JAR_PATH = "TCMIntegrationJarPath";
	/** The TCM integration attach evidence. */
	public static final String TCM_INTEGRATION_ATTACH_EVIDENCE = "TCMIntegrationAttachEvidence";
	/** The TCM integration class name. */
	public static final String TCM_INTEGRATION_CLASS_NAME = "TCMIntegrationClassName";
	/** The Application configuration file path. */
	public static final String APP_CONFIG_FILE_PATH = "AppConfigFilePath";
	/** The Attach execution log file. */
	public static final String ATTACH_EXECUTION_LOG_FILE = "AttachExecutionLogFile";
	/** The Test failure email notification. */
	public static final String TEST_FAILURE_EMAILNOTIF = "TestFailureEmailNotification";
	/** The Sikuli images path. */
	public static final String SIKULI_IMAGESPATH = "Sikuli_ImagesPath";
	/** The Sikuli move mouse delay. */
	public static final String SIKULI_MOVEMOUSEDELAY = "Sikuli_MoveMouseDelay";
	/** The Sikuli recognition efficiency. */
	public static final String SIKULI_RECOGNITIONEFFICIENCY = "Sikuli_RecognitionEfficiency";
	/** The Link checker page time out ms. */
	public static final String LINK_CHECKER_PAGE_TIME_OUT = "LinkCheckerPageTimeOut_ms";
	/** The Link checker page time out ms. */
	public static final String LINK_CHECKER_USER_AGENT = "LinkCheckerUserAgent";
	/** The Link checker page time out ms. */
	public static final String LINK_CHECKER_PROXY_PATH = "LinkCheckerProxy";
	/** The Link checker page time out ms. */
	public static final String EXTERNAL_SCRIPT_METHOD_PREFIX = "ExternalScriptMethodPrefix";
	/** The Link checker page time out ms. */
	// Report file retention period check params
	public static final String LOG_FILE_RETENTION_PERIOD = "LogFile_RetentionPeriod_Days";
	public static final String REPORT_FILE_RETENTION_PERIOD = "ReportFile_RetentionPeriod_Days";
	public static final String SERVICE_REQUESTS_RETENTION_PERIOD = "ServiceRequests_RetentionPeriod_Days";
	public static final String SCREEN_SHOTS_RETENTION_PERIOD = "Screenshots_RetentionPeriod_Days";

	public static final String ONERROR_DB_CONNECTION = "OnError_DB_Connection";

	public static final String SHOW_SCROLLABLE_TEST_SCENARIO_FRAME = "ShowScrollableTestScenarioFrame";
	public static final String SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE = "ScrollableTestScenarioFrame_FontSize";
	public static final String SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_TIME = "ScrollableTestScenarioFrame_RefreshTime";
	public static final String LINK_CHECKER_THREAD_COUNT = "LinkCheckerThreadCount";
	public static final String DISABLE_STANDARD_REPORTING = "DisableStandardReporting";
	public static final String ENLACE_URL = "EnlaceUrl";

	public static final String[] CONFIG_PARAMS = { TESTBATCH_PATH,
			LOG4J_PROPERTIES_FILEPATH, LOGFILE_PATH, SCREENSHOT_PATH,
			SERVICEREQUEST_PATH, TESTRESULT_PATH,
			RANDOM_DATA_RESOURCES_FILEPATH, CAPTURESCREENSHOT,
			STEP_VALUES_DELIMITER, TEST_DATA_COLUMN_DELIMITER, ONERROR,
			TEST_EXECUTION_RESULT_EMAILNOTIF, EMAIL_NOTIF_FROM_ADDR,
			EMAIL_NOTIF_TO_ADDR, EMAIL_NOTIF_CC_ADDR, EMAIL_SERVER_ADDR,
			TCM_INTEGRATION, TCM_INTEGRATION_JAR_PATH,
			TCM_INTEGRATION_ATTACH_EVIDENCE, TCM_INTEGRATION_CLASS_NAME,
			APP_CONFIG_FILE_PATH, ATTACH_EXECUTION_LOG_FILE, EMAIL_SERVER_PORT,
			EMAIL_SERVER_AUTH, EMAIL_SERVER_STARTTLS, TEST_FAILURE_EMAILNOTIF,
			LOG_FILE_RETENTION_PERIOD, SERVICE_REQUESTS_RETENTION_PERIOD,
			SCREEN_SHOTS_RETENTION_PERIOD, REPORT_FILE_RETENTION_PERIOD,
			SHOW_SCROLLABLE_TEST_SCENARIO_FRAME,
			SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE,
			SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_TIME, ONERROR_DB_CONNECTION,
			SIKULI_IMAGESPATH, SIKULI_MOVEMOUSEDELAY,
			SIKULI_RECOGNITIONEFFICIENCY, LINK_CHECKER_PAGE_TIME_OUT,
			LINK_CHECKER_USER_AGENT, LINK_CHECKER_PROXY_PATH,
			EXTERNAL_SCRIPT_METHOD_PREFIX, LINK_CHECKER_THREAD_COUNT,DISABLE_STANDARD_REPORTING,ENLACE_URL };

	public static final String LOGLEVEL_TRACE = "trace";
	public static final String LOGLEVEL_DEBUG = "debug";
	public static final String LOGLEVEL_INFO = "info";
	public static final String LOGLEVEL_WARN = "warn";
	public static final String LOGLEVEL_ERROR = "error";
	public static final String LOGLEVEL_FATAL = "fatal";

	public static final String DEFAULT_LOGLEVEL = LOGLEVEL_INFO;
	public static final String DEFAULT_SCREENSHOTS_PATH = "./Screenshots/";
	public static final String DEFAULT_SERVICEREQUEST_PATH = "./serviceRequests/";
	public static final String DEFAULT_RANDOM_DATA_RESOURCE_FILEPATH = "./resource/RandomData/";
	public static final String DEFAULT_LOGFILE_PATH = "./logging";

	public static final String ONERROR_RESUMENEXTTESTSCENARIO = "ResumeNextTestScenario";
	public static final String ONERROR_RESUMENEXTTESTSTEP = "ResumeNextTestStep";
	public static final String ONERROR_RESUMENEXTTESTCASE = "ResumeNextTestCase";
	public static final String ONERROR_RESUMENEXTTESTSUITE = "ResumeNextTestSuite";
	public static final String DEFAULT_ONERROR = ONERROR_RESUMENEXTTESTSCENARIO;
	public static final String DEFAULT_ONERROR_DB_CONNECTION = ONERROR_RESUMENEXTTESTSCENARIO;
	public static final String SELENIUM_PAGELOAD_TIMEOUT_MIN = "1000";
	public static final String SELENIUM_PAGELOAD_TIMEOUT_MAX = "1800000";
	public static final String DEFAULT_SELENIUM_PAGELOAD_TIMEOUT = "60000";
	public static final String ELEMENT_WAIT_TIME_MIN = "1000";
	public static final String ELEMENT_WAIT_TIME_MAX = "1800000";
	public static final String DEFAULT_ELEMENT_WAIT_TIME = "30000";
	public static final String CAPTURESCREENSHOT_ALL = "All";
	public static final String CAPTURESCREENSHOT_ERROR = "OnError";
	public static final String CAPTURESCREENSHOT_VERIFY = "OnVerify";
	public static final String CAPTURESCREENSHOT_NONE = "None";
	public static final String DEFAULT_CAPTURE_SCREENSHOT_VALUE = CAPTURESCREENSHOT_ERROR;
	public static final String DEFAULT_RANDOM_DATA_RESOURCES_FILEPATH = "./resource/RandomData/";
	public static final String DEFAULT_TESTRESULT_PATH = "./testReports/xml";
	public static final String DEFAULT_SSLENABLED = "NO";
	public static final String DEFAULT_TCM_INTEGRATION = "NO";
	public static final String DEFAULT_TCM_INTEGRATION_ATTACH_EVIDENCE = "NO";
	public static final String DEFAULT_EMAIL_SERVER_ADDR = "smtp.gmail.com";
	public static final String DEFAULT_EMAIL_SERVER_PORT = "587";
	public static final String DEFAULT_EMAIL_SERVER_AUTH = "TRUE";
	public static final String DEFAULT_EMAIL_SERVER_STARTTLS = "TRUE";
	public static final String DEFAULT_TEST_EXECUTION_RESULT_EMAILNOTIF = "NO";
	public static final String DEFAULT_SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE = "18";
	public static final String DEFAULT_SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_PERIOD = "200";
	public static final String DEFAULT_SHOW_SCROLLABLE_TEST_SCENARIO_FRAME = "NO";
	public static final String DEFAULT_APP_CONFIG_FILE_PATH = "./configuration/AppConfig.xml";
	public static final String DEFAULT_SIKULI_IMAGESPATH = "./objectRepository/images";
	public static final String DEFAULT_SIKULI_MOVEMOUSEDELAY = "0.5";
	public static final String DEFAULT_SIKULI_RECOGNITIONEFFICIENCY = "12";
	public static final String DEFAULT_LINK_CHECKER_THREAD_COUNT = "10";
	public static final String DEFAULT_DISABLE_STANDARD_REPORTING = "NO";

	/**
	 * Singleton instance method
	 * @return config
	 */
	public static ConfigProperties getInstance() {
		if (config == null) {
			LOGGER.trace("Creating instance of AFT Configuration Properties");
			config = new ConfigProperties();
		}
		return config;
	}

	/**
	 * Method to load the properties file into the memory.
	 * 
	 * @param aftConfigFilePath
	 *            the aft config file path
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	public void loadConfigPropertiesFromFileSystem(String aftConfigFilePath)
			throws AFTException {

		try {
			// Reading AFT Config file for configuration properties
			LOGGER.trace("Creating file stream object to load configuration properties file ["
					+ aftConfigFilePath + "]");
			LOGGER.info("Reading config file [" + aftConfigFilePath + "]");

			File configFile = new File(aftConfigFilePath);

			// Create an XMLParser Object
			XMLParser xmlParser = new XMLParser();
			xmlParser.readXML(configFile.getAbsolutePath());
			// Get the Config Properties as a list
			List<Map<String, String>> teMapList = xmlParser
					.getAttributeNameList("TestEnvironment");

			// Read the first config block...
			teConfigMap = teMapList.get(0);

			// set configuration attribute values
			setConfigAttributeValues();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		LOGGER.debug("Reading first set of environment data from config file ["
				+ aftConfigFilePath + "]");
	}

	/**
	 * Method to validate the config properties and to set the default values if
	 * the values are blank
	 * 
	 * @throws AFTException
	 *             the framework initialization exception
	 */
	private void setConfigAttributeValues() throws AFTException {
		boolean defaultValueSet = false;
		boolean validValue = false;
		String defaultValue = "";
		String attributeValue = "";
		String attributeName = "";

		try {
			// Validate all attributes are present in config property file
			validateConfigAttributes();

			// If SEND_FRAMEWORK_NOTIFICATION_EMAILS value is blank or invalid,
			// so to default value
			attributeName = TEST_EXECUTION_RESULT_EMAILNOTIF;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_TEST_EXECUTION_RESULT_EMAILNOTIF)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_TEST_EXECUTION_RESULT_EMAILNOTIF;
				defaultValueSet = true;
				// Set SEND_FRAMEWORK_NOTIFICATION_EMAILS to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// Validate EMAIL_SERVER_ADDR, EMAIL_SERVER_PORT, EMAIL_SERVER_AUTH
			// EMAIL_SERVER_STARTTLS if SEND_FRAMEWORK_NOTIFICATION_EMAILS is
			// marked YES
			if (attributeValue.equalsIgnoreCase("YES")) {
				// If EMAIL_SERVER_ADDR value is blank, so to default value
				attributeName = EMAIL_SERVER_ADDR;
				attributeValue = getConfigProperty(attributeName);

				if (attributeValue.length() <= 0) {
					defaultValue = DEFAULT_EMAIL_SERVER_ADDR;
					defaultValueSet = true;
					// Set EMAIL_SERVER_ADDR to default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

				// If EMAIL_SERVER_PORT value is blank or contains alphabets, so
				// to default value
				attributeName = EMAIL_SERVER_PORT;
				attributeValue = getConfigProperty(attributeName);

				if (attributeValue.length() <= 0
						|| !Util.getInstance().containsOnlyNumbers(
								attributeValue)) {
					defaultValue = DEFAULT_EMAIL_SERVER_PORT;
					defaultValueSet = true;
					// Set EMAIL_SERVER_PORT to default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

				// If EMAIL_SERVER_AUTH value is blank or invalid, so to default
				// value
				attributeName = EMAIL_SERVER_AUTH;
				attributeValue = getConfigProperty(attributeName);

				if (!attributeValue.equalsIgnoreCase(DEFAULT_EMAIL_SERVER_AUTH)
						&& !attributeValue.equalsIgnoreCase("FALSE")) {
					defaultValue = DEFAULT_EMAIL_SERVER_AUTH;
					defaultValueSet = true;
					// Set EMAIL_SERVER_AUTH to default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

				// If EMAIL_SERVER_STARTTLS value is blank, so to default value
				attributeName = EMAIL_SERVER_STARTTLS;
				attributeValue = getConfigProperty(attributeName);

				if (!attributeValue
						.equalsIgnoreCase(DEFAULT_EMAIL_SERVER_STARTTLS)
						&& !attributeValue.equalsIgnoreCase("FALSE")) {
					defaultValue = DEFAULT_EMAIL_SERVER_STARTTLS;
					defaultValueSet = true;
					// Set EMAIL_SERVER_STARTTLS to default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

				// If either EMAIL_USERNAME, EMAIL_PASSWORD, EMAIL_NOTIF_TO_ADDR
				// EMAIL_NOTIF_FROM_ADDR value is blank, raise Exception
				int paramValueMissing = 0;
				String[] emailParams = { EMAIL_USERNAME, EMAIL_PASSWORD,
						EMAIL_NOTIF_TO_ADDR, EMAIL_NOTIF_FROM_ADDR };
				for (int i = 0; i < emailParams.length; i++) {
					attributeName = emailParams[i];
					attributeValue = getConfigProperty(attributeName);
					if (attributeValue.length() <= 0) {
						paramValueMissing = paramValueMissing + 1;
					}
				}

				if (paramValueMissing > 0) {
					String errMsg = "Value for one of the required email parameters(EMAIL_USERNAME, EMAIL_PASSWORD,"
							+ "EMAIL_NOTIF_TO_ADDR, EMAIL_NOTIF_FROM_ADDR) is missing.";

					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}

			}

			// If TCM_INTEGRATION_ATTACH_EVIDENCE value is blank or invalid, set
			// to default value
			attributeName = TCM_INTEGRATION_ATTACH_EVIDENCE;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_TCM_INTEGRATION_ATTACH_EVIDENCE)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_TCM_INTEGRATION_ATTACH_EVIDENCE;
				defaultValueSet = true;
				// Set TCM_INTEGRATION_ATTACH_EVIDENCE to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If TCM_INTEGRATION value is blank or invalid, set to default
			// value
			attributeName = TCM_INTEGRATION;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_TCM_INTEGRATION)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_TCM_INTEGRATION;
				defaultValueSet = true;
				// Set TCM_INTEGRATION to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If TESTRESULTPATH value is blank or invalid, so to default value
			attributeName = TESTRESULT_PATH;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_TESTRESULT_PATH)) {
				defaultValue = DEFAULT_TESTRESULT_PATH;
				defaultValueSet = true;
				// Set TESTRESULT_PATH to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If SHOW_SCROLLABLE_TEST_SCENARIO_FRAME value is blank or invalid,
			// so to default value
			attributeName = SHOW_SCROLLABLE_TEST_SCENARIO_FRAME;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_SHOW_SCROLLABLE_TEST_SCENARIO_FRAME)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_SHOW_SCROLLABLE_TEST_SCENARIO_FRAME;
				defaultValueSet = true;
				// Set SEND_FRAMEWORK_NOTIFICATION_EMAILS to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			if (attributeValue.equalsIgnoreCase("Yes")) {
				// If REFRESH_PERIOD value is blank, so to default value
				attributeName = SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_TIME;
				attributeValue = getConfigProperty(attributeName);

				// Check if the Browser Timeout contains only numerics
				if (attributeValue.length() <= 0
						|| !Util.getInstance().containsOnlyNumbers(
								attributeValue)) {
					defaultValue = DEFAULT_SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_PERIOD;
					defaultValueSet = true;
					// Set SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_TIME to
					// default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

				// If FONT_SIZE value is blank, so to default value
				attributeName = SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE;
				attributeValue = getConfigProperty(attributeName);

				// Check if the Browser Timeout contains only numerics
				if (attributeValue.length() <= 0
						|| !Util.getInstance().containsOnlyNumbers(
								attributeValue)) {
					defaultValue = DEFAULT_SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE;
					defaultValueSet = true;
					// Set SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE to default
					// Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}

			}
			// If RANDOMDATARESOURCESFILEPATH value is blank or invalid, so to
			// default value
			attributeName = RANDOM_DATA_RESOURCES_FILEPATH;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_RANDOM_DATA_RESOURCES_FILEPATH)) {
				defaultValue = DEFAULT_RANDOM_DATA_RESOURCES_FILEPATH;
				defaultValueSet = true;
				// Set RANDOM_DATA_RESOURCES_FILEPATH to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If ONERROR value is invalid , so to default value
			attributeName = ONERROR;
			attributeValue = getConfigProperty(attributeName);
			validValue = false;
			// test whether onError value is valid or not.
			validValue = isValidOnErrorValue(attributeValue);
			if (!validValue) {
				defaultValue = DEFAULT_ONERROR;
				defaultValueSet = true;
				// Set ON ERROR to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If OnError_DB_Connection value is invalid , so to default value
			attributeName = ONERROR_DB_CONNECTION;
			attributeValue = getConfigProperty(attributeName);
			// test whether onError value is valid or not.
			validValue = isValidOnErrorValue(attributeValue);
			if (!validValue) {
				defaultValue = DEFAULT_ONERROR_DB_CONNECTION;
				defaultValueSet = true;
				// Set ONERROR_DB_CONNECTION to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If Capture Screen Shot Property is set to invalid value,set
			// default value
			attributeName = CAPTURESCREENSHOT;
			attributeValue = getConfigProperty(attributeName);
			String[] captureTypes = { CAPTURESCREENSHOT_ALL,
					CAPTURESCREENSHOT_ERROR, CAPTURESCREENSHOT_VERIFY,
					CAPTURESCREENSHOT_NONE };
			validValue = false;
			for (int k = 0; k < captureTypes.length; k++) {
				if (attributeValue.equalsIgnoreCase(captureTypes[k])) {
					validValue = true;
					break;
				}
			}
			if (!validValue) {
				defaultValue = DEFAULT_CAPTURE_SCREENSHOT_VALUE;
				defaultValueSet = true;
				// Set Capture Screenshot Path to default value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If APP_CONFIG_FILE_PATH value is blank or invalid, so to
			// default value
			attributeName = APP_CONFIG_FILE_PATH;
			attributeValue = getConfigProperty(attributeName);

			if (attributeValue.isEmpty() || !attributeValue.endsWith(".xml")) {
				defaultValue = DEFAULT_APP_CONFIG_FILE_PATH;
				defaultValueSet = true;
				// Set APP_CONFIG_FILE_PATH to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If SERVICEREQUEST_PATH value is blank or invalid, so to
			// default value
			attributeName = SERVICEREQUEST_PATH;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue.equalsIgnoreCase(DEFAULT_SERVICEREQUEST_PATH)) {
				defaultValue = DEFAULT_SERVICEREQUEST_PATH;
				defaultValueSet = true;
				// Set SERVICEREQUEST_PATH to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If SIKULI_IMAGESPATH value is blank or invalid, so to
			// default value
			attributeName = SIKULI_IMAGESPATH;
			attributeValue = getConfigProperty(attributeName);

			if (attributeValue.isEmpty() || attributeValue.length() <= 0) {
				defaultValue = DEFAULT_SIKULI_IMAGESPATH;
				defaultValueSet = true;
				// Set SIKULI_IMAGESPATH to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If SIKULI_MOVEMOUSEDELAY value is blank or invalid, so to
			// default value
			attributeName = SIKULI_MOVEMOUSEDELAY;
			attributeValue = getConfigProperty(attributeName);
			validValue = true;

			if (attributeValue.length() <= 0) {
				validValue = false;
			} else {
				try {
					Float.parseFloat(attributeValue);
				} catch (NumberFormatException e) {
					validValue = false;
				}
			}

			if (!validValue) {
				defaultValue = DEFAULT_SIKULI_MOVEMOUSEDELAY;
				defaultValueSet = true;
				// Set MOVEMOUSEDELAY to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If SIKULI_RECOGNITIONEFFICIENCY value is blank or invalid, so to
			// default value
			attributeName = SIKULI_RECOGNITIONEFFICIENCY;
			attributeValue = getConfigProperty(attributeName);

			if (attributeValue.length() <= 0
					|| !Util.getInstance().containsOnlyNumbers(attributeValue)) {
				defaultValue = DEFAULT_SIKULI_RECOGNITIONEFFICIENCY;
				defaultValueSet = true;
				// Set MINTARGETSIZE to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If link checker thread count value is not set, set the default
			// count
			attributeName = LINK_CHECKER_THREAD_COUNT;
			attributeValue = getConfigProperty(attributeName);
			if (attributeValue.isEmpty()) {
				defaultValue = DEFAULT_LINK_CHECKER_THREAD_COUNT;
				defaultValueSet = true;
				// Set SEND_FRAMEWORK_NOTIFICATION_EMAILS to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}

			// If DISABLE_STANDARD_REPORTING value is blank or invalid,
			// so to default value i.e NO
			attributeName = DISABLE_STANDARD_REPORTING;
			attributeValue = getConfigProperty(attributeName);

			if (!attributeValue
					.equalsIgnoreCase(DEFAULT_DISABLE_STANDARD_REPORTING)
					&& !attributeValue.equalsIgnoreCase("YES")) {
				defaultValue = DEFAULT_DISABLE_STANDARD_REPORTING;
				defaultValueSet = true;
				// Set DISABLE_STANDARD_REPORTING to default Value
				setDefaultConfigAttributeValue(defaultValueSet, defaultValue,
						attributeValue, attributeName);
			}
			
			
			// If ENLACE_URL value is blank or invalid,
			// so to default value i.e NO
			attributeName = ENLACE_URL;
			attributeValue = getConfigProperty(attributeName);

			if (attributeValue.isEmpty() || attributeValue.length() <= 0) {
				defaultValue = null;
				defaultValueSet = true;
				if (!Helper.getInstance().isFileSystemRequest()) {
					// Set DISABLE_STANDARD_REPORTING to default Value
					setDefaultConfigAttributeValue(defaultValueSet,
							defaultValue, attributeValue, attributeName);
				}
			}
			DatabaseUtil.getInstance().setEnlaceURL(attributeValue);
		}

		catch (Exception e) {
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
	public void loadConfigPropertiesFromDB() throws AFTException {
		teConfigMap = new HashMap<String, String>();
		try {
			teConfigMap = DatabaseUtil.getInstance().loadConfigProperties(
					teConfigMap, Constants.AFTCONFIGTYPE);
			dbConfigMap = new ArrayList<String>();
			dbConfigMap.add(ConfigProperties.SCREENSHOT_PATH);
			dbConfigMap.add(ConfigProperties.SERVICEREQUEST_PATH);
			dbConfigMap.add(ConfigProperties.RANDOM_DATA_RESOURCES_FILEPATH);
			dbConfigMap.add(ConfigProperties.STEP_VALUES_DELIMITER);
			dbConfigMap.add(ConfigProperties.TEST_DATA_COLUMN_DELIMITER);
			dbConfigMap.add(ConfigProperties.TESTRESULT_PATH);
			dbConfigMap.add(ConfigProperties.APP_CONFIG_FILE_PATH);
			dbConfigMap.add(ConfigProperties.TESTBATCH_PATH);
			dbConfigMap.add(ConfigProperties.LOG4J_PROPERTIES_FILEPATH);
			dbConfigMap.add(ConfigProperties.DISABLE_STANDARD_REPORTING);
			// set configuration attribute values
			setConfigAttributeValues();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		LOGGER.info("---------Exiting loadConfigPropertiesFromDb---------------------------");

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
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		boolean flag = true;
		if (!isFileSystemRequest && dbConfigMap.contains(propertyName)) {
			flag = false;
		}
		if (defaultValueSet && flag) {
			LOGGER.warn("Invalid property Value [" + propertyValue
					+ "] specified for [" + propertyName
					+ "]. Resetting it to default value [" + defaultValue + "]");
			teConfigMap.put(propertyName, defaultValue);
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
	 *  @return config value
	 * @throws AFTException
	 * 
	 */
	public String getConfigProperty(String propertyName) throws AFTException {

		String configPropValue = null;

		try {
			LOGGER.trace("Retrieving value for property [" + propertyName + "]");
			if (teConfigMap != null) {
				configPropValue = teConfigMap.get(propertyName);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return Util.getInstance().replaceNull(configPropValue);
	}

	/**
	 * Method to validate all required attributes are present in AFTConfig.xml
	 * 
	 * @throws AFTException
	 * 
	 */
	public void validateConfigAttributes() throws AFTException {

		String attributeName = null;
		boolean attributePresent = false;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		boolean flag = true;

		for (int iLoop = 0; iLoop < CONFIG_PARAMS.length; iLoop++) {
			attributeName = CONFIG_PARAMS[iLoop];
			attributePresent = teConfigMap.containsKey(attributeName);
			if (!isFileSystemRequest && !attributePresent
					&& dbConfigMap.contains(attributeName)) {
				flag = false;
			}

			if (isFileSystemRequest
					&& !attributePresent
					&& attributeName
							.equalsIgnoreCase(ConfigProperties.ENLACE_URL)) {
				flag = false;
			}
			if (!attributePresent && flag) {
				String errMsg = "Required attribute ["
						+ attributeName
						+ "] is missing in AFTConfig.xml. "
						+ "Refer to AFT wiki for a complete list of required attributes.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		}

	}

	/**
	 * Method to validate the OnError value
	 * @param attributeValue
	 *          attributeValue
	 * @return validValue
	 * 
	 */
	private boolean isValidOnErrorValue(String attributeValue) {
		boolean validValue = false;
		String[] onErrorTypes = { ONERROR_RESUMENEXTTESTSCENARIO,
				ONERROR_RESUMENEXTTESTSTEP, ONERROR_RESUMENEXTTESTCASE,
				ONERROR_RESUMENEXTTESTSUITE };
		for (int k = 0; k < onErrorTypes.length; k++) {
			if (attributeValue.equalsIgnoreCase(onErrorTypes[k])) {
				validValue = true;
			}
		}
		return validValue;
	}

	/**
	 * This method will return whether standard reporting is disaled or not
	 * 
	 * @return flag
	 * @throws AFTException
	 */
	public boolean isStandardReportingDisabled()throws AFTException{
		String configValue = getConfigProperty(ConfigProperties.DISABLE_STANDARD_REPORTING);
		boolean flag = false;

		if (configValue != null && configValue.equalsIgnoreCase("YES")){
			flag = true;
		}
		return flag;
	}
}