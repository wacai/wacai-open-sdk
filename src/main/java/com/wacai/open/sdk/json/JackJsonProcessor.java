package com.wacai.open.sdk.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 */
public class JackJsonProcessor implements JsonProcessor {

    private ObjectMapper objMapper = new ObjectMapper();
    public JackJsonProcessor(){
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    @Override
    public <T> T deserialization(String json, Type type) {

        try {
            JavaType javaType = TypeFactory.defaultInstance().constructType(type);
            return objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialization(Object obj) {
        try {
           return objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String objToStr(Object obj) {
        try {
            return objMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Map<String, Object> objToMap(Object obj) {
        try {
            String jsonStr = objMapper.writeValueAsString(obj);
            return objMapper.readValue(jsonStr,Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
