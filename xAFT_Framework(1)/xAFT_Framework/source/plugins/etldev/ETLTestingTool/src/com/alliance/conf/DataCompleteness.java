package com.alliance.conf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class DataCompleteness {

	private static final Logger LOG = LoggerFactory.getLogger(DataCompleteness.class);
	
		//Method to get Count if user is passing only the query
		public String getCount(final String leftQuery, final String rightQuery,
				final Connection leftConnection, final Connection rightConnection) throws SQLException {
			
			Gson gson = new Gson();		
			final Map<String, Object> result = Maps.newHashMap();
			final List<Map<String, Integer>> lists = Lists.newArrayList();
			final Map<String, Integer> count = Maps.newHashMap();		
			count.put("source", getCountForRecords(leftQuery, leftConnection));
			count.put("target", getCountForRecords(rightQuery, rightConnection));
			LOG.info("Source Count -> " + count.get("source"));
			LOG.info("Destination Count -> " + count.get("target"));
			lists.add(count);
			result.put("dataCompleteness", lists);		
			return gson.toJson(result);
		}
		
		//Method to get Count if user is passing the count(*) from query
		public String getIntegerCount(final String leftQuery, final String rightQuery,
				final Connection leftConnection, final Connection rightConnection) throws SQLException {
			
			Gson gson = new Gson();		
			final Map<String, Object> result = Maps.newHashMap();
			final List<Map<String, Integer>> lists = Lists.newArrayList();
			final Map<String, Integer> count = Maps.newHashMap();		
			count.put("source", getCountFromQuery(leftQuery, leftConnection));
			count.put("target", getCountFromQuery(rightQuery, rightConnection));
			LOG.info("Source Count -> " + count.get("source"));
			LOG.info("Destination Count -> " + count.get("target"));
			lists.add(count);
			result.put("dataCompleteness", lists);
			return gson.toJson(result);
		}
		
		private int getCountFromQuery(final String query,
				final Connection connection) throws SQLException {
			
			final Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			final ResultSet resultSet = stmt.executeQuery(query);
			
			while (resultSet.next()) {
				if (resultSet.first()) {
					return resultSet.getInt(1);
				}				
			}			
			return 0;
		}
		
		private Integer getCountForRecords(final String query,
				final Connection connection) throws SQLException {
			
			final Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			
			final ResultSet resultSet = stmt.executeQuery(query);
			
			resultSet.last();
			final int count = resultSet.getRow();
			
			ETLSqlUtil.close(stmt);
			ETLSqlUtil.close(resultSet);
			//ETLSqlUtil.close(connection);
			return count;
		}
}
