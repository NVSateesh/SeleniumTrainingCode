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
 * Class: WebServicesFixture
 * 
 * Purpose: This class implements methods that allows users to execute
 * webservices
 */
package com.ags.aft.fixtures.webservices;

import static com.jayway.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ags.aft.common.JsonParser;
import com.ags.aft.common.XMLParser;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.fixtures.common.CommandFixtures;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * The Class WebServicesFixture.
 */
public final class WebServicesFixture {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(WebServicesFixture.class);

	/** The web services. */
	private static WebServicesFixture webServices;

	/** The xml parser obj. */
	private XMLParser xmlParserObj;

	/** The xml parser obj. */
	private JsonParser jsonParserObj;

	/** The request. */
	private String request;

	/** The response. */
	private String response;

	private String contentType;

	private Map<String, List<String>> authenticationData = new HashMap<String, List<String>>();

	/**
	 * private constructor for creating a singleton object.
	 */
	private WebServicesFixture() {
		super();

		xmlParserObj = null;
		jsonParserObj = null;
	}

	/**
	 * Gets the single instance of WebServicesFixture.
	 * 
	 * @return single instance of WebServicesFixture
	 */
	public static WebServicesFixture getInstance() {
		if (webServices == null) {
			webServices = new WebServicesFixture();
			LOGGER.trace("Creating instance of WebServicesFixture");
		}

		return webServices;
	}

