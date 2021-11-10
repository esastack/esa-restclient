package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

public class FormURLEncodedEncoder implements Encoder<MultipartBody> {

    @Override
    public RequestContent<MultipartBody> encode(EncodeContext<MultipartBody> encodeContext) throws Exception {
        MediaType contentType = encodeContext.contentType();
        Class<?> type = encodeContext.type();
        if (contentType != null && MediaTypeUtil.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)
                && MultipartBody.class.isAssignableFrom(type)) {
            return RequestContent.of((MultipartBody) encodeContext.entity());
        }

        return encodeContext.next();
    }
}
