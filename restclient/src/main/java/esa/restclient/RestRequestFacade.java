package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface RestRequestFacade extends ExecutableRestRequest {

    /**
     * Fills the request's entity with given entity.The entity will be encoded to
     * be request,s {@link RequestBodyContent} by {@link esa.restclient.codec.Encoder#encode}.
     *
     * @param entity entity
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(Object entity);

    /**
     * Fills the request's entity with given content.The content will be encoded to
     * be request,s {@link RequestBodyContent} by {@link esa.restclient.codec.Encoder#encode}.
     *
     * @param content content
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(String content);

    /**
     * Fills the request's entity with given data.The data will be encoded to
     * be request,s {@link RequestBodyContent} by {@link esa.restclient.codec.Encoder#encode}.
     *
     * @param data data
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(byte[] data);

    /**
     * Fills the request's entity with given file.The file will be encoded to
     * be request,s {@link RequestBodyContent} by {@link esa.restclient.codec.Encoder#encode}.
     *
     * @param file data
     * @return RestFileRequest
     */
    RestFileRequest entity(File file);

    /**
     * Converts to a {@link RestMultipartRequest} which can be used to handle the body
     * by multipart encoding.
     *
     * @return RestMultipartRequest
     */
    RestMultipartRequest multipart();

    @Override
    RestRequestFacade addParams(Map<String, String> params);

    @Override
    RestRequestFacade addParam(String name, String value);

    @Override
    RestRequestFacade cookie(Cookie cookie);

    @Override
    RestRequestFacade cookie(String name, String value);

    @Override
    RestRequestFacade cookies(List<Cookie> cookies);

    @Override
    RestRequestFacade contentType(ContentType contentType);

    @Override
    RestRequestFacade accept(ContentType... contentTypes);

    @Override
    RestRequestFacade addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestFacade addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade removeHeader(CharSequence name);

    @Override
    RestRequestFacade enableUriEncode();

    @Override
    RestRequestFacade readTimeout(int readTimeout);

    @Override
    RestRequestFacade disableExpectContinue();

    @Override
    RestRequestFacade maxRedirects(int maxRedirects);

    @Override
    RestRequestFacade maxRetries(int maxRetries);

}
