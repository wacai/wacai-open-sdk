package com.wacai.open.sdk.request;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

import lombok.Data;

@Data
public class StandardRequest {

    @JSONField(name = "_v")
    private Integer version;

    @JSONField(name = "api_name")
    private String apiName;

    @JSONField(name = "api_version")
    private String apiVersion;

    @JSONField(name = "biz_params")
    private Map<String, Object> bizParams;

    @JSONField(name = "ts")
    private Long timestamp;

    @JSONField(name = "access_token")
    private String accessToken;

    private String sign;
}
