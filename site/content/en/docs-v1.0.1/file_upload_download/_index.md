---
tags: ["usage"]
title: "大文件上传"
linkTitle: "大文件上传"
weight: 50
description: >
  `RestClient`支持大文件上传功能。**需要说明地是，对于内容较小的文件，可通过直接将文件内容写入请求body。** 本文只讨论当文件内容过大，直接写入有OOM风险时的大文件上传功能。
---
## 使用方式
### 不使用Multipart编码
```java
final RestClient client = RestClient.ofDefault();
final String entity = client.post("http://127.0.0.1:8081/")
        .entity(new File("aaa"))
        .execute()
        .toCompletableFuture()
        .get()
        .bodyToEntity(String.class);
```
如上所示，`RestClient`将分块读取文件内容并将其写入请求body中，对应请求的Content-Type为**application/octet-stream**。该情形适用于单个大文件内容作为原始body内容上传的情况。

### 使用Multipart编码
```java
final RestClient client = RestClient.ofDefault();

File file = new File("xxxxx");
final RestMultipartRequest request = client.post("http://127.0.0.1:9997/file/upload")
        .multipart()
        .file("file", file)
        .attr("name", "Bob")
        .attr("address", "China");

RestResponseBase response = request
        .execute()
        .toCompletableFuture()
        .get();
System.out.println(response.status());
System.out.println(response.bodyToEntity(String.class));
```
如上所示，`RestClient`将添加的文件和表单参数进行Multipart Encode的结果作为请求的body内容，对应的Content-Type为**multipart/form-data。** 该情形适用于需要进行multipart encode或者存在表单参数的情形。**特别地，如果只上传表单参数，不存在文件时，则可以直接将Content-Type设置为application/x-www-form-urlencoded。**