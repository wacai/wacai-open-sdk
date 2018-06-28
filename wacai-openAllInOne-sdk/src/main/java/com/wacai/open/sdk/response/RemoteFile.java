package com.wacai.open.sdk.response;

import java.io.Serializable;
import lombok.Data;

@Data
public class RemoteFile implements Serializable {

	private String filename;

	private String originalName;

	private String namespace;

	private String secretKey;

	@Override
	public String toString() {
		return "RemoteFile{" +
				"filename='" + filename + '\'' +
				", namespace='" + namespace + '\'' +
				", secretKey='" + secretKey + '\'' +
				'}';
	}
}
