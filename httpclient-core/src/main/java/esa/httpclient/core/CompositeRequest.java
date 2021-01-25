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
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.netty.NettyHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final byte INIT_STATUS = -1;
    private static final byte PLAIN_PREPARING_STATUS = 0;
    private static final byte MULTIPART_PREPARING_STATUS = 1;
    private static final byte SEGMENT_PREPARING_STATUS = 2;
    private static final byte FILE_PREPARING_STATUS = 3;
    private static final byte PLAIN_EXECUTED_STATUS = 4;
    private static final byte MULTIPART_EXECUTED_STATUS = 5;
    private static final byte SEGMENT_EXECUTED_STATUS = 6;
    private static final byte FILE_EXECUTED_STATUS = 7;

    private final Object monitor = new Object();
    private final NettyHttpClient client;
    private final Supplier<SegmentRequest> request;

    /**
     * Body data
     */
    private final MultiValueMap<String, String> attrs = new HashMultiValueMap<>();
    private final List<MultipartFileItem> files = new LinkedList<>();

    private Buffer buffer;
    private File file;
    private boolean multipartEncode = true;

    /**
     *-1: init
     * 0: plain-preparing;
     * 1: multipart-preparing;
     * 2: segment-preparing;
     * 3: file-preparing;
     * 4: plain-executed;
     * 5: multipart-executed;
     * 6: segment-executed;
     * 7: file-executed;
     */
    private volatile byte status = INIT_STATUS;

    public CompositeRequest(HttpClientBuilder builder,
                            NettyHttpClient client,
                            Supplier<SegmentRequest> request,
                            HttpMethod method,
                            String uri) {
        super(builder, method, uri);
        Checks.checkNotNull(client, "NettyHttpClient must not be null");
        Checks.checkNotNull(request, "SegmentRequest must not be null");
        this.client = client;
        this.request = request;
    }

    @Override
    public CompletableFuture<HttpResponse> execute() {
        final byte newStatus = status >= 0 ? (byte) (status + (byte) 4) : PLAIN_EXECUTED_STATUS;
        checkNotStartedAndUpdateStatus(newStatus);
        return client.execute(this, ctx, handle, handler);
    }

    @Override
    public PlainRequest body(Buffer data) {
        checkNotStartedAndUpdateStatus(PLAIN_PREPARING_STATUS);
        this.buffer = data;
        return self();
    }

    @Override
    public MultipartRequest multipart() {
        checkNotStartedAndUpdateStatus(MULTIPART_PREPARING_STATUS);
        return this;
    }

    @Override
    public SegmentRequest segment() {
        checkNotStartedAndUpdateStatus(SEGMENT_PREPARING_STATUS);
        return request.get();
    }

    @Override
    public FileRequest body(File file) {
        Checks.checkNotNull(file, "File must not b null");
        checkNotStartedAndUpdateStatus(FILE_PREPARING_STATUS);
        this.file = file;
        return self();
    }

    @Override
    public Buffer buffer() {
        return buffer;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public MultipartRequest multipartEncode(boolean multipartEncode) {
        checkStarted();
        this.multipartEncode = multipartEncode;
        return self();
    }

    @Override
    public boolean multipartEncode() {
        return multipartEncode;
    }

    @Override
    public MultipartRequest attr(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        checkStarted();
        attrs.add(name, value);
        return self();
    }

    @Override
    public MultipartRequest file(String name, File file) {
        if (illegalArgs(name, file)) {
            return self();
        }
        return file(name, file.getName(), file, DEFAULT_BINARY_CONTENT_TYPE, false);
    }

    @Override
    public MultipartRequest file(String name, File file, String contentType) {
        if (illegalArgs(name, file)) {
            return self();
        }
        return file(name, file.getName(), file, contentType,
                DEFAULT_TEXT_CONTENT_TYPE.equalsIgnoreCase(contentType));
    }

    @Override
    public MultipartRequest file(String name,
                                 File file,
                                 String contentType,
                                 boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        return file(name, file.getName(), file, contentType, isText);
    }

    @Override
    public MultipartRequest file(String name,
                                 String filename,
                                 File file,
                                 String contentType,
                                 boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        checkStarted();
        checkMultipartFile();
        files.add(new MultipartFileItem(name, filename, file, contentType, isText));
        return self();
    }

    @Override
    public MultiValueMap<String, String> attrs() {
        if (status == MULTIPART_PREPARING_STATUS || status == MULTIPART_EXECUTED_STATUS) {
            return new HashMultiValueMap<>(attrs);
        } else {
            return new HashMultiValueMap<>();
        }
    }

    @Override
    public List<MultipartFileItem> files() {
        if (status == MULTIPART_PREPARING_STATUS || status == MULTIPART_EXECUTED_STATUS) {
            return new ArrayList<>(files);
        } else {
            return Collections.emptyList();
        }
    }

    ////////**********************COMMON SETTER************************////////

    @Override
    public CompositeRequest uriEncodeEnabled(Boolean uriEncodeEnabled) {
        checkStarted();
        super.uriEncodeEnabled(uriEncodeEnabled);
        return self();
    }

    @Override
    public CompositeRequest expectContinueEnabled(Boolean expectContinueEnabled) {
        checkStarted();
        super.expectContinueEnabled(expectContinueEnabled);
        return self();
    }

    @Override
    public CompositeRequest maxRedirects(int maxRedirects) {
        checkStarted();
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public CompositeRequest maxRetries(int maxRetries) {
        checkStarted();
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public CompositeRequest readTimeout(int readTimeout) {
        checkStarted();
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public CompositeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        checkStarted();
        super.addHeaders(headers);
        return self();
    }

    @Override
    public CompositeRequest addHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.addHeader(name, value);
        return self();
    }

    @Override
    public CompositeRequest setHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.setHeader(name, value);
        return self();
    }

    @Override
    public CompositeRequest removeHeader(CharSequence name) {
        checkStarted();
        super.removeHeader(name);
        return self();
    }

    @Override
    public CompositeRequest addParams(Map<String, String> params) {
        checkStarted();
        super.addParams(params);
        return self();
    }

    @Override
    public CompositeRequest addParam(String name, String value) {
        checkStarted();
        super.addParam(name, value);
        return self();
    }

    @Override
    public CompositeRequest handle(Consumer<Handle> handle) {
        checkStarted();
        super.handle(handle);
        return self();
    }

    @Override
    public CompositeRequest handler(Handler handler) {
        checkStarted();
        super.handler(handler);
        return self();
    }

    @Override
    public boolean isSegmented() {
        return this.status == SEGMENT_PREPARING_STATUS || this.status == SEGMENT_EXECUTED_STATUS;
    }

    @Override
    public boolean isMultipart() {
        return this.status == MULTIPART_PREPARING_STATUS || this.status == MULTIPART_EXECUTED_STATUS;
    }

    @Override
    public boolean isFile() {
        return this.status == FILE_PREPARING_STATUS || this.status == FILE_EXECUTED_STATUS;
    }

    @Override
    public CompositeRequest copy() {
        final CompositeRequest copied = new CompositeRequest(builder, client,
                request, method(), uri().toString());
        copyTo(this, copied);

        copied.attrs.putAll(attrs);
        copied.files.addAll(files);
        copied.multipartEncode(multipartEncode);
        if (buffer != null) {
            copied.body(buffer.copy());
        }
        if (file != null) {
            copied.body(file);
        }

        copied.status = status >= PLAIN_EXECUTED_STATUS ? (byte) (status - (byte) 4) : status;

        return copied;
    }

    private void checkStarted() {
        if (status >= PLAIN_EXECUTED_STATUS) {
            throw new IllegalStateException("Request has started to execute" +
                    " and the modification isn't allowed");
        }
    }

    private CompositeRequest self() {
        return this;
    }

    private void checkMultipartFile() {
        if (!multipartEncode) {
            throw new IllegalArgumentException("File is not allowed to be added, maybe multipart is false?");
        }
    }

    private void checkNotStartedAndUpdateStatus(byte newStatus) {
        synchronized (monitor) {
            if (status >= PLAIN_EXECUTED_STATUS) {
                throw new IllegalStateException("The execute() has been called before");
            } else if (status != INIT_STATUS && newStatus < PLAIN_EXECUTED_STATUS) {
                throw new IllegalStateException(decideType() + " request has been set before");
            }
            this.status = newStatus;
        }
    }

    private String decideType() {
        switch (status) {
            case PLAIN_PREPARING_STATUS:
                return "PLAIN";
            case MULTIPART_PREPARING_STATUS:
                return "MULTIPART";
            case SEGMENT_PREPARING_STATUS:
                return "SEGMENT";
            case FILE_PREPARING_STATUS:
                return "FILE";
            default:
                return "PLAIN";
        }
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }

}
