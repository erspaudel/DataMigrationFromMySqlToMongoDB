package com.utils;

import java.util.List;

public class StringUtils {

	public static boolean isNull(String str) {

		if (str == null || str.trim().length() == 0 || str.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean containsIgnoreCase(String str, String searchStr) {
		if (str == null || searchStr == null) {
			return false;
		}
		int len = searchStr.length();
		int max = str.length() - len;
		for (int i = 0; i <= max; i++) {
			if (str.regionMatches(true, i, searchStr, 0, len)) {
				return true;
			}
		}
		return false;
	}

	public static String trim(String str) {

		return (str == null ? null : str.trim());
	}

	public static boolean isNotEmpty(String str) {

		return (str != null && str.trim().length() > 0);
	}

	public static boolean isEmpty(String str) {

		return (str == null || str.trim().length() == 0);
	}

	public static boolean equals(String str1, String str2) {

		return (str1 == null ? str2 == null : str1.equals(str2));
	}

	public static boolean equalsIgnoreCase(String str1, String str2) {

		return (str1 == null ? str2 == null : str1.equalsIgnoreCase(str2));
	}

	public static boolean isAlphanumeric(String str) {

		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isLetterOrDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumeric(String str) {

		return isNumeric(str, false);
	}

	public static boolean isNumeric(String str, boolean allowNegative) {

		if (StringUtils.isEmpty(str)) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {

			if (allowNegative && i == 0 && '-' == str.charAt(i))
				continue;

			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean isDecimalNumber(String str) {

		if (str == null) {
			return false;
		}

		return str.matches("-?[0-9]+(\\.[0-9]+)?");

	}

	public static String getSafeString(String s, String sDefault) {

		if (s == null) {
			return sDefault;
		}
		return s;
	}

	public static String getSafeString(String s) {

		if (s == null) {
			return "";
		}
		return s;
	}

	public static String toString(Object object) {

		return object == null ? "" : object.toString();
	}

	public static String arrayToString(String[] array) {
		StringBuffer result = new StringBuffer();
		for (String s : array) {
			result.append(s);
		}
		return result.toString();
	}

	public static boolean containsIgnoreCase(List<String> stringList, String testString) {

		for (String str : stringList) {

			if (StringUtils.equalsIgnoreCase(str, testString)) {
				return true;
			}
		}

		return false;
	}

	public static String[] convertListToArray(List<String> stringList) {

		String[] stringArray = new String[stringList.size()];
		for (int i = 0; i < stringArray.length; i++) {
			stringArray[i] = stringList.get(i);

		}
		return stringArray;
	}

	public static String getAsciiValue(String value) {
		StringBuilder builder = new StringBuilder();

		char[] charArr = value.toCharArray();

		for (char c : charArr) {
			builder.append((int) c);
		}

		System.out.println("ASCII: " + builder.toString());

		return builder.toString();
	}

}
