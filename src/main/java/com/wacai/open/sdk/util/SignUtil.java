package com.wacai.open.sdk.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class SignUtil {

    public static String generateSign(String plainText, String cipher) {
        Mac mac;
        try {
            mac = Mac.getInstance("hmacSha256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("hmacSha256", e);
        }
        byte[] secretBytes = cipher.getBytes(StandardCharsets.UTF_8);
        try {
            mac.init(new SecretKeySpec(secretBytes, "hmacSha256"));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("cipher : " + cipher, e);
        }
        byte[] signatureBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(signatureBytes);
    }

    public static String generateSign(Object object, String cipher) {
        return generateSign(generateStringRecursively((JSONObject) JSON.toJSON(object)), cipher);
    }

    private static String generateStringRecursively(JSONObject apiParam) {
        return generateStringRecursively(apiParam, 0);
    }

    private static String generateStringRecursively(JSONObject apiParam, int level) {
        List<String> keyList = new ArrayList<>();
        keyList.addAll(apiParam.keySet());
        Collections.sort(keyList);

        StringBuilder sb = new StringBuilder();
        for (String key : keyList) {
            if (level == 0 && "sign".equals(key)) {
                continue;
            }
            if (apiParam.get(key) == null) {
                continue;
            }

            if (apiParam.get(key) instanceof JSONObject) {
                JSONObject value = (JSONObject) apiParam.get(key);
                sb.append(generateStringRecursively(value, level + 1));
            } else {
                sb.append(apiParam.getString(key));
            }
        }
        return sb.toString();
    }
}
