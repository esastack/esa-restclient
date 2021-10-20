package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.exec.RestRequestExecutor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class RestCompositeRequest extends AbstractExecutableRestRequest
        implements RestRequestFacade, RestFileRequest, RestMultipartRequest {

    private Object entity;

    RestCompositeRequest(CompositeRequest request,
                                RestClientOptions clientOptions,
                                RestRequestExecutor requestExecutor) {
        super(request, clientOptions, requestExecutor);
    }

    @Override
    public Object entity() {
        return entity;
    }

    @Override
    public RestCompositeRequest removeHeader(CharSequence name) {
        super.removeHeader(name);
        return self();
    }

    @Override
    public RestCompositeRequest readTimeout(long readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public RestCompositeRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public RestCompositeRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public RestCompositeRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public RestCompositeRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public File file() {
        return (File) entity;
    }

    @Override
    public RestMultipartRequest attr(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).attr(name, value);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file, contentType);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType, boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file, contentType, isText);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, String filename, File file, String contentType, boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, filename, file, contentType, isText);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestCompositeRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public RestCompositeRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public RestCompositeRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest cookies(List<Cookie> cookies) {
        super.cookies(cookies);
        return self();
    }

    @Override
    public RestCompositeRequest contentType(ContentType contentType) {
        super.contentType(contentType);
        return self();
    }

    @Override
    public RestCompositeRequest accept(AcceptType... acceptTypes) {
        super.accept(acceptTypes);
        return self();
    }

    @Override
    public RestCompositeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public RestCompositeRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest entity(Object entity) {
        Checks.checkNotNull(entity, "entity");
        checkEntityHadSet();
        setContentTypeIfAbsent(ContentType.APPLICATION_JSON_UTF8);
        this.entity = entity;
        return self();
    }

    @Override
    public ExecutableRestRequest entity(String content) {
        Checks.checkNotNull(content, "content");
        checkEntityHadSet();
        setContentTypeIfAbsent(ContentType.TEXT_PLAIN);
        this.entity = content;
        return self();
    }

    @Override
    public ExecutableRestRequest entity(byte[] data) {
        Checks.checkNotNull(data, "data");
        checkEntityHadSet();
        setContentTypeIfAbsent(ContentType.APPLICATION_OCTET_STREAM);
        this.entity = data;
        return self();
    }

    @Override
    public RestFileRequest entity(File file) {
        Checks.checkNotNull(file, "file");
        checkEntityHadSet();
        setContentTypeIfAbsent(ContentType.FILE);
        this.entity = file;
        return self();
    }

    @Override
    public RestMultipartRequest multipart() {
        checkEntityHadSet();
        setContentTypeIfAbsent(ContentType.MULTIPART_FORM_DATA);
        this.entity = new MultipartBodyImpl();
        return self();
    }

    private void checkEntityHadSet() {
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
    }

    private void setContentTypeIfAbsent(ContentType contentType) {
        if (this.contentType == null) {
            contentType(contentType);
        }
    }

    private RestCompositeRequest self() {
        return this;
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }
}
