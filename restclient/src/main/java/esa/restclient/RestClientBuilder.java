package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.Reusable;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.interceptor.Interceptor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RestClientBuilder implements Reusable<RestClientBuilder>, RestClientConfig {

    private final HttpClientBuilder httpClientBuilder;

    private final List<Interceptor> interceptors = new LinkedList<>();

    private final LinkedList<ContentTypeResolver> contentTypeResolvers = new LinkedList<>();

    private final LinkedList<ContentTypeProvider> contentTypeProviders = new LinkedList<>();

    private ContentTypeProvider[] unmodifiableContentTypeProviders
            = buildUnmodifiableContentTypeProviders();

    private ContentTypeResolver[] unmodifiableContentTypeResolvers
            = buildUnmodifiableContentTypeResolvers();

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

    public void addContentTypeProvider(ContentTypeProvider contentTypeProvider) {
        this.contentTypeProviders.add(contentTypeProvider);
        this.unmodifiableContentTypeProviders = buildUnmodifiableContentTypeProviders();
    }

    public void addContentTypeProviders(List<ContentTypeProvider> contentTypeProviders) {
        this.contentTypeProviders.addAll(contentTypeProviders);
        this.unmodifiableContentTypeProviders = buildUnmodifiableContentTypeProviders();
    }

    public void addContentTypeResolver(ContentTypeResolver contentTypeResolver) {
        this.contentTypeResolvers.add(contentTypeResolver);
        this.unmodifiableContentTypeResolvers = buildUnmodifiableContentTypeResolvers();
    }

    public void addContentTypeResolvers(List<ContentTypeResolver> contentTypeResolvers) {
        this.contentTypeResolvers.addAll(contentTypeResolvers);
        this.unmodifiableContentTypeResolvers = buildUnmodifiableContentTypeResolvers();
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
    public int readTimeout() {
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
    public List<Interceptor> interceptors() {
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
    public ContentTypeProvider[] unmodifiableContentTypeProviders() {
        return unmodifiableContentTypeProviders;
    }

    @Override
    public ContentTypeResolver[] unmodifiableContentTypeResolvers() {
        return unmodifiableContentTypeResolvers;
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

    private ContentTypeResolver[] buildUnmodifiableContentTypeResolvers() {
        final List<ContentTypeResolver> contentTypeResolvers0 = new LinkedList<>(contentTypeResolvers);
        contentTypeResolvers0.addAll(ContentTypeResolverFactory.DEFAULT.contentTypeResolvers());

        OrderedComparator.sort(contentTypeResolvers0);
        return Collections.unmodifiableList(contentTypeResolvers0).toArray(new ContentTypeResolver[0]);
    }

    private ContentTypeProvider[] buildUnmodifiableContentTypeProviders() {
        final List<ContentTypeProvider> contentTypeProviders0 = new LinkedList<>(contentTypeProviders);
        contentTypeProviders0.addAll(ContentTypeProviderFactory.DEFAULT.contentTypeProviders());

        OrderedComparator.sort(contentTypeProviders0);
        return Collections.unmodifiableList(contentTypeProviders0).toArray(new ContentTypeProvider[0]);
    }

    @Override
    public RestClientBuilder copy() {
        RestClientBuilder restClientBuilder = new RestClientBuilder(httpClientBuilder);
        restClientBuilder.addInterceptors(interceptors);
        restClientBuilder.addContentTypeProviders(contentTypeProviders);
        restClientBuilder.addContentTypeResolvers(contentTypeResolvers);
        return restClientBuilder;
    }
}
