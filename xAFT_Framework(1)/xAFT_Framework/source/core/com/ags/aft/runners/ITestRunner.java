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
 * Interface: ITestRunner
 * 
 * Purpose: This Interface class for implementation of TestRunners for Test
 * Case, Test Suite and Test Batch which initialize system variables related to
 * test batch/test suite/test step before execution by runner
 */

package com.ags.aft.runners;

import com.ags.aft.exception.AFTException;

/**
 * Interface class for implementation of TestRunners
 * 
 */
public interface ITestRunner {
	/**
	 * execute method to execute the runner
	 * 
	 * @throws AFTException
	 */
	void execute() throws AFTException;

	/**
	 * Initialize system variables related to test batch/test suite/test step
	 * before execution by runner
	 * 
	 * @throws AFTException
	 */
	void initSystemVariables() throws AFTException;
}
