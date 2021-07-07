package esa.restclient;

import esa.restclient.serializer.RxSerializer;

public class AcceptType {

    private final MediaType mediaType;
    private final RxSerializer rxSerializer;
    private final boolean ignoreResponseTypeMismatch;

    public AcceptType(MediaType mediaType, RxSerializer rxSerializer) {
        this.mediaType = mediaType;
        this.rxSerializer = rxSerializer;
        this.ignoreResponseTypeMismatch = false;
    }

    public AcceptType(MediaType mediaType, RxSerializer rxSerializer, boolean ignoreResponseTypeMismatch) {
        this.mediaType = mediaType;
        this.rxSerializer = rxSerializer;
        this.ignoreResponseTypeMismatch = ignoreResponseTypeMismatch;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public RxSerializer getRxSerializer() {
        return rxSerializer;
    }

    public boolean isIgnoreResponseTypeMismatch() {
        return ignoreResponseTypeMismatch;
    }
}
