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
 * Class: PageErrors
 * 
 * Purpose: This class Handles the XML nodes and attributes for creating and
 * parsing XML Objects for Page and Link Errors
 */

package com.ags.aft.Reporting;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.XMLParser;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;
import com.ags.aft.util.Helper;

import org.apache.log4j.Logger;

/**
 * The Class PageErrors.
 */
public class PageErrors {
	private static final Logger LOGGER = Logger
			.getLogger(ReportGenerator.class);

	/** The Constant LOGGER. */
	/** The file path. */
	private String filePath;

	/** The link errors. */
	private String linkErrors;

	/** Test set Names related to Spell Errors **/
	private List<String> spellErrorsTestSetNames = new ArrayList<String>();

	/** Test Scenario ID's related to the Spell Errors **/
	private List<String> spellErrorsTestScenarioId = new ArrayList<String>();

	/** Spell Errors List **/
	private List<Set<String>> spellErrors = new ArrayList<Set<String>>();

	/** Grammar Errors List **/
	private List<Set<String>> grammarErrors = new ArrayList<Set<String>>();

	/** Spell Errors URL information **/
	private List<String> spellErrorsURL = new ArrayList<String>();

	/** Test set Names related to Link Errors **/
	private List<String> linkErrorsTestSetNames = new ArrayList<String>();

	/** Test set Names related to Link Errors **//*
	private List<String> linkErrorsTestSets = new ArrayList<String>();*/

	/** Test Scenario ID's related to the Link Errors **/
	private List<String> linkErrorsTestScenarioId = new ArrayList<String>();

	/** Broken Link Errors List **/
	private List<Map<String, String>> linkBrokenErrors = new ArrayList<Map<String, String>>();

	/** Link Errors URL information **/
	private List<String> linkErrorsURL = new ArrayList<String>();
	
	/** Report Test Suite Id's **/
	private List<Integer> reportTestSuiteId = new ArrayList<Integer>();
	
	private int idProject;

	/**
	 * @return the idProject
	 */
	public int getIdProject() {
		return idProject;
	}

	/**
	 * @param idProject
	 *            the idProject to set
	 */
	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}

	/**
	 * Gets the linkBrokenErrors
	 * 
	 * @return the linkBrokenErrors
	 */
	public List<Map<String, String>> getLinkBrokenErrors() {
		return linkBrokenErrors;
	}

	/**
	 * Sets the linkBrokenErrors.
	 * 
	 * @param linkBrokenErrors
	 *            the linkBrokenErrors
	 */
	public void setLinkBrokenErrors(Map<String, String> linkBrokenErrors) {
		this.linkBrokenErrors.add(linkBrokenErrors);
	}

	/**
	 * Gets the linkErrorsTestSetNames.
	 * 
	 * @return the linkErrorsTestSetNames
	 */
	public List<String> getLinkErrorsTestSetNames() {
		return linkErrorsTestSetNames;
	}

	/**
	 * Sets the linkErrorsTestSetNames.
	 * 
	 * @param linkErrorsTestSetNames
	 *            the linkErrorsTestSetNames
	 */
	public void setLinkErrorsTestSetNames(String linkErrorsTestSetNames) {
		this.linkErrorsTestSetNames.add(linkErrorsTestSetNames);
	}

