package com.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.utils.SystemUtils.RAMUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class MySqlUtils {

	public static String DB_NAME = "db_hot3l";
	public static final String URL = "jdbc:mysql://localhost:3306";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "";

	public static final String TABLE = "TABLE";
	public static final String TABLE_NAME = "TABLE_NAME";
	public static final String COLUMN_NAME = "COLUMN_NAME";
	public static final String TYPE_NAME = "TYPE_NAME";
	public static final String COLUMN_SIZE = "COLUMN_SIZE";
	public static final String FKTABLE_NAME = "FKTABLE_NAME";
	public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
	public static final String PKTABLE_NAME = "PKTABLE_NAME";
	public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";

	public static Connection connection = null;
	public static Statement statement = null;
	public static DatabaseMetaData databaseMetaData = null;

	public static Map<String, List<String>> primaryKeys = null;
	public static Map<String, List<String>> foreignKeys = null;
	public static Map<String, Integer> queryLimitStart = null;
	public static Map<String, Integer> iteratorLength = null;

	static {
		init(null);
	}

	public static void init(String dbName) {

		System.out.println("MySqlUtils.init()");

		try {
			DB_NAME = dbName;
			Class.forName("com.mysql.jdbc.Driver");
			if (StringUtils.isEmpty(dbName)) {
				connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			} else {
				connection = DriverManager.getConnection(URL + File.separator + DB_NAME, USERNAME, PASSWORD);
			}
			statement = connection.createStatement();
			databaseMetaData = connection.getMetaData();

			primaryKeys = getAllPrimaryKeys(dbName);
			foreignKeys = getAllForeignKeys(dbName);
			queryLimitStart = getAllQueryLimitStart(dbName);
			iteratorLength = getAllItertorLength(dbName);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		return connection;
	}

	public static Statement getStatement2() throws SQLException {

		// statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
		// ResultSet.CONCUR_READ_ONLY);
		statement = connection.createStatement();
		return statement;
	}

	public static Statement getStatement3() throws SQLException {

		// statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
		// ResultSet.CONCUR_READ_ONLY);
		statement = connection.createStatement();
		return statement;
	}

	public static Statement getStatement4() throws SQLException {

		// statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
		// ResultSet.CONCUR_READ_ONLY);
		statement = connection.createStatement();
		return statement;
	}

	public static Statement getStatement() throws SQLException {

		// statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
		// ResultSet.CONCUR_READ_ONLY);
		return connection.createStatement();
	}

	public static Statement getReadStatement() throws SQLException {
		Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
				java.sql.ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);

		return stmt;
	}

	public static DatabaseMetaData getDatabaseMetaData() {
		return databaseMetaData;
	}

	public static void closeConnection(Connection connection) throws SQLException {
		connection.close();
	}

	public static String[] getAllDatabase() throws SQLException {
		ResultSet res = databaseMetaData.getCatalogs();
		List<String> dbNames = new ArrayList<String>();
		while (res.next()) {
			String dbName = res.getString("TABLE_CAT");
			dbNames.add(dbName);
		}

		return StringUtils.convertListToArray(dbNames);
	}

	public static List<String> getAllTableNames(String dbName, boolean init) throws SQLException {

		if (init) {
			init(dbName);
		}

		List<String> dbList = new ArrayList<>();

		String[] types = { TABLE };

		ResultSet rs = getDatabaseMetaData().getTables(null, null, "%", types);

		while (rs.next()) {

			String tableName = rs.getString(TABLE_NAME);

			dbList.add(tableName);
		}

		return dbList;
	}

	public static Map<String, String> getForeignKeysMap(String tableName) throws SQLException {

		Map<String, String> foreignKeysMap = new HashMap<>();

		ResultSet foreignKeys = getDatabaseMetaData().getImportedKeys(getConnection().getCatalog(), null, tableName);

		while (foreignKeys.next()) {

			// String fkTableName =
			// foreignKeys.getString(MySqlUtils.FKTABLE_NAME);
			String fkColumnName = foreignKeys.getString(MySqlUtils.FKCOLUMN_NAME);
			String pkTableName = foreignKeys.getString(MySqlUtils.PKTABLE_NAME);
			// String pkColumnName =
			// foreignKeys.getString(MySqlUtils.PKCOLUMN_NAME);

			foreignKeysMap.put(fkColumnName, pkTableName);

			// System.out.println("K: " + fkColumnName + ", V: " + pkTableName);

			// System.out.println(fkTableName + "." + fkColumnName + " -> " +
			// pkTableName + "." + pkColumnName);
		}

		return foreignKeysMap;
	}

	public static boolean isForeignKey(String tableName, String columnName) throws SQLException {

		List<String> fks = getStaticForeignKeys(tableName);

		for (String fk : fks) {
			if (StringUtils.equalsIgnoreCase(fk, columnName)) {
				return true;
			}
		}

		return false;
	}

	public static String getPrimaryTableName(String tableName, String columnName) throws SQLException {
		ResultSet foreignKeys = getDatabaseMetaData().getImportedKeys(getConnection().getCatalog(), null, tableName);

		while (foreignKeys.next()) {

			String fkColumnName = foreignKeys.getString(MySqlUtils.FKCOLUMN_NAME);

			if (StringUtils.equalsIgnoreCase(fkColumnName, columnName)) {
				return foreignKeys.getString(MySqlUtils.PKTABLE_NAME);
			}
		}

		return "";
	}

	public static int getRowsCount(String dbName, String tableName) throws SQLException {

		DB_NAME = dbName;

		// init(dbName);

		String query = "SELECT count(*) FROM " + tableName;

		ResultSet rs = statement.executeQuery(query);
		rs.next();
		return rs.getInt(1);
	}

	public static int getColumnCount(String dbName, String tableName) throws SQLException {

		StringBuilder builder = new StringBuilder("SELECT count(*) FROM information_schema.columns WHERE");
		builder.append(" table_schema = '");
		builder.append(dbName);
		builder.append("' AND table_name = '");
		builder.append(tableName);
		builder.append("'");

		// System.out.println(builder.toString());

		ResultSet rs = getStatement().executeQuery(builder.toString());
		rs.next();
		return rs.getInt(1);
	}

	public static List<String> getColumns(String dbName, String tableName) throws SQLException {

		List<String> columns = new ArrayList<>();

		DatabaseMetaData meta = getConnection().getMetaData();
		ResultSet resultSet = meta.getColumns(dbName, null, tableName, "%");
		while (resultSet.next()) {
			columns.add(resultSet.getString(4));
		}
		return columns;
	}

	public static List<String> getPrimaryKeys(String dbName, String tableName) throws SQLException {
		List<String> primaryKeys = new ArrayList<>();

		DatabaseMetaData meta = getConnection().getMetaData();
		ResultSet resultSet = meta.getPrimaryKeys(dbName, null, tableName);
		while (resultSet.next()) {
			primaryKeys.add(resultSet.getString("COLUMN_NAME"));
		}
		return primaryKeys;
	}

	public static List<String> getForeignKeys(String dbName, String tableName) throws SQLException {
		List<String> foriegnKeys = new ArrayList<>();

		ResultSet foreignKeys = getDatabaseMetaData().getImportedKeys(getConnection().getCatalog(), null, tableName);

		while (foreignKeys.next()) {

			String fkColumnName = foreignKeys.getString(MySqlUtils.FKCOLUMN_NAME);

			foriegnKeys.add(fkColumnName);
		}

		return foriegnKeys;
	}

	public static List<String> getStaticPrimaryKeys(String tableName) {
		return primaryKeys.get(tableName);
	}

	public static List<String> getStaticForeignKeys(String tableName) {
		return foreignKeys.get(tableName);
	}

	public static Integer getStaticQueryLimitStart(String tableName) {
		return queryLimitStart.get(tableName);
	}

	public static Integer getStaticIteratorLength(String tableName) {
		return iteratorLength.get(tableName);
	}

	private static int getMySqlQueryLimitStart(String dbName, String tableName) throws SQLException {

		long ramSize = RAMUtils.getPhysicalMemorySize();
		int columnSize = getColumnCount(dbName, tableName);

		if (columnSize > ramSize) {
			return 1;
		}

		Double queryLimitDouble = Math.ceil(((double) ramSize / columnSize) / 3);

		int queryLimit = queryLimitDouble.intValue();

		System.out.println("Database: " + dbName + ", Table: " + tableName + ", Column Size: " + columnSize
				+ ", Query Limit: " + queryLimit);

		return queryLimit;
	}

	public static int getMySqlQueryLimitlength(String dbName, String tableName) throws SQLException {

		return 100;
	}

	private static int getIteratorLength(String dbName, String tableName) throws SQLException {

		int totalRows = getRowsCount(dbName, tableName);
		int queryLimit = getStaticQueryLimitStart(tableName);

		Double iteratorLengthDouble = Math.ceil((double) totalRows / queryLimit);

		int iteratorLength = iteratorLengthDouble.intValue();

		return iteratorLength;
	}

	public static int getDBSize() throws SQLException {

		String query = "SELECT sum(round(((data_length + index_length) / 1024 / 1024), 2)) FROM information_schema.TABLES WHERE table_schema = '"
				+ DB_NAME + "'";

		ResultSet rs = getStatement().executeQuery(query);
		rs.next();
		Double val = rs.getDouble(1);
		val = Math.ceil(val);
		return val.intValue();
	}

	public static int getDBSize(String dbName) throws SQLException {

		String query = "SELECT sum(round(((data_length + index_length) / 1024 / 1024), 2)) FROM information_schema.TABLES WHERE table_schema = '"
				+ dbName + "'";

		ResultSet rs = getStatement().executeQuery(query);
		rs.next();
		Double val = rs.getDouble(1);
		val = Math.ceil(val);
		return val.intValue();
	}

	public static int getRelationCount(String tableName, String foreignTableName) throws SQLException {

		return getRelationCount(tableName, foreignTableName, null);
	}

	public static int getRelationCount(String tableName, String foreignTableName, String dbName) throws SQLException {

		if (StringUtils.isEmpty(dbName)) {

			dbName = ConfigUtils.getDbName();
		}

		StringBuffer sb = new StringBuffer("SELECT MAX(counted) FROM (");

		System.out.println(
				tableName + ", " + foreignTableName + ", " + getRelationKey(dbName, tableName, foreignTableName));

		sb.append("SELECT COUNT(");
		sb.append(tableName);
		sb.append(".");
		sb.append(getRelationKey(dbName, tableName, foreignTableName));
		sb.append(") AS counted");
		sb.append(" FROM ");
		sb.append(foreignTableName);
		sb.append(" LEFT JOIN ");
		sb.append(tableName);
		sb.append(" ON ");
		sb.append(foreignTableName);
		sb.append(".");
		sb.append(getPrimaryKey(dbName, foreignTableName));
		sb.append("=");
		sb.append(tableName);
		sb.append(".");
		sb.append(getRelationKey(dbName, tableName, foreignTableName));
		sb.append(" GROUP BY ");
		sb.append(foreignTableName);
		sb.append(".");
		sb.append(getPrimaryKey(dbName, foreignTableName));
		sb.append(")");
		sb.append(" AS counts");

		System.out.println(sb.toString());

		MySqlUtils.init(dbName);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());
		rs.next();

		return rs.getInt(1);

	}

	public static String getRelationKey(String dbName, String tableName, String foreignName) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(dbName, foreignName);
		Map<String, String> foreignKeys = MySqlUtils.getForeignKeysMap(tableName);

		String possibleValue = "";

		for (String primaryKey : primaryKeys) {

			for (Map.Entry<String, String> map : foreignKeys.entrySet()) {

				String combination1 = primaryKey;
				if (StringUtils.equalsIgnoreCase(map.getKey(), combination1)) {
					possibleValue = combination1;
				}

				String combination2 = foreignName + "_" + primaryKey;
				if (StringUtils.equalsIgnoreCase(map.getKey(), combination2)) {
					possibleValue = combination2;
				}

				String combination3 = foreignName + primaryKey;
				if (StringUtils.equalsIgnoreCase(map.getKey(), combination3)) {
					possibleValue = combination3;
				}

				if (map.getKey().contains(combination1) || map.getKey().contains(combination2)
						|| map.getKey().contains(combination3)) {
					possibleValue = map.getKey();
				}

				if (possibleValue.contains(foreignName)) {
					return possibleValue;
				}
			}

		}

		return possibleValue;
	}

	public static String getPrimaryKey(String dbName, String tableName) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(dbName, tableName);

		if (primaryKeys.size() > 0) {
			return primaryKeys.get(0);
		}

		return "";
	}

	public static String getOrderBy(String dbName, String tableName) throws SQLException {
		List<String> columns = new ArrayList<>();

		DatabaseMetaData meta = getConnection().getMetaData();
		ResultSet indexInformation = meta.getIndexInfo(dbName, dbName, tableName, true, true);
		while (indexInformation.next()) {
			String dbColumnName = indexInformation.getString("COLUMN_NAME");

			if (StringUtils.isNotEmpty(dbColumnName)) {
				return dbColumnName;
			}
		}

		ResultSet resultSet = meta.getColumns(dbName, null, tableName, "%");
		while (resultSet.next()) {
			columns.add(resultSet.getString(4));
		}
		return "";
	}

	public static Map<String, List<String>> getAllPrimaryKeys(String dbName) throws SQLException {

		Map<String, List<String>> map = new HashMap<>();

		List<String> tables = getAllTableNames(dbName, false);

		for (String table : tables) {
			List<String> pks = getPrimaryKeys(dbName, table);
			map.put(table, pks);
		}

		return map;
	}

	private static Map<String, List<String>> getAllForeignKeys(String dbName) throws SQLException {

		Map<String, List<String>> map = new HashMap<>();

		List<String> tables = getAllTableNames(dbName, false);

		for (String table : tables) {
			List<String> fks = getForeignKeys(dbName, table);
			map.put(table, fks);
		}

		return map;
	}

	private static Map<String, Integer> getAllQueryLimitStart(String dbName) throws SQLException {

		System.out.println("MySqlUtils.getAllQueryLimitStart()");

		Map<String, Integer> map = new HashMap<>();

		List<String> tables = getAllTableNames(dbName, false);

		for (String table : tables) {
			Integer limit = getMySqlQueryLimitStart(dbName, table);
			map.put(table, limit);
		}

		return map;
	}

	private static Map<String, Integer> getAllItertorLength(String dbName) throws SQLException {

		Map<String, Integer> map = new HashMap<>();

		List<String> tables = getAllTableNames(dbName, false);

		for (String table : tables) {
			Integer limit = getIteratorLength(dbName, table);
			map.put(table, limit);
		}

		return map;
	}

	public static void testIndex2(String dbName, String tableName) throws SQLException {
		List<String> columns = new ArrayList<>();

		DatabaseMetaData meta = getConnection().getMetaData();
		ResultSet indexInformation = meta.getIndexInfo(dbName, dbName, tableName, true, true);
		while (indexInformation.next()) {
			String dbCatalog = indexInformation.getString("TABLE_CATALOG");
			String dbSchema = indexInformation.getString("TABLE_SCHEMA");
			String dbTableName = indexInformation.getString("TABLE_NAME");
			boolean dbNoneUnique = indexInformation.getBoolean("NON_UNIQUE");
			String dbIndexQualifier = indexInformation.getString("INDEX_QUALIFIER");
			String dbIndexName = indexInformation.getString("INDEX_NAME");
			short dbType = indexInformation.getShort("TYPE");
			short dbOrdinalPosition = indexInformation.getShort("ORDINAL_POSITION");
			String dbColumnName = indexInformation.getString("COLUMN_NAME");
			String dbAscOrDesc = indexInformation.getString("ASC_OR_DESC");
			int dbCardinality = indexInformation.getInt("CARDINALITY");
			int dbPages = indexInformation.getInt("PAGES");
			String dbFilterCondition = indexInformation.getString("FILTER_CONDITION");

			System.out.println("index name=" + dbIndexName);
			System.out.println("table=" + dbTableName);
			System.out.println("column=" + dbColumnName);
			System.out.println("catalog=" + dbCatalog);
			System.out.println("schema=" + dbSchema);
			System.out.println("nonUnique=" + dbNoneUnique);
			System.out.println("indexQualifier=" + dbIndexQualifier);
			System.out.println("type=" + dbType);
			System.out.println("ordinalPosition=" + dbOrdinalPosition);
			System.out.println("ascendingOrDescending=" + dbAscOrDesc);
			System.out.println("cardinality=" + dbCardinality);
			System.out.println("pages=" + dbPages);
			System.out.println("filterCondition=" + dbFilterCondition);
		}

		ResultSet resultSet = meta.getColumns(dbName, null, tableName, "%");
		while (resultSet.next()) {
			columns.add(resultSet.getString(4));
		}
		// return columns;
	}

	public static int getMinId(String dbName, String tableName) throws SQLException {

		DB_NAME = dbName;

		init(dbName);

		String query = "SELECT MIN(id) FROM " + tableName;

		ResultSet rs = statement.executeQuery(query);
		rs.next();
		return rs.getInt(1);
	}

	public static int getMaxId(String dbName, String tableName) throws SQLException {

		DB_NAME = dbName;

		init(dbName);

		String query = "SELECT MAX(id) FROM " + tableName;

		ResultSet rs = statement.executeQuery(query);
		rs.next();
		return rs.getInt(1);
	}

	public static void createTable() throws SQLException {

		System.out.println("\nCreating table...");
		String sql = "alter TABLE zuction2 add constraint FOREIGN KEY (companies_id) REFERENCES companies(id)";

		statement.executeUpdate(sql);
		System.out.println("Table successfully created");
	}

	public static int getTotalThreads(String dbName) throws SQLException {

		List<Document> tables = ConfigUtils.getTables();

		int totalThreads = 0;

		for (Document tableDocument : tables) {

			if (tableDocument == null) {
				continue;
			}

			String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

			totalThreads += MySqlUtils.getIteratorLength(dbName, tableName);
		}

		return totalThreads;
	}

	public static void main(String[] args) throws SQLException {
		// System.out.println("tes");
		// testIndex("employees", "salaries");
		init("college");
		System.out.println(getRelationCount("course_student", "student","college"));
		// createTable();
		// System.out.println(getRelationCount("company", "business_category"));
	}

}
