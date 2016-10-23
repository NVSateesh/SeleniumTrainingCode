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
 * Class: Annotations
 * 
 * Purpose: This class will have list of all the Annotations used in the AFT
 * project
 */

package com.ags.aft.constants;

/**
 * The Class aftAnnotationss. This class will have list of all the Annotations
 * used by AFT
 */
public class Annotations {
	/**
	 * constructor for Annotations.
	 */
	protected Annotations() {

	}

	// List of Annotations
	// public static final String ExecuteReusableTestScenario =
	// "@executeReusableTestScenario";
	public static final String JUMPTOTESTCASE = "@jumpToTestCase";
	public static final String LOGLEVEL = "@logLevel";
	public static final String SETEXECUTIONSPEED = "@setExecutionSpeed";
	public static final String MOVETONEXTTESTDATAROW = "@moveToNextTestDataRow";
	public static final String MOVETOPREVTESTDATAROW = "@moveToPrevTestDataRow";
	public static final String SETTESTDATAROW = "@setTestDataRow";
	public static final String TERMINATECURRENTTESTCASE = "@terminateCurrentTestCase";
	public static final String TERMINATECURRENTTESTSUITE = "@terminateCurrentTestSuite";
	public static final String TERMINATECURRENTTESTSCENARIO = "@terminateCurrentTestScenario";
	public static final String CONTINUE = "@continue";
	public static final String RAISEEXCEPTION = "@raiseException";
	public static final String CAPTURESCREENSHOT = "@captureScreenShot";
	public static final String ONERROR = "@onError";
	public static final String SETTCMINTEGRATIONTESTCASEID = "@setTCMIntegrationTestCaseID";
	public static final String ELEMENTPOLLTIME_S = "@ElementPollTime_s";
	public static final String CAPTURESCREENSHOTNOW = "@captureScreenShotNow";
	public static final String LOGMESSAGE = "@logMessage";
	public static final String USESCRIPTINSTANCE = "@useScriptInstance";
	public static final String DISPLAYSCROLLABLEFRAME = "@displayScrollableFrame";
	public static final String SETSIKULIIMAGESPATH = "@setSikuliImagesPath";
	public static final String REPORTTESTSTEP = "@reportTestStep";
	public static final String CUSTOMDICTIONARYPATH="@customDictionaryPath";
	public static final String SWITCHTOENGINE="@switchToExecutionEngine";


	// public static final String LoadObjectRepository =
	// "@loadObjectRepository";
	// public static final String getTestDataRowCount = "@getTestDataRowCount";
	// public static final String getCurrentTestDataRow =
	// "@getCurrentTestDataRow";
}
