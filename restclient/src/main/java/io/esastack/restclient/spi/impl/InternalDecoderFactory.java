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
package io.esastack.restclient.spi.impl;

import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.JacksonCodec;
import io.esastack.restclient.codec.impl.StringCodec;
import io.esastack.restclient.spi.DecoderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InternalDecoderFactory implements DecoderFactory {

    @Override
    public Collection<Decoder> decoders(RestClientOptions clientOptions) {
        List<Decoder> decoders = new ArrayList<>();
        decoders.add(new ByteToByteCodec());
        decoders.add(new JacksonCodec());
        decoders.add(new StringCodec());
        return decoders;
    }
}
