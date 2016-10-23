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
 * Class: StackTestData
 * 
 * Purpose: This class contains utility methods related to test data.
 */
package com.ags.aft.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;

public final class StackTestData {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(StackTestData.class);

	/** The stackTestData. */
	private static StackTestData stackTestData;
	
	/** The colors random data array. */
	private List<String> colorsRandomDataArray;

	/** The currency codes random data array. */
	private List<String> currencyCodesRandomDataArray;

	/** The female first names random data array. */
	private List<String> femaleFirstNamesRandomDataArray;

	/** The last names random data array. */
	private List<String> lastNamesRandomDataArray;

	/** The male first names random data array. */
	private List<String> maleFirstNamesRandomDataArray;

	/** The months random data array. */
	private List<String> monthsRandomDataArray;

	/** The names random data array. */
	private List<String> namesRandomDataArray;

	/** The US cities random data array. */
	private List<String> usCitiesRandomDataArray;

	/** The US state codes random data array. */
	private List<String> usStateCodesRandomDataArray;

	/** The US state names random data array. */
	private List<String> usStateNamesRandomDataArray;

	/** The US street address random data array. */
	private List<String> usStreetAddressRandomDataArray;

	/** The US zip codes random data array. */
	private List<String> usZipCodesRandomDataArray;

	/** The CC numbers array. */
	private List<String> ccNumbersArray;

	/** The US phone number random data array. */
	private List<String> usPhoneNumberRandomDataArray;

	/** The SSN # random data array. */
	private List<String> ssNArray;

	/**
	 * Instantiates a new stackTestData.
	 */
	private StackTestData() {
		super();
	}

	/**
	 * Gets the single instance of stackTestData.
	 * 
	 * @return single instance of stackTestData
	 */
	public static StackTestData getInstance() {
		if (stackTestData == null) {
			stackTestData = new StackTestData();
			LOGGER.trace("Creating instance of stackTestData");
		}

		return stackTestData;
	}
	
