package com.wacai.open.sdk.response;


import java.io.Serializable;

import lombok.Data;

@Data
public class AccessToken implements Serializable {

    private String accessToken;

    private String tokenType;

    private Integer expires;

    private String refreshToken;
}

