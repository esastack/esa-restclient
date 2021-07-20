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
package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

/**
 * This serializer is used to serialize the data to byte[] for http response.
 */
public interface TxSerializer {

    byte[] DELAY_SERIALIZE_IN_NETTY = new byte[0];

    /**
     * serialize the object to byte array
     *
     * @param target target
     * @return byte array
     * @throws Exception error
     */
    byte[] serialize(MediaType mediaType, HttpHeaders headers, Object target) throws Exception;

    final class DelaySerializeInNetty implements TxSerializer {

        public static final DelaySerializeInNetty INSTANCE = new DelaySerializeInNetty();

        @Override
        public byte[] serialize(MediaType mediaType, HttpHeaders headers, Object target) throws Exception {
            return DELAY_SERIALIZE_IN_NETTY;
        }
    }

}
