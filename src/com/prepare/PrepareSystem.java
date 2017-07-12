package com.prepare;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import com.utils.FileUtils;
import com.utils.MySqlUtils;

public class PrepareSystem {

	public void createDirectories(String dbName) throws Exception {

		File file = new File(FileUtils.FILE_PATH_IN_PROGRESS);
		System.out.println("Directory Created: " + FileUtils.FILE_PATH_IN_PROGRESS);
		if (!file.exists()) {
			file.mkdirs();
		}
		// System.out.println("Directory Created: " +
		// FileUtils.FILE_PATH_COMPLETED);
		// file = new File(FileUtils.FILE_PATH_COMPLETED);
		// if (!file.exists()) {
		// file.mkdirs();
		// }
		// System.out.println("Directory Created: " +
		// FileUtils.FILE_PATH_ERROR_IMPORT);
		// file = new File(FileUtils.FILE_PATH_ERROR_IMPORT);
		// if (!file.exists()) {
		// file.mkdirs();
		// }
		//
		// System.out.println("Directory Created: " +
		// FileUtils.FILE_PATH_ERROR_EXPORT);
		// file = new File(FileUtils.FILE_PATH_ERROR_EXPORT);
		// if (!file.exists()) {
		// file.mkdirs();
		// }
	}

	@SuppressWarnings("unused")
	private void deleteFolders(String dbName) throws SQLException {

		List<String> dbList = MySqlUtils.getAllTableNames(dbName, true);

		File file = null;

		for (String tableName : dbList) {
			file = new File(FileUtils.FILE_PATH_IN_PROGRESS + File.separator + tableName);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		PrepareSystem set = new PrepareSystem();
		set.createDirectories("db_hot3l");
		// set.deleteFolders("db_hot3l");
	}

}
