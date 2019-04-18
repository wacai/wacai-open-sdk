package com.wacai.open.sdk;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

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
		wacaiOpenApiClient.setGatewayEntryUrl("http://guard.ngrok.wacaiyun.com/gw/api_entry");
		wacaiOpenApiClient.setGatewayAuthUrl("http://guard.ngrok.wacaiyun.com/token/auth");
		
		wacaiOpenApiClient.init();
	}

	@Test
	public void test0() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.dubbo.teacher.getbyid",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("id", "10");

		Teacher teacher = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Teacher.class);
		Assert.assertThat(teacher, is(notNullValue()));
		Assert.assertThat(teacher.id, is(equalTo(88)));
	}

	@Test
	public void test1() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.dubbo.teacher.getbyid",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("id", "0");
		try {
			wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<Teacher>() {
					});
			Assert.fail("should throw exception");
		} catch (WacaiOpenApiResponseException e) {
			Assert.assertThat(e.getCode(), is(equalTo(100000000)));
		}
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
	}

	@Data
	private static class Cash {
		private Integer a;

		private Integer b;

		private Map<String, Object> c;
	}

	@Test
	public void test4() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.error.code",
				"1.0.0");

		try {
			wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<String>() {
			});
		} catch (WacaiOpenApiResponseException e) {
			Assert.assertThat(e.getCode(), is(equalTo(1003230001)));
		}
	}

	@Test
	public void test5() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.error.code",
				"1.0.0");
		String codeParam = "12";
		wacaiOpenApiRequest.putBizParam("code", codeParam);

		String code = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, String.class);
		Assert.assertThat(code + "", is(equalTo(codeParam)));
	}

	@Test
	public void test6() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.response.mapping",
				"1.0.0");
		int expectedErrorCode = 123;
		wacaiOpenApiRequest.putBizParam("a", "false");
		wacaiOpenApiRequest.putBizParam("b", expectedErrorCode);
		try {
			wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Map.class);
			Assert.fail("should throw exception");
		} catch (WacaiOpenApiResponseException e) {
			Assert.assertThat(e.getCode(), is(equalTo(expectedErrorCode)));
		}
	}

	@Test
	public void test7() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.response.mapping",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("a", "true");
		wacaiOpenApiRequest.putBizParam("b", 0);

		Map m = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Map.class);
		Assert.assertThat(m.size(), is(equalTo(3)));
	}

	@Test
	public void test8() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.response.mapping",
				"1.0.0");
		int expectedErrorCode = 123;
		wacaiOpenApiRequest.putBizParam("a", "string");
		wacaiOpenApiRequest.putBizParam("b", expectedErrorCode);

		try {
			wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Map.class);
			Assert.fail("should throw exception");
		} catch (WacaiOpenApiResponseException e) {
			Assert.assertThat(e.getCode(), is(equalTo(expectedErrorCode)));
		}
	}

	@Test
	public void test9() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.response.mapping",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("a", "string");
		wacaiOpenApiRequest.putBizParam("b", 0);

		Map m = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Map.class);

		Assert.assertThat(m.size(), is(equalTo(3)));
	}

	@Test
	public void test10() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.dubbo.response.mapping",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("a", 10);

		Teacher teacher = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Teacher.class);

		Assert.assertThat(teacher, is(notNullValue()));
	}

	@Test
	public void test11() {
		WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("guard.test.biz.dubbo.response.mapping",
				"1.0.0");
		wacaiOpenApiRequest.putBizParam("a", 0);

		try {
			wacaiOpenApiClient.invoke(wacaiOpenApiRequest, Teacher.class);
			Assert.fail("should throw exception");
		} catch (WacaiOpenApiResponseException e) {
			Assert.assertThat(e.getCode(), is(equalTo(1103230001)));
		}
	}
}
