package com.alliance.schema.xmlvalidation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.alliance.conf.CustomCompareUtil;
import com.alliance.conf.ETLSqlUtil;
import com.alliance.schema.xmlvalidation.Database.Tables;
import com.alliance.schema.xmlvalidation.Database.Tables.Table;
import com.alliance.schema.xmlvalidation.Database.Tables.Table.Field;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class SchemaValidation {

	public Document xmlValidation(String sourceXmlFile, Connection connection)
			throws Exception {

		final Statement statement = connection.createStatement();
		List<Table> sourceTables = new ArrayList<Table>();
		final DatabaseMetaData databaseMetadata = connection.getMetaData();
		final JAXBContext jaxbContext = JAXBContext
				.newInstance("com.alliance.schema.xmlvalidation");
		final Unmarshaller um = jaxbContext.createUnmarshaller();
		// Unmarshal XML contents of the file myDoc.xml into your Java
		// object instance.
		final Database sourceObject = (Database) um
				.unmarshal(new FileInputStream(sourceXmlFile));

		final ObjectFactory targetFactory = new ObjectFactory();
		final Database targetDatabase = (Database) targetFactory
				.createDatabase();
		sourceTables = sourceObject.getTables().getTable();
		targetDatabase.setName(sourceObject.getName());
		targetDatabase.setSchema(sourceObject.getSchema());

		Document document = compareSchemaAndGenerateXml(statement, sourceTables, databaseMetadata,
				sourceObject);
		ETLSqlUtil.close(statement);
		//ETLSqlUtil.close(connection);
		return document;
	}

	/**
	 * @param target
	 * @param source
	 * @param statement
	 * @param sourceTables
	 * @param databaseMetadata
	 * @param sourceObject
	 * @param targetXml 
	 * @return
	 * @throws SQLException
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException 
	 */
	private Document compareSchemaAndGenerateXml(
			final Statement statement, final List<Table> sourceTables,
			final DatabaseMetaData databaseMetadata, final Database sourceObject)
			throws SQLException, JAXBException, FileNotFoundException, ParserConfigurationException {
		final JAXBContext jaxbContext = JAXBContext
				.newInstance("com.alliance.schema.xmlvalidation");
		final Marshaller marshall = jaxbContext.createMarshaller();

		ObjectFactory targetObjectFactory = new ObjectFactory();
		Database marshalJAXBObject = (Database) targetObjectFactory
				.createDatabase();
		Tables targetTables = targetObjectFactory.createDatabaseTables();
		List<Table> tableList = new ArrayList<Table>();
		
		final Map<String, Map<String, List<Field>>> target = Maps.newHashMap();
		final Map<String, Map<String, List<Field>>> source = Maps.newHashMap();
		String tableName;
		for (int i = 0; i < sourceTables.size(); i++) {
			final List<Field> elementsList = Lists.newArrayList();
			tableName = sourceTables.get(i).getName();
			final ResultSet tableResultSet = databaseMetadata.getTables(null,
					null, tableName, null);
			ResultSet resultSet = statement.executeQuery("SELECT TOP 1 * FROM "
					+ sourceObject.getSchema()+"." + tableName);

			Map<String, List<Field>> sourceColumns = Maps.newHashMap();
			final Map<String, List<Field>> targetColumns = Maps.newHashMap();
			while (tableResultSet.next()) {
				List<Field> fieldListSource = new ArrayList<Field>();
				fieldListSource = sourceObject.getTables().getTable().get(i)
						.getField();

				final List<String> actualColumnNames = CustomCompareUtil
						.readColumnNames(resultSet);

				sourceColumns = createElementsForSource(fieldListSource);
				
				final MapDifference<String, Collection<Object>> mapDiff = Maps
						.difference(
								getRowMap(fieldListSource),
								getRowMap(resultSet, actualColumnNames,
										tableName));

				if (!mapDiff.entriesInCommon().isEmpty()) {
					createMatchedSchemaTargetElements(elementsList,
							getRowMap(resultSet, actualColumnNames, tableName),
							mapDiff);
				}
				if (!mapDiff.entriesDiffering().isEmpty()) {

					createUnmatchedSchemaTargetElements(elementsList,
							getRowMap(resultSet, actualColumnNames, tableName),
							mapDiff);
				}
				/*
				 * if(!mapDiff.entriesOnlyOnLeft().isEmpty()) {
				 * createMissingColumnsSchemaElements(elementsList,
				 * getRowMap(fieldListSource), mapDiff); }
				 */

				targetColumns.put("columns", elementsList);

				Table databaseTable = targetObjectFactory
						.createDatabaseTablesTable();

				databaseTable.setField(elementsList);

				tableList.add(databaseTable);

				target.put(tableName, targetColumns);

				source.put(tableName, sourceColumns);

			}
			targetTables.setTable(tableList);
			marshalJAXBObject.setTables(targetTables);
			ETLSqlUtil.close(resultSet);
			ETLSqlUtil.close(tableResultSet);
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.newDocument();
			
			marshall.marshal(marshalJAXBObject, document);
			return document;
		} catch (ParserConfigurationException e) {			
			throw new ParserConfigurationException("Unable to marshal the Object");
		}		
	}

	


	/**
	 * @param elementsList
	 * @param actualMap
	 * @param mapDiff
	 */
	private static void createUnmatchedSchemaTargetElements(
			final List<Field> elementsList,
			final Map<String, Collection<Object>> actualMap,
			final MapDifference<String, Collection<Object>> mapDiff) {

		for (final String key : mapDiff.entriesDiffering().keySet()) {
			elementsList.add(getSchemaElementForUnmatched(key, actualMap));
		}
	}

	/**
	 * @param elementsList
	 * @param actualMap
	 * @param mapDiff
	 */
	private static void createMatchedSchemaTargetElements(
			final List<Field> elementsList,
			final Map<String, Collection<Object>> actualMap,
			final MapDifference<String, Collection<Object>> mapDiff) {
		for (final String key : mapDiff.entriesInCommon().keySet()) {
			elementsList.add(getSchemaElementMatched(key, actualMap));
		}
	}

	private static Field getSchemaElementMatched(String key,
			Map<String, Collection<Object>> actualMap) {

		return new Field(key, String.valueOf(actualMap.get(key).toArray()[0]),
				String.valueOf(actualMap.get(key).toArray()[1]),
				String.valueOf(actualMap.get(key).toArray()[2]));
	}

	private static Map<String, List<Field>> createElementsForSource(
			final List<Field> fieldListSource) {

		final Map<String, List<Field>> dataMap = Maps.newHashMap();
		final List<Field> listData = Lists.newArrayList();
		for (final Field field : fieldListSource) {
			listData.add(getSchemaElement(field));
		}
		dataMap.put("columns", listData);
		return dataMap;
	}

	private static Field getSchemaElement(final Field field) {
		return new Field(field.name, field.type, field.size, field.tablename);
	}

	
	private static Field getSchemaElementForUnmatched(final String key,
			final Map<String, Collection<Object>> actualMap) {
		return new Field(key, String.valueOf(actualMap.get(key).toArray()[1]),
				String.valueOf(actualMap.get(key).toArray()[0]),
				String.valueOf(actualMap.get(key).toArray()[2]));
	}

	private static Map<String, Collection<Object>> getRowMap(
			final List<Field> fieldListSource) {

		final Multimap<String, Object> rowMap = ArrayListMultimap.create();
		for (final Field field : fieldListSource) {
			rowMap.put(field.name, field.type);
			rowMap.put(field.name, field.size);

		}
		return rowMap.asMap();
	}

	private static Map<String, Collection<Object>> getRowMap(
			final ResultSet resultSet, final List<String> actualColumnNames,
			String tableName) throws SQLException {

		final Multimap<String, Object> rowMap = ArrayListMultimap.create();

		for (final String columnName : actualColumnNames) {
			rowMap.put(
					columnName,
					resultSet.getMetaData().getColumnTypeName(
							resultSet.findColumn(columnName)));
			rowMap.put(
					columnName,
					String.valueOf(resultSet.getMetaData().getPrecision(
							resultSet.findColumn(columnName))));
			rowMap.put(columnName, tableName);
		}
		return rowMap.asMap();
	}

}
