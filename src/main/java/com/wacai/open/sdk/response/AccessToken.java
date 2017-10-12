package com.wacai.open.sdk.response;


import lombok.Data;

@Data
public class AccessToken {

    private String accessToken;

    private String tokenType;

    private Integer expires;

    private String refreshToken;
}

