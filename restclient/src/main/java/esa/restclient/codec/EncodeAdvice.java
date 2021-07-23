package esa.restclient.codec;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RequestBodyContent;

public interface EncodeAdvice extends Ordered {

    RequestBodyContent<?> aroundEncode(EncodeContext encodeContext) throws Exception;
}
