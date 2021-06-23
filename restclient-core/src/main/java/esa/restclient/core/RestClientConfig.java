package esa.restclient.core;

import esa.commons.http.HttpVersion;
import esa.httpclient.core.config.*;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.restclient.core.codec.Decoder;
import esa.restclient.core.codec.Encoder;
import esa.restclient.core.interceptor.Interceptor;

import java.util.List;

public interface RestClientConfig {
    HostResolver resolver();

    boolean ish2ClearTextUpgrade();

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

    List<Interceptor> interceptors();

    List<Decoder> decoders();

    List<Encoder> encoders();

    RetryOptions retryOptions();

    int maxRedirects();

    ChannelPoolOptionsProvider channelPoolOptionsProvider();
}
