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
 * Class: TestSuite
 * 
 * Purpose: This class store Testset data for execution.
 */

package com.ags.aft.testObjects;

/**
 * Class to store Testset data.
 * 
 */
public class TestSet {
	private String idTestSuite;
	private String testSetName;
	private String applicationName;
	private String description;
	private String executionEngine;
	private String applicationUrl;
	private String browser;
	private String hostMachine;
	private String appExecutionConfiguration;
	private String testSuitePath;
	private String scenariosSheetName;
	private String testStepsSheetName ;
	private String objectRepositoryPath;
	private String testDataTablePath;
	private String reusableTestSuitePath;
	private String reusableScenariosSheetName;
	private String reusableTestStepsSheetName;
	private String tcmIntegrationConfigFilePath;
	private String sikuliImagesPath;
	private String category;
	private String scenarioInitializationIDs;
	private String scenarioCleanupIDs;
	private String testSetInitializationIDs;
	private String testSetCleanupIDs;
	private String executeSuite;
	private String appConfigFilePath;
	private String customDictionaryPath;
	private String spellCheckLanguage;
	private String spellCheckSuggestion;
	private String idProject;

	/**
	 * @return the spellCheckLanguage
	 */
	public String getSpellCheckLanguage() {
		return spellCheckLanguage;
	}
	
	/**
	 * @param spellCheckLanguage the spellCheckLanguage to set
	 */
	public void setSpellCheckLanguage(String spellCheckLanguage) {
		this.spellCheckLanguage = spellCheckLanguage;
	}
	
	/**
	 * @return the spellCheckSuggestion
	 */
	public String getSpellCheckSuggestion() {
		return spellCheckSuggestion;
	}
	
	/**
	 * @param spellCheckSuggestion the spellCheckSuggestion to set
	 */
	public void setSpellCheckSuggestion(String spellCheckSuggestion) {
		this.spellCheckSuggestion = spellCheckSuggestion;
	}
	

	/**
	 * @return the idTestSuite
	 */
	public String getIdTestSuite() {
		return idTestSuite;
	}
	/**
	 * @param idTestSuite the idTestSuite to set
	 */
	public void setIdTestSuite(String idTestSuite) {
		this.idTestSuite = idTestSuite;
	}
	/**
	 * @return the testSetName
	 */
	public String getTestSetName() {
		return testSetName;
	}
	/**
	 * @param testSetName the testSetName to set
	 */
	public void setTestSetName(String testSetName) {
		this.testSetName = testSetName;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the executionEngine
	 */
	public String getExecutionEngine() {
		return executionEngine;
	}
	/**
	 * @param executionEngine the executionEngine to set
	 */
	public void setExecutionEngine(String executionEngine) {
		this.executionEngine = executionEngine;
	}
	/**
	 * @return the applicationUrl
	 */
	public String getApplicationUrl() {
		return applicationUrl;
	}
	/**
	 * @param applicationUrl the applicationUrl to set
	 */
	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}
	/**
	 * @return the browser
	 */
	public String getBrowser() {
		return browser;
	}
	/**
	 * @param browser the browser to set
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	/**
	 * @return the hostMachine
	 */
	public String getHostMachine() {
		return hostMachine;
	}
	/**
	 * @param hostMachine the hostMachine to set
	 */
	public void setHostMachine(String hostMachine) {
		this.hostMachine = hostMachine;
	}
	/**
	 * @return the appExecutionConfiguration
	 */
	public String getAppExecutionConfiguration() {
		return appExecutionConfiguration;
	}
	/**
	 * @param appExecutionConfiguration the appExecutionConfiguration to set
	 */
	public void setAppExecutionConfiguration(String appExecutionConfiguration) {
		this.appExecutionConfiguration = appExecutionConfiguration;
	}
	/**
	 * @return the testSuitePath
	 */
	public String getTestSuitePath() {
		return testSuitePath;
	}
	/**
	 * @param testSuitePath the testSuitePath to set
	 */
	public void setTestSuitePath(String testSuitePath) {
		this.testSuitePath = testSuitePath;
	}
	/**
	 * @return the scenariosSheetName
	 */
	public String getScenariosSheetName() {
		return scenariosSheetName;
	}
	/**
	 * @param scenariosSheetName the scenariosSheetName to set
	 */
	public void setScenariosSheetName(String scenariosSheetName) {
		this.scenariosSheetName = scenariosSheetName;
	}
	/**
	 * @return the testStepsSheetName
	 */
	public String getTestStepsSheetName() {
		return testStepsSheetName;
	}
	/**
	 * @param testStepsSheetName the testStepsSheetName to set
	 */
	public void setTestStepsSheetName(String testStepsSheetName) {
		this.testStepsSheetName = testStepsSheetName;
	}
	/**
	 * @return the objectRepositoryPath
	 */
	public String getObjectRepositoryPath() {
		return objectRepositoryPath;
	}
	/**
	 * @param objectRepositoryPath the objectRepositoryPath to set
	 */
	public void setObjectRepositoryPath(String objectRepositoryPath) {
		this.objectRepositoryPath = objectRepositoryPath;
	}
	/**
	 * @return the testDataTablePath
	 */
	public String getTestDataTablePath() {
		return testDataTablePath;
	}
	/**
	 * @param testDataTablePath the testDataTablePath to set
	 */
	public void setTestDataTablePath(String testDataTablePath) {
		this.testDataTablePath = testDataTablePath;
	}
	/**
	 * @return the reusableTestSuitePath
	 */
	public String getReusableTestSuitePath() {
		return reusableTestSuitePath;
	}
	/**
	 * @param reusableTestSuitePath the reusableTestSuitePath to set
	 */
	public void setReusableTestSuitePath(String reusableTestSuitePath) {
		this.reusableTestSuitePath = reusableTestSuitePath;
	}
	/**
	 * @return the reusableScenariosSheetName
	 */
	public String getReusableScenariosSheetName() {
		return reusableScenariosSheetName;
	}
	/**
	 * @param reusableScenariosSheetName the reusableScenariosSheetName to set
	 */
	public void setReusableScenariosSheetName(String reusableScenariosSheetName) {
		this.reusableScenariosSheetName = reusableScenariosSheetName;
	}
	/**
	 * @return the reusableTestStepsSheetName
	 */
	public String getReusableTestStepsSheetName() {
		return reusableTestStepsSheetName;
	}
	/**
	 * @param reusableTestStepsSheetName the reusableTestStepsSheetName to set
	 */
	public void setReusableTestStepsSheetName(String reusableTestStepsSheetName) {
		this.reusableTestStepsSheetName = reusableTestStepsSheetName;
	}
	/**
	 * @return the tcmIntegrationConfigFilePath
	 */
	public String getTcmIntegrationConfigFilePath() {
		return tcmIntegrationConfigFilePath;
	}
	/**
	 * @param tcmIntegrationConfigFilePath the tcmIntegrationConfigFilePath to set
	 */
	public void setTcmIntegrationConfigFilePath(String tcmIntegrationConfigFilePath) {
		this.tcmIntegrationConfigFilePath = tcmIntegrationConfigFilePath;
	}
	/**
	 * @return the sikuliImagesPath
	 */
	public String getSikuliImagesPath() {
		return sikuliImagesPath;
	}
	/**
	 * @param sikuliImagesPath the sikuliImagesPath to set
	 */
	public void setSikuliImagesPath(String sikuliImagesPath) {
		this.sikuliImagesPath = sikuliImagesPath;
	}
	
