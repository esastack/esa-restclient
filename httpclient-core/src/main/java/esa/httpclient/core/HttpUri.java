/*
 * Copyright 2020 OPPO ESA Stack Project
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
package esa.httpclient.core;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class HttpUri {

    private final String rawUri;
    private final URI uri;
    private final MultiValueMap<String, String> params;

    public HttpUri(String rawUri) {
        Checks.checkNotEmptyArg(rawUri, "rawUri must not be empty");
        this.uri = URI.create(rawUri);
        this.rawUri = rawUri;
        this.params = new HashMultiValueMap<>();
    }

    public HttpUri(String rawUri, MultiValueMap<String, String> params) {
        Checks.checkNotEmptyArg(rawUri, "rawUri must not be empty");
        this.uri = URI.create(rawUri);
        this.rawUri = rawUri;
        this.params = params == null ? new HashMultiValueMap<>() : new HashMultiValueMap<>(params);
    }

    public HttpUri(URI uri, MultiValueMap<String, String> params) {
        Checks.checkNotNull(uri, "uri must not be null");
        this.uri = uri;
        this.rawUri = uri.toString();
        this.params = params == null ? new HashMultiValueMap<>() : new HashMultiValueMap<>(params);
    }

    public void addParam(String key, String value) {
        params.add(key, value);
    }

    public void addParams(String key, List<String> values) {
        params.addAll(key, values);
    }

    public List<String> params(String key) {
        final List<String> values = params.get(key);
        return values == null ? Collections.emptyList() : values;
    }

    public String getParam(String name) {
        return params.getFirst(name);
    }

    public Set<String> paramNames() {
        return params.keySet();
    }

    public MultiValueMap<String, String> params() {
        return params;
    }

    public URI netURI() {
        return uri;
    }

    public String path() {
        return uri.getPath();
    }

    public String host() {
        return uri.getHost();
    }

    public int port() {
        return uri.getPort();
    }

    public String relative(boolean uriEncoding) {
        return uriEncoding ? relative(StandardCharsets.UTF_8) : spliceRelativeRefDirectly();
    }

    public String relative(Charset charset) {
        if (StringUtils.isNotEmpty(uri.getQuery())) {
            throw new IllegalArgumentException("query: " + uri.getRawQuery() + " is not allowed if you want to " +
                    "encode uri correctly, target uri: " + uri.toString() + ", please use addParam()!");
        }

        if (params.isEmpty()) {
            if (StringUtils.isEmpty(uri.getQuery())) {
                return uri.getPath();
            }
        }

        final QueryStringEncoder encoder = new QueryStringEncoder(uri.getRawPath(), charset);
        for (Map.Entry<String, List<String>> item : params.entrySet()) {
            if (item.getValue().isEmpty()) {
                continue;
            }

            for (String value : item.getValue()) {
                encoder.addParam(item.getKey(), value);
            }
        }

        return encoder.toString();
    }

    public String spliceRelativeRefDirectly() {
        if (params.isEmpty()) {
            if (StringUtils.isEmpty(uri.getRawQuery())) {
                return uri.getPath();
            }

            // Parse query params, eg: /?a=b&c=d
            return uri.getRawPath() + "?" + uri.getRawQuery();
        }

        final StringBuilder target = new StringBuilder(uri.getRawPath()).append("?");
        if (StringUtils.isNotEmpty(uri.getRawQuery())) {
            target.append(uri.getRawQuery()).append("&");
        }

        for (Map.Entry<String, List<String>> item : params.entrySet()) {
            if (item.getValue().isEmpty()) {
                continue;
            }

            for (String value : item.getValue()) {
                target.append(item.getKey()).append("=").append(value).append("&");
            }
        }

        return target.substring(0, target.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpUri uri1 = (HttpUri) o;
        return Objects.equals(rawUri, uri1.rawUri) &&
                Objects.equals(params, uri1.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawUri, params);
    }

    @Override
    public String toString() {
        return rawUri;
    }
}
