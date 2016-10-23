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
 * Class: PageLink
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */
package com.ags.aft.fixtures.linkchecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.ags.aft.Reporting.PageErrors;
import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * The Class PageLink.
 */
public class PageLink {

	/** The SCHEME. */
	private static Scheme scheme;

	/** The Constant LOG. */
	private static final Logger LOGGER = Logger.getLogger(PageLink.class);

	/** The _link. */
	private String link;

	/** The _caption. */
	private String caption;

	/** The _is good. */
	private boolean isGood;

	/** The _response status. */
	private String responseStatus;

	/** The _is verified. */
	private boolean isVerified;

	/** The _content type. */
	private String contentType;

	/** The _content length. */
	private long contentLength = -1;

	/** The _scan time. */
	private long scanTime;

	/** The _thread. */
	private Thread thread;

	/** The _type. */
	private String type;

	private String currentBrokenLink;

	private String currentBrokenLinkResponse;
	
	/**test step runner**/
	private TestStepRunner testStepRunner;

//	private org.w3c.dom.Element node;
//
//	private HashMap<String, String> responseMap = new HashMap<String, String>();;

	/**
	 * @return currentBrokenLinkResponse
	 *            currentBrokenLinkResponse
	 */
	public String getCurrentBrokenLinkResponse() {
		return currentBrokenLinkResponse;
	}

	/**
	 * @param currentBrokenLinkResponse
	 *            currentBrokenLinkResponse
	 */
	public void setCurrentBrokenLinkResponse(String currentBrokenLinkResponse) {
		this.currentBrokenLinkResponse = currentBrokenLinkResponse;
	}

	/**
	 * @return currentBrokenLink
	 *            currentBrokenLink
	 */
	public String getCurrentBrokenLink() {
		return currentBrokenLink;
	}
	/**
	 * @param currentBrokenLink
	 *            currentBrokenLink
	 */
	public void setCurrentBrokenLink(String currentBrokenLink) {
		this.currentBrokenLink = currentBrokenLink;
	}

	/**
	 * Instantiates a new page link.
	 * 
	 * @param testStepRunner
	 *            the testStepRunner
	 * @param plink
	 *            the plink
	 * @param pcaption
	 *            the pcaption
	 * @param ptype
	 *            the ptype
	 */
	public PageLink(TestStepRunner testStepRunner,final String plink, final String pcaption,
			final String ptype) {
		link = plink;
		caption = pcaption;
		type = ptype;
		this.testStepRunner=testStepRunner;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the uRL.
	 * 
	 * @return the uRL
	 */
	public String getURL() {
		return link;
	}

	/**
	 * Gets the response status.
	 * 
	 * @return the response status
	 */
	public String getResponseStatus() {
		return responseStatus;
	}

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType == null ? "" : contentType;
	}

	/**
	 * Gets the content length.
	 * 
	 * @return the content length
	 */
	public String getContentLength() {
		return -1 == contentLength ? "" : "" + contentLength;
	}

	/**
	 * Gets the scan time.
	 * 
	 * @return the scan time
	 */
	public String getScanTime() {
		return "" + scanTime;
	}

	/**
	 * Gets the verified thread.
	 * 
	 * @return the verified thread
	 */
	public String getVerifiedThread() {
		return thread == null ? "" : thread.getName();
	}

	/**
	 * Gets the caption.
	 * 
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Checks if is good.
	 * 
	 * @return true, if is good
	 */
	public boolean isGood() {
		return isGood;
	}

	/**
	 * Sets the result.
	 * 
	 * @param pisGood
	 *            the is good
	 * @param perrorMsg
	 *            the error msg
	 */
	private void setResult(final boolean pisGood, final String perrorMsg) {
		isVerified = true;
		isGood = pisGood;
		responseStatus = perrorMsg;
		
		if (!isGood) {
			
			LOGGER.warn("Broken link [" + getURL() + "], response status ["
					+ getResponseStatus() + "]");
			
			try {
				//Get the current page
				String page =  LinkChecker.getInstance().getUrl();
				//Get the broken link
				String brokenLink = getURL();
				//Get the broken link response
				String brokenLinkResponse = getResponseStatus();
				Log4JPlugin.getInstance().writeLinkErrors(
						getURL() + ",  response status [" + getResponseStatus()	+ "] \n");
				Map<String, String> rMap = new HashMap<String, String>();
				rMap.put(brokenLink, brokenLinkResponse);
				Map<String, Map<String, String>> tMap = new HashMap<String, Map<String,String>>();
				tMap.put(page, rMap);
				
				//For UI
				String testSetName = testStepRunner.getTestSuiteRunner().getTestBatchRunner().getTestSetName();
			/*	if (!PageErrors.getInstance().getLinkErrorsTestSets()
						.contains(testSetName)) {
					PageErrors.getInstance().setLinkErrorsTestSets(testSetName);
					
				}*/
				if (!Helper.getInstance().isFileSystemRequest()) {
					PageErrors.getInstance().setIdProject(
							Integer.parseInt(testStepRunner
									.getTestSuiteRunner().getTestSet()
									.getIdProject()));
				}
				PageErrors.getInstance().setLinkErrorsURL(page);
				PageErrors.getInstance().setLinkBrokenErrors(rMap);
				PageErrors.getInstance().setLinkErrorsTestSetNames(testSetName);
				PageErrors.getInstance().setLinkErrorsTestScenarioId(Helper.getInstance().getActionValue(Variable.getInstance().generateSysVarName(SystemVariables.AFT_CURBUSINESSSCENARIOID)));
				PageErrors.getInstance().setReportTestSuiteId(
						ReportGenerator.getInstance().getLatestTestSuite()
								.getIdReportTestSuite());
	
			} catch (AFTException ae) {
				// ignore the exception msg
				LOGGER.error("Failed to set the Broken status of the link");
			}
		}
	}

