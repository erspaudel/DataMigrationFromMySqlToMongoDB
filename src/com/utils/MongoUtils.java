package com.utils;

import org.bson.types.ObjectId;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class MongoUtils {

	public static ObjectId getObjectId(int counter) {
		return ObjectId.createFromLegacyFormat(ConfigUtils.getUnixTime(), com.mongo.ObjectId.createMachineIdentifier(),
				counter);
	}

	public static ObjectId getObjectId() {
		return new ObjectId();
	}

	public static int getUnixTime() {

		long unixTime = System.currentTimeMillis() / 1000L;
		String unixStr = String.valueOf(unixTime);
		if (StringUtils.isNumeric(unixStr)) {
			return Integer.parseInt(unixStr);
		}

		throw new NumberFormatException("The MySql table id should be interger value");
	}

	public static void main(String[] args) {
		System.out.println(getObjectId(1));
		System.out.println(getUnixTime());
		System.out.println(getUnixTime());
	}

}
