package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpResponse;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class RestResponseImpl implements RestResponse {

    private final RestRequest request;
    private final HttpResponse response;

    public RestResponseImpl(
            RestRequest request,
            HttpResponse response) {
        Checks.checkNotNull(request, "Request must be not null!");
        Checks.checkNotNull(response, "Response must be not null!");
        this.request = request;
        this.response = response;
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
        return resolveContentType(request, response, entityClass)
                .rxSerializer()
                .deSerialize(response.body().getByteBuf().array(), entityClass);
    }

    @Override
    public <T> T bodyToEntity(Type type) throws Exception {
        return resolveContentType(request, response, type)
                .rxSerializer()
                .deSerialize(response.body().getByteBuf().array(), type);
    }

    private ContentType resolveContentType(RestRequest request, HttpResponse response, Type type) {
        ResponseContentTypeResolver contentTypeResolver = request.responseContentTypeResolver();
        if (contentTypeResolver == null) {
            throw new IllegalStateException("ResponseContentTypeResolver must not be null!" +
                    "Please set correct contentTypeResolver to request or client!");
        }
        Optional<ContentType> contentType = contentTypeResolver.resolve(request, response.headers(), type);
        if (contentType.isPresent()) {
            return contentType.get();
        }
        throw new IllegalStateException("Can,t resolve contentType of response!" +
                "response.status: " + response.status() +
                ",response.headers: " + response.headers());
    }

}
