package com.alliance.schema.validation;

public class SchemaElements {
	
	private String name;
	private String type;
	private String size;
	private String status;
	private String errorMessage;
	private String tableName;
	
	
	//default constructor
	public SchemaElements() {
	}
	
	
	
	public SchemaElements(String name, String type, String size, String status,
			String errorMessage, String tableName) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.status = status;
		this.errorMessage = errorMessage;
		this.tableName = tableName;
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
	public SchemaElements(String name, String type, String size, String tableName,
			String status) {
		
		this.name = name;
		this.type = type;
		this.size = size;
		this.status = status;
		this.tableName = tableName;
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
	public String getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}
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



	public String getTableName() {
		return tableName;
	}



	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
}
