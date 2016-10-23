package com.ags.aft.fixtures.spellChecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import com.ags.aft.Reporting.PageErrors;
import com.ags.aft.Reporting.ReportGenerator;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.enginemanager.EngineManager;
import com.ags.aft.exception.AFTException;
import com.ags.aft.logging.Log4JPlugin;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.util.Helper;
import com.ags.aft.util.StackTestData;
import com.ags.aft.util.Variable;

/**
 * The Class SpellChecker.
 */
public class SpellChecker {

	/** The Constant LOGGER. */
	private final Logger logger = Logger.getLogger(SpellChecker.class);

	/*
	 * Declaring the default language
	 */
	/** The default language. */
	private String defaultUSEnglishLang = "AMERICAN_ENGLISH";

	/*
	 * Declaring the langTool variable
	 */
	/** The lang tool. */
	private JLanguageTool langTool = null;

	/*
	 * Declaring the default langTool variable for English language
	 */
	/** The lang tool. */
	private JLanguageTool defaultUSEnglishLangTool = null;

	/*
	 * Declaring the spellChecker variable
	 */
	/** The spell checker. */
	private static SpellChecker spellChecker = null;

	// spell error list
	private Set<String> aSpellMistakes = new HashSet<String>();
	// grammar error list
	private Set<String> aGrammarMistakes = new HashSet<String>();

	// custom dictionary object
	private Map<String, String> customDictionary = null;

	// Spell check suggestions
	private String defaultSpellCheckOptionNo = "No";
	private String defaultSpellCheckOptionYes = "Yes";

	/** test step runner **/
	private TestStepRunner testStepRunner;

	/**
	 * Gets the single instance of JLanguageTool.
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 * @return single instance of JLanguageTool
	 * @throws AFTException
	 */
	private JLanguageTool init(TestStepRunner testStepRunner)
			throws AFTException {
		try {
			this.testStepRunner = testStepRunner;
			// Get the language from ConfigProperties file
			String language = validateLanguage(testStepRunner
					.getTestSuiteRunner().getTestSet().getSpellCheckLanguage());
			logger.info("The language taken from configuration file  ["
					+ language + "]");
			if (langTool == null) {
				// Get the specific language using Reflection
				Class<?> langClass = Class.forName("org.languagetool.Language");
				Language lang = (Language) langClass.getDeclaredField(language)
						.get(language);
				langTool = new JLanguageTool(lang);
				langTool.activateDefaultPatternRules();
			}

			// Loading custom dictionary file
			loadCustomDict(testStepRunner);

			if ((language.compareToIgnoreCase(defaultUSEnglishLang) != 0)
					&& (defaultUSEnglishLangTool == null)) {
				// Get the specific language using Reflection
				Class<?> defaultLangClass = Class
						.forName("org.languagetool.Language");
				Language defaultLang = (Language) defaultLangClass
						.getDeclaredField(defaultUSEnglishLang).get(
								defaultUSEnglishLang);
				defaultUSEnglishLangTool = new JLanguageTool(defaultLang);
				defaultUSEnglishLangTool.activateDefaultPatternRules();
			}

		} catch (Exception e) {
			logger.error("Exception::", e);
			throw new AFTException(e);
		}
		return langTool;
	}

	/**
	 * Verify the spell checker language
	 * 
	 * @param language
	 *            language
	 * @return requiredLanguage
	 */
	private String validateLanguage(String language) throws AFTException {
		String requiredLanguage = "";
		int i = 0;
		String[] spellCheckLanguages = { "AMERICAN_ENGLISH", "ASTURIAN",
				"AUSTRALIAN_ENGLISH", "AUSTRIAN_GERMAN", "BELARUSIAN",
				"BRETON", "BRITISH_ENGLISH", "CANADIAN_ENGLISH", "CATALAN",
				"DANISH", "DUTCH", "ENGLISH", "FRENCH", "GERMAN",
				"GERMANY_GERMAN", "GREEK", "ICELANDIC", "ITALIAN", "JAPANESE",
				"ROMANIAN", "RUSSIAN", "SLOVAK", "SLOVENIAN",
				"SOUTH_AFRICAN_ENGLISH", "SPANISH", "SWEDISH", "SWISS_GERMAN",
				"TAGALOG", "UKRAINIAN" };
		if (!language.isEmpty()) {
			for (; i < spellCheckLanguages.length; i++) {
				if (spellCheckLanguages[i].trim().equalsIgnoreCase(language)) {
					requiredLanguage = spellCheckLanguages[i];
					break;
				}
			}
			if (i == spellCheckLanguages.length) {
				throw new AFTException(
						"Pass the correct language in the TestBatch.xml!!");
			}
		} else {
			requiredLanguage = defaultUSEnglishLang;
			logger.info("As user didn't specify any language in the test set so default language ["
					+ requiredLanguage + "]");
		}
		return requiredLanguage;
	}

