package com.alliance.conf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class ETLCompareEngine {

	private static final Logger LOG = LoggerFactory.getLogger(ETLCompareEngine.class);
	private int matchedCounter = 0;
	private int unmatchedCounter = 0;
	private int allRecordsTotal = 0;
	
	//method to compare data
	public Map<String, String> readAndCompareRows(String left, Connection conn,
			String right, Connection conn2) throws SQLException {
		
		final List<String> columnsList = Lists.newArrayList();
		Gson gson = new Gson();
		final List<Map<String, Object>> matchedList = Lists.newArrayList();		
		final List<Map<String, Object>> unmatchedList = Lists.newArrayList();
		//Multimap<String, Map<String, Object>> multiMap = ArrayListMultimap.create();
		//final List<String> columnsList = Lists.newArrayList();
		ResultSet leftResultSet = ETLSqlUtil.executeQuery(left, conn);
		ResultSet rightResultSet = ETLSqlUtil.executeQuery(right, conn2);
		
		while (leftResultSet.next() && rightResultSet.next()) {
			allRecordsTotal++;
			if(allRecordsTotal < 2){
				columnsList.addAll(ETLSqlUtil.readRows(leftResultSet).keySet());
			}
			
			if(Maps.difference(ETLSqlUtil.readRows(leftResultSet), ETLSqlUtil.readRows(rightResultSet)).areEqual()) {
				if(matchedCounter < 10000) {
					
					matchedList.add(ETLSqlUtil.readRows(leftResultSet));
					matchedCounter++;
				}
			} else {
				if(unmatchedCounter < 100000) {
					//multiMap.put("left", ETLSqlUtil.readRows(leftResultSet));
					//map.clear();
					
					//multiMap.put("right", ETLSqlUtil.readRows(rightResultSet));
					//unmatchedList.add(ETLSqlUtil.readRows(rightResultSet));	
					unmatchedList.add(getRowMap(ETLSqlUtil.readRows(leftResultSet), ETLSqlUtil.readRows(rightResultSet)));					
					unmatchedCounter++;
				}
			}
		}
		ETLSqlUtil.close(leftResultSet);
		ETLSqlUtil.close(rightResultSet);
		
		//if(multiMap.get("left").
		
		LOG.info("Total Matched Records -> " + matchedList.size());
		Map<String, Object> sourceMap = Maps.newHashMap();
		sourceMap.put("rows", matchedList);
		Map<String, Object> targetMap = Maps.newHashMap();
		targetMap.put("rows", unmatchedList);
		
		Map<String, String> jsonMap = Maps.newHashMap();
		jsonMap.put("matched", gson.toJson(sourceMap));
		jsonMap.put("unmatched", gson.toJson(targetMap));
		jsonMap.put("totalUnmatched", String.valueOf(unmatchedList.size()));
		jsonMap.put("columns", gson.toJson(columnsList));
		return jsonMap;
	}
	
	
	
	
	
	//old method to compare data
	public Map<String, String> compareList(final List<Map<String, Object>> listLeft,
			final List<Map<String, Object>> listRight) throws SQLException {		
		
		final List<Map<String, Object>> matchedList = Lists.newArrayList();		
		final List<Map<String, Object>> unmatchedList = Lists.newArrayList();	
		final List<String> columnsList = Lists.newArrayList();
		
		if(listLeft.size() > 0 && listRight.size() > 0) {
			columnsList.addAll(listLeft.get(0).keySet());			
		}
		
		for (int i = 0,j=0; i < listLeft.size() && j < listRight.size(); i++,j++) {
						
			if(Maps.difference(listLeft.get(i), listRight.get(j)).areEqual()) {	
				matchedList.add(listLeft.get(i));
			} else {
				unmatchedList.add(getRowMap(listLeft.get(i), listRight.get(i)));
			}
		}
		return getJsonMatchedAndUnmatched(matchedList, unmatchedList, columnsList);
	}

	
	//method to create unmatched rows with left and right
	private Map<String, Object> getRowMap(final Map<String, Object> leftMap,
			final Map<String, Object> rightMap) {
		
		final Map<String, Object> unmatchedMap = Maps.newLinkedHashMap();
		unmatchedMap.put("left", leftMap);
		unmatchedMap.put("right", rightMap);
		return unmatchedMap;
	}



	/**
	 * @param matchedList
	 * @param unmatchedList
	 * @param columnsList 
	 * @return 
	 * @throws SQLException
	 */
	private Map<String, String> getJsonMatchedAndUnmatched(
			final List<Map<String, Object>> matchedList,
			final List<Map<String, Object>> unmatchedList, final List<String> columnsList)
			throws SQLException {
		final Map<String, String> resultMap = Maps.newHashMap();
		//final Map<String, Object> matched = Maps.newHashMap();
		final Map<String, Object> unmatched = Maps.newHashMap();
		//matched.put("rows", matchedList);
		//unmatched.put("rows", unmatchedList);
		
		final Gson gson = new Gson();
		resultMap.put("columns", gson.toJson(columnsList));
		resultMap.put("totalUnmatched", String.valueOf(unmatchedList.size()));
		long starttime = System.currentTimeMillis();
		
		//resultMap.put("matched", gson.toJson(matched));
		long endTime = System.currentTimeMillis();
		LOG.info("TIME TAKEN FOR MAKING MATCHED JSON OBJECT -> " + (starttime - endTime));
		resultMap.put("unmatched", gson.toJson(unmatched));
		return resultMap;		
	}
	
	
	
}
