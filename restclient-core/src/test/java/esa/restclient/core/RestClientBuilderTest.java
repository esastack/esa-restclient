package esa.restclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.core.codec.Decoder;
import esa.restclient.core.codec.Encoder;
import esa.restclient.core.exec.InvokeChain;
import esa.restclient.core.interceptor.Interceptor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestClientBuilderTest {
    private final static HostResolver resolver = inetHost -> null;
    private final static boolean h2ClearTextUpgrade = ThreadLocalRandom.current().nextBoolean();
    private final static int connectTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
    private final static int readTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
    private final static boolean keepAlive = ThreadLocalRandom.current().nextBoolean();
    private final static HttpVersion version = HttpVersion.HTTP_2;
    private final static int connectionPoolSize = ThreadLocalRandom.current().nextInt(1, 1000);
    private final static int connectionPoolWaitQueueSize = ThreadLocalRandom.current().nextInt(1, 1000);
    private final static boolean useDecompress = ThreadLocalRandom.current().nextBoolean();
    private final static Decompression decompression = Decompression.GZIP_DEFLATE;
    private final static boolean useExpectContinue = ThreadLocalRandom.current().nextBoolean();
    private final static ChannelPoolOptionsProvider channelPoolOptionsProvider = key -> null;
    private final static NetOptions netOptions = NetOptions.ofDefault();
    private final static Http1Options http1Options = Http1Options.ofDefault();
    private final static Http2Options http2Options = Http2Options.ofDefault();
    private final static RetryOptions retryOptions = RetryOptions.ofDefault();
    private final static SslOptions sslOptions = SslOptions.options().build();
    private final static int maxRedirects = ThreadLocalRandom.current().nextInt(10, 1000);
    private final static long maxContentLength = ThreadLocalRandom.current().nextLong(10000);
    private final static int idleTimeoutSeconds = ThreadLocalRandom.current().nextInt(1000);

    @Test
    void testBasic() {
        assertBasicData(wrapWithBasicData(new RestClientBuilder()), false);
        assertBasicData(new RestClientBuilder(wrapWithBasicData(new HttpClientBuilder())), true);
    }

    @Test
    void testEncoderAndDecoder() {
        RestClientBuilder builder = new RestClientBuilder();
        assertThrows(NullPointerException.class, () -> builder.addDecoder(null));
        assertThrows(NullPointerException.class, () -> builder.addDecoders(null));
        assertThrows(NullPointerException.class, () -> builder.addEncoder(null));
        assertThrows(NullPointerException.class, () -> builder.addEncoders(null));
        assertThrows(UnsupportedOperationException.class, () -> builder.decoders().add(new TestDecoder()));
        builder.addDecoder(new TestDecoder());
        assertEquals(1, builder.decoders().size());
        builder.addDecoder(new TestDecoder());
        assertEquals(2, builder.decoders().size());
        List<Decoder> decoders = Arrays.asList(new TestDecoder[]{new TestDecoder(), new TestDecoder()});
        builder.addDecoders(decoders);
        assertEquals(4, builder.decoders().size());
        assertThrows(UnsupportedOperationException.class, () -> builder.decoders().add(new TestDecoder()));

        assertThrows(UnsupportedOperationException.class, () -> builder.encoders().add(new TestEncoder()));
        builder.addEncoder(new TestEncoder());
        assertEquals(1, builder.encoders().size());
        builder.addEncoder(new TestEncoder());
        assertEquals(2, builder.encoders().size());
        List<Encoder> encoders = Arrays.asList(new TestEncoder[]{new TestEncoder(), new TestEncoder()});
        builder.addEncoders(encoders);
        assertEquals(4, builder.encoders().size());
        assertThrows(UnsupportedOperationException.class, () -> builder.encoders().add(new TestEncoder()));
    }


    @Test
    void testInterceptor() {
        RestClientBuilder builder = new RestClientBuilder();
        assertThrows(NullPointerException.class, () -> builder.addInterceptor(null));
        assertThrows(NullPointerException.class, () -> builder.addInterceptors(null));
        assertThrows(UnsupportedOperationException.class, () -> builder.interceptors().add(new TestInterceptor()));
        builder.addInterceptor(new TestInterceptor());
        assertEquals(1, builder.interceptors().size());
        builder.addInterceptor(new TestInterceptor());
        assertEquals(2, builder.interceptors().size());
        List<Interceptor> interceptors = Arrays.asList(new TestInterceptor[]{new TestInterceptor(), new TestInterceptor()});
        builder.addInterceptors(interceptors);
        assertEquals(4, builder.interceptors().size());
        assertThrows(UnsupportedOperationException.class, () -> builder.interceptors().add(new TestInterceptor()));
    }


    @Test
    void testBuild() {
        RestClientBuilder builder = wrapWithBasicData(new RestClientBuilder());
        builder.addEncoder(new TestEncoder());
        builder.addDecoder(new TestDecoder());
        builder.addInterceptor(new TestInterceptor());
        builder.build();
    }

    @Test
    void testCopy() {
        RestClientBuilder builder = wrapWithBasicData(new RestClientBuilder());
        Encoder encoder = new TestEncoder();
        builder.addEncoder(encoder);
        Decoder decoder = new TestDecoder();
        builder.addDecoder(decoder);
        Interceptor interceptor = new TestInterceptor();
        builder.addInterceptor(interceptor);
        testCopied(builder, builder.copy());
    }

    static void testCopied(RestClientBuilder builder, RestClientBuilder copiedBuilder) {
        then(copiedBuilder.resolver()).isSameAs(builder.resolver());
        then(copiedBuilder.ish2ClearTextUpgrade()).isEqualTo(builder.ish2ClearTextUpgrade());
        then(copiedBuilder.connectTimeout()).isEqualTo(builder.connectTimeout());
        then(copiedBuilder.readTimeout()).isEqualTo(builder.readTimeout());
        then(copiedBuilder.isKeepAlive()).isEqualTo(builder.isKeepAlive());
        then(copiedBuilder.version()).isSameAs(builder.version());
        then(copiedBuilder.connectionPoolSize()).isEqualTo(builder.connectionPoolSize());
        then(copiedBuilder.connectionPoolWaitingQueueLength()).isEqualTo(builder.connectionPoolWaitingQueueLength());
        then(copiedBuilder.isUseDecompress()).isEqualTo(builder.isUseDecompress());
        then(copiedBuilder.decompression()).isSameAs(builder.decompression());
        then(copiedBuilder.isUseExpectContinue()).isEqualTo(builder.isUseExpectContinue());
        then(copiedBuilder.channelPoolOptionsProvider()).isSameAs(builder.channelPoolOptionsProvider());
        then(copiedBuilder.netOptions()).isNotSameAs(builder.netOptions());
        then(copiedBuilder.http1Options()).isNotSameAs(builder.http1Options());
        then(copiedBuilder.http2Options()).isNotSameAs(builder.http2Options());
        then(copiedBuilder.retryOptions()).isNotSameAs(builder.retryOptions());
        then(copiedBuilder.maxRedirects()).isEqualTo(builder.maxRedirects());
        then(copiedBuilder.maxContentLength()).isEqualTo(builder.maxContentLength());
        then(copiedBuilder.idleTimeoutSeconds()).isEqualTo(builder.idleTimeoutSeconds());
        then(builder.encoders()).isNotSameAs(copiedBuilder.encoders());
        then(builder.decoders()).isNotSameAs(copiedBuilder.decoders());
        then(builder.interceptors()).isNotSameAs(copiedBuilder.interceptors());
        assertEquals(1, copiedBuilder.encoders().size());
        assertEquals(builder.encoders().get(0), copiedBuilder.encoders().get(0));
        assertEquals(1, copiedBuilder.decoders().size());
        assertEquals(builder.decoders().get(0), copiedBuilder.decoders().get(0));
        assertEquals(1, copiedBuilder.interceptors().size());
        assertEquals(builder.interceptors().get(0), copiedBuilder.interceptors().get(0));
    }


    static RestClientBuilder wrapWithBasicData(RestClientBuilder builder) {
        return builder
                .resolver(resolver)
                .h2ClearTextUpgrade(h2ClearTextUpgrade)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .keepAlive(keepAlive)
                .version(version)
                .connectionPoolSize(connectionPoolSize)
                .connectionPoolWaitingQueueLength(connectionPoolWaitQueueSize)
                .useDecompress(useDecompress)
                .decompression(decompression)
                .useExpectContinue(useExpectContinue)
                .channelPoolOptionsProvider(channelPoolOptionsProvider)
                .netOptions(netOptions)
                .http1Options(http1Options)
                .http2Options(http2Options)
                .retryOptions(retryOptions)
                .sslOptions(sslOptions)
                .maxRedirects(maxRedirects)
                .maxContentLength(maxContentLength)
                .idleTimeoutSeconds(idleTimeoutSeconds);
    }

    static void assertBasicData(final RestClientBuilder builder, boolean fromHttpClientBuilder) {
        then(builder.resolver()).isSameAs(resolver);
        then(builder.ish2ClearTextUpgrade()).isEqualTo(h2ClearTextUpgrade);
        then(builder.connectTimeout()).isEqualTo(connectTimeout);
        then(builder.readTimeout()).isEqualTo(readTimeout);
        then(builder.isKeepAlive()).isEqualTo(keepAlive);
        then(builder.version()).isSameAs(version);
        then(builder.connectionPoolSize()).isEqualTo(connectionPoolSize);
        then(builder.connectionPoolWaitingQueueLength()).isEqualTo(connectionPoolWaitQueueSize);
        then(builder.isUseDecompress()).isEqualTo(useDecompress);
        then(builder.decompression()).isSameAs(decompression);
        then(builder.isUseExpectContinue()).isEqualTo(useExpectContinue);
        then(builder.channelPoolOptionsProvider()).isSameAs(channelPoolOptionsProvider);
        if (fromHttpClientBuilder) {
            then(builder.netOptions()).isNotSameAs(netOptions);
            then(builder.sslOptions()).isNotSameAs(sslOptions);
            then(builder.http1Options()).isNotSameAs(http1Options);
            then(builder.http2Options()).isNotSameAs(http2Options);
            then(builder.retryOptions()).isNotSameAs(retryOptions);
        } else {
            then(builder.netOptions()).isSameAs(netOptions);
            then(builder.sslOptions()).isSameAs(sslOptions);
            then(builder.http1Options()).isSameAs(http1Options);
            then(builder.http2Options()).isSameAs(http2Options);
            then(builder.retryOptions()).isSameAs(retryOptions);
        }
        then(builder.maxRedirects()).isEqualTo(maxRedirects);
        then(builder.maxContentLength()).isEqualTo(maxContentLength);
        then(builder.idleTimeoutSeconds()).isEqualTo(idleTimeoutSeconds);
    }

    static HttpClientBuilder wrapWithBasicData(HttpClientBuilder builder) {
        return builder
                .resolver(resolver)
                .h2ClearTextUpgrade(h2ClearTextUpgrade)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .keepAlive(keepAlive)
                .version(version)
                .connectionPoolSize(connectionPoolSize)
                .connectionPoolWaitingQueueLength(connectionPoolWaitQueueSize)
                .useDecompress(useDecompress)
                .decompression(decompression)
                .useExpectContinue(useExpectContinue)
                .channelPoolOptionsProvider(channelPoolOptionsProvider)
                .netOptions(netOptions)
                .http1Options(http1Options)
                .http2Options(http2Options)
                .sslOptions(sslOptions)
                .retryOptions(retryOptions)
                .maxRedirects(maxRedirects)
                .maxContentLength(maxContentLength)
                .idleTimeoutSeconds(idleTimeoutSeconds);
    }

    static class TestDecoder implements Decoder {

        @Override
        public boolean canDecode(Class type, Type genericType, MediaType mediaType) {
            return false;
        }

        @Override
        public Object decode(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, Buffer buffer) {
            return null;
        }
    }

    static class TestEncoder implements Encoder {

        @Override
        public boolean canEncode(Class type, Type genericType, MediaType mediaType) {
            return false;
        }

        @Override
        public Buffer encode(Object entity, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
            return null;
        }
    }

    static class TestInterceptor implements Interceptor {
        @Override
        public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request, InvokeChain next) {
            return null;
        }
    }

}
