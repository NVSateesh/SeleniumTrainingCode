/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General public License, Version 3.0 (the "License") you
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
 * Class: Constants
 * 
 * Purpose:This class will have all the common constants used by AFT
 */

package com.ags.aft.constants;

/**
 * The Class aftConstants. This class will have all the common constants used by
 * AFT
 */
public class Constants {

	/**
	 * constructor for Constants.
	 */
	protected Constants() {

	}

	/** The Close browser. */
	public static final boolean CLOSEBROWSER = false;
	/** The Empty value. */
	public static final String EMPTYVALUE = "novalue";
	/** The Test result file. */
	public static final String TESTREPORTRESULTFILE = "TestResultList.xml";
	/** The Test data result list file. */
	public static final String TESTDATAREPORTRESULTFILE = "TestDataResultList.xml";
	/** The Colors file. */
	public static final String COLORSFILE = "Color.txt";
	/** The Currency code file. */
	public static final String CURRENCYCODEFILE = "CurrencyCode.txt";
	/** The Female first name file. */
	public static final String FEMALEFIRSTNAMEFILE = "FemaleFirstName.txt";
	/** The Last name file. */
	public static final String LASTNAMEFILE = "LastName.txt";
	/** The Male first name file. */
	public static final String MALEFIRSTNAMEFILE = "MaleFirstName.txt";
	/** The Month file. */
	public static final String MONTHFILE = "Month.txt";
	/** The Name file. */
	public static final String NAMEFILE = "Name.txt";
	/** The US city file. */
	public static final String USCITYFILE = "USCity.txt";
	/** The US state code file. */
	public static final String USSTATECODEFILE = "USStateCode.txt";
	/** The US state name file. */
	public static final String USSTATENAMEFILE = "USStateName.txt";
	/** The US street address file. */
	public static final String USSTREETADDRESSFILE = "USStreetAddress.txt";
	/** The US zip code file. */
	public static final String USZIPCODEFILE = "USZipCode.txt";
	/** The CC number file. */
	public static final String CCNUMBERSFILE = "CCN_list.txt";
	/** The US phone number file. */
	public static final String USPHONENOFILE = "USPhoneNumber.txt";
	/** The SSN number file. */
	public static final String SSNNUMBERSFILE = "SSN_list.txt";
	/** The Email subject started. */
	public static final String EMAILSUBJECTSTARTED = "AutomatedFunctionalTester: Successfully started test";
	/** The Email body started. */
	public static final String EMAILMSGBODYSTARTED = "AutomatedFunctionalTester: Successfully started test at ";
	/** The Email subject end. */
	public static final String EMAILSUBJECTEND = "AutomatedFunctionalTester: Successfully completed test";
	/** The Email body end. */
	public static final String EMAILMSGBODYEND = "AutomatedFunctionalTester: Completed test execution at ";
	/** The Email subject exception. */
	public static final String EMAILSUBJECTEXCEPTION = "AutomatedFunctionalTester: Encountered exception / error";
	/** The Email to. */
	public static final String EMAILTO = "aft@allianceglobalservices.com";
	/** The Email subject. */
	public static final String EMAILSUBJECT = "This is dummy test Subject";
	/** The Email body. */
	public static final String EMAILBODY = "This is a dummy test body of the Email";
	/** The Default send framework notification email's. */
	public static final String DEFAULTSENDFRAMEWORKNOTIFICATIONEMAILS = "Yes";
	/** The Execution speed slow. */
	public static final String EXECUTIONSPEEDSLOW = "slow";
	/** The Execution speed medium. */
	public static final String EXECUTIONSPEEDMEDIUM = "medium";
	/** The Execution speed normal. */
	public static final String EXECUTIONSPEEDNORMAL = "normal";
	/** The Execution engine. */
	public static final String EXECUTIONENGINE = "ExecutionEngine";
	/** The Executed procedure name. */
	public static final String EXECUTEDBPROCEDUREPARAMPROCEDURENAME = "procname";
	/** The Executed procedure connection identifier. */
	public static final String EXECUTEDBPROCEDUREPARAMCONNECTIONIDENTIFIER = "connectionidentifier";
	/** The Executed procedure parameters. */
	public static final String EXECUTEDBPROCEDUREPARAMPARAMETERS = "parameters";
	/** The Common config. */
	public static final String COMMONCONFIG = "CommonConfig";
	/** The Test failure. */
	public static final String TESTSTEPFAILURE = "  - Failure Notification";
	/** The scenario ID. */
	public static final String SCENARIOID = "BusinessScenario # ";
	public static final String GETELEMENTSCREENSHOT = "getElementScreenshot";
	

