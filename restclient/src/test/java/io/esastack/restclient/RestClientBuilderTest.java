package io.esastack.restclient;

import esa.commons.http.HttpVersion;
import io.esastack.httpclient.core.config.Decompression;
import io.esastack.httpclient.core.config.Http1Options;
import io.esastack.httpclient.core.config.Http2Options;
import io.esastack.httpclient.core.config.NetOptions;
import io.esastack.httpclient.core.config.RetryOptions;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.resolver.HostResolver;
import io.esastack.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.exec.ClientInterceptor;
import io.esastack.restclient.exec.InvocationChain;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.BDDAssertions.then;

class RestClientBuilderTest {

    @Test
    void testBasic() {
        RestClientBuilder builder = new RestClientBuilder();
        ChannelPoolOptionsProvider channelPoolOptionsProvider = key -> null;
        builder.channelPoolOptionsProvider(channelPoolOptionsProvider);
        then(builder.channelPoolOptionsProvider()).isEqualTo(channelPoolOptionsProvider);

        builder.connectionPoolSize(5);
        then(builder.connectionPoolSize()).isEqualTo(5);

        builder.connectionPoolWaitingQueueLength(5);
        then(builder.connectionPoolWaitingQueueLength()).isEqualTo(5);

        builder.connectTimeout(5);
        then(builder.connectTimeout()).isEqualTo(5);

        builder.decompression(Decompression.GZIP_DEFLATE);
        then(builder.decompression()).isEqualTo(Decompression.GZIP_DEFLATE);

        builder.h2ClearTextUpgrade(true);
        then(builder.ish2ClearTextUpgrade()).isTrue();

        Http1Options http1Options = Http1Options.options()
                .maxChunkSize(2)
                .maxInitialLineLength(3)
                .build();
        builder.http1Options(http1Options);
        then(builder.http1Options()).isEqualTo(http1Options);

        Http2Options http2Options = Http2Options.options()
                .maxFrameSize(16386)
                .maxReservedStreams(1)
                .build();
        builder.http2Options(http2Options);
        then(builder.http2Options()).isEqualTo(http2Options);

        builder.idleTimeoutSeconds(5);
        then(builder.idleTimeoutSeconds()).isEqualTo(5);

        builder.keepAlive(false);
        then(builder.isKeepAlive()).isFalse();

        builder.maxContentLength(5);
        then(builder.maxContentLength()).isEqualTo(5);

        builder.maxRedirects(5);
        then(builder.maxRedirects()).isEqualTo(5);

        NetOptions netOptions = NetOptions.options()
                .soKeepAlive(true)
                .build();
        builder.netOptions(netOptions);
        then(builder.netOptions()).isEqualTo(netOptions);

        builder.readTimeout(5);
        then(builder.readTimeout()).isEqualTo(5);

        HostResolver resolver = inetHost -> null;
        builder.resolver(resolver);
        then(builder.resolver()).isEqualTo(resolver);

        RetryOptions retryOptions = RetryOptions.options()
                .maxRetries(3)
                .build();
        builder.retryOptions(retryOptions);
        then(builder.retryOptions()).isEqualTo(retryOptions);

        SslOptions sslOptions = SslOptions.options()
                .sessionCacheSize(10)
                .build();
        builder.sslOptions(sslOptions);
        then(builder.sslOptions()).isEqualTo(sslOptions);

        builder.version(HttpVersion.HTTP_1_0);
        then(builder.version()).isEqualTo(HttpVersion.HTTP_1_0);

        builder.useDecompress(true);
        then(builder.isUseDecompress()).isTrue();

        builder.useExpectContinue(true);
        then(builder.isUseExpectContinue()).isTrue();

        builder.addInterceptor(createInterceptor(2));
        builder.addDecodeAdvice(createDecodeAdvice(2));
        builder.addEncodeAdvice(createEncodeAdvice(2));

        isEqual(builder, builder.copy());
        isEqual(builder, builder.build().clientOptions());
    }

