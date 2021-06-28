package esa.restclient.response;

import esa.commons.Checks;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpResponse;
import esa.restclient.MediaType;
import esa.restclient.codec.BodyProcessor;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DefaultRestHttpResponse implements RestHttpResponse {

    private final HttpVersion httpVersion;
    private final int status;
    private final HttpHeaders headers;
    private final HttpHeaders trailers;
    private final InputStream bodyStream;
    private final BodyProcessor bodyProcessor;

    public DefaultRestHttpResponse(
            HttpResponse response,
            BodyProcessor bodyProcessor) {
        Checks.checkNotNull(response, "Response must be not null!");
        Checks.checkNotNull(bodyProcessor, "CodecManager must be not null!");
        this.httpVersion = response.version();
        this.status = response.status();
        this.headers = response.headers();
        this.trailers = response.trailers();
        this.bodyProcessor = bodyProcessor;

        Buffer buffer = response.body();
        if (buffer != null) {
            this.bodyStream = new InputStream() {
                @Override
                public int read() {
                    return buffer.readInt();
                }

                @Override
                public int read(byte[] b, int off, int len) {
                    int readableBytes = buffer.readableBytes();
                    if (readableBytes >= len) {
                        buffer.readBytes(b, off, len);
                        return len;
                    } else if (readableBytes > 0) {
                        buffer.readBytes(b, off, readableBytes);
                        return readableBytes;
                    }
                    return -1;
                }
            };
        } else {
            this.bodyStream = null;
        }
    }

    @Override
    public <T> T bodyToEntity(Class<T> entityClass) {
        if (entityClass == null) {
            return null;
        }
        return (T) bodyProcessor.read(entityClass, entityClass, contentType(), headers, bodyStream);
    }

    @Override
    public <T> T bodyToEntity(Type type) {
        if (type == null) {
            return null;
        }
        return (T) bodyProcessor.read(type.getClass(), getClass(type), contentType(), headers, bodyStream);
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public InputStream bodyStream() {
        return bodyStream;
    }

    @Override
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public MediaType contentType() {
        String contentTypeHeader = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeHeader == null) {
            return null;
        }
        return MediaType.parseMediaType(contentTypeHeader);
    }

    @Override
    public HttpVersion version() {
        return httpVersion;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Returns the object representing the class or interface that declared
     * the supplied {@code type}.
     *
     * @param type {@code Type} to inspect.
     * @return the class or interface that declared the supplied {@code type}.
     */
    private static Class getClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class) {
                return (Class) parameterizedType.getRawType();
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType array = (GenericArrayType) type;
            final Class<?> componentRawType = getClass(array.getGenericComponentType());
            return getArrayClass(componentRawType);
        }
        throw new IllegalArgumentException("Type parameter " + type.toString() + " not a class or " +
                "parameterized type whose raw type is a class");
    }

    /**
     * Get Array class of component class.
     *
     * @param c the component class of the array
     * @return the array class.
     */
    private static Class getArrayClass(Class c) {
        try {
            Object o = Array.newInstance(c, 0);
            return o.getClass();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
