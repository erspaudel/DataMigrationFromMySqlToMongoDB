package com.utils;

import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.utils.SystemUtils.RAMUtils;
import com.utils.SystemUtils.SpaceUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class MigratorUtils {

	public static String getPrettyJson(String data) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(data);
		return gson.toJson(je);

	}

	public static boolean shouldProcessSerially() {
		return false;
	}

	public static boolean shouldUseFileMigration(String dbName) throws SQLException {

		int dbSize = MySqlUtils.getDBSize(dbName);
		int usableSpace = SpaceUtils.getUsableSpace();

		if (dbSize >= usableSpace) {
			return false;
		}

		int availableMemory = RAMUtils.getPhysicalMemorySize();
		int freeMemory = RAMUtils.getFreePhysicalMemorySize();
		float MEMORY_THRESHOLD = 0.2f;

		float memoryRatio = (float) freeMemory / availableMemory;

		if (memoryRatio < MEMORY_THRESHOLD) {
			return false;
		}

		return false;
	}

	public static void main(String[] args) {

	}

}
