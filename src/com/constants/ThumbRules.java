package com.constants;

public class ThumbRules {
	
	public enum ONE_TO_ONE {

		EMBED(1, "Embed -F- to -P-"), REFERENCE(2, "Reference ObjectID of -F- in -P-");

		private int id;
		private String value;

		private ONE_TO_ONE(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public int getId() {
			return id;
		}

	}

	public static enum ONE_TO_FEW {

		EMBED(1, "Embed all -F- columns to -P-"), REFERENCE(2, "Reference ObjectIDs of -F- in -P-");

		private int id;
		private String value;

		private ONE_TO_FEW(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public int getId() {
			return id;
		}

	}

	public static enum ONE_TO_MANY {

		REFERENCE_FOREIGN_COLUMNS_IN_PRIMARY_TABLE(1,
				"Reference ObjectIDs of -F- in -P-"), REFERENCE_PRIMARY_COLUMNS_IN_FOREIGN_TABLE(1,
						"Reference ObjectIDs of -P- in -F-"), DENORMALIZE_FOREIGN_COLUMNS_TO_PRIMARY_TABLE(1,
								"Denormalize Many-to-One | -F- -> -P-"), DENORMALIZE_PRIMARY_COLUMNS_TO_FOREIGN_TABLE(1,
										"Denormalize One-to-many | -P- -> -F-");

		private int id;
		private String value;

		private ONE_TO_MANY(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public int getId() {
			return id;
		}

	}

	public static enum ONE_TO_SQUILLION {

		REFERENCE1(2, "Reference ObjectIDs of -P- in -F-"), DEMORMALIZE_MANY_TO_SQUILLION(1,
				"Denormalize Many-to-Squillion | -F- -> -P-");

		private int id;
		private String value;

		private ONE_TO_SQUILLION(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public int getId() {
			return id;
		}

	}

}
