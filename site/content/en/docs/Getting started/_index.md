---
categories: ["Examples", "Placeholders"]
tags: ["test","docs"] 
title: "Getting Started"
linkTitle: "Getting Started"
weight: 2
description: >
  It's very easy to get started with `RestClient`!
---

## Step 1: Add dependency

```xml
<dependency>
    <groupId>io.esastack</groupId>
    <artifactId>restclient</artifactId>
    <version>${esa-restclient.version}</version>
</dependency>
```

## Step 2: Send a request and handle response

```java
final RestClient client = RestClient.ofDefault();

final String entity = client.post("http://127.0.0.1:8081/")
        .entity("Hello Server")
        .execute()
        .toCompletableFuture()
        .get()
        .bodyToEntity(String.class);

```