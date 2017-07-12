package com.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.file.handling.ConfigWriter;
import com.utils.ConfigUtils;
import com.utils.MigratorUtils;
import com.utils.StringUtils;

@SuppressWarnings("unchecked")
public class RefactorConfigFile {

	public static void refactorConfig() throws IOException {

		Document document = ConfigUtils.getDocument();

		System.err.println(MigratorUtils.getPrettyJson(document.toJson()));

		try {
			boolean isRefactored = document.getBoolean(ConfigUtils.IS_REFACTORED);

			if (isRefactored) {
				return;
			}
		} catch (Exception ex) {

		}

		System.out.println(MigratorUtils.getPrettyJson(document.toJson()));

		List<Document> tables = (List<Document>) document.get(ConfigUtils.TABLES);

		Map<String, Document> rm = new HashMap<>();

		for (Document table : tables) {
			// System.out.println(table.toJson());

			Document tableNew = new Document();

			String tableName = table.getString(ConfigUtils.TABLE_NAME);
			// boolean shouldMigrate = table.getBoolean(ConfigUtils.m)

			tableNew.append(ConfigUtils.TABLE_NAME, tableName);

			List<Document> relations = (List<Document>) table.get(ConfigUtils.RELATIONS);

			if (relations == null) {
				continue;
			}

			for (Document relation : relations) {
				if (relation == null) {
					continue;
				}

				String fkTableName = relation.getString(ConfigUtils.FK_TABLE_NAME);

				int relationCode = 0;
				try {
					relationCode = relation.getInteger(ConfigUtils.FK_RELATION_CODE);
				} catch (Exception ex) {
					continue;
				}

				System.out.println("RC: " + relationCode);

				if (relationCode == ConfigUtils.ONE_to_FEW || relationCode == ConfigUtils.ONE_to_MANY) {

					rm.put(tableName + "-" + fkTableName, relation);
				}
			}

		}

		for (Map.Entry<String, Document> m : rm.entrySet()) {
			System.out.println("\t\tMAP: " + m.getKey() + ", " + m.getValue().toJson());
		}

		for (Document table : tables) {

			String tableName = table.getString(ConfigUtils.TABLE_NAME);

			System.out.println(tableName.toUpperCase());

			for (Map.Entry<String, Document> map : rm.entrySet()) {

				String tableName1 = getTableName(map.getKey());
				String fkTableName1 = getFkTableName(map.getKey());

				System.out.println("\t" + map.getKey());

				Document documentToBeAdded = copyDocument(map.getValue());

				System.out.println("\t" + documentToBeAdded.toJson());

				if (StringUtils.equalsIgnoreCase(tableName, tableName1)) {

					List<Document> relations = (List<Document>) table.get(ConfigUtils.RELATIONS);

					relations.remove(documentToBeAdded);

					if (shouldAddDocument(documentToBeAdded)) {
						documentToBeAdded.append(ConfigUtils.FK_THUMB_RULE_CODE,
								ConfigUtils.RELATION_CODE_One_to_Many_5);
						relations.add(documentToBeAdded);
					}

				} else if (StringUtils.equalsIgnoreCase(tableName, fkTableName1)) {

					Object relation = table.get(ConfigUtils.RELATIONS);

					List<Document> relations = null;

					if (relation == null) {
						relations = new ArrayList<>();
					} else {
						relations = (List<Document>) relation;
						table.remove(relation);
					}

					if (shouldAddDocument(documentToBeAdded)) {

						Object denormalization = documentToBeAdded.get(ConfigUtils.DENORMALIZATION_COLUMNS);

						if (denormalization != null) {

							documentToBeAdded.append(ConfigUtils.DENORMALIZATION_COLUMNS, null);
						}
					}

					documentToBeAdded.append(ConfigUtils.FK_TABLE_NAME, tableName1);

					System.out.println("\t>>" + documentToBeAdded.toJson());

					relations.add(documentToBeAdded);

					table.append(ConfigUtils.RELATIONS, relations);
				}

			}

		}

		Object table = document.get(ConfigUtils.TABLES);

		document.remove(table);

		document.append(ConfigUtils.IS_REFACTORED, true);

		document.append(ConfigUtils.TABLES, tables);

		System.out.println("\t\t" + MigratorUtils.getPrettyJson(document.toJson()));

		ConfigWriter cr = new ConfigWriter();
		cr.clearFileContent();
		cr.write(document.toJson());
		cr.close();
	}

	public static Document copyDocument(Document document) {

		Document doc = new Document();

		for (Map.Entry<String, Object> value : document.entrySet()) {
			doc.append(value.getKey(), value.getValue());
		}

		return doc;
	}

	public static boolean shouldAddDocument(Document documentToBeAdded) {
		Object rc = documentToBeAdded.get(ConfigUtils.FK_RELATION_CODE);

		if (rc != null) {

			int relationCode = (int) rc;

			if (relationCode == ConfigUtils.ONE_to_MANY) {

				Object thumbCode = documentToBeAdded.get(ConfigUtils.FK_THUMB_RULE_CODE);

				if (thumbCode != null) {

					if (StringUtils.equalsIgnoreCase(thumbCode.toString(), ConfigUtils.RELATION_CODE_One_to_Many_4)) {
						return true;
					}
				}

			}

		}

		return false;
	}

	public static String getTableName(String key) {

		String[] keys = key.split("-");
		return keys[0];

	}

	public static String getFkTableName(String key) {
		String[] keys = key.split("-");
		return keys[1];
	}

	public static void print(String msg, int noOfTabs) {
		for (int i = 0; i < noOfTabs; i++) {
			System.out.print("\t");
		}

		System.out.println(msg);
	}

	public static void main(String[] args) throws Exception {

		refactorConfig();

	}

}
