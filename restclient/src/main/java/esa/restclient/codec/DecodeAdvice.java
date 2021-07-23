package esa.restclient.codec;

import esa.httpclient.core.util.Ordered;

public interface DecodeAdvice extends Ordered {

    Object aroundDecode(DecodeContext decodeContext);
}
