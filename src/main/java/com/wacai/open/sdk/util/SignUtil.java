package com.wacai.open.sdk.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    public static String generateSign(Object object, String cipher) {
        if (object == null) {
            throw new IllegalArgumentException("object can't be null");
        }
        JSONObject jsonObject = object instanceof JSONObject ? (JSONObject) object
                                                             : (JSONObject) JSON.toJSON(object);
        return generateSign(generateStringRecursively(jsonObject), cipher);
    }

    static String generateStringRecursively(JSONObject apiParam) {
        return generateStringRecursively(apiParam, 0);
    }

    private static String generateStringRecursively(JSONObject apiParam, int level) {
        Map<String, Object> sortedMap = new TreeMap<>(apiParam);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
            if (level == 0 && "sign".equals(entry.getKey())) {
                continue;
            }
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                continue;
            }

            if (entryValue instanceof JSONObject) {
                JSONObject value = (JSONObject) entryValue;
                sb.append(generateStringRecursively(value, level + 1));
            } else {
                sb.append(entryValue);
            }
        }
        return sb.toString();
    }
}
