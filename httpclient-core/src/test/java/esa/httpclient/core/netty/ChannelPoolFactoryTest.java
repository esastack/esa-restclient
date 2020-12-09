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

import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.config.NetOptions;
import esa.httpclient.core.resolver.SystemDefaultResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Java6BDDAssertions.then;

class ChannelPoolFactoryTest {

    private static final int DEFAULT_READ_TIMEOUT = 6000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    private static final int DEFAULT_POOL_SIZE = 512;
    private static final int DEFAULT_QUEUE_SIZE = 256;

    @Test
    void testDetectOptions() {
        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final int connectTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
        final int readTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
        final int connectionPoolSize = ThreadLocalRandom.current().nextInt(1, 1000);
        final int connectionPoolWaitQueueSize = ThreadLocalRandom.current().nextInt(1, 1000);

        final ChannelPoolOptions options1 = ChannelPoolOptions.options()
                .readTimeout(readTimeout)
                .connectTimeout(connectTimeout)
                .poolSize(connectionPoolSize)
                .waitingQueueLength(connectionPoolWaitQueueSize)
                .build();

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 9090);
        final ChannelPoolOptionsProvider provider = key -> {
            if (address1.equals(key)) {
                return options1;
            }
            return null;
        };

        // Case 1: ChannelPoolOptionsProvider is null
        final HttpClientBuilder builder = HttpClient.create();
        final ChannelPoolOptions options11 = ChannelPoolFactory.detectOptions(address1, builder);
        then(options11.readTimeout()).isEqualTo(DEFAULT_READ_TIMEOUT);
        then(options11.connectTimeout()).isEqualTo(DEFAULT_CONNECT_TIMEOUT);
        then(options11.poolSize()).isEqualTo(DEFAULT_POOL_SIZE);
        then(options11.waitingQueueLength()).isEqualTo(DEFAULT_QUEUE_SIZE);

        final ChannelPoolOptions options12 = ChannelPoolFactory.detectOptions(address2, builder);
        then(options12.readTimeout()).isEqualTo(DEFAULT_READ_TIMEOUT);
        then(options12.connectTimeout()).isEqualTo(DEFAULT_CONNECT_TIMEOUT);
        then(options12.poolSize()).isEqualTo(DEFAULT_POOL_SIZE);
        then(options12.waitingQueueLength()).isEqualTo(DEFAULT_QUEUE_SIZE);

        // Case 2: using provider
        builder.channelPoolOptionsProvider(provider);
        final ChannelPoolOptions options21 = ChannelPoolFactory.detectOptions(address1, builder);
        then(options21.readTimeout()).isEqualTo(readTimeout);
        then(options21.connectTimeout()).isEqualTo(connectTimeout);
        then(options21.poolSize()).isEqualTo(connectionPoolSize);
        then(options21.waitingQueueLength()).isEqualTo(connectionPoolWaitQueueSize);

        final ChannelPoolOptions options22 = ChannelPoolFactory.detectOptions(address2, builder);
        then(options22.readTimeout()).isEqualTo(DEFAULT_READ_TIMEOUT);
        then(options22.connectTimeout()).isEqualTo(DEFAULT_CONNECT_TIMEOUT);
        then(options22.poolSize()).isEqualTo(DEFAULT_POOL_SIZE);
        then(options22.waitingQueueLength()).isEqualTo(DEFAULT_QUEUE_SIZE);

        // Case 3: default from HttpClientBuilder
        builder.channelPoolOptionsProvider(null);
        builder.readTimeout(1).connectTimeout(2).connectionPoolWaitingQueueLength(3).connectionPoolSize(4);
        final ChannelPoolOptions options3 = ChannelPoolFactory.detectOptions(address1, builder);
        then(options3.readTimeout()).isEqualTo(1);
        then(options3.connectTimeout()).isEqualTo(2);
        then(options3.poolSize()).isEqualTo(4);
        then(options3.waitingQueueLength()).isEqualTo(3);
    }

    @Test
    void testBuildBootstrap() {
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final EventLoopGroup group = new NioEventLoopGroup();

        final NetOptions options = NetOptions.options().soRcvBuf(10)
                .soSndBuf(100)
                .soKeepAlive(true)
                .soLinger(1000)
                .soKeepAlive(true)
                .soReuseAddr(true)
                .tcpNoDelay(true)
                .writeBufferHighWaterMark(2000)
                .writeBufferLowWaterMark(1000).build();
        final int connectTimeout = ThreadLocalRandom.current().nextInt(2000);
        final Bootstrap bootstrap = ChannelPoolFactory
                .buildBootstrap(address,
                        group,
                        options,
                        connectTimeout,
                        new SystemDefaultResolver());

        then(bootstrap.config().remoteAddress()).isSameAs(address);
        then(bootstrap.config().group()).isSameAs(group);
        then(bootstrap.config().options().get(ChannelOption.CONNECT_TIMEOUT_MILLIS)).isEqualTo(connectTimeout);
        then(bootstrap.config().options().get(ChannelOption.SO_RCVBUF)).isEqualTo(10);
        then(bootstrap.config().options().get(ChannelOption.SO_SNDBUF)).isEqualTo(100);
        then(bootstrap.config().options().get(ChannelOption.SO_LINGER)).isEqualTo(1000);
        then(bootstrap.config().options().get(ChannelOption.SO_REUSEADDR)).isEqualTo(true);
        then(bootstrap.config().options().get(ChannelOption.SO_KEEPALIVE)).isEqualTo(true);
        then(bootstrap.config().options().get(ChannelOption.TCP_NODELAY)).isEqualTo(true);
    }

}
