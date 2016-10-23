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
 * Class: SikuliObjectManager
 * 
 * Purpose: This class manages the sikuli object maps
 */

package com.ags.aft.fixtures.sikuli;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sikuli.script.App;
import org.sikuli.script.Pattern;

public final class SikuliObjectManager {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(SikuliObjectManager.class);

	/** The SikuliObjectManager */
	private static SikuliObjectManager sikuliObjectMgr;

	/** The Sikuli objects map. */
	private Map<String, Pattern> sikuliObjectMap = new HashMap<String, Pattern>();
	/** The Sikuli application map. */
	private Map<String, App> sikuliApplicationMap = new HashMap<String, App>();
	/** Sequence number for random name generation. */
	private int randomNameSequence = 1;

	/**
	 * Constructor for SikuliObjectManager
	 * 
	 */
	private SikuliObjectManager() {
		super();
	}

	/**
	 * Gets the single instance of sikuliObjectMgr.
	 * 
	 * @return single instance of sikuliObjectMgr
	 */
	public static SikuliObjectManager getInstance() {
		if (sikuliObjectMgr == null) {
			sikuliObjectMgr = new SikuliObjectManager();
			LOGGER.trace("Creating instance of aft SikuliObjectManager");
		}

		return sikuliObjectMgr;
	}

	/**
	 * Generates a unique identifier to store the Repository object in the map
	 * @param objType
	 *           objType
	 * 
	 * @return unique identifier
	 */
	public String createUniqueObjectIdentifier(String objType) {

		LOGGER.debug("Creating a unique object identifier");

		// generate a random name for the connection
		Calendar myCalendar = Calendar.getInstance();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssS");
		String objStartingChar = "";

		if (objType.equalsIgnoreCase("pattern")) {
			objStartingChar = "Pattern";
		} else if (objType.equalsIgnoreCase("region")) {
			objStartingChar = "Region";
		} else if (objType.equalsIgnoreCase("location")) {
			objStartingChar = "Location";
		} else if (objType.equalsIgnoreCase("application")) {
			objStartingChar = "Application";
		}

		String instanceIdentifier = objStartingChar
				+ dateFormatter.format(myCalendar.getTime())
				+ randomNameSequence;
		randomNameSequence = randomNameSequence + 1;
		LOGGER.info("Generated unique object identifier [" + instanceIdentifier
				+ "]");

		return instanceIdentifier;
	}

	/**
	 * Stores the (object identifier, Pattern object) pair in the
	 * sikuliObjectMap
	 * 
	 * @param objectIdentifier
	 *            Unique identifier using which to store the object
	 * @param objPattern
	 *            Pattern Object to be stored
	 */
	public void addSikuliPatternObject(String objectIdentifier, Pattern objPattern) {
		sikuliObjectMap.put(objectIdentifier, objPattern);
	}

	/**
	 * Returns the Sikuli object mapped to the key(objectIdentifier) in the
	 * sikuliObjectMap
	 * 
	 * @param objectIdentifier
	 *            Unique identifier using which to get the object
	 * 
	 * @return Sikuli object
	 */
	public Pattern getSikuliPatternObject(String objectIdentifier) {
		return sikuliObjectMap.get(objectIdentifier);
	}

	/**
	 * Checks if the Key(objectIdentifier) exists in the sikuliObjectMap
	 * 
	 * @param objectIdentifier
	 *            Sikuli object identifier in the list to search for
	 * 
	 * @return Boolean
	 */
	public boolean isSikuliPatternObjectExists(String objectIdentifier) {
		return sikuliObjectMap.containsKey(objectIdentifier);
	}

	/**
	 * Clear all the Sikuli objects
	 * 
	 */
	public void destorySikuliObjects() {

		try {

			// if the map is not empty
			if (!sikuliObjectMap.isEmpty()) {

				Iterator<String> iLoop = sikuliObjectMap.keySet().iterator();
				// loop thru the keys
				while (iLoop.hasNext()) {
					String patternObjIdentifier = iLoop.next();
					// if key has any database instance associated with it
					if (sikuliObjectMap.get(patternObjIdentifier) != null) {

						// get the instance object
						@SuppressWarnings("unused")
						Pattern pattern = sikuliObjectMap
								.get(patternObjIdentifier);

						pattern = null;
					}
				}

				sikuliObjectMap.clear();
			}
			// if the map is not empty
			if (!sikuliApplicationMap.isEmpty()) {
				Iterator<String> iLoop = sikuliApplicationMap.keySet()
						.iterator();
				// loop thru the keys
				while (iLoop.hasNext()) {
					String appObjIdentifier = iLoop.next();
					// if key has any database instance associated with it
					if (sikuliApplicationMap.get(appObjIdentifier) != null) {

						// get the instance object
						App app = sikuliApplicationMap.get(appObjIdentifier);

						app.close();
						app = null;
					}
				}

				sikuliApplicationMap.clear();
			}
			
			// destroy sikuli instance objects
			if(SikuliInstance.doesSikuliInstanceExists()){
				SikuliInstance.getInstance().destroySikuliInstance();
			}
			

		} catch (Exception e) {
			LOGGER.warn("Exception::", e);
		}

	}

	/**
	 * Stores the (object identifier, Application) pair in the
	 * sikuliApplicationMap
	 * 
	 * @param objectIdentifier
	 *            Unique identifier using which to store the object
	 * @param sikuliApp
	 *            Application Object to be stored
	 */
	public void addSikuliAppObject(String objectIdentifier, App sikuliApp) {
		sikuliApplicationMap.put(objectIdentifier, sikuliApp);
	}

	/**
	 * Returns the Application object mapped to the key(objectIdentifier) in the
	 * sikuliApplicationMap
	 * 
	 * @param objectIdentifier
	 *            Unique identifier using which to get the object
	 * 
	 * @return Application object
	 */
	public App getSikuliAppObject(String objectIdentifier) {
		return sikuliApplicationMap.get(objectIdentifier);
	}

	/**
	 * Checks if the Key(objectIdentifier) exists in the sikuliApplicationMap
	 * 
	 * @param objectIdentifier
	 *            Sikuli object identifier in the list to search for
	 * 
	 * @return Boolean
	 */
	public boolean isSikuliAppObjectExists(String objectIdentifier) {
		return sikuliApplicationMap.containsKey(objectIdentifier);
	}

}