	/**
	 * Verify the Spell check suggestions
	 * 
	 * @param suggestion
	 *            suggestion
	 * @return requiredSuggestion
	 */
	private String validateSuggestion(String suggestion) {
		String requiredSuggestion = "";
		if (suggestion.isEmpty()
				|| (!suggestion.equalsIgnoreCase(defaultSpellCheckOptionNo) && !suggestion
						.equalsIgnoreCase(defaultSpellCheckOptionYes))) {
			requiredSuggestion = defaultSpellCheckOptionNo;
		}
		return requiredSuggestion;
	}

	/**
	 * Loads the respective language custom dictionary
	 * 
	 * @param testStepRunner
	 *            testStepRunner
	 */
	private void loadCustomDict(TestStepRunner testStepRunner) {
		String custDicFileName = testStepRunner.getTestSuiteRunner()
				.getTestSet().getCustomDictionaryPath();
		File f = new File(custDicFileName);
		if (f.exists() && f.isFile() && !f.isHidden()) {
			List<String> custDicArray = StackTestData
					.loadTextfromFiletoArray(custDicFileName);

			customDictionary = new HashMap<String, String>();
			for (String str : custDicArray) {
				customDictionary.put(str.trim().toLowerCase(), str);
			}
		} else {
			logger.info("No custom dictionary file with name ["
					+ custDicFileName
					+ "] found. Custom dictionary will not be loaded.");
		}

	}

	/**
	 * Gets the JLanguage tool instance
	 * 
	 * @param lang
	 *            lang
	 * @return JLanguageTool instance
	 * @throws AFTException
	 */
	public JLanguageTool getLangTool(String lang) throws AFTException {
		JLanguageTool languageTool = null;
		try {
			Class<?> langClass = Class.forName("org.languagetool.Language");
			Language language = (Language) langClass.getDeclaredField(
					lang.toUpperCase()).get(lang.toUpperCase());
			languageTool = new JLanguageTool(language);
			languageTool.activateDefaultPatternRules();
		} catch (Exception e) {
			logger.error("Exception::", e);
			throw new AFTException(e);
		}
		return languageTool;
	}

	/**
	 * Gets the single instance of SpellChecker.
	 * 
	 * @return single instance of SpellChecker
	 */
	public static SpellChecker getInstatnce() {
		if (spellChecker == null) {
			spellChecker = new SpellChecker();
		}
		return spellChecker;
	}

	/**
	 * Get the required word
	 * 
	 * @param data
	 *            : Context to get the required string
	 * @param no
	 *            : specified index
	 * @return value
	 */

	public String getString(String data, int no) {
		logger.trace("Data [" + data + "], Error at index no [" + no + "]");
		char[] myData = data.toCharArray();
		int beginIndex = 0;
		int endIndex = myData.length;
		for (int i = no - 1; i >= 0; i--) {
			if (myData[i] == ' ') {
				beginIndex = i;
				break;
			}
		}

		for (int i = no - 1; i < myData.length; i++) {
			if (myData[i] == ' ') {
				endIndex = i;
				break;
			}
		}
		return data.substring(beginIndex, endIndex).trim();
	}