	/*
	 * public static String[] BATCH_PARAMS = { Constants.BROWSER_TYPE,
	 * APPLICATION_URL, APPLICATION_NAME, TESTSUITE_PATH, SCENARIO_SHEET_NAME,
	 * TESTSTEPS_SHEETNAME, OBJECT_REPOSITORY_PATH, TESTDATA_TABLE_PATH,
	 * REUSABLE_TEST_SUITE_PATH, REUSABLE_SCENARIOS_SHEETNAME,
	 * REUSABLE_TESTSTEPS_SHEETNAME, APP_CONFIG_FILE_PATH,
	 * APP_EXECUTION_CONFIGURATION, TCM_INTEGRATION_CONFIG_FILE_PATH, ca,
	 * EXECUTION_ENGINE };
	 */
	/** This array contains list of all columns of Test Scenario sheet. */
	public static final String[] TESTSCENARIOCOLUMNHEADINGS = {
			"Business Scenario Id", "Test Case Id / Requirement Id",
			"Business Scenario Description", "Category", "Execution Flag" };

	/** This array contains list of all columns of Test Steps sheet. */
	public static final String[] TESTSTEPCOLUMNHEADINGS = {
			"Business Scenario Id", "Test Case Id", "Test Case Description",
			"Pre-Step Action", "Pre-Step ElementPath/Name (Visible)",
			"Pre-Step ElementValue", "Step Action",
			"Step ElementPath/Name (Visible)", "Step ElementValue",
			"Post-Step Action", "Post-Step ElementPath/Name (Visible)",
			"Post-Step ElementValue" };

