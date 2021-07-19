package esa.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpResponse;
import esa.restclient.serializer.RxSerializer;
import esa.restclient.serializer.RxSerializerResolver;
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
        return computeRxSerializer(request, response, entityClass)
                .deSerialize(response.body().getByteBuf().array(), entityClass);
    }

    @Override
    public <T> T bodyToEntity(Type type) throws Exception {
        return computeRxSerializer(request, response, type)
                .deSerialize(response.body().getByteBuf().array(), type);
    }

    private RxSerializer computeRxSerializer(RestRequest request, HttpResponse response, Type type) {
        final String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType = null;
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            mediaType = MediaType.parseMediaType(mediaTypeValue);
        }

        final ContentType[] acceptTypes = request.acceptTypes();
        final HttpHeaders headers = response.headers();

        final RxSerializerResolver[] rxSerializerFactories = clientConfig.unmodifiableRxSerializerResolvers();
        for (RxSerializerResolver rxSerializerResolverTem : rxSerializerFactories) {
            RxSerializer rxSerializer = rxSerializerResolverTem.resolve(request, acceptTypes, mediaType, headers, type);
            if (rxSerializer != null) {
                return rxSerializer;
            }
        }

        throw new IllegalStateException("Can,t resolve contentType of response!" +
                "request.uri: " + request.uri() +
                ",response.status: " + response.status() +
                ",response.headers: " + headers);
    }
}
