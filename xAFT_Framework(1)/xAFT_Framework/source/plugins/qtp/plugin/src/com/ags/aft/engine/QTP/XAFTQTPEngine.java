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
 * Class: XAFTQTPEngine
 * 
 * Purpose: This class implements common interface for implementing Common
 * Interface for execution engines
 */
package com.ags.aft.engine.QTP;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.pluginEngine.IxAFTEngine;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class XAFTQTPEngine implements IxAFTEngine {
	// static org.apache.log4j.Logger LOGGER;
	private static final Logger LOGGER = Logger.getLogger(XAFTQTPEngine.class);
	private ActiveXComponent xAFTQTPEngineCOM;
	private boolean teardownQTP = true;
	private String currentBrowser;
	private Map<String, String> qtpCOnfigMap = new HashMap<String, String>();
	private boolean isxAFTCentral = false;
	private static XAFTQTPEngine engine = null;

	private XAFTQTPEngine() {
		// need to check for some initialization stuff
	}

	public static XAFTQTPEngine getQTPEngineInstance() {
		LOGGER.info("Creating singleton  instance of WebDriver Engine");
		if (engine == null) {
			engine = new XAFTQTPEngine();
		}
		return engine;
	}

	@Override
	public void initialize(String rootPath, String appURL, String appName,
			String browserName, String oRFilePath, boolean isFileSystemRequest)
			throws AFTException {
		LOGGER.info("Hello!! am in QTP Initialize");

		this.currentBrowser = browserName;
		teardownQTP = true;
		KeyPairGenerator kpg;
		String pkey = "";
		qtpCOnfigMap.put("is_xAFTCentral", "false");
		qtpCOnfigMap.put("qtpActionFixture", "./Library/qtpActionFixture.xml");
		
		String logFile="";
		try {
			//try find logfile
			FileAppender fileAppender = null;
			Enumeration appenders = LOGGER.getAllAppenders(); 
			while(appenders.hasMoreElements()) {

			    Appender currAppender = (Appender) appenders.nextElement();
			    if(currAppender instanceof FileAppender) {
			        fileAppender = (FileAppender) currAppender;
			    }
			}

			if(fileAppender != null) {
				logFile = fileAppender.getFile();
				LOGGER.info("Getlogfile from FileAppender: LOG File: " + fileAppender.getFile());
			}
			else
			{
				logFile=Log4JPlugin.getInstance().getLogFile();
				LOGGER.warn("Getlogfile from FileAppender: Unable to find file appender; " +
						"Retrieved log file from log4j properties: " + logFile);
			}
			
			LOGGER.info("Log File: " + logFile);
			qtpCOnfigMap.put("logFile",logFile);
			
			LOGGER.info("Accessing QTP COM Plugin using JACOB");
			xAFTQTPEngineCOM = new ActiveXComponent(
					"com.ags.aft.engine.plugin.QTP");
			if (!isFileSystemRequest) {
				LOGGER.info("Its xAFT Central");
				isxAFTCentral = true;
				qtpCOnfigMap.put("is_xAFTCentral", "true");
				qtpCOnfigMap.put("XAFTCENTRAL_TSR_FILE", DatabaseUtil.getInstance().getQTPObjectRepository());
				
				// if xAFt Cenrtral
				qtpCOnfigMap = DatabaseUtil.getInstance().loadConfigProperties(
						qtpCOnfigMap, Constants.QTPCONFIGTYPE);
				LOGGER.info("Loading Object Repository for from xAFT Central DB");
				ObjectRepositoryManager.getInstance().loadObjectRepository(
						null, false);
				//set OR opath
				qtpCOnfigMap.put("ObjectRepositoryPath",DatabaseUtil.getInstance().getQTPObjectRepository().replace("/","\\"));
				
				LOGGER.info("Loading External Libraries set for QTP Project");
				//Set libraries; We get list of vs files from db when its xAFT Central;Send this list to qtp com plugin
				for(String libPath : DatabaseUtil.getInstance().getVbsFileDump().toArray(new String[0]))
				{
					Dispatch.call(xAFTQTPEngineCOM, "setLibrary", libPath);
				}
				//this assumes that if list is empty, it would consider the same logic used for xAFT power user
				//TO DO: need to implement same logic for power user mode.
			}

			for (String key : qtpCOnfigMap.keySet()) {
				System.out.println(key + "--" + qtpCOnfigMap.get(key));
				Dispatch.call(xAFTQTPEngineCOM, "setConfig", key,
						qtpCOnfigMap.get(key)).toString();
			}
			
			
			// Dispatch.call(xAFTQTPEngineCOM, "setConfig",
			// "Test","How are you").toString();
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
					RSAPrivateKeySpec.class);
			pkey = priv.getModulus().toString() + "-SEP-"
					+ priv.getPrivateExponent().toString();

			LOGGER.info("Creating listener over port 55555");
			Dispatch.call(xAFTQTPEngineCOM, "Initialize", pkey, rootPath,
					appName, browserName, oRFilePath).toString();
			LOGGER.info("Sync till listener is created over port 55555");
			Dispatch.call(xAFTQTPEngineCOM, "SyncListner");

			LOGGER.info("Done!! am out of QTP Initialize method");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			LOGGER.error("Exception::", e);
//			throw new AFTException(e);
		}

	}

	@Override
	public void tearDown() throws AFTException {
		if (teardownQTP) {
			teardownQTP = false;
			LOGGER.info("QTP Teardown initaited");
			try {
				Dispatch.call(xAFTQTPEngineCOM, "StopQTP");
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
			LOGGER.info("QTP teardown done");
		} else {
			LOGGER.info("QTP teardown is already initiated");
		}

		engine = null;
	}

	@Override
	public String executeAction(TestStepRunner testStepRunner, String action,
			String elementName, String elementValue, String actualValue)
			throws AFTException {
		String xAFTObjectType ="",retVal = "", returnVariable=null;
		
		// Temp FIX Start
		if ((elementName.startsWith("tbl")) || (elementName.startsWith("TBL"))) {
			elementName = Helper.getInstance().getActionValue(elementName);
		}
		if(action.equalsIgnoreCase("methodCall"))
		{
			String[] externalMethods = elementName.split(",");
			if( externalMethods.length>1)
			{
				//has return variable
				returnVariable = externalMethods[1].trim();
				elementName = externalMethods[0].trim();
			}
		}
		
		
		
		LOGGER.info("QTP ExecuteAction<ACTION:" + action + " ELEMENT:"
				+ elementName + " DATA:" + elementValue + ">");

		String objectID = null;
		boolean objectFound = false;
		if (isxAFTCentral) {
			try {

				objectID = ObjectRepositoryManager.getInstance().getObjectID(
						elementName.trim().toUpperCase());
				xAFTObjectType = ObjectRepositoryManager.getInstance().getObjectType(elementName.trim().toUpperCase());
				System.out.println("OBJECT: " + elementName + " " + objectID);
				if (objectID != null) { // So its an object from repository
					objectFound = true;
				}
			} catch (Exception exp) {
				objectFound = false;
			}
		}
		try {
			if (objectFound) {// pass object id if object is found in db
				LOGGER.info("\n[" + "objectFound && isxAFTCentral \n"
						+ " -xAFTObjectType: " + xAFTObjectType + " \n" + " -Element: "
						+ " -Action: " + action + " \n" + " -Element: "
						+ objectID + " \n" + " -ElementValue: " + elementValue
						+ " \n" + " -ObjectFound?: " + objectFound + "] \n");
				retVal = Dispatch.call(xAFTQTPEngineCOM, "ExecuteAction",xAFTObjectType,
						action, objectID, elementValue, objectFound).toString();
			} else {
				LOGGER.info("\n[" + "!objectFound && isxAFTCentral \n"
						+ " -xAFTObjectType: " + xAFTObjectType + " \n" + " -Element: "
						+ " -Action: " + action + " \n" + " -Element: "
						+ elementName + " \n" + " -ElementValue: "
						+ elementValue + " \n" + " -ObjectFound?: "
						+ objectFound + "\n] ");
				retVal = Dispatch.call(xAFTQTPEngineCOM, "ExecuteAction",xAFTObjectType,
						action, elementName, elementValue, false).toString();
			}
			if (action.equalsIgnoreCase("getrowid")) {
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TABLEROWID), true, retVal);
			} else if (action.equalsIgnoreCase("getcolumnid")) {
				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TABLECOLID), true, retVal);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		/*
		 * if element name contains dynamic variable delimiter set the
		 * return value to the dynamic variable.
		 */
		if (returnVariable != null) {
			Variable.getInstance().setVariableValue(
					testStepRunner.getTestSuiteRunner(), "methodCall",
					returnVariable, false, retVal);
		}
		LOGGER.info("QTP ExecuteAction: DONE");
		return retVal;
	}

	@Override
	public void captureScreenshot(String sPath) {
		LOGGER.info("<QTP>Capture snapshot to :" + sPath);
		Dispatch.call(xAFTQTPEngineCOM, "CaptureImage", sPath);
	}

	@Override
	public String getObjectId(String elementName) {
		// TODO Need to discuss with Sandeep Joshi
		// return Dispatch.call(xAFTQTPEngineCOM, "GetObjectID",
		// elementName).toString();
		return "";
	}

	public void loadObjectRepository(String objectReposFilePath)
			throws AFTException {
		Dispatch.call(xAFTQTPEngineCOM, "AddRepository", objectReposFilePath);

	}

	@Override
	public void unLoadObjectRepository() throws AFTException {
		// TODO:: remove third parameter from this call...
		Dispatch.call(xAFTQTPEngineCOM, "RemoveRepository");

	}

	@Override
	public String getBrowserVersion() {
		LOGGER.info("QTP! Getting Browser Version");
		String browserVersion ="--";
		try{
			browserVersion = Dispatch.call(xAFTQTPEngineCOM,
				"GetBrowserVersion", currentBrowser).toString();
		}catch(Exception e)
		{
			//If not browser, expecting exception
			browserVersion = "--";
		}
		// Trimming the minor version
		//browserVersion.indexOf(".");
		browserVersion = browserVersion.substring(0, browserVersion.indexOf(".")+2);
		return browserVersion;
	}

	@Override
	public void executeAnnotation(String annotationName, String annotationValue)
			throws AFTException {
		LOGGER.info("Executing [executeAnnotation], annotation Name ["
				+ annotationName + "], annotation value [" + annotationValue
				+ "]");

		// TODO Auto-generated method stub
		Dispatch.call(xAFTQTPEngineCOM, "ExecuteAnnotation", annotationName,
				annotationValue);

	}

	@Override
	public String getCurrentURL() {
		return null;
	}
}
