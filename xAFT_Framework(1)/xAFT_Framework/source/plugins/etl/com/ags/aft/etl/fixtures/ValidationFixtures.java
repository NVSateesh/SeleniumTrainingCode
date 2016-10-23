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
 * Class: CommandFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the UI objects like click, type, select, remove
 */

package com.ags.aft.etl.fixtures;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBFlavor;
import org.w3c.dom.Document;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.XMLParser;
import com.ags.aft.etl.conf.DataCompleteness;
import com.ags.aft.etl.conf.DataCorrectness;
import com.ags.aft.etl.schema.validation.SchemaValidation;
import com.ags.aft.exception.AFTException;
import com.ags.aft.fixtures.database.DatabaseInstance;
import com.ags.aft.fixtures.database.DatabaseInstanceManager;
import com.google.gson.Gson;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandFixtures.
 * 
 */
public class ValidationFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ValidationFixtures.class);

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public ValidationFixtures() {
	}

	/**
	 * Verify data correctness.
	 * 
	 * @param objectID
	 *            the object id
	 * @param elementName
	 *            the element name
	 * @param parsedElementValue
	 *            the parsed element value
	 * @return true, if successful
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean verifyDataCorrectness(String objectID, String elementName,
			String parsedElementValue) throws AFTException {

		String[] values = parsedElementValue.split(";");
		if (values.length < 4) {
			LOGGER.info("Insufficient Parameters parsed");
			throw new AFTException("Insufficient Parameters parsed");
		}
		// Get Source Connection
		DatabaseInstance dbInstance1 = DatabaseInstanceManager.getInstance()
				.getDBInstance(values[0]);
		Connection connection1 = dbInstance1.getConnectionObject();

		// Get Destination Connection
		DatabaseInstance dbInstance2 = DatabaseInstanceManager.getInstance()
				.getDBInstance(values[2]);
		Connection connection2 = dbInstance2.getConnectionObject();

		String[] sourceDBParams = DatabaseInstanceManager.getInstance()
				.getDBInstanceParameters(values[0]);

		String[] dbParams = sourceDBParams[0].split("=");
		String[] dbuser = sourceDBParams[1].split("=");
		String[] dbPassword = sourceDBParams[2].split("=");
		String[] dbhost = sourceDBParams[3].split("=");
		String[] dbPort = sourceDBParams[4].split("=");
		String[] dbSchema = sourceDBParams[5].split("=");
		DKDBFlavor dkbFlavor = null;

		if (dbParams[1].equalsIgnoreCase("mysql")) {

			dkbFlavor = DKDBFlavor.MYSQL;
		}
		if (dbParams[1].equalsIgnoreCase("mssql")) {
			try {
				System.out.println(DKDBFlavor.SQLSERVER);
				dkbFlavor = DKDBFlavor.SQLSERVER;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DKDBConnectionInfo lhsDBConnectionInfo_ = new DKDBConnectionInfo(
				"test", DKDBFlavor.SQLSERVER, dbSchema[1], dbhost[1],
				Long.valueOf(dbPort[1]), dbuser[1], dbPassword[1]);

		String[] destDBParams = DatabaseInstanceManager.getInstance()
				.getDBInstanceParameters(values[2]);

		String[] ddbParams = destDBParams[0].split("=");
		String[] ddbuser = destDBParams[1].split("=");
		String[] ddbPassword = destDBParams[2].split("=");
		String[] ddbhost = destDBParams[3].split("=");
		String[] ddbPort = destDBParams[4].split("=");
		String[] ddbSchema = destDBParams[5].split("=");
		if (ddbParams[1].equalsIgnoreCase("mysql")) {
			dkbFlavor = DKDBFlavor.MYSQL;
		}
		if (ddbParams[1].equalsIgnoreCase("mssql")) {
			dkbFlavor = DKDBFlavor.SQLSERVER;
		}
		DKDBConnectionInfo rhsDBConnectionInfo_ = new DKDBConnectionInfo(
				"test", dkbFlavor, ddbSchema[1], ddbhost[1],
				Long.valueOf(ddbPort[1]), ddbuser[1], ddbPassword[1]);

		/*
		 * DKDBConnectionInfo lhsDBConnectionInfo_ = new
		 * DKDBConnectionInfo(conn1); DKDBConnectionInfo rhsDBConnectionInfo_ =
		 * new DKDBConnectionInfo(conn2);
		 */

		DataCorrectness compareResSet = new DataCorrectness();
		Map<String, String> result = null;
		try {
			result = compareResSet.compareDataMap(values[1], values[3],
					connection1, connection2, lhsDBConnectionInfo_,
					rhsDBConnectionInfo_);
			// Retain the map for ETL reporting purposes
			DatabaseUtil.getInstance().setEtlrecordsMap(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int unmatchedCount = Integer.parseInt(result.get("totalUnmatched")
				.toString());
		boolean unmatched = false;
		if (unmatchedCount > 0) {
			throw new AFTException("There are unmatched records!");
		} else {
			unmatched = true;
		}

		// Insert the matched and unmatched records into database
		return unmatched;
	}

	/**
	 * Verify data completeness.
	 * 
	 * @param objectID
	 *            the object id
	 * @param elementName
	 *            the element name
	 * @param parsedElementValue
	 *            the parsed element value
	 * @return
	 * @throws SQLException
	 *             the sQL exception
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean verifyDataCompleteness(String objectID, String elementName,
			String parsedElementValue) throws SQLException, AFTException {
		String[] values = parsedElementValue.split(";");
		if (values.length < 4) {
			LOGGER.info("Insufficient Parameters parsed");
			throw new AFTException("Insufficient Parameters parsed");
		}
		// Get Source Connection
		DatabaseInstance dbInstance1 = DatabaseInstanceManager.getInstance()
				.getDBInstance(values[0]);
		Connection leftConnection = dbInstance1.getConnectionObject();

		// Get Destination Connection
		DatabaseInstance dbInstance2 = DatabaseInstanceManager.getInstance()
				.getDBInstance(values[2]);
		Connection rightConnection = dbInstance2.getConnectionObject();

		DataCompleteness compareResSet = new DataCompleteness();
		String countMap = compareResSet.getIntegerCount(values[1], values[3],
				leftConnection, rightConnection);
		String[] countMapValues = countMap.split(",");
		String source = countMapValues[0].substring(31,
				countMapValues[0].length());
		String dest = countMapValues[1].substring(9,
				countMapValues[1].length() - 3);
		DatabaseUtil.getInstance().setEtlDataCompletenessCount(countMap);
		if (!source.equals(dest)) {
			throw new AFTException("Unmatched columns");
		}
		return true;
	}

	/**
	 * Verify schema.
	 * 
	 * @param objectID
	 *            the object id
	 * @param elementName
	 *            the element name
	 * @param parsedElementValue
	 *            the parsed element value
	 * @throws Exception
	 */
	public String verifySchema(String objectID, String elementName,
			String parsedElementValue) throws Exception {

		SchemaValidation schemaValidation = new SchemaValidation();
		String[] parsedElementValues = parsedElementValue.split(";");
		if (parsedElementValues.length < 3) {
			throw new Exception("Insufficient paramentes passed!");
		}
		DatabaseInstance dbInstance = DatabaseInstanceManager.getInstance()
				.getDBInstance(parsedElementValues[0]);
		String schemaType = parsedElementValues[1];
		DatabaseUtil.getInstance().setSchemaType(schemaType);
		Connection connection = dbInstance.getConnectionObject();
		String xmlString = parsedElementValues[2];
		DateFormat dt = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();

		XMLParser xmlParser = new XMLParser();
		String xmlFilePath = xmlParser.createInMemoryXML(xmlString);
		// Convert Document object to string
		Map<String, Object> schemaMap = schemaValidation.xmlValidation(
				xmlFilePath, connection);
		// Gson gson = new Gson();
		String dataCorrectnessString = schemaMap.get("result").toString();
		DatabaseUtil.getInstance().setSchemaValidationResult(
				dataCorrectnessString);
		if (schemaMap.get("status").toString().equalsIgnoreCase("true")) {
			return schemaMap.get("status").toString();
		} else {
			throw new AFTException("Expected and Actual Schema Does not match!");
		}
	}
}