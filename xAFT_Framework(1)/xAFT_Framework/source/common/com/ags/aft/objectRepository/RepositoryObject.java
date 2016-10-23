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
 * Class: RepositoryObject
 * 
 * Purpose: This class implements RepositoryObject for each element in OR
 */
package com.ags.aft.objectRepository;

/**
 * The Class ObjectRepository.
 */
public class RepositoryObject {

	/** The logical name. */
	private String logicalName;

	/** The type. */
	private String type;

	/** The id. */
	private String id;

	/** The xpath. */
	private String xpath;

	/** The css. */
	private String css;

	/** The name. */
	private String name;

	/** The link */
	private String link;

	/** The logical class. */
	private String logicalClass;

	/** The page title. */
	private String pageTitle;

	/** The identification type. */
	private String identificationType;
	
	/** The file name. */
	private String imageName;
	
	/** The target offset. */
	private String targetOffset;
	
	/** The accuracy. */
	private String accuracy;
	
	/** The index. */
	private String index;
	
	/** The text. */
	private String text;
	
	/** The multiple sikuli images. */
	private String multipleImages;

	/**
	 * @return the logicalName
	 */
	public String getLogicalName() {
		return logicalName;
	}

	/**
	 * @param logicalName
	 *            the logicalName to set
	 */
	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the xpath
	 */
	public String getXpath() {
		return xpath;
	}

	/**
	 * @param xpath
	 *            the xpath to set
	 */
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	/**
	 * @return the css
	 */
	public String getCss() {
		return css;
	}

	/**
	 * @param css
	 *            the css to set
	 */
	public void setCss(String css) {
		this.css = css;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the logicalClass
	 */
	public String getLogicalClass() {
		return logicalClass;
	}

	/**
	 * @param logicalClass
	 *            the logicalClass to set
	 */
	public void setLogicalClass(String logicalClass) {
		this.logicalClass = logicalClass;
	}

	/**
	 * @return the pageTitle
	 */
	public String getPageTitle() {
		return pageTitle;
	}

	/**
	 * @param pageTitle
	 *            the pageTitle to set
	 */
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 * @return the identificationType
	 */
	public String getIdentificationType() {
		return identificationType;
	}

	/**
	 * @param identificationType
	 *            the identificationType to set
	 */
	public void setIdentificationType(String identificationType) {
		this.identificationType = identificationType;
	}
	
	/**
	* @return the fileName
	*/
	public String getImageName() {
		return imageName;
	}

	/**
	* @param imageName
	*            the imageName to set
	*/
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
	/**
	* @return the targetOffset
	*/
	public String getTargetOffset() {
		return targetOffset;
	}

	/**
	* @param targetOffset
	*            the targetOffset to set
	*/
	public void setTargetOffset(String targetOffset) {
		this.targetOffset = targetOffset;
	}
	
	/**
	* @return the accuracy
	*/
	public String getAccuracy() {
		return accuracy;
	}

	/**
	* @param accuracy
	*            the accuracy to set
	*/
	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(String index) {
		this.index = index;
	}
	

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	* @return the multiple images
	*/
	public String getMultipleImages() {
		return multipleImages;
	}

	/**
	* @param multipleImages
	*            the multiple images value
	*/
	public void setMultipleImages(String multipleImages) {
		this.multipleImages = multipleImages;
	}
}
