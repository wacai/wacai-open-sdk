package com.wacai.open.sdk.request;

import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.exception.WacaiOpenApiResponseException;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class WacaiOpenApiRequest {

	private final String apiName;

	private final String apiVersion;

	private Map<String, Object> bizParam = new HashMap<>();

	private byte[] byteBuffer;

	public void putBizParam(String paramName, Object paramValue) {
		bizParam.put(paramName, paramValue);
	}

	public void setByteBuffer(byte[] byteBuffer) {
		if (byteBuffer == null || byteBuffer.length == 0) {
			throw new WacaiOpenApiResponseException(ErrorCode.ERROR_PARAM);
		}
		this.byteBuffer = byteBuffer;
	}
}
