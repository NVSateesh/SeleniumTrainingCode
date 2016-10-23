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
 * Class: ApplicationException
 * 
 * Purpose: This class implements customized exceptions for AFT
 */

package com.ags.aft.exception;

/**
 * Class for displaying user-friendly custom exceptions.
 * 
 */
public class AFTException extends Exception {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The error message. */
	private String errorMessage = null;

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Creates an instance of the class.
	 * 
	 * @param page
	 *          the page
	 * @param object
	 *           the object
	 */
	public AFTException(String page, String object) {
		super();
		StringBuffer message = new StringBuffer();
		try {
			if (page != null && !page.equals("") && object != null
					&& !object.equals("")) {
				String key = page.toUpperCase() + "." + object.toUpperCase();
				message.append(key);
			}
		} catch (Exception e) {
			message.append(e.getMessage());
		}

		this.errorMessage = message.toString();
	}

	/**
	 * Instantiates a new aFT exception.
	 *
	 * @param message the message
	 */
	public AFTException(String message) {
		super(message);
		errorMessage = message;
	}

	/**
	 * Instantiates a new aFT exception.
	 *
	 * @param e the e
	 */
	public AFTException(Throwable e) {
		super(e.getLocalizedMessage().split("\n")[0]);
		errorMessage = e.getMessage();
	}
}
