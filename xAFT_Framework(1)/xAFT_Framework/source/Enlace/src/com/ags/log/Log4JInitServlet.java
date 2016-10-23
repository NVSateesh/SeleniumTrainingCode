package com.ags.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4JInitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(Log4JInitServlet.class);
	private String logfileName = "Enlace";

	public void init(ServletConfig config) throws ServletException {
		LOGGER.info("Log4JInitServlet is initializing log4j");
		String log4jLocation = config
				.getInitParameter("log4j-properties-location");

		ServletContext sc = config.getServletContext();

		if (log4jLocation == null) {
			LOGGER.warn("*** No log4j-properties-location init param, so initializing log4j with BasicConfigurator");
			BasicConfigurator.configure();
		} else {
			String webAppPath = sc.getRealPath("/");
			String log4jProp = webAppPath + log4jLocation;
			Properties props = new Properties();
			FileInputStream istream = null;
			try {
				istream = new FileInputStream(log4jProp);
				props.load(istream);
				// setLogFileName(props.getProperty("log4j.appender.R.File"));
			} catch (IOException ie) {
				LOGGER.error("Exception::", ie);

			} finally {
				try {
					if (istream != null) {
						istream.close();
					}
				} catch (Exception e) {
					LOGGER.error("Exception::", e);
				}
			}
			String tomcatHome = System.getProperty("catalina.home");
			DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String timestamp = formatter.format(new Date());
			props.setProperty("log4j.appender.R.File", tomcatHome + "/logs"
					+ "//" + logfileName + "_" + timestamp + ".log");
			PropertyConfigurator.configure(props);
		}
		super.init(config);
	}
}
