---
sort: 2
---

# Codec
用户请求时`RestClient`会自动根据用户的 `Headers` 与 `Entity` 自动选择合适的`Decoder`或`Encoder`进行`Decode`或`Encode`。同时`RestClient`也支持用户在`codec`前后进行插入业务逻辑。

{% include list.liquid all=true %}