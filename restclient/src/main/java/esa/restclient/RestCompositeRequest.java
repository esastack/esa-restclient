package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.MultipartRequest;
import esa.restclient.exec.RestRequestExecutor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class RestCompositeRequest extends AbstractExecutableRestRequest
        implements RestRequestFacade, RestEntityRequest,
        RestFileRequest, RestMultipartRequest {

    private Object entity;

    public RestCompositeRequest(CompositeRequest request,
                                RestClientConfig clientConfig,
                                RestRequestExecutor requestExecutor) {
        super(request, clientConfig, requestExecutor);
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
    public RestCompositeRequest readTimeout(int readTimeout) {
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
        return target.file();
    }

    @Override
    public RestMultipartRequest multipartEncode(boolean multipartEncode) {
        ((MultipartRequest) target).multipartEncode(multipartEncode);
        return self();
    }

    @Override
    public RestMultipartRequest attr(String name, String value) {
        target.attr(name, value);
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file) {
        target.file(name, file);
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType) {
        target.file(name, file, contentType);
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType, boolean isText) {
        target.file(name, file, contentType, isText);
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, String filename, File file, String contentType, boolean isText) {
        target.file(name, filename, file, contentType, isText);
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
    public RestCompositeRequest accept(ContentType... contentTypes) {
        super.accept(contentTypes);
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
    public RestEntityRequest entity(Object entity) {
        Checks.checkNotNull(entity, "entity");
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
        setContentTypeIfAbsent(ContentType.APPLICATION_JSON_UTF8);
        this.entity = entity;
        return self();
    }

    @Override
    public RestEntityRequest entity(String content) {
        Checks.checkNotNull(content, "content");
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
        setContentTypeIfAbsent(ContentType.TEXT_PLAIN);
        this.entity = content;
        return self();
    }

    @Override
    public RestEntityRequest entity(byte[] data) {
        Checks.checkNotNull(data, "data");
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
        setContentTypeIfAbsent(ContentType.APPLICATION_OCTET_STREAM);
        this.entity = data;
        return self();
    }

    @Override
    public RestFileRequest entity(File file) {
        Checks.checkNotNull(file, "file");
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
        setContentTypeIfAbsent(ContentType.FILE);
        target.body(file);
        return self();
    }

    //TODO 不允许多次设置BODY
    @Override
    public RestMultipartRequest multipart() {
        setContentTypeIfAbsent(ContentType.MULTIPART_FORM_DATA);
        target.multipart();
        return self();
    }

    private void setContentTypeIfAbsent(ContentType contentType) {
        if (this.contentType == null) {
            contentType(contentType);
        }
    }

    private RestCompositeRequest self() {
        return this;
    }
}
