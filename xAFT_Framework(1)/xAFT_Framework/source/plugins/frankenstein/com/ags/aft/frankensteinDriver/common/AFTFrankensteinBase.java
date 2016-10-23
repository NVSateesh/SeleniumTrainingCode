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
 * Class: AFTSeleniumBase
 * 
 * Purpose: This class has implement methods to setup/teardown Selenium Server
 * and client.
 */

package com.ags.aft.frankensteinDriver.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ags.aft.exception.AFTException;
import com.thoughtworks.frankenstein.drivers.DefaultFrankensteinDriver;
import com.thoughtworks.frankenstein.drivers.FrankensteinDriver;

/**
 * Base class for selenium testcases. This class has basic methods useful for
 * all selenium testcases.
 */
public final class AFTFrankensteinBase{
	/** The logger. */
	private static final Logger LOGGER = Logger
			.getLogger(AFTFrankensteinBase.class);

	/** The url of the application. */
	private String sBaseClass;

	/** The driver. */
	private FrankensteinDriver driver;

	/** Application arguments**/
	public static String[] applicationargs=null;


	// Instance object
	private static AFTFrankensteinBase aftFrankenstein;

	/**
	 * Instantiates a new aft helper.
	 */
	private AFTFrankensteinBase() {
		super();
	}

	/**
	 * Gets the single instance of AftHelper.
	 * 
	 * @return single instance of AftHelper
	 */
	public static AFTFrankensteinBase getInstance() {
		
		if (aftFrankenstein == null) {
			aftFrankenstein = new AFTFrankensteinBase();
		
			LOGGER.trace("Creating instance of AFTFrankensteinBase");
		}

		return aftFrankenstein;
	}

	public static void  removeInstance() {
		
		aftFrankenstein =null;
		LOGGER.trace("Removing instance of AFTFrankensteinBase");
	}
	/**
	 * Init and Start Selenium.
	 * 
	 * @param sBrowserType
	 *            : Browser type
	 * @param sBaseClass
	 *            : Application url
	 * @throws AFTException
	 *             the exception
	 * @throws ClassNotFoundException
	 */
	public void setUpFrankenstein(String sBaseClass) throws AFTException,
			ClassNotFoundException {
		this.sBaseClass = sBaseClass;
		if(null==applicationargs){
			applicationargs = new String[] {};
		}
		if (driver == null) {
			LOGGER.info("Launching the Applicaton");
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			driver = new DefaultFrankensteinDriver(
					Class.forName(this.sBaseClass), applicationargs);

		}
	}

	
	public void loadJNLP(String jnlpFilePath)throws Exception{
		if(!jnlpFilePath.isEmpty()){
			try{
				File file =new File(jnlpFilePath);
				if(!file.exists()){
					throw new Exception(jnlpFilePath+" jnlp file not found!!!!");
				}
				BufferedReader reader=new BufferedReader(new FileReader(file));
				StringBuffer stringbuffer=new StringBuffer();
				String s="";
				while((s=reader.readLine())!=null){
					if(!s.contains("j2se")){
						stringbuffer.append(s+"\n");
					}
				}
				reader.close();
				BufferedWriter bw=new BufferedWriter(new FileWriter(file));
				bw.write(stringbuffer.toString());
				bw.close();
				
				DocumentBuilderFactory builderFactory=DocumentBuilderFactory.newInstance();
				DocumentBuilder builder=builderFactory.newDocumentBuilder();
				Document doc=builder.parse(file);
				NodeList list=doc.getElementsByTagName("argument");
				List<String> parameters=new ArrayList<String>();
				for(int i=0;i<list.getLength();i++){
					Node node=list.item(i);
					parameters.add(node.getTextContent());
				}
				applicationargs=parameters.toArray(new String[parameters.size()]);
			}catch(Exception e){
				e.printStackTrace();
				throw new Exception("Exception occured in loading the jnlp file:: "+jnlpFilePath);
			}
		}
	}


	/**
	 * Stops selenium Instance.
	 */
	public void stopFrankenstein() {
			try {
				LOGGER.info("Stopping the Frankenstain");
				driver.closeAllDialogs();
				driver.terminate();
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
			}finally{
				driver = null;
		//		AFTFrankensteinBase.removeInstance();
			}
	}

	/**
	 * Tears down the selenium and selenium server objects.
	 */
	public void teardown() {
		LOGGER.trace("Calling stopFrankenstein()");
		stopFrankenstein();
	}

	/**
	 * Gets the driver.
	 * 
	 * @return the driver
	 */
	public FrankensteinDriver getDriver() {
		return driver;
	}

}
