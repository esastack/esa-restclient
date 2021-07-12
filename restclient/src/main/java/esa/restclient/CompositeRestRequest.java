package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.httpclient.core.HttpRequestFacade;
import esa.restclient.exec.RestRequestExecutor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CompositeRestRequest extends AbstractExecutableRestRequest implements RestRequestFacade, RestEntityRequest {

    private Object body;

    public CompositeRestRequest(HttpRequestFacade request, RestClientConfig clientConfig, RestRequestExecutor requestExecutor) {
        super(request, clientConfig, requestExecutor);
    }

    @Override
    public CompositeRestRequest removeHeader(CharSequence name) {
        super.removeHeader(name);
        return self();
    }

    @Override
    public CompositeRestRequest readTimeout(int readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public CompositeRestRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public CompositeRestRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public CompositeRestRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public CompositeRestRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public CompositeRestRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public CompositeRestRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public CompositeRestRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public CompositeRestRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public CompositeRestRequest cookies(List<Cookie> cookies) {
        super.cookies(cookies);
        return self();
    }

    @Override
    public CompositeRestRequest contentType(ContentType contentType) {
        super.contentType(contentType);
        return self();
    }

    @Override
    public CompositeRestRequest contentType(RequestContentTypeFactory requestContentTypeFactory) {
        super.contentType(requestContentTypeFactory);
        return self();
    }

    @Override
    public CompositeRestRequest accept(ContentType... contentTypes) {
        super.accept(contentTypes);
        return self();
    }

    @Override
    public CompositeRestRequest responseContentTypeResolver(ResponseContentTypeResolver responseContentTypeResolver) {
        super.responseContentTypeResolver(responseContentTypeResolver);
        return self();
    }

    @Override
    public CompositeRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public CompositeRestRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public CompositeRestRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public RestEntityRequest entity(Object entity) {
        Checks.checkNotNull(entity, "Entity must nor be null");
        this.body = entity;
        return self();
    }

    @Override
    public RestFileRequest file(File file) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestMultipartRequest multipart() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public Object body() {
        //TODO
        return body;
    }

    @Override
    public Object entity() {
        return body;
    }

    private CompositeRestRequest self() {
        return this;
    }
}
