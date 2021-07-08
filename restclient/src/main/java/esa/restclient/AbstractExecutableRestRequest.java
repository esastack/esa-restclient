package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.HttpUri;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    private final CompositeRequest target;


    protected AbstractExecutableRestRequest(CompositeRequest request) {
        Checks.checkNotNull(request, "Request must not be null");
        this.target = request;
    }


    @Override
    public HttpMethod method() {
        return target.method();
    }

    @Override
    public String scheme() {
        return target.scheme();
    }

    @Override
    public String path() {
        return target.path();
    }

    @Override
    public HttpUri uri() {
        return target.uri();
    }

    @Override
    public String getParam(String name) {
        return target.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        return target.getParams(name);
    }

    @Override
    public Set<String> paramNames() {
        return target.paramNames();
    }

    @Override
    public HttpHeaders headers() {
        return target.headers();
    }

    @Override
    public CharSequence getHeader(CharSequence name) {
        return target.getHeader(name);
    }

    @Override
    public ExecutableRestRequest removeHeader(CharSequence name) {
        target.removeHeader(name);
        return self();
    }

    @Override
    public boolean uriEncode() {
        return target.uriEncode();
    }

    @Override
    public int readTimeout() {
        return target.readTimeout();
    }

    @Override
    public CompletionStage<RestResponse> execute() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest readTimeout(int readTimeout) {
        target.readTimeout(readTimeout);
        return self();
    }

    @Override
    public ExecutableRestRequest maxRedirects(int maxRedirects) {
        target.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public ExecutableRestRequest maxRetries(int maxRetries) {
        target.maxRetries(maxRetries);
        return self();
    }

    @Override
    public ExecutableRestRequest disableExpectContinue() {
        target.disableExpectContinue();
        return self();
    }

    @Override
    public ExecutableRestRequest enableUriEncode() {
        target.enableUriEncode();
        return self();
    }

    @Override
    public ExecutableRestRequest addParams(Map<String, String> params) {
        target.addParams(params);
        return self();
    }

    @Override
    public ExecutableRestRequest addParam(String name, String value) {
        target.addParam(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest cookie(Cookie cookie) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest cookie(String name, String value) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest contentType(ContentType contentType) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest contentType(ContentTypeFactory contentTypeFactory) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest accept(AcceptType... acceptTypes) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest accept(AcceptTypeFactory acceptTypeFactory) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest acceptResolver(AcceptTypeResolver acceptTypeResolver) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public Object body() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RequestContext context() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        target.addHeaders(headers);
        return self();
    }

    @Override
    public ExecutableRestRequest addHeader(CharSequence name, CharSequence value) {
        target.addHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest setHeader(CharSequence name, CharSequence value) {
        target.setHeader(name, value);
        return self();
    }

    private ExecutableRestRequest self() {
        return this;
    }
}
