package com.wacai.open.sdk.util;

import static com.wacai.open.sdk.request.WacaiOpenApiHeader.X_WAC_SIGNATURE;
import static com.wacai.open.sdk.request.WacaiOpenApiHeader.X_WAC_SIGNATURE_HEADERS;
import static com.wacai.open.sdk.util.SignUtil.generateSign;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestSignUtil {

	private static final Logger log = LoggerFactory.getLogger(RequestSignUtil.class);

	/**
	 * 用于校验请求进来的参数是否和sign匹配
	 *
	 * @param method 请求的方法名称，GET、POST
	 * @param bodyMd5 body的md5，调用方自行计算，计算方式见文档
	 * @param headers 所有的请求header
	 * @param params 所有请求参数，form表单和query的所有参数
	 * @param appSecret 用户的appSecret
	 * @return {@code true} 如果匹配
	 */
	public static boolean checkInboundRequestSign(String method, String bodyMd5,
			Map<String, String> headers,
			Map<String, String> params, String appSecret) {
		char delimit = '|';

		List<String> keys = Arrays.asList(headers.get(X_WAC_SIGNATURE_HEADERS).split(","));
		String headerString = generateHeadersPlainText(headers, keys);

		String paramString = generateParamsPlainText(params);

		String signPlainText =
				method + delimit + bodyMd5 + delimit + headerString + delimit + paramString;
		String signature = generateSign(signPlainText, appSecret);

		String expectedSign = headers.get(X_WAC_SIGNATURE);
		boolean equals = Objects.equals(signature, expectedSign);
		if (!equals) {
			log.warn("sign not match, expected sign is {}, generated sign is {}, sign plaintext is {}",
					expectedSign, signature, signPlainText);
		}
		return equals;
	}

	private static String generateHeadersPlainText(Map<String, String> headerMap, List<String> keys) {
		Map<String, String> headersForSign = new TreeMap<>();
		for (Entry<String, String> entry : headerMap.entrySet()) {
			if (keys.contains(entry.getKey())) {
				headersForSign.put(entry.getKey(), entry.getValue());
			}
		}

		if (headersForSign.isEmpty()) {
			return "";
		}

		StringBuilder headerStringBuilder = new StringBuilder();
		for (Entry<String, String> entry : headersForSign.entrySet()) {
			headerStringBuilder.append('&').append(entry.getKey().toLowerCase())
					.append("=").append(entry.getValue() == null? "" : entry.getValue());
		}

		return headerStringBuilder.substring(1);
	}

	private static String generateParamsPlainText(Map<String, String> paramsMap) {
		if (paramsMap == null || paramsMap.isEmpty()) {
			return "";
		}

		Map<String, String> headersForSign = new TreeMap<>(paramsMap);

		StringBuilder headerStringBuilder = new StringBuilder();
		for (Entry<String, String> entry : headersForSign.entrySet()) {
			headerStringBuilder.append('&').append(entry.getKey().toLowerCase())
					.append("=").append(entry.getValue() == null? "" : entry.getValue());
		}

		return headerStringBuilder.substring(1);
	}

	public static boolean checkInboundRequestSign(HttpServletRequest httpServletRequest,
			String appSecret) throws IOException {
		String method = httpServletRequest.getMethod().trim().toUpperCase();

		String bodyMd5 = "";
		if ("POST".equals(method)
				&& !httpServletRequest.getHeader("content-type")
				.contains("application/x-www-form-urlencoded")
				&& !httpServletRequest.getHeader("content-type").contains("multipart/form-data")) {
			// POST 并且非 form 表单
			ServletInputStream inputStream = httpServletRequest.getInputStream();
			bodyMd5 = Base64.encodeBase64String(DigestUtils.md5(inputStream));
		}

		Map<String, String> headers = resolveHeaders(httpServletRequest);

		Map<String, String> params = resolveParams(httpServletRequest);

		return checkInboundRequestSign(method, bodyMd5, headers, params, appSecret);
	}

	private static Map<String, String> resolveHeaders(HttpServletRequest httpServletRequest) {
		Map<String, String> headers = new HashMap<>();

		Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = httpServletRequest.getHeader(headerName);
			headers.put(headerName.trim().toLowerCase(), headerValue);
		}
		return headers;
	}

	private static Map<String, String> resolveParams(HttpServletRequest httpServletRequest) {
		Map<String, String> params = new HashMap<>();

		Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();

		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			String[] values = entry.getValue();

			String value = "";
			if (values.length == 1) {
				value = values[0];
			} else if (values.length > 1) {
				StringBuilder valueStringBuilder = new StringBuilder();
				for (String v : values) {
					valueStringBuilder.append(",").append(v);
				}
				value = valueStringBuilder.substring(1);
			}
			params.put(entry.getKey(), value);
		}

		return params;
	}
}
