package com.ags.aft.enginemanager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ags.aft.constants.SystemVariables;
import com.ags.aft.engine.QTP.XAFTQTPEngine;
import com.ags.aft.engine.appium.XAFTAppiumEngine;
import com.ags.aft.engine.etl.XAFTEtlEngine;
import com.ags.aft.engine.frankensteinDriver.XAFTFrankensteinDriverEngine;
import com.ags.aft.engine.robotium.XAFTRobotiumEngine;
import com.ags.aft.engine.twin.XAFTTwinEngine;
import com.ags.aft.engine.webdriver.XAFTWebDriverEngine;
import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.pluginEngine.IxAFTEngine;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * The Class EngineManager.
 */
public class EngineManager {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(EngineManager.class);

	/** The engine manager. */
	private static EngineManager engineManager;

	/** The engine name. */
	private String currentExecutionEngineName;

	private IxAFTEngine currentExecutionEngine = null;

	/** The robotium present. */
	private boolean robotiumPresent;

	/** The engine map. */
	private Map<String, IxAFTEngine> engineMap = new ConcurrentHashMap<String, IxAFTEngine>();

	/**
	 * Gets the single instance of EngineManager.
	 * 
	 * @return single instance of EngineManager
	 */
	public static EngineManager getInstance() {
		if (engineManager == null) {
			engineManager = new EngineManager();
		}
		return engineManager;
	}

	/**
	 * Gets the current execution engine.
	 * 
	 * @return the current execution engine
	 */
	public IxAFTEngine getCurrentExecutionEngine() {
		return this.currentExecutionEngine;
	}

	/**
	 * Gets the current engine name.
	 * 
	 * @return the current engine name
	 */
	public String getCurrentExecutionEngineName() {
		return currentExecutionEngineName;
	}

	/**
	 * Initialize robotium.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeRobotium(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {
		// Set this flag to close the Droid at screen
		setRobotiumPresent(true);

		IxAFTEngine engine = null;

		LOGGER.info("Initializing the Robotium Engine with [" + appURL + "], ["
				+ appName + "]");

		engine = XAFTRobotiumEngine.getRobotiumEngineInstance();

		// Initialize the robotium engine
		engine.initialize(rootPath, appURL, appName, browser, objRepoPath,
				isPowerUserMode);

		return engine;
	}

	/**
	 * Initialize Appium.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeAppium(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {
		IxAFTEngine engine = null;

		LOGGER.info("Initializing the Appium Engine...");

		engine = XAFTAppiumEngine.getAppiumEngineInstance();

		// Initialize the robotium engine
		engine.initialize(rootPath, appURL, appName, browser, objRepoPath,
				isPowerUserMode);

		return engine;
	}

	/**
	 * Initialize core.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeCore(String objRepoPath,
			boolean isPowerUserMode) throws AFTException {

		IxAFTEngine engine = null;

		// Load object repository associated with the test suite
		String oRFilePath = objRepoPath;
		if (oRFilePath != null && oRFilePath.length() > 0) {
			// Load the object repository file...
			ObjectRepositoryManager.getInstance().loadObjectRepository(
					oRFilePath, isPowerUserMode);

		}

		return engine;
	}

	/**
	 * Initialize frankenstein.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeFrankenstein(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {

		IxAFTEngine engine = null;

		// Initializing Frankenstein
		LOGGER.info("Initializing the Frankenstein Engine with [" + appURL
				+ "], [" + appName + "]");

		engine = XAFTFrankensteinDriverEngine
				.getFrankensteinDriverEngineInstance();

		// Initialize the webdriver engine
		engine.initialize(rootPath, appURL, appName, browser, objRepoPath,
				isPowerUserMode);

		return engine;
	}

	/**
	 * Initialize twin.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeTwin(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {

		IxAFTEngine engine = null;

		LOGGER.info("Initializing the TWIN Engine with [" + appURL + "], ["
				+ appName + "]");
		// Instantiate the Twin engine
		engine = XAFTTwinEngine.getTWINEngineInstance();

		// Initialize the Twin engine properties
		engine.initialize(rootPath, appURL, appName, browser, objRepoPath,
				isPowerUserMode);

		return engine;
	}

	/**
	 * Initialize qtp.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @return
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeQTP(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {

		IxAFTEngine engine = null;

		// Initializing QTP
		LOGGER.info("Initializing the QTP Engine with [" + appURL + "], ["
				+ appName + "]");

		String orPath = objRepoPath.replace(".\\", rootPath + "\\");

		engine = XAFTQTPEngine.getQTPEngineInstance();

		// Initialize the QTP engine
		engine.initialize(rootPath, appURL, appName, browser, orPath,
				isPowerUserMode);

		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_BROWSERVERSION), true,
				engine.getBrowserVersion());

		LOGGER.debug("Value for system variable [AFT_BrowserVersion] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_BROWSERVERSION)) + "]");

		return engine;
	}

	/**
	 * Initialize webdriver.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeWebdriver(String rootPath, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException {

		IxAFTEngine engine = null;

		// get Webdriver instance
		engine = XAFTWebDriverEngine.getWebDriverEngineInstance();
		LOGGER.info("Initializing the WebDriver Engine with [" + appURL
				+ "], [" + appName + "]");

		// Initialize the webdriver configuration
		engine.initialize(rootPath, appURL, appName, browser, objRepoPath,
				isPowerUserMode);

		// Init the System Variable: AFT_BrowserVersion
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_BROWSERVERSION), true,
				engine.getBrowserVersion());

		LOGGER.debug("Value for system variable [AFT_BrowserVersion] set to ["
				+ Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_BROWSERVERSION)) + "]");

		return engine;
	}

	/**
	 * Initialize qtp.
	 * 
	 * @param testSet
	 *            the test set
	 * @param rootPath
	 *            the root path
	 * @param isFileSystemRequest
	 *            the is file system request
	 * @return
	 * @throws AFTException
	 *             the aFT exception
	 */
	private IxAFTEngine initializeETL(boolean isPowerUserMode)
			throws AFTException {

		IxAFTEngine engine = null;

		// Initializing QTP
		LOGGER.info("Initializing the ETL Engine with");
		engine = XAFTEtlEngine.getETLEngineInstance();
		return engine;
	}

