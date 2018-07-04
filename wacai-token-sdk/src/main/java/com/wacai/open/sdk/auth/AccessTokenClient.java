package com.wacai.open.sdk.auth;

import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.exception.WacaiOpenApiResponseException;
import com.wacai.open.sdk.json.JsonProcessor;
import com.wacai.open.sdk.json.JsonTool;
import com.wacai.open.sdk.response.AccessToken;
import com.wacai.open.sdk.response.AccessTokenDto;
import com.wacai.open.sdk.response.WacaiErrorResponse;
import com.wacai.open.sdk.util.SignUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
@RequiredArgsConstructor
public class AccessTokenClient {

	private static ScheduledExecutorService checkThread = Executors
			.newSingleThreadScheduledExecutor();

	private static String cacheDir =
			System.getProperty("user.home", File.separator + "tmp") + File.separator + ".wacaiSdk"
					+ File.separator + "caches";

	private final String appKey;

	private final String appSecret;

	private OkHttpClient client = new OkHttpClient();

	private volatile AccessToken accessTokenCached;

	private Date accessTokenExpireDate;

	@Setter
	private String gatewayAuthUrl = "https://open.wacai.com/gw/auth";

	/**
	 * 用来AccessToken强制缓存重新加载
	 */
	@Setter
	private volatile boolean forceCacheInvalid = false;

	@Setter
	private JsonProcessor processor;

	private static ConcurrentMap<String, Runnable> scheduleTask = new ConcurrentHashMap<>();

