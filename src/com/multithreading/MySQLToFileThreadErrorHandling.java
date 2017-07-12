package com.multithreading;

import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.action.MySQLToFileErrorHandling;

public class MySQLToFileThreadErrorHandling implements Runnable {

	private Document document;
	private final CountDownLatch latch;
	private String ids;

	public MySQLToFileThreadErrorHandling(Document document, CountDownLatch latch, String ids) {

		this.document = document;
		this.latch = latch;
		this.ids = ids;
	}

	@Override
	public void run() {

		MySQLToFileErrorHandling migrator = new MySQLToFileErrorHandling();

		try {
			migrator.processTable(document, ids);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.latch.countDown();

	}

	public static void main(String[] args) {

	}

}
