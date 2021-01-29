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
package esa.httpclient.core;

import esa.commons.Checks;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.core.Buffer;

import java.io.File;
import java.util.List;
import java.util.Set;

public class DelegatingRequest implements HttpRequest {

    private final HttpRequest underlying;

    public DelegatingRequest(HttpRequest underlying) {
        Checks.checkNotNull(underlying, "HttpRequest must not be null");
        this.underlying = underlying;
    }

    @Override
    public HttpMethod method() {
        return underlying.method();
    }

    @Override
    public String scheme() {
        return underlying.scheme();
    }

    @Override
    public String path() {
        return underlying.path();
    }

    @Override
    public HttpUri uri() {
        return underlying.uri();
    }

    @Override
    public HttpRequest addParam(String name, String value) {
        return underlying.addParam(name, value);
    }

    @Override
    public String getParam(String name) {
        return underlying.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        return underlying.getParams(name);
    }

    @Override
    public Set<String> paramNames() {
        return underlying.paramNames();
    }

    @Override
    public HttpHeaders headers() {
        return underlying.headers();
    }

    @Override
    public HttpRequest addHeader(CharSequence name, CharSequence value) {
        return underlying.addHeader(name, value);
    }

    @Override
    public CharSequence getHeader(CharSequence name) {
        return underlying.getHeader(name);
    }

    @Override
    public HttpRequest setHeader(CharSequence name, CharSequence value) {
        return underlying.setHeader(name, value);
    }

    @Override
    public HttpRequest removeHeader(CharSequence name) {
        return underlying.removeHeader(name);
    }

    @Override
    public boolean uriEncode() {
        return underlying.uriEncode();
    }

    @Override
    public int readTimeout() {
        return underlying.readTimeout();
    }

    @Override
    public boolean isSegmented() {
        return underlying.isSegmented();
    }

    @Override
    public boolean isMultipart() {
        return underlying.isMultipart();
    }

    @Override
    public Buffer buffer() {
        return underlying.buffer();
    }

    @Override
    public File file() {
        return underlying.file();
    }

    @Override
    public MultiValueMap<String, String> attrs() {
        return underlying.attrs();
    }

    @Override
    public List<MultipartFileItem> files() {
        return underlying.files();
    }

    @Override
    public boolean isFile() {
        return underlying.isFile();
    }

    @Override
    public HttpRequest copy() {
        return underlying.copy();
    }

    @Override
    public boolean multipartEncode() {
        return underlying.multipartEncode();
    }

    @Override
    public String toString() {
        return underlying.toString();
    }
}
