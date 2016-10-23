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

package com.ags.aft.engine.frankensteinDriver;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;
import com.ags.aft.frankensteinDriver.common.AFTFrankensteinBase;
import com.ags.aft.frankensteinDriver.common.FrankensteinDriverConfigProperties;
import com.ags.aft.frankensteinDriver.common.UIFixtureUtils;
import com.ags.aft.frankensteinDriver.fixtures.FrameFixtures;
import com.ags.aft.frankensteinDriver.fixtures.JGraphFixtures;
import com.ags.aft.frankensteinDriver.fixtures.JPanelFixtures;
import com.ags.aft.frankensteinDriver.fixtures.MouseKeyboardEvents;
import com.ags.aft.frankensteinDriver.fixtures.TableFixture;
import com.ags.aft.frankensteinDriver.fixtures.TreeFixtures;
import com.ags.aft.frankensteinDriver.fixtures.UICommandFixtures;
import com.ags.aft.frankensteinDriver.fixtures.ValidationFixtures;
import com.ags.aft.frankensteinDriver.fixtures.WaitFixtures;
import com.ags.aft.frankensteinDriver.fixtures.WindowFixtures;
import com.ags.aft.runners.TestStepRunner;

/**
 * The Class FrankensteinDriverActionParser.
 */
public class FrankensteinDriverActionParser {

	/** The Constant LOGGER. */

	private static final Logger LOGGER = Logger
			.getLogger(FrankensteinDriverActionParser.class);

	/** Default wait time **/
	private int sleepTime = 100;

	// fixture objects
	/** The Table fixture object. */
	private TableFixture objUITable = new TableFixture();
	/** The UICommand fixture object. */
	private UICommandFixtures objUICommand = new UICommandFixtures();

	/** The Validation fixture object. */
	private ValidationFixtures objValidate = new ValidationFixtures();

	/** The Wait fixture object. */
	private WaitFixtures objWait = new WaitFixtures();

	/** The Keyboardevents fixture object. */
	private MouseKeyboardEvents objKbdMouseEvents = new MouseKeyboardEvents();

	/** The Tree fixture object. */
	private TreeFixtures objTree = new TreeFixtures();

	/** The JGraph fixture object. */
	private JGraphFixtures jgraph = new JGraphFixtures();
	
	/** The JPanel fixture object. */
	private JPanelFixtures jpanel = new JPanelFixtures();
	
	/** The window fixture object. */
	private WindowFixtures objwin = new WindowFixtures();

	/** The frame fixture object. */
	private FrameFixtures objfrm = new FrameFixtures();

