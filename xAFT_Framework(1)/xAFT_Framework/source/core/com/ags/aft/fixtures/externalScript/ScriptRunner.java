/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General Public License, Version 3.0 (the "License") youent
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
 * Class: ExternalScriptRunner
 * 
 * Purpose: This class implements methods that allows users to execute external
 * scripts written in Jython/Python/JScript/Ruby
 */
package com.ags.aft.fixtures.externalScript;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.DynamicClassLoader;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.ags.aft.webdriver.common.AFTSeleniumBase;

/**
 * The Class ScriptsRunner class
 */
public class ScriptRunner {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ScriptRunner.class);

	/** The script engine manager. */
	private ScriptEngineManager manager = null;

	/** The api connector class. */
	private static Class<?> apiConnectorClass = null;

	/** The api connector object. */
	private static Object apiConnectorObject = null;

	/**
	 * Instantiates a new aFT external scripts fixture.
	 * 
	 */
	public ScriptRunner() {
		manager = new ScriptEngineManager();
	}

	/**
	 * Creates an instance of the external script and stores it for use
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param elementName
	 *            the external file path and function to invoke
	 * @param scriptIdentifier
	 *            user defined variable name to store created script instance
	 * @return the created script instance
	 * @throws AFTException
	 */
	public String createScriptInstance(TestStepRunner testStepRunner,
			String elementName, String scriptIdentifier) throws AFTException {

		ScriptInstance scriptInstance = null;
		String returnVal = null;
		String objectName = elementName;
		String extension = "";
		String scriptValue = "";
		Object scriptObj = "";

		try {
			// checking whether the elementName parameter actually contains
			// multiple parameters
			// Splitting the parameters into an array

			LOGGER.info("User specified script [" + objectName + "]");
			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();

			if (isFileSystemRequest) {
				// Get the file type based on the file name extension
				extension = getFileType(objectName);
			} else {
				String[] values = objectName.split("\\.");
				// get the custom script module id based on element name
				int moduleId = DatabaseUtil.getInstance()
						.getCustomScriptModuleId(values[0]);
				// Based on module id and custom script name get the script type
				// and script value.
				Map<String, String> customScriptsData = DatabaseUtil
						.getInstance()
						.getCustomScriptsData(moduleId, values[1]);
				String type = customScriptsData.get("scriptType");
				// Get the file type based on the file name extension
				extension = getFileType("." + type);
				scriptValue = customScriptsData.get("scriptValue");
			}
			// parse the value string to substitute values for
			// variables and test data
			objectName = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), objectName);

			if (extension.equalsIgnoreCase("jar")) {
				if (objectName.contains(",")) {
					String jarPath = objectName.split(",")[0].trim();
					String classToLoad = objectName.split(",")[1].trim();

					URL urls[] = {};

					// Call the loader class to load the API Integration jar
					DynamicClassLoader loader = new DynamicClassLoader(urls);
					apiConnectorClass = loader.loadJar(jarPath, classToLoad);

					apiConnectorObject = apiConnectorClass.newInstance();
					LOGGER.debug("Successfully created instance of ["
							+ classToLoad + "] class");

					scriptInstance = new ScriptInstance(apiConnectorObject,
							null, objectName);
				} else {
					String errorMsg = "Jar file path or Class name "
							+ "["
							+ objectName
							+ "]"
							+ " not specified. Please go through Titanium xAFT documentation on how to call Java APIs.";
					LOGGER.error(errorMsg);
					throw new Exception(errorMsg);
				}
			} else {
				/*
				 * Retrieve a new instance of a ScriptingEngine for the
				 * specified extension of a script file.
				 */
				ScriptEngine engine = manager.getEngineByName(extension);
				// create instance of Invocable
				Invocable invocableScript = (Invocable) engine;

				// create instance of file input stream passing the external
				// file as parameter
				if (isFileSystemRequest) {
					LOGGER.trace("Creating new filestream object for file ["
							+ objectName + "]");
					InputStream fis = new FileInputStream(objectName);
					// create instance of reader passing the file input stream
					// as a
					// parameter
					LOGGER.trace("Opening input stream reader for file ["
							+ objectName + "]");
					Reader reader = new InputStreamReader(fis);
					// evaluate the reader
					LOGGER.trace("Execute the specified script by calling engine.eval() for script file ["
							+ objectName + "]");
					scriptObj = engine.eval(reader);
					LOGGER.trace("Closing Input Stream Reader");
					reader.close();
					LOGGER.trace("Closing File Input Stream");
					fis.close();
				} else {
					scriptObj = engine.eval(scriptValue);
				}

				scriptInstance = new ScriptInstance(scriptObj, invocableScript,
						objectName);
			}

			String scriptInstanceIdentifier = ScriptInstanceManager
					.getInstance().createUniqueScriptInstanceIdentifier();

			ScriptInstanceManager.getInstance().addScriptInstance(
					scriptInstanceIdentifier, scriptInstance);

			returnVal = scriptInstanceIdentifier;

			/*
			 * Set the return value to the variable passed by the user
			 */
			// if user did not provide any variable to store the script
			// Identifier
			if (scriptIdentifier == null || scriptIdentifier.isEmpty()
					|| scriptIdentifier.equalsIgnoreCase(Constants.EMPTYVALUE)) {
				LOGGER.info("No user variable passed");

			} else {
				// assign scriptIdentifier already stored in returnVal
				LOGGER.info("Storing script instance [" + returnVal
						+ "] in user variable [" + scriptIdentifier + "]");
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(),
						"createScriptInstance", scriptIdentifier, false,
						returnVal);
			}

			// store the scriptIdentifier in system variable
			// AFT_LastScriptInstance
			LOGGER.info("Storing script instance ["
					+ returnVal
					+ "] in system variable [AFT_CURRENTSCRIPTINSTANCE] by default");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_CURRENTSCRIPTINSTANCE), true,
					returnVal);

		} catch (FileNotFoundException fe) {
			LOGGER.error("FileNotFoundException::", fe);
			throw new AFTException(fe);
		} catch (ScriptException se) {
			LOGGER.error("ScriptException::", se);
			throw new AFTException(se);
		} catch (IOException e) {
			LOGGER.error("IOException::", e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);

			String exceptionMsg = getExceptionMessage(e);

			Exception ee = new Exception(exceptionMsg);
			LOGGER.error("Exception::", ee);
			// now throw the Application exception with the exception message...
			throw new AFTException(e);
		}

		LOGGER.debug("Script identifier [" + returnVal
				+ "] created for external script [" + objectName + "]");
		return returnVal;
	}

	/**
	 * Destroys the script instance associated with the given script
	 * identifier/System variable
	 * 
	 * @param scriptIdentifier
	 *            identifier passed by user
	 * 
	 * @throws AFTException
	 */
	public void destroyScriptInstance(String scriptIdentifier)
			throws AFTException {

		String scriptInstanceIdentifier = null, lastScriptInstance = null;
		String errorMessage;
		String scriptID = scriptIdentifier;

		LOGGER.trace("Executing [destroyScriptInstance] for script identifier ["
				+ scriptID + "]");

		// if user has not passed an identifier
		if (scriptID == null || scriptID.isEmpty()) {
			LOGGER.info("User has not passed script identifier. "
					+ "Getting script instance from AFT_LastScriptInstance");
			scriptID = Variable.getInstance().generateSysVarName(
					SystemVariables.AFT_CURRENTSCRIPTINSTANCE);
		}

		// get the actual value
		scriptInstanceIdentifier = Helper.getInstance()
				.getActionValue(scriptID);

		// if there is no value returned
		if (scriptInstanceIdentifier.isEmpty()
				|| !ScriptInstanceManager.getInstance()
						.checkScriptInstanceKeyExists(scriptInstanceIdentifier)) {
			errorMessage = "No script instance found. Please refer to Wiki for more details on [destroyScriptInstance]";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// get the connection object mapped to the key
		ScriptInstanceManager.getInstance().removeScriptInstance(
				scriptInstanceIdentifier);

		lastScriptInstance = Helper.getInstance().getActionValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_CURRENTSCRIPTINSTANCE));
		if (lastScriptInstance.compareToIgnoreCase(scriptInstanceIdentifier) == 0) {
			// store the randomName in system variable AFT_LastScriptInstance
			LOGGER.info("User has destroyed the script instance stored in system variable [AFT_LastScriptInstance]. Let us clear this system variable also.");
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_CURRENTSCRIPTINSTANCE), true,
					"");
		}
	}

	/**
	 * Executes external script.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * 
	 * @param elementName
	 *            the script identifier, method to execute and user defined
	 *            variable to store value returned by the called method
	 * @param parameter
	 *            the method parameters
	 * @return the value returned by the called method
	 * @throws AFTException
	 */
	public String executeScript(TestStepRunner testStepRunner,
			String elementName, String parameter) throws AFTException {
		String dynamicVariableDelimiter = Constants.DYNAMICVARIABLEDELIMITER;
		Object returnValue = null;
		String returnVal = "";
		String returnVariable = null;
		String methodName = null;
		String scriptIdentifier = null;
		List<String> scriptParamArr = new ArrayList<String>();
		try {
			LOGGER.trace("Validating if the elementName specified in test suite contains both the file path and function name or not...");
			// checking whether the elementName parameter actually contains
			// multiple parameters
			// Splitting the parameters into an array
			LOGGER.trace("Splitting the parameters to find the script instance, method name and user-defined variable to store value returned by the called method");
			String[] elements = elementName.split(",");

			if (elements[0].contains(dynamicVariableDelimiter)) {

				if (elements.length < 2) {
					String errMsg = "user must specify methodname and variable to store return value from method. Please verify the syntax and rerun the tests.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}

				// the first parameter is script identifier
				scriptIdentifier = elements[0];
				LOGGER.debug("Script identifier is [" + scriptIdentifier + "]");

				methodName = elements[1].trim();
				LOGGER.debug("Method to be invoked is [" + methodName + "]");

				if (elements.length > 2) {
					returnVariable = elements[2];
					LOGGER.debug("Variable to store value returned by method is ["
							+ returnVariable + "]");
				}
			} else {
				scriptIdentifier = Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_CURRENTSCRIPTINSTANCE);
				LOGGER.debug("No script identifier specified. Using [AFT_LastScriptInstance]");

				// the first parameter is method to execute
				methodName = elements[0].trim();
				LOGGER.debug("Method to be invoked is [" + methodName + "]");

				if (elements.length > 1) {
					returnVariable = elements[1];
					LOGGER.debug("Variable to store value returned by method is ["
							+ returnVariable + "]");
				}
			}

			// get the actual value of the script identifier
			scriptIdentifier = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), scriptIdentifier);

			ScriptInstance scriptInstance = ScriptInstanceManager.getInstance()
					.getStoredScriptInstance(scriptIdentifier);
			if (scriptInstance == null) {
				String errMsg = "Invalid script identifier ["
						+ scriptIdentifier
						+ "] specified. Pls verify that the script instance could be successfully created or this instance has not been destroyed already.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
			if (!parameter.contains("novalue")) {
				scriptParamArr = Helper.getInstance().parseActionParameterList(
						testStepRunner.getTestSuiteRunner(), parameter, true);
			}
			if (scriptParamArr.size() <= 1) {
				if (scriptInstance.getScriptObj() == null) {
					/*
					 * if the method to be invoked is not within a class and if
					 * there is only one or zero parameters then invoke this
					 * method
					 */
					returnValue = runMethodWithZeroOrOneParam(
							scriptInstance.getInvocableScript(), methodName,
							scriptParamArr);
				} else {
					/*
					 * invoke this method if the method is within a class and
					 * there is only one or zero parameters
					 */
					returnValue = runMethodWithinClassWithZeroOrOneParam(
							scriptInstance.getInvocableScript(),
							scriptInstance.getScriptObj(), methodName,
							scriptParamArr);
				}
			} else if (scriptParamArr.size() > 1) {
				if (scriptInstance.getScriptObj() == null) {
					/*
					 * if the method to be invoked is not within a class and if
					 * there is more than one parameter then invoke this method.
					 * Selenium and Log4J objects are passed as the first and
					 * second parameters.
					 */
					returnValue = runMethodWithMultipleParams(
							scriptInstance.getInvocableScript(), methodName,
							scriptParamArr);
				} else {
					/*
					 * invoke this method if the method is within a class and
					 * there is more than one parameters. Selenium and Log4J
					 * objects are passed as the first and second parameters.
					 */
					returnValue = runMethodWithinClassWithMultiParams(
							scriptInstance.getInvocableScript(),
							scriptInstance.getScriptObj(), methodName,
							scriptParamArr);
				}
			}
			if (returnValue != null) {
				returnVal = returnValue.toString();
			} else {
				returnVal = "";
			}
			/*
			 * if element name contains dynamic variable delimiter set the
			 * return value to the dynamic variable.
			 */
			if (returnVariable != null) {
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(), "executeScript",
						returnVariable, false, returnVal);
			}

			/*
			 * Set the return value to the system variable
			 */
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_EXTERNALSCRIPTRETURNVALUE),
					true, returnVal);

		} catch (ScriptException se) {
			LOGGER.error("ScriptException::", se);
			throw new AFTException(se);
		} catch (NoSuchMethodException e) {
			LOGGER.error("NoSuchMethodException::", e);
			throw new AFTException(e);
		} catch (InstantiationException e) {
			LOGGER.error("InstantiationException::", e);
			throw new AFTException(e);
		} catch (IllegalAccessException e) {
			LOGGER.error("IllegalAccessException::", e);
			throw new AFTException(e);
		} catch (InvocationTargetException e) {
			LOGGER.error("Exception:: " + e.getCause(), e);
			throw new AFTException(e);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);

			String exceptionMsg = getExceptionMessage(e);

			int index = exceptionMsg.indexOf('\n', 0);
			String exceptionDetail = exceptionMsg.substring(0, index);
			if (exceptionDetail.length() < 2) {
				exceptionDetail = exceptionMsg;
			}
			Exception ee = new Exception(exceptionDetail);
			ee.setStackTrace(e.getStackTrace());
			LOGGER.error("Exception::", ee);
			// now throw the Application exception with the exception message...
			throw new AFTException(e);
		}

		LOGGER.debug("Returning value [" + returnVal
				+ "] after executing method [" + methodName
				+ "] for script identifier [" + scriptIdentifier + "]");
		return returnVal;
	}

	/**
	 * Executes script which takes zero or one parameter as input. Selenium and
	 * Log4J instances are passed as parameters.
	 * 
	 * @param invocableScript
	 *            invocableScript
	 * @param methodName
	 *            the methodName
	 * @param scriptParamArr
	 *            the value
	 * @return the object
	 * @throws NoSuchMethodException
	 * @throws ScriptException
	 */
	private Object runMethodWithZeroOrOneParam(Invocable invocableScript,
			String methodName, List<String> scriptParamArr)
			throws ScriptException, NoSuchMethodException {
		Object returnValue = null;

		/*
		 * invoke the function. Selenium and Log4J objects are passed as the
		 * first and second parameters.
		 */
		if (scriptParamArr.size() == 0) {
			LOGGER.info("Invoking function [" + methodName
					+ "] with no parameters.");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER);
		} else {
			LOGGER.info("Invoking function [" + methodName
					+ "] with 1 parameter [" + scriptParamArr.get(0) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0));
		}

		return returnValue;
	}

	/**
	 * Runs the method within a class which takes zero or one parameter as
	 * input. Selenium and Log4J instances are passed as parameters.
	 * 
	 * @param invocableScript
	 *            invocableScript
	 * @param scriptObj
	 *            the scriptObj
	 * @param methodName
	 *            the methodName
	 * @param scriptParamArr
	 *            the scriptParamArr
	 * @return the object
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ScriptException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	private Object runMethodWithinClassWithZeroOrOneParam(
			Invocable invocableScript, Object scriptObj, String methodName,
			List<String> scriptParamArr) throws InstantiationException,
			IllegalAccessException, ScriptException, NoSuchMethodException,
			InvocationTargetException {
		Method method = null;
		Object returnValue = null;
		Object instance = null;
		LOGGER.trace("Creating instance of the class [" + scriptObj + "]");
		// Create instance of class in the script file
		if (invocableScript != null) {
			if (!invocableScript.getClass().getSimpleName()
					.equalsIgnoreCase("RhinoScriptEngine")) {
				instance = ((Class<? extends Object>) scriptObj).newInstance();
			}

		} else {
			apiConnectorClass = scriptObj.getClass();
			apiConnectorObject = scriptObj;
		}

		/*
		 * invoke the method defined in the class with the instance created
		 * above. Selenium and Log4J objects are passed as the first and second
		 * parameters.
		 */
		if (scriptParamArr.size() == 0) {

			LOGGER.info("Invoking function [" + methodName
					+ "] with no parameters.");
			if (invocableScript != null) {

				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {

					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER);
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER);
				}

			} else {
				method = apiConnectorClass.getMethod(methodName);
				returnValue = method.invoke(apiConnectorObject);
			}
		} else {
			LOGGER.info("Invoking function [" + methodName
					+ "] with 1 parameter [" + scriptParamArr.get(0) + "]");
			if (invocableScript != null) {

				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0));
				} else {
					returnValue = invocableScript
							.invokeMethod(instance, methodName, AFTSeleniumBase
									.getInstance().getDriver(), LOGGER,
									scriptParamArr.get(0));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName,
						new Class[] { String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0));
			}
		}
		return returnValue;
	}

	/**
	 * Executes script which takes multiple parameters as input. Selenium and
	 * Log4J instances are passed as parameters.
	 * 
	 * @param invocableScript
	 *            invocableScript
	 * @param methodName
	 *            the methodName
	 * @param scriptParamArr
	 *            the scriptParamArr
	 * @return the object
	 * @throws NoSuchMethodException
	 * @throws ScriptException
	 */
	private Object runMethodWithMultipleParams(Invocable invocableScript,
			String methodName, List<String> scriptParamArr)
			throws ScriptException, NoSuchMethodException {
		Object returnValue = null;
		// Splitting the parameter list into an array
		switch (scriptParamArr.size()) {
		case 2:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 2 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1));
			break;
		case 3:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 3 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2));
			break;
		case 4:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 4 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3));
			break;
		case 5:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 5 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4));
			break;
		case 6:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 6 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4), scriptParamArr.get(5));
			break;
		case 7:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 7 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4), scriptParamArr.get(5),
					scriptParamArr.get(6));
			break;
		case 8:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 8 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4), scriptParamArr.get(5),
					scriptParamArr.get(6), scriptParamArr.get(7));
			break;
		case 9:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 9 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "," + scriptParamArr.get(8) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4), scriptParamArr.get(5),
					scriptParamArr.get(6), scriptParamArr.get(7),
					scriptParamArr.get(8));
			break;
		case 10:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 10 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "," + scriptParamArr.get(8) + ","
					+ scriptParamArr.get(9) + "]");
			returnValue = invocableScript.invokeFunction(methodName,
					AFTSeleniumBase.getInstance().getDriver(), LOGGER,
					scriptParamArr.get(0), scriptParamArr.get(1),
					scriptParamArr.get(2), scriptParamArr.get(3),
					scriptParamArr.get(4), scriptParamArr.get(5),
					scriptParamArr.get(6), scriptParamArr.get(7),
					scriptParamArr.get(8), scriptParamArr.get(9));
			break;
		default:
			LOGGER.warn("Invalid number of parameters specified. Maximum of 10 parameters can be passed to the script fn. Pls refer to AFT documentation for more details.");
		}

		return returnValue;
	}

	/**
	 * Run method within a class which takes multiple parameters as input.
	 * Selenium and Log4J instances are passed as parameters.
	 * 
	 * @param invocableScript
	 *            the invocableScript
	 * @param scriptObj
	 *            the scriptObj
	 * @param methodName
	 *            the methodName
	 * @param scriptParamArr
	 *            the scriptParamArr
	 * @return the object
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	private Object runMethodWithinClassWithMultiParams(
			Invocable invocableScript, Object scriptObj, String methodName,
			List<String> scriptParamArr) throws InstantiationException,
			IllegalAccessException, ScriptException, NoSuchMethodException,
			InvocationTargetException {
		Method method = null;

		Object returnValue = null;
		// Splitting the parameter list into an array
		LOGGER.trace("Creating instance of the class [" + scriptObj + "]");
		Object instance = null;
		if (invocableScript != null) {
			if (!invocableScript.getClass().getSimpleName()
					.equalsIgnoreCase("RhinoScriptEngine")) {
				instance = ((Class<? extends Object>) scriptObj).newInstance();
			}
		} else {
			apiConnectorClass = scriptObj.getClass();
			apiConnectorObject = scriptObj;
		}
		switch (scriptParamArr.size()) {
		case 2:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 2 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "]");
			if (invocableScript != null) {

				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1));
				}

			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class });

				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1));
			}
			break;
		case 3:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 3 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2));
				}

			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2));
			}
			break;
		case 4:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 4 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3));
				}
				;
			} else {
				method = apiConnectorClass.getMethod(methodName,
						new Class[] { String.class, String.class, String.class,
								String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3));
			}

			break;
		case 5:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 5 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class, String.class,
						String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4));
			}
			break;
		case 6:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 6 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class, String.class,
						String.class, String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4), scriptParamArr.get(5));
			}
			break;
		case 7:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 7 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class, String.class,
						String.class, String.class, String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4), scriptParamArr.get(5),
						scriptParamArr.get(6));
			}
			break;
		case 8:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 8 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName,
						new Class[] { String.class, String.class, String.class,
								String.class, String.class, String.class,
								String.class, String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4), scriptParamArr.get(5),
						scriptParamArr.get(6), scriptParamArr.get(7));
			}
			break;
		case 9:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 9 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "," + scriptParamArr.get(8) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7),
							scriptParamArr.get(8));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7),
							scriptParamArr.get(8));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class, String.class,
						String.class, String.class, String.class, String.class,
						String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4), scriptParamArr.get(5),
						scriptParamArr.get(6), scriptParamArr.get(7),
						scriptParamArr.get(8));
			}
			break;
		case 10:
			LOGGER.info("Invoking function [" + methodName
					+ "] with 10 parameters [" + scriptParamArr.get(0) + ","
					+ scriptParamArr.get(1) + "," + scriptParamArr.get(2) + ","
					+ scriptParamArr.get(3) + "," + scriptParamArr.get(4) + ","
					+ scriptParamArr.get(5) + "," + scriptParamArr.get(6) + ","
					+ scriptParamArr.get(7) + "," + scriptParamArr.get(8) + ","
					+ scriptParamArr.get(9) + "]");
			if (invocableScript != null) {
				if (invocableScript.getClass().getSimpleName()
						.contains("RhinoScriptEngine")) {
					returnValue = invocableScript.invokeFunction(methodName,
							AFTSeleniumBase.getInstance().getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7),
							scriptParamArr.get(8), scriptParamArr.get(9));
				} else {
					returnValue = invocableScript.invokeMethod(instance,
							methodName, AFTSeleniumBase.getInstance()
									.getDriver(), LOGGER,
							scriptParamArr.get(0), scriptParamArr.get(1),
							scriptParamArr.get(2), scriptParamArr.get(3),
							scriptParamArr.get(4), scriptParamArr.get(5),
							scriptParamArr.get(6), scriptParamArr.get(7),
							scriptParamArr.get(8), scriptParamArr.get(9));
				}
			} else {
				method = apiConnectorClass.getMethod(methodName, new Class[] {
						String.class, String.class, String.class, String.class,
						String.class, String.class, String.class, String.class,
						String.class, String.class });
				returnValue = method.invoke(apiConnectorObject,
						scriptParamArr.get(0), scriptParamArr.get(1),
						scriptParamArr.get(2), scriptParamArr.get(3),
						scriptParamArr.get(4), scriptParamArr.get(5),
						scriptParamArr.get(6), scriptParamArr.get(7),
						scriptParamArr.get(8), scriptParamArr.get(9));
			}
			break;
		default:
			LOGGER.warn("Invalid number of parameters specified. Maximum of 10 parameters can be passed to the script fn. Pls refer to AFT documentation for more details.");
		}
		return returnValue;
	}

	/**
	 * Gets the extension of the particular script file.
	 * 
	 * @param elementName
	 *            the external file path and function to invoke
	 * @return the extension
	 */
	private String getFileType(String elementName) {
		String fileType = "";
		LOGGER.trace("Verifying the type of file...");
		if (elementName.endsWith(".js")) {
			LOGGER.debug("JavaScript file specified is [" + elementName + "]");
			fileType = "JavaScript";
		} else if (elementName.endsWith(".groovy")) {
			LOGGER.debug("Groovy file specified is [" + elementName + "]");
			fileType = "groovy";
		} else if (elementName.endsWith(".py") || elementName.endsWith(".jy")) {
			LOGGER.debug("Python/Jython file specified is [" + elementName
					+ "]");
			fileType = "python";
		} else if (elementName.endsWith(".rb")) {
			LOGGER.debug("Ruby file specified is [" + elementName + "]");
			fileType = "ruby";
		} else if (elementName.contains(".jar")) {
			LOGGER.debug("Jar file specified is [" + elementName + "]");
			fileType = "jar";
		}
		return fileType;
	}

	/**
	 * Parse nested exception object for exception message and stack trace.
	 * 
	 * @param e
	 *            exception object
	 * @return exception message and stack trace
	 */
	private String getExceptionMessage(Exception e) {
		String exceptionMsg = "";

		if (e.getMessage() != null) {
			exceptionMsg = e.getMessage();
		} else if (e.getLocalizedMessage() != null) {
			exceptionMsg = e.getLocalizedMessage();
		} else {
			// ah, no message in the exception object, let us parse the
			// stacktrace to construct the error message...

			// create a StringWriter object to print the stack trace
			StringWriter sw = new StringWriter();
			// Create a PrintWriter object to flush the stack trace from the
			// throwable object...
			PrintWriter pw = new PrintWriter(sw);
			try {
				e.printStackTrace(pw);
			} catch (RuntimeException ex) {
			}
			// we have written the stack trace to this print object. Let us
			// flush it to get the content written to the print object...
			pw.flush();

			// Let us read the stack trace line by line
			LineNumberReader reader = new LineNumberReader(new StringReader(
					sw.toString()));
			try {
				String line = reader.readLine();
				while (line != null) {
					exceptionMsg += line + "\n";
					line = reader.readLine();

					// if this is empty line, it means we have reached end
					// of one block of exception, typically the actual
					// exception message..
					// let us break because we just need the exception
					// message and not the complete stack trace...
					if (line == null || line.length() <= 0) {
						break;
					}
				}
			} catch (InterruptedIOException ioe) {
				Thread.currentThread().interrupt();
			} catch (IOException ex) {
				exceptionMsg += "\n" + ex.toString();
			}
		}

		return exceptionMsg;
	}
}
