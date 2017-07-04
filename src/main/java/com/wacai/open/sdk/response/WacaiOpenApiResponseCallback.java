package com.wacai.open.sdk.response;

public interface WacaiOpenApiResponseCallback<T> {

    void onSuccess(T data);

    void onFailure(int code, String error);
}
