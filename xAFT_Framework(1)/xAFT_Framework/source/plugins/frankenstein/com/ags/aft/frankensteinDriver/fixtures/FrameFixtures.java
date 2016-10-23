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
 * Class: FrameFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on frames
 */

package com.ags.aft.frankensteinDriver.fixtures;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;

/**
 * The Class FrameFixtures.
 */
public class FrameFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(FrameFixtures.class);

	/** The wait fixtures. */
	private final WaitFixtures waitFixtures;

	/** The wait for element. */
	private boolean waitForElement;

	/**
	 * Instantiates a new command fixtures.
	 * 
	 */
	public FrameFixtures() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 * Set frame: set the frame
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void setFrame(String objectID, String elementName)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [selectFrame]");
				String name = repositoryObject.getName();
				AFTFrankensteinBase.getInstance().getDriver()
						.setFrameContext(name);
				LOGGER.info("[Activated] window [" + objectID + "]");
			} else {
				LOGGER.trace("Element [" + objectID + "] is found");
				throw new AFTException("Element [" + objectID + "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Set Default frame: set the frame
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void setDefaultFrame() {
		LOGGER.trace("Executing command: [selectFrame]");
		AFTFrankensteinBase.getInstance().getDriver().setFrameContext("top");
	}

}
