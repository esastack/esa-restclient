package esa.restclient;

import esa.commons.http.HttpVersion;
import esa.httpclient.core.config.Decompression;
import esa.httpclient.core.config.Http1Options;
import esa.httpclient.core.config.Http2Options;
import esa.httpclient.core.config.NetOptions;
import esa.httpclient.core.config.RetryOptions;
import esa.httpclient.core.config.SslOptions;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.codec.DecodeAdvice;
import esa.restclient.codec.DecoderSelector;
import esa.restclient.codec.EncodeAdvice;
import esa.restclient.exec.ClientInterceptor;

import java.util.List;

/**
 * RestClientOptions is designed for wrapping the configuration of restClient
 */
public interface RestClientOptions {

    HostResolver resolver();

    boolean ish2ClearTextUpgrade();

    int connectTimeout();

    int readTimeout();

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

    List<ClientInterceptor> interceptors();

    RetryOptions retryOptions();

    int maxRedirects();

    ChannelPoolOptionsProvider channelPoolOptionsProvider();

    DecoderSelector[] unmodifiableDecoderSelectors();

    EncodeAdvice[] unmodifiableEncodeAdvices();

    DecodeAdvice[] unmodifiableDecodeAdvices();
}
