package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.GenericType;
import io.esastack.restclient.codec.impl.DecodeContextImpl;
import io.esastack.restclient.utils.CookiesUtil;

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T bodyToEntity(Class<T> entityClass) throws Exception {
        final DecodeContext decodeContext = new DecodeContextImpl(
                request,
                this,
                clientOptions,
                entityClass,
                entityClass,
                response.body().getByteBuf());
        return (T) decodeContext.proceed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T bodyToEntity(GenericType<T> genericType) throws Exception {
        final DecodeContext decodeContext = new DecodeContextImpl(
                request,
                this,
                clientOptions,
                genericType.getRawType(),
                genericType.getType(),
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
