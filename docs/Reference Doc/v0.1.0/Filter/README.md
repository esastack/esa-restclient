---
sort: 3
---

# Filter

`Filter`与实际网络请求有关，每次将完整的请求写入channel之前，`RequestFilter`都会执行；每次从channel读取到完整的响应数据之后，`ResponseFilter`都会执行。

```tip
- `RequestFilter`和`ResponseFilter`相关功能通过优先级最低的`Interceptor`实现
- 多个`Filter`之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高
```

{% include list.liquid all=true %}