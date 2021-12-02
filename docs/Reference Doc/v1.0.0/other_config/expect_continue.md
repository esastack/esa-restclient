---
sort: 4
---

# 100-expect-continue
`RestClient`的`100-expect-continue`功能通过底层的`HttpClient`来实现，可以分别支持 Client 级别 及 Request 级别。默认`100-expect-continue`为false。

## Client级别`100-expect-continue`
Client级别的`100-expect-continue`将对该Client下的所有 Request 生效，具体配置方式如下：
```java
final RestClient client = RestClient.create()
        .useExpectContinue(true)
        .build();
```

## Request级别`100-expect-continue`
当Request设置了`100-expect-continue`，其数据将覆盖Client设置的`100-expect-continue`，具体配置方式如下:
```java
final String entity = client.get("http://127.0.0.1:8081/")
        .disableExpectContinue()
        .execute()
        .get()
        .bodyToEntity(String.class);
```