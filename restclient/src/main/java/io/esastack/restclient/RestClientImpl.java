/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.metrics.CallbackExecutorMetric;
import io.esastack.httpclient.core.metrics.ConnectionPoolMetricProvider;
import io.esastack.httpclient.core.metrics.IoThreadGroupMetric;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.exec.RestRequestExecutorImpl;

import java.io.IOException;

public class RestClientImpl implements RestClient {

    private final RestRequestExecutor requestExecutor;
    private final HttpClient httpClient;
    private final RestClientOptions clientOptions;

    RestClientImpl(RestClientOptions clientOptions, HttpClient httpClient) {
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(httpClient, "httpClient");
        this.clientOptions = clientOptions;
        this.httpClient = httpClient;
        this.requestExecutor = new RestRequestExecutorImpl(clientOptions);
    }

    @Override
    public ExecutableRestRequest get(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.get(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.post(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade delete(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.delete(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade put(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.put(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest head(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.head(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest options(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient
                .options(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientOptions;
    }

    @Override
    public ConnectionPoolMetricProvider connectionPoolMetric() {
        return httpClient.connectionPoolMetric();
    }

    @Override
    public IoThreadGroupMetric ioThreadsMetric() {
        return httpClient.ioThreadsMetric();
    }

    @Override
    public CallbackExecutorMetric callbackExecutorMetric() {
        return httpClient.callbackExecutorMetric();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
