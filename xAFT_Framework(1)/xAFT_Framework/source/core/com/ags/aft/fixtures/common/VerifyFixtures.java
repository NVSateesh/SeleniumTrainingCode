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
 * Class: VerifyFixtures
 * 
 * Purpose: This class implements methods that allows users to perform
 * validations on the UI objects
 */

package com.ags.aft.fixtures.common;

import org.apache.log4j.Logger;

import com.ags.aft.constants.Constants;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.fixtures.linkchecker.LinkChecker;
import com.ags.aft.fixtures.spellChecker.SpellChecker;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;

/**
 * The Class VerifyFixtures.
 * 
 */
public class VerifyFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(VerifyFixtures.class);

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public VerifyFixtures() {
	}

	/**
	 * Verify spelling.
	 * 
	 * @param action
	 *            the action
	 * @param testStepRunner
	 *            the test step runner
	 * @param elementName
	 *            the element name
	 * @param elementValue
	 *            the element value
	 * @param parsedElementValue
	 *            the parsed element value
	 * @return true, if successful
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean verifySpelling(String action, TestStepRunner testStepRunner,
			String elementName, String elementValue, String parsedElementValue)
			throws AFTException {
		boolean flag = false;
		String innerText = "";
		if (EngineManager.getInstance().getCurrentExecutionEngine() != null) {
			innerText = EngineManager.getInstance().getCurrentExecutionEngine().executeAction(
							testStepRunner, "getText", elementName,
							parsedElementValue, elementValue);
		}

		if (!innerText.isEmpty()) {

			String currentURL = EngineManager.getInstance()
					.getCurrentExecutionEngine().getCurrentURL();

			Log4JPlugin.getInstance().writeSpellErrors(
					"------------------" + "Verifying spellings on page: "
							+ currentURL + "------------------\n");


			if(!parsedElementValue.isEmpty()){
				if(parsedElementValue.contains(",")){
					flag = SpellChecker.getInstatnce().verifySpelling(innerText,parsedElementValue.split(",")[0].trim(),parsedElementValue.split(",")[1].trim(),testStepRunner);
				}else{
					flag = SpellChecker.getInstatnce().verifySpelling(innerText,parsedElementValue.trim(),testStepRunner);
				}
			}else{
				flag = SpellChecker.getInstatnce().verifySpelling(innerText,testStepRunner);
			}
			if (!flag) {
				throw new AFTException("Total count["
						+ SpellChecker.getInstatnce().getSpellErrorCount()
						+ "], " + SpellChecker.getInstatnce().getSpellErrors());
			} else {
				LOGGER.info("No spelling errors found!!");
			}
		} else {
			throw new AFTException("[" + elementName
					+ "] doesn't contain any text");
		}

		return flag;
	}

	/**
	 * Verify links.
	 * @param testStepRunner
	 *           testStepRunner
	 * @param url
	 *            the url
	 * @return true, if successful
	 * @throws AFTException
	 *             the aFT exception
	 */
	public boolean verifyLinks(TestStepRunner testStepRunner,String url) throws AFTException {

		boolean bErrors = true;
		int brokenLinkCount = 0;

		try {
			LOGGER.debug("Creating an instance of LinkChecker");

			Log4JPlugin.getInstance().writeLinkErrors(
					"------------------" + "Verifying links on page: " + url
							+ "------------------\n");

			LinkChecker linkChecker = LinkChecker.getInstance();

			linkChecker.validateLinks(testStepRunner,url);

			Log4JPlugin.getInstance().writeLinkErrors("\n");

			brokenLinkCount = linkChecker.getBrokenLinkCount();

			if (brokenLinkCount > 0) {
				LOGGER.error("# of good links found ["
						+ linkChecker.getGoodLinkCount()
						+ "], # of broken links found [" + brokenLinkCount
						+ "]!");
				throw new AFTException("Total Broken links count["
						+ brokenLinkCount + "]. ["
						+ linkChecker.getBrokenLinkList().toString() + "]");
			} else {
				LOGGER.info("# of good links found ["
						+ linkChecker.getGoodLinkCount() + "]");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return bErrors;
	}

	/**
	 * Verifies whether the specified values are matching or not
	 * 
	 * @param value
	 *            the list of items to be verified
	 * @param testStepRunner
	 *            testStepRunner
	 * @return true or false based on the verification
	 * @throws AFTException
	 */
	public boolean verifyDataValues(String value, TestStepRunner testStepRunner)
			throws AFTException {
		boolean result = false;
		String[] dataValues = null;
		String parsedValue = value;
		if ((parsedValue.trim().startsWith(
				Constants.TESTDATASTARTVARIABLEIDENTIFIER) && parsedValue
				.trim().endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER))
				|| (parsedValue.trim().startsWith(
						Constants.DYNAMICVARIABLEDELIMITER) && parsedValue
						.trim().endsWith(Constants.DYNAMICVARIABLEDELIMITER))) {
			parsedValue = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), parsedValue);
		}
		dataValues = parsedValue.trim().split(",");
		if (dataValues.length < 2) {
			String errMsg = "Invalid verifyDataValues usage ["
					+ parsedValue
					+ "] specified. Please refer technical documentation on how to use [verifyDataValues]";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		if (dataValues[0].equalsIgnoreCase(dataValues[1])) {
			result = true;
		}
		if (!result) {
			String errMsg = ("Verify: Failure, [" + dataValues[0] + "]"
					+ " doesn't match with [" + dataValues[1] + "]");
			LOGGER.error(errMsg);
			result = false;
			throw new AFTException(errMsg);
		}
		return result;
	}
}
