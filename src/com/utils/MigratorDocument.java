package com.utils;

import org.bson.Document;

public class MigratorDocument extends Document {

	private Document document;

	private static final long serialVersionUID = 1L;

	public MigratorDocument(Document document) {

		this.document = document;
	}

	public String getDbName() {
		if (this.document != null) {
			return document.getString(ConfigUtils.DB_NAME).toString();
		}
		return "";
	}

	public boolean shouldMigrateAllTables() {

		if (this.document != null) {
			return document.getBoolean(ConfigUtils.DB_MIGRATE_ALL_TABLES);
		}
		return false;
	}

	public boolean isDefaultMigration() {
		return document.getBoolean(ConfigUtils.DB_DEFAULT_MIGRATION);
	}

}
