package com.wacai.open.sdk.response;


import java.util.List;
import lombok.Data;

@Data
public class FileGatewayRes {

	public static final int SUCCESS_CODE = 0;
	public static final int ERROR_CODE = 1;
	public static final int RETRY_CODE = 8;

	private List<RemoteFile> data;

	private int code;

	private String error;

	@Override
	public String toString() {
		return "FileGatewayRes{" +
				"data=" + data +
				", code=" + code +
				", error='" + error + '\'' +
				'}';
	}
}