	private String lastAction = "";

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
	 * @return returned value
	 * @throws AFTException
	 *             the application exception
	 */
	String parseAndExecute(TestStepRunner testStepRunner, String action,
			String elementName, String objectID, String parsedElementValue,
			String actualValue) throws AFTException {
		String result = null;

		try {
			Thread.sleep(3500);
			if (!action.equals("")) {
				// Browser fixtures...
				//
				if (action.equalsIgnoreCase("type")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					objUICommand.type(objectID, parsedElementValue,
							elementName, lastAction);
				} else if (action.equalsIgnoreCase("cleartext")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.type(objectID, "", elementName, lastAction);

				} else if (action.equalsIgnoreCase("open")) {
					// Thread.sleep(10000);
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					try {
						AFTFrankensteinBase.getInstance().setUpFrankenstein(
								XAFTFrankensteinDriverEngine
										.getFrankensteinDriverEngineInstance()
										.getBaseClass());
					} catch (ClassNotFoundException e) {
						LOGGER.error("Exception::", e);
						throw new AFTException(e);
					}
				} else if (action.equalsIgnoreCase("loadJNLP")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					try {
						AFTFrankensteinBase.getInstance().loadJNLP(
								parsedElementValue);
					} catch (ClassNotFoundException e) {
						LOGGER.error("Exception::", e);
						throw new AFTException(e);
					}
				} else if (action.equalsIgnoreCase("close")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					if (parsedElementValue.isEmpty()) {
						parsedElementValue = objectID;
					}
					AFTFrankensteinBase.getInstance().getDriver()
							.closeApplication(parsedElementValue);
				//AFTFrankensteinBase.getInstance().stopFrankenstein();
				} else if (action.equalsIgnoreCase("quit")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					if (parsedElementValue.isEmpty()) {
						parsedElementValue = objectID;
					}
					AFTFrankensteinBase.getInstance().getDriver().closeApplication(parsedElementValue);
				AFTFrankensteinBase.getInstance().stopFrankenstein();
				} else if (action.equalsIgnoreCase("appendText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.appendText(objectID, parsedElementValue,
							elementName);
				} else if (action.equalsIgnoreCase("click")) {

					LOGGER.trace("Command [" + action + "], object ["
							+
							objectID + "]");
					
					objUICommand.click(objectID, elementName);

					/*
					 * } else if (action.equalsIgnoreCase("getValue")) {
					 * LOGGER.trace("Command [" + action + "], object [" +
					 * objectID + "]"); result = objUICommand.getValue(objectID,
					 * elementName);
					 */

				} else if (action.equalsIgnoreCase("setParent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objwin.setParent(objectID, elementName);
				} else if (action.equalsIgnoreCase("setParentContext")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					if (elementName.equalsIgnoreCase("novalue")) {
						elementName = parsedElementValue;
					}
					objwin.setParentContext(elementName);
				} else if (action.equalsIgnoreCase("setDefaultParent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objwin.setDefaultParent();
				} else if (action.equalsIgnoreCase("customClick")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					if (!parsedElementValue.isEmpty()) {
						objUICommand.customClick(objectID, parsedElementValue);
					} else {
						objUICommand.customClick(objectID, elementName);
					}
				} else if (action.equalsIgnoreCase("clickLabel")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.clickLabel(objectID);

				} else if (action.equalsIgnoreCase("mouseClick")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.mouseClick(objectID, elementName);

				} else if (action.equalsIgnoreCase("mouseDown")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.mouseDown(objectID, elementName);

				} else if (action.equalsIgnoreCase("mouseUp")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.mouseUp(objectID, elementName);

				} else if (action.equalsIgnoreCase("generateDynamicObjectId")) {
					LOGGER.trace("Command [" + action + "], elementName ["
							+ elementName + "], value [" + parsedElementValue
							+ "]");
					objUICommand.generateDynamicObjectId(testStepRunner,
							elementName, parsedElementValue, actualValue);
				} else if (action.equalsIgnoreCase("check")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.check(objectID, elementName);

				} else if (action.equalsIgnoreCase("uncheck")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUICommand.uncheck(objectID, elementName);

				} else if (action.equalsIgnoreCase("selectListOptions")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.selectListOptions(objectID,
							parsedElementValue, elementName, "value");
				} else if (action.equalsIgnoreCase("isOptionPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUICommand.verifyListOptions(objectID,
							parsedElementValue, elementName) + "";
				} else if (action.equalsIgnoreCase("selectOptionByIndex")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.selectListOptions(objectID,
							parsedElementValue, elementName, "index");
				} else if (action.equalsIgnoreCase("selectMenu")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.selectMenu(objectID, parsedElementValue,
							elementName);
				} else if (action.equalsIgnoreCase("isEnabled")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUICommand.isEnabled(objectID,
							parsedElementValue, elementName) + "";
				} else if (action.equalsIgnoreCase("selectTab")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUICommand.selectTab(objectID, elementName,
							parsedElementValue);
				} else if (action.equalsIgnoreCase("getText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUICommand.getText(objectID, elementName);
				} else if (action.equalsIgnoreCase("getTooltip")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUICommand.getTooltip(objectID, elementName);
				} else if (action.equalsIgnoreCase("getTabName")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUICommand.getTabName(objectID, elementName);
				} else if (action.equalsIgnoreCase("getLabel")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUICommand.getLabel(objectID, elementName);
				} else if (action.equalsIgnoreCase("verifyLabel")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean stateValue = objValidate.verifyLabel(objectID,
							elementName, parsedElementValue);

					result = Boolean.valueOf(stateValue).toString();
				} else if (action.equalsIgnoreCase("getOptionCount")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					int count = objUICommand.getOptionCount(objectID,
							elementName);
					result = Integer.valueOf(count).toString();
				}
				// JGraph fixtures
				else if (action.equalsIgnoreCase("clickVertex")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.clickVertex(objectID, elementName,
							parsedElementValue);
					// result = Integer.valueOf(count).toString();
				} else if (action.equalsIgnoreCase("clickEdge")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.clickEdge(objectID, elementName, parsedElementValue);
					// result = Integer.valueOf(count).toString();
				} else if (action.equalsIgnoreCase("rightClickVertex")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.rightClickVertex(objectID, elementName,
							parsedElementValue);
					// result = Integer.valueOf(count).toString();
				} else if (action.equalsIgnoreCase("rightClickEdge")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.rightClickEdge(objectID, elementName,
							parsedElementValue);
					// result = Integer.valueOf(count).toString();
					
				}
				
				else if (action.equalsIgnoreCase("rightClickPanel")) {
							LOGGER.trace("Command [" + action + "], object ["
									+ objectID + "]");
							jpanel.rightClickPanelContext(objectID, elementName,
									parsedElementValue);					
				} else if (action.equalsIgnoreCase("selectVertex")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.selectVertex(objectID, elementName,
							parsedElementValue);
					// result = Integer.valueOf(count).toString();
				} else if (action.equalsIgnoreCase("selectEdge")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.selectEdge(objectID, elementName, parsedElementValue);
					// result = Integer.valueOf(count).toString();
				} else if (action.equalsIgnoreCase("isGraphPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					jgraph.isGraphPresent(objectID, elementName,
							parsedElementValue);
					// result = Integer.valueOf(count).toString();
				}
				// Table Fixtures...
				else if (action.equalsIgnoreCase("isTableHeaderPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					result = objUITable.isTableHeaderPresent(objectID,
							elementName);
				}
				else if (action.equalsIgnoreCase("getTableHeader")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					result = objUITable.getTableHeader(elementName);
				}else if (action.equalsIgnoreCase("getTableColumnCount")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					result = Integer.toString(objUITable.getTableColumnCount(
							objectID, elementName));

				} else if (action.equalsIgnoreCase("getTableRowCount")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					result = Integer.toString(objUITable.getTableRowCount(
							objectID, elementName));

				} else if (action.equalsIgnoreCase("selectTableRow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUITable.selectTableRow(objectID, parsedElementValue,
							elementName);
				} else if (action.equalsIgnoreCase("selectAllTableRows")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUITable.selectAllTableRows(objectID, elementName);
				} else if (action.equalsIgnoreCase("selectTableRowWithText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUITable.selectTableRowWithText(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("getRowWithText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getRowWithText(objectID, elementName);
				}else if (action.equalsIgnoreCase("getEMSRowWithTask")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getEMSRowWithTask(objectID, elementName);
				}else if (action.equalsIgnoreCase("getEMSRowCountWithPartialTask")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getEMSRowCountWithPartialTask(objectID, elementName);
				}else if (action.equalsIgnoreCase("getEMSClaimTask")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getEMSClaimTask(objectID, elementName);
				}  else if (action.equalsIgnoreCase("rightClickOnTableRow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUITable.rightClickOnTableRow(objectID,
							parsedElementValue, elementName);

				} else if (action
						.equalsIgnoreCase("selectContextMenuTableCellRightClick")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUITable.selectContextMenuTablecellRightClick(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("doubleClickTableCell")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					Thread.sleep(sleepTime);
					objUITable.doubleClickTableCell(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("selectTablecell")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objUITable.selectTablecell(objectID, parsedElementValue,
							elementName);
				} else if (action.equalsIgnoreCase("getCellValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					Thread.sleep(sleepTime);
					result = objUITable.getCellValue(objectID, elementName);
				}else if (action.equalsIgnoreCase("isCellEditable")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					Thread.sleep(sleepTime);
					result = objUITable.isCellEditable(objectID, elementName)+"";
				}else if (action.equalsIgnoreCase("getAllTableRowTextValidation")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getAllTableRowTextValidation(objectID, parsedElementValue,
							elementName)+"";					
				}else if (action.equalsIgnoreCase("setEMSValidationComent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					 objUITable.setEMSValidationComent(objectID, parsedElementValue,
							elementName);					
				}else if (action.equalsIgnoreCase("getEMSApprovedTask")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUITable.getEMSApprovedTask(objectID, elementName);					
				}else if (action.equalsIgnoreCase("getCellPlainText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUITable.getCellPlainText(objectID, elementName);
				} else if (action.equalsIgnoreCase("getCustomTableCellValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUITable.getCustomTableCellValue(elementName);

				} else if (action.equalsIgnoreCase("editTableCell")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUITable.edittablecell(objectID, parsedElementValue,
							elementName);

				} else if (action.equalsIgnoreCase("setTableCellValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUITable.setTableCellValue(objectID, parsedElementValue,
							elementName);

				} else if (action.equalsIgnoreCase("selectCellValueByIndex")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUITable.selectCellValueByIndex(objectID, parsedElementValue,
							elementName);

				}else if (action.equalsIgnoreCase("getCellOptionCount")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUITable.getCellOptionCount(objectID,elementName)+"";

				}
				else if (action.equalsIgnoreCase("getCellSelectedValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objUITable.getCellSelectedValue(objectID, elementName)+"";

				} else if (action.equalsIgnoreCase("selectTableCheckbox")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objUITable.selectTableCheckbox(objectID,
							parsedElementValue, elementName);

				}
				// Dialog Fixtures
				else if (action.equalsIgnoreCase("CloseDialog")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objwin.closeDialog(objectID, elementName);

				} else if (action.equalsIgnoreCase("selectDialog")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objwin.selectDialog(objectID, elementName);

				} else if (action.equalsIgnoreCase("activateDialog")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					objwin.activateDialog(objectID, elementName);

				} else if (action.equalsIgnoreCase("getDialogtext")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = (objwin.getDialogtext(objectID, elementName));
				}

				// Tree Fixtures

				else if (action.equalsIgnoreCase("selectTree")
						|| action.equalsIgnoreCase("selectTreeNode")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objTree.selectTreeNode(objectID, parsedElementValue,
							elementName);
				} else if (action
						.equalsIgnoreCase("selectContextMenuTreeRightClick")
						|| action.equalsIgnoreCase("selectTreeNodeContextMenu")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + elementName + "]");
					objTree.selectTreeNodeContextMenu(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("rightClickTree")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objTree.rightclickTree(objectID, parsedElementValue,
							elementName);

				} else if (action.equalsIgnoreCase("selectedTreeNode")
						|| action.equalsIgnoreCase("getSelectedTreeNode")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objTree.getSelectedTreeNode(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("getTreeChildNodeCount")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");

					result = Integer.toString(objTree
							.getTreeChildNodeCount(elementName));

				} else if (action.equalsIgnoreCase("getTreeChildNodeName")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");

					result = (objTree.getTreeChildNodeName(elementName));

				}

				// Wait fixtures...
				//
				else if (action.equalsIgnoreCase("wait")) {

					LOGGER.trace("Command [" + action + "], value ["
							+ parsedElementValue + "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {

						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());

					}
					objWait.wait(value);
				} else if (action.equalsIgnoreCase("waitForElementValue")) {

					LOGGER.trace("Command [" + elementName + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {
						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}

				} else if (action.equalsIgnoreCase("waitForElementPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {
						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}

					objWait.waitForElementPresent(objectID,
							Integer.parseInt(value), elementName);
				} else if (action.equalsIgnoreCase("waitForElementNotPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {
						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}

					objWait.waitForElementNotPresent(objectID,
							Integer.parseInt(value), elementName);
				} else if (action
						.equalsIgnoreCase("waitTillElementIsNotVisible")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {
						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}

					objWait.waitTillElementIsNotVisible(objectID,
							Integer.parseInt(value), elementName);
				} else if (action.equalsIgnoreCase("isElementPresent")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + elementName + "]");
					try {
						result = objWait.waitForElementPresent(objectID, 0,
								elementName) + "";
					} catch (Exception e) {
						result = false + "";
					}
				} else if (action.equalsIgnoreCase("waitForElementEnabled")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					String value = parsedElementValue;
					if (value.equals("") || value == null) {
						value = Integer.toString(UIFixtureUtils.getInstance()
								.getElementWaitTime());
					}

					objWait.waitForElementEnabled(objectID,
							Integer.parseInt(value), elementName);
				}
				// Verify fixtures...
				//
				else if (action.equalsIgnoreCase("verifyText")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifyText(objectID,
							elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();

				} else if (action.equalsIgnoreCase("verifyTableCheckboxState")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifyTableCheckboxState(
							objectID, elementName, parsedElementValue);
					result = Boolean.valueOf(bResult).toString();
				} else if (action.equalsIgnoreCase("verifySelectOptions")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean bResult = objValidate.verifySelectedOption(
							objectID, elementName, parsedElementValue);

					result = Boolean.valueOf(bResult).toString();

				} else if (action.equalsIgnoreCase("getselectedValue")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					result = objUICommand.getSelectedValue(objectID,
							parsedElementValue, elementName);
				} else if (action.equalsIgnoreCase("verifyState")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					boolean stateValue = objValidate.verifyState(objectID,
							elementName, parsedElementValue);

					result = Boolean.valueOf(stateValue).toString();
				}

				// Frame fixtures
				else if (action.equalsIgnoreCase("selectFrame")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objfrm.setFrame(objectID, elementName);
				} else if (action.equalsIgnoreCase("selectDefaultFrame")) {
					LOGGER.trace("Command [" + action + "]");
					objfrm.setDefaultFrame();
				}

				// window fixtures..
				else if (action.equalsIgnoreCase("activateWindow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");

					objwin.activateWindow(objectID, elementName);
				} else if (action.equalsIgnoreCase("maximizewindow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objwin.maximizeWindow(objectID, elementName);
				} else if (action.equalsIgnoreCase("minimizeWindow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objwin.minimizeWindow(objectID, elementName);

				} else if (action.equalsIgnoreCase("closeWindow")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "], value [" + parsedElementValue
							+ "]");
					objwin.closeWindow(objectID, elementName);
				}

				else if (action.equalsIgnoreCase("getchildWindowTitle")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objwin.getchildWindowTitle(objectID, elementName);

				} else if (action.equalsIgnoreCase("getActiveWindowTitle")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objwin.getActiveWindowTitle();
				} else if (action.equalsIgnoreCase("getLastWindowTitle")) {
					LOGGER.trace("Command [" + action + "], object ["
							+ objectID + "]");
					result = objwin.getLastWindowTitle();
				}
				/* *
				 * // Keyboard and mouse fixtures
				 */
				else if (action.equalsIgnoreCase("keyPress")) {
					LOGGER.trace("Command [" + action + "], value ["
							+ parsedElementValue + "]");
					objKbdMouseEvents.keyPress(objectID, parsedElementValue);
				} else if (action.equalsIgnoreCase("keyboardType")) {
					LOGGER.trace("Command [" + action + "], value ["
							+ parsedElementValue + "]");
					objKbdMouseEvents.typeKeys(objectID, parsedElementValue);
				} else if (action.equalsIgnoreCase("keyRelease")) {
					LOGGER.trace("Command [" + action + "], value ["
							+ parsedElementValue + "]");
					objKbdMouseEvents.keyRelease(objectID, parsedElementValue);
				} else if (action.equalsIgnoreCase("mouse")) {
					LOGGER.trace("Command [" + action + "], mouse action:"
							+ elementName + " value " + parsedElementValue
							+ "]");
					objKbdMouseEvents.mouseEvent(elementName,
							parsedElementValue);
				}

			}
		} catch (Exception e) {
			LOGGER.error("Exception:", e);
			throw new AFTException(e);
		} finally {
			lastAction = action;
		}
		return result;
	}

	/**
	 * Function to run Annotation ElementWaitTime.
	 * 
	 * @param annotationname
	 *            the annotation name
	 * @param annotationValue
	 *            the annotation value
	 * @throws AFTException
	 *             the application exception
	 */
	public void callToAnnotationElementWaitTime(String annotationname,
			String annotationValue) throws AFTException {

		LOGGER.info("Executing [" + annotationname
				+ "] annotation with value [" + annotationValue + "]");
		String value = annotationValue;
		int minValue = Integer
				.parseInt(FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MIN);
		int maxValue = Integer
				.parseInt(FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MAX);

		// Validate that the entered value is valid. If not assign config
		// value to property
		try {
			if (Integer.parseInt(value) < minValue
					|| Integer.parseInt(value) > maxValue) {

				LOGGER.warn("Invalid value ["
						+ value
						+ "] specified for annotation ["
						+ annotationname
						+ "]. Config value ["
						+ FrankensteinDriverConfigProperties
								.getInstance()
								.getConfigProperty(
										FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MS)
						+ "] is assigned.");

				value = FrankensteinDriverConfigProperties
						.getInstance()
						.getConfigProperty(
								FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MS);
			}
		} catch (Exception e) {
			LOGGER.info("Setting the element wait time to default value");
			value = FrankensteinDriverConfigProperties.DEFAULT_ELEMENT_WAIT_TIME;
			LOGGER.info("Element wait time has been set to [" + value);
		}

		FrankensteinDriverConfigProperties.getInstance().setConfigProperty(
				FrankensteinDriverConfigProperties.ELEMENT_WAIT_TIME_MS, value);
	}
}