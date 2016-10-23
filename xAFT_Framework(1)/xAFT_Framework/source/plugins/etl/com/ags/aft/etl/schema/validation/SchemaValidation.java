package com.ags.aft.etl.schema.validation;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang.builder.CompareToBuilder;

import com.ags.aft.etl.conf.CustomCompareUtil;
import com.ags.aft.etl.conf.ETLSqlUtil;
import com.ags.aft.etl.schema.validation.Database.Tables.Table;
import com.ags.aft.etl.schema.validation.Database.Tables.Table.Field;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

public class SchemaValidation {

	public SchemaValidation() {

	}

	public Map<String, Object> xmlValidation(String sourceXmlFile,
			Connection connection) throws Exception {

		final Statement statement = connection.createStatement();
		List<Table> sourceTables = new ArrayList<Table>();

		final DatabaseMetaData databaseMetadata = connection.getMetaData();
		final JAXBContext jaxbContext = JAXBContext
				.newInstance("com.ags.aft.etl.schema.validation");

		// Create unmarshaller
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		// Unmarshal XML contents of the file myDoc.xml into your Java
		// object instance.
		final Database sourceObject = (Database) unmarshaller
				.unmarshal(new FileInputStream(sourceXmlFile));

		final ObjectFactory targetFactory = new ObjectFactory();
		final Database targetDatabase = (Database) targetFactory
				.createDatabase();
		sourceTables = sourceObject.getTables().getTable();
		targetDatabase.setName(sourceObject.getName());
		targetDatabase.setSchema(sourceObject.getSchema());

		Map<String, Object> result = compareSchemaAndGenerateJson(statement,
				sourceTables, databaseMetadata, sourceObject);
		ETLSqlUtil.close(statement);
		// ETLSqlUtil.close(connection);
		return result;
	}

	/**
	 * @param target
	 * @param source
	 * @param statement
	 * @param sourceTables
	 * @param databaseMetadata
	 * @param sourceObject
	 * @return
	 * @throws SQLException
	 */
	private Map<String, Object> compareSchemaAndGenerateJson(
			final Statement statement, final List<Table> sourceTables,
			final DatabaseMetaData databaseMetadata, final Database sourceObject)
			throws SQLException {

		final List<SchemaElements> sourceList = Lists.newArrayList();
		final List<SchemaElements> targetList = Lists.newArrayList();
		boolean isPassed = false;

		String tableName;
		// int flag1 = 0;
		for (int i = 0; i < sourceTables.size(); i++) {
			final List<SchemaElements> elementsList = Lists.newArrayList();
			tableName = sourceTables.get(i).getName();
			final ResultSet tableResultSet = databaseMetadata.getTables(null,
					null, tableName, null);
			ResultSet resultSet = statement.executeQuery("SELECT TOP 1 * FROM "
					+ sourceObject.getSchema() + "." + tableName);
			List<SchemaElements> sourceColumns = Lists.newArrayList();

			while (tableResultSet.next()) {
				List<Field> fieldListSource = new ArrayList<Field>();
				fieldListSource = sourceObject.getTables().getTable().get(i)
						.getField();

				final List<String> actualColumnNames = CustomCompareUtil
						.readColumnNames(resultSet);

				sourceColumns = createElementsForSource(fieldListSource);
				// Map<String, Collection<Object>> expectedMap =
				// getRowMap(fieldListSource);
				// Map<String, Collection<Object>> actualMap =
				// getRowMap(resultSet, actualColumnNames);
				final MapDifference<String, Collection<Object>> mapDiff = Maps
						.difference(getRowMap(fieldListSource),
								getRowMap(resultSet, actualColumnNames));

				if (!mapDiff.entriesInCommon().isEmpty()) {
					createMatchedSchemaTargetElements(elementsList,
							getRowMap(resultSet, actualColumnNames), mapDiff,
							tableName);
				}
				if (!mapDiff.entriesDiffering().isEmpty()) {
					createUnmatchedSchemaTargetElements(elementsList,
							getRowMap(resultSet, actualColumnNames), mapDiff,
							tableName);
				}
				if (!mapDiff.entriesOnlyOnLeft().isEmpty()) {
					createMissingColumnsSchemaElements(elementsList,
							getRowMap(fieldListSource), mapDiff, tableName);
				}

				if (mapDiff.entriesDiffering().isEmpty()
						&& mapDiff.entriesOnlyOnLeft().isEmpty()) {
					isPassed = true;
				}

				sourceList.addAll(sourceColumns);
				targetList.addAll(elementsList);

			}
			ETLSqlUtil.close(resultSet);
			ETLSqlUtil.close(tableResultSet);
		}
		return createJsonForSchemaElements(targetList, sourceList, isPassed);
	}

