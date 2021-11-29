---
sort: 1
---

# 使用方式

```java
final HttpClient client = HttpClient.create().resolver(new HostResolver() {
    @Override
    public CompletableFuture<InetAddress> resolve(String inetHost) {
        // resolve inetHost
        return null;
    }
}).build();
```
在构造`HttpClient`时传入自定义的`HostResolver`，后续建立连接时会调用`resolve()`方法进行Host地址解析。默认情况下，将使用系统默认的命名服务进行Host解析，详情请查看`SystemDefaultResolver`。