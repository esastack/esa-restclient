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
import io.esastack.httpclient.core.util.Futures;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestBody;
import io.esastack.restclient.codec.impl.EncodeContextImpl;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.utils.CookiesUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    protected final CompositeRequest target;
    protected final RestClientOptions clientOptions;
    protected final RestRequestExecutor requestExecutor;
    protected MediaType contentType = null;
    private MediaType[] acceptTypes = null;
    private Encoder encoder = null;
    private Decoder decoder = null;

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
        return requestExecutor.execute(this);
    }

    RestClientOptions clientOptions() {
        return clientOptions;
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
        return entity() != null;
    }

    private RequestBody<?> encode() throws Exception {
        return new EncodeContextImpl(this, entity(), clientOptions).proceed();
    }

    private void fillBody(RequestBody<?> requestBody) {
        if (requestBody.isBytes()) {
            target.body(requestBody.getBytes());
        } else if (requestBody.isFile()) {
            target.body(requestBody.getFile());
        } else if (requestBody.isMultipart()) {
            target.multipart(requestBody.getMultipart());
        } else {
            throw new IllegalStateException("Illegal requestBody type! type of requestBody: " + requestBody.getType()
                    + " , content of requestBody: " + requestBody.getContent());
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
    public ExecutableRestRequest contentType(MediaType contentType) {
        Checks.checkNotNull(contentType, "contentType");

        this.contentType = contentType;
        headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.value());
        return self();
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public ExecutableRestRequest accept(MediaType... acceptTypes) {
        Checks.checkNotNull(acceptTypes, "acceptTypes");
        this.acceptTypes = acceptTypes;
        processAcceptHeader();
        return self();
    }

    private void processAcceptHeader() {
        if (this.acceptTypes == null || this.acceptTypes.length == 0) {
            headers().remove(HttpHeaderNames.ACCEPT);
            return;
        }
        StringBuilder acceptBuilder = new StringBuilder();

        for (int i = 0; i < this.acceptTypes.length; i++) {
            MediaType acceptType = this.acceptTypes[i];
            if (acceptType == null) {
                throw new NullPointerException("acceptType is null when index is equal to" + i);
            }
            if (acceptBuilder.length() > 0) {
                acceptBuilder.append(",");
            }
            acceptBuilder.append(acceptType.value());
        }

        int length = acceptBuilder.length();
        if (length == 0) {
            headers().remove(HttpHeaderNames.ACCEPT);
        } else if (acceptBuilder.length() > 0) {
            headers().set(HttpHeaderNames.ACCEPT, acceptBuilder.toString());
        }
    }

    @Override
    public MediaType[] acceptTypes() {
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

    @Override
    public ExecutableRestRequest encoder(Encoder encoder) {
        this.encoder = encoder;
        return self();
    }

    @Override
    public Encoder encoder() {
        return encoder;
    }

    @Override
    public ExecutableRestRequest decoder(Decoder decoder) {
        this.decoder = decoder;
        return self();
    }

    @Override
    public Decoder decoder() {
        return decoder;
    }

    private ExecutableRestRequest self() {
        return this;
    }
}