	/**
	 * Split the given content based on line break.
	 * 
	 * @param sText
	 *            the list of lines
	 * @return List<String>, it returns the list of lines
	 */
	public List<String> getSplittedText(List<String> sText) {
		List<String> splittedString = new ArrayList<String>();
		for (String rData1 : sText) {
			String[] newData = rData1.split("\n");
			for (String addData : newData) {
				splittedString.add(addData);
			}
		}
		return splittedString;
	}

	/**
	 * Verify spelling.
	 * 
	 * @param sText
	 *            the s text
	 * @param testStepRunner
	 *            testStepRunner
	 * @return true, if successful
	 * @throws AFTException
	 */
	public boolean verifySpelling(String sText, TestStepRunner testStepRunner)
			throws AFTException {
		boolean flag = true;
		try {
			spellChecker.init(testStepRunner);
			List<String> requiredString = langTool.sentenceTokenize(sText);
			List<String> splittedString = new ArrayList<String>();
			aSpellMistakes = new HashSet<String>();
			aGrammarMistakes = new HashSet<String>();

			// split the original text to individual lines of text or smaller
			// tokens
			splittedString = getSplittedText(requiredString);

			// verify the spelling
			flag = checkSpelling(splittedString, langTool,
					defaultUSEnglishLangTool, testStepRunner);

		} catch (IOException e) {
			logger.error("Exception::", e);
			throw new AFTException(e);
		}

		return flag;
	}

	/**
	 * Verify spelling.
	 * 
	 * @param sText
	 *            the content
	 * @param language
	 *            the first language that used to validate the content
	 * @param testStepRunner
	 *            testStepRunner
	 * @return true, if successful
	 * @throws AFTException
	 */
	public boolean verifySpelling(String sText, String language,
			TestStepRunner testStepRunner) throws AFTException {
		boolean flag = true;
		try {
			JLanguageTool languageTool = getLangTool(language);
			List<String> requiredString = languageTool.sentenceTokenize(sText);
			List<String> splittedString = new ArrayList<String>();
			aSpellMistakes = new HashSet<String>();
			aGrammarMistakes = new HashSet<String>();

			splittedString = getSplittedText(requiredString);

			// Loading custom dictionary
			loadCustomDict(testStepRunner);

			// verify the spelling
			flag = checkSpelling(splittedString, languageTool, null,
					testStepRunner);
		} catch (IOException e) {
			logger.error("Exception::", e);
			throw new AFTException(e);
		}

		return flag;
	}

	/**
	 * Verify spelling.
	 * 
	 * @param sText
	 *            the content
	 * @param primaryLanguage
	 *            the first language that used to validate the content
	 * @param secondaryLanguage
	 *            the second language that used to validate the content
	 * @param testStepRunner
	 *            testStepRunner
	 * @return true, if successful
	 * @throws AFTException
	 */
	public boolean verifySpelling(String sText, String primaryLanguage,
			String secondaryLanguage, TestStepRunner testStepRunner)
			throws AFTException {
		boolean flag = true;
		try {
			JLanguageTool primaryLangToolObj = getLangTool(primaryLanguage);
			JLanguageTool secondaryLangToolObj = getLangTool(secondaryLanguage);
			List<String> requiredString = primaryLangToolObj
					.sentenceTokenize(sText);

			List<String> splittedString = new ArrayList<String>();

			aSpellMistakes = new HashSet<String>();
			aGrammarMistakes = new HashSet<String>();

			splittedString = getSplittedText(requiredString);

			// Loading custom dictionary
			loadCustomDict(testStepRunner);

			// verify the spelling
			flag = checkSpelling(splittedString, primaryLangToolObj,
					secondaryLangToolObj, testStepRunner);

		} catch (IOException e) {
			logger.error("Exception::", e);
			throw new AFTException(e);
		}

		return flag;
	}

