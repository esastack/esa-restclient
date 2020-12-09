/*
 * Copyright 2020 OPPO ESA Stack Project
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
package esa.httpclient.core;

public interface PlainRequest extends HttpRequest {

    /**
     * Obtains request's body as byte[] format
     *
     * @return data
     */
    byte[] body();

    /**
     * {@link HttpRequest} as {@link RequestType#PLAIN}
     *
     * @return type
     */
    @Override
    default RequestType type() {
        return RequestType.PLAIN;
    }
}
