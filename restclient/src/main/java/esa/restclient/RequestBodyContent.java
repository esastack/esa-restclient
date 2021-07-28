package esa.restclient;

import esa.httpclient.core.MultipartBody;

import java.io.File;

/**
 * Used to support the transfer of several special content classes can be received by
 * httpclient between HttpClient and RestClient
 *
 * @param <T> the class of content
 */
public final class RequestBodyContent<T> implements BodyContent<T> {

    /**
     * Use this flag to represent content type to avoid using content.getClass().equal().
     * <p>
     * <ul>
     *     <li>type = TYPE.BYTES:  The underlying httpclient will send the byte array directly.
     *     <li>type = TYPE.FILE:  The underlying httpclient will send the file by
     *     {@link io.netty.channel.FileRegion} which supports zero-copy file transfer
     *     <li>type = TYPE.MULTIPART:   The underlying httpclient will send the request in
     *     multipart encoding
     * </ul>
     */
    private final byte type;

    public static final class TYPE {
        public static final byte BYTES = 1;
        public static final byte FILE = 2;
        public static final byte MULTIPART = 3;
    }

    private final T content;

    private RequestBodyContent(byte type, T content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public byte type() {
        return type;
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
        return new RequestBodyContent<>(TYPE.BYTES, content);
    }

    /**
     * The underlying httpclient will send the file by
     * {@link io.netty.channel.FileRegion} which supports zero-copy file transfer
     *
     * @param content content
     * @return RequestBodyContent
     */
    public static RequestBodyContent<File> of(File content) {
        return new RequestBodyContent<>(TYPE.FILE, content);
    }

    /**
     * The underlying httpclient will send the request in multipart encoding
     *
     * @param content content
     * @return RequestBodyContent
     */
    public static RequestBodyContent<MultipartBody> of(MultipartBody content) {
        return new RequestBodyContent<>(TYPE.MULTIPART, content);
    }

}
