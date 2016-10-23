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
 * Class: ClickThread
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */
package com.ags.aft.fixtures.linkchecker;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;

/**
 * The Class ClickThread.
 */
public class ClickThread implements Runnable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ClickThread.class);

	/** The _ page_link. */
	private final PageLink pageLink;

	/** The _name. */
	private final String name;

	/**
	 * Instantiates a new click thread.
	 * 
	 * @param name
	 *            the name
	 * @param pageLink
	 *            the page link
	 */
	public ClickThread(final String name, final PageLink pageLink) {
		this.name = name;
		this.pageLink = pageLink;
	}


	/**
	 * run method implementation.
	 * 
	 */
	public void run() {
		LOGGER.trace(this.name + " : I'm running ! ");
		try {
			pageLink.checkLink();
		} catch (AFTException e) {
			LOGGER.error("Exception::", e);
		}
		LOGGER.trace(this.name + " : I'm done ! ");
	}
}
