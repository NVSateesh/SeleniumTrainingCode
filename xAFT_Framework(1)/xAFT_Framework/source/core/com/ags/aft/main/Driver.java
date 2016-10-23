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
 * Class: Driver
 * 
 * Purpose: This class has the main method which drives the execution.
 */

package com.ags.aft.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.common.RuntimeProperties;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.runners.TestBatchRunner;
import com.ags.aft.util.FileUtil;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Notifications;
import com.ags.aft.version.Version;

public final class Driver {

	// hiding the dummy constructor as we do not this class every to be
	// instantiated...
	/**
	 * constructor for Driver.
	 */
	private Driver() {

	}

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Driver.class);
		
	private static String aftConfigurationFilePath = "./configuration/AFTConfig.xml";
	private static String aftLogPropertiesFileName = "AFTLog4J.properties";
	private static TestBatchRunner testBatchRunner;

	/**
	 * main method implementation * @param args arguments
	 */
	public static void main(String args[]) {		
		AFTException applException = null;
		// Startup date/time stamp...
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
		// format to AFTConstants
		java.util.Date testStartTime = new java.util.Date();
		// Getting test batch file from command line arguments
		String testResultEmailNotif = "";

		try {
			LOGGER.trace("Loading config properties file ["
					+ aftConfigurationFilePath + "]");

			if (args.length > 0) {
				LOGGER.trace("Command line arguments length [" + args.length
						+ "]");
				RuntimeProperties.getInstance().readCommandLineArguments(args);
			}

			// Load configuration properties file...
			if (Helper.getInstance().isFileSystemRequest()) {
				// Load configuration properties file...
				ConfigProperties.getInstance()
						.loadConfigPropertiesFromFileSystem(
								aftConfigurationFilePath);
			} else {
				ConfigProperties.getInstance().loadConfigPropertiesFromDB();
			}

			LOGGER.trace("Successfully loaded config properties file ["
					+ aftConfigurationFilePath + "]");

			// Initialize logger
			// This needs to be print statement as the logger is not yet
			// initialized
			LOGGER.info("Initializing logger. LogFilePath ["
					+ ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.LOG4J_PROPERTIES_FILEPATH)
					+ "], LogFileName [" + aftLogPropertiesFileName + "]");
			Log4JPlugin.getInstance().init(
					ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.LOG4J_PROPERTIES_FILEPATH),
					aftLogPropertiesFileName, testStartTime);

			// Startup logging...
			LOGGER.info("-----------------------------------------------------------------------------------------------------------------------");
			LOGGER.info("Started Automated Functional Tester (AFT) version ["
					+ Version.getVersion() + "] at ["
					+ dateFormat.format(testStartTime) + "]");
			LOGGER.info("-----------------------------------------------------------------------------------------------------------------------");
			/*
			 * LOGGER.info("Batch file from command line arguments [" +
			 * testBatchFilePath + "]");
			 */
			// Create & Init TestBatchRunner object
			//
			LOGGER.trace("Creating instance of AFTTestBatchRunner");
			testBatchRunner = new TestBatchRunner(testStartTime);
			// call the initSystemVariables
			testBatchRunner.initSystemVariables();

			// calling shutdown hook
			Helper.getInstance().attachShutDownHook(testBatchRunner);

			// Read email notification properties and send test started
			// notification
			//
			testResultEmailNotif = ConfigProperties.getInstance()
					.getConfigProperty(
							ConfigProperties.TEST_EXECUTION_RESULT_EMAILNOTIF);
			if ((testResultEmailNotif == null)
					|| (testResultEmailNotif.length() <= 0)) {
				testResultEmailNotif = Constants.DEFAULTSENDFRAMEWORKNOTIFICATIONEMAILS;
			} else if (!(testResultEmailNotif.equalsIgnoreCase("Yes") || testResultEmailNotif
					.equalsIgnoreCase("No"))) {
				testResultEmailNotif = Constants.DEFAULTSENDFRAMEWORKNOTIFICATIONEMAILS;
			}
			// Send email notification
			if (testResultEmailNotif.equalsIgnoreCase("Yes")) {
				Notifications.getInstance().sendTestStartedNotification(
						Constants.TESTSTARTEMAILNOTIF);
			}
			// Reports and Log file Retention policy check
			LOGGER.info("Checking report file retention policy...");
			FileUtil.getInstance().fileRetentionPeriodCheck();

			LOGGER.trace("Starting execution of functional test scenarios");
			testBatchRunner.execute();

		} catch (AFTException f) {
			applException = f;
			LOGGER.error(f.getMessage()
					+ "\nAction:Please check the selenium server host Id or port number configuration in AFTConfig.properties file");
			LOGGER.error("Exception::", f);
			LOGGER.trace("Exception:: [" + f.getLocalizedMessage() + "]");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			LOGGER.trace("Exception:: [" + e.getLocalizedMessage() + "]");
			if (e.getLocalizedMessage().contains(
					"Could not start Selenium session: Connection timed out")) {
				LOGGER.error("Please check the selenium server host Id or port number configuration in AFTConfig.properties file");
			}
		} finally {
			try {
				// Send Log and Test Report as an Email
				if (testResultEmailNotif.equalsIgnoreCase("Yes")) {
					Notifications.getInstance().sendTestStartedNotification(
							Constants.TESTENDEMAILNOTIF);
				}
			} catch (Exception e) {
				// ignore the exception
				LOGGER.error("Exception::", e);
			}
			java.util.Date testEndTime = new java.util.Date();
			long timeDiff = testEndTime.getTime() - testStartTime.getTime();
			long hr = timeDiff / 1000 / 60 / 60;
			long min = timeDiff / 1000 / 60 - hr * 60;
			long sec = timeDiff / 1000 - hr * 60 * 60 - min * 60;
			long msec = timeDiff - (hr * 60 * 60 - min * 60 - sec) * 1000;
			LOGGER.info("------------------------------------------------------------------------------------------");
			LOGGER.info("Completed execution of Automated Functional Tester (AFT)"
					+ " of test run at : " + dateFormat.format(testEndTime));
			LOGGER.info("Total execution time for this test run was : " + hr
					+ ":" + min + ":" + sec + ":" + msec);
			LOGGER.info("------------------------------------------------------------------------------------------");

			// If any of the specifed file not found, set the System exit code
			// to 1 to show
			// the jenkins job as failure
			if (!Helper.getInstance().allFrameworkFileFound()) {
				LOGGER.debug("The system cannot find the file specified, Setting the system exit code to 1");
				System.exit(1);
			}
			// If any of the tests fails, set the System exit code to 1 to show
			// the jenkins job as failure
			if (ReportGenerator.getInstance().getFailCount() > 0) {
				LOGGER.debug("There are test case failures, Setting the system exit code to 1");
				System.exit(1);
			}

			if (applException != null) {
				LOGGER.debug("There are errors, Setting the system exit code to 1");
				System.exit(1);
			}

			// if the engine is of type robotium, close the screen
			if (EngineManager.getInstance().isRobotiumPresent()) {
				// close the droid screen
				System.exit(0);
			}

		}
	}
}
