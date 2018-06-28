package com.wacai.open.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Version {

	private static final int PROTOCOL_VERSION;

	private static final String SDK_VERSION;

	static {
		InputStream sdkStream = Version.class.getClassLoader()
				.getResourceAsStream("config/sdk.properties");

		Properties properties = new Properties();
		try {
			properties.load(sdkStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		SDK_VERSION = properties.getProperty("sdk.version");
		PROTOCOL_VERSION = Integer.parseInt(properties.getProperty("protocol.version"));
	}

	public static int getProtocolVersion() {
		return PROTOCOL_VERSION;
	}

	public static String getSdkVersion() {
		return SDK_VERSION;
	}
}
