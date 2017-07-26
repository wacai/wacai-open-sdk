## 鉴权&授权

### 采用OAuth 2机制，具体可以参考[这里](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)

### 鉴权和授权入口
- 测试环境统一入口是`http://guard.test.wacai.info/auth`
- 统一使用POST表单提交

#### 生成access_token和refresh_token
- 接口路径：/token
- Content_Type: application/x-www-form-urlencoded
- 接口参数：
  - grant_type: 授权类型，必填，当前接口目前只支持 client_credentials
  - app_key: 必传
  - timestamp: 当前时间戳，以秒为单位
  - sign: 请求参数的签名，必传
- 返回结构：

```json
{
  "code": 0,
  "error": null,
  "data": {
    "access_token": "15916d6b09a140cb9625d2ac14d1aa28",
    "token_type": "Bearer",
    "expires_in": 864000,
    "refresh_token": "6703cabab4634af38916c46905bfca72"
  }
}
```
- access_token: 授权之后产生的令牌
- token_type: 目前全是Bearer
- expires_in: 表示access_token的有效时间，以秒为单位，access_token将在这个时间之后过期
- refresh_token: 用来刷新access_token，重新生成access_token时使用的

#### 使用refresh_token刷新access_token
- 接口路径: /refresh
- Content_Type: application/x-www-form-urlencoded
- 接口参数：
  - grant_type: 授权类型，必填，当前接口目前只支持 refresh_token
  - app_key: 必传
  - timestamp: 当前时间戳，以秒为单位
  - sign: 请求参数签名，必传
  - refresh_token: 必传，第一步申请得到的refresh_token
- 返回结果同上
