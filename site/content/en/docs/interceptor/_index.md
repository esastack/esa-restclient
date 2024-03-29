---
tags: ["extension"]
title: "Interceptor"
linkTitle: "Interceptor"
weight: 30
description: >
  `RestClient`支持通过builder配置和SPI加载两种方式配置`RestInterceptor`。
---
## Builder配置
在构造`RestClient`时传入自定义的`RestInterceptor`实例，如：
```java
final RestClient client = RestClient.create()
        .addInterceptor((request, next) -> {
            System.out.println("Interceptor");
            return next.proceed(request);
        }).build();
```
{{< alert title="Tip" >}}
多个拦截器之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
{{< /alert >}}

## SPI
### 普通SPI
`RestClient`支持通过Spi的方式加载`RestInterceptor`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`RestInterceptor`放入指定的目录下即可。
##### RestInterceptorFactory
如果用户自定义的`RestInterceptor`对于不同`RestClient`的配置有不同的实现，则用户可以实现`RestInterceptorFactory`接口，并按照SPI的加载规则将自定义的`RestInterceptorFactory`放入指定的目录下即可。
```java
public interface RestInterceptorFactory {
    Collection<RestInterceptor> interceptors(RestClientOptions clientOptions);
}
```
在`RestClient`构建时将调用`RestInterceptorFactory.interceptors(RestClientOptions clientOptions)`，该方法返回的所有`RestInterceptor`都将加入到构建好的`RestClient`中。

## 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`RestInterceptor`。