package com.wacai.open.sdk.json;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSON 处理接口
 */
public interface JsonProcessor {


    <T> T deserialization(String json, Type type);

    byte[] serialization(Object obj);

    String objToStr(Object obj);

    Map<String,Object> objToMap(Object obj);

}
