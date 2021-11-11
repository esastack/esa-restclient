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

import io.esastack.httpclient.core.util.Ordered;

/**
 * Interface for encode advice that wrap around calls to {@link Encoder#encode}
 *
 * @see Encoder
 */
public interface EncodeAdvice extends Ordered {

    /**
     * Method wrapping calls to {@link Encoder#encode} method.
     * <p>
     * The parameters of the wrapped method called are available from context. Implementations
     * of this method SHOULD explicitly call {@link EncodeAdviceContext#next()} to invoke the
     * next <code>EncodeAdvice</code> in the chain, and ultimately the wrapped {@link
     * Encoder#encode} method.
     *
     * @param context encode invocation context
     * @return encoded requestContent
     * @throws Exception error
     * @see Encoder
     * @see EncodeAdviceContext
     */
    RequestContent<?> aroundEncode(EncodeAdviceContext context) throws Exception;
}
