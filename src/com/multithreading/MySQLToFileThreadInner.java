package com.multithreading;

import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.action.MySQLToFile;

public class MySQLToFileThreadInner extends Thread {

	private Document document;
	private final CountDownLatch latch;
	private int startLimit;
	private int endLimit;
	private int iterateCounter;

	public MySQLToFileThreadInner(Document document, CountDownLatch latch, int startLimit, int endLimit,
			int iterateCounter) {

		this.document = document;
		this.latch = latch;
		this.startLimit = startLimit;
		this.endLimit = endLimit;
		this.iterateCounter = iterateCounter;
	}

	@Override
	public void run() {

		MySQLToFile migrator = new MySQLToFile();

		try {

			migrator.processTable(document, startLimit, endLimit, iterateCounter);

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.latch.countDown();

	}

	public static void main(String[] args) {

	}

}
