---
sort: 8
---

# 连接池配置
连接的创建和销毁通常比较消耗资源，为了提升高并发下场景下的通信效率，`HttpClient`会自动使用连接池来管理与服务端的**长连接**。
默认情况下，单个域名的连接池配置如下：

|         Parameter  	  |	    Description  	  |	      Default     |
|      --------------     |  -----------------    |    ------------   |
|    connectionPoolSize   | 	连接池最大值	      |         512       |
|    connectionPoolWaitingQueueLength   | 	等待获取连接队列大小	      |         256       |
|    connectTimeout       | 	连接超时时间	      |         3000(ms)   |
|    readTimeout          | 	读超时时间	      |         6000(ms)   |

### 全局配置
在build`HttpClient`实例时，可以直接设置全局连接池参数，通过该方式设置的参数对构造出的client实例全局生效。具体使用方式如下：
```java
final HttpClient client = HttpClient.create()
        .connectionPoolSize(512)
        .connectionPoolWaitingQueueLength(256)
        .readTimeout(6000)
        .connectTimeout(3000)
        .build();
```

### 域名级别配置
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
如上所示，`HttpClient`将请求url中的地址解析成`SocketAddress`，然后以该地址为key获取对应的连接池参数，如果结果不为`null`则以获取到的
值为准，否则将使用连接池全局配置。

## 连接池缓存
连接的保持同样需要消耗一定的系统资源，因此及时关闭一些不再需要的连接池是必要的。`HttpClient`默认连接池缓存参数如下：

|         Parameter  	  |	    Description  	  |	      Default     |
|      --------------     |  -----------------    |    ------------   |
|    initialCapacity      | 	缓存池初始化大小	  |         16        |
|    maximumSize          |      缓存池最大值	  |         512       |
|    expireSeconds        | 	访问过期时间	      |         600(s)    |

如上参数表示：连接池初始容量为16，最大容量为512，当连续10min连接池未被使用时该连接池将被关闭。使用时，可以通过系统属性更新上述参数，具体为：
- 通过name为"io.esastack.httpclient.caching-connectionPools.initialCapacity"的系统属性设置连接池初始化大小
- 通过name为"io.esastack.httpclient.caching-connectionPools.maximumSize"的系统属性设置连接池最大值
- 通过name为"io.esastack.httpclient.caching-connectionPools.expireAfterAccess"的系统属性设置访问过期时间
