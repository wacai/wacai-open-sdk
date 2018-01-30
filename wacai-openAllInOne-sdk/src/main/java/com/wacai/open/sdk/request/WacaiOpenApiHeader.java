package com.wacai.open.sdk.request;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WacaiOpenApiHeader {

  public static final String X_WAC_VERSION = "x-wac-version";

  public static final String X_WAC_TIMESTAMP = "x-wac-timestamp";

  public static final String X_WAC_SIGNATURE = "x-wac-signature";

  public static final String X_WAC_ACCESS_TOKEN = "x-wac-access-token";

  public static final String X_WAC_SIGNATURE_HEADERS = "x-wac-signature-headers";

  public static final String X_WAC_SDK_VERSION = "x-wac-sdk-version";

  public static final String X_WAC_DECODE = "x_wac_decode_flag";

  public static final String X_WAC_TRACE_ID = "custom";
}
