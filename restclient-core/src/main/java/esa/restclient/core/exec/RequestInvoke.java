package esa.restclient.core.exec;

import esa.commons.Checks;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequestFacade;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.SegmentRequest;
import esa.restclient.core.RestClientConfig;
import esa.restclient.core.codec.BodyProcessor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.DefaultRestHttpResponse;
import esa.restclient.core.response.RestHttpResponse;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RequestInvoke implements InvokeChain {
    private final HttpClient httpClient;
    private final BodyProcessor bodyProcessor;

    RequestInvoke(HttpClient httpClient, RestClientConfig clientConfig, BodyProcessor bodyProcessor) {
        Checks.checkNotNull(httpClient, "HttpClient must not be null");
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null");
        Checks.checkNotNull(bodyProcessor, "CodecManager must not be null");
        this.httpClient = httpClient;
        this.bodyProcessor = bodyProcessor;
    }


    @Override
    public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request) {
        return doRequest(request).thenApply((response ->
                new DefaultRestHttpResponse(response,
                        bodyProcessor
                )));
    }


    private CompletableFuture<HttpResponse> doRequest(RestHttpRequest baseRequest) {
        HttpMethod method = baseRequest.method();
        if (HttpMethod.GET.equals(method)) {
            final HttpRequestFacade targetRequest = httpClient.get(baseRequest.uri().toString());
            wrapRequest(baseRequest, targetRequest);
            return targetRequest.execute();
        } else if (HttpMethod.POST.equals(method)) {
            final HttpRequestFacade targetRequest = httpClient.post(baseRequest.uri().toString());
            wrapRequest(baseRequest, targetRequest);
            return writeBody(baseRequest, targetRequest);
        } else {
            //TODO implement the method!
            throw new UnsupportedOperationException("The method need to be implemented!");
        }


    }

    private void wrapRequest(RestHttpRequest baseRequest, final HttpRequestFacade targetRequest) {
        baseRequest.headers().forEach((header) -> targetRequest.addHeader(header.getKey(), header.getValue()));
        targetRequest.maxRetries(baseRequest.maxRetries());
        targetRequest.maxRedirects(baseRequest.maxRedirects());
        targetRequest.readTimeout(baseRequest.readTimeout());
        if (!baseRequest.isUseExpectContinue()) {
            targetRequest.disableExpectContinue();
        }
        if (baseRequest.uriEncode()) {
            targetRequest.enableUriEncode();
        }
    }

    private CompletableFuture<HttpResponse> writeBody(RestHttpRequest baseRequest, final HttpRequestFacade targetRequest) {
        Object entity = baseRequest.bodyEntity();
        Type genericType = null;
        if (entity != null) {
            //TODO 获取泛型还有点问题
        }
        SegmentRequest segmentRequest = targetRequest.segment();
        RequestBodyOutputStream requestBodyOutputStream = new RequestBodyOutputStream(segmentRequest);
        bodyProcessor.write(baseRequest.bodyEntity(), genericType, baseRequest.contentType(), baseRequest.headers(), requestBodyOutputStream);
        return segmentRequest.end();
    }


    private final static class RequestBodyOutputStream extends OutputStream {
        private final SegmentRequest segmentRequest;

        RequestBodyOutputStream(SegmentRequest segmentRequest) {
            this.segmentRequest = segmentRequest;
        }

        @Override
        public void write(int b) {
            byte[] oneByte = new byte[1];
            oneByte[0] = (byte) b;
            this.write(oneByte, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            segmentRequest.write(b, off, len);
        }
    }


}
