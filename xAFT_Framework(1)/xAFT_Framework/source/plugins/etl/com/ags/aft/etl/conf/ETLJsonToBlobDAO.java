package com.ags.aft.etl.conf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ETLJsonToBlobDAO {

	private static final Logger LOG = LoggerFactory.getLogger(ETLJsonToBlobDAO.class);
	
	public static Connection getMysqlJDBCConnection() {
		 Connection con=null;
		  try {   
		  Class.forName("com.mysql.jdbc.Driver");   
		 } catch (java.lang.ClassNotFoundException e) {  
			 LOG.debug("ClassNotFoundException: ");
			 LOG.debug(e.getMessage());		  
	  }   
	  
	 try {   
		   con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "password");   
		 } catch (SQLException ex) {   
			 LOG.debug("SQLException: ");
			 LOG.debug(ex.getMessage());		      
		  }		  
		  return con;
	}
	
	//method to insert matched records into database
	public static void insertJsonBlob(final String matched, final String unmatched) throws SQLException {
		
	    Connection conn=getMysqlJDBCConnection();
	    PreparedStatement preps;
	    preps = conn.prepareStatement("insert into jsonblob(matchedRecords,unmatchedRecords) values (?,?)");	     
	    preps.setObject(1,matched);
	    preps.setObject(2,unmatched);
	    preps.execute();
	}

}
