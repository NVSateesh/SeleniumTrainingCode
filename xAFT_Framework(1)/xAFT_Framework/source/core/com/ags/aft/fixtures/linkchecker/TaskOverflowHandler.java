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
 * Class: TaskOverflowHandler
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */

package com.ags.aft.fixtures.linkchecker;

import org.apache.log4j.Logger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The custom {@link java.util.concurrent.RejectedExecutionHandler} to handle
 * the rejected tasks / {@link Runnable}
 */
public class TaskOverflowHandler implements RejectedExecutionHandler {
	private static final Logger LOG = Logger
			.getLogger(TaskOverflowHandler.class);

	/**
	 * This method will reject the execution.
	 * 
	 * @param runnable
	 *            the runnable
	 * @param executor
	 *            the executor
	 */
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
		LOG.error(runnable.toString() + " : I've been rejected ! ");
	}
}