	/**
	 * appendErrorMsg.
	 * 
	 * @param match
	 *            RuleMatch object
	 * @param errMsgSb
	 *            the StringBuilder object
	 * @param misSpellWord
	 *            the incorrect spell word
	 * @param text
	 *            the actual test
	 * @return String, the error message
	 */
	private String appendErrorMsg(RuleMatch match, StringBuilder errMsgSb,
			String misSpellWord, String text) {
		String errType = "";
		if (match.getRule().isSpellingRule()) {
			errType = "Possible spelling error";
			errMsgSb.append("[" + misSpellWord + "]");
			aSpellMistakes.add(errMsgSb.toString());
		} else {
			errType = "Possible grammar issue";
			if (!text.contains("<")) {
				errMsgSb.append("[" + text + "]");
				aGrammarMistakes.add(errMsgSb.toString());
			}
		}
		logger.warn(errType + " at line [" + match.getEndLine() + "], column ["
				+ match.getColumn() + "], error message [" + match.getMessage()
				+ "], Rule [" + match.getRule().getCategory().toString() + "]");
		return errType;
	}

	/**
	 * appendSuggestions.
	 * 
	 * @param errMsgSbWithSuggestion
	 *            the StringBuilder object
	 * @param suggestions
	 *            the suggestions
	 * @param testStepRunner
	 *            testStepRunner
	 * @throws AFTException
	 */
	private void appendSuggestions(StringBuilder errMsgSbWithSuggestion,
			List<String> suggestions, TestStepRunner testStepRunner)
			throws AFTException {
		if (validateSuggestion(
				testStepRunner.getTestSuiteRunner().getTestSet()
						.getSpellCheckSuggestion()).equalsIgnoreCase("yes")) {
			// ok, add the suggestion to the message
			errMsgSbWithSuggestion.append(", Suggestions " + suggestions);
			errMsgSbWithSuggestion.append("\n");
		}
	}

