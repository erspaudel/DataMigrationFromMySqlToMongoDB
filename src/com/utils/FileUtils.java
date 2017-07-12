package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtils {

	public static String ROOT_FILE_PATH = "";
	public static String FILE_PATH_IN_PROGRESS = ROOT_FILE_PATH + "/In_Progress";
	public static String FILE_PATH_COMPLETED = ROOT_FILE_PATH + "/Completed";
	public static String FILE_PATH_ERROR = ROOT_FILE_PATH + "/Error";
	public static String FILE_PATH_ERROR_IMPORT = FILE_PATH_ERROR + "/Import";
	public static String FILE_PATH_ERROR_EXPORT = FILE_PATH_ERROR + "/Export";
	public static String FILE_EXTENTION_TXT = ".txt";
	public static String FILE_EXTENTION_JSON = ".json";

	public static String FILE_CONFIG = "Config" + FILE_EXTENTION_TXT;

	public static String FILE_PATH_CONFIG = ROOT_FILE_PATH + File.separator + FILE_CONFIG;

	public static void init() {

		init(null);
	}

	public static void init(String path) {

		if (path == null) {
			ROOT_FILE_PATH = ConfigUtils.getMigrationDirectory();
		} else {
			ROOT_FILE_PATH = path;
		}

		FILE_PATH_IN_PROGRESS = ROOT_FILE_PATH + "/In_Progress";
		FILE_PATH_COMPLETED = ROOT_FILE_PATH + "/Completed";
		FILE_PATH_ERROR = ROOT_FILE_PATH + "/Error";
		FILE_PATH_ERROR_IMPORT = FILE_PATH_ERROR + "/Import";
		FILE_PATH_ERROR_EXPORT = FILE_PATH_ERROR + "/Export";

		FILE_PATH_CONFIG = ROOT_FILE_PATH + File.separator + FILE_CONFIG;
	}

	public static boolean moveFile(File source, File dest) throws Exception {

		System.out.println("FileUtils.moveFile()");

		if (!source.exists()) {
			return false;
		}

		if (source.length() == 0) {

			source.delete();

			// if (!dest.createNewFile()) {
			// return false;
			// }

		} else {

			if (!FileUtils.copyFile(source, dest)) {
				return false;
			}

		}

		if (!source.delete()) {
			return false;
		}

		return true;

	}

	public static boolean copyFile(File sourceFile, File destFile) throws Exception {

		if (sourceFile == null || destFile == null) {
			throw new Exception("Source or destination file null");
		}

		System.out.println("SRC: " + sourceFile + ", " + sourceFile.isFile());
		System.out.println("DEST: " + destFile + ", " + destFile.isFile());

		if (!sourceFile.isFile()) {
			return false;
		}

		if (StringUtils.equalsIgnoreCase(".DS_Store", sourceFile.getName())
				|| StringUtils.equalsIgnoreCase(".DS_Store", destFile.getName())) {
			return false;
		}

		System.out.println("Before creating...");

		if (sourceFile.length() == 0) {

			if (destFile.exists())
				destFile.delete();

			if (destFile.createNewFile()) {

				return true;

			} else {

				throw new Exception("Could not create empty file! " + destFile.getAbsolutePath());

			}

		}

		System.out.println("Source File: " + sourceFile);

		try (FileInputStream in = new FileInputStream(sourceFile);
				FileOutputStream out = new FileOutputStream(destFile)) {

			byte buf[] = new byte[2048];
			int len = in.read(buf);

			if (len == -1) {
				throw new Exception("Bad initial block file read! " + sourceFile.getAbsolutePath());
			}

			while (len != -1) {
				out.write(buf, 0, len);
				len = in.read(buf);
			}

			out.flush();

		}

		return true;

	}

	public static void delete(File file) {

		if (file != null) {
			file.delete();
		}
	}

	public static void deleteEmptyDirectory(File file) {

		if (isDirectoryEmpty(file)) {
			delete(file);
		}
	}

	public static void deleteEmptyParentDirectory(File file) {

		File parent = new File(file.getParent());

		File[] files = parent.listFiles();

		if (files.length == 0) {
			parent.delete();
		}

	}

	public static boolean isDirectoryEmpty(File file) {

		if (!file.isDirectory()) {
			return false;
		}

		if (file.listFiles().length == 0) {
			return true;
		}

		return false;
	}

	public static void createDirectory(File file) {

		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static void createDirectory(String path) {

		File file = new File(path);

		if (!file.exists()) {
			file.mkdirs();
		}
	}
}
