## 入口

### 协议
- 使用 HTTPS 协议作为目前的交互协议
- 环境下网关的统一入口是 `https://open.wacai.com/gw/api_entry`
- 客户端统一使用 POST 方式向网关入口提交数据
- 非透传请求客户端和开放网关的请求报文、响应报文格式都是 JSON，content_type为application/json
- 交互的编码格式统一为 UTF-8
- HTTP 正常响应的 http code 都是200，非正常返回 400

### 透传协议
- 定义: HTTP 请求报文的 body 及请求返回的结果，开放网关不做任何解析处理，客户端与后端服务自行解析。
- 使用场景: 小文件上传(目前服务端限制 4M)、 自定义序列化协议。
- 使用方法及注意事项:  透传请求 body 通过 byteBuffer 设置传递信息, bizParam 设置参数将被忽略, byte[] 接收返回结果,通过异常判断是否调用成功。
- java 代码样例:
```java
WacaiOpenApiClient wacaiOpenApiClient = new WacaiOpenApiClient("${appKey}", "${appSecret}");
wacaiOpenApiClient.init();
WacaiOpenApiRequest wacaiOpenApiRequest = new WacaiOpenApiRequest("wacai.file", "1.0.0");
byte[] bytesIn = new byte[];
wacaiOpenApiRequest.setByteBuffer(bytesIn);
try {
byte[] response  = wacaiOpenApiClient.invoke(wacaiOpenApiRequest, new TypeReference<byte[]>() {});
     } catch (Exception e) {
    log.error(e)
    }
```
### 请求报文

#### 请求路径
请求路径为 /api_entry，遵循 rest 规范，api_name 和 api_version 会呈现在 path 路径上，路径为 /api_entry/${api_name}/${api_version}

#### 请求头
请求头里边主要放一些通用参数，独立于每一次请求的业务参数，与业务参数分开，目前通用的参数有以下，分别介绍下
- x-wac-version 网关交互协议版本，类似 HTTP 协议版本 1.0、1.1、2.0 的作用，目前是 4，必填
- x-wac-timestamp 客户端发起请求的时间戳，毫秒数，必填
- **x-wac-access-token 客户端请求的 access_token **，如果不使用 token，而是采用直连 API 网关模式，需要传输 x-wac-app-key，对应的 value 就是 appKey
- x-wac-signature 请求的签名，必填，是通过签名算法对请求参数签名生成的，可以参考[这里](api_sign.md)

#### 请求体
请求体里边放对应 api 的请求参数，参数类型和名称，和具体的 api 有关。对应的业务参数都放在一个大的框架里，如下结构：
```json
  {
         "param_00": "someValue",
         "param_01": 
         {
             "a": 1,
             "b": 2,
             "c": 
             { 
                "d": 2017 
             }
          }
  }
```
其中，存放的是业务参数，是和 api 直接相关的。

### 响应报文
- 正常响应看下响应体的案例：
```json
  {
      "appId": 1,
      "cardId": "12新闻34",
      "source": "outside"
  }
```
- 非正常响应的结构
```json
  {
    "code": xxx,
    "error": "xxxxxx"
  }
```
- 分别解释下其中的每个字段
  - `code` 表示错误码。另外，错误码的规范还需要跟其他的团队沟通来制定出来，并且以后的 code 由开放网关来统一管理。
  - `error` 表示错误信息，当请求调用失败时，该字段可以用来展示错误原因。
