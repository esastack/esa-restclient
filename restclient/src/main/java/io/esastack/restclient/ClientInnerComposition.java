package io.esastack.restclient;

import io.esastack.httpclient.core.HttpClient;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.exec.ClientInterceptor;
import io.esastack.restclient.exec.RestRequestExecutor;

public interface ClientInnerComposition {
    ClientInterceptor[] interceptors();

    DecodeAdvice[] decodeAdvices();

    EncodeAdvice[] encodeAdvices();

    Decoder[] decoders();

    Encoder[] encoders();

    RestRequestExecutor requestExecutor();

    HttpClient httpClient();

    RestClientOptions clientOptions();
}
