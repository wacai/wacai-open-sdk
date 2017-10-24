package com.wacai.open.sdk.json;

import java.lang.reflect.Type;
import java.util.Map;

/**
 */
public class JsonTool {

  private static JsonConfig config = JsonConfig.getInstance();

  public static <T> T deserialization(String json, Type type) {
    JsonProcessor defaultProc = config.getDefaultProc();
    return defaultProc.deserialization(json, type);
  }

  public static byte[] serialization(Object obj) {
    JsonProcessor defaultProc = config.getDefaultProc();
    return defaultProc.serialization(obj);
  }

  public static Map<String, Object> objToMap(Object obj) {
    JsonProcessor defaultProc = config.getDefaultProc();
    return defaultProc.objToMap(obj);
  }

  public static String objToStr(Object obj) {
    JsonProcessor defaultProc = config.getDefaultProc();
    return defaultProc.objToStr(obj);
  }
}
