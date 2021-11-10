package io.esastack.restclient.codec;

import esa.commons.DateUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;

public interface JsonCodec extends ByteCodec {
    String DEFAULT_DATE_FORMAT = DateUtils.yyyyMMddHHmmss;

    @Override
    default RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception {
        MediaType contentType = encodeContext.contentType();
        if (contentType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return encodeToJson(encodeContext);
        }

        return encodeContext.next();
    }

    RequestContent<byte[]> encodeToJson(EncodeContext<byte[]> encodeContext) throws Exception;

    @Override
    default Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception {
        MediaType contentType = decodeContext.contentType();
        if (contentType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(contentType)) {
            byte[] content = decodeContext.content().value();
            if (content == null) {
                return null;
            }
            return decodeFromJson(decodeContext);
        }
        return decodeContext.next();
    }

    Object decodeFromJson(DecodeContext<byte[]> decodeContext) throws Exception;
}
