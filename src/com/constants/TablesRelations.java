package com.constants;

import com.utils.ConfigUtils;

public enum TablesRelations {

	ONE_TO_ONE(ConfigUtils.ONE_to_ONE, "One to One"), ONE_TO_FEW(ConfigUtils.ONE_to_FEW, "One to Few"), ONE_TO_MANY(
			ConfigUtils.ONE_to_MANY,
			"One to Many"), ONE_TO_SQUILLION(ConfigUtils.ONE_to_SQUILLION, "One to Squillions");

	private int id;
	private String value;

	private TablesRelations(int id, String value) {
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
