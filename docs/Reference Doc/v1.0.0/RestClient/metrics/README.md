---
sort: 9
---

# 指标统计

`RestClient`提供了IO线程池及连接池的Metric指标统计，使用时通过`RestClient`实例便可直接获取，具体使用如下：
```java
final RestClient client = RestClient.ofDefault();

ConnectionPoolMetricProvider connectionPoolMetricProvider = client.connectionPoolMetric();
ConnectionPoolMetric connectionPoolMetric = connectionPoolMetricProvider.get(InetSocketAddress.createUnresolved("127.0.0.1", 8080));

// 连接池配置
connectionPoolMetric.options();

// 等待获取连接的请求个数
connectionPoolMetric.pendingAcquireCount();

// 活跃连接个数
connectionPoolMetric.active();

// 等待获取连接队列最大值
connectionPoolMetric.maxPendingAcquires();

// 连接池最大值
connectionPoolMetric.maxSize();

IoThreadGroupMetric ioThreadGroupMetric = client.ioThreadsMetric();

for (IoThreadMetric ioThreadMetric : ioThreadGroupMetric.childExecutors()) {
    // 任务队列大小
    ioThreadMetric.pendingTasks();

    // 任务队列最大值
    ioThreadMetric.maxPendingTasks();

    // 线程状态
    ioThreadMetric.state();

    // 线程名称
    ioThreadMetric.name();
}
```
