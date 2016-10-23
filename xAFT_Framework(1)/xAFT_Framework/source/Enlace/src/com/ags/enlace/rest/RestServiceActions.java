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
 * Class: RestServiceActions
 * 
 * Purpose: This class contains rest services for storing and retrieving
 * screenshot information.
 */
package com.ags.enlace.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.logging.Logger;
import com.ags.enlace.beans.ExecutionDetails;
import com.ags.enlace.beans.ImagePath;
import com.ags.enlace.beans.ScreenShot;

/**
 * The Class RestServiceActions.
 */
@Path("/XAFT")
public class RestServiceActions {

	/** The Constant LOG. */
	private static final Logger LOGGER = Logger
			.getLogger(RestServiceActions.class);

	/** Date format folder name */
	private static final String DATEFORMATFOLDERNAME = "yyyyMMdd_HHmmssS";
	private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String PARSEDATEFORMAT = "EEE MMM dd kk:mm:ss z yyyy";
	private static final String EXECUTION_STATAUS = "Scheduled";
	private static final String JSON = "JSON";
	private String portNumber;
	private String dbUrl;
	private String dbUser;
	private String dbPassword;
	private String serverHostName;

	/**
	 * This service will post the screenshot details information.
	 * 
	 * @param screenshotObj
	 *            the screenshotObj
	 * @return the ScreenShot object
	 */
	@POST
	@Path("storeScreenshot")
	@Consumes("application/json")
	@Produces("application/json")
	public ScreenShot storeScreenShot(ScreenShot screenshotObj)
			throws UnknownHostException {
		LOGGER.info("storeScreenShot method implementation");
		String screenShotFile = null;
		String tomcatHome = System.getProperty("catalina.home");
		String imageDataString = screenshotObj.getImageDataString();
		String imagePath = "";
		// set the dbDetails
		setDbDetails();
		// String ipAddress = InetAddress.getLocalHost().getHostName();
		StringBuffer url = new StringBuffer();
		url.append("http://").append(getServerHostName()).append(":")
				.append(getPortNumber());
		/*
		 * Converting a Base64 String into Image byte array
		 */
		byte[] imageByteArray = decodeImage(imageDataString);

		// Write a image byte array into file system
		FileOutputStream imageOutFile = null;
		// Create a place holder to store the screen shots or service
		// requests.
		DateFormat formatter = new SimpleDateFormat(DATEFORMATFOLDERNAME);
		String timestamp = formatter.format(new Date());
		try {
			LOGGER.info("Test suite Name as: "
					+ screenshotObj.getTestSuiteName());
			String screenShotPath = createFileDir(tomcatHome
					+ "/webapps/screenShots", screenshotObj.getTestSuiteName(),
					timestamp);
			LOGGER.info("Test scenario Id as: "
					+ screenshotObj.getTestScenarioId());
			String testCaseId = screenshotObj.getTestScenarioId() + "_1";
			String screenShotFileName = screenshotObj.getTestScenarioId() + "_"
					+ testCaseId + "_steps" + " " + timestamp + ".png";
			screenShotFile = screenShotPath + '/' + screenShotFileName;
			imageOutFile = new FileOutputStream(screenShotFile);
			imageOutFile.write(imageByteArray);
			imageOutFile.close();
			LOGGER.info("Stored the screenshot image successfully.");
			String home = tomcatHome + "/webapps";
			String path = screenShotFile.substring(home.length());
			path = url.toString() + path;
			File file = new File(path);
			imagePath = file.getPath();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
		LOGGER.info("Return the response with image path as : " + imagePath);
		screenshotObj.setPath(imagePath);
		return screenshotObj;
	}

	/**
	 * This service will post the servicerequest details information.
	 * 
	 * @param screenshotObj
	 *            the screenshotObj
	 * @return the ScreenShot object
	 */
	@POST
	@Path("storeServiceRequest")
	@Consumes("application/json")
	@Produces("application/json")
	public ScreenShot storeServiceRequest(ScreenShot screenshotObj)
			throws UnknownHostException {
		LOGGER.info("storeServiceRequest method implementation");
		String screenShotFile = null;
		String tomcatHome = System.getProperty("catalina.home");
		String imageDataString = screenshotObj.getImageDataString();
		String imagePath = "";
		// set the dbDetails
		setDbDetails();
		// String ipAddress = InetAddress.getLocalHost().getHostName();
		StringBuffer url = new StringBuffer();
		url.append("http://").append(getServerHostName()).append(":")
				.append(getPortNumber());
		/*
		 * Converting a Base64 String into Image byte array
		 */
		byte[] imageByteArray = decodeImage(imageDataString);

		// Write a image byte array into file system
		FileOutputStream imageOutFile = null;
		// Create a place holder to store the screen shots or service
		// requests.
		DateFormat formatter = new SimpleDateFormat(DATEFORMATFOLDERNAME);
		String timestamp = formatter.format(new Date());
		try {
			LOGGER.info("Test suite Name as: "
					+ screenshotObj.getTestSuiteName());
			String screenShotPath = createFileDir(tomcatHome
					+ "/webapps/serviceRequests",
					screenshotObj.getTestSuiteName(), timestamp);
			LOGGER.info("Test scenario Id as: "
					+ screenshotObj.getTestScenarioId());
			String testCaseId = screenshotObj.getTestScenarioId() + "_1";
			String screenShotFileName = screenshotObj.getTestScenarioId() + "_"
					+ testCaseId + "_steps" + " " + timestamp + ".xml";
			screenShotFile = screenShotPath + '/' + screenShotFileName;
			imageOutFile = new FileOutputStream(screenShotFile);
			if (screenshotObj.getResponseType().equalsIgnoreCase(JSON)) {
				String tag = "<jsondata>";
				imageOutFile.write(tag.getBytes());
				tag = "<![CDATA[";
				imageOutFile.write(tag.getBytes());
			}
			imageOutFile.write(imageByteArray);
			if (screenshotObj.getResponseType().equalsIgnoreCase(JSON)) {
				String tag = "]]>";
				imageOutFile.write(tag.getBytes());
				tag = "</jsondata>";
				imageOutFile.write(tag.getBytes());
			}
			imageOutFile.close();
			LOGGER.info("Stored the serviceRequest xml successfully.");
			String home = tomcatHome + "/webapps";
			String path = screenShotFile.substring(home.length());
			path = url.toString() + path;
			File file = new File(path);
			imagePath = file.getPath();
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
		LOGGER.info("Return the response with image path as : " + imagePath);
		screenshotObj.setPath(imagePath);
		return screenshotObj;
	}

	/**
	 * This service will get the screen shot details information.
	 * 
	 * @param testSuiteId
	 *            the testSuiteId
	 * @param projectId
	 *            projectId
	 * @param scenarioId
	 *            scenarioId
	 * @param testStepId
	 *            testStepId
	 * @return the ScreenShot object
	 */
	@GET
	@Path("getScreenshot/{testSuiteId}/{projectId}/{scenarioId}")
	@Consumes("application/json")
	@Produces("application/json")
	public ScreenShot getScreenshot(
			@PathParam("testSuiteId") String testSuiteId,
			@PathParam("projectId") String projectId,
			@PathParam("scenarioId") String scenarioId,
			@QueryParam("testStepId") String testStepId)
			throws UnknownHostException {

		LOGGER.info("getScreenshot method implementation with parameters as :");
		LOGGER.info("Test suite Id :" + testSuiteId);
		LOGGER.info("Project Id :" + projectId);
		LOGGER.info("Test scenario Id :" + scenarioId);

		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		StringBuffer sqlQuery = new StringBuffer();
		ScreenShot obj = new ScreenShot();
		ImagePath path = null;
		List<ImagePath> pathList = new ArrayList<ImagePath>();
		try {
			// set the dbDetails
			setDbDetails();
			Class.forName("com.mysql.jdbc.Driver");
			// get the Db connection
			conn = DriverManager.getConnection(getDbUrl(), getDbUser(),
					getDbPassword());
			// call the statement
			stmt = conn.createStatement();
			sqlQuery.append("select rtss.idProject, rtss.testSuiteName, rtss.idTestSuiteReport,rts.idTestScenariosReport,rts.description,rt.idTestStepsReport, rt.result, rt.executionEvidence,rt.description as testStepDesc from rpt_testsuite rtss INNER JOIN rpt_testscenarios rts ON rtss.idTestSuiteReport=rts.idTestSuiteReport INNER JOIN rpt_teststeps rt ON rts.idTestScenariosReport=rt.idTestScenariosReport where rtss.idProject=");
			sqlQuery.append(projectId);
			sqlQuery.append(" and rtss.idTestSuiteReport=");
			sqlQuery.append(testSuiteId);
			sqlQuery.append(" and rts.idTestScenariosReport=");
			sqlQuery.append(scenarioId);
			if (testStepId != null) {
				sqlQuery.append(" and rt.idTestStepsReport=");
				sqlQuery.append(testStepId);
			} else {
				sqlQuery.append(" and rt.evidenceType='IMAGE'");
			}
			LOGGER.info("SQl Query :" + sqlQuery);
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				obj.setProjectId(rs.getString("idProject"));
				obj.setTestSuiteId(rs.getString("idTestSuiteReport"));
				obj.setTestSuiteName(rs.getString("testSuiteName"));
				obj.setTestScenarioId(rs.getString("idTestScenariosReport"));
				obj.setTestScenarioDescription(rs.getString("description"));

				// set image path details
				path = new ImagePath();
				path.setImagePath(rs.getString("executionEvidence"));
				path.setTestStepId(rs.getString("idTestStepsReport"));
				path.setTestStepDescription(rs.getString("testStepDesc"));
				path.setResult(rs.getString("result"));
				pathList.add(path);
				obj.setImagePaths(pathList);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
		}
		return obj;
	}

	/**
	 * This service will post the execution details information.
	 * 
	 * @param executionDetails
	 *            the executionDetails
	 * @return the executionDetails
	 */
	@POST
	@Path("saveExecutionDetails")
	@Consumes("application/json")
	@Produces("application/json")
	public ExecutionDetails saveExecutionDetails(
			ExecutionDetails executionDetails) throws UnknownHostException {
		LOGGER.info("saveExecutionDetails method implementation");
		Connection conn = null;
		Statement stmt = null;
		Date date = null;
		String formattedDate = "";
		StringBuffer sqlQuery = new StringBuffer();
		boolean flag = false;
		ExecutionDetails details = new ExecutionDetails();
		try {
			// set the dbDetails
			setDbDetails();
			DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
			SimpleDateFormat sdf = new SimpleDateFormat(PARSEDATEFORMAT);
			if (executionDetails.getExecutionTime() != null) {
				date = sdf.parse(executionDetails.getExecutionTime());
				if (date != null) {
					formattedDate = dateFormat.format(date);
				}

			}

			LOGGER.info("save ExecutionDetails with parameters as :");
			LOGGER.info("Test Batch Id :" + executionDetails.getTestBatchId());
			LOGGER.info("Test set Id :" + executionDetails.getTestSetId());
			LOGGER.info("Project Id :" + executionDetails.getProjectId());
			LOGGER.info("Execution Status :"
					+ executionDetails.getExecutionStatus());
			LOGGER.info("Execution Time :"
					+ executionDetails.getExecutionTime());
			LOGGER.info("Execution Order :"
					+ executionDetails.getExecutionOrder());
			LOGGER.info("Host Ip :" + executionDetails.getHostIp());
			Class.forName("com.mysql.jdbc.Driver");
			// get the Db connection
			conn = DriverManager.getConnection(getDbUrl(), getDbUser(),
					getDbPassword());
			// call the statement
			stmt = conn.createStatement();
			sqlQuery.append("INSERT INTO testexecutions (idTestBatch,idTestSet,idProject,executionStatus,executionOrder,executionTime,hostName,hostIp)");
			sqlQuery.append("VALUES(");
			sqlQuery.append(executionDetails.getTestBatchId()).append(",");
			sqlQuery.append(executionDetails.getTestSetId()).append(",");
			sqlQuery.append(executionDetails.getProjectId()).append(",");
			sqlQuery.append("'").append(executionDetails.getExecutionStatus())
					.append("'").append(",");
			sqlQuery.append(executionDetails.getExecutionOrder()).append(",");
			sqlQuery.append("'").append(formattedDate).append("'").append(",");
			sqlQuery.append("'").append(executionDetails.getHostName())
					.append("'").append(",");
			sqlQuery.append("'").append(executionDetails.getHostIp())
					.append("'");
			sqlQuery.append(")");
			LOGGER.info("SQl Query :" + sqlQuery.toString());
			// insert the record into DB.
			int record = stmt.executeUpdate(sqlQuery.toString());
			if (record > 0) {
				flag = true;
				details.setInserted(flag);
				LOGGER.info("Inserted successfully.");
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
		}
		return details;
	}

	/**
	 * This service will gets the execution details information.
	 * 
	 * @param hostIp
	 *            the hostIp
	 * @return the detailsList
	 */
	@GET
	@Path("getExecutionDetails/{hostIp}")
	@Consumes("application/json")
	@Produces("application/json")
	public List<ExecutionDetails> getExecutionDetails(
			@PathParam("hostIp") String hostIp) {
		LOGGER.info("getExecutionDetails method implementation with parameters hostIp as :"
				+ hostIp);
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		ExecutionDetails details = null;
		List<ExecutionDetails> detailsList = new ArrayList<ExecutionDetails>();
		try {
			// set the dbDetails
			setDbDetails();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			StringBuffer sqlQuery = new StringBuffer();
			sqlQuery.append("SELECT * FROM testexecutions WHERE hostIp=");
			sqlQuery.append("'").append(hostIp).append("'");
			sqlQuery.append(" and executionTime <= ").append("'");
			sqlQuery.append(dateFormat.format(cal.getTime())).append("'");
			sqlQuery.append(" and executionStatus=").append("'")
					.append(EXECUTION_STATAUS).append("'");
			sqlQuery.append(" order by executionTime,executionorder");
			Class.forName("com.mysql.jdbc.Driver");
			// get the Db connection
			conn = DriverManager.getConnection(getDbUrl(), getDbUser(),
					getDbPassword());
			// call the statement
			stmt = conn.createStatement();
			LOGGER.info("SQl Query :" + sqlQuery);
			rs = stmt.executeQuery(sqlQuery.toString());
			while (rs.next()) {
				details = new ExecutionDetails();
				details.setProjectId(rs.getString("idProject"));
				details.setTestBatchId(rs.getString("idTestBatch"));
				details.setTestSetId(rs.getString("idTestSet"));
				details.setHostName(rs.getString("hostName"));
				details.setExecutionId(rs.getString("idTestexecutions"));
				details.setDbUrl(getDbUrl());
				details.setDbUserName(getDbUser());
				details.setDbPassword(getDbPassword());
				detailsList.add(details);
			}
			LOGGER.info("Retrieved the execution details successfully.");
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
			rs = null;
			stmt = null;
			conn = null;
		}
		return detailsList;
	}

	/**
	 * This service will update the execution details information.
	 * 
	 * @param details
	 *            the details
	 */

	@PUT
	@Path("update")
	@Consumes("application/json")
	@Produces("application/json")
	public void updateExecutionDetials(ExecutionDetails details) {
		LOGGER.info("updateExecutionDetials method implementation");
		Connection conn = null;
		Statement stmt = null;
		try {
			StringBuffer sqlQuery = new StringBuffer();
			// set the dbDetails
			setDbDetails();
			LOGGER.info("Execution Status as :" + details.getExecutionStatus());
			sqlQuery.append("update testexecutions ").append(
					"set executionStatus=");
			sqlQuery.append("'").append(details.getExecutionStatus())
					.append("'");
			sqlQuery.append(" WHERE idTestexecutions=");
			LOGGER.info("Execution Id as :" + details.getExecutionId());
			sqlQuery.append(details.getExecutionId());
			Class.forName("com.mysql.jdbc.Driver");
			// get the Db connection
			conn = DriverManager.getConnection(getDbUrl(), getDbUser(),
					getDbPassword());
			// call the statement
			stmt = conn.createStatement();
			LOGGER.info("SQl Query :" + sqlQuery);
			stmt.executeUpdate(sqlQuery.toString());
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
			stmt = null;
			conn = null;
		}
	}

	/**
	 * Create folder for capturing screen shot (or) service request.
	 * 
	 * @param path
	 *            the path
	 * @param testStartTime
	 *            time when the test started to create folder for capturing
	 *            screenshots or service requests or report xml's for this run
	 * @param testSuiteName
	 *            the test suite name
	 * @param sourceName
	 *            the source name
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	private String createFileDir(String path, String testSuiteName,
			String timestamp) {
		LOGGER.info("createFileDir method implementation");
		String filePath = path;
		try {

			File f = new File(filePath);
			if (f.exists() || f.isDirectory() || f.canWrite()) {
				filePath = filePath + "/" + testSuiteName + "_" + timestamp;
				(new File(filePath)).mkdir();
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
		}
		return filePath;
	}

	/**
	 * Read Database Details from properties file.
	 */
	private void setDbDetails() {
		LOGGER.info("setDbDetails method implementation");
		File dbProperties = new File(System.getProperty("catalina.base"),
				"conf/enlace.properties");
		if (dbProperties.exists()) {
			InputStream input;
			try {
				input = new FileInputStream(dbProperties);
				Properties properties = new Properties();
				properties.load(input);
				setDbUrl(properties.getProperty("dburl"));
				LOGGER.info("dbUrl:" + properties.getProperty("dburl"));
				setDbUser(properties.getProperty("dbuserName"));
				LOGGER.info("dbuserName:"
						+ properties.getProperty("dbuserName"));
				setDbPassword(properties.getProperty("dbpassword"));
				LOGGER.info("dbpassword:"
						+ properties.getProperty("dbpassword"));
				setServerHostName(properties.getProperty("serverHostName"));
				LOGGER.info("serverHostName:"
						+ properties.getProperty("serverHostName"));
				setPortNumber(properties.getProperty("portNumber"));
				LOGGER.info("portNumber:"
						+ properties.getProperty("portNumber"));
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}
		}
	}

	/**
	 * Decodes the base64 string into byte array
	 * 
	 * @param imageDataString
	 *            - a {@link java.lang.String}
	 * @return byte array
	 */
	private static byte[] decodeImage(String imageDataString) {
		return Base64.decodeBase64(imageDataString);
	}

	/**
	 * @return the dbUrl
	 */
	private String getDbUrl() {
		return dbUrl;
	}

	/**
	 * @param dbUrl
	 *            the dbUrl to set
	 */
	private void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * @return the dbUser
	 */
	private String getDbUser() {
		return dbUser;
	}

	/**
	 * @param dbUser
	 *            the dbUser to set
	 */
	private void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	/**
	 * @return the dbPassword
	 */
	private String getDbPassword() {
		return dbPassword;
	}

	/**
	 * @param dbPassword
	 *            the dbPassword to set
	 */
	private void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	/**
	 * @return the serverHostName
	 */
	private String getServerHostName() {
		return serverHostName;
	}

	/**
	 * @param serverHostName
	 *            the serverHostName to set
	 */
	private void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	/**
	 * @return the portNumber
	 */
	private String getPortNumber() {
		return portNumber;
	}

	/**
	 * @param portNumber
	 *            the portNumber to set
	 */
	private void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}

}
