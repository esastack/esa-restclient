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
package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

public class FormURLEncodedEncoder implements Encoder {

    @Override
    public RequestContent<?> encode(EncodeContext<?> ctx) throws Exception {
        MediaType contentType = ctx.contentType();
        Class<?> type = ctx.entityType();
        if (contentType != null && MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)
                && MultipartBody.class.isAssignableFrom(type)) {
            MultipartBody formBody = (MultipartBody) ctx.entity();
            formBody.multipartEncode(false);
            return RequestContent.of(formBody);
        }

        return ctx.next();
    }
}
