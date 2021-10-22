package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.util.Futures;
import io.esastack.restclient.codec.impl.EncodeContextImpl;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.utils.CookiesUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    protected final CompositeRequest target;
    protected final RestClientOptions clientOptions;
    protected final RestRequestExecutor requestExecutor;
    protected ContentType contentType;
    private AcceptType[] acceptTypes = {AcceptType.DEFAULT};

    protected AbstractExecutableRestRequest(CompositeRequest request,
                                            RestClientOptions clientOptions,
                                            RestRequestExecutor requestExecutor) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(requestExecutor, "requestExecutor");
        this.target = request;
        this.clientOptions = clientOptions;
        this.requestExecutor = requestExecutor;
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
    public long readTimeout() {
        return target.readTimeout();
    }

    @Override
    public CompletionStage<RestResponseBase> execute() {
        fillAcceptHeader();
        return requestExecutor.execute(this);
    }

    CompletionStage<HttpResponse> sendRequest() {
        try {
            if (hasBody()) {
                fillBody(encode());
            }
        } catch (Exception e) {
            return Futures.completed(e);
        }
        return target.execute();
    }

    private boolean hasBody() {
        HttpMethod method = method();
        return method != HttpMethod.GET &&
                method != HttpMethod.HEAD &&
                method != HttpMethod.OPTIONS;
    }

    private RequestBodyContent<?> encode() throws Exception {
        return new EncodeContextImpl(this, entity(), clientOptions.unmodifiableEncodeAdvices()).proceed();
    }

    private void fillBody(RequestBodyContent<?> content) {
        Object data = content.content();
        if (data == null || data instanceof byte[]) {
            target.body((byte[]) data);
        } else if (data instanceof File) {
            target.body((File) data);
        } else if (data instanceof MultipartBody) {
            target.multipart((MultipartBody) data);
        } else {
            throw new IllegalStateException("Illegal content type:" + data.getClass());
        }
    }

    @Override
    public ExecutableRestRequest readTimeout(long readTimeout) {
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
        CookiesUtil.cookie(cookie, headers(), false);
        return self();
    }

    @Override
    public ExecutableRestRequest cookie(String name, String value) {
        CookiesUtil.cookie(name, value, headers(), false);
        return self();
    }

    @Override
    public ExecutableRestRequest cookies(List<Cookie> cookies) {
        CookiesUtil.cookies(cookies, headers(), false);
        return self();
    }

    @Override
    public List<Cookie> removeCookies(String name) {
        return CookiesUtil.removeCookies(name, headers(), false);
    }

    @Override
    public List<Cookie> cookies(String name) {
        return CookiesUtil.getCookies(name, headers(), false);
    }

    @Override
    public Map<String, List<Cookie>> cookiesMap() {
        return CookiesUtil.getCookiesMap(headers(), false);
    }

    @Override
    public ExecutableRestRequest contentType(ContentType contentType) {
        Checks.checkNotNull(contentType, "contentType");

        this.contentType = contentType;
        headers().set(HttpHeaderNames.CONTENT_TYPE,
                contentType.mediaType().toString());
        return self();
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public ExecutableRestRequest accept(AcceptType... acceptTypes) {
        Checks.checkNotNull(acceptTypes, "acceptTypes");
        this.acceptTypes = acceptTypes;
        return self();
    }

    private void fillAcceptHeader() {
        if (this.acceptTypes == null || this.acceptTypes.length == 0) {
            return;
        }
        StringBuilder acceptBuilder = new StringBuilder();

        for (int i = 0; i < this.acceptTypes.length; i++) {
            AcceptType acceptType = this.acceptTypes[i];
            if (acceptType == null) {
                throw new NullPointerException("acceptType is null when index is equal to" + i);
            }
            MediaType mediaType = acceptType.mediaType();
            if (mediaType == AcceptType.EMPTY_MEDIA_TYPE) {
                continue;
            }
            if (acceptBuilder.length() > 0) {
                acceptBuilder.append(",");
            }
            acceptBuilder.append(mediaType.toString());
        }

        if (acceptBuilder.length() > 0) {
            headers().set(HttpHeaderNames.ACCEPT, acceptBuilder.toString());
        }
    }

    @Override
    public AcceptType[] acceptTypes() {
        return acceptTypes;
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
