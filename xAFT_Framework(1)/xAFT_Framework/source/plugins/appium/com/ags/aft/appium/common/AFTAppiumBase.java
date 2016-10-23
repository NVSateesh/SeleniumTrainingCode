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
 * Class: AFTRobotiumBase
 * 
 * Purpose: This class has implement methods to setup/teardown Selenium Server
 * and client.
 */

package com.ags.aft.appium.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.ags.aft.exception.AFTException;
import com.jayway.android.robotium.remotecontrol.solo.SoloTest;

/**
 * Base class for robotium testcases. This class has basic methods useful for
 * all Appium testcases.
 */
public final class AFTAppiumBase extends SoloTest {
	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(AFTAppiumBase.class);

	/** The Constant INSTRUMENT. */
	public static final String INSTRUMENT = "com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner";

	// Instance object
	/** The appium base. */
	private static AFTAppiumBase appiumBase;

	static WebDriver driver;

	/**
	 * Instantiates a new aFT appium base.
	 * 
	 * @param args
	 *            the args
	 */
	public AFTAppiumBase(String[] args) {
		super(args);
	}

	/*
	 * public AFTRobotiumBase(String messengerApk, String testRunnerApk, String
	 * instrumentArg) { super(messengerApk, testRunnerApk, instrumentArg); }
	 */

	/**
	 * Instantiates a new aFT appium base.
	 * 
	 */
	public AFTAppiumBase() {
	}

	/**
	 * Gets the single instance of AFTAppiumBase.
	 * 
	 * @return single instance of AFTAppiumBase
	 */
	public static AFTAppiumBase getInstance() {
		if (appiumBase == null) {
			LOGGER.trace("Creating instance of AFTAppiumBase");
			appiumBase = new AFTAppiumBase();
			return appiumBase;
		}
		return appiumBase;
	}

	public void startAppium() throws AFTException {

		LOGGER.info("Starting the Webdriver appium client..");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "iOS");
		capabilities.setCapability(CapabilityType.VERSION, "6.0");
		capabilities.setCapability(CapabilityType.PLATFORM, "Mac");
		capabilities.setCapability("app", AppiumConfigProperties.getInstance()
				.getConfigProperty("applicationPath"));
		LOGGER.info("Please be patient while we start instrumenting your Application. This might take few seconds...");
		try {
			driver = new RemoteWebDriver(new URL(AppiumConfigProperties
					.getInstance().getConfigProperty("Server")
					+ ":"
					+ AppiumConfigProperties.getInstance().getConfigProperty(
							"port") + "/wd/hub"), capabilities);

			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage());
		}
	}

	/**
	 * Stops appium Instance.
	 */
	public void stopAppium() throws AFTException {
		if (driver != null) {
			try {
				// Close the application on Device
				LOGGER.info("Stopping the Appium Client..");
				driver.quit();
				LOGGER.info("Appium Client Stopped");
			} catch (IllegalThreadStateException e) {
				LOGGER.error(e);
				throw new AFTException(e);
			}
		}
	}

	/**
	 * Tears down the appium objects.
	 */
	public void teardown() throws AFTException {
		try {
			LOGGER.trace("Calling stopAppium()");
			stopAppium();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new AFTException(e);
		}
	}

	/**
	 * Getter for solo object.
	 * 
	 * @return the solo
	 */
	public WebDriver getDriver() {
		return driver;
	}

}
