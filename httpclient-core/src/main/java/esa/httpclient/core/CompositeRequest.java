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
import esa.commons.collection.MultiMaps;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpMethod;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.netty.NettyHttpClient;
import esa.httpclient.core.util.MultiValueMapUtils;

import java.io.File;
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

    private static final byte STATE_INIT = -1;
    private static final byte STATE_PLAIN_PREPARING = 0;
    private static final byte STATE_MULTIPART_PREPARING = 1;
    private static final byte STATE_SEGMENT_PREPARING = 2;
    private static final byte STATE_FILE_PREPARING = 3;
    private static final byte STATE_PLAIN_EXECUTED = 4;
    private static final byte STATE_MULTIPART_EXECUTED = 5;
    private static final byte STATE_SEGMENT_EXECUTED = 6;
    private static final byte STATE_FILE_EXECUTED = 7;

    private final Object monitor = new Object();
    private final NettyHttpClient client;
    private final Supplier<SegmentRequest> request;

    /**
     * Body data which isn't always exists, so it's good to be instantiate lazily.
     */
    private MultiValueMap<String, String> attrs;
    private List<MultipartFileItem> files;

    private Buffer buffer;
    private File file;
    private boolean useMultipartEncode = true;

    /**
     * A bitmask where the bits are defined as
     * <ul>
     *     <li>{@link #STATE_INIT}</li>
     *     <li>{@link #STATE_PLAIN_PREPARING}</li>
     *     <li>{@link #STATE_MULTIPART_PREPARING}</li>
     *     <li>{@link #STATE_SEGMENT_PREPARING}</li>
     *     <li>{@link #STATE_FILE_PREPARING}</li>
     *     <li>{@link #STATE_PLAIN_EXECUTED}</li>
     *     <li>{@link #STATE_MULTIPART_EXECUTED}</li>
     *     <li>{@link #STATE_SEGMENT_EXECUTED}</li>
     *     <li>{@link #STATE_FILE_EXECUTED}</li>
     * </ul>
     */
    private volatile byte status = STATE_INIT;

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
        final byte newStatus = status >= 0 ? (byte) (status + (byte) 4) : STATE_PLAIN_EXECUTED;
        checkNotStartedAndUpdateStatus(newStatus);
        return client.execute(this, ctx, handle, handler);
    }

    @Override
    public PlainRequest body(Buffer data) {
        checkNotStartedAndUpdateStatus(STATE_PLAIN_PREPARING);
        this.buffer = data;
        return self();
    }

    @Override
    public MultipartRequest multipart() {
        checkNotStartedAndUpdateStatus(STATE_MULTIPART_PREPARING);
        return this;
    }

    @Override
    public SegmentRequest segment() {
        checkNotStartedAndUpdateStatus(STATE_SEGMENT_PREPARING);
        SegmentRequest segmentRequest = request.get();
        HttpRequestBaseImpl.copyTo(this, (HttpRequestBaseImpl) segmentRequest);
        return segmentRequest;
    }

    @Override
    public FileRequest body(File file) {
        Checks.checkNotNull(file, "File must not b null");
        checkNotStartedAndUpdateStatus(STATE_FILE_PREPARING);
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
    public MultipartRequest multipartEncode(boolean useMultipartEncode) {
        checkStarted();
        this.useMultipartEncode = useMultipartEncode;
        return self();
    }

    @Override
    public boolean multipartEncode() {
        return isMultipart() && useMultipartEncode;
    }

    @Override
    public MultipartRequest attr(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        checkStarted();
        checkAttrsNotNull();
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
        checkFilesNotNull();
        files.add(new MultipartFileItem(name, filename, file, contentType, isText));
        return self();
    }

    @Override
    public MultiValueMap<String, String> attrs() {
        if (attrs != null && (status == STATE_MULTIPART_PREPARING || status == STATE_MULTIPART_EXECUTED)) {
            return MultiValueMapUtils.unmodifiableMap(attrs);
        } else {
            return MultiMaps.emptyMultiMap();
        }
    }

    @Override
    public List<MultipartFileItem> files() {
        if (files != null && (status == STATE_MULTIPART_PREPARING || status == STATE_MULTIPART_EXECUTED)) {
            return Collections.unmodifiableList(files);
        } else {
            return Collections.emptyList();
        }
    }

    ////////**********************COMMON SETTER************************////////

    @Override
    public CompositeRequest enableUriEncode() {
        checkStarted();
        super.enableUriEncode();
        return self();
    }

    @Override
    public CompositeRequest disableExpectContinue() {
        checkStarted();
        super.disableExpectContinue();
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
        super.addHeaders(headers);
        return self();
    }

    @Override
    public CompositeRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public CompositeRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public CompositeRequest removeHeader(CharSequence name) {
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
        return this.status == STATE_SEGMENT_PREPARING || this.status == STATE_SEGMENT_EXECUTED;
    }

    @Override
    public boolean isMultipart() {
        return this.status == STATE_MULTIPART_PREPARING || this.status == STATE_MULTIPART_EXECUTED;
    }

    @Override
    public boolean isFile() {
        return this.status == STATE_FILE_PREPARING || this.status == STATE_FILE_EXECUTED;
    }

    @Override
    public CompositeRequest copy() {
        final CompositeRequest copied = new CompositeRequest(builder, client,
                request, method(), uri().toString());
        copyTo(this, copied);

        if (attrs != null) {
            List<String> values;
            for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    for (String value : values) {
                        copied.attr(entry.getKey(), value);
                    }
                }
            }
        }

        if (files != null) {
            for (MultipartFileItem item : files) {
                copied.file(item.name(), item.fileName(), item.file(), item.contentType(), item.isText());
            }
        }

        copied.multipartEncode(useMultipartEncode);
        if (buffer != null) {
            copied.body(buffer.copy());
        }
        if (file != null) {
            copied.body(file);
        }

        copied.status = status >= STATE_PLAIN_EXECUTED ? (byte) (status - (byte) 4) : status;

        return copied;
    }

    private void checkStarted() {
        if (status >= STATE_PLAIN_EXECUTED) {
            throw new IllegalStateException("Request's execute() has been called " +
                    " and the modification isn't allowed");
        }
    }

    private CompositeRequest self() {
        return this;
    }

    private void checkMultipartFile() {
        if (!useMultipartEncode) {
            throw new IllegalArgumentException("File is not allowed to be added, maybe multipart is false?");
        }
    }

    private void checkNotStartedAndUpdateStatus(byte newStatus) {
        synchronized (monitor) {
            if (status >= STATE_PLAIN_EXECUTED) {
                throw new IllegalStateException("The execute() has been called before");
            } else if (status != STATE_INIT && newStatus < STATE_PLAIN_EXECUTED) {
                throw new IllegalStateException(decideType() + " request has been set before");
            }
            this.status = newStatus;
        }
    }

    private void checkAttrsNotNull() {
        if (attrs == null) {
            synchronized (monitor) {
                if (attrs == null) {
                    attrs = new HashMultiValueMap<>();
                }
            }
        }
    }

    private void checkFilesNotNull() {
        if (files == null) {
            synchronized (monitor) {
                if (files == null) {
                    files = new LinkedList<>();
                }
            }
        }
    }

    private String decideType() {
        switch (status) {
            case STATE_MULTIPART_PREPARING:
                return "MULTIPART";
            case STATE_SEGMENT_PREPARING:
                return "SEGMENT";
            case STATE_FILE_PREPARING:
                return "FILE";
            default:
                return "PLAIN";
        }
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }

    @Override
    public String toString() {
        return uri().toString();
    }
}