    void isEqual(RestClientOptions origin, RestClientOptions other) {
        then(origin.channelPoolOptionsProvider()).isEqualTo(other.channelPoolOptionsProvider());
        then(origin.connectionPoolSize()).isEqualTo(other.connectionPoolSize());
        then(origin.connectionPoolWaitingQueueLength()).isEqualTo(other.connectionPoolWaitingQueueLength());
        then(origin.connectTimeout()).isEqualTo(other.connectTimeout());
        then(origin.decompression()).isEqualTo(other.decompression());
        then(origin.ish2ClearTextUpgrade()).isEqualTo(other.ish2ClearTextUpgrade());
        then(origin.http1Options().maxChunkSize()).isEqualTo(other.http1Options().maxChunkSize());
        then(origin.http1Options().maxHeaderSize()).isEqualTo(other.http1Options().maxHeaderSize());
        then(origin.http1Options().maxInitialLineLength()).isEqualTo(other.http1Options().maxInitialLineLength());
        then(origin.http2Options().maxFrameSize()).isEqualTo(other.http2Options().maxFrameSize());
        then(origin.http2Options().gracefulShutdownTimeoutMillis()).isEqualTo(other.http2Options().gracefulShutdownTimeoutMillis());
        then(origin.idleTimeoutSeconds()).isEqualTo(other.idleTimeoutSeconds());
        then(origin.isKeepAlive()).isEqualTo(other.isKeepAlive());
        then(origin.maxContentLength()).isEqualTo(other.maxContentLength());
        then(origin.maxRedirects()).isEqualTo(other.maxRedirects());
        then(origin.netOptions().isSoKeepAlive()).isEqualTo(other.netOptions().isSoKeepAlive());
        then(origin.netOptions().isSoReuseAddr()).isEqualTo(other.netOptions().isSoReuseAddr());
        then(origin.netOptions().isTcpNoDelay()).isEqualTo(other.netOptions().isTcpNoDelay());
        then(origin.netOptions().soLinger()).isEqualTo(other.netOptions().soLinger());
        then(origin.readTimeout()).isEqualTo(other.readTimeout());
        then(origin.resolver()).isEqualTo(other.resolver());
        then(origin.retryOptions().maxRetries()).isEqualTo(other.retryOptions().maxRetries());
        then(origin.sslOptions().sessionTimeout()).isEqualTo(other.sslOptions().sessionTimeout());
        then(origin.version()).isEqualTo(other.version());
        then(origin.isUseDecompress()).isEqualTo(other.isUseDecompress());
        then(origin.isUseExpectContinue()).isEqualTo(other.isUseExpectContinue());
        then(origin.unmodifiableDecodeAdvices()[0]).isEqualTo(other.unmodifiableDecodeAdvices()[0]);
        then(origin.unmodifiableEncodeAdvices()[0]).isEqualTo(other.unmodifiableEncodeAdvices()[0]);
        then(origin.unmodifiableInterceptors()[0]).isEqualTo(other.unmodifiableInterceptors()[0]);
    }

    @Test
    void testAddDecodeAdvice() {
        RestClientBuilder builder = new RestClientBuilder();
        DecodeAdvice decodeAdvice1 = createDecodeAdvice(3);
        builder.addDecodeAdvice(decodeAdvice1);
        DecodeAdvice decodeAdvice2 = createDecodeAdvice(-1);
        DecodeAdvice decodeAdvice3 = createDecodeAdvice(1);
        List<DecodeAdvice> decodeAdvices = Arrays.asList(decodeAdvice2, decodeAdvice3);
        builder.addDecodeAdvices(decodeAdvices);

        DecodeAdvice[] orderedDecodeAdvices = builder.unmodifiableDecodeAdvices();
        then(orderedDecodeAdvices[0]).isEqualTo(decodeAdvice2);
        then(orderedDecodeAdvices[1]).isEqualTo(decodeAdvice3);
        then(orderedDecodeAdvices[2]).isEqualTo(decodeAdvice1);
    }

    @Test
    void testAddEncodeAdvice() {
        RestClientBuilder builder = new RestClientBuilder();
        EncodeAdvice encodeAdvice1 = createEncodeAdvice(3);
        builder.addEncodeAdvice(encodeAdvice1);
        EncodeAdvice encodeAdvice2 = createEncodeAdvice(-1);
        EncodeAdvice encodeAdvice3 = createEncodeAdvice(1);
        List<EncodeAdvice> encodeAdvices = Arrays.asList(encodeAdvice2, encodeAdvice3);
        builder.addEncodeAdvices(encodeAdvices);

        EncodeAdvice[] orderedEncodeAdvices = builder.unmodifiableEncodeAdvices();
        then(orderedEncodeAdvices[0]).isEqualTo(encodeAdvice2);
        then(orderedEncodeAdvices[1]).isEqualTo(encodeAdvice3);
        then(orderedEncodeAdvices[2]).isEqualTo(encodeAdvice1);
    }

    @Test
    void testAddInterceptor() {
        RestClientBuilder builder = new RestClientBuilder();
        ClientInterceptor interceptor1 = createInterceptor(3);
        builder.addInterceptor(interceptor1);
        ClientInterceptor interceptor2 = createInterceptor(-1);
        ClientInterceptor interceptor3 = createInterceptor(1);
        List<ClientInterceptor> interceptors = Arrays.asList(interceptor2, interceptor3);
        builder.addInterceptors(interceptors);

        ClientInterceptor[] orderedInterceptors = builder.unmodifiableInterceptors();
        then(orderedInterceptors[0]).isEqualTo(interceptor2);
        then(orderedInterceptors[1]).isEqualTo(interceptor3);
        then(orderedInterceptors[2]).isEqualTo(interceptor1);
    }

    private DecodeAdvice createDecodeAdvice(int order) {
        return new DecodeAdvice() {
            @Override
            public Object aroundDecode(DecodeContext context) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private EncodeAdvice createEncodeAdvice(int order) {
        return new EncodeAdvice() {
            @Override
            public RequestBodyContent<?> aroundEncode(EncodeContext context) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private ClientInterceptor createInterceptor(int order) {
        return new ClientInterceptor() {

            @Override
            public CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }
}
