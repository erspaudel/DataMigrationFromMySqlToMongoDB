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

import com.utils.SystemUtils.RAMUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class MySqlUtils2 {

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

	static {
		init(null);
	}

	public static void init(String dbName) {

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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		return connection;
	}

	public static Statement getStatement() throws SQLException {

		statement = connection.createStatement();
		return statement;
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

	public static List<String> getAllTableNames(String dbName) throws SQLException {

		init(dbName);

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
			String fkColumnName = foreignKeys.getString(MySqlUtils2.FKCOLUMN_NAME);
			String pkTableName = foreignKeys.getString(MySqlUtils2.PKTABLE_NAME);
			// String pkColumnName =
			// foreignKeys.getString(MySqlUtils.PKCOLUMN_NAME);

			foreignKeysMap.put(fkColumnName, pkTableName);

			// System.out.println("K: "+fkColumnName+", V: "+pkTableName);

			// System.out.println(fkTableName + "." + fkColumnName + " -> " +
			// pkTableName + "." + pkColumnName);
		}

		return foreignKeysMap;
	}

	public static int getRowsCount(String dbName, String tableName) throws SQLException {

		DB_NAME = dbName;

		init(dbName);

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

	public static int getMySqlQueryLimit(String dbName, String tableName) throws SQLException {

		long ramSize = RAMUtils.getPhysicalMemorySize();
		int columnSize = getColumnCount(dbName, tableName);

		if (columnSize > ramSize) {
			return 1;
		}

		// Double queryLimitDouble = Math.ceil((double) ramSize / columnSize);

		// int queryLimit = queryLimitDouble.intValue();

		// System.out.println("Database: " + dbName + ", Table: " + tableName +
		// ", Column Size: " + columnSize
		// + ", Query Limit: " + queryLimit);

		return 100;
	}

	public static int getIteratorLength(String dbName, String tableName) throws SQLException {

		int totalRows = getRowsCount(dbName, tableName);
		int queryLimit = getMySqlQueryLimit(dbName, tableName);

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

		String dbName = ConfigUtils.getDbName();

		StringBuffer sb = new StringBuffer("SELECT MAX(counted) FROM (");

		sb.append("SELECT COUNT(");
		sb.append(tableName);
		sb.append(".");
		sb.append(foreignTableName);
		sb.append("_id) AS counted");
		sb.append(" FROM ");
		sb.append(foreignTableName);
		sb.append(" LEFT JOIN ");
		sb.append(tableName);
		sb.append(" ON ");
		sb.append(foreignTableName);
		sb.append(".id=");
		sb.append(tableName);
		sb.append(".");
		sb.append(foreignTableName);
		sb.append("_id GROUP BY ");
		sb.append(foreignTableName);
		sb.append(".id)");
		sb.append(" AS counts");

		System.out.println(sb.toString());

		MySqlUtils2.init(dbName);

		ResultSet rs = MySqlUtils2.getStatement().executeQuery(sb.toString());
		rs.next();

		return rs.getInt(1);

	}

	public static String getRelationKey(String dbName, String tableName, String foreignName) throws SQLException {

		List<String> primaryKeys = MySqlUtils2.getPrimaryKeys(dbName, foreignName);
		Map<String, String> foreignKeys = MySqlUtils2.getForeignKeysMap(tableName);

		String primaryKey = primaryKeys.get(0);

		for (Map.Entry<String, String> map : foreignKeys.entrySet()) {
			
			System.out.println(map.getKey()+": "+map.getValue());

			String combination = primaryKey;
			if (StringUtils.equalsIgnoreCase(map.getKey(), combination)) {
				return combination;
			}

			combination = foreignName + "_" + primaryKey;
			if (StringUtils.equalsIgnoreCase(map.getKey(), combination)) {
				return combination;
			}

			combination = foreignName + primaryKey;
			if (StringUtils.equalsIgnoreCase(map.getKey(), combination)) {
				return combination;
			}
		}

		return "";
	}

	public static String getPrimaryKey(String dbName, String tableName) throws SQLException {

		List<String> primaryKeys = MySqlUtils2.getPrimaryKeys(dbName, tableName);

		if (primaryKeys.size() > 0) {
			return primaryKeys.get(0);
		}

		return "";
	}

	public static void testIndex(String dbName, String tableName) throws SQLException {
		List<String> columns = new ArrayList<>();

		DatabaseMetaData meta = getConnection().getMetaData();
		ResultSet indexInformation = meta.getIndexInfo(null, dbName, tableName, true, true);
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

	public static void main(String[] args) throws SQLException {
		System.out.println("tes");
		// testIndex("db_hot3l", "companies");'
		init("db_hot3l");
		// createTable();
		System.out.println(getRelationCount("company", "business_category"));
	}

}
