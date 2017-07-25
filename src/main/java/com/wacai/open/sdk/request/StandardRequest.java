package com.wacai.open.sdk.request;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

import lombok.Data;

@Data
public class StandardRequest {

    @JSONField(name = "biz_params")
    private Map<String, Object> bizParams;

    public static final String X_WAC_VERSION = "x-wac-version";

    public static final String X_WAC_TIMESTAMP = "x-wac-timestamp";

    public static final String X_WAC_SIGNATURE = "x-wac-signature";

    public static final String X_WAC_ACCESS_TOKEN = "x-wac-access-token";
}
