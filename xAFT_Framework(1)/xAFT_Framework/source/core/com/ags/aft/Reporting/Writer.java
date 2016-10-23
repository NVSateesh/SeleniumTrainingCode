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
 * Class: Writer
 * 
 * Purpose: This utility class generate XML document for reporting from
 * TestSuite result.
 */

package com.ags.aft.Reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.Util;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.testObjects.TestSuite;
import com.ags.aft.util.Helper;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utility class to generate XML document from TestSuite result.
 * 
 */
public class Writer {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Writer.class);

	private List<TestSuite> suiteResult;
	private String startTime;
	private String endTime;
	private Document document;
	private DocumentBuilder builder;
	private Document resultListDocument;
	private static String resultListFile = "";
	private String resultFileName = "";

	/**
	 * Creates an instance of XMLWriter.
	 * 
	 * @param suiteResult
	 *            ArrayList<TestSuite> object.
	 * @param startTime
	 *            startTime
	 * @param endTime
	 *            endTime
	 * @throws AFTException
	 */
	public Writer(List<TestSuite> suiteResult, String startTime, String endTime)
			throws AFTException {
		this.suiteResult = suiteResult;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * writes the reporting data to a file
	 * 
	 * @throws AFTException
	 */
	public void writeToFile() throws AFTException {
		try {
			resultListFile = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.TESTRESULT_PATH)
					+ "\\" + Constants.TESTREPORTRESULTFILE;

			LOGGER.trace("Creating new DocumentBuilderFactory object");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			builder = factory.newDocumentBuilder();
			LOGGER.trace("Creating new document object");
			document = builder.newDocument();
			LOGGER.trace("Creating DOM trees for TestSuite and TestCase reporting");
			createDOMTree();
		} catch (ParserConfigurationException pe) {
			LOGGER.error("Exception:: ", pe);
			throw new AFTException(pe);
		} catch (UnknownHostException ue) {
			LOGGER.error("Exception:: ", ue);
			throw new AFTException(ue);
		}
	}

	/**
	 * writes the reporting data to DB
	 * 
	 * @throws AFTException
	 */
	public void writeToDB(boolean isEndOfTestSuite) throws AFTException {
		try {
			DatabaseUtil.getInstance().insertTestExecutionData(suiteResult,
					endTime, isEndOfTestSuite);
		} catch (Exception e) {
			LOGGER.error("Exception:: ", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Refreshes the test result file TestResultList.xml for new test result xml
	 * 
	 * @throws ApplicationException
	 * 
	 */
	private void refreshTestResultXMLFile() throws AFTException {
		LOGGER.trace("Refreshing test result file ["
				+ Constants.TESTREPORTRESULTFILE + "]");

		try {
			File resultFolder = null;
			resultFolder = new File(ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.TESTRESULT_PATH));
			resultListDocument = builder.newDocument();
			Element rootNode = resultListDocument.createElement("ResultList");
			File[] xmlList = resultFolder.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					String f = new File(name).getName();
					return f.contains(".xml")
							&& !f.contains(Constants.TESTREPORTRESULTFILE);
				}
			});

			xmlList = sortFilesByDate(xmlList);

			for (int i = 0; i < xmlList.length; i++) {
				if (!(xmlList[i].getName().startsWith("SpellErrors")
						|| xmlList[i].getName().startsWith("LinkErrors") || xmlList[i]
						.getName().equalsIgnoreCase("ErrorResults.xml"))) {
					Element resultElement = resultListDocument
							.createElement("Result");
					resultElement.setAttribute("file", xmlList[i].getName());
					rootNode.appendChild(resultElement);
					Document doc = builder.parse(resultFolder + "/"
							+ xmlList[i].getName());
					NodeList suiteList = doc.getElementsByTagName("TestSuite");
					createTestSuiteElement(suiteList, resultElement);
				}
			}
			resultListDocument.appendChild(rootNode);
			LOGGER.trace("Updating XML Test Report Result file with new test result xml file details");
			OutputFormat format = new OutputFormat(resultListDocument);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
					resultListFile), format);
			serializer.serialize(resultListDocument);
			LOGGER.trace("Updated XML Test Report Result file with new test result xml file details successfully");
		} catch (IOException io) {
			LOGGER.error("Exception:: ", io);
			throw new AFTException(io);
		} catch (SAXException e) {
			LOGGER.error("Exception:: ", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Workhorse method for creating XML document.
	 * 
	 * @throws UnknownHostException
	 * @throws DOMException
	 * @throws AFTException
	 */
	private void createDOMTree() throws UnknownHostException, AFTException {
		LOGGER.trace("Creating DOM tree for test results");

		Element rootElement = document.createElement("TestResult");
		document.appendChild(rootElement);

		Iterator<TestSuite> suiteIterator = suiteResult.iterator();
		while (suiteIterator.hasNext()) {
			TestSuite suite = (TestSuite) suiteIterator.next();
			String browser = suite.getBrowserName();
			String url = suite.geturl();

			Element suiteElement = createTestSuiteElement(suite);
			suiteElement.setAttribute("browser", browser);
			suiteElement.setAttribute("BaseUrl", url);
			rootElement.appendChild(suiteElement);
		}
		rootElement.appendChild(createEnvInfoDOM());
		LOGGER.trace("Created DOM tree for test results successfully");
	}

	/**
	 * @return envInfoElement
	 * @throws UnknownHostException
	 * @throws AFTException
	 */
	private Element createEnvInfoDOM() throws UnknownHostException,
			AFTException {
		LOGGER.trace("Creating environment information DOM tree element");

		InetAddress addr = InetAddress.getLocalHost();
		String hostname = addr.getHostName();
		String os = System.getProperty("os.name");

		// String testType = "Regression";

		Element envInfoElement = document.createElement("Environment");
		envInfoElement.setAttribute("os", os);
		// envInfoElement.setAttribute("testType", testType);
		envInfoElement.setAttribute("hostname", hostname);
		envInfoElement.setAttribute("startTime", startTime);

		if (endTime != null && !endTime.equals("")) {
			envInfoElement.setAttribute("endTime", endTime);
			String totalExecutionDuration = getTotalExecutionTime();
			envInfoElement.setAttribute("totalExecutionDuration",
					totalExecutionDuration);
		} else {
			envInfoElement.setAttribute("endTime", "Execution Not Completed");
			envInfoElement.setAttribute("totalExecutionDuration",
					"Execution Not Completed");
		}

		// adding log file name and path attributes
		String logFile = Log4JPlugin.getInstance().getLogFile();
		envInfoElement.setAttribute("logFile", logFile);

		LOGGER.trace("Created environment information DOM tree element successfully");

		return envInfoElement;
	}

	/**
	 * Helper method which creates a XML element <TestSuite>
	 * 
	 * @param suite
	 *            TestSuite whose xml representation has to be created.
	 * @return XML element snippet representing a book
	 * @throws AFTException 
	 * @throws DOMException 
	 */
	private Element createTestSuiteElement(TestSuite suite)
			throws DOMException, AFTException {
		LOGGER.trace("Creating DOM tree element for test suite ["
				+ suite.getTestSuiteName() + "]");

		Element suiteElement = document.createElement("TestSuite");
		suiteElement.setAttribute("Name", suite.getTestSuiteName());
		// Category attribute added.
		suiteElement.setAttribute("Category", suite.getCategory());

		// ExecutionEngine attribute added.
		suiteElement
				.setAttribute("ExecutionEngine", suite.getExecutionEngine());

		List<TestScenario> testcases = suite.getTestScenariosArr();
		Iterator<TestScenario> testsIterator = testcases.iterator();

		long totalScenariosExecutionTime = 0;
		while (testsIterator.hasNext()) {
			TestScenario testScenario = (TestScenario) testsIterator.next();
			Element testElement = document.createElement("Test");
			testElement
					.setAttribute("id", testScenario.getBusinessScenarioId());
			testElement.setAttribute("disableStandardReporting", new Boolean(
					ConfigProperties.getInstance()
							.isStandardReportingDisabled()).toString());
			/*
			 * testElement.setAttribute("executionTime",
			 * formatTime(testScenario.getExecutionTime()));
			 */
			long totalExecutionTime = 0;
			List<TestCase> testCaseList = testScenario.getTestCaseDetails();
			if (testCaseList != null && testCaseList.size() > 0) {
				Iterator<TestCase> testCaseIterator = testCaseList.iterator();
				while (testCaseIterator.hasNext()) {
					TestCase testCase = testCaseIterator.next();
					totalExecutionTime = totalExecutionTime
							+ testCase.getTestCaseExecutionTime();
				}
			}
			testScenario.setExecutionTime(totalExecutionTime);
			totalScenariosExecutionTime = totalScenariosExecutionTime
					+ totalExecutionTime;
			testElement.setAttribute("executionTime", Util.getInstance()
					.formatTimeMilliSec(totalExecutionTime));
			Element descElement = document.createElement("Description");
			Text description = document.createTextNode(testScenario
					.getBusinessScenarioDesc());
			descElement.appendChild(description);
			testElement.appendChild(descElement);

			Element resultElement = document.createElement("Result");
			Text result = document.createTextNode(testScenario
					.getExecutionResult());
			resultElement.appendChild(result);
			testElement.appendChild(resultElement);

			// Test Case Id / Requirement Id Column added
			Element testCaseRequirementId = document
					.createElement("TestCaseRequirementId");
			String testCaseReqmtId = testScenario
					.getTestScenarioRequirementId();
			if (Constants.EMPTYVALUE.equals(testCaseReqmtId)) {
				testCaseReqmtId = "N/A";
			}
			Text testCaseRequirementId1 = document
					.createTextNode(testCaseReqmtId);
			testCaseRequirementId.appendChild(testCaseRequirementId1);
			testElement.appendChild(testCaseRequirementId);

			testElement.appendChild(createTestCaseDetailsElement(testScenario));
			testElement.appendChild(createTestCaseElement(testScenario));
			suiteElement.appendChild(testElement);
		}

		suiteElement.setAttribute("executionTime", Util.getInstance()
				.formatTime(totalScenariosExecutionTime));

		suite.setExecutionTime(totalScenariosExecutionTime);
		LOGGER.trace("Created DOM tree element for test suite ["
				+ suite.getTestSuiteName() + "] successfully");

		return suiteElement;
	}

	/**
	 * @param testScenario
	 *            testScenario
	 * @return stepsElement
	 */
	private Element createTestCaseElement(TestScenario testScenario) {
		LOGGER.trace("Creating DOM tree element for test case ["
				+ testScenario.getBusinessScenarioId() + "]");

		Element stepsElement = document.createElement("Steps");
		Element stepElement = null;
		List<TestCase> testCaseList = testScenario.getTestCaseDetails();
		if (testCaseList != null && testCaseList.size() > 0) {
			Iterator<TestCase> testCaseIterator = testCaseList.iterator();
			while (testCaseIterator.hasNext()) {
				TestCase testCase = testCaseIterator.next();
				List<TestStep> testStepList = testCase.getTestStepDetails();
				if (testStepList != null && testStepList.size() > 0) {
					Iterator<TestStep> testStepIterator = testStepList
							.iterator();
					while (testStepIterator.hasNext()) {
						TestStep testStep = testStepIterator.next();

						stepElement = document.createElement("Step");
						stepElement.setAttribute("id", testStep.getTestCase()
								.getTestCaseId());
						stepElement.setAttribute("object",
								testStep.getElementName());
						stepElement
								.setAttribute("action", testStep.getAction());
						stepElement
								.setAttribute("result", testStep.getResult());
						stepElement.setAttribute("errorMessage",
								testStep.getErrorMessage());
						stepElement.setAttribute("description",
								testStep.getActionDescription());
						stepElement
								.setAttribute("sortId", testStep.getSortId());
						stepElement.setAttribute("reusable",
								Boolean.valueOf(testCase.isReusable())
										.toString());
						stepElement.setAttribute(
								"executionTime",
								Util.getInstance().formatTimeMilliSec(
										testStep.getTestStepExecutionTime()));
						if (testStep.getAction().toUpperCase()
								.startsWith("WS_")) {
							if (testStep.getServiceRequestName().length() > 0) {
								stepElement.setAttribute("image",
										testStep.getServiceRequestName());
							} else {
								stepElement.setAttribute("image",
										testStep.getServiceResponseName());
							}
						} else {
							stepElement.setAttribute("image",
									testStep.getImageName());
						}
						stepsElement.appendChild(stepElement);
					}
				}
			}
		}

		LOGGER.trace("Created DOM tree element for test case ["
				+ testScenario.getBusinessScenarioId() + "] successfully");
		return stepsElement;
	}

	/**
	 * Create DOM Tree element for Test Case Details
	 * 
	 * @param testScenario
	 *            testScenario
	 * @return testCaseDetails
	 */
	private Element createTestCaseDetailsElement(TestScenario testScenario) {
		LOGGER.trace("Creating DOM tree element for test case details ["
				+ testScenario.getBusinessScenarioId() + "]");

		Element testCaseDetails = document.createElement("TestCaseDetails");
		Element testCaseElement = null;
		List<TestCase> testCaseList = testScenario.getTestCaseDetails();
		if (testCaseList != null && testCaseList.size() > 0) {
			Iterator<TestCase> testCaseIterator = testCaseList.iterator();
			while (testCaseIterator.hasNext()) {
				TestCase testCase = testCaseIterator.next();
				testCaseElement = document.createElement("TestCase");
				testCaseElement.setAttribute("id", testCase.getTestCaseId());
				testCaseElement.setAttribute("description",
						testCase.getTestCaseDesc());
				testCaseElement.setAttribute("sortId", testCase.getSortId());
				testCaseElement.setAttribute("reusable",
						Boolean.valueOf(testCase.isReusable()).toString());

				List<TestStep> testStepList = testCase.getTestStepDetails();
				long totalExecutionTime = 0;
				if (testStepList != null && testStepList.size() > 0) {
					Iterator<TestStep> testStepIterator = testStepList
							.iterator();
					while (testStepIterator.hasNext()) {
						TestStep testStep = testStepIterator.next();
						totalExecutionTime = totalExecutionTime
								+ testStep.getTestStepExecutionTime();
					}

				}
				testCaseElement.setAttribute("executionTime", Util
						.getInstance().formatTimeMilliSec(totalExecutionTime));
				testCase.setTestCaseExecutionTime(totalExecutionTime);
				testCaseDetails.appendChild(testCaseElement);
			}
		}

		LOGGER.trace("Created DOM tree element for test case details ["
				+ testScenario.getBusinessScenarioId() + "] successfully");
		return testCaseDetails;
	}

	/**
	 * This method uses Xerces specific classes and prints the XML document to
	 * file.
	 * 
	 * @param startTime
	 *            startTime
	 * @param testSuiteName
	 *            testSuiteName
	 * @throws AFTException
	 * 
	 */
	public void saveTestResult(java.util.Date startTime, String testSuiteName)
			throws AFTException {
		LOGGER.trace("Creating test result xml file");

		try {
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			// String resultFileName = "";

			resultFileName = Helper.getInstance().createReportXmlFileName(
					ConfigProperties.getInstance().getConfigProperty(
							ConfigProperties.TESTRESULT_PATH), startTime,
					testSuiteName, "Reports")
					+ ".xml";
			XMLSerializer serializer = null;
			serializer = new XMLSerializer(new FileOutputStream(new File(
					resultFileName)), format);
			serializer.serialize(document);

			LOGGER.debug("Created test result xml file [" + resultFileName
					+ "] successfully");

			refreshTestResultXMLFile();	 
		} catch (IOException io) {
			LOGGER.error("Exception:: ", io);
			throw new AFTException(io);
		}
	}

	/**
	 * @param files
	 *            array to be sorted by date
	 * @return sorted files array by date
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static File[] sortFilesByDate(File files[]) {
		Arrays.sort(files, new Comparator() {
			public int compare(final Object o1, final Object o2) {
				return new Long(((File) o1).lastModified()).compareTo(new Long(
						((File) o2).lastModified()));
			}
		});
		return files;
	}

	/**
	 * Calculates and Returns the Total Execution Duration.
	 * 
	 * @return totalExecutionDuration
	 * @throws AFTException
	 */
	private String getTotalExecutionTime() throws AFTException {

		String totalExecutionDuration = "";

		try {

			Iterator<TestSuite> suiteIterator = suiteResult.iterator();
			long totalSuiteExecutionTime = 0;
			while (suiteIterator.hasNext()) {
				TestSuite suite = (TestSuite) suiteIterator.next();
				totalSuiteExecutionTime = totalSuiteExecutionTime
						+ suite.getExecutionTime();
				totalExecutionDuration = Util.getInstance().formatTime(
						totalSuiteExecutionTime);
			}
			LOGGER.info("Total Execution Duration [" + totalExecutionDuration
					+ "]");

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return totalExecutionDuration;
	}

	/**
	 * This method will create test suite element
	 * 
	 * @param suiteList
	 *            suiteList
	 * @param resultElement
	 *            resultElement
	 */
	private void createTestSuiteElement(NodeList suiteList,
			Element resultElement) {
		for (int i = 0; i < suiteList.getLength(); i++) {
			Node nNode = suiteList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				Element suiteElement = resultListDocument
						.createElement(eElement.getNodeName());
				suiteElement
						.setAttribute("Name", eElement.getAttribute("Name"));
				suiteElement.setAttribute("browser",
						eElement.getAttribute("browser"));
				resultElement.appendChild(suiteElement);
				NodeList testList = eElement.getElementsByTagName("Test");
				setTestSuiteData(testList, suiteElement);
			}
		}
	}

	/**
	 * This method will set test suite data
	 * 
	 * @param testList
	 *            testList
	 * @param suiteElement
	 *            suiteElement
	 */

	private void setTestSuiteData(NodeList testList, Element suiteElement) {
		int testPass = 0;
		int testFail = 0;
		int testCasePass = 0;
		int testCaseFail = 0;
		for (int j = 0; j < testList.getLength(); j++) {
			Node testNode = testList.item(j);
			if (testNode.getNodeType() == Node.ELEMENT_NODE) {
				Element testElement = (Element) testNode;
				String result = testElement.getElementsByTagName("Result")
						.item(0).getTextContent();
				if (result.equalsIgnoreCase("Pass")) {
					testPass = testPass + 1;
				} else {
					testFail = testFail + 1;
				}
				NodeList stepsList = testElement.getElementsByTagName("Steps");
				for (int k = 0; k < stepsList.getLength(); k++) {
					Node stepNode = stepsList.item(k);
					if (stepNode.getNodeType() == Node.ELEMENT_NODE) {
						Element stepElement = (Element) stepNode;
						NodeList stepList = stepElement
								.getElementsByTagName("Step");
						for (int m = 0; m < stepList.getLength(); m++) {
							Node sNode = stepList.item(m);
							if (sNode.getNodeType() == Node.ELEMENT_NODE) {
								Element sElement = (Element) sNode;
								if (!sElement.getAttribute("result")
										.equalsIgnoreCase("FAIL")) {
									testCasePass = testCasePass + 1;
								} else {
									testCaseFail = testCaseFail + 1;
								}
							}
						}
					}
				}
			}
		}
		setElementData(testPass, testFail, testCasePass, testCaseFail,
				suiteElement);
	}

	/**
	 * This method will set the Element data
	 * 
	 * @param testPass
	 *            testPass
	 * @param testFail
	 *            testFail
	 * @param testCasePass
	 *            testCasePass
	 * @param testCaseFail
	 *            testCaseFail
	 * @param suiteElement
	 *            suiteElement
	 */
	private void setElementData(int testPass, int testFail, int testCasePass,
			int testCaseFail, Element suiteElement) {
		Element eleTestPass = resultListDocument.createElement("Test_Pass");
		eleTestPass.setTextContent(testPass + "");
		Element eleTestFail = resultListDocument.createElement("Test_Fail");
		eleTestFail.setTextContent(testFail + "");
		Element eleTestCasePass = resultListDocument
				.createElement("TestCase_Pass");
		eleTestCasePass.setTextContent(testCasePass + "");
		Element eleTestCaseFail = resultListDocument
				.createElement("TestCase_Fail");
		eleTestCaseFail.setTextContent(testCaseFail + "");
		suiteElement.appendChild(eleTestPass);
		suiteElement.appendChild(eleTestFail);
		suiteElement.appendChild(eleTestCasePass);
		suiteElement.appendChild(eleTestCaseFail);
	}

	/**
	 * Gets the resultFileName.
	 * 
	 * @return the resultFileName
	 */
	public String getResultFileName() {
		return resultFileName;
	}

	/**
	 * set the resultFileName
	 * 
	 * @param resultFileName
	 *            resultFileName
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}
}