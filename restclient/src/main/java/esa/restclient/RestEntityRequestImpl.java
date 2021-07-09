package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.CompositeRequest;
import esa.restclient.exec.RestRequestExecutor;

import java.util.List;
import java.util.Map;

public class RestEntityRequestImpl extends AbstractExecutableRestRequest implements RestEntityRequest {

    private final Object entity;

    protected RestEntityRequestImpl(CompositeRequest request, RestClientConfig clientConfig, RestRequestExecutor requestExecutor, Object entity) {
        super(request, clientConfig, requestExecutor);
        this.entity = entity;
    }

    @Override
    public RestEntityRequest readTimeout(int readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public RestEntityRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public RestEntityRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public RestEntityRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public RestEntityRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public RestEntityRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public RestEntityRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public RestEntityRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public RestEntityRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public RestEntityRequest cookies(List<Cookie> cookies) {
        super.cookies(cookies);
        return self();
    }

    @Override
    public RestEntityRequest contentType(ContentType contentType) {
        super.contentType(contentType);
        return self();
    }

    @Override
    public RestEntityRequest contentType(ContentTypeFactory contentTypeFactory) {
        super.contentType(contentTypeFactory);
        return self();
    }

    @Override
    public RestEntityRequest accept(AcceptType... acceptTypes) {
        super.accept(acceptTypes);
        return self();
    }

    @Override
    public RestEntityRequest acceptTypeResolver(AcceptTypeResolver acceptTypeResolver) {
        super.acceptTypeResolver(acceptTypeResolver);
        return self();
    }

    @Override
    public RestEntityRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public RestEntityRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public RestEntityRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public RestEntityRequest removeHeader(CharSequence name) {
        super.removeHeader(name);
        return self();
    }

    @Override
    protected ContentType defaultContentType() {
        return ContentType.APPLICATION_JSON_UTF8_JACKSON;
    }

    private RestEntityRequest self() {
        return this;
    }

    @Override
    public Object entity() {
        return entity;
    }

    @Override
    public Object body() {
        return entity;
    }
}
