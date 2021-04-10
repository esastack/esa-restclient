---
sort: 1
---

# 内置拦截器

## 100-expect-continue
`HttpClient`内置了对100("expect-continue")响应码的支持，使用时只需要设置Client或者Request级别的expectContinueEnabled参数为false即可。

## 重试
`HttpClient`使用内置的`RetryInterceptor`实现重试功能。默认情况下，会对所有抛出连接异常的请求进行重试，其中：最大重试次数为3（不包括原始请求），重试间隔时间为0。使用时，可以通过自定义`RetryOptions`参数更改重试次数、重试条件、重试间隔时间等。

## 重定向
默认情况下，`HttpClient`会对响应状态码为301，302，303，307，308的请求重定向，其中：最大重定向次数为5（不包含原始请求）。使用时，可以通过maxRedirects更新重定向次数或者禁用重定向功能(maxRedirects=0)。

## 覆盖内置拦截器
当内置拦截器的功能不能满足用户需求时，可重写对应的内置拦截器的相关方法并通过主动配置或者Spi加载的机制传入，此时，对应的内置拦截器将自动失效。

```tip
内置拦截器默认执行顺序： 100-expect-continue > 重试 > 重定向
```
