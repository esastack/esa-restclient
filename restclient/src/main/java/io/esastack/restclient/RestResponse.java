package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.Response;

import java.util.List;
import java.util.Map;

public interface RestResponse extends Response {

    List<Cookie> cookies(String name);

    Map<String, List<Cookie>> cookiesMap();

    /**
     * @return contentType of response
     */
    MediaType contentType();
}
