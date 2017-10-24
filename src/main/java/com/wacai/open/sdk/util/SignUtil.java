package com.wacai.open.sdk.util;

import static com.wacai.open.sdk.request.WacaiOpenApiHeader.X_WAC_SIGNATURE;
import static com.wacai.open.sdk.request.WacaiOpenApiHeader.X_WAC_SIGNATURE_HEADERS;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public final class SignUtil {

  public static String generateSign(String plainText, String cipher) {
    Mac mac;
    String algorithm = "hmacSha256";
    try {
      mac = Mac.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(algorithm, e);
    }
    byte[] secretBytes = cipher.getBytes(StandardCharsets.UTF_8);
    try {
      mac.init(new SecretKeySpec(secretBytes, algorithm));
    } catch (InvalidKeyException e) {
      throw new RuntimeException("cipher : " + cipher, e);
    }
    byte[] signatureBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeBase64URLSafeString(signatureBytes);
  }

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
    String headerString = headers.entrySet().stream().filter(entry -> keys.contains(entry.getKey()))
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> {
          String value = entry.getValue();
          if (value == null) {
            value = "";
          }
          return entry.getKey().toLowerCase() + "=" + value;
        })
        .collect(joining("&"));

    String paramString = params.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> {
          String value = entry.getValue();
          if (value == null) {
            value = "";
          }
          return entry.getKey() + "=" + value;
        })
        .collect(joining("&"));

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
      headers.put(headerName, headerValue);
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
        value = Arrays.stream(values).collect(joining(","));
      }
      params.put(entry.getKey(), value);
    }

    return params;
  }
}
