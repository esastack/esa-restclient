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
package esa.restclient.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

public class FastJsonCodec implements JsonCodec {

    static {
        //global date format
        JSON.DEFFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteDateUseDateFormat.getMask();
    }

    @Override
    public byte[] encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        return JSON.toJSONBytes(entity);
    }

    @Override
    public <T> T decode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) {
        return JSON.parseObject(data, type);
    }
}
