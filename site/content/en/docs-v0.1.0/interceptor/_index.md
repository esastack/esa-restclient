---
tags: ["extension"]
title: "Interceptor"
linkTitle: "Interceptor"
weight: 20
description: >
  在`HttpRequest`处理过程中，有时可能需要执行Retry、Redirect、Cache等操作，使用`Interceptor`可以实现类似功能。
---

## 与`Filter`的区别
与`Filter`不同的是，在`Interceptor`中
可以同时获取`HttpRequest`及经过处理后的`HttpResponse`，甚至可以替换原始的`HttpRequest`及处理后的`HttpResponse`。更多与`Filter`的不同如下：
1. 执行时机不同：`RequestFilter`在所有拦截器对`HttpRequest`的处理之后才会执行，`ResponseFilter`在响应头接收到之后（响应body处理前）立即执行，此时所有拦截器对`HttpResponse`的处理均未执行。
2. 关联性不同：`Interceptor`中可以获取`HttpRequest`经过处理后的`HttpResponse`，通过此种方式可以将请求和响应关联起来，而`RequestFilter`和`ResponseFilter`中均无法同时获取到请求和响应，因此
也不能将两者关联起来，但是同一个请求将在`RequestFilter`和`ResponseFilter`中共享一个`FilterContext`实例，因此可以通过该对象传递上下文参数。
3. 通过拦截器可以替换`HttpRequest`或`HttpResponse`，而通过`Filter`无法实现。

鉴于`Interceptor`和`Filter`的区别，下述场景更适合使用`Interceptor`：
1. 需要同时处理`HttpRequest`及`HttpResponse`，如重试、重定向等（因为需要获取`HttpRequest`处理结果后的`HttpResponse`再决定下一步处理逻辑）。
2. `HttpRequest`处理过程中仅需要执行一次的逻辑，同时需要注意该类拦截器的优先级要高于重试、重定向等内置拦截器，否则发生重试、重定向时该拦类拦截器仍会被
多次执行。
3. 需要替换原始的`HttpRequest`或处理后的`HttpResponse`。
