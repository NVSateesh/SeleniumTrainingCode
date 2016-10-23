package com.ags.aft.Reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.ags.aft.exception.AFTException;

/** The class WriteXMLFile. */
public class WriteXMLFile {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WriteXMLFile.class);
	/** The saxBuilder variable. */
	private SAXBuilder builder = null;
	/** The document variable. */
	private Document doc = null;
	
	/**
	 * This method will create a root node.
	 * 
	 * @return doc
	 * */
	public Document createRootNode() {
		Element company = new Element("ResultList");
		doc = new Document();
		doc.setRootElement(company);
		return doc;
	}

	/**
	 * This method will get a root node.
	 * @param fileName
	 *          fileName
	 * @return doc
	 * @throws IOException
	 */
	public Document getRootNode(String fileName) throws JDOMException,
			IOException {
		builder = new SAXBuilder();
		doc = (Document) builder.build(fileName);
		return doc;
	}

	/**
	 * This method will verify file.
	 * 
	 * @param file
	 *            file
	 * @return boolean
	 */
	public boolean verifyFile(String file) {
		boolean flag = false;
		File f = new File(file);
		if (f.exists()) {
			flag = true;
		}
		return flag;
	}

	/**
	 * This method will get a root node.
	 * @param fileName
	 *          the fileName
	 * @param readFileName
	 *          the readFileName
	 * @throws IOException
	 */
	public void writeXMLFile(String fileName, String readFileName)
			throws AFTException {
		XMLOutputter xmlOutput = new XMLOutputter();
		// display nice nice
		xmlOutput.setFormat(Format.getPrettyFormat());
		try {
			xmlOutput.output(createXML(fileName, readFileName), new FileWriter(
					fileName));
		} catch (Exception e) {
			LOGGER.error(e);
			throw new AFTException(e);
		}
	}

	/**
	 * This method will get test suite count.
	 * @param file
	 *          the file
	 * @return testSuiteList
	 */
	public List<Element> testSuiteCount(String file) {
		List<Element> testSuiteList = null;
		File xmlFile = null;
		builder = new SAXBuilder();
		xmlFile = new File(file);
		try {
			Document document = (Document) builder.build(xmlFile);
			Element rootNode = document.getRootElement();
			testSuiteList = rootNode.getChildren("TestSuite");
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return testSuiteList;
	}

	/**
	 * This method will create xml.
	 * 
	 * @param acFile
	 *            the file
	 * @param readFile
	 *            the readFile
	 * @return documnet
	 */
	public Document createXML(String acFile, String readFile)
			throws AFTException {
		Element rootNode = null;
		Element testSuite = null;
		int testPass = 0;
		int testFail = 0;
		int testCasePass = 0;
		int testCaseFail = 0;
		Document document = null;
		try {
			if (verifyFile(acFile)) {
				document = getRootNode(acFile);
			} else {
				document = createRootNode();
			}
			rootNode = document.getRootElement();
			Element result = new Element("Result");
			result.setAttribute(new Attribute("file", readFile.substring(
					readFile.lastIndexOf('/') + 1, readFile.length())));
			rootNode.addContent(result);
			List<Element> listTSCount = testSuiteCount(readFile);
			Iterator<Element> i = listTSCount.iterator();
			while (i.hasNext()) {
				testSuite = i.next();
				Element mainFileTS = new Element(testSuite.getName());
				mainFileTS.setAttribute(new Attribute("name", testSuite
						.getAttributeValue("Name")));
				mainFileTS.setAttribute(new Attribute("Browser", testSuite
						.getAttributeValue("browser")));
				result.addContent(mainFileTS);

				List<Element> listTest = testSuite.getChildren("Test");
				Iterator<Element> j = listTest.iterator();
				while (j.hasNext()) {
					Element testscenario = j.next();
					if (testscenario.getChildText("Result").equalsIgnoreCase(
							"Pass")) {
						testPass = testPass + 1;
					} else {
						testFail = testFail + 1;
					}
					List<Element> stepsList = testscenario.getChildren("Steps");
					Iterator<Element> stepsIterator = stepsList.iterator();
					while (stepsIterator.hasNext()) {
						List<Element> stepList = stepsIterator.next()
								.getChildren("Step");
						Iterator<Element> stepIterator = stepList.iterator();
						while (stepIterator.hasNext()) {
							Element stepNode = stepIterator.next();
							if (stepNode.getAttribute("result").getValue()
									.equalsIgnoreCase("PASS")) {
								testCasePass = testCasePass + 1;
							} else {
								testCaseFail = testCaseFail + 1;
							}
						}
					}
				}
				setElementData(testPass, testFail, testCasePass, testCaseFail,
						mainFileTS);
			}
		} catch (Exception e) {
			LOGGER.error(e);
			throw new AFTException(e);
		}

		return document;

	}

	/**
	 * This method will set the Element data
	 * 
	 * @param testPass
	 *          the testPass
	 * @param testFail
	 *          the testFail
	 * @param testCasePass
	 *           the testCasePass
	 * @param testCaseFail
	 *            the testCaseFail
	 * @param mainFileTS
	 *             the mainFileTS
	 */
	private void setElementData(int testPass, int testFail,
			int testCasePass, int testCaseFail, Element mainFileTS ) {
		Element eleTestPass = new Element("Test_Pass");
		eleTestPass.setText(testPass + "");
		Element eleTestFail = new Element("Test_Fail");
		eleTestFail.setText(testFail + "");
		Element eleTestCasePass = new Element("TestCase_Pass");
		eleTestCasePass.setText(testCasePass + "");
		Element eleTestCaseFail = new Element("TestCase_Fail");
		eleTestCaseFail.setText(testCaseFail + "");
		mainFileTS.addContent(eleTestPass);
		mainFileTS.addContent(eleTestFail);
		mainFileTS.addContent(eleTestCasePass);
		mainFileTS.addContent(eleTestCaseFail);
	}
}