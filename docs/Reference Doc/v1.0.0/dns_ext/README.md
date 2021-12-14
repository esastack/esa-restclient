---
sort: 4
---

# DNS扩展

在每次建立连接前，client可能需要将域名解析成IP地址，`RestClient`通过适配`netty`原生的`AddressResolverGroup`提供了一种更加简单、
灵活的`NameResolver`扩展，用于将url地址中的域名解析成IP地址。

{% include list.liquid all=true %}
