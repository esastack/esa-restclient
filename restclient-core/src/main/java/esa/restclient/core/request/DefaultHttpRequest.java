package esa.restclient.core.request;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.*;
import esa.commons.netty.http.CookieImpl;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.HttpUri;
import esa.httpclient.core.Scheme;
import esa.restclient.core.MediaType;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultHttpRequest implements HttpRequest {

    private final HttpHeaders headers;
    private final Map<String, Object> properties;
    private final HttpMethod method;
    private final HttpUri uri;
    private final HttpVersion httpVersion;

    DefaultHttpRequest(String uri,
                       HttpMethod method,
                       HttpVersion httpVersion
    ) {
        Checks.checkNotEmptyArg(uri, "Request's uri must not be empty");
        Checks.checkNotNull(method, "HttpMethod must not be null");
        Checks.checkNotNull(httpVersion, "HttpVersion must not be null");
        this.method = method;
        this.uri = new HttpUri(uri);
        this.httpVersion = httpVersion;
        this.headers = new Http1HeadersImpl();
        this.properties = new ConcurrentHashMap<>(8);
    }


    DefaultHttpRequest(HttpRequest httpRequest) {
        Checks.checkNotNull(httpRequest, "HttpRequest must not be null!");
        this.method = httpRequest.method();
        this.uri = httpRequest.uri();
        this.httpVersion = httpRequest.version();
        this.headers = httpRequest.headers();
        this.properties = new ConcurrentHashMap<>(httpRequest.properties());
        Checks.checkNotNull(this.uri, "Request's uri must not be null");
        Checks.checkNotNull(this.method, "HttpMethod must not be null");
        Checks.checkNotNull(this.httpVersion, "HttpVersion must not be null");
        Checks.checkNotNull(this.headers, "Headers must not be null");
        Checks.checkNotNull(this.properties, "Properties must not be null");
    }


    @Override
    public Scheme scheme() {
        if (uri == null) {
            return null;
        }
        String scheme = uri.netURI().getScheme();
        if (StringUtils.isBlank(scheme)) {
            return null;
        }

        return Scheme.valueOf(scheme.toUpperCase());
    }

    @Override
    public HttpUri uri() {
        return uri;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        if (uri == null) {
            return "";
        }

        return uri.path();
    }

    @Override
    public HttpRequest addParams(Map<String, String> params) {
        if (params == null) {
            return self();
        }
        for (Map.Entry<String, String> row : params.entrySet()) {
            addParam(row.getKey(), row.getValue());
        }

        return self();
    }

    @Override
    public HttpRequest addParam(String name, String value) {
        Checks.checkNotNull(name, "Name of param must be not null!");
        Checks.checkNotNull(value, "Value of param must be not null!");
        this.uri.addParam(name, value);
        return self();
    }

    @Override
    public String getParam(String name) {
        return uri.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        return Collections.unmodifiableList(uri.params(name));
    }

    @Override
    public Set<String> paramNames() {
        return Collections.unmodifiableSet(uri.paramNames());
    }

    @Override
    public HttpRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        if (headers == null) {
            return self();
        }

        for (Map.Entry<? extends CharSequence, ? extends CharSequence> row : headers.entrySet()) {
            addHeader(row.getKey(), row.getValue());
        }

        return self();
    }

    @Override
    public HttpRequest addHeader(CharSequence name, CharSequence value) {
        Checks.checkNotNull(name, "Name of header must be not null!");
        Checks.checkNotNull(value, "Value of header must be not null!");
        this.headers.add(name, value);
        return self();
    }

    @Override
    public HttpRequest setHeader(CharSequence name, CharSequence value) {
        Checks.checkNotNull(name, "Name of header must be not null!");
        Checks.checkNotNull(value, "Value of header must be not null!");
        this.headers.set(name, value);
        return self();
    }

    @Override
    public String getHeader(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public List<String> getHeaders(CharSequence name) {
        return Collections.unmodifiableList(headers.getAll(name));
    }

    @Override
    public List<String> removeHeaders(CharSequence name) {
        List<String> headers = getHeaders(name);
        this.headers.remove(name);
        return headers;
    }

    @Override
    public MediaType contentType() {
        String contentTypeHeader = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeHeader == null) {
            return null;
        }
        return MediaType.parseMediaType(contentTypeHeader);
    }

    @Override
    public HttpRequest contentType(MediaType mediaType) {
        if (mediaType == null) {
            return self();
        }
        headers.set(HttpHeaderNames.CONTENT_TYPE, mediaType.value());
        return self();
    }


    @Override
    public HttpRequest cookie(Cookie cookie) {
        if (cookie == null) {
            return self();
        }
        headers.add(HttpHeaderNames.COOKIE, cookie.encode(false));
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

    @Override
    public HttpRequest cookie(String name, String value) {
        return cookie(new CookieImpl(name, value));
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
        List<String> cookieHeaders = headers.getAll(HttpHeaderNames.COOKIE);
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

    private void coverAllCookies(List<Cookie> cookies) {
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
    public HttpRequest accept(MediaType... mediaTypes) {
        if (mediaTypes == null) {
            return self();
        }

        if (headers.contains(HttpHeaderNames.ACCEPT)) {
            headers.remove(HttpHeaderNames.ACCEPT);
        }

        for (MediaType mediaType : mediaTypes) {
            if (mediaType == null) {
                continue;
            }
            headers.add(HttpHeaderNames.ACCEPT, mediaType.value());
        }

        return self();
    }

    @Override
    public List<MediaType> acceptTypes() {

        List<String> contentTypeHeaders = headers.getAll(HttpHeaderNames.ACCEPT);
        if (contentTypeHeaders == null || contentTypeHeaders.size() == 0) {
            return Collections.emptyList();
        }

        List<MediaType> mediaTypes = new ArrayList<>();
        for (String contentTypeHeader : contentTypeHeaders) {
            if (StringUtils.isBlank(contentTypeHeader)) {
                continue;
            }
            mediaTypes.addAll(MediaType.parseMediaTypes(contentTypeHeader));
        }

        return Collections.unmodifiableList(mediaTypes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return (T) properties.get(name);
    }

    @Override
    public <T> T getProperty(String name, T defaultValue) {
        final T value = getProperty(name);
        return value == null ? defaultValue : value;
    }

    @Override
    public HttpRequest property(String name, Object value) {
        Checks.checkNotNull(name, "Name must be not null!");
        Checks.checkNotNull(value, "Value must be not null!");
        properties.put(name, value);
        return self();
    }

    @Override
    public Set<String> propertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public Map<String, Object> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeProperty(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return (T) properties.remove(name);
    }

    @Override
    public HttpVersion version() {
        return httpVersion;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    private HttpRequest self() {
        return this;
    }
}
