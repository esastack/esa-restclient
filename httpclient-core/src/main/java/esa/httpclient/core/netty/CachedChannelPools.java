/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package esa.httpclient.core.netty;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import esa.commons.Checks;
import esa.commons.reflect.BeanUtils;
import esa.httpclient.core.config.CacheOptions;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.metrics.ConnectionPoolMetric;
import esa.httpclient.core.metrics.ConnectionPoolMetricProvider;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * This class is designed to cache the given {@link ChannelPool}s.
 */
public class CachedChannelPools implements ConnectionPoolMetricProvider {

    private final Cache<SocketAddress, ChannelPool> cachedPools;
    private final AtomicBoolean closed = new AtomicBoolean();

    public CachedChannelPools(CacheOptions options) {
        Checks.checkNotNull(options, "options");
        cachedPools = Caffeine.newBuilder()
                .initialCapacity(options.initialCapacity())
                .maximumSize(options.maximumSize())
                .expireAfterAccess(options.expireSeconds(), TimeUnit.SECONDS)
                .removalListener(new ChannelPoolRemovalListener())
                .build();
    }

    ChannelPool getIfPresent(SocketAddress address) {
        checkClosed();

        return cachedPools.getIfPresent(address);
    }

    ChannelPool getOrCreate(boolean keepAlive,
                            SocketAddress address,
                            Function<SocketAddress, ChannelPool> creator) {
        checkClosed();

        // Only keepAlive connection will be cached.
        if (keepAlive) {
            return cachedPools.get(address, creator);
        } else {
            return creator.apply(address);
        }
    }

    void put(SocketAddress address, ChannelPool channelPool) {
        checkClosed();

        if (channelPool != null) {
            cachedPools.put(address, channelPool);
        }
    }

    void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        for (Map.Entry<SocketAddress, ChannelPool> entry : cachedPools.asMap().entrySet()) {
            try {
                close(entry.getKey(), entry.getValue(), false);
            } catch (Throwable th) {
                LoggerUtils.logger().error("Exception occurred when closing connection pool: {}",
                        entry.getKey(), th);
            }
        }
    }

    @Override
    public ConnectionPoolMetric get(SocketAddress address) {
        if (closed.get()) {
            return null;
        } else {
            return all().get(address);
        }
    }

    @Override
    public Map<SocketAddress, ConnectionPoolMetric> all() {
        if (closed.get()) {
            return Collections.emptyMap();
        }

        Map<SocketAddress, ChannelPool> channelPools0 = new HashMap<>(cachedPools.asMap());

        Map<SocketAddress, ConnectionPoolMetric> metrics = new HashMap<>(channelPools0.size());
        for (Map.Entry<SocketAddress, ChannelPool> entry : channelPools0.entrySet()) {
            metrics.put(entry.getKey(), new ChannelPoolMetricImpl(entry.getValue()));
        }

        return metrics;
    }

    private void checkClosed() {
        if (closed.get()) {
            throw new IllegalStateException("ConnectionPools has been closed");
        }
    }

    static void close(SocketAddress address, ChannelPool channelPool, boolean async) {
        if (channelPool == null) {
            return;
        }

        io.netty.channel.pool.ChannelPool underlying = channelPool.underlying;
        if (!(underlying instanceof SimpleChannelPool)) {
            underlying.close();
            return;
        }

        long startTime = System.nanoTime();

        // Do close sync
        if (!async) {
            try {
                underlying.close();
                LoggerUtils.logger().info("Closed connection pool {} successfully, time elapsed: {}ms",
                        address, (System.nanoTime() - startTime) / 1000_000);
            } catch (Throwable ex) {
                LoggerUtils.logger().error("Failed to close connection pool {}, time elapsed: {}ms",
                        address, (System.nanoTime() - startTime) / 1000_000);
            }
        } else {
            // Do close async
            Future<Void> closeFuture = ((SimpleChannelPool) underlying).closeAsync();
            if (closeFuture.isDone()) {
                closingLog(address, closeFuture, startTime);
            } else {
                closeFuture.addListener(future -> closingLog(address, closeFuture, startTime));
            }
        }
    }

    private static void closingLog(SocketAddress address, Future<Void> closeFuture, long startTime) {
        long endTime = System.nanoTime();
        if (closeFuture.isSuccess()) {
            LoggerUtils.logger().info("Closed connection pool {} successfully, time elapsed: {}ms",
                    address, (endTime - startTime) / 1000_000);
        } else {
            LoggerUtils.logger().error("Failed to close connection pool {}, time elapsed: {}ms",
                    address, (endTime - startTime) / 1000_000);
        }
    }

    private static class ChannelPoolRemovalListener implements RemovalListener<SocketAddress, ChannelPool> {

        @Override
        public void onRemoval(SocketAddress key, ChannelPool value, RemovalCause cause) {
            close(key, value, true);
        }
    }

    private static class ChannelPoolMetricImpl implements ConnectionPoolMetric {

        private final FixedChannelPool channelPool;
        private final ChannelPoolOptions options;

        private ChannelPoolMetricImpl(ChannelPool channelPool) {
            this.channelPool = (FixedChannelPool) channelPool.underlying;
            this.options = channelPool.options;
        }

        @Override
        public int maxSize() {
            return (int) BeanUtils.getFieldValue(channelPool, "maxConnections");
        }

        @Override
        public int maxPendingAcquires() {
            return (int) BeanUtils.getFieldValue(channelPool, "maxPendingAcquires");
        }

        @Override
        public int active() {
            return ((AtomicInteger) BeanUtils.getFieldValue(channelPool, "acquiredChannelCount")).intValue();
        }

        @Override
        public int pendingAcquireCount() {
            return (int) BeanUtils.getFieldValue(channelPool, "pendingAcquireCount");
        }

        @Override
        public ChannelPoolOptions options() {
            return options;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ChannelPoolMetricImpl.class.getSimpleName() + "[", "]")
                    .add("options=" + options)
                    .add("maxSize=" + maxSize())
                    .add("maxPendingAcquires=" + maxPendingAcquires())
                    .add("active=" + active())
                    .add("pendingAcquireCount=" + pendingAcquireCount())
                    .toString();
        }
    }

}