	/*
	 * public static String EXECUTE_DB_PROCEDURE_PARAM_CONNECTIONIDENTIFIER =
	 * "connectionidentifier";
	 * 
	 * public static String EXECUTE_DB_PROCEDURE_PARAM_PROCEDURE_NAME =
	 * "procname";
	 * 
	 * public static String EXECUTE_DB_PROCEDURE_PARAM_PARAMETERS =
	 * "parameters"; }
	 */
	/** Execution speed slow value. */
	public static final int EXECUTIONSPEEDSLOWVALUE = 500;
	/** Execution speed medium value. */
	public static final int EXECUTIONSPEEDMEDIUMVALUE = 250;
	/** Execution speed normal value. */
	public static final int EXECUTIONSPEEDNORMALVALUE = 0;
	/** Default execution speed. */
	public static final String DEFAULTEXECUTIONSPEED = EXECUTIONSPEEDNORMAL;
	/** Default execution speed value. */
	public static final int DEFAULTEXECUTIONSPEEDVALUE = EXECUTIONSPEEDNORMALVALUE;
	/** Default URL. */
	public static final String DEFAULTURL = "http://www.allianceglobalservices.com";
	/** Pre step prefix. */
	public static final String PRESTEPPREFIX = "Pre-";
	/** Post step prefix. */
	public static final String POSTSTEPPREFIX = "Post-";
	/** dynamic variable delimiter. */
	public static final String DYNAMICVARIABLEDELIMITER = "#";
	/** Test data start variable identifier. */
	public static final String TESTDATASTARTVARIABLEIDENTIFIER = "${";
	/** Test data end variable identifier. */
	public static final String TESTDATAENDVARIABLEIDENTIFIER = "}$";
	/** Attributes delimiter. */
	public static final String ATTRIBUTESDELIMITER = "^";
	/** Object repository identifier start delimiter. */
	public static final String OBJECTREPOSITORYIDENTIFIERSTARTDELIMITER = "@{";
	/** Object repository identifier end delimiter. */
	public static final String OBJECTREPOSITORYIDENTIFIERENDDELIMITER = "}@";
	/** Static parameter start identifier. */
	public static final String STATICPARAMETERSTARTIDENTIFIER = "[[";
	/** Static parameter end identifier. */
	public static final String STATICPARAMETERENDIDENTIFIER = "]]";
	/** Dynamic variable escape char delimiter. */
	public static final String DYNAMICVARIABLEESCAPECHARDELIMITER = "\\\\";
	/** Dynamic variable escape char delimiter. */
	public static final String ESCAPECHARDELIMITERS = "\\#${[";
	/** Default DB fixture time out */
	public static final String DEFAULTDBFIXTURETIMEOUT = "60000";
	/** DB fixture parameter delimiter */
	public static final String DBFIXTUREPARAMDELIMITER = ";";
	/** DB fixture stored procedure input parameter delimiter */
	public static final String DBFIXTURESTOREDPROCINPUTPARAMLISTDELIMITER = "|";
	/** DB fixture stored procedure data type value delimiter */
	public static final String DBFIXTURESTOREDPROCDATATYPENVALUEDELIMITER = "^";
	/** Max array row count */
	public static final int MAXARRAYROWCOUNT = 100;
	/** Max array column count */
	public static final int MAXARRAYCOLUMNCOUNT = 100;
	/** Array access notation char */
	public static final String ARRAYACCESSNOTATIONCHAR = ".";
	/** DB array type */
	public static final String DBARRAYTYPE = "database";
	/** Web table array type */
	public static final String WEBTABLEARRAYTYPE = "webtable";
	/** Excel array type */
	public static final String EXCELARRAYTYPE = "excel";
	/** User created array type */
	public static final String USERCREATEDARRAYTYPE = "usercreated";
	/** Test start email notification */
	public static final int TESTSTARTEMAILNOTIF = 1;
	/** Test end email notification */
	public static final int TESTENDEMAILNOTIF = 2;
	/** Variable */
	public static final int VARIABLE = 1;
	/** Test data */
	public static final int TESTDATA = 2;
	/** Date format */
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	/** Date format report */
	public static final String DATEFORMATREPORT = "yyyy.MM.dd-HH.mm.ss.S";
	/** Date format folder name */
	public static final String DATEFORMATFOLDERNAME = "yyyyMMdd_HHmmssS";
	/** Date format log file */
	public static final String DATEFORMATLOGFILE = "yyyyMMdd_HHmmss";
	/** Date format screen shot */
	public static final String DATEFORMATSCREENSHOT = "yyyyMMdd_HHmmssS";
	/** Execute suite */
	public static final String EXECUTESUITE = "ExecuteSuite";
	/** Browser type */
	public static final String BROWSERTYPE = "BrowserType";
	/** Application url */
	public static final String APPLICATIONURL = "ApplicationUrl";
	/** Application name */
	public static final String APPLICATIONNAME = "ApplicationName";
	/** Test suite path */
	public static final String TESTSUITEPATH = "TestSuitePath";
	/** Scenario sheet name */
	public static final String SCENARIOSHEETNAME = "ScenariosSheetName";
	/** Test steps sheet name */
	public static final String TESTSTEPSSHEETNAME = "TestStepsSheetName";
	/** Object repository path */
	public static final String OBJECTREPOSITORYPATH = "ObjectRepositoryPath";
	/** Test data table path */
	public static final String TESTDATATABLEPATH = "TestDataTablePath";
	public static final String FAIL = "FAIL";
	/** Name */
	public static final String TESTNAME = "Name";
	/** ReusableTestSuite path */
	public static final String REUSABLETESTSUITEPATH = "ReusableTestSuitePath";
	/** ReusableTestScenarios SheetName */
	public static final String REUSABLESCENARIOSSHEETNAME = "ReusableScenariosSheetName";
	/** ReusableTestSteps SheetName */
	public static final String REUSABLETESTSTEPSSHEETNAME = "ReusableTestStepsSheetName";
	/** TCMIntegration ConfigFilePath */
	public static final String TCMINTEGRATIONCONFIGFILEPATH = "TCMIntegrationConfigFilePath";
	/** AppConfig FilePath */
	public static final String APPCONFIGFILEPATH = "AppConfigFilePath";
	/** AppExecution Configuration */
	public static final String APPEXECUTIONCONFIGURATION = "AppExecutionConfiguration";
	/**Category */
	public static final String CATEGORY = "Category";
	/** Scenario initialization id's */
	public static final String SCENARIOINITIALIZATIONIDS = "ScenarioInitializationIDs";
	/** Scenario clean up id's */
	public static final String SCENARIOCLEANUPIDS = "ScenarioCleanupIDs";
	/** Test initialization id's */
	public static final String TESTSETINITIALIZATIONIDS = "TestSetInitializationIDs";
	/** Test set cleanup id's */
	public static final String TESTSETCLEANUPIDS = "TestSetCleanupIDs";
	/** Object delimiter */
	public static final String OBJECTDELIMITER = ",";
	/** Step prefix */
	public static final String STEPPREFIX = "step";
	/** Table prefix */
	public static final String TABLEPREFIX = "dt_";
	/** reusable value */
	public static final String LOADREUSABLETESTSCENARIOS = "1";
	/** non reusable value */
	public static final String LOADNONREUSABLETESTSCENARIOS = "0";
	/** soap */
	public static final String SOAP = "SOAP";
	/** rest */
	public static final String REST = "REST";
	/** xml */
	public static final String XML = "XML";
	/** get */
	public static final String GET = "GET";
	/** post */
	public static final String POST = "POST";
	/** put */
	public static final String PUT = "PUT";
	/** delete */
	public static final String DELETE = "DELETE";
	/** json */
	public static final String JSON = "JSON";
	/** root */
	public static final String ROOT = "root";
	/** Custome dictionary path */
	public static final String CUSTOMDICTIONARYPATH = "CustomDictionaryPath";
	/** Spell check language */
	public static final String SPELLCHECKLANGUAGE = "SpellCheckLanguage";
	/** Spell check suggestion */
	public static final String SPELLCHECKSUGGESTION = "SpellCheckSuggestion";
	/** AFT config type */
	public static final int AFTCONFIGTYPE = 1;
	/** Webdriver config type */
	public static final int WEBDRIVERCONFIGTYPE = 2;
	/** qtp config type */
	public static final int QTPCONFIGTYPE = 3;
	/** Twin config type */
	public static final int TWINCONFIGTYPE = 4;
	/** Robotium config type */
	public static final int ROBOTIUMCONFIGTYPE = 5;
	/** Appium config type */
	public static final int APPIUMCONFIGTYPE = 7;
	/**Config type 6 is used for AppConfig and 8 for ETL**/
	