	/**
	 * Load random data resource files.
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	public void loadRandomDataResourceFiles() throws AFTException {
		// Loading the text from the text files to the array
		try {
			// retrieve file path from AFTConfig.properties
			String filePath = ConfigProperties.getInstance().getConfigProperty(
					ConfigProperties.RANDOM_DATA_RESOURCES_FILEPATH);

			if (filePath.isEmpty()) {
				LOGGER.info("Random Data resource file path not specified in AFTConfig.properties file, defaulting to ["
						+ ConfigProperties.DEFAULT_RANDOM_DATA_RESOURCE_FILEPATH
						+ "]");
				filePath = ConfigProperties.DEFAULT_RANDOM_DATA_RESOURCE_FILEPATH;
				LOGGER.info("Random Data resource file path defaulted to ["
						+ filePath + "]");
			} else if (!filePath.endsWith("/")) {
				filePath += "/";
			}

			// Load all random data files
			colorsRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.COLORSFILE);
			currencyCodesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.CURRENCYCODEFILE);
			femaleFirstNamesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.FEMALEFIRSTNAMEFILE);
			lastNamesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.LASTNAMEFILE);
			maleFirstNamesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.MALEFIRSTNAMEFILE);
			monthsRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.MONTHFILE);
			namesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.NAMEFILE);
			usCitiesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USCITYFILE);
			usStateCodesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USSTATECODEFILE);
			usStateNamesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USSTATENAMEFILE);
			usStreetAddressRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USSTREETADDRESSFILE);
			usZipCodesRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USZIPCODEFILE);
			ccNumbersArray = loadTextfromFiletoArray(filePath
					+ Constants.CCNUMBERSFILE);
			usPhoneNumberRandomDataArray = loadTextfromFiletoArray(filePath
					+ Constants.USPHONENOFILE);
			ssNArray = loadTextfromFiletoArray(filePath
					+ Constants.SSNNUMBERSFILE);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}
	
	/**
	 * Generate random value for the specified variable.
	 * 
	 * @param variableName
	 *            the variable name for which the random value needs to be
	 *            generated
	 * @return the string containing the random value
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String generateRandomValue(String variableName) throws AFTException {
		int randomIndex;
		String randomValue = "";
		Random randomNumGenerator = new Random();

		if (variableName.equalsIgnoreCase(SystemVariables.AFT_GENERATECOLOR)) {
			randomIndex = randomNumGenerator.nextInt(colorsRandomDataArray
					.size());
			randomValue = colorsRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATECURRENCYCODE)) {
			randomIndex = randomNumGenerator
					.nextInt(currencyCodesRandomDataArray.size());
			randomValue = currencyCodesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEFEMALEFIRSTNAME)) {
			randomIndex = randomNumGenerator
					.nextInt(femaleFirstNamesRandomDataArray.size());
			randomValue = femaleFirstNamesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATELASTNAME)) {
			randomIndex = randomNumGenerator.nextInt(lastNamesRandomDataArray
					.size());
			randomValue = lastNamesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEMALEFIRSTNAME)) {
			randomIndex = randomNumGenerator
					.nextInt(maleFirstNamesRandomDataArray.size());
			randomValue = maleFirstNamesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEMONTH)) {
			randomIndex = randomNumGenerator.nextInt(monthsRandomDataArray
					.size());
			randomValue = monthsRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATENAME)) {
			randomIndex = randomNumGenerator.nextInt(namesRandomDataArray
					.size());
			randomValue = namesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSCITY)) {
			randomIndex = randomNumGenerator.nextInt(usCitiesRandomDataArray
					.size());
			randomValue = usCitiesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTATECODE)) {
			randomIndex = randomNumGenerator
					.nextInt(usStateCodesRandomDataArray.size());
			randomValue = usStateCodesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTATENAME)) {
			randomIndex = randomNumGenerator
					.nextInt(usStateNamesRandomDataArray.size());
			randomValue = usStateNamesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTREETADDRESS)) {
			randomIndex = randomNumGenerator
					.nextInt(usStreetAddressRandomDataArray.size());
			randomValue = usStreetAddressRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSZIPCODE)) {
			randomIndex = randomNumGenerator.nextInt(usZipCodesRandomDataArray
					.size());
			randomValue = usZipCodesRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATECCNUMBER)) {
			randomIndex = randomNumGenerator.nextInt(ccNumbersArray.size());
			randomValue = ccNumbersArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATECCTYPE)) {
			randomValue = creditCardType();
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSPHONENUMBER)) {
			randomIndex = randomNumGenerator
					.nextInt(usPhoneNumberRandomDataArray.size());
			randomValue = usPhoneNumberRandomDataArray.get(randomIndex);
		} else if (variableName
				.equalsIgnoreCase(SystemVariables.AFT_GENERATESSN)) {
			randomIndex = randomNumGenerator.nextInt(ssNArray.size());
			randomValue = ssNArray.get(randomIndex);
		}

		return randomValue;
	}

	
	/**
	 * Load textfrom fileto array.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the array list
	 */
	public static List<String> loadTextfromFiletoArray(String fileName) {
		String data;
		ArrayList<String> randomData = new ArrayList<String>();

		try {
			// Open file stream
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName),
							"UTF-8"));

			if (!inputStream.ready()) {
				throw new IOException();
			}

			// Read filed
			while ((data = inputStream.readLine()) != null) {
				randomData.add(data);
			}
			// close file stream
			inputStream.close();
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			randomData = null;
		}

		// return the random data read from file
		return randomData;
	}
	/**
	 * Returns the Credit card type based on the value of the previously
	 * generated Random CC number.
	 * 
	 * @return the string containing the CC type for the previously generated
	 *         Random CC number
	 * @throws AFTException
	 *             the aFT exception
	 */
	private String creditCardType() throws AFTException {
		String cardType = null;

		// Construct the variable name...
		String sysVarName = Variable.getInstance().generateSysVarName(
				SystemVariables.AFT_GENERATECCNUMBER);

		// Fetch current variable value...
		String ccNumber = Helper.getInstance().getActionValue(sysVarName);

		if (ccNumber.substring(0, 2).equals("56")) {
			cardType = "Maestro";
		} else if (ccNumber.substring(0, 2).equals("30")
				|| ccNumber.substring(0, 2).equals("36")
				|| ccNumber.substring(0, 2).equals("38")) {
			cardType = "Diners";
		} else if (ccNumber.substring(0, 2).equals("37")) {
			cardType = "Amex";
		} else if (ccNumber.startsWith("4")) {
			cardType = "Visa";
		} else if (ccNumber.startsWith("5")) {
			cardType = "Master";
		} else if (ccNumber.startsWith("6")) {
			cardType = "Discover";
		} else {
			LOGGER.error("Card Number [" + ccNumber + "] is invalid");
		}

		LOGGER.info("Card Number is [" + ccNumber + "], Card Type is ["
				+ cardType + "]");

		return cardType;
	}
	
	/**
	 * Validates if the variable is a Random data variable or not.
	 * 
	 * @param variableName
	 *            the variable name to validate if it is a random data variable
	 *            name
	 * @return true, if the variable name passed is a random data variable
	 */
	public boolean randomDataVariable(String variableName) {
		boolean randomDataVar = false;

		if (variableName.equalsIgnoreCase(SystemVariables.AFT_GENERATECOLOR)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATECURRENCYCODE)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEFEMALEFIRSTNAME)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATELASTNAME)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEMALEFIRSTNAME)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEMONTH)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATENAME)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSCITY)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSPHONENUMBER)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTATECODE)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTATENAME)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSSTREETADDRESS)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATEUSZIPCODE)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATECCNUMBER)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATECCTYPE)
				|| variableName
						.equalsIgnoreCase(SystemVariables.AFT_GENERATESSN)) {
			randomDataVar = true;
		}

		return randomDataVar;
	}
}
