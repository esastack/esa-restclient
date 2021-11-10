package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.exec.ClientInterceptor;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.exec.RestRequestExecutorImpl;

public class ClientInnerCompositionImpl implements ClientInnerComposition {

    private final RestRequestExecutor requestExecutor;
    private final HttpClient httpClient;
    private final RestClientOptions clientOptions;
    private final ClientInterceptor[] interceptors;
    private final DecodeAdvice[] decodeAdvices;
    private final EncodeAdvice[] encodeAdvices;
    private final Decoder<?>[] decoders;
    private final Encoder<?>[] encoders;

    public ClientInnerCompositionImpl(RestClientOptions clientOptions,
                                      HttpClient httpClient) {
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(httpClient, "httpClient");
        this.httpClient = httpClient;
        this.interceptors = clientOptions.unmodifiableInterceptors().toArray(new ClientInterceptor[0]);
        this.decodeAdvices = clientOptions.unmodifiableDecodeAdvices().toArray(new DecodeAdvice[0]);
        this.encodeAdvices = clientOptions.unmodifiableEncodeAdvices().toArray(new EncodeAdvice[0]);
        this.decoders = clientOptions.unmodifiableDecoders().toArray(new Decoder[0]);
        this.encoders = clientOptions.unmodifiableEncoders().toArray(new Encoder[0]);
        this.clientOptions = clientOptions;
        this.requestExecutor = new RestRequestExecutorImpl(this);
    }

    @Override
    public ClientInterceptor[] interceptors() {
        return interceptors;
    }

    @Override
    public DecodeAdvice[] decodeAdvices() {
        return decodeAdvices;
    }

    @Override
    public EncodeAdvice[] encodeAdvices() {
        return encodeAdvices;
    }

    @Override
    public Decoder<?>[] decoders() {
        return decoders;
    }

    @Override
    public Encoder<?>[] encoders() {
        return encoders;
    }

    @Override
    public RestRequestExecutor requestExecutor() {
        return requestExecutor;
    }

    @Override
    public HttpClient httpClient() {
        return httpClient;
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientOptions;
    }
}
