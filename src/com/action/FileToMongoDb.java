package com.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.utils.ConfigUtils;
import com.utils.FileUtils;
import com.utils.NoSqlUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */
public class FileToMongoDb {

	// private void migrateAll() throws Exception {
	//
	// /*
	// * Array of directories under
	// */
	// File[] directories = new
	// File(FileUtils.FILE_PATH_IN_PROGRESS).listFiles(File::isDirectory);
	//
	// for (File file : directories) {
	//
	// System.out.println(file.getAbsolutePath());
	// System.err.println("\t" + file.getName().toUpperCase());
	//
	// processTable(file.getAbsolutePath());
	//
	// FileUtils.deleteEmptyDirectory(file);
	// }
	//
	// File file = new File(FileUtils.FILE_PATH_IN_PROGRESS);
	// FileUtils.deleteEmptyDirectory(file);
	// }

	// private void processTable(String path) throws Exception {
	//
	// File file = new File(path);
	// File[] files = file.listFiles();
	// for (File sourceFile : files) {
	// System.out.println("SOURCE: ----------------------------" +
	// sourceFile.getName());
	// migrate(sourceFile);
	// File destFile = new File(FileUtils.FILE_PATH_COMPLETED + File.separator +
	// getCollectionName(sourceFile)
	// + File.separator + sourceFile.getName());
	// handleFile(sourceFile, destFile);
	// }
	// }

	public void migrate(String filePath) throws Exception {

		File file = new File(filePath);
		migrate(file);
	}

	public void migrate(File file) throws Exception {

		File[] directories = file.listFiles();

		if (directories == null) {
			return;
		}

		String collectionName = getCollectionName(file, false);

		for (File tableFile : directories) {

			List<Document> documents = parse(tableFile.getAbsolutePath());
			NoSqlUtils.saveDocuments(collectionName, documents);

			if (ConfigUtils.shouldBackUpFiles()) {
				moveFile(tableFile);
			} else {
				tableFile.delete();
			}

		}

		FileUtils.deleteEmptyDirectory(file);
		FileUtils.deleteEmptyParentDirectory(file);

	}

	public void moveFile(File sourceFile) throws Exception {

		File destFile = new File(FileUtils.FILE_PATH_COMPLETED + File.separator + getCollectionName(sourceFile, true)
				+ File.separator + sourceFile.getName());
		handleFile(sourceFile, destFile);

	}

	private List<Document> parse(String fileName) throws Exception {

		List<Document> documents = new ArrayList<>();
		String val;
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while ((val = bufferedReader.readLine()) != null) {

			Document doc = Document.parse(val);
			documents.add(doc);
		}

		bufferedReader.close();

		return documents;
	}

	private String getCollectionName(File file, boolean useParent) {

		String collectionName = file.getAbsolutePath();
		if (useParent) {
			collectionName = file.getParent();
		}
		int startIndex = collectionName.lastIndexOf("/") + 1;
		int endIndex = collectionName.length();
		collectionName = collectionName.substring(startIndex, endIndex);

		return collectionName;
	}

	private void handleFile(File source, File destination) throws Exception {
		FileUtils.moveFile(source, destination);
	}

	public static void main(String[] args) throws Exception {

//		FileToMongoDb importer = new FileToMongoDb();
		// importer.migrateAll();

		// importer.processTable("/Users/kuldipadhikari/Desktop/MySqlToNoSql/In_Progress/zones");

	}

}
