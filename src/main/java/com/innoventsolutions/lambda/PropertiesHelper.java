package com.innoventsolutions.lambda;

import java.util.Properties;

public class PropertiesHelper {
	private final Properties properties;

	public PropertiesHelper(final Properties properties) {
		this.properties = properties;
	}

	public String get(final String key, final String defaultValue) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public int get(final String key, final int defaultValue) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}
}