	/**
	 * @return the custom dictionary path
	 */
	public String getCustomDictionaryPath() {
		return customDictionaryPath;
	}
	
	/**
	 * @param customDictionaryPath to set the custom dictionary path
	 */
	public void setCustomDictionaryPath(String customDictionaryPath) {
		this.customDictionaryPath = customDictionaryPath;
	}
	
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the scenarioInitializationIDs
	 */
	public String getScenarioInitializationIDs() {
		return scenarioInitializationIDs;
	}
	/**
	 * @param scenarioInitializationIDs the scenarioInitializationIDs to set
	 */
	public void setScenarioInitializationIDs(String scenarioInitializationIDs) {
		this.scenarioInitializationIDs = scenarioInitializationIDs;
	}
	/**
	 * @return the scenarioCleanupIDs
	 */
	public String getScenarioCleanupIDs() {
		return scenarioCleanupIDs;
	}
	/**
	 * @param scenarioCleanupIDs the scenarioCleanupIDs to set
	 */
	public void setScenarioCleanupIDs(String scenarioCleanupIDs) {
		this.scenarioCleanupIDs = scenarioCleanupIDs;
	}
	/**
	 * @return the testSetInitializationIDs
	 */
	public String getTestSetInitializationIDs() {
		return testSetInitializationIDs;
	}
	/**
	 * @param testSetInitializationIDs the testSetInitializationIDs to set
	 */
	public void setTestSetInitializationIDs(String testSetInitializationIDs) {
		this.testSetInitializationIDs = testSetInitializationIDs;
	}
	/**
	 * @return the testSetCleanupIDs
	 */
	public String getTestSetCleanupIDs() {
		return testSetCleanupIDs;
	}
	/**
	 * @param testSetCleanupIDs the testSetCleanupIDs to set
	 */
	public void setTestSetCleanupIDs(String testSetCleanupIDs) {
		this.testSetCleanupIDs = testSetCleanupIDs;
	}
	/**
	 * @return the executeSuite
	 */
	public String getExecuteSuite() {
		return executeSuite;
	}
	/**
	 * @param executeSuite the executeSuite to set
	 */
	public void setExecuteSuite(String executeSuite) {
		this.executeSuite = executeSuite;
	}
	
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @param applicationName the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/**
	 * @return the appConfigFilePath
	 */
	public String getAppConfigFilePath() {
		return appConfigFilePath;
	}
	/**
	 * @param appConfigFilePath the appConfigFilePath to set
	 */
	public void setAppConfigFilePath(String appConfigFilePath) {
		this.appConfigFilePath = appConfigFilePath;
	}
	/**
	 * @return the idProject
	 */
	public String getIdProject() {
		return idProject;
	}

	/**
	 * @param idProject the idProject to set
	 */
	public void setIdProject(String idProject) {
		this.idProject = idProject;
	}
}