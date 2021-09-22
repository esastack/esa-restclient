package esa.restclient;

import esa.commons.http.HttpVersion;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.codec.DecodeAdvice;
import esa.restclient.codec.DecoderSelector;
import esa.restclient.codec.EncodeAdvice;
import esa.restclient.exec.Interceptor;

import java.util.List;

/**
 * RestClientConfig is designed for wrapping the configuration of restClient
 */
// TODO: XxxConfig（XxxOptions）命名风格？该类的作用
public interface RestClientConfig {

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

    List<Interceptor> interceptors();

    RetryOptions retryOptions();

    int maxRedirects();

    ChannelPoolOptionsProvider channelPoolOptionsProvider();

    DecoderSelector[] unmodifiableDecoderSelectors();

    EncodeAdvice[] unmodifiableEncodeAdvices();

    DecodeAdvice[] unmodifiableDecodeAdvices();
}
