package com.converter;

import com.prepare.PrepareSystem;

/**
 * 
 * @author Sushil Paudel
 *
 */
public class Converter {

	public static void main2(String[] args) throws Exception {

		String dbName = "db_hot3l";

		Converter converter = new Converter();

		/*
		 * Prepare System
		 */
		converter.highlight("Preparing System");

		PrepareSystem prepare = new PrepareSystem();
		prepare.createDirectories(dbName);

		/*
		 * Parse
		 */
		converter.highlight("Converting MySql data to JSON");

//		MySqlToFile parser = new MySqlToFile(dbName);
//		parser.parse();

		/*
		 * Import MySql
		 */

		converter.highlight("Converting JSON data to MongoDB");

//		FileToMongoDb importer = new FileToMongoDb();
//		importer.migrateAll();

		/*
		 * Handling Error
		 */
		
//		ErrorHandler error = new ErrorHandler();
//		error.handleExportError();
//		error.handleImportError();

	}

	public void highlight(String text) {
		System.err.println(
				"\n####################################################################################################");
		System.err.println("\t\t\t\t\t" + text.toUpperCase() + "\t\t\t\t\t");
		System.err.println(
				"####################################################################################################\n");
	}

	public static void test() {

	}

}
