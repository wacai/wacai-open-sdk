## token请求Sign生成

### accesstoken的plainText
```java
String signPlainText =appKey + "client_credentials" + timestamp;
```
appKey和timestamp分别是应用key和当前时间戳

### refreshtoken的plainText
```java
String signPlainText =appKey + "refresh_token" + refreshToken + timestamp;
```
appKey和refreshToken和timestamp分别是应用key和刷新token及当前时间戳

### 生成签名的算法
```java
    public static String generateSign(String plainText, String appSecret) {
        Mac mac;
        String algorithm = "hmacSha256";
        try {
            mac = Mac.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm, e);
        }
        try {
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), algorithm));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("invalid key appSecret : " + appSecret, e);
        }
        byte[] signatureBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64URLSafeString(signatureBytes);
    }
```
appSecret即最初分发的秘钥。

具体实现细节可参考当前sdk中的实现，建议直接使用当前sdk来和API网关通信。

