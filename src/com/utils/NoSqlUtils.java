
package com.utils;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class NoSqlUtils {

	public static String DB_NAME = "Test";
	public static final String URL = "mongodb://localhost:27017";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "";

	private static MongoClientURI mongoClientURI;
	private static MongoClient mongoClient;
	private static MongoDatabase mongoDatabase;

	static {
		init();
	}

	public static void init() {
		mongoClientURI = new MongoClientURI(URL);
		mongoClient = new MongoClient(mongoClientURI);
		String dbName = ConfigUtils.getDbName();
		DB_NAME = dbName;
		mongoDatabase = mongoClient.getDatabase(DB_NAME);
	}

	public static MongoClient getMongoClient() {
		return mongoClient;
	}

	public static void printNoSql() {
		MongoClient mongoClient = getMongoClient();
		MongoDatabase database = mongoClient.getDatabase("Test");

		MongoCollection<Document> collection = database.getCollection("user");

		MongoCursor<Document> cursor = collection.find().iterator();
		try {
			while (cursor.hasNext()) {
				System.out.println(MigratorUtils.getPrettyJson(cursor.next().toJson()));
			}
		} finally {
			cursor.close();
		}

		/*
		 * Document doc = new Document("name", "MongoDB") .append("type",
		 * "database") .append("count", 1) .append("info", new Document("x",
		 * 203).append("y", 102)); collection.insertOne(doc);
		 */
	}

	public static MongoCollection<Document> getMongoCollection(String collectionName) {
		return mongoDatabase.getCollection(collectionName);
	}

	public static MongoCollection<Document> getMongoCollection(String databaseName, String collectionName) {
		init();
		return mongoDatabase.getCollection(collectionName);
	}

	public static void createCollection(String databaseName, String collectionName) {
		getMongoDatabase(databaseName).createCollection(collectionName);
	}

	public static void createCollection(String collectionName) {
		mongoDatabase.createCollection(collectionName);
	}

	public static MongoDatabase getMongoDatabase(String dbName) {

		if (StringUtils.isEmpty(dbName)) {
			return mongoDatabase;
		}

		mongoDatabase = mongoClient.getDatabase(dbName);
		return mongoDatabase;
	}

	public static void saveDocuments(String collectionName, List<Document> documents) {

		MongoCollection<Document> collection = getMongoCollection(collectionName);

		for (Document document : documents) {

			try {

				if (TestUtils.ENABLE_IMPORT_TEST) {
					List<String> ids = new ArrayList<>();
					ids.add("2");
					ids.add("3");
					ids.add("5");
					ids.add("10");
					ids.add("20");
					ids.add("30");
					ids.add("50");

					if (ids.contains(document.getString("id"))) {
						// throw new Exception("ID under import testing...");
						System.err.println("ID under export testing: " + document.getString("id"));
					}
				}

				collection.insertOne(document);

			} catch (Exception ex) {

				System.out.println("Catching id: " + document.getString("id"));

				ErrorUtils.processError(collectionName, document, ErrorUtils.ERROR_TYPE_IMPORT);
			}
		}
	}

	public static MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}

	public static boolean doesCollectionExist(String collectionName) {
		return mongoDatabase.getCollection(collectionName).count() > 0 ? true : false;
	}

}
