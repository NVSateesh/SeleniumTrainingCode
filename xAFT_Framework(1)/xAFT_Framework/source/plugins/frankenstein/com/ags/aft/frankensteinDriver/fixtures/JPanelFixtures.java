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
public class JPanelFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(JPanelFixtures.class);

	/** The wait fixtures. */
	private final WaitFixtures waitFixtures;

	/** The wait for element. */
	private boolean waitForElement;

	/**
	 * Instantiates a new command fixtures.
	 * 
	 */
	public JPanelFixtures() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 *rigthClickPanelContext: To rightClick on the panel context
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void rightClickPanelContext(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			AFTFrankensteinBase.getInstance().getDriver().rightClickPanelContext(objectID);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	/**
	 *panelEvent: To rightClick on the panel context
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	/*public void panelEventMenu(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			AFTFrankensteinBase.getInstance().getDriver().panelEvent(objectID);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}*/
	
	
	
	
	
	/**
	 * click vertex: click vertex in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void clickVertex(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.clickVertex(objectID, elementValue);
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
	 * rightClick vertex: rightClick vertex in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void rightClickVertex(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.rightClickVertex(objectID, elementValue);
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
	 * Click Edge: Click Edge in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void clickEdge(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.clickEdge(objectID, elementValue);
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
	 * RightClick Edge: RightClick Edge in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void rightClickEdge(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.rightClickEdge(objectID, elementValue);
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

	 /* SelectVertex: Select Vertex in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectVertex(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.selectVertex(objectID, elementValue);
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

	
	/*SelectEdge: Select Edge in JGraph
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void selectEdge(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.selectEdge(objectID, elementValue);
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
	
	/*isGraphPresent: is JGraph present
	 * 
	 * @param objectID
	 *            = Object ID in properties file
	 * @param elementName
	 *            the element name
	 * @throws AFTException
	 *             the application exception
	 */
	public void isGraphPresent(String objectID, String elementName, String elementValue)
			throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + elementName + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			if (waitForElement) {
				LOGGER.trace("Element [" + objectID + "] is found");
				LOGGER.trace("Executing command: [clickVertex]");
				AFTFrankensteinBase.getInstance().getDriver()
						.isGraphPresent(objectID);
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
