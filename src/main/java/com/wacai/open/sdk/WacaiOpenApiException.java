package com.wacai.open.sdk;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WacaiOpenApiException extends RuntimeException {

    private int code;

    public WacaiOpenApiException(String message) {
        super(message);
    }

    public WacaiOpenApiException(String message, Throwable t) {
        super(message, t);
    }
}
