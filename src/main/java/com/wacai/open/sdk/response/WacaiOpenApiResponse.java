package com.wacai.open.sdk.response;

import lombok.Data;

@Data
public class WacaiOpenApiResponse<T> {

  private static final int SUCCESS_CODE = 0;

  private int code;

  private String error;

  private T data;

  public boolean isSuccess() {
    return code == SUCCESS_CODE;
  }
}