	/**
	 * check Spelling.
	 * 
	 * @param splittedString
	 *            the string in list format
	 * @param primaryLangToolObj
	 *            the primary JLanguage tool object
	 * @param secondaryLangToolObj
	 *            the secondary JLanguage tool object
	 * @param testStepRunner
	 *            testStepRunner
	 * @return true, if successful
	 * @throws AFTException
	 * @throws IOException
	 */
	private boolean checkSpelling(List<String> splittedString,
			JLanguageTool primaryLangToolObj,
			JLanguageTool secondaryLangToolObj, TestStepRunner testStepRunner)
			throws AFTException, IOException {
		boolean flag = true;
		List<RuleMatch> matches = new ArrayList<RuleMatch>();
		List<RuleMatch> lang2matches = new ArrayList<RuleMatch>();
		List<String> suggestions = new ArrayList<String>();
		// split the original text to individual lines of text or smaller
		// tokens
		for (int i = 0; i < splittedString.size(); i++) {
			if (splittedString.get(i).trim().length() > 0) {
				matches = primaryLangToolObj.check(splittedString.get(i));
				for (RuleMatch match : matches) {

					suggestions = match.getSuggestedReplacements();
					boolean misSpelled = true;
					// get the mis-spelled word and check in default US
					// English language if the user selected language is
					// different
					String misSpellWord = getString(splittedString.get(i),
							match.getColumn());

					// check if the word exists in the custom dictionary
					if (customDictionary != null
							&& customDictionary.get(misSpellWord.trim()
									.toLowerCase()) != null) {
						misSpelled = false;
					}

					// check if the word is a standard english
					// language word
					StringBuilder errMsgSb = new StringBuilder();
					StringBuilder errMsgSbWithSuggestion = new StringBuilder();
					String errType = "";

					if (misSpelled) {
						if (secondaryLangToolObj != null
								&& match.getRule().isSpellingRule()) {
							lang2matches = secondaryLangToolObj
									.check(misSpellWord);
							for (RuleMatch lang2match : lang2matches) {
								suggestions = lang2match
										.getSuggestedReplacements();
								errType = appendErrorMsg(lang2match,
										errMsgSbWithSuggestion, misSpellWord,
										splittedString.get(i));
							}
						} else {
							errType = appendErrorMsg(match,
									errMsgSbWithSuggestion, misSpellWord,
									splittedString.get(i));
						}

						// append the mis-spelled word to the error
						// object
						errMsgSbWithSuggestion.append(errMsgSb.toString());

						// did user enable suggestion? if yes, let
						// us add that also for log message
						//

						appendSuggestions(errMsgSbWithSuggestion, suggestions,
								testStepRunner);

						// log message first
						logger.warn(errMsgSbWithSuggestion.toString());

						// log file second
						if (!(errType + errMsgSbWithSuggestion.toString())
								.isEmpty()) {
							Log4JPlugin.getInstance().writeSpellErrors(
									errType + errMsgSbWithSuggestion.toString()
											+ "\n");
						}

						// add to the specific error object based on
						// type of error third

						// set flag to false to indicate we found
						// at least one error
						flag = false;
					}

				}
			}
		}

		// Generate the errors xml
		generateSpellErrorsXML(
				EngineManager.getInstance().getCurrentExecutionEngine()
						.getCurrentURL(),
				aSpellMistakes,
				aGrammarMistakes,
				Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_CURBUSINESSSCENARIOID)),
				testStepRunner.getTestSuiteRunner().getTestBatchRunner()
						.getTestSetName());

		return flag;
	}

	/**
	 * Getting the spell mistakes.
	 * 
	 * @return String
	 */
	public String getSpellErrors() {
		StringBuilder sb = new StringBuilder();
		StringBuilder sberrors = new StringBuilder();
		StringBuilder sbgrammer = new StringBuilder();

		if (aSpellMistakes.size() > 0) {
			sberrors.append("Spelling issues found:");
			sberrors.append("\n");
		}

		int i = 1;
		for (String s : aSpellMistakes) {
			if (!sberrors.toString().toLowerCase().contains(s.toLowerCase())) {
				sberrors.append("     " + s);
				sberrors.append("\n");
				if (i >= 4) {
					sberrors.append("      ...");
					break;
				} else {
					i++;
				}
			}
		}

		if (aGrammarMistakes.size() > 0) {
			sbgrammer.append("Grammer issues found:");
			sbgrammer.append("\n");
		}
		i = 1;
		for (String s : aGrammarMistakes) {
			if (!sbgrammer.toString().toLowerCase().contains(s.toLowerCase())) {
				sbgrammer.append("     " + s);
				sbgrammer.append("\n");
				if (i >= 4) {
					sbgrammer.append("      ...");
					break;
				} else {
					i++;
				}
			}
		}
		sb.append(sberrors);
		sb.append(sbgrammer);

		return sb.toString();
	}

	/**
	 * Returns the Spelling error count.
	 * 
	 * @return int
	 */
	public int getSpellErrorCount() {

		int errorCount = 0;

		if (aSpellMistakes != null) {
			errorCount = aSpellMistakes.size();
		}
		return errorCount;
	}

	/**
	 * Creates the xml file for spell errors and grammar errors.
	 * 
	 * @param url
	 *            application url
	 * @param spellMistakes
	 *            spell mistakes found in the specified page
	 * @param grammerMistakes
	 *            grammar mistakes found in the specified page.
	 * @param testScenarioID
	 *            the testScenarioID
	 * @param testSetName
	 *            the testSetName
	 */
	public void generateSpellErrorsXML(String url, Set<String> spellMistakes,
			Set<String> grammerMistakes, String testScenarioID,
			String testSetName) throws AFTException {
		PageErrors.getInstance().setspellErrorsURL(url);
		PageErrors.getInstance().setSpellErrorsTestSetNames(testSetName);
		PageErrors.getInstance().setSpellErrorsTestScenarioId(
				Helper.getInstance().getActionValue(
						Variable.getInstance().generateSysVarName(
								SystemVariables.AFT_CURBUSINESSSCENARIOID)));
		PageErrors.getInstance().setSpellErrors(spellMistakes);
		PageErrors.getInstance().setGrammarErrors(grammerMistakes);
		PageErrors.getInstance().setIdProject(
				Integer.parseInt(testStepRunner.getTestSuiteRunner()
						.getTestSet().getIdProject()));
		PageErrors.getInstance().setReportTestSuiteId(
				ReportGenerator.getInstance().getLatestTestSuite()
						.getIdReportTestSuite());

	}

}