	/** TestComplete config type */
	public static final int TESTCOMPLETECONFIGTYPE = 9;
	

	public static final String FULLSCREEN = "fullPage";

	/**
	 * This array contains list of all NON-UI actions to see if a screenshot
	 * should be captured or not...
	 */
	public static final String[] NONUIACTIONS = { "ifThenElse",
			"generateDynamicObjectId", "executeReusableTestScenario",
			"loadObjectRepository", "getTestDataRowCount",
			"getCurrentTestDataRow", "getTestDataRowNumber", "copyData",
			"createScriptInstance", "executeScript", "destroyScriptInstance",
			"WS_", "Operator", "openDBConnection", "executeDBCommand",
			"closeDBConnection", "executeStoredProc", "executeDBQuery",
			"logTestResultInTCM" };

	// These flags are used to check pass as parameter to methods to indicate
	// from which execution level they are called
	//
	/** Test case */
	public static final int TESTCASE = 1;
	/** Test scenario */
	public static final int TESTSCENARIO = 2;
	/** Test suite */
	public static final int TESTSUITE = 3;
	/** Batch params */
	public static final String[] BATCHPARAMS = { Constants.BROWSERTYPE,
			APPLICATIONURL, APPLICATIONNAME, TESTSUITEPATH, SCENARIOSHEETNAME,
			TESTSTEPSSHEETNAME, OBJECTREPOSITORYPATH, TESTDATATABLEPATH,
			REUSABLETESTSUITEPATH, REUSABLESCENARIOSSHEETNAME,
			REUSABLETESTSTEPSSHEETNAME, APPCONFIGFILEPATH,
			APPEXECUTIONCONFIGURATION, TCMINTEGRATIONCONFIGFILEPATH,
			EXECUTIONENGINE, CATEGORY, SCENARIOINITIALIZATIONIDS,
			SCENARIOCLEANUPIDS, TESTSETINITIALIZATIONIDS, TESTSETCLEANUPIDS };
}
