/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.utils;

import esa.commons.netty.http.CookieImpl;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookiesUtil {

    private CookiesUtil() {
    }

    private static void cookie(Cookie cookie, HttpHeaders headers, boolean isResponse) {
        if (cookie == null) {
            return;
        }
        if (isResponse) {
            headers.add(HttpHeaderNames.SET_COOKIE, cookie.encode(true));
        } else {
            headers.add(HttpHeaderNames.COOKIE, cookie.encode(false));
        }
    }

    public static void cookie(String name, String value, HttpHeaders headers, boolean isResponse) {
        cookie(new CookieImpl(name, value), headers, isResponse);
    }

    public static void cookie(HttpHeaders headers, boolean isResponse, Cookie... cookies) {
        if (cookies == null || cookies.length == 0) {
            return;
        }

        for (Cookie cookie : cookies) {
            cookie(cookie, headers, isResponse);
        }
    }

    public static List<Cookie> getCookies(String name, HttpHeaders headers, boolean isResponse) {
        List<Cookie> cookies = getCookiesMap(headers, isResponse).get(name);
        return cookies == null ? Collections.emptyList() : Collections.unmodifiableList(cookies);
    }

    public static Map<String, List<Cookie>> getCookiesMap(HttpHeaders headers, boolean isResponse) {
        return Collections.unmodifiableMap(getModifiableCookiesMap(headers, isResponse));
    }

    public static List<Cookie> removeCookies(String name, HttpHeaders headers, boolean isResponse) {
        if (name == null) {
            return Collections.emptyList();
        }
        Map<String, List<Cookie>> cookiesMap = getModifiableCookiesMap(headers, isResponse);
        List<Cookie> cookiesWithName = cookiesMap.remove(name);
        List<Cookie> allCookies = new ArrayList<>();
        cookiesMap.values().forEach(allCookies::addAll);
        coverAllCookies(allCookies, headers, isResponse);
        return cookiesWithName == null ? Collections.emptyList() : Collections.unmodifiableList(cookiesWithName);
    }

    private static void coverAllCookies(List<Cookie> cookies, HttpHeaders headers, boolean isResponse) {
        if (isResponse) {
            headers.remove(HttpHeaderNames.SET_COOKIE);
            if (cookies == null) {
                return;
            }
            for (Cookie cookie : cookies) {
                cookie(cookie, headers, true);
            }
        } else {
            headers.remove(HttpHeaderNames.COOKIE);
            if (cookies == null) {
                return;
            }
            for (Cookie cookie : cookies) {
                cookie(cookie, headers, false);
            }
        }
    }

    private static Map<String, List<Cookie>> getModifiableCookiesMap(HttpHeaders headers, boolean isResponse) {
        List<String> cookieHeaders;
        if (isResponse) {
            cookieHeaders = headers.getAll(HttpHeaderNames.SET_COOKIE);
        } else {
            cookieHeaders = headers.getAll(HttpHeaderNames.COOKIE);
        }
        if (cookieHeaders == null || cookieHeaders.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<Cookie>> cookiesMap = new HashMap<>();
        for (String cookieHeader : cookieHeaders) {
            decodeAndFillToMap(cookieHeader, cookiesMap, isResponse);
        }
        return cookiesMap;
    }

    private static void decodeAndFillToMap(String cookieHeader,
                                           Map<String, List<Cookie>> cookiesMap, boolean isResponse) {
        if (isResponse) {
            ServerCookieDecoder.STRICT.decodeAll(cookieHeader)
                    .forEach(cookie -> fillCookieToMap(new CookieImpl(cookie), cookiesMap));
        } else {
            String[] cookieStrings = cookieHeader.split(";");
            for (String cookieString : cookieStrings) {
                fillCookieToMap(new CookieImpl(ClientCookieDecoder.STRICT.decode(cookieString)), cookiesMap);
            }
        }
    }

    private static void fillCookieToMap(Cookie cookie, Map<String, List<Cookie>> cookiesMap) {
        List<Cookie> cookies = cookiesMap.computeIfAbsent(cookie.name(), (name) -> new ArrayList<>());
        cookies.add(cookie);
    }
}
