package com.wacai.open.sdk.json;

/**
 */
public class JsonConfig {

	private volatile static JsonConfig instance;

	private JsonProcessor defaultProc;//json默认处理类


	private JsonConfig() {

	}

	public static JsonConfig getInstance() {
		if (instance == null) {
			synchronized (JsonConfig.class) {
				if (instance == null) {
					instance = new JsonConfig();
				}
			}
		}
		return instance;
	}


	/**
	 * 设置默认json处理器
	 * @param value json处理器
	 */
	public void setDefaultProcessor(JsonProcessor value) {
		defaultProc = value;
	}

	public JsonProcessor getDefaultProc() {
		if (defaultProc == null) {
			throw new RuntimeException("json处理类初始化失败,没有设置默认json处理类");
		}
		return defaultProc;
	}


	public void init() {
		if (defaultProc == null) {
			throw new RuntimeException("json处理类初始化失败,没有设置默认json处理类");
		}
	}
}
