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
package esa.httpclient.core;

import esa.commons.Checks;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.config.CacheOptions;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.config.Decompression;
import esa.httpclient.core.config.Http1Options;
import esa.httpclient.core.config.Http2Options;
import esa.httpclient.core.config.NetOptions;
import esa.httpclient.core.config.RetryOptions;
import esa.httpclient.core.config.SslOptions;
import esa.httpclient.core.exec.ExpectContinueInterceptor;
import esa.httpclient.core.exec.FilteringExec;
import esa.httpclient.core.exec.Interceptor;
import esa.httpclient.core.exec.RedirectInterceptor;
import esa.httpclient.core.exec.RetryInterceptor;
import esa.httpclient.core.filter.DuplexFilter;
import esa.httpclient.core.filter.RequestFilter;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.netty.ChannelPools;
import esa.httpclient.core.netty.NettyHttpClient;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.resolver.SystemDefaultResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.httpclient.core.spi.DuplexFilterFactory;
import esa.httpclient.core.spi.InterceptorFactory;
import esa.httpclient.core.spi.RequestFilterFactory;
import esa.httpclient.core.spi.ResponseFilterFactory;
import esa.httpclient.core.util.OrderedComparator;
import io.netty.channel.pool.ChannelPool;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The facade which is designed to help user obtain a configured {@link HttpClient} easily. Before staring
 * to use this builder, we expect that you have known that:
 * <p>
 * 1. The generated {@link HttpClient}s will cache it's {@link ChannelPool} by {@link SocketAddress},
 * you can get more information from ChannelPools which is in netty package.
 * <p>
 * 2. The default {@link SystemDefaultResolver} will be used to resolve host to ips.
 * <p>
 * 3. All {@link InetAddress}s resolved from the same host name will share a common connection pool whose size is
 * configured by {@link #connectionPoolSize} and waiting queue length is configured by
 * {@link #connectionPoolWaitingQueueLength}.
 */
public class HttpClientBuilder implements Reusable<HttpClientBuilder> {

    private static final HostResolver DEFAULT_RESOLVER = new SystemDefaultResolver();
    private static final ChannelPoolOptionsProvider NULL_CHANNEL_OPTIONS = address -> null;

    private boolean keepAlive = true;
    private boolean useDecompress = false;
    private Decompression decompression = Decompression.GZIP_DEFLATE;

    private HostResolver resolver = DEFAULT_RESOLVER;

    private HttpVersion version = HttpVersion.HTTP_1_1;

    /**
     * Default -1L, Which means not limited
     */
    private long maxContentLength = -1L;

    private int idleTimeoutSeconds = -1;

    private int readTimeout = 6000;

    /**
     * Whether to use h2c only when current {@link #version} equals {@link HttpVersion#HTTP_2} and scheme
     * equals {@link Scheme#HTTP}. If {@code true}, we try to use application-layer protocol negotiation
     * firstly(if fails, fallback to {@link HttpVersion#HTTP_1_1}), otherwise, use {@link HttpVersion#HTTP_2}
     * directly.
     */
    private boolean h2ClearTextUpgrade = true;

    private int connectTimeout = 3000;

    /**
     * Max connection pool size of a single domain:port or ip:port which is defined in {@link HttpRequest#uri()}.
     * eg, if the uri is configured as: http://www.baidu.com, all {@link java.net.SocketAddress} resolved by
     * {@link HostResolver#resolve(String)} will share a common connection pool. In other way, if the uri is
     * configured as: http://127.0.0.1/index, the address of {@code 127.0.0.1} will use all the connection pool
     * exclusive.
     */
    private int connectionPoolSize = 512;

    /**
     * Max connection pool wait queue of a connection pool.
     */
    private int connectionPoolWaitingQueueLength = 256;

    private int maxRedirects = 5;

    private SslOptions sslOptions;
    private NetOptions netOptions;
    private Http1Options http1Options;
    private Http2Options http2Options;

    private RetryOptions retryOptions = RetryOptions.ofDefault();
    private boolean useExpectContinue = false;

    /**
     * This map is helpful for configuring {@link ChannelPoolOptions}s for every {@link SocketAddress}.
     */
    private ChannelPoolOptionsProvider channelPoolOptionsProvider = NULL_CHANNEL_OPTIONS;

    private final List<Interceptor> interceptors = new LinkedList<>();
    private final List<RequestFilter> requestFilters = new LinkedList<>();
    private final List<ResponseFilter> responseFilters = new LinkedList<>();

    private Interceptor[] unmodifiableInterceptors;

    public HttpClientBuilder resolver(HostResolver resolver) {
        this.resolver = resolver;
        return self();
    }

    public HttpClientBuilder h2ClearTextUpgrade(boolean h2ClearTextUpgrade) {
        this.h2ClearTextUpgrade = h2ClearTextUpgrade;
        return self();
    }

    public HttpClientBuilder connectTimeout(int timeout) {
        Checks.checkArg(timeout > 0, "connectTimeout must greater than 0");
        this.connectTimeout = timeout;
        return self();
    }

    public HttpClientBuilder idleTimeoutSeconds(int idleTimeoutSeconds) {
        this.idleTimeoutSeconds = idleTimeoutSeconds;
        return self();
    }

    public HttpClientBuilder readTimeout(int timeout) {
        Checks.checkArg(timeout > 0, "readTimeout must greater than 0");
        this.readTimeout = timeout;
        return self();
    }

    public HttpClientBuilder maxContentLength(long maxContentLength) {
        this.maxContentLength = maxContentLength;
        return self();
    }

    public HttpClientBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return self();
    }

    public HttpClientBuilder version(HttpVersion version) {
        Checks.checkNotNull(version, "HttpVersion must not be null");
        this.version = version;
        return self();
    }

    public HttpClientBuilder connectionPoolSize(int size) {
        Checks.checkArg(size >= 1, "poolSize is " + size +
                " (expected >= 1)");
        this.connectionPoolSize = size;
        return self();
    }

    public HttpClientBuilder connectionPoolWaitingQueueLength(int queueSize) {
        Checks.checkArg(queueSize >= 1, "waitingQueueLength is " + queueSize +
                " (expected >= 1)");
        this.connectionPoolWaitingQueueLength = queueSize;
        return self();
    }

    public HttpClientBuilder useDecompress(boolean useDecompress) {
        this.useDecompress = useDecompress;
        return self();
    }

    public HttpClientBuilder decompression(Decompression decompression) {
        Checks.checkNotNull(decompression, "Decompression must not be null");
        this.decompression = decompression;
        return self();
    }

    public HttpClientBuilder useExpectContinue(boolean useExpectContinue) {
        this.useExpectContinue = useExpectContinue;
        return self();
    }

    public boolean isUseExpectContinue() {
        return this.useExpectContinue;
    }

    public HttpClientBuilder addInterceptor(Interceptor interceptor) {
        Checks.checkNotNull(interceptor, "Interceptor must not be null");
        this.interceptors.add(interceptor);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addInterceptors(List<Interceptor> interceptors) {
        Checks.checkNotNull(interceptors, "Interceptors must not be null");
        this.interceptors.addAll(interceptors);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addRequestFilter(RequestFilter filter) {
        Checks.checkNotNull(filter, "RequestFilter must not be null");
        this.requestFilters.add(filter);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addRequestFilters(List<RequestFilter> filters) {
        Checks.checkNotNull(filters, "RequestFilters must not be null");
        this.requestFilters.addAll(filters);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addResponseFilter(ResponseFilter filter) {
        Checks.checkNotNull(filter, "ResponseFilter must not be null");
        this.responseFilters.add(filter);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addResponseFilters(List<ResponseFilter> filters) {
        Checks.checkNotNull(filters, "ResponseFilters must not be null");
        this.responseFilters.addAll(filters);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addFilter(DuplexFilter filter) {
        Checks.checkNotNull(filter, "DuplexFilter must not be null");
        this.requestFilters.add(filter);
        this.responseFilters.add(filter);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder addFilters(List<DuplexFilter> filters) {
        Checks.checkNotNull(filters, "DuplexFilters must not be null");
        this.requestFilters.addAll(filters);
        this.responseFilters.addAll(filters);
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder channelPoolOptionsProvider(ChannelPoolOptionsProvider channelPoolOptionsProvider) {
        this.channelPoolOptionsProvider = channelPoolOptionsProvider;
        return self();
    }

    public HttpClientBuilder sslOptions(SslOptions sslOptions) {
        this.sslOptions = sslOptions;
        return self();
    }

    public HttpClientBuilder netOptions(NetOptions netOptions) {
        this.netOptions = netOptions;
        return self();
    }

    public HttpClientBuilder http1Options(Http1Options http1Options) {
        this.http1Options = http1Options;
        return self();
    }

    public HttpClientBuilder http2Options(Http2Options http2Options) {
        this.http2Options = http2Options;
        return self();
    }

    public HttpClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        this.unmodifiableInterceptors = buildUnmodifiableInterceptors();
        return self();
    }

    public HttpClientBuilder maxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return self();
    }

    public HostResolver resolver() {
        return resolver == null ? DEFAULT_RESOLVER : resolver;
    }

    public boolean ish2ClearTextUpgrade() {
        return h2ClearTextUpgrade;
    }

    private HttpClientBuilder self() {
        return this;
    }

    /**
     * Builds a {@link HttpClient} instance.
     *
     * @return client
     */
    public HttpClient build() {
        return new NettyHttpClient(copy(), new ChannelPools(CacheOptions.ofDefault()));
    }

    /**
     * Obtains a new {@link HttpClientBuilder} as a copy of current instance.
     *
     * @return B
     */
    @Override
    public HttpClientBuilder copy() {
        return new HttpClientBuilder()
                .useDecompress(useDecompress)
                .decompression(decompression)
                .resolver(resolver)
                .h2ClearTextUpgrade(h2ClearTextUpgrade)
                .useExpectContinue(useExpectContinue)
                .connectTimeout(connectTimeout)
                .idleTimeoutSeconds(idleTimeoutSeconds)
                .readTimeout(readTimeout)
                .maxContentLength(maxContentLength)
                .keepAlive(keepAlive)
                .version(version)
                .maxRedirects(maxRedirects)
                .connectionPoolSize(connectionPoolSize)
                .connectionPoolWaitingQueueLength(connectionPoolWaitingQueueLength)
                .sslOptions(Reusable.copyOf(sslOptions))
                .netOptions(Reusable.copyOf(netOptions))
                .http1Options(Reusable.copyOf(http1Options))
                .http2Options(Reusable.copyOf(http2Options))
                .retryOptions(Reusable.copyOf(retryOptions))
                .channelPoolOptionsProvider(channelPoolOptionsProvider)
                .addInterceptors(interceptors)
                .addRequestFilters(requestFilters)
                .addResponseFilters(responseFilters);
    }

    //***********************************       GET METHODS        ***************************************//

    public int connectTimeout() {
        return connectTimeout;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public long maxContentLength() {
        return maxContentLength;
    }

    public int idleTimeoutSeconds() {
        return idleTimeoutSeconds;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public HttpVersion version() {
        return version;
    }

    public int connectionPoolSize() {
        return connectionPoolSize;
    }

    public int connectionPoolWaitingQueueLength() {
        return connectionPoolWaitingQueueLength;
    }

    public boolean isUseDecompress() {
        return this.useDecompress;
    }

    public Decompression decompression() {
        return this.decompression;
    }

    public ChannelPoolOptionsProvider channelPoolOptionsProvider() {
        return this.channelPoolOptionsProvider;
    }

    public SslOptions sslOptions() {
        return this.sslOptions;
    }

    public NetOptions netOptions() {
        return this.netOptions;
    }

    public Http1Options http1Options() {
        return http1Options;
    }

    public Http2Options http2Options() {
        return http2Options;
    }

    public List<Interceptor> interceptors() {
        return Collections.unmodifiableList(new ArrayList<>(interceptors));
    }

    public List<RequestFilter> requestFilters() {
        return Collections.unmodifiableList(new ArrayList<>(requestFilters));
    }

    public List<ResponseFilter> responseFilters() {
        return Collections.unmodifiableList(new ArrayList<>(responseFilters));
    }

    public RetryOptions retryOptions() {
        return retryOptions;
    }

    public int maxRedirects() {
        return maxRedirects;
    }

    /**
     * Obtains the unmodified {@link Interceptor}s of current {@link #interceptors}.
     *
     * @return interceptors
     */
    public Interceptor[] unmodifiableInterceptors() {
        if (unmodifiableInterceptors == null) {
            unmodifiableInterceptors = buildUnmodifiableInterceptors();
        }
        return unmodifiableInterceptors;
    }

    public ResponseFilter[] buildUnmodifiableResponseFilters() {
        final List<ResponseFilter> filters0 = new LinkedList<>(responseFilters);
        filters0.addAll(ResponseFilterFactory.DEFAULT.filters());
        filters0.addAll(DuplexFilterFactory.DEFAULT.filters());

        OrderedComparator.sort(filters0);
        return Collections.unmodifiableList(filters0).toArray(new ResponseFilter[0]);
    }

    private Interceptor[] buildUnmodifiableInterceptors() {
        final List<Interceptor> interceptors0 = new LinkedList<>(interceptors);
        interceptors0.addAll(InterceptorFactory.DEFAULT.interceptors());

        // Add RetryInterceptor only when configured and absent
        if (retryOptions != null && absent(interceptors0, RetryInterceptor.class)) {
            interceptors0.add(new RetryInterceptor(retryOptions.predicate(), retryOptions.intervalMillis()));
        }

        // Add RedirectInterceptor only when configured and absent
        if (absent(interceptors0, RedirectInterceptor.class)) {
            interceptors0.add(new RedirectInterceptor());
        }

        // Add ExpectContinueInterceptor only when configured an absent
        if (absent(interceptors0, ExpectContinueInterceptor.class)) {
            interceptors0.add(new ExpectContinueInterceptor());
        }

        // Add FilteringInterceptor only when absent
        if (absent(interceptors0, FilteringExec.class)) {
            interceptors0.add(new FilteringExec(buildUnmodifiableRequestFilters()));
        }

        OrderedComparator.sort(interceptors0);
        return Collections.unmodifiableList(interceptors0).toArray(new Interceptor[0]);
    }

    private RequestFilter[] buildUnmodifiableRequestFilters() {
        final List<RequestFilter> filters0 = new LinkedList<>(requestFilters);
        filters0.addAll(RequestFilterFactory.DEFAULT.filters());
        filters0.addAll(DuplexFilterFactory.DEFAULT.filters());

        OrderedComparator.sort(filters0);
        return Collections.unmodifiableList(filters0).toArray(new RequestFilter[0]);
    }

    private static boolean absent(List<Interceptor> interceptors, Class<? extends Interceptor> target) {
        if (interceptors == null || interceptors.isEmpty()) {
            return true;
        }

        for (Interceptor interceptor : interceptors) {
            if (target.isAssignableFrom(interceptor.getClass())) {
                return false;
            }
        }

        return true;
    }
}
