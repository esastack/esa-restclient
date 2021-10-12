package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.http.CookieImpl;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.HttpUri;
import esa.httpclient.core.MultipartBody;
import esa.httpclient.core.util.Futures;
import esa.restclient.codec.impl.EncodeContextImpl;
import esa.restclient.exec.RestRequestExecutor;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    protected final CompositeRequest target;
    protected final RestClientOptions clientOptions;
    protected final RestRequestExecutor requestExecutor;
    protected ContentType contentType;
    private ContentType[] acceptTypes;

    protected AbstractExecutableRestRequest(CompositeRequest request,
                                            RestClientOptions clientOptions,
                                            RestRequestExecutor requestExecutor) {
        Checks.checkNotNull(request, "Request must not be null");
        Checks.checkNotNull(clientOptions, "ClientOptions must not be null");
        Checks.checkNotNull(requestExecutor, "RequestExecutor must not be null");
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
    public int readTimeout() {
        return target.readTimeout();
    }

    @Override
    public CompletionStage<RestResponseBase> execute() {
        return requestExecutor.execute(this);
    }

    CompletionStage<HttpResponse> sendRequest() {
        try {
            fillBody(encode());
        } catch (Exception e) {
            return Futures.completed(e);
        }
        return target.execute();
    }

    private RequestBodyContent<?> encode() throws Exception {
        return new EncodeContextImpl(this, entity(), clientOptions.unmodifiableEncodeAdvices()).proceed();
    }

    private void fillBody(RequestBodyContent<?> content) {
        byte type = content.type();
        if (type == RequestBodyContent.TYPE.BYTES) {
            target.body((byte[]) content.content());
        } else if (type == RequestBodyContent.TYPE.FILE) {
            target.body((File) content.content());
        } else if (type == RequestBodyContent.TYPE.MULTIPART) {
            target.multipart((MultipartBody) content.content());
        } else {
            throw new IllegalStateException("Illegal type:" + type
                    + ",Type only supports elements of RequestContent.TYPE");
        }
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
        if (cookie == null) {
            return self();
        }
        headers().add(HttpHeaderNames.COOKIE, cookie.encode(false));
        return self();
    }

    @Override
    public ExecutableRestRequest cookie(String name, String value) {
        return cookie(new CookieImpl(name, value));
    }

    @Override
    public ExecutableRestRequest cookies(List<Cookie> cookies) {
        if (cookies == null) {
            return self();
        }
        headers().add(HttpHeaderNames.COOKIE, encodeCookies(cookies));
        return self();
    }

    @Override
    public List<Cookie> removeCookies(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        Map<String, List<Cookie>> cookiesMap = getModifiableCookiesMap();
        List<Cookie> cookiesWithName = cookiesMap.remove(name);
        List<Cookie> allCookies = new ArrayList<>();
        cookiesMap.values().forEach(allCookies::addAll);
        coverAllCookies(allCookies);
        return cookiesWithName == null ? Collections.emptyList() : Collections.unmodifiableList(cookiesWithName);
    }

    private void coverAllCookies(List<Cookie> cookies) {
        HttpHeaders headers = headers();
        if (cookies == null || cookies.size() == 0) {
            headers.remove(HttpHeaderNames.COOKIE);
            return;
        }
        headers.set(HttpHeaderNames.COOKIE, encodeCookies(cookies));
    }

    private String encodeCookies(List<Cookie> cookies) {
        List<io.netty.handler.codec.http.cookie.Cookie> adapterCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            adapterCookies.add(new DefaultCookie(cookie.name(), cookie.value()));
        }
        return ClientCookieEncoder.STRICT.encode(adapterCookies);
    }

    @Override
    public List<Cookie> getCookies(String name) {
        List<Cookie> cookies = getCookiesMap().get(name);
        return cookies == null ? Collections.emptyList() : Collections.unmodifiableList(cookies);
    }

    @Override
    public Map<String, List<Cookie>> getCookiesMap() {
        return Collections.unmodifiableMap(getModifiableCookiesMap());
    }

    private Map<String, List<Cookie>> getModifiableCookiesMap() {
        List<String> cookieHeaders = headers().getAll(HttpHeaderNames.COOKIE);
        if (cookieHeaders == null || cookieHeaders.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<Cookie>> cookiesMap = new HashMap<>();
        for (String cookieHeader : cookieHeaders) {
            String[] cookieStrings = cookieHeader.split(";");
            for (String cookieString : cookieStrings) {
                Cookie cookie = new CookieImpl(ClientCookieDecoder.STRICT.decode(cookieString));
                List<Cookie> cookies = cookiesMap.computeIfAbsent(cookie.name(), (name) -> new ArrayList<>());
                cookies.add(cookie);
            }
        }
        return cookiesMap;
    }

    @Override
    public ExecutableRestRequest contentType(ContentType contentType) {
        if (contentType == null) {
            throw new NullPointerException("contentType");
        }
        if (contentType.encoder() == null) {
            throw new NullPointerException("contentType‘s encoder");
        }

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
    public ExecutableRestRequest accept(ContentType... acceptTypes) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return self();
        }
        StringBuilder acceptBuilder = new StringBuilder();

        for (int i = 0; i < acceptTypes.length; i++) {
            ContentType acceptType = acceptTypes[i];
            if (acceptType == null) {
                throw new NullPointerException("acceptType is null when index is equal to" + i);
            }
            if (acceptType.decoder() == null) {
                throw new NullPointerException("acceptType‘s decoder is null when index is equal to" + i);
            }
            if (i > 0) {
                acceptBuilder.append(",");
            }
            acceptBuilder.append(acceptType.mediaType().toString());
        }

        headers().set(HttpHeaderNames.ACCEPT, acceptBuilder.toString());
        this.acceptTypes = acceptTypes;
        return self();
    }

    @Override
    public ContentType[] acceptTypes() {
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
