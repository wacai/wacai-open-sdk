package com.wacai.open.sdk.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  SYSTEM_ERROR(10000, "系统错误"),
  FILE_SYSTEM_ERROR(10014, "文件系统服务端非200错误"),
  FILE_SYSTEM_CLIENT_ERROR(10016, "文件系统客户端网络IO错误"),
  FILE_SYSTEM_BIZ_ERROR(10015, "文件系统业务错误"),
  ERROR_RET_TYPE(10001,"接口需要设置byte[]返回类型"),
  ERROR_PARAM(1002,"参数错误"),
  SIGN_NOT_MATCH(10008, "sign值不匹配"),
  INVALID_REFRESH_TOKEN(10010, "非法的refresh_token"),
  ACCESS_TOKEN_EXPIRED(10011, "access_token已过期"),
  INVALID_ACCESS_TOKEN(10012, "access_token非法"),
  REFRESH_TOKEN_EXPIRED(10013, "refresh_token已过期"),;

  private final int code;

  private final String description;
}