/*	*//**
	 * @return the linkErrorsTestSets
	 *//*
	public List<String> getLinkErrorsTestSets() {
		return linkErrorsTestSets;
	}

	*//**
	 * @param linkErrorsTestSets
	 *            the linkErrorsTestSets to set
	 *//*
	public void setLinkErrorsTestSets(String linkErrorsTestSets) {
		this.linkErrorsTestSets.add(linkErrorsTestSets);
	}*/

	/**
	 * Gets the linkErrorsTestScenarioId.
	 * 
	 * @return the linkErrorsTestScenarioId
	 */
	public List<String> getLinkErrorsTestScenarioId() {
		return linkErrorsTestScenarioId;
	}

	/**
	 * Sets the linkErrorsTestScenarioId.
	 * 
	 * @param linkErrorsTestScenarioId
	 *            the linkErrorsTestScenarioId
	 */
	public void setLinkErrorsTestScenarioId(String linkErrorsTestScenarioId) {
		this.linkErrorsTestScenarioId.add(linkErrorsTestScenarioId);
	}
	/**
	 * @return the reportTestSuiteId
	 */
	public List<Integer> getReportTestSuiteId() {
		return reportTestSuiteId;
	}

	/**
	 * @param reportTestSuiteId the reportTestSuiteId to set
	 */
	public void setReportTestSuiteId(Integer reportTestSuiteId) {
		this.reportTestSuiteId.add(reportTestSuiteId);
	}

	/**
	 * Gets the linkErrorsURL.
	 * 
	 * @return the linkErrorsURL
	 */
	public List<String> getLinkErrorsURL() {
		return linkErrorsURL;
	}

	/**
	 * Sets the linkErrorsURL.
	 * 
	 * @param linkErrorsURL
	 *            the linkErrorsURL
	 */
	public void setLinkErrorsURL(String linkErrorsURL) {
		this.linkErrorsURL.add(linkErrorsURL);
	}

	/** The page response list. */
	// private List<Map<String, Map<String, String>>> pageResponseList = new
	// ArrayList<Map<String, Map<String, String>>>();

	/** TestSet name & Business Scenario ID for the LinkErrors **/
	// private Map<String,String> linkErrorsScenarios=new
	// HashMap<String,String>();

	/** The page error. */
	private static PageErrors pageError = null;

	/**
	 * Gets the spellErrorsTestSetNames.
	 * 
	 * @return the spellErrorsTestSetNames
	 */
	public List<String> getSpellErrorsTestSetNames() {
		return spellErrorsTestSetNames;
	}

	/**
	 * Sets the spellErrorsTestSetName.
	 * 
	 * @param spellErrorsTestSetName
	 *            the spellErrorsTestSetName
	 */
	public void setSpellErrorsTestSetNames(String spellErrorsTestSetName) {
		this.spellErrorsTestSetNames.add(spellErrorsTestSetName);
	}

	/**
	 * Gets the spellErrorsTestScenarioId.
	 * 
	 * @return the spellErrorsTestScenarioId
	 */
	public List<String> getSpellErrorsTestScenarioId() {
		return spellErrorsTestScenarioId;
	}

	/**
	 * Sets the spellErrorsTestScenarioId.
	 * 
	 * @param spellErrorsTestScenarioId
	 *            the spellErrorsTestScenarioId
	 */
	public void setSpellErrorsTestScenarioId(String spellErrorsTestScenarioId) {
		this.spellErrorsTestScenarioId.add(spellErrorsTestScenarioId);
	}

	/**
	 * Gets the spellErrors.
	 * 
	 * @return the spellErrors
	 */
	public List<Set<String>> getSpellErrors() {
		return spellErrors;
	}

	/**
	 * Sets the spellErrors.
	 * 
	 * @param spellErrors
	 *            the spellErrors
	 */
	public void setSpellErrors(Set<String> spellErrors) {
		this.spellErrors.add(spellErrors);
	}

	/**
	 * Gets the grammarErrors.
	 * 
	 * @return the grammarErrors
	 */
	public List<Set<String>> getGrammarErrors() {
		return grammarErrors;
	}

	/**
	 * Sets the grammarErrors.
	 * 
	 * @param grammarErrors
	 *            the grammarErrors
	 */
	public void setGrammarErrors(Set<String> grammarErrors) {
		this.grammarErrors.add(grammarErrors);
	}

	/**
	 * Gets the spellErrorsURL.
	 * 
	 * @return the spellErrorsURL
	 */
	public List<String> getspellErrorsURL() {
		return spellErrorsURL;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the url
	 */
	public void setspellErrorsURL(String url) {
		this.spellErrorsURL.add(url);
	}

	/**
	 * Gets the link errors.
	 * 
	 * @return the link errors
	 */
	public String getLinkErrors() {
		return linkErrors;
	}

	/**
	 * Sets the link errors.
	 * 
	 * @param linkErrors
	 *            the new link errors
	 */
	public void setLinkErrors(String linkErrors) {
		this.linkErrors = linkErrors;
	}

	/**
	 * Sets the file path.
	 * 
	 * @param filePath
	 *            the new file path
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Gets the file path.
	 * 
	 * @return the file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Gets the single instance of PageErrors.
	 * 
	 * @return single instance of PageErrors
	 */
	public static PageErrors getInstance() {
		if (pageError == null) {
			pageError = new PageErrors();
			return pageError;
		}
		return pageError;
	}

	/**
	 * Construct file name.
	 * 
	 * @param errorType
	 *            the error type
	 * @return the string
	 * @throws AFTException
	 */
	public String constructFileName(String errorType) throws AFTException {
		String fileName = "";
		try {
			// Create file Name
			File resultFile = new File(getFilePath());
			fileName = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.TESTRESULT_PATH)
					+ "\\" + errorType + "_" + resultFile.getName() + ".xml";
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return fileName;

	}

	/**
	 * To generate the spell errors report.
	 * 
	 * @throws AFTException
	 */
	public void generateSpellErrors() throws AFTException {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		try {
			if (isFileSystemRequest) {
				generateSpellErrorXML();
			} else {
				storeSpellErrors();
			}
		} catch (Exception e) {
			LOGGER.error("Exception e:" + e);
			throw new AFTException(e);
		}
	}

	/**
	 * To generate the spell errors report.
	 * 
	 * @throws AFTException
	 */
	public void generateSpellErrorXML() throws AFTException {
		String rootNodeName = "Errors";
		String testSetNodeName = "TestSet";
		String testScenarioNodeName = "TestScenario";
		String errorPageNodeName = "ErrorPage";
		String spellErrorsNodeName = "SpellErrors";
		String grammarErrorsNodeName = "GrammarErrors";
		String nameAttr = "name";
		String idAttr = "id";
		String pageAttr = "page";

		if (!getSpellErrors().isEmpty() || !getGrammarErrors().isEmpty()) {
			String xmlFileName = constructFileName("SpellErrors");
			XMLParser xmlParser = new XMLParser(xmlFileName);

			// Create document object
			Document doc = xmlParser.createDocument();

			// Create Root Element
			Element rootNode = xmlParser.createRootElement(rootNodeName);

			for (int testSetCount = 0; testSetCount < getSpellErrorsTestSetNames()
					.size(); testSetCount++) {

				// Spell Errors and grammer errors
				Set<String> spellError = getSpellErrors().get(testSetCount);
				Set<String> grammarError = getGrammarErrors().get(testSetCount);

				if (spellError.isEmpty() && grammarError.isEmpty()) {
					continue;
				}

				// Creating the Test Set node
				Element testSetNode = getRequiredNode(doc, rootNode,
						testSetNodeName, nameAttr, getSpellErrorsTestSetNames()
								.get(testSetCount));

				// Creating the Test Scenario Node
				Element testScenarioNode = getRequiredNode(doc, testSetNode,
						testScenarioNodeName, idAttr,
						getSpellErrorsTestScenarioId().get(testSetCount));

				// Creating the ErrorPage node
				Element errorPageNode = getRequiredNode(doc, testScenarioNode,
						errorPageNodeName, pageAttr,
						getspellErrorsURL().get(testSetCount));

				// Create Spell Errors node

				if (!spellError.isEmpty()) {
					int i = 1;
					Element spellErrorsNode;
					if (verifyChildNode(errorPageNode, spellErrorsNodeName)) {
						spellErrorsNode = (Element) errorPageNode
								.getElementsByTagName(spellErrorsNodeName);
						i = errorPageNode.getElementsByTagName(
								spellErrorsNodeName).getLength();
					} else {
						spellErrorsNode = doc
								.createElement(spellErrorsNodeName);
						errorPageNode.appendChild(spellErrorsNode);
					}

					// Iterate through spell error nodes
					Iterator<String> spellErrorIterator = spellError.iterator();
					while (spellErrorIterator.hasNext()) {
						Node errorNode = doc.createElement("error" + i);
						errorNode.appendChild(doc
								.createCDATASection(spellErrorIterator.next()));
						spellErrorsNode.appendChild(errorNode);
						i++;
					}
				}

				// Create Grammar Errors node
				if (!grammarError.isEmpty()) {
					int i = 1;
					Element grammarErrorsNode;
					if (verifyChildNode(errorPageNode, grammarErrorsNodeName)) {
						grammarErrorsNode = (Element) errorPageNode
								.getElementsByTagName(grammarErrorsNodeName);
						i = errorPageNode.getElementsByTagName(
								grammarErrorsNodeName).getLength();
					} else {
						grammarErrorsNode = doc
								.createElement(grammarErrorsNodeName);
						errorPageNode.appendChild(grammarErrorsNode);
					}

					Iterator<String> grammarErrorIterator = grammarError
							.iterator();
					// Iterate through Grammer error nodes
					while (grammarErrorIterator.hasNext()) {
						Node errorNode = doc.createElement("error" + i);
						errorNode
								.appendChild(doc
										.createCDATASection(grammarErrorIterator
												.next()));
						grammarErrorsNode.appendChild(errorNode);
						i++;
					}
				}

			}
			// Create the final xml
			xmlParser.generateXML();
		}
	}

	/**
	 * To generate the spell errors report.
	 * 
	 * @throws AFTException
	 */
	public void storeSpellErrors() throws AFTException {
		if (!getSpellErrors().isEmpty() || !getGrammarErrors().isEmpty()) {
			for (int testSetCount = 0; testSetCount < getSpellErrorsTestSetNames()
					.size(); testSetCount++) {
				int spellErrorCount = 0;
				int grammarErrorCount = 0;
				// Insert spell errors data
				int spellErrorId = DatabaseUtil.getInstance()
						.insertSpellErrorsData(
								getIdProject(),
								getSpellErrorsTestSetNames().get(testSetCount),
								getSpellErrorsTestScenarioId()
										.get(testSetCount),
								ReportGenerator.getInstance().getStartDate(),
								ReportGenerator.getInstance().getEndDate(),
								getspellErrorsURL().get(testSetCount),
								spellErrorCount, grammarErrorCount,
								getReportTestSuiteId().get(testSetCount));
				
				// Spell Errors and grammer errors
				Set<String> spellError = getSpellErrors().get(testSetCount);
				Set<String> grammarError = getGrammarErrors().get(testSetCount);

				if (spellError.isEmpty() && grammarError.isEmpty()) {
					continue;
				}

				// Store Spell Errors
				
				if (!spellError.isEmpty()) {
					String errorType = "Spelling";
					// Iterate through spell error nodes
					Iterator<String> spellErrorIterator = spellError.iterator();
					while (spellErrorIterator.hasNext()) {
						String errorDetails = spellErrorIterator.next();
						// insert spell error details data.
						DatabaseUtil.getInstance().insertSpellErrorsDetails(
								errorType, errorDetails, spellErrorId);
						spellErrorCount++;
					}
				}
				
				// Store Grammar Errors
				if (!grammarError.isEmpty()) {
					String errorType = "Grammar";
					Iterator<String> grammarErrorIterator = grammarError
							.iterator();
					// Iterate through Grammer error nodes
					while (grammarErrorIterator.hasNext()) {
						String errorDetails = grammarErrorIterator.next();
						// insert spell error details data.
						DatabaseUtil.getInstance().insertSpellErrorsDetails(
								errorType, errorDetails, spellErrorId);
						grammarErrorCount++;
					}
				}
				DatabaseUtil.getInstance().updateSpellErrorsData(spellErrorId,
						spellErrorCount, grammarErrorCount);
			}
		}
	}

	/**
	 * To generate the link errors report.
	 * 
	 * @throws AFTException
	 */
	public void generatePageErrors() throws AFTException {
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();
		try {
			if (isFileSystemRequest) {
				generatePageErrorXML();
			} else {
				storeLinkErrors();
			}
		} catch (Exception e) {
			LOGGER.error("Exception e:" + e);
			throw new AFTException(e);
		}
	}

	/**
	 * To generate the spell errors report.
	 * 
	 * @throws AFTException
	 */
	public void generatePageErrorXML() throws AFTException {
		String rootNodeName = "Errors";
		String testSetNodeName = "TestSet";
		String testScenarioNodeName = "TestScenario";
		String errorPageNodeName = "ErrorPage";
		String nameAttr = "name";
		String idAttr = "id";
		String pageAttr = "page";

		if (!getLinkBrokenErrors().isEmpty()) {
			String xmlFileName = constructFileName("LinkErrors");
			XMLParser xmlParser = new XMLParser(xmlFileName);

			// Create document object
			Document doc = xmlParser.createDocument();

			// Create Root Element
			Element rootNode = xmlParser.createRootElement(rootNodeName);

			for (int testSetCount = 0; testSetCount < getLinkErrorsTestSetNames()
					.size(); testSetCount++) {

				// Spell Errors and grammar errors
				Map<String, String> brokenLinkError = getLinkBrokenErrors()
						.get(testSetCount);

				if (brokenLinkError.isEmpty()) {
					continue;
				}

				// Creating the Test Set node
				Element testSetNode = getRequiredNode(doc, rootNode,
						testSetNodeName, nameAttr, getLinkErrorsTestSetNames()
								.get(testSetCount));

				// Creating the Test Scenario Node
				Element testScenarioNode = getRequiredNode(doc, testSetNode,
						testScenarioNodeName, idAttr,
						getLinkErrorsTestScenarioId().get(testSetCount));

				// Creating the ErrorPage node
				Element errorPageNode = getRequiredNode(doc, testScenarioNode,
						errorPageNodeName, pageAttr,
						getLinkErrorsURL().get(testSetCount));

				// Create Spell Errors node

				if (!brokenLinkError.isEmpty()) {
					Element linkErrorsNode = null;
					String errorNodeName = null;
					String errorMesssage = "";
					Set<String> brokenLinks = brokenLinkError.keySet();
					Iterator<String> brokenLinksIterator = brokenLinks
							.iterator();
					// Iterate through broken links list
					while (brokenLinksIterator.hasNext()) {
						String linkErrorNode = brokenLinksIterator.next();
						// get the error response
						String response = brokenLinkError.get(linkErrorNode);
						if (response.toLowerCase().startsWith("http")) {
							errorNodeName = "Error"
									+ response.substring(8, 12).trim();
						} else if (response.toLowerCase().startsWith(
								"link failed")) {
							errorNodeName = "Exceptions";
							String[] responses = response.split(",");
							for (int i = 1; i < responses.length; i++) {
								errorMesssage = errorMesssage + responses[i];
							}

						} else if (!response.equalsIgnoreCase(
								"Empty Link")) {
							errorNodeName = "Miscelleneous";
						}
						if (errorNodeName != null) {
							if (verifyChildNode(errorPageNode, errorNodeName)) {
								NodeList nodes = errorPageNode.getChildNodes();
								for (int i = 0; i < nodes.getLength(); i++) {
									if (nodes.item(i).getNodeName()
											.equals(errorNodeName)) {
										linkErrorsNode = (Element) nodes
												.item(i);
										break;
									}
								}
							} else {
								linkErrorsNode = doc
										.createElement(errorNodeName);
								errorPageNode.appendChild(linkErrorsNode);
							}
							if (linkErrorsNode != null) {
								// if Node is of type Exception,append the Error
								// message
								if (errorNodeName
										.equalsIgnoreCase("Exceptions")) {
									linkErrorNode = linkErrorNode + "||"
											+ errorMesssage.trim();
								}
								// Create Cdata node
								linkErrorsNode.appendChild(doc
										.createCDATASection(linkErrorNode));

							}
						}
					}
				}
			}
			// Create the final xml
			xmlParser.generateXML();
		}
	}

	/**
	 * To store the link errors report.
	 * 
	 * @throws AFTException
	 */
	public void storeLinkErrors() throws AFTException {
		if (!getLinkBrokenErrors().isEmpty()) {

			for (int testSetCount = 0; testSetCount < getLinkErrorsTestSetNames()
					.size(); testSetCount++) {
				int exceptionCount = 0;
				int error404Count = 0;
				int error500Count = 0;
				// Insert link errors data
				int linkErrorId = DatabaseUtil
						.getInstance()
						.insertLinkErrorsData(
								getIdProject(),
								getLinkErrorsTestSetNames().get(testSetCount),
								getLinkErrorsTestScenarioId().get(testSetCount),
								ReportGenerator.getInstance().getStartDate(),
								ReportGenerator.getInstance().getEndDate(),
								getLinkErrorsURL().get(testSetCount),
								exceptionCount, error404Count, error500Count,
								getReportTestSuiteId().get(testSetCount));
				
				Map<String, String> brokenLinkError = getLinkBrokenErrors()
						.get(testSetCount);
				if (!brokenLinkError.isEmpty()) {
				//	for (Map<String, String> brokenLinkError : getLinkBrokenErrors()) {
						String errorMesssage = "";
						String errorType = "";
						String errorUrl = "";
						Set<String> brokenLinks = brokenLinkError.keySet();
						Iterator<String> brokenLinksIterator = brokenLinks
								.iterator();
						// Iterate through broken links list
					while (brokenLinksIterator.hasNext()) {
						errorUrl = brokenLinksIterator.next();
						// get the error response
						String response = brokenLinkError.get(errorUrl);
						if (response.toLowerCase().startsWith("http")) {
							errorType = response.substring(8, 12).trim();
							if (errorType.equalsIgnoreCase("404")) {
								error404Count++;
								errorMesssage = "Page not Found";
							} else if (errorType.equalsIgnoreCase("500")) {
								errorMesssage = "Internal Server Error";
								error500Count++;
							}
						} else if (response.toLowerCase().startsWith(
								"link failed")) {
							errorType = "Exceptions";
							String[] responses = response.split(",");
							exceptionCount++;
							for (int i = 1; i < responses.length; i++) {
								errorMesssage = errorMesssage + responses[i];
							}

						}
						// insert link error details data.
						DatabaseUtil.getInstance()
								.insertLinkErrorsDetails(errorType, errorUrl,
										errorMesssage, linkErrorId);
					}
					//}
				}
				// update link errors data
				DatabaseUtil.getInstance().updateLinkErrorsData(linkErrorId,
						exceptionCount, error404Count, error500Count);
			}
		}
	}

	/**
	 * Verify child node exists in the xml or not.
	 * 
	 * @param element
	 *            : The element object
	 * @param requiredNodeName
	 *            : child node in the element node
	 * @return true, if child node exists in the main node
	 */
	private boolean verifyChildNode(Element element, String requiredNodeName) {
		if (element.hasChildNodes()) {
			NodeList nodes = element.getChildNodes();

			// Iterate through all child nodes and check if the child node
			// exists
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName()
						.equalsIgnoreCase(requiredNodeName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the required node from the document
	 * 
	 * @param doc
	 *            , the document object
	 * @param mainNode
	 *            , the main node
	 * @param nodeName
	 *            , the required node name
	 * @param attr
	 *            , the attribute
	 * @param value
	 *            , the attribute value
	 * @return Element, the required ndoe.
	 */
	private Element getRequiredNode(Document doc, Element mainNode,
			String nodeName, String attr, String value) {
		Element testSetNode = getNode(mainNode, attr, value);
		if (testSetNode == null) {
			// Create and append the node to the main node
			testSetNode = doc.createElement(nodeName);
			testSetNode.setAttribute(attr, value);
			mainNode.appendChild(testSetNode);
		}
		return testSetNode;
	}

	/**
	 * Get the required node from the document
	 * 
	 * @param parentNode
	 *            , the parent node
	 * @param attr
	 *            , the attribute
	 * @param attrValue
	 *            , the attribute value
	 * @return Element, the required node object
	 */
	@SuppressWarnings("null")
	private Element getNode(Element parentNode, String attr, String attrValue) {
		Element requiredNode = null;
		// Get all children under parent nodes
		NodeList nodes = parentNode.getChildNodes();
		// Iterate through parent nodes and return required node
		for (int count = 0; count < nodes.getLength(); count++) {
			Node node = nodes.item(count);
			if (attr != null) {
				if (node.getAttributes().getNamedItem(attr).getNodeValue()
						.equals(attrValue)) {
					requiredNode = (Element) node;
					break;
				}
			} else {
				if (requiredNode.getNodeValue().equals(attrValue)) {
					requiredNode = (Element) node;
					break;
				}
			}
		}
		return requiredNode;
	}

	/**
	 * Generate final xml.
	 * 
	 * @throws AFTException
	 * 
	 */
	public void generateFinalXml() throws AFTException {
		try {
			String rootNode = "Errors";
			String spellErrorNode = "SpellError";
			String linkErrorNode = "LinkError";
			String fileName = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.TESTRESULT_PATH)
					+ "/" + "ErrorResults.xml";
			List<String> spellNodesNames = new ArrayList<String>();
			// Create document object
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = null;

			Element rootElement = null;
			File resultFile = new File(fileName);
			if (resultFile.exists()) {
				doc = docBuilder.parse(resultFile);
				rootElement = doc.getDocumentElement();
			} else {
				doc = docBuilder.newDocument();
				rootElement = doc.createElement(rootNode);
				doc.appendChild(rootElement);
			}

			// Adding nodes
			// If spell error file exists
			if (new File(constructFileName("SpellErrors")).exists()) {
				// Create Node 'Spell Errors'
				Element spellNode = createNode(doc, rootElement,
						spellErrorNode, constructFileName("SpellErrors"));
				Document spellDoc = getDoc(constructFileName("SpellErrors"));
				Element spellRootNode = spellDoc.getDocumentElement();
				NodeList nodes = spellRootNode
						.getElementsByTagName("ErrorPage");

				// Iterate through error page nodes and append the child nodes
				for (int i = 0; i < nodes.getLength(); i++) {
					Node nodeName = nodes.item(i);
					if (nodeName.getNodeName().equalsIgnoreCase("ErrorPage")) {
						NodeList nodes1 = nodes.item(i).getChildNodes();
						for (int nodeChild = 0; nodeChild < nodes1.getLength(); nodeChild++) {
							if (!spellNodesNames.contains(nodes1
									.item(nodeChild).getNodeName())) {
								spellNodesNames.add(nodes1.item(nodeChild)
										.getNodeName());
								Element spellCount = doc.createElement(nodes1
										.item(nodeChild).getNodeName());
								spellCount.setTextContent(getNodeCount(
										spellDoc, nodes1.item(nodeChild)
												.getNodeName())
										+ "");
								spellNode.appendChild(spellCount);
							}
						}
					}
				}
			}
			// If file link error exists
			if (new File(constructFileName("LinkErrors")).exists()) {
				// Create a node 'link errors'
				Element linkNode = createNode(doc, rootElement, linkErrorNode,
						constructFileName("LinkErrors"));
				Document linkDoc = getDoc(constructFileName("LinkErrors"));
				Element linkRootNode = linkDoc.getDocumentElement();
				NodeList linkNodes = linkRootNode
						.getElementsByTagName("ErrorPage");

				// Iterate through error page nodes and append the child nodes
				for (int i = 0; i < linkNodes.getLength(); i++) {
					Node nodeName = linkNodes.item(i);
					if (nodeName.getNodeName().equalsIgnoreCase("Errorpage")) {
						NodeList nodes = linkNodes.item(i).getChildNodes();
						for (int nodeChild = 0; nodeChild < nodes.getLength(); nodeChild++) {
							if (!spellNodesNames.contains(nodes.item(nodeChild)
									.getNodeName())) {
								spellNodesNames.add(nodes.item(nodeChild)
										.getNodeName());
								Element errorNode = doc.createElement(nodes
										.item(nodeChild).getNodeName());
								errorNode.setTextContent(getNodeCount(linkDoc,
										errorNode.getNodeName()) + "");
								linkNode.appendChild(errorNode);
							}
						}

					}
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(
					new File(fileName).getAbsolutePath());

			transformer.transform(source, result);

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Create the node
	 * 
	 * @param doc
	 *            : The document object
	 * @param rootNode
	 *            : Root Node
	 * @param nodeName
	 *            : Node name to be created
	 * @param filePath
	 *            : attribute value
	 * @return Element, returns the node
	 */
	private Element createNode(Document doc, Element rootNode, String nodeName,
			String filePath) {
		Element node = doc.createElement(nodeName);
		node.setAttribute("file", new File(filePath).getName());
		rootNode.appendChild(node);
		return node;
	}

	/**
	 * Gets the doc.
	 * 
	 * @param filePath
	 *            the file path
	 * @return the doc
	 * @throws AFTException
	 */
	private Document getDoc(String filePath) throws AFTException {
		Document doc = null;
		try {
			File fXmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return doc;
	}

	/**
	 * Gets the link nodes.
	 * 
	 * @param readDOC
	 *            the read doc
	 * @param nodes
	 *            the nodes
	 * @param requiredDoc
	 *            the required doc
	 * @param appendNodeName
	 *            the append node name
	 */
	/*
	 * public void getLinkNodes(Document readDOC, NodeList nodes, Document
	 * requiredDoc, Element appendNodeName) { for (int linkCount = 0; linkCount
	 * < nodes.getLength(); linkCount++) { Node requiredNode =
	 * nodes.item(linkCount); NodeList childNodes =
	 * requiredNode.getChildNodes();
	 * 
	 * for (int childCount = 0; childCount < childNodes.getLength();
	 * childCount++) { Node errorNode = childNodes.item(childCount); if
	 * (!errorNode.getNodeName().contains("text")) { Node appendNode =
	 * requiredDoc.createElement(errorNode .getNodeName());
	 * appendNode.setTextContent(getNodeCount(readDOC, errorNode.getNodeName())
	 * + ""); appendNodeName.appendChild(appendNode); } } } }
	 */

	/**
	 * Get the node count of a particular node.
	 * 
	 * @param doc
	 *            the doc
	 * @param nodeName
	 *            the node name
	 * @return int value, node count
	 */
	private int getNodeCount(Document doc, String nodeName) {
		int count = 0;
		NodeList nodes = doc.getElementsByTagName(nodeName);
		// Return the node count based on the passed node name
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			count += node.getChildNodes().getLength();
		}
		return count;
	}
}
