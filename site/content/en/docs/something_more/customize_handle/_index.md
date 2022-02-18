---
tags: ["usage"]
title: "自定义响应处理"
linkTitle: "自定义响应处理"
weight: 50
description: >
  默认情况下，`HttpClient`需要将整个响应body内容聚合后存放在内存中返回给业务处理，对于响应body内容较大的请求，此种方式可能会导致OOM。`HttpClient`
  开放了底层的接口，支持用户自定义响应信息处理Handler，用于处理接收到的响应headers、body(分块的)、trailers等数据。通过这种方式，可以
  灵活的处理响应数据，避免响应body堆积在内存中产生OOM的风险。
---

## 使用方式
`HttpClient`提供了两种不同形式的用法来实现自定义Handle----接口实现和流式写法，前者可以方便的共享对象全局属性，后者
使用方式更简洁，使用时可根据需要选择其一。具体使用方式如下：

### 自定义Handler
```java
public class FileHandler extends Handler {
    private static final String PATH = "xxxx";

    private RandomAccessFile file;

    @Override
    public void onStart() {
        String fileName = response().headers().get("fileName");
        try {
            file = new RandomAccessFile(new File(PATH, fileName), "rw");
        } catch (FileNotFoundException e) {
            // Handle execption
        }
    }

    @Override
    public void onData(Buffer content) {
        if (file != null) {
            byte[] data = new byte[content.readableBytes()];
            content.readBytes(data);
            try {
                file.write(data);
            } catch (IOException e) {
                // Handle exception
            }
        } else {
            throw new IllegalStateException("file is null");
        }
    }

    @Override
    public void onEnd() {
        IOUtils.closeQuietly(file);
    }

    @Override
    public void onError(Throwable cause) {
        IOUtils.closeQuietly(file);
    }
}

```
如上所示，自定义一个用于文件下载的Handler，并在构建`HttpClient`时传入该`FileHandler`。如下：
```java
final HttpClient client = HttpClient.create()
        .readTimeout(5000).build();
final HttpRequestFacade request = client.get("http://127.0.0.1:8080/abc").handler(new FileHandler());

final CompletableFuture<HttpResponse> response =  request.execute();

response.whenComplete((rsp, th) -> System.out.println(rsp.status()));

// Wait until complete
System.in.read();
```

### 自定义Handle
除了自定义上述`Handler`之外，`HttpClient`提供了一种更优雅的流式写法来处理响应数据，使用示例如下：
```java
final HttpClient client = HttpClient.create().readTimeout(5000).build();

CompletableFuture<HttpResponse> response =  client.get("http://127.0.0.1:8080/abc")
        .handle((Handle h) -> {
            h.onData((Buffer buf) -> {
                System.out.println("Received response data: " + buf.readableBytes());
            })
             .onTrailer((HttpHeaders trailers) -> h.trailers().add(trailers))
             .onEnd((Void v) -> {
                 System.out.println("Response end");
             })
             .onError((Throwable t) -> System.out.println("Unexpected error: " + t.getMessage()));
        }).execute();

response.whenComplete((rsp, th) -> System.out.println(rsp.status()));

// Wait until complete
System.in.read();

```
如上所示，当接收到请求数据时会调用用户自定义的`Handle`来处理。

