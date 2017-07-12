package com.multithreading;

import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.action.MySQLToFile7;
import com.utils.ConfigUtils;
import com.utils.MySqlUtils;

public class MySQLToFileThread2 extends Thread {

	private Document document;
	private final CountDownLatch latch;

	public MySQLToFileThread2(Document document, CountDownLatch latch) {

		this.document = document;
		this.latch = latch;
	}

	@Override
	public void run() {
		
		MySQLToFile7 migrator = new MySQLToFile7();

		try {
			
			Document mainDocument = ConfigUtils.getDocument();

			String dbName = mainDocument.getString(ConfigUtils.DB_NAME);
			
			String tableName = document.getString(ConfigUtils.TABLE_NAME);

			int startLimit = 0;
			int limit = MySqlUtils.getStaticQueryLimitStart(tableName);
			int endLimit = limit;
			int iteratorLength = MySqlUtils.getStaticIteratorLength(tableName);

			for (int i = 1; i <= iteratorLength; i++) {

				
				migrator.processTable(document);
				
				startLimit = endLimit;
				endLimit += limit;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.latch.countDown();

	}

	public static void main(String[] args) {

	}

}
