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
    private final RestClientConfig clientConfig;

    public RestResponseBaseImpl(
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
                clientConfig,
                type,
                mediaType,
                ResponseBodyContent.of(response.body().getByteBuf().array()));
        return (T) decodeContext.proceed();
    }

}
