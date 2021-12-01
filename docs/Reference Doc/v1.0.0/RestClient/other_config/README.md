---
sort: 8
---

# 其他配置
{% include list.liquid all=true %}

```tip
以上三个通过底层的`HttpClient`内置拦截器实现的功能默认执行顺序为： 100-expect-continue > 重试 > 重定向
```
