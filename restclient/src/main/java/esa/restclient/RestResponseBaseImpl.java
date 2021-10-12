package esa.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpResponse;
import esa.restclient.codec.DecodeContext;
import esa.restclient.codec.DecodeContextImpl;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public class RestResponseBaseImpl implements RestResponseBase {

    private final RestRequest request;
    private final HttpResponse response;
    private final RestClientOptions clientOptions;

    public RestResponseBaseImpl(
            RestRequest request,
            HttpResponse response,
            RestClientOptions clientOptions) {
        Checks.checkNotNull(request, "Request must be not null!");
        Checks.checkNotNull(response, "Response must be not null!");
        Checks.checkNotNull(clientOptions, "ClientOptions must be not null!");
        this.request = request;
        this.response = response;
        this.clientOptions = clientOptions;
    }

    @Override
    public int status() {
        return response.status();
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T bodyToEntity(Type type) throws Exception {

        final String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType = null;
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            mediaType = MediaType.parseMediaType(mediaTypeValue);
        }

        final DecodeContext decodeContext = new DecodeContextImpl(
                request,
                this,
                clientOptions,
                type,
                mediaType,
                response.body().getByteBuf());
        return (T) decodeContext.proceed();
    }

}
