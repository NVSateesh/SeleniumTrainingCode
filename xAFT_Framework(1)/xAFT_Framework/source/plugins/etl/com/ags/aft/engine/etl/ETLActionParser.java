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
 * Class: Parser
 * 
 * Purpose: This class creates instances of other classes, parses the commands
 * and invokes the appropriate or relevant methods.
 */

package com.ags.aft.engine.etl;

import org.apache.log4j.Logger;

import com.ags.aft.etl.fixtures.ValidationFixtures;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;

/**
 * The Class seleniumMethodSelector.
 */
public class ETLActionParser {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ETLActionParser.class);

	// fixture objects

	/** The Validation fixture object. */
	private ValidationFixtures objValidate = new ValidationFixtures();

	/** The Start Time. */
	private long testActionStartTime;

	/** The Executing Action Flag. */
	private boolean isExecutingAction = false;

	/**
	 * Parses action, calls method on the corresponding fixture to execution
	 * action.
	 * 
	 * @param testStepRunner
	 *            Test Step Runner Object Instance
	 * @param action
	 *            action to perform
	 * @param elementName
	 *            User defined elementName for the object
	 * @param objectID
	 *            parsed object id
	 * @param parsedElementValue
	 *            parsed action value
	 * @param actualValue
	 *            actualValue
	 * @return returned value
	 * @throws AFTException
	 *             the application exception
	 */
	String parseAndExecute(TestStepRunner testStepRunner, String action,
			String elementName, String objectID, String parsedElementValue,
			String actualValue) throws AFTException {
		String result = "";
		try {
			if (!action.equals("")) {
				// set action start time
				setTestActionStartTime(System.currentTimeMillis());
				// set Executing Action flag to true.
				setExecutingAction(true);
				if (action.equalsIgnoreCase("verifyDataCorrectness")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifyDataCorrectness(
							objectID, elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifyDataCompleteness")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifyDataCompleteness(
							objectID, elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifySchema")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objValidate.verifySchema(objectID, elementName,
							parsedElementValue);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			setExecutingAction(false);
		}
		return result;
	}

	/**
	 * @return isExecutingAction
	 */
	public boolean isExecutingAction() {
		return isExecutingAction;
	}

	/**
	 * @param isExecutingAction
	 *            the isExecutingAction to set
	 */
	public void setExecutingAction(boolean isExecutingAction) {
		this.isExecutingAction = isExecutingAction;
	}

	/**
	 * @return testActionStartTime
	 */
	public long getTestActionStartTime() {
		return testActionStartTime;
	}

	/**
	 * @param testActionStartTime
	 *            the testActionStartTime to set
	 */
	public void setTestActionStartTime(long testActionStartTime) {
		this.testActionStartTime = testActionStartTime;
	}
}