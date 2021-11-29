---
sort: 5
---

# 自定义响应处理

默认情况下，`HttpClient`需要将整个响应body内容聚合后存放在内存中返回给业务处理，对于响应body内容较大的请求，此种方式可能会导致OOM。`HttpClient`
开放了底层的接口，支持用户自定义响应信息处理Handler，用于处理接收到的响应headers、body(分块的)、trailers等数据。通过这种方式，可以
灵活的处理响应数据，避免响应body堆积在内存中产生OOM的风险。

{% include list.liquid all=true %}
