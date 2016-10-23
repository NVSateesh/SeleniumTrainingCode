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
 * Class: ScriptInstanceManager
 * 
 * Purpose: This class manages external script instance objects
 */
package com.ags.aft.fixtures.externalScript;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.ags.aft.util.Helper;

public final class ScriptInstanceManager {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	/** The ScriptInstanceManager */
	private static ScriptInstanceManager scriptInstance;

	private static int randomNameSequence = 1;

	private Map<String, ScriptInstance> scriptInstanceMap = new HashMap<String, ScriptInstance>();


	/**
	 * private constructor for ScriptInstance manager.
	 * 
	 */
	private ScriptInstanceManager() {
		super();
	}

	/**
	 * Gets the single instance of ScriptInstance.
	 * 
	 * @return single instance of ScriptInstance
	 */
	public static ScriptInstanceManager getInstance() {
		if (scriptInstance == null) {
			scriptInstance = new ScriptInstanceManager();
			LOGGER.trace("Creating instance of aftscriptInstance");
		}

		return scriptInstance;
	}

	/**
	 * Generates a unique identifier to store the script instance in the map
	 * 
	 * @return unique script instance identifier
	 */
	public String createUniqueScriptInstanceIdentifier() {

		LOGGER.debug("Creating a unique script instance identifier");

		// generate a random name for the connection
		Calendar myCalendar = Calendar.getInstance();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssS");
		String scriptIdentifier = "scriptInstance"
				+ dateFormatter.format(myCalendar.getTime())
				+ randomNameSequence;
		randomNameSequence = randomNameSequence + 1;

		LOGGER.info("Generated unique script instance identifier ["
				+ scriptIdentifier + "]");

		return scriptIdentifier;
	}

	/**
	 * Stores the (script identifier, script instance object)pair in the
	 * scriptInstanceMap
	 * 
	 * @param scriptIdentifier
	 *            Unique script identifier using which to store the script
	 *            instance object
	 * @param scriptInstance
	 *            script instance object
	 */
	public void addScriptInstance(String scriptIdentifier,
			ScriptInstance scriptInstance) {
		LOGGER.info("Added a new script instance identifier ["
				+ scriptIdentifier + "]");
		scriptInstanceMap.put(scriptIdentifier, scriptInstance);
	}

	/**
	 * Removes the (script identifier, script instance object) pair from the
	 * scriptInstanceMap
	 * 
	 * @param scriptIdentifier
	 *            Unique script identifier using which to retrieve the script
	 *            instance object
	 */
	public void removeScriptInstance(String scriptIdentifier) {
		LOGGER.info("Removing the script instance identifier ["
				+ scriptIdentifier + "]");
		scriptInstanceMap.remove(scriptIdentifier);
	}

	/**
	 * Returns the script identifier object mapped to the key(script identifier)
	 * in the scriptInstanceMap
	 * 
	 * @param scriptIdentifier
	 *            Unique script identifier using which to retrieve the script
	 *            instance object
	 * 
	 * @return script instance object
	 */
	public ScriptInstance getStoredScriptInstance(String scriptIdentifier) {
		LOGGER.info("Trying to fetch stored script instance identifier ["
				+ scriptIdentifier + "]");
		return scriptInstanceMap.get(scriptIdentifier);
	}

	/**
	 * Checks if the Key(script identifier) exists in the scriptInstanceMap
	 * 
	 * @param scriptIdentifier
	 *            Unique script identifier in the script instance list to search
	 *            for
	 * 
	 * @return Boolean
	 */
	public boolean checkScriptInstanceKeyExists(String scriptIdentifier) {
		LOGGER.info("Checking if the script instance identifier ["
				+ scriptIdentifier + "] exists.");
		return scriptInstanceMap.containsKey(scriptIdentifier);
	}

	/**
	 * Closes all the open scriot instance objects stored in the
	 * scriptInstanceMap
	 * 
	 */
	public void closeAllOpenScriptInstances() {

		boolean closedConn = false;

		try {
			// if the map is not empty
			if (!scriptInstanceMap.isEmpty()) {

				scriptInstanceMap.clear();

				closedConn = true;
			}
		} catch (Exception e) {
			LOGGER.warn("Exception::", e);
		}

		if (!closedConn) {
			LOGGER.info("No open script instances found!");
		}
	}
}
