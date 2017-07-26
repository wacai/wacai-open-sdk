## 接受请求时Sign生成规则

### 参与签名计算的核心算法
```java
String signPlainText = method + "|" + bodyMd5 + "|" + headerString + "|" + paramString;
```
method是指当前请求方法的http method，目前支持GET、POST。

#### headerString
Headers 是指参与 Headers 签名计算的 Header 的 Key、Value 拼接的字符串，所有需要参与计算的header的名称都在x-wac-signature-headers这个header对应的value里边，以","分隔。

对需要参与计算的header按照headerName的字母表升序排列，对排序之后的header列表做字符串拼接，如下代码所示
```java
String headerString = header1Name + "=" + header1Value + "&" +
                      header2Name + "=" + header2Value + "&" +
                      header3Name + "=" + header3Value + "&" +
                      ...
                      headernName + "=" + headernValue;
```
**注意，headerName在做拼接时，必须转化全小写。对于value为集合的话，用","分隔符号连接所有的value作为value参与到计算中。**
#### bodyMd5
只有是POST并且非form表单（比如JSON）时才需要计算bodyMd5，不需要计算时，bodyMd5为空字符串，计算方法如下
```java
String bodyMd5 = Base64.encodeBase64String(DigestUtils.md5(bodyBytes));
```
#### paramString
所有Query和form表单的参数都一起要参与计算，对需要参与计算的参数按照参数名称的字母表升序排列，对排序之后的参数列表做字符串拼接，如下代码所示
```java
String paramString = param1Name + "=" + param1Value + "&"
                     param2Name + "=" + param2Value + "&"
                     param3Name + "=" + param3Value + "&"
                     ...
                     paramnName + "=" + paramnValue;
```
对于value为集合的话，用","分隔符号连接所有的value作为value参与到计算中。
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

### 生成的签名放入请求
网关生成的签名会以x-wac-signature为headername放入请求头中。
