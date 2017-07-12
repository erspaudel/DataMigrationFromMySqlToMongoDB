package com.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.file.handling.FileExporter;
import com.utils.ConfigUtils;
import com.utils.MySqlUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */
@SuppressWarnings("unchecked")
public class MySQLToFileErrorHandling extends MySQLToFile {

	public void processTable(Document tableDocument, String ids) throws Exception {

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		List<Document> documents = getDocuments(tableDocument, ids);

		FileExporter exporter = new FileExporter(tableName, -999);
		exporter.write(tableName, documents);
		exporter.close();
	}

	public List<Document> getDocuments(Document tableDocument, String ids) throws Exception {

		List<Document> documents = new ArrayList<>();
		try {

			List<Document> relations = (List<Document>) tableDocument.get(ConfigUtils.RELATIONS);

			String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

			String[] idsSplitted = ids.split(",");

			List<String> primaryKeys = MySqlUtils.getPrimaryKeys(ConfigUtils.getDbName(), tableName);

			for (int i = 0; i < idsSplitted.length; i++) {

				Document document = getDocumentFromMySql(tableDocument, idsSplitted[i], primaryKeys);

				if (relations == null) {
					continue;
				}

				for (Document relationDocument : relations) {

					if (relationDocument == null) {
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

						processOneToOne(tableName, document, relationDocument);
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

	public Document getDocumentFromMySql(Document tableDocument, String id, List<String> primaryKeys)
			throws SQLException {

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" WHERE id =");
		sb.append(id);

		return getDocumentByQuery(tableName, sb.toString(), true, primaryKeys);

	}

}
