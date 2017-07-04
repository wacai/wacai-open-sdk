package com.wacai.open.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wacai.open.sdk.auth.AccessTokenClient;
import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.request.StandardRequest;
import com.wacai.open.sdk.request.WacaiOpenApiRequest;
import com.wacai.open.sdk.response.WacaiOpenApiResponse;
import com.wacai.open.sdk.response.WacaiOpenApiResponseCallback;
import com.wacai.open.sdk.util.SignUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class WacaiOpenApiClient {

    private static final MediaType JSON_MEDIA_TYPE
        = MediaType.parse("application/json; charset=utf-8");

    private final String appKey;

    private final String appSecret;

    @Setter
    private OkHttpClient client;

    private AccessTokenClient accessTokenClient;

    private AtomicBoolean initFlag = new AtomicBoolean(false);

    @Setter
    private String gatewayEntryUrl = "https://gw.wacai.com/api_entry";

    @Setter
    private String gatewayAuthUrl = "https://gw.wacai.com/auth";

    public WacaiOpenApiClient(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public void init() {

        if (!initFlag.compareAndSet(false, true)) {
            throw new IllegalStateException("init state");
        }

        if (gatewayEntryUrl == null || gatewayEntryUrl.trim().length() <= 0) {
            throw new IllegalArgumentException("invalid gatewayEntryUrl " + gatewayEntryUrl);
        }

        if (gatewayAuthUrl == null || gatewayAuthUrl.trim().length() <= 0) {
            throw new IllegalArgumentException("invalid gatewayAuthUrl " + gatewayAuthUrl);
        }
        this.accessTokenClient = new AccessTokenClient(appKey, appSecret);
        this.accessTokenClient.setGatewayAuthUrl(gatewayAuthUrl);
        if (client == null) {
            this.client = new OkHttpClient();
        }
    }

    public static WacaiOpenApiClient init(String appKey, String appSecret) {
        WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient(appKey, appSecret);
        wacaiOpenApiClient.init();
        return wacaiOpenApiClient;
    }

    public <T> WacaiOpenApiResponse<T> invoke(WacaiOpenApiRequest wacaiOpenApiRequest,
                                              TypeReference<WacaiOpenApiResponse<T>> typeReference) {

        Request request = assemblyRequest(wacaiOpenApiRequest);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                WacaiOpenApiResponse<T> wacaiOpenApiResponse = new WacaiOpenApiResponse<>();
                wacaiOpenApiResponse.setCode(ErrorCode.SYSTEM_ERROR.getCode());
                wacaiOpenApiResponse.setError(ErrorCode.SYSTEM_ERROR.getDescription());
                return wacaiOpenApiResponse;
            }

            String responseBodyString = response.body().string();
            WacaiOpenApiResponse<T> openApiResponse = JSON.parseObject(responseBodyString, typeReference);
            if (openApiResponse.getCode() == ErrorCode.ACCESS_TOKEN_EXPIRED.getCode()
                || openApiResponse.getCode() == ErrorCode.INVALID_ACCESS_TOKEN.getCode()) {

                log.info("Access token invalid or expired, apply new one instead.");
                accessTokenClient.setForceCacheInvalid(true);
                return invoke(wacaiOpenApiRequest, typeReference);
            }
            return openApiResponse;
        } catch (IOException e) {
            log.error("failed to execute {}", request, e);

            WacaiOpenApiResponse<T> wacaiOpenApiResponse = new WacaiOpenApiResponse<>();
            wacaiOpenApiResponse.setCode(ErrorCode.SYSTEM_ERROR.getCode());
            wacaiOpenApiResponse.setError(ErrorCode.SYSTEM_ERROR.getDescription());
            return wacaiOpenApiResponse;
        }
    }

    private RequestBody assemblyRequestBody(WacaiOpenApiRequest wacaiOpenApiRequest) {
        StandardRequest standardRequest = new StandardRequest();
        standardRequest.setVersion(Version.getCurrentVersion());
        standardRequest.setApiName(wacaiOpenApiRequest.getApiName());
        standardRequest.setApiVersion(wacaiOpenApiRequest.getApiVersion());
        standardRequest.setTimestamp(System.currentTimeMillis() / 1000);
        standardRequest.setAccessToken(accessTokenClient.getCachedAccessToken());
        standardRequest.setBizParams(wacaiOpenApiRequest.getBizParam());

        String sign = SignUtil.generateSign(standardRequest, appSecret);
        standardRequest.setSign(sign);

        String requestJson = JSON.toJSONString(standardRequest);
        return RequestBody.create(JSON_MEDIA_TYPE, requestJson);
    }



    public <T> void invoke(final WacaiOpenApiRequest wacaiOpenApiRequest,
                           final TypeReference<WacaiOpenApiResponse<T>> typeReference,
                           final WacaiOpenApiResponseCallback<T> callback) {
        Request request = assemblyRequest(wacaiOpenApiRequest);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(ErrorCode.SYSTEM_ERROR.getCode(),
                                   ErrorCode.SYSTEM_ERROR.getDescription());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(ErrorCode.SYSTEM_ERROR.getCode(),
                                       ErrorCode.SYSTEM_ERROR.getDescription());
                    return;
                }

                String responseBodyString = response.body().string();
                WacaiOpenApiResponse<T> openApiResponse = JSON.parseObject(responseBodyString, typeReference);
                if (!openApiResponse.isSuccess()) {
                    if (openApiResponse.getCode() == ErrorCode.ACCESS_TOKEN_EXPIRED.getCode()
                        || openApiResponse.getCode() == ErrorCode.INVALID_ACCESS_TOKEN.getCode()) {

                        log.info("Access token invalid or expired, apply new one instead.");
                        accessTokenClient.setForceCacheInvalid(true);
                        invoke(wacaiOpenApiRequest, typeReference, callback);
                    } else {
                        callback.onFailure(openApiResponse.getCode(), openApiResponse.getError());
                    }
                } else {
                    callback.onSuccess(openApiResponse.getData());
                }
            }
        });
    }

    private Request assemblyRequest(WacaiOpenApiRequest wacaiOpenApiRequest) {

        if (!initFlag.get()) {
            throw new IllegalStateException("Not initial client, please call init method before invoke");
        }

        RequestBody body = assemblyRequestBody(wacaiOpenApiRequest);

        return new Request.Builder().url(gatewayEntryUrl)
            .addHeader("Guard-Open-Request", "true").post(body).build();
    }
}
