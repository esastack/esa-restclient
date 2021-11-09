package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.http.HttpVersion;
import esa.commons.spi.SpiLoader;
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
import io.esastack.restclient.codec.ByteDecoder;
import io.esastack.restclient.codec.ByteEncoder;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.exec.ClientInterceptor;
import io.esastack.restclient.spi.ClientInterceptorFactory;
import io.esastack.restclient.spi.DecodeAdviceFactory;
import io.esastack.restclient.spi.DecoderFactory;
import io.esastack.restclient.spi.EncodeAdviceFactory;
import io.esastack.restclient.spi.EncoderFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The facade which is designed to help user obtain a configured {@link RestClient} easily. Before staring
 * to use this builder.
 */
public class RestClientBuilder implements Reusable<RestClientBuilder>, RestClientOptions {

    public static final String CLIENT = "RestClient";

    private final HttpClientBuilder httpClientBuilder;
    private final List<ClientInterceptor> interceptors = new LinkedList<>();
    private final List<DecodeAdvice> decodeAdvices = new LinkedList<>();
    private final List<EncodeAdvice> encodeAdvices = new LinkedList<>();
    private final List<Decoder> decoders = new LinkedList<>();
    private final List<Encoder> encoders = new LinkedList<>();
    private String name = CLIENT;

    RestClientBuilder() {
        this.httpClientBuilder = new HttpClientBuilder();
    }

    private RestClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder.copy();
    }

    public RestClientBuilder name(String name) {
        this.name = name;
        return self();
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
        return self();
    }

    public RestClientBuilder addEncodeAdvices(List<EncodeAdvice> encodeAdvices) {
        Checks.checkNotNull(encodeAdvices, "encodeAdvices");
        this.encodeAdvices.addAll(encodeAdvices);
        return self();
    }

    public RestClientBuilder addDecodeAdvice(DecodeAdvice decodeAdvice) {
        Checks.checkNotNull(decodeAdvice, "decodeAdvice");
        this.decodeAdvices.add(decodeAdvice);
        return self();
    }

    public RestClientBuilder addDecodeAdvices(List<DecodeAdvice> decodeAdvices) {
        Checks.checkNotNull(decodeAdvices, "decodeAdvices");
        this.decodeAdvices.addAll(decodeAdvices);
        return self();
    }

    public RestClientBuilder addEncoder(Encoder encoder) {
        Checks.checkNotNull(encoder, "encoder");
        this.encoders.add(encoder);
        return self();
    }

    public RestClientBuilder addByteEncoder(ByteEncoder byteEncoder) {
        Checks.checkNotNull(byteEncoder, "byteEncoder");
        this.encoders.add(byteEncoder);
        return self();
    }

    public RestClientBuilder addEncoders(List<Encoder> encoders) {
        Checks.checkNotNull(encoders, "encoders");
        this.encoders.addAll(encoders);
        return self();
    }

    public RestClientBuilder addByteEncoders(List<ByteEncoder> byteEncoders) {
        Checks.checkNotNull(byteEncoders, "byteEncoders");
        this.encoders.addAll(byteEncoders);
        return self();
    }

    public RestClientBuilder addDecoder(Decoder decoder) {
        Checks.checkNotNull(decoder, "decoder");
        this.decoders.add(decoder);
        return self();
    }

    public RestClientBuilder addByteDecoder(ByteDecoder byteDecoder) {
        Checks.checkNotNull(byteDecoder, "byteDecoder");
        this.decoders.add(byteDecoder);
        return self();
    }

    public RestClientBuilder addDecoders(List<Decoder> decoders) {
        Checks.checkNotNull(decoders, "decoders");
        this.decoders.addAll(decoders);
        return self();
    }

    public RestClientBuilder addByteDecoders(List<Decoder> byteDecoders) {
        Checks.checkNotNull(byteDecoders, "byteDecoders");
        this.decoders.addAll(byteDecoders);
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

    @Override
    public String name() {
        return name;
    }

    //***********************************       GET METHODS        ***************************************//
    @Override
    public HostResolver resolver() {
        return httpClientBuilder.resolver();
    }

    @Override
    public boolean isH2ClearTextUpgrade() {
        return httpClientBuilder.isH2ClearTextUpgrade();
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
    public List<EncodeAdvice> unmodifiableEncodeAdvices() {
        return Collections.unmodifiableList(encodeAdvices);
    }

    @Override
    public List<DecodeAdvice> unmodifiableDecodeAdvices() {
        return Collections.unmodifiableList(decodeAdvices);
    }

    @Override
    public List<Encoder> unmodifiableEncoders() {
        return Collections.unmodifiableList(encoders);
    }

    @Override
    public List<Decoder> unmodifiableDecoders() {
        return Collections.unmodifiableList(decoders);
    }

    @Override
    public List<ClientInterceptor> unmodifiableInterceptors() {
        return Collections.unmodifiableList(interceptors);
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

        copiedRestClientBuilder.loadInterceptorsFromSpi();
        copiedRestClientBuilder.sortInterceptors();

        copiedRestClientBuilder.loadDecodersFromSpi();
        copiedRestClientBuilder.sortDecoders();

        copiedRestClientBuilder.loadDecodeAdvicesFromSpi();
        copiedRestClientBuilder.sortDecodeAdvices();

        copiedRestClientBuilder.loadEncodersFromSpi();
        copiedRestClientBuilder.sortEncoders();

        copiedRestClientBuilder.loadEncodeAdvicesFromSpi();
        copiedRestClientBuilder.sortEncodeAdvices();

        return new RestClientImpl(copiedRestClientBuilder,
                copiedRestClientBuilder.httpClientBuilder.build());
    }

    private void loadDecodeAdvicesFromSpi() {
        SpiLoader.cached(DecodeAdviceFactory.class)
                .getByGroup(name(), true)
                .forEach(decodeAdviceFactory ->
                        decodeAdvices.addAll(decodeAdviceFactory.decodeAdvices(this))
                );
        OrderedComparator.sort(decodeAdvices);
    }

    private void sortDecodeAdvices() {
        OrderedComparator.sort(decodeAdvices);
    }

    private void loadEncodeAdvicesFromSpi() {
        SpiLoader.cached(EncodeAdviceFactory.class)
                .getByGroup(name(), true)
                .forEach(encodeAdviceFactory ->
                        encodeAdvices.addAll(encodeAdviceFactory.encodeAdvices(this))
                );
    }

    private void sortEncodeAdvices() {
        OrderedComparator.sort(encodeAdvices);
    }

    private void loadInterceptorsFromSpi() {
        SpiLoader.cached(ClientInterceptorFactory.class)
                .getByGroup(name(), true)
                .forEach(clientInterceptorFactory ->
                        interceptors.addAll(clientInterceptorFactory.interceptors(this))
                );
    }

    private void sortInterceptors() {
        OrderedComparator.sort(interceptors);
    }

    private void loadEncodersFromSpi() {
        SpiLoader.cached(EncoderFactory.class)
                .getByGroup(name(), true)
                .forEach(encoderFactory ->
                        encoders.addAll(encoderFactory.encoders(this))
                );
    }

    private void sortEncoders() {
        OrderedComparator.sort(encoders);
    }


    private void loadDecodersFromSpi() {
        SpiLoader.cached(DecoderFactory.class)
                .getByGroup(name(), true)
                .forEach(decoderFactory ->
                        decoders.addAll(decoderFactory.decoders(this))
                );
    }

    private void sortDecoders() {
        OrderedComparator.sort(decoders);
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
