### Token的SDK使用



#### 依赖

```xml
<dependency>
   <groupId>com.wacai</groupId>
   <artifactId>wacai-token-sdk</artifactId>
   <version>${version}</version>
</dependency>
```

#### 代码使用样例
 
1. 初始化
   ```java
   AccessTokenClient tokenClient = new AccessTokenClient(${appKey}, ${appSecret});
   tokenClient.init();  
   
   ```
   
2. 获取accessToken
   
   ```java
    AccessTokenClient tokenClient = new AccessTokenClient(${appKey}, ${appSecret});
    tokenClient.init(); 
    String accessToken = tokenClient.getCachedAccessToken();
   
   ```
3. accessToken失效之后refreshToken获取accessToken
   ```java
   AccessTokenClient tokenClient = new AccessTokenClient(${appKey}, ${appSecret});
   tokenClient.init(); 
   tokenClient.setForceCacheInvalid(true);
   String accessToken = tokenClient.getCachedAccessToken();
   ```
 
