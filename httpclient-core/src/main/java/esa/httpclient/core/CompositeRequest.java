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
import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.netty.NettyHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CompositeRequest extends HttpRequestBaseImpl implements PlainRequest, FileRequest,
        MultipartRequest, HttpRequestFacade {

    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    private final NettyHttpClient client;
    private final Supplier<ChunkRequest> request;

    /**
     * Body data
     */
    private final MultiValueMap<String, String> attributes = new HashMultiValueMap<>();
    private final List<MultipartFileItem> files = new LinkedList<>();
    private volatile byte[] bytes;
    private volatile File file;
    private volatile boolean multipartEncode = true;
    private boolean started;

    /**
     * 1: multipart; 2: segment; 3. file; others: plain
     */
    private volatile byte type = 0;

    public CompositeRequest(HttpClientBuilder builder,
                            NettyHttpClient client,
                            Supplier<ChunkRequest> request,
                            HttpMethod method,
                            String uri) {
        super(builder, method, uri);
        Checks.checkNotNull(client, "NettyHttpClient must not be null");
        Checks.checkNotNull(request, "ChunkRequest must not be null");
        this.client = client;
        this.request = request;
    }

    @Override
    public synchronized CompletableFuture<HttpResponse> execute() {
        if (started) {
            throw new IllegalStateException("The execute() has been called before");
        }
        this.started = true;
        return client.execute(this, ctx, handle, handler);
    }

    @Override
    public synchronized PlainRequest body(byte[] bytes) {
        checkStarted();
        cleanBody();
        this.type = 0;
        this.bytes = bytes;
        return self();
    }

    @Override
    public MultipartRequest multipart() {
        this.type = 1;
        return this;
    }

    @Override
    public ChunkRequest segment() {
        this.type = 2;
        return request.get();
    }

    @Override
    public synchronized FileRequest body(File file) {
        checkStarted();
        cleanBody();
        this.type = 3;
        this.file = file;
        return self();
    }

    @Override
    public byte[] bytes() {
        return bytes;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public synchronized MultipartRequest multipartEncode(boolean multipartEncode) {
        checkStarted();
        this.multipartEncode = multipartEncode;
        return self();
    }

    @Override
    public boolean multipartEncode() {
        return multipartEncode;
    }

    @Override
    public synchronized MultipartRequest attr(String name, String value) {
        checkStarted();
        if (illegalArgs(name, value)) {
            return self();
        }
        attributes.add(name, value);
        return self();
    }

    @Override
    public synchronized MultipartRequest file(String name, File file) {
        checkStarted();
        return file(name, file, DEFAULT_BINARY_CONTENT_TYPE);
    }

    @Override
    public synchronized MultipartRequest file(String name, File file, String contentType) {
        checkStarted();
        return file(name, file, contentType, DEFAULT_TEXT_CONTENT_TYPE.equalsIgnoreCase(contentType));
    }

    @Override
    public synchronized MultipartRequest file(String name,
                                              File file,
                                              String contentType,
                                              boolean isText) {
        checkStarted();
        if (illegalArgs(name, file)) {
            return self();
        }
        checkMultipartFile();
        return file(name, file.getName(), file, contentType, isText);
    }

    @Override
    public synchronized MultipartRequest file(String name,
                                              String filename,
                                              File file,
                                              String contentType,
                                              boolean isText) {
        checkStarted();
        checkMultipartFile();
        files.add(new MultipartFileItem(name, filename, file, contentType, isText));
        return self();
    }

    @Override
    public MultiValueMap<String, String> attributes() {
        return new HashMultiValueMap<>(attributes);
    }

    @Override
    public List<MultipartFileItem> files() {
        return new ArrayList<>(files);
    }

    ////////**********************COMMON SETTER************************////////

    @Override
    public synchronized CompositeRequest uriEncodeEnabled(Boolean uriEncodeEnabled) {
        checkStarted();
        super.uriEncodeEnabled(uriEncodeEnabled);
        return self();
    }

    @Override
    public synchronized CompositeRequest expectContinueEnabled(Boolean expectContinueEnabled) {
        checkStarted();
        super.expectContinueEnabled(expectContinueEnabled);
        return self();
    }

    @Override
    public synchronized CompositeRequest maxRedirects(int maxRedirects) {
        checkStarted();
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public synchronized CompositeRequest maxRetries(int maxRetries) {
        checkStarted();
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public synchronized CompositeRequest readTimeout(int readTimeout) {
        checkStarted();
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public synchronized CompositeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        checkStarted();
        super.addHeaders(headers);
        return self();
    }

    @Override
    public synchronized CompositeRequest addHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.addHeader(name, value);
        return self();
    }

    @Override
    public synchronized CompositeRequest setHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.setHeader(name, value);
        return self();
    }

    @Override
    public synchronized CompositeRequest removeHeader(CharSequence name) {
        checkStarted();
        super.removeHeader(name);
        return self();
    }

    @Override
    public synchronized CompositeRequest addParams(Map<String, String> params) {
        checkStarted();
        super.addParams(params);
        return self();
    }

    @Override
    public synchronized CompositeRequest addParam(String name, String value) {
        checkStarted();
        super.addParam(name, value);
        return self();
    }

    @Override
    public synchronized CompositeRequest handle(Consumer<Handle> handle) {
        checkStarted();
        super.handle(handle);
        return self();
    }

    @Override
    public synchronized CompositeRequest handler(Handler handler) {
        checkStarted();
        super.handler(handler);
        return self();
    }

    @Override
    public boolean isSegmented() {
        return this.type == 2;
    }

    @Override
    public boolean isMultipart() {
        return this.type == 1;
    }

    @Override
    public CompositeRequest copy() {
        final CompositeRequest copied = new CompositeRequest(builder, client,
                request, method(), uri().toString());
        copyTo(this, copied);

        copied.attributes.putAll(attributes);
        copied.files.addAll(files);
        copied.multipartEncode(multipartEncode);
        if (bytes != null) {
            copied.body(bytes);
        }
        if (file != null) {
            copied.body(file);
        }

        copied.type = type;

        return copied;
    }

    private void checkStarted() {
        if (started) {
            throw new IllegalStateException("Request has started to execute" +
                    " and the modification isn't allowed");
        }
    }

    private CompositeRequest self() {
        return this;
    }

    private void cleanBody() {
        this.bytes = null;
        this.file = null;
    }

    private void checkMultipartFile() {
        if (!multipartEncode) {
            throw new IllegalArgumentException("File is not allowed to be added, maybe multipart is false?");
        }
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }

}
