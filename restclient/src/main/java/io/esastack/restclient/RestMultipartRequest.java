package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.MultipartConfigure;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

import java.io.File;
import java.util.Map;

public interface RestMultipartRequest extends ExecutableRestRequest, MultipartConfigure {

    /**
     * add multipart attribute,this method is not thread-safe.
     *
     * @param name  name
     * @param value value
     * @return this
     */
    @Override
    RestMultipartRequest attr(String name, String value);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name name
     * @param file value
     * @return this
     */
    @Override
    RestMultipartRequest file(String name, File file);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param file        file
     * @param contentType contentType
     * @return this
     */
    @Override
    RestMultipartRequest file(String name, File file, String contentType);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param file        file
     * @param contentType contentType
     * @param isText      isText
     * @return this
     */
    @Override
    RestMultipartRequest file(String name, File file, String contentType, boolean isText);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param filename    filename
     * @param file        file
     * @param contentType contentType
     * @param isText      isText
     * @return this
     */
    @Override
    RestMultipartRequest file(String name, String filename, File file, String contentType, boolean isText);

    @Override
    RestMultipartRequest addParams(Map<String, String> params);

    @Override
    RestMultipartRequest addParam(String name, String value);

    @Override
    RestMultipartRequest cookie(String name, String value);

    @Override
    RestMultipartRequest cookie(Cookie... cookies);

    @Override
    RestMultipartRequest contentType(MediaType contentType);

    @Override
    RestMultipartRequest accept(MediaType... acceptTypes);

    @Override
    RestMultipartRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestMultipartRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestMultipartRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestMultipartRequest removeHeader(CharSequence name);

    @Override
    RestMultipartRequest enableUriEncode();

    @Override
    RestMultipartRequest readTimeout(long readTimeout);

    @Override
    RestMultipartRequest disableExpectContinue();

    @Override
    RestMultipartRequest maxRedirects(int maxRedirects);

    @Override
    RestMultipartRequest maxRetries(int maxRetries);

    @Override
    RestMultipartRequest encoder(Encoder encoder);

    @Override
    RestMultipartRequest decoder(Decoder decoder);

}
