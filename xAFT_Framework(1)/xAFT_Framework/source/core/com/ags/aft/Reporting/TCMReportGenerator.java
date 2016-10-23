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
 * Class: Generator
 * 
 * Purpose: This is utility class to generate reports for the Testsuite result
 */

package com.ags.aft.Reporting;

import org.apache.log4j.Logger;
import com.ags.aft.testObjects.TestSuite;

/**
 * Utility class to generate reports for the Testsuite result.
 * 
 */
public final class TCMReportGenerator {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(TCMReportGenerator.class);

	private static TCMReportGenerator tcmReportGenerator = null;
	private TestSuite testSuite = null;
	private String startTime = null;
	private String endTime = null;

	/**
	 * Constructs a new ReportGenerator instance.
	 */
	private TCMReportGenerator() {
		LOGGER.debug("Creating new TCMReportGenerator object for reporting");
	}

	/**
	 * Method to return ReportGenerator singleton instance.
	 * 
	 * @return ReportGenerator instance.
	 */
	public static TCMReportGenerator getInstance() {
		if (tcmReportGenerator == null) {
			tcmReportGenerator = new TCMReportGenerator();
		}
		return tcmReportGenerator;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param ts
	 *            set test suite reporting object
	 */
	public void setTestSuite(TestSuite ts) {
		LOGGER.debug("Adding a new testSuite [" + ts.getTestSuiteName()
				+ "] for reporting");
		testSuite = ts;
	}

	/**
	 * @return TestSuite reporting object
	 */
	public TestSuite getTestSuite() {
		return testSuite;
	}

}
