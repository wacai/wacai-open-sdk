package com.wacai.open.sdk.request;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WacaiOpenApiRequest {

  private final String apiName;

  private final String apiVersion;

  private Map<String, Object> bizParam = new HashMap<>();

  public void putBizParam(String paramName, Object paramValue) {
    bizParam.put(paramName, paramValue);
  }
}
