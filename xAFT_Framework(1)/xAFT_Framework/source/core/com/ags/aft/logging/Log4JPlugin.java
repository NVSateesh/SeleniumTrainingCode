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
 * Class: Log4JPlugin
 * 
 * Purpose: This class is a singleton class which implements Log4J and load the
 * AFTLog4J.properties file.
 */

package com.ags.aft.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.util.Helper;

/**
 * Log4JPlugin class is singleton class and load the AFTLog4J.properties file.
 */
public final class Log4JPlugin {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Log4JPlugin.class);
	private static final String DEBUG_TRUE = "true";
	private static final String DEFAULTLOGFILENAME = "AutomatedFunctionalTester";

	private String logFileName = null;

	private String debug = "true";

	private Properties props = new Properties();

	private java.util.Date startDateTime;

	private static Log4JPlugin instance = null;
	/**
	 * The Log4JPlugin constructor instance.
	 */
	private Log4JPlugin() {
		super();
		LOGGER.trace("Into Log4JPlugin private constructor");
	}

	/**
	 * @return instance of log4j
	 */
	public static Log4JPlugin getInstance() {
		if (instance == null) {
			LOGGER.trace("Creating ApplicationConfiguration Instance");
			instance = new Log4JPlugin();
			LOGGER.trace("Successfully created ApplicationConfiguration Instance");
		}

		return instance;
	}

	/**
	 * load the AFTLog4J.properties and set the logging output file path to
	 * AFTLog4J.properties file.
	 * 
	 * @param filePath
	 *            log file path
	 * @param fileName
	 *            log file name
	 * @param testStartTime
	 *            start time when test started execution
	 * @throws AFTException
	 */
	public void init(String filePath, String fileName,
			java.util.Date testStartTime) throws AFTException {

		this.startDateTime = testStartTime;

		LogLog.setInternalDebugging(DEBUG_TRUE.equalsIgnoreCase(debug));
		String logfileName = "";
		if (fileName != null) {
			// This defines the logger configuration for all classes in this
			// application
			String configFile = filePath + "/" + fileName;
			props = new Properties();
			FileInputStream istream = null;
			try {
				istream = new FileInputStream(configFile);
				props.load(istream);
				//setLogFileName(props.getProperty("log4j.appender.R.File"));
			} catch (IOException ie) {
				LOGGER.warn("Exception::", ie);
				LOGGER.trace("Log4j properties file path ["
						+ configFile
						+ "] looks incorrect. Pls check the configuration property [Log4JPropertiesFilePath] and run again.\n");
				if (ie.toString().contains("java.io.FileNotFoundException")) {
					Helper.getInstance().setFrameworkFileFound(false);
				}

				throw new AFTException(ie);
			} finally {
				try {
					if (istream != null) {
						istream.close();
					}
				} catch(Exception e) {
					LOGGER.error("Exception::", e);
				}
			}

			LOGGER.trace("Log file path = " + getLogFilePath());
			File f = new File(getLogFilePath());

			// Make sure the file or directory exists and isn't write protected
			if (f.exists() && f.isDirectory() && f.canWrite()) {
				LOGGER.trace("Valid log file path [" + getLogFilePath() + "]");
			} else {
				LOGGER.trace("Invalid log file path [" + getLogFilePath()
						+ "] specified. Please correct and run again.\n");
				throw new AFTException(
						"Invalid logfile path specified " + getLogFilePath());
			}

			DateFormat formatter = new SimpleDateFormat(
					Constants.DATEFORMATLOGFILE);
			String timestamp = formatter.format(testStartTime);
			logfileName = getLogFileName();
			if (logfileName == null || logfileName.length() <= 0) {
				logfileName = DEFAULTLOGFILENAME;
			}
			props.setProperty("log4j.appender.R.File", getLogFilePath() + "//"
					+ logfileName + "_" + timestamp + ".log");
			props.setProperty("log4j.appender.EMAIL.Subject",
					"Email Notification from AFT");
			PropertyConfigurator.configure(props);
		}

		// We want to stop any internal logging of messages -- just in case
		// we get too wordy.
		LogLog.setInternalDebugging(false);
	}

	/**
	 * Set the log4j.appender.EMAIL.To and log4j.appender.EMAIL.Cc properties of
	 * Log4j.
	 * 
	 * @param toAddress
	 *            To addr to send email notification
	 * @param ccAddress
	 *            CC addr to send email notification
	 * 
	 */
	public void setEmailNotificationForToAndCc(String toAddress,
			String ccAddress) {
		props.setProperty("log4j.appender.EMAIL.To", toAddress);
		props.setProperty("log4j.appender.EMAIL.Cc", ccAddress);
		PropertyConfigurator.configure(props);
	}

	/**
	 * 
	 * This method changes the loglevel for the com.ags package at runtime
	 * 
	 * @param logLevel
	 *            loglevel to set. This must be a valid log level, else a
	 *            warning message is thrown and no action is performed
	 * 
	 */
	public void setLogLevel(String logLevel) {
		String aftLogLevel = props.getProperty("log4j.logger.com.ags");

		if (logLevel.compareToIgnoreCase("trace") == 0
				|| logLevel.compareToIgnoreCase("debug") == 0
				|| logLevel.compareToIgnoreCase("info") == 0
				|| logLevel.compareToIgnoreCase("warn") == 0
				|| logLevel.compareToIgnoreCase("error") == 0
				|| logLevel.compareToIgnoreCase("fatal") == 0) {

			if (aftLogLevel.contains("trace")) {
				aftLogLevel = aftLogLevel.replace("trace",
						logLevel.toLowerCase());
			} else if (aftLogLevel.contains("debug")) {
				aftLogLevel = aftLogLevel.replace("debug",
						logLevel.toLowerCase());
			} else if (aftLogLevel.contains("info")) {
				aftLogLevel = aftLogLevel.replace("info",
						logLevel.toLowerCase());
			} else if (aftLogLevel.contains("warn")) {
				aftLogLevel = aftLogLevel.replace("warn",
						logLevel.toLowerCase());
			} else if (aftLogLevel.contains("error")) {
				aftLogLevel = aftLogLevel.replace("error",
						logLevel.toLowerCase());
			} else if (aftLogLevel.contains("fatal")) {
				aftLogLevel = aftLogLevel.replace("fatal",
						logLevel.toLowerCase());
			}
		} else {
			// oops, incorrect value, throw warning message
			LOGGER.warn("Invalid Log level ["
					+ logLevel
					+ "], Please check AFT or log4j documentation for supported log levels.");
		}

		props.setProperty("log4j.logger.com.ags", aftLogLevel);
		PropertyConfigurator.configure(props);
	}

	/**
	 * Gets the path
	 * 
	 * @return Log file path
	 * @throws AFTException
	 */
	public String getLogFilePath() throws AFTException {
		String logFilePath = null;
		if (ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.LOGFILE_PATH) != null
				&& !ConfigProperties.getInstance()
						.getConfigProperty(ConfigProperties.LOGFILE_PATH)
						.isEmpty()) {
			logFilePath = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.LOGFILE_PATH);
		} else {
			logFilePath = ConfigProperties.DEFAULT_LOGFILE_PATH;
		}
		return logFilePath;
	}

	/**
	 * This method returns the Log file
	 * 
	 * @return log file name
	 */
	public String getLogFile() {
		String logFile = null;
		logFile = props.getProperty("log4j.appender.R.File");
		return logFile;
	}

	/**
	 * Write spell errors.
	 * 
	 * @param strText
	 *            text to write
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void writeSpellErrors(String strText) throws AFTException {

		FileWriter spellErrorFW = null;
		BufferedWriter spellErrorBW = null;
		String timestamp = null;
		try {

			DateFormat formatter = new SimpleDateFormat(
					Constants.DATEFORMATLOGFILE);
			timestamp = formatter.format(startDateTime);

			String spellErrorFileName = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.LOGFILE_PATH)
					+ "//"
					+ "spellErrors" + "_" + timestamp + ".log";

			// open in Append mode
			spellErrorFW = new FileWriter(spellErrorFileName, true);
			spellErrorBW = new BufferedWriter(spellErrorFW);

			spellErrorBW.append(strText);

		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (spellErrorBW != null) {
				try {
					spellErrorBW.close();
				} catch (IOException io) {
					// ignore
				}
			}

			if (spellErrorFW != null) {
				try {
					spellErrorFW.close();
				} catch (IOException io) {
					// ignore
				}
			}
		}
	}

	/**
	 * Write link errors.
	 * 
	 * @param urlLink
	 *            link url
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void writeLinkErrors(String urlLink) throws AFTException {

		FileWriter linkErrorFW = null;
		BufferedWriter linkErrorBW = null;
		String timestamp = null;
		try {

			DateFormat formatter = new SimpleDateFormat(
					Constants.DATEFORMATLOGFILE);
			timestamp = formatter.format(startDateTime);

			String linkErrorFileName = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.LOGFILE_PATH)
					+ "//"
					+ "linkErrors" + "_" + timestamp + ".log";

			// open in Append mode
			linkErrorFW = new FileWriter(linkErrorFileName, true);
			linkErrorBW = new BufferedWriter(linkErrorFW);

			linkErrorBW.append(urlLink);

		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			if (linkErrorBW != null) {
				try {
					linkErrorBW.close();
				} catch (IOException io) {
					// ignore
				}
			}

			if (linkErrorFW != null) {
				try {
					linkErrorFW.close();
				} catch (IOException io) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * set log File Name
	 */
	public void setLogFileName(String logFile) {
		logFileName = logFile;
	}

	/**
	 * @return the logFileName
	 */
	public String getLogFileName() {
		return logFileName;
	}
}
