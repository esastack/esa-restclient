---
sort: 1
---

# 读超时
`RestClient`的读超时功能通过底层的`HttpClient`来实现，可以分别支持 Client 级别 及 Request 级别。默认读超时为`6000L`。

## Client级别读超时
Client级别的读超时将对该Client下的所有请求生效，具体配置方式如下：
```java
final RestClient client = RestClient.create()
                      .readTimeout(3000)     //设置读超时
                      .build();
```

## Request级别读超时
当Request设置了读超时，其数据将覆盖Client设置的读超时，具体配置方式如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
                        .readTimeout(3000)      //设置读超时
                        .execute()
                        .get()
                        .bodyToEntity(String.class);
```