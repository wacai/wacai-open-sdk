package com.wacai.open.sdk.auth;

import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.exception.WacaiOpenApiResponseException;
import com.wacai.open.sdk.json.JsonTool;
import com.wacai.open.sdk.response.AccessToken;
import com.wacai.open.sdk.response.AccessTokenDto;
import com.wacai.open.sdk.response.WacaiErrorResponse;
import com.wacai.open.sdk.util.SignUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

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

@Slf4j
@RequiredArgsConstructor
public class AccessTokenClient {

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

    private static ScheduledExecutorService checkThread = Executors.newSingleThreadScheduledExecutor();

    private static String cacheDir = System.getProperty("user.home", File.separator + "tmp") + File.separator + ".wacaiSdk" + File.separator + "caches";

    public synchronized void init(){
        Runnable task = () -> {
            try {

                String fileName = cacheDir+File.separator+Base64.encodeBase64URLSafeString(DigestUtils.md5(appKey+"_"+appSecret));
                boolean exists = FileUtils.fileExists(fileName);
                AccessTokenDto dto;
                if (exists && accessTokenCached == null) {//需要文件操作场景
                    try {
                        Object obj = FileUtils.objRead(fileName);
                        if (null != obj) {
                            AccessTokenDto tokenFile = (AccessTokenDto) obj;
                            if (tokenFile.getAccessTokenExpireDate().getTime() > System.currentTimeMillis() && !forceCacheInvalid) {//不刷新场景
                                accessTokenCached = tokenFile.getToken();
                                accessTokenExpireDate = tokenFile.getAccessTokenExpireDate();
                                return;
                            }else {//文件token失效
                                throw new RuntimeException("token失效");
                            }
                        }else {//文件无有效信息
                            throw new RuntimeException("token文件无有效信息");
                        }
                    } catch (Exception e) {//统一处理token无效情况
                        log.error("token error:",e);
                        dto = cachedAccessToken();
                    }
                }else {
                    if (accessTokenExpireDate.getTime() < System.currentTimeMillis()) {
                        forceCacheInvalid = true;
                    }
                    dto = cachedAccessToken();
                }
                FileUtils.objWrite(fileName,dto);
                log.info("schedule refresh token:{}",dto);
            } catch (Exception e) {
                log.error("get access token error:",e);
            }
        };
        checkThread.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
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
                }
                else if (accessTokenExpireDate.getTime() > System.currentTimeMillis() && !forceCacheInvalid) {
                    return AccessTokenDto.build(accessTokenCached,accessTokenExpireDate,forceCacheInvalid);
                }
                else {
                    accessTokenCached = refreshAccessToken();
                    forceCacheInvalid = false;
                }
                accessTokenExpireDate = new Date(System.currentTimeMillis() + accessTokenCached.getExpires() * 1000);
            }
        }
        return AccessTokenDto.build(accessTokenCached,accessTokenExpireDate,forceCacheInvalid);
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
        long timestamp = System.currentTimeMillis() ;
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
        } catch (WacaiOpenApiResponseException e) {
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
            if (responseBody == null) {
                throw new WacaiOpenApiResponseException(ErrorCode.SYSTEM_ERROR);
            }
            if (response.code() == 400) {
                WacaiErrorResponse wacaiErrorResponse =
                    JsonTool.deserialization(responseBody.string(), WacaiErrorResponse.class);
                throw new WacaiOpenApiResponseException(wacaiErrorResponse);
            } else if (response.code() != 200) {
                throw new WacaiOpenApiResponseException(ErrorCode.SYSTEM_ERROR);
            }
            Map tokenMap = JsonTool.deserialization(responseBody.string(), Map.class);
            return tokenTransfer(tokenMap);
        } catch (IOException e) {
            throw new WacaiOpenApiResponseException(ErrorCode.SYSTEM_ERROR, e);
        }
    }
    private AccessToken tokenTransfer(Map mapParam){
        AccessToken token = new AccessToken();
        token.setAccessToken(String.valueOf(mapParam.get("access_token")));
        token.setTokenType(String.valueOf(mapParam.get("token_type")));
        token.setExpires(Integer.valueOf(String.valueOf( mapParam.get("expires_in"))));
        token.setRefreshToken(String.valueOf(mapParam.get("refresh_token")));
        return token;
    }

    public static class FileUtils {

        public static boolean fileExists(String file) {
            File f = new File(file);
            return f.exists();
        }

        /**
         * create dir if not exist
         */
        public static boolean mkdirsIfNecessary(String dir) {
            File file = new File(dir);
            if (!file.exists() || !file.isDirectory()) {
                return file.mkdirs();
            }
            return true;
        }

        /**
         * clear all files in current dir
         */
        public static void clearDir(String dir) {
            manageDir(new File(dir), false);
        }

        /**
         * remove dir if exist(in recursion, like 'rm -r')
         */
        public static void rmdir(String dir) {
            manageDir(new File(dir), true);
        }

        public static void objWrite(String file,Object obj) throws IOException {
            File outFile = new File(file);
            FileOutputStream fos = new FileOutputStream(outFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            fos.close();
        }

        public static Object objRead(String file) throws IOException, ClassNotFoundException {
            File f = new File(file);
            if (!f.exists() || !f.isFile()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();

        }
        public static void writeFile(String file, String content) throws IOException {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        }

        public static byte[] readFile(String file) throws IOException {
            File f = new File(file);
            if (!f.exists() || !f.isFile()) {
                return new byte[0];
            }
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[16];
            int readBytes = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
            while (-1 != (readBytes = fis.read(buf, 0, buf.length))) {
                baos.write(buf, 0, readBytes);
            }
            fis.close();
            return baos.toByteArray();
        }

        public static void rmFile(String file) {
            rmFile(new File(file));
        }

        private static void manageDir(File file, boolean rm) {
            if (file.exists() && file.isDirectory()) {
                File[] children = file.listFiles();
                for (File child : children) {
                    if (child.isFile()) {
                        rmFile(child);
                    } else {
                        manageDir(child, true);
                    }
                }
                if (rm) {
                    file.delete();
                }
            }
        }

        private static void rmFile(File file) {
            if (null != file && file.exists()) {
                file.delete();
            }
        }
    }
}
