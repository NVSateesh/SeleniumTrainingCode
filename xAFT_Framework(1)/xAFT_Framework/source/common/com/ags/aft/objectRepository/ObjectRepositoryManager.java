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
 * Class: ObjectRepositoryManager
 * 
 * Purpose: This class implements ObjectRepositoryManager interface for working
 * with OR
 */
package com.ags.aft.objectRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.XMLParser;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;

/**
 * The Class ObjectRepositoryManager.
 */
public final class ObjectRepositoryManager {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ObjectRepositoryManager.class);

	/** The ObjectRepositoryManager */
	private static ObjectRepositoryManager objectRepositoryManager;

	/** The ui map. */
	private Map<String, RepositoryObject> uiObjectMap;

	/** The dynamic ui map. */
	private Map<String, RepositoryObject> aftDynamicUIMap;

	/** The is flag indicating if object repository is loaded. */
	private boolean isObjectRepositoryLoaded = false;

	/**
	 * Instantiates a new ObjectRepositoryManager.
	 */
	private ObjectRepositoryManager() {
		super();
	}

	/**
	 * Gets the single instance of ObjectRepositoryManager.
	 * 
	 * @return single instance of ObjectRepositoryManager
	 */
	public static ObjectRepositoryManager getInstance() {
		if (objectRepositoryManager == null) {
			objectRepositoryManager = new ObjectRepositoryManager();
			LOGGER.trace("Creating instance of ObjectRepositoryManager");
		}

		return objectRepositoryManager;
	}

	/**
	 * Load object repository file.
	 * 
	 * @param objectRespositoryFilePath
	 *            object repository file path
	 * @param isFileSystemRequest
	 *            isFileSystemRequest
	 * @throws AFTException
	 *             the application exception
	 */
	public void loadObjectRepository(String objectRespositoryFilePath,
			boolean isFileSystemRequest) throws AFTException {
		// Get list of OR Maps from the OR XML file
		List<Map<String, RepositoryObject>> orList = null;
		XMLParser xmlParser = new XMLParser();
		if (isFileSystemRequest) {
			try {
				LOGGER.info("Loading object repository from file ["
						+ objectRespositoryFilePath + "]");
				LOGGER.trace("Call to XML Parser to parse the XML Object Repository");
				xmlParser.readXML(objectRespositoryFilePath);
			} catch (AFTException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			// Get list of OR Maps from the OR XML file
			orList = xmlParser.getObjRepositoryAttrNameList("FeatureSet");
			LOGGER.debug("Loading object respository file ["
					+ objectRespositoryFilePath + "]");
		} else {
			LOGGER.info("Loading object repository from DB");
			orList = DatabaseUtil.getInstance().loadObjectRepository();
			LOGGER.debug("Loaded object respository from DB");
		}
		setObjectRepositoryLoaded(true);
		uiObjectMap = new HashMap<String, RepositoryObject>();
		for (Map<String, RepositoryObject> orMap : orList) {
			uiObjectMap.putAll(orMap);
		}

	}

	/**
	 * Returns object id associated with a element name.
	 * 
	 * @param elementName
	 *            Element name for which the object id needs to be retrieved
	 *            from UI map
	 * @throws AFTException
	 * @return retrieved object id
	 */
	public String getObjectID(String elementName) throws AFTException {
		String objectID = null;
		RepositoryObject repositoryObject = null;
		try {
			if ((aftDynamicUIMap != null) && (aftDynamicUIMap.size() > 0)) {
				LOGGER.debug("Local OR has been loaded by user. System will first try to read objectID from this OR");
				repositoryObject = aftDynamicUIMap.get(elementName);
			} else {
				repositoryObject = uiObjectMap.get(elementName);

			}
			if (repositoryObject != null) {
				objectID = getObjectValue(repositoryObject);
			}

		/*	if(objectID != null || elementName.equalsIgnoreCase("novalue")){
				LOGGER.debug("Object ID found or Element is novalue");
			}else{
				throw new AFTException("Unable to load element name: [" + elementName);
			}*/
				
			
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return objectID;
	}

	/**
	 * Returns object type associated with a element name.
	 * 
	 * @param elementName
	 *            Element name for which the object type needs to be retrieved
	 *            from UI map
	 * @throws AFTException
	 * @return retrieved xAFT object type
	 */
	public String getObjectType(String elementName) throws AFTException {
		String objectID = null;
		RepositoryObject repositoryObject = null;
		try {
			if ((aftDynamicUIMap != null) && (aftDynamicUIMap.size() > 0)) {
				LOGGER.debug("Local OR has been loaded by user. System will first try to read objectID from this OR");
				repositoryObject = aftDynamicUIMap.get(elementName);
			} else {
				repositoryObject = uiObjectMap.get(elementName);

			}
			if (repositoryObject != null) {
				objectID = repositoryObject.getType();
			}

		/*	if(objectID != null || elementName.equalsIgnoreCase("novalue")){
				LOGGER.debug("Object ID found or Element is novalue");
			}else{
				throw new AFTException("Unable to load element name: [" + elementName);
			}*/
				
			
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return objectID;
	}

	/**
	 * Returns object Repository associated with a element name.
	 * 
	 * @param elementName
	 *            Element name for which the object id needs to be retrieved
	 *            from UI map
	 * @return retrieved object Repository
	 * @throws AFTException
	 */
	public RepositoryObject getObject(String elementName) throws AFTException {

		RepositoryObject repositoryObject = null;
		String objName = elementName;

		try {
			if (elementName.contains(Constants.OBJECTDELIMITER)) {
				String[] elementsArray = elementName.trim().split(
						Constants.OBJECTDELIMITER);
				objName = elementsArray[0];
				LOGGER.trace("Parsed element name [" + objName + "] ");
			}
			if ((aftDynamicUIMap != null) && (aftDynamicUIMap.size() > 0)) {
				LOGGER.debug("Local OR has been loaded by user. System will first try to read objectID from this OR");
				repositoryObject = aftDynamicUIMap.get(objName);
			} else if (uiObjectMap != null && uiObjectMap.size() > 0) {
				repositoryObject = uiObjectMap.get(objName);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return repositoryObject;
	}

	/**
	 * This Method gets id/css/name/xpath value from the ObjectRepository based
	 * on IdentificationType of the ObjectRepository
	 * 
	 * @param repositoryObject
	 *            repositoryObject
	 * @return objectValue
	 * @throws AFTException
	 * 
	 */
	private String getObjectValue(RepositoryObject repositoryObject)
			throws AFTException {
		String objectValue = null;
		try {
			if (repositoryObject.getIdentificationType().equalsIgnoreCase("id")) {
				objectValue = repositoryObject.getId();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("xpath")) {
				objectValue = repositoryObject.getXpath();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("css")) {
				objectValue = repositoryObject.getCss();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("name")) {
				objectValue = repositoryObject.getName();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("link")) {
				objectValue = repositoryObject.getLink();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("imagename")) {
				objectValue = repositoryObject.getImageName();
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("index")) {
				objectValue = repositoryObject.getIndex();
			}  else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("text")) {
				objectValue = repositoryObject.getText();
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		LOGGER.info("Object value is " + objectValue);
		return objectValue;
	}

	/**
	 * This Method sets new value to the UIMap for a specified Key.
	 * 
	 * @param elementName
	 *            name of the element for which objectId needs to be set in UI
	 *            map
	 * @param objectID
	 *            the object id
	 * @throws AFTException
	 */
	public void setObjectID(String elementName, String objectID)
			throws AFTException {
		LOGGER.trace("Updating the  value for property [" + elementName + "]"
				+ " with New Value [ " + objectID + " ]");
		try {
			RepositoryObject repositoryObject = uiObjectMap.get(elementName);

			if (repositoryObject != null) {
				repositoryObject.setId(objectID);
				repositoryObject = setObjectValue(repositoryObject, objectID);
				uiObjectMap.put(elementName, repositoryObject);
			} else {
				LOGGER.error("Element [" + elementName
						+ "] not found in Object Repository");
				throw new AFTException("Element [" + elementName
						+ "] not found in Object Repository");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * This Method sets new repository object to the UIMap for a specified Key.
	 * 
	 * @param elementName
	 *            name of the element for which objectId needs to be set in UI
	 *            map
	 * @param repositoryObject
	 *            object the object id
	 * @throws AFTException
	 */
	public void setRespositoryObject(String elementName,
			RepositoryObject repositoryObject) throws AFTException {
		try {
			uiObjectMap.put(elementName, repositoryObject);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * This Method sets id/css/name/xpath value of the ObjectRepository based on
	 * IdentificationType of the ObjectRepository
	 * 
	 * @param repositoryObject
	 *            repositoryObject
	 * @param objectID
	 *            objectID
	 *  @return repositoryObject
	 * @throws AFTException
	 * 
	 */
	private RepositoryObject setObjectValue(RepositoryObject repositoryObject,
			String objectID) throws AFTException {
		try {
			if (repositoryObject.getIdentificationType().equalsIgnoreCase("id")) {
				repositoryObject.setId(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("xpath")) {
				repositoryObject.setXpath(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("css")) {
				repositoryObject.setCss(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("name")) {
				repositoryObject.setName(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("link")) {
				repositoryObject.setLink(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("imagename")) {
				repositoryObject.setImageName(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("index")) {
				repositoryObject.setIndex(objectID);
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("text")) {
				repositoryObject.setText(objectID);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return repositoryObject;
	}

	/**
	 * Checks if is property exists.
	 * 
	 * @param key
	 *            the key
	 * @return true, if is property exists
	 */
	public boolean isObjectExists(String key) {
		return uiObjectMap.containsKey(key);
	}

	/**
	 * Load object repository file.
	 * 
	 * @param objectRespositoryFilePath
	 *            object repository file path
	 * @throws IOException
	 *             throws IOException in case file path is incorrect or an error
	 *             occurs while reading file stream
	 * @throws AFTException
	 */
	public void loadLocalObjectRepository(String objectRespositoryFilePath)
			throws IOException, AFTException {

		LOGGER.debug("Loading Object Repository file path ["
				+ objectRespositoryFilePath + "]");

		File objectRepos = new File(objectRespositoryFilePath);

		if (!objectRepos.isFile()) {
			LOGGER.error("Invalid file path [" + objectRespositoryFilePath
					+ "] specified");
			throw new AFTException("Invalid file path ["
					+ objectRespositoryFilePath + "]");
		}

		XMLParser xmlParser = null;
		try {
			xmlParser = new XMLParser();
			xmlParser.readXML(objectRespositoryFilePath);
		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		// Get list of OR Maps from the OR XML file
		List<Map<String, RepositoryObject>> orList = xmlParser
				.getObjRepositoryAttrNameList("FeatureSet");

		LOGGER.debug("Loading object respository file ["
				+ objectRespositoryFilePath + "]");
		aftDynamicUIMap = new HashMap<String, RepositoryObject>();
		for (Map<String, RepositoryObject> orMap : orList) {
			aftDynamicUIMap.putAll(orMap);
		}
	}

	/**
	 * unloads local object repository
	 */
	public void unLoadLocalObjectRepository() {
		aftDynamicUIMap = null;
	}

	/**
	 * @return the isObjectRepositoryLoaded
	 */
	public boolean isObjectRepositoryLoaded() {
		return isObjectRepositoryLoaded;
	}

	/**
	 * @param isORLoaded
	 *            the isRepositoryLoaded to set
	 */
	public void setObjectRepositoryLoaded(boolean isORLoaded) {
		this.isObjectRepositoryLoaded = isORLoaded;
	}
}
