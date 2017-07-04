package com.wacai.open.sdk;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Stopwatch;
import com.wacai.open.sdk.request.WacaiOpenApiRequest;
import com.wacai.open.sdk.response.WacaiOpenApiResponse;
import com.wacai.open.sdk.response.WacaiOpenApiResponseCallback;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppTest {

	@Test
	public void test_invoke_sync() {
		WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("dasdklafjaksjfkla", "dasdklafjaksjfkla");
		wacaiOpenApiClient.setGatewayEntryUrl("http://guard.test.wacai.info/api_entry");
		wacaiOpenApiClient.setGatewayAuthUrl("http://guard.test.wacai.info/auth");
		wacaiOpenApiClient.init();

		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
		wacaiOpenApiRequest.putBizParam("card_id", "{'id':12}");
		wacaiOpenApiRequest.putBizParam("apply_money", 10);

		WacaiOpenApiResponse<OrderDeleteResponseObject> wacaiOpenApiResponse =
			wacaiOpenApiClient
				.invoke(wacaiOpenApiRequest, new TypeReference<WacaiOpenApiResponse<OrderDeleteResponseObject>>() {
				});

		Assert.assertNotNull(wacaiOpenApiResponse);
		Assert.assertTrue(wacaiOpenApiResponse.isSuccess());
	}

	@Data
	private static class OrderDeleteResponseObject {
		private String cardId;

		private int appId;

		private String cash;
	}

	@Test
	public void test_invoke_with_callback() throws InterruptedException {
		WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("dasdklafjaksjfkla", "dasdklafjaksjfkla");
		wacaiOpenApiClient.setGatewayEntryUrl("http://guard.test.wacai.info/api_entry");
		wacaiOpenApiClient.setGatewayAuthUrl("http://guard.test.wacai.info/auth");
		wacaiOpenApiClient.init();

		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.order.delete", "v2");
		wacaiOpenApiRequest.putBizParam("card_id", "我是中文");
		wacaiOpenApiRequest.putBizParam("apply_money", 10);

		for (int i = 0; i < 10; i++) {
			Stopwatch started = Stopwatch.createStarted();
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
			long elapsed = started.elapsed(TimeUnit.MILLISECONDS);
			log.error("response is async, elapsed {} ms", elapsed);
		}

		TimeUnit.SECONDS.sleep(1);
	}
}
