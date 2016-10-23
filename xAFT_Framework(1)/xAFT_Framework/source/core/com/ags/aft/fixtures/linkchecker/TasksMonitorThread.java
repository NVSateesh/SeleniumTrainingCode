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
 * Class: TasksMonitorThread
 * 
 * Purpose: This class implements ITestRunner to execute one test step.
 * Instantiated and called by TestSuiteRunner
 */
package com.ags.aft.fixtures.linkchecker;

import org.apache.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * My monitor thread. To monitor the status of {@link ThreadPoolExecutor} and
 * its status.
 */
public class TasksMonitorThread implements Runnable {
	private static final Logger LOGGER = Logger
			.getLogger(TasksMonitorThread.class);
	private ThreadPoolExecutor executor;
	private Object callBack;
	/**
	 * Constructor for TasksMonitorThread
	 * @param executor
	 *          executor
	 * @param callBack
	 *           callBack         
	 */
	public TasksMonitorThread(final ThreadPoolExecutor executor,
			final Object callBack) {
		this.executor = executor;
		this.callBack = callBack;
	}
	/**
	 * run method implementation
	 */
	public void run() {
		try {
			do {
				LOGGER.info(String
						.format("[monitor] ActiveThreads: %d, TotalThreads: %d, CompletedTasks: %d, TotalTasks: %d",
								this.executor.getActiveCount(),
								this.executor.getCorePoolSize(),
								this.executor.getCompletedTaskCount(),
								this.executor.getTaskCount()));

				if (this.executor.getTaskCount() > 0
						&& this.executor.getActiveCount() == 0
						&& this.executor.getTaskCount() == this.executor
								.getCompletedTaskCount()) {
					LOGGER.debug("Task queue is empty. Lets shutdown.");
					synchronized (callBack) {
						callBack.notify();
					}
					return;
				}

				Thread.sleep(3000);
			} while (true);
		} catch (Exception e) {
			LOGGER.error("Exception", e);
		}
	}
}
