package esa.restclient.core.request;

import esa.commons.http.Cookie;
import esa.restclient.core.MediaType;
import esa.restclient.core.RestClient;
import esa.restclient.core.response.RestHttpResponse;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ExecutableRequest extends HttpRequest {

    CompletionStage<RestHttpResponse> execute();

    int maxRetries();

    int maxRedirects();

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    long readTimeout();

    ExecutableRequest readTimeout(long readTimeout);

    ExecutableRequest maxRedirects(int maxRedirects);

    ExecutableRequest maxRetries(int maxRetries);

    ExecutableRequest disableExpectContinue();

    boolean isUseExpectContinue();

    ExecutableRequest enableUriEncode();

    /**
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncode();

    @Override
    ExecutableRequest addParams(Map<String, String> params);

    @Override
    ExecutableRequest addParam(String name, String value);

    @Override
    ExecutableRequest cookie(Cookie cookie);

    @Override
    ExecutableRequest cookie(String name, String value);

    @Override
    ExecutableRequest contentType(MediaType mediaType);

    @Override
    ExecutableRequest accept(MediaType... mediaTypes);

    @Override
    ExecutableRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    ExecutableRequest addHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRequest setHeader(CharSequence name, CharSequence value);

    RestClient client();
}
