package com.alliance.conf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;

import com.google.common.collect.Maps;

public class ETLSqlUtil {
	
		/**
	    * calls executeQuery(sql_, connection_, 0)
	    */
	   public static ResultSet executeQuery(String sql_, Connection connection_)
	      throws SQLException {
	      return executeQuery(sql_, connection_, 0);
	   }

	   public static ResultSet executeQuery(String sql_, Connection connection_,
	                                        int fetchSize_) throws SQLException {
	      
	      if ((sql_ == null) || (connection_ == null))
	         return null;
	      Statement statement = createStatement(connection_);
	      if (statement == null)
	         return null;

	      statement.setFetchSize(fetchSize_);
	      return statement.executeQuery(sql_);
	   }

	   /**
	    * null and Exception safe
	    */
	   public static Statement createStatement(Connection connection_) {
	      if (connection_ == null)
	         return null;
	      try {
	         return connection_.createStatement();
	      }
	      catch (Exception e_) {	         
	         return null;
	      }
	   }
	   
	   /**
	    * null and Exception safe
	    */
	   public static void close(Statement statement_) {
	      if (statement_ == null)
	         return;

	      try {
	         statement_.close();
	      }
	      catch (Exception e_) {
	    	  
	      }
	   }

	   /**
	    * null and Exception safe
	    */
	   public static void close(ResultSet resultSet_) {
	      if (resultSet_ == null)
	         return;

	      try {
	         resultSet_.close();
	      }
	      catch (Exception e_) {
	         //LOG.warn(null, e_);
	      }
	   }

	   /**
	    * null and Exception safe
	    */
	   public static void close(Connection connection_) {
	      if (connection_ == null)
	         return;

	      try {
	         connection_.close();
	      }
	      catch (Exception e_) {
	         //LOG.warn(null, e_);
	      }
	   }
	   
	   /**
	    * does not close connection_
	    */
	  /* public static List<Map<String, Object>> readRows(String selectSql_, Connection connection_)
	      throws SQLException {
	      if ((selectSql_ == null) || (connection_ == null))
	         return null;
	      ResultSet resultSet = executeQuery(selectSql_, connection_);
	      if (resultSet == null)
	         return null;
	      //List<Map<String, Object>> rows = readRows(resultSet);
	      close(resultSet);
	      return rows;
	   }*/
	   
	   public static Map<String, Object> readRows(ResultSet resultSet_) throws SQLException {
		      return readRows(resultSet_, false);
		   }

		   /**
		    * does not close resultSet_
		    */
		   public static Map<String, Object> readRows(ResultSet resultSet_, boolean keysUpper_)
		      throws SQLException {
		      if (resultSet_ == null)
		         return null;
		      SQLWarning warnings = resultSet_.getWarnings();
		      if (warnings != null) {
		         
		         return null;
		      }
		      String[] columnNames = getColumnNames(resultSet_);
		      if (columnNames == null) {
		         
		         return null;
		      }

		     return getRowMap(columnNames, resultSet_, keysUpper_);
		        
		      
		   }

		   public static Map<String, Object> getRowMap(String[] columnNames_, ResultSet resultSet_,
		                                          boolean keysUpper_) throws SQLException {
		      if ((columnNames_ == null) || (resultSet_ == null))
		         return null;

		      Map<String, Object> rowMap = Maps.newLinkedHashMap();
		      /*for (String columnName : columnNames_) {
		         Object columnValue = getColumnValue(columnName, resultSet_);
		         
		         if (keysUpper_)
		            columnName = columnName.toUpperCase();
		         rowMap.put(columnName, columnValue);
		      }*/
		      
		      for (int i = 0; i < columnNames_.length; i++) {
		    	  Object columnValue = getColumnValue(columnNames_[i], resultSet_, i);
		    	  
		    	  if (keysUpper_)
		    		  columnNames_[i] = columnNames_[i].toUpperCase();
			         rowMap.put(columnNames_[i], columnValue);
		      }
		      

		      return rowMap;
		   }

