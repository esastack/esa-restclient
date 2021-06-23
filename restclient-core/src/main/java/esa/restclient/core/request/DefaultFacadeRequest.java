package esa.restclient.core.request;

import esa.commons.http.Cookie;
import esa.commons.http.HttpMethod;
import esa.restclient.core.MediaType;
import esa.restclient.core.RestClient;
import esa.restclient.core.RestClientBuilder;
import esa.restclient.core.response.RestHttpResponse;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class DefaultFacadeRequest extends DefaultExecutableRequest implements FacadeRequest {


    public DefaultFacadeRequest(String url, HttpMethod httpMethod, RestClientBuilder builder, RestClient client) {
        super(url, httpMethod, builder, client);
    }

    @Override
    public EntityRequest bodyEntity(Object entity) {
        return bodyEntity(entity, MediaType.APPLICATION_JSON_UTF8);
    }

    @Override
    public EntityRequest bodyEntity(Object entity, MediaType mediaType) {
        contentType(MediaType.APPLICATION_JSON_UTF8);
        return new DefaultEntityRequest(this, entity);
    }

    @Override
    public FileRequest bodyFile(File file) {
        return bodyFile(file, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public FileRequest bodyFile(File file, MediaType mediaType) {
        contentType(mediaType);
        return new DefaultFileRequest(this, file);
    }

    @Override
    public MultipartRequest multipart() {
        contentType(MediaType.MULTIPART_FORM_DATA);
        return new DefaultMultipartRequest(this);
    }

    @Override
    public CompletionStage<RestHttpResponse> execute() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }


    @Override
    public FacadeRequest readTimeout(long readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public FacadeRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public FacadeRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public FacadeRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public FacadeRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public FacadeRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public FacadeRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public FacadeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public FacadeRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public FacadeRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public FacadeRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public FacadeRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public FacadeRequest accept(MediaType... mediaTypes) {
        super.accept(mediaTypes);
        return self();
    }

    @Override
    public FacadeRequest contentType(MediaType mediaType) {
        super.contentType(mediaType);
        return self();
    }

    @Override
    public FacadeRequest property(String name, Object value) {
        super.property(name, value);
        return self();
    }

    private FacadeRequest self() {
        return this;
    }
}
