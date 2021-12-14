---
sort: 1
---

# Quick Start
It's so easy to get start with `RestClient`.

#### Step 1: Add dependency
```xml
<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>restclient</artifactId>
    <version>${esa-restclient.version}</version>
</dependency>
```

#### Step 2: Send a request and handle response
```java
final RestClient client = RestClient.ofDefault();

final String entity = client.post("http://127.0.0.1:8081/")
        .entity("Hello Server")
        .execute()
        .get()
        .bodyToEntity(String.class);

```

```note
执行请求得到的CompletionStage<RestResponseBase>直接由IO线程执行，**请勿在该线程内做其他耗时操作，以免阻塞IO线程**。
```
```note
`netty` 4.1.52.Final and `tcnative` 2.0.34.Final are directly dependent on. And please make sure the version of `tcnative` matches the version of `netty`.
```