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
 * Class: TCMIntegration
 * 
 * Purpose: Dynamically loads the JAR file
 */
package com.ags.aft.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

public class DynamicClassLoader extends URLClassLoader {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(DynamicClassLoader.class);

	/**
	 * Instantiates a new dynamic class loader.
	 * 
	 * @param url
	 *            the url
	 */
	public DynamicClassLoader(URL[] url) {
		super(url);
	}

	/**
	 * add the Jar file to the classpath.
	 * 
	 * @param path
	 *            the path
	 * @throws MalformedURLException
	 *             the malformed url exception
	 */
	public void addFile(String path) throws MalformedURLException {
		// construct the jar url path
		String urlPath = "jar:file:" + path + "!/";

		// invoke the base method
		addURL(new URL(urlPath));
	}

	/**
	 * add the Jar file to the classpath
	 * 
	 * @param paths
	 *            []
	 * 
	 * @throws MalformedURLException
	 */
	public void addFile(String paths[]) throws MalformedURLException {
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				addFile(paths[i]);
			}
		}
	}

	/**
	 * Load jar.
	 * 
	 * @param jarToLoad
	 *            the jar to load
	 * @param classToLoad
	 *            the class to load
	 * @return the class
	 * @throws MalformedURLException
	 *             the malformed url exception
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public Class<?> loadJar(String jarToLoad, String classToLoad)
			throws MalformedURLException, ClassNotFoundException {
		// Load the jar at run time
		LOGGER.debug("Loading the jarfile [" + jarToLoad + "] in Runtime");
		addFile(jarToLoad);
		LOGGER.debug("Completed loading the jarfile [" + jarToLoad
				+ "] in Runtime");
		// Load the class at runTime
		LOGGER.debug("Loading the class [" + classToLoad + "]");
		Class<?> dynamicClass = loadClass(classToLoad);
		LOGGER.debug("Completed loading the class [" + classToLoad + "]");
		return dynamicClass;

	}

}
