---
sort: 1
---

# 使用方式

## 主动配置

在构造`HttpClient`时传入自定义的`Interceptor`实例，如：
```java
final HttpClient client = HttpClient.create().addInterceptor((request, next) -> {
    System.out.println("Interceptor");
    return next.proceed(request);
}).build();

```

## SPI

`HttpClient`支持通过Spi的方式加载`Interceptor`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Interceptor`放入指定的目录下即可。

```tip
- 使用时可以通过`Interceptor`替换原始的`HttpRequest`和`HttpResponse`; 2.`HttpClient`内置的重试、重定向、100-expect-continue协商等功能通过`Interceptor`实现。

- 多个拦截器之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```