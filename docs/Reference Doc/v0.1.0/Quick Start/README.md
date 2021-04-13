---
sort: 1
---

# Quick Start
It's so easy to get start with `ESA HttpClient`.

#### Step 1: Add dependency
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
