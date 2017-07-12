package com.file.handling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.bson.Document;

import com.utils.FileUtils;
import com.utils.MigratorDocument;

public class ConfigWriter {

	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;

	public ConfigWriter() {
	}

	public ConfigWriter(String path) {
		init(path);
	}

	public void init(String path) {
		File file = null;

		if (path != null) {
			file = new File(path);
		} else {
			file = new File(getFileName());
		}
		try {
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String data) throws IOException {

		init(null);

		bufferedWriter.write(data);
		bufferedWriter.write("\n");
	}

	public void write(String data, String path) throws IOException {

		init(path);

		bufferedWriter.write(data);
		bufferedWriter.write("\n");
	}

	public static String getFileName() {

		return FileUtils.FILE_PATH_CONFIG;
	}

	public void close() throws IOException {
		bufferedWriter.close();
		fileWriter.close();
	}

	public void clearFileContent() throws FileNotFoundException {

		clearFileContent(null);
	}

	public void clearFileContent(String path) throws FileNotFoundException {
		File file = null;

		if (path == null) {
			file = new File(getFileName());
		} else {
			file = new File(path);
		}


		// PrintWriter writer = new PrintWriter(file);
		// writer.print("");
		// writer.close();

	}

	public static MigratorDocument getDocument() {
		try {

			FileReader fr = new FileReader(getFileName());
			BufferedReader br = new BufferedReader(fr);

			String sCurrentLine;

			br = new BufferedReader(fr);

			while ((sCurrentLine = br.readLine()) != null) {
				Document document = Document.parse(sCurrentLine);
				return new MigratorDocument(document);
			}
			br.close();

		} catch (IOException e) {

			e.printStackTrace();
		} finally {
		}

		return null;

	}

	public void main(String[] args) {

	}
}
