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
 * Class: ScreenshotCapture
 * 
 * Purpose: This class contains utility methods to capture the screen shots.
 */


package com.ags.aft.common;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.testObjects.TestStep;

/**
 * The Class ScreenshotCapture.
 */
public final class ScreenshotCapture {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ScreenshotCapture.class);

	/** The screenshotCapture. */
	private static ScreenshotCapture screenshotCapture;

	/**
	 * Instantiates a new screenshotCapture.
	 */
	private ScreenshotCapture() {
		super();
	}

	/**
	 * Gets the single instance of ScreenshotCapture.
	 * 
	 * @return single instance of ScreenshotCapture
	 */
	public static ScreenshotCapture getInstance() {
		if (screenshotCapture == null) {
			screenshotCapture = new ScreenshotCapture();
			LOGGER.trace("Creating instance of screenshotCapture");
		}

		return screenshotCapture;
	}
	
	/**
	 * This returns when the capture screenshot should be done.
	 * 
	 * @return String as "All" or "None", two possible values for capture
	 *         screenshot in AFTConfig
	 * @throws AFTException
	 *             the application exception
	 */
	public String captureScreenShotWhen() throws AFTException {
		String captureScreenShot = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.CAPTURESCREENSHOT);

		if (captureScreenShot.isEmpty()) {
			LOGGER.warn("CaptureScreenShot value not specified in AFTConfig.properties file, setting default value to ["
					+ ConfigProperties.DEFAULT_CAPTURE_SCREENSHOT_VALUE + "]");
			captureScreenShot = ConfigProperties.DEFAULT_CAPTURE_SCREENSHOT_VALUE;
			LOGGER.info("CaptureScreenShot value defaulted to ["
					+ captureScreenShot + "]");
		}

		return captureScreenShot;
	}

	/**
	 * This returns if screenshot should be captured or not.
	 * 
	 * @param ts
	 *            testStep object for which screenshot is being captured
	 * @param isError
	 *            indicates if an error has occurred while executing this step
	 * @return boolean true, if screenshot should be captured and false if it
	 *         should not be captured
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean isCaptureScreenShot(TestStep ts, boolean isError)
			throws AFTException {

		boolean captureScreenShot = false;
		String capture = captureScreenShotWhen();

		// check on which conditions we need to capture screenshot...
		if (capture.compareToIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ALL) == 0) {
			// If configuration is all, just set the captureScreenShot to TRUE
			captureScreenShot = true;
		} else if ((capture
				.compareToIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_VERIFY) == 0)
				&& ((ts.getAction().toLowerCase().startsWith("verify")) || (isError))) {
			// If configuration is Verify, we need to see if the action starts
			// with verify or not. If it does, set the captureScreenShot to TRUE
			captureScreenShot = true;
		} else if ((isError)
				&& (capture
						.compareToIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ERROR) == 0)) {
			// If configuration is Error, we need to see isError is true or not.
			// If it does, set the captureScreenShot to TRUE
			captureScreenShot = true;
		}

		// now check on which actions we should not capture screenshot and set
		// it to false...
		for (int iLoop = 0; iLoop < Constants.NONUIACTIONS.length; iLoop++) {
			String actionName = Constants.NONUIACTIONS[iLoop].toLowerCase();
			if (ts.getAction().toLowerCase().contains(actionName)) {
				captureScreenShot = false;
				break;
			}
		}

		return captureScreenShot;
	}

	/**
	 * This returns if service request/response should be stored or not.
	 * 
	 * @param ts
	 *            testStep object for which service request/response is being
	 *            captured
	 * @param isError
	 *            indicates if an error has occurred while executing this step
	 * @return boolean true, if service request/response should be captured and
	 *         false if it should not be captured
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean isCaptureServiceRequest(TestStep ts, boolean isError)
			throws AFTException {

		boolean captureServiceRequest = false;
		String capture = captureScreenShotWhen();

		if (ts.getAction().toUpperCase().startsWith("WS_")
				&& ((capture
						.compareToIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ALL) == 0) || (isError && (capture
						.compareToIgnoreCase(ConfigProperties.CAPTURESCREENSHOT_ERROR) == 0)))) {

			captureServiceRequest = true;
		}

		return captureServiceRequest;
	}

	/**
	 * Capture service request/response.
	 * 
	 * @param ts
	 *            the ts
	 * @param serviceRequestFilePath
	 *            the service request/response file path
	 * @param serviceRequestFileName
	 *            the service request/response file name
	 * @throws AFTException
	 *             the application exception
	 */
	public void captureServiceRequest(TestStep ts, String serviceRequestResponseXMLValue,
			String serviceRequestFilePath, String serviceRequestFileName, String responseType)
			throws AFTException {

		String serviceRequestFile = serviceRequestFilePath + '/'
				+ serviceRequestFileName;

		boolean saveFile = false;
		boolean isRequest = false;


		if ((ts.getAction().compareToIgnoreCase("WS_LoadRequest") == 0)
				|| (ts.getAction().compareToIgnoreCase("WS_SubstituteValue") == 0)) {
			saveFile = true;
			isRequest = true;
		} else if ((ts.getAction().compareToIgnoreCase("WS_SendRequest") == 0)
				|| (ts.getAction().compareToIgnoreCase("WS_ValidateValue") == 0)
				|| (ts.getAction().compareToIgnoreCase("WS_GetValue") == 0)) {
			saveFile = true;
			isRequest = false;
		}

		if (saveFile) {

			if (responseType.equalsIgnoreCase(Constants.XML)) {
				// Create the service request/response file path
				XMLParser.convertXMLStringToFile(
						serviceRequestResponseXMLValue, serviceRequestFile);
			} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
				// Create the service request/response file path
				//Added the below code to support rendering of JSON data in UI Central- JIRA 1626
				serviceRequestResponseXMLValue = "<jsondata><![CDATA[ " + serviceRequestResponseXMLValue + "  ]]></jsondata>";
				JsonParser.convertJSONStringToFile(
						serviceRequestResponseXMLValue, serviceRequestFile);
			}
		   ts.setWsResponseType(responseType);
			if (isRequest) {
				ts.setServiceRequestName(serviceRequestFile);
			} else {
				ts.setServiceResponseName(serviceRequestFile);
			}
			LOGGER.debug("Successfully captured service request/response to file ["
					+ serviceRequestFile + "]");
		}
	}

	/**
	 * Create folder for capturing screen shots.
	 * 
	 * @param testStartTime
	 *            time when the test started to create folder for capturing
	 *            screenshots for this run
	 * @param testSuiteName
	 *            the test suite name
	 * @param isCaptureScreenShotNow
	 *            the is capture screen shot now
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String createScreenShotPath(java.util.Date testStartTime,
			String testSuiteName, boolean isCaptureScreenShotNow)
			throws AFTException {

		String screenShotPath = "";
		try {
			if (captureScreenShotWhen().compareToIgnoreCase(
					ConfigProperties.CAPTURESCREENSHOT_NONE) != 0) {
				screenShotPath = ConfigProperties.getInstance()
						.getConfigProperty(ConfigProperties.SCREENSHOT_PATH);

				if (screenShotPath.isEmpty()) {
					LOGGER.warn("Screenshot path not specified in AFTConfig.properties file, setting default value to ["
							+ ConfigProperties.DEFAULT_SCREENSHOTS_PATH + "]");
					screenShotPath = ConfigProperties.DEFAULT_SCREENSHOTS_PATH;
					LOGGER.info("Screenshot path defaulted to ["
							+ screenShotPath + "]");
				}
				screenShotPath = createFileDir(screenShotPath, testStartTime,
						testSuiteName, "Screen Shot");
				LOGGER.info("Created a folder [" + screenShotPath
						+ "] under screen shot with current timestamp");
			} else if (isCaptureScreenShotNow) {
				screenShotPath = ConfigProperties.DEFAULT_SCREENSHOTS_PATH;
				screenShotPath = createFileDir(screenShotPath, testStartTime,
						testSuiteName, "Screen Shot");
				LOGGER.info("Created a folder [" + screenShotPath
						+ "] under screen shot with current timestamp");
			} else {
				LOGGER.info("CaptureScreenshot configuration value is set to 'None'. No screenshots will be captured");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return screenShotPath;
	}
	/**
	 * Creates the service request/response path.
	 * 
	 * @param testStartTime
	 *            the test start time
	 * @param testSuiteName
	 *            the test suite name
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String createServiceRequestPath(java.util.Date testStartTime,
			String testSuiteName) throws AFTException {

		String serviceRequestPath = "";
		try {
			if (captureScreenShotWhen().compareToIgnoreCase(
					ConfigProperties.CAPTURESCREENSHOT_NONE) != 0) {
				serviceRequestPath = ConfigProperties
						.getInstance()
						.getConfigProperty(ConfigProperties.SERVICEREQUEST_PATH);

				if (serviceRequestPath.isEmpty()) {
					LOGGER.warn("ServiceRequests Folder path not specified in AFTConfig.properties file, setting default value to ["
							+ ConfigProperties.DEFAULT_SERVICEREQUEST_PATH
							+ "]");
					serviceRequestPath = ConfigProperties.DEFAULT_SERVICEREQUEST_PATH;
					LOGGER.info("ServiceRequests folder path defaulted to ["
							+ serviceRequestPath + "]");
				}
				File f = new File(serviceRequestPath);
				if (!f.exists() || !f.isDirectory() || !f.canWrite()) {
					LOGGER.warn("Invalid path ["
							+ serviceRequestPath
							+ "] specified for service request/response. Service request/response may not be captured for this run. Pls check for [ServiceRequestPath] value in AFTConfig.xml file.");
				} else {
					serviceRequestPath = createFileDir(serviceRequestPath,
							testStartTime, testSuiteName, "Service Request");
					LOGGER.info("Created serviceRequest folder ["
							+ serviceRequestPath
							+ "] under service request/response with current timestamp");
				}
			} else {
				LOGGER.info("CaptureScreenshot configuration value is set to 'None'. No screenshots will be captured");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return serviceRequestPath;
	}
	
	/**
	 * Create folder for capturing screen shot (or) service request.
	 * 
	 * @param path
	 *            the path
	 * @param testStartTime
	 *            time when the test started to create folder for capturing
	 *            screenshots or service requests or report xml's for this run
	 * @param testSuiteName
	 *            the test suite name
	 * @param sourceName
	 *            the source name
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String createFileDir(String path, java.util.Date testStartTime,
			String testSuiteName, String sourceName) throws AFTException {
		String filePath = path;
		try {

			File f = new File(filePath);
			if (!f.exists() || !f.isDirectory() || !f.canWrite()) {
				LOGGER.warn("Invalid path [" + filePath + "] specified for "
						+ sourceName + ". Pls check for [" + sourceName
						+ "] path value in AFTConfig.xml file.");
			} else {
				// Create a place holder to store the screen shots or service
				// requests.
				DateFormat formatter = new SimpleDateFormat(
						Constants.DATEFORMATFOLDERNAME);
				String timestamp = formatter.format(testStartTime);
				filePath = filePath + "/" + testSuiteName + "_" + timestamp;
				(new File(filePath)).mkdir();
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return filePath;
	}
	
	/**
	 * Write screen shot.
	 * 
	 * @param screenShotFilePath
	 *            the screen shot file path
	 */
	public void writeScreenShot(String screenShotFilePath) {
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

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
	}
}