	/**
	 * Checks if is robotium present.
	 * 
	 * @return true, if is robotium present
	 */
	public boolean isRobotiumPresent() {
		return robotiumPresent;
	}

	/**
	 * Sets the robotium present.
	 * 
	 * @param robotiumPresent
	 *            the new robotium present
	 */
	public void setRobotiumPresent(boolean robotiumPresent) {
		this.robotiumPresent = robotiumPresent;
	}

	/**
	 * Gets the execution engine.
	 * 
	 * @param engineName
	 *            the engine name
	 * @param appURL
	 *            the app url
	 * @param appName
	 *            the app name
	 * @param browser
	 *            the browser
	 * @param objRepoPath
	 *            the obj repo path
	 * @param isPowerUserMode
	 *            the is power user node
	 * @return the execution engine
	 * @throws IOException
	 * @throws AFTException
	 */
	public IxAFTEngine getExecutionEngine(String engineName, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException, IOException {

		IxAFTEngine engine = null;

		// Check if engine map is not empty and if engine map contains
		// engine name return existing engine or create a new engine object
		if (!engineName.equalsIgnoreCase("core")) {
			if (engineMap != null) {
				LOGGER.debug("Looks like there is an engine object existing inside Engine Map..");
				LOGGER.debug("Checking for Engine [ " + engineName + "]"
						+ "with in the Engine Map");
				if (engineMap.containsKey(engineName.toLowerCase())) {
					LOGGER.debug("Found the engine object + [" + engineName
							+ "]" + "Using the same..");

					// let us get the already created execution engine and
					// return to user
					engine = engineMap.get(engineName.toLowerCase());

					// If engine already exists load the switch engine OR
					if (objRepoPath != null && objRepoPath.length() > 0) {
						// Load the object repository file...
						ObjectRepositoryManager.getInstance()
								.loadObjectRepository(objRepoPath,
										isPowerUserMode);

					}

				}
			}

			if (engine == null) {
				LOGGER.debug("Creating a new Engine object [" + engineName
						+ "]");

				// create the new execution engine
				engine = createExecutionEngine(engineName, appURL, appName,
						browser, objRepoPath, isPowerUserMode);

				// add the newly created engine to the map so that it could be
				// used for execution
				engineMap.put(engineName.toLowerCase(), engine);
			}
		} else {
			engine = initializeCore(objRepoPath, isPowerUserMode);

		}

		// set currently returned execution engine as the current execution
		// engine
		currentExecutionEngine = engine;

		// set the current execution engine name as well
		currentExecutionEngineName = engineName;

		return engine;
	}

	/**
	 * Creates the execution engine.
	 * 
	 * @param engineName
	 *            the engine name
	 * @param appURL
	 *            the app url
	 * @param appName
	 *            the app name
	 * @param browser
	 *            the browser
	 * @param objRepoPath
	 *            the obj repo path
	 * @param isPowerUserMode
	 *            the is power user node
	 * @return
	 * @throws AFTException
	 * @throws IOException
	 */
	private IxAFTEngine createExecutionEngine(String engineName, String appURL,
			String appName, String browser, String objRepoPath,
			boolean isPowerUserMode) throws AFTException, IOException {

		IxAFTEngine engine = null;

		try {
			String rootPath = new File(".").getCanonicalPath();

			if (engineName.compareToIgnoreCase("webdriver") == 0) {
				// Initializing WebDriver
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeWebdriver(rootPath, appURL, appName,
						browser, objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("qtp") == 0) {
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeQTP(rootPath, appURL, appName, browser,
						objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("twin") == 0) {
				// Initializing TWIN
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeTwin(rootPath, appURL, appName, browser,
						objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("Frankenstein") == 0) {
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeFrankenstein(rootPath, appURL, appName,
						browser, objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("Core") == 0) {
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeCore(objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("Robotium") == 0) {
				LOGGER.debug("Creating an instance for [" + engineName);
				// Initializing Robotium
				engine = initializeRobotium(rootPath, appURL, appName, browser,
						objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("appium") == 0) {
				LOGGER.debug("Creating an instance for [" + engineName);
				// Initialize appium
				engine = initializeAppium(rootPath, appURL, appName, browser,
						objRepoPath, isPowerUserMode);
			} else if (engineName.compareToIgnoreCase("etl") == 0) {
				// Initialize ETL
				LOGGER.debug("Creating an instance for [" + engineName);
				engine = initializeETL(isPowerUserMode);
			} else {
				LOGGER.error("Invalid engine name ["
						+ engineName
						+ "] specified by user. Please refer to online documentation and specify a valid execution engine name to switch to a new execution engine.");
				throw new AFTException(
						"Invalid engine name ["
								+ engineName
								+ "] specified by user. Please refer to online documentation and specify a valid execution engine name to switch to a new execution engine.");
			}

		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return engine;
	}

	/**
	 * Tear down execution engine.
	 * 
	 * @param engineName
	 *            the engine name
	 * @throws AFTException
	 */
	public void tearDownExecutionEngine(String engineName) throws AFTException {
		if ((engineMap != null)
				&& (engineMap.containsKey(engineName.toLowerCase()))) {
			IxAFTEngine engine = engineMap.get(engineName.toLowerCase());

			// if the engine we are tearing down is also the current
			// execution engine, let us set it to null
			if ((currentExecutionEngine != null)
					&& (currentExecutionEngine.equals(engine))) {
				currentExecutionEngine = null;
				currentExecutionEngineName = null;
			}

			engine.tearDown();
			engineMap.remove(engineName.toLowerCase());
		}
	}

	/**
	 * Tear down all execution engines.
	 * 
	 * @throws AFTException
	 */
	public void tearDownAllExecutionEngines() throws AFTException {
		if (engineMap != null) {
			for (Entry<String, IxAFTEngine> entry : engineMap.entrySet()) {
				String engineName = entry.getKey();
					tearDownExecutionEngine(engineName);
			}

		}
	}

}