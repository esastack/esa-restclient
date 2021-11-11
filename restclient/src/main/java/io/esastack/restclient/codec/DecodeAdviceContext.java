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
package io.esastack.restclient.codec;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

/**
 * Context class used by {@link DecodeAdvice} to intercept calls to
 * {@link Decoder#decode}.
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Decoder#decode}
 *
 * @see Decoder
 * @see DecodeAdvice
 */
public interface DecodeAdviceContext extends DecodeChain {

    RestRequest request();

    RestResponse response();

    /**
     * set responseContent,this method is not safe for use by multiple threads
     *
     * @param responseContent responseContent
     */
    void content(ResponseContent<?> responseContent);
}
