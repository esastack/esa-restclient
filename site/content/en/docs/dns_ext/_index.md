---
title: "DNS扩展"
linkTitle: "DNS扩展"
weight: 9
description: >
  在每次建立连接前，client可能需要将域名解析成IP地址，`RestClient`通过适配`netty`原生的`AddressResolverGroup`提供了一种更加简单、
  灵活的`NameResolver`扩展，用于将url地址中的域名解析成IP地址。
---

# 使用方式

```java
final RestClient client = RestClient.create().resolver(new HostResolver() {
    @Override
    public CompletionStage<InetAddress> resolve(String inetHost) {
        // resolve inetHost
        return CompletableFuture.completedFuture(null);
    }
}).build();
```
在构造`RestClient`时传入自定义的`HostResolver`，后续建立连接时会调用`resolve()`方法进行Host地址解析。默认情况下，将使用系统默认的命名服务进行Host解析，详情请查看`SystemDefaultResolver`。