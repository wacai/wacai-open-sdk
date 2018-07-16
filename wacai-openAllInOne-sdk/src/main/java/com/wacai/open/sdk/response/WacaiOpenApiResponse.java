package com.wacai.open.sdk.response;

import lombok.Data;

/**
 * 不建议使用，已废弃，建议业务自行提供
 */
@Deprecated
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