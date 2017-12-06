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

  public static void initJsonProcess(JsonProcessor processor){
    //显示指定json处理类优先级最高,支持覆盖
    if (processor != null) {
      JsonConfig.getInstance().setDefaultProcessor(processor);
    }
    //json处理类初始化标记
    boolean jsonNeedInitFlag = false;
    try {
      JsonConfig.getInstance().getDefaultProc();
    } catch (Exception e) {
      jsonNeedInitFlag = true;
    }
    //没有设置json处理类 &&其它地方也没有初始化
    if (processor == null&&jsonNeedInitFlag) {
      try {
        Class.forName("com.alibaba.fastjson.JSON");
        processor = new FastJsonProcessor();
      }
      catch (ClassNotFoundException e) {
        try {
          processor = (JsonProcessor) Class.forName(JsonConst.JACKSON_KEY).newInstance();
        }
        catch (Exception e1) {
          throw new RuntimeException(e1);
        }
      }
    }
    if (jsonNeedInitFlag) {
      JsonConfig.getInstance().setDefaultProcessor(processor);
    }
    JsonConfig.getInstance().init();
  }
}
