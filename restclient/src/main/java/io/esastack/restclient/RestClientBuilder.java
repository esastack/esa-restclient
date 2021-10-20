package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.http.HttpVersion;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.Reusable;
import io.esastack.httpclient.core.config.Decompression;
import io.esastack.httpclient.core.config.Http1Options;
import io.esastack.httpclient.core.config.Http2Options;
import io.esastack.httpclient.core.config.NetOptions;
import io.esastack.httpclient.core.config.RetryOptions;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.resolver.HostResolver;
import io.esastack.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.esastack.httpclient.core.util.OrderedComparator;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.exec.ClientInterceptor;
import io.esastack.restclient.spi.DecodeAdviceFactory;
import io.esastack.restclient.spi.EncodeAdviceFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The facade which is designed to help user obtain a configured {@link RestClient} easily. Before staring
 * to use this builder.
 */
public class RestClientBuilder implements Reusable<RestClientBuilder>, RestClientOptions {

    private final HttpClientBuilder httpClientBuilder;
    private final List<ClientInterceptor> interceptors = new LinkedList<>();
    private final LinkedList<DecodeAdvice> decodeAdvices = new LinkedList<>();
    private final LinkedList<EncodeAdvice> encodeAdvices = new LinkedList<>();
    private DecodeAdvice[] unmodifiableDecodeAdvices
            = buildUnmodifiableDecodeAdvices();
    private EncodeAdvice[] unmodifiableEncodeAdvices
            = buildUnmodifiableEncodeAdvices();

    RestClientBuilder() {
        this.httpClientBuilder = new HttpClientBuilder();
    }

    RestClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder.copy();
    }

    public RestClientBuilder resolver(HostResolver resolver) {
        httpClientBuilder.resolver(resolver);
        return self();
    }

    public RestClientBuilder h2ClearTextUpgrade(boolean h2ClearTextUpgrade) {
        httpClientBuilder.h2ClearTextUpgrade(h2ClearTextUpgrade);
        return self();
    }

    public RestClientBuilder connectTimeout(int timeout) {
        httpClientBuilder.connectTimeout(timeout);
        return self();
    }

    public RestClientBuilder idleTimeoutSeconds(int idleTimeoutSeconds) {
        httpClientBuilder.idleTimeoutSeconds(idleTimeoutSeconds);
        return self();
    }

    public RestClientBuilder readTimeout(long readTimeout) {
        httpClientBuilder.readTimeout(readTimeout);
        return self();
    }

    public RestClientBuilder maxContentLength(long maxContentLength) {
        httpClientBuilder.maxContentLength(maxContentLength);
        return self();
    }

    public RestClientBuilder keepAlive(boolean keepAlive) {
        httpClientBuilder.keepAlive(keepAlive);
        return self();
    }

    public RestClientBuilder version(HttpVersion version) {
        httpClientBuilder.version(version);
        return self();
    }

    public RestClientBuilder connectionPoolSize(int size) {
        httpClientBuilder.connectionPoolSize(size);
        return self();
    }

    public RestClientBuilder connectionPoolWaitingQueueLength(int queueSize) {
        httpClientBuilder.connectionPoolWaitingQueueLength(queueSize);
        return self();
    }

    public RestClientBuilder useDecompress(boolean useDecompress) {
        httpClientBuilder.useDecompress(useDecompress);
        return self();
    }

    public RestClientBuilder decompression(Decompression decompression) {
        httpClientBuilder.decompression(decompression);
        return self();
    }

    public RestClientBuilder useExpectContinue(boolean useExpectContinue) {
        httpClientBuilder.useExpectContinue(useExpectContinue);
        return self();
    }

    public RestClientBuilder addInterceptor(ClientInterceptor interceptor) {
        Checks.checkNotNull(interceptor, "interceptor");
        this.interceptors.add(interceptor);
        return self();
    }

    public RestClientBuilder addInterceptors(List<ClientInterceptor> interceptors) {
        Checks.checkNotNull(interceptors, "interceptors");
        this.interceptors.addAll(interceptors);
        return self();
    }

    public RestClientBuilder addEncodeAdvice(EncodeAdvice encodeAdvice) {
        Checks.checkNotNull(encodeAdvice, "encodeAdvice");
        this.encodeAdvices.add(encodeAdvice);
        this.unmodifiableEncodeAdvices = buildUnmodifiableEncodeAdvices();
        return self();
    }

    public RestClientBuilder addEncodeAdvices(List<EncodeAdvice> encodeAdvices) {
        Checks.checkNotNull(encodeAdvices, "encodeAdvices");
        this.encodeAdvices.addAll(encodeAdvices);
        this.unmodifiableEncodeAdvices = buildUnmodifiableEncodeAdvices();
        return self();
    }

    public RestClientBuilder addDecodeAdvice(DecodeAdvice decodeAdvice) {
        Checks.checkNotNull(decodeAdvice, "decodeAdvice");
        this.decodeAdvices.add(decodeAdvice);
        this.unmodifiableDecodeAdvices = buildUnmodifiableDecodeAdvices();
        return self();
    }

    public RestClientBuilder addDecodeAdvices(List<DecodeAdvice> decodeAdvices) {
        Checks.checkNotNull(decodeAdvices, "decodeAdvices");
        this.decodeAdvices.addAll(decodeAdvices);
        this.unmodifiableDecodeAdvices = buildUnmodifiableDecodeAdvices();
        return self();
    }

    public RestClientBuilder channelPoolOptionsProvider(ChannelPoolOptionsProvider channelPoolOptionsProvider) {
        httpClientBuilder.channelPoolOptionsProvider(channelPoolOptionsProvider);
        return self();
    }

    public RestClientBuilder sslOptions(SslOptions sslOptions) {
        httpClientBuilder.sslOptions(sslOptions);
        return self();
    }

    public RestClientBuilder netOptions(NetOptions netOptions) {
        httpClientBuilder.netOptions(netOptions);
        return self();
    }

    public RestClientBuilder http1Options(Http1Options http1Options) {
        httpClientBuilder.http1Options(http1Options);
        return self();
    }

    public RestClientBuilder http2Options(Http2Options http2Options) {
        httpClientBuilder.http2Options(http2Options);
        return self();
    }

    public RestClientBuilder retryOptions(RetryOptions retryOptions) {
        httpClientBuilder.retryOptions(retryOptions);
        return self();
    }

    public RestClientBuilder maxRedirects(int maxRedirects) {
        Checks.checkArg(maxRedirects >= 0, "MaxRedirects must be >= 0!");
        httpClientBuilder.maxRedirects(maxRedirects);
        return self();
    }

    //***********************************       GET METHODS        ***************************************//
    @Override
    public HostResolver resolver() {
        return httpClientBuilder.resolver();
    }

    @Override
    public boolean ish2ClearTextUpgrade() {
        return httpClientBuilder.ish2ClearTextUpgrade();
    }

    @Override
    public int connectTimeout() {
        return httpClientBuilder.connectTimeout();
    }

    @Override
    public long readTimeout() {
        return httpClientBuilder.readTimeout();
    }

    @Override
    public long maxContentLength() {
        return httpClientBuilder.maxContentLength();
    }

    @Override
    public int idleTimeoutSeconds() {
        return httpClientBuilder.idleTimeoutSeconds();
    }

    @Override
    public boolean isKeepAlive() {
        return httpClientBuilder.isKeepAlive();
    }

    @Override
    public HttpVersion version() {
        return httpClientBuilder.version();
    }

    @Override
    public int connectionPoolSize() {
        return httpClientBuilder.connectionPoolSize();
    }

    @Override
    public int connectionPoolWaitingQueueLength() {
        return httpClientBuilder.connectionPoolWaitingQueueLength();
    }

    @Override
    public boolean isUseDecompress() {
        return httpClientBuilder.isUseDecompress();
    }

    @Override
    public Decompression decompression() {
        return httpClientBuilder.decompression();
    }

    @Override
    public boolean isUseExpectContinue() {
        return httpClientBuilder.isUseExpectContinue();
    }

    @Override
    public SslOptions sslOptions() {
        return httpClientBuilder.sslOptions();
    }

    @Override
    public NetOptions netOptions() {
        return httpClientBuilder.netOptions();
    }

    @Override
    public Http1Options http1Options() {
        return httpClientBuilder.http1Options();
    }

    @Override
    public Http2Options http2Options() {
        return httpClientBuilder.http2Options();
    }

    @Override
    public List<ClientInterceptor> interceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public RetryOptions retryOptions() {
        return httpClientBuilder.retryOptions();
    }

    @Override
    public int maxRedirects() {
        return httpClientBuilder.maxRedirects();
    }

    @Override
    public ChannelPoolOptionsProvider channelPoolOptionsProvider() {
        return httpClientBuilder.channelPoolOptionsProvider();
    }

    @Override
    public EncodeAdvice[] unmodifiableEncodeAdvices() {
        return unmodifiableEncodeAdvices;
    }

    @Override
    public DecodeAdvice[] unmodifiableDecodeAdvices() {
        return unmodifiableDecodeAdvices;
    }

    private RestClientBuilder self() {
        return this;
    }

    /**
     * Builds a {@link RestClient} instance.
     *
     * @return client
     */
    public RestClient build() {
        RestClientBuilder copiedRestClientBuilder = copy();

        return new RestClientImpl(copiedRestClientBuilder,
                copiedRestClientBuilder.httpClientBuilder.build());
    }

    private DecodeAdvice[] buildUnmodifiableDecodeAdvices() {
        final List<DecodeAdvice> decodeAdvices0 = new LinkedList<>(decodeAdvices);
        decodeAdvices0.addAll(DecodeAdviceFactory.DEFAULT.decodeAdvices());
        OrderedComparator.sort(decodeAdvices0);
        return Collections.unmodifiableList(decodeAdvices0).toArray(new DecodeAdvice[0]);
    }

    private EncodeAdvice[] buildUnmodifiableEncodeAdvices() {
        final List<EncodeAdvice> encodeAdvices0 = new LinkedList<>(encodeAdvices);
        encodeAdvices0.addAll(EncodeAdviceFactory.DEFAULT.encodeAdvices());
        OrderedComparator.sort(encodeAdvices0);
        return Collections.unmodifiableList(encodeAdvices0).toArray(new EncodeAdvice[0]);
    }

    @Override
    public RestClientBuilder copy() {
        RestClientBuilder restClientBuilder = new RestClientBuilder(httpClientBuilder);
        restClientBuilder.addInterceptors(interceptors);
        restClientBuilder.addEncodeAdvices(encodeAdvices);
        restClientBuilder.addDecodeAdvices(decodeAdvices);
        return restClientBuilder;
    }
}
