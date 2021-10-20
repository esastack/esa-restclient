package io.esastack.restclient.codec;

import esa.commons.DateUtils;

public interface JsonCodec extends ByteCodec {
    String DEFAULT_DATE_FORMAT = DateUtils.yyyyMMddHHmmss;
}
