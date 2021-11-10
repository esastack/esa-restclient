package io.esastack.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpVersion;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.codec.DecodeAdviceContext;
import io.esastack.restclient.codec.impl.DecodeChainImpl;
import io.esastack.restclient.utils.CookiesUtil;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RestResponseBaseImpl implements RestResponseBase {

    private final RestRequestBase request;
    private final HttpResponse response;
    private final RestClientOptions clientOptions;

    RestResponseBaseImpl(
            RestRequestBase request,
            HttpResponse response,
            RestClientOptions clientOptions) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(clientOptions, "clientOptions");
        this.request = request;
        this.response = response;
        this.clientOptions = clientOptions;
    }

    @Override
    public int status() {
        return response.status();
    }

    @Override
    public HttpHeaders trailers() {
        return response.trailers();
    }

    @Override
    public HttpVersion version() {
        return response.version();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public <T> T bodyToEntity(Class<T> entityClass) throws Exception {
        return bodyToEntity((Type) entityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T bodyToEntity(Type genericType) throws Exception {
        DecodeAdviceContext decodeContext = new DecodeChainImpl(
                request,
                this,
                clientOptions,
                getClass(genericType),
                genericType,
                response.body().getByteBuf());
        return (T) decodeContext.next();
    }

    @Override
    public List<Cookie> cookies(String name) {
        return CookiesUtil.getCookies(name, headers(), true);
    }

    @Override
    public Map<String, List<Cookie>> cookiesMap() {
        return CookiesUtil.getCookiesMap(headers(), true);
    }

    @Override
    public MediaType contentType() {
        String contentType = headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (StringUtils.isNotBlank(contentType)) {
            return MediaTypeUtil.valueOf(contentType);
        }
        return null;
    }

    /**
     * Returns the object representing the class or interface that declared
     * the supplied {@code type}.
     *
     * @param type {@code Type} to inspect.
     * @return the class or interface that declared the supplied {@code type}.
     */
    private static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class) {
                return (Class<?>) parameterizedType.getRawType();
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
    private static Class<?> getArrayClass(Class<?> c) {
        try {
            Object o = Array.newInstance(c, 0);
            return o.getClass();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
