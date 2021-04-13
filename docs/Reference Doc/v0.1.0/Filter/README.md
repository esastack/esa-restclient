---
sort: 3
---

# Filter

`Filter`分为`RequestFilter`和`ResponseFilter`两种，前者主要用于处理`HttpRequest`，在所有`Interceptor`对`HttpRequest`处理之后执行，
后者主要用于处理`HttpResponse`，在响应headers到达之后立即执行，此时所有拦截器对`HttpResponse`的处理均未开始执行。更多与`Interceptor`的区别如下：

1. 执行顺序不同：`RequestFilter`在所有拦截器对`HttpRequest`的处理之后才会执行，`ResponseFilter`在响应头接收到之后（响应body处理前）立即执行，此时所有拦截器对`HttpResponse`的处理均未执行。
2. 关联性不同：`RequestFilter`和`ResponseFilter`中均无法同时获取到请求和响应，因此也不能将两者关联起来，但是同一个请求将在`RequestFilter`和`ResponseFilter`中共享一个`FilterContext`实例，
因此可以通过该对象传递上下文参数。而`Interceptor`中可以获取`HttpRequest`经过处理后的`HttpResponse`，因此可以通过此种方式将请求和响应关联起来。
3. 通过`RequestFilter`或`ResponseFilter`无法替换原始的`HttpRequest`或`HttpResponse`，而通过`Interceptor`可以实现。

鉴于`Filter`和`Interceptor`的区别，下述场景更适合使用`Filter`：
1. 只需要单独处理`HttpRequest`或`HttpResponse`，如：对每个`HttpRequest`或`HttpResponse`添加固定的请求header。
2. `HttpRequest`处理过程中可能需要多次执行的逻辑，比如在发生重试、重定向时会发出多个网络请求，而这些请求均需要执行的逻辑。

```tip
- 多个`Filter`之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高
- 同一个`HttpRequest`可以通过共享一个`FitlerContext`实例在多个`RequestFilter`和`ResponseFilter`实例间传递上下文参数
```

{% include list.liquid all=true %}
