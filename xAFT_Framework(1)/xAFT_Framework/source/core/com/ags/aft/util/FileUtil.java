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
 * Class: FileUtility
 * 
 * Purpose: This class contains utility methods to delete the files, which is
 * older than the file retention period configured.
 */
package com.ags.aft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * The Class FileUtility.
 */
public final class FileUtil {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(FileUtil.class);
	/**
	 * Constant used for xAFT Delete files/folders older than the retention
	 * period.
	 */
	private static final String TESTREPORTRESULTFILE = "TestResultList.xml";

	/** The XMl_ report_ file_ path. */
	private String xmlREPORTFILEPATH = "";

	/** The Log_ file_ path. */
	private String logFILEPATH = "";

	/** The SCREE n_shot_ file_ path. */
	private String screenSHOTFILEPATH = "";

	/** The SERVIC e_ request_ file_ path. */
	private String serviceREQUESTFILEPATH = "";

	/** contains screen shots. */
	private List<String> lstScreenShotsToDelete;

	/** contains log file. */
	private List<String> lstLogFileToDelete;

	/** contains service requests. */
	private List<String> lstServiceRequestsToDelete;

	/** contains report file to delete. */
	private List<String> fileListToDelete;

	/** The screen shot folder array. */
	private List<Set<String>> screenShotFolderArray = null;

	/** The message. */
	private String message = "";

	// file retention period variables
	/** The log file retention period. */
	private long logFileRetentionPeriod;

	/** The screen shots retention period. */
	private long screenShotsRetentionPeriod;

	/** The service requests retention period. */
	private long serviceRequestsRetentionPeriod;

	/** The report file retention period. */
	private long reportFileRetentionPeriod;

	// Instance of class
	/** The obj file utility. */
	private static FileUtil objFileUtility = null;

