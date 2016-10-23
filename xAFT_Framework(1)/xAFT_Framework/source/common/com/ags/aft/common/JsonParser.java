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
 * Class: JSONParser
 * 
 * Purpose: Implements JSON Parser methods - read/write at Node/attribute level
 */

package com.ags.aft.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;

/**
 * The Class JsonParser.
 * 
 */
public class JsonParser {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(JsonParser.class);

	/**
	 * Initializes JsonParser object.
	 * 
	 * @throws AFTException
	 */
	public JsonParser() throws AFTException {

	}

	/**
	 * Gets the attribute value for a specific element name
	 * 
	 * @param responseJson
	 *            responseJson
	 * @param xPathArray
	 *            xPathArray
	 * @return attribute value
	 * @throws AFTException
	 */
	public String getAttributeValue(String responseJson, String[] xPathArray)
			throws AFTException {
		String attributeValue = null;
		JSONObject jsonObject = null;
		String attributeName = null;
		try {
			LOGGER.info("Fetching the value for attribute/element");
			if (xPathArray.length > 2) {
				attributeName = xPathArray[2];
			} else {
				attributeName = xPathArray[1];
			}
			Object obj = getParsedObject(responseJson, xPathArray,
					null, false);
			if (obj != null) {
				jsonObject = (JSONObject) obj;
			}
			if (jsonObject != null) {
				attributeValue = jsonObject.get(attributeName).toString();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}
		return attributeValue;
	}

	/**
	 * Checks whether attribute is present or not.
	 * 
	 * @param responseJson
	 *            responseJson
	 * @param xPathArray
	 *            xPathArray
	 * @return booelan
	 * @throws AFTException
	 */
	public boolean isAttributePresent(String responseJson, String[] xPathArray)
			throws AFTException {
		boolean attributeFound = false;
		JSONObject jsonObject = null;
		String attributeName = null;
		try {
			try {
				LOGGER.info("Checking whether the attribute is present or not in json response");
				if (xPathArray.length > 2) {
					attributeName = xPathArray[2];
				} else {
					attributeName = xPathArray[1];
				}
				// get the json object from json response
				jsonObject = (JSONObject) getParsedObject(responseJson,
						xPathArray, null, false);
				// Check whether the json object contains the attribute or not.
				if (jsonObject != null && jsonObject.containsKey(attributeName)) {
					attributeFound = true;
				}

			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				throw new AFTException(e);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}
		return attributeFound;
	}

	/**
	 * Gets the JSON object
	 * 
	 * @param nodeName
	 *            nodeName
	 * @param nodeObj
	 *            nodeObj
	 * @return attribute value
	 * @throws AFTException
	 */
	private JSONObject getJsonObject(String nodeName, JSONObject nodeObj)
			throws AFTException {
		JSONObject jsonobj = null;
		try {

			LOGGER.info("Get the json object from json array");
			// get the index of the node name.
			String name = nodeName.substring(0, nodeName.indexOf('['));
			String index = nodeName.substring(nodeName.indexOf('[') + 1,
					nodeName.length() - 1);
			if (nodeObj instanceof JSONObject) {
				JSONObject object = (JSONObject) nodeObj;
				if (object != null) {
					// get the json array from json object
					JSONArray jsonarray = (JSONArray) object.get(name);
					// from json array get the object record based on index
					// value.
					if (jsonarray != null) {
						jsonobj = (JSONObject) jsonarray.get(Integer
								.parseInt(index));
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}
		return jsonobj;
	}

	/**
	 * Convert JSON string to file.
	 * 
	 * @param jsonString
	 *            the json string
	 * @param fileName
	 *            the file name
	 * @throws AFTException
	 *             the AFT exception
	 */
	public static void convertJSONStringToFile(String jsonString,
			String fileName) throws AFTException {
		try {
			LOGGER.debug("Saving JSON string to file [" + fileName + "]");

			FileWriter destFile = new FileWriter(fileName);
			destFile.write(jsonString);
			destFile.flush();
			destFile.close();
			LOGGER.debug("Successfully saved json string to file [" + destFile
					+ "]");
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Reads the JSON file
	 * 
	 * @param filePath
	 *            the file path
	 * @return return the JSON as string
	 * @throws AFTException
	 */
	public String readJson(String filePath) throws AFTException {
		// Check whether the File passed is a Logical File(JSON as a string) or
		// a Physical File
		LOGGER.trace("Data received by JSON Parser: \n" + filePath + "\n");
		String jsonString = null;
		try {
			File file = new File(filePath);
			// ensuring 'filepath' is either a valid/invalid path.
			if (!filePath.contains("<") && filePath.endsWith(".json")) {
				if (file.exists()) {
					LOGGER.debug("JSON file received");
					jsonString = createJsonObject(filePath);
				} else {
					String errMsg = "File [" + filePath
							+ "] (The system cannot find the file specified)";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			} else {
				LOGGER.debug("Request Json received");
				// Create In-memory JSON file
				String jsonFilePath = createInMemoryJson(filePath);
				jsonString = createJsonObject(jsonFilePath);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return jsonString;
	}

	/**
	 * Creates the in memory json for the specified json string.
	 * 
	 * @param jsonObj
	 *            the jsonObj
	 * @return the string
	 * @throws AFTException
	 */
	private String createInMemoryJson(String jsonObj) throws AFTException {

		Writer writer = null;
		// Get the System Temp Directory where you want to write the json
		// contents
		String tempFilePath = System.getenv("TMP") + ".json";
		LOGGER.debug("Creating a temporary JSON file [" + tempFilePath + "]");

		try {
			// Initiate the writer object to write the contents in to a
			// Temporary JSON
			writer = new FileWriter(new File(tempFilePath));
			LOGGER.debug("Writing the contents into file into [" + tempFilePath
					+ "]");
			writer.write(jsonObj);
			LOGGER.debug("Creation of json file [" + tempFilePath
					+ "] completed");
			LOGGER.debug("Closing the Writer..");
			writer.close();
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return tempFilePath;
	}

	/**
	 * Creates an JSON object for the filePath Specified.
	 * 
	 * @param filePath
	 *            the file path
	 * @return the JSON document
	 * @throws AFTException
	 * @throws ParseException
	 */
	private String createJsonObject(String filePath) throws AFTException {
		LOGGER.debug("Reading the JSON file [" + filePath + "]");
		File file = new File(filePath);
		String jsonString = null;
		try {
			LOGGER.debug("Parsing the JSON file [" + filePath + "]");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(file));
			if (obj instanceof JSONObject) {
				// get the json string from json object
				JSONObject object = (JSONObject) obj;
				jsonString = object.toJSONString();
			} else if (obj instanceof JSONArray) {
				// get the json string from json array
				JSONArray object = (JSONArray) obj;
				jsonString = object.toJSONString();
			}
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (ParseException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		LOGGER.info("Completed reading the JSON file [" + filePath + "]");
		// Delete the Temp File
		if (filePath.contains("Temp")) {
			LOGGER.trace("Creating a file object");
			file = new File(filePath);
			// Check if the file is deleted successfully
			boolean success = file.delete();
			if (!success) {
				LOGGER.warn("Failed to delete the Temp File [" + filePath + "]");
			} else {
				LOGGER.trace("Successfully Deleted the Temp File [" + filePath
						+ "]");
			}
		}
		return jsonString;
	}

	/**
	 * Updates JSON attribute value for a specified Node and Attribute Name.
	 * 
	 * @param requestJson
	 *            requestJson
	 * @param xPathArray
	 *            xPathArray
	 * @param newAttributeValue
	 *            the new attribute value
	 * @return the String
	 * @throws AFTException
	 */
	public String updateAttributeValue(String requestJson, String[] xPathArray,
			String newAttributeValue) throws AFTException {
		String jsonObject = null;
		// get the json object from request json
		jsonObject = getParsedObject(requestJson, xPathArray,
				newAttributeValue, true).toString();
		return jsonObject;
	}

	/**
	 * Gets the parsed JSON object
	 * 
	 * @param responseJson
	 *            responseJson
	 * @param xPathArray
	 *            xPathArray
	 * @param newAttributeValue
	 *            newAttributeValue
	 * @param isUpdate
	 *            isUpdate
	 * @return jsonObject value
	 * @throws AFTException
	 */
	@SuppressWarnings("unchecked")
	private Object getParsedObject(String responseJson, String[] xPathArray,
			String newAttributeValue, boolean isUpdate) throws AFTException {
		JSONObject jsonObject = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(responseJson);
			if (xPathArray.length > 2) {
				// get the parent node and child node from xpathArray.
				String rootName = xPathArray[0];
				String nodeName = xPathArray[1];
				if (obj instanceof JSONObject) {
					JSONObject jsonInstnace = (JSONObject) obj;
					// get the json object of parent node.
					JSONObject nodeObj = (JSONObject) jsonInstnace
							.get(rootName);
					// if the child node contains '[' then get the json object
					// by passing node name and parent node json object.
					if (nodeName.contains("[")) {
						jsonObject = getJsonObject(nodeName, nodeObj);
						String name = xPathArray[2];
						// if the parent node json object contains child node
						// name then add child node name to json object.
						if (isUpdate && jsonObject != null
								&& jsonObject.containsKey(name)) {
							jsonObject.put(name, newAttributeValue);
						}

					} else {
						// if the child node doesn't contain array then get the
						// json object by passing child node name
						if (nodeObj != null) {
							JSONObject jsonInstance = (JSONObject) nodeObj;
							if (jsonInstance != null) {
								jsonObject = (JSONObject) jsonInstance
										.get(nodeName);
							}
						}
					}
				}
			} else {
				// get the parsed json object
				jsonObject = parseObject(obj, xPathArray, isUpdate,
						newAttributeValue);
			}
			if (isUpdate) {
				return obj;
			} else {
				return jsonObject;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new AFTException(e);
		}
	}

	/**
	 * parse the JSON object
	 * 
	 * @param obj
	 *            obj
	 * @param xPathArray
	 *            xPathArray
	 * @param isUpdate
	 *            isUpdate
	 * @param newAttributeValue
	 *            newAttributeValue
	 * @return jsonObject
	 * @throws AFTException
	 */

	@SuppressWarnings("unchecked")
	private JSONObject parseObject(Object obj, String[] xPathArray,
			boolean isUpdate, String newAttributeValue) throws AFTException {
		JSONObject jsonObject = null;
		if (obj != null) {
			String nodeName = xPathArray[0];
			String attrName = xPathArray[1];
			// if attribute starts with root then get the json object.
			if (nodeName.startsWith(Constants.ROOT)) {
				if (obj instanceof JSONArray) {
					// if the object is array then get the index from node name
					// and pass the index to array to get the json object.
					JSONArray jsonarray = (JSONArray) obj;
					if (nodeName.contains("[")) {
						// get the index from json array.
						String index = nodeName.substring(5,
								nodeName.length() - 1);
						if (jsonarray != null) {
							// get the json object by pssing index to json
							// array.
							jsonObject = (JSONObject) jsonarray.get(Integer
									.parseInt(index));
							// if the json object contains attaribute name then
							// attribute name to the json object.
							if (isUpdate && jsonObject != null
									&& jsonObject.containsKey(attrName)) {
								jsonObject.put(attrName, newAttributeValue);
							}
						}
					}
				} else {
					// if the object is not an arrya then get the json object
					// from response.
					if (obj instanceof JSONObject) {
						jsonObject = (JSONObject) obj;
					}
				}
			} else {
				// parse the node object
				jsonObject = parseNodeObject(obj, nodeName, isUpdate,
						newAttributeValue, attrName);
			}
		}
		return jsonObject;
	}

	/**
	 * parse the node object
	 * 
	 * @param obj
	 *            obj
	 * @param nodeName
	 *            nodeName
	 * @param isUpdate
	 *            isUpdate
	 * @param newAttributeValue
	 *            newAttributeValue
	 * @param attrName
	 *            attrName
	 * @return jsonObject
	 * @throws AFTException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject parseNodeObject(Object obj, String nodeName,
			boolean isUpdate, String newAttributeValue, String attrName)
			throws AFTException {
		JSONObject jsonObject = null;
		LOGGER.info("Parse the json response and get the json object");
		// if node name contains [ then get the json object by passing node name
		// and json object
		// and check whether
		// nodeName is present in the json ojbect or not.
		if (nodeName.contains("[")) {
			if (obj instanceof JSONObject) {
				JSONObject jsonInstnace = (JSONObject) obj;
				if (jsonInstnace != null) {
					// get the json object by passing node name and json
					// instance.
					jsonObject = getJsonObject(nodeName, jsonInstnace);
					// if the json object contains attribute name then add the
					// attribute to the json object.
					if (isUpdate && jsonObject != null
							&& jsonObject.containsKey(attrName)) {
						jsonObject.put(attrName, newAttributeValue);
					}
				}
			}
		} else {
			// if node name doesn't contain [ then get the json object by
			// giving node name.
			if (obj instanceof JSONObject) {
				JSONObject jsonInstance = (JSONObject) obj;
				if (jsonInstance != null) {
					jsonObject = (JSONObject) jsonInstance.get(nodeName);
					// if the json object contains attribute name then add the
					// attribute to the json object.
					if (isUpdate && jsonObject != null
							&& jsonObject.containsKey(attrName)) {
						jsonObject.put(attrName, newAttributeValue);
					}
				}
			}
		}
		return jsonObject;
	}
}
