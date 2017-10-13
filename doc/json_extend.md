### JSON序列化与反序列化

#### 概述
- sdk默认使用fastjson作为json处理类
- sdk提供fastjson和jackson两种选择，同时提供自定义扩展
- 选择一种依赖可以排除其它相关依赖，详见依赖选择与排除

#### 依赖选择与排除
``` 
1.选择fastjson
<dependency>
   <groupId>com.wacai</groupId>
   <artifactId>wacai-open-sdk</artifactId>
   <version>1.0.0-SNAPSHOT</version>
   <exclusions>
     <exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
         <artifactId>jackson-core</artifactId>
      </exclusion>
      exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
      </exclusion>
      exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-annotations</artifactId>
      </exclusion>
   </exclusions>
</dependency>

2.选择jackson
<dependency>
   <groupId>com.wacai</groupId>
   <artifactId>wacai-open-sdk</artifactId>
   <version>1.0.0-SNAPSHOT</version>
   <exclusions>
     <exclusion>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
      </exclusion>
   </exclusions>
</dependency>

```
#### 代码使用样例
##### 默认json处理类
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient(${appKey}, ${appSecret});
wacaiOpenApiClient.init();

WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
wacaiOpenApiRequest.putBizParam("card_id", "34121141242144");
wacaiOpenApiRequest.putBizParam("apply_money", 10);   

// 不设置将首先使用fastjson作为默认处理类

```
#### 扩展
##### 自定义json处理类
```
public class FastJsonProcessor implements JsonProcessor {
    @Override
    public <T> T deserialization(String json, Type type) {
        return JSON.parseObject(json,type);
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
            JSONObject objJson = (JSONObject)JSONObject.toJSON(obj);
            Map<String, Object> resMap = new HashMap<>();
            resMap.putAll(objJson);
            return resMap;
        }
        throw new IllegalArgumentException("pojoParam " + obj + " is not kv object");
    }
}

```
##### 使用自定义json处理类
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient(${appKey}, ${appSecret});
wacaiOpenApiClient.setProcessor("自定义json处理类");
wacaiOpenApiClient.init();

WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
wacaiOpenApiRequest.putBizParam("card_id", "34121141242144");
wacaiOpenApiRequest.putBizParam("apply_money", 10);  
```
