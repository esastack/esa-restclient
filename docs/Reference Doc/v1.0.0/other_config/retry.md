---
sort: 2
---

# 重试
`RestClient`的重试功能通过底层的`HttpClient`来实现，可以分别支持 Client 级别 及 Request 级别。默认最大重试次数为3。

## Client级别重试
Client级别的重试将对该Client下的所有 Request 生效，使用时，可以通过自定义`RetryOptions`参数更改重试次数、重试条件、重试间隔时间等。具体配置方式如下：
```java
final RestClient client = RestClient.create()
        .retryOptions(RetryOptions.options()
                .maxRetries(3)
                .intervalMs(value -> value)
                .predicate((request, response, ctx, cause) -> cause != null)
                .build())
        .connectionPoolSize(2048)
        .build();
```

## Request级别重试
当Request设置了重试次数，其数据将覆盖Client设置的重试次数，具体配置方式如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
        .maxRetries(3)
        .execute()
        .toCompletableFuture()
        .get()
        .bodyToEntity(String.class);
```