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
 * Class: ImagePath
 * 
 * Purpose: This class contains setters and getters for ImagePath.
 */
package com.ags.enlace.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ImagePath.
 */
@XmlRootElement
public class ImagePath {
	
	private String imagePath;
	private String testStepId;
	private String testStepDescription;
	private String result;
	
	/**
	 * @return the imagePath
	 */
	public String getImagePath() {
		return imagePath;
	}
	/**
	 * @param imagePath the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	/**
	 * @return the testStepId
	 */
	public String getTestStepId() {
		return testStepId;
	}
	/**
	 * @param testStepId the testStepId to set
	 */
	public void setTestStepId(String testStepId) {
		this.testStepId = testStepId;
	}
	/**
	 * @return the testStepDescription
	 */
	public String getTestStepDescription() {
		return testStepDescription;
	}
	/**
	 * @param testStepDescription the testStepDescription to set
	 */
	public void setTestStepDescription(String testStepDescription) {
		this.testStepDescription = testStepDescription;
	}
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
}
