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
 * Class: ScriptInstance
 * 
 * Purpose: This class stores script instance object
 */
package com.ags.aft.fixtures.externalScript;

import javax.script.Invocable;
import org.apache.log4j.Logger;
import com.ags.aft.util.Helper;

public class ScriptInstance {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Helper.class);

	// Script instance object returned by engine.eval()
	private Object scriptObj;

	/**
	 * @return scriptObj
	 */
	public Object getScriptObj() {
		return scriptObj;
	}

	// script engine instance
	private Invocable invocableScript;

	/**
	 * @return invocableScript
	 */
	public Invocable getInvocableScript() {
		return invocableScript;
	}

	// external script file name and path
	private String scriptFilePath;

	/**
	 * @return scriptFilePath
	 */
	public String getScriptFilePath() {
		return scriptFilePath;
	}

	/**
	 * Script Instance constructor to initialize the object
	 * @param script
	 *          script
	 * @param engine
	 *          engine
	 * @param scriptFile
	 *          scriptFile
	 */
	public ScriptInstance(Object script, Invocable engine, String scriptFile) {
		LOGGER.debug("Script Instance object created for script file ["
				+ scriptFile + "]!");

		scriptObj = script;
		invocableScript = engine;
		scriptFilePath = scriptFile;
	}

}
