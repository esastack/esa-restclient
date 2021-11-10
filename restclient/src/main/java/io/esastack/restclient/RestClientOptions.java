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
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.exec.ClientInterceptor;

import java.util.List;

/**
 * RestClientOptions is designed for wrapping the configuration of restClient
 */
public interface RestClientOptions {

    String name();

    HostResolver resolver();

    boolean isH2ClearTextUpgrade();

    int connectTimeout();

    long readTimeout();

    long maxContentLength();

    int idleTimeoutSeconds();

    boolean isKeepAlive();

    HttpVersion version();

    int connectionPoolSize();

    int connectionPoolWaitingQueueLength();

    boolean isUseDecompress();

    Decompression decompression();

    boolean isUseExpectContinue();

    SslOptions sslOptions();

    NetOptions netOptions();

    Http1Options http1Options();

    Http2Options http2Options();

    RetryOptions retryOptions();

    int maxRedirects();

    ChannelPoolOptionsProvider channelPoolOptionsProvider();

    List<EncodeAdvice> unmodifiableEncodeAdvices();

    List<DecodeAdvice> unmodifiableDecodeAdvices();

    List<Encoder<?>> unmodifiableEncoders();

    List<Decoder<?>> unmodifiableDecoders();

    List<ClientInterceptor> unmodifiableInterceptors();
}
