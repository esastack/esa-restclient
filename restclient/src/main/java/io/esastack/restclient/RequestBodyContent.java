package io.esastack.restclient;

import io.esastack.httpclient.core.MultipartBody;

import java.io.File;

/**
 * Used to support the transfer of several special content classes can be received by
 * httpclient between HttpClient and RestClient
 *
 * @param <T> the class of content
 */
public final class RequestBodyContent<T> implements BodyContent<T> {

    private final T content;

    private RequestBodyContent(T content) {
        this.content = content;
    }

    @Override
    public T content() {
        return content;
    }

    /**
     * The underlying httpclient will send the byte array directly.
     *
     * @param content content
     * @return RequestBodyContent
     */
    public static RequestBodyContent<byte[]> of(byte[] content) {
        return new RequestBodyContent<>(content);
    }

    /**
     * The underlying httpclient will send the file by
     * {@link io.netty.channel.FileRegion} which supports zero-copy file transfer
     *
     * @param content content
     * @return RequestBodyContent
     */
    public static RequestBodyContent<File> of(File content) {
        return new RequestBodyContent<>(content);
    }

    /**
     * The underlying httpclient will send the request in multipart encoding
     *
     * @param content content
     * @return RequestBodyContent
     */
    public static RequestBodyContent<MultipartBody> of(MultipartBody content) {
        return new RequestBodyContent<>(content);
    }

}
