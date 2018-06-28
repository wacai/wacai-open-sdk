package com.wacai.open.sdk.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/**
	 * SDK 中定义的 error code 往前排，不能和服务端冲突
	 */
	FILE_SYSTEM_ERROR(9993, "文件系统服务端非200错误"),
	FILE_SYSTEM_CLIENT_ERROR(9994, "文件系统客户端网络IO错误"),
	FILE_SYSTEM_BIZ_ERROR(9995, "文件系统业务错误"),
	ERROR_RET_TYPE(9996, "接口需要设置byte[]返回类型"),
	ERROR_PARAM(9997, "参数错误"),
	REFRESH_TOKEN_FAILURE_MAX_NUM(9998, "换取令牌失败超过最大次数"),
	CLIENT_SYSTEM_ERROR(9999, "系统错误"),
	SYSTEM_ERROR(10000, "系统错误"),
	SIGN_NOT_MATCH(10008, "sign值不匹配"),
	INVALID_REFRESH_TOKEN(10010, "非法的refresh_token"),
	ACCESS_TOKEN_EXPIRED(10011, "access_token已过期"),
	INVALID_ACCESS_TOKEN(10012, "access_token非法"),
	REFRESH_TOKEN_EXPIRED(10013, "refresh_token已过期"),;

	private final int code;

	private final String description;
}
