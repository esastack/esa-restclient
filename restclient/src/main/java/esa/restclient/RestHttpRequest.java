package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Request;
import esa.httpclient.core.RequestBaseConfig;

import java.util.Map;
import java.util.Set;

public interface RestHttpRequest extends Request, RequestBaseConfig {

    int maxRetries();

    int maxRedirects();

    @Override
    RestHttpRequest addParams(Map<String, String> params);

    @Override
    RestHttpRequest addParam(String name, String value);

    @Override
    RestHttpRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestHttpRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestHttpRequest setHeader(CharSequence name, CharSequence value);

    RestHttpRequest cookie(Cookie cookie);

    RestHttpRequest cookie(String name, String value);

    RestHttpRequest contentType(ContentType contentType);

    RestHttpRequest contentType(ContentTypeFactory contentTypeFactory);

    RestHttpRequest accept(AcceptType... acceptTypes);

    RestHttpRequest accept(AcceptTypeFactory acceptTypeFactory);

    RestHttpRequest acceptResolver(AcceptTypeResolver acceptTypeResolver);

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    int readTimeout();

    /**
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncode();

    boolean isUseExpectContinue();

    Object bodyEntity();

    RequestContext context();

}
