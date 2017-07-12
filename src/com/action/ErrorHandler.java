package com.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.CountDownLatch;

import com.multithreading.FileToMongoDBThread;
import com.multithreading.MySQLToFileThreadErrorHandling;
import com.utils.ConfigUtils;
import com.utils.FileUtils;
import com.utils.JavaFxUtils;
import com.utils.StringUtils;
import com.utils.TestUtils;

import javafx.application.Platform;

public class ErrorHandler {

	private void handleError(String path) {

		File[] directories = new File(path).listFiles(File::isDirectory);

		if (directories == null || directories.length == 0) {
			return;
		}

		CountDownLatch latch = new CountDownLatch(directories.length);

		for (File file : directories) {
			FileReader fileReader;
			try {

				File[] filesDirectory = file.listFiles();

				File file2 = filesDirectory[0];

				fileReader = new FileReader(file2);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String val = null;

				StringBuilder builder = new StringBuilder();

				while ((val = bufferedReader.readLine()) != null) {
					builder.append(val + ",");
				}

				if (StringUtils.isEmpty(builder.toString())) {
					bufferedReader.close();
					return;
				}

				String ids = builder.substring(0, builder.length() - 1);

				MySQLToFileThreadErrorHandling thread = new MySQLToFileThreadErrorHandling(
						ConfigUtils.getDocument(file.getName()), latch, ids);
				thread.run();

				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		try {
			latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		directories = new File(FileUtils.FILE_PATH_IN_PROGRESS).listFiles(File::isDirectory);

		latch = new CountDownLatch(directories.length);

		for (File file : directories) {

			System.out.println(file.getAbsolutePath());
			System.err.println("\t" + file.getName().toUpperCase());

			FileToMongoDBThread thread = new FileToMongoDBThread(file, latch);

			thread.start();

			FileUtils.deleteEmptyDirectory(file);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void handleExportError() {
		handleError(FileUtils.FILE_PATH_ERROR_EXPORT);
	}

	private void handleImportError() {
		handleError(FileUtils.FILE_PATH_ERROR_IMPORT);
	}

	public static void startErrorHandling() throws InterruptedException {

		TestUtils.ENABLE_EXPORT_TEST = false;
		TestUtils.ENABLE_IMPORT_TEST = false;

		ErrorHandler handler = new ErrorHandler();

		System.out.println("IMPORT ERROR HANDLING...");

		handler.handleImportError();

		if (TestUtils.ENABLE_THREAD_SLEEP) {

			Thread.sleep(60000);
		}

		System.out.println("Waiting for export error handling....");

		System.out.println("IMPORT ERROR HANDLING...");

		handler.handleExportError();

		System.out.println("ALL PROCESS COMPLETED");

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				try {
					JavaFxUtils.showNextPage("FinishPage.fxml");
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

	public static void main(String[] args) throws InterruptedException {
		// MySqlUtils.init("hotel");
		startErrorHandling();
	}
}
