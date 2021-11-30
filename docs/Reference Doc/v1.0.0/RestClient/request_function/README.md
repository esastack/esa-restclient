---
sort: 1
---

# 读超时
## 请求级别读超时
该功能通过底层的`HttpClient`来实现，可以指定每个请求的读超时时间，使用方式如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
                        .execute()
                        .readTimeout(3000)      //设置读超时
                        .get()
                        .bodyToEntity(String.class);
```

## 100-expect-continue
该功能通过底层的`HttpClient`内置的拦截器对100("expect-continue")响应码的支持，使用时可以设置Client或者Request级别的useExpectContinue参数为false来禁用该功能。如下:

#### Client使用100-expect-continue
```java
RestClient client = RestClient.create()
                .useExpectContinue(true)                //使用100-expect-continue
                .build();
```

#### Request使用100-expect-continue
```java
final String entity = client.get("http://127.0.0.1:8081/")
                        .execute()
                        .useExpectContinue(true)        //使用100-expect-continue,如果Client已经设为true,则这里没有必要重复指定
                        .get()
                        .bodyToEntity(String.class);
```

## 重试
该功能通过底层的`HttpClient`内置的`RetryInterceptor`实现。默认情况下，会对所有抛出连接异常的请求进行重试，其中：最大重试次数为3（不包括原始请求），重试间隔时间为0。使用时，可以通过自定义`RetryOptions`参数更改重试次数、重试条件、重试间隔时间等。如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
                        .execute()
                        .retry(3)         //设置该请求最多重试3次
                        .get()
                        .bodyToEntity(String.class);
```

## 重定向
该功能通过底层的`HttpClient`内置的拦截器实现。默认情况下，`HttpClient`会对响应状态码为301，302，303，307，308的请求重定向，其中：最大重定向次数为5（不包含原始请求）。使用时，可以通过maxRedirects更新重定向次数或者禁用（maxRedirects=0）重定向功能。如下:
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.get("http://127.0.0.1:8081/")
                        .execute()
                        .maxRedirects(3)         //设置该请求最多重定向3次
                        .get()
                        .bodyToEntity(String.class);
```

```tip
以上三个通过底层的`HttpClient`内置拦截器实现的功能默认执行顺序为： 100-expect-continue > 重试 > 重定向
```
