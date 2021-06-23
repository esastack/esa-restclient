package esa.restclient.core;

import esa.commons.Checks;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.Reusable;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.core.codec.Decoder;
import esa.restclient.core.codec.Encoder;
import esa.restclient.core.interceptor.Interceptor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RestClientBuilder implements Reusable<RestClientBuilder> {


    private final HttpClientBuilder httpClientBuilder;

    private final List<Interceptor> interceptors = new LinkedList<>();
    private final List<Encoder> encoders = new LinkedList<>();
    private final List<Decoder> decoders = new LinkedList<>();

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

    //TODO 关于readTimeout要不要改成long类型
    public RestClientBuilder readTimeout(int readTimeout) {
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

    public RestClientBuilder addDecoder(Decoder decoder) {
        Checks.checkNotNull(decoder, "Decoder must not be null");
        this.decoders.add(decoder);
        return self();
    }

    public RestClientBuilder addDecoders(List<Decoder> decoders) {
        Checks.checkNotNull(decoders, "Decoders must not be null");
        this.decoders.addAll(decoders);
        return self();
    }

    public RestClientBuilder addEncoder(Encoder encoder) {
        Checks.checkNotNull(encoder, "Encoder must not be null");
        this.encoders.add(encoder);
        return self();
    }

    public RestClientBuilder addEncoders(List<Encoder> encoders) {
        Checks.checkNotNull(encoders, "Encoders must not be null");
        this.encoders.addAll(encoders);
        return self();
    }

    public RestClientBuilder addInterceptor(Interceptor interceptor) {
        Checks.checkNotNull(interceptor, "Interceptor must not be null");
        this.interceptors.add(interceptor);
        return self();
    }

    public RestClientBuilder addInterceptors(List<Interceptor> interceptors) {
        Checks.checkNotNull(interceptors, "Interceptors must not be null");
        this.interceptors.addAll(interceptors);
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
        Checks.checkArg(maxRedirects >= 0,"MaxRedirects must be >= 0!");
        httpClientBuilder.maxRedirects(maxRedirects);
        return self();
    }


    //***********************************       GET METHODS        ***************************************//

    public HostResolver resolver() {
        return httpClientBuilder.resolver();
    }

    public boolean ish2ClearTextUpgrade() {
        return httpClientBuilder.ish2ClearTextUpgrade();
    }

    public int connectTimeout() {
        return httpClientBuilder.connectTimeout();
    }

    public long readTimeout() {
        return httpClientBuilder.readTimeout();
    }

    public long maxContentLength() {
        return httpClientBuilder.maxContentLength();
    }

    public int idleTimeoutSeconds() {
        return httpClientBuilder.idleTimeoutSeconds();
    }

    public boolean isKeepAlive() {
        return httpClientBuilder.isKeepAlive();
    }

    public HttpVersion version() {
        return httpClientBuilder.version();
    }

    public int connectionPoolSize() {
        return httpClientBuilder.connectionPoolSize();
    }

    public int connectionPoolWaitingQueueLength() {
        return httpClientBuilder.connectionPoolWaitingQueueLength();
    }

    public boolean isUseDecompress() {
        return httpClientBuilder.isUseDecompress();
    }

    public Decompression decompression() {
        return httpClientBuilder.decompression();
    }

    public boolean isUseExpectContinue() {
        return httpClientBuilder.isUseExpectContinue();
    }

    public SslOptions sslOptions() {
        return httpClientBuilder.sslOptions();
    }

    public NetOptions netOptions() {
        return httpClientBuilder.netOptions();
    }

    public Http1Options http1Options() {
        return httpClientBuilder.http1Options();
    }

    public Http2Options http2Options() {
        return httpClientBuilder.http2Options();
    }

    public List<Interceptor> interceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    public List<Decoder> decoders() {
        return Collections.unmodifiableList(decoders);
    }

    public List<Encoder> encoders() {
        return Collections.unmodifiableList(encoders);
    }

    public RetryOptions retryOptions() {
        return httpClientBuilder.retryOptions();
    }

    public int maxRedirects() {
        return httpClientBuilder.maxRedirects();
    }


    public ChannelPoolOptionsProvider channelPoolOptionsProvider() {
        return httpClientBuilder.channelPoolOptionsProvider();
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
        return new DefaultRestClient(copiedRestClientBuilder,
                copiedRestClientBuilder.httpClientBuilder.build());
    }

    @Override
    public RestClientBuilder copy() {
        return new RestClientBuilder(httpClientBuilder)
                .addInterceptors(interceptors)
                .addEncoders(encoders)
                .addDecoders(decoders);
    }
}
