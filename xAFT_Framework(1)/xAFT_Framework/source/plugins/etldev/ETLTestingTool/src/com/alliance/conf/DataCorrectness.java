package com.alliance.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.diffkit.db.DKDBConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCorrectness  {

	
	private static final Logger LOG = LoggerFactory.getLogger(DataCorrectness.class);
	
	public Map<String, String> compareDataMap(final String left, final String right,
			final Connection conn, final Connection conn2, 
			final DKDBConnectionInfo lhsConnInfo_, 
			final DKDBConnectionInfo rhsConnInfo_) throws Exception {
		
		LOG.info("compare data");
		if(left.contains("{")) {		
			return compareDataUsingId(left, right, lhsConnInfo_, rhsConnInfo_);
		} else {			
			return compareData(left, right, conn, conn2);	
		}
	}


	/**
	 * @param left
	 * @param right
	 * @param conn
	 * @param conn2
	 * @return
	 * @throws SQLException
	 */
	private Map<String, String> compareData(final String left,
			final String right, final Connection conn, final Connection conn2)
			throws SQLException {
		final ETLCompareEngine compareEngine = new ETLCompareEngine();		
		return compareEngine.readAndCompareRows(left, conn, right, conn2);
		//return compareEngine.compareList(ETLSqlUtil.readRows(left, conn), ETLSqlUtil.readRows(right, conn2));
	}


	/**
	 * @param left
	 * @param right
	 * @param lhsConnInfo_
	 * @param rhsConnInfo_
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> compareDataUsingId(final String left,
			final String right, final DKDBConnectionInfo lhsConnInfo_,
			final DKDBConnectionInfo rhsConnInfo_) throws Exception {
		
		final ETLApplication diffApp = new ETLApplication();
		return diffApp.getDiff(left.substring(0,left.indexOf("{")-1), right.substring(0,right.indexOf("{") -1), 
				lhsConnInfo_, rhsConnInfo_, getIdForComparision(left, right));
	}

	//Method to extract Id from query if passed
	private String getIdForComparision(final String left, final String right) {
		
		final String leftId = left.substring(left.indexOf("{") + 1, left.indexOf("}"));
		final String rightId = right.substring(right.indexOf("{") + 1, right.indexOf("}"));
		
		return leftId.equals(rightId) ? leftId : null;
	}

	
}
