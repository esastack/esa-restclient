---
sort: 1
---

# 使用方式
`HttpClient`支持通过builder配置和SPI加载两种方式配置`Filter`。

## Builder配置

```java
final HttpClient client = HttpClient.create().addRequestFilter((request, ctx) -> {  // 仅处理Request
    System.out.println("Request Filter");
    return CompletableFuture.completedFuture(null);
}).addResponseFilter((request, response, ctx) -> {   // 仅处理Response
    System.out.println("Response Filter");
    return CompletableFuture.completedFuture(null);
}).addFilter(new DuplexFilter() {   // 同时处理Request和Response
    @Override
    public CompletableFuture<Void> doFilter(HttpRequest request, FilterContext ctx) {
        System.out.println("Request Filter(Duplex)");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> doFilter(HttpRequest request, HttpResponse response, FilterContext ctx) {
        System.out.println("Response Filter(Duplex)");
        return CompletableFuture.completedFuture(null);
    }
}).build();
```

## SPI

`HttpClient`支持通过Spi的方式加载`Filter`，使用时，只需要按照Spi的加载规则将自定义的`Filter`放入指定的目录下即可。
