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
 * Class: Notifications
 * 
 * Purpose: This class contains utility methods to send email notifications.
 */

package com.ags.aft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.testObjects.TestCase;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.testObjects.TestStep;
import com.ags.aft.testObjects.TestSuite;

/**
 * The Class Notifications.
 */
public final class Notifications {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Notifications.class);

	/** The aftNotifications. */
	private static Notifications aftNotifications;

	/**
	 * Instantiates a new Notifications.
	 */
	private Notifications() {
		super();
	}

	/**
	 * Gets the single instance of Notifications.
	 * 
	 * @return single instance of Notifications
	 */
	public static Notifications getInstance() {
		if (aftNotifications == null) {
			aftNotifications = new Notifications();
			LOGGER.trace("Creating instance of Notifications");
		}

		return aftNotifications;
	}

	/**
	 * sends the email notification to specified email address.
	 * 
	 * @param emailSubject
	 *            the email subject
	 * @param msgBody
	 *            the msg body
	 * @throws AFTException
	 *             the application exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void emailFrameworkNotification(String emailSubject, String msgBody)
			throws AFTException, IOException {
		String attachment = "";
		String toAddress = ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.EMAIL_NOTIF_TO_ADDR);

		String ccAddress = ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.EMAIL_NOTIF_CC_ADDR);

		LOGGER.debug("Executing Email Notification for: \n" + "toAddress ["
				+ toAddress + "], ccAddress [" + ccAddress + "]");

		String attachExecutionLogFile = ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.ATTACH_EXECUTION_LOG_FILE);
		if (attachExecutionLogFile.equalsIgnoreCase("Yes")) {
			File zipFile = createZipFile(Log4JPlugin.getInstance().getLogFile());
			if (zipFile != null) {
				attachment = zipFile.getAbsolutePath();
			}
		}
		String value = "[[" + toAddress + "]],[[" + ccAddress + "]],[["
				+ emailSubject + "]],[[" + msgBody + "]],[[" + attachment
				+ "]]";
		sendEmail(value);
	}

	/**
	 * This method sends the test started/stopped email notifications.
	 * 
	 * @param iStarted
	 *            This parameter is used to identify if the notification to be
	 *            sent is for test started/stopped TESTSTARTEMAILNOTIF (1) =
	 *            Test Start Notification Email TESTENDEMAILNOTIF (2) = Test
	 *            Stop Notification Email
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void sendTestStartedNotification(int iStarted) throws AFTException {
		// Read email notification properties...
		//
		try {
			String toAddress = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_NOTIF_TO_ADDR);
			LOGGER.info("toAddress: [" + toAddress + "]");

			if (toAddress.isEmpty()) {
				LOGGER.error("Email Notification - To Address is left blank");
			}

			String ccAddress = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_NOTIF_CC_ADDR);

			// Set email notification TO and CC properties for Log4J
			//
			Log4JPlugin.getInstance().setEmailNotificationForToAndCc(toAddress,
					ccAddress);
			if (iStarted == Constants.TESTENDEMAILNOTIF) {
				LOGGER.info("Emailing the Test results..");
				String emailMessageBody = createTestResultEmail();
				// String attachment = file.getAbsolutePath();
				emailFrameworkNotification(Constants.EMAILSUBJECTEND,
						emailMessageBody);
			}
		} catch (Exception e) {
			// display the exception
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * This method creates a string of each test suite results to be printed in
	 * the Test Ended email notificaiton.
	 * 
	 * @return String appended with Test suite results
	 */
	public String createSuiteResultString() {

		String testsuiteResults, testcaseResults, totalExecutionTime, browserType;

		int passedCount, failedCount, totalExecutedCount;

		long totalExecution, hr, min, sec, msec;

		testsuiteResults = "";
		testcaseResults = "";

		// Iterate through each of the TestSuite
		// Get the count of passed, failed test case in each suite
		// and prepare the result string with other details

		Iterator<TestSuite> suiteIterator = ReportGenerator.getInstance()
				.getSuiteResult().iterator();

		while (suiteIterator.hasNext()) {

			TestSuite suite = suiteIterator.next();

			List<TestScenario> testScenarios = suite.getTestScenariosArr();

			Iterator<TestScenario> testScenarioIter = testScenarios.iterator();

			// initialize/reset the count
			passedCount = 0;
			failedCount = 0;
			totalExecutedCount = 0;
			// get passed,failed,total test case count in the current suite
			// create string with results of each test case in the current suite
			while (testScenarioIter.hasNext()) {
				TestScenario testScenario = testScenarioIter.next();
				if (testScenario != null) {
					if (testScenario.getExecutionResult().equalsIgnoreCase(
							"Pass")) {
						passedCount++;
					} else {
						failedCount++;
					}
				}

				totalExecutedCount++;

				// string with each test case results
				testcaseResults = testcaseResults + "\nTest Case ID:"
						+ testScenario.getBusinessScenarioId() + " "
						+ "Test Case Description :"
						+ testScenario.getBusinessScenarioDesc() + " "
						+ "Execution Status :"
						+ testScenario.getExecutionResult();

			}

			// total execution time of the current suite
			totalExecution = suite.getExecutionTime();
			hr = totalExecution / 1000 / 60 / 60;
			min = totalExecution / 1000 / 60 - hr * 60;
			sec = totalExecution / 1000 - hr * 60 * 60 - min * 60;
			msec = totalExecution - (hr * 60 * 60 - min * 60 - sec) * 1000;

			totalExecutionTime = hr + ":" + min + ":" + sec + ":" + msec;

			// build browser type
			browserType = getBrowserType(suite.getBrowserName());

			// append the current suite + test case results
			testsuiteResults = testsuiteResults + "\n\nTest Suite :"
					+ suite.getTestSuiteName() + " " + "Browser Type :"
					+ browserType + " " + "Application URL :" + suite.geturl()
					+ " " + "Test Cases Executed :" + totalExecutedCount + " "
					+ "Passed :" + passedCount + " " + "Failed :" + failedCount
					+ " " + "Total Execution Time :" + totalExecutionTime
					+ testcaseResults;

			// reset testcaseResults
			testcaseResults = "";

		}

		return testsuiteResults;
	}

	/**
	 * Send Email Method : To send Email about the test execution status. It can
	 * be used either at the beginning or in the middle or at the end of the
	 * test execution The input string value should contain the data in the form
	 * of [[ToList]], [[CCList]], [[Subject]], [[Body]] and [[Attachments]]
	 * 
	 * @param value
	 *            the value
	 * @throws AFTException
	 *             the application exception
	 */
	public void sendEmail(String value) throws AFTException {
		// verify the input data format
		try {
			if (!value.contains(",")) {
				LOGGER.error("Value should contain [[ToList]], [[CCList]], [[Subject]], [[Body]] and [[Attachments]]");
				throw new AFTException(
						"Value should contain [[ToList]], [[CCList]], [[Subject]], [[Body]] and [[Attachments]]");
			}

			// Split the Element value separated with ','
			String[] valueList = value.split("]],");

			// Verifying the length of the value list
			if (valueList.length < 5) {
				LOGGER.error("Value should contain [[ToList]], [[CCList]], [[Subject]], [[Body]] and [[Attachments]]");
				throw new AFTException(
						"Value should contain [[ToList]], [[CCList]], [[Subject]], [[Body]] and [[Attachments]]");
			}

			// Separate the ValueList from the parenthesis '[[ ]]'
			for (int i = 0; i < valueList.length; i++) {
				valueList[i] = valueList[i].replace("[", "");
				valueList[i] = valueList[i].replace("]", "");
			}

			// Log the ValueList
			LOGGER.trace("To List [" + valueList[0] + "]");
			LOGGER.trace("CC List [" + valueList[1] + "]");
			LOGGER.trace("Subject [" + valueList[2] + "]");
			LOGGER.trace("Body [" + valueList[3] + "]");
			LOGGER.trace("Attachments [" + valueList[4] + "]");

			String serverAddress = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_SERVER_ADDR);

			String serverPort = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_SERVER_PORT);

			String serverAuth = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_SERVER_AUTH);

			String serverStartTLS = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_SERVER_STARTTLS);

			final String userName = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_USERNAME);

			final String password = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_PASSWORD);

			String fromAddress = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_NOTIF_FROM_ADDR);

			LOGGER.debug("Sending email using following properties: \n"
					+ "\t serverAddress [" + serverAddress + "], serverPort ["
					+ serverPort + "], serverAuth [" + serverAuth
					+ "], serverStartTLS [" + serverStartTLS + "], userName ["
					+ userName + "], password [" + password + "]");

			// Verify the ToList content
			if (valueList[0].length() == 0) {
				LOGGER.error("TO List should not be empty");
				throw new AFTException("TO List should not be empty");
			}
			// Verify the length of the subject
			if (valueList[2].length() > 255) {
				LOGGER.error("Subject length should be < 255 characters");
				throw new AFTException(
						"Subject length should be < 255 characters");
			}

			LOGGER.debug("Executing Email Notification for: \n"
					+ "\t fromAddress [" + fromAddress + "], toAddress ["
					+ valueList[0] + "], ccAddress [" + valueList[1]
					+ "], emailSubject [" + valueList[2] + "], msg ["
					+ valueList[3] + "], attachments [" + valueList[4] + "]");

			Session session = null;
			// Create properties, get Session
			Properties props = new Properties();

			if (serverAuth.compareToIgnoreCase("true") == 0) {
				props.put("mail.smtp.auth", "true");
				if (serverStartTLS.compareToIgnoreCase("true") == 0) {
					props.put("mail.smtp.starttls.enable", "true");
				}
				props.put("mail.transport.protocol", "smtp");
				props.put("mail.smtp.host", serverAddress);
				props.put("mail.smtp.port", Integer.parseInt(serverPort));

				session = Session.getDefaultInstance(props,
						new javax.mail.Authenticator() {
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(userName,
										password);
							}
						});
			} else {
				// If using static Transport.send(), need to specify which host
				// to send it to
				props.put("mail.smtp.host", serverAddress);

				session = Session.getInstance(props);
			}

			Message msg = getMessage(session, valueList, fromAddress);

			// Send the message
			Transport.send(msg);

		} catch (MessagingException mex) {
			// display the exception
			LOGGER.error("Exception::", mex);
			throw new AFTException(mex);
		}
	}

	/**
	 * Gets the message.
	 * 
	 * @param session
	 *            the session
	 * @param valueList
	 *            the value list
	 * @param fromAddress
	 *            the from address
	 * @return the message
	 * @throws MessagingException
	 *             the messaging exception
	 */
	private Message getMessage(Session session, String[] valueList,
			String fromAddress) throws MessagingException {
		// Instantiate a message
		Message msg = new MimeMessage(session);

		// Set message attributes
		msg.setFrom(new InternetAddress(fromAddress));

		// Split the multiple email ID s separated with ';'
		String[] toList = valueList[0].split(";");
		// create a addressTo object
		InternetAddress[] addressTo = new InternetAddress[toList.length];
		for (int i = 0; i < toList.length; i++) {
			// assign to addressTo object
			addressTo[i] = new InternetAddress(toList[i]);
		}

		// Set the recipient To list
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		// Verify the content of CC List
		if (valueList[1].length() > 0) {
			// Split the multiple email ID s separated with
			// ';'
			String[] ccList = valueList[1].split(";");
			// create a addressCC object
			InternetAddress[] addressCC = new InternetAddress[ccList.length];
			for (int i = 0; i < ccList.length; i++) {
				// assign to addressCC object
				addressCC[i] = new InternetAddress(ccList[i]);
			}

			msg.setRecipients(Message.RecipientType.CC, addressCC);
		} else {
			LOGGER.trace("CC List is Empty ");
		}

		// set the subject
		msg.setSubject(valueList[2]);

		// set the body of the message
		msg.setContent(valueList[3], "text/html");

		// verify the attachment content
		if (valueList[4].equals("")) {
			LOGGER.trace("No attachment to set");
		} else {
			LOGGER.trace("Attachment is there to set");

			// create the bodypart object
			BodyPart messageBodyPart = new MimeBodyPart();
			String name = null;
			// Fill the body message
			messageBodyPart.setContent(valueList[3], "text/html");

			// create the multipart
			Multipart multipart = new MimeMultipart();
			// add the body to the multipart
			multipart.addBodyPart(messageBodyPart);

			String attachment = valueList[4];

			// for (int index = 0; index < attachmentList.length; index++) {
			messageBodyPart = new MimeBodyPart();
			// create a Datasource object
			DataSource source = new FileDataSource(attachment);
			// set the data source
			messageBodyPart.setDataHandler(new DataHandler(source));
			String fileName = new FileDataSource(attachment).getFile()
					.getName();

			if (fileName.contains("Automated")) {
				String logFile = Log4JPlugin.getInstance().getLogFile();
				String[] f = logFile.split("//");
				String[] logFileName = f[1].split(".log");
				name = logFileName[0];
				name = name.concat(".zip");
			}
			// set the attachment part
			messageBodyPart.setFileName(name);
			// add the body part
			multipart.addBodyPart(messageBodyPart);
			// Put parts in message
			msg.setContent(multipart);
		}
		return msg;
	}

	/**
	 * This method is used to prepare the test results in html format.
	 * 
	 * @return the string
	 */
	public String createTestResultEmail() {
		// File file = null;
		String totalExecutionTime;
		long totalExecution, hr, min, sec;
		int passedCount, failedCount, totalExecutedCount;
		StringBuffer html = new StringBuffer();
		int rowCounter = 0;
		String startTime = "";
		String endTime = "";
		String endDate = "";
		try {
			LOGGER.info("---------Inside createTestResultAttachment method---------------------------");

			startTime = ReportGenerator.getInstance().getStartDate();
			endTime = ReportGenerator.getInstance().getEndTime();
			if (endTime != null && !endTime.equals("")) {
				endDate = endTime;
			} else {
				endDate = "Execution Not Completed";
			}

			html = setEmailTestSummaryData(startTime, endDate, html);
			List<TestSuite> suiteIterator = ReportGenerator.getInstance()
					.getSuiteResult();

			if (suiteIterator != null & suiteIterator.size() > 0) {
				html = createEmailTableHeader(html);

				for (TestSuite suite : suiteIterator) {
					List<TestScenario> testScenarios = suite
							.getTestScenariosArr();
					// initialize/reset the count
					passedCount = 0;
					totalExecutedCount = 0;
					failedCount = 0;
					// get passed,failed,total test case count in the current
					// suite
					// create string with results of each test case in the
					// current suite
					for (TestScenario testScenario : testScenarios) {
						if (testScenario != null) {
							if (testScenario.getExecutionResult()
									.equalsIgnoreCase("Pass")) {
								passedCount++;
							} else {
								failedCount++;
							}
						}
						totalExecutedCount++;
					}
					// total execution time of the current suite
					totalExecution = suite.getExecutionTime();
					hr = totalExecution / 1000 / 60 / 60;
					min = totalExecution / 1000 / 60 - hr * 60;
					sec = totalExecution / 1000 - hr * 60 * 60 - min * 60;

					totalExecutionTime = hr + ":" + min + ":" + sec;

					// Table data at test Batch level.
					if (rowCounter % 2 == 0) {
						html.append("		<tr style=\"background-color: #88ACE0;text-align:center\">");
					} else {
						html.append("		<tr style=\"background-color: #BCD2EE;text-align:center\">");
					}
					html = setEmailTestSuiteData(html, suite,
							totalExecutionTime, passedCount, failedCount,
							totalExecutedCount);
					rowCounter++;
				}
				html.append("	</TABLE>");
				html.append("			<br>");
				html = createEmailTestScenarioHeader(html);
				html = setEmailTestScenarioData(suiteIterator, html);
				html.append("	</TABLE>");
			}
			html.append("			<br>").append("		</form>").append("	</body>")
					.append("</html>");

		} catch (Exception e) {// Catch exception if any
			LOGGER.error("Exception::", e);
		}
		LOGGER.info("---------Exiting createTestResultAttachment---------------------------");
		return html.toString();
	}

	/**
	 * This method is used to set test scenario level data for publishing
	 * results in email.
	 * 
	 * @param suiteIterator
	 *            the suite iterator
	 * @param html
	 *            the html
	 * @return the string buffer
	 */
	private StringBuffer setEmailTestScenarioData(
			List<TestSuite> suiteIterator, StringBuffer html) {
		int counter = 0;
		long hr, min, sec;
		// Table data at test scenario level.
		for (TestSuite suite : suiteIterator) {
			List<TestScenario> testScenarios = suite.getTestScenariosArr();
			for (TestScenario testScenario : testScenarios) {
				if (testScenario != null) {
					long scenarioExecution = testScenario.getExecutionTime();
					hr = scenarioExecution / 1000 / 60 / 60;
					min = scenarioExecution / 1000 / 60 - hr * 60;
					sec = scenarioExecution / 1000 - hr * 60 * 60 - min * 60;
					String scenarioExecutionTime = hr + ":" + min + ":" + sec;
					if (counter % 2 == 0) {
						if (testScenario.getExecutionResult().equalsIgnoreCase(
								"Fail")) {
							html.append("		<tr style=\"background-color: #88ACE0;color: #FF0000;text-align:center\">");
						} else {
							html.append("		<tr style=\"background-color: #88ACE0;text-align:center\">");
						}

					} else {
						if (testScenario.getExecutionResult().equalsIgnoreCase(
								"Fail")) {
							html.append("		<tr style=\"background-color: #BCD2EE;color: #FF0000;text-align:center\">");
						} else {
							html.append("		<tr style=\"background-color: #BCD2EE;text-align:center\">");
						}
					}
					String suiteName = suite.getTestSuiteName();
					html.append(
							"				<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
							.append(suiteName)
							.append(".")
							.append(testScenario.getBusinessScenarioId())
							.append("				</td>")
							.append("				<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
							.append(testScenario.getBusinessScenarioDesc())
							.append("				</td>")
							.append("				<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
							.append(testScenario.getExecutionResult())
							.append("				</td>")

							.append("				<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
							.append(scenarioExecutionTime).append("				</td>")
							.append("	   		</tr>");
					counter++;
				}
			}
		}
		return html;
	}

	/**
	 * This method will create a test scenario table header for publishing
	 * results in email.
	 * 
	 * @param html
	 *            the html
	 * @return the string buffer
	 */
	private StringBuffer createEmailTestScenarioHeader(StringBuffer html) {
		html.append(
				"	<TABLE WIDTH=\"98%\" align=\"center\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\" ID=\"tb2Sample\">")
				.append("		<caption  style=\"color:#FF4500;margin-left:-100px;font-family: Arial;font-size: 16px;font-weight: bold;text-decoration:underline;\">")
				.append("Test Scenario Level Report")
				.append("     </caption>")
				.append("		<tr style=\"background-color: #26466D;color: white;text-align:center\">")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Scenario ID</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Description</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Result</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Execution Duration</Th>")
				.append("		</tr>");
		return html;

	}

	/**
	 * This method is used to set test suite level data for publishing results
	 * in email.
	 * 
	 * @param html
	 *            the html
	 * @param suite
	 *            the suite
	 * @param totalExecutionTime
	 *            the total execution time
	 * @param passedCount
	 *            the passed count
	 * @param failedCount
	 *            the failed count
	 * @param totalExecutedCount
	 *            the total executed count
	 * @return the string buffer
	 */
	private StringBuffer setEmailTestSuiteData(StringBuffer html,
			TestSuite suite, String totalExecutionTime, int passedCount,
			int failedCount, int totalExecutedCount) {
		String suiteName = suite.getTestSuiteName();
		// build browser type
		String browserType = getBrowserType(suite.getBrowserName());
		html.append(
				"			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(suiteName)
				.append("			</td>")
				.append("			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(suite.getCategory())
				.append("			</td>")
				.append("			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(suite.getExecutionEngine())
				.append("			</td>")
				.append("			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(browserType)
				.append("			</td>")
				.append("			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(suite.geturl())
				.append("			</td>")
				.append("			<td style=\"color:#1E762D;text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(passedCount)
				.append("			</td>")
				.append("			<td style=\"color:#FF0000;text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(failedCount)
				.append("			</td>")
				.append("			<td style=\"color:#0000FF;text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(totalExecutedCount)
				.append("			</td>")
				.append("			<td style=\"text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
				.append(totalExecutionTime).append("			</td>")
				.append("	   </tr>");
		return html;
	}

	/**
	 * This method is used to set test summary level data for publishing results
	 * in email.
	 * 
	 * @param startTime
	 *            the start time
	 * @param endDate
	 *            the end date
	 * @param html
	 *            the html
	 * @return the string buffer
	 * @throws UnknownHostException
	 *             the unknown host exception
	 */
	private StringBuffer setEmailTestSummaryData(String startTime,
			String endDate, StringBuffer html) throws UnknownHostException {
		html.append("<html>")
				.append("	<body style=\"background-color:white\">")
				.append("		<form name=\"resultListForm\" style=\"background-color:#FFFFFF\" method=\"get\">")
				.append("			<br>")
				.append("			<TABLE WIDTH=\"98%\" align=\"center\"  border=\"0\" cellspacing=\"1\" cellpadding=\"1\" ID=\"Table1\">")
				.append("	<caption  style=\"color:#FF4500;margin-left:-100px;font-family: Arial;font-size: 16px;font-weight: bold;text-decoration:underline;\">")
				.append("Execution Summary Report")
				.append("			</caption>")
				.append("				<tr  style=\"background-color: #BCD2EE;color: white;text-align:center\">")
				.append("					<td style=\"color:#DC143C; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 14px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
				.append("					Host :")
				.append("				<span style=\"color:#0000FF; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"host_info\"> ")
				.append(InetAddress.getLocalHost().getHostName())
				.append("		            </span>")
				.append("					</td>")
				.append("					<td style=\"color:#DC143C; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 14px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
				.append("					Operating System : ")
				.append("				<span style=\"color:#0000FF; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"os_info\"> ")
				.append(System.getProperty("os.name"))
				.append("		            </span>")
				.append("					</td>")
				.append("				</tr>")
				.append("				<tr style=\"background-color: #BCD2EE;color: white;text-align:center\">")
				.append("					<td style=\"color:#DC143C; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 14px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
				.append("					Start Time : ")
				.append("				<span style=\"color:#0000FF; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\"  id=\"start_time\"> ")
				.append(startTime)
				.append("		            </span>")
				.append("					</td>")
				.append("					<td style=\"color:#DC143C; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 14px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
				.append("					End Time : ")
				.append("				<span style=\"color:#0000FF; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"end_time\"> ")
				.append(endDate).append("		            </span>")
				.append("					</td>").append("				</tr>").append("			</TABLE>")
				.append("			<br>");
		return html;
	}

	/**
	 * This method is used to create email Table Header.
	 * 
	 * @param html
	 *            the html
	 * @return the string buffer
	 */
	private StringBuffer createEmailTableHeader(StringBuffer html) {
		html.append(
				"			<TABLE WIDTH=\"98%\" align=\"center\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\" ID=\"tblSample\">")
				.append("		<tr style=\"background-color: #26466D;color: white;text-align:center\">")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Batch Name</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Category</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Execution Engine</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Browser</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Execution URL</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Pass</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Fail</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Total</Th>")
				.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Execution Duration</Th>")
				.append("		</tr>");
		return html;
	}

	/**
	 * This method is used to zip the given file.
	 * 
	 * @param logFile
	 *            the log file
	 * @return the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public File createZipFile(String logFile) throws IOException {
		LOGGER.info("---------Inside createZipFile---------------------------");
		File file = new File(logFile);
		File zipFile = null;
		if (!file.exists()) {
			LOGGER.error("Log File not found to zip.");
		} else {
			String[] f = logFile.split("//");
			String[] logFileName = f[1].split(".log");
			byte[] buffer = new byte[18024];
			try {
				zipFile = File.createTempFile(logFileName[0], ".zip");
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
						zipFile));
				out.setLevel(Deflater.DEFAULT_COMPRESSION);
				FileInputStream in = new FileInputStream(logFile);
				out.putNextEntry(new ZipEntry(logFile));
				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				out.closeEntry();
				in.close();
				out.close();
			} catch (IllegalArgumentException iae) {
				LOGGER.error("Exception::", iae);
			} catch (FileNotFoundException fnfe) {
				LOGGER.error("Exception::", fnfe);
			} catch (IOException ioe) {
				LOGGER.error("Exception::", ioe);
			}
		}
		LOGGER.info("---------Exiting createZipFile---------------------------");
		return zipFile;
	}

	/**
	 * This method sends the test scenario failure email notifications.
	 * 
	 * @param suite
	 *            the suite
	 * @param applException
	 *            the appl exception
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void sendTestFailedNotification(TestSuite suite,
			AFTException applException) throws AFTException {
		// Read email notification properties...
		//
		LOGGER.info("---------Inside sendTestFailedNotification---------------------------");
		int rowCounter = 0;

		String toAddress = ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.EMAIL_NOTIF_TO_ADDR);
		LOGGER.info("toAddress: [" + toAddress + "]");
		try {
			if (toAddress.isEmpty()) {
				LOGGER.error("Email Notification - To Address is left blank");
			}

			String ccAddress = ConfigProperties.getInstance()
					.getConfigProperty(ConfigProperties.EMAIL_NOTIF_CC_ADDR);

			// Set email notification TO and CC properties for Log4J
			//
			Log4JPlugin.getInstance().setEmailNotificationForToAndCc(toAddress,
					ccAddress);

			LOGGER.info("Sending Failure notification email");
			StringBuffer emailMessageBody = new StringBuffer();
			String browserType = getBrowserType(suite.getBrowserName());
			emailMessageBody
					.append("<html>")
					.append("	<body style=\"background-color:white\">")
					.append("		<form name=\"resultListForm\" style=\"background-color:#FFFFFF\" method=\"get\">")
					.append("			<br>")
					.append("			<TABLE WIDTH=\"98%\" align=\"center\"  border=\"0\" cellspacing=\"1\" cellpadding=\"1\" ID=\"Table1\">")
					.append("	<caption  style=\"color:#FF4500;margin-left:-100px;font-family: Arial;font-size: 16px;font-weight: bold;text-decoration:underline;\">")
					.append("Execution Summary Report")
					.append("			</caption>")
					.append("				<tr  style=\"background-color: #BCD2EE;color: white;text-align:center\">")
					.append("					<td style=\"color:#27426E; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
					.append("					Host :")
					.append("				<span style=\"color:black; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"host_info\"> ")
					.append(InetAddress.getLocalHost().getHostName())
					.append("		            </span>")
					.append("					</td>")
					.append("					<td style=\"color:#27426E; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
					.append("					Operating System : ")
					.append("				<span style=\"color:black; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"os_info\"> ")
					.append(System.getProperty("os.name"))
					.append("		            </span>")
					.append("					</td>")
					.append("				</tr>")
					.append("				<tr  style=\"background-color: #BCD2EE;color: white;text-align:center\">")
					.append("					<td style=\"color:#27426E; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
					.append("					Browser :")
					.append("				<span style=\"color:black; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"host_info\"> ")
					.append(browserType)
					.append("		            </span>")
					.append("					</td>")
					.append("					<td style=\"color:#27426E; text-align: left; vertical-align: bottom;font-family: Arial;font-size: 12px;font-weight: bold;padding-top: 5px;padding-left:15px;\" colspan=\'1\'>")
					.append("					Execution URL : ")
					.append("				<span style=\"color:black; text-align: left; font-family: Arial;font-size: 12px;font-weight: bold;\" id=\"os_info\"> ")
					.append(suite.geturl()).append("		            </span>")
					.append("					</td>").append("				</tr>")
					.append("			</TABLE>").append("			<br>");
			List<TestCase> testCaseDetails = suite.getTestScenario()
					.getTestCaseDetails();
			if (testCaseDetails != null && testCaseDetails.size() > 0) {
				emailMessageBody
						.append("	<TABLE WIDTH=\"98%\" align=\"center\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\" ID=\"tb2Sample\">")
						.append("		<caption  style=\"color:#FF4500;margin-left:-100px;font-family: Arial;font-size: 16px;font-weight: bold;text-decoration:underline;\">")
						.append("Test Scenario Level Report")
						.append("     </caption>")
						.append("		<tr style=\"background-color: #26466D;color: white;text-align:center\">")
						.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Scenario ID</Th>")
						.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Scenario Description</Th>")
						.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Case ID</Th>")
						.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Test Case Description</Th>")
						.append("			<Th style=\"color:#FFFFFF;font-family: Arial;font-size: 12px;font-weight: bold;\">Exception Details</Th>")
						.append("		</tr>");

				for (TestCase testCase : testCaseDetails) {
					for (TestStep testStep : testCase.getTestStepDetails()) {
						if (testStep.getResult().equalsIgnoreCase("FAIL")) {
							// Table data at test Batch level.
							if (rowCounter % 2 == 0) {
								emailMessageBody
										.append("		<tr style=\"background-color: #88ACE0;text-align:center\">");
							} else {
								emailMessageBody
										.append("		<tr style=\"background-color: #BCD2EE;text-align:center\">");
							}

							emailMessageBody
									.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
									.append(suite.getTestScenario()
											.getBusinessScenarioId())
									.append("				</td>")
									.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
									.append(suite.getTestScenario()
											.getBusinessScenarioDesc())
									.append("				</td>")
									.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
									.append(testStep.getTestCase()
											.getTestCaseId())
									.append("				</td>")
									.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
									.append(testCase.getTestCaseDesc())
									.append("				</td>");
							if (testStep.getErrorMessage() != null) {
								emailMessageBody
										.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
										.append(testStep.getErrorMessage());
							} else {
								emailMessageBody
										.append("				<td style=\"text-align: left; color: #FF0000; vertical-align: bottom;font-family: Arial;font-size: 12px;padding-top: 5px;padding-left:15px;\">")
										.append(" ");
							}
							emailMessageBody.append("				</td>").append(
									"	   		</tr>");
						}
						rowCounter++;
					}
				}
				emailMessageBody.append("	</TABLE>");
			}
			emailMessageBody.append("			<br>").append("		</form>")
					.append("	</body>").append("</html>");
			emailFrameworkNotification(Constants.SCENARIOID
					+ suite.getTestScenario().getBusinessScenarioId()
					+ Constants.TESTSTEPFAILURE, emailMessageBody.toString());
			LOGGER.info("Failure Notification mail sent successfully");
		} catch (Exception e) {
			LOGGER.error("Failed to send Failure Notification Email");
		}
		LOGGER.info("---------Exiting sendTestFailedNotification---------------------------");
	}

	/**
	 * This method gets the browser type based on given browser name.
	 * 
	 * @param browserType
	 *            the browser type
	 * @return the browser type
	 */
	private String getBrowserType(String browserType) {
		String browserName = browserType;
		if (browserName.equals("*iexplore")
				|| browserName.equals("*iexploreproxy")
				|| browserName.equals("*iehta")) {
			browserName = "Internet Explorer";
		} else if (browserName.equals("*firefox")
				|| browserName.equals("*firefoxproxy")
				|| browserName.equals("*chrome")) {
			browserName = "Firefox";
		} else {
			browserName = browserName.replace('*', ' ');
		}
		return browserName;
	}

}
