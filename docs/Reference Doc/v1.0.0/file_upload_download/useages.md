---
sort: 1
---

# 使用方式

## 大文件下载

### 不使用Multipart编码
```java
final RestClient client = RestClient.ofDefault();
final String entity = client.post("http://127.0.0.1:8081/")
                        .entity(new File("aaa"))
                        .execute()
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

RestResponseBase response = request.execute().get();
System.out.println(response.status());
System.out.println(response.bodyToEntity(String.class));
```
如上所示，`RestClient`将添加的文件和表单参数进行Multipart Encode的结果作为请求的body内容，对应的Content-Type为**multipart/form-data。** 该情形适用于需要进行multipart encode或者存在表单参数的情形。**特别地，如果只上传表单参数，不存在文件时，则可以直接将Content-Type将设置为application/x-www-form-urlencoded。**

```tip
当下载文件内容较大时，建议使用HttpClient并使用[自定义响应处理](../customize_handle/)功能，分块读取响应body内容并将其直接写入文件，避免产生OOM。
```