	/**
	 * Sets the broken.
	 * 
	 * @param perrorMsg
	 *            the new broken
	 */
	private void setBroken(final String perrorMsg) {
		setResult(false, perrorMsg);
	}

	/**
	 * Sets the good.
	 * 
	 * @param presponse
	 *            the response
	 * @param pcontentType
	 *            the content type
	 * @param pcontentLength
	 *            the content length
	 */
	private void setGood(final String presponse, final String pcontentType,
			final long pcontentLength) {
		setResult(true, presponse);
		contentLength = pcontentLength;
		contentType = pcontentType;
	}

	/**
	 * Check link.
	 * 
	 * @throws AFTException
	 */
	public void checkLink() throws AFTException {
		thread = Thread.currentThread();
		LOGGER.debug("Checking _link: " + link);
		if (null == link || link.trim().length() < 1) {
			setBroken("Empty Link");
			return;
		}

		final HttpResponse response;
		long startTime = System.currentTimeMillis();
		boolean deepScan = false;

		try {
			// Commented as this is trimming the actual URL
			// _link = encodeQuery(_link);
			final HttpUriRequest method;
			if (!deepScan) {
				LOGGER.trace("Performing Quick Scan of the URL");
				method = new HttpHead(link);
			} else {
				// deep check by downloading complete page
				LOGGER.trace("Performing Deep Scan of the URL");
				method = new HttpGet(link);
			}
			String userAgent = ConfigProperties
					.getInstance()
					.getConfigProperty(ConfigProperties.LINK_CHECKER_USER_AGENT);
			method.addHeader(HttpHeaders.USER_AGENT, userAgent);
			final HttpClient client = getHttpClient();
			startTime = System.currentTimeMillis();
			response = client.execute(method);
			scanTime = (System.currentTimeMillis() - startTime);
			final StatusLine pline = response.getStatusLine();
			if (pline.getStatusCode() == 404) {
				setBroken(pline.toString());
			} else {
				String cType = "";
				final Header pcontentTypeHeader = response
						.getLastHeader(HttpHeaders.CONTENT_TYPE);
				if (null != pcontentTypeHeader
						&& pcontentTypeHeader.getValue() != null) {
					cType = pcontentTypeHeader.getValue();
				}
				final Header plengthHeader = response
						.getLastHeader(HttpHeaders.CONTENT_LENGTH);
				long pcontentLengh = 0;
				if (null != plengthHeader) {
					pcontentLengh = Long.parseLong(plengthHeader.getValue());
				}

				setGood(pline.toString(), cType, pcontentLengh);
			}
		} catch (Exception exp) {
			scanTime = (System.currentTimeMillis() - startTime);
			String msg = "Link failed: " + caption + " via " + link
					+ ", Exception: " + exp;
			if (exp.getCause() != null) {
				msg = msg + ", Cause: " + exp.getCause().getMessage();
			}
			LOGGER.error(msg, exp);
			setBroken(msg);
		}
	}

	/**
	 * toString implementation method
	 * @return String
	 */
	@Override
	public String toString() {
		return "org.open.pagehealth.PageLink{" + "_link='" + link + '\''
				+ ", _caption='" + caption + '\'' + ", _isGood=" + isGood
				+ ", _responseStatus='" + responseStatus + '\''
				+ ", _isVerified=" + isVerified + ", _contentType='"
				+ contentType + '\'' + ", _contentLength=" + contentLength
				+ '}';
	}

