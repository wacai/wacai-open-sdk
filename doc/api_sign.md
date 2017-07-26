## 请求Sign生成

### 参与签名计算的核心算法
```java
String signPlainText = apiName + apiVersion + headerString + bodyMd5;
```
apiName和apiVersion是指调用的接口名称和版本，这个不难理解。

#### headerString
Headers 是指参与 Headers 签名计算的 Header 的 Key、Value 拼接的字符串，目前只有header中的x-wac-timestamp、x-wac-access-token、x-wac-version参与计算。
对需要参与计算的header按照headerName的字母升序排列之后，连接起来，如下代码所示
```java
String headerString = header1Name + "=" + header1Value + 
                      header2Name + "=" + header2Value +
                      header3Name + "=" + header3Value +
                      ...
                      headernName + "=" + headernValue;
```
#### bodyMd5
因为请求的数据是在body里边，所以我们会直接计算body的md5作为对body数据的签名，计算方法如下
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
        return Base64.encodeBase64String(signatureBytes);
    }
```
appSecret即最初分发的秘钥。

具体实现细节可参考[sdk](api_sdk.md)中的实现。

