### wacai-open-sdk是用来跟API网关交互的sdk

#### 依赖GAV
**依赖并未提交到maven中央仓库，建议使用者clone代码修改之后deploy到自己公司的私服**
```xml
<dependency>
   <groupId>com.wacai</groupId>
   <artifactId>wacai-open-sdk</artifactId>
   <version>${version}</version>
</dependency>
```
#### 核心功能
- 封装和授权系统的[交互逻辑](doc/api_auth.md)
- 封装接口序列化&反序列化 [定制序列化详情](doc/json_extend.md)
- 封装和API网关[协议](doc/api_entry.md)实现细节
- 封装参数[签名](doc/api_sign.md)逻辑
- 提供同步和异步回调实现，方便适应不同的编程模式
- 被调用时的参数[请求签名校验](doc/request_sign.md)


#### 核心依赖
- okhttp3 用于在http层面做通信
- slf4j-api 日志接口
- fastjson/jackson 用于序列化&反序列化
- commons-codes 签名字节数组的base64处理

#### 核心类和实现
- WacaiOpenApiClient 核心类，用于对API网关发起调用的Client
- WacaiOpenApiRequest API网关请求报文的封装类
- WacaiOpenApiResponse API网关响应报文的封装类

#### 代码案例

##### 构建Client和Request
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("${appKey}", "${appSecret}");
wacaiOpenApiClient.init();

WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
wacaiOpenApiRequest.putBizParam("card_id", "34121141242144");
wacaiOpenApiRequest.putBizParam("apply_money", 10);
```

##### 同步方式：
```java
OrderDeleteResponseObject wacaiOpenApiResponse = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<OrderDeleteResponseObject>() {});
```
**同步接口在异常返回时会抛出 WacaiOpenApiResponseException 异常，里边可以获取响应的错误码和详细的错误信息**

##### 异步方式：
```java
wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<OrderDeleteResponseObject>() {}, new WacaiOpenApiResponseCallback<OrderDeleteResponseObject>() {
    @Override
    public void onSuccess(OrderDeleteResponseObject data){
        log.error("success {}", data);
    }

    @Override
    public void onFailure(WacaiOpenApiResponseException ex) {
        log.error("failure {}", ex);
    }
});
```
