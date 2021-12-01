---
sort: 3
---

# 重定向
`RestClient`的重定向功能通过底层的`HttpClient`来实现，可以分别支持 Client 级别 及 Request 级别。默认最大重定向次数为5。

## Client级别重定向
Client级别的重定向将对该Client下的所有 Request 生效，具体配置方式如下：
```java
final RestClient client = RestClient.create()
                      .maxRedirects(3)     //设置重定向次数
                      .build();
```

## Request级别重定向
当Request设置了重定向次数，其数据将覆盖Client设置的重定向次数，具体配置方式如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
                        .maxRedirects(3)     //设置重定向次数
                        .execute()
                        .get()
                        .bodyToEntity(String.class);
```