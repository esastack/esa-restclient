package esa.restclient.utils;

import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.netty.http.CookieImpl;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookiesUtil {

    private CookiesUtil() {
    }

    public static List<Cookie> getCookies(String name, HttpHeaders headers) {
        List<Cookie> cookies = getCookiesMap(headers).get(name);
        return cookies == null ? Collections.emptyList() : Collections.unmodifiableList(cookies);
    }

    public static Map<String, List<Cookie>> getCookiesMap(HttpHeaders headers) {
        return Collections.unmodifiableMap(getModifiableCookiesMap(headers));
    }

    public static List<Cookie> removeCookies(String name, HttpHeaders headers) {
        if (name == null) {
            return Collections.emptyList();
        }
        Map<String, List<Cookie>> cookiesMap = getModifiableCookiesMap(headers);
        List<Cookie> cookiesWithName = cookiesMap.remove(name);
        List<Cookie> allCookies = new ArrayList<>();
        cookiesMap.values().forEach(allCookies::addAll);
        coverAllCookies(allCookies, headers);
        return cookiesWithName == null ? Collections.emptyList() : Collections.unmodifiableList(cookiesWithName);
    }

    private static void coverAllCookies(List<Cookie> cookies, HttpHeaders headers) {
        if (cookies == null || cookies.size() == 0) {
            headers.remove(HttpHeaderNames.COOKIE);
            return;
        }
        headers.set(HttpHeaderNames.COOKIE, encodeCookies(cookies));
    }

    public static String encodeCookies(List<Cookie> cookies) {
        List<io.netty.handler.codec.http.cookie.Cookie> adapterCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            adapterCookies.add(new DefaultCookie(cookie.name(), cookie.value()));
        }
        return ClientCookieEncoder.STRICT.encode(adapterCookies);
    }

    private static Map<String, List<Cookie>> getModifiableCookiesMap(HttpHeaders headers) {
        List<String> cookieHeaders = headers.getAll(esa.commons.http.HttpHeaderNames.COOKIE);
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
}
