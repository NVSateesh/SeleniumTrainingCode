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
 * Class: databaseInstanceManager
 * 
 * Purpose: This class manages database instance objects
 */

package com.ags.aft.fixtures.database;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.util.Helper;

public final class DatabaseInstanceManager {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	/** The databaseInstanceManager */
	private static DatabaseInstanceManager databaseInstanceMgr;

	/** The stored databaseInstance objects map. */
	private static Map<String, DatabaseInstance> databaseInstanceMap = new HashMap<String, DatabaseInstance>();
	/** The database instance params. */
	private static Map<String, String[]> databaseInstanceParams = new HashMap<String, String[]>();

	/**
	 * private constructor for DatabaseInstance manager.
	 * 
	 */
	private DatabaseInstanceManager() {
		super();
	}

	/**
	 * Gets the single instance of databaseInstance.
	 * 
	 * @return single instance of databaseInstance
	 */
	public static DatabaseInstanceManager getInstance() {
		if (databaseInstanceMgr == null) {
			databaseInstanceMgr = new DatabaseInstanceManager();
			LOGGER.trace("Creating instance of aft databaseInstanceManager");
		}

		return databaseInstanceMgr;
	}

	/**
	 * Stores the (instance identifier, databaseInstance object)pair in the
	 * databaseInstanceMap
	 * 
	 * @param instanceIdentifierValue
	 *            Unique identifier using which to store the database connection
	 *            object
	 * @param objDBInstance
	 *            databaseInstance object
	 */
	public void addDBInstance(String instanceIdentifierValue,
			DatabaseInstance objDBInstance) {
		databaseInstanceMap.put(instanceIdentifierValue, objDBInstance);
	}

	/**
	 * Adds the db instance parameters.
	 * 
	 * @param instanceIdentifierValue
	 *            the instance identifier value
	 * @param paramArray
	 *            the param array
	 */
	public void addDBInstanceParameters(String instanceIdentifierValue,
			String[] paramArray) {
		databaseInstanceParams.put(instanceIdentifierValue, paramArray);
	}

	/**
	 * Gets the dB instance parameters.
	 * 
	 * @param instanceIdentifierValue
	 *            the instance identifier value
	 * @return the dB instance parameters
	 */
	public String[] getDBInstanceParameters(String instanceIdentifierValue) {
		return databaseInstanceParams.get(instanceIdentifierValue);
	}

	/**
	 * Returns the Database Instance object mapped to the
	 * key(dbInstanceIdentifier) in the databaseInstanceMap
	 * 
	 * @param dbInstanceIdentifier
	 *            Unique identifier using which to get the database instance
	 *            object
	 * 
	 * @return Database instance object
	 */
	public DatabaseInstance getDBInstance(String dbInstanceIdentifier) {
		return databaseInstanceMap.get(dbInstanceIdentifier);
	}

	/**
	 * Checks if the Key(dbInstanceIdentifier) exists in the databaseInstanceMap
	 * 
	 * @param dbInstanceIdentifier
	 *            Database instance identifier in the list to search for
	 * 
	 * @return Boolean
	 */
	public boolean checkDBInstanceExists(String dbInstanceIdentifier) {
		return databaseInstanceMap.containsKey(dbInstanceIdentifier);
	}

	/**
	 * Removes the (dbInstanceIdentifier, databaseInstance) pair from the
	 * databaseInstanceMap
	 * 
	 * @param dbInstanceIdentifier
	 *            Unique database instance identifier to be removed from
	 *            databaseInstanceMap
	 */
	public void removeDBInstance(String dbInstanceIdentifier) {
		LOGGER.debug("Removing the Database instance associated with identifier ["
				+ dbInstanceIdentifier + "] from databaseInstanceMap");
		databaseInstanceMap.remove(dbInstanceIdentifier);
	}

	/**
	 * Closes any open database connection associated with each databaseInstance
	 * stored in the databaseInstanceMap
	 * 
	 */
	public void destroyAllOpenDBInstances() {

		boolean closedConn = false;
		Connection objConnection = null;

		try {
			// if the map is not empty
			if (!databaseInstanceMap.isEmpty()) {

				Iterator<String> iLoop = databaseInstanceMap.keySet()
						.iterator();

				// loop thru the keys
				while (iLoop.hasNext()) {

					String connIdentifierValue = iLoop.next();

					// if key has any database instance associated with it
					if (databaseInstanceMap.get(connIdentifierValue) != null) {

						// get the instance object
						DatabaseInstance dbInstance = databaseInstanceMap
								.get(connIdentifierValue);

						// get the connection object
						objConnection = dbInstance.getConnectionObject();

						// if the connection is not closed already
						if (!objConnection.isClosed()) {

							objConnection.close();
							closedConn = true;
						}
					}
				}

				databaseInstanceMap.clear();
			}
		} catch (Exception e) {
			LOGGER.warn("Exception::", e);
		} finally {
			try {
				if (objConnection != null && !objConnection.isClosed()) {
					objConnection.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
			objConnection = null;
		}

		if (!closedConn) {
			LOGGER.info("No open DB connections found!");
		}
	}

}