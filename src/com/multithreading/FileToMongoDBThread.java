package com.multithreading;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import com.action.FileToMongoDb;

public class FileToMongoDBThread extends Thread {

	private File file;
	private final CountDownLatch latch;

	public FileToMongoDBThread(File file, CountDownLatch latch) {

		this.file = file;
		this.latch = latch;
	}

	@Override
	public void run() {

		FileToMongoDb migrator = new FileToMongoDb();

		try {
			migrator.migrate(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		latch.countDown();

	}

	public static void main(String[] args) {

	}

}
