---
tags: ["usage"]
title: "文件上传及下载"
linkTitle: "文件上传及下载"
weight: 70
description: >
  `HttpClient`支持文件上传和下载功能。**需要说明地是，对于内容较小的文件，可通过直接将文件内容写入请求body中或者直接从响应body中读取。** 本文只讨论当文件内容过大，直接读取或者写入有OOM风险时的大文件上传和下载功能。
---
## 大文件上传

### 不使用Multipart编码
```java
final HttpClient client = HttpClient.ofDefault();
HttpResponse response = client.post("http://127.0.0.1:8080/abc")
        .body(new File("xxxxx"))
        .execute()
        .get();
System.out.println(response.status());
```
如上所示，`HttpClient`将分块读取文件内容并将其写入请求body中，对应请求的Content-Type为**application/octet-stream**。该情形适用于单个大文件内容作为原始body内容上传的情况。

### 使用Multipart编码
```java
final HttpClient client = HttpClient.ofDefault();

File file = new File("xxxxx");
final MultipartRequest request = client.post("http://127.0.0.1:9997/file/upload")
        .multipart()
        .file("file", file)
        .attr("name", "Bob")
        .attr("address", "China");

HttpResponse response = request.execute().get();
System.out.println(response.status());
System.out.println(response.body().string(StandardCharsets.UTF_8));
```
如上所示，`HttpClient`将添加的文件和表单参数进行Multipart Encode的结果作为请求的body内容，对应的Content-Type为**multipart/form-data。** 该情形适用于需要进行multipart encode或者存在表单参数的情形。**特别地，如果只上传表单参数，不存在文件时，可以设置multipart值为false，后续上传时请求的Content-Type将设置为application/x-www-form-urlencoded。**

{{< alert title="Note" >}}
当下载文件内容较大时，建议使用[自定义响应处理](../customize_handle/)功能，分块读取响应body内容并将其直接写入文件，避免产生OOM。
{{< /alert >}}