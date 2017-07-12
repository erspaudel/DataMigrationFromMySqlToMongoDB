package com.file.handling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.utils.ErrorUtils;
import com.utils.FileUtils;

public class ErrorWriter {

	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	public static final String finalFileName = "Result";

	public ErrorWriter(String fileName, int code) throws Exception {

		FileUtils.createDirectory(getDirectory(code) + File.separator + fileName);

		init(fileName, code);
	}

	public void init(String fileName, int code) throws Exception {

		File file = new File(getFileName(fileName, code));

		fileWriter = new FileWriter(file, true);
		bufferedWriter = new BufferedWriter(fileWriter);
	}

	public void write(String data) throws IOException {
		
		bufferedWriter.write(data);
		bufferedWriter.write("\n");
	}

	public static String getDirectory(int code) throws Exception {

		if (code == ErrorUtils.ERROR_TYPE_IMPORT) {
			return FileUtils.FILE_PATH_ERROR_IMPORT;
		} else if (code == ErrorUtils.ERROR_TYPE_EXPORT) {
			return FileUtils.FILE_PATH_ERROR_EXPORT;
		}

		throw new Exception("Undefined Error Type!");
	}

	public static String getFileName(String fileName, int code) throws Exception {

		return getDirectory(code) + File.separator + fileName + File.separator + fileName + FileUtils.FILE_EXTENTION_TXT;
	}

	public void close() throws IOException {
		bufferedWriter.close();
		fileWriter.close();
	}

	public void main(String[] args) {

	}

}
