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
 * Class: TCMIntegration
 * 
 * Purpose: Integration connector for Test case Management tool integration
 */

package com.ags.aft.integrations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.ags.aft.common.DynamicClassLoader;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;

/**
 * The Class TCMInteration.
 */
@SuppressWarnings("rawtypes")
public final class TCMIntegration {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TCMIntegration.class);

	/** The Constant CLASS_TO_LOAD. */
	private static String classToLoad = null;

	/** The tcm integration. */
	private static TCMIntegration tcmIntegration = null;

	/** The qmetry login token. */
	private String tcmConnectionToken = null;

	/** The tcm connector class. */
	private Class tcmConnectorClass = null;

	/** The tcm connector object. */
	private Object tcmConnectorObject = null;

	/**
	 * Instantiates a new tCM integration.
	 * 
	 * @throws AFTException
	 *             the application exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	private TCMIntegration() throws AFTException, InstantiationException,
			IllegalAccessException {

		// Load the TCMIntegration jar dynamically.
		this.tcmConnectorClass = loadTCMIntegrationConnectorClass();

		// Create a dynamic object for TCMIntegration jar
		this.tcmConnectorObject = this.tcmConnectorClass.newInstance();
	}

	/**
	 * Gets the single instance of TCMIntegration.
	 * 
	 * @return single instance of TCMIntegration
	 * @throws AFTException
	 */
	public static TCMIntegration getInstance() throws AFTException {
		if (tcmIntegration == null) {
			// Create instance of TCMIntegration class
			LOGGER.debug("Creating instance of TCMIntegration class");
			try {
				tcmIntegration = new TCMIntegration();
				LOGGER.debug("Successfully created instance of TCMIntegration class");

			} catch (AFTException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (InstantiationException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			return tcmIntegration;
		}
		return tcmIntegration;
	}

	/**
	 * Checks if is tCM instance.
	 * 
	 * @param tcmConfigFile
	 *            the tcm config file
	 * @return tcmConnectionToken
	 * @throws AFTException
	 *             the application exception
	 */
	@SuppressWarnings("unchecked")
	public String init(String tcmConfigFile) throws AFTException {
		// Load the AFTTCMIntegrationConnector jar
		LOGGER.debug("Loading AFT TCM Integration Connector adapter from path ["
				+ ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.TCM_INTEGRATION_JAR_PATH) + "]");
		LOGGER.debug("Initializing connection with TCM tool using AFT TCM Connector adapter. Config file ["
				+ tcmConfigFile + "]");

		// Call the init method dynamically
		LOGGER.debug("Making a dynamic call to Init method..");
		Method initMethod = null;
		try {
			initMethod = tcmConnectorClass.getMethod("init",
					new Class[] { String.class });
		} catch (SecurityException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (NoSuchMethodException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		// Invoke method using reflection.
		LOGGER.debug("Invoking dynamic method [" + initMethod.getName() + "]");
		try {
			this.tcmConnectionToken = (String) initMethod.invoke(
					tcmConnectorObject, tcmConfigFile);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (InvocationTargetException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		if (this.tcmConnectionToken != null) {
			LOGGER.info("Successfully created token [" + tcmConnectionToken
					+ "] for connection with TCM tool");
		} else {
			LOGGER.error("Successfully created token [" + tcmConnectionToken
					+ "] for connection with TCM tool");
			throw new AFTException("Successfully created token ["
					+ tcmConnectionToken + "] for connection with TCM tool");
		}

		return this.tcmConnectionToken;
	}

	/**
	 * Log qmetry test execution result.
	 * 
	 * @param testCaseId
	 *            the test case id
	 * @param additionalInfo
	 *            the additional info
	 * @param testResult
	 *            the test result
	 * @param screenShotPaths
	 *            the screen shot paths
	 * @param serviceRequestPaths
	 *            the service request paths
	 * @param serviceResponsePaths
	 *            the service response paths
	 * @throws AFTException
	 *             the application exception
	 */
	@SuppressWarnings("unchecked")
	public void logQmetryTestExecutionResult(String testCaseId,
			String additionalInfo, String testResult,
			List<String> screenShotPaths, List<String> serviceRequestPaths,
			List<String> serviceResponsePaths) throws AFTException {

		LOGGER.debug("Logging result for Test Case [" + testCaseId
				+ "] with test result [" + testResult + "] to QMetry");

		// Call the init method dynamically
		LOGGER.debug("Making a dynamic call to Init method..");
		Method executeResultMethod = null;
		try {
			executeResultMethod = tcmConnectorClass.getMethod(
					"logTestExecutionResult", new Class[] { String.class,
							String.class, String.class, String.class,
							ArrayList.class, ArrayList.class, ArrayList.class });
		} catch (SecurityException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (NoSuchMethodException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		// Invoke method using reflection.
		LOGGER.debug("Invoking dynamic method ["
				+ executeResultMethod.getName() + "]");
		try {
			executeResultMethod.invoke(this.tcmConnectorObject,
					this.tcmConnectionToken, testCaseId, additionalInfo,
					testResult, screenShotPaths, serviceRequestPaths,
					serviceResponsePaths);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (InvocationTargetException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		LOGGER.info("Successfully logged result for Test Case [" + testCaseId
				+ "] with test result [" + testResult + "] in QMetry");
	}

	/**
	 * Load aft qmetry connector class.
	 * 
	 * @return the class
	 * @throws AFTException
	 *             the application exception
	 */
	private Class loadTCMIntegrationConnectorClass() throws AFTException {
		Class tcmIntegrationClass = null;
		try {
			// initialize URL object with empty path
			URL urls[] = {};
			// create instance
			LOGGER.debug("Creating an instance for DynamicClassLoader Class");

			// Call the laoder class to load the TCMIntegration jar
			DynamicClassLoader loader = new DynamicClassLoader(urls);

			LOGGER.info("Reading the TCM Config property file");
			String jarPath = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.TCM_INTEGRATION_JAR_PATH);

			// Load the jar file
			LOGGER.debug("Loading [" + jarPath + "] in runtime");
			loader.addFile(jarPath);
			classToLoad = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.TCM_INTEGRATION_CLASS_NAME);
			// load the class
			LOGGER.debug("Instantiating class [" + classToLoad + "] in runtime");
			tcmIntegrationClass = loader.loadClass(classToLoad);
			LOGGER.info("Class [" + classToLoad + "] instantiated successfully");
		} catch (Exception ex) {
			LOGGER.error("Exception::", ex);
			throw new AFTException(ex);
		}
		return tcmIntegrationClass;
	}
}
