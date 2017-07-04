package com.wacai.open.sdk.request;

import lombok.Data;

@Data
public class NotifyRequest<T> {

    private String sign;

    private Long timestamp;

    private T data;
}
