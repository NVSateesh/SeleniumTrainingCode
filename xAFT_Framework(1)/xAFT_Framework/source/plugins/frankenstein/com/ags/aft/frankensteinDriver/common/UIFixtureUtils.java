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
 * Class: Helper
 * 
 * Purpose: This class contains utility methods to read configuration files,
 * validate test data header and send email notifications etc
 */

package com.ags.aft.frankensteinDriver.common;

import java.io.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import com.ags.aft.exception.AFTException;
import com.thoughtworks.frankenstein.drivers.FrankensteinDriver;

/**
 * The Class UIFixtureUtils.
 */
public final class UIFixtureUtils {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(UIFixtureUtils.class);

	/** The UIFixtureUtils */
	private static UIFixtureUtils uiUtils;

	private static String applicationPath;

	/**
	 * Instantiates a new UIFixtureUtils
	 */
	private UIFixtureUtils() {
		super();
	}

	/**
	 * Gets the single instance of UIFixtureUtils.
	 * 
	 * @return single instance of UIFixtureUtils
	 */
	public static UIFixtureUtils getInstance() {
		if (uiUtils == null) {
			uiUtils = new UIFixtureUtils();
			LOGGER.trace("Creating instance of UIFixtureUtils");
		}

		return uiUtils;
	}

	/**
	 * This method is developed to handle null value or empty value for
	 * ElementWaitTime config property. This method checks if the property value
	 * is null or empty, it assigns the default value
	 * 
	 * @return Element wait time value (either defined in config property or if
	 *         not defined, default constant value defined
	 * 
	 */
	public int getElementWaitTime() {
		int iElementWaitTime = Integer
				.parseInt(FrankensteinDriverConfigProperties.DEFAULT_ELEMENT_WAIT_TIME);

		try {
			String elementWaitTime = FrankensteinDriverConfigProperties
					.getInstance()
					.getConfigProperty(
							FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MS);

			if ((elementWaitTime != null) && !elementWaitTime.isEmpty()) {
				iElementWaitTime = Integer.parseInt(elementWaitTime);
			} else {
				LOGGER.warn("Element Wait time not set in AFTConfig.properties file... defaulting the element wait time to ["
						+ FrankensteinDriverConfigProperties.DEFAULT_ELEMENT_WAIT_TIME
						+ "]");
				iElementWaitTime = Integer
						.parseInt(FrankensteinDriverConfigProperties.DEFAULT_ELEMENT_WAIT_TIME);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}

		return iElementWaitTime;
	}

	/**
	 * Constructs browser version string and initializes AFT_BrowserVersion
	 * system variable...
	 * 
	 * @param frankenstein
	 *            frankenstein object
	 * @return Application Path
	 * 
	 * @throws AFTException
	 * 
	 */
	public String getApplicationPath(FrankensteinDriver frankenstein)
			throws AFTException {

		String userAgentString = frankenstein.getClass().toString();
		applicationPath = userAgentString;

		LOGGER.debug("Navigator/application user agent details are ["
				+ userAgentString + "]");

		return applicationPath;
	}

	/**
	 * This method will return the value in string Byte array to string.
	 * 
	 * @param aByteArray
	 *            the a byte array
	 * @return the string
	 */
	public String byteArrayToString(byte[] aByteArray) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();

		for (int i = 0; i < aByteArray.length; i++) {
			if (aByteArray[i] != 0) {
				bs.write(aByteArray[i]);
			}
		}

		return bs.toString();
	}

}
