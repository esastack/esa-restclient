---
tags: ["usage"]
title: "分块读写"
linkTitle: "分块读写"
weight: 60
description: >
  `HttpClient`支持分块写请求数据及分块处理响应数据，分块读功能请参考[自定义响应处理](../customize_handle)，此处不再赘述。本文仅介绍分块写请求body相关功能。
---
## 使用方式

```java
final HttpClient client = HttpClient.ofDefault();

final SegmentRequest request = client.post("http://127.0.0.1:8080/").segment();

for (int i = 0; i < 100; i++) {
    // request.isWritable（）判断适用于较大内容的分块写
    if (request.isWritable()) {
        request.write("It's body".getBytes());
    } else {
        throw new IllegalStateException("Channel is unwritable");
    }
}

HttpResponse response = request.end("It's end".getBytes()).get();
System.out.println(response.status());
System.out.println(response.body().string(StandardCharsets.UTF_8));
```
如上所示，使用时通过`HttpClient`构造一个可分块写的`SegmentRequest`并在有可写数据时直接写入，最后结束请求。
