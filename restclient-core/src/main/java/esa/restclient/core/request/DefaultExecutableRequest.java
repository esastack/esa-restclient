package esa.restclient.core.request;

import esa.commons.Checks;
import esa.commons.http.Cookie;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.config.RetryOptions;
import esa.restclient.core.MediaType;
import esa.restclient.core.RestClientConfig;
import esa.restclient.core.exec.RestRequestExecutor;
import esa.restclient.core.response.RestHttpResponse;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class DefaultExecutableRequest extends DefaultHttpRequest implements ExecutableRequest {
    private volatile int maxRedirects;
    private volatile int maxRetries = 0;
    private long readTimeout;
    private boolean useUriEncode;
    private final RestRequestExecutor requestExecutor;
    private boolean useExpectContinue;

    DefaultExecutableRequest(String url, HttpMethod httpMethod, RestClientConfig clientConfig, RestRequestExecutor requestExecutor) {
        super(url, httpMethod, clientConfig.version());
        Checks.checkNotNull(requestExecutor, "RequestExecutor must not be null");
        this.requestExecutor = requestExecutor;
        readTimeout(clientConfig.readTimeout());
        maxRedirects(clientConfig.maxRedirects());
        RetryOptions retryOptions = clientConfig.retryOptions();
        if (retryOptions != null) {
            maxRetries(clientConfig.retryOptions().maxRetries());
        }
        this.useExpectContinue = clientConfig.isUseExpectContinue();
    }

    DefaultExecutableRequest(DefaultExecutableRequest executableRequest) {
        super(executableRequest);
        readTimeout(executableRequest.readTimeout());
        maxRedirects(executableRequest.maxRedirects());
        maxRetries(executableRequest.maxRetries());
        if (executableRequest.uriEncode()) {
            enableUriEncode();
        }
        this.useExpectContinue = executableRequest.isUseExpectContinue();
        Checks.checkNotNull(executableRequest.requestExecutor, "RequestExecutor must not be null");
        this.requestExecutor = executableRequest.requestExecutor;
    }

    @Override
    public CompletionStage<RestHttpResponse> execute() {
        return requestExecutor.execute(this);
    }

    @Override
    public int maxRetries() {
        return maxRetries;
    }

    @Override
    public int maxRedirects() {
        return maxRedirects;
    }

    @Override
    public long readTimeout() {
        return readTimeout;
    }

    @Override
    public ExecutableRequest readTimeout(long readTimeout) {
        Checks.checkArg(readTimeout > 0, "ReadTimeout is " + readTimeout +
                " (expected > 0)");
        this.readTimeout = readTimeout;
        return self();
    }

    @Override
    public ExecutableRequest maxRedirects(int maxRedirects) {
        Checks.checkArg(maxRedirects >= 0, "MaxRedirects is " + maxRedirects +
                " (expected >= 0)");
        this.maxRedirects = maxRedirects;
        return self();
    }

    @Override
    public ExecutableRequest maxRetries(int maxRetries) {
        Checks.checkArg(maxRetries >= 1, "maxRetries is " + maxRetries +
                " (expected >= 1)");
        this.maxRetries = maxRetries;
        return self();
    }

    @Override
    public ExecutableRequest disableExpectContinue() {
        this.useExpectContinue = false;
        return self();
    }

    @Override
    public boolean isUseExpectContinue() {
        return useExpectContinue;
    }

    @Override
    public ExecutableRequest enableUriEncode() {
        this.useUriEncode = true;
        return self();
    }

    @Override
    public boolean uriEncode() {
        return useUriEncode;
    }

    @Override
    public ExecutableRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public ExecutableRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public ExecutableRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public ExecutableRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public InputStream getBodyStream() {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public ExecutableRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public ExecutableRequest accept(MediaType... mediaTypes) {
        super.accept(mediaTypes);
        return self();
    }

    @Override
    public ExecutableRequest contentType(MediaType mediaType) {
        super.contentType(mediaType);
        return self();
    }


    private ExecutableRequest self() {
        return this;
    }
}
