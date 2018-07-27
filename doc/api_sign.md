## 请求 Sign 生成

### 参与签名计算的核心算法
```java
String signPlainText = apiName + "|" + apiVersion + "|" + headerString + "|" + bodyMd5;
```
apiName 和 apiVersion 是指调用的接口名称和版本，这个不难理解。

#### headerString
Headers 是指参与 Headers 签名计算的 Header 的 Key、Value 拼接的字符串，目前只有 header 中的 x-wac-timestamp、x-wac-access-token(直连模式使用 x-wac-app-key)、x-wac-version 参与计算。

对需要参与计算的 header 按照 headerName 的字母表升序排列，对排序之后的 header 列表做字符串拼接，如下代码所示
```java
String headerString = header1Name + "=" + header1Value + "&" +
                      header2Name + "=" + header2Value + "&" +
                      header3Name + "=" + header3Value + "&" +
                      ...
                      headernName + "=" + headernValue;
```
#### bodyMd5
因为请求的数据是在 body 里边，所以我们会直接计算 body 的 md5 作为对 body 数据的签名，计算方法如下
```java
String bodyMd5 = Base64.encodeBase64String(DigestUtils.md5(bodyBytes));
```
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
appSecret 即最初分发的秘钥。

具体实现细节可参考当前 sdk 中的[实现](
https://github.com/wacai/wacai-open-sdk/blob/34fe4e752d01255ec8adddeefe7a511d8f4c4c5f/wacai-openAllInOne-sdk/src/main/java/com/wacai/open/sdk/WacaiOpenApiClient.java#L341)，建议直接使用当前 sdk 来和 API 网关通信。

