package com.alliance.schema.xmlvalidation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "Table")

public class SchemaElements {
	
	private String name;
	private String type;
	private String size;
	public String getSize() {
		return size;
	}



	public void setSize(String size) {
		this.size = size;
	}
	private String status;
	private String errorMessage;
	
	
	
	//default constructor
	public SchemaElements() {		
	}
	
	
	
	/**
	 * @param name
	 * @param type
	 * @param size
	 */
	public SchemaElements(String name, String type, String size) {
		this.name = name;
		this.type = type;
		this.size = size;
	}

	
	
	
	/**
	 * @param name
	 * @param type
	 * @param size
	 * @param status
	 */
	public SchemaElements(String name, String type, String size, String status) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.status = status;
	}

	/**
	 * @param name
	 * @param type
	 * @param size
	 * @param status
	 */
	public SchemaElements(String name, String type, String size, String status,
			String errorMessage) {
		
		this.name = name;
		this.type = type;
		this.size = size;
		this.status = status;
		this.errorMessage = errorMessage;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the size
	 */
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
