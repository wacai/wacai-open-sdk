package com.wacai.open.sdk.response;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class AccessTokenDto implements Serializable {

	private AccessToken token;

	private Date accessTokenExpireDate;

	private boolean forceCacheInvalid;

	public static AccessTokenDto build(AccessToken token, Date expireDate, boolean invalid) {
		AccessTokenDto dto = new AccessTokenDto();
		dto.setForceCacheInvalid(invalid);
		dto.setAccessTokenExpireDate(expireDate);
		dto.setToken(token);
		return dto;
	}
}
