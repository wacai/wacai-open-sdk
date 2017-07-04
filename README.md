### wacai-open-sdk用来跟API网关交互的sdk

#### 依赖GAV
```xml
<dependency>
   <groupId>com.wacai</groupId>
   <artifactId>wacai-open-sdk</artifactId>
   <version>1.0.0-SNAPSHOT</version>
</depenency>
```
#### 代码案例

##### 同步方式：
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("dasdklafjaksjfkla", "dasdklafjaksjfkla");
wacaiOpenApiClient.setGatewayEntryUrl("http://guard.test.wacai.info/api_entry");
wacaiOpenApiClient.setGatewayAuthUrl("http://guard.test.wacai.info/auth");
wacaiOpenApiClient.init();

WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
wacaiOpenApiRequest.putBizParam("card_id", "我是中文");
wacaiOpenApiRequest.putBizParam("apply_money", 10);

WacaiOpenApiResponse<OrderDeleteResponseObject> wacaiOpenApiResponse = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<WacaiOpenApiResponse<OrderDeleteResponseObject>>() {});
```

##### 异步方式：
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("dasdklafjaksjfkla", "dasdklafjaksjfkla");
wacaiOpenApiClient.setGatewayEntryUrl("http://guard.test.wacai.info/api_entry");
wacaiOpenApiClient.setGatewayAuthUrl("http://guard.test.wacai.info/auth");
wacaiOpenApiClient.init();

WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
wacaiOpenApiRequest.putBizParam("card_id", "我是中文");
wacaiOpenApiRequest.putBizParam("apply_money", 10);

wacaiOpenApiClient
				.invoke(wacaiOpenApiRequest, new TypeReference<WacaiOpenApiResponse<OrderDeleteResponseObject>>() {
				}, new WacaiOpenApiResponseCallback<OrderDeleteResponseObject>() {
					@Override
					public void onSuccess(OrderDeleteResponseObject data) {
						log.error("success {}", data);
					}

					@Override
					public void onFailure(int code, String error) {
						log.error("failure {} {}", code, error);
					}
				});
```
