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
 * Class: XMLParser
 * 
 * Purpose: Implements XML Parser methods - read/write at Node/attribute level
 */

package com.ags.aft.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ags.aft.exception.AFTException;
import com.ags.aft.objectRepository.RepositoryObject;

/**
 * The Class XMLParser.
 * 
 * @author kkanumuri
 */
public class XMLParser {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(XMLParser.class);

	/** The doc. */
	private Document doc = null;

	/**  
	 * @return doc
	 */
	/*public Document getDoc() {
		return doc;
	}*/
	/**  
	 * @param doc
	 *         doc
	 */
//	public void setDoc(Document doc) {
//		this.doc = doc;
//	}

	/** The root element. */
	private Element rootElement;

	// /** The node. */
	// private static Element node;

	/** The file name. */
	private String fileName;

	/** The pnode. */
	private Element node;

//	private static XMLParser xmlParser;

	/**
	 * Initializes XMLParser object.
	 * 
	 * @throws AFTException
	 *             the aFT exception
	 */
	public XMLParser() throws AFTException {

	}
	/**
	 * Initializes XMLParser object.
	 * 
	 * @throws AFTException
	 *             the aFT exception
	 * @return xmlParser
	 *//*
	public static XMLParser getInstance() throws AFTException {

		if (xmlParser == null) {
			xmlParser = new XMLParser();
			return xmlParser;
		}
		return xmlParser;

	}*/

	/**
	 * Instantiates a new xML parser.
	 * 
	 * @param fName
	 *            the f name
	 */
	public XMLParser(String fName) {
		// create first attribute
		fileName = fName;
	}

	/**
	 * Gets the root element for the specified XML Resource.
	 * 
	 * @return the root element
	 *//*
	public String getRootElement() {
		return doc.getDocumentElement().getNodeName();
	}
*/
	/**
	 * Verify the root element.
	 * 
	 * @param rootElementName
	 *            the root element name
	 * @return the string
	 */
	public boolean verifyRootElement(String rootElementName) {
		boolean isRootElement = false;
		if (rootElementName.equals(doc.getDocumentElement().getNodeName())) {
			return isRootElement;
		}
		return isRootElement;
	}

	/**
	 * Gets the attribute list for the specified nodes.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return the attribute list
	 */
	public List<Map<String, String>> getAttributeNameList(String nodeName) {
		LOGGER.debug("Fetching the Attribute list for the node [" + nodeName
				+ "]");
		List<Map<String, String>> attrList = new ArrayList<Map<String, String>>();

		// Get the nodeList based on the nodeName Provided
		NodeList nodeList = doc.getElementsByTagName(nodeName);

		// Get the NodeList Length
		int nodeListLength = nodeList.getLength();
		LOGGER.debug("Total Nodes found [" + nodeListLength + "]");
		LOGGER.trace("Iterating through nodes...");
		for (int i = 0; i < nodeListLength; i++) {
			LOGGER.trace("----------------------------");
			LOGGER.trace("\n");

			HashMap<String, String> attrMap = new HashMap<String, String>();
			Node nodeObj = nodeList.item(i);
			if (nodeObj.hasAttributes()) {
				// add current node attributes name and its corresponding value
				// to the map
				int nodeAttrLength = nodeObj.getAttributes().getLength();
				LOGGER.debug("Total attributes count for node [" + nodeName
						+ "] is [" + nodeAttrLength + "]");
				LOGGER.trace("Iterating through attributes...");
				for (int nodeAttr = 0; nodeAttr < nodeAttrLength; nodeAttr++) {
					String nodeAttrName = nodeObj.getAttributes().item(nodeAttr)
							.getNodeName();
					String nodeAttrValue = nodeObj.getAttributes().item(nodeAttr)
							.getNodeValue();
					LOGGER.trace(nodeAttrName + " = " + nodeAttrValue);
					attrMap.put(nodeAttrName, nodeAttrValue);
				}
			}
			NodeList childNodeList = nodeObj.getChildNodes();
			int childNodeListLength = childNodeList.getLength();
			LOGGER.debug("Total child nodes count for node [" + nodeName
					+ "] is [" + childNodeListLength + "]");
			LOGGER.trace("Iterating through child nodes...");
			if (!nodeName.equalsIgnoreCase("FeatureSet")) {
				for (int j = 0; j < childNodeListLength; j++) {
					Node childNode = childNodeList.item(j);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String childNodeName = childNode.getNodeName();
						int attrLength = childNode.getAttributes().getLength();
						for (int k = 0; k < attrLength; k++) {
							String attributeValue = childNode.getAttributes()
									.item(k).getNodeValue();
							LOGGER.trace(childNodeName + " = " + attributeValue);
							attrMap.put(childNodeName, attributeValue);
						}
					}
				}
			}

			attrList.add(attrMap);
			LOGGER.debug("Successfully built attribute list for the for the specified node ["
					+ nodeName + "] and its child nodes");
		}

