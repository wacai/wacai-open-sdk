package com.wacai.open.sdk.response;

import com.wacai.open.sdk.exception.WacaiOpenApiResponseException;

public interface WacaiOpenApiResponseCallback<T> {

  void onSuccess(T data);

  void onFailure(WacaiOpenApiResponseException ex);
}
