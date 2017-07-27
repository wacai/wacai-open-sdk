package com.wacai.open.sdk.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wacai.open.sdk.WacaiOpenApiException;
import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.response.AccessToken;
import com.wacai.open.sdk.response.WacaiOpenApiResponse;
import com.wacai.open.sdk.util.SignUtil;

import java.io.IOException;
import java.util.Date;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@RequiredArgsConstructor
public class AccessTokenClient {

    private final String appKey;

    private final String appSecret;

    private OkHttpClient client = new OkHttpClient();

    private volatile AccessToken accessTokenCached;

    private Date accessTokenExpireDate;

    @Setter
    private String gatewayAuthUrl = "https://gw.wacai.com/auth";

    /**
     * 用来AccessToken强制缓存重新加载
     */
    @Setter
    private volatile boolean forceCacheInvalid = false;

    public String getCachedAccessToken() {
        if (accessTokenCached == null || forceCacheInvalid) {
            synchronized (this) {
                if (accessTokenCached == null) {
                    accessTokenCached = applyAccessToken();
                    forceCacheInvalid = false;
                } else if (accessTokenExpireDate.getTime() > System.currentTimeMillis() && !forceCacheInvalid) {
                    return accessTokenCached.getAccessToken();
                } else {
                    accessTokenCached = refreshAccessToken();
                    forceCacheInvalid = false;
                }
                accessTokenExpireDate = new Date(System.currentTimeMillis()
                                                 + accessTokenCached.getExpires() * 1000);
            }
        }
        return accessTokenCached.getAccessToken();
    }

    private AccessToken applyAccessToken() {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(appKey + "client_credentials" + timestamp, appSecret);

        RequestBody body = new FormBody.Builder()
            .add("app_key", appKey)
            .add("grant_type", "client_credentials")
            .add("timestamp", String.valueOf(timestamp))
            .add("sign", sign).build();

        Request request = new Request.Builder().url(gatewayAuthUrl + "/token").post(body).build();
        return sendRequest(request);
    }

    private AccessToken refreshAccessToken() {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(appKey + "refresh_token"
                                            + accessTokenCached.getRefreshToken() + timestamp, appSecret);

        RequestBody body = new FormBody.Builder()
            .add("app_key", appKey)
            .add("grant_type", "refresh_token")
            .add("timestamp", String.valueOf(timestamp))
            .add("refresh_token", accessTokenCached.getRefreshToken())
            .add("sign", sign).build();

        Request request = new Request.Builder().url(gatewayAuthUrl + "/refresh").post(body).build();
        try {
            return sendRequest(request);
        } catch (WacaiOpenApiException e) {
            if (e.getCode() == ErrorCode.INVALID_REFRESH_TOKEN.getCode()
                || e.getCode() == ErrorCode.REFRESH_TOKEN_EXPIRED.getCode()) {

                log.info("Refresh token {} is invalid or expired, apply a new instead",
                         accessTokenCached.getRefreshToken());
                return applyAccessToken();
            }
            throw e;
        }
    }

    private AccessToken sendRequest(Request request) {
        try (Response response = client.newCall(request).execute()) {

            ResponseBody responseBody = response.body();
            if (!response.isSuccessful() || responseBody == null) {
                throw new WacaiOpenApiException("http code " + response.code());
            }

            WacaiOpenApiResponse<AccessToken> openApiResponse =
                JSON.parseObject(responseBody.string(), new TypeReference<WacaiOpenApiResponse<AccessToken>>() {
                });

            if (!openApiResponse.isSuccess()) {
                WacaiOpenApiException openApiException = new WacaiOpenApiException(openApiResponse.getError());
                openApiException.setCode(openApiResponse.getCode());
                throw openApiException;
            }

            return openApiResponse.getData();
        } catch (IOException e) {
            throw new WacaiOpenApiException("failed to fetch access token", e);
        }
    }
}
