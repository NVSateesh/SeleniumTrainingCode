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
 * Class: WebTableFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the Table Object in the UI
 */

package com.ags.aft.frankensteinDriver.fixtures;

import org.apache.log4j.Logger;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.objectRepository.ObjectRepositoryManager;
import com.ags.aft.objectRepository.RepositoryObject;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.ags.aft.frankensteinDriver.fixtures.WaitFixtures;

/**
 * The Class SwingTreeFixtures.
 * 
 */
public class TreeFixtures {

	/** Default wait time **/
	private int WAITTIME_MS = 500;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TreeFixtures.class);

	/** The Tree fixtures. */
	private final WaitFixtures waitFixtures;

	/** The tree for element. */
	private boolean waitForElement;

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public TreeFixtures() {
		waitFixtures = new WaitFixtures();
	}

	/**
	 * selectTreeNode in Swing Application .
	 * 
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the value
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */

	public void selectTreeNode(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();

				String[] parsedElementValueArr = parsedElementValue.split("->");

				AFTFrankensteinBase.getInstance().getDriver()
						.selectTree(name, parsedElementValueArr);

				LOGGER.info("[selectTree] executed on [" + name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * rightclickTree in Swing Application .
	 * 
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the value
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */

	public void rightclickTree(String objectID, String parsedElementValue,
			String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();

				String[] parsedElementValueArr = parsedElementValue.split("->");

				AFTFrankensteinBase.getInstance().getDriver()
						.rightClickTree(name, parsedElementValueArr);

				LOGGER.info("[rightclickTree] executed on [" + name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * select item selectTreeNodeContextMenu.
	 * 
	 * @param elementName
	 *            the element name
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the text value to search for
	 * @return true, if is element present
	 * @throws AFTException
	 *             the application exception
	 */

	public void selectTreeNodeContextMenu(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		try {
			LOGGER.trace("Waiting for element [" + objectID + "] to be present");

			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);

			if (waitForElement) {
				String name = repositoryObject.getName();
				String[] parsedElementValueNav = parsedElementValue.split(",");
				String[] parsedElementValueArr = parsedElementValueNav[0]
						.split("->");

				AFTFrankensteinBase.getInstance().getDriver()
						.rightClickTree(name, parsedElementValueArr);

				Thread.sleep(WAITTIME_MS);

				AFTFrankensteinBase.getInstance().getDriver()
						.navigate(parsedElementValueNav[1]);

				LOGGER.info("[selectContextMenuTreeRightClick] executed on ["
						+ name + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * getSelectedTreeNode: Get Selected Tree Node name from Swing Application .
	 * 
	 * @param objectID
	 *            the object id
	 * @param value
	 *            the value
	 * @param elementName
	 *            the element name
	 * @return
	 * @throws AFTException
	 *             the application exception
	 */

	public String getSelectedTreeNode(String objectID,
			String parsedElementValue, String elementName) throws AFTException {
		String selectedNode = null;
		try {

			LOGGER.trace("Waiting for element [" + objectID + "] to be present");
			waitForElement = waitFixtures.waitForElementPresent(objectID,
					UIFixtureUtils.getInstance().getElementWaitTime(),
					elementName);

			RepositoryObject repositoryObject = ObjectRepositoryManager
					.getInstance().getObject(elementName);
			if (waitForElement) {
				String name = repositoryObject.getName();
				selectedNode = AFTFrankensteinBase.getInstance().getDriver()
						.selectedNodeName(name);

				LOGGER.info("[selectedTree Node name] executed on ["
						+ selectedNode + "]");
			} else {
				LOGGER.trace("Element [" + elementName + "] is found");
				throw new AFTException("Element [" + elementName
						+ "] not found");
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return selectedNode;
	}

	/**
	 * getTree ChildNode Count.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public int getTreeChildNodeCount(String elementName) throws AFTException {
		int nodeCount = 0;
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String name = repositoryObject.getName();
		if (name.isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				LOGGER.trace("Element [" + name + "] is found");
				LOGGER.trace("Executing command [getXpathCount]");
				nodeCount = AFTFrankensteinBase.getInstance().getDriver()
						.childNodeCount(name);

				Variable.getInstance().setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_TREECHILDNODECOUNT), true,
						Integer.toString(nodeCount));

			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return nodeCount;
	}

	/**
	 * getTree ChildNode name.
	 * 
	 * @param objectID
	 *            the objectID
	 * @param elementName
	 *            element name
	 * @return the table row count
	 * @throws AFTException
	 *             the application exception
	 */
	public String getTreeChildNodeName(String elementName) throws AFTException {
		String nodeName = "";
		RepositoryObject repositoryObject = ObjectRepositoryManager
				.getInstance().getObject(elementName);
		String name = repositoryObject.getName();
		String searchText="";
		if (name.isEmpty()) {
			LOGGER.error("Element [" + name
					+ "] not found in Object Repository");
			throw new AFTException("Element [" + name
					+ "] not found in Object Repository");
		} else {
			try {
				String[] parsedElementValueArr = elementName.split(",");
				if(parsedElementValueArr[1].contains("#")){
					searchText=Helper.getInstance().getActionValue(parsedElementValueArr[1]);
				}else{
					searchText=parsedElementValueArr[1];
				}
				int nodeIndex = Integer.parseInt(searchText);
				
				LOGGER.trace("Element [" + name + "] is found");
				nodeName = AFTFrankensteinBase.getInstance().getDriver()
						.childNodeName(name, nodeIndex);

			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return nodeName;
	}

}
