package com.alliance.conf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ETLCompareFlatFileMap {

	public static Connection connection2() throws ClassNotFoundException,
			SQLException

	{
		String DriverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String url = "jdbc:sqlserver://HYD-MASHRAF:1433;databaseName=emp;";
		String DBuser = "ashraf";
		String DBpassword = "alliance123$";
		Class.forName(DriverName);
		Connection con = DriverManager.getConnection(url, DBuser, DBpassword);
		System.out.println("Connected Successfully");
		return con;
	}
	 public static List<Map<String, Object>> fileToObject() throws IOException {   
	     
	        List<Map<String, Object>> listLeft = new ArrayList<Map<String,Object>>();				
	        BufferedReader in = new BufferedReader(new FileReader("e:\\source.txt"));
	        String line = "";
	        String colNames=in.readLine();
	        String colNamess[]=colNames.split(",");
	        System.out.println(colNamess.length);
	        while ((line = in.readLine()) != null) {
	        	 Map<String, Object> map = new HashMap<String, Object>();
	            String values[] = line.split(",");
	           for (int i=0;i<colNamess.length;i++)
	           {
	        	   map.put(colNamess[i], values[i].trim()); 
	        	   
	           }
	           listLeft.add(map);
	          
	        }
	        in.close();
	       return listLeft; 
	    }
	 public static void compareMap() throws ClassNotFoundException, SQLException, IOException
	 {   
		 Connection con = connection2();
		 String targetQuery="select * from sample";
		 List<Map<String, Object>> listRight =ETLFlatFileCompareUtil.readRows(targetQuery, con); 
		 List<Map<String, Object>> listLeft =fileToObject();
		 final List<Map<String, Object>> matchedList = Lists.newArrayList();		
			final List<Map<String, Map<String, Object>>> unmatchedList = Lists.newArrayList();		
			
			for (int i = 0,j=0; i < listLeft.size() && j < listRight.size(); i++,j++) {
				
				final Map<String, Map<String, Object>> unmatchedMap = Maps.newHashMap();
				MapDifference<String, Object> mapDifference = Maps.difference(listLeft.get(i), listRight.get(j));
				
				if(mapDifference.areEqual()) {
					matchedList.add(listLeft.get(i));
				} else {
					unmatchedMap.put("left", listLeft.get(i));
					unmatchedMap.put("right", listRight.get(i));
					unmatchedList.add(unmatchedMap);
				}
				
			}
			System.out.println(matchedList);
			System.out.println(unmatchedList);
			
	 }

}
