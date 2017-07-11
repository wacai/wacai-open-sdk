package com.wacai.open.sdk.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SignUtilTest {

    @Test
    public void test_generateStringRecursively() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("_v", 4);
        map.put("api_name", "wacai.order.delete");
        map.put("api_version", "1.0.0");
        map.put("sign", "level-01-sign");
        map.put("NullKey", null);

        Map<String, Object> bizParams = Maps.newHashMap();
        map.put("biz_params", bizParams);

        bizParams.put("a", 1);
        bizParams.put("b", 2);
        bizParams.put("NullKey", null);

        Map<String, Object> param = Maps.newHashMap();
        bizParams.put("c", param);

        param.put("g", "g");
        param.put("f", "f");
        param.put("sign", "level-03-sign");

        JSONObject jsonObject = (JSONObject) JSON.toJSON(map);
        String signPlainText = SignUtil.generateStringRecursively(jsonObject);
        String signExpected = "4wacai.order.delete1.0.012fglevel-03-sign";
        Assert.assertEquals(signExpected, signPlainText);
    }
}
