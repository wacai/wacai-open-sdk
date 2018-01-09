## 入口

### 协议
- 使用 HTTP(S) 协议作为目前的交互协议
- 测试环境下网关的统一入口是 `http://guard.test.wacai.info/gw/api_entry`
- 客户端统一使用 POST 方式向网关入口提交数据
- 客户端和开放网关的请求报文、响应报文格式都是 JSON，content_type 为 application/json
- 交互的编码格式统一为 UTF-8
- HTTP 正常响应的 http code 都是 200，非正常返回 400

### 请求报文

#### 请求路径
请求路径为 /api_entry，遵循 rest 规范，api_name 和 api_version 会呈现在 path 路径上，路径为 /api_entry/${api_name}/${api_version}

#### 请求头
请求头里边主要放一些通用参数，独立于每一次请求的业务参数，与业务参数分开，目前通用的参数有以下，分别介绍下
- x-wac-version 网关交互协议版本，类似 http 协议版本 1.0、1.1、2.0 的作用，目前是 4，必填
- x-wac-timestamp 客户端发起请求的时间戳，毫秒数，必填
- x-wac-access-token 客户端请求的 access_token ，必填
- x-wac-signature 请求的签名，必填，是通过签名算法对请求参数签名生成的，可以参考[这里](api_sign.md)

#### 请求体
请求体里边放对应 API 的请求参数，参数类型和名称，和具体的 API 有关，如下结构：
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

### 响应报文
- 正常响应(返回码 200)下响应体的结构和每个接口返回值有关系，API 网关不做处理
```json
  {  
	"appId": 1,
    "cardId": "12新闻34",
    "source": "outside"
  }
```
- 错误响应（返回码 400）下响应体的结构是统一定义的
```json
  {
    "code": 0,
    "error": ""
  }
```
- 分别解释下其中的每个字段
  - `code` 表示错误码。另外，错误码的规范还需要跟其他的团队沟通来制定出来，并且以后的 code 由开放网关来统一管理。
  - `error` 表示错误信息，当请求调用失败时，该字段可以用来展示错误原因。

### 不同环境的入口
- dev/test : http://guard.test.wacai.info/gw/api_entry
- staging : http://guard.staging.wacai.info/gw/api_entry
- prod : http://open.wacai.info/gw/api_entry