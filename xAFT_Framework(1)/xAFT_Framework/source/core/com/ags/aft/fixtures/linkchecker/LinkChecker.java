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
 * Class: LinkCheker
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */
package com.ags.aft.fixtures.linkchecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;
import com.ags.aft.runners.TestStepRunner;

/**
 * Example program to list links from a URL.
 */
public final class LinkChecker {

	/** The Constant LOG. */
	static final Logger LOGGER = Logger.getLogger(LinkChecker.class);

	/** The _root page. */
	private PageLink rootPage;
	
	/** The url. */
	private String url;

	/**
	 * Gets theurl.
	 * 
	 * @return theurl
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * set the url
	 * 
	 * @param url
	 *            url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/** The link checker. */
	private static LinkChecker linkChecker;

	/** The broken link list. */
	private List<String> brokenLinkList = new ArrayList<String>();;

	/** The good link count. */
	private int goodLinkCount = 0;

	/** The broken link count. */
	private int brokenLinkCount = 0;

	private int poolSize = 0;

	/**
	 * Gets the good link count.
	 * 
	 * @return the good link count
	 */
	public int getGoodLinkCount() {
		return goodLinkCount;
	}

	/**
	 * Gets the broken link count.
	 * 
	 * @return the broken link count
	 */
	public int getBrokenLinkCount() {
		return brokenLinkCount;
	}

