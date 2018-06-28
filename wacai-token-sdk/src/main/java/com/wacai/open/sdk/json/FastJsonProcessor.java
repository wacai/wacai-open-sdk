package com.wacai.open.sdk.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class FastJsonProcessor implements JsonProcessor {

	@Override
	public <T> T deserialization(String json, Type type) {
		return JSON.parseObject(json, type);
	}

	@Override
	public byte[] serialization(Object obj) {
		return JSON.toJSONBytes(obj);
	}

	@Override
	public String objToStr(Object obj) {
		return JSON.toJSONString(obj);
	}

	@Override
	public Map<String, Object> objToMap(Object obj) {
		if (obj != null && JSONObject.toJSON(obj) instanceof JSONObject) {
			JSONObject objJson = (JSONObject) JSONObject.toJSON(obj);
			Map<String, Object> resMap = new HashMap<>();
			resMap.putAll(objJson);
			return resMap;
		}
		throw new IllegalArgumentException("pojoParam " + obj + " is not kv object");
	}
}
