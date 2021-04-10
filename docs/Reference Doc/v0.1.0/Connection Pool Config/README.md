---
sort: 8
---

# 连接池配置

`HttpClient`支持对不同域名设置不同的连接池参数，如果需要使用该功能，只需要在构造`HttpClient`实例时传入`ChannelPoolOptionsProvider`即可。示例如下：
```java
final HttpClient client = HttpClient.create().channelPoolOptionsProvider(new ChannelPoolOptionsProvider() {
    @Override
    public ChannelPoolOptions get(SocketAddress key) {
        // customize options
	return null;
    }
}).build();
```