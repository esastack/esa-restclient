package io.esastack.restclient;

import esa.commons.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.GenericEntity;

import java.io.File;
import java.util.Map;

public interface RestRequestFacade extends ExecutableRestRequest {

    /**
     * Fills the request's entity with given entity.The entity will be encoded to
     * be request,s {@link io.esastack.restclient.codec.CodecResult} by {@link Encoder#encode}.
     *
     * @param entity entity
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(Object entity);

    /**
     * Fills the request's entity with given entity.The entity will be encoded to
     * be request,s {@link io.esastack.restclient.codec.CodecResult} by {@link Encoder#encode}.
     *
     * @param entity entity
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(GenericEntity<?> entity);

    /**
     * Fills the request's entity with given content.The content will be encoded to
     * be request,s {@link io.esastack.restclient.codec.CodecResult} by {@link Encoder#encode}.
     *
     * @param content content
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(String content);

    /**
     * Fills the request's entity with given data.The data will be encoded to
     * be request,s {@link io.esastack.restclient.codec.CodecResult} by {@link Encoder#encode}.
     *
     * @param data data
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(byte[] data);

    /**
     * Fills the request's entity with given file.The file will be encoded to
     * be request,s {@link io.esastack.restclient.codec.CodecResult} by {@link Encoder#encode}.
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
    RestRequestFacade cookie(String name, String value);

    @Override
    RestRequestFacade cookie(Cookie... cookies);

    @Override
    RestRequestFacade contentType(MediaType contentType);

    @Override
    RestRequestFacade accept(MediaType... acceptTypes);

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
    RestRequestFacade readTimeout(long readTimeout);

    @Override
    RestRequestFacade disableExpectContinue();

    @Override
    RestRequestFacade maxRedirects(int maxRedirects);

    @Override
    RestRequestFacade maxRetries(int maxRetries);

    @Override
    RestRequestFacade encoder(Encoder encoder);

    @Override
    RestRequestFacade decoder(Decoder decoder);


}
