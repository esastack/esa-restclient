package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Request;
import esa.httpclient.core.RequestBaseConfig;

import java.util.Map;

public interface RestRequest extends Request, RequestBaseConfig {

    @Override
    RestRequest addParams(Map<String, String> params);

    @Override
    RestRequest addParam(String name, String value);

    @Override
    RestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequest setHeader(CharSequence name, CharSequence value);

    RestRequest cookie(Cookie cookie);

    RestRequest cookie(String name, String value);

    RestRequest contentType(ContentType contentType);

    RestRequest contentType(ContentTypeFactory contentTypeFactory);

    RestRequest accept(AcceptType... acceptTypes);

    RestRequest accept(AcceptTypeFactory acceptTypeFactory);

    RestRequest acceptResolver(AcceptTypeResolver acceptTypeResolver);

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

    Object body();

    RequestContext context();

}
