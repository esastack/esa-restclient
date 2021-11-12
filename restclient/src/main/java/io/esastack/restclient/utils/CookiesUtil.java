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

import esa.commons.Checks;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CookiesUtil {

    private CookiesUtil() {
    }

    private static void addCookie(Cookie cookie, HttpHeaders headers, boolean isResponse) {
        if (cookie == null) {
            return;
        }

        if (isResponse) {
            headers.add(HttpHeaderNames.SET_COOKIE, cookie.encode(true));
        } else {
            headers.add(HttpHeaderNames.COOKIE, cookie.encode(false));
        }
    }

    public static void addCookie(String name, String value, HttpHeaders headers, boolean isResponse) {
        Checks.checkNotNull(name, "name");
        Checks.checkNotNull(value, "value");
        Checks.checkNotNull(headers, "headers");
        addCookie(new CookieImpl(name, value), headers, isResponse);
    }

    public static void addCookies(HttpHeaders headers, boolean isResponse, Cookie... cookies) {
        if (cookies == null || cookies.length == 0) {
            return;
        }
        Checks.checkNotNull(headers, "headers");
        for (Cookie cookie : cookies) {
            addCookie(cookie, headers, isResponse);
        }
    }

    public static Cookie getCookie(String name, HttpHeaders headers, boolean isResponse) {
        Checks.checkNotNull(name, "name");
        for (Cookie cookie : getCookieSet(headers, isResponse)) {
            if (name.equals(cookie.name())) {
                return cookie;
            }
        }
        return null;
    }

    public static Set<Cookie> getCookieSet(HttpHeaders headers, boolean isResponse) {
        Checks.checkNotNull(headers, "headers");
        List<String> cookieHeaders;
        if (isResponse) {
            cookieHeaders = headers.getAll(HttpHeaderNames.SET_COOKIE);
        } else {
            cookieHeaders = headers.getAll(HttpHeaderNames.COOKIE);
        }
        if (cookieHeaders == null || cookieHeaders.size() == 0) {
            return Collections.emptySet();
        }
        Set<Cookie> cookieSet = new HashSet<>();
        for (String cookieHeader : cookieHeaders) {
            decodeAndFillToSet(cookieHeader, cookieSet, isResponse);
        }
        return cookieSet;
    }

    private static void decodeAndFillToSet(String cookieHeader,
                                           Set<Cookie> cookieSet, boolean isResponse) {
        if (isResponse) {
            ServerCookieDecoder.STRICT.decodeAll(cookieHeader)
                    .forEach(cookie -> fillCookieToSet(new CookieImpl(cookie), cookieSet));
        } else {
            String[] cookieStrings = cookieHeader.split(";");
            for (String cookieString : cookieStrings) {
                fillCookieToSet(new CookieImpl(ClientCookieDecoder.STRICT.decode(cookieString)), cookieSet);
            }
        }
    }

    private static void fillCookieToSet(Cookie cookie, Set<Cookie> cookieSet) {
        cookieSet.add(cookie);
    }
}