		   public static Object getColumnValue(String columnName_, ResultSet resultSet_, int i) {
		      if ((columnName_ == null) || (resultSet_ == null))
		         return null;
		     
		      try {
		    	  if("varbinary".equalsIgnoreCase(resultSet_.getMetaData().getColumnTypeName(i+1)) || 
		    			  "hierarchyid".equalsIgnoreCase(resultSet_.getMetaData().getColumnTypeName(i+1))) {
		    		  return convertToHexString(resultSet_.getString(columnName_));
		    	  }
		    	  return resultSet_.getString(columnName_);
		    	  
		      }
		      catch (Exception e_) {
		         
		         if (e_.getClass().getName().equalsIgnoreCase(
		            "com.microsoft.sqlserver.jdbc.SQLServerException"))
		            return null;
		         throw new RuntimeException(e_);
		      }
		   }
		   
		   
		   //Backup
		   public static Object getColumnValue(String columnName_, ResultSet resultSet_) {
			      if ((columnName_ == null) || (resultSet_ == null))
			         return null;
			     
			      try {			    	  
			    	  return resultSet_.getString(columnName_);
			    	  
			      }
			      catch (Exception e_) {
			         
			         if (e_.getClass().getName().equalsIgnoreCase(
			            "com.microsoft.sqlserver.jdbc.SQLServerException"))
			            return null;
			         throw new RuntimeException(e_);
			      }
			   }
		   
		   public static String[] getColumnNames(ResultSet resultSet_) throws SQLException {
			      if (resultSet_ == null)
			         return null;

			      ResultSetMetaData metaData = resultSet_.getMetaData();
			      int columnCount = metaData.getColumnCount();
			      if (columnCount < 1)
			         return null;
			      String[] columnNames = new String[columnCount];
			      for (int i = 1; i <= columnCount; i++) {
			         columnNames[i - 1] = metaData.getColumnLabel(i);
			      }
			      return columnNames;
		   }

		public static Map<String, Object> readRows(ResultSet resultSet_,
				Map<String, Object> map) throws SQLException {
			
			return readRows(resultSet_, map, false);			
		}

		private static Map<String, Object> readRows(ResultSet resultSet_,
				Map<String, Object> map, boolean keysUpper_) throws SQLException {
			
			if (resultSet_ == null)
		         return null;
		      SQLWarning warnings = resultSet_.getWarnings();
		      if (warnings != null) {
		         
		         return null;
		      }
		      String[] columnNames = getColumnNames(resultSet_);
		      if (columnNames == null) {
		         
		         return null;
		      }

		    return getRowMap(columnNames, map, resultSet_, keysUpper_);			
		}

		private static Map<String, Object> getRowMap(String[] columnNames_,
				Map<String, Object> rowMap, ResultSet resultSet_,
				boolean keysUpper_) {
			
			if ((columnNames_ == null) || (resultSet_ == null))
		         return null;

		      
			  for (int i = 0; i < columnNames_.length; i++) {
		    	  Object columnValue = getColumnValue(columnNames_[i], resultSet_, i);
		    	  
		    	  if (keysUpper_)
		    		  columnNames_[i] = columnNames_[i].toUpperCase();
			         rowMap.put(columnNames_[i], columnValue);
		      }

		      return rowMap;			
		}

		private static String convertToHexString(String data) {
			
			if(null== data) 
				return null;
			
			StringBuffer buf = new StringBuffer();
			buf.append("0x");
			buf.append(data);
			return buf.toString();
		}
		
		
		/*private static String convertToHexString(byte[] data) {
			
			if(null== data || data.length <= 0) 
				return null;
			
			StringBuffer buf = new StringBuffer();
			buf.append("0x");
			
			for (int i = 0; i < data.length; i++) {
			    int halfbyte = (data[i] >>> 4) & 0x0F;
			    int two_halfs = 0;
			    do {
			        if ((0 <= halfbyte) && (halfbyte <= 9))
			            buf.append((char) ('0' + halfbyte));
			        else
			            buf.append((char) ('a' + (halfbyte - 10)));
			            halfbyte = data[i] & 0x0F;
			        } while(two_halfs++ < 1);
			    }
			return buf.toString();
		}*/
		   
		   
		

}
