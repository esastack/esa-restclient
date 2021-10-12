package esa.restclient.codec.impl;

import esa.commons.Checks;
import esa.restclient.ContentType;
import esa.restclient.RequestBodyContent;
import esa.restclient.RestRequest;
import esa.restclient.codec.EncodeAdvice;
import esa.restclient.codec.EncodeContext;
import esa.restclient.codec.Encoder;

public class EncodeContextImpl implements EncodeContext {

    private final RestRequest request;
    private final EncodeAdvice[] advices;
    private int adviceIndex = 0;
    private Object entity;

    public EncodeContextImpl(RestRequest request, Object entity, EncodeAdvice[] advices) {
        this.request = request;
        this.entity = entity;
        this.advices = advices;
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public Object entity() {
        return entity;
    }

    @Override
    public void entity(Object entity) {
        this.entity = entity;
    }

    @Override
    public RequestBodyContent<?> proceed() throws Exception {
        if (advices == null || adviceIndex >= advices.length) {
            ContentType contentType = request.contentType();
            Encoder encoder = getEncoder(request.contentType());
            return encoder.encode(contentType.mediaType(), request.headers(), entity);
        }
        return advices[adviceIndex++].aroundEncode(this);
    }

    private Encoder getEncoder(ContentType contentType) {
        Checks.checkNotNull(contentType, "contentType");
        return contentType.encoder();
    }

}