	/**
	 * Web services - load request xml/json.
	 * 
	 * @param value
	 *            the value
	 * @return the request as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsLoadRequest(String value) throws AFTException {
		String action = null;
		String requestValue = null;

		try {
			String[] values = value.split("\\" + Constants.ATTRIBUTESDELIMITER);
			if (values.length < 2) {
				String errMsg = "Invalid ws_LoadRequest usage ["
						+ value
						+ "] specified. Please refer technical documentation on how to use [wsLoadRequest]";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			} else if (values.length > 2) {
				requestValue = values[2];
				action = values[1];
			} else if (values.length == 2) {
				requestValue = values[0];
				action = values[1];
			}
			if (action.equalsIgnoreCase(Constants.XML)) {
				request = wsLoadXMLRequest(requestValue);
				contentType = "text/XML";
			} else if (action.equalsIgnoreCase(Constants.JSON)) {
				request = wsLoadJsonRequest(requestValue);
				contentType = "application/json";
			}

			// Set the request value to system variable system
			// AFT_WSREQUEST_RESPONSE
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_WSREQUEST_RESPONSE), true,
					request);

			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE), true,
					action);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return request;
	}

	/**
	 * Web services - load request xml.
	 * 
	 * @param value
	 *            the value
	 * @return the request xml as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsLoadXMLRequest(String value) throws AFTException {
		String xmlString;

		if (value.isEmpty()) {
			LOGGER.error("Base XML need to be provided");
			throw new AFTException("Base XML need to be provided");
		} else {
			xmlParserObj = new XMLParser();
			xmlString = xmlParserObj.readXML(value);
		}

		return xmlString;
	}

	/**
	 * Web services - load request json.
	 * 
	 * @param value
	 *            the value
	 * @return the request json as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsLoadJsonRequest(String value) throws AFTException {
		String jsonString;

		if (value.isEmpty()) {
			LOGGER.error("Base Json need to be provided");
			throw new AFTException("Base Json need to be provided");
		} else {
			jsonParserObj = new JsonParser();
			jsonString = jsonParserObj.readJson(value);
		}

		return jsonString;
	}

	/**
	 * Web services - construct request.
	 * 
	 * @param element
	 *            the element
	 * @param value
	 *            the value
	 * @param testStepRunner
	 *            test step runner object
	 * @return the request as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsSubstituteValue(String element, String value,
			TestStepRunner testStepRunner) throws AFTException {

		try {
			String responseType = Variable.getInstance().generateSysVarName(
					SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE);

			// get the actual value
			responseType = Helper.getInstance().getActionValue(responseType);
			if (element.contains(Constants.ATTRIBUTESDELIMITER)) {
				// substitute multiple attributes.
				substituteMultipleAttributes(element, responseType, value,
						testStepRunner);
				LOGGER.info("Request after substitution [" + request + "]");

			} else if (element.contains("/")) {
				// substitute attribute.
				substituteAttribute(element, responseType, value,
						testStepRunner);
				LOGGER.info("Request after substitution [" + request + "]");
			}

			// Set the XML request value to system variable system
			// AFT_WSREQUEST_RESPONSE
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_WSREQUEST_RESPONSE), true,
					request);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return request;
	}

	/**
	 * Substitutes multiple attributes with the given values
	 * 
	 * @param element
	 *            element
	 * @param responseType
	 *            responseType
	 * @param value
	 *            value
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void substituteMultipleAttributes(String element,
			String responseType, String value, TestStepRunner testStepRunner)
			throws AFTException {
		String[] elementsArray = null;
		String[] valueArray = null;
		elementsArray = element.trim().split(
				"\\" + Constants.ATTRIBUTESDELIMITER);
		valueArray = value.split("\\" + Constants.ATTRIBUTESDELIMITER);
		for (int i = 0; i <= elementsArray.length - 1; i++) {
			if ((elementsArray[i].trim().startsWith(
					Constants.TESTDATASTARTVARIABLEIDENTIFIER) && elementsArray[i]
					.trim().endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER))
					|| (elementsArray[i].trim().startsWith(
							Constants.DYNAMICVARIABLEDELIMITER) && elementsArray[i]
							.trim()
							.endsWith(Constants.DYNAMICVARIABLEDELIMITER))) {
				elementsArray[i] = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), elementsArray[i]);
			}
			String[] xPathArray = elementsArray[i].trim().split("/");
			if (responseType.equalsIgnoreCase(Constants.XML)) {
				request = xmlParserObj.updateAttributeValue(xPathArray[0],
						xPathArray[1], valueArray[i]);
			} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
				request = jsonParserObj.updateAttributeValue(request,
						xPathArray, valueArray[i]);
			}
		}
	}

	/**
	 * Substitutes attribute with the given value
	 * 
	 * @param element
	 *            element
	 * @param responseType
	 *            responseType
	 * @param value
	 *            value
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 *             the application exception
	 */
	private void substituteAttribute(String element, String responseType,
			String value, TestStepRunner testStepRunner) throws AFTException {
		String elementVal = null;
		if ((element.trim().startsWith(
				Constants.TESTDATASTARTVARIABLEIDENTIFIER) && element.trim()
				.endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER))
				|| (element.trim().startsWith(
						Constants.DYNAMICVARIABLEDELIMITER) && element.trim()
						.endsWith(Constants.DYNAMICVARIABLEDELIMITER))) {
			elementVal = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), element);
		} else {
			elementVal = element;
		}
		String[] elementList = elementVal.trim().split("/");
		if (responseType.equalsIgnoreCase(Constants.XML)) {
			request = xmlParserObj.updateAttributeValue(elementList[0],
					elementList[1], value);
			LOGGER.info("Request xml after substitution [" + request + "]");
		} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
			request = jsonParserObj.updateAttributeValue(request, elementList,
					value);
			LOGGER.info("Request json after substitution [" + request + "]");
		}
	}

	/**
	 * Web services - send request.
	 * 
	 * @param value
	 *            the value
	 * @param testStepRunner
	 *            testStepRunner
	 * @return the xml as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsSendRequest(String value, TestStepRunner testStepRunner)
			throws AFTException {

		try {
			String[] values = value.split(",");
			if (values.length < 1) {
				String errMsg = "Invalid ws_SendRequest usage ["
						+ value  
						+ "] specified. Please refer technical documentation on how to use [ws_SendRequest]";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			if (values[0].equalsIgnoreCase(Constants.REST)) {
				response = wsRestRequest(values, testStepRunner);
			} else if (values[0].equalsIgnoreCase(Constants.SOAP)
					|| values[0].equalsIgnoreCase(Constants.XML)) {
				response = wsPostRequest(values);
			} else {
				response = wsPostRequest(values);
			}

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		// Set the response value to system variable system
		// AFT_WSREQUEST_RESPONSE
		Variable.getInstance()
				.setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_WSREQUEST_RESPONSE), true,
						response);

		return response;
	}

	/**
	 * Web services - post request.
	 * 
	 * @param value
	 *            the value
	 * @return the xml as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsPostRequest(String[] value) throws AFTException {
		String url, soapAction = null, contentTypeVal = null;

		try {
			if (value.length > 1) {
				if (value.length > 1) {
					url = value[1];
				} else {
					LOGGER.error("No end-point url specified");
					throw new AFTException("No end-point url specified");
				}
				if (value.length > 2) {
					soapAction = value[2];
				}
				if (value.length > 3) {
					contentTypeVal = value[3];
				} else {
					contentTypeVal = "text/xml;charset=UTF-8";
				}
			} else {
				url = value[0];
				contentTypeVal = "text/xml;charset=UTF-8";
			}

			if (url.toLowerCase().startsWith("https")) {
				setHttpsConnection(url);
			}
			HttpClient httpclient = new HttpClient();
			PostMethod post = new PostMethod(url);
			// Process web service request when directly targeting the endpoint
			// url with no Request XML specified
			if (request != null) {
				RequestEntity entity = new StringRequestEntity(request, null,
						null);
				post.setRequestEntity(entity);
			} else {
				LOGGER.info("Looks like user has not specified any Request XML."
						+ " Making a direct call to Endpoint url");
			}
			post.setRequestHeader("Content-type", contentTypeVal);
			post.setRequestHeader("SOAPAction", soapAction);

			int statusCode = httpclient.executeMethod(post);
			LOGGER.info("Soap action [" + soapAction
					+ "] returned status Code [" + statusCode + "]");

			response = post.getResponseBodyAsString();
			LOGGER.info("Response XML [" + response + "]");

			// check if the response code is OK (200)
			if (statusCode != 200) {
				String errMsg = "Response from webservice ["
						+ url
						+ "] is ["
						+ statusCode
						+ "] (NOT SUCCESS)."
						+ "Please check the parameters passed and send the request again.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			// Set the response type to system variable system
			// AFT_WSREQUEST_RESPONSE_TYPE
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE), true,
					Constants.XML);
		} catch (IOException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return response;
	}

	/**
	 * Web services - send request.
	 * 
	 * @param value
	 *            the value
	 * @param testStepRunner
	 *            testStepRunner
	 * @return the response as string
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsRestRequest(String[] value, TestStepRunner testStepRunner)
			throws AFTException {
		String url = null;
		String restAction = null;
		Response responseObj = null;

		try {
			if (value.length == 3) {
				url = value[1];
				restAction = value[2];
			} else {
				LOGGER.error("No end-point url  or rest action not specified");
				throw new AFTException(
						"No end-point url  or rest action not specified");
			}
			if (restAction != null) {
				// get the response
				responseObj = getRestResponse(url, restAction);
			}
			if (responseObj != null) {
				// set the response in system variable
				setResponse(responseObj, url);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return response;
	}

	/**
	 * Web services - Get the response.
	 * 
	 * @param url
	 *            the url
	 * @param restAction
	 *            rest action
	 * @return the response as string
	 * @throws AFTException
	 *             the application exception
	 */
	private Response getRestResponse(String url, String restAction)
			throws AFTException {
		boolean matchFound = false;
		boolean isCertificate = false;
		String element = "";
		String password = "";
		Response responseObj = null;
		String urlValue = "";
		String action = "";
		for (Map.Entry<String, List<String>> data : authenticationData
				.entrySet()) {
			if (data.getKey().equalsIgnoreCase(url + restAction)) {
				List<String> list = data.getValue();
				if (list != null && list.size() == 5) {
					urlValue = list.get(0);
					action = list.get(1);
					element = list.get(2);
					password = list.get(3);
					isCertificate = Boolean.parseBoolean(list.get(4));
				}
				matchFound = true;
				break;
			}
		}
		if (matchFound) {
			if (isCertificate) {
				// following code will do the certificate authentication
				// based on certificate path provided.
				RequestSpecification req = given().auth().certificate(element,
						password);
				responseObj = getResponse(req, action, urlValue);
			} else {
				// following code will do the basic authentication as
				// path not provided.
				RequestSpecification req = given().auth().basic(element,
						password);
				responseObj = getResponse(req, action, urlValue);
			}
		} else {
			// get the response as authentication data not provided.
			responseObj = getResponse(given(), restAction, url);
		}

		return responseObj;
	}

	/**
	 * set the response type either xml/json in system variable
	 * AFT_WSREQUEST_RESPONSE_TYPE
	 * 
	 * @param responseObj
	 *            responseObj
	 * @param url
	 *            url
	 * @throws AFTException
	 *             the application exception
	 */
	private void setResponse(Response responseObj, String url)
			throws AFTException {
		String responseType = null;
		// check if the response code is OK (200)
		if (responseObj.getStatusCode() != 200) {
			String errMsg = "Response from webservice ["
					+ url
					+ "] is ["
					+ responseObj.getStatusCode()
					+ "] (NOT SUCCESS)."
					+ "Please check the parameters passed and send the request again.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

		response = responseObj.asString();
		LOGGER.info("Response [" + response + "]");

		if (responseObj.getContentType().contains("json")) {
			responseType = Constants.JSON;
		} else if (responseObj.getContentType().contains("xml")) {
			responseType = Constants.XML;
		}

		// Set the response type to system variable system
		// AFT_WSREQUEST_RESPONSE_TYPE
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE), true,
				responseType);
	}

	/**
	 * Web services - send request.
	 * 
	 * @param requestSpec
	 *            requestSpec
	 * @param restAction
	 *            restAction
	 * @param url
	 *            url
	 * @return the response
	 * @throws AFTException
	 *             the application exception
	 */
	private Response getResponse(RequestSpecification requestSpec,
			String restAction, String url) throws AFTException {
		Response responseObj = null;
		try {
			if (request != null && !restAction.equalsIgnoreCase(Constants.GET)) {
				// if the user has specified request then set the request in
				// body
				LOGGER.info("Content type is :" + contentType);
				LOGGER.info("request :" + request);
				requestSpec.contentType(contentType);
				requestSpec.body(request);
			}
			if (restAction.equalsIgnoreCase(Constants.GET)) {
				responseObj = requestSpec.get(url).andReturn();
			} else if (restAction.equalsIgnoreCase(Constants.POST)) {
				responseObj = requestSpec.post(url).andReturn();
			} else if (restAction.equalsIgnoreCase(Constants.PUT)) {
				responseObj = requestSpec.put(url).andReturn();
			} else if (restAction.equalsIgnoreCase(Constants.DELETE)) {
				responseObj = requestSpec.delete(url).andReturn();
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return responseObj;
	}

	/**
	 * Web services - authenticate request.
	 * 
	 * @param element
	 *            the element
	 * @param value
	 *            the value
	 * @throws AFTException
	 *             the application exception
	 */
	public void wsAuthenticate(String element, String value)
			throws AFTException {
		List<String> list = null;
		String[] elements = value.split(",");
		boolean isCertificate = false;
		if (elements.length < 3) {
			LOGGER.error("User Credentials or end point url or rest action are not provided");
			throw new AFTException(
					"User Credentials or end point url or rest action are not provided");
		} else {
			if (elements.length > 3) {
				list = new ArrayList<String>();
				list.add(elements[0]);
				list.add(elements[1]);
				list.add(elements[2]);
				list.add(elements[3]);
				list.add(Boolean.valueOf(isCertificate).toString());
				// add the data to map
				authenticationData.put(elements[0] + elements[1], list);
			} else if (elements.length == 3
					&& !element.equalsIgnoreCase("novalue")
					&& !element.equals("")) {
				isCertificate = true;
				list = new ArrayList<String>();
				list.add(elements[0]);
				list.add(elements[1]);
				list.add(element);
				list.add(elements[2]);
				list.add(Boolean.valueOf(isCertificate).toString());
				// add the data to map
				authenticationData.put(elements[0] + elements[1], list);
			}
		}
	}

	/**
	 * Web services - validate response.
	 * 
	 * @param element
	 *            the element
	 * @param value
	 *            the value
	 * @param testStepRunner
	 *            test step runner object
	 * @return true, if successful
	 * @throws AFTException
	 *             the application exception
	 */
	public boolean wsValidateValue(String element, String value,
			TestStepRunner testStepRunner) throws AFTException {
		boolean validateResult = false;
		String eleValue = null;

		String responseType = Variable.getInstance().generateSysVarName(
				SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE);

		// get the actual value
		responseType = Helper.getInstance().getActionValue(responseType);

		if (xmlParserObj == null) {
			xmlParserObj = new XMLParser();
		}
		jsonParserObj = new JsonParser();
		String[] elements = value.split("\\" + Constants.ATTRIBUTESDELIMITER
				+ "\\" + Constants.ATTRIBUTESDELIMITER);
		if (elements.length > 1) {
			LOGGER.info("Looks like user had passed static response to validate");
			response = elements[0];
			if (StringUtils.isEmpty(responseType)) {
				if (response != null && response.startsWith("<?xml")) {
					responseType = Constants.XML;
				} else {
					responseType = Constants.JSON;
				}
			}
			eleValue = elements[1];
		} else {
			eleValue = value;
		}
		LOGGER.debug("Response to Validate [" + response + "]");
		// Set the XML response value to system variable system
		// AFT_XMLREQUEST_RESPONSE
		Variable.getInstance()
				.setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_WSREQUEST_RESPONSE), true,
						response);
		try {
			// Check if user has specified multiple attributes to validate or we
			// will be validating just one attribute/node
			if (element.contains(Constants.ATTRIBUTESDELIMITER)) {
				validateResult = validateMutlipleAttributes(element, eleValue,
						responseType, testStepRunner);
			} else {
				validateResult = validateAttribute(element, eleValue,
						responseType, testStepRunner);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return validateResult;
	}

	/**
	 * Checks whether the attributes are valid or not
	 * 
	 * @param element
	 *            element
	 * @param eleValue
	 *            eleValue
	 * @param responseType
	 *            responseType
	 * @param testStepRunner
	 *            testStepRunner
	 * @return boolean
	 * 
	 * @throws AFTException
	 *             the application exception
	 */

	private boolean validateMutlipleAttributes(String element, String eleValue,
			String responseType, TestStepRunner testStepRunner)
			throws AFTException {
		String[] elementsArray = null;
		String[] valueArray = null;
		String[] xPathArray = null;
		boolean validateResult = false;

		try {
			// split nodes/attributes specified by user
			elementsArray = element.trim().split(
					"\\" + Constants.ATTRIBUTESDELIMITER);
			// check if user specified any values to be within the xml
			if (!eleValue.equals("")) {
				valueArray = eleValue.split("\\"
						+ Constants.ATTRIBUTESDELIMITER);
			}
			for (int i = 0; i <= elementsArray.length - 1; i++) {
				if ((elementsArray[i].trim().startsWith(
						Constants.TESTDATASTARTVARIABLEIDENTIFIER) && elementsArray[i]
						.trim().endsWith(
								Constants.TESTDATAENDVARIABLEIDENTIFIER))
						|| (elementsArray[i].trim().startsWith(
								Constants.DYNAMICVARIABLEDELIMITER) && elementsArray[i]
								.trim().endsWith(
										Constants.DYNAMICVARIABLEDELIMITER))) {
					elementsArray[i] = Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(),
							elementsArray[i]);
				}
				xPathArray = elementsArray[i].trim().split("/");

				// Validate node existence in Response
				if (eleValue.equals("")) {
					validateNodeExistence(responseType, xPathArray);
				} else {
					validateResult = isValidValue(responseType, xPathArray,
							valueArray[i], elementsArray[i]);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return validateResult;
	}

	/**
	 * Checks whether the node exist or not
	 * 
	 * @param responseType
	 *            responseType
	 * @param xPathArray
	 *            xPathArray
	 * @return boolean
	 * @throws AFTException
	 *             the application exception
	 */
	private boolean validateNodeExistence(String responseType,
			String[] xPathArray) throws AFTException {
		boolean isNodeExists = false;
		boolean validateResult = false;
		try {
			if (responseType.equalsIgnoreCase(Constants.XML)) {
				response = getEnrichedResponseXML();
				isNodeExists = xmlParserObj.isAttributePresent(xPathArray[0],
						xPathArray[1], response);
			} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
				isNodeExists = jsonParserObj.isAttributePresent(response,
						xPathArray);
			}
			if (isNodeExists) {
				LOGGER.info("Verify: Success, Node/Attribute [" + xPathArray[0]
						+ "/" + xPathArray[1] + "] found");
				validateResult = true;
			} else {
				String errMsg = "Verify: Failure, Node/Attribute ["
						+ xPathArray[0] + "/" + xPathArray[1] + "not found";
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return validateResult;
	}

	/**
	 * Checks whether the element is valid or not
	 * 
	 * @param responseType
	 *            responseType
	 * @param xPathArray
	 *            xPathArray
	 * @param value
	 *            value
	 * @param element
	 *            element
	 * @return boolean
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	private boolean isValidValue(String responseType, String[] xPathArray,
			String value, String element) throws AFTException, SAXException,
			IOException, ParserConfigurationException {
		boolean isValid = false;
		boolean validateResult = false;
		Map<String, String> attributeValue = null;
		String attValue = null;
		if (responseType.equalsIgnoreCase(Constants.XML)) {
			response = getEnrichedResponseXML();
			attributeValue = xmlParserObj.getAttributeValue(response,
					xPathArray[0], xPathArray[1]);
			attValue = attributeValue.get(xPathArray[1]);
			if (attValue.equals(value)) {
				isValid = true;
			}
		} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
			attValue = jsonParserObj.getAttributeValue(response, xPathArray);
			if (attValue != null && attValue.equals(value)) {
				isValid = true;
			}
		}

		if (isValid) {
			LOGGER.info("Verify: Success, actual value is [" + attValue
					+ "], expected value is [" + value
					+ "] for attribute/element [" + element + "]");
			validateResult = true;

		} else {
			String errMsg = "Verify: Failed, actual value is [" + attValue
					+ "], expected value is [" + value
					+ "] for attribute/element [" + element + "]";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		return validateResult;
	}

	/**
	 * Checks whether the attributes are valid or not
	 * 
	 * @param element
	 *            element
	 * @param eleValue
	 *            eleValue
	 * @param responseType
	 *            responseType
	 * @param testStepRunner
	 *            testStepRunner
	 * @return boolean
	 * 
	 * @throws AFTException
	 *             the application exception
	 */

	private boolean validateAttribute(String element, String eleValue,
			String responseType, TestStepRunner testStepRunner)
			throws AFTException {
		String[] xPathArray = null;
		boolean validateResult = false;

		try {
			String elementValue = null;
			if ((element.trim().startsWith(
					Constants.TESTDATASTARTVARIABLEIDENTIFIER) && element
					.trim().endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER))
					|| (element.trim().startsWith(
							Constants.DYNAMICVARIABLEDELIMITER) && element
							.trim()
							.endsWith(Constants.DYNAMICVARIABLEDELIMITER))) {
				elementValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), element);
			} else {
				elementValue = element;
			}
			xPathArray = elementValue.trim().split("/");

			// Validate node existence in Response
			if (eleValue.equals("")) {
				boolean isNodeExists = false;
				if (responseType.equalsIgnoreCase(Constants.XML)) {
					response = getEnrichedResponseXML();
					// Set the target XML as request XML as parser class by
					// default searches the node
					isNodeExists = xmlParserObj.isAttributePresent(
							xPathArray[0], xPathArray[1], response);
				} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
					isNodeExists = jsonParserObj.isAttributePresent(response,
							xPathArray);
				}
				if (isNodeExists) {
					LOGGER.info("Verify: Success, Node/Attribute ["
							+ xPathArray[0] + "/" + xPathArray[1] + "found");
					validateResult = true;
				} else {
					String errMsg = "Verify: Failure, Node/Attribute ["
							+ xPathArray[0] + "/" + xPathArray[1] + "not found";
					throw new AFTException(errMsg);
				}

			} else {
				validateResult = isValidValue(responseType, xPathArray,
						eleValue, element);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return validateResult;

	}

	/**
	 * Web services - get response value.
	 * 
	 * @param element
	 *            the element
	 * @param value
	 *            the value
	 * @param testStepRunner
	 *            test step runner object
	 * @param commandFixture
	 *            CommandFixtures object
	 * @return XML Value as String
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	public String wsGetValue(String element, String value,
			TestStepRunner testStepRunner, CommandFixtures commandFixture)
			throws AFTException {
		String attrValue = "";
		String responseType = null;
		String eleValue = null;
		jsonParserObj = new JsonParser();

		responseType = Variable.getInstance().generateSysVarName(
				SystemVariables.AFT_WSREQUEST_RESPONSE_TYPE);

		// get the actual value
		responseType = Helper.getInstance().getActionValue(responseType);

		String[] elements = value.split(",");
		if (elements.length > 1) {
			LOGGER.info("Looks like user had passed static response to validate");
			response = elements[0];
			response = Helper.getInstance().getActionValue(
					testStepRunner.getTestSuiteRunner(), response);
			if (StringUtils.isEmpty(responseType)) {
				if (response != null && response.startsWith("<?xml")) {
					responseType = Constants.XML;
				} else {
					responseType = Constants.JSON;
				}
			}
			eleValue = elements[1];
		} else {
			eleValue = value;
		}
		LOGGER.debug("Response to Parse [" + response + "]");
		// Set the response value to system variable system
		// AFT_XMLREQUEST_RESPONSE
		Variable.getInstance()
				.setVariableValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_WSREQUEST_RESPONSE), true,
						response);

		try {
			if (element.contains(Constants.ATTRIBUTESDELIMITER)) {
				// copy the attributes data to user defined variables
				copyMutipleAttributeData(element, eleValue, responseType,
						testStepRunner, commandFixture);
			} else {
				// copy the attribute data to user defined variable
				copyAttributeData(element, eleValue, responseType,
						testStepRunner, commandFixture);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return attrValue;
	}

	/**
	 * This method will copy the multiple attribute data from response to a user
	 * defined variables.
	 * 
	 * @param element
	 *            the element
	 * @param eleValue
	 *            the eleValue
	 * @param responseType
	 *            response type
	 * @param testStepRunner
	 *            test step runner object
	 * @param commandFixture
	 *            CommandFixtures object
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	private void copyMutipleAttributeData(String element, String eleValue,
			String responseType, TestStepRunner testStepRunner,
			CommandFixtures commandFixture) throws AFTException {
		String[] elementsArray = null;
		String[] valueArray = null;
		String[] xPathArray = null;
		String attrValue = "";
		Map<String, String> attributeValue;

		try {
			elementsArray = element.trim().split(
					"\\" + Constants.ATTRIBUTESDELIMITER);
			valueArray = eleValue.split("\\" + Constants.ATTRIBUTESDELIMITER);

			if (elementsArray.length != valueArray.length) {
				String errMsg = "Size of the attribute list ["
						+ element
						+ "] to parse result and target variable/testdata identifier list ["
						+ eleValue
						+ "] do not match. These should match for parsing webservices response.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			for (int i = 0; i <= elementsArray.length - 1; i++) {
				if ((elementsArray[i].trim().startsWith(
						Constants.TESTDATASTARTVARIABLEIDENTIFIER) && elementsArray[i]
						.trim().endsWith(
								Constants.TESTDATAENDVARIABLEIDENTIFIER))
						|| (elementsArray[i].trim().startsWith(
								Constants.DYNAMICVARIABLEDELIMITER) && elementsArray[i]
								.trim().endsWith(
										Constants.DYNAMICVARIABLEDELIMITER))) {
					elementsArray[i] = Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(),
							elementsArray[i]);
				}
				xPathArray = elementsArray[i].trim().split("/");

				if (responseType.equalsIgnoreCase(Constants.XML)) {
					response = getEnrichedResponseXML();
					attributeValue = xmlParserObj.getAttributeValue(response,
							xPathArray[0], xPathArray[1]);
					attrValue = attributeValue.get(xPathArray[1]);
				} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
					attrValue = jsonParserObj.getAttributeValue(response,
							xPathArray);
				}

				commandFixture.copyData(testStepRunner, "[[" + attrValue
						+ "]]," + valueArray[i]);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

	}

	/**
	 * This method will copy the attribute data from response to a user defined
	 * variable.
	 * 
	 * @param element
	 *            the element
	 * @param eleValue
	 *            the eleValue
	 * @param responseType
	 *            response type
	 * @param testStepRunner
	 *            test step runner object
	 * @param commandFixture
	 *            CommandFixtures object
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	private void copyAttributeData(String element, String eleValue,
			String responseType, TestStepRunner testStepRunner,
			CommandFixtures commandFixture) throws AFTException {
		String elementValue = null;
		String[] xPathArray = null;
		String attrValue = "";
		Map<String, String> attributeValue;
		try {
			if ((element.trim().startsWith(
					Constants.TESTDATASTARTVARIABLEIDENTIFIER) && element
					.trim().endsWith(Constants.TESTDATAENDVARIABLEIDENTIFIER))
					|| (element.trim().startsWith(
							Constants.DYNAMICVARIABLEDELIMITER) && element
							.trim()
							.endsWith(Constants.DYNAMICVARIABLEDELIMITER))) {
				elementValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), element);
			} else {
				elementValue = element;
			}
			xPathArray = elementValue.trim().split("/");
			if (responseType.equalsIgnoreCase(Constants.XML)) {
				attributeValue = xmlParserObj.getAttributeValue(response,
						xPathArray[0], xPathArray[1]);
				attrValue = attributeValue.get(xPathArray[1]);
			} else if (responseType.equalsIgnoreCase(Constants.JSON)) {
				attrValue = jsonParserObj.getAttributeValue(response,
						xPathArray);
			}
			if (attrValue == null) {
				String errMsg = "Attribute value is [" + attrValue
						+ "] for attribute/element [" + elementValue + "]";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
			commandFixture.copyData(testStepRunner, "[[" + attrValue + "]],"
					+ eleValue);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Gets the enriched response xml.
	 * 
	 * @return the enriched response xml
	 */
	private String getEnrichedResponseXML() {
		// Code to handle CDATA content
		if (response.contains("&lt;")) {
			response = response.replaceAll("&lt;", "<");
			response = response.replaceAll("&gt;", ">");
			response = response.replaceAll("\\?", "~");
			response = response.replaceAll("<~xml version='1.0'~>", "");
			response = response.replaceAll("~", "\\?");
		}

		if (response.contains("xD;")) {
			response = response.replaceAll("\\?", "~");
			response = response.replaceAll("&~xD;", "");
			response = response.replaceAll("~", "\\?");
		}
		return response;
	}

	/**
	 * Sets the https connection.
	 * 
	 * @param httpsUrl
	 *            the new https connection
	 * @throws AFTException
	 *             the exception
	 */
	public void setHttpsConnection(String httpsUrl) throws AFTException {

		try {
			SSLContext sslCtx = SSLContext.getInstance("SSL");
			LOGGER.debug("SSL Context Object [" + sslCtx + "]");
			sslCtx.init(new KeyManager[0],
					new TrustManager[] { new DefaultTrustManager() },
					new SecureRandom());
			SSLContext.setDefault(sslCtx);
			URL url = new URL(httpsUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * The Class DefaultTrustManager.
	 */
	private static class DefaultTrustManager implements X509TrustManager {

		/**
		 * interface method declaraion.
		 * 
		 * @param arg0
		 *            arg0
		 * @param arg1
		 *            arg1
		 */
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		/**
		 * interface method declaraion.
		 * 
		 * @param arg0
		 *            arg0
		 * @param arg1
		 *            arg1
		 */
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		/**
		 * interface method declaraion.
		 * 
		 * @return accepted users
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