	/**
	 * Gets the broken link list.
	 * 
	 * @return the broken link list
	 */
	public String getBrokenLinkList() {

		// return brokenLinkList;

		Iterator<String> brokenLinkItr = brokenLinkList.iterator();
		if (!brokenLinkItr.hasNext()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Broken links found on page: " + rootPage.getURL());
		for (int i = 0;; i++) {
			String e = brokenLinkItr.next();
			sb.append("\n");
			sb.append("      " + e);
			// we want to report only top 5 broken links for each page and rest
			// should be scanned in logs
			if (!brokenLinkItr.hasNext() || (i >= 4)) {
				if (brokenLinkItr.hasNext()) {
					sb.append("      ...");
				}
				return sb.toString();
			}
		}
	}

	/**
	 * Gets the single instance of LinkChecker.
	 * 
	 * @return single instance of LinkChecker
	 */
	public static LinkChecker getInstance() {
		if (linkChecker == null) {
			linkChecker = new LinkChecker();
			return linkChecker;
		}
		return linkChecker;
	}

	/**
	 * Instantiates a new link checker.
	 */
	private LinkChecker() {
	}

	/**
	 * validates the links.
	 * @param testStepRunner
	 *            testStepRunner
	 * @param rootURL
	 *            the rootURL
	 * @throws AFTException
	 */
	public void validateLinks(TestStepRunner testStepRunner,final String rootURL) throws AFTException {
		try {
			long startTime;
			long scanTime;
			setUrl(rootURL);
			String urlToVerify = validateURL(rootURL);
			rootPage = new PageLink(testStepRunner,urlToVerify, "Root", "Root");
			startTime = System.currentTimeMillis();
			final Map<String, PageLink> pageLinks = getLinksNodes(testStepRunner,rootPage);
//			HashMap<String, String> responseMap = new HashMap<String, String>();

			poolSize = Integer.parseInt(ConfigProperties.getInstance()
					.getConfigProperty(
							ConfigProperties.LINK_CHECKER_THREAD_COUNT));
			
			checkLinks(pageLinks);

			scanTime = (System.currentTimeMillis() - startTime);
			LOGGER.debug("Scan duration: " + scanTime + " ms with " + poolSize
					+ " threads.");
			// printResult(pageLinks);
			getLinkCount(pageLinks);
			//If Broken Link found
			/*if (!rootPage.isGood()) {
				String nodeName = "";
				
				String responseStatus = rootPage.getCurrentBrokenLinkResponse();
				if (responseStatus != null) {
					if (responseStatus.contains("HTTP/1.1")) {
						nodeName = "Error" + responseStatus.substring(8, 3);
					} else if (responseStatus.contains("Link failed")) {
						nodeName = "Exceptions";
					} else {
						nodeName = "Miscelleneous";
					}
					//Need to remove the hard coded value once the file name & folder is confirmed
					XMLParser xmlParser = new XMLParser(
							"C:\\Users\\kkanumuri\\Desktop\\Sample.xml");
					xmlParser.createDocument();
					xmlParser.createRootElement("LinkErrors");
					// Create node
					node = xmlParser.createNode(nodeName);
					responseMap.put("Page", rootURL);
					responseMap.put("FailedLink",
							rootPage.getCurrentBrokenLink());

					xmlParser.createAttributes(responseMap);
					//generate the XML
					xmlParser.generateXML();
				}

			}*/

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Validate url.
	 * 
	 * @param rootURL
	 *            the root url
	 * @return the string
	 * @throws AFTException
	 */
	private static String validateURL(String rootURL) throws AFTException {
		String rooturl = rootURL;
		if (rooturl == null || rooturl.trim().length() < 1) {
			return null;
		}
		try {
			// If user did not specify http, prefix the protocol
			if (!rooturl.startsWith("http")) {
				LOGGER.info("User has not specified the http prefix for URL");
				LOGGER.info("Adding prefix to the url " + rooturl);
				rooturl = "http://" + rooturl;
			}
			final URL userURL = new URL(rooturl);
			userURL.getContent();
			return userURL.toString();
		} catch (MalformedURLException exp) {
			LOGGER.error("Invalid URL entered. Message: " + exp.getMessage());
			throw new AFTException(exp);
		} catch (IOException exp) {
			LOGGER.error("Exception::", exp);
			throw new AFTException(exp);
		}
	}

	/**
	 * Gets the links nodes.
	 * @param testStepRunner
	 *            testStepRunner
	 * @param checkPage
	 *            the check page
	 * @return the links nodes
	 * @throws AFTException
	 */
	private Map<String, PageLink> getLinksNodes(TestStepRunner testStepRunner,final PageLink checkPage)
			throws AFTException {
		final long sTime = System.currentTimeMillis();
		Map<String, PageLink> pageLinks = null;
		try {
			LOGGER.info("Scanning page: " + checkPage.getURL());
			final Connection conn = Jsoup.connect(checkPage.getURL());
			final Document doc = conn.get();
			final Elements links = doc.select("a[href]");
			final Elements media = doc.select("[src]");
			final Elements imports = doc.select("link[href]");
			LOGGER.info("Page contains: Links: " + links.size() + ", Media: "
					+ media.size() + ", " + "Imports: " + imports.size());
			pageLinks = new HashMap<String, PageLink>(links.size()
					+ media.size() + imports.size());

			pageLinks = appendElements(testStepRunner,pageLinks, links, "abs:href");
			pageLinks = appendElements(testStepRunner,pageLinks, media, "abs:src");
			pageLinks = appendElements(testStepRunner,pageLinks, imports, "abs:href");

			LOGGER.debug("Root page scan took "
					+ (System.currentTimeMillis() - sTime) + " ms");
			return pageLinks;
		} catch (Exception exp) {
			LOGGER.error("Exception::", exp);
			throw new AFTException(exp);
		}
	}

	/**
	 * Check links.
	 * 
	 * @param pageLinks
	 *            the page links
	 */
	private void checkLinks(final Map<String, PageLink> pageLinks) {

		if (null == pageLinks || pageLinks.size() < 1) {
			LOGGER.warn("No pageLinks found on the page");
			// throw new Exception();
			return;
		}

		/*
		 * for (org.open.pagehealth.PageLink node : pageLinks) { Thread th = new
		 * Thread(new org.open.pagehealth.ClickThread(node)); th.start(); }
		 */

		final BlockingQueue<Runnable> worksQueue = new ArrayBlockingQueue<Runnable>(
				pageLinks.size());
		final RejectedExecutionHandler executionHandler = new TaskOverflowHandler();
		LOGGER.trace("Starting Pool: Threads: " + poolSize);
		// Create the ThreadPoolExecutor
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize,
				poolSize, 3, TimeUnit.SECONDS, worksQueue, executionHandler);
		executor.allowCoreThreadTimeOut(true);

		final Object callback = new Object();

		// Starting the monitor thread as a daemon
		final Thread monitor = new Thread(new TasksMonitorThread(executor,
				callback), "TasksMonitorThread");
		monitor.setDaemon(true);
		monitor.start();
		final Iterator<PageLink> pageLinkIterator = pageLinks.values()
				.iterator();
		int counter = 1;
		// Adding the tasks
		while (pageLinkIterator.hasNext()) {
			final PageLink pLink = pageLinkIterator.next();
			executor.execute(new ClickThread("" + counter++, pLink));
		}
		try {
			synchronized (callback) {
				LOGGER.trace("Going to sleep until all tasks are complete");
				callback.wait();
			}
		} catch (Exception exp) {
			LOGGER.error("Exception in wait", exp);
		}
		executor.shutdown();
		LOGGER.debug("Is pool stopped: " + executor.isShutdown());
		LOGGER.debug("Pool shutdown.");
	}

	/**
	 * Gets the link count.
	 * 
	 * @param pageLinks
	 *            the page links
	 * @throws AFTException
	 */
	private void getLinkCount(final Map<String, PageLink> pageLinks)
			throws AFTException {
		try {

			// Iterate and get the Good & Broken Link Count
			for (final PageLink page : pageLinks.values()) {
				if (page.isGood()) {
					goodLinkCount++;
				} else if (!page.isGood()) {
					brokenLinkCount++;
					brokenLinkList.add(page.getURL());
				}
			}

		} catch (Exception exp) {
			LOGGER.error("Exception::", exp);
			throw new AFTException(exp);
		}
	}

	/**
	 * Append elements.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @param pageLinkList
	 *            the page link list
	 * @param elem
	 *            the elem
	 * @param attrKey
	 *            the attr key
	 * @return the hash map
	 */
	private Map<String, PageLink> appendElements(TestStepRunner testStepRunner,
			final Map<String, PageLink> pageLinkList, final Elements elem,
			final String attrKey) {
		for (final Element pageElement : elem) {
			final String linkTarget = pageElement.attr(attrKey);
			if (linkTarget == null || linkTarget.trim().length() < 1) {
				final String href = pageElement.attr("href");
				if (null != href && href.startsWith("javascript:")) {
					LOGGER.info("Skipping javascript link: " + pageElement);
					continue;
				}
				LOGGER.error("Empty link found" + pageElement);
			}
			if (linkTarget.startsWith("mailto")) {
				LOGGER.debug("ignoring mailto link: " + pageElement);
				continue;
			}
			final String caption = pageElement.hasText() ? pageElement.text()
					: pageElement.attr("alt");
			pageLinkList.put(linkTarget,
					new PageLink(testStepRunner,linkTarget, trim(caption, 35), pageElement
							.tag().toString()));
			// print(" * a: <%s>  (%s)", li, trim(link.text(), 35));
		}
		return pageLinkList;
	}

	/**
	 * trim method implementation.
	 * 
	 * @param s
	 *        value to be trimmed
	 * @param width
	 *            the width
	 * @return the string
	 */
	private String trim(final String s, final int width) {
		if (s.length() > width) {
			return s.substring(0, width - 1) + ".";
		} else {
			return s;
		}
	}
}
