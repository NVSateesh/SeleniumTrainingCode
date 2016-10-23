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
 * Class: Helper
 * 
 * Purpose: This class contains utility methods to read configuration files,
 * validate test data header and send email notifications etc
 */

package com.ags.aft.common;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Logger;

/**
 * The Class AftHelper.
 */
public final class Util {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(Util.class);

	/** The aft helper. */
	private static Util aftUtil;

	private static final String EMPTY_VALUE = "novalue";

	/**
	 * Instantiates a new Util.
	 */
	private Util() {
		super();
	}

	/**
	 * Gets the single instance of Util.
	 * 
	 * @return single instance of Util
	 */
	public static Util getInstance() {
		if (aftUtil == null) {
			aftUtil = new Util();
			LOGGER.trace("Creating instance of Util");
		}

		return aftUtil;
	}

	/**
	 * Method to check for EMPTY_VALUE in test suite and replace it with "".
	 * 
	 * @param sValue
	 *            the s value
	 * @return the string
	 */
	public String checkForEmptyValue(String sValue) {
		String value = sValue;
		try {
			if (value == null) {
				value = "";
			}
			if (value.equalsIgnoreCase(EMPTY_VALUE)) {
				value = "";
			}
		} catch (Exception e) {
			LOGGER.warn("Exception::", e);
		}

		return value;
	}

	/**
	 * if string value found "null" replace with "" ,else return string actual
	 * value.
	 * 
	 * @param s
	 *            the s
	 * @return :string value
	 */
	public String replaceNull(String s) {
		return (s == null) ? "" : s.trim();
	}

	/**
	 * function to Delete a specified file.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public void deleteFile(String fileName) {
		// A File object to represent the filename
		File f = new File(fileName);

		// Make sure the file or directory exists and isn't write protected
		if (f.exists()) {
			if (!f.canWrite()) {
				throw new IllegalArgumentException("Delete: write protected: "
						+ fileName);
			}

			// If it is a directory, make sure it is empty
			if (f.isDirectory()) {
				File[] files = f.listFiles();

				for (int i = 0; i < files.length; i++) {
					if (!files[i].isHidden() && files[i].isFile()
							&& files[i].canWrite()) {
						boolean success = files[i].delete();

						if (!success) {
							throw new IllegalArgumentException(
									"Delete: deletion failed");
						}
					}
				}
			} else {
				// Attempt to delete it
				boolean success = f.delete();

				if (!success) {
					throw new IllegalArgumentException(
							"Delete: deletion failed");
				}
			}
		}
	}

	/**
	 * Trims leading and trailing spaces and unprintable characters from the
	 * passed string.
	 * 
	 * @param valueToTrim
	 *            String to Trim
	 * @return Trimmed String
	 */
	public String trimUnusedCharacters(String valueToTrim) {
		String trimmedValue = valueToTrim.trim();

		if (trimmedValue.startsWith("\\n") || trimmedValue.startsWith("\\t")
				|| trimmedValue.startsWith("\\r")) {
			trimmedValue = trimmedValue.substring(1);
		}

		if (trimmedValue.endsWith("\\n") || trimmedValue.endsWith("\\t")
				|| trimmedValue.endsWith("\\r")) {
			trimmedValue = trimmedValue.substring(0, trimmedValue.length() - 1);
		}

		return trimmedValue;
	}

	/**
	 * Splits the file path and return a String array containing elements of the
	 * file path.
	 * 
	 * @param filePath
	 *            file path to split
	 * @return split file path elements in a String array
	 */
	public String[] splitFilePath(String filePath) {
		String[] path = null;
		Boolean flag = Boolean.valueOf(filePath.contains("\\"));
		Boolean value = Boolean.TRUE;
		try {
			if ( flag.compareTo(value) == 0) {
				path = filePath.split("\\\\");
			} else {
				path = filePath.split("/");
			}
		} catch (PatternSyntaxException pse) {
			LOGGER.error("Exception::", pse);
		}

		return path;
	}

	/**
	 * get the current time in hh:mm:ss format.
	 * 
	 * @return time string in hh:mm:ss format
	 */
	public String getCurrentTime() {
		// Construct a new date object
		java.util.Date curTime = new java.util.Date();
		// Set format to HH:mm:ss
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String currentTime = dateFormat.format(curTime);

		return currentTime;

	}

	/**
	 * Checks if a string contains only numbers.
	 * 
	 * @param str
	 *            the str
	 * @return boolean
	 */
	public boolean containsOnlyNumbers(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}


	/**
	 * To return time in MM:SS:sss format
	 * 
	 * @param milliseconds
	 *           milliseconds
	 * @return string
	 */
	public String formatTimeMilliSec(long milliseconds) {
		StringBuffer time = new StringBuffer();
		int hours = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds) / 3600;
		int mins = (int) ((TimeUnit.MILLISECONDS.toSeconds(milliseconds) / 60) - (hours * 60));
		int secs = (int) (TimeUnit.MILLISECONDS.toSeconds(milliseconds) - (hours * 3600 + mins * 60));
		int msecs = (int) (TimeUnit.MILLISECONDS.toMillis(milliseconds) - ((hours
				* 3600 + mins * 60 + secs) * 1000));

		// Minutes
		String strMins = String.valueOf(mins);
		strMins = strMins.length() == 1 ? "0" + strMins : strMins;
		// Seconds
		String strSec = String.valueOf(secs);
		strSec = strSec.length() == 1 ? "0" + strSec : strSec;
		// Milliseconds
		String mSec = String.valueOf(msecs);
		if (mSec.length() == 1) {
			mSec = "00" + mSec;
		} else if (mSec.length() == 2) {
			mSec = "0" + mSec;
		}
		time.append(strMins + ":" + strSec + ":" + mSec); // MM:SS:sss
		return time.toString();
	}
	
	/**
	 * @param milliseconds
	 *          milliseconds
	 * @return String
	 */
	public String formatTime(long milliseconds) {
		StringBuffer time = new StringBuffer();
		int hours = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds) / 3600;
		int mins = (int) ((TimeUnit.MILLISECONDS.toSeconds(milliseconds) / 60) - (hours * 60));
		int secs = (int) (TimeUnit.MILLISECONDS.toSeconds(milliseconds) - (hours * 3600 + mins * 60));
		/*
		 * int msecs = (int) (TimeUnit.MILLISECONDS.toMillis(milliseconds) -
		 * ((hours 3600 + mins * 60 + secs) * 1000));
		 */

		// time.append(hours + ":" + mins + ":" + secs + "." + msecs);
		String strHours = String.valueOf(hours);
		strHours = strHours.length() == 1 ? "0" + strHours : strHours;

		String strMins = String.valueOf(mins);
		strMins = strMins.length() == 1 ? "0" + strMins : strMins;

		String strSecs = String.valueOf(secs);
		strSecs = strSecs.length() == 1 ? "0" + strSecs : strSecs;

		time.append(strHours + ":" + strMins + ":" + strSecs); // milli sec
		// removed
		return time.toString();
	}
	/**
	 * Get the current timestamp.
	 * 
	 * @return java.sql.Timestamp
	 */
	public java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date date = new java.util.Date();

		return new java.sql.Timestamp(date.getTime());
	}
}