	/**
	 * Gets the display status.
	 * 
	 * @return the display status
	 */
	public String getDisplayStatus() {
		return isGood ? "Good" : "Broken";
	}

	/**
	 * Gets the display verified status.
	 * 
	 * @return the display verified status
	 */
	public String getDisplayVerifiedStatus() {
		return isVerified ? "Yes" : "No";
	}

	/**
	 * Gets the trust all scheme.
	 * 
	 * @return the trust all scheme
	 * @throws AFTException
	 */
	private static Scheme getTrustAllScheme() throws AFTException {
		try {
			// Now you are telling the JRE to trust any https server.
			// If you know the URL that you are connecting to then this should
			// not be a problem
			// trustAllHttpsCertificates
			// Create a trust manager that does not validate certificate chains:
			final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
			final javax.net.ssl.TrustManager tm = new EnvTrustManager();
			trustAllCerts[0] = tm;
			final javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
					.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
/*			final org.apache.http.conn.ssl.SSLSocketFactory sf = new org.apache.http.conn.ssl.SSLSocketFactory(
					sc,
					org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// create scheme for apache HttpClient
			scheme = new Scheme("https", 443, sf);
*/
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
					.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(final String purlHostName,
								final SSLSession session) {
							LOGGER.warn("Warning: URL Host: " + purlHostName
									+ " vs. " + session.getPeerHost());
							return true;
						}
					});

			// trust all HttpUnit Connection
			com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl
					.setDefaultHostnameVerifier(new com.sun.net.ssl.HostnameVerifier() {
						public boolean verify(final String urlHostname,
								final String certHostname) {
							return true;
						}
					});

		} catch (Exception exp) {
			LOGGER.error("Exception::", exp);
			throw new AFTException(exp);
		}
		return scheme;
	}

	/**
	 * Gets the http client.
	 * 
	 * @return the http client
	 * @throws AFTException
	 * @throws NumberFormatException
	 */
	public static DefaultHttpClient getHttpClient()
			throws AFTException {
		if (null == scheme) {
			scheme = getTrustAllScheme();
		}
		int connTimeOut = Integer
				.parseInt(ConfigProperties.getInstance().getConfigProperty(
						ConfigProperties.LINK_CHECKER_PAGE_TIME_OUT));
		final HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				connTimeOut);
		String proxy = ConfigProperties.getInstance().getConfigProperty(
				ConfigProperties.LINK_CHECKER_PROXY_PATH);
		if (!proxy.equals("")) {
			final HttpHost proxyHost = new HttpHost(proxy.substring(0,
					proxy.indexOf(':')), Integer.parseInt(proxy.substring(proxy
					.indexOf(':') + 1)));
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
		}

		// HttpClientParams.setRedirecting(params, false);
		final DefaultHttpClient client = new DefaultHttpClient(params);
		client.getConnectionManager().getSchemeRegistry().register(scheme);

		return client;
	}

	// Just add these two functions in your program

	/**
	 * The Class EnvTrustManager.
	 */
	public static class EnvTrustManager implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {


		/**
		 * Gets the accepted issuers
		 * 
		 * @return null
		 */
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		/**
		 * Checks if is server trusted.
		 * 
		 * @param certs
		 *            the certs
		 * @return true, if is server trusted
		 */
		public boolean isServerTrusted(
				final java.security.cert.X509Certificate[] certs) {
			return true;
		}

		/**
		 * Checks if is client trusted.
		 * 
		 * @param certs
		 *            the certs
		 * @return true, if is client trusted
		 */
		public boolean isClientTrusted(
				final java.security.cert.X509Certificate[] certs) {
			return true;
		}

		/**
		 * Checks if is server trusted.
		 * 
		 * @param certs
		 *            the certs
		 * @param pauthType
		 *            pauthType
		 * @throws CertificateException
		 * 
		 */
		public void checkServerTrusted(
				final java.security.cert.X509Certificate[] certs,
				final String pauthType)
				throws java.security.cert.CertificateException {
			return;
		}

		/**
		 * Checks if is clinet trusted.
		 * 
		 * @param certs
		 *            the certs
		 * @param pauthType
		 *            pauthType
		 * @throws CertificateException
		 * 
		 */
		public void checkClientTrusted(
				final java.security.cert.X509Certificate[] certs,
				final String pauthType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

	/**
	 * Encode query.
	 * 
	 * @param unEscaped
	 *            the un escaped
	 * @return the string
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	public static String encodeQuery(final String unEscaped)
			throws URISyntaxException {
		final int mark = unEscaped.indexOf('?');
		if (mark == -1) {
			return unEscaped;
		}
		final String[] parts = unEscaped.split(":");
		return new URI(parts[0], parts[1], null).toString();
	}

}
