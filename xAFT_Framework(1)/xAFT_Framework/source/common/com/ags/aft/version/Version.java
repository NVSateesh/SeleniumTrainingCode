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
 * Class: Version
 * 
 * Purpose: Provides the latest version information of AFT
 */

package com.ags.aft.version;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * The Class aftVersion.
 */
public final class Version {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Version.class);

	private static String version = "";

	// Method to return the version number

	/**
	 * constructor
	 */
	private Version() {
		super();
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {

		return version;
	}

	// Loading build revision properties for build version
	static {
		Properties buildProperties = new Properties();
		try {
			buildProperties.load(new FileInputStream("build_info.properties"));
			version = buildProperties.getProperty("build.initial.number") + "."
					+ buildProperties.getProperty("build.major.number") + "."
					+ buildProperties.getProperty("build.minor.number") + "."
					+ buildProperties.getProperty("build.revision.number");
		} catch (Exception exception) {
			LOGGER.warn("build_info.properties file not available");
		}
	}

}