	/**
	 * @param targetList
	 * @param sourceList
	 * @param isPassed
	 * @return
	 */
	private Map<String, Object> createJsonForSchemaElements(
			final List<SchemaElements> targetList,
			final List<SchemaElements> sourceList, boolean isPassed) {

		final Map<String, Object> jsonData = Maps.newHashMap();
		final Map<String, Object> result = Maps.newHashMap();
		final Gson gson = new Gson();
		sortElements(sourceList);
		sortElements(targetList);
		jsonData.put("source", sourceList);
		jsonData.put("target", targetList);
		result.put("status", isPassed);
		result.put("result", gson.toJson(jsonData));
		return result;
	}

	/**
	 * @param elementsList
	 */
	private void sortElements(final List<SchemaElements> elementsList) {
		Collections.sort(elementsList, new Comparator<SchemaElements>() {

			@Override
			public int compare(SchemaElements left, SchemaElements right) {

				return new CompareToBuilder()
						.append(left.getTableName(), right.getTableName())
						.append(left.getName(), right.getName()).toComparison();
			}
		});
	}

	/**
	 * @param elementsList
	 * @param expectedMap
	 * @param mapDiff
	 * @param tableName
	 */
	private void createMissingColumnsSchemaElements(
			final List<SchemaElements> elementsList,
			final Map<String, Collection<Object>> expectedMap,
			final MapDifference<String, Collection<Object>> mapDiff,
			String tableName) {

		for (final String key : mapDiff.entriesOnlyOnLeft().keySet()) {
			elementsList.add(getSchemaElement(key, expectedMap, tableName));
		}
	}

	/**
	 * @param elementsList
	 * @param actualMap
	 * @param mapDiff
	 * @param tableName
	 */
	private void createUnmatchedSchemaTargetElements(
			final List<SchemaElements> elementsList,
			final Map<String, Collection<Object>> actualMap,
			final MapDifference<String, Collection<Object>> mapDiff,
			String tableName) {

		for (final String key : mapDiff.entriesDiffering().keySet()) {
			elementsList.add(getSchemaElementForUnmatched(key, actualMap,
					tableName));
		}
	}

	/**
	 * @param elementsList
	 * @param actualMap
	 * @param mapDiff
	 * @param tableName
	 */
	private void createMatchedSchemaTargetElements(
			final List<SchemaElements> elementsList,
			final Map<String, Collection<Object>> actualMap,
			final MapDifference<String, Collection<Object>> mapDiff,
			String tableName) {
		for (final String key : mapDiff.entriesInCommon().keySet()) {
			elementsList
					.add(getSchemaElementMatched(key, actualMap, tableName));
		}
	}

	private SchemaElements getSchemaElementMatched(String key,
			Map<String, Collection<Object>> actualMap, String tableName) {

		return new SchemaElements(key, String.valueOf(actualMap.get(key)
				.toArray()[0]),
				String.valueOf(actualMap.get(key).toArray()[1]), tableName,
				"PASS");
	}

	private List<SchemaElements> createElementsForSource(
			final List<Field> fieldListSource) {

		final List<SchemaElements> listData = Lists.newArrayList();
		for (final Field field : fieldListSource) {
			listData.add(getSchemaElement(field));
		}

		return listData;
	}

	private SchemaElements getSchemaElement(final Field field) {
		return new SchemaElements(field.name, field.type, field.size, null,
				null, field.tablename);
	}

	private SchemaElements getSchemaElement(final String key,
			final Map<String, Collection<Object>> actualMap, String tableName) {

		return new SchemaElements(key, "Not Found", "Not Found", tableName,
				"FAIL");
	}

	private SchemaElements getSchemaElementForUnmatched(final String key,
			final Map<String, Collection<Object>> actualMap, String tableName) {
		return new SchemaElements(key, String.valueOf(actualMap.get(key)
				.toArray()[0]),
				String.valueOf(actualMap.get(key).toArray()[1]), tableName,
				"FAIL");
	}

	private Map<String, Collection<Object>> getRowMap(
			final List<Field> fieldListSource) {

		final Multimap<String, Object> rowMap = LinkedListMultimap.create();
		for (final Field field : fieldListSource) {
			rowMap.put(field.name, field.type);
			rowMap.put(field.name, field.size);
		}
		return rowMap.asMap();
	}

	private Map<String, Collection<Object>> getRowMap(
			final ResultSet resultSet, final List<String> actualColumnNames)
			throws SQLException {

		final Multimap<String, Object> rowMap = LinkedListMultimap.create();

		for (final String columnName : actualColumnNames) {
			rowMap.put(
					columnName,
					resultSet.getMetaData().getColumnTypeName(
							resultSet.findColumn(columnName)));
			rowMap.put(
					columnName,
					String.valueOf(resultSet.getMetaData().getPrecision(
							resultSet.findColumn(columnName))));
		}
		return rowMap.asMap();
	}

}
