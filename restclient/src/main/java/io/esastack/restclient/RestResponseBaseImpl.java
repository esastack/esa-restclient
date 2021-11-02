package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.impl.DecodeContextImpl;
import io.esastack.restclient.utils.CookiesUtil;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RestResponseBaseImpl implements RestResponseBase {

    private final RestRequestBase request;
    private final HttpResponse response;
    private final RestClientOptions clientOptions;

    RestResponseBaseImpl(
            RestRequestBase request,
            HttpResponse response,
            RestClientOptions clientOptions) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(clientOptions, "clientOptions");
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
            mediaType = MediaTypeUtil.parseMediaType(mediaTypeValue);
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

    @Override
    public void cookie(Cookie cookie) {
        CookiesUtil.cookie(cookie, headers(), true);
    }

    @Override
    public void cookie(String name, String value) {
        CookiesUtil.cookie(name, value, headers(), true);
    }

    @Override
    public void cookies(List<Cookie> cookies) {
        CookiesUtil.cookies(cookies, headers(), true);
    }

    @Override
    public List<Cookie> removeCookies(String name) {
        return CookiesUtil.removeCookies(name, headers(), true);
    }

    @Override
    public List<Cookie> cookies(String name) {
        return CookiesUtil.getCookies(name, headers(), true);
    }

    @Override
    public Map<String, List<Cookie>> cookiesMap() {
        return CookiesUtil.getCookiesMap(headers(), true);
    }
}
