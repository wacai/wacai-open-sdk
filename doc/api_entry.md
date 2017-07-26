## 入口

### 协议
- 使用HTTP(S)协议作为目前的交互协议
- 测试环境下网关的统一入口是 `http://guard.test.wacai.info/api_entry`
- 客户端统一使用POST方式向网关入口提交数据
- 客户端和开放网关的请求报文、响应报文格式都是JSON，content_type为application/json
- 交互的编码格式统一为UTF-8
- HTTP正常响应的http code都是200，除非网关宕机等等情况会返回404或者500

### 请求报文

#### 请求路径
请求路径为/api_entry，遵循rest规范，api_name和api_version会呈现在path路径上，路径为/api_entry/${api_name}/${api_version}

#### 请求头
请求头里边主要放一些通用参数，独立于每一次请求的业务参数，与业务参数分开，目前通用的参数有以下，分别介绍下
- x-wac-version 网关交互协议版本，类似http协议版本1.0、1.1、2.0的作用，目前是4，必填
- x-wac-timestamp 客户端发起请求的时间戳，毫秒数，必填
- x-wac-access-token 客户端请求的access_token，必填
- x-wac-signature 请求的签名，必填，是通过签名算法对请求参数签名生成的，可以参考[这里](api_sign.md)

#### 请求体
请求体里边放对应api的请求参数，参数类型和名称，和具体的api有关。对应的业务参数都放在一个大的框架里，如下结构：
```json
  {
      "biz_params": 
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
      },
      "system_params":
      {
          "param_03": ""
      }
  }
```
其中，biz_params内存放的是业务参数，是和api直接相关的，system_params是用来传递给API网关的一些参数，比如调用回调接口需要告诉API网关要调用哪个合作商的回调接口，需要传递合作商的app_id，此时app_id就放在system_params里边传给API网关的。

### 响应报文
- 同样看下响应体的案例：
```json
  {
    "code": 0,
    "data": 
    {
      "appId": 1,
      "cardId": "12新闻34",
      "source": "outside"
    },
    "error": ""
  }
```

- 分别解释下其中的每个字段
  - `code`表示错误码，返回0表示成功请求。另外，错误码的规范还需要跟其他的团队沟通来制定出来，并且以后的code由开放网关来统一管理。
  - `data`表示API调用的返回的业务数据，对应的内容是JSON格式，格式不限制，可以多层级嵌套。
  - `error`表示错误信息，当请求调用失败时，该字段可以用来展示错误原因。
