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
 * Class: databaseInstance
 * 
 * Purpose: This class stores database instance object
 */

package com.ags.aft.fixtures.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.log4j.Logger;
import com.ags.aft.util.Helper;

public class DatabaseInstance {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	/** The Database connection object. */
	private Connection connectionObject;

	/** A unique String identifier for this database connection object */
	private String connectionIdentifier;

	/** The Database type string. */
	private String databaseType;

	/** Sequence number for random name generation. */
	private static int randomNameSequence = 1;

	/**
	 * Database Instance constructor
	 * @param connectionObject
	 *          connectionObject
	 * @param databaseType
	 *          databaseType
	 */
	public DatabaseInstance(Connection connectionObject, String databaseType) {

		this.connectionObject = connectionObject;

		this.databaseType = databaseType;

		connectionIdentifier = createUniqueDatabaseInstanceIdentifier();

		LOGGER.debug("Database Instance object created for connection identifier ["
				+ connectionIdentifier + "]!");
	}
	
	/**
	 * @return connectionObject
	 *          connectionObject
	 */
	public Connection getConnectionObject() {
		return connectionObject;
	}

	/**
	 * @return connectionIdentifier
	 *          connectionIdentifier
	 */
	public String getConnectionIdentifier() {
		return connectionIdentifier;
	}

	/**
	 * @return databaseType
	 *          databaseType
	 */
	public String getDatabaseType() {
		return databaseType;
	}

	/**
	 * Generates a unique identifier to store the database instance in the map
	 * 
	 * @return unique identifier
	 */
	private String createUniqueDatabaseInstanceIdentifier() {

		LOGGER.debug("Creating a unique database connection identifier");

		// generate a random name for the connection
		Calendar myCalendar = Calendar.getInstance();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssS");
		String instanceIdentifier = "DatabaseInstance"
				+ dateFormatter.format(myCalendar.getTime())
				+ randomNameSequence;
		randomNameSequence = randomNameSequence + 1;

		LOGGER.info("Generated unique database connection identifier ["
				+ instanceIdentifier + "]");

		return instanceIdentifier;
	}

}
