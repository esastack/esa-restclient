---
sort: 1
---

# Quick Start
It's so easy to get start with `ESA HttpClient`.

#### Step 1: Add dependency
> **Note：`netty` 4.1.52.Final and `tcnative` 2.0.34.Final are directly dependent on.**

> **Note: Please make sure the version of `tcnative` matches the version of `netty`.**


```xml
<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>httpclient-core</artifactId>
    <version>${esa-httpclient.version}</version>
</dependency>
```

#### Step 2: Send a request and handle response
```java
final HttpClient client = HttpClient.create()
        .version(HttpVersion.HTTP_2)
        .h2ClearTextUpgrade(true)
        .build();

final HttpResponse response = client.post("http://127.0.0.1:8081/").body("Hello Server".getBytes()).execute().get();
// handle response here...
```
