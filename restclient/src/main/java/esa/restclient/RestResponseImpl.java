package esa.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpResponse;
import esa.restclient.serializer.RxSerializer;
import esa.restclient.serializer.RxSerializerAdvice;
import esa.restclient.serializer.RxSerializerSelector;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public class RestResponseImpl implements RestResponse {

    private final RestRequest request;
    private final HttpResponse response;
    private final RestClientConfig clientConfig;

    public RestResponseImpl(
            RestRequest request,
            HttpResponse response,
            RestClientConfig clientConfig) {
        Checks.checkNotNull(request, "Request must be not null!");
        Checks.checkNotNull(response, "Response must be not null!");
        Checks.checkNotNull(clientConfig, "ClientConfig must be not null!");
        this.request = request;
        this.response = response;
        this.clientConfig = clientConfig;
    }

    @Override
    public int status() {
        return response.status();
    }

    @Override
    public Buffer body() {
        return response.body();
    }

    @Override
    public HttpHeaders trailers() {
        return response.trailers();
    }

    @Override
    public HttpVersion version() {
        return response.version();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public <T> T bodyToEntity(Class<T> entityClass) throws Exception {
        return bodyToEntity((Type) entityClass);
    }

    @Override
    public <T> T bodyToEntity(Type type) throws Exception {
        final String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType = null;
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            mediaType = MediaType.parseMediaType(mediaTypeValue);
        }

        final ContentType[] acceptTypes = request.acceptTypes();
        final HttpHeaders headers = response.headers();

        final RxSerializerSelector[] rxSerializerSelectors = clientConfig.unmodifiableRxSerializerSelectors();
        for (RxSerializerSelector rxSerializerSelector : rxSerializerSelectors) {
            RxSerializer rxSerializer = rxSerializerSelector.select(request, acceptTypes, mediaType, headers, type);
            if (rxSerializer != null) {
                return deSerialize(rxSerializer, mediaType,
                        headers, response.body().getByteBuf().array(), type);
            }
        }
        throw new IllegalStateException("There is no suitable rxSerializer for this response," +
                "Please set correct acceptType and rxSerializerSelector!" +
                "request.uri: " + request.uri() +
                ",response.status: " + response.status() +
                ",response.headers: " + headers);

    }

    @SuppressWarnings("unchecked")
    private <T> T deSerialize(RxSerializer rxSerializer, MediaType mediaType,
                              HttpHeaders headers, byte[] data, Type type) throws Exception {
        for (RxSerializerAdvice rxSerializerAdvice : clientConfig.unmodifiableRxSerializeAdvices()) {
            rxSerializerAdvice.beforeDeSerialize(request, this, type);
        }

        Object entity = rxSerializer.deSerialize(mediaType, headers, response.body().getByteBuf().array(), type);
        for (RxSerializerAdvice rxSerializerAdvice : clientConfig.unmodifiableRxSerializeAdvices()) {
            entity = rxSerializerAdvice.afterDeSerialize(request, this, type, entity);
        }

        return (T) entity;
    }
}
