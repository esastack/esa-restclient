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
import esa.httpclient.core.util.Futures;
import esa.restclient.exec.RequestAction;
import esa.restclient.exec.RestRequestExecutor;
import esa.restclient.serializer.TxSerializer;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.*;
import java.util.concurrent.CompletionStage;

public abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    protected final CompositeRequest target;
    private final RestClientConfig clientConfig;
    private final RestRequestExecutor requestExecutor;
    private final RequestContext context = new RequestContextImpl();
    private ContentType contentType;
    private ContentTypeFactory contentTypeFactory;
    private List<AcceptType> acceptTypes;
    private AcceptTypeResolver acceptTypeResolver;

    protected AbstractExecutableRestRequest(CompositeRequest request, RestClientConfig clientConfig, RestRequestExecutor requestExecutor) {
        Checks.checkNotNull(request, "Request must not be null");
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null");
        Checks.checkNotNull(requestExecutor, "RequestExecutor must not be null");
        this.target = request;
        this.clientConfig = clientConfig;
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
    public CompletionStage<RestResponse> execute() {
        return requestExecutor.execute(this, createRequestAction());
    }

    private RequestAction createRequestAction() {
        return () -> {
            try {
                ContentType contentType = computeContentType();
                if (contentType == null) {
                    throw new IllegalStateException("The request has no contentType," +
                            "Please set the correct contentType or contentTypeFactory");
                }
                target.setHeader(HttpHeaderNames.CONTENT_TYPE, contentType.getMediaType().toString());

                TxSerializer txSerializer = contentType.getTxSerializer();
                if (txSerializer != ContentType.NO_NEED_SERIALIZE) {
                    target.body(txSerializer.serialize(body()));
                }
            } catch (Exception e) {
                return Futures.completed(e);
            }

            return target.execute().thenApply(this::processTargetResponse);
        };
    }

    private ContentType computeContentType() {
        if (contentType != null) {
            return contentType;
        }
        if (contentTypeFactory != null) {
            Optional<ContentType> optionalContentType = contentTypeFactory.create(headers(), context, body());
            if (optionalContentType.isPresent()) {
                return optionalContentType.get();
            }
        }
        ContentTypeFactory contentTypeFactoryOfClient = clientConfig.contentTypeFactory();
        if (contentTypeFactoryOfClient != null) {
            Optional<ContentType> optionalContentType = contentTypeFactoryOfClient.create(headers(), context, body());
            if (optionalContentType.isPresent()) {
                return optionalContentType.get();
            }
        }
        return defaultContentType();
    }

    abstract protected ContentType defaultContentType();

    private RestResponse processTargetResponse(HttpResponse response) {
        return new RestResponseImpl(this, response);
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
        this.contentType = contentType;
        return self();
    }

    @Override
    public ExecutableRestRequest contentType(ContentTypeFactory contentTypeFactory) {
        this.contentTypeFactory = contentTypeFactory;
        return self();
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public ExecutableRestRequest accept(AcceptType... acceptTypes) {
        if (acceptTypes == null) {
            return self();
        }
        for (AcceptType acceptType : acceptTypes) {
            headers().add(HttpHeaderNames.ACCEPT, acceptType.getMediaType().toString());
        }
        this.acceptTypes = Arrays.asList(acceptTypes);
        return self();
    }

    @Override
    public List<AcceptType> acceptTypes() {
        return acceptTypes;
    }

    @Override
    public ExecutableRestRequest acceptTypeResolver(AcceptTypeResolver acceptTypeResolver) {
        this.acceptTypeResolver = acceptTypeResolver;
        return self();
    }

    @Override
    public RequestContext context() {
        return context;
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
