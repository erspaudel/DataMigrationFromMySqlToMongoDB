package com.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.bson.Document;

public class ConfigUtils {

	public static final String DB_NAME = "db_name";
	public static final String DB_MIGRATE_ALL_TABLES = "migrate_all_tables";
	public static final String DB_DEFAULT_MIGRATION = "default_migration";
	public static final String DB_BACKUP_FILES = "backup_files";
	public static final String MIGRATION_DIRECTORY = "migration_directory";
	public static final String IS_REFACTORED = "is_refactored";
	public static final String UNIX_TIME = "unix_time";
	public static final String EMBED_DOCUMENT_LIMIT = "embed_document_limit";

	public static final String TABLES = "tables";
	public static final String TABLE_NAME = "table_name";
	public static final String RELATIONS = "table_relations";
	public static final String FK_TABLE_NAME = "fk_table_name";
	public static final String FK_IS_AUTOMATIC = "fk_is_automatic";
	public static final String FK_RELATION_CODE = "fk_relation_code";
	public static final String FK_THUMB_RULE_CODE = "fk_thumb_rule_code";
	public static final String DENORMALIZATION_COLUMNS = "denormalization_columns";

	public static final int ONE_to_ONE = 1;
	public static final int ONE_to_FEW = 2;
	public static final int ONE_to_MANY = 3;
	public static final int ONE_to_SQUILLION = 4;

	public static final String RELATION_CODE_One_to_One_1 = "1";
	public static final String RELATION_CODE_One_to_One_2 = "2";

	public static final String RELATION_CODE_One_to_Few_1 = "3";
	public static final String RELATION_CODE_One_to_Few_2 = "4";

	public static final String RELATION_CODE_One_to_Many_1 = "5";
	public static final String RELATION_CODE_One_to_Many_2 = "6";
	public static final String RELATION_CODE_One_to_Many_3 = "7";
	public static final String RELATION_CODE_One_to_Many_4 = "8";
	public static final String RELATION_CODE_One_to_Many_5 = "9";

	public static final String RELATION_CODE_One_to_Squillion_1 = "10";
	public static final String RELATION_CODE_One_to_Squillion_2 = "11";

	public static final int LIMIT_ONE_to_ONE = 1;
	public static final int LIMIT_ONE_to_FEW = 10;
	public static final int LIMIT_ONE_to_MANY = 50;

	public static final int FIXED_THREAD_POOL = 4;

	public static final boolean APPEND_FOREIGN_KEY_ID = false;

	public static final boolean ONLY_INCLUDE_MONGODB_OBJECTID = true;

	private static Document document;

	// static {
	//
	// init();
	//
	// printAll();
	// }

	public static void init() {
		document = getDocument();
	}

	public static void printAll() {
		System.err.println("DB Name: " + getDbName());
		System.err.println("MIGRATE ALL TABLES: " + shouldMigrateAllTables());
		System.err.println("BACK UP FILES: " + shouldBackUpFiles());
		System.err.println("DEFAULT MIGRATION: " + shouldUseDefaultMigrationConfig());
		System.err.println("ROOT PATH: " + getMigrationDirectory());
	}

	public static String getDbName() {
		return document.getString(DB_NAME);
	}

	public static String getMigrationDirectory() {
		return document.getString(MIGRATION_DIRECTORY);
	}

	public static int getUnixTime() {
		return document.getInteger(UNIX_TIME);
	}

	public static boolean shouldBackUpFiles() {
		return document.getBoolean(DB_BACKUP_FILES, false);
	}

	public static boolean shouldMigrateAllTables() {
		return document.getBoolean(DB_MIGRATE_ALL_TABLES, false);
	}

	public static boolean shouldUseDefaultMigrationConfig() {
		return document.getBoolean(DB_DEFAULT_MIGRATION, false);
	}

	public static int getEmbedDocumentLimit() {
		return Integer.parseInt(document.getString(EMBED_DOCUMENT_LIMIT));
	}

	public static List<Document> getTables() {

		refreshDocument();

		return (List<Document>) document.get(TABLES);
	}

	public static Document getDocument(String tableName) {

		List<Document> tables = getTables();

		for (Document document : tables) {
			if (StringUtils.equalsIgnoreCase(document.getString("table_name"), tableName)) {
				return document;
			}
		}

		return null;
	}

	public static Document getTablesObject() {

		refreshDocument();

		// JSONObject obj = new JSONObject(document.toJson());
		//
		// JSONArray a = obj.getJSONArray(TABLES);
		//
		//
		// JsonObject o = new JsonObject();
		// o.add
		// JsonArray ar = obj.getJSONArray(TABLES);

		// System.out.println(obj.get("table_name"));
		// System.out.println(a.toString());

		return (Document) document.get(TABLES);
	}

	public static List<Document> getRelations(String tableName) {

		Document tables = (Document) getTablesObject().get(tableName);
		return (List<Document>) tables.get(RELATIONS);
	}

	public static void refreshDocument() {

		document = getDocument();
	}

	public static Document getDocument() {

		Document document = null;

		try {

			FileReader fileReader = new FileReader(FileUtils.FILE_PATH_CONFIG);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String val = null;

			while ((val = bufferedReader.readLine()) != null) {
				document = Document.parse(val);
			}

			bufferedReader.close();
		} catch (Exception ex) {

		}

		return document;
	}

	public static void main(String[] args) {
		System.out.println(getDocument().toJson());
	}

}
