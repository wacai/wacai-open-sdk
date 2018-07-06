package com.wacai.open.sdk;

import static org.hamcrest.CoreMatchers.equalTo;

import com.alibaba.fastjson.JSON;
import com.wacai.open.sdk.exception.WacaiOpenApiResponseException;
import com.wacai.open.sdk.json.TypeReference;
import com.wacai.open.sdk.request.WacaiOpenApiRequest;
import java.util.Map;
import lombok.Data;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest {

	private static WacaiOpenApiClient wacaiOpenApiClient;

	@BeforeClass
	public static void init() {
		wacaiOpenApiClient = new WacaiOpenApiClient("5nteennva5ah",
				"bec93f8ffe88da37");
		wacaiOpenApiClient.setGatewayEntryUrl("http://localhost:8060/gw/api_entry");
		wacaiOpenApiClient.setGatewayAuthUrl("http://open.wacaiyun.com/gw/auth");

		wacaiOpenApiClient.init();
	}

	@Test(expected = WacaiOpenApiResponseException.class)
	public void test() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.dubbo.teacher.getbyid",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("id", "12");

		Teacher teacher = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<Teacher>() {
		});

		Assert.assertNotNull(teacher);
	}

	@Data
	private static class Teacher {

		private String name;

		private int id;
	}

	@Test
	public void test2() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.api.transparent",
				"1.0.0");
		wacaiOpenApiRequest.setByteBuffer("{\"a\": \"1\"}".getBytes());

		byte[] teacher = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<byte[]>() {
		});

		System.out.println(JSON.parse(teacher));
		Assert.assertNotNull(teacher);
	}

	@Test
	public void test3() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.system",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("a", 12);

		Cash cash = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<Cash>() {
		});

		Assert.assertNotNull(cash);
		Assert.assertThat(cash.a, equalTo(12));
		Assert.assertThat(cash.b, equalTo(1234));
	}

	@Data
	private static class Cash {
		private Integer a;

		private Integer b;

		private Map<String, Object> c;
	}
}
