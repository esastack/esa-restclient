/*
 * Copyright 2022 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.ext;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface TrafficSplitContext {
    RestRequest request();

    /**
     * Proceed to the next action in the chain.And the final action will proceed next
     * {@link io.esastack.restclient.exec.RestInterceptor}.
     *
     * @return RestResponse
     */
    CompletionStage<RestResponse> next();
}
