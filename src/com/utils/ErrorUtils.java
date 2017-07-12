package com.utils;

import org.bson.Document;

import com.file.handling.ErrorWriter;

public class ErrorUtils {

	public static final int ERROR_TYPE_IMPORT = 1;
	public static final int ERROR_TYPE_EXPORT = 2;

	public static void processError(String tableName, Document document, int code) {
		try {
			ErrorWriter error = new ErrorWriter(tableName, code);
			error.write(document.getString("id"));
			error.close();
		} catch (Exception ex) {

		}
	}

}
