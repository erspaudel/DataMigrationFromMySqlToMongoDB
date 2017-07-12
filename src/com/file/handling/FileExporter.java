package com.file.handling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.utils.ConfigUtils;
import com.utils.ErrorUtils;
import com.utils.FileUtils;
import com.utils.TestUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */
public class FileExporter {

	private File file;
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;

	/*
	 * public MySqlExporter(String fileName) throws IOException {
	 * 
	 * fileName = getFileName(fileName); System.out.println(
	 * "MySqlExporter.MySqlExporter(): " + fileName); File file =
	 * getFile(fileName); fileWriter = new FileWriter(file); bufferedWriter =
	 * new BufferedWriter(fileWriter); }
	 */
	public FileExporter(String fileName, int counter) throws IOException {

		createInProgressDirectory(fileName);

		if (ConfigUtils.shouldBackUpFiles()) {

			createbBackUpDirectory(fileName);
		}

		file = new File(getFileName(fileName, counter));

		fileWriter = new FileWriter(file);
		bufferedWriter = new BufferedWriter(fileWriter);
	}

	private void write(String data) throws IOException {

		bufferedWriter.write(data);
		bufferedWriter.write("\n");
	}

	public void write(String fileName, List<Document> documents) throws Exception {

		for (Document document : documents) {
			try {
//				System.out.println("\t\tWriting: " + document.toJson());

				if (TestUtils.ENABLE_EXPORT_TEST) {
					List<String> ids = new ArrayList<>();
					ids.add("1");
					ids.add("4");
					ids.add("15");
					ids.add("25");
					ids.add("35");
					ids.add("45");
					ids.add("55");

					if (ids.contains(document.getString("id"))) {
//						throw new Exception("ID under export testing...");
						System.err.println("ID under export testing: "+document.getString("id"));
					}
				}

				write(document.toJson());
			} catch (Exception e) {
				e.printStackTrace();

				ErrorUtils.processError(fileName, document, ErrorUtils.ERROR_TYPE_EXPORT);
				// int id = document.getInteger("id");
				// ErrorWriter er = new ErrorWriter(fileName,
				// ErrorUtils.ERROR_TYPE_EXPORT);
				// er.write(id + "\n");
			}
		}
	}

	private void createInProgressDirectory(String fileName) {

		File file = new File(FileUtils.FILE_PATH_IN_PROGRESS + File.separator + fileName);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	private void createbBackUpDirectory(String fileName) {

		File file = new File(getBackUpDirectory(fileName));

		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void backUpCompleted() throws Exception {

		File destFile = new File(getBackUpDirectory(file.getName()));

		FileUtils.copyFile(file, destFile);
	}

	public String getBackUpDirectory(String fileName) {
		return FileUtils.FILE_PATH_COMPLETED + File.separator + fileName;
	}

	public String getFileName(String fileName, int counter) {

		return FileUtils.FILE_PATH_IN_PROGRESS + File.separator + fileName + File.separator + fileName + "_" + counter
				+ FileUtils.FILE_EXTENTION_TXT;
	}

	public void close() throws IOException {
		bufferedWriter.close();
		fileWriter.close();
	}

	public String getFilePath() {
		return file.getAbsolutePath();
	}

	public static void main(String[] args) {

	}
}