	public synchronized void init() {
		FileUtils.mkdirsIfNecessary(cacheDir);
		String taskKey = appKey + "_" + appSecret;
		//类锁解决cas问题
		synchronized (AccessTokenClient.class) {
			Runnable oldTask = scheduleTask.get(taskKey);
			if (oldTask == null) {
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							String fileName = cacheDir + File.separator + Base64
									.encodeBase64URLSafeString(DigestUtils.md5(appKey + "_" + appSecret));
							boolean exists = FileUtils.fileExists(fileName);
							AccessTokenDto dto;
							if (exists && accessTokenCached == null) {//需要文件操作场景
								try {
									Object obj = FileUtils.objRead(fileName);
									if (null != obj) {
										AccessTokenDto tokenFile = (AccessTokenDto) obj;
										if (tokenFile.getAccessTokenExpireDate().getTime() > System.currentTimeMillis()
												&& !forceCacheInvalid) {//不刷新场景
											accessTokenCached = tokenFile.getToken();
											accessTokenExpireDate = tokenFile.getAccessTokenExpireDate();
											return;
										} else {//文件token失效
											throw new RuntimeException("token失效");
										}
									} else {//文件无有效信息
										throw new RuntimeException("token文件无有效信息");
									}
								} catch (Exception e) {//统一处理token无效情况
									log.error("token error:", e);
									dto = AccessTokenClient.this.cachedAccessToken();
								}
							} else {//文件不存在 || 文件存在且过期
								if (exists
										&& accessTokenExpireDate.getTime()
										< System.currentTimeMillis() + 400000) {//文件存在&&过期
									forceCacheInvalid = true;
								}
								dto = AccessTokenClient.this.cachedAccessToken();
							}
							FileUtils.objWrite(fileName, dto);
							log.info("schedule refresh token:{}", dto);
						} catch (Exception e) {
							log.error("get access token error:", e);
						}
					}
				};
				checkThread.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
				scheduleTask.put(taskKey, task);
			}
		}

		//初始化json处理类
		JsonTool.initJsonProcess(processor);
	}

	public String getCachedAccessToken() {
		return cachedAccessToken().getToken().getAccessToken();
	}

	private AccessTokenDto cachedAccessToken() {
		if (accessTokenCached == null || forceCacheInvalid) {
			synchronized (this) {
				if (accessTokenCached == null) {
					accessTokenCached = applyAccessToken();
					forceCacheInvalid = false;
				} else if (accessTokenExpireDate.getTime() > System.currentTimeMillis()
						&& !forceCacheInvalid) {
					return AccessTokenDto.build(accessTokenCached, accessTokenExpireDate, forceCacheInvalid);
				} else {
					accessTokenCached = refreshAccessToken();
					forceCacheInvalid = false;
				}
				accessTokenExpireDate = new Date(
						System.currentTimeMillis() + accessTokenCached.getExpires() * 1000);
			}
		}
		return AccessTokenDto.build(accessTokenCached, accessTokenExpireDate, forceCacheInvalid);
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
		String refreshToken = accessTokenCached.getRefreshToken();
		String sign = SignUtil.generateSign(appKey + "refresh_token" + refreshToken + timestamp,
				appSecret);

		RequestBody body = new FormBody.Builder()
				.add("app_key", appKey)
				.add("grant_type", "refresh_token")
				.add("timestamp", String.valueOf(timestamp))
				.add("refresh_token", refreshToken)
				.add("sign", sign).build();

		Request request = new Request.Builder().url(gatewayAuthUrl + "/refresh").post(body).build();
		try {
			return sendRequest(request);
		} catch (WacaiOpenApiResponseException e) {
			if (e.getCode() == ErrorCode.INVALID_REFRESH_TOKEN.getCode()
					|| e.getCode() == ErrorCode.REFRESH_TOKEN_EXPIRED.getCode()) {

				log.info("Refresh token {} is invalid or expired, apply a new instead", refreshToken);
				return applyAccessToken();
			}
			throw e;
		}
	}

	private AccessToken sendRequest(Request request) {
		try (Response response = client.newCall(request).execute()) {

			ResponseBody responseBody = response.body();
			if (responseBody == null) {
				throw new WacaiOpenApiResponseException(ErrorCode.CLIENT_SYSTEM_ERROR);
			}
			String responseBodyStr = responseBody.string();
			int code = response.code();
			log.info("token request res:{},code:{}", responseBodyStr, code);
			if (code == 400) {
				WacaiErrorResponse wacaiErrorResponse =
						JsonTool.deserialization(responseBodyStr, WacaiErrorResponse.class);
				throw new WacaiOpenApiResponseException(wacaiErrorResponse);
			} else if (code != 200) {
				throw new WacaiOpenApiResponseException(ErrorCode.CLIENT_SYSTEM_ERROR);
			}
			Map tokenMap = JsonTool.deserialization(responseBodyStr, Map.class);
			return tokenTransfer(tokenMap);
		} catch (IOException e) {
			throw new WacaiOpenApiResponseException(ErrorCode.CLIENT_SYSTEM_ERROR, e);
		}
	}

	private AccessToken tokenTransfer(Map mapParam) {
		AccessToken token = new AccessToken();
		token.setAccessToken(String.valueOf(mapParam.get("access_token")));
		token.setTokenType(String.valueOf(mapParam.get("token_type")));
		token.setExpires(Integer.valueOf(String.valueOf(mapParam.get("expires_in"))));
		token.setRefreshToken(String.valueOf(mapParam.get("refresh_token")));
		return token;
	}

	public static class FileUtils {

		static boolean fileExists(String file) {
			File f = new File(file);
			return f.exists();
		}

		/**
		 * create dir if not exist
		 */
		static boolean mkdirsIfNecessary(String dir) {
			File file = new File(dir);
			if (!file.exists() || !file.isDirectory()) {
				return file.mkdirs();
			}
			return true;
		}

		static void objWrite(String file, Object obj) throws IOException {
			File outFile = new File(file);
			FileOutputStream fos = new FileOutputStream(outFile, false);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			fos.close();
		}

		static Object objRead(String file) throws IOException, ClassNotFoundException {
			File f = new File(file);
			if (!f.exists() || !f.isFile()) {
				return null;
			}
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			return ois.readObject();
		}
	}
}
