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
import esa.httpclient.core.util.MultiValueMapUtils;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class HttpUri {

    /**
     * The uri specified by {@link HttpClient}, such as {@link HttpClient#get(String)} and so on.
     */
    private final String rawUri;

    /**
     * The uri created by {@link URI#create(String)}
     */
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
        Checks.checkNotNull(uri, "uri");
        this.uri = uri;
        this.rawUri = uri.toString();
        this.params = params == null ? new HashMultiValueMap<>() : new HashMultiValueMap<>(params);
    }

    public void addParam(String name, String value) {
        Checks.checkNotNull(name, "name");
        Checks.checkNotNull(value, "value");
        params.add(name, value);
    }

    public void addParams(String name, List<String> values) {
        Checks.checkNotNull(name, "name");
        Checks.checkNotNull(values, "values");
        params.addAll(name, values);
    }

    /**
     * Gets values of given {@code name} which is unmodifiable.
     *
     * @param name  name
     * @return  unmodifiable values, may be null
     */
    public List<String> params(String name) {
        Checks.checkNotNull(name, "name");
        return params.get(name);
    }

    public String getParam(String name) {
        Checks.checkNotNull(name, "name");
        final List<String> values = params.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    public Set<String> paramNames() {
        return params.keySet();
    }

    public MultiValueMap<String, String> params() {
        // the user may be operate the params directly.
        return params;
    }

    public MultiValueMap<String, String> unmodifiableParams() {
        return MultiValueMapUtils.unmodifiableMap(params());
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

    /**
     * Obtains relative path of the fully uri and query parameters. eg:
     *
     * uri is http://127.0.0.1:8080/abc/def and
     * params are: a=b,c=d,x=y then
     *
     * relative result is: "/abc/def?a=b%26c=d%26x=y"
     *
     * @param encode encode
     * @return  relative uri
     */
    public String relative(boolean encode) {
        return encode ? encodeRelative() : spliceRelativeDirectly();
    }

    private String encodeRelative() {
        if (StringUtils.isNotEmpty(uri.getQuery())) {
            throw new IllegalArgumentException("query: " + uri.getRawQuery() + " is not allowed if you want to " +
                    "encode uri correctly, target uri: " + uri + ", please use addParam()!");
        }

        if (params().isEmpty()) {
            if (StringUtils.isEmpty(uri.getQuery())) {
                return uri.getPath();
            }
        }

        final QueryStringEncoder encoder = new QueryStringEncoder(uri.getRawPath(), StandardCharsets.UTF_8);
        for (Map.Entry<String, List<String>> item : params().entrySet()) {
            if (item.getValue().isEmpty()) {
                continue;
            }

            for (String value : item.getValue()) {
                encoder.addParam(item.getKey(), value);
            }
        }

        return encoder.toString();
    }

    private String spliceRelativeDirectly() {
        if (params().isEmpty()) {
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

        for (Map.Entry<String, List<String>> item : params().entrySet()) {
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