	// Private constructor
	/**
	 * Instantiates a new file utility.
	 * 
	 * @throws AFTException
	 */
	private FileUtil() throws AFTException {
		// Retrieving the file paths form config
		this.xmlREPORTFILEPATH = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.TESTRESULT_PATH);
		this.logFILEPATH = ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.LOGFILE_PATH);
		this.screenSHOTFILEPATH = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.SCREENSHOT_PATH);
		this.serviceREQUESTFILEPATH = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.SERVICEREQUEST_PATH);
	}

	// creating object of Singleton class
	/**
	 * Gets the single instance of FileUtility.
	 * 
	 * @return single instance of FileUtility
	 * @throws AFTException
	 */
	public static FileUtil getInstance() throws AFTException {
		if (null == objFileUtility) {
			objFileUtility = new FileUtil();
		}
		return objFileUtility;
	}

	/**
	 * To check the file/folders retention period. And Delete if the
	 * files/folders found older than the retention period.
	 * 
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void fileRetentionPeriodCheck() throws AFTException {

		// Retrieving the retention period params from config
		this.logFileRetentionPeriod = stringToLong(
				ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.LOG_FILE_RETENTION_PERIOD), "logFile");
		this.serviceRequestsRetentionPeriod = stringToLong(
				ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.SERVICE_REQUESTS_RETENTION_PERIOD),
				"serviceReq");
		this.screenShotsRetentionPeriod = stringToLong(
				ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.SCREEN_SHOTS_RETENTION_PERIOD),
				"screenShots");
		this.reportFileRetentionPeriod = stringToLong(
				ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.REPORT_FILE_RETENTION_PERIOD),
				"reportFile");

		LOGGER.info("LogFileRetentionPeriod [" + logFileRetentionPeriod
				+ "], ServiceRequestsRetentionPeriod ["
				+ serviceRequestsRetentionPeriod
				+ "], ScreenShotsRetentionPeriod ["
				+ screenShotsRetentionPeriod + "], ReportFileRetentionPeriod ["
				+ reportFileRetentionPeriod + "]");
		String fileInfo = deleteFiles();
		LOGGER.info(fileInfo);
	}

	/**
	 * Init method for deleting files exceeding the retention period.
	 * 
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String deleteFiles() throws AFTException {
		String msg = "0 files found to delete older than the retention period.";
		fileListToDelete = new ArrayList<String>();
		lstScreenShotsToDelete = new ArrayList<String>();
		lstLogFileToDelete = new ArrayList<String>();
		lstServiceRequestsToDelete = new ArrayList<String>();

		// get the file list exceeding the retention period and add to the List.
		listFilesExceedingRetentionPeriod();

		// iterate thru the file list array and get the list of folders to
		// delete
		if (null != fileListToDelete && fileListToDelete.size() > 0) {
			for (int i = 0; i < fileListToDelete.size(); i++) {

				msg = startDelete(fileListToDelete.get(i), "Report");
			}
		} else {
			// screen shots folders list
			if (null != lstScreenShotsToDelete
					&& lstScreenShotsToDelete.size() > 0) {
				for (int i = 0; i < lstScreenShotsToDelete.size(); i++) {

					msg = startDelete(lstScreenShotsToDelete.get(i),
							"ScreenShots");
				}
			}
			// log file list
			if (null != lstLogFileToDelete && lstLogFileToDelete.size() > 0) {
				for (int j = 0; j < lstLogFileToDelete.size(); j++) {

					msg = startDelete(lstLogFileToDelete.get(j), "LogFile");
				}
			}
			// service requests list
			if (null != lstServiceRequestsToDelete
					&& lstServiceRequestsToDelete.size() > 0) {
				for (int j = 0; j < lstServiceRequestsToDelete.size(); j++) {

					msg = startDelete(lstServiceRequestsToDelete.get(j),
							"ServiceRequests");
				}
			}
		}

		return msg;
	}

	/**
	 * prepare the list of files exceeding 15 days and 30 days retention period.
	 * 
	 * @throws AFTException
	 *             the aFT exception
	 */
	private void listFilesExceedingRetentionPeriod() throws AFTException {
		File file = new File(xmlREPORTFILEPATH);
		if (file.isDirectory()) {
			File[] fileList = file.listFiles();

			for (int i = 1; i < fileList.length; i++) {
				String fileName = fileList[i].getName();
				if (fileName.endsWith(".xml")
						&& !fileName.equalsIgnoreCase(TESTREPORTRESULTFILE)) {
					String fileInDateTimeFormat = convertFiletoDateFormat(fileName);
					if (!fileInDateTimeFormat.isEmpty()) {

						if (reportFileRetentionPeriod != -1
								&& getNumberOfDays(fileInDateTimeFormat) >= reportFileRetentionPeriod) {
							fileListToDelete.add(fileName);
						} else {
							if (screenShotsRetentionPeriod != -1
									&& getNumberOfDays(fileInDateTimeFormat) >= screenShotsRetentionPeriod) {
								lstScreenShotsToDelete.add(fileName);
							}
							if (logFileRetentionPeriod != -1
									&& getNumberOfDays(fileInDateTimeFormat) >= logFileRetentionPeriod) {
								lstLogFileToDelete.add(fileName);
							}
							if (serviceRequestsRetentionPeriod != -1
									&& getNumberOfDays(fileInDateTimeFormat) >= serviceRequestsRetentionPeriod) {
								lstServiceRequestsToDelete.add(fileName);
							}
						}
					}
				}
			}// eof loop

		}
	}

	/**
	 * Convert xml file to this format "2012-09-06 18:31:28".
	 * 
	 * @param reportFileName
	 *            the report file name
	 * @return the string
	 */
	private String convertFiletoDateFormat(String reportFileName) {
		String reqFormat = "";
		String dateTimeArr[] = reportFileName.split(".xml");
		dateTimeArr = dateTimeArr[0].split("_");
		if (dateTimeArr.length > 2) {
			reqFormat = dateTimeArr[1] + dateTimeArr[2];
		}
		return reqFormat;
	}

	/**
	 * Gets the number of days.
	 * 
	 * @param strStartTime
	 *            the str start time
	 * @return the number of days
	 * @throws AFTException
	 *             the aFT exception
	 */
	private long getNumberOfDays(String strStartTime) throws AFTException {
		long days = -1;
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date startDate;
		try {
			long currentTime = System.currentTimeMillis();
			startDate = formatter.parse(strStartTime);

			long startTime = startDate.getTime();
			long timeDiff = currentTime - startTime;

			days = TimeUnit.MILLISECONDS.toDays(timeDiff);

		} catch (ParseException e) {
			LOGGER.error("Exception::", e);
			// throw new AFTException(e);
		}
		return days;
	}

	/**
	 * Convert String to Long and also checks for default values.
	 * 
	 * @param value
	 *            the value
	 * @param type
	 *            the type
	 * @return the long
	 * @throws AFTException
	 *             the aFT exception
	 */
	private long stringToLong(String value, String type) throws AFTException {
		long intVal = -1;
		try {
			if (null != value) {
				intVal = Long.parseLong(value);

				if (type.contains("logFile") || type.contains("serviceReq")
						|| type.contains("reportFile")) {
					if (intVal != -1 && intVal < 15) { // Default value is 15
														// days
						intVal = -1;
					}
				} else if (type.contains("screenShots") && (intVal != -1)
						&& (intVal < 30)) { // Default value is 30 days
					intVal = -1;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::" + e);
			// throw new AFTException(e);
		}
		return intVal;
	}

	/**
	 * This function iterate thru the Document object to get the log file and
	 * folders details.
	 * 
	 * @param reportName
	 *            the report name
	 * @param type
	 *            the type
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	private String startDelete(String reportName, String type)
			throws AFTException {

		/** The xml report file name. */
		String xmlReportFileName = null;
		/** The log file name. */
		String logFileName = null;

		try {
			// prepare xml report file name format
			xmlReportFileName = reportName;

			String fileName = xmlREPORTFILEPATH + "/" + xmlReportFileName;
			LOGGER.trace("XML Report FileName = " + fileName);

			// Get the Document object by parsing the xml file
			Document doc = null;
			// doc = getDocument(fileName);
			File file = new File(fileName);

			if (file.isFile()) {
				doc = getDocument(fileName);
			} else {
				message = "The XML Report file '" + xmlReportFileName
						+ "' does not exist.";
			}

			Element elem = doc.getDocumentElement();

			// Get the LogFile Name
			logFileName = getLogFileName(elem);

			// Get the Screen shot folder Name
			Set<String> screenshotFolders = getScreenshotFolderNames(elem);

			// Adding the screen shot folders to the array list
			if (!screenshotFolders.isEmpty()) {
				addFolders(screenshotFolders);
			}
			LOGGER.trace("***** Deleting Report Process Started..****");
			if (type.equalsIgnoreCase("Report")) {
				// delete screenShotFolder
				deleteFolders(screenShotFolderArray, "ScreenShots");
				// delete serviceRequestFolder
				deleteFolders(screenShotFolderArray, "ServiceRequests");

				// delete log file
				deleteFile(logFILEPATH, logFileName);

				// delete xml report file
				boolean result = deleteFile(xmlREPORTFILEPATH, reportName);
				if (result) {
					// Refresh the TestResultList.xml file.
					refreshTestResultXMLFile();
				}
			} else if (type.equals("LogFile")) {
				// delete log file
				deleteFile(logFILEPATH, logFileName);
			} else if (type.equalsIgnoreCase("ScreenShots")
					|| type.equalsIgnoreCase("ServiceRequests")) {

				if (type.equalsIgnoreCase("ScreenShots")) {
					// delete screenShotFolder
					deleteFolders(screenShotFolderArray, type);
				} else if (type.equalsIgnoreCase("ServiceRequests")) {
					// delete serviceRequestFolder
					deleteFolders(screenShotFolderArray, type);
				}
			}

			message = "Files/Folders older than the retention period deleted successfully ... ";
		} catch (Exception e) {
			LOGGER.error("Exception::" + e);
			// throw new AFTException(e);
		}

		return message;
	}// eof startDelete()

	/**
	 * Returns the Document object after parsing the xml.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the document
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Document getDocument(String fileName) throws AFTException {
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			File file = new File(fileName);
			if (file.exists()) {
				doc = db.parse(file);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			// throw new AFTException(e);
		}

		return doc;

	}

	/**
	 * Return Log File Name form the XML Element passed.
	 * 
	 * @param elem
	 *            the elem
	 * @return the log file name
	 */
	private String getLogFileName(Element elem) {
		String fileName = null;
		NodeList nodeList = elem.getElementsByTagName("Environment");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NamedNodeMap attr = node.getAttributes();
			Node logFile = attr.getNamedItem("logFile");
			if (logFile.toString().length() > 0) {
				fileName = logFile.toString();
				fileName = fileName.substring(20, fileName.length() - 1);
			}
		}
		return fileName;
	}

	/**
	 * Returns HashSet object of Screen shot Folder Names from the XML element
	 * passed.
	 * 
	 * @param elem
	 *            the elem
	 * @return the screenshot folder names
	 */
	private Set<String> getScreenshotFolderNames(Element elem) {
		HashSet<String> screenshotFolders = null;
		screenshotFolders = new HashSet<String>();

		NodeList testSuiteList = elem.getElementsByTagName("TestSuite");
		for (int i = 0; i < testSuiteList.getLength(); i++) {
			NodeList testSuite = testSuiteList.item(i).getChildNodes();
			for (int p = 0; p < testSuite.getLength(); p++) {
				Node tests = testSuite.item(p);
				if ("Test".equals(tests.getNodeName().toString())) {
					NodeList testchild = tests.getChildNodes();
					for (int j = 0; j < testchild.getLength(); j++) {
						Node test = testchild.item(j);
						if ("Steps".equals(test.getNodeName())) {
							NodeList stepsChild = test.getChildNodes();
							for (int k = 0; k < stepsChild.getLength(); k++) {
								Node step = stepsChild.item(k);
								if ("Step".equals(step.getNodeName())) {
									NamedNodeMap stepAttr = step
											.getAttributes();
									if (stepAttr.getNamedItem("image")
											.toString().equals("image=\"\"")) {
										continue;
									} else {
										Node image = stepAttr
												.getNamedItem("image");

										// Adding screen shot folder to HashSet
										if (null != getFolderName(image
												.toString())) {
											String screenShotFolder = getFolderName(image
													.toString());
											screenshotFolders
													.add(screenShotFolder);
										}
									}
								}
							}
						}

					}
				}
			}
		}

		return screenshotFolders;
	}

	/**
	 * Returns Screen shot folder Name from the image path passed.
	 * 
	 * @param imagePath
	 *            the image path
	 * @return the folder name
	 */
	private String getFolderName(String imagePath) {
		String folderName = null;
		String imgFolder = "";
		if (null != imagePath && imagePath.length() > 0) {
			if (imagePath.indexOf("screenShots") > 0) {
				imgFolder = imagePath.substring(21);
				folderName = imgFolder.substring(0, imgFolder.indexOf('\\'));
			} else {
				imgFolder = imagePath.substring(25);
				folderName = imgFolder.substring(0, imgFolder.indexOf('/'));
			}
		}

		return folderName;
	}

	/**
	 * Adding HashSet object of screenshotFolder to ArrayList.
	 * 
	 * @param screenshotFolders
	 *            the screenshot folders
	 */
	private void addFolders(Set<String> screenshotFolders) {
		screenShotFolderArray = new ArrayList<Set<String>>();
		screenShotFolderArray.add(screenshotFolders);
	}

	/**
	 * Delete the folder from the path provided.
	 * 
	 * @param path
	 *           path
	 * @param folderName
	 *           folderName
	 * @return boolean either true/false.
	 */
	private boolean deleteFolder(String path, String folderName) {
		boolean deleted = false;
		File file = new File(path + "/" + folderName);
		if (file.isDirectory()) {
			File[] filesArray = file.listFiles();
			for (int i = 0; i < filesArray.length; i++) {
				filesArray[i].delete();
			}
			// delete folder
			deleted = file.delete();
			if (deleted) {
				message = "The screenShot folder '" + folderName
						+ "' has been deleted successfully.";
			} else {
				message = "The screenShot folder '" + folderName
						+ "' could not delete.";
			}
		} else {
			message = "The screenShot folder '" + folderName
					+ "' not available.";
		}
		return deleted;
	}

	/**
	 * Delete the file from the specific path
	 * 
	 * @param path
	 *         path
	 * @param fileName
	 *         fileName
	 * @return boolean either true/false.
	 */
	private boolean deleteFile(String path, String fileName) {
		boolean deleted = false;
		File file = new File(path + "/" + fileName);
		if (file.isFile()) {
			deleted = file.delete();
			if (deleted) {
				message = "The File '" + fileName
						+ "' has been deleted successfully.";
			} else {
				message = "The File '" + fileName
						+ "' could not be able to delete.";
			}
		} else {
			message = "The File '" + fileName + "' not available.";
		}
		return deleted;
	}

	/**
	 * Delete folders.
	 * 
	 * @param screenShotFolderArray
	 *            the screen shot folder array
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private boolean deleteFolders(List<Set<String>> screenShotFolderArray,
			String type) {
		boolean result = false;
		// screen shots and service request folders check
		if (null == screenShotFolderArray) {
			LOGGER.trace("Folders array is empty.");
		} else {
			ListIterator<Set<String>> itr = screenShotFolderArray
					.listIterator();
			while (itr.hasNext()) {
				Set<String> hs = itr.next();
				Iterator<String> strItr = hs.iterator();
				while (strItr.hasNext()) {
					String folderName = strItr.next();

					if (type.equalsIgnoreCase("ScreenShots")) {
						// Delete the screen shots folder
						result = deleteFolder(screenSHOTFILEPATH, folderName);
					}
					if (type.equalsIgnoreCase("ServiceRequests")) {
						// Delete the ServiceRequestFolder
						result = deleteFolder(serviceREQUESTFILEPATH,
								folderName);
					}

				}
			}// eof while
		}// eof else

		return result;
	}

	/**
	 * Refresh the TestResultList.xml file after performing delete operation.
	 * 
	 * @param currentTestResultFile
	 */
	private void refreshTestResultXMLFile() throws AFTException {
		LOGGER.trace("Refreshing test result file [TestResultList.xml	]");

		String resultListFile = xmlREPORTFILEPATH + TESTREPORTRESULTFILE;

		Document resultListDocument = null;
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			builder = factory.newDocumentBuilder();
			File resultFolder = null;
			resultFolder = new File(xmlREPORTFILEPATH);
			resultListDocument = builder.newDocument();
			Element rootNode = resultListDocument.createElement("ResultList");
			File[] xmlList = resultFolder.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					String f = new File(name).getName();
					return f.contains(".xml")
							&& !f.contains(TESTREPORTRESULTFILE);
				}
			});

			xmlList = sortFilesByDate(xmlList);

			for (int i = 0; i < xmlList.length; i++) {
				Element resultElement = resultListDocument
						.createElement("Result");
				resultElement.setAttribute("file", xmlList[i].getName());
				rootNode.appendChild(resultElement);
			}
			resultListDocument.appendChild(rootNode);
			OutputFormat format = new OutputFormat(resultListDocument);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
					resultListFile), format);
			serializer.serialize(resultListDocument);
			LOGGER.trace("Updated XML Test Report Result file with new test result xml file details successfully");
		} catch (IOException io) {
			LOGGER.error("Exception::" + io);
			throw new AFTException(io);
		} catch (ParserConfigurationException e) {
			LOGGER.error("Exception::" + e);
			throw new AFTException(e);
		}
	}

	/**
	 * 
	 * @param files
	 *          files
	 * @return files
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static File[] sortFilesByDate(File files[]) {
		Arrays.sort(files, new Comparator() {
			public int compare(final Object o1, final Object o2) {
				return new Long(((File) o1).lastModified()).compareTo(new Long(
						((File) o2).lastModified()));
			}
		});
		return files;
	}

}// eof class
