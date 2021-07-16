package esa.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpResponse;
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

        String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType = null;
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            mediaType = MediaType.parseMediaType(mediaTypeValue);
            ContentType[] acceptTypes = request.acceptTypes();
            if (acceptTypes != null && acceptTypes.length > 0) {
                for (ContentType contentType : acceptTypes) {
                    if (contentType.getMediaType().includes(mediaType)) {
                        return contentType;
                    }
                }
            }
        }

        ContentTypeResolver contentTypeResolver = request.contentTypeResolver();
        if (contentTypeResolver != null) {
            ContentType contentType = contentTypeResolver.resolve(request, mediaType, response.headers(), type);
            if (contentType != null) {
                return contentType;
            }
        }

        ContentTypeResolver[] contentTypeResolvers = clientConfig.unmodifiableContentTypeResolvers();
        for (ContentTypeResolver contentTypeResolverTem : contentTypeResolvers) {
            ContentType contentType = contentTypeResolverTem.resolve(request, mediaType, response.headers(), type);
            if (contentType != null) {
                return contentType;
            }
        }

        throw new IllegalStateException("Can,t resolve contentType of response!" +
                "request.uri: " + request.uri() +
                ",response.status: " + response.status() +
                ",response.headers: " + response.headers());
    }
}
