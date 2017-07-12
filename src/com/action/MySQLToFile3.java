package com.action;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.file.handling.FileExporter;
import com.utils.ConfigUtils;
import com.utils.MongoUtils;
import com.utils.MySqlUtils;
import com.utils.StringUtils;
import com.utils.TestUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */
@SuppressWarnings("unchecked")
public class MySQLToFile3 {

	public void processTable(Document tableDocument) throws Exception {

		Document mainDocument = ConfigUtils.getDocument();

		String dbName = mainDocument.getString(ConfigUtils.DB_NAME);
		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		int startLimit = 0;
		int limit = MySqlUtils.getStaticQueryLimitStart(tableName);
		int endLimit = limit;
		int iteratorLength = MySqlUtils.getStaticIteratorLength(tableName);

		for (int i = 1; i <= iteratorLength; i++) {

			try {

				List<Document> documents = getDocuments(tableDocument, startLimit, endLimit);

				FileExporter exporter = new FileExporter(tableName, i);
				exporter.write(tableName, documents);
				exporter.close();
			} catch (Exception e) {
				e.printStackTrace();
				// continue;
			}

			startLimit = endLimit;
			endLimit += limit;
		}
	}

	public List<Document> getDocuments(Document tableDocument, int startLimit, int endLimit) throws Exception {

		List<Document> documents = new ArrayList<>();
		try {

			List<Document> relations = (List<Document>) tableDocument.get(ConfigUtils.RELATIONS);

			String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

			String orderBy = MySqlUtils.getOrderBy(ConfigUtils.getDbName(), tableName);

			for (int i = startLimit; i < endLimit; i += MySqlUtils.getMySqlQueryLimitlength(ConfigUtils.getDbName(),
					tableName)) {

				Document document = getDocumentFromMySql(tableDocument, i, orderBy);

				if (document.isEmpty()) {
					continue;
				}

				if (relations == null) {
					documents.add(document);
					continue;
				}

				for (Document relationDocument : relations) {

					if (relationDocument == null) {
						continue;
					}

					List<Document> tables = ConfigUtils.getTables();

					boolean shouldMigrate = false;

					for (Document table : tables) {
						if (StringUtils.equalsIgnoreCase(table.getString(ConfigUtils.TABLE_NAME),
								relationDocument.getString(ConfigUtils.FK_TABLE_NAME))) {
							shouldMigrate = true;
						}
					}

					if (!shouldMigrate) {
						continue;
					}

					int relationCode = 0;

					try {

						Object rc = relationDocument.get(ConfigUtils.FK_RELATION_CODE);

						if (rc != null) {
							relationCode = (int) rc;
						}

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

					switch (relationCode) {
					case ConfigUtils.ONE_to_ONE:

						processOneToOne(document, relationDocument);
						break;
					case ConfigUtils.ONE_to_FEW:

						processOneToFew(tableName, document, relationDocument);
						break;
					case ConfigUtils.ONE_to_MANY:

						processOneToMany(tableName, document, relationDocument);
						break;
					case ConfigUtils.ONE_to_SQUILLION:

						processOneToSquillion(tableName, document, relationDocument);
						break;

					}

				}

				documents.add(document);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return documents;
	}

	public void processOneToOne(Document document, Document relationDocument) throws SQLException {

		String thumbRuleCode = "";

		// thumbRuleCode =
		// relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);

		String fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);

		Object fkId = document.get(fkTableName + "_id");

		if (fkId == null) {
			return;
		}

		boolean embed = true;

		switch (thumbRuleCode) {
		case ConfigUtils.RELATION_CODE_One_to_One_1:

			embed = true;

			break;

		case ConfigUtils.RELATION_CODE_One_to_One_2:

			embed = false;

			break;
		}

		StringBuilder sb = new StringBuilder();

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), fkTableName);

		String pKeys = "";

		for (String string : primaryKeys) {
			pKeys += string + ",";
		}

		if (pKeys.endsWith(",")) {
			pKeys = pKeys.substring(0, pKeys.length() - 1);
		}

		sb.append("SELECT * FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE ");
		sb.append(getOneToOneWhereQuery(document, fkTableName));

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (embed) {
					if (StringUtils.isNotEmpty(value)) {
						document.append(key, value);
					}
				} else {
					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						document.append(fkTableName + "_id", MongoUtils.getObjectId(mySqlId).toString());
					}
				}
			}
		}
	}

	public void processOneToFew(String tableName, Document document, Document relationDocument) throws SQLException {

		String thumbRuleCode = "";
		String fkTableName = "";
		try {

			Object fkTableNameObject = relationDocument.get(ConfigUtils.FK_TABLE_NAME);
			if (fkTableNameObject != null) {

				fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean embedAll = true;

		switch (thumbRuleCode) {
		case ConfigUtils.RELATION_CODE_One_to_Few_1:

			embedAll = true;
			break;
		case ConfigUtils.RELATION_CODE_One_to_Few_2:

			embedAll = false;
			break;
		}

		String whereClause = getOneToFewWhereQuery(document, tableName, fkTableName);

		if (StringUtils.isEmpty(whereClause)) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE ");
		sb.append(whereClause);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		List<Document> embedDocuments = new ArrayList<>();
		List<String> referenceDocuments = new ArrayList<>();

		while (rs.next()) {

			Document doc = new Document();

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (embedAll) {

					if (StringUtils.equalsIgnoreCase(tableName + "_id", key)) {
						continue;
					}

					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					}

					if (StringUtils.isNotEmpty(value)) {
						doc.append(key, value);
					}

				} else {
					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						referenceDocuments.add(MongoUtils.getObjectId(mySqlId).toString());
					}

				}

			}

			if (embedAll) {
				embedDocuments.add(doc);
				document.append(fkTableName + "s", embedDocuments);
			} else {
				document.append(fkTableName + "s", referenceDocuments);
			}
		}

	}

	public void processOneToMany(String tableName, Document document, Document relationDocument) throws SQLException {

		String thumbRuleCode = "";
		String fkTableName = "";
		int fkId = 0;
		int id = 0;
		boolean isAutomatedProcess = false;

		try {
			Object thumbRuleCodeObject = relationDocument.get(ConfigUtils.FK_THUMB_RULE_CODE);

			if (thumbRuleCodeObject != null) {
				thumbRuleCode = relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);
			} else {
				isAutomatedProcess = true;
			}

			Object fkTableNameObject = relationDocument.get(ConfigUtils.FK_TABLE_NAME);
			if (fkTableNameObject != null) {

				fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);
			}

			Object idStr = document.get("id");
			if (idStr != null) {
				id = Integer.parseInt(idStr.toString());
			}

			Object fkIdStr = document.get(fkTableName + "_id");
			if (fkIdStr != null) {
				fkId = Integer.parseInt(fkIdStr.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String whereClause = getOneToManyWhereQuery(document, tableName, fkTableName, thumbRuleCode, id, fkId);

		if (TestUtils.ENABLE_THREAD_SLEEP) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (StringUtils.isEmpty(whereClause)) {
			return;
		}

		Object denormalization = relationDocument.get(ConfigUtils.DENORMALIZATION_COLUMNS);

		String selectColumns = "";

		if (denormalization != null) {

			List<String> denormalizationColumns = (List<String>) denormalization;

			for (String string : denormalizationColumns) {
				selectColumns += string + ",";
			}
		}

		if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
			selectColumns = "id," + selectColumns;
		} else if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_4)) {
			selectColumns = "id," + selectColumns;
		} else if (isAutomatedProcess) {
			selectColumns = "id," + selectColumns;
		}

		if (selectColumns.endsWith(",")) {
			selectColumns = selectColumns.substring(0, selectColumns.length() - 1);
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		if (StringUtils.isNotEmpty(selectColumns)) {
			sb.append(selectColumns);
		} else {
			sb.append("*");
		}
		sb.append(" FROM ");
		if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_4)) {
			sb.append(fkTableName);
		} else if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
			sb.append(fkTableName);
		} else if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_5)) {
			sb.append(fkTableName);
		} else if (isAutomatedProcess) {
			sb.append(fkTableName);
		}
		sb.append(" WHERE ");

		sb.append(whereClause);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		List<Document> documents = new ArrayList<>();

		while (rs.next()) {

			Document doc = new Document();

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (StringUtils.equals("id", key)) {
					int mySqlId = Integer.parseInt(value);
					if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					} else if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_4)) {
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					} else if (isAutomatedProcess) {
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					}
				}

				if (StringUtils.isNotEmpty(value)) {

					if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
						doc.append(key, value);
					} else if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_5)) {
						document.append(key, value);
					}
				}

			}

			documents.add(doc);

		}

		if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
			document.append(fkTableName + "s", documents);
		}
		try {
			List<String> ids = new ArrayList<>();

			for (Document d : documents) {
				ids.add(d.getString("_id"));
			}

			if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_4)
					|| isAutomatedProcess) {
				if (ids.size() > 0) {
					document.append(fkTableName + "s", ids);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processOneToSquillion(String tableName, Document document, Document relationDocument)
			throws SQLException {

		String thumbRuleCode = "";
		String fkTableName = "";
		try {
			Object thumbRuleCodeObject = relationDocument.get(ConfigUtils.FK_THUMB_RULE_CODE);
			if (thumbRuleCodeObject != null) {
				thumbRuleCode = relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);
			}

			Object fkTableNameObject = relationDocument.get(ConfigUtils.FK_TABLE_NAME);
			if (fkTableNameObject != null) {

				fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (StringUtils.equalsIgnoreCase(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Squillion_1)) {
			return;
		}

		Object denormalization = relationDocument.get(ConfigUtils.DENORMALIZATION_COLUMNS);

		String selectColumns = "id,";

		if (denormalization != null) {

			List<String> denormalizationColumns = (List<String>) denormalization;

			for (String string : denormalizationColumns) {
				selectColumns += string + ",";
			}
		}

		if (selectColumns.endsWith(",")) {
			selectColumns = selectColumns.substring(0, selectColumns.length() - 1);
		}

		String whereClause = getOneToOneSquillionQuery(document, fkTableName);

		if (StringUtils.isEmpty(whereClause)) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		sb.append(selectColumns);
		sb.append(" FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE ");
		sb.append(whereClause);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (StringUtils.equals("id", key)) {
					int mySqlId = Integer.parseInt(value);
					document.append(fkTableName + "_id", MongoUtils.getObjectId(mySqlId).toString());
				}

				if (StringUtils.isNotEmpty(value)) {
					document.append(key, value);
				}

			}
		}

	}

	public Document getDocumentFromMySql(Document tableDocument, int startLimit, String orderBy) throws SQLException {

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" ORDER BY ");

		sb.append(orderBy);

		sb.append(" ASC limit ");
		sb.append(startLimit);
		sb.append(",");
		sb.append(MySqlUtils.getMySqlQueryLimitlength(ConfigUtils.getDbName(), tableName));

		System.out.println(tableName.toUpperCase() + ": " + sb.toString());

		return getDocumentByQuery(sb.toString(), true);

	}

	public String getOrderByQuery(String tableName) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), tableName);

		StringBuilder builder = new StringBuilder();

		for (String string : primaryKeys) {

			if (StringUtils.isNotEmpty(builder.toString())) {
				builder.append(",");
			}

			builder.append(tableName);
			builder.append(".");
			builder.append(string);
		}

		return builder.toString();
	}

	public Document getDocumentByQuery(String query, boolean includeMongoDBId) throws SQLException {

		Document document = new Document();

		ResultSet rs = MySqlUtils.getStatement().executeQuery(query.toString());

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {

			/*
			 * Get values by column names
			 */
			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (includeMongoDBId) {
					if (StringUtils.equals("id", key)) {
						int id = Integer.parseInt(value);
						document.append("_id", MongoUtils.getObjectId(id).toString());
					}
				}

				if (StringUtils.isNotEmpty(value)) {

					document.append(key, value);
				}

			}

		}

		return document;
	}

	public String getOneToOneWhereQuery(Document document, String foriegnTable) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), foriegnTable);
		List<String> keys = new ArrayList<>();

		for (String string : primaryKeys) {
			keys.add(foriegnTable + "_" + string);
		}

		String where = "";

		for (Map.Entry<String, Object> value : document.entrySet()) {

			if (keys.contains(value.getKey())) {
				where += getPrimaryKeyFromId(value.getKey()) + "=" + value.getValue().toString();
			}
		}

		return where;
	}

	public String getOneToFewWhereQuery(Document document, String tableName, String foriegnTable) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), tableName);
		List<String> keys = new ArrayList<>();

		String where = "";

		for (String string : primaryKeys) {
			keys.add(tableName + "_" + string);
		}

		for (Map.Entry<String, Object> value : document.entrySet()) {

			for (String key : keys) {

				String pkId = getPrimaryKeyFromId(key);

				if (StringUtils.equalsIgnoreCase(pkId, value.getKey())) {
					String and = "";
					if (StringUtils.isNotEmpty(where)) {
						and = " AND ";
					}
					where += and + key + "=" + value.getValue().toString();
				}
			}

		}

		return where;
	}

	public String getOneToManyWhereQuery(Document document, String tableName, String foriegnTable, String thumbRuleCode,
			int id, int fkId) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), tableName);
		List<String> keys = new ArrayList<>();

		String where = "";

		for (String string : primaryKeys) {
			keys.add(tableName + "_" + string);
		}

		for (Map.Entry<String, Object> value : document.entrySet()) {

			for (String key : keys) {

				String pkId = getPrimaryKeyFromId(key);

				if (StringUtils.equalsIgnoreCase(pkId, value.getKey())) {

					if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_3)) {
						if (StringUtils.equalsIgnoreCase(pkId, value.getKey())) {
							String and = "";
							if (StringUtils.isNotEmpty(where)) {
								and = " AND ";
							}
							where += and + " " + key + "=" + id;
						}
					} else if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_4)) {

						String and = "";

						if (StringUtils.isNotEmpty(where)) {
							and = " AND ";
						}
						where += and + " " + tableName + "_id=" + id;

					} else if (StringUtils.equals(thumbRuleCode, ConfigUtils.RELATION_CODE_One_to_Many_5)) {

						String and = "";

						if (StringUtils.isNotEmpty(where)) {
							and = " AND ";
						}
						where += and + " " + "id=" + fkId;
					} else {

						String and = "";

						if (StringUtils.isNotEmpty(where)) {
							and = " AND ";
						}
						where += and + " " + tableName + "_id=" + id;

					}
				}
			}

		}

		return where;
	}

	public String getOneToOneSquillionQuery(Document document, String foriegnTable) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), foriegnTable);
		List<String> keys = new ArrayList<>();

		for (String string : primaryKeys) {
			keys.add(foriegnTable + "_" + string);
		}

		// StringBuilder sb = new StringBuilder();
		//
		// sb.append("SELECT ");
		// sb.append(selectColumns);
		// sb.append(" FROM ");
		// sb.append(fkTableName);
		// sb.append(" WHERE ");
		// sb.append("id=");
		// sb.append(id);

		String where = "";

		for (Map.Entry<String, Object> value : document.entrySet()) {

			if (keys.contains(value.getKey())) {
				// where += getPrimaryKeyFromId(value.getKey()) + "=" +
				// value.getValue().toString();
				where += "id=" + value.getValue().toString();
			}
		}

		return where;
	}

	public String removedAND(String value) {
		if (value.endsWith("AND")) {
			value = value.substring(0, value.length() - 3);
		}
		return value;
	}

	public String getPrimaryKeyFromId(String id) {
		String[] ids = id.split("_");
		return ids[1];
	}
}
