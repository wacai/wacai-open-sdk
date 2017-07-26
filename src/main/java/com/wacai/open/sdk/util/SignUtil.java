package com.wacai.open.sdk.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.wacai.open.sdk.request.StandardRequest.X_WAC_SIGNATURE;
import static com.wacai.open.sdk.request.StandardRequest.X_WAC_SIGNATURE_HEADERS;

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
        return Base64.encodeBase64String(signatureBytes);
    }

    /**
     * 用于校验请求进来的参数是否和sign匹配
     *
     * @param method 请求的方法名称，GET、POST
     * @param bodyMd5 body的md5，调用方自行计算，计算方式建文档
     * @param headers 所有的请求header
     * @param params 所有请求参数，form表单和query的所有参数
     * @param appSecret 用户的appSecret
     * @return {@code true} 如果匹配
     */
    public static boolean checkInboundRequestSign(String method, String bodyMd5, Map<String, String> headers,
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
            }).reduce("", (s1, s2) -> s1 + "&" + s2);

        String paramString = params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String value = entry.getValue();
                if (value == null) {
                    value = "";
                }
                return entry.getKey() + "=" + value;
            }).reduce("", (s1, s2) -> s1 + "&" + s2);

        String signPlainText = method + delimit + bodyMd5 + delimit + headerString + delimit + paramString;
        String signature = generateSign(signPlainText, appSecret);

        return Objects.equals(signature, headers.get(X_WAC_SIGNATURE));
    }
}
