package com.wacai.open.sdk.response;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

@Data
public class AccessToken {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "token_type")
    private String tokenType;

    @JSONField(name = "expires_in")
    private Integer expires;

    @JSONField(name = "refresh_token")
    private String refreshToken;
}

