/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restclient;

import io.esastack.commons.net.http.HttpVersion;
import io.esastack.httpclient.core.config.Decompression;
import io.esastack.httpclient.core.config.Http1Options;
import io.esastack.httpclient.core.config.Http2Options;
import io.esastack.httpclient.core.config.NetOptions;
import io.esastack.httpclient.core.config.RetryOptions;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.resolver.HostResolver;
import io.esastack.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.esastack.restclient.codec.ByteDecoder;
import io.esastack.restclient.codec.ByteEncoder;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeAdviceContext;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeAdviceContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.exec.RestInterceptor;
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
        then(builder.isH2ClearTextUpgrade()).isTrue();

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
        builder.addEncoder(createByteEncoder(-10000));
        builder.addDecoder(createByteDecoder(-10000));

        isEqual(builder, builder.copy());
        isEqual(builder, builder.build().clientOptions());
    }

    void isEqual(RestClientOptions origin, RestClientOptions other) {
        then(origin.channelPoolOptionsProvider()).isEqualTo(other.channelPoolOptionsProvider());
        then(origin.connectionPoolSize()).isEqualTo(other.connectionPoolSize());
        then(origin.connectionPoolWaitingQueueLength()).isEqualTo(other.connectionPoolWaitingQueueLength());
        then(origin.connectTimeout()).isEqualTo(other.connectTimeout());
        then(origin.decompression()).isEqualTo(other.decompression());
        then(origin.isH2ClearTextUpgrade()).isEqualTo(other.isH2ClearTextUpgrade());
        then(origin.http1Options().maxChunkSize()).isEqualTo(other.http1Options().maxChunkSize());
        then(origin.http1Options().maxHeaderSize()).isEqualTo(other.http1Options().maxHeaderSize());
        then(origin.http1Options().maxInitialLineLength()).isEqualTo(other.http1Options().maxInitialLineLength());
        then(origin.http2Options().maxFrameSize()).isEqualTo(other.http2Options().maxFrameSize());
        then(origin.http2Options().gracefulShutdownTimeoutMillis())
                .isEqualTo(other.http2Options().gracefulShutdownTimeoutMillis());
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

        then(origin.unmodifiableDecodeAdvices().size()).isEqualTo(other.unmodifiableDecodeAdvices().size());
        then(origin.unmodifiableEncodeAdvices().size()).isEqualTo(other.unmodifiableEncodeAdvices().size());
        then(origin.unmodifiableInterceptors().size()).isEqualTo(other.unmodifiableInterceptors().size());

        then(origin.unmodifiableDecodeAdvices().get(0)).isEqualTo(other.unmodifiableDecodeAdvices().get(0));
        then(origin.unmodifiableEncodeAdvices().get(0)).isEqualTo(other.unmodifiableEncodeAdvices().get(0));
        then(origin.unmodifiableInterceptors().get(0)).isEqualTo(other.unmodifiableInterceptors().get(0));
        then(origin.unmodifiableEncoders().get(0)).isEqualTo(other.unmodifiableEncoders().get(0));
        then(origin.unmodifiableDecoders().get(0)).isEqualTo(other.unmodifiableDecoders().get(0));
    }

    @Test
    void testAddEncoder() {
        RestClientBuilder builder = new RestClientBuilder();
        ByteEncoder encoder1 = createByteEncoder(3);
        builder.addByteEncoder(encoder1);
        ByteEncoder encoder2 = createByteEncoder(-1);
        ByteEncoder encoder3 = createByteEncoder(1);
        List<ByteEncoder> encoders = Arrays.asList(encoder2, encoder3);
        builder.addByteEncoders(encoders);

        List<Encoder> orderedEncoders = builder.unmodifiableEncoders();
        then(orderedEncoders.size()).isEqualTo(3);
        //unmodifiableEncoders().size() = encoders added(3) + encoders from spi(6)
        then(builder.build().clientOptions().unmodifiableEncoders().size()).isEqualTo(9);
        then(builder.build().clientOptions().unmodifiableEncoders().get(2)).isEqualTo(encoder2);
    }

    @Test
    void testAddDecoder() {
        RestClientBuilder builder = new RestClientBuilder();
        ByteDecoder decoder1 = createByteDecoder(3);
        builder.addByteDecoder(decoder1);
        ByteDecoder decoder2 = createByteDecoder(-1);
        ByteDecoder decoder3 = createByteDecoder(1);
        List<ByteDecoder> decoders = Arrays.asList(decoder2, decoder3);
        builder.addByteDecoders(decoders);

        List<Decoder> orderedDecoders = builder.unmodifiableDecoders();
        then(orderedDecoders.size()).isEqualTo(3);
        //unmodifiableDecoders().size() = decoders added(3) + decoders from spi(3)
        then(builder.build().clientOptions().unmodifiableDecoders().size()).isEqualTo(6);
        then(builder.build().clientOptions().unmodifiableDecoders().get(2)).isEqualTo(decoder2);
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

        List<DecodeAdvice> orderedDecodeAdvices = builder.unmodifiableDecodeAdvices();
        then(orderedDecodeAdvices.size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableDecodeAdvices().size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableDecodeAdvices().get(0)).isEqualTo(decodeAdvice2);
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

        List<EncodeAdvice> orderedEncodeAdvices = builder.unmodifiableEncodeAdvices();
        then(orderedEncodeAdvices.size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableEncodeAdvices().size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableEncodeAdvices().get(0)).isEqualTo(encodeAdvice2);
    }

    @Test
    void testAddInterceptor() {
        RestClientBuilder builder = new RestClientBuilder();
        RestInterceptor interceptor1 = createInterceptor(3);
        builder.addInterceptor(interceptor1);
        RestInterceptor interceptor2 = createInterceptor(-1);
        RestInterceptor interceptor3 = createInterceptor(1);
        List<RestInterceptor> interceptors = Arrays.asList(interceptor2, interceptor3);
        builder.addInterceptors(interceptors);

        List<RestInterceptor> orderedInterceptors = builder.unmodifiableInterceptors();
        then(orderedInterceptors.size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableInterceptors().size()).isEqualTo(3);
        then(builder.build().clientOptions().unmodifiableInterceptors().get(0)).isEqualTo(interceptor2);
    }

    private ByteEncoder createByteEncoder(int order) {
        return new ByteEncoder() {
            @Override
            public RequestContent<byte[]> doEncode(EncodeContext<byte[]> ctx) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private ByteDecoder createByteDecoder(int order) {
        return new ByteDecoder() {
            @Override
            public Object doDecode(DecodeContext<byte[]> ctx) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private DecodeAdvice createDecodeAdvice(int order) {
        return new DecodeAdvice() {
            @Override
            public Object aroundDecode(DecodeAdviceContext context) {
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
            public RequestContent<?> aroundEncode(EncodeAdviceContext context) {
                return null;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private RestInterceptor createInterceptor(int order) {
        return new RestInterceptor() {

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
