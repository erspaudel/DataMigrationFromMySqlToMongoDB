package com.multithreading;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;

import com.utils.ConfigUtils;
import com.utils.MySqlUtils;

public class MySQLToFileThread extends Thread {

	private Document document;
	private final CountDownLatch latch;

	public MySQLToFileThread(Document document, CountDownLatch latch) {

		this.document = document;
		this.latch = latch;
	}

	@Override
	public void run() {

		CountDownLatch innerLatch = null;

		try {

			Document mainDocument = ConfigUtils.getDocument();

			String dbName = mainDocument.getString(ConfigUtils.DB_NAME);
			String tableName = document.getString(ConfigUtils.TABLE_NAME);

			int startLimit = 0;
			int limit = MySqlUtils.getStaticQueryLimitStart(tableName);
			int endLimit = limit;
			int iteratorLength = MySqlUtils.getStaticIteratorLength(tableName);

			innerLatch = new CountDownLatch(iteratorLength);
			
			ExecutorService executor = Executors.newFixedThreadPool(ConfigUtils.FIXED_THREAD_POOL);

			for (int i = 1; i <= iteratorLength; i++) {
				
				MySQLToFileThreadInner inner = new MySQLToFileThreadInner(document, innerLatch, startLimit, endLimit,
						i);
				
				executor.execute(inner);

				startLimit = endLimit;
				endLimit += limit;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			innerLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.latch.countDown();

	}

	public static void main(String[] args) {

	}

}