		return attrList;
	}

	/**
	 * Gets the attribute list for the specified nodes.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return the attribute list
	 * @throws AFTException
	 *             the aFT exception
	 */
	public List<Map<String, RepositoryObject>> getObjRepositoryAttrNameList(
			String nodeName) throws AFTException {
		LOGGER.debug("Fetching the Attribute list for the node [" + nodeName
				+ "]");
		List<Map<String, RepositoryObject>> attrList = new ArrayList<Map<String, RepositoryObject>>();
		try {
			// Get the nodeList based on the nodeName Provided
			NodeList nodeList = doc.getElementsByTagName(nodeName);

			// Get the NodeList Length
			int nodeListLength = nodeList.getLength();
			LOGGER.debug("Total Nodes found [" + nodeListLength + "]");
			List<String> attrNameList = new LinkedList<String>();
			LOGGER.trace("Iterating through nodes...");
			for (int i = 0; i < nodeListLength; i++) {
				Node nodeObj = nodeList.item(i);
				NodeList childNodeList = nodeObj.getChildNodes();
				int childNodeListLength = childNodeList.getLength();
				LOGGER.debug("Total child nodes count for node [" + nodeName
						+ "] is [" + childNodeListLength + "]");
				LOGGER.trace("Iterating through child nodes...");

				// get the attribute map for object repository.
				Map<String, RepositoryObject> attrMap = getObjRepositoryAttrMap(
						childNodeListLength, childNodeList);

				attrList.add(attrMap);
				LOGGER.debug("Successfully built attribute list for the for the specified node ["
						+ nodeName + "] and its child nodes");
			}

			// now check if the document contains any duplicate OR names...
			//
			if (nodeName.equalsIgnoreCase("FeatureSet")) {
				attrNameList = getAttrNameList();
			}
			int listSize = attrNameList.size();
			for (int k = 0; k < listSize; k++) {
				String attrName = attrNameList.get(0);
				attrNameList.remove(attrName);
				if (attrNameList.contains(attrName)) {
					LOGGER.warn("Duplicate attribute name ["
							+ attrName
							+ "] specified in the Object Repository. The first attribute value will be used wherever ["
							+ attrName + "] element name is used.");
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return attrList;
	}

	/**
	 * Gets the attribute names list for the specified nodes.
	 * 
	 * @param childNodeListLength
	 *            the childNodeListLength
	 * @param childNodeList
	 *            childNodeList
	 * @return the attrMap
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Map<String, RepositoryObject> getObjRepositoryAttrMap(
			int childNodeListLength, NodeList childNodeList)
			throws AFTException {

		Map<String, RepositoryObject> attrMap = new HashMap<String, RepositoryObject>();
		try {
			for (int j = 0; j < childNodeListLength; j++) {
				Node childNode = childNodeList.item(j);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element) childNode;
					// create object repository instance.
					RepositoryObject repositoryObject = new RepositoryObject();
					// set the Element node attribute values to objectRepository
					// name and type fields.
					repositoryObject.setLogicalName(child.getAttribute("name"));
					repositoryObject.setType(child.getAttribute("type"));
					// set the repository object data
					repositoryObject = setRepositoryObjectData(child,
							repositoryObject);
					// put the repositoryObject into map.
					attrMap.put(repositoryObject.getLogicalName(),
							repositoryObject);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return attrMap;
	}

	/**
	 * Sets the Repository object data.
	 * 
	 * @param child
	 *            the child
	 * @param repositoryObject
	 *            repositoryObject
	 * @return repositoryObject
	 */
	private RepositoryObject setRepositoryObjectData(Element child,
			RepositoryObject repositoryObject) {
		// get the child nodes of and Element node.
		NodeList elementChildNodeList = child.getChildNodes();
		RepositoryObject object = repositoryObject;
		for (int eleNodeAttr = 0; eleNodeAttr < elementChildNodeList
				.getLength(); eleNodeAttr++) {
			Node elementChildNode = elementChildNodeList.item(eleNodeAttr);
			if (elementChildNode.getNodeType() == Node.ELEMENT_NODE) {
				Element elementChild = (Element) elementChildNode;

				// if the child node is not a
				// pageTitle/targetOffset/accuracy/multipleImages then
				// set identification type and
				// the child node
				// value to object repository.
				if (!elementChild.getNodeName().equalsIgnoreCase("PageTitle")
						&& !elementChild.getNodeName().equalsIgnoreCase(
								"TargetOffset")
						&& !elementChild.getNodeName().equalsIgnoreCase(
								"accuracy")
						&& !elementChild.getNodeName().equalsIgnoreCase(
								"multipleImages")) {
					// set the data other than pageTitle, accuracy,
					// targetOffset and multipleImages
					object = setChildNodeData(object, elementChild);
				} else {
					// if the child node is pageTitle
					if (elementChild.getNodeName()
							.equalsIgnoreCase("pageTitle")) {
						object.setPageTitle(elementChild.getTextContent());
					} else if (elementChild.getNodeName().equalsIgnoreCase(
							"targetOffset")) {
						// if the child node is targetOffset
						object.setTargetOffset(elementChild.getTextContent());
					} else if (elementChild.getNodeName().equalsIgnoreCase(
							"accuracy")) {
						// if the child node is accuracy
						object.setAccuracy(elementChild.getTextContent());
					} else if (elementChild.getNodeName().equalsIgnoreCase(
							"multipleImages")) {
						// if the child node is multipleImages
						object.setMultipleImages(elementChild.getTextContent());
					}
				}
			}
		}
		return object;
	}

	/**
	 * Sets the child node data to Repository object other than pageTitle,
	 * accuracy and targetOffset.
	 * 
	 * @param repositoryObject
	 *            repositoryObject
	 * @param elementChild
	 *            the element child
	 * @return repositoryObject
	 */

	private RepositoryObject setChildNodeData(
			RepositoryObject repositoryObject, Element elementChild) {
		repositoryObject.setIdentificationType(elementChild.getNodeName());
		if (elementChild.getNodeName().equalsIgnoreCase("id")) {
			repositoryObject.setId(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("xpath")) {
			repositoryObject.setXpath(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("css")) {
			repositoryObject.setCss(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("name")) {
			repositoryObject.setName(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("link")) {
			repositoryObject.setLink(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("imagename")) {
			repositoryObject.setImageName(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("index")) {
			repositoryObject.setIndex(elementChild.getTextContent());
		} else if (elementChild.getNodeName().equalsIgnoreCase("text")) {
			repositoryObject.setText(elementChild.getTextContent());
		}
		return repositoryObject;
	}

	/**
	 * parses the XML document to construct the list of all attribute names in
	 * the document.
	 * 
	 * @return list of all attributes in the document
	 * @throws AFTException
	 *             the aFT exception
	 */
	private List<String> getAttrNameList() throws AFTException {
		LOGGER.debug("Fetching the child node name list for the node [FeatureSet]");
		List<String> attrNameList = new LinkedList<String>();
		try {
			// Get the nodeList based on the nodeName Provided
			NodeList nodeList = doc.getElementsByTagName("FeatureSet");

			// Get the NodeList Length
			int nodeListLength = nodeList.getLength();
			LOGGER.debug("Total nodes count for node [FeatureSet] is ["
					+ nodeListLength + "]");
			LOGGER.trace("Iterating through nodes...");
			for (int i = 0; i < nodeListLength; i++) {
				Node nodeObj = nodeList.item(i);
				if (nodeObj.hasAttributes()) {
					// add current node attributes names to the linked list
					int nodeAttrLength = nodeObj.getAttributes().getLength();
					for (int nodeAttr = 0; nodeAttr < nodeAttrLength; nodeAttr++) {
						String nodeAttrName = nodeObj.getAttributes()
								.item(nodeAttr).getNodeValue();
						attrNameList.add(nodeAttrName);
					}
				}
				NodeList childNodeList = nodeObj.getChildNodes();
				int childNodeListLength = childNodeList.getLength();
				LOGGER.debug("Total child nodes count for node [FeatureSet] is ["
						+ childNodeListLength + "]");
				LOGGER.trace("Iterating through child nodes...");
				for (int j = 0; j < childNodeListLength; j++) {
					Node childNode = childNodeList.item(j);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element child = (Element) childNode;
						String childNodeName = child.getAttribute("name");
						attrNameList.add(childNodeName);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return attrNameList;
	}

	/**
	 * Gets the attribute values for specified nodeName and the attribute Name.
	 * 
	 * @param nodeName
	 *            the node name
	 * @param attributeName
	 *            the attribute name
	 * @return the attributeValue
	 */
	public List<String> getAttributeList(String nodeName, String attributeName) {
		LOGGER.info("Fetching the attribute value for attribute " + "["
				+ attributeName + "] in node [" + nodeName + "]");

		String attributeValue = null;
		List<String> attrValueList = new ArrayList<String>();

		// get the list of nodes for the specified node name
		//
		NodeList nodeList = doc.getElementsByTagName(nodeName);
		LOGGER.debug("Total Nodes found [" + nodeList.getLength() + "]");

		// parse thru all nodes and fetch the attribute value for the specified
		// attribute name
		//
		for (int i = 0; i < nodeList.getLength(); i++) {
			attributeValue = nodeList.item(i).getAttributes()
					.getNamedItem(attributeName).getNodeValue();

			if (attributeValue != null) {
				attrValueList.add(attributeValue);
			}
		}

		return attrValueList;
	}

	/**
	 * Checks if is attribute present or not.
	 * 
	 * @param nodeName
	 *            the node name
	 * @param attributeName
	 *            the attribute name
	 * @param responseXML
	 *            the response xml
	 * @return true, if is attribute present
	 * @throws AFTException
	 *             the aFT exception
	 * @throws SAXException
	 *             the sAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public boolean isAttributePresent(String nodeName, String attributeName,
			String responseXML) throws AFTException, SAXException, IOException,
			ParserConfigurationException {

		LOGGER.trace("Response XML received by XML Parser: \n" + responseXML
				+ "\n");

		boolean attributeFound = false;
		Node nodeObj = null;

		try {
			/* The db. */
			DocumentBuilder db = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			LOGGER.debug("Creating XML Document Object");
			db = dbf.newDocumentBuilder();
			Document responseDoc = db.parse(new InputSource(new StringReader(
					responseXML)));
			NodeList nodeList = responseDoc.getElementsByTagName(nodeName);
			LOGGER.trace("Iterating through Nodes..");
			// parse thru all nodes and search for the specified attribute name
			//
			LOGGER.trace(nodeList.getLength());
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodeObj = nodeList.item(i);
				boolean isAttribute = nodeObj.hasAttributes();
				boolean isElement = nodeObj.hasChildNodes();
				if (isAttribute) {
					// Get attribute list to try to find the matching attribute
					NamedNodeMap attributeList = nodeObj.getAttributes();
					LOGGER.debug("Total attribute count for node [" + nodeName
							+ "] is [" + attributeList.getLength() + "]");
					// fetch the specified attribute and if found, add to the
					// attrValueList and exit
					//
					Node attrName = attributeList.getNamedItem(attributeName);
					if (attrName != null) {
						LOGGER.debug("Attribute [" + attrName
								+ "] found in the response");

						attributeFound = true;
						break;
					}
				}
				if (!attributeFound && isElement) {
					// Get node list to try to find the matching element
					NodeList childNodesList = nodeObj.getChildNodes();
					LOGGER.debug("Total child nodes count for node ["
							+ nodeName + "] is [" + childNodesList.getLength()
							+ "]");
					// iterate thru all elements for the specified node
					for (int j = 0; j < childNodesList.getLength(); j++) {
						// fetch the specified element and if found, add to the
						// attrValueList and exit
						//
						String elementName = childNodesList.item(j)
								.getNodeName();
						if (elementName.equals(attributeName)) {
							LOGGER.debug("Attribute [" + attributeName
									+ "] found in the response");

							attributeFound = true;
							break;
						}
					}
				}
			}
		} catch (IOException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		} catch (SAXException ie) {
			LOGGER.error("Exception::", ie);
			throw new AFTException(ie);
		}

		return attributeFound;
	}

	/**
	 * Updates XML attribute value for a specified Node and Attribute Name.
	 * 
	 * @param nodeName
	 *            the node name
	 * @param attributeName
	 *            the attribute name
	 * @param newAttributeValue
	 *            the new attribute value
	 * @return the String
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String updateAttributeValue(String nodeName, String attributeName,
			String newAttributeValue) throws AFTException {
		boolean attributeFound = false;
		boolean elementFound = false;
		// Get the nodeList based on the nodeName Provided
		NodeList nodeList = doc.getElementsByTagName(nodeName);

		// Get the NodeList Length
		int nodeListLength = nodeList.getLength();
		LOGGER.debug("Total nodes found [" + nodeListLength + "]");
		LOGGER.trace("Iterating through nodes...");
		for (int i = 0; i < nodeListLength; i++) {
			Node nodeObj = nodeList.item(i);
			boolean isAttribute = nodeObj.hasAttributes();
			boolean isElement = nodeObj.hasChildNodes();
			// Iterate through attribute list and check if the attribute is
			// present for that node
			if (isAttribute) {
				// checks whether attribute is found or not.
				attributeFound = isAttributeFound(nodeObj, attributeName,
						newAttributeValue);
			}
			// We did not find the matching attribute, let us now check for a
			// matching element
			//
			if (!attributeFound && isElement) {
				// checks whether element is found or not
				elementFound = isElementFound(nodeObj, attributeName,
						newAttributeValue, nodeName);
			}
		}
		if (!attributeFound && !elementFound) {
			String errMsg = "Attribute/Element [" + nodeName + "\\"
					+ attributeName + "] not found in xml ["
					+ doc.getXmlStandalone() + "]";
			LOGGER.error(errMsg);
		}

		return convertDocToString(doc);
	}

	/**
	 * Checks whether attribute is found or not.
	 * 
	 * @param node
	 *            the node
	 * @param attributeName
	 *            the attribute name
	 * @param newAttributeValue
	 *            the new attribute value
	 * @return the string
	 */
	private boolean isAttributeFound(Node node, String attributeName,
			String newAttributeValue) {
		boolean attributeFound = false;

		NamedNodeMap attributeList = node.getAttributes();
		LOGGER.trace("Iterating through [" + attributeList.getLength()
				+ "] attributes");
		for (int j = 0; j < attributeList.getLength(); j++) {
			// Get the attribute name
			String attrName = node.getAttributes().item(j).getNodeName();
			// Check whether attribute passed is present in the
			// attribute list and if present, update the attribute value
			// with new value
			if (attrName.equals(attributeName)) {
				LOGGER.info("Updating the attribute [" + attributeName
						+ "] with new value [" + newAttributeValue + "]");
				node.getAttributes().item(j).setNodeValue(newAttributeValue);
				LOGGER.debug("Successfully updated the attribute value");
				attributeFound = true;
				break;
			}
		}
		return attributeFound;
	}

	/**
	 * Checks whether element is found or not.
	 * 
	 * @param node
	 *            the node
	 * @param attributeName
	 *            the attribute name
	 * @param newAttributeValue
	 *            the new attribute value
	 * @param nodeName
	 *            the node name
	 * @return the string
	 */
	private boolean isElementFound(Node node, String attributeName,
			String newAttributeValue, String nodeName) {
		boolean elementFound = false;
		// Get node list to try to find the matching element
		NodeList childNodesList = node.getChildNodes();
		LOGGER.debug("Total child nodes count for node [" + nodeName + "] is ["
				+ childNodesList.getLength() + "]");
		// iterate thru all elements for the specified node
		for (int j = 0; j < childNodesList.getLength(); j++) {

			// fetch the specified element and if found, add to the
			// attrValueList and exit
			//
			String elementName = childNodesList.item(j).getNodeName();
			// Check whether element name passed is present in the node
			// list list and if present, update the element value with
			// new value
			if (elementName.equals(attributeName)) {
				LOGGER.info("Updating the element [" + elementName
						+ "] with new value [" + newAttributeValue + "]");
				node.getChildNodes().item(j).setNodeValue(newAttributeValue);
				node.getChildNodes().item(j).setTextContent(newAttributeValue);
				LOGGER.debug("Successfully updated the element value");
				elementFound = true;
				break;
			}
		}
		return elementFound;
	}

	/**
	 * Convert document object to string.
	 * 
	 * @param doc
	 *            the Document
	 * @return the string
	 * @throws AFTException
	 *             the application exception
	 */
	public String convertDocToString(Document doc) throws AFTException {
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
			LOGGER.debug("Converting XML Document object to String");
			transformer.transform(domSource, result);
		} catch (TransformerConfigurationException errMsg) {
			LOGGER.error("Exception::", errMsg);
			throw new AFTException(errMsg);
		} catch (TransformerException errMsg) {
			LOGGER.error("Exception::", errMsg);
			throw new AFTException(errMsg);
		}
		return writer.toString();
	}

	/**
	 * Convert xml string to file.
	 * 
	 * @param xmlString
	 *            the xml string
	 * @param fileName
	 *            the file name
	 * @throws AFTException
	 *             the AFT exception
	 */
	public static void convertXMLStringToFile(String xmlString, String fileName)
			throws AFTException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			LOGGER.debug("Saving XML string to file [" + fileName + "]");

			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					xmlString)));
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = null;
			aTransformer = tranFactory.newTransformer();

			Source src = new DOMSource(document);
			File destFile = new File(fileName);
			Result dest = new StreamResult(destFile.getAbsolutePath());
			aTransformer.transform(src, dest);

			LOGGER.debug("Successfully saved XML string to file ["
					+ destFile.getAbsolutePath() + "]");
		} catch (TransformerException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (SAXException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (ParserConfigurationException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Reads the xml file.
	 * 
	 * @param filePath
	 *            the file path
	 * @return return the xml as string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String readXML(String filePath) throws AFTException {
		// Check whether the File passed is a Logical File(XML as a string) or a
		// Physical File
		LOGGER.trace("Data received by XML Parser: [" + filePath + "]");

		File file = new File(filePath);
		// ensuring 'filepath' is either a valid/invalid path.
		if (!filePath.contains("<") && filePath.endsWith(".xml")) {
			if (file.exists()) {
				LOGGER.debug("XML file found");
				createXMLObject(filePath);
			} else {
				String errMsg = "File [" + filePath
						+ "] (The system cannot find the file specified)";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

		} else {
			LOGGER.debug("Request XML received");
			// Create In-memory XML file
			String xmlFilePath = createInMemoryXML(filePath);

			// Create the XML Document Object
			doc = createXMLObject(xmlFilePath);
		}
		return convertDocToString(doc);
	}

	/**
	 * Creates the in memory xml for the specified XML string.
	 * 
	 * @param filePath
	 *            the file path
	 * @return the string
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String createInMemoryXML(String filePath) throws AFTException {
		// Create an SAXBuilder object to write the string contents in to
		// in-memory XML Object
		LOGGER.debug("Creating SAX Builder Object");

		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document document = null;

		try {
			LOGGER.debug("Constructing In Memory XML from the XML String...");
			document = builder.build(new StringReader(filePath));
		} catch (JDOMException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		Writer writer = null;

		// Get the System Temp Directory where you want to write the XML
		// contents
		String tempFilePath = System.getenv("TMP") + ".xml";
		LOGGER.debug("Creating a temporary xml file [" + tempFilePath + "]");

		XMLOutputter outPutter = null;

		try {
			// Initiate the writer object to write the contents in to a
			// Temporary XML
			writer = new FileWriter(new File(tempFilePath));
			outPutter = new XMLOutputter(Format.getPrettyFormat());
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		try {
			LOGGER.debug("Writing the contents into file into [" + tempFilePath
					+ "]");
			outPutter.output(document, writer);
			LOGGER.debug("Creation of XML file [" + tempFilePath
					+ "] completed");
			LOGGER.debug("Closing the Writer..");
			writer.close();
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		return tempFilePath;
	}

	/**
	 * Creates an xml object for the filePath Specified.
	 * 
	 * @param filePath
	 *            the file path
	 * @return the xml document
	 * @throws AFTException
	 *             the aFT exception
	 */
	private Document createXMLObject(String filePath) throws AFTException {
		LOGGER.debug("Reading the XML file [" + filePath + "]");

		File file = new File(filePath);
		try {
			/** The db. */
			DocumentBuilder db = null;

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			LOGGER.debug("Creating XML Document Object");
			db = dbf.newDocumentBuilder();

			LOGGER.debug("Parsing the XML file [" + filePath + "]");
			doc = db.parse(file);

		} catch (ParserConfigurationException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (SAXException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} catch (IOException e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}

		doc.getDocumentElement().normalize();
		LOGGER.info("Completed reading the XML file [" + filePath + "]");
		// Delete the Temp File
		if (filePath.contains("Temp")) {
			LOGGER.trace("Creating a file object");
			file = new File(filePath);
			// Check if the file is deleted successfully
			boolean success = file.delete();
			if (!success) {
				LOGGER.warn("Failed to delete the Temp File [" + filePath + "]");
			} else {
				LOGGER.trace("Successfully Deleted the Temp File [" + filePath
						+ "]");
			}
		}
		return doc;
	}

	/**
	 * Gets the node list length for the specified node.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return the node list length
	 */
	public int getNodeListLength(String nodeName) {
		LOGGER.info("Getting node list length for node [" + nodeName + "]");
		NodeList nodeList = doc.getElementsByTagName(nodeName);
		return nodeList.getLength();
	}

	/**
	 * Gets the attribute value.
	 * 
	 * @param responseXML
	 *            the response xml
	 * @param nodeName
	 *            the node name
	 * @param attributeName
	 *            the attribute name
	 * @return List of attributes from responseXML matching passed attributename
	 *         and their Value
	 * @throws AFTException
	 *             the aFT exception
	 * @throws SAXException
	 *             the sAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public Map<String, String> getAttributeValue(String responseXML,
			String nodeName, String attributeName) throws AFTException,
			SAXException, IOException, ParserConfigurationException {

		LOGGER.info("Fetching the value for attribute/element " + "["
				+ attributeName + "] in node " + "[" + nodeName + "]");

		/** The db. */
		DocumentBuilder db = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		LOGGER.debug("Creating XML Document Object");
		db = dbf.newDocumentBuilder();
		Document responseDoc = db.parse(new InputSource(new StringReader(
				responseXML)));
		NodeList nodeList = responseDoc.getElementsByTagName(nodeName);
		boolean attributeFound = false;

		Map<String, String> attrValueList = new HashMap<String, String>();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nodeObj = nodeList.item(i);
			boolean isAttribute = nodeObj.hasAttributes();
			boolean isElement = nodeObj.hasChildNodes();
			// Check whether the tag within the Node is of type Element or
			// Attribute
			if (isAttribute) {
				// Get attribute list to try to find the matching attribute
				NamedNodeMap attributeList = nodeObj.getAttributes();
				LOGGER.debug("Total attribute count for node [" + nodeName
						+ "] is [" + attributeList.getLength() + "]");

				// fetch the specified attribute and if found, add to the
				// attrValueList and exit
				//
				Node attrName = attributeList.getNamedItem(attributeName);
				if (attrName != null) {
					LOGGER.debug("Attribute found in the response [" + attrName
							+ "]");

					String attributeValue = attrName.getNodeValue();

					LOGGER.debug("Found value [" + attributeValue
							+ "] in attribute " + "[" + attributeName
							+ "] in node [" + nodeName + "]");

					attrValueList.put(attributeName, attributeValue);

					attributeFound = true;
					break;
				}
			}

			// We did not find the matching attribute, let us now check for a
			// matching element
			//
			if (!attributeFound && isElement) {
				// Get node list to try to find the matching element
				NodeList childNodesList = nodeObj.getChildNodes();
				LOGGER.debug("Total child nodes count for node [" + nodeName
						+ "] is [" + childNodesList.getLength() + "]");

				// iterate thru all elements for the specified node
				for (int j = 0; j < childNodesList.getLength(); j++) {
					// fetch the specified element and if found, add to the
					// attrValueList and exit
					//
					String elementName = childNodesList.item(j).getNodeName();
					if (elementName.equals(attributeName)) {
						LOGGER.debug("Attribute found in the response ["
								+ elementName + "]");
						String elementValue = childNodesList.item(j)
								.getTextContent();

						LOGGER.debug("Found value [" + elementValue
								+ "] in child node " + "[" + elementName
								+ "] in node [" + nodeName + "]");

						attrValueList.put(elementName, elementValue);

						attributeFound = true;
						break;
					}
				}
			}
		}

		if (!attributeFound) {
			String errMsg = "Attribute/Element [" + nodeName + "\\"
					+ attributeName + "] not found in xml [" + responseXML
					+ "]";
			LOGGER.error(errMsg);
		}

		return attrValueList;
	}

	/**
	 * Gets the document object.
	 * 
	 * @return the document object
	 */
	public Document getDocumentObject() {
		return this.doc;
	}

	/**
	 * set the object repository values ObjectRepository.
	 * 
	 * @param rs
	 *            the rs
	 * @return the attrMap
	 * @throws AFTException
	 *             the aFT exception
	 */
	public Map<String, RepositoryObject> setObjectRepositoryValues(ResultSet rs)
			throws AFTException {
		Map<String, RepositoryObject> attrMap = new HashMap<String, RepositoryObject>();
		try {
			// create object repository instance.
			RepositoryObject repositoryObject = new RepositoryObject();
			// set the Element node attribute values to objectRepository
			// name and type fields.
			repositoryObject.setLogicalName(rs.getString("elementName"));
			repositoryObject.setType(rs.getString("elementType"));

			repositoryObject.setIdentificationType(rs.getString("objectType"));
			if (repositoryObject.getIdentificationType().equalsIgnoreCase("id")) {
				repositoryObject.setId(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("xpath")) {
				repositoryObject.setXpath(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("css")) {
				repositoryObject.setCss(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("name")) {
				repositoryObject.setName(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("link")) {
				repositoryObject.setLink(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("imagename")) {
				repositoryObject.setImageName(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("index")) {
				repositoryObject.setIndex(rs.getString("objectIdentifier"));
			} else if (repositoryObject.getIdentificationType()
					.equalsIgnoreCase("text")) {
				repositoryObject.setText(rs.getString("objectIdentifier"));
			}
			repositoryObject.setPageTitle(rs.getString("pageTitle"));

			// put the objectRepository into map.
			attrMap.put(repositoryObject.getLogicalName(), repositoryObject);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
		return attrMap;
	}

	/**
	 * Creates the document.
	 * 
	 * @return the document
	 */
	public Document createDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;

		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error(e);
		}

		// root elements
		this.doc = docBuilder.newDocument();
		return doc;
	}

	/**
	 * Creates the root element.
	 * 
	 * @param rootElementName
	 *            the root element name
	 * @return rootElement
	 */
	public Element createRootElement(String rootElementName) {
		rootElement = doc.createElement(rootElementName);
		doc.appendChild(rootElement);
		return rootElement;

	}

	/**
	 * Creates the node.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return the element
	 */
	public Element createNode(String nodeName) {
		node = doc.createElement(nodeName);
		rootElement.appendChild(node);
		return node;
	}

	/**
	 * Creates the attributes.
	 * 
	 * @param attrMap
	 *            the attr map
	 */
	public void createAttributes(Map<String, String> attrMap) {
		@SuppressWarnings("rawtypes")
		Iterator entries = attrMap.entrySet().iterator();
		String key = null;
		String value = null;
		/** The attr. */
		Attr attr;
		while (entries.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) entries.next();
			key = (String) entry.getKey();
			value = (String) entry.getValue();
			attr = doc.createAttribute(key);
			attr.setValue(value);
			node.setAttributeNode(attr);

		}

		rootElement.appendChild(node);

		// node.setAttributeNode(attr);

	}

	/**
	 * Generate xml.
	 */
	public void generateXML() {
		// create first attribute
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileName).getAbsolutePath());

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			LOGGER.error(e);
		} catch (TransformerException e) {
			LOGGER.error(e);
		}

		LOGGER.info("File saved!");
	}
}
