package esa.restclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.core.codec.BodyReader;
import esa.restclient.core.codec.BodyWriter;
import esa.restclient.core.exec.InvokeChain;
import esa.restclient.core.interceptor.Interceptor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        assertThrows(NullPointerException.class, () -> builder.addBodyReader(null));
        assertThrows(NullPointerException.class, () -> builder.addBodyReaders(null));
        assertThrows(NullPointerException.class, () -> builder.addBodyWriter(null));
        assertThrows(NullPointerException.class, () -> builder.addBodyWriters(null));
        assertThrows(UnsupportedOperationException.class, () -> builder.bodyReaders().add(new TestBodyReader()));
        builder.addBodyReader(new TestBodyReader());
        assertEquals(1, builder.bodyReaders().size());
        builder.addBodyReader(new TestBodyReader());
        assertEquals(2, builder.bodyReaders().size());
        List<BodyReader> bodyReaders = Arrays.asList(new TestBodyReader[]{new TestBodyReader(), new TestBodyReader()});
        builder.addBodyReaders(bodyReaders);
        assertEquals(4, builder.bodyReaders().size());
        assertThrows(UnsupportedOperationException.class, () -> builder.bodyReaders().add(new TestBodyReader()));

        assertThrows(UnsupportedOperationException.class, () -> builder.bodyWriters().add(new TestBodyWriter()));
        builder.addBodyWriter(new TestBodyWriter());
        assertEquals(1, builder.bodyWriters().size());
        builder.addBodyWriter(new TestBodyWriter());
        assertEquals(2, builder.bodyWriters().size());
        List<BodyWriter> bodyWriters = Arrays.asList(new TestBodyWriter[]{new TestBodyWriter(), new TestBodyWriter()});
        builder.addBodyWriters(bodyWriters);
        assertEquals(4, builder.bodyWriters().size());
        assertThrows(UnsupportedOperationException.class, () -> builder.bodyWriters().add(new TestBodyWriter()));
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
        builder.addBodyWriter(new TestBodyWriter());
        builder.addBodyReader(new TestBodyReader());
        builder.addInterceptor(new TestInterceptor());
        builder.build();
    }

    @Test
    void testCopy() {
        RestClientBuilder builder = wrapWithBasicData(new RestClientBuilder());
        BodyWriter bodyWriter = new TestBodyWriter();
        builder.addBodyWriter(bodyWriter);
        BodyReader bodyReader = new TestBodyReader();
        builder.addBodyReader(bodyReader);
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
        then(builder.bodyWriters()).isNotSameAs(copiedBuilder.bodyWriters());
        then(builder.bodyReaders()).isNotSameAs(copiedBuilder.bodyReaders());
        then(builder.interceptors()).isNotSameAs(copiedBuilder.interceptors());
        assertEquals(1, copiedBuilder.bodyWriters().size());
        assertEquals(builder.bodyWriters().get(0), copiedBuilder.bodyWriters().get(0));
        assertEquals(1, copiedBuilder.bodyReaders().size());
        assertEquals(builder.bodyReaders().get(0), copiedBuilder.bodyReaders().get(0));
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

    static class TestBodyReader implements BodyReader {
        @Override
        public boolean canRead(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
            //TODO implement the method!
            throw new UnsupportedOperationException("The method need to be implemented!");
        }

        @Override
        public Object read(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) throws IOException {
            //TODO implement the method!
            throw new UnsupportedOperationException("The method need to be implemented!");
        }
    }

    static class TestBodyWriter implements BodyWriter {

        @Override
        public boolean canWrite(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
            //TODO implement the method!
            throw new UnsupportedOperationException("The method need to be implemented!");
        }

        @Override
        public void write(Object entity, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, OutputStream bodyStream) throws IOException {
            //TODO implement the method!
            throw new UnsupportedOperationException("The method need to be implemented!");
        }
    }

    static class TestInterceptor implements Interceptor {
        @Override
        public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request, InvokeChain next) {
            return null;
        }
    }

